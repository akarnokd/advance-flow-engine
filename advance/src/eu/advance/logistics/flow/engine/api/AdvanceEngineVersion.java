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

import eu.advance.logistics.flow.engine.model.XSerializable;
import eu.advance.logistics.flow.engine.xml.typesystem.XElement;

/**
 * Contains version information and engine details.
 * @author karnokd, 2011.09.19.
 */
public class AdvanceEngineVersion implements XSerializable {
	/**
	 * @return the minor version number. When displayed, this should be a two digit zero padded number, e.g., 1 is 1.01 and 20 is 1.20. 
	 */
	public int minorVersion;
	/** @return the major version number. No padding is required*/
	public int majorVersion;
	/** @return the build number. When displayed, this should be a three digit zero padded number, e.g., 1 is 1.00.001. */
	public int buildNumber;
	@Override
	public void load(XElement source) {
		parse(source.get("version"));
	}
	@Override
	public void save(XElement destination) {
		destination.set("version", format());
	}
	/**
	 * Set the version values from the string.
	 * @param versionString the string in format 0.00.000
	 */
	public void parse(String versionString) {
		int idx1 = versionString.indexOf('.');
		int idx2 = versionString.indexOf('.');
		if (idx1 < 0 || idx2 < 0) {
			throw new IllegalArgumentException("Version format error: " + versionString);
		}
		majorVersion = Integer.parseInt(versionString.substring(0, idx1));
		minorVersion = Integer.parseInt(versionString.substring(idx1 + 1, idx2));
		buildNumber = Integer.parseInt(versionString.substring(idx2 + 1));
	}
	/**
	 * Format the engine version.
	 * @return the engine version in format 0.00.000
	 */
	public String format() {
		return String.format("%d.%02d.%03d", majorVersion, minorVersion, buildNumber);
	}
}
