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
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.GroupLayout.Alignment;

import eu.advance.logistics.flow.engine.model.fd.AdvanceBlockDescription;
import eu.advance.logistics.flow.engine.model.fd.AdvanceCompositeBlock;
import eu.advance.logistics.flow.engine.model.fd.AdvanceConstantBlock;
import eu.advance.logistics.flow.engine.model.rt.AdvanceBlock;
import eu.advance.logistics.flow.engine.model.rt.AdvanceSchedulerPreference;
import eu.advance.logistics.flow.engine.xml.typesystem.XElement;

/**
 * Displays an input box which sends out strings as the user presses ENTER or the SEND button.
 * @author akarnokd, 2011.10.27.
 */
public class InputBox extends AdvanceBlock {
	/** The peer frame. */
	protected JFrame frame;
	/** The text field. */
	protected JTextField text;
	/** The peer button. */
	protected JButton button;
	/** The message of the input. */
	protected JLabel message;
	/** The deferred button title. */
	protected String titleDefer = "Message:";
	/**
	 * Constructor.
	 * @param id the block global id
	 * @param parent the parent composite block
	 * @param schedulerPreference the scheduler preference
	 */
	public InputBox(String id, AdvanceCompositeBlock parent, 
			AdvanceSchedulerPreference schedulerPreference) {
		super(id, parent, schedulerPreference);
	}
	@Override
	public void init(AdvanceBlockDescription desc,
			Map<String, AdvanceConstantBlock> constantParams) {
		super.init(desc, constantParams);
		if (constantParams.containsKey("title")) {
			titleDefer = constantParams.get("title").value.content;
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
		frame = new JFrame("Input box");
		frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		button = new JButton("Send");
		message = new JLabel(titleDefer);
		text = new JTextField(20);
		
		
		Container c = frame.getContentPane();
		GroupLayout gl = new GroupLayout(c);
		c.setLayout(gl);
		gl.setAutoCreateGaps(true);
		gl.setAutoCreateContainerGaps(true);
		
		gl.setHorizontalGroup(
			gl.createParallelGroup(Alignment.CENTER)
			.addGroup(
				gl.createParallelGroup()
				.addComponent(message)
				.addComponent(text)
			)
			.addComponent(button)
		);
		gl.setVerticalGroup(
			gl.createSequentialGroup()
			.addComponent(message)
			.addComponent(text)
			.addComponent(button)
		);
		frame.pack();
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);
		
		ActionListener sendAction = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				doSend();
			}

		};
		button.addActionListener(sendAction);
		text.addActionListener(sendAction);
	}
	/**
	 * Send the contents of the text field.
	 */
	public void doSend() {
		final String txt = text.getText();
		scheduler().schedule(new Runnable() {
			@Override
			public void run() {
				XElement e = new XElement("string");
				e.content = txt;
				dispatchOutput(Collections.singletonMap("out", e));
			}
		});
	}
	@Override
	protected void invoke(Map<String, XElement> params) {
		final String title = params.get("title").content;
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				message.setText(title);
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
