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

package eu.advance.logistics.flow.engine.typesystem;

import java.util.ArrayList;
import java.util.List;

/**
 * Tracks the recursion for XType objects for comparison.
 * @author karnokd, 2012.03.05.
 */
public class XTypeRecursionTracker {
	/** The first path of XTypes. */
	final List<XType> xs = new ArrayList<XType>();
	/** The second path of XTypes. */
	final List<XType> ys = new ArrayList<XType>();
	/**
	 * Enter the first path.
	 * @param type the type
	 */
	public void enterFirst(XType type) {
		xs.add(type);
	}
	/**
	 * Enter the second path.
	 * @param type the type
	 */
	public void enterSecond(XType type) {
		ys.add(type);
	}
	/**
	 * Leave the first.
	 */
	public void leaveFirst() {
		xs.remove(xs.size() - 1);
	}
	/**
	 * Leave the second.
	 */
	public void leaveSecond() {
		ys.remove(ys.size() - 1);
	}
	/**
	 * Check the recursive index on first.
	 * @param type the type
	 * @return the index or -1 if not recursive
	 */
	public int indexFirst(XType type) {
		int idx = xs.lastIndexOf(type);
		if (idx < 0) {
			return -1;
		}
		return xs.size() - idx - 1;
	}
	/**
	 * Check the recursive index on second.
	 * @param type the type
	 * @return the index or -1 if not recursive
	 */
	public int indexSecond(XType type) {
		int idx = ys.lastIndexOf(type);
		if (idx < 0) {
			return -1;
		}
		return ys.size() - idx - 1;
	}
}
