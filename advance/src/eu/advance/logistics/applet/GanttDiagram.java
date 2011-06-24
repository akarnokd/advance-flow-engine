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



import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Stroke;
import java.awt.TexturePaint;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map.Entry;

import javax.swing.JComponent;

/**
 * Gantt diagram for multilayer schedule.
 * @author Karnok Dávid, 2007.06.04.
 * @version $Revision 1.0$
 */
public class GanttDiagram extends JComponent {
	/**
	 * A composite key for texture caching.
	 * @author karnokd, 2008.02.06.
	 * @version $Revision 1.0$
	 */
	private static class PatternColor {
		/**
		 * The pattern.
		 */
		public final FillPattern pattern;
		/**
		 * The color.
		 */
		public final Color color;
		/**
		 * Constructor.
		 * @param pattern the pattern value, can be null
		 * @param color the color value, can be null
		 */
		public PatternColor(FillPattern pattern, Color color) {
			this.pattern = pattern;
			this.color = color;
		}
		/**
		 * {@inheritDoc}
		 */
		@Override
		public int hashCode() {
			int hash = 17;
			if (pattern != null) {
				hash = 37 * hash + pattern.hashCode();
			}
			if (color != null) {
				hash = 37 * hash + color.hashCode();
			}
			return hash;
		}
		/**
		 * {@inheritDoc}
		 */
		@Override
		public boolean equals(Object obj) {
			if (obj instanceof PatternColor) {
				PatternColor o = (PatternColor)obj;
				return pattern == null ? o.pattern == null : pattern.equals(o.pattern)
						&& color == null ? o.color == null : color.equals(o.color)
						;
			}
			return false;
		}
	}
	/**
	 * 
	 */
	private static final long serialVersionUID = -8605298661649196604L;
	/**
	 * The row height in pixels.
	 */
	private int rowHeight = 30;
	/**
	 * Row Starting in pixels.
	 */
	private int startPixel;
	/**
	 * Draw horizontal grid?
	 */
	private boolean drawHorizontalGrid = true;
	/**
	 * The data model.
	 */
	private GanttModel model;
	/**
	 * Show shifts.
	 */
	private boolean showShifts = true;
	/**
	 * Show tasks.
	 */
	private boolean showTasks = true;;
	/**
	 * Show work days.
	 */
	private boolean showWorkDays = true;
	/**
	 * The selected task or null if no selection.
	 */
	private Task selected;
	/**
	 * Color of each shift.
	 */
	public Color shiftColor = new Color(0xEE, 0xEE, 0xEE);
	/**
	 * Color of shifts 2.
	 */
	public Color shiftColor2 = new Color(0xE0, 0xE0, 0xE0);
	/**
	 * Line color.
	 */
	public Color line = new Color(0x80, 0x80, 0x80);
	/** The calendar for time calculations. */
	private GregorianCalendar cal = new GregorianCalendar();
	/** The entangled timeLabel. */
	private TimeLabel timeLabel;
	/** The object of the last tooltip. */
	private Task lastTooltipTask;
	/**
	 * Constructor.
	 * @param timeLabel the associated time label.
	 */
	public GanttDiagram(TimeLabel timeLabel) {
		this.timeLabel = timeLabel;
		setOpaque(true);
		addMouseMotionListener(new MouseAdapter() {
			@Override
			public void mouseMoved(MouseEvent e) {
				Task t = findTask(e.getX(), e.getY());
				if (t == lastTooltipTask) {
					return;
				}
				lastTooltipTask = t;
				if (t == null) {
					setToolTipText(null);
				} else {
					StringBuilder b = new StringBuilder("<html>");
					int i = 0;
					for (Attribute a : t.attributes) {
						if (i > 0) {
							b.append("<br>");
						}
						b.append(a.name).append(": ").append(a.value);
						i++;
					}
					setToolTipText(b.toString());
				}
			}
		});
	}
	/**
	 * Sets the underlying data model.
	 * @param model the model to set
	 */
	public void setModel(GanttModel model) {
		this.model = model;
		// clear tooltip
		lastTooltipTask = null;
		setToolTipText(null);
	}
	/**
	 * @return the underlying data model.
	 */
	public GanttModel getModel() {
		return model;
	}
	/**
	 * Custom LRU enabled cache for pattern caching.
	 * @author karnokd, 2008.02.07.
	 * @version $Revision 1.0$
	 */
	private static class PatternColorCache extends LinkedHashMap<PatternColor, BufferedImage> {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		/** The cache capacity. */
		private int capacity;
		/**
		 * Constructor.
		 * @param capacity the cache capacity.
		 */
		public PatternColorCache(int capacity) {
			this.capacity = capacity;
		}
		/**
		 * {@inheritDoc}
		 */
		@Override
		protected boolean removeEldestEntry(Entry<PatternColor, BufferedImage> eldest) {
			return size() == capacity;
		}
	};
	/**
	 * The pattern+color LRU cache.
	 */
	private PatternColorCache cache = new PatternColorCache(256);
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void paint(Graphics g) {
		Graphics2D g2 = (Graphics2D)g;
		if (isOpaque()) {
			g2.setColor(getBackground());
			g2.fillRect(0, 0, getWidth(), getHeight());
		}
		if (model == null) {
			return;
		}
		double xoffs = timeLabel.getTickSize() / 2.0;
		double scale = timeLabel.getScale();
		long timeOffset = timeLabel.getOffset();
		long displaceTime = (long)(xoffs * scale);
		long endTime = Math.round(timeOffset + scale * getWidth());
		
		int startRow = startPixel / rowHeight;
		int y = -startPixel + startRow * rowHeight;
		if (model.machines.size() > startRow) {
			Iterator<Machine> it = model.machines.listIterator(startRow);
			while (it.hasNext()) {
				Machine m = it.next();
				if (showShifts) {
					int idx = 0;
					for (WorkShift c : m.shifts) {
						if (c.startDate > endTime) {
							break;
						}
						if ((c.endDate > timeOffset - displaceTime && c.startDate < endTime - displaceTime) 
								|| (c.startDate <= timeOffset - displaceTime && c.endDate >= endTime - displaceTime)) {
							double x = (c.startDate - timeOffset) / scale + xoffs;
							double w = (c.endDate - c.startDate) / scale;
							g2.setColor(idx % 2 == 0 ? shiftColor : shiftColor2);
							int ix = (int)Math.round(x);
							int iw = (int)Math.round(w);
							g2.fillRect(ix, y, iw, rowHeight - 1);
						}
						idx++;
					}
				}
				if (showTasks) {
					Paint paintSave = g2.getPaint();
					//Stroke wide = new BasicStroke(5);
					for (Task s : m.taskMap.tailMap(timeOffset - displaceTime, true).values()) {
						if (s.startDate > endTime) {
							break;
						}
						if ((s.endDate > timeOffset - displaceTime && s.startDate < endTime - displaceTime) 
								|| (s.startDate <= timeOffset - displaceTime && s.endDate >= endTime - displaceTime)) {
							double x = (s.startDate - timeOffset) / scale + xoffs;
							double w = (s.endDate - s.startDate) / scale;
							BufferedImage image = null;
							Color color = s.color;
							if (s.selected) {
								color = new Color(255 - color.getRed(), 255 - color.getGreen(), 255 - color.getBlue());
							}
							if (s.pattern != null && s.pattern != FillPattern.NONE && s.pattern != FillPattern.SOLID) {
								PatternColor key = new PatternColor(s.pattern, color);
								image = cache.get(key);
								if (image == null) {
									image = s.pattern.getProvider().getTexture(color);
									cache.put(key, image);
								}
							}
							s.x = (int)Math.round(x);
							s.y = y;
							int iw = (int)Math.round(w);
							s.x2 = s.x + iw - 1;
							//s.y2 = y + rowHeight - 5;
							g2.setColor(color);
							if (s.pattern != FillPattern.SOLID) {
								g2.drawRect(s.x, s.y + 2, iw, rowHeight - 6);
							}
							if (s.pattern != FillPattern.NONE && s.pattern != FillPattern.SOLID) {
								g2.setPaint(new TexturePaint(image, new Rectangle2D.Double(s.x, s.y, 7, 7)));
								//g2.setPaint(new TexturePaint(image, new Rectangle2D.Double(0, 0, 7, 7)));
							} else {
								g2.setPaint(null);
							}
							if (s.pattern != FillPattern.NONE) {
								g2.fillRect(s.x, s.y + 2, iw, rowHeight - 5);
							}
						} else {
							// not visible, no bounding box
							s.x = 0;
							s.y = 0;
							s.x2 = 0;
							//s.y2 = 0;
						}
					}
					g2.setPaint(paintSave);
				}
				y += rowHeight;
				if (drawHorizontalGrid) {
					g2.setColor(Color.BLACK);
					g2.drawLine(0, y - 1, getWidth(), y - 1);
				}
				if (y > getHeight()) {
					break;
				}
			}
			if (showTasks) {
				if (selected != null) {
					Stroke sp = g2.getStroke();
					g2.setStroke(new BasicStroke(3));
					g2.setColor(line);
					Task s = selected;
					while (s.prev != null) {
						s = s.prev;
					}
					while (s.next != null) {
						double x = (s.startDate - timeOffset) / scale + (s.endDate - s.startDate) / scale / 2 + xoffs;
						y = -startPixel + s.machine.index * rowHeight + rowHeight / 2;
						double x2 = (s.next.startDate - timeOffset) / scale + (s.next.endDate - s.next.startDate) / scale / 2 + xoffs;
						int y2 = -startPixel + s.next.machine.index * rowHeight + rowHeight / 2;
						
						g2.drawLine((int)Math.round(x), y, (int)Math.round(x2), y2);
						s = s.next;
					}
					g2.setStroke(sp);
				}
			}
		}
		if (showWorkDays) {
			cal.setTimeInMillis(model.startDate);
			long date = model.startDate;
			while (date < model.endDate + 24L * 60 * 60 * 1000) {
				long st = date;
				long en = st + 24L * 60 * 60 * 1000;
				if (st > endTime) {
					break;
				}
				if ((en > timeOffset - displaceTime && st < endTime - displaceTime) 
						|| (st <= timeOffset - displaceTime && en >= endTime - displaceTime)) {
					double x = (st - timeOffset) / scale + xoffs;
					//double w = (en - st) / scale;
					int ix = (int)Math.round(x);
					//int iw = (int)Math.round(w);
					g2.setColor(Color.BLACK);
					g2.drawLine(ix, 0, ix, getHeight() - 1);
					//g2.drawLine(ix+iw+1, 0, ix+iw+1, getHeight()-1);
				}
				cal.add(GregorianCalendar.DATE, 1);
				date = cal.getTimeInMillis();
			}
		}
	}
	/**
	 * @return Returns the rowHeight.
	 */
	public int getRowHeight() {
		return rowHeight;
	}
	/**
	 * @param rowHeight The rowHeight to set.
	 */
	public void setRowHeight(int rowHeight) {
		this.rowHeight = rowHeight;
		repaint();
	}
	/**
	 * @return Returns the selected.
	 */
	public Task getSelected() {
		return selected;
	}
	/**
	 * Set selected schedule state.
	 * @param s the DMAppletSchedule object
	 * @param state the state to set
	 */
	private void setSelected(Task s, boolean state) {
		Task p = s.prev;
		s.selected = state;
		while (p != null) {
			p.selected = state;
			p = p.prev;
		}
		p = s.next;
		while (p != null) {
			p.selected = state;
			p = p.next;
		}
	}
	/**
	 * @param selected The selected to set.
	 */
	public void setSelected(Task selected) {
		if (this.selected != null) {
			setSelected(this.selected, false);
		}
		this.selected = selected;
		if (this.selected != null) {
			setSelected(this.selected, true);
		}
		repaint();
	}
	/**
	 * @return Returns the startPixel.
	 */
	public int getStartPixel() {
		return startPixel;
	}
	/**
	 * @param startPixel The startPixel to set.
	 */
	public void setStartPixel(int startPixel) {
		this.startPixel = startPixel;
		repaint();
	}
	/**
	 * Get the first visible row index. Even if partially visible.
	 * @return the row index
	 */
	public int getVisibleStartRow() {
		return startPixel / rowHeight;
	}
	/**
	 * Get the visible last row index. Even if partially visible.
	 * Only returns the index up to the data.size()-1;
	 * @return the row index
	 */
	public int getVisibleLastRow() {
		int idx = (startPixel + getHeight()) / rowHeight;
		return idx < model.machines.size() ? idx : model.machines.size() - 1; 
	}
	/**
	 * Returns the last visible row index. This index
	 * @return the last row index
	 */
	public int getLastRow() {
		return (startPixel + getHeight()) / rowHeight;
	}
	/**
	 * @return Returns the drawHorizontalGrid.
	 */
	public boolean isDrawHorizontalGrid() {
		return drawHorizontalGrid;
	}
	/**
	 * @param drawHorizontalGrid The drawHorizontalGrid to set.
	 */
	public void setDrawHorizontalGrid(boolean drawHorizontalGrid) {
		this.drawHorizontalGrid = drawHorizontalGrid;
	}
	/**
	 * Find a schedule.
	 * @param x X coordinate
	 * @param y Y coordinate
	 * @return the DMAppletSchedule or null
	 */
	public Task findTask(int x, int y) {
		if (model == null) {
			return null;
		}
		int row = (startPixel + y) / rowHeight;
		if (row < model.machines.size()) {
			for (Task s : model.machines.get(row).tasks) {
				if (s.x <= x && s.x2 >= x) {
					return s;
				}
			}
		}
		return null;
	}
}
