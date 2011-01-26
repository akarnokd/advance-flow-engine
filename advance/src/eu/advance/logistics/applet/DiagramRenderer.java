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

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import javax.swing.JComponent;

/**
 * The SCADA diagram panel displaying multiple diagrams and stuff.
 * @author karnokd
 *
 */
public class DiagramRenderer extends JComponent {
	/** */
	private static final long serialVersionUID = 3532682264547450007L;
	/** The time zoom factor. */
	private double zoom = 1.0;
	/** The time offset. */
	private long timeOffset;
	/** The value offset. */
	double valueOffset;
	/** The model. */
	DataDiagram model;
	/** The currently select range start. */
	private long selectionStart = -1L;
	/** The currently selected range end. */
	private long selectionEnd = -1L;
	/** The predefined color table. */
	static List<Color> colors;
	/** The ordered action list. */
	List<DiagramSeries<DataAction>> actionList;
	/** The ordered alarm list. */
	List<DiagramSeries<DataAlarm>> alarmList;
	/** The scada signal list. */
	List<DiagramSeries<DataDiagramValues<DataSignal>>> signalList;
	/** The precomputed day UTCs to display. */
	List<Long> days;
	/** The scada status series. Different series for each status value. */
	List<DiagramSeries<DataStatus>> statusList;
	/** The stroke width. */
	float strokeWidth = 2;
	/** The axis renderer. */
	private AxisRenderer axisRenderer;
	static {
		colors = new ArrayList<Color>();
		for (int red : new int[] { 0, 0x40, 0x80, 0xC0, 0xFF }) {
			for (int green : new int[] { 0, 0x40, 0x80, 0xC0, 0xFF }) {
				for (int blue : new int[] { 0, 0x40, 0x80, 0xC0, 0xFF }) {
					if (red == 0 && green == 0 && blue == 0) {
						continue;
					} else
					if (red == 0xFF && green == 0xFF && blue == 0xFF) {
						continue;
					} else {
						colors.add(new Color(red, green, blue));
					}
				}
			}
		}
		Collections.shuffle(colors, new Random(0));
	}
	/** The diagram series. */
	public static class DiagramSeries<T> {
		/** Is the series visible. */
		public boolean visible = true;
		/** The display color. */
		public Color color;
		/** The transparency value. */
		public float alpha;
		/** The series name. */
		public String name;
		/** The list of items. */
		public List<T> items;
	}
	@Override
	public void paint(Graphics g) {
		Graphics2D g2 = (Graphics2D)g;
		
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g2.setColor(Color.WHITE);
		g2.fillRect(0, 0, getWidth(), getHeight());
		
		AffineTransform at = g2.getTransform();
		
		FontMetrics fm = g2.getFontMetrics(getFont());
		int timeHeight = fm.getHeight(); // minimum distance between small ticks
		g2.translate(timeHeight / 2, timeHeight / 2);
		if (model == null || model.startTime == null) {
			return;
		}
		int diagramHeight = getHeight() - timeHeight;
		
		// the scale: milliseconds per pixel
		double timescale = computeScale();
		
		Composite baseComposite = g2.getComposite();
		// paint status background
		for (DiagramSeries<DataStatus> statuses : statusList) {
			if (!statuses.visible) {
				continue;
			}
			g2.setColor(statuses.color);
			g2.setComposite(AlphaComposite.SrcOver.derive(statuses.alpha));
			for (DataStatus status : statuses.items) {
				int x0 = (int)((status.start.getTime() - model.startTime.getTime() - getTimeOffset()) / timescale);
				int x1 = (int)((status.end.getTime() - model.startTime.getTime() - getTimeOffset()) / timescale);
				g2.fillRect(x0, 0, x1 - x0 + 1, diagramHeight);
			}
		}
		// paint graphs
		int row = 0;
		for (DiagramSeries<DataDiagramValues<DataSignal>> signals : signalList) {
			if (!signals.visible) {
				row++;
				continue;
			}
			DataDiagramValues<DataSignal> sig = signals.items.get(0);

			Color c = signals.color;
			Color c2 = c.darker();
			g2.setComposite(AlphaComposite.SrcOver.derive(signals.alpha));
			int lastx = 0;
			int lasty = 0;
			if (sig.minimum != null && !sig.minimum.equals(sig.maximum)) {
				BigDecimal minmax = sig.maximum.subtract(sig.minimum);
				boolean wasLast = false;
				List<Integer> xlist = new ArrayList<Integer>(sig.values.size() + 1);
				List<Integer> ylist = new ArrayList<Integer>(sig.values.size() + 1);
				for (DataSignal ssg : sig.values) {
					if (ssg.value != null) {
						int x = (int)((ssg.timestamp.getTime() - model.startTime.getTime() - getTimeOffset()) / timescale);
						int y = (int)(diagramHeight - 1 - (ssg.value.subtract(sig.minimum).divide(minmax, 2, RoundingMode.HALF_UP)).doubleValue() * diagramHeight);
						if (y < 0) {
							y = 0;
						}
						Stroke s = g2.getStroke();
						if (wasLast) {
							g2.setColor(c);
							g2.fillPolygon(new int[] { lastx, lastx, x, x }, new int[] { diagramHeight - 1, lasty, y, diagramHeight - 1 }, 4);
						}
						g2.setStroke(s);
						xlist.add(x);
						ylist.add(y);
						
						lastx = x;
						lasty = y;
						wasLast = true;
					}
				}
				xlist.add(lastx);
				ylist.add(getHeight() - 1);
				int[] xs = toIntArray(xlist);
				int[] ys = toIntArray(ylist);
				g2.setColor(c2);
				Stroke s = g2.getStroke();
				g2.setStroke(new BasicStroke(strokeWidth));
				g2.drawPolyline(xs, ys, xs.length);
				g2.setStroke(s);
			}
			row++;
		}
		row = 0;
		double laneHeight = diagramHeight * 1.0 / actionList.size();
		for (DiagramSeries<DataAction> actions : actionList) {
			if (!actions.visible) {
				row++;
				continue;
			}
			g2.setColor(actions.color);
			g2.setComposite(AlphaComposite.SrcOver.derive(actions.alpha));
			for (DataAction action : actions.items) {
				int x = (int)((action.timestamp.getTime() - model.startTime.getTime() - getTimeOffset()) / timescale);
				int y = (int)((row + 0.5) * laneHeight);
				switch (row % 3) {
				case 0:
					g2.fillRect(x - 5, y - 5, 10, 10);
					break;
				case 1:
					g2.fillOval(x - 5, y - 5, 10, 10);
					break;
				case 2:
					g2.fillPolygon(new int[] {x - 5, x, x + 5, x }, new int[] {y, y - 5, y, y + 5 }, 4);
					break;
				default:
				}
			}
			row++;
		}
		row = 0;
		laneHeight = diagramHeight * 1.0 / alarmList.size();
		for (DiagramSeries<DataAlarm> alarms : alarmList) {
			if (!alarms.visible) {
				row++;
				continue;
			}
			g2.setComposite(AlphaComposite.SrcOver.derive(alarms.alpha));
			for (DataAlarm alarm : alarms.items) {
				int x0 = (int)((alarm.start.getTime() - model.startTime.getTime() - getTimeOffset()) / timescale);
				int x1 = (int)((alarm.end.getTime() - model.startTime.getTime() - getTimeOffset()) / timescale);
				int y = (int)((row + 0.5) * laneHeight - laneHeight * 0.4);
				int h = (int)(laneHeight * 0.8);
				
				g2.setColor(alarms.color);
				g2.fillRect(x0, y, x1 - x0 + 1, h);
				g2.setColor(alarms.color.darker());
				g2.drawRect(x0, y, x1 - x0 + 1, h);
			}
			row++;
		}
		
		g2.setComposite(baseComposite);
		// paint day separators.
		g2.setColor(Color.DARK_GRAY);
		for (Long day : days) {
			int x = (int)((day - model.startTime.getTime() - getTimeOffset()) / timescale);
			g2.drawLine(x, -timeHeight / 2, x, getHeight());
		}
		
		if (selectionStart >= 0L) {
			g2.setComposite(AlphaComposite.SrcOver.derive(0.5f));
			g2.setColor(Color.BLUE);
			if (selectionStart != selectionEnd) {
				int x = (int)((selectionStart - model.startTime.getTime() - getTimeOffset()) / timescale);
				int x2 = (int)((selectionEnd - model.startTime.getTime() - getTimeOffset()) / timescale);
				if (x > x2) {
					int x1 = x2;
					x2 = x;
					x = x1;
				}
				g2.fillRect(x, 0, x2 - x + 1, diagramHeight);
			} else {
				int x = (int)((selectionStart - model.startTime.getTime() - getTimeOffset()) / timescale);
				g2.drawLine(x, 0, x, diagramHeight);
			}
		}
		g2.setComposite(AlphaComposite.SrcOver.derive(0.75f));
		g2.setColor(Color.DARK_GRAY);
		g2.setTransform(at);
		if (axisRenderer != null) {
			axisRenderer.paint(g2, getWidth());
		}
	}
	/**
	 * Convert the list of integers to array of ints.
	 * @param list the integer list
	 * @return the array of ints
	 */
	int[] toIntArray(List<Integer> list) {
		int[] result = new int[list.size()];
		for (int i = 0; i < result.length; i++) {
			result[i] = list.get(i);
		}
		return result;
	}
	/**
	 * Set the backing model.
	 * @param model the model
	 */
	public void setModel(DataDiagram model) {
		this.model = model;
		prepareModel();
		invalidate();
		repaint();
	}
	/** Prepare the model for display. */
	void prepareModel() {
		
		statusList = new ArrayList<DiagramSeries<DataStatus>>();
		
		Map<Long, DiagramSeries<DataStatus>> map = new HashMap<Long, DiagramSeries<DataStatus>>();
		
		for (DataStatus status : model.statuses) {
			DiagramSeries<DataStatus> e = map.get(status.startStatus);
			if (e == null) {
				e = new DiagramSeries<DataStatus>();
				map.put(status.startStatus, e);
				statusList.add(e);
				e.alpha = 0.75f;
				e.name = model.get("Status:") + " " + status.name + " " + status.startStatus;
				e.items = new ArrayList<DataStatus>();
				e.color = colors.get((int)(status.startStatus % colors.size()));
			}
			e.items.add(status);
		}
		
		actionList = new ArrayList<DiagramSeries<DataAction>>();
		
		int i = 0;
		for (Map.Entry<String, List<DataAction>> items : model.actions.entrySet()) {
			DiagramSeries<DataAction> item = new DiagramSeries<DataAction>();
			
			item.items = items.getValue();
			item.alpha = 0.75f;
			item.name = items.getKey();
			item.color = colors.get(((i / 3) % colors.size()));
			
			actionList.add(item);
			i++;
		}
		
		Collections.sort(actionList, new Comparator<DiagramSeries<DataAction>>() {
			@Override
			public int compare(DiagramSeries<DataAction> o1,
					DiagramSeries<DataAction> o2) {
				return o1.name.compareTo(o2.name);
			}
		});
		
		
		alarmList = new ArrayList<DiagramSeries<DataAlarm>>();
		i = 0;
		for (Map.Entry<String, List<DataAlarm>> items : model.alarms.entrySet()) {
			DiagramSeries<DataAlarm> item = new DiagramSeries<DataAlarm>();
			item.items = items.getValue();
			item.alpha = 0.75f;
			item.name = items.getKey();
			item.color = colors.get(((colors.size() - 1) - i % colors.size()));
			i++;
			alarmList.add(item);
		}
		
		Collections.sort(alarmList, new Comparator<DiagramSeries<DataAlarm>>() {
			@Override
			public int compare(DiagramSeries<DataAlarm> o1,
					DiagramSeries<DataAlarm> o2) {
				return o1.name.compareTo(o2.name);
			}
		});
		signalList = new ArrayList<DiagramSeries<DataDiagramValues<DataSignal>>>();
		i = 0;
		for (Map.Entry<String, DataDiagramValues<DataSignal>> items : model.signals.entrySet()) {
			DiagramSeries<DataDiagramValues<DataSignal>> item = new DiagramSeries<DataDiagramValues<DataSignal>>();
			item.name = items.getKey();
			item.alpha = 0.75f;
			item.color = colors.get(((i) % colors.size()));
			item.items = Collections.singletonList(items.getValue());
			i++;
			signalList.add(item);
		}
		
		
		if (model.startTime != null) {
			GregorianCalendar cal = new GregorianCalendar();
			cal.setTimeInMillis(model.startTime.getTime());
			cal.set(GregorianCalendar.HOUR_OF_DAY, 0);
			cal.set(GregorianCalendar.MINUTE, 0);
			cal.set(GregorianCalendar.SECOND, 0);
			cal.set(GregorianCalendar.MILLISECOND, 0);
			days = new ArrayList<Long>();
			while (cal.getTimeInMillis() < model.endTime.getTime()) {
				days.add(cal.getTimeInMillis());
				cal.add(GregorianCalendar.DATE, 1);
			}
		}
	}
	/** 
	 * Set the RELATIVE time offset for display. 
	 * @param timeOffset the relative time offset
	 */
	public void setTimeOffset(long timeOffset) {
		this.timeOffset = timeOffset;
	}

	/**
	 * @return the RELATIVE time offset
	 */
	public long getTimeOffset() {
		return timeOffset;
	}
	/**
	 * Set the zoom factor.
	 * @param zoom the zoom factor
	 */
	public void setZoom(double zoom) {
		this.zoom = zoom;
	}
	/**
	 * @return the zoom factor
	 */
	public double getZoom() {
		return zoom;
	}
	@Override
	public Dimension getPreferredSize() {
		return new Dimension(640, 640);
	}
	/**
	 * @return Compute the milliseconds/pixel ratio based on the current timescale and component drawing width
	 */
	public double computeScale() {
		return zoom * (getTimeRange()) / getDiagramWidth();
	}
	/**
	 * @return the diagram's global time range
	 */
	public long getTimeRange() {
		return model.endTime.getTime() - model.startTime.getTime();
	}
	/**
	 * @return computes the available diagram width by using padding
	 */
	public int getDiagramWidth() {
		return (getWidth() - getFontMetrics(getFont()).getHeight() / 2);
	}
	/**
	 * Set the selection range to a specific time point. Does not repaint.
	 * @param time the time
	 */
	public void setSelection(long time) {
		this.selectionStart = time;
		this.selectionEnd = time;
	}
	/**
	 * Set the start of the selection range. Does not repaint
	 * @param time the selection end time
	 */
	public void setSelectionStart(long time) {
		this.selectionStart = time;
	}
	/**
	 * Set the end of the selection range. Does not repaint
	 * @param time the selection end time
	 */
	public void setSelectionEnd(long time) {
		this.selectionEnd = time;
	}
	/**
	 * Clear the current time selection. Does not repaint.
	 */
	public void clearSelection() {
		this.selectionStart = -1L;
		this.selectionEnd = -1L;
	}
	/**
	 * Retrieves the displayed time value at the given pixel location.
	 * @param x the X coordinate
	 * @return the timestamp or -1 if no model
	 */
	public long getTimeAt(int x) {
		if (model != null && model.startTime != null) {
			return (long)(model.startTime.getTime() + timeOffset + (x - getFontMetrics(getFont()).getHeight() / 2) * computeScale());
		}
		return -1;
	}
	/**
	 * @return the current selection start
	 */
	public long getSelectionStart() {
		return selectionStart;
	}
	/**
	 * @return the current selection end
	 */
	public long getSelectionEnd() {
		return selectionEnd;
	}
	/**
	 * @return Returns the current model
	 */
	public DataDiagram getModel() {
		return model;
	}
	/**
	 * @return returns the model's start timestamp or -1 if no info
	 */
	public long getStartTime() {
		if (model != null && model.startTime != null) {
			return model.startTime.getTime();
		}
		return -1;
	}
	/**
	 * @return returns the model's end timestamp or -1 if no info
	 */
	public long getEndTime() {
		if (model != null && model.endTime != null) {
			return model.endTime.getTime();
		}
		return -1;
	}
	/**
	 * @return the status list diagram series
	 */
	public List<DiagramSeries<DataStatus>> getStatusList() {
		return statusList;
	}
	/**
	 * @return the signal list diagram series
	 */
	public List<DiagramSeries<DataDiagramValues<DataSignal>>> getSignalList() {
		return signalList;
	}
	/**
	 * @return the alarm list diagram series
	 */
	public List<DiagramSeries<DataAlarm>> getAlarmList() {
		return alarmList;
	}
	/**
	 * @return the action list diagram series
	 */
	public List<DiagramSeries<DataAction>> getActionList() {
		return actionList;
	}
	/**
	 * Set the axis renderer.
	 * @param axisRenderer the axis renderer
	 */
	public void setAxisRenderer(AxisRenderer axisRenderer) {
		this.axisRenderer = axisRenderer;
	}
	/**
	 * Retrieve the axis renderer.
	 * @return the axis renderer
	 */
	public AxisRenderer getAxisRenderer() {
		return axisRenderer;
	}
}
