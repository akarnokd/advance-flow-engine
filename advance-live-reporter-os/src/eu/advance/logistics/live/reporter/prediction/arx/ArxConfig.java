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

import java.util.HashSet;
import java.util.Set;

import org.joda.time.DateMidnight;

/**
 * The configuration for the ARX learning.
 * @author karnokd, 2013.05.03.
 */
public class ArxConfig {
	/** The number of days to consider. */
	public int modelOrder = 10;
	/** Number of clusters. */
	public int clusterCount = 1;
	/** Maximum iteration. */
	public int maxIterations = 10;
	/** Length of validation horizon. */
	public int horizon = 5;
	/** Normalize values? */
	public boolean normalize = true;
	/** Percentage between train and test set. */
	public double split = 0.75;
	/** The set of holidays to ignore. */
	public final Set<DateMidnight> holidays = new HashSet<>();
	/** Ignore weekend data? */
	public boolean ignoreWeekends = true;
}