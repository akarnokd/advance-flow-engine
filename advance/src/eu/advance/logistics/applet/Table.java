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

import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JComponent;

/**
 * A component to draw an untitled multicolumn table with optional
 * tooltip on cells.
 * @author karnokd, 2008.02.06.
 * @version $Revision 1.0$
 */
public class Table extends JComponent {
	/**
	 * 
	 */
	private static final long serialVersionUID = -1607781118816324929L;
	/** The cell data model. */
	private TableModel model;
	/** The row height. */
	private int rowHeight = 30;
	/** Offset to rendering the columns. */
	private int offset;
	/** Draw horizontal grid. */
	private boolean drawHorizontalGrid = true;
	/** Draw vertical grid. */
	private boolean drawVerticalGrid = true;
	/** The left margin. */
	private int leftMargin = 5;
	/** The right margin. */
	private int rightMargin = 5;
	/**
	 * The preferred column widths.
	 */
	private int[] columnWidths;
	/**
	 * Constructor.
	 * Adds a location sensitive tooltip selector.
	 */
	public Table() {
		addMouseMotionListener(new MouseAdapter() {
			@Override
			public void mouseMoved(MouseEvent e) {
				setToolTipText(getTooltipAtPoint(e.getX(), e.getY()));
			}
		});
	}
	/**
	 * @return return the data model.
	 */
	public TableModel getModel() {
		return model;
	}
	/**
	 * Set the data model.
	 * @param model the data model.
	 */
	public void setModel(TableModel model) {
		this.model = model;
	}
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
		int rows = model.getRowCount();
		int cols = model.getColumnCount();
		if (rows == 0 || cols == 0)  {
			return;
		}
		FontMetrics fm = getFontMetrics(getFont());
		g2.setFont(getFont());
		g2.setColor(getForeground());
		int startRow = offset / rowHeight;
		int y2 = (int)Math.ceil(startRow + (double)getHeight() / rowHeight + 1);
		if (y2 > rows) {
			y2 = rows;
		}
		int startPixel = startRow * rowHeight - offset;
		int w = getWidth() / cols;
		int textY = (rowHeight - fm.getHeight()) / 2 + fm.getLeading() + fm.getAscent();
		if (columnWidths == null) {
			getPreferredWidth();
		}
		for (int j = startRow; j < y2; j++) {
			int colPixel = 0;
			for (int i = 0; i < cols; i++) {
				Object value = model.getValueAt(j, i);
				if (value instanceof Number) {
					String s = value.toString();
					g2.drawString(s, colPixel + leftMargin + w - fm.stringWidth(s), startPixel + textY);
				} else {
					g2.drawString(value.toString(), colPixel + leftMargin, startPixel + textY);
				}
				colPixel += columnWidths[i];
			}
			startPixel += rowHeight;
			if (drawHorizontalGrid) {
				g2.drawLine(0, startPixel - 1, getWidth(), startPixel - 1);
			}
		}
		if (drawVerticalGrid) {
			int xpix = 0;
			g2.drawLine(0, 0, 0, getHeight());
			for (int i = 0; i < cols; i++) {
				xpix += w;
				g2.drawLine(xpix - 1, 0, xpix - 1, getHeight());
			}
		}
	}
	/**
	 * Calculate the preferred total width .
	 * @return the preferred width
	 */
	public int getPreferredWidth() {
		int maxwidth = 0;
		if (model != null) {
			columnWidths = new int[model.getColumnCount()];
			FontMetrics fm = getFontMetrics(getFont());
			for (int col = 0; col < model.getColumnCount(); col++) {
				int colwidth = 0;
				for (int row = 0; row < model.getRowCount(); row++) {
					Object value = model.getValueAt(row, col);
					if (value != null) {
						int width = leftMargin + fm.stringWidth(value.toString()) + rightMargin;
						if (width > colwidth) {
							colwidth = width;
						}
					}
				}
				columnWidths[col] = colwidth;
				maxwidth += colwidth;
			}
		}
		return maxwidth;
	}
	/**
	 * {@inheritDoc}
	 */
	@Override
	public Dimension getPreferredSize() {
		return new Dimension(getPreferredWidth(), super.getPreferredSize().height);
	}
	/**
	 * Return value at point.
	 * @param x the x coordinate
	 * @param y the y coordinage
	 * @return the value or null if out of range
	 */
	public Object getValueAtPoint(int x, int y) {
		if (model != null) {
			int row = (y + offset) / rowHeight;
			int col = x * model.getColumnCount() / getWidth();
			if (row < model.getRowCount()
					&& col < model.getColumnCount()) {
				return model.getValueAt(row, col);
			}
		}
		return null;
	}
	/**
	 * Returns the optional tooltip for the given coordinates.
	 * @param x the x coordinate
	 * @param y the y coordinates
	 * @return the tooltip or null
	 */
	public String getTooltipAtPoint(int x, int y) {
		if (model != null) {
			int row = (y + offset) / rowHeight;
			int col = x * model.getColumnCount() / getWidth();
			if (row < model.getRowCount()
					&& col < model.getColumnCount()) {
				return model.getTooltipAt(row, col);
			}
		}
		return null;
	}
	/**
	 * Set the start pixel of row to render with offset.
	 * @param pixel the pixel.
	 */
	public void setStartPixel(int pixel) {
		this.offset = pixel;
	}
	/**
	 * @return the start offset
	 */
	public int getStartPixel() {
		return offset;
	}
}
