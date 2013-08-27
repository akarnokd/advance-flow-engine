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

import hu.akarnokd.utils.xml.XNElement;

import java.awt.Color;
import java.awt.Container;

import javax.swing.GroupLayout;
import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import eu.advance.logistics.annotations.Block;
import eu.advance.logistics.annotations.Input;
import eu.advance.logistics.flow.engine.AdvanceBlock;
import eu.advance.logistics.flow.engine.AdvanceRuntimeContext;
import eu.advance.logistics.flow.engine.block.BlockVisualizer;
import eu.advance.logistics.flow.engine.runtime.BlockSettings;

/**
 * A block which displays a single frame with a single titled button.
 * @author akarnokd, 2011.10.27.
 */
@Block(category = "demo", description = "Displays a red rectangle if the trigger arrives.")
public class Alert extends AdvanceBlock {
	/** The title attribute. */
    @Input("advance:boolean")
    protected static final String TRIGGER = "alert";
    
	/** The peer frame. */
	protected JInternalFrame frame;
	/** The status indicator panel. */
	protected JPanel panel;
	@Override
	public void init(BlockSettings<XNElement, AdvanceRuntimeContext> settings) {
		super.init(settings);
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
		frame = new JInternalFrame(id(), false);
		frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		
		Container c = frame.getContentPane();
		GroupLayout gl = new GroupLayout(c);
		c.setLayout(gl);
		gl.setAutoCreateGaps(true);
		gl.setAutoCreateContainerGaps(true);
		
		panel = new JPanel();
		panel.setBackground(Color.GREEN);
		
		gl.setHorizontalGroup(
			gl.createSequentialGroup()
			.addComponent(panel, 200, 200, 200)
		);
		gl.setVerticalGroup(
			gl.createSequentialGroup()
			.addComponent(panel, 50, 50, 50)
		);

		frame.pack();
		frame.setVisible(true);
		
		BlockVisualizer.getInstance().add(frame);
	}
	@Override
	protected void invoke() {
		final boolean b = getBoolean(TRIGGER);
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				if (b) {
					panel.setBackground(Color.RED);
				} else {
					panel.setBackground(Color.GREEN);
				}
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
