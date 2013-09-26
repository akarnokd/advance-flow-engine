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
import hu.akarnokd.reactive4java.base.Pair;
import hu.akarnokd.utils.database.DB;
import hu.akarnokd.utils.database.SQLInvoke;
import hu.akarnokd.utils.trove.TroveUtils;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.joda.time.DateTime;
import org.joda.time.LocalTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import eu.advance.logistics.live.reporter.model.MlPrediction;
import eu.advance.logistics.live.reporter.model.ServiceLevel;
import eu.advance.logistics.live.reporter.model.UOM;

/**
 * Machine learning database handlers.
 * @author karnokd, 2013.05.27.
 *
 */
public final class MLDB {
	/** The logger. */
	protected static final Logger LOGGER = LoggerFactory.getLogger(MLDB.class);
	/** Database class. */
	private MLDB() { }
	/**
	 * Returns the list of predictions filtered by various parameters.
	 * @param hub the hub
	 * @param depotIds the optional set of depots
	 * @param when the current time
	 * @return the list of predictions
	 */
	public static Map<Long, Pair<LocalTime, List<MlPrediction>>> getMLLatestPredictions(
			final long hub, 
			@Nullable TLongCollection depotIds,
			@NonNull final DateTime when) {
		LOGGER.debug("Loading latest predictions.");
		final Map<Long, Pair<LocalTime, List<MlPrediction>>> result = new HashMap<>();
		try (DB db = DB.connect()) {
			StringBuilder sql = new StringBuilder();
			
			sql.append("SELECT ")
				.append("depot, time_of_day, day_offset, service_level, inbound, unit, current, remaining ")
			.append("FROM ")
				.append("ml_predictions ")
			.append("WHERE hub = ? ")
			.append("AND day = ? ")
			.append("AND time_of_day <= ? ");
			
			if (!TroveUtils.isNullOrEmpty(depotIds)) {
				sql.append("AND depot IN (");
				TroveUtils.join(depotIds, ",", sql);
				sql.append(") ");
			}

			db.queryReadOnly(sql, new SQLInvoke() {
				@Override
				public void invoke(ResultSet t) throws SQLException {
					MlPrediction p = new MlPrediction();
					
					p.hub = hub;
					p.depot = t.getLong(1);
					p.day = when.toDateMidnight();
					p.time = new LocalTime(t.getTime(2));
					p.dayOffset = t.getInt(3);
					p.service = ServiceLevel.values()[t.getInt(4)];
					p.inbound = t.getBoolean(5);
					p.unit = UOM.values()[t.getInt(6)];
					p.current = t.getDouble(7);
					p.remaining = t.getDouble(8);
					
					Pair<LocalTime, List<MlPrediction>> pds = result.get(p.depot);
					if (pds == null || pds.first.compareTo(p.time) < 0) {
						pds = Pair.of(p.time, (List<MlPrediction>)new ArrayList<MlPrediction>());
						result.put(p.depot, pds);
					}
					pds.second.add(p);
				}
			}, hub, when.toDateMidnight(), when.toLocalTime());
			
		} catch (IOException | SQLException ex) {
			LOGGER.error(ex.toString(), ex);
		}		
		return result;
	}
	/**
	 * Retrieve all predictions for a given day.
	 * @param hub the target hub
	 * @param depotIds the optional set of depot ids
	 * @param now the current date and time
	 * @return the list of predictions
	 */
	public static List<MlPrediction> getPredictionsOnDay(
			final Long hub, 
			@Nullable TLongCollection depotIds,
			final DateTime now) {
		final List<MlPrediction> result = new ArrayList<>();
		try (DB db = DB.connect()) {
			StringBuilder sql = new StringBuilder();
			
			sql.append("SELECT ")
				.append("depot, time_of_day, day_offset, service_level, ")
				.append("inbound, unit, current, remaining ")
			.append("FROM ")
				.append("ml_predictions ")
			.append("WHERE ")
			.append("day = ? ")
			.append("AND time_of_day <= ? ");
			if (hub != null) {
				sql.append("AND hub = ").append(hub).append(" ");
			}
			if (!TroveUtils.isNullOrEmpty(depotIds)) {
				sql.append("AND depot IN (");
				TroveUtils.join(depotIds, ",", sql);
				sql.append(") ");
			}
			db.queryReadOnly(sql, 
			new SQLInvoke() {
				@Override
				public void invoke(ResultSet t) throws SQLException {
					MlPrediction p = new MlPrediction();
					
					p.hub = hub != null ? hub : -1;
					p.depot = t.getLong(1);
					p.day = now.toDateMidnight();
					p.time = new LocalTime(t.getTime(2));
					p.dayOffset = t.getInt(3);
					p.service = ServiceLevel.values()[t.getInt(4)];
					p.inbound = t.getBoolean(5);
					p.unit = UOM.values()[t.getInt(6)];
					p.current = t.getDouble(7);
					p.remaining = Math.max(0, t.getDouble(8));
					
					result.add(p);
				}
			}, now.toDateMidnight(), now.toLocalTime());
		} catch (IOException | SQLException ex) {
			LOGGER.error(ex.toString(), ex);
		}		
		return result;
	}
}
