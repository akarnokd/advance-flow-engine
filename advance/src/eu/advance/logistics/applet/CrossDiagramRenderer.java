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
import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Point2D;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import javax.swing.JComponent;

/**
 * The cross diagram renderer.
 * @author karnokd
 *
 */
public class CrossDiagramRenderer extends JComponent {
	/** */
	private static final long serialVersionUID = 758405474134417105L;
	/** The diagram model. */
	private DataDiagram model;
	/** The zoom level on the X coordinate. */
	private double zoomX = 1.0;
	/** The zoom level on the Y coordinate. */
	private double zoomY = 1.0;
	/** The X offset for panning. */
	private double offsetX;
	/** The Y offset for painting. */
	private double offsetY;
	/** The list of diagram values. */
	private Map<InterpolationMode, List<DiagramValue>> values;
	/** The values on the X coordinate. */
	private DataDiagramValues<DataSignal> valuesX;
	/** The values on the Y coordinate. */
	private DataDiagramValues<DataSignal> valuesY;
	/** Show exact values. */
	private boolean showExact = true;
	/** Show X interpolated values. */
	private boolean showXInter = true;
	/** Show Y interpolated values. */
	private boolean showYInter = true;
	/** The interpolation mode. */
	public enum InterpolationMode {
		/** Exact value. */
		EXACT,
		/** The X value was interpolated. */
		X_VALUE_INTERPOLATED,
		/** The Y value was interpolated. */
		Y_VALUE_INTERPOLATED
	}
	/** The non-interpolated color. */
	private Color normalColor = new Color(0, 0, 128);
	/** The color for interpolated X values. */
	private Color xColor = new Color(255, 0, 0);
	/** THe color for interpolated Y values. */
	private Color yColor = new Color(0, 224, 0);
	/** The point radius. */
	private int pointRadius = 2;
	/** The X axis renderer. */
	private AxisRenderer xAxisRenderer;
	/** The Y axis renderer. */
	private AxisRenderer yAxisRenderer;
	/** The selection start. */
	private Point2D.Double selectionStart;
	/** The selection end. */
	private Point2D.Double selectionEnd;
	/**
	 * A diagram value. 
	 * @author karnokd
	 */
	public static class DiagramValue {
		/** The X value. */
		public double x;
		/** The Y value. */
		public double y;
		/** The timestamp of the point. */
		public Timestamp timestamp;
		/** Interpolation mode. */
		public InterpolationMode mode;
	}
	/**
	 * @return the diagram model
	 */
	public DataDiagram getModel() {
		return model;
	}
	@Override
	public void paint(Graphics g) {
		Graphics2D g2 = (Graphics2D)g;
		g2.setColor(Color.WHITE);
		g2.fillRect(0, 0, getWidth(), getHeight());
		if (values == null) {
			return;
		}
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		double scalex = computeScaleX();
		double scaley = computeScaleY();
		
		int textheight = getFontMetrics(getFont()).getHeight();
		
//		g2.setColor(Color.DARK_GRAY);
//		g2.drawRect(textheight / 2, textheight / 2, getDiagramWidth(), getDiagramHeight());

		Composite c = g2.getComposite();
		g2.setComposite(AlphaComposite.SrcOver.derive(0.25f));
		g2.setColor(Color.DARK_GRAY);
		if (xAxisRenderer != null) {
			xAxisRenderer.paint(g2, getHeight());
		}
		if (yAxisRenderer != null) {
			yAxisRenderer.paint(g2, getWidth());
		}
		g2.setComposite(c);

		
		for (Map.Entry<InterpolationMode, List<DiagramValue>> e : values.entrySet()) {
			switch (e.getKey()) {
			case EXACT:
				if (!showExact) {
					continue;
				}
				g2.setColor(normalColor);
				break;
			case X_VALUE_INTERPOLATED:
				if (!showXInter) {
					continue;
				}
				g2.setColor(xColor);
				break;
			case Y_VALUE_INTERPOLATED:
				if (!showYInter) {
					continue;
				}
				g2.setColor(yColor);
				break;
			default:
			}
			double m = Double.MAX_VALUE;
			for (DiagramValue dv : e.getValue()) {
				m = Math.min(m, dv.x);
				int x = (int)((dv.x - offsetX - valuesX.minimum.doubleValue()) / scalex) + textheight / 2;
				int y = getDiagramHeight() - (int)((dv.y - offsetY - valuesY.minimum.doubleValue()) / scaley) + textheight / 2;
				g2.fillOval(x - pointRadius, y - pointRadius, 2 * pointRadius, 2 * pointRadius);
			}
		}
		if (selectionStart != null || selectionEnd != null) {
			g2.setComposite(AlphaComposite.SrcOver.derive(0.5f));
			g2.setColor(Color.BLUE);
			int x0 = 0;
			int y0 = 0;
			int x1 = 0;
			int y1 = 0;
			if (selectionStart != null) {
				x0 = (int)((selectionStart.x - offsetX - valuesX.minimum.doubleValue()) / scalex) + textheight / 2;
				y0 = getDiagramHeight() - (int)((selectionStart.y - offsetY - valuesY.minimum.doubleValue()) / scaley) + textheight / 2;
				g2.drawLine(0, y0, getWidth(), y0);
				g2.drawLine(x0, 0, x0, getHeight());
			}
			if (selectionEnd != null) {
				x1 = (int)((selectionEnd.x - offsetX - valuesX.minimum.doubleValue()) / scalex) + textheight / 2;
				y1 = getDiagramHeight() - (int)((selectionEnd.y - offsetY - valuesY.minimum.doubleValue()) / scaley) + textheight / 2;
				g2.drawLine(0, y1, getWidth(), y1);
				g2.drawLine(x1, 0, x1, getHeight());
			}
			if (selectionStart != null && selectionEnd != null) {
				g2.fillRect(Math.min(x0, x1), Math.min(y0, y1), Math.abs(x1 - x0) + 1, Math.abs(y1 - y0) + 1);
			}
		}
	}
	/**
	 * Sets the diagram model. Does not repaint automatically.
	 * @param model the model to set
	 */
	public void setModel(DataDiagram model) {
		this.model = model;
		if (model != null) {
			prepareModel();
		} else {
			values = null;
			valuesX = null;
			valuesY = null;
		}
	}
	/**
	 * Set the X offset.
	 * @param offsetX the X offset on the diagram
	 */
	public void setOffsetX(double offsetX) {
		this.offsetX = offsetX;
	}
	/** 
	 * Set the Y offset.
	 * @param offsetY the Y offset on the diagram
	 */
	public void setOffsetY(double offsetY) {
		this.offsetY = offsetY;
	}
	/**
	 * Set the horizontal zoom value.
	 * @param zoomX the zoom value
	 */
	public void setZoomX(double zoomX) {
		this.zoomX = zoomX;
	}
	/**
	 * Set the vertical zoom value.
	 * @param zoomY the zoom value
	 */
	public void setZoomY(double zoomY) {
		this.zoomY = zoomY;
	}
	/**
	 * Set the zoom value on both axis.
	 * @param zoom the zoom level.
	 */
	public void setZoom(double zoom) {
		this.zoomX = zoom;
		this.zoomY = zoom;
	}
	/**
	 * @return the diagram width in pixels excluding any vertical axis components
	 */
	public int getDiagramWidth() {
		return getWidth() - getFontMetrics(getFont()).getHeight();
	}
	/**
	 * @return the diagram height in pixels exluding any horizontal axis components
	 */
	public int getDiagramHeight() {
		return getHeight() - getFontMetrics(getFont()).getHeight();
	}
	/**
	 * @return compute the value/pixel scale on the X axis
	 */
	public double computeScaleX() {
		return (valuesX.maximum.doubleValue() - valuesX.minimum.doubleValue()) / getDiagramWidth() * zoomX;
	}
	/**
	 * @return compute the value/pixel scale on the Y axis
	 */
	public double computeScaleY() {
		return (valuesY.maximum.doubleValue() - valuesY.minimum.doubleValue()) / getDiagramHeight() * zoomY;	
	}
	/** Prepare the model for display. */
	private void prepareModel() {
		values = new HashMap<InterpolationMode, List<DiagramValue>>();
		Iterator<DataDiagramValues<DataSignal>> it = model.signals.values().iterator();
		if (it.hasNext()) {
			valuesX = it.next();
			if (it.hasNext()) {
				valuesY = it.next();
			} else {
				valuesY = valuesX;
			}
		}
		Comparator<DataSignal> comp = new Comparator<DataSignal>() {
			@Override
			public int compare(DataSignal o1, DataSignal o2) {
				return o1.timestamp.compareTo(o2.timestamp);
			}
		};
		TreeSet<DataSignal> allSignal = new TreeSet<DataSignal>(comp);
		allSignal.addAll(valuesX.values);
		allSignal.addAll(valuesY.values);
		Collections.sort(valuesX.values, comp);
		Collections.sort(valuesY.values, comp);
		for (DataSignal ssa : allSignal) {
			int xi = Collections.binarySearch(valuesX.values, ssa, comp);
			int yi = Collections.binarySearch(valuesY.values, ssa, comp);
			if (xi >= 0 && yi >= 0) {
				DataSignal xs = valuesX.values.get(xi);
				DataSignal ys = valuesY.values.get(yi);
				
				DiagramValue dv = new DiagramValue();
				dv.mode = InterpolationMode.EXACT;
				dv.timestamp = xs.timestamp;
				dv.x = xs.value.doubleValue();
				dv.y = ys.value.doubleValue();
				addDiagramValue(dv, dv.mode);
			} else
			if (xi >= 0 && yi < 0) {
				DataSignal xs = valuesX.values.get(xi);
				// int yi = (-(ip) - 1)
				int ip = -(yi + 1);
				DataSignal ys0 = null;
				DataSignal ys1 = null;
				if (ip == valuesY.values.size()) {
					ys0 = valuesY.values.get(ip - 1);
					ys1 = ys0;
				} else
				if (ip == 0) {
					ys0 = valuesY.values.get(0);
					ys1 = ys0;
				} else {
					ys0 = valuesY.values.get(ip - 1);
					ys1 = valuesY.values.get(ip);
				}
				
				DiagramValue dv = new DiagramValue();
				dv.mode = InterpolationMode.Y_VALUE_INTERPOLATED;
				dv.timestamp = xs.timestamp;
				dv.x = xs.value.doubleValue();
				if (ys0 == ys1) {
					dv.y = ys0.value.doubleValue();
				} else {
					dv.y = ys0.value.doubleValue() + (ys1.value.doubleValue() - ys0.value.doubleValue()) * (xs.timestamp.getTime() - ys0.timestamp.getTime()) / (ys1.timestamp.getTime() - ys0.timestamp.getTime());
				}
				addDiagramValue(dv, dv.mode);
				
			} else
			if (xi < 0 && yi >= 0) {
				DataSignal ys = valuesY.values.get(yi);
				
				int ip = -(xi + 1);
				DataSignal xs0 = null;
				DataSignal xs1 = null;
				if (ip == valuesX.values.size()) {
					xs0 = valuesX.values.get(ip - 1);
					xs1 = xs0;
				} else
				if (ip == 0) {
					xs0 = valuesX.values.get(0);
					xs1 = xs0;
				} else {
					xs0 = valuesX.values.get(ip - 1);
					xs1 = valuesX.values.get(ip);
				}
				
				
				DiagramValue dv = new DiagramValue();
				dv.mode = InterpolationMode.X_VALUE_INTERPOLATED;
				dv.timestamp = ys.timestamp;
				dv.y = ys.value.doubleValue();
				if (xs0 == xs1) {
					dv.x = xs0.value.doubleValue();
				} else {
					dv.x = xs0.value.doubleValue() + (xs1.value.doubleValue() - xs0.value.doubleValue()) * (ys.timestamp.getTime() - xs0.timestamp.getTime()) / (xs1.timestamp.getTime() - xs0.timestamp.getTime());
				}
				
				addDiagramValue(dv, dv.mode);
				
			}
		}
	}
	/**
	 * Add a diagram value to the proper interpolation bucket.
	 * @param d the diagram value
	 * @param m the interpolation mode
	 */
	private void addDiagramValue(DiagramValue d, InterpolationMode m) {
		List<DiagramValue> dv = values.get(m);
		if (dv == null) {
			dv = new ArrayList<DiagramValue>();
			values.put(m, dv);
		}
		dv.add(d);
	}
	/**
	 * @return the horizontal zoom factor
	 */
	public double getZoomX() {
		return zoomX;
	}
	/**
	 * @return the vertical zoom factor
	 */
	public double getZoomY() {
		return zoomY;
	}
	/**
	 * Get the numerical value at the given X coordinate.
	 * @param x the X coordinate
	 * @return the value
	 */
	public double getValueAtX(int x) {
		return valuesX.minimum.doubleValue() + offsetX + x * computeScaleX();
	}
	/**
	 * Get the numerical value at the given Y coordinate.
	 * @param y the Y coordinate
	 * @return the value
	 */
	public double getValueAtY(int y) {
		return valuesY.minimum.doubleValue() + offsetY + (getDiagramHeight() - y) * computeScaleY();
	}
	/**
	 * @return get the X offset 
	 */
	public double getOffsetX() {
		return offsetX;
	}
	/**
	 * @return get the Y offset
	 */
	public double getOffsetY() {
		return offsetY;
	}
	/**
	 * @return return the minimum value of the X series
	 */
	public double getMinimumX() {
		return valuesX.minimum.doubleValue();
	}
	/**
	 * @return return the minimum value of the Y series
	 */
	public double getMinimumY() {
		return valuesY.minimum.doubleValue();
	}
	/**
	 * @return return the maximum value of the X series
	 */
	public double getMaximumX() {
		return valuesX.maximum.doubleValue();
	}
	/**
	 * @return return the maximum value of the Y series
	 */
	public double getMaximumY() {
		return valuesY.maximum.doubleValue();
	}
	/**
	 * @return the showExact
	 */
	public boolean isShowExact() {
		return showExact;
	}
	/**
	 * @param showExact the showExact to set
	 */
	public void setShowExact(boolean showExact) {
		this.showExact = showExact;
	}
	/**
	 * @return the showXInter
	 */
	public boolean isShowXInter() {
		return showXInter;
	}
	/**
	 * @param showXInter the showXInter to set
	 */
	public void setShowXInter(boolean showXInter) {
		this.showXInter = showXInter;
	}
	/**
	 * @return the showYInter
	 */
	public boolean isShowYInter() {
		return showYInter;
	}
	/**
	 * @param showYInter the showYInter to set
	 */
	public void setShowYInter(boolean showYInter) {
		this.showYInter = showYInter;
	}
	/**
	 * Set the X axis renderer.
	 * @param xAxisRenderer the X axis renderer
	 */
	public void setXAxisRenderer(AxisRenderer xAxisRenderer) {
		this.xAxisRenderer = xAxisRenderer;
	}
	/**
	 * @return get the X axis renderer.
	 */
	public AxisRenderer getXAxisRenderer() {
		return xAxisRenderer;
	}
	/**
	 * Get Y axis renderer.
	 * @param yAxisRenderer the y axis renderer
	 */
	public void setYAxisRenderer(AxisRenderer yAxisRenderer) {
		this.yAxisRenderer = yAxisRenderer;
	}
	/**
	 * @return the y axis renderer
	 */
	public AxisRenderer getYAxisRenderer() {
		return yAxisRenderer;
	}
	/**
	 * Set the selection start point.
	 * @param x the x value
	 * @param y the y value
	 */
	public void setSelectionStart(double x, double y) {
		selectionStart = new Point2D.Double(x, y);
	}
	/**
	 * Set the selection start.
	 * @param point the point2d
	 */
	public void setSelectionStart(Point2D.Double point) {
		selectionStart = point;
	}
	/**
	 * Set the selection start.
	 * @param point the point2d
	 */
	public void setSelectionEnd(Point2D.Double point) {
		selectionEnd = point;
	}
	/**
	 * Set the selection end point.
	 * @param x the x value
	 * @param y the y value
	 */
	public void setSelectionEnd(double x, double y) {
		selectionEnd = new Point2D.Double(x, y);
	}
	/**
	 * Clear the selection start.
	 */
	public void clearSelectionStart() {
		selectionStart = null;
	}
	/**
	 * Clear the selection end.
	 */
	public void clearSelectionEnd() {
		selectionEnd = null;
	}
	/**
	 * @return the current selection start
	 */
	public Point2D.Double getSelectionStart() {
		return selectionStart;
	}
	/**
	 * @return the current selection end
	 */
	public Point2D.Double getSelectionEnd() {
		return selectionEnd;
	}
	/**
	 * Get the X and Y values.
	 * @param x the mouse X coordinate
	 * @param y the mouse Y coordinate
	 * @return the function values
	 */
	public Point2D.Double getValuesAt(int x, int y) {
		int textheight = getFontMetrics(getFont()).getHeight();

		double vx = (x - textheight / 2) * computeScaleX() + offsetX + valuesX.minimum.doubleValue();
		double vy = (getDiagramHeight()  - y + textheight / 2) * computeScaleY() + offsetY + valuesY.minimum.doubleValue();
		
		return new Point2D.Double(vx, vy);
	}
	/**
	 * @return the X axis signal name
	 */
	public String getXSignalName() {
		return valuesX.values.get(0).signalName;
	}
	/**
	 * @return the Y axis signal name
	 */
	public String getYSignalName() {
		return valuesY.values.get(0).signalName;
	}
	/**
	 * @return get the value list.
	 */
	public Map<InterpolationMode, List<DiagramValue>> getValues() {
		return values;
	}
	/**
	 * Set the normal color.
	 * @param normalColor the normal color.
	 */
	public void setNormalColor(Color normalColor) {
		this.normalColor = normalColor;
	}
	/**
	 * @return the normal color
	 */
	public Color getNormalColor() {
		return normalColor;
	}
	/**
	 * Set the X interpolated color.
	 * @param xColor the color
	 */
	public void setXColor(Color xColor) {
		this.xColor = xColor;
	}
	/**
	 * @return the X interpolated color
	 */
	public Color getXColor() {
		return xColor;
	}
	/**
	 * Set the Y interpolated color.
	 * @param yColor the color
	 */
	public void setYColor(Color yColor) {
		this.yColor = yColor;
	}
	/**
	 * @return the Y interpolated color
	 */
	public Color getYColor() {
		return yColor;
	}
}
