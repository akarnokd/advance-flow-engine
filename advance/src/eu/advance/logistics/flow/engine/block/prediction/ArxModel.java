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
public class ArxModel {
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
	/**
	 * Initialize the model.
	 * @param p the model order
	 * @param m the number of additional effects
	 * @param u Function to return the external effects at time {@code t}
	 */
	public ArxModel(
			int p, 
			int m, 
			Func2<Integer, Integer, Double> u) {
		this.p = p;
		this.m = m;
		this.u = u;
	}
	/**
	 * Solve the model and find the coefficients by using the Mean Square Error estimation algorithm.
	 * @param trainData the multiple set of time series
	 */
	public void solveMSE(double[][] trainData) {
		final int seriesCount = trainData.length;
		final int timeCount = trainData[0].length;
		
		double[][] at = new double[p + m][seriesCount * timeCount];
		ArrayList<Double> xList = new ArrayList<Double>();		
		int curr = 0;
		for (int n = 0; n < seriesCount; n++) {
			ArrayList<Double> yList = new ArrayList<Double>();
			ArrayList<Integer> tList = new ArrayList<Integer>();
			for (int t = 0; t < timeCount; t++) {
				double entry = trainData[n][t];
				if (entry > 0.0) {
					yList.add(entry);
					tList.add(t);
					if (yList.size() > p) {
						xList.add(entry);
					}
				}
			}
			
			int yl = yList.size();
			
			for (int i = 0; i < yl - p; i++) {
				curr++;
				for (int j = 0; j < p; j++) {
					double yt = yList.get(i + p - 1 - j);
					at[j][curr] = yt;
				}
				for (int j = p; j < p + m; j++) {
					double ut = u.invoke((tList.get(i) + p), j - p);
					at[j][curr] = ut;
				}			
			}
		}
		
		if (curr == 0) {
			arCoeffs = new double[p];
			uCoeffs = new double[m];
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
		
		arCoeffs = thetav.clone();
		uCoeffs  = thetav.clone();
		
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
	public int getP() {
		return p;
	}
	/** @return the external effects count. */
	public int getM() {
		return m;
	}
	
	/**
	 * estimates data based on parameters computed for normalized data.
	 * @param data the time series to estimate
	 * @param initValues the initial values
	 * @param scale the scale of the unnormalized time series
	 * @param horizon the lookahead horizon
	 * @return the predicted data [horizon][?]
	 */
	public double[][] estimate(double[] data, double[] initValues, double scale, int horizon) {
		int timeCount = data.length;
		double[][] initValuesEst = new double[horizon][horizon];
		double[][] predictedData = new double[horizon][timeCount];
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
			for (int t = 0; t < p + h; t++) {
				double prediction = 0.0;
				int hh = Math.min(h, p);
				for (int i = hh; i < p; i++) {
					prediction += (t - i - 1 < 0) 
							? initValues[p + horizon + t - i - 1] * arCoeffs[i] 
											: data[t - i - 1] * arCoeffs[i];
				}
				for (int i = 0; i < hh; i++) {
					prediction += (t - i - 1 < 0) 
							? initValuesEst[horizon + t - i - 1][i] * arCoeffs[i]
									: predictedData[i][t - i - 1] * arCoeffs[i];
				}
				for (int i = 0; i < m; i++) {
					prediction += scale * uCoeffs[i] * u.invoke(t, i);
				}
				predictedData[h][t] = prediction;
			}
			for (int t = p + h; t < timeCount; t++) {
				double prediction = 0.0;
				int hh = Math.min(h, p);
				for (int i = hh; i < p; i++) {
					prediction += data[t - i - 1] * arCoeffs[i];
				}
				for (int i = 0; i < hh; i++) {
					prediction += predictedData[i][t - i - 1] * arCoeffs[i];
				}
				for (int i = 0; i < m; i++) {
					prediction += scale * uCoeffs[i] * u.invoke(t, i);
				}
				predictedData[h][t] = prediction;
			}
		}
		return predictedData;
	}
	/**
	 * Solve the model and find the coefficients by using the Mean Square Error estimation algorithm.
	 * @param x the time series
	 */
	public void solveMSE(double[] x) {
		int n = x.length;
		
		if (n < p) {
			arCoeffs = new double[p];
			uCoeffs = new double[m];
			return;
		}
		
		RealMatrix am = MatrixUtils.createRealMatrix(n - p, p + m);
		RealMatrix atm = MatrixUtils.createRealMatrix(p + m, n - p);
		
		for (int i = 0; i < n - p; i++) {
			for (int j = 0; j < p; j++) {
				double xt = x[i - 1 + p - j];
				am.setEntry(i, j, xt);
				atm.setEntry(j, i, xt);
			}
			for (int j = p; j < p + m; j++) {
				double ut = u.invoke((i + p) % (n), j - p); //check if % indeed works
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
		
		arCoeffs = thetav.clone();
		uCoeffs  = thetav.clone();
		
		System.arraycopy(thetav, 0, arCoeffs, 0, p);
		System.arraycopy(thetav, p, uCoeffs, 0, m);
	}
	
	/**
	 * estimates data based on parameters computed for normalized data.
	 * @param data the time series to estimate
	 * @param initValues the initial values
	 * @param scale the scale of the unnormalized time series
	 * @return the esimation
	 */
	public double[] estimate(double[] data, double[] initValues, double scale) {
		int timeCount = data.length;
		double[] predictedData = new double[timeCount];
		if (initValues == null) {
			for (int t = 0; t < p; t++) {
				predictedData[t] = 0.0;
			}
		} else {
			for (int t = 0; t < p; t++) {
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
		for (int t = p; t < timeCount; t++) {
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
	/**
	 * Compute the forecast for the next h time values.
	 * @param x the observations
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
	 * Sets the AR coefficient.
	 * @param index the index
	 * @param value the new value
	 */
	public void setArCoefficient(int index, double value) {
		arCoeffs[index] = value;
	}
	/**
	 * Sets the ARX external coefficient.
	 * @param index the index
	 * @param value the new value
	 */
	public void setUCoefficient(int index, double value) {
		uCoeffs[index] = value;
	}
}
