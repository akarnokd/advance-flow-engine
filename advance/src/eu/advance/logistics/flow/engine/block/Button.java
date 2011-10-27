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

import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collections;
import java.util.Map;

import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import eu.advance.logistics.flow.engine.model.fd.AdvanceBlockDescription;
import eu.advance.logistics.flow.engine.model.fd.AdvanceCompositeBlock;
import eu.advance.logistics.flow.engine.model.fd.AdvanceConstantBlock;
import eu.advance.logistics.flow.engine.model.rt.AdvanceBlock;
import eu.advance.logistics.flow.engine.model.rt.AdvanceSchedulerPreference;
import eu.advance.logistics.flow.engine.xml.typesystem.XElement;

/**
 * A block which displays a single frame with a single titled button.
 * @author akarnokd, 2011.10.27.
 */
public class Button extends AdvanceBlock {
	/** The peer frame. */
	protected JFrame frame;
	/** The peer button. */
	protected JButton button;
	/**
	 * Constructor.
	 * @param id the block global id
	 * @param parent the parent composite block
	 * @param schedulerPreference the scheduler preference
	 */
	public Button(String id, AdvanceCompositeBlock parent, 
			AdvanceSchedulerPreference schedulerPreference) {
		super(id, parent, schedulerPreference);
	}
	@Override
	public void init(AdvanceBlockDescription desc,
			Map<String, AdvanceConstantBlock> constantParams) {
		super.init(desc, constantParams);
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
		frame = new JFrame("Button");
		frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		button = new JButton("Click me");
		
		Container c = frame.getContentPane();
		GroupLayout gl = new GroupLayout(c);
		c.setLayout(gl);
		gl.setAutoCreateGaps(true);
		gl.setAutoCreateContainerGaps(true);
		
		gl.setHorizontalGroup(
			gl.createSequentialGroup()
			.addComponent(button)
		);
		gl.setVerticalGroup(
			gl.createSequentialGroup()
			.addComponent(button)
		);
		frame.pack();
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);
		
		button.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				scheduler().schedule(new Runnable() {
					@Override
					public void run() {
						dispatchOutput(Collections.singletonMap("out", new XElement("object")));
					}
				});
			}
		});
	}
	@Override
	protected void invoke(Map<String, XElement> params) {
		final String title = params.get("title").content;
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				button.setText(title);
				frame.pack();
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
				}
			}
		});
		super.done();
	}
}
