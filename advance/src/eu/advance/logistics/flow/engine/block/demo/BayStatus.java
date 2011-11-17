/*
 * Copyright 2010-2013 The Advance EU 7th Framework project consortium
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

package eu.advance.logistics.flow.engine.block.demo;

import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import javax.swing.GroupLayout;
import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;

import com.google.common.collect.Lists;

import eu.advance.logistics.annotations.Block;
import eu.advance.logistics.annotations.Input;
import eu.advance.logistics.flow.engine.api.core.AdvanceData;
import eu.advance.logistics.flow.engine.block.BlockVisualizer;
import eu.advance.logistics.flow.engine.model.rt.AdvanceBlock;
import eu.advance.logistics.flow.engine.model.rt.AdvanceBlockSettings;
import eu.advance.logistics.flow.engine.xml.typesystem.XElement;

/**
 * A block which displays a single frame with a single titled button.
 * @author akarnokd, 2011.10.27.
 */
@Block(category = "demo", description = "Displays a bargraph of values.")
public class BayStatus extends AdvanceBlock {
    /** The dialog title. */
    @Input("advance:string")
    protected static final String TITLE = "title";
    /** The capacity limit. */
    @Input("advance:integer")
    protected static final String CRITICAL_LOAD_LIMIT = "critical-load-limit";
	/** The current load. */
    @Input("advance:collection<advance:integer>")
    protected static final String LOAD = "load";
    /** The bar graph. */
    protected JPanel graph;
    /** The critical load limit. */
    protected final AtomicInteger criticalLoadLimit = new AtomicInteger(100);
    /** The critical load limit. */
    protected final AtomicReference<String> title = new AtomicReference<String>("Graph");
    /** Bars. */
    protected final List<Integer> values = Lists.newArrayList();
	/** The peer frame. */
	protected JInternalFrame frame;
	@Override
	public void init(AdvanceBlockSettings settings) {
		super.init(settings);
		if (settings.constantParams.containsKey(TITLE)) {
			title.set(AdvanceData.getString(settings.constantParams.get(TITLE).value));
		}
		if (settings.constantParams.containsKey(CRITICAL_LOAD_LIMIT)) {
			criticalLoadLimit.set(AdvanceData.getInt(settings.constantParams.get(CRITICAL_LOAD_LIMIT).value));
		}
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				createGUI();
			}
		});
	}
	/**
	 * Create the GUI.
	 */
	protected void createGUI() {
		frame = new JInternalFrame(title.get(), true);
		frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		
		Container c = frame.getContentPane();
		GroupLayout gl = new GroupLayout(c);
		c.setLayout(gl);
		gl.setAutoCreateGaps(true);
		gl.setAutoCreateContainerGaps(true);
		
		graph = new JPanel() {
			/** */
			private static final long serialVersionUID = -4265716673615049190L;
	    	@Override
	    	public Dimension getPreferredSize() {
	    		return new Dimension(300, values.size() * (getFontMetrics(getFont()).getHeight() + 6));
	    	}
	    	@Override
	    	public void paint(Graphics g) {
	    		Graphics2D g2 = (Graphics2D)g;
	    		int w = getWidth();
	    		g2.setColor(Color.WHITE);
	    		g2.fillRect(0, 0, w, getHeight());
	    		int limit = criticalLoadLimit.get();
	    		FontMetrics fm = g2.getFontMetrics();
				int h = fm.getHeight();
	    		int y = 0;
	    		int maxx = 0;
	    		int maxval = 0;
	    		for (int i = 0; i < values.size(); i++) {
	    			String n = "Bay " + (i);
	    			int sw = fm.stringWidth(n);
	    			maxx = Math.max(maxx, sw);

	    			int dy = fm.getAscent() + 3;
	    			g2.setColor(Color.BLACK);
	    			g2.drawString(n, 5, y + dy);
	    			
	    			y += h + 6;
	    			
	    			maxval = Math.max(maxval, values.get(i));
	    		}
	    		int maxy = y;
				g2.setColor(Color.BLACK);
				g2.drawLine(maxx + 5, 0, maxx + 5, maxy);
	    		if (maxval < limit * 2) {
	    			maxval = limit * 2;
	    		}
	    		y = 0;
	    		maxx += 10;
				int bw0 = limit * (w - maxx - 5) / maxval;
				g2.setColor(Color.RED);
				g2.drawLine(maxx + bw0, 0, maxx + bw0, maxy);
	    		for (int i = 0; i < values.size(); i++) {
	    			int val = values.get(i);
	    			
	    			if (val >= limit) {
	    				g2.setColor(new Color(0xFF, 0xCC, 0xCC, 0x80));
	    			} else {
	    				g2.setColor(new Color(0x80, 0xFF, 0x80, 0x80));
	    			}
	    			
	    			if (maxval > 0) {
	    				int bw = val * (w - maxx - 5) / maxval;
	    				g2.fillRect(maxx, y + 3, bw, h);
	    			}
	    			
	    			int dy = fm.getAscent() + 3;
	    			g2.setColor(Color.BLACK);
	    			g2.drawString(Integer.toString(val), maxx + 2, y + dy);
	    			
	    			y += h + 6;
	    		}
	    	}
	    };
		
		JScrollPane sp = new JScrollPane(graph);
		
		gl.setHorizontalGroup(
			gl.createSequentialGroup()
			.addComponent(sp, 100, 200, Short.MAX_VALUE)
		);
		gl.setVerticalGroup(
			gl.createSequentialGroup()
			.addComponent(sp, 100, 200, Short.MAX_VALUE)
		);

		frame.pack();
		frame.setVisible(true);
		
		BlockVisualizer.getInstance().add(frame);
	}
	@Override
	protected void invoke() {
		title.set(AdvanceData.getString(params.get(TITLE)));
		criticalLoadLimit.set(AdvanceData.getInt(params.get(CRITICAL_LOAD_LIMIT)));
		final List<XElement> list = AdvanceData.getList(params.get(LOAD));
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				frame.setTitle(title.get());
				values.clear();
				for (XElement e : list) {
					values.add(AdvanceData.getInt(e));
				}
				graph.revalidate();
				graph.repaint();
			}
		});
	}
	@Override
	public void done() {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				if (frame != null) {
					frame.dispose();
					BlockVisualizer.getInstance().remove(frame);
				}
			}
		});
		super.done();
	}
}
