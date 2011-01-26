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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.net.URL;
import java.net.URLEncoder;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.zip.GZIPInputStream;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.ImageIcon;
import javax.swing.JApplet;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JColorChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.RowFilter;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.TransferHandler;
import javax.swing.table.TableRowSorter;

import eu.advance.logistics.applet.DiagramRenderer.DiagramSeries;

/**
 * The applet for displaying composite scada diagrams.
 * @author karnokd
 *
 */
public class DiagramApplet extends JApplet {
	/** */
	private static final long serialVersionUID = -8405901519603213477L;
	/** The bottom status label. */
	JLabel status;
	/** The working indicator icon. */
	ImageIcon workingIcon;
	/** The error icon. */
	ImageIcon errorIcon;
	/** The help icon. */
	ImageIcon helpIcon;
	/** The diagram panel. */
	private DiagramRenderer diagram;
	/** The time label. */
	private TimeLabel timelabel;
	/** The diagram composite panel. */
	private JPanel diagramComposite;
	/** The current data model. */
	private DataDiagram model;
	/** The split pane. */
	private JSplitPane split;
	/** The info model. */
	private InfoTableModel infoModel;
	/** The info table. */
	private JTable infoTable;
	/** The date formatter for the time column. */
	SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	/** The zoom in button. */
	private JButton zoomIn;
	/** The zoom out button. */
	private JButton zoomOut;
	/** The zoom fit buttom. */
	private JButton zoomFit;
	/** The screenshot button. */
	private JButton screenShot;
	/** The values only. */
	private JCheckBox valuesOnly;
	/** Zoom into selection. */
	private JButton zoomSelect;
	/** The left axis. */
	private VerticalAxisRenderer leftAxis;
	/** The right axis. */
	private VerticalAxisRenderer rightAxis;
	/** The help. */
	private JButton help;
	/**
	 * Initializes the applet.
	 */
	public DiagramApplet() {
		super();
	}
	@Override
	public void init() {
		super.init();
		
		URL wi = getClass().getResource("ajax-loader.gif");
		if (wi != null) {
			workingIcon = new ImageIcon(wi);
		} else {
			System.err.println("Missing resource: ajax-loader.gif");
		}
		URL errorIconURL = getClass().getResource("error.png");
		if (errorIconURL != null) {
			errorIcon = new ImageIcon(errorIconURL);
		} else {
			System.err.println("Missing resource: error.png");
		}
		URL helpIconURL = getClass().getResource("help.png");
		if (helpIconURL != null) {
			helpIcon = new ImageIcon(helpIconURL);
		} else {
			System.err.println("Missing resource: help.png");
		}
		createGUI();
	}
	@Override
	public void start() {
		SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
			/** The retrieved data. */
			private Result<DataDiagram> data;
			Throwable error;
			@Override
			protected Void doInBackground() throws Exception {
				try {
					String url = getCodeBase() + "DemoDiagramServlet?"
					+ "X=" + URLEncoder.encode("Y", "UTF-8")
					;
					InputStream in = new URL(url).openStream();
					try {
						ObjectInputStream oin = new ObjectInputStream(new GZIPInputStream(in));
						try {
							@SuppressWarnings("unchecked") Result<DataDiagram> result = (Result<DataDiagram>)oin.readObject();
							data = result;
						} finally {
							oin.close();
						}
					} finally {
						in.close();
					}
				} catch (Throwable t) {
					t.printStackTrace();
					error = t;
				}
				return null;
			}
			@Override
			protected void done() {
				if (data != null) {
					if (data.success()) {
						status.setIcon(workingIcon);
						status.setText("Processing data...");
						processData(data.get());
						status.setIcon(null);
						status.setText("Done.");
					} else {
						status.setIcon(errorIcon);
						status.setText("Error: " + data.error());
					}
				} else {
					status.setIcon(errorIcon);
					status.setText("Error: " + error);
				}
			}
		};
		status.setIcon(workingIcon);
		status.setText("Downloading data...");
		worker.execute();
	}
	/** Create the GUI elements. */
	void createGUI() {
		Container c = getContentPane();
		c.setLayout(new BorderLayout());
		
		status = new JLabel();
		
		c.add(status, BorderLayout.NORTH);
		
		JPanel diagramAndControls = new JPanel();
		diagramAndControls.setDoubleBuffered(true);
		
		GroupLayout gl0 = new GroupLayout(diagramAndControls);
		diagramAndControls.setLayout(gl0);
		
		
		diagramComposite = new JPanel();
		GroupLayout gl = new GroupLayout(diagramComposite);
		diagramComposite.setLayout(gl);

		diagram = new DiagramRenderer();
		diagram.addMouseWheelListener(new MouseWheelListener() {
			@Override
			public void mouseWheelMoved(MouseWheelEvent e) {
				if (e.isControlDown()) {
					if (e.getUnitsToScroll() < 0) {
						doZoomIn(e.getX());
					} else {
						doZoomOut(e.getX());
					}
					e.consume();
				}
			}
		});
		diagram.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if (SwingUtilities.isMiddleMouseButton(e)) {
					doZoomFit();
				}
			}
		});
		timelabel = new TimeLabel();
		timelabel.setTopMode(true);
		
		MouseAdapter drag = new MouseAdapter() {
			boolean dragging;
			int lastx;
			int lasty;
			@Override
			public void mousePressed(MouseEvent e) {
				if (SwingUtilities.isRightMouseButton(e)) {
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
					doMouseDragged(lastx - e.getX(), lasty - e.getY());
					lastx = e.getX();
					lasty = e.getY();
				}
			}
		};
		diagram.addMouseListener(drag);
		diagram.addMouseMotionListener(drag);
		timelabel.addMouseListener(drag);
		timelabel.addMouseMotionListener(drag);
		
		MouseAdapter selectRange = new MouseAdapter() {
			boolean selecting;
			@Override
			public void mousePressed(MouseEvent e) {
				if (SwingUtilities.isLeftMouseButton(e)) {
					selecting = true;
					if (!e.isShiftDown()) {
						long t = diagram.getTimeAt(e.getX());
						diagram.setSelection(t);
						diagram.repaint();
						fillInfoValues(e.getX());
					}
				}
			}
			@Override
			public void mouseDragged(MouseEvent e) {
				if (selecting) {
					long t = diagram.getTimeAt(e.getX());
					diagram.setSelectionEnd(t);
					diagram.repaint();
				}
			}
			@Override
			public void mouseReleased(MouseEvent e) {
				if (selecting && e.isControlDown()) {
					doZoomSelection();
				}
				selecting = false;
			}
		};
		diagram.addMouseListener(selectRange);
		diagram.addMouseMotionListener(selectRange);
		
//		diagram.setPreferredSize(new Dimension(getWidth() - 20, getHeight() - 150));
		
		// the user interface controls
		JPanel controls = new JPanel();
		controls.setLayout(new FlowLayout());
		
		zoomIn = new JButton("Zoom in");
		zoomIn.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				doZoomIn();
			}
		});
		zoomOut = new JButton("Zoom out");
		zoomOut.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				doZoomOut();
			}
		});
		zoomFit = new JButton("Zoom to fit");
		zoomFit.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				doZoomFit();
			}
		});
		zoomSelect = new JButton("Zoom selection");
		zoomSelect.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				doZoomSelection();
			}
		});
		screenShot = new JButton("Take screenshot");
		screenShot.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				doScreenshot();
			}
		});
		screenShot.setVisible(false);
		
		valuesOnly = new JCheckBox("Values only");
		valuesOnly.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				infoModel.fireTableDataChanged();
			}
		});
		
		leftAxis = new VerticalAxisRenderer();
		rightAxis = new VerticalAxisRenderer();
		rightAxis.setLeftSide(true);
		
		controls.add(zoomIn);
		controls.add(zoomOut);
		controls.add(zoomFit);
		controls.add(zoomSelect);
		controls.add(screenShot);
		controls.add(valuesOnly);
		help = new JButton("Usage", helpIcon);
		help.setEnabled(false);
		help.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				UsageDialog dlg = new UsageDialog(model);
				dlg.setLocationRelativeTo(DiagramApplet.this);
				dlg.setVisible(true);
			}
		});
		controls.add(help);
		
		
		gl.setHorizontalGroup(
				gl.createSequentialGroup()
				.addComponent(leftAxis, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
				.addGroup(
					gl.createParallelGroup()
					.addComponent(diagram)
					.addComponent(timelabel)
				)
				.addComponent(rightAxis, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
		);
		gl.setVerticalGroup(
				gl.createSequentialGroup()
				.addGroup(
					gl.createParallelGroup(Alignment.LEADING)
					.addComponent(leftAxis, 0, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)
					.addComponent(diagram, 0, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)
					.addComponent(rightAxis, 0, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)
				)
				
				.addComponent(timelabel, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
		);
		
		gl0.setHorizontalGroup(
			gl0.createParallelGroup()
			.addComponent(controls)
			.addComponent(diagramComposite)
		);
		gl0.setVerticalGroup(
			gl0.createSequentialGroup()
			.addComponent(controls, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
			.addComponent(diagramComposite, 0, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)
		);
		
		split = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		split.setTopComponent(diagramAndControls);
		
		JPanel infoPanel = new JPanel();
		
		buildInfoPanel(infoPanel);
		
		split.setBottomComponent(infoPanel);
		split.setDividerLocation(0.85);
		final Runnable run = new Runnable() {
			@Override
			public void run() {
				if (split.isShowing()) {
					split.setDividerLocation(0.85);
				} else {
					SwingUtilities.invokeLater(this);
				}
			}
		};
		SwingUtilities.invokeLater(run);
		
		c.add(split, BorderLayout.CENTER);
	}
	/**
	 * Build the contents of the info panel.
	 * @param infoPanel the info panel.
	 */
	private void buildInfoPanel(JPanel infoPanel) {
		infoModel = new InfoTableModel();
		infoTable = new JTable(infoModel);
		JScrollPane sp = new JScrollPane(infoTable);
		infoPanel.setLayout(new BorderLayout());
		infoPanel.add(sp, BorderLayout.CENTER);
		sp.setPreferredSize(new Dimension(640, 100));
		infoTable.getColumnModel().getColumn(0).setPreferredWidth(35);
		infoTable.getColumnModel().getColumn(1).setPreferredWidth(50);
		infoTable.getColumnModel().getColumn(2).setPreferredWidth(35);
		infoTable.getColumnModel().getColumn(3).setPreferredWidth(250);
		infoTable.getColumnModel().getColumn(4).setPreferredWidth(100);
		infoTable.getColumnModel().getColumn(5).setPreferredWidth(250);
		infoTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		infoTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		
		infoTable.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if (SwingUtilities.isRightMouseButton(e)) {
					Point p = e.getPoint();
					 
					// get the row index that contains that coordinate
					int rowAtPoint = infoTable.rowAtPoint(p);
					if (rowAtPoint >= 0) {
						int rowNumber = infoTable.convertRowIndexToModel(rowAtPoint);
			 
						// Get the ListSelectionModel of the JTable
						ListSelectionModel model = infoTable.getSelectionModel();
			 
						// set the selected interval of rows. Using the "rowNumber"
						// variable for the beginning and end selects only that one row.
						model.setSelectionInterval(rowNumber, rowNumber);
						
						displayColorChooser();
					}
				}
			}
		});
		TableRowSorter<InfoTableModel> sorter = new TableRowSorter<InfoTableModel>(infoModel);
		sorter.setRowFilter(new RowFilter<Object, Object>() {
			@Override
			public boolean include(
					javax.swing.RowFilter.Entry<? extends Object, ? extends Object> entry) {
				return valuesOnly.isSelected() ? entry.getValue(4) != null : true;
			}
		});
		infoTable.setRowSorter(sorter);
	}
	/**
	 * The info table entry. 
	 * @author karnokd
	 */
	static class InfoTableEntry {
		/** The numerical value. */
		public Double value;
		/** The selected object's start time. */
		public long startTime = -1;
		/** The selected object's end time, optional. */
		public long endTime = -1;
		/** The backing diagram series. */
		public DiagramSeries<?> series;
		/** The min-max object. */
		public MinMax axis;
	}
	/**
	 * The information table model.
	 * @author karnokd
	 */
	class InfoTableModel extends GenericTableModel<InfoTableEntry> {
		/**	 */
		private static final long serialVersionUID = 6072414322834981552L;
		/** Constructor. Prepares the columns. */
		public InfoTableModel() {
			setColumnClasses(Boolean.class, Float.class, String.class, String.class, Double.class, String.class);
			setColumnNames("Visible", "Alpha", "Color", "Name", "Value", "Time");
		}
		@Override
		public Object getValueFor(int rowIndex, int columnIndex,
				InfoTableEntry entry) {
			switch (columnIndex) {
			case 0:
				return entry.series.visible;
			case 1:
				return entry.series.alpha;
			case 2:
				return String.format("<html><span style='background-color: #%06X'>&nbsp;&nbsp;&nbsp;&nbsp;", (entry.series.color.getRGB() & 0xFFFFFF));
			case 3:
				return entry.series.name;
			case 4:
				return entry.value;
			case 5:
				StringBuilder sb = new StringBuilder();
				if (entry.startTime >= 0) {
					sb.append(sdf.format(new Timestamp(entry.startTime)));
				}
				if (sb.length() > 0) {
					sb.append(" - ");
				}
				if (entry.endTime >= 0) {
					sb.append(sdf.format(new Timestamp(entry.endTime)));
				}
				return sb.toString();
			default:
			}
			return null;
		}
		@Override
		public void setValueAt(int rowIndex, int columnIndex, InfoTableEntry t,
				Object aValue) {
			if (columnIndex == 0) {
				t.series.visible = (Boolean)aValue;
				diagram.repaint();
			} else
			if (columnIndex == 1) {
				t.series.alpha = (Float)aValue;
				diagram.repaint();
			}
		}
		@Override
		public boolean isCellEditable(int rowIndex, int columnIndex) {
			return columnIndex == 0 || columnIndex == 1;
		}
	}
	/**
	 * Zoom to the current selection.
	 */
	protected void doZoomSelection() {
		if (diagram.getSelectionStart() >= 0 && diagram.getSelectionStart() != diagram.getSelectionEnd()) {
			long newOffset = Math.min(diagram.getSelectionStart(), diagram.getSelectionEnd()) - diagram.getStartTime();
			long newEndOffset = Math.max(diagram.getSelectionStart(), diagram.getSelectionEnd()) - diagram.getStartTime();
			
			double scale = 1.0 * (newEndOffset - newOffset) / diagram.getDiagramWidth();
			double zoom10scale = 1.0 * diagram.getTimeRange() / diagram.getDiagramWidth();
			
			diagram.setZoom(scale / zoom10scale);
			diagram.setTimeOffset(newOffset);
			timelabel.setZoom(scale / zoom10scale);
			timelabel.setScale(timelabel.computeScale());
			
			limitTimeOffset();
			
			timelabel.invalidate();
			diagram.invalidate();
			
			timelabel.repaint();
			diagram.repaint();
		}
	}
	/**
	 * React to mouse dragging on screen.
	 * @param dx the movement difference in X direction
	 * @param dy the movement difference in Y direction
	 */
	protected void doMouseDragged(int dx, int dy) {
		long offset = diagram.getTimeOffset();
		double scale = diagram.computeScale();
		long doff = (long)(offset + dx * scale);
		
		diagram.setTimeOffset(doff);
		
		limitTimeOffset();

//		System.out.printf("dx = %d, dy = %d, offset = %d, scale = %.3f, doff = %d, maxOffs = %d%n", dx, dy, offset, scale, doff, maxOffs);

		timelabel.invalidate();
		diagram.invalidate();
		
		timelabel.repaint();
		diagram.repaint();
	}
	/**
	 * Take a screenshot.
	 */
	protected void doScreenshot() {
		BufferedImage bimg = new BufferedImage(diagramComposite.getWidth(), diagramComposite.getHeight(), BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = bimg.createGraphics();
		diagramComposite.paintAll(g);
		g.dispose();
		JLabel lbl = new JLabel(new ImageIcon(bimg));
		new ImageSelection().exportToClipboard(lbl, getToolkit().getSystemClipboard(), TransferHandler.COPY);
	}
	/**
	 * Zoom the image back to fit the total screen size.
	 */
	protected void doZoomFit() {
		if (model.startTime != null) {
			timelabel.setStartDate(model.startTime.getTime());
			timelabel.setEndDate(model.endTime.getTime());
			timelabel.setOffset(timelabel.getStartDate());
			diagram.setTimeOffset(0L);
			
			timelabel.setZoom(1.0);
			diagram.setZoom(1.0);
			timelabel.setScale(timelabel.computeScale());
			
			timelabel.invalidate();
			diagram.invalidate();
			
			timelabel.repaint();
			diagram.repaint();
		}
		
	}
	/**
	 * Zoom out.
	 */
	protected void doZoomOut() {
		double zoom = diagram.getZoom() * 1.2;
		timelabel.setZoom(zoom);
		diagram.setZoom(zoom);
		timelabel.setScale(timelabel.computeScale());

		limitTimeOffset();

		timelabel.invalidate();
		diagram.invalidate();
		
		timelabel.repaint();
		diagram.repaint();
	}
	/** Zoom in. */
	protected void doZoomIn() {
		double zoom = diagram.getZoom() / 1.2;
		timelabel.setZoom(zoom);
		diagram.setZoom(zoom);
		timelabel.setScale(timelabel.computeScale());
		
		limitTimeOffset();
		
		timelabel.invalidate();
		diagram.invalidate();
		
		timelabel.repaint();
		diagram.repaint();
	}
	/**
	 * Limit the time offset's value based on the zooming factor.
	 */
	private void limitTimeOffset() {
		long doff = diagram.getTimeOffset();
		long maxOffs = (long)(diagram.getTimeRange() - diagram.computeScale() * diagram.getDiagramWidth());
		if (doff > maxOffs) {
			doff = maxOffs;
		}
		if (doff < 0) {
			doff = 0;
		}
		diagram.setTimeOffset(doff);
		timelabel.setOffset(timelabel.getStartDate() + doff);
	}
	/**
	 * Zoom out.
	 * @param x the current x to keep centered
	 */
	protected void doZoomOut(int x) {
		long t = diagram.getTimeAt(x);
		
		double zoom = diagram.getZoom() * 1.2;
		timelabel.setZoom(zoom);
		diagram.setZoom(zoom);
		timelabel.setScale(timelabel.computeScale());

		long t1 = diagram.getTimeAt(x);
		
		diagram.setTimeOffset(diagram.getTimeOffset() + (t - t1));
		
		limitTimeOffset();

		timelabel.invalidate();
		diagram.invalidate();
		
		timelabel.repaint();
		diagram.repaint();
	}
	/** 
	 * Zoom in. 
	 * @param x the current x to keep centered
	 */
	protected void doZoomIn(int x) {
		long t = diagram.getTimeAt(x);
		double zoom = diagram.getZoom() / 1.2;
		timelabel.setZoom(zoom);
		diagram.setZoom(zoom);
		timelabel.setScale(timelabel.computeScale());
		
		long t1 = diagram.getTimeAt(x);
		diagram.setTimeOffset(diagram.getTimeOffset() + (t - t1));
		
		limitTimeOffset();
		
		timelabel.invalidate();
		diagram.invalidate();
		
		timelabel.repaint();
		diagram.repaint();
	}
	/**
	 * Process the scada diagram data.
	 * @param model the data
	 */
	protected void processData(DataDiagram model) {
		this.model = model;
		if (model != null) {
			if (model.startTime != null) {
				timelabel.setStartDate(model.startTime.getTime());
				timelabel.setEndDate(model.endTime.getTime());
				timelabel.setOffset(timelabel.getStartDate());
				timelabel.setScale(timelabel.computeScale());
			} else {
				timelabel.setStartDate(0);
				timelabel.setEndDate(0);
				timelabel.setOffset(0);
			}
			diagram.setModel(model);
	
			infoModel.clear();
			for (DiagramSeries<DataStatus> statuses : diagram.getStatusList()) {
				InfoTableEntry te = new InfoTableEntry();
				te.series = statuses;
				infoModel.add(te);
			}
			for (DiagramSeries<DataAction> actions : diagram.getActionList()) {
				InfoTableEntry te = new InfoTableEntry();
				te.series = actions;
				infoModel.add(te);
			}
			for (DiagramSeries<DataAlarm> alarms : diagram.getAlarmList()) {
				InfoTableEntry te = new InfoTableEntry();
				te.series = alarms;
				infoModel.add(te);
			}
			int i = 0;
			for (DiagramSeries<DataDiagramValues<DataSignal>> signals : diagram.getSignalList()) {
				MinMax mm = new MinMax();
				mm.min = signals.items.get(0).minimum.doubleValue();
				mm.max = signals.items.get(0).maximum.doubleValue();
				mm.name = signals.name;
				mm.color = signals.color;
				if (i % 2 == 0) {
					leftAxis.getAxises().add(mm);
				} else {
					rightAxis.getAxises().add(mm);
				}
				InfoTableEntry te = new InfoTableEntry();
				te.series = signals;
				te.axis = mm;
				infoModel.add(te);
				i++;
			}
			infoModel.fireTableDataChanged();
			
			zoomIn.setText(model.get("Zoom in"));
			zoomOut.setText(model.get("Zoom out"));
			zoomFit.setText(model.get("Zoom to fit"));
			zoomSelect.setText(model.get("Zoom selection"));
			valuesOnly.setText(model.get("Values only"));
			screenShot.setText(model.get("Take a screenshot"));
			infoModel.setColumnNames(model.get("Visible"), model.get("Alpha"), model.get("Color"), model.get("Name"), model.get("Value"), model.get("Time"));
			diagram.setAxisRenderer(leftAxis.createAxisRenderer(0));
		}
		help.setEnabled(true);
		invalidate();
		repaint();
	}
	/**
	 * Fill in the info values for the various series based on the currently selected time.
	 * @param x the click coordinate
	 */
	protected void fillInfoValues(int x) {
		long t = diagram.getTimeAt(x);
		int i = 0;
		for (DiagramSeries<DataStatus> statuses : diagram.getStatusList()) {
			InfoTableEntry te = infoModel.get(i);
			te.value = null;
			te.startTime = -1;
			te.endTime = -1;
			for (DataStatus st : statuses.items) {
				if (st.start.getTime() <= t && st.end.getTime() >= t) {
					te.startTime = st.start.getTime();
					te.endTime = st.end.getTime();
					te.value = (double)st.startStatus;
					break;
				}
			}
			i++;
		}
		long t0 = diagram.getTimeAt(x - 3);
		long t1 = diagram.getTimeAt(x + 3);
		for (DiagramSeries<DataAction> actions : diagram.getActionList()) {
			InfoTableEntry te = infoModel.get(i);
			te.value = null;
			te.startTime = -1;
			te.endTime = -1;
			for (DataAction st : actions.items) {
				if (t0 <= st.timestamp.getTime() && st.timestamp.getTime() <= t1) {
					te.startTime = st.timestamp.getTime();
					te.endTime = -1;
					te.value = 1.0;
					break;
				}
			}
			i++;
		}
		for (DiagramSeries<DataAlarm> alarms : diagram.getAlarmList()) {
			InfoTableEntry te = infoModel.get(i);
			te.value = null;
			te.startTime = -1;
			te.endTime = -1;
			for (DataAlarm st : alarms.items) {
				if (st.start.getTime() <= t && st.end.getTime() >= t) {
					te.startTime = st.start.getTime();
					te.endTime = st.end.getTime();
					te.value = 1.0;
					break;
				}
			}
			i++;
		}
		for (DiagramSeries<DataDiagramValues<DataSignal>> signals : diagram.getSignalList()) {
			InfoTableEntry te = infoModel.get(i);
			te.value = null;
			te.startTime = -1;
			te.endTime = -1;
			List<DataSignal> values = signals.items.get(0).values;
			for (int j = 0; j < values.size() - 1; j++) {
				DataSignal a1 = values.get(j);
				DataSignal a2 = values.get(j + 1);
				if (a1.timestamp.getTime() <= t && t <= a2.timestamp.getTime()) {
					te.startTime = a1.timestamp.getTime();
					te.endTime = a2.timestamp.getTime();
					if (a1.value != null && a2.value != null) {
						if (te.startTime == te.endTime) {
							te.value = a1.value.doubleValue();
						} else {
							te.value = a1.value.doubleValue() + (a2.value.doubleValue() - a1.value.doubleValue()) * (t - te.startTime) / (te.endTime - te.startTime);
						}
					}
					break;
				}
			}
			i++;
		}
		infoModel.fireTableDataChanged();
	}
	/** Display the color chooser. */
	protected void displayColorChooser() {
		if (infoTable.getSelectedRow() >= 0) {
			InfoTableEntry e = infoModel.get(infoTable.convertRowIndexToModel(infoTable.getSelectedRow()));
			Color c = JColorChooser.showDialog(infoTable, model.get("Select a color"), e.series.color);
			if (c != null) {
				e.series.color = c;
				if (e.axis != null) {
					e.axis.color = c;
				}
				infoModel.fireTableDataChanged();
				repaint();
			}
		}
	}
	
}
