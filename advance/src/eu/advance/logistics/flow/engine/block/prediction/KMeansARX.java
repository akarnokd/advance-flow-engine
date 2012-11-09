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

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.google.common.collect.Lists;

/**
 * K-means algorithm for classifying and predicting multiple time series.
 * @author gneu, 2012.02.10.
 */
public class KMeansARX {
	/** Number of clusters. */
	protected int clusterCount;
	/** Number of external factors. */
	protected int m;
	/** Number of AR parameters. */
	protected int p;
	/** Maximal number of iterations. */
	protected int maxIter;
	/** Whether to use normalization. */
	protected boolean normalized;
	/** Function to return the external effects at time {@code t}.*/
	protected Func2<Integer, Integer, Double> u;
	/** The set of K ARX Models. */
	protected ArxModel[] allModels;
	/** All data. */
	protected double[][] allData;
	/** Training data. */
	protected double[][] trainData;
	/** Test data. */
	protected double[][] testData;
	/** Training data estimates. */
	protected double[][] trainDataEst;
	/** Test data estimates. */
	protected double[][] testDataEst;
	/** L1 error on the train set. */
	protected double l1train;
	/** L1 error on the test set. */
	protected double l1test;
	/** L2 error on the train set. */
	protected double l2train;
	/** L2 error on the test set. */
	protected double l2test;
	/** Array indicating classes for each time series. */
	protected int[] classes;
	/** Sets of time series belonging to each cluster. (i.e, [cluster #] == series index */
	protected List<Set<Integer>> classSet;
	/** Number of time series. */
	protected int numSeries;
	/** Length of individual training time series. */
	protected int numTraining;
	/** Normalization coefficients for each time series. */
	protected double[] trainMean;
	/** Horizon for measuring accuracy. */
	protected int horizon;
	/** Where to split the provided data into train and test datasets. */
	protected int split;
	
	/**
	 * Initialize.
	 * @param allData all available data
	 * @param split the split count, if between 1 and the number of columns in allData, 0 for no split
	 * @param p the model order
	 * @param m the number of external factors
	 * @param cluserCount the number of ARX models
	 * @param maxIter the maximal number of iterations for K-means
	 * @param normalized whether to use normalization
	 * @param horizon the prediction horizon
	 * @param u the function that represents external factors
	 */
	public KMeansARX(
			double[][] allData,
			int split,
			int p,
			int m,
			int cluserCount,
			int maxIter,
			boolean normalized,
			int horizon,
			Func2<Integer, Integer, Double> u) {
		this.allData = allData;
		this.split = split;
		this.p = p;
		this.m = m;
		this.clusterCount = cluserCount;
		this.maxIter = maxIter;
		this.normalized = normalized;
		this.horizon = horizon;
		this.u = u;
		this.numSeries = allData.length;
		
		if (split > 0) {
			int len = allData[0].length;
			trainData = new double[numSeries][split];
			testData = new double[numSeries][len - split];
			for (int n = 0; n < numSeries; n++) {
				System.arraycopy(allData[n], 0, trainData[n], 0, split);
				System.arraycopy(allData[n], split, testData[n], 0, allData[0].length - split);
			}
		} else {
			trainData = allData.clone();
			testData = allData.clone();
		}
			
		this.numTraining = trainData[0].length;
		this.classes = new int[numSeries];
		this.trainMean = new double[numSeries];
		this.trainDataEst = new double[numSeries][];
		this.testDataEst = new double[numSeries][];
		this.classSet = Lists.newArrayList();;
		for (int k = 0; k < cluserCount; k++) {
			classSet.add(new HashSet<Integer>());
		}
	}
	
	
	/** 
	 * Set all data.
	 * @param allData the data to set
	 */
	public void setAllData(double[][] allData) {
		int len = allData[0].length;
		trainData = new double[numSeries][split];
		testData = new double[numSeries][len - split];
		for (int n = 0; n < numSeries; n++) {
			System.arraycopy(allData[n], 0, trainData[n], 0, split);
			System.arraycopy(allData[n], split, testData[n], 0, len - split);
		}
		this.allData = allData.clone();
	}
	
	/** 
	 * Set train-test split.
	 * @param split where to split the data in two
	 */
	public void setSplit(int split) {
		this.split = split;
	}
	
	
	
	/** 
	 * Set the function returning external factors.
	 * @param u the function to set
	 */
	public void setU(Func2<Integer, Integer, Double> u) {
		this.u = u;
		for (int k = 0; k < clusterCount; k++) {
			allModels[k].u = u;
		}
	}
	
	
	/** 
	 * Set the training data.
	 * @param trainData the training data to set
	 */
	public void setTrainData(double[][] trainData) {
		this.trainData = trainData.clone();
	}
	
	/** 
	 * Set the test data.
	 * @param testData the test data
	 */
	public void setTestData(double[][] testData) {
		this.testData = testData.clone();
	}
	
	/** @return the predictions for the test window */
	public double[][] getPredictions() {
		return testDataEst;
	}
	
	/** @return the L1 error of the predictions on the test window */
	public double getL1TestError() {
		return l1test;
	}
	
	/** @return the L2 error of the predictions on the test window */
	public double getL2TestError() {
		return l2test;
	}
	
	/** @return the L1 error of the predictions on the train window*/
	public double getL1TrainError() {
		return l1train;
	}
	
	/** @return the L2 error of the predictions on the train window*/
	public double getL2TrainError() {
		return l2train;
	}
	/**
	 * Returns the average L2 error between two sets of time series.
	 * @param data the original set of time series
	 * @param dataEst the estimated set of time series
	 * @return  the l2 error
	 */
	public static double l2Error(double[][] data, double[][] dataEst) {
		double err = 0.0;
		int nonzeroes = 0;
		for (int n = 0; n < data.length; n++) {
			for (int t = 0; t < data[n].length; t++) {
				double d = data[n][t];
				double dE = dataEst[n][t];
				if (d > 0.0 && dE != -1.0) {
					err += (d - dE) * (d - dE);
					nonzeroes++;
				}
			}
		}
		if (nonzeroes > 0) {
			return err / nonzeroes;
		}
		return 0.0;
	}
	
	/**
	 * Returns the average L2 error between two time series.
	 * @param data the original time series
	 * @param dataEst the estimated time series
	 * @return the average L2 error
	 */
	protected static double l2Error(double[] data, double[] dataEst) {
		double err = 0.0;
		int nonzeroes = 0;
		for (int t = 0; t < data.length; t++) {
			double d = data[t];
			double dE = dataEst[t];
			if (d > 0.0 && dE != -1.0) {
				err += (d - dE) * (d - dE);
				nonzeroes++;
			}
		}
		if (nonzeroes > 0) {
			return err / nonzeroes;
		}
		return 0.0;
	}
	
	/**
	 * Returns the average L1 error between two sets of time series.
	 * @param data the original set of time series
	 * @param dataEst the estimated set of time series
	 * @return the average L1 error
	 */
	public static double l1Error(double[][] data, double[][] dataEst) {
		double err = 0.0;
		int nonzeroes = 0;
		for (int n = 0; n < data.length; n++) {
			for (int t = 0; t < data[n].length; t++) {
				double d = data[n][t];
				double dE = dataEst[n][t];
				if (d > 0.0 && dE != -1.0) {
					err += Math.abs(d - dE);
					nonzeroes++;
				}
			}
		}
		if (nonzeroes > 0) {
			return err / nonzeroes;
		}
		return 0.0;
	}
	
	/**
	 * Returns the average L1 error between two time series.
	 * @param data the original time series
	 * @param dataEst the estimated time series
	 * @return the average L2 error
	 */
	protected static double l1Error(double[] data, double[] dataEst) {
		double err = 0.0;
		int nonzeroes = 0;
		for (int t = 0; t < data.length; t++) {
			double d = data[t];
			double dE = dataEst[t];
			if (d > 0.0 && dE != -1.0) {
				err += Math.abs(d - dE);
				nonzeroes++;
			}
		}
		if (nonzeroes > 0) {
			return err / nonzeroes;
		}
		return 0.0;
	}
	
	/**
	 * Classifies time series into one of K clusters.
	 * @return if the clusers have changed
	 */
	protected boolean classify() {
		boolean changed = false;
		int bestK = 0;
		double[] trainEst = null;
		for (int n = 0; n < numSeries; n++) {
			double bestErr = Double.POSITIVE_INFINITY;
			for (int k = 0; k < clusterCount; k++) {
				// this could be slightly improved if we also pass some initializing values to the function
				// though the expected improvement is negligible if p<<T, which should be the case
				double[] dummy = allModels[k].estimate(trainData[n], null, trainMean[n]); 
				double err = l2Error(trainData[n], dummy);
				//System.out.println(k + ": " + err);
				if (err <= bestErr) {
					bestK = k;
					bestErr = err;
					trainEst = dummy.clone();
				}
			}
			
			if (classes[n] != bestK) {
				classSet.get(classes[n]).remove(n);
				classes[n] = bestK;
				changed = true;
			} 

			classSet.get(bestK).add(n);
			trainDataEst[n] = trainEst;
		}
		//for (int k=0; k<K; k++) 
		//	System.out.print(classSet[k].size() + " ");
		//System.out.println();
		return changed;
	}
	
	/**
	 * Updates parameters belonging to each cluster.
	 */
	protected void update() {
		for (int k = 0; k < clusterCount; k++) {
			//System.out.println(k + ": " + Arrays.toString(allModels[k].uCoeffs));
			// concatenate training time series
			if (classSet.get(k).isEmpty()) {
				//int index = (int) Math.floor(Math.random()*N);
				//allModels[k] = new ArxModel(trainData[index], p, m, u);
				//allModels[k].solveMSE();
				continue;
			}
			double[] longTrainingSeries = new double[classSet.get(k).size() * numTraining];
			double[][] trainingSet = new double[classSet.get(k).size()][numTraining];
			int h = 0;
			for (int n : classSet.get(k)) {
				for (int t = 0; t < numTraining; t++) {
					double tt = trainData[n][t] / trainMean[n];
					trainingSet[h][t] = tt;
					longTrainingSeries[h * numTraining + t] = tt;
				}
				h++;
			}
			// solve for new parameter values
			//allModels[k].setNumSeries(classSet[k].size());
			//allModels[k].setObservations(longTrainingSeries);
			//allModels[k].solveMSE(); // simple and dirty
			allModels[k].solveMSE(trainingSet); // somewhat more difficult, but overall superior performance
			//System.out.println(k + ": " + Arrays.toString(allModels[k].arCoeffs));
		}
	}
	
	/**
	 * Runs K-means clustering.
	 */
	public void solve() {
		// normalizes data by the mean of the training data if requested
		if (normalized) {
			for (int n = 0; n < numSeries; n++) {
				double trainM = 0.0;
				for (int t = 0; t < numTraining; t++) {
					trainM += trainData[n][t] / numTraining;
				}
				trainMean[n] = trainM > 0 ? trainM : 1.0;	
			}
		} else {
			for (int n = 0; n < numSeries; n++) {
				trainMean[n] = 1.0;
			}
		}
			
				
		// initialize if needed
		if (allModels == null) {
			if (clusterCount >= numSeries) {
				clusterCount = numSeries;
			}
			allModels = new ArxModel[clusterCount];
			for (int k = 0; k < clusterCount; k++) {
				int index = (int) Math.floor(Math.random() * numSeries);
				allModels[k] = new ArxModel(trainData[index], p, m, u);
				allModels[k].solveMSE();
				classSet.get(k).add(index);
				classes[index] = k;
			}
		}
		
		boolean changed = true;
		
		for (int iter = 0; (iter < maxIter) && (changed); iter++) {
			changed = clusterCount < numSeries ? classify() : false;
			
			update();
		}

		predict(0);
	}
	/**
	 * Run the prediction.
	 * @param lookahead the lookahead days
	 * @return the estimated data
	 */
	public double[][] predict(int lookahead) {
		// compute estimates for the test section of each time series
		for (int n = 0; n < numSeries; n++) {
			int k = classes[n];
			double[] initValues = Arrays.copyOfRange(trainData[n], numTraining - p - horizon, numTraining);
			double[][] estimates = allModels[k].estimate(testData[n], initValues, trainMean[n], horizon, split);
			testDataEst[n] = estimates[lookahead];

			//double[] initValues = Arrays.copyOfRange(trainData[n], T-p, T);
			//testDataEst[n]= allModels[k].estimate(testData[n], initValues, trainMean[n]);
			trainDataEst[n] = allModels[k].estimate(trainData[n], null, trainMean[n]);
		}
		
		l2train = l2Error(trainData, trainDataEst);
		l1train = l1Error(trainData, trainDataEst);
		l2test  = l2Error(testData, testDataEst);
		l1test  = l1Error(testData, testDataEst);
		
		return testDataEst;
	}
	/** @return the models. */
	public List<ArxModel> models() {
		return Arrays.asList(allModels);
	}
	/**
	 * @return the learned class index for each row of the input data
	 */
	public int[] getClasses() {
		return classes.clone();
	}
	/**
	 * Returns the set of input row indexes for the given cluser index.
	 * @param clusterIndex the cluster index
	 * @return the set of row indexes
	 */
	public Set<Integer> getTimeseriesRowsForCluster(int clusterIndex) {
		return classSet.get(clusterIndex);
	}
	/** 
	 * Split data into trainset and test set.
	 * @param split where to split the data in two
	 * */
	public void split(int split) {
		int len = allData[0].length;
		split = Math.min(len, split);
		trainData = new double[numSeries][split];
		testData = new double[numSeries][len - split];
		for (int n = 0; n < numSeries; n++) {
			System.arraycopy(allData[n], 0, trainData[n], 0, split);
			System.arraycopy(allData[n], split, testData[n], 0, len - split);
		}
		this.split = split;
	}
	
}
