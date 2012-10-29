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

import hu.akarnokd.reactive4java.base.Func2;

import com.google.common.primitives.Ints;

/**
 * Windowed K-means meta-algorithm for classifying and predicting multiple time series.
 * @author gneu
 *
 */

public class KMeansARXMETA {
	/** Vector of window sizes. */
    protected int[] wVector;
    /** Maximal train window size. */
    protected int maxW;
	/** Vector of cluster numbers. */
	protected int[] kVector;
	/** Number of external factors. */
	protected int m;
	/** Vector of  AR parameter numbers. */
	protected int[] pVector;
	/** Number of window sizes. */
	protected int wL;
	/** Number of cluster numbers. */
	protected int kL;
	/** Number of degrees. */
	protected int pL;
	/** Maximal number of iterations. */
	protected int maxIter;
	/** Whether to use normalization. */
	protected boolean normalized;
	/** Function to return the external effects at time {@code t}.*/
	protected Func2<Integer, Integer, Double> u;
	/** The set of all ARX Models. */
	protected KMeansARX[][][] allKMARXModels; 
	/** All available data. */
	protected double[][] allData;
	/** Maximal length of time series to keep. */
	protected int maxData;
	/** Current training data. */
	protected double[][] trainData;
	/** Test data estimates. */
	protected double[][] testDataEst;
	/** L1 error on the train set. */
	protected double l1train;
	/** L1 error on the validation set. */
	protected double l1test;
	/** L2 error on the train set. */
	protected double l2train;
	/** L2 error on the validation set. */
	protected double l2test;
	/** Number of time series. */
	protected int numSeries;
	/** Length of individual time series. */
	protected int lengthSeries;
	/** Indicator of changes. */
	protected boolean changed;
	/** Normalization coefficients for each time series. */
	double[] trainMean;
	/** Index of the best training window. */ 
	protected int bestW;
	/** Index of the best cluster number. */
	protected int bestK;
	/** Index of the best degree. */
	protected int bestP;
	/** Horizon for measuring accuracy. */
	protected int horizon;
	/** Total number of days. */
	protected int days;
	
	/** @return the L1 error of the predictions on the test window. */
	public double getL1TestError() {
		return l1test;
	}
	
	/** @return the L2 error of the predictions on the test window */
	public double getL2TestError() {
		return l2test;
	}
	
	/** @return the L1 error of the predictions on the train window */
	public double getL1TrainError() {
		return l1train;
	}
	
	/** @return the L2 error of the predictions on the train window*/
	public double getL2TrainError() {
		return l2train;
	}
	
	/** @return the predictions for the test window */
	public double[][] getPredictions() {
		return testDataEst;
	}
	
	/**
	 * Initialize.
	 * @param wVector vector of window sizes
	 * @param kVector vector of cluster numbers
	 * @param pVector vector of AR parameter numbers
	 * @param m number of external factors
	 * @param maxIter maximal number of iterations for K-Means
	 * @param normalized whether to use normalization
	 * @param horizon horizon to measure accuracy
	 * @param u function to return external factors at time {@code t}
	 */
	public KMeansARXMETA(int[] wVector, int[] kVector,  int[] pVector, int m,
			int maxIter, boolean normalized, int horizon, Func2<Integer, Integer, Double> u) {
		super();
		this.wVector = wVector;
		this.kVector = kVector;
		this.m = m;
		this.pVector = pVector;
		this.maxIter = maxIter;
		this.normalized = normalized;
		this.horizon = horizon;
		this.u = u;
		this.allData = null;
		this.maxW = Ints.max(wVector);			
		this.wL = wVector.length;
		this.kL = kVector.length;
		this.pL = pVector.length;
		this.days = 0;
		
		allKMARXModels = new KMeansARX[wL][kL][pL];
	}
	/**
	 * Execute a data step?
	 * @param freshData the fresh data
	 * @param retrain retain current ?
	 * @return the updated data ?
	 */
	public double[][] step(double[][] freshData, boolean retrain) {
		int testW = freshData[0].length;
		if (allData == null) {
			allData = freshData.clone();
			numSeries = allData.length;
			lengthSeries = allData[0].length;
			double bestErr = Double.POSITIVE_INFINITY;
			for (int wI = 0; wI < wL; wI++) {
				int window = Math.min(wVector[wI], lengthSeries);
				double[][] currData = new double[numSeries][window];
				for (int n = 0; n < numSeries; n++) {
					System.arraycopy(allData[n], lengthSeries - window, currData[n], 0, window);
				}
				for (int kI = 0; kI < kL; kI++) {
					for (int pI = 0; pI < pL; pI++) {
						allKMARXModels[wI][kI][pI] = new KMeansARX(currData, 0, 
								pVector[pI], m, kVector[kI], maxIter, normalized, horizon, u);
						allKMARXModels[wI][kI][pI].solve();
						double err = allKMARXModels[wI][kI][pI].getL1TestError();
						if (err < bestErr) {
							testDataEst = allKMARXModels[wI][kI][pI].getPredictions();
							bestW = wI;
							bestK = kI;
							bestP = pI;
							bestErr = err;
						}
					}
				}
			}
		} else {
			int newBestW = bestW;
			int newBestK = bestK;
			int newBestP = bestP;
			
			lengthSeries = allData[0].length;
			
			double bestErr = Double.POSITIVE_INFINITY;
			for (int wI = 0; wI < wL; wI++) {
				int window = Math.min(wVector[wI], lengthSeries);
				double[][]  currData = new double[numSeries][window + testW];
				for (int n = 0; n < numSeries; n++) {
					System.arraycopy(allData[n], lengthSeries - window, currData[n], 0, window);
					System.arraycopy(freshData[n], 0, currData[n], window, testW);
				}
				for (int kI = 0; kI < kL; kI++) {
					for (int pI = 0; pI < pL; pI++) {
						allKMARXModels[wI][kI][pI].setU(
							new Func2<Integer, Integer, Double>() {
								@Override
								public Double invoke(Integer param1, Integer param2) {
									return u.invoke(param1 + days, param2);
								}
							});
						allKMARXModels[wI][kI][pI].setSplit(window);
						allKMARXModels[wI][kI][pI].setAllData(currData);
						if (retrain) {
							allKMARXModels[wI][kI][pI].solve();
						}
						double err = allKMARXModels[wI][kI][pI].getL1TestError();
						if (err < bestErr) {
							newBestW = wI;
							newBestK = kI;
							newBestP = pI;
							bestErr = err;
						}
						if (bestW == wI && bestP == pI && bestK == kI) {
							testDataEst = allKMARXModels[wI][kI][pI].getPredictions();
							l2test = allKMARXModels[wI][kI][pI].getL2TestError();
							l1test = allKMARXModels[wI][kI][pI].getL1TestError();
							l2train = allKMARXModels[wI][kI][pI].getL2TrainError();
							l1train = allKMARXModels[wI][kI][pI].getL1TrainError();
						}
					}
				}
			}
			
			double [][] newData = new double[numSeries][Math.min(lengthSeries + testW, maxW)];
			if (lengthSeries + testW < maxW) {
				for (int n = 0; n < numSeries; n++) {
					System.arraycopy(allData[n], 0, newData[n], 0, lengthSeries);
					System.arraycopy(freshData[n], 0, newData[n], lengthSeries, testW);
				}
			} else {
				int start = lengthSeries + testW - maxW;
				int count = maxW - testW;
				for (int n = 0; n < numSeries; n++) {
					System.arraycopy(allData[n], start, newData[n], 0, count);
					System.arraycopy(freshData[n], 0, newData[n], count, testW);
				}
				days += start;
			}
			
			allData = newData;
			bestP = newBestP;
			bestK = newBestK;
			bestW = newBestW;
		}
		
		return testDataEst;
	}
		

}
