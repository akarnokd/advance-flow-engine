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

import java.text.ParseException;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.advance.logistics.flow.engine.xml.typesystem.XElement;
import eu.advance.logistics.flow.engine.xml.typesystem.XSerializable;


/**
 * The diagnostic port for the ADVANCE blocks.
 * @author karnokd, 2011.06.22.
 */
public final class AdvanceBlockDiagnostic implements XSerializable {
	/** The logger. */
	protected static final Logger LOG = LoggerFactory.getLogger(AdvanceBlockDiagnostic.class);
	/** The affected block. */
	public String blockId;
	/** The target ream. */
	public String realm;
	/** The possible copy of the value within the port. */
	public Option<AdvanceBlockState> state;
	/** The timestamp when the port received this value. */
	public Date timestamp = new Date();
	/**
	 * Constructor.
	 * @param realm the realm in question
	 * @param blockId the affected block identifier
	 * @param state the block state
	 */
	public AdvanceBlockDiagnostic(String realm, String blockId, Option<AdvanceBlockState> state) {
		this.blockId = realm;
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
			state = Option.some(AdvanceBlockState.valueOf(stateStr));
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
