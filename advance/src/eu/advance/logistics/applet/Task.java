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

import java.awt.Color;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import eu.advance.logistics.xml.XMLParser;
import eu.advance.logistics.xml.XsdDateTime;

/**
 * Contains an unit of work on one machine.
 * @author karnokd, 2008.02.05.
 * @version $Revision 1.0$
 */
public class Task implements Serializable {
	/** */
	private static final long serialVersionUID = -1371949826819545716L;
	/** The calculated x coordinates after rendering. */
	int x;
	/** The calculated y coordinates after rendering. */
	int y;
	/** The calculated x2 coordinates after rendering. */
	int x2;
	/** The previous task element, null if this is the first task. */
	Task prev;
	/** The next task element, null if this is the last task. */
	Task next;
	/** Backlink to the owner machine. */
	Machine machine;
	/** Task is selected on screen? */
	boolean selected;
	/**
	 * An identifier to allow visual linking of the schedule
	 * if it is scattered among multiple machines.
	 */
	public String jobID;
	/**
	 * The index of the schedule if the task is scattered among different
	 * machines.
	 */
	public int index;
	/**
	 * The start date of the work.
	 */
	public long startDate;
	/**
	 * The end date of the work.
	 */
	public long endDate;
	/**
	 * The color of the schedule.
	 */
	public Color color = Color.GREEN;
	/**
	 * The fill pattern to fill in the schedule box.
	 */
	public FillPattern pattern = FillPattern.SOLID;
	/**
	 * The optional list of attributes.
	 */
	public final List<Attribute> attributes = new ArrayList<Attribute>();
	/**
	 * Serialize the object.
	 * @param writer the stream writer, cannot be null
	 * @throws XMLStreamException on error
	 */
	public void serialize(XMLStreamWriter writer) throws XMLStreamException {
		writer.writeStartElement("Task");
		
		writer.writeStartElement("JobID");
		writer.writeAttribute("index", Integer.toString(index));
		writer.writeCharacters(jobID);
		writer.writeEndElement();
		
		writer.writeStartElement("StartDate");
		writer.writeCharacters(XsdDateTime.format(new Date(startDate)));
		writer.writeEndElement();
		
		writer.writeStartElement("EndDate");
		writer.writeCharacters(XsdDateTime.format(new Date(endDate)));
		writer.writeEndElement();
		
		if (color != null) {
			writer.writeStartElement("Color");
			StringBuilder b = new StringBuilder();
			int c = color.getRed();
			if (c < 16) {
				b.append('0');
			}
			b.append(Integer.toHexString(c));
			c = color.getGreen();
			if (c < 16) {
				b.append('0');
			}
			b.append(Integer.toHexString(c));
			c = color.getBlue();
			if (c < 16) {
				b.append('0');
			}
			b.append(Integer.toHexString(c));
			writer.writeCharacters(b.toString());
			writer.writeEndElement();
		} else {
			writer.writeEmptyElement("Color");
		}
		
		if (pattern != null) {
			writer.writeStartElement("Pattern");
			writer.writeCharacters(pattern.name());
			writer.writeEndElement();
		} else {
			writer.writeEmptyElement("Pattern");
		}
		writer.writeStartElement("Attributes");
		for (Attribute a : attributes) {
			a.serialize(writer);
		}
		writer.writeEndElement();
		
		writer.writeEndElement();
	}
	/**
	 * Build a Task object from the parser. Current element should be: Task.
	 * @param parser the parser to read from
	 * @return the built object
	 * @throws XMLStreamException on error.
	 */
	public static Task build(XMLParser parser) throws XMLStreamException {
		Task result = new Task();
		parser.nextElement();
		Integer intValue = parser.getAttributeInteger("index");
		if (intValue != null) {
			result.index = intValue;
		}
		result.jobID = parser.getString();
		parser.nextElement();
		result.startDate = parser.getDateUTC();
		parser.nextElement();
		result.endDate = parser.getDateUTC();
		parser.nextElement();
		String value = parser.getString();
		if (value.length() != 6) {
			throw new XMLStreamException("Value is not 6 long");
		}
		try {
			result.color = new Color(Integer.parseInt(value.substring(0, 2), 16), 
					Integer.parseInt(value.substring(2, 4), 16),
					Integer.parseInt(value.substring(4, 6), 16));
		} catch (NumberFormatException ex) {
			throw new XMLStreamException(ex);
		}
		parser.nextElement();
		value = parser.getString();
		result.pattern = FillPattern.valueOf(value);
		parser.nextElement();
		// loop until
		while (parser.tryNextElement("Attribute")) {
			result.attributes.add(Attribute.build(parser));
		}
		return result;
	}
	/**
	 * Copy the Task object.
	 * @param t the object to copy, cannot be null
	 * @return the copy of the object
	 */
	public static Task copy(Task t) {
		Task result = new Task();
		result.jobID = t.jobID;
		result.index = t.index;
		result.startDate = t.startDate;
		result.endDate = t.endDate;
		result.color = t.color;
		result.pattern = t.pattern;
		for (Attribute a : t.attributes) {
			result.attributes.add(Attribute.copy(a));
		}
		return result;
	}
}
