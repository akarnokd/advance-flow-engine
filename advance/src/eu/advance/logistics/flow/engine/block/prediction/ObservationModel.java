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

import eu.advance.logistics.flow.engine.block.prediction.Statistics.SampleStats;
import eu.advance.logistics.flow.engine.util.U;

/**
 * Base class for mixture and hidden markov models.
 * @author karnokd, 2012.01.30.
 */
public abstract class ObservationModel {
	/**
	 * The set of observations.
	 */
	protected double[] observations;
	/** The convergence factor. */
	protected double convergence = 1E-6;
	/** The iteration limit. */
	protected int iterationLimit = 5000;
	/**
	 * Initialize the model with default initials and poisson emissions.
	 * @param observations the base observations
	 */
	public ObservationModel(double[] observations) {
		this.observations = observations;
	}
	/**
	 * Set the convergence limit for the EM algorithm.
	 * @param newConvergence the new convergence (typically 1E-6)
	 */
	public void setConvergence(double newConvergence) {
		this.convergence = newConvergence;
	}
	/**
	 * @return the current convergence limit for the EM algorithm
	 */
	public double getConvergence() {
		return convergence;
	}
	/**
	 * Set the iteration limit for the EM algorithm.
	 * @param newLimit the new limit (typically 5000)
	 */
	public void setIterationLimit(int newLimit) {
		this.iterationLimit = newLimit;
	}
	/**
	 * @return the current iteration limit for the EM algorithm
	 */
	public int getIterationLimit() {
		return iterationLimit;
	}
	/**
	 * @return the observations
	 */
	public double[] getObservations() {
		return observations;
	}
	/**
	 * Replace the observations of this model.
	 * @param values the new observation values
	 */
	public void setObservations(double... values) {
		this.observations = values;
	}
	/**
	 * Replace the observations of this model.
	 * @param values the new observation values
	 */
	public void setObservations(int... values) {
		this.observations = U.convertToDouble(values);
	}
	/**
	 * Replace the observations of this model.
	 * @param values the new observation values
	 */
	public void setObservations(Iterable<? extends Number> values) {
		this.observations = U.convertToDouble(values);
	}
	/**
	 * Returns the indexth observation.
	 * @param index the index
	 * @return the observation value
	 */
	public double observation(int index) {
		return observations[index];
	}
	/**
	 * Returns the observation count of this model.
	 * @return the observation count
	 */
	public int observationCount() {
		return observations.length;
	}
	/**
	 * @return the computed sample statistics
	 */
	public SampleStats observationStatistics() {
		return Statistics.sampleStatistics(observations);
	}
	/**
	 * Compute the log likelihood of the current model.
	 * @return the likelihood
	 */
	public abstract double logLikelihood();
	/**
	 * @return The number of parameters of the model.
	 */
	public abstract int paramCount();
	/**
	 * @return the Akaie information criterion (used for model selection).
	 */
	public double aic() {
		// model parameter count: initials, transitions, emissions
		int p = paramCount();
		return -2 * logLikelihood() + 2 * p;
	}
	/**
	 * @return the Bayesian information criterion (user for model selection.
	 */
	public double bic() {
		// model parameter count: initials, transitions, emissions
		int p = paramCount();
		return -2 * logLikelihood() + p * Math.log(observations.length);
	}

}
