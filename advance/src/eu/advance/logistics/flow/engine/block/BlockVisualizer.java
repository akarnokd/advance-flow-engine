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

package eu.advance.logistics.flow.engine.block;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.util.Arrays;
import java.util.Deque;
import java.util.Hashtable;
import java.util.List;

import javax.swing.JDesktopPane;
import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JScrollPane;

import com.google.common.collect.Lists;

/**
 * The parent frame for visualizing block inputs and outputs.
 * @author akarnokd, 2011.11.07.
 */
public class BlockVisualizer extends JFrame {
	/** */
	private static final long serialVersionUID = 2683140861365995193L;
	/** The singleton instance. */
	private static BlockVisualizer instance;
	/** The desktop for parenting the internal frames. */
	protected JDesktopPane desktop;
	/**
	 * Constructor. Initializes the frame.
	 */
	public BlockVisualizer() {
		super("Advance Block Visualizations");
		desktop = new ScrollableDesktop();
		getContentPane().add(new JScrollPane(desktop));
		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		
		JMenuBar mainmenu = new JMenuBar();
		JMenu mnuView = new JMenu("View");
		JMenuItem autoLayout = new JMenuItem("Automatic layout");
		autoLayout.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				layoutFrames();
			}
		});
		
		mnuView.add(autoLayout);
		mainmenu.add(mnuView);
		setJMenuBar(mainmenu);
	}
	/**
	 * Add the frame to this visualizer.
	 * @param frame the frame to add
	 */
	public void add(JInternalFrame frame) {
		if (desktop.getComponentCount() == 0) {
			setVisible(true);
		}
		desktop.add(frame);
		layoutFrames();
	}
	/**
	 * In a non-overlapping fashion.
	 */
	public void layoutFrames() {
		Deque<JInternalFrame> frames = Lists.newLinkedList(Arrays.asList(desktop.getAllFrames()));
		
		List<JInternalFrame> placed = Lists.newLinkedList();
		List<Rectangle> placedRects = Lists.newLinkedList();
		int w = desktop.getWidth();
		int h = desktop.getHeight();
		while (!frames.isEmpty()) {
			JInternalFrame f = frames.removeFirst();
		
			Rectangle r = new Rectangle((w - f.getWidth()) / 2, (h - f.getHeight()) / 2, f.getWidth(), f.getHeight());
			
			if (placedRects.isEmpty()) {
				placed.add(f);
				placedRects.add(r);
				continue;
			}
			outer:
			for (Rectangle p : placedRects) {
				if (r.intersects(p)) {
					int rx = r.x;
					int ry = r.y;
					double radius = 0;
					while (!Thread.currentThread().isInterrupted()) {
						for (double d = 0; d < Math.PI * 2; d += Math.PI / 36) {
							r.x = (int)(rx + radius * Math.cos(d));
							r.y = (int)(ry + radius * Math.sin(d));
							boolean intr = false;
							for (Rectangle p2 : placedRects) {
								if (r.intersects(p2)) {
									intr = true;
									break;
								}
							}
							if (!intr) {
								placed.add(f);
								placedRects.add(r);
								break outer;
							}
						}
						radius += 8;
					}
				} else {
					placed.add(f);
					placedRects.add(r);
				}
			}
		}
		int xmin = Integer.MAX_VALUE;
		int ymin = Integer.MAX_VALUE;
		for (int i = 0; i < placedRects.size(); i++) {
			Rectangle r = placedRects.get(i);
			xmin = Math.min(xmin, r.x);
			ymin = Math.min(ymin, r.y);
		}
		for (int i = 0; i < placedRects.size(); i++) {
			Rectangle r = placedRects.get(i);
			placed.get(i).setLocation(r.x - xmin, r.y - ymin);
		}
	}
	/**
	 * Remove the frame from this visualizer.
	 * @param frame the frame to remove
	 */
	public void remove(JInternalFrame frame) {
		desktop.remove(frame);
		if (desktop.getComponentCount() == 0) {
			dispose();
		}
	}
	/**
	 * Create and display the single instance of the visualizer.
	 * @return the block visualizer frame
	 */
	public static BlockVisualizer getInstance() {
		if (instance == null) {
			instance = new BlockVisualizer();
			instance.setSize(800, 600);
			instance.setLocationRelativeTo(null);
			instance.setVisible(true);
		}
		return instance;
	}
	/**
	 * A DesktopPane with support for scrolling its contents.
	 * @author akarnokd, 2011.11.07.
	 */
	public static class ScrollableDesktop extends JDesktopPane {
		/** */
		private static final long serialVersionUID = 2927356450989873143L;
		/** The listeners.*/
		protected Hashtable<Component, ComponentListener> listeners = new Hashtable<Component, ComponentListener>();

		@Override
		public Dimension getPreferredSize() {
	      JInternalFrame [] array = getAllFrames();
	      int maxX = 0;
	      int maxY = 0;
	      for (int i = 0; i < array.length; i++) {
	        int x = array[i].getX() + array[i].getWidth();
	        if (x < maxX) {
	        	maxX = x;
	        }
	        int y = array[i].getY() + array[i].getHeight();
	        if (y < maxY) {
	        	maxY = y;
	        }
	      }
	      return new Dimension(maxX, maxY);
	    }

		@Override
		public void add(Component comp, Object constraints) {
			super.add(comp, constraints);
			ComponentListener listener = new ComponentListener() {
				@Override
				public void componentResized(ComponentEvent e) { // Layout the
																	// JScrollPane
					getParent().getParent().validate();
				}
				@Override
				public void componentMoved(ComponentEvent e) {
					componentResized(e);
				}

				@Override
				public void componentShown(ComponentEvent e) {
				}

				@Override
				public void componentHidden(ComponentEvent e) {
				}
			};
			comp.addComponentListener(listener);
			listeners.put(comp, listener);
		}
		@Override
		public void remove(Component comp) {
			comp.removeComponentListener(listeners.get(comp));
			super.remove(comp);
			getParent().getParent().validate(); // Layout the JScrollPane
		}
	}
}
