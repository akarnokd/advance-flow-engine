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

import hu.akarnokd.utils.database.DB;
import hu.akarnokd.utils.database.SQLInvoke;

import java.io.IOException;
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

import eu.advance.logistics.live.reporter.model.ArxPrediction;
import eu.advance.logistics.live.reporter.model.ArxPredictionKey;
import eu.advance.logistics.live.reporter.model.ServiceLevel;
import eu.advance.logistics.live.reporter.model.UOM;

/**
 * Manages the prediction classes.
 * @author karnokd, 2013.09.24.
 */
public final class PredictionDB {
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
}
