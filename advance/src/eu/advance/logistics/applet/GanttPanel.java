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
import java.awt.Dimension;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 * Composite panel which manages the multi component drawing scrolling of
 * gantt diagrams.
 * @author karnokd, 2008.02.07.
 * @version $Revision 1.0$
 */
public class GanttPanel extends JPanel {
	/**
	 * 
	 */
	private static final long serialVersionUID = 8516909531397426490L;
	/** The time label. */
	private TimeLabel timeLabel;
	/** The Gantt Diagram. */
	private GanttDiagram gantt;
	/** The machine title table. */
	private Table machineTable;
	/** The machine table model. */
	private TableModel machineModel;
	/** The gantt diagram model. */
	private GanttModel model;
	/** The key performance indicator table. */
	private Table kpiTable;
	/** The key performance indicator model. */
	private TableModel kpiModel;
	/** The zoom slider. */
	private JSlider zoom;
	/** The horizontal scrollbar. */
	private JScrollBar scroll;
	/** The vertical scrollbar. */
	private JScrollBar vscroll;
	/**
	 * Constructor.
	 */
	public GanttPanel() {
		init();
	}
	/**
	 * Initialize panel components.
	 */
	private void init() {
		setDoubleBuffered(true);
		GroupLayout gl = new GroupLayout(this);
		setLayout(gl);
		
		timeLabel = new TimeLabel();
		timeLabel.setOpaque(true);
		
		timeLabel.setScale(14 * 24 * 60 * 60);
		
		gantt = new GanttDiagram(timeLabel);
		gantt.setPreferredSize(new Dimension(200, 100));
		gantt.setBackground(Color.WHITE);
		machineTable = new Table();
		machineModel = new TableModel() {
			/** */
			private static final long serialVersionUID = 8896809030160326378L;
			@Override
			public int getColumnCount() {
				return 1;
			}
			@Override
			public int getRowCount() {
				if (model != null) {
					return model.machines.size();
				}
				return 0;
			}
			@Override
			public Object getValueAt(int row, int column) {
				return model.machines.get(row).name;
			}
			@Override
			public String getTooltipAt(int row, int column) {
				StringBuilder b = new StringBuilder("<html>");
				b.append(model.machines.get(row).tooltip);
				for (Attribute a : model.machines.get(row).attributes) {
					b.append("<br>").append(a.name).append(": ").append(a.value);
				}
				return b.toString();
			}
		};
		machineTable.setModel(machineModel);
		kpiTable = new Table();
		kpiModel = new TableModel() {
			/** */
			private static final long serialVersionUID = 7600628733475219759L;
			@Override
			public int getColumnCount() {
				if (getRowCount() > 0) {
					return model.machines.get(0).kpis.size();
				}
				return 0;
			}

			@Override
			public int getRowCount() {
				if (model != null) {
					return model.machines.size();
				}
				return 0;
			}

			@Override
			public String getTooltipAt(int row, int column) {
				return model.machines.get(row).kpis.get(column).tooltip;
			}

			@Override
			public Object getValueAt(int row, int column) {
				return model.machines.get(row).kpis.get(column).value;
			}
			
		};
		kpiTable.setModel(kpiModel);
		
		zoom = new JSlider(-8, 5);
		scroll = new JScrollBar(JScrollBar.HORIZONTAL);
		scroll.setMinimum(0);
		vscroll = new JScrollBar(JScrollBar.VERTICAL);
		vscroll.setMinimum(0);
		zoom.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				timeLabel.setScale(14 * 24 * 60 * 60 * Math.pow(2, zoom.getValue()));
				int xmax = timeLabel.getEffectiveWidth() + scroll.getModel().getExtent();
				scroll.setMaximum(Math.max(0, xmax));
				invalidate();
				repaint();
			}
		});
		scroll.setValue(0);
		scroll.addAdjustmentListener(new AdjustmentListener() {
			@Override
			public void adjustmentValueChanged(AdjustmentEvent e) {
				timeLabel.scrollToPixel(scroll.getValue());
				invalidate();
				repaint();
			}
		});
		vscroll.setValue(0);
		vscroll.addAdjustmentListener(new AdjustmentListener() {
			@Override
			public void adjustmentValueChanged(AdjustmentEvent e) {
				gantt.setStartPixel(vscroll.getValue());
				machineTable.setStartPixel(vscroll.getValue());
				kpiTable.setStartPixel(vscroll.getValue());
				invalidate();
				repaint();
			}
		});
		MouseAdapter mouseOps = new MouseAdapter() {
			/** The last x coordinate. */
			private int lastx;
			/** The last y coordinate. */
			private int lasty;
			/** Is currently dragging. */
			private boolean dragging;
			@Override
			public void mouseWheelMoved(MouseWheelEvent e) {
				if ((e.getModifiersEx() & MouseWheelEvent.CTRL_DOWN_MASK) != 0) {
					// set zoom factor
					zoom.setValue(zoom.getValue() + (e.getUnitsToScroll() < 0 ? -1 : 1));
					
				} else {
					vscroll.setValue(vscroll.getValue() + (e.getUnitsToScroll() * gantt.getRowHeight()));
				}
			}
			@Override
			public void mousePressed(MouseEvent e) {
				if ((e.getButton() == MouseEvent.BUTTON1
						&& (e.getModifiersEx() & MouseEvent.ALT_DOWN_MASK) != 0)
						|| (e.getButton() == MouseEvent.BUTTON3)) {
					lastx = e.getX();
					lasty = e.getY();
					dragging = true;
				}
			}
			@Override
			public void mouseReleased(MouseEvent e) {
				dragging = false;
			}
			@Override
			public void mouseDragged(MouseEvent e) {
				if (dragging) {
					int deltax = (e.getX() - lastx);
					int deltay = (e.getY() - lasty);
					// snap scroll horizontally
					int newx = scroll.getValue() - deltax;
					scroll.setValue(Math.max(0, newx));
					vscroll.setValue(vscroll.getValue() - deltay);
					lastx = e.getX();
					lasty = e.getY();
				}
			}
			/* (non-Javadoc)
			 * @see java.awt.event.MouseAdapter#mouseClicked(java.awt.event.MouseEvent)
			 */
			@Override
			public void mouseClicked(MouseEvent e) {
				if (e.getButton() == MouseEvent.BUTTON1 && e.getClickCount() == 1) {
					Task t = gantt.findTask(e.getX(), e.getY());
					gantt.setSelected(t);
					gantt.repaint();
				}
			}
		};
		MouseAdapter tableOps = new MouseAdapter() {
			/** The last y coordinate. */
			private int lasty;
			/** Is currently dragging. */
			private boolean dragging;
			@Override
			public void mouseWheelMoved(MouseWheelEvent e) {
				if ((e.getModifiersEx() & MouseWheelEvent.CTRL_DOWN_MASK) != 0) {
					// set zoom factor
					zoom.setValue(zoom.getValue() + (e.getUnitsToScroll() < 0 ? -1 : 1));
					
				} else {
					vscroll.setValue(vscroll.getValue() + (e.getUnitsToScroll() * gantt.getRowHeight()));
				}
			}
			@Override
			public void mousePressed(MouseEvent e) {
				if ((e.getButton() == MouseEvent.BUTTON1
						&& (e.getModifiersEx() & MouseEvent.ALT_DOWN_MASK) != 0)
						|| (e.getButton() == MouseEvent.BUTTON2)) {
					lasty = e.getY();
					dragging = true;
				}
			}
			@Override
			public void mouseReleased(MouseEvent e) {
				dragging = false;
			}
			@Override
			public void mouseDragged(MouseEvent e) {
				if (dragging) {
					int deltay = (e.getY() - lasty);
					// snap scroll horizontally
					vscroll.setValue(vscroll.getValue() - deltay);
					lasty = e.getY();
				}
			}
			/* (non-Javadoc)
			 * @see java.awt.event.MouseAdapter#mouseClicked(java.awt.event.MouseEvent)
			 */
			@Override
			public void mouseClicked(MouseEvent e) {
				if (e.getButton() == MouseEvent.BUTTON1 && e.getClickCount() == 1) {
					Task t = gantt.findTask(e.getX(), e.getY());
					gantt.setSelected(t);
					gantt.repaint();
				}
			}
		};
		
		gantt.addMouseWheelListener(mouseOps);
		gantt.addMouseListener(mouseOps);
		gantt.addMouseMotionListener(mouseOps);

		machineTable.addMouseWheelListener(tableOps);
		machineTable.addMouseListener(tableOps);
		machineTable.addMouseMotionListener(tableOps);

		kpiTable.addMouseWheelListener(tableOps);
		kpiTable.addMouseListener(tableOps);
		kpiTable.addMouseMotionListener(tableOps);
		
		gl.setHorizontalGroup(
			gl.createSequentialGroup()
			.addComponent(machineTable, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
			.addGroup(
				gl.createParallelGroup(Alignment.LEADING)
				.addComponent(timeLabel, 1, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
				.addComponent(gantt, 1, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
				.addComponent(scroll)
				.addComponent(zoom)
			)
			.addComponent(kpiTable, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
			.addComponent(vscroll)
		);
		gl.setVerticalGroup(
			gl.createSequentialGroup()
			.addComponent(timeLabel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
			.addGroup(
				gl.createParallelGroup(Alignment.LEADING)
				.addComponent(machineTable)
				.addComponent(gantt, 1, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
				.addComponent(kpiTable)
				.addComponent(vscroll)
			)
			.addComponent(scroll)
			.addComponent(zoom)
		);
		
		addComponentListener(new ComponentAdapter() {
			@Override
			public void componentResized(ComponentEvent e) {
				resetScrollLimits();
			}

		});
	}
	/**
	 * Reset scroll limits.
	 */
	private void resetScrollLimits() {
		int xmax = timeLabel.getEffectiveWidth() + scroll.getModel().getExtent();
		scroll.setMaximum(Math.max(xmax, 0));
		scroll.setUnitIncrement(timeLabel.getTickSize());
		scroll.setBlockIncrement(timeLabel.getTickSize() * 3);
		int machineCount = model != null ? model.machines.size() : 0;
		int vmax = machineCount * gantt.getRowHeight() - gantt.getHeight() + vscroll.getModel().getExtent();
		vscroll.setMaximum(Math.max(0, vmax));
		vscroll.setUnitIncrement(gantt.getRowHeight());
		vscroll.setBlockIncrement(gantt.getRowHeight() * 3);
	}
	/**
	 * Sets the model on the diagram.
	 * @param model the data model to set
	 */
	public void setModel(GanttModel model) {
		this.model = model;
		if (model != null) {
			model.linkTasks();
			timeLabel.setStartDate(model.startDate);
			timeLabel.setEndDate(model.endDate);
			timeLabel.setOffset(timeLabel.getStartDate());
		} else {
			timeLabel.setStartDate(0L);
			timeLabel.setEndDate(0L);
			timeLabel.setOffset(0L);
		}
		gantt.setModel(model);
		resetScrollLimits();
		invalidate();
		repaint();
	}
	/**
	 * @return returns the gantt model.
	 */
	public GanttModel getModel() {
		return model;
	}
        /**
         * @return returns the gantt diagram component
         */
        public GanttDiagram getGantt() {
            return gantt;
        }
}
