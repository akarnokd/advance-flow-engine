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

package eu.advance.logistics.flow.engine.cc;

/**
 * Interface to support loading and saving form values.
 * @author akarnokd, 2011.10.12.
 * @param <T> the element type
 */
public interface CCLoadSave<T> {
	/**
	 * Load the form values from the supplied object.
	 * @param value the value
	 */
	void load(T value);
	/**
	 * @return the form values saved into the supplied object
	 */
	T save();
	/**
	 * The method to call after the save has been done (e.g., disable some fields, update field values.
	 */
	void onAfterSave();
}
