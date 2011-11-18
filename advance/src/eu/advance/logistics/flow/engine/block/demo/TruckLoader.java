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

import java.awt.Container;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;

import javax.swing.GroupLayout;
import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;

import com.google.common.collect.Lists;

import eu.advance.logistics.annotations.Block;
import eu.advance.logistics.annotations.Input;
import eu.advance.logistics.annotations.Output;
import eu.advance.logistics.flow.engine.block.AdvanceBlock;
import eu.advance.logistics.flow.engine.block.AdvanceRuntimeContext;
import eu.advance.logistics.flow.engine.block.BlockVisualizer;
import eu.advance.logistics.flow.engine.runtime.BlockSettings;
import eu.advance.logistics.flow.engine.xml.XElement;

/**
 *.
 * @author szmarcell
 */
@Block(description = "Collects pallets and sends out a truck once it is filled.", category = "demo")
public class TruckLoader extends AdvanceBlock {
	/** The logger. */
    protected static final Logger LOGGER = Logger.getLogger(TruckLoader.class.getName());
    /** Incoming pallets. */
    @Input("advance:pallet")
    protected static final String PALLET = "pallet";
    /** The capacity limit. */
    @Input("advance:integer")
    protected static final String CAPACITY = "capacity";
    /** Out. */
    @Output("advance:truck")
    protected static final String OUT = "truck";
    /** The list of aggregated pallets so far. */
    protected final List<XElement> pallets = Lists.newArrayList();
	/** The peer frame. */
	protected JInternalFrame frame;
	/** The current load level. */
	protected JLabel currentLoad;
	/** The capacity level. */
	protected final AtomicInteger capacity = new AtomicInteger(5);
	@Override
	public void init(BlockSettings<XElement, AdvanceRuntimeContext> settings) {
		super.init(settings);
		if (settings.constantValues.containsKey(CAPACITY)) {
			capacity.set(resolver().getInt(settings.constantValues.get(CAPACITY)));
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
		frame = new JInternalFrame("Truck loaded", false);
		frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		
		Container c = frame.getContentPane();
		GroupLayout gl = new GroupLayout(c);
		c.setLayout(gl);
		gl.setAutoCreateGaps(true);
		gl.setAutoCreateContainerGaps(true);
		
		currentLoad = new JLabel("0 / 0");
		
		gl.setHorizontalGroup(
			gl.createSequentialGroup()
			.addComponent(currentLoad, 0, 75, GroupLayout.PREFERRED_SIZE)
		);
		gl.setVerticalGroup(
			gl.createSequentialGroup()
			.addComponent(currentLoad)
		);

		frame.pack();
		frame.setVisible(true);
		
		BlockVisualizer.getInstance().add(frame);
	}
    @Override
    protected void invoke() {
    	XElement pallet = get(PALLET);
    	capacity.set(getInt(CAPACITY));
    	pallets.add(pallet);
    	if (pallets.size() >= capacity.get()) {
    		XElement truck = DemoTypes.createTruck(pallets);
    		dispatch(OUT, truck);
    		pallets.clear();
    	}
    	final int size = pallets.size();
    	final int cap = capacity.get();
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				currentLoad.setText(size + " / " + cap);
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
