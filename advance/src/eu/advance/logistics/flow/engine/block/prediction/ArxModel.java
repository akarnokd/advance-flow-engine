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

import java.util.ArrayList;
import java.util.Arrays;

import org.apache.commons.math.linear.MatrixUtils;
import org.apache.commons.math.linear.RealMatrix;
import org.apache.commons.math.linear.SingularValueDecompositionImpl;

/**
 * Autoregressive model with exogenous effects.
 * @author karnokd, 2012.02.01.
 */
public class ArxModel extends ObservationModel {
	/** The start offset. */
	protected int start;
	/** The number of elements to process. */
	protected int count;
	/** The external factor count. */
	protected int m;
	/** Function to return the external effects at time {@code t}. */
	protected Func2<Integer, Integer, Double> u;
	/** The order of the Ar model. */
	protected int p;
	/** The autoregression part coefficients. */
	protected double[] arCoeffs;
	/** The external factors coefficeints. */
	protected double[] uCoeffs;
	/** The number of time series in the training set. */
	protected int numSeries;
	/** The set of time series used for training. */
	protected double[][] trainingSet;
	/**
	 * Initialize the model.
	 * @param x the array of observations [n]
	 * @param start the start index to process
	 * @param count the number of elements to process
	 * @param p the model order
	 * @param m the number of additional effects
	 * @param u Function to return the external effects at time {@code t}
	 */
	public ArxModel(
			double[] x,
			int start,
			int count,
			int p, 
			int m, 
			Func2<Integer, Integer, Double> u) {
		super(x);
		trainingSet = new double[1][];
		trainingSet[0] = Arrays.copyOf(x, x.length);
		this.start = start;
		this.count = count;
		this.p = p;
		this.m = m;
		this.u = u;
		this.numSeries = 1;
	}
	/**
	 * Initialize the model.
	 * @param x the array of observations [n]
	 * @param p the model order
	 * @param m the number of additional effects
	 * @param u Function to return the external effects at time {@code t}.
	 */
	public ArxModel(
			double[] x,
			int p, 
			int m, 
			Func2<Integer, Integer, Double> u) {
		this(x, 0, x.length, p, m, u);
	}
	

	
	/**
	 * Solve the model and find the coefficients by using the Mean Square Error estimation algorithm.
	 */
	public void solveMSE() {
		int n = this.observations.length;
		
		if (n < p) {
			arCoeffs = new double[p];
			uCoeffs = new double[m];
			Arrays.fill(arCoeffs, 0.0);
			Arrays.fill(uCoeffs, 0.0);
			return;
		}
		
		RealMatrix am = MatrixUtils.createRealMatrix(n - p, p + m);
		RealMatrix atm = MatrixUtils.createRealMatrix(p + m, n - p);
		
		double[] x = this.observations;
		
		for (int i = 0; i < n - p; i++) {
			for (int j = 0; j < p; j++) {
				double xt = x[start + i - 1 + p - j];
				am.setEntry(i, j, xt);
				atm.setEntry(j, i, xt);
			}
			for (int j = p; j < p + m; j++) {
				double ut = u.invoke((start + i + p) % (n / numSeries), j - p); // check if % indeed works
				am.setEntry(i, j, ut);
				atm.setEntry(j, i, ut);
			}			
		}
		
		// theta = pinv(a'a) * a' * x
		
		RealMatrix atmam = atm.multiply(am);
		
		RealMatrix atmamInv = new SingularValueDecompositionImpl(atmam).getSolver().getInverse();
		
		RealMatrix atmamInvAtm = atmamInv.multiply(atm);
		
		RealMatrix xv = MatrixUtils.createColumnRealMatrix(Arrays.copyOfRange(x, p, n));
		
		RealMatrix theta = atmamInvAtm.multiply(xv);
		
		double[] thetav = theta.getColumn(0);
		
		arCoeffs = new double[p];
		uCoeffs  = new double[m];
		
		System.arraycopy(thetav, 0, arCoeffs, 0, p);
		System.arraycopy(thetav, p, uCoeffs, 0, m);
	}
	
	/**
	 * Solve the model and find the coefficients by using the Mean Square Error estimation algorithm.
	 * @param trainData the training data.
	 */
	public void solveMSE(double[][] trainData) {
		int numGroups = trainData.length;
		int numTimes = trainData[0].length;
		
		double[][] at = new double[p + m][numGroups * numTimes];
		ArrayList<Double> xList = new ArrayList<Double>();		
		int curr = 0;
		for (int n = 0; n < numGroups; n++) {
			ArrayList<Double> yList = new ArrayList<Double>();
			ArrayList<Integer> tList = new ArrayList<Integer>();
			for (int t = 0; t < numTimes; t++) {
				double entry = trainData[n][t];
				if (entry > 0.0) {
					yList.add(entry);
					if (yList.size() > p) {
						xList.add(entry);
						tList.add(t);
					}
				}
			}
			
			Object[] yv = yList.toArray();
			Object[] tv = tList.toArray();
			
			int yl = yv.length;
			
			for (int i = 0; i < yl - p; i++) {
				curr++;
				for (int j = 0; j < p; j++) {
					double yt = (Double)yv[i + p - 1 - j];
					at[j][curr] = yt;
				}
				for (int j = p; j < p + m; j++) {
					double ut = u.invoke(((Integer)tv[i]), j - p);
					at[j][curr] = ut;
				}			
			}
		}
		
		if (curr == 0) {
			arCoeffs = new double[p];
			uCoeffs = new double[m];
			Arrays.fill(arCoeffs, 0.0);
			Arrays.fill(uCoeffs, 0.0);
			return;
		}
	
		double[][] atmPrep = new double[m + p][curr];
		for (int n = 0; n < (m + p); n++) {
			atmPrep[n] = Arrays.copyOf(at[n], curr);
		}
		
		RealMatrix atm = MatrixUtils.createRealMatrix(atmPrep);
		RealMatrix am  = atm.transpose();
		
		// theta = pinv(a'a) * a' * x
		
		RealMatrix atmam = atm.multiply(am);
		
		RealMatrix atmamInv = new SingularValueDecompositionImpl(atmam).getSolver().getInverse();
		
		RealMatrix atmamInvAtm = atmamInv.multiply(atm);
		
		RealMatrix xv = MatrixUtils.createRealMatrix(xList.size(), 1);
		for (int i = 0; i < xList.size(); i++) {
			xv.setEntry(i, 0, xList.get(i));
		}
				
		RealMatrix theta = atmamInvAtm.multiply(xv);
		
		double[] thetav = theta.getColumn(0);
		
		arCoeffs = new double[p];
		uCoeffs  = new double[m];
		
		System.arraycopy(thetav, 0, arCoeffs, 0, p);
		System.arraycopy(thetav, p, uCoeffs, 0, m);
	}
	/**
	 * @return the autoregression's coefficients [p]
	 */
	public double[] getArCoefficients() {
		return arCoeffs.clone();
	}
	/** @return the external effect's coefficients [m]. */
	public double[] getUCoefficients() {
		return uCoeffs.clone();
	}
	/** @return the order of the autoregressive model. */
	public double getP() {
		return p;
	}
	/** @return the external effects count. */
	public double getM() {
		return m;
	}
	/**
	 * Compute the forecast for the next h time values.
	 * @param h the time horizon, h > 0
	 * @return the forecast values [h]
	 */
	public double[] forecastAll(int h) {
		return forecastAll(this.observations, h);
	}
	/**
	 * Compute the forecast for the next h time values.
	 * @param x the samples
	 * @param h the time horizon, h > 0
	 * @return the forecast values [h]
	 */
	public double[] forecastAll(double[] x, int h) {
		double[] xf = new double[h];
		
		int n = x.length;
		
		for (int t = n; t < n + h; t++) {
			double sum = 0d;
			for (int i = 0; i < p; i++) {
				
				double o = 0d;
				if (t - i - 1 < n) {
					o = x[t - i - 1];
				} else {
					o = xf[t - n - 1 - i];
				}
				
				sum += arCoeffs[i] * o;
			}
			double sumu = 0d;
			for (int i = 0; i < m; i++) {
				sumu += uCoeffs[i] * u.invoke(t, i);
			}
			xf[t - n] = sum + sumu;
		}
		
		return xf;
	}
	
	/**
	 * Estimates data based on parameters computed for normalized data.
	 * @param data the time series to estimate
	 * @param initValues the initial values
	 * @param scale the scale of the unnormalized time series
	 * @param horizon the lookahead horizon
	 * @param offset ?
	 * @return the estimated values for each group
	 */
	public double[][] estimate(
			double[] data, 
			double[] initValues, double scale, int horizon, int offset) {
		int numTime = data.length;
		int iT = initValues.length;
		
		double[] fullData = new double[numTime + iT];
		System.arraycopy(initValues, 0, fullData, 0, iT);
		System.arraycopy(data, 0, fullData, iT, numTime);
		
		ArrayList<Integer> tList = new ArrayList<Integer>();
		
		for (int t = 0; t < numTime; t++) {
			if (data[t] > 0.0) {
				tList.add(t);
			}
		}
		
		int filteredTimes = tList.size();
		
		double[][] predictedData = new double[horizon][numTime];
		double[][] initValuesEst = new double[horizon][horizon];
		
		for (int h = 0; h < horizon; h++) {
			for (int j = h; j < horizon; j++) {
					double initPred = 0.0;
					int hh = Math.min(h, p);
					for (int i = hh; i < p; i++) {
						initPred += initValues[p + j - i - 1] * arCoeffs[i];
					}
					for (int i = 0; i < hh; i++) {
						initPred += initValuesEst[j - i - 1][i] * arCoeffs[i];
					}
					initValuesEst[j][h] = initPred;
			}
		}
			
		for (int h = 0; h < horizon; h++) {
			int ph = Math.min(p + h, filteredTimes);
			for (int t = 0; t < ph; t++) {
				double prediction = 0.0;
				int hh = Math.min(h, p);
				for (int i = hh; i < p; i++) {
					prediction += 
							(t - i - 1 < 0) 
							? initValues[p + horizon + t - i - 1] * arCoeffs[i] 
											: data[tList.get(t - i - 1)] * arCoeffs[i];
				}
				for (int i = 0; i < hh; i++) {
					prediction += (t - i - 1 < 0) 
							? initValuesEst[horizon + t - i - 1][i] * arCoeffs[i]
									: predictedData[i][tList.get(t - i - 1)] * arCoeffs[i];
				}
				for (int i = 0; i < m; i++) {
					prediction += scale * uCoeffs[i] * u.invoke(tList.get(t) + offset, i);
				}
				predictedData[h][tList.get(t)] = prediction;
			}
			for (int t = ph; t < filteredTimes; t++) {
				double prediction = 0.0;
				int hh = Math.min(h, p);
				for (int i = hh; i < p; i++) {
					prediction += data[tList.get(t - i - 1)] * arCoeffs[i];
				}
				for (int i = 0; i < hh; i++) {
					prediction += predictedData[i][tList.get(t - i - 1)] * arCoeffs[i];
				}
				for (int i = 0; i < m; i++) {
					prediction += scale * uCoeffs[i] * u.invoke(tList.get(t) + offset, i);
				}
				predictedData[h][tList.get(t)] = prediction;
			}
		}
		return predictedData;
	}
	
	/**
	 * Estimates data based on parameters computed for normalized data.
	 * @param data the time series to estimate
	 * @param initValues the initial values
	 * @param scale the scale of the unnormalized time series
	 * @return the estimates
	 */
	public double[] estimate(double[] data, double[] initValues, double scale) {
		int numTimes = data.length;
		double[] predictedData = new double[numTimes];
		int pp = Math.min(p, numTimes);
		if (initValues == null) {
			for (int t = 0; t < pp; t++) {
				predictedData[t] = -1.0;
			}
		} else {
			for (int t = 0; t < pp; t++) {
				double prediction = 0.0;
				for (int i = 0; i < p; i++) {
					prediction += 
							(t - i - 1 < 0) 
							? initValues[p + t - i - 1] * arCoeffs[i] 
									: data[t - i - 1] * arCoeffs[i];
				}
				for (int i = 0; i < m; i++) {
					prediction += scale * uCoeffs[i] * u.invoke(t, i);
				}
				predictedData[t] = prediction;
			}
		}
		for (int t = pp; t < numTimes; t++) {
			double prediction = 0.0;
			for (int i = 0; i < p; i++) {
				prediction += data[t - i - 1] * arCoeffs[i];
			}
			for (int i = 0; i < m; i++) {
				prediction += scale * uCoeffs[i] * u.invoke(t, i);
			}
			predictedData[t] = prediction;
		}
		return predictedData;
	}
	
	@Override
	public int paramCount() {
		return m + p;
	}
	@Override
	public double logLikelihood() {
		throw new UnsupportedOperationException();
	}
	/**
	 * Sets the autoregression model coefficient.
	 * @param index the index &lt; p
	 * @param value the value
	 */
	public void setArCoefficient(int index, double value) {
		arCoeffs[index] = value;
	}
	/**
	 * Sets the autoregression exogenous model coefficient.
	 * @param index the index, &lt; m
	 * @param value the value
	 */
	public void setUCoefficient(int index, double value) {
		uCoeffs[index] = value;
	}
}
