/*
 * Copyright 2010-2013 The Advance EU 7th Framework project consortium
 *
 * This file is part of Advance.
 *
 * Advance is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of
 * the License, or (at your option) any later version.
 *
 * Advance is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with Advance.  If not, see
 * <http://www.gnu.org/licenses/>.
 *
 */
package eu.advance.logistics.live.reporter.db;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;

import org.joda.time.DateMidnight;
import org.joda.time.DateTime;
import org.joda.time.DateTimeConstants;
import org.joda.time.LocalTime;
import org.joda.time.ReadableDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import eu.advance.logistics.live.reporter.model.ConsignmentSummary;
import eu.advance.logistics.live.reporter.model.ItemEventType;
import eu.advance.logistics.live.reporter.model.ItemStatus;
import eu.advance.logistics.live.reporter.model.ItemSummary;
import eu.advance.logistics.live.reporter.model.DeclaredProgress;
import eu.advance.logistics.live.reporter.model.QueryTimeRange;
import eu.advance.logistics.live.reporter.model.ServiceLevel;
import eu.advance.logistics.live.reporter.model.UOM;
import gnu.trove.TLongCollection;
import gnu.trove.map.TLongDoubleMap;
import gnu.trove.map.TLongObjectMap;
import gnu.trove.map.hash.TLongDoubleHashMap;
import gnu.trove.map.hash.TLongObjectHashMap;
import gnu.trove.set.TLongSet;
import gnu.trove.set.hash.TLongHashSet;
import hu.akarnokd.reactive4java.base.Func1;
import hu.akarnokd.utils.collection.AggregatorHashMap1;
import hu.akarnokd.utils.collection.AggregatorMap1;
import hu.akarnokd.utils.database.DB;
import hu.akarnokd.utils.database.SQLInvoke;
import hu.akarnokd.utils.trove.TroveUtils;

/**
 * Consignment record management routines.
 * @author karnokd, 2013.09.24.
 */
public final class ConsignmentDB {
	/** Database class. */
	private ConsignmentDB() { }
	/** The logger. */
	private static final Logger LOG = LoggerFactory.getLogger(ConsignmentDB.class);
	/**
	 * How many complete work days to look back.
	 */
	public static final int WORKDAYS = 2;
	/** When the shift starts. */
	public static final int SHIFT_START_HOUR = 6;
	/**
	 * Compute the query time range based on default number of workdays and shift starting hour.
	 * @param now the current time
	 * @param workDays the number of work days
	 * @return the range
	 */
	public static QueryTimeRange computeRange(ReadableDateTime now, int workDays) {
		QueryTimeRange result = new QueryTimeRange();
		
		result.now = now.toDateTime();
		
		// determine the range start
		
		if (now.getHourOfDay() >= SHIFT_START_HOUR) {
			result.today = result.now.withHourOfDay(SHIFT_START_HOUR).withMinuteOfHour(0).withSecondOfMinute(0).withMillisOfSecond(0);
			result.start = result.now.minusDays(workDays).withHourOfDay(SHIFT_START_HOUR).withMinuteOfHour(0).withSecondOfMinute(0).withMillisOfSecond(0);
		} else {
			result.today = result.now.minusDays(1).withHourOfDay(SHIFT_START_HOUR).withMinuteOfHour(0).withSecondOfMinute(0).withMillisOfSecond(0);
			result.start = result.now.minusDays(workDays + 1).withHourOfDay(SHIFT_START_HOUR).withMinuteOfHour(0).withSecondOfMinute(0).withMillisOfSecond(0);
		}

		Set<DateMidnight> holidays = MasterDB.holidays();
		while (true) {
			if (holidays.contains(result.start.toDateMidnight())) {
				result.start = result.start.minusDays(1);
			} else
			if (result.start.getDayOfWeek() == DateTimeConstants.SATURDAY) {
				result.start = result.start.minusDays(1);
			} else
			if (result.start.getDayOfWeek() == DateTimeConstants.SUNDAY) {
				result.start = result.start.minusDays(2);
			} else {
				break;
			}
		}
		
		
		return result;
	}
	/**
	 * Aggregates the consignment status up to the given time.
	 * @param hub the target hub
	 * @param sourceDepots the set of source depots, may be null
	 * @param destinationDepots the set of destination depots, may be null
	 * @param when the time to aggregate up to
	 * @return the map from tracking id to consignment summary records
	 */
	public static TLongObjectMap<ConsignmentSummary> consignmentStatus(
			final long hub, 
			@Nullable final TLongCollection sourceDepots,
			@Nullable final TLongCollection destinationDepots,
			@NonNull ReadableDateTime when) {
		return consignmentStatus(hub, sourceDepots, destinationDepots, when, WORKDAYS);
	}
	/**
	 * Aggregates the consignment status up to the given time.
	 * @param hub the target hub
	 * @param sourceDepots the set of source depots, may be null
	 * @param destinationDepots the set of destination depots, may be null
	 * @param when the time to aggregate up to
	 * @param workDays the number of working days to look back
	 * @return the map from tracking id to consignment summary records
	 */
	public static TLongObjectMap<ConsignmentSummary> consignmentStatus(
			final long hub, 
			@Nullable final TLongCollection sourceDepots,
			@Nullable final TLongCollection destinationDepots,
			@NonNull ReadableDateTime when,
			int workDays) {
		final QueryTimeRange range = computeRange(when, workDays);
		
		final TLongObjectMap<ConsignmentSummary> consignments = new TLongObjectHashMap<>();
		try (DB db = DB.connect()) {
			
			// query consignments
			
			StringBuilder sql = new StringBuilder();
			
			sql.append("SELECT ")
			.append("id, created, declared, collection_depot, delivery_depot, service_level, item_count ")
			.append("FROM ")
				.append("consignments ")
			.append("WHERE ")
				.append("created >= ? AND created < ? ")
				.append("AND hub = ? ");
			
			StringBuilder depotFilter = new StringBuilder();
			if (!TroveUtils.isNullOrEmpty(sourceDepots)
					|| !TroveUtils.isNullOrEmpty(destinationDepots)) {
				depotFilter.append("AND (");

				int p = 0;
				if (!TroveUtils.isNullOrEmpty(sourceDepots)) {
					depotFilter.append("collection_depot IN (");
					TroveUtils.join(sourceDepots, ",", depotFilter);
					depotFilter.append(") ");
					p++;
				}
				if (!TroveUtils.isNullOrEmpty(destinationDepots)) {
					if (p > 0) {
						depotFilter.append(" OR ");
					}
					depotFilter.append("delivery_depot IN (");
					TroveUtils.join(destinationDepots, ",", depotFilter);
					depotFilter.append(") ");
					p++;
				}
				
				depotFilter.append(") ");
			}
			sql.append(depotFilter);
			
			db.queryReadOnly(sql, new SQLInvoke() {
				@Override
				public void invoke(ResultSet t) throws SQLException {
					ConsignmentSummary s = new ConsignmentSummary();
					s.id = t.getLong(1);
					s.created = DB.getDateTime(t, 2);
					s.declared = DB.getDateTime(t, 3);
					s.collectionDepot = t.getLong(4);
					s.deliveryDepot = t.getLong(5);
					s.level = ServiceLevel.values()[t.getInt(6)];
					s.itemCount = t.getInt(7);
					
					consignments.put(s.id, s);
				}
			}, range.start, range.now, hub);
			
			final TLongObjectMap<ItemSummary> items = new TLongObjectHashMap<>();
			
			// query consignment items
			
			for (TLongCollection cids : TroveUtils.split(consignments.keySet(), 1000)) {
				sql.setLength(0);
				
				sql.append("SELECT ")
				.append("id, consignment_id, width, height, length ")
				.append("FROM items ")
				.append("WHERE consignment_id IN (");
				TroveUtils.join(cids, ",", sql);
				sql.append(")");
				
				db.queryReadOnly(sql, new SQLInvoke() {
					@Override
					public void invoke(ResultSet t) throws SQLException {
						long id = t.getLong(1);
						long cid = t.getLong(2);
						ConsignmentSummary s = consignments.get(cid);
						if (s == null) {
							LOG.error("Missing ConsignmentSummary? " + cid + " for " + id);
							return;
						}
						ItemSummary is = new ItemSummary();
						is.id = id;
						is.consignmentId = cid;
						is.width = t.getDouble(3);
						is.height = t.getDouble(4);
						is.length = t.getDouble(5);
						is.status = ItemStatus.CREATED;
						
						s.add(is);
						items.put(is.id, is);
					}
				});
			}
			
			// query item states
			
			sql.setLength(0);
			sql.append("SELECT item_id, event_timestamp, event_type ")
			.append("FROM events ")
			.append("WHERE event_timestamp >= ? AND event_timestamp < ? ")
			.append("ORDER BY event_timestamp ");
			
			db.queryReadOnly(sql, new SQLInvoke() {
				@Override
				public void invoke(ResultSet t) throws SQLException {
					long id = t.getLong(1);
					ItemSummary is = items.get(id);
					if (is != null) {
						DateTime dt = DB.getDateTime(t, 2);
						ItemEventType et = ItemEventType.values()[t.getInt(3)];
						
						switch (et) {
						case DECLARED:
							is.updateStatus(ItemStatus.DECLARED);
							break;
						case SOURCE_SCAN:
							is.updateStatus(ItemStatus.SCANNED);
							break;
						case HUB_ARRIVE:
						case SCAN_OFF:
						case WAREHOUSE_ENTER:
							is.updateStatus(ItemStatus.AT_HUB);
							break;
						case HUB_LEAVE:
						case DESTINATION_SCAN:
							if (dt.compareTo(range.today) < 0) {
								is.updateStatus(ItemStatus.LEFT_HUB);
							} else {
								is.updateStatus(ItemStatus.LEFT_HUB_TODAY);
							}
							break;
						default:
						}
					}
				}
			}, range.start, range.now);
			
		} catch (SQLException | IOException ex) {
			LOG.error(ex.toString(), ex);
		}

		return consignments;
	}
	/**
	 * Returns the floorspace of each individual item.
	 * @param itemIds the item identifier collection
	 * @return the map from item id to floorspace
	 */
	public static TLongDoubleMap itemFloorspace(TLongCollection itemIds) {
		final TLongDoubleMap result = new TLongDoubleHashMap();
		try (DB db = DB.connect()) {
			for (TLongCollection iids : TroveUtils.split(itemIds, 1000)) {
				StringBuilder sql = new StringBuilder();
				sql.append("SELECT id, width * length ")
				.append("FROM items ")
				.append("WHERE id IN (");
				TroveUtils.join(iids, ",", sql);
				sql
				.append(")");
				
				db.queryReadOnly(sql, new SQLInvoke() {
					@Override
					public void invoke(ResultSet t) throws SQLException {
						result.put(t.getLong(1), t.getDouble(2));
					}
				});
			}
			
		} catch (SQLException | IOException ex) {
			LOG.error(ex.toString(), ex);
		}
		return result;
	}
	/**
	 * Returns a set of items of a depot placed at the given hub+warehouse location
	 * at the given time.
	 * @param hub the target hub
	 * @param warehouse the warehouse name
	 * @param time the time
	 * @param depotItemsMap the map from depot to set of items ids
	 * @param exec the executor service to run
	 */
	public static void itemsInWarehouse(
			final long hub, 
			@NonNull final String warehouse, 
			@NonNull final ReadableDateTime time, 
			@NonNull final TLongObjectMap<EnumMap<ServiceLevel, TLongSet>> depotItemsMap,
			@NonNull ExecutorService exec) {
		// how many working days to consider before the current day
		final QueryTimeRange range = computeRange(time, WORKDAYS); 

		StringBuilder sql = new StringBuilder();
		sql.append("SELECT ")
			.append("consignment_id, item_id, scan_type ")
		.append("FROM scans ")
		.append("WHERE ")
			.append("scan_timestamp >= ? AND scan_timestamp < ? ")
			.append("AND scan_type IN (1, 2, 3, 4)") // manual scan load+unload, automatic scan in+out
			.append("AND location = ? ")
		.append("ORDER BY ")
			.append("scan_timestamp ");

		try (DB db = DB.connect()) {
			
			// items at the hub
			final TLongObjectMap<TLongSet> consignmentItems = new TLongObjectHashMap<>();
			
			db.queryReadOnly(sql, new SQLInvoke() {
				@Override
				public void invoke(ResultSet t) throws SQLException {
					long cid = t.getLong(1);
					long id = t.getLong(2);
					int ty = t.getInt(3);
					if (ty == 1 || ty == 3) {
						TLongSet s = consignmentItems.get(cid);
						if (s == null) {
							s = new TLongHashSet();
							consignmentItems.put(cid, s);
						}
						s.add(id);
					} else {
						TLongSet s = consignmentItems.get(cid);
						if (s != null) {
							s.remove(id);
							if (s.isEmpty()) {
								consignmentItems.remove(cid);
							}
						}
					}
				}
			}, range.start, range.now, hub + " " + warehouse);
			
			for (TLongCollection cids : TroveUtils.split(consignmentItems.keySet(), 1000)) {
				StringBuilder sql2 = new StringBuilder();
				sql2.append("SELECT id, service_level, delivery_depot ")
				.append("FROM consignments ")
				.append("WHERE id IN (");
				TroveUtils.join(cids, ",", sql2);
				sql2.append(")");
				
				db.queryReadOnly(sql2, new SQLInvoke() {
					@Override
					public void invoke(ResultSet t) throws SQLException {
						long cid = t.getLong(1);
						ServiceLevel sl = ServiceLevel.values()[t.getInt(2)];
						long dd = t.getLong(3);
						
						EnumMap<ServiceLevel, TLongSet> perDepot = depotItemsMap.get(dd);
						if (perDepot == null) {
							perDepot = new EnumMap<>(ServiceLevel.class);
							depotItemsMap.put(dd, perDepot);
						}
						
						TLongSet ids = perDepot.get(sl);
						if (ids == null) {
							ids = new TLongHashSet();
							perDepot.put(sl, ids);
						}
						TLongSet ids2 = consignmentItems.get(cid);
						if (ids2 != null) {
							ids.addAll(ids2);
						}
					}
				});
			}
		} catch (SQLException | IOException ex) {
			LOG.error(ex.toString(), ex);
		}
	}
	/**
	 * Retrieves the consignments declared in the current shift.
	 * @param hub the target hub
	 * @param depotIds the optional set of depots
	 * @param now the current time
	 * @param minutes the quantization minutes
	 * @return the list of declared items
	 */
	public static AggregatorMap1<LocalTime, List<DeclaredProgress>> getDeclaredProgress(
			final long hub,
			@Nullable final TLongCollection depotIds,
			final DateTime now,
			final int minutes) {
		final QueryTimeRange range = computeRange(now, WORKDAYS);
		
		final AggregatorMap1<LocalTime, List<DeclaredProgress>> map = new AggregatorHashMap1<>(new Func1<Object, List<DeclaredProgress>>() {
			@Override
			public List<DeclaredProgress> invoke(Object param1) {
				return new ArrayList<>();
			}
		});
		final List<LocalTime> timepoints = new ArrayList<>();
		for (int i = 0; i < 24 * 60; i += minutes) {
			timepoints.add(new LocalTime(i / 60, i % 60));
		}
		/** Local consignment info. */
		class ConsInfo {
			public DateTime declared;
			public long coll;
			public long deliv;
			public ServiceLevel sl;
		}
		try (DB db = DB.connect()) {
			StringBuilder sql = new StringBuilder();
			sql.append("SELECT ")
				.append("id, declared, collection_depot, delivery_depot, service_level ")
			.append("FROM consignments FORCE INDEX (idx_created) ")
			.append("WHERE ")
				.append("created BETWEEN ? AND ? ")
				.append("AND declared BETWEEN ? AND ? ")
				.append("AND hub = ").append(hub).append(" ");
			
			if (!TroveUtils.isNullOrEmpty(depotIds)) {
				sql.append("AND ((collection_depot IN (");
				TroveUtils.join(depotIds, ",", sql);
				sql.append(")) OR (delivery_depot IN (");
				TroveUtils.join(depotIds, ",", sql);
				sql.append(")))");
			}
			final TLongObjectMap<ConsInfo> consMap = new TLongObjectHashMap<>();
			db.queryReadOnly(sql, new SQLInvoke() {
				@Override
				public void invoke(ResultSet t) throws SQLException {
					ConsInfo ci = new ConsInfo();
					long id = t.getLong(1);
					ci.declared = DB.getDateTime(t, 2);
					ci.coll = t.getLong(3);
					ci.deliv = t.getLong(4);
					ci.sl = ServiceLevel.values()[t.getInt(5)];
					
					consMap.put(id, ci);
				}
			}, range.start, range.now, range.today, range.now);
			
			for (TLongCollection ids : TroveUtils.split(consMap.keySet(), 1000)) {
				sql.setLength(0);
				sql.append("SELECT consignment_id, width, height, length FROM items ")
				.append("WHERE consignment_id IN (");
				TroveUtils.join(ids, ",", sql);
				sql.append(")");
				
				db.queryReadOnly(sql, new SQLInvoke() {
					@Override
					public void invoke(ResultSet t) throws SQLException {
						long cid = t.getLong(1);
						double w = t.getDouble(2);
						double h = t.getDouble(3);
						double ln = t.getDouble(4);
						ConsInfo ci = consMap.get(cid);
						
						DeclaredProgress mp1 = new DeclaredProgress();
						mp1.hub = hub;
						mp1.depot = ci.coll;
						mp1.inbound = true;
						mp1.service = ci.sl;
						mp1.timestamp = ci.declared;
						mp1.add(UOM.PRICEUNIT, w * h * ln);
						mp1.add(UOM.ITEMCOUNT, 1);
						mp1.add(UOM.FLOORSPACE, w * ln);
						
						LocalTime tm = ci.declared.toLocalTime();
						for (LocalTime tm0 : timepoints) {
							if (tm0.compareTo(tm) >= 0) {
								map.get(tm0).add(mp1);
							}
						}
						
						DeclaredProgress mp2 = new DeclaredProgress();
						mp2.hub = hub;
						mp2.depot = ci.deliv;
						mp2.service = ci.sl;
						mp2.timestamp = ci.declared;
						mp2.add(UOM.PRICEUNIT, w * h * ln);
						mp2.add(UOM.ITEMCOUNT, 1);
						mp2.add(UOM.FLOORSPACE, w * ln);
						
						for (LocalTime tm0 : timepoints) {
							if (tm0.compareTo(tm) >= 0) {
								map.get(tm0).add(mp2);
							}
						}
					}
				});
			}
		} catch (IOException | SQLException ex) {
			LOG.error(ex.toString(), ex);
		}
		return map;
	}
}
