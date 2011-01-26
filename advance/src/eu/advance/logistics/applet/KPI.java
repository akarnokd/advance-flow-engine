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
 * Record to store key performance indicator values.
 * @author karnokd, 2008.02.06.
 * @version $Revision 1.0$
 */
public class KPI implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 248251557227012316L;
	/**
	 * The name of the indicator.
	 */
	public String name;
	/**
	 * The optional value of the indicator.
	 */
	public String value;
	/**
	 * The optional tooltip associated with the value.
	 */
	public String tooltip;
	/**
	 * Constructor.
	 */
	public KPI() {
		// empty
	}
	/**
	 * Constructor.
	 * @param name the name
	 * @param value the optional value
	 * @param tooltip the optional tooltip
	 */
	public KPI(String name, String value, String tooltip) {
		this.name = name;
		this.value = value;
		this.tooltip = tooltip;
	}
	/**
	 * Serialize object to the writer.
	 * @param writer the writer, cannot be null
	 * @throws XMLStreamException on error
	 */
	public void serialize(XMLStreamWriter writer) throws XMLStreamException {
		writer.writeStartElement("KPI");
		writer.writeAttribute("name", name != null ? name : "");
		if (tooltip != null) {
			writer.writeAttribute("tooltip", tooltip);
		}
		writer.writeCharacters(value != null ? value : "");
		writer.writeEndElement();
	}
	/**
	 * Create a KPI record from the parser.
	 * @param parser the parser object positioned on a KPI element.
	 * @return the created object
	 * @throws XMLStreamException on error
	 */
	public static KPI build(XMLParser parser) throws XMLStreamException {
		KPI result = new KPI();
		result.name = parser.getAttributeString("name");
		result.tooltip = parser.getAttributeString("tooltip");
		result.value = parser.getString(); 
		return result;
	}
	/**
	 * Copy the given KPI object.
	 * @param k the object to copy, cannot be null
	 * @return the copy of the object
	 */
	public static KPI copy(KPI k) {
		KPI result = new KPI();
		result.name = k.name;
		result.value = k.value;
		result.tooltip = k.tooltip;
		return result;
	}
}
