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
 * The diagnostic port for advance regular ports.
 * @author akarnokd, 2011.06.22.
 */
public final class PortDiagnostic implements XSerializable {
	/** The logger. */
	protected static final Logger LOG = LoggerFactory.getLogger(PortDiagnostic.class);
	/** The realm. */
	public String realm;
	/** The affected block. */
	public String blockId;
	/** The affected port. */
	public String port;
	/** The possible copy of the value within the port. */
	public Option<?> value;
	/** The timestamp when the port received this value. */
	public Date timestamp = new Date();
	/** Creates a new instance of this class. */
	public static final Func0<PortDiagnostic> CREATOR = new Func0<PortDiagnostic>() {
		@Override
		public PortDiagnostic invoke() {
			return new PortDiagnostic();
		}
	};
	/** Creates an empty object to be filled in by load(). */
	private PortDiagnostic() {
		
	}
	/**
	 * Constructor.
	 * @param realm the realm
	 * @param blockId the affected block
	 * @param port the affected port
	 * @param value the value
	 */
	public PortDiagnostic(String realm, String blockId, String port, Option<?> value) {
		this.realm = realm;
		this.blockId = blockId;
		this.port = port;
		this.value = value;
	}
	@Override
	public void load(XElement source) {
		realm = source.get("realm");
		blockId = source.get("block-id");
		port = source.get("port");
		try {
			timestamp = XElement.parseDateTime(source.get("timestamp"));
		} catch (ParseException ex) {
			LOG.error(ex.toString(), ex);
		}
		if (source.children().size() >= 1) {
			value = Option.some(source.children().get(0).copy());
		} else {
			value = Option.none();
		}
	}
	@Override
	public void save(XElement destination) {
		destination.set("realm", realm);
		destination.set("block-id", blockId);
		destination.set("port", port);
		destination.set("timestamp", timestamp);
		if (Option.isSome(value)) {
			Object val = value.value();
			if (val instanceof XElement) {
				XElement v = (XElement)value.value();
				destination.add(v.copy());
			} else
			if (val instanceof XSerializable) {
				XElement v = new XElement(value.value().getClass().getSimpleName());
				((XSerializable)val).save(v);
				destination.add(v);
			} else {
				if (val != null) {
					destination.content = value.value().getClass() + " " + value.value().toString();
				}
			}
		}
	}
}