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

package eu.advance.logistics.flow.engine.comm;

import java.io.Closeable;
import java.io.IOException;
import java.util.Collection;

import eu.advance.logistics.flow.engine.xml.typesystem.XElement;

/**
 * Represents a SOAP connection.
 * @author karnokd, 2011.10.06.
 */
public class SOAPConnection implements Closeable {
	/** The SOAP envelope namespace. */
	public static final String SOAP_NAMESPACE = "http://www.w3.org/2003/05/soap-envelope";
	/** The SOAP encoding style namespace. */
	public static final String SOAP_ENCODING = "http://www.w3.org/2003/05/soap-encoding";
	/** The WS-Addressing namespace. */
	public static final String WSA_NAMESPACE = "http://schemas.xmlsoap.org/ws/2004/08/addressing";
	@Override
	public void close() throws IOException {
		// TODO Auto-generated method stub

	}
	/**
	 * Create a low-level SOAP envelope message.
	 * @param body the message body
	 * @param headers the optional collection of headers.
	 * @return the envelope object
	 */
	public static XElement createSOAPEnvelope(XElement body, Collection<XElement> headers) {
		XElement envelope = new XElement("Envelope", SOAP_NAMESPACE);
		envelope.prefix = "SOAP-ENV";
		
		if (headers.size() > 0) {
			XElement header = envelope.add("Header", SOAP_NAMESPACE);
			header.prefix = "SOAP-ENV";
			for (XElement h : headers) {
				header.add(h.copy());
			}
		}
		
		XElement xbody = envelope.add("Body", SOAP_NAMESPACE);
		xbody.prefix = "SOAP-ENV";
		xbody.add(body.copy());

		return envelope;
	}
}
