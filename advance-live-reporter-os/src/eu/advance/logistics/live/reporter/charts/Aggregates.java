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

package eu.advance.logistics.live.reporter.charts;

import java.util.LinkedHashMap;

import edu.umd.cs.findbugs.annotations.NonNull;
import eu.advance.logistics.live.reporter.model.ItemStatus;

/** The value aggregates record. */
public class Aggregates {
	/** Value. */
	public double created;
	/** Value. */
	public double scanned;
	/** Value. */
	public double declared;
	/** Value. */
	public double athub;
	/** Value. */
	public double lefthub;
	/** Value. */
	public double predicted;
	/**
	 * Create a map of properties from the given aggregates object.
	 * @return the map
	 */
	@NonNull
	public LinkedHashMap<ItemStatus, BarData> create() {
		LinkedHashMap<ItemStatus, BarData> p = new LinkedHashMap<>();
		p.put(ItemStatus.LEFT_HUB_TODAY, new BarData(lefthub));
		p.put(ItemStatus.CREATED, new BarData(created));
		p.put(ItemStatus.SCANNED, new BarData(scanned));
		p.put(ItemStatus.DECLARED, new BarData(declared));
		p.put(ItemStatus.AT_HUB, new BarData(athub));
		p.put(ItemStatus.PREDICTED, new BarData(predicted));
		return p;
	}
	/**
	 * Returns a value for a concrete item status.
	 * @param status the item status
	 * @return the value
	 */
	public double value(ItemStatus status) {
		double value = 0;
		switch (status) {
		case AT_HUB:
			value = athub;
			break;
		case CREATED:
			value = created;
			break;
//		case LEFT_HUB:
		case LEFT_HUB_TODAY:
			value = lefthub;
			break;
		case DECLARED:
			value = declared;
			break;
		case PREDICTED:
			value = predicted;
			break;
		case SCANNED:
			value = scanned;
			break;
		default:
		}
		return value;
	}
}