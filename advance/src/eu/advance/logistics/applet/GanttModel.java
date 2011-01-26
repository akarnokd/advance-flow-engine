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
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import eu.advance.logistics.xml.XMLParser;

/**
 * Class to store the Gantt related data.
 * @author karnokd, 2008.02.05.
 * @version $Revision 1.0$
 */
public class GanttModel implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -1729874240460539440L;
	/**
	 * Is the data modell linked?
	 */
	private boolean linked;
	/**
	 * The minimum startDate of all the schedules.
	 */
	long startDate;
	/**
	 * The maximum endDate of all schedules.
	 */
	long endDate;
	/**
	 * List of optional machine data.
	 */
	public final List<Machine> machines = new ArrayList<Machine>();
	/**
	 * Builds the Gantt Data record using the given parser.
	 * @param parser the parser
	 * @return the created GanttData object, never null
	 * @throws XMLStreamException on processing error
	 */
	public static GanttModel build(XMLParser parser) throws XMLStreamException {
		GanttModel result = new GanttModel();
		// position to GanttData element
		parser.nextElement();
		// loop on machine
		while (parser.tryNextElement("Machine")) {
			Machine m = Machine.build(parser);
			m.index = result.machines.size();
			result.machines.add(m);
			parser.endElement();
		}
		result.linkTasks();
		result.linked = true;
		return result;
	}
	/**
	 * Serialize the data model. 
	 * Does not write the startDocument/endDocument to the stream.
	 * @param writer the writer to serialize into
	 * @throws XMLStreamException on error
	 */
	public void serialize(XMLStreamWriter writer) throws XMLStreamException {
		writer.writeStartElement("GanttData");
		for (Machine m : machines) {
			m.serialize(writer);
		}
		writer.writeEndElement();
	}
	/**
	 * Creates a deep copy clone of the given gantt model.
	 * @param model the gantt model to copy, cannot be null
	 * @return the cloned gantt model.
	 */
	public static GanttModel copy(GanttModel model) {
		GanttModel result = new GanttModel();
		for (Machine m : model.machines) {
			result.machines.add(Machine.copy(m));
		}
		result.linkTasks();
		return result;
	}
	/**
	 * Link tasks with the same job id into a sequence.
	 */
	void linkTasks() {
		if (linked) {
			return;
		}
		long minDate = 0;
		long maxDate = 0;
		Map<String, List<Task>> jobMap = new HashMap<String, List<Task>>();
		int machineIndex = 0;
		for (Machine m : machines) {
			m.index = machineIndex++; // index machines
			for (Task t : m.tasks) {
				m.taskMap.put(t.endDate, t);
				t.machine = m; // link to machines
				if (minDate == 0 || t.startDate < minDate) {
					minDate = t.startDate;
				}
				if (maxDate < t.endDate) {
					maxDate = t.endDate;
				}
				List<Task> tasks = jobMap.get(t.jobID);
				if (tasks == null) {
					tasks = new ArrayList<Task>();
					jobMap.put(t.jobID, tasks);
				}
				tasks.add(t);
			}
		}
		startDate = minDate;
		endDate = maxDate;
		for (List<Task> tasks : jobMap.values()) {
			Collections.sort(tasks, new Comparator<Task>() {
				@Override
				public int compare(Task o1, Task o2) {
					if (o1.index < o2.index) {
						return -1;
					} else
					if (o1.index > o2.index) {
						return 1;
					}
					return 0;
				}
			});
			Task prev = null;
			for (Task t : tasks) {
				if (prev != null) {
					prev.next = t;
				}
				t.prev = prev;
				prev = t;
			}
			if (prev != null) {
				prev.next = null;
			}
		}
	}
}
