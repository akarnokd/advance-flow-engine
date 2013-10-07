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

import gnu.trove.TLongCollection;
import gnu.trove.map.TLongObjectMap;
import gnu.trove.map.TObjectDoubleMap;
import gnu.trove.map.hash.TLongObjectHashMap;
import gnu.trove.map.hash.TObjectDoubleHashMap;
import hu.akarnokd.utils.database.DB;
import hu.akarnokd.utils.database.SQLInvoke;
import hu.akarnokd.utils.trove.TroveUtils;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nullable;

import org.joda.time.DateMidnight;
import org.joda.time.DateTime;
import org.joda.time.DateTimeConstants;
import org.joda.time.ReadableDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.advance.logistics.live.reporter.model.ArxPrediction;
import eu.advance.logistics.live.reporter.model.ArxPredictionKey;
import eu.advance.logistics.live.reporter.model.Consignment;
import eu.advance.logistics.live.reporter.model.ServiceLevel;
import eu.advance.logistics.live.reporter.model.UOM;

/**
 * Manages the prediction classes.
 * @author karnokd, 2013.09.24.
 */
public final class PredictionDB {
	/** The logger. */
	private static final Logger LOG = LoggerFactory.getLogger(PredictionDB.class);
	/** Database class. */
	private PredictionDB() { }
	/**
	 * Returns the ARX predictions for the given hub,
	 * optionally for a depot, from a day up to
	 * the given number of days.
	 * @param hub the target hub
	 * @param depot the optional depot
	 * @param forDay the start of the target day
	 * @param horizon the number of days
	 * @param out the output map
	 */
	public static void getArxPredictions(
			long hub, 
			@Nullable Long depot,
			DateMidnight forDay,
			int horizon,
			final Map<ArxPredictionKey, List<ArxPrediction>> out) {
		
		DateMidnight day2 = forDay;
		Set<DateMidnight> hs = MasterDB.holidays();
		while (horizon > 1) {
			day2 = day2.plusDays(1);
			if (!hs.contains(day2)
					&& day2.getDayOfWeek() != DateTimeConstants.SATURDAY
					&& day2.getDayOfWeek() != DateTimeConstants.SUNDAY) {
				horizon--;
			}
		}
		final Map<ArxPredictionKey, DateTime> lastPrediction = new HashMap<>();
		try (DB db = DB.connect()) {
			List<Object> params = new ArrayList<>();
			StringBuilder sql = new StringBuilder();
			
			sql.append("SELECT ")
				.append("hub, depot, day_run, day_predict, service_level, inbound, unit, value ")
			.append("FROM ")
				.append("arx_predictions ")
			.append("WHERE ")
				.append("hub = ? ")
				.append("AND day_predict BETWEEN ? AND ? ")
				.append("AND day_run < ?");
			params.add(hub);
			params.add(forDay);
			params.add(day2);
			params.add(forDay.plusDays(1));
			
			if (depot != null) {
				sql.append("AND depot = ? ");
				params.add(depot);
			}
			
			db.queryReadOnly(sql, new SQLInvoke() {
				@Override
				public void invoke(ResultSet t) throws SQLException {
					ArxPrediction p = new ArxPrediction();
					p.hub = t.getLong(1);
					p.depot = t.getLong(2);
					p.dayRun = DB.getDateTime(t, 3);
					p.dayPredict = DB.getDay(t, 4);
					p.service = ServiceLevel.values()[t.getInt(5)];
					p.inbound = t.getBoolean(6);
					p.unit = UOM.values()[t.getInt(7)];
					
					p.value = t.getDouble(8);
					
					ArxPredictionKey key = p.key();
					
					// aggregate only the values with the latest run
					DateTime last = lastPrediction.get(key);
					if (last == null || last.compareTo(p.dayRun) < 0) {
						lastPrediction.put(key, p.dayRun);
						out.remove(key);
						last = p.dayRun;
					}
					if (last.equals(p.dayRun)) {
						List<ArxPrediction> list = out.get(key);
						if (list == null) {
							list = new ArrayList<>();
							out.put(key, list);
						}
						list.add(p);
					}
				}
			}, params);
		} catch (IOException | SQLException ex) {
			ex.printStackTrace();
		}
		
	}
	/**
	 * Delete the ARX predictions based on the given settings.
	 * @param hub the target hub
	 * @param depot the target depot if not null
	 * @param from the start of the prediction date, if not null, inclusive
	 * @param to the end of the prediction date, if not null, exclusive
	 */
	public static void deleteARXPredictions(long hub, @Nullable Long depot, 
			@Nullable ReadableDateTime from, @Nullable ReadableDateTime to) {
		try (DB db = DB.connect()) {
			StringBuilder sql = new StringBuilder();
			List<Object> params = new ArrayList<>();
			
			sql.append("DELETE FROM arx_predictions WHERE hub = ? ");
			params.add(hub);
			if (depot != null) {
				sql.append("AND depot = ? ");
				params.add(depot);
			}
			if (from != null) {
				sql.append("AND day_predict >= ? ");
				params.add(from);
			}
			if (to != null) {
				sql.append("AND day_predict < ? ");
				params.add(to);
			}
			
			db.update(sql, params);
			db.commit();
		} catch (IOException | SQLException ex) {
			ex.printStackTrace();
		}
	}
	/**
	 * Returns the daily manifests for a given time range, grouped by
	 * service level, depot.
	 * @param hub the target hub
	 * @param start the start date, used for entered
	 * @param end the end date, used for entered
	 * @param shiftStartHour the shift start hour
	 * @param inbound the map of inbound manifested values from depots
	 * @param outbound the map of outbound manifested values to depots
	 */
	public static void dailyManifests(
			final long hub, 
			final ReadableDateTime start, 
			final ReadableDateTime end, 
			final int shiftStartHour,
			final Map<ArxPredictionKey, TObjectDoubleMap<DateMidnight>> inbound,
			final Map<ArxPredictionKey, TObjectDoubleMap<DateMidnight>> outbound) {
		try (DB db = DB.connect()) {
			StringBuilder sql = new StringBuilder();
			sql
			.append("SELECT ")
				.append("id, declared, collection_depot, delivery_depot, service_level ")
			.append("FROM ")
				.append("consignments ")
			.append("WHERE ")
				.append("hub = ? ")
				.append("AND created >= ? AND created < ? ")
				.append("AND declared IS NOT NULL ");
			
			final TLongObjectMap<Consignment> cs = new TLongObjectHashMap<>();
			
			db.queryReadOnly(sql, new SQLInvoke() {
				@Override
				public void invoke(ResultSet t) throws SQLException {
					Consignment c = new Consignment();
					c.id = t.getLong(1);
					c.declared = DB.getDateTime(t, 2);
					c.collectionDepot = t.getLong(3);
					c.deliveryDepot = t.getLong(4);
					c.service = ServiceLevel.values()[t.getInt(5)];
					
					cs.put(c.id, c);
				}
			}, hub, start, end);
			
			for (TLongCollection ci : TroveUtils.split(cs.keySet(), 1000)) {
				sql.setLength(0);
				sql.append("SELECT ")
					.append(" consignment_id, width, height, length ")
				.append("FROM ")
					.append("items ")
				.append("WHERE ")
					.append("consignment_id IN (");
				TroveUtils.join(ci, ",", sql);
				sql.append(")");
				
				db.queryReadOnly(sql, new SQLInvoke() {
					@Override
					public void invoke(ResultSet t) throws SQLException {
						Consignment c = cs.get(t.getLong(1));
						
						double w = t.getDouble(2);
						double h = t.getDouble(3);
						double l = t.getDouble(4);
						
						double f = w * l;
						double p = w * l * h;
						
						DateMidnight dm = c.declared.toDateMidnight();
						if (c.declared.getHourOfDay() < shiftStartHour) {
							dm = dm.minusDays(1);
						}
						
						add(true, c.service, UOM.ITEMCOUNT, c.collectionDepot, dm, 1);
						add(false, c.service, UOM.ITEMCOUNT, c.deliveryDepot, dm, 1);

						add(true, c.service, UOM.PRICEUNIT, c.collectionDepot, dm, p);
						add(false, c.service, UOM.PRICEUNIT, c.deliveryDepot, dm, p);

						add(true, c.service, UOM.FLOORSPACE, c.collectionDepot, dm, f);
						add(false, c.service, UOM.FLOORSPACE, c.deliveryDepot, dm, f);

					}
					/**
					 * Add an entry to the maps.
					 * @param in inbound?
					 * @param service service level
					 * @param uom unit of measure
					 * @param depot depot
					 * @param dm day
					 * @param value value
					 */
					void add(boolean in, ServiceLevel service, UOM uom, long depot, DateMidnight dm, double value) {
						Map<ArxPredictionKey, TObjectDoubleMap<DateMidnight>> map = in ? inbound : outbound;
						ArxPredictionKey key = new ArxPredictionKey(service, uom, depot);
						TObjectDoubleMap<DateMidnight> m1 = map.get(key);
						if (m1 == null) {
							m1 = new TObjectDoubleHashMap<>();
							map.put(key, m1);
						}
						
						m1.adjustOrPutValue(dm, value, value);
					}
				});
			}
			
		} catch (IOException | SQLException ex) {
			LOG.error(ex.toString(), ex);
		}
	}
	/**
	 * Save the ARX prediction records.
	 * @param predictions the predictions
	 */
	public static void saveARXPredictions(List<ArxPrediction> predictions) {
		try (DB db = DB.connect()) {
			try (PreparedStatement pstmt = db.prepare(
					"INSERT INTO arx_predictions VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)")) {
				for (ArxPrediction p : predictions) {
					DB.setParams(pstmt, p.hub, p.depot, p.dayRun, p.dayPredict, p.service.ordinal(), p.inbound, p.unit.ordinal(), p.value, Double.class);
					pstmt.addBatch();
				}
				pstmt.executeBatch();
				db.commit();
			}
		} catch (IOException | SQLException ex) {
			ex.printStackTrace();
		}
	}
}
