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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Map;

import javax.swing.GroupLayout;
import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

import eu.advance.logistics.annotations.Block;
import eu.advance.logistics.annotations.Input;
import eu.advance.logistics.flow.engine.block.BlockVisualizer;
import eu.advance.logistics.flow.engine.model.fd.AdvanceConstantBlock;
import eu.advance.logistics.flow.engine.model.rt.AdvanceBlock;
import eu.advance.logistics.flow.engine.model.rt.AdvanceBlockSettings;
import eu.advance.logistics.flow.engine.xml.typesystem.XElement;

/**
 * A block which displays a single frame with a single titled button.
 * @author akarnokd, 2011.10.27.
 */
@Block(category = "demo", description = "Displays a red rectangle if the trigger arrives.")
public class Alert extends AdvanceBlock {
	/** The title attribute. */
    @Input("advance:object")
    protected static final String TRIGGER = "trigger";
    
	/** The peer frame. */
	protected JInternalFrame frame;
	/** The status indicator panel. */
	protected JPanel panel;
	/** The highlight timer. */
	protected Timer highlightTimer;
	/**
	 * Constructor.
	 * @param settings the block settings
	 */
	public Alert(AdvanceBlockSettings settings) {
		super(settings);
	}
	@Override
	public void init(Map<String, AdvanceConstantBlock> constantParams) {
		super.init(constantParams);
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
		frame = new JInternalFrame("Alert", false);
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
			.addComponent(panel, 300, 300, 300)
		);
		gl.setVerticalGroup(
			gl.createSequentialGroup()
			.addComponent(panel, 100, 100, 100)
		);

		frame.pack();
		frame.setVisible(true);
		
		highlightTimer = new Timer(3000, new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				panel.setBackground(Color.GREEN);
				highlightTimer.stop();
			}
		});
		
		BlockVisualizer.getInstance().add(frame);
	}
	@Override
	protected void invoke(Map<String, XElement> params) {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				panel.setBackground(Color.RED);
				highlightTimer.stop();
				highlightTimer.start();
			}
		});
	}
	@Override
	public void done() {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				if (frame != null) {
					highlightTimer.stop();
					frame.dispose();
					BlockVisualizer.getInstance().remove(frame);
				}
			}
		});
		super.done();
	}
}
