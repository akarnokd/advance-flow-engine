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

package eu.advance.logistics.flow.engine.block.prediction;

import hu.akarnokd.reactive4java.base.Action1;
import hu.akarnokd.reactive4java.interactive.Interactive;

import java.text.ParseException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.joda.time.DateTime;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import eu.advance.logistics.flow.engine.runtime.DataResolver;
import eu.advance.logistics.flow.engine.util.Triplet;
import eu.advance.logistics.flow.engine.xml.XElement;

/**
 * Takes a sequence of timestamp-group-value and creates a matrix of values
 * where each row is a group and each column is a timestamp.
 * @author karnokd, 2012.02.27.
 */
public class TimeGroupSeriesMapper {
	/** The set of dates. */
	public final Map<DateTime, Integer> dateset = Maps.newHashMap();
	/** The set of row ids. */
	public final Map<String, Integer> rowset = Maps.newHashMap();
	/** The output data map. */
	public final Map<Integer, Map<Integer, Double>> datamap = Maps.newHashMap();
	/** The sorted date list. */
	public final List<DateTime> dateList = Lists.newArrayList();
	/** The days since the epoch for each index in the datamap. */
	public final List<Integer> daysSinceEpoch = Lists.newArrayList();
	/**
	 * Map the given sequence of timed value group.
	 * @param seq the sequence
	 * @param resolver the resolver
	 */
	public void map(Iterable<XElement> seq, DataResolver<XElement> resolver) {
		for (XElement e : seq) {
			try {
				DateTime dt = new DateTime(resolver.getTimestamp(e.childElement("timestamp")).getTime());

				String group = resolver.getString(e.childElement("group"));
				double v = resolver.getDouble(e.childElement("value"));
				
				Integer ridx = rowset.get(group);
				if (ridx == null) {
					ridx = rowset.size();
					rowset.put(group, ridx);
				}
				
				Map<Integer, Double> row = datamap.get(group);
				if (row == null) {
					row = Maps.newHashMap();
					datamap.put(ridx, row);
				}
				Integer idx = dateset.get(dt);
				if (idx == null) {
					idx = dateset.size();
					dateset.put(dt, idx);
				}
				
				Double d = row.get(idx);
				if (d != null) {
					d += v;
				} else {
					d = v;
				}
				row.put(idx, d);
				
			} catch (ParseException ex) {
				LoggerFactory.getLogger(TimeGroupSeriesMapper.class).error(ex.toString(), ex);
			}
		}
		dateList.addAll(dateset.keySet());
		Collections.sort(dateList);
		Iterables.addAll(daysSinceEpoch, Interactive.repeat(0, dateset.size()));
		for (DateTime dt : dateList) {
			Integer idx = dateset.get(dt);
			daysSinceEpoch.set(idx, (int)(dt.getMillis() / (24L * 60 * 60 * 1000)));
		}
	}
	/**
	 * Traverse each existing group and each time slot and emit the value.
	 * @param action the action to invoke
	 */
	public void createMatrix(Action1<Triplet<Integer, Integer, Double>> action) {
		for (Map.Entry<Integer, Map<Integer, Double>> re : datamap.entrySet()) {
			for (DateTime dt : dateList) {
				Integer idx = dateset.get(dt);
				Double v = re.getValue().get(idx);
				if (v != null) {
					action.invoke(Triplet.of(re.getKey(), idx, v));
				}
			}
		}
	}
}
