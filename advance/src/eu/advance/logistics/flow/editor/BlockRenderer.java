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

package eu.advance.logistics.flow.editor;

import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.CubicCurve2D;
import java.awt.image.BufferedImage;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JComponent;
import javax.swing.SwingUtilities;

import com.google.common.base.Supplier;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;

/**
 * The panel which renders blocks and wires.
 * @author karnokd, 2011.07.04.
 */
public class BlockRenderer extends JComponent {
	/** */
	private static final long serialVersionUID = 4753911877178282938L;
	/** The current rendering origin. */
	protected int offsetX;
	/** The current rendering origin. */
	protected int offsetY;
	/** The current zoom level. */
	protected double zoom = 1.0;
	/** The list of blocks. */
	public final List<Block> blocks = Lists.newArrayList();
	/** The list of wires. */
	public final List<Wire> wires = Lists.newArrayList();
	/** Is the dragging on? */
	boolean drag;
	/** Is the panning on? */
	boolean pan;
	/** The current block dragged. */
	Block selectedBlock;
	/** Currently selected block port or -1 if none. */
	int selectedBlockPort = -1;
	/** The exact block location for dragging. */
	double selectedBlockX;
	/** The exact block location for dragging. */
	double selectedBlockY;
	/** Initialize event handlers. */
	public BlockRenderer() {
		MouseAdapter ma = new MouseAdapter() {
			/** The last mouse event coordinate. */
			int lastx;
			/** The last mouse event coordinate. */
			int lasty;
			@Override
			public void mousePressed(MouseEvent e) {
				if (SwingUtilities.isLeftMouseButton(e)) {
					List<Block> bs = blockAt(e.getX(), e.getY());
					if (bs.size() > 0) {
						Block b0 = selectedBlock;
						selectedBlock = bs.get(0);
						selectedBlockX = selectedBlock.x;
						selectedBlockY = selectedBlock.y;
						if (b0 != selectedBlock) {
							selectedBlockPort = getBlockPortIndex(selectedBlock, e.getX(), e.getY());
						} else {
							int idx = getBlockPortIndex(selectedBlock, e.getX(), e.getY());
							if (idx >= 0) {
								selectedBlockPort = idx;
							}
						}
						drag = true;
					} else {
						selectedBlock = null;
						selectedBlockPort = -1;
					}
					repaint();
				} else
				if (SwingUtilities.isRightMouseButton(e)) {
					pan = true;
				} else
				if (SwingUtilities.isMiddleMouseButton(e)) {
					if (e.isControlDown()) {
						autoHorizontalLayout();
					} else {
						int minx = 0;
						int miny = 0;
						for (Block b : blocks) {
							minx = Math.min(minx, b.x);
							miny = Math.min(miny, b.y);
						}
						offsetX = minx;
						offsetY = miny;
						zoom = 1;
					}
					repaint();
				}
				lastx = e.getX();
				lasty = e.getY();
			}
			@Override
			public void mouseWheelMoved(MouseWheelEvent e) {
				if (e.isControlDown()) {
					double z0 = zoom;
					if (e.getUnitsToScroll() < 0) {
						zoom = Math.max(0.1, Math.min(10, zoom + 0.1));
					} else {
						zoom = Math.max(0.1, Math.min(10, zoom - 0.1));
					}
					double vx = (e.getX() - offsetX) / z0;
					double vy = (e.getY() - offsetY) / z0;
					
					offsetX = (int)Math.round(e.getX() - vx * zoom);
					offsetY = (int)Math.round(e.getY() - vy * zoom);
					
				} else
				if (e.isShiftDown()) {
					if (e.getUnitsToScroll() < 0) {
						offsetX += 30;
					} else {
						offsetX -= 30;
					}
				} else {
					if (e.getUnitsToScroll() < 0) {
						offsetY += 30;
					} else {
						offsetY -= 30;
					}
				}
				repaint();
			}
			@Override
			public void mouseReleased(MouseEvent e) {
				if (SwingUtilities.isLeftMouseButton(e)) {
					drag = false;
				} else
				if (SwingUtilities.isRightMouseButton(e)) {
					pan = false;
				}
			}
			@Override
			public void mouseDragged(MouseEvent e) {
				if (drag) {
					selectedBlockX -= (lastx - e.getX()) / zoom;
					selectedBlockY -= (lasty - e.getY()) / zoom;
					
					selectedBlock.x = (int)Math.round(selectedBlockX);
					selectedBlock.y = (int)Math.round(selectedBlockY);
					repaint();
				}
				if (pan) {
					offsetX -= lastx - e.getX();
					offsetY -= lasty - e.getY();
					repaint();
				}
				lastx = e.getX();
				lasty = e.getY();
			}
		};
		addMouseListener(ma);
		addMouseMotionListener(ma);
		addMouseWheelListener(ma);
	}
	/**
	 * Get the port index for the given block and mouse coordinates.
	 * @param b the block
	 * @param x the mouse X
	 * @param y the mouse Y
	 * @return the port index or -1 if none
	 */
	protected int getBlockPortIndex(Block b, int x, int y) {
		Graphics2D g2 = (Graphics2D)getGraphics();
		int h = g2.getFontMetrics().getHeight();
		for (int i = 0; i < b.inputs.size(); i++) {
			int py = b.getInputY(g2, i);
			if (py - h / 2 <= y - offsetY - b.y && y - offsetY - b.y <= py + h / 2) {
				return i;
			}
		}
		for (int i = 0; i < b.outputs.size(); i++) {
			int py = b.getOutputY(g2, i);
			if (py - h / 2 <= y - offsetY - b.y && y - offsetY - b.y <= py + h / 2) {
				return i + b.inputs.size();
			}
		}
		return -1;
	}
	@Override
	public void paint(Graphics g) {
		Graphics2D g2 = (Graphics2D)g;
		
		g2.setColor(Color.WHITE);
		g2.fillRect(0, 0, getWidth(), getHeight());
		
		AffineTransform save0 = g2.getTransform();
		
		g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		
		// ---------------------------------------------------------------------------------

		g2.translate(offsetX, offsetY);
		g2.scale(zoom, zoom);
		
		for (Wire w : wires) {
			g2.setColor(Color.BLACK);
			
			int sx = w.source.x + w.source.getWidth(g2);
			int sy = w.source.y + w.source.getOutputY(g2, w.sourcePort);
			
			int dx = w.destination.x;
			int dy = w.destination.y + w.destination.getInputY(g2, w.destinationPort);
			
			CubicCurve2D.Double cc2 = new CubicCurve2D.Double(sx, sy, sx + 20, sy, dx - 20, dy, dx, dy);
			g2.draw(cc2);
			g2.fillOval(dx - 3, dy - 3, 6, 6);
		}
		for (Block b : blocks) {
			int bwidth = b.getWidth(g2);
			int bheight = b.getHeight(g2);
			
			GradientPaint gp = new GradientPaint(b.x, b.y, Color.WHITE, 
					b.x, b.y + bheight, 
					b == selectedBlock ? new Color(0xFFCC40) : new Color(0x00FF61)
			
			);
			Paint save1 = g2.getPaint();
			g2.setPaint(gp);
			
			g2.fillRoundRect(b.x, b.y, bwidth, bheight, 5, 3);
			g2.setPaint(save1);
			g2.setColor(Color.BLACK);
			g2.drawRoundRect(b.x, b.y, bwidth, bheight, 5, 3);
			
			int fascent = g2.getFontMetrics().getAscent();
			
			g2.drawString(b.name, b.x + 5, b.y + 3 + fascent);
			int fheight = g2.getFontMetrics().getHeight();
			int namey = b.y + fheight + 5;
			int port = 0;
			if (b.inputs.size() > 0) {
				g2.drawLine(b.x, namey, b.x + bwidth, namey);
				namey += 2;
				for (String s : b.inputs) {
					if (port == selectedBlockPort && b == selectedBlock) {
						g2.setColor(new Color(0xF0F0F0));
						g2.fillRect(b.x + 2, namey, bwidth - 3, fheight);
						g2.setColor(Color.BLACK);
					}
					g2.drawString(s, b.x + 5, namey + fascent);
					namey += fheight;
					port++;
				}
			}
			if (b.outputs.size() > 0) {
				if (b.inputs.size() > 0) {
					namey += 3;
				}
				g2.drawLine(b.x, namey, b.x + bwidth, namey);
				namey += 2;
				for (String s : b.outputs) {
					if (port == selectedBlockPort && b == selectedBlock) {
						g2.setColor(new Color(0xF0F0F0));
						g2.fillRect(b.x + 2, namey, bwidth - 3, fheight);
						g2.setColor(Color.BLACK);
					}
					g2.drawString(s, b.x + bwidth - 5 - g2.getFontMetrics().stringWidth(s), namey + fascent);
					namey += fheight;
					port++;
				}
			}
		}

		// ---------------------------------------------------------------------------------
		
		g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_DEFAULT);
		
		g2.setTransform(save0);
	}
	/**
	 * Get the blocks at a given mouse coordinate.
	 * @param x the X coordinate
	 * @param y the Y coordinate
	 * @return the list of blocks at that point
	 */
	public List<Block> blockAt(int x, int y) {
		List<Block> result = Lists.newArrayList();

		Graphics2D g2 = (Graphics2D)getGraphics();
		
		double mx = (x - offsetX) / zoom;
		double my = (y - offsetY) / zoom;
		
		for (Block b : blocks) {
			if (b.x <= mx && b.y <= my 
					&& b.x + b.getWidth(g2) > mx && b.y + b.getHeight(g2) > my) {
				result.add(b);
			}
		}
		
		return result;
	}
	/**
	 * Automatically layout the blocks in a horizontal column- manner.
	 */
	public void autoHorizontalLayout() {
		Multimap<Integer, Block> columns = Multimaps.newListMultimap(new HashMap<Integer, Collection<Block>>(), new Supplier<List<Block>>() {
			@Override
			public List<Block> get() {
				return Lists.newArrayList();
			}
		});
		for (Block b : blocks) {
			columns.put(blockDepth(b), b);
		}
		Graphics2D g2 = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB).createGraphics();
		int x = 20;
		int maxh = 0;
		// place blocks into columns, compute heights
		Map<Integer, Integer> colHeights = Maps.newHashMap();
		for (Integer k : columns.keySet()) {
			int maxw = 0;
			int totalh = 0;
			for (Block b : columns.get(k)) {
				maxw = Math.max(maxw, b.getWidth(g2));
				b.x = x;
				if (totalh != 0) {
					totalh += 30;
				}
				totalh += b.getHeight(g2);
			}
			x += maxw + 40;
			maxh = Math.max(maxh, totalh);
			colHeights.put(k, totalh);
		}
		for (Integer k : columns.keySet()) {
			int y = (maxh - colHeights.get(k)) / 2 + 20;
			for (Block b : columns.get(k)) {
				b.y = y;
				y += b.getHeight(g2) + 30;
			}			
		}
		g2.dispose();
	}
	/**
	 * Calculate the block depth by counting the hops of the wire-chain.
	 * Note that loops are not handled.
	 * @param b the block to scan
	 * @return the depth
	 */
	public int blockDepth(Block b) {
		int dmax = 0;
		for (int i = 0; i < b.inputs.size(); i++) {
			for (Wire w : wires) {
				if (w.destination == b && w.destinationPort == i) {
					dmax = Math.max(dmax, blockDepth(w.source) + 1);
				}
			}
		}
		return dmax;
	}
}
