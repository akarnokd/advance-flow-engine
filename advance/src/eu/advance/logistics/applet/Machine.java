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
import java.util.ArrayList;
import java.util.List;
import java.util.NavigableMap;
import java.util.TreeMap;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import eu.advance.logistics.xml.XMLParser;

/**
 * Describes a machine with its name, key performance indicators,
 * schedule and shift.
 * @author karnokd, 2008.02.05.
 * @version $Revision 1.0$
 */
public class Machine implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -2050844705985533014L;
	/** The presentation index of the machine. */
	int index;
	/** The task map sorted by start date. */
	NavigableMap<Long, Task> taskMap = new TreeMap<Long, Task>();
	/**
	 * The name (identifier) of the machine.
	 */
	public String name;
	/**
	 * The optional tooltip for the machine name.
	 */
	public String tooltip;
	/**
	 * Map of key performance indicator values identified by
	 * the type of the indicator. Can be null or empty meaning no 
	 * KPIs are available.
	 */
	public final List<KPI> kpis = new ArrayList<KPI>();
	/**
	 * The optional list of schedule on the machine.
	 */
	public final List<Task> tasks = new ArrayList<Task>();
	/**
	 * Optional list of work shifts.
	 */
	public final List<WorkShift> shifts = new ArrayList<WorkShift>();
	/**
	 * List of attributes which will be listed in the tooltip/detail dialog.
	 */
	public final List<Attribute> attributes = new ArrayList<Attribute>();
	/**
	 * Build a machine from the parser.
	 * @param parser the parser to read from
	 * @return the built machine object
	 * @throws XMLStreamException on error
	 */
	public static Machine build(XMLParser parser) throws XMLStreamException {
		Machine result = new Machine();
		result.name = parser.getAttributeString("name");
		result.tooltip = parser.getAttributeString("tooltip");
		// Position on Tasks
		parser.nextElement();
		while (parser.tryNextElement("Task")) {
			Task t = Task.build(parser);
			t.machine = result;
			result.tasks.add(t);
			parser.endElement();
		}
		// Position on Shifts
		parser.nextElement();
		while (parser.tryNextElement("Shift")) {
			result.shifts.add(WorkShift.build(parser));
			parser.endElement();
		}
		// Position on KPIs
		parser.nextElement();
		while (parser.tryNextElement("KPI")) {
			result.kpis.add(KPI.build(parser));
			//parser.endElement();
		}
		// Position on Attributes
		parser.nextElement();
		while (parser.tryNextElement("Attribute")) {
			result.attributes.add(Attribute.build(parser));
			parser.endElement();
		}
		
		return result;
	}
	/**
	 * Serialize the current element into the writer.
	 * @param writer the writer, cannot be null
	 * @throws XMLStreamException on stream error
	 */
	public void serialize(XMLStreamWriter writer) throws XMLStreamException {
		writer.writeStartElement("Machine");
		writer.writeAttribute("name", name);
		if (tooltip != null) {
			writer.writeAttribute("tooltip", tooltip);
		}
		writer.writeStartElement("Tasks");
		for (Task t : tasks) {
			t.serialize(writer);
		}
		writer.writeEndElement();
		
		writer.writeStartElement("Shifts");
		for (WorkShift t : shifts) {
			t.serialize(writer);
		}
		writer.writeEndElement();

		writer.writeStartElement("KPIs");
		for (KPI k : kpis) {
			k.serialize(writer);
		}
		writer.writeEndElement();
		
		writer.writeStartElement("Attributes");
		for (Attribute a : attributes) {
			a.serialize(writer);
		}
		writer.writeEndElement();
		
		writer.writeEndElement();
	}
	/**
	 * Creates a deep copy of the given machine.
	 * @param m the machine to copy, cannot be null
	 * @return the clone Machine object
	 */
	public static Machine copy(Machine m) {
		Machine result = new Machine();
		result.name = m.name;
		result.tooltip = m.tooltip;
		for (Task t : m.tasks) {
			result.tasks.add(Task.copy(t));
		}
		for (WorkShift s : m.shifts) {
			result.shifts.add(WorkShift.copy(s));
		}
		for (KPI k : m.kpis) {
			result.kpis.add(KPI.copy(k));
		}
		for (Attribute a : m.attributes) {
			result.attributes.add(Attribute.copy(a));
		}
		return result;
	}
}
