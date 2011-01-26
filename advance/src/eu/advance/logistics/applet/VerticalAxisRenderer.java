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
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JComponent;

/**
 * The multiple vertical axis renderer.
 * @author karnokd
 *
 */
public class VerticalAxisRenderer extends JComponent {
	/** */
	private static final long serialVersionUID = 8300337673377022820L;
	/** The selected axis. */
	private int selected = -1;
	/** Show the line marks at the left side? */
	private boolean leftSide;
	/** Display the axis color box? */
	private boolean displayAxisColor = true;
	/** The list of axises. */
	List<MinMax> axises = new ArrayList<MinMax>();
	/** The current offset. */
	private double offset;
	/** The zoom value. */
	private double zoom = 1.0;
	@Override
	public Dimension getPreferredSize() {
		FontMetrics fm = getFontMetrics(getFont());
		int w = 0;
		for (MinMax mm : axises) {
			DecimalFormat df = getFormatter(mm.min, mm.max);
			String s = df.format(mm.min);
			String s1 = df.format(mm.max);
			w += fm.stringWidth(s.length() > s1.length() ? s : s1) + 10 + fm.getHeight() + 4;
		}
		return new Dimension(w, 480);
	}
	@Override
	public void paint(Graphics g) {
		Graphics2D g2 = (Graphics2D)g;
		
		g2.setColor(Color.WHITE);
		g2.fillRect(0, 0, getWidth(), getHeight());
		
		if (axises.size() == 0) {
			return;
		}
		
		FontMetrics fm = getFontMetrics(getFont());
		FontMetrics fmb = getFontMetrics(getFont().deriveFont(Font.BOLD));
		Font font90 = getFont().deriveFont(AffineTransform.getRotateInstance(-Math.PI / 2)).deriveFont(Font.BOLD);
		
		int textHeight = fm.getHeight();
		int width = getWidth() / axises.size();
		
		int x = 0;
		int k = 0;
		for (MinMax mm : axises) {
			if (k == selected) {
				g2.setColor(Color.BLUE);
				g2.fillRect(x, 0, width, getHeight());
				g2.setColor(Color.WHITE);
			} else {
				g2.setColor(Color.BLACK);
			}
			
			double scale = (mm.max - mm.min) / (getHeight() - textHeight) * zoom;
			int y1 = (getHeight() - textHeight / 2) + (int)((offset) / scale);
			int y0 = y1 - (int)((mm.max - mm.min) / scale);
			g2.setFont(font90);
			int lw = fmb.stringWidth(mm.name);
			if (leftSide) {
				g2.drawLine(x, y0, x, y1);
				g2.drawString(mm.name, x + width - fm.getHeight() - 2 + fm.getAscent(), (getHeight() + lw) / 2);
				if (isDisplayAxisColor()) {
					g2.setColor(mm.color);
					g2.fillRect(x + width - fm.getHeight() - 2, (getHeight() + lw) / 2 - lw - 4 - fm.getHeight(), fm.getHeight(), fm.getHeight());
				}
			} else {
				g2.drawLine(x + width - 1, y0, x + width - 1, y1);
				g2.drawString(mm.name, x + 2 + fm.getAscent(), (getHeight() + lw) / 2);
				if (isDisplayAxisColor()) {
					g2.setColor(mm.color);
					g2.fillRect(x + 2, (getHeight() + lw) / 2 - lw - 4 - fm.getHeight(), fm.getHeight(), fm.getHeight());
				}
			}
			if (k == selected) {
				g2.setColor(Color.WHITE);
			} else {
				g2.setColor(Color.BLACK);
			}
			g2.setFont(getFont());
			int lineCount = (y1 - y0) / textHeight;
			double dy = 1.0 * (y1 - y0) / lineCount;
			double dv = (mm.max - mm.min) / lineCount;
			DecimalFormat df = getFormatter(mm.min, mm.max);
			for (int i = 0; i <= lineCount; i++) {
				int y = y0 + (int)(dy * i);
				double v = mm.max - (dv * i);
				String sv = df.format(v);
				
				if (leftSide) {
					g2.drawLine(x, y, x + 6, y);
					g2.drawString(sv, x + 8, y + fm.getAscent() / 2);
				} else {
					g2.drawLine(x + width - 6, y, x + width - 1, y);
					g2.drawString(sv, x + width - 8 - fm.stringWidth(sv), y + fm.getAscent() / 2);
				}
			}
			
			x += width;
			k++;
		}
	}
	/**
	 * Get a scaled number formatter.
	 * @param min the minimum value
	 * @param max the maximum value
	 * @return the decimal formatter
	 */
	DecimalFormat getFormatter(double min, double max) {
		double log = Math.log10(Math.max(Math.abs(min), Math.abs(max)));
		int nscale1 = (int)(log < 0 ? Math.floor(log) : Math.ceil(log));
		if (nscale1 > 2) {
			return new DecimalFormat("#,##0");
		} else
		if (nscale1 == 2) {
			return new DecimalFormat("#0.0");
		} else 
		if (nscale1 == 1) {
			return new DecimalFormat("0.00");
		} else
		if (nscale1 == 0) {
			return new DecimalFormat("0.000");
		} else
		if (nscale1 == -1) {
			return new DecimalFormat("0.0000");
		} else
		if (nscale1 == -2) {
			return new DecimalFormat("0.00000");
		} else {
			return new DecimalFormat("0.000000");
		}
	}
	/**
	 * Returns the list of axises.
	 * @return the list of axises.
	 */
	public List<MinMax> getAxises() {
		return axises;
	}
	/**
	 * Display the markers on the left side?
	 * @param leftSide markers on the left side
	 */
	public void setLeftSide(boolean leftSide) {
		this.leftSide = leftSide;
	}
	/**
	 * Is the marker on the left side?
	 * @return the marker is on the left side
	 */
	public boolean isLeftSide() {
		return leftSide;
	}
	/** 
	 * The selected axis. 
	 * @param selected the new selected index
	 */
	public void setSelected(int selected) {
		this.selected = selected;
	}
	/**
	 * @return the selected index
	 */
	public int getSelected() {
		return selected;
	}
	/**
	 * Create an axis renderer for the given axis index.
	 * @param index the axis index
	 * @return the renderer or null
	 */
	public AxisRenderer createAxisRenderer(final int index) {
		if (index < 0 || index >= axises.size()) {
			return null;
		}
		return new AxisRenderer() {
			@Override
			public void paint(Graphics2D g2, int width) {
				MinMax mm = axises.get(index);
				FontMetrics fm = g2.getFontMetrics(getFont());
				int textHeight = fm.getHeight();
				
				double scale = (mm.max - mm.min) / (getHeight() - textHeight) * zoom;
				int y1 = (getHeight() - textHeight / 2) + (int)((offset) / scale);
				int y0 = y1 - (int)((mm.max - mm.min) / scale);

				int lineCount = (y1 - y0) / textHeight;
				double dy = 1.0 * (y1 - y0) / lineCount;
				for (int i = 0; i <= lineCount; i++) {
					int y = y0 + (int)(dy * i);
					g2.drawLine(0, y, width, y);
				}
			}
		};
	}
	/**
	 * Display the axis color after the name?
	 * @param displayAxisColor display?
	 */
	public void setDisplayAxisColor(boolean displayAxisColor) {
		this.displayAxisColor = displayAxisColor;
	}
	/**
	 * @return is the axis color displayed after the name?
	 */
	public boolean isDisplayAxisColor() {
		return displayAxisColor;
	}
	/**
	 * Set the value offset.
	 * @param offset the offset
	 */
	public void setOffset(double offset) {
		this.offset = offset;
	}
	/**
	 * @return the current offset
	 */
	public double getOffset() {
		return offset;
	}
	/**
	 * Set the zoom value.
	 * @param zoom the zoom value
	 */
	public void setZoom(double zoom) {
		this.zoom = zoom;
	}
	/**
	 * @return the zoom value
	 */
	public double getZoom() {
		return zoom;
	}
}
