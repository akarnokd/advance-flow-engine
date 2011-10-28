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
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;
import javax.swing.GroupLayout.Alignment;

import eu.advance.logistics.flow.engine.model.fd.AdvanceBlockDescription;
import eu.advance.logistics.flow.engine.model.fd.AdvanceCompositeBlock;
import eu.advance.logistics.flow.engine.model.fd.AdvanceConstantBlock;
import eu.advance.logistics.flow.engine.model.rt.AdvanceBlock;
import eu.advance.logistics.flow.engine.model.rt.AdvanceSchedulerPreference;
import eu.advance.logistics.flow.engine.xml.typesystem.XElement;

/**
 * A block which sends one single message it has accumulated..
 * @author akarnokd, 2011.10.27.
 */
public class Gate extends AdvanceBlock {
	/** The peer frame. */
	protected JFrame frame;
	/** The peer button. */
	protected JButton button;
	/** The queue length. */
	protected JLabel queueLength;
	/** The deferred button title. */
	protected String buttonTitleDefer = "Click me";
	/** The queued messages. */
	protected final Queue<XElement> messages = new LinkedBlockingQueue<XElement>();
	/**
	 * Constructor.
	 * @param id the block global id
	 * @param parent the parent composite block
	 * @param schedulerPreference the scheduler preference
	 */
	public Gate(String id, AdvanceCompositeBlock parent, 
			AdvanceSchedulerPreference schedulerPreference) {
		super(id, parent, schedulerPreference);
	}
	@Override
	public void init(AdvanceBlockDescription desc,
			Map<String, AdvanceConstantBlock> constantParams) {
		super.init(desc, constantParams);
		if (constantParams.containsKey("title")) {
			buttonTitleDefer = constantParams.get("title").value.content;
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
		frame = new JFrame("Gate");
		frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		button = new JButton(buttonTitleDefer);
		queueLength = new JLabel("Queue length: 0");
		
		Container c = frame.getContentPane();
		GroupLayout gl = new GroupLayout(c);
		c.setLayout(gl);
		gl.setAutoCreateGaps(true);
		gl.setAutoCreateContainerGaps(true);
		
		gl.setHorizontalGroup(
			gl.createParallelGroup(Alignment.CENTER)
			.addComponent(queueLength)
			.addComponent(button)
		);
		gl.setVerticalGroup(
			gl.createSequentialGroup()
			.addComponent(queueLength)
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
						XElement e = messages.poll();
						final int size = messages.size();
						SwingUtilities.invokeLater(new Runnable() {
							@Override
							public void run() {
								queueLength.setText(String.format("Queue length: %d", size));
							}
						});
						if (e != null) {
							dispatchOutput(Collections.singletonMap("out", e));
						}
					}
				});
			}
		});
	}
	@Override
	protected void invoke(Map<String, XElement> params) {
		final String title = params.get("title").content;
		messages.add(params.get("in"));
		final int size = messages.size();
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				queueLength.setText(String.format("Queue length: %d", size));
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
