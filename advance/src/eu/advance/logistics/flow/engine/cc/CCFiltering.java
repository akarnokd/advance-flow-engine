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

package eu.advance.logistics.flow.engine.cc;

import java.text.ParseException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;


/**
 * Filter table records based on a filter syntax.
 * @author akarnokd, 2011.10.27.
 */
public final class CCFiltering {
	/** Utility class. */
	private CCFiltering() {
		// utility class
	}
	/**
	 * A filter item. 
	 * @author akarnokd, 2011.10.27.
	 */
	public static class FilterItem {
		/** The filter function name. */
		public String name;
		/** Filter operator. */
		public FilterOp op;
		/** The filter values (or-ed). */
		public final List<String> values = Lists.newArrayList();
		@Override
		public String toString() {
			
			return name + " " + op + " " + values;
		}
	}
	/** The filter operations enum. */
	public enum FilterOp {
		/** Value equals. */
		EQUAL("="),
		/** Greater than. */
		GREATER(">"),
		/** Lower than. */
		LOWER("<"),
		/** Greater or equal. */
		GREATER_OR_EQUAL(">="),
		/** Lower or equal. */
		LOWER_OR_EQUAL("<="),
		/** Inequal. */
		INEQUAL("!="),
		/** Starts with. */
		STARTS_WITH("|="),
		/** Ends with. */
		ENDS_WITH("=|"),
		/** Contains. */
		CONTAINS("||"),
		/** Between. */
		BETWEEN("><"),
		/** Not between. */
		NOT_BETWEEN("<>")
		;
		/** The operator string. */
		@NonNull
		public final String op;
		/**
		 * Operator string to FilterOp mapping.
		 */
		private static final Map<String, FilterOp> MAP = Maps.newHashMap();
		static {
			for (FilterOp o : values()) {
				MAP.put(o.op, o);
			}
		}
		/**
		 * Get a filter op for the op string.
		 * @param op the operator string
		 * @return the filter op or null if not found
		 */
		@Nullable
		public static FilterOp get(@NonNull String op) {
			return MAP.get(op);
		}
		/**
		 * Initialize the operator.
		 * @param op the operator stirng
		 */
		FilterOp(@NonNull String op) {
			this.op = op;
		}
	}
	/**
	 * Parse the text into a filter.
	 * @param text the text to parse
	 * @return the list of filter items
	 * @throws ParseException on error
	 */
	@NonNull
	public static List<FilterItem> parse(@NonNull String text) throws ParseException {
		int idx = 0;
		int idx0 = 0;
		List<FilterItem> result = Lists.newArrayList();
		FilterItem fi = null;
		while (idx < text.length()) {
			char c = text.charAt(idx);
			if (c == '!' || c == '>' || c == '<' || c == '=' || c == '|') {
				if (idx + 1 >= text.length()) {
					throw new ParseException("Input terminates unexpectedly after an operator", idx);
				}
				
				fi = new FilterItem();
				fi.name = text.substring(idx0, idx).trim().toLowerCase();
				result.add(fi);
				char c1 = text.charAt(idx + 1);
				int idx2 = idx + 1;
				if (c1 == '=' || c1 == '>' || c1 == '<' || c == '|') {
					fi.op = FilterOp.get(text.substring(idx, idx + 2));
					idx2++;
				} else {
					fi.op = FilterOp.get(String.valueOf(c));
				}
				if (fi.op == null) {
					throw new ParseException("Unknown operator: " + c + c1, idx);
				}
				// find a non-whitespace character
				while (idx2 < text.length() && Character.isWhitespace(text.charAt(idx2))) {
					idx2++;
				}
				if (idx2 >= text.length()) {
					throw new ParseException("Input terminates unexpectedly before a value", idx2);
				}
				char c2 = text.charAt(idx2);
				if (c2 == '(') {
					// find closing parenthesis
					int idx3 = idx2 + 1;
					while (idx3 < text.length() && text.charAt(idx3) != ')') {
						idx3++;
					}
					if (idx3 >= text.length()) {
						throw new ParseException("Input terminates unexpectedly without closing parenthesis", idx3);
					}
					for (String v : Arrays.asList(text.substring(idx2 + 1, idx3).split(","))) {
						fi.values.add(v.trim());
					}
					
					// find comma
					while (idx3 < text.length() && text.charAt(idx3) != ',') {
						idx3++;
					}
					
					idx = idx3 + 1;
					idx0 = idx;
				} else {
					// find comma
					int idx3 = idx2;
					while (idx3 < text.length() && text.charAt(idx3) != ',') {
						idx3++;
					}
					fi.values.add(text.substring(idx2, idx3));
					idx = idx3 + 1;
					idx0 = idx;
				}
			} else {
				idx++;
			}
		}
		return result;
	}
	/**
	 * Test program.
	 * @param args no arguments
	 * @throws Exception ignored
	 */
	public static void main(String[] args) throws Exception {
		System.out.println(parse("op = value, op <> (value, value), op != ()"));
	}
}
