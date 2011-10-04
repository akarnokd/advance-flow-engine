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

package eu.advance.logistics.flow.engine.model.rt;

import hu.akarnokd.reactive4java.base.Option;
import eu.advance.logistics.flow.engine.xml.typesystem.XElement;

/**
 * The diagnostic port for advance regular ports.
 * @author karnokd, 2011.06.22.
 */
public final class AdvanceParameterDiagnostic {
	/** The affected block. */
	public final AdvanceBlock block;
	/** The affected port. */
	public final AdvancePort port;
	/** The possible copy of the value within the port. */
	public final Option<XElement> value;
	/** The timestamp when the port received this value. */
	public final long timestamp = System.currentTimeMillis();
	/**
	 * Constructor.
	 * @param block the affected block
	 * @param port the affected port
	 * @param value the value
	 */
	public AdvanceParameterDiagnostic(AdvanceBlock block, AdvancePort port, Option<XElement> value) {
		this.block = block;
		this.port = port;
		this.value = value;
	}
}
