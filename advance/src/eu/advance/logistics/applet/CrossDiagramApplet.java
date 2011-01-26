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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.net.URL;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPInputStream;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.ImageIcon;
import javax.swing.JApplet;
import javax.swing.JButton;
import javax.swing.JCheckBox;
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

import eu.advance.logistics.applet.CrossDiagramRenderer.DiagramValue;
import eu.advance.logistics.applet.CrossDiagramRenderer.InterpolationMode;

/**
 * The applet for displaying composite scada diagrams.
 * @author karnokd
 *
 */
public class CrossDiagramApplet extends JApplet {
	/** */
	private static final long serialVersionUID = -8405901519603213477L;
	/** The value entry. */
	public static class ValueEntry {
		/** The color indicator. */
		public Color color;
		/** The interpolation mode. */
		public InterpolationMode mode;
		/** The X value. */
		public double x;
		/** The Y value. */
		public double y;
	}
	/**
	 * The value model. 
	 * @author karnokd
	 */
	public static class ValueModel extends GenericTableModel<ValueEntry> {
		/** */
		private static final long serialVersionUID = -4668299919436347944L;

		@Override
		public Object getValueFor(int rowIndex, int columnIndex, ValueEntry entry) {
			switch (columnIndex) {
			case 0:
				return rowIndex;
			case 1:
				return String.format("<html><font style='background-color: %06X'>&nbsp;&nbsp;&nbsp;&nbsp;", (entry.color.getRGB() & 0xFFFFFF));
			case 2:
				return entry.mode.toString();
			case 3:
				return entry.x;
			case 4:
				return entry.y;
			default:
				return null;
			}
		};
	}
	/** The bottom status label. */
	JLabel status;
	/** The working indicator icon. */
	ImageIcon workingIcon;
	/** The error icon. */
	ImageIcon errorIcon;
	/** The help icon. */
	ImageIcon helpIcon;
	/** The diagram panel. */
	private CrossDiagramRenderer diagram;
	/** The diagram composite panel. */
	private JPanel diagramComposite;
	/** The current data model. */
	private DataDiagram model;
	/** Sets the split location after it is displayed. */
	/** The split pane. */
	private JSplitPane split;
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
	/** Show exact. */
	private JCheckBox showExact;
	/** Show X interpolated. */
	private JCheckBox showXInter;
	/** Show Y interpolated. */
	private JCheckBox showYInter;
	/** The information table. */
	private JTable infoTable;
	/** X axis. */
	private HorizontalAxisRenderer xAxis;
	/** Y axis. */
	private VerticalAxisRenderer yAxis;
	/** Zoom into selection. */
	private JButton zoomSelection;
	/** The value model. */
	private ValueModel infoModel;
	/** The help. */
	private JButton help;
	/**
	 * Initializes the applet.
	 */
	public CrossDiagramApplet() {
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
					String url = getCodeBase() + "DemoCrossDiagramServlet?"
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

		diagram = new CrossDiagramRenderer();
		diagram.setPreferredSize(new Dimension(640, 640));
		diagram.addMouseWheelListener(new MouseWheelListener() {
			@Override
			public void mouseWheelMoved(MouseWheelEvent e) {
				if (e.isControlDown()) {
					if (e.getUnitsToScroll() < 0) {
						doZoomIn(e.getX(), e.getY());
					} else {
						doZoomOut(e.getX(), e.getY());
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
		MouseAdapter drag = new MouseAdapter() {
			boolean dragging;
			int lastx;
			int lasty;
			@Override
			public void mousePressed(MouseEvent e) {
				if (SwingUtilities.isRightMouseButton(e)) {
					dragging = true;
					lastx = e.getX();
					lasty = e.getY();
				}
			}
			@Override
			public void mouseReleased(MouseEvent e) {
				if (SwingUtilities.isRightMouseButton(e)) {
					dragging = false;
				}
			}
			@Override
			public void mouseDragged(MouseEvent e) {
				if (dragging) {
					doDrag(e.getX() - lastx, e.getY() - lasty);
					lastx = e.getX();
					lasty = e.getY();
				}
			};
		};
		diagram.addMouseListener(drag);
		diagram.addMouseMotionListener(drag);
		
		MouseAdapter select = new MouseAdapter() {
			boolean selecting;
			@Override
			public void mousePressed(MouseEvent e) {
				if (SwingUtilities.isLeftMouseButton(e)) {
					selecting = true;
					diagram.setSelectionStart(diagram.getValuesAt(e.getX(), e.getY()));
					diagram.clearSelectionEnd();
					diagram.repaint();
				}
			}
			@Override
			public void mouseReleased(MouseEvent e) {
				if (SwingUtilities.isLeftMouseButton(e)) {
					diagram.setSelectionEnd(diagram.getValuesAt(e.getX(), e.getY()));
					if (e.isControlDown()) {
						doZoomSelection();
					} else {
						diagram.repaint();
					}
					doListInfo();
					selecting = false;
				}
			}
			@Override
			public void mouseDragged(MouseEvent e) {
				if (selecting) {
					diagram.setSelectionEnd(diagram.getValuesAt(e.getX(), e.getY()));
					diagram.repaint();
				}
			};
		};
		diagram.addMouseListener(select);
		diagram.addMouseMotionListener(select);
		
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
		screenShot = new JButton("Take screenshot");
		screenShot.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				doScreenshot();
			}
		});
		screenShot.setVisible(false);
		zoomSelection = new JButton("Zoom selection");
		zoomSelection.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				doZoomSelection();
			}
		});
		
		
		controls.add(zoomIn);
		controls.add(zoomOut);
		controls.add(zoomFit);
		controls.add(screenShot);
		controls.add(zoomSelection);
		help = new JButton("Usage", helpIcon);
		help.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				UsageDialog dlg = new UsageDialog(model);
				dlg.setLocationRelativeTo(CrossDiagramApplet.this);
				dlg.setVisible(true);
			}
		});
		help.setEnabled(false);
		controls.add(help);
		
		xAxis = new HorizontalAxisRenderer();
		xAxis.setTopSide(true);
		yAxis = new VerticalAxisRenderer();
		yAxis.setLeftSide(true);
		yAxis.setDisplayAxisColor(false);
		
		gl.setHorizontalGroup(
				gl.createSequentialGroup()
				.addGroup(
					gl.createParallelGroup()
					.addComponent(diagram)
					.addComponent(xAxis, 0, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)
				)
				.addComponent(yAxis, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
		);
		gl.setVerticalGroup(
				gl.createSequentialGroup()
				.addGroup(
					gl.createParallelGroup(Alignment.LEADING)
					.addComponent(diagram, 0, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)
					.addComponent(yAxis, 0, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)
				)
				.addComponent(xAxis, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
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
					split.setDividerLocation(0.9);
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
		infoPanel.setLayout(new BorderLayout());
		
		JPanel checkboxes = new JPanel();
		checkboxes.setLayout(new BoxLayout(checkboxes, BoxLayout.LINE_AXIS));
		
		showExact = new JCheckBox("Display exact points");
		showExact.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				diagram.setShowExact(showExact.isSelected());
				diagram.repaint();
				infoModel.fireTableDataChanged();
			}
		});
		showXInter = new JCheckBox("Display X interpolated points");
		showXInter.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				diagram.setShowXInter(showXInter.isSelected());
				diagram.repaint();
				infoModel.fireTableDataChanged();
			}
		});
		showYInter = new JCheckBox("Display Y interpolated points");
		showYInter.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				diagram.setShowYInter(showYInter.isSelected());
				diagram.repaint();
				infoModel.fireTableDataChanged();
			}
		});
		
		checkboxes.add(Box.createHorizontalGlue());
		checkboxes.add(showExact);
		checkboxes.add(Box.createHorizontalStrut(10));
		checkboxes.add(showXInter);
		checkboxes.add(Box.createHorizontalStrut(10));
		checkboxes.add(showYInter);
		checkboxes.add(Box.createHorizontalGlue());
		
		infoTable = new JTable();
//		infoTable.setAutoCreateRowSorter(true);
		
		JScrollPane sp = new JScrollPane(infoTable);
		sp.setPreferredSize(new Dimension(640, 150));
		infoPanel.add(checkboxes, BorderLayout.NORTH);
		infoPanel.add(sp, BorderLayout.CENTER);
		
	}
	/**
	 * Zoom to the current selection.
	 */
	protected void doZoomSelection() {
		Point2D.Double p1 = diagram.getSelectionStart();
		Point2D.Double p2 = diagram.getSelectionEnd();
		if (p1 != null && p2 != null && p1.x != p2.x && p1.y != p2.y) {
			double newX1 = Math.min(p1.x, p2.x) - diagram.getMinimumX();
			double newY1 = Math.min(p1.y, p2.y) - diagram.getMinimumY();
			
			double newX2 = Math.max(p1.x, p2.x) - diagram.getMinimumX();
			double newY2 = Math.max(p1.y, p2.y) - diagram.getMinimumY();
			
			double scaleX = (newX2 - newX1) / diagram.getDiagramWidth();
			double scaleY = (newY2 - newY1) / diagram.getDiagramHeight();
			double zoomX10 = (diagram.getMaximumX() - diagram.getMinimumX()) / diagram.getDiagramWidth(); 
			double zoomY10 = (diagram.getMaximumY() - diagram.getMinimumY()) / diagram.getDiagramHeight(); 
			
			diagram.setZoomX(scaleX / zoomX10);
			diagram.setZoomY(scaleY / zoomY10);
			xAxis.setZoom(scaleX / zoomX10);
			yAxis.setZoom(scaleY / zoomY10);
			
			limitOffset(newX1, newY1);
			
			diagramComposite.repaint();
			
		}
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
		diagram.setZoom(1.0);
		xAxis.setZoom(1.0);
		yAxis.setZoom(1.0);
		limitOffset(diagram.getOffsetX(), diagram.getOffsetY());
	}
	/**
	 * Zoom out.
	 */
	protected void doZoomOut() {
		diagram.setZoom(diagram.getZoomX() * 1.2);
		xAxis.setZoom(diagram.getZoomX());
		yAxis.setZoom(diagram.getZoomX());
		limitOffset(diagram.getOffsetX(), diagram.getOffsetY());
	}
	/** Zoom in. */
	protected void doZoomIn() {
		diagram.setZoom(diagram.getZoomX() / 1.2);
		xAxis.setZoom(diagram.getZoomX());
		yAxis.setZoom(diagram.getZoomX());
		limitOffset(diagram.getOffsetX(), diagram.getOffsetY());
	}
	/**
	 * Zoom out.
	 * @param x the current x to keep centered
	 * @param y the current y to keep centered
	 */
	protected void doZoomOut(int x, int y) {
		double vx = diagram.getValueAtX(x);
		double vy = diagram.getValueAtY(y);
		
		diagram.setZoom(diagram.getZoomX() * 1.2);
		xAxis.setZoom(diagram.getZoomX());
		yAxis.setZoom(diagram.getZoomX());
		
		double wx = diagram.getValueAtX(x);
		double wy = diagram.getValueAtY(y);

		double newX = diagram.getOffsetX() + (vx - wx);
		double newY = diagram.getOffsetY() + (vy - wy);
		
		limitOffset(newX, newY);
	}
	/** 
	 * Zoom in. 
	 * @param x the current x to keep centered
	 * @param y the current y to keep centered
	 */
	protected void doZoomIn(int x, int y) {
		double vx = diagram.getValueAtX(x);
		double vy = diagram.getValueAtY(y);
		
		diagram.setZoom(diagram.getZoomX() / 1.2);
		xAxis.setZoom(diagram.getZoomX());
		yAxis.setZoom(diagram.getZoomX());
		
		double wx = diagram.getValueAtX(x);
		double wy = diagram.getValueAtY(y);

		double offsetX = diagram.getOffsetX();
		double newX = offsetX + (vx - wx);
		double offsetY = diagram.getOffsetY();
		double newY = offsetY + (vy - wy);
		
		limitOffset(newX, newY);
		
//		System.out.printf("OffsetX = %s, OffsetY = %s, vx = %s, vy = %s, %nwx = %s, wy = %s, X = %s, Y = %s, %n OX = %s, OY = %s%n", 
//				offsetX, offsetY, vx, vy, wx, wy, diagram.getValueAtX(x), diagram.getValueAtY(y), diagram.getOffsetX(), diagram.getOffsetY());
		
	}
	/**
	 * Set the X and Y coordinates limited to the current maximum offset.
	 * @param newX the new X coordinate
	 * @param newY the new Y coordinate
	 */
	private void limitOffset(double newX, double newY) {
		double maxX = diagram.getMaximumX() - diagram.getMinimumX() - diagram.getDiagramWidth() * diagram.computeScaleX();
		double maxY = diagram.getMaximumY() - diagram.getMinimumY() - diagram.getDiagramHeight() * diagram.computeScaleY();
//		System.out.printf("maxx = %s, maxy = %s%n", maxX, maxY);
		diagram.setOffsetX(Math.max(Math.min(newX, maxX), 0));
		diagram.setOffsetY(Math.max(Math.min(newY, maxY), 0));
		
		xAxis.setOffset(diagram.getOffsetX());
		yAxis.setOffset(diagram.getOffsetY());
		
		diagramComposite.repaint();
//		diagram.repaint();
//		xAxis.repaint();
//		yAxis.repaint();
	}
	/**
	 * Drag the viewport.
	 * @param dx the X delta
	 * @param dy the Y delta
	 */
	protected void doDrag(int dx, int dy) {
		double newX = diagram.getOffsetX() - dx * diagram.computeScaleX();
		double newY = diagram.getOffsetY() + dy * diagram.computeScaleY();
		limitOffset(newX, newY);
	}
	/**
	 * Process the scada diagram data.
	 * @param model the data
	 */
	protected void processData(DataDiagram model) {
		this.model = model;
		if (model != null) {
			diagram.setModel(this.model);
			
			showExact.setText(model.get("Display exact points"));
			showXInter.setText(model.get("Display X interpolated points"));
			showYInter.setText(model.get("Display Y interpolated points"));
			showExact.setSelected(diagram.isShowExact());
			showXInter.setSelected(diagram.isShowXInter());
			showYInter.setSelected(diagram.isShowYInter());
			
			MinMax horiz = new MinMax();
			horiz.color = Color.BLACK;
			horiz.name = diagram.getXSignalName();
			horiz.min = diagram.getMinimumX();
			horiz.max = diagram.getMaximumX();
			xAxis.getAxises().add(horiz);
			
			MinMax vert = new MinMax();
			vert.color = Color.BLACK;
			vert.name = diagram.getYSignalName();
			vert.min = diagram.getMinimumY();
			vert.max = diagram.getMaximumY();
			yAxis.getAxises().add(vert);
	
			diagram.setXAxisRenderer(xAxis.createAxisRenderer());
			diagram.setYAxisRenderer(yAxis.createAxisRenderer(0));
	
			xAxis.invalidate();
			yAxis.invalidate();
			diagram.invalidate();
			
			infoModel = new ValueModel();
			infoModel.setColumnClasses(Integer.class, String.class, String.class, Double.class, Double.class);
			infoModel.setColumnNames(model.get("Index"), model.get("Color"), model.get("Type"), diagram.getXSignalName(), diagram.getYSignalName());
			
			infoTable.setModel(infoModel);
			
			infoTable.getColumnModel().getColumn(1).setPreferredWidth(35);
			infoTable.getColumnModel().getColumn(2).setPreferredWidth(175);
			infoTable.getColumnModel().getColumn(3).setPreferredWidth(250);
			infoTable.getColumnModel().getColumn(4).setPreferredWidth(250);
			infoTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
			infoTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			
			TableRowSorter<ValueModel> sorter = new TableRowSorter<ValueModel>(infoModel);
			sorter.setRowFilter(new RowFilter<Object, Object>() {
				@Override
				public boolean include(
						javax.swing.RowFilter.Entry<? extends Object, ? extends Object> entry) {
					int idx = (Integer)entry.getIdentifier();
					if (!showExact.isSelected() && infoModel.get(idx).mode == InterpolationMode.EXACT) {
						return false;
					}
					if (!showXInter.isSelected() && infoModel.get(idx).mode == InterpolationMode.X_VALUE_INTERPOLATED) {
						return false;
					}
					if (!showYInter.isSelected() && infoModel.get(idx).mode == InterpolationMode.Y_VALUE_INTERPOLATED) {
						return false;
					}
					return true;
				}
			});
			infoTable.setRowSorter(sorter);
		}
		help.setEnabled(true);
		invalidate();
		repaint();
	}
	/** Do list info. */
	protected void doListInfo() {
		infoModel.clear();
		double x0 = Math.min(diagram.getSelectionStart().x, diagram.getSelectionEnd().x);
		double y0 = Math.min(diagram.getSelectionStart().y, diagram.getSelectionEnd().y);
		double x1 = Math.max(diagram.getSelectionStart().x, diagram.getSelectionEnd().x);
		double y1 = Math.max(diagram.getSelectionStart().y, diagram.getSelectionEnd().y);
		Color[] colors = new Color[] { diagram.getNormalColor(), diagram.getXColor(), diagram.getYColor() };
		for (Map.Entry<InterpolationMode, List<DiagramValue>> e : diagram.getValues().entrySet()) {
			for (DiagramValue dv : e.getValue()) {
				if (x0 <= dv.x && dv.x <= x1 && y0 <= dv.y && dv.y <= y1) {
					ValueEntry ve = new ValueEntry();
					ve.mode = e.getKey();
					ve.x = dv.x;
					ve.y = dv.y;
					ve.color = colors[ve.mode.ordinal()];
					infoModel.add(ve);
				}
			}
		}
		
	}
}
