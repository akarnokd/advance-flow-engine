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
 * The horizontal axis renderer.
 * @author karnokd
 */
public class HorizontalAxisRenderer extends JComponent {
	/** */
	private static final long serialVersionUID = -477338970860780463L;
	/** The list of axises. */
	List<MinMax> axises = new ArrayList<MinMax>();
	/** Draw the ticks on the top side? */
	private boolean topSide;
	/** Is axis selected. */
	private int selected = -1;
	/** The zoom factor. */
	private double zoom = 1.0;
	/** The current numerical offset. */
	private double offset;
	@Override
	public void paint(Graphics g) {
		Graphics2D g2 = (Graphics2D)g;
		g2.setColor(Color.WHITE);
		g2.fillRect(0, 0, getWidth(), getHeight());
		if (axises.size() == 0) {
			return;
		}
		
		FontMetrics fm = getFontMetrics(getFont());
		Font bold = getFont().deriveFont(Font.BOLD);
		FontMetrics fmb = getFontMetrics(bold);
		Font font90 = getFont().deriveFont(AffineTransform.getRotateInstance(-Math.PI / 2));
		
		int textHeight = fm.getHeight();
		int w = getWidth();
		int h = getHeight() / axises.size();
		
		int y = 0;
		int k = 0;
		for (MinMax mm : axises) {
			if (k == getSelected()) {
				g2.setColor(Color.BLUE);
				g2.fillRect(0, y, w, h);
				g2.setColor(Color.WHITE);
			} else {
				g2.setColor(Color.BLACK);
			}
			double scale = (mm.max - mm.min) / (w - textHeight) * zoom;
			int x0 = textHeight / 2 - (int)(offset / scale);
			int x1 = x0 + (int)((mm.max - mm.min) / scale);
			g2.setFont(bold);
			int lw = fmb.stringWidth(mm.name);
			if (isTopSide()) {
				g2.drawLine(x0, y, x1, y);
				g2.drawString(mm.name, (w - lw) / 2, y + h - fm.getHeight() - 2 + fm.getAscent());
//				g2.setColor(mm.color);
//				g2.fillRect(x + width - fm.getHeight() - 2, (height + lw) / 2 - lw - 4 - fm.getHeight(), fm.getHeight(), fm.getHeight());
			} else {
				g2.drawLine(x0, y + h - 1, x1, y + h - 1);
				g2.drawString(mm.name, (w - lw) / 2, y + 2 + fm.getAscent());
//				g2.setColor(mm.color);
//				g2.fillRect(x + 2, (height + lw) / 2 - lw - 4 - fm.getHeight(), fm.getHeight(), fm.getHeight());
			}
			
			if (k == getSelected()) {
				g2.setColor(Color.WHITE);
			} else {
				g2.setColor(Color.BLACK);
			}
			g2.setFont(font90);
			int lineCount = (x1 - x0) / textHeight;
			double dx = 1.0 * (x1 - x0) / lineCount;
			double dv = (mm.max - mm.min) / lineCount;
			DecimalFormat df = getFormatter(mm.min, mm.max);
			for (int i = 0; i <= lineCount; i++) {
				int x = x0 + (int)(dx * i);
				double v = mm.min + (dv * i);
				String sv = df.format(v);
				
				if (isTopSide()) {
					g2.drawLine(x, y, x, y + 6);
					g2.drawString(sv, x + fm.getAscent() / 2, y + fm.stringWidth(sv) + 8);
				} else {
					g2.drawLine(x, y + h - 6, x, y + h - 1);
					g2.drawString(sv, x + fm.getAscent() / 2, y + h - 8);
				}
			}
			
			y += h;
			k++;
		}
	}
	@Override
	public Dimension getPreferredSize() {
		FontMetrics fm = getFontMetrics(getFont());
		int w = 0;
		for (MinMax mm : axises) {
			double nscale1 = Math.abs(Math.log10(Math.max(Math.abs(mm.min), Math.abs(mm.max))));
			StringBuilder sb = new StringBuilder("-.");
			for (int i = 0; i < nscale1 + 2; i++) {
				sb.append("#");
			}
			w += fm.stringWidth(sb.toString()) + 10 + fm.getHeight() + 4;
		}
		return new Dimension(640, w);
	};
	/**
	 * @return The list of axises 
	 */
	public List<MinMax> getAxises() {
		return axises;
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
	 * Set the top side indicator.
	 * @param topSide paint the ticks topside?
	 */
	public void setTopSide(boolean topSide) {
		this.topSide = topSide;
	}
	/**
	 * @return is the ticks drawn top side?
	 */
	public boolean isTopSide() {
		return topSide;
	}
	/**
	 * Set the selected index.
	 * @param selected the selected index
	 */
	 
	public void setSelected(int selected) {
		this.selected = selected;
	}
	/**
	 * @return Get the selected index
	 */
	public int getSelected() {
		return selected;
	}
	/**
	 * Sets the zoom factor. Does not repaint.
	 * @param zoom the zoom factor
	 */
	public void setZoom(double zoom) {
		this.zoom = zoom;
	}
	/**
	 * @return Retrieves the current zoom factor. 
	 */
	public double getZoom() {
		return zoom;
	}
	/** 
	 * Set the relative offset.
	 * @param offset the current relative offset
	 */
	public void setOffset(double offset) {
		this.offset = offset;
	}
	/**
	 * @return the current relative offset
	 */
	public double getOffset() {
		return offset;
	}
	/**
	 * Create an axis renderer.
	 * @return the axis renderer
	 */
	public AxisRenderer createAxisRenderer() {
		return new AxisRenderer() {
			@Override
			public void paint(Graphics2D g2, int width) {
				MinMax mm = axises.get(0);
				FontMetrics fm = getFontMetrics(getFont());

				int textHeight = fm.getHeight();
				double scale = (mm.max - mm.min) / (getWidth() - textHeight) * zoom;
				int x0 = textHeight / 2 - (int)(offset / scale);
				int x1 = x0 + (int)((mm.max - mm.min) / scale);
				int lineCount = (x1 - x0) / textHeight;
				double dx = 1.0 * (x1 - x0) / lineCount;
				for (int i = 0; i <= lineCount; i++) {
					int x = x0 + (int)(dx * i);
					g2.drawLine(x, 0, x, width);
				}
			}
		};
	}
}
