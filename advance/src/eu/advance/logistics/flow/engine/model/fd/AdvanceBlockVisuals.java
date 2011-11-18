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

package eu.advance.logistics.flow.engine.model.fd;

import eu.advance.logistics.flow.engine.xml.XElement;
import eu.advance.logistics.flow.engine.xml.XSerializable;

/**
 * Record to store visualization properties of a block, constant or composite block.
 * @author akarnokd, 2011.09.28.
 */
public class AdvanceBlockVisuals implements XSerializable {
	/** The location on screen. */
	public int x;
	/** The location on screen. */
	public int y;
	@Override
	public void load(XElement source) {
		x = source.getInt("x", 0);
		y = source.getInt("y", 0);
	}

	@Override
	public void save(XElement destination) {
		destination.set("x", x);
		destination.set("y", y);
	}

}
