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

package eu.advance.logistics.live.reporter.prediction.arx;

import eu.advance.logistics.live.reporter.model.TimedValue;
import hu.akarnokd.reactive4java.base.Func2;

import java.util.ArrayList;
import java.util.List;

import org.joda.time.DateMidnight;

/**
 * The ARX learner routine.
 * @author karnokd, 2013.05.03.
 */
public final class ArxLearner {
	/** Utility class. */
	private ArxLearner() { }
	/**
	 * Learn the given time series.
	 * @param timeSeries the time value group sequence
	 * @param config the learning configuration
	 * @return the learnt model coefficients
	 */
	public static ArxCoefficients learn(Iterable<? extends TimedValue> timeSeries, ArxConfig config) {
		final ArxTimedValueAggregator series = new ArxTimedValueAggregator(
				config.holidays, config.ignoreWeekends);
		series.addAll(timeSeries);
		series.build();
		series.addForecastDays(1);
		
		int minSamples = (int)Math.ceil((1 + config.split) * config.modelOrder);
		int m = 5;
		if (series.dayOfWeek.size() >= minSamples) {
			ArxKMeans kMeans = new ArxKMeans(
					series.timeSeriesMatrix,
					(int)(series.dayOfWeek.size() * config.split),
					config.modelOrder,
					m,
					1, // model count
					config.maxIterations,
					config.normalize,
					config.horizon,
					new Func2<Integer, Integer, Double>() {
						@Override
						public Double invoke(Integer param1, Integer param2) {
							return series.dayOfWeek.get(param1) == param2.intValue() ? 1d : 0d;
						}
					}
			);
			kMeans.solve();
			ArxCoefficients result = new ArxCoefficients();
			for (ArxModel md : kMeans.models()) {
				result.modelCoefficients.addAll(md.getArCoefficients());
				result.externalCoefficients.addAll(md.getUCoefficients());
			}
			return result;
		}
		return new ArxCoefficients();
	}
	/**
	 * Calculate a prediction by using the given coefficients, time series
	 * and horizon length.
	 * @param config the learning configuration
	 * @param coeffs the coefficients
	 * @param timeSeries the time series
	 * @param horizon the horizon after the time series ends
	 * @return the predicted values paired by the day and value
	 */
	public static List<TimedValue> predict(
			ArxConfig config,
			ArxCoefficients coeffs, 
			Iterable<? extends TimedValue> timeSeries,
			int horizon) {
		final ArxTimedValueAggregator series = new ArxTimedValueAggregator(config.holidays, config.ignoreWeekends);
		series.addAll(timeSeries);
		series.build();

		// extend day of week info by the horizon
		List<DateMidnight> horizonDays = series.addForecastDays(horizon);
		
		// create arx model
		ArxModel arxModel = new ArxModel(
				new double[0],
				coeffs.modelCoefficients.size(), 
				coeffs.externalCoefficients.size(), 
				new Func2<Integer, Integer, Double>() {
			@Override
			public Double invoke(Integer param1, Integer param2) {
				return series.dayOfWeek.get(param1) == param2.intValue() ? 1d : 0d;
			}
		});

		// setup coefficients
		int i = 0;
		for (double ac : coeffs.modelCoefficients.toArray()) {
			arxModel.setArCoefficient(i, ac);
			i++;
		}
		i = 0;
		for (double uc : coeffs.externalCoefficients.toArray()) {
			arxModel.setUCoefficient(i, uc);
			i++;
		}
		
		// calculte forecasts
		double[] values = arxModel.forecastAll(series.timeSeriesMatrix[0], horizon);
		
		List<TimedValue> result = new ArrayList<>();
		i = 0;
		for (double v : values) {
			result.add(new TimedValue(horizonDays.get(i), v));
			i++;
		}
		return result;
	}
}
