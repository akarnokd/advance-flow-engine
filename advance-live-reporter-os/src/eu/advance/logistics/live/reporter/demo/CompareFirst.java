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
package eu.advance.logistics.live.reporter.demo;

import hu.akarnokd.reactive4java.base.Pair;

import java.util.Comparator;

import org.joda.time.DateTime;

/**
 * Compare the first element of a pair.
 * @author karnokd, 2013.10.02.
 */
public class CompareFirst implements Comparator<Pair<DateTime, ?>>  {
	@Override
	public int compare(Pair<DateTime, ?> o1, Pair<DateTime, ?> o2) {
		return o1.first.compareTo(o2.first);
	}
}