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
import java.util.Date;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import eu.advance.logistics.xml.XMLParser;
import eu.advance.logistics.xml.XsdDateTime;


/**
 * Contains a non-zero length work shift with optional
 * resource listing.
 * @author karnokd, 2008.02.05.
 * @version $Revision 1.0$
 */
public class WorkShift implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -729474564025904453L;
	/**
	 * The shift start date UTC.
	 */
	public long startDate;
	/**
	 * The shift end date UTC.
	 */
	public long endDate;
	/**
	 * Serialize object to the writer.
	 * @param writer the stream writer, cannot be null
	 * @throws XMLStreamException on error
	 */
	public void serialize(XMLStreamWriter writer) throws XMLStreamException {
		writer.writeStartElement("Shift");
		
		writer.writeStartElement("StartDate");
		writer.writeCharacters(XsdDateTime.format(new Date(startDate)));
		writer.writeEndElement();
		
		writer.writeStartElement("EndDate");
		writer.writeCharacters(XsdDateTime.format(new Date(endDate)));
		writer.writeEndElement();
		
		writer.writeEndElement();
	}
	/**
	 * Build a WorkShift object from the parser.
	 * @param parser the parser positioned on a Shift object
	 * @return the built WorkShift object
	 * @throws XMLStreamException on error
	 */
	public static WorkShift build(XMLParser parser) throws XMLStreamException {
		WorkShift result = new WorkShift();
		parser.nextElement();
		result.startDate = parser.getDateUTC();
		parser.nextElement();
		result.endDate = parser.getDateUTC();
		return result;
	}
	/**
	 * Copy the work shift.
	 * @param s the object to copy, cannot be null
	 * @return the copy of the object
	 */
	public static WorkShift copy(WorkShift s) {
		WorkShift result = new WorkShift();
		result.startDate = s.startDate;
		result.endDate = s.endDate;
		return result;
	}
}
