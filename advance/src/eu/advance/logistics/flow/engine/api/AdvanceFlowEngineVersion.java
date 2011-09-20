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

package eu.advance.logistics.flow.engine.api;

/**
 * Contains version information and engine details.
 * @author karnokd, 2011.09.19.
 */
public class AdvanceFlowEngineVersion {
	/**
	 * @return the minor version number. When displayed, this should be a two digit zero padded number, e.g., 1 is 1.01 and 20 is 1.20. 
	 */
	public int minorVersion;
	/** @return the major version number. No padding is required*/
	public int majorVersion;
	/** @return the build number. When displayed, this should be a three digit zero padded number, e.g., 1 is 1.00.001. */
	public int buildNumber;
}
