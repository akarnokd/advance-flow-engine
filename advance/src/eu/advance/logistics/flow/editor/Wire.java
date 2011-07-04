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

package eu.advance.logistics.flow.editor;

/**
 * A wire connecting two blocks.
 * @author karnokd, 2011.07.04.
 */
public class Wire {
	/** The source block. */
	public Block source;
	/** The source port index. */
	public int sourcePort;
	/** The destination block. */
	public Block destination;
	/** The destination port index. */
	public int destinationPort;
	/**
	 * Construct a wire between two blocks and parameters.
	 * @param source the source block
	 * @param sourcePort the output port index of the source block
	 * @param destination the destination block
	 * @param destinationPort the input index of the destination block
	 */
	public Wire(Block source, int sourcePort, Block destination,
			int destinationPort) {
		this.source = source;
		this.sourcePort = sourcePort;
		this.destination = destination;
		this.destinationPort = destinationPort;
	}
	
}
