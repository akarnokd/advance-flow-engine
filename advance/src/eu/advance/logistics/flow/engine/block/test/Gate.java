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

package eu.advance.logistics.flow.engine.block.test;

import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collections;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;

import eu.advance.logistics.annotations.Block;
import eu.advance.logistics.annotations.Input;
import eu.advance.logistics.annotations.Output;
import eu.advance.logistics.flow.engine.block.AdvanceBlock;
import eu.advance.logistics.flow.engine.block.AdvanceRuntimeContext;
import eu.advance.logistics.flow.engine.block.BlockVisualizer;
import eu.advance.logistics.flow.engine.runtime.BlockSettings;
import eu.advance.logistics.flow.engine.xml.XElement;

/**
 * A block which sends one single message it has accumulated..
 * @author akarnokd, 2011.10.27.
 */
@Block(parameters = { "T" }, category = "user-interface", description = "Queues the incoming messages and relays them one-by-one if the user clicks on the button.")
public class Gate extends AdvanceBlock {
	/** In. */
    @Input("?T")
    private static final String IN = "in";
    /** Title. */
    @Input("advance:string")
    private static final String TITLE = "title";
    /** Out. */
    @Output("?T")
    private static final String OUT = "out";
    
	/** The peer frame. */
	protected JInternalFrame frame;
	/** The peer button. */
	protected JButton button;
	/** The queue length. */
	protected JLabel queueLength;
	/** The deferred button title. */
	protected String buttonTitleDefer = "Click me";
	/** The queued messages. */
	protected final Queue<XElement> messages = new LinkedBlockingQueue<XElement>();
	@Override
	public void init(BlockSettings<XElement, AdvanceRuntimeContext> settings) {
		super.init(settings);
		if (settings.constantValues.containsKey(TITLE)) {
			buttonTitleDefer = resolver().getString(settings.constantValues.get(TITLE));
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
		frame = new JInternalFrame(settings.id, false);
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
							dispatchOutput(Collections.singletonMap(OUT, e));
						}
					}
				});
			}
		});
		BlockVisualizer.getInstance().add(frame);
	}
	@Override
	protected void invoke() {
		final String title = params.get(TITLE).content;
		messages.add(params.get(IN));
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
					BlockVisualizer.getInstance().remove(frame);
				}
			}
		});
		super.done();
	}
}
