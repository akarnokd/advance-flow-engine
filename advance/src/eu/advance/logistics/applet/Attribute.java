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

package eu.advance.logistics.applet;

import java.io.Serializable;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import eu.advance.logistics.xml.XMLParser;

/**
 * A named and valued attribute of a machine or schedule item.
 * @author karnokd, 2008.02.05.
 * @version $Revision 1.0$
 */
public class Attribute implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -6966039453182902573L;
	/**
	 * The attribute name. Should not be null.
	 */
	public String name;
	/**
	 * The attribute value. Can be null, but won't show anything
	 */
	public String value;
	/**
	 * Constructor.
	 */
	public Attribute() {
		// no op
	}
	/**
	 * Constructor.
	 * @param name the name
	 * @param value the value
	 */
	public Attribute(String name, String value) {
		this.name = name;
		this.value = value;
	}
	/**
	 * Serialize the object to a writer.
	 * @param writer the writer, cannot be null
	 * @throws XMLStreamException on error
	 */
	public void serialize(XMLStreamWriter writer) throws XMLStreamException {
		writer.writeStartElement("Attribute");
		writer.writeAttribute("name", name);
		writer.writeCharacters(value != null ? value : "");
		writer.writeEndElement();
	}
	/**
	 * Build an attribute object from the parser.
	 * @param parser the parser object currently on an Attribute element
	 * @return the built attribute.
	 * @throws XMLStreamException on error
	 */
	public static Attribute build(XMLParser parser) throws XMLStreamException {
		Attribute result = new Attribute();
		result.name = parser.getAttributeString("name");
		result.value = parser.getString();
		return result;
	}
	/**
	 * Copy an attribute.
	 * @param a the attribute to copy, not null
	 * @return the copy of attribute
	 */
	public static Attribute copy(Attribute a) {
		return new Attribute(a.name, a.value);
	}
}
