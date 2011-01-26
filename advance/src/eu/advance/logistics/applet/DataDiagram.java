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
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * The composite scada diagram object with various signals and events.
 * @author karnokd
 */
public class DataDiagram implements Serializable {
	/** */
	private static final long serialVersionUID = 532198905563857729L;
	/** The map from signal name to an timestamp ordered list of scada signal values. */
	public Map<String, DataDiagramValues<DataSignal>> signals;
	/** The map from alarm name to a start-timestamp ordered list of alarm durations. */
	public Map<String, List<DataAlarm>> alarms;
	/** The map from action name to a timestamp ordered list of action events. */
	public Map<String, List<DataAction>> actions;
	/** A start-timestamp ordered list of status changes. */
	public List<DataStatus> statuses;
	/** The earliest signal among the signals and alarms. */
	public Timestamp startTime;
	/** The latest signal among the signals and alarms. */
	public Timestamp endTime;
	/** The UI labels. */
	public Map<String, String> labels;
	/**
	 * Add the list of scada alarms to the alarms map and update the start-time values accordingly.
	 * @param name the alarm name
	 * @param scadaAlarms the list of scada alarm records
	 */
	public void addAlarms(String name, List<DataAlarm> scadaAlarms) {
		List<DataAlarm> targetAlarms = alarms.get(name);
		if (targetAlarms == null) {
			targetAlarms = new ArrayList<DataAlarm>();
			alarms.put(name, targetAlarms);
		}
		for (DataAlarm sa : scadaAlarms) {
			targetAlarms.add(sa);
			if (startTime == null || startTime.compareTo(sa.start) > 0) {
				startTime = sa.start;
			}
			if (startTime == null || (sa.end != null && startTime.compareTo(sa.end) > 0)) {
				startTime = sa.end;
			}
			if (endTime == null || endTime.compareTo(sa.start) < 0) {
				endTime = sa.start;
			}
			if (endTime == null || (sa.end != null && endTime.compareTo(sa.end) < 0)) {
				endTime = sa.end;
			}
		}
	}
	/**
	 * Add the list of scada alarms to the alarms map and update the start-time values accordingly.
	 * @param name the action name
	 * @param scadaActions the list of scada action records
	 */
	public void addActions(String name, List<DataAction> scadaActions) {
		List<DataAction> targetActions = actions.get(name);
		if (targetActions == null) {
			targetActions = new ArrayList<DataAction>();
			actions.put(name, targetActions);
		}
		for (DataAction sa : scadaActions) {
			targetActions.add(sa);
			if (startTime == null || startTime.compareTo(sa.timestamp) > 0) {
				startTime = sa.timestamp;
			}
			if (endTime == null || endTime.compareTo(sa.timestamp) < 0) {
				endTime = sa.timestamp;
			}
		}
	}
	/**
	 * Add the list of scada alarms to the alarms map and update the start-time values accordingly.
	 * @param name the signal name
	 * @param scadaSignals the list of scada signal records
	 */
	public void addSignals(String name, List<DataSignal> scadaSignals) {
		DataDiagramValues<DataSignal> targetSignals = signals.get(name);
		if (targetSignals == null) {
			targetSignals = new DataDiagramValues<DataSignal>();
			targetSignals.values = new ArrayList<DataSignal>();
			signals.put(name, targetSignals);
		}
		for (DataSignal sa : scadaSignals) {
			sa.signalName = name;
			targetSignals.values.add(sa);
			if (startTime == null || startTime.compareTo(sa.timestamp) > 0) {
				startTime = sa.timestamp;
			}
			if (endTime == null || endTime.compareTo(sa.timestamp) < 0) {
				endTime = sa.timestamp;
			}
			if (sa.value != null && (targetSignals.minimum == null || targetSignals.minimum.compareTo(sa.value) > 0)) {
				targetSignals.minimum = sa.value;
			}
			if (sa.value != null && (targetSignals.maximum == null || targetSignals.maximum.compareTo(sa.value) < 0)) {
				targetSignals.maximum = sa.value;
			}
		}
	}
	/**
	 * Add the list of scada alarms to the alarms map and update the start-time values accordingly.
	 * @param scadaStatuses the list of scada status records
	 */
	public void addStatuses(List<DataStatus> scadaStatuses) {
		for (DataStatus sa : scadaStatuses) {
			statuses.add(sa);
			if (startTime == null || startTime.compareTo(sa.start) > 0) {
				startTime = sa.start;
			}
			if (startTime == null || (sa.end != null && startTime.compareTo(sa.end) > 0)) {
				startTime = sa.end;
			}
			if (endTime == null || endTime.compareTo(sa.start) < 0) {
				endTime = sa.start;
			}
			if (endTime == null || (sa.end != null && endTime.compareTo(sa.end) < 0)) {
				endTime = sa.end;
			}
		}
	}
	/**
	 * Get a localized label.
	 * @param label the label
	 * @return the localized label
	 */
	public String get(String label) {
		String s = labels.get(label);
		if (s != null) {
			return s;
		}
		System.err.println("Missing label: " + label);
		new Exception().printStackTrace();
		return label;
	}
}
