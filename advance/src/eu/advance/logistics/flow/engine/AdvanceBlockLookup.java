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

package eu.advance.logistics.flow.engine;

import edu.umd.cs.findbugs.annotations.NonNull;
import eu.advance.logistics.flow.model.AdvanceBlockDescription;

/**
 * Utility class to look up various blocks.
 * @author karnokd, 2011.06.27.
 */
public final class AdvanceBlockLookup {
	/**
	 * Utility class.
	 */
	private AdvanceBlockLookup() {
	}
	/**
	 * Locate the block description in the repository (e.g., block-description-list.xml).
	 * @param id the block identifier
	 * @return the block
	 */
	public static AdvanceBlockDescription lookup(@NonNull String id) {
		// TODO implement
		return null;
	}
	/**
	 * Create a concrete block by using the given settings.
	 * @param gid the global block id
	 * @return the new block instance 
	 */
	public static AdvanceBlock create(int gid) {
		// TODO implement
		return null;
	}
}
