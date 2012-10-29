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

/**
 * Statistics on integer values.
 * @author karnokd, 2012.01.24.
 */
public final class Statistics {
	/** Utility class. */
	private Statistics() {
	}
	/**
	 * Average of the array of integers.
	 * @param data the data array
	 * @return the average
	 */
	public static double average(int[] data) {
		double n = 0;
		for (int v : data) {
			n += v;
		}
		return n / data.length;
	}
	/**
	 * Average of the array of integers.
	 * @param data the data array
	 * @return the average
	 */
	public static double average(double[] data) {
		double n = 0;
		for (double v : data) {
			n += v;
		}
		return n / data.length;
	}
	/**
	 * Compute the average and standard deviation.
	 * @param data the data
	 * @return the [average, stddev] pair
	 */
	public static SampleStats sampleStatistics(int[] data) {
		double a = average(data);
		double sum = 0d;
		for (int i = 0; i < data.length; i++) {
			double diff = (data[i] - a);
			sum += diff * diff;
		}
		SampleStats result = new SampleStats();
		
		result.average = a;
		result.varianceSample = sum / data.length;
		result.sampleVariance = sum / (data.length - 1);
		result.stdDevSample = Math.sqrt(result.varianceSample);
		result.sampleStdDev = Math.sqrt(result.sampleVariance);
		
		return result;
	}
	/**
	 * Compute the average and standard deviation.
	 * @param data the data
	 * @return the [average, stddev] pair
	 */
	public static SampleStats sampleStatistics(double[] data) {
		double a = average(data);
		double sum = 0d;
		for (int i = 0; i < data.length; i++) {
			double diff = (data[i] - a);
			sum += diff * diff;
		}
		SampleStats result = new SampleStats();
		
		result.average = a;
		result.varianceSample = sum / data.length;
		result.sampleVariance = sum / (data.length - 1);
		result.stdDevSample = Math.sqrt(result.varianceSample);
		result.sampleStdDev = Math.sqrt(result.sampleVariance);
		
		return result;
	}
	/**
	 * The sample statistics record. 
	 * @author karnokd, 2012.01.24.
	 */
	public static class SampleStats {
		/** The average. */
		public double average;
		/** The standard deviation of the sample, i.e, 1/N. */
		public double stdDevSample;
		/** The sample standard deviation, i.e., 1/(N - 1). */
		public double sampleStdDev;
		/** The variance, e.g., sampleStdDev**2 (1/(N - 1). */
		public double sampleVariance;
		/** The variance, e.g., sampleStdDev**2. (1/N) */
		public double varianceSample;
	}
	/**
	 * Computes the counts of individual values.
	 * @param ds the datas, each >= 0
	 * @return the counts
	 */
	public static int[] frequencies(int... ds) {
		int max = 0;
		for (int d : ds) {
			max = Math.max(max, d);
		}
		int[] result = new int[max + 1];
		for (int d : ds) {
			result[d]++;
		}		
		return result;
	}
	/**
	 * Computes the counts of individual values.
	 * @param ds the datas, each >= 0 and is integer
	 * @return the counts
	 */
	public static double[] frequencies(double... ds) {
		double max = 0;
		for (double d : ds) {
			max = Math.max(max, d);
		}
		double[] result = new double[(int)max + 1];
		for (double d : ds) {
			result[(int)d]++;
		}		
		return result;
	}
	/**
	 * Computes the frequency distribution of the values.
	 * @param ds the datas, each >= 0 and is integer
	 * @return the counts
	 */
	public static double[] distribution(double... ds) {
		double max = 0;
		int count = 0;
		for (double d : ds) {
			max = Math.max(max, d);
		}
		int n = (int)max + 1;
		double[] result = new double[n];
		for (double d : ds) {
			result[(int)d]++;
			count++;
		}
		for (int i = 0; i < result.length; i++) {
			result[i] /= count;
		}
		return result;
	}
}
