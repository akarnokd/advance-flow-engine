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

package eu.advance.logistics.flow.engine.runtime;

import hu.akarnokd.reactive4java.base.Func0;
import hu.akarnokd.reactive4java.base.Option;

import java.text.ParseException;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.advance.logistics.flow.engine.xml.XElement;
import eu.advance.logistics.flow.engine.xml.XSerializable;


/**
 * The diagnostic port for the ADVANCE blocks.
 * @author akarnokd, 2011.06.22.
 */
public final class BlockDiagnostic implements XSerializable {
	/** The logger. */
	protected static final Logger LOG = LoggerFactory.getLogger(BlockDiagnostic.class);
	/** The affected block. */
	public String blockId;
	/** The target ream. */
	public String realm;
	/** The possible copy of the value within the port. */
	public Option<BlockState> state;
	/** The timestamp when the port received this value. */
	public Date timestamp = new Date();
	/** Function to create a new instance of this class. */
	public static final Func0<BlockDiagnostic> CREATOR = new Func0<BlockDiagnostic>() {
		@Override
		public BlockDiagnostic invoke() {
			return new BlockDiagnostic();
		}
	};
	/** Create an empty block diagnostic object to be filled in via load(). */
	private BlockDiagnostic() {
		
	}
	/**
	 * Constructor.
	 * @param realm the realm in question
	 * @param blockId the affected block identifier
	 * @param state the block state
	 */
	public BlockDiagnostic(String realm, String blockId, Option<BlockState> state) {
		this.realm = realm;
		this.blockId = blockId;
		this.state = state;
	}
	@Override
	public void load(XElement source) {
		realm = source.get("realm");
		blockId = source.get("block-id");
		try {
			timestamp = XElement.parseDateTime(source.get("timestamp"));
		} catch (ParseException ex) {
			LOG.error(ex.toString(), ex);
		}
		String stateStr = source.get("state");
		if (stateStr != null) {
			state = Option.some(BlockState.valueOf(stateStr));
		} else {
			state = Option.none();
		}
	}
	@Override
	public void save(XElement destination) {
		destination.set("realm", realm);
		destination.set("block-id", blockId);
		destination.set("timestamp", timestamp);
		if (state instanceof Option.Some<?>) {
			destination.set("state", state.value());
		}
	}
}
