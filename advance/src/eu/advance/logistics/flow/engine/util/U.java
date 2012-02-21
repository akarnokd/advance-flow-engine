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

package eu.advance.logistics.flow.engine.util;

import hu.akarnokd.reactive4java.base.Func2;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import com.google.common.collect.Lists;
import com.google.common.collect.Multiset;
import com.google.common.collect.Multiset.Entry;

/**
 * Utility class.
 * @author karnokd, 2012.01.20.
 */
public final class U {

	/**
	 * Utility class.
	 */
	private U() {
		// TODO Auto-generated constructor stub
	}
	/**
	 * Sort by count descending.
	 * @param <T> the element type
	 * @param set the multiset
	 * @return the result
	 */
	public static <T> List<Multiset.Entry<T>> sortByCount(Multiset<T> set) {
		List<Multiset.Entry<T>> result = Lists.newArrayList(set.entrySet());
		Collections.sort(result, new Comparator<Multiset.Entry<T>>() {
			@Override
			public int compare(Entry<T> o1, Entry<T> o2) {
				int i2 = o2.getCount();
				int i1 = o1.getCount();
				return i2 < i1 ? -1 : (i2 > i1 ? 1 : 0);
			}
		});
		return result;
	}
	/**
	 * Returns an iterable which reads through the supplied buffered reader line by line.
	 * @param in the buffered reader.
	 * @return the iterable sequence
	 */
	public static Iterable<String> lines(final BufferedReader in) {
		return new Iterable<String>() {
			@Override
			public Iterator<String> iterator() {
				return new Iterator<String>() {
					String line;
					boolean checked;
					@Override
					public boolean hasNext() {
						if (!checked) {
							try {
								line = in.readLine();
							} catch (IOException ex) {
								throw new RuntimeException(ex);
							}
							checked = true;
						}
						return line != null;
					}
					@Override
					public String next() {
						if (hasNext()) {
							checked = false;
							return line;
						}
						throw new NoSuchElementException();
					}
					@Override
					public void remove() {
						throw new UnsupportedOperationException();
					}
				};
			}
		};
	}
	/**
	 * Reads a multiline csv row entry ant returns it as a whole.
	 * @param in the input
	 * @return the line or null if no more data
	 * @throws IOException on error
	 */
	public static List<String> csvLine(BufferedReader in) throws IOException {
		List<String> result = null;
		StringBuilder b = new StringBuilder();
		boolean escape = false;
		while (true) {
			int c = in.read();
			if (c < 0) {
				if (b.length() > 0) {
					result.add(b.toString());
				}
				break;
			}
			if (result == null) {
				result = Lists.newArrayList();
			}
			if (c == '"') {
				escape = !escape;
				continue;
			}
			if (c == '\t' && !escape) {
				result.add(b.toString());
				b.setLength(0);
				continue;
			}
			if (c == '\n' && !escape) {
				result.add(b.toString());
				break;
			}
			if (c != '\r') {
				b.append((char)c);
			}
		}
		return result;
	}
	/**
	 * Check if a value is power of 2.
	 * @param value the value
	 * @return true if the value is power of 2
	 */
	public static boolean isPower2(int value) {
		return value != 0 && ((value & (value - 1)) == 0);
	}
	/**
	 * If the source has a size as power of 2 returns the source,
	 * otherwise, it creates a new array with the size to the next power 2 value.
	 * @param source the source array
	 * @return an array with power2 length
	 */
	public static int[] padded(int[] source) {
		if (isPower2(source.length)) {
			return source;
		}
		int n2 = Integer.highestOneBit(source.length) * 2;
		return Arrays.copyOf(source, n2);
	}
	/**
	 * If the source has a size as power of 2 returns the source,
	 * otherwise, it creates a new array with the size to the next power 2 value.
	 * @param source the source array
	 * @return an array with power2 length
	 */
	public static double[] padded(double[] source) {
		if (isPower2(source.length)) {
			return source;
		}
		int n2 = Integer.highestOneBit(source.length) * 2;
		return Arrays.copyOf(source, n2);
	}
	/**
	 * Converts the source array of booleans into an array of 0s and 1s.
	 * @param source the source array
	 * @return the double array
	 */
	public static double[] convertToDouble(boolean... source) {
		double[] result = new double[source.length];
		for (int i = 0; i < source.length; i++) {
			result[i] = source[i] ? 1 : 0;
		}
		return result;
	}
	/**
	 * Converts the source array of primitive into an array of doubles.
	 * @param source the source array
	 * @return the double array output
	 */
	public static double[] convertToDouble(byte... source) {
		double[] result = new double[source.length];
		for (int i = 0; i < source.length; i++) {
			result[i] = source[i];
		}
		return result;
	}
	/**
	 * Converts the source array of primitive into an array of doubles.
	 * @param source the source array
	 * @return the double array output
	 */
	public static double[] convertToDouble(short... source) {
		double[] result = new double[source.length];
		for (int i = 0; i < source.length; i++) {
			result[i] = source[i];
		}
		return result;
	}
	/**
	 * Converts the source array of primitive into an array of doubles.
	 * @param source the source array
	 * @return the double array output
	 */
	public static double[] convertToDouble(char... source) {
		double[] result = new double[source.length];
		for (int i = 0; i < source.length; i++) {
			result[i] = source[i];
		}
		return result;
	}
	/**
	 * Converts the source array of primitive into an array of doubles.
	 * @param source the source array
	 * @return the double array output
	 */
	public static double[] convertToDouble(int... source) {
		double[] result = new double[source.length];
		for (int i = 0; i < source.length; i++) {
			result[i] = source[i];
		}
		return result;
	}
	/**
	 * Converts the source array of primitive into an array of doubles.
	 * @param source the source array
	 * @return the double array output
	 */
	public static double[] convertToDouble(long... source) {
		double[] result = new double[source.length];
		for (int i = 0; i < source.length; i++) {
			result[i] = source[i];
		}
		return result;
	}
	/**
	 * Converts the source array of primitive into an array of doubles.
	 * @param source the source array
	 * @return the double array output
	 */
	public static double[] convertToDouble(float... source) {
		double[] result = new double[source.length];
		for (int i = 0; i < source.length; i++) {
			result[i] = source[i];
		}
		return result;
	}
	/**
	 * Convert a collection of some numbers into an array of doubles.
	 * @param source the source collection
	 * @return the double array
	 */
	public static double[] convertToDouble(Iterable<? extends Number> source) {
		if (source instanceof Collection) {
			int len = ((Collection<?>)source).size();
			double[] result = new double[len];
			int i = 0;
			for (Number n : source) {
				result[i] = n.doubleValue();
				i++;
			}
			return result;
		}
		int len = 0;
		double[] result = new double[16];
		for (Number n : source) {
			if (len == result.length) {
				result = Arrays.copyOf(result, len + (len >> 1));
			}
			result[len] = n.doubleValue();
			len++;
		}
		if (len != result.length) {
			return Arrays.copyOf(result, len);
		}
		return result;
	}
	/**
	 * Simply returns the varargs parameter.
	 * @param ds the elements
	 * @return same as ds
	 */
	public static double[] doubles(double... ds) {
		return ds;
	}
	/**
	 * Simply returns the varargs parameter.
	 * @param ds the elements
	 * @return same as ds
	 */
	public static int[] ints(int... ds) {
		return ds;
	}
	/**
	 * Creates a single dimensional array from the sub-dimensions of the source array.
	 * @param src the source
	 * @return the sequentialized array
	 */
	public static double[] sequential(double[][] src) {
		int n = 0;
		for (int i = 0; i < src.length; i++) {
			n += src[i].length;
		}
		double[] result = new double[n];
		int dst = 0;
		for (int i = 0; i < src.length; i++) {
			System.arraycopy(src[i], 0, result, dst, src[i].length);
			dst += src[i].length;
		}
		return result;
	}
	/**
	 * Create a matrix of values with the given column count.
	 * @param cols the column count
	 * @param values the values to add to the matrix
	 * @return the matrix
	 */
	public static double[][] matrix(int cols, double... values) {
		int n = values.length;
		int r = n / cols;
		double[][] result = new double[r][cols];
		int src = 0;
		for (int i = 0; i < r; i++) {
			int len = Math.min(cols, values.length - src);
			System.arraycopy(values, src, result[i], 0, len);
			src += len;
		}
		return result;
	}
	/**
	 * Compute the matrix multiple of two matrices.
	 * @param first the fist matrix with [n][m]
	 * @param second the second matrix with [m][k]
	 * @return the output matrix with [n][k]
	 */
	public static double[][] multiply(double[][] first, double[][] second) {
		final int n = first.length;
		final int m = first[0].length;
		final int k = second[0].length;
		final double[][] result = new double[n][k];
		
		for (int i = 0; i < n; i++) {
			final double[] fs = first[i];
			final double[] ri = result[i];
			for (int j = 0; j < k; j++) {
				double sum = 0d;
				for (int c = 0; c < m; c++) {
					sum += fs[c] * second[c][j];
				}
				ri[j] = sum;
			}
		}
		
		return result;
	}
	/**
	 * Raise the matrix to the given power.
	 * @param matrix the matrix
	 * @param power the power
	 * @return the new matrix
	 */
	public static double[][] power(double[][] matrix, int power) {
		int maxBit = 32 - Integer.numberOfLeadingZeros(power);
		double[][][] powers = new double[maxBit][][];
		powers[0] = matrix;
		for (int i = 1; i < maxBit; i++) {
			powers[i] = multiply(powers[i - 1], powers[i - 1]);
		}
		double[][] result = null;
		for (int i = 0; i < maxBit; i++) {
			if ((power & (1 << i)) != 0) {
				if (result == null) {
					result = powers[i];
				} else {
					result = multiply(result, powers[i]);
				}
			}
		}
		return result;
	}
	/**
	 * Raise the matrix to the given power and row-normalize the result.
	 * @param matrix the matrix
	 * @param power the power
	 * @return the new matrix
	 */
	public static double[][] powerNormal(double[][] matrix, int power) {
		int maxBit = 32 - Integer.numberOfLeadingZeros(power);
		double[][][] powers = new double[maxBit][][];
		powers[0] = copy(matrix);
		rowNormalize(powers[0]);
		for (int i = 1; i < maxBit; i++) {
			powers[i] = multiply(powers[i - 1], powers[i - 1]);
			rowNormalize(powers[i]);
		}
		double[][] result = null;
		for (int i = 0; i < maxBit; i++) {
			if ((power & (1 << i)) != 0) {
				if (result == null) {
					result = powers[i];
				} else {
					result = multiply(result, powers[i]);
					rowNormalize(result);
				}
			}
		}
		return result;
	}
	/**
	 * Create a copy of the given matrix.
	 * @param matrix the source matrix
	 * @return the copy
	 */
	public static double[][] copy(double[][] matrix) {
		double[][] result = new double[matrix.length][];
		
		for (int i = 0; i < matrix.length; i++) {
			result[i] = new double[matrix[i].length];
			System.arraycopy(matrix[i], 0, result[i], 0, result[i].length);
		}
		return result;
	}
	/**
	 * Normalize the rows of the given matrix, e.g., divide each element with the row sum.
	 * @param matrix the matrix
	 */
	public static void rowNormalize(double[][] matrix) {
		for (int i = 0; i < matrix.length; i++) {
			double sum = 0d;
			int n = matrix[i].length;
			for (int j = 0; j < n; j++) {
				sum += matrix[i][j];
			}
			for (int j = 0; j < n; j++) {
				matrix[i][j] /= sum;
			}
		}
	}
	/**
	 * Multiply the vector elements by a number.
	 * @param vector the vector
	 * @param value the value
	 */
	public static void multiply(double[] vector, double value) {
		for (int i = 0; i < vector.length; i++) {
			vector[i] *= value;
		}
	}
	/**
	 * Compute the current day of week (0 - Monday, 6 - Saturday) from the given days since the unix epoch.
	 * @param daysSinceEpoch the days since the unix epoch
	 * @return the day of week
	 */
	public static int dayOfWeek(int daysSinceEpoch) {
		return (3 + (daysSinceEpoch % 7)) % 7;
	}
	/**
	 * Copy the elements from the {@code first} array into the second where
	 * filter function returns true.
	 * @param first the first array [n]
	 * @param second the second array [n]
	 * @param filter the filter function
	 * @return the number of items copied
	 */
	public static int filterCopy(double[] first, double[] second, 
			Func2<? super Integer, ? super Double, Boolean> filter) {
		int j = 0;
		for (int i = 0; i < first.length; i++) {
			if (filter.invoke(i, first[i])) {
				second[j++] = first[i];
			}
		}
		return j;
	}
}
