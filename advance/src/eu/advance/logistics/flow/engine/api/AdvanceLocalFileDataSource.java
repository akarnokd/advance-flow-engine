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
 * A local file or directory data source.
 * @author karnokd, 2011.09.20.
 */
public interface AdvanceLocalFileDataSource extends AdvanceCreateModifyInfo {
	/** @return the unique identifier of this data source. */
	int id();
	/** @return the name of the data source as used by blocks. */
	String name();
	/** @return the directory where the file source(s) are located. */
	String directory();
	/** 
	 * @param newName the new name
	 */
	void name(String newName);
	/**
	 * @param newDirectory the new directory
	 */
	void directory(String newDirectory);
}
