/*
 * Copyright 2010-2012 The Advance EU 7th Framework project consortium
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

package eu.advance.logistics.flow.engine.util;

import java.util.List;

import com.google.common.collect.Lists;

import edu.umd.cs.findbugs.annotations.NonNull;

/**
 * String utilities.
 * @author karnokd, 2011.06.21.
 */
public final class Strings {
	/**
	 * Utility class.
	 */
	private Strings() {
	}
	/**
	 * Split the given string based on the supplied separator.
	 * Empty string will produce an empty list.
	 * @param s the string to split
	 * @param separator the separator character
	 * @return the list of string pieces
	 */
	@NonNull
	public static List<String> split(@NonNull String s, char separator) {
		List<String> result = Lists.newArrayList();
		if (s.isEmpty()) {
			return result;
		}
		int idx = 0;
		do {
			int idx2 = s.indexOf(separator, idx);
			if (idx2 >= 0) {
				result.add(s.substring(idx, idx2));
				idx = idx2 + 1;
			} else {
				result.add(s.substring(idx));
				break;
			}
		} while (true);
		return result;
	}
	/**
	 * Trim the elements of the given string list.
	 * @param list the list of strings to trim
	 * @return the list itself
	 */
	public static List<String> trim(@NonNull List<String> list) {
		for (int i = 0; i < list.size(); i++) {
			list.set(i, list.get(i).trim());
		}
		return list;
	}
	/**
	 * Joins an iterable sequence of objects by converting them via {@code String.valueOf}.
	 * @param items the sequence of objects
	 * @param separator the separator
	 * @return the combined string
	 */
	public static String join(Iterable<?> items, String separator) {
		StringBuilder b = new StringBuilder();
		for (Object o : items) {
			if (b.length() > 0) {
				b.append(separator);
			}
			b.append(o);
		}
		return b.toString();
	}
}
