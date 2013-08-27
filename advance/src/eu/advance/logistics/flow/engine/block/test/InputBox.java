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

import hu.akarnokd.utils.xml.XNElement;

import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collections;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import eu.advance.logistics.annotations.Block;
import eu.advance.logistics.annotations.Input;
import eu.advance.logistics.annotations.Output;
import eu.advance.logistics.flow.engine.AdvanceBlock;
import eu.advance.logistics.flow.engine.AdvanceRuntimeContext;
import eu.advance.logistics.flow.engine.block.BlockVisualizer;
import eu.advance.logistics.flow.engine.runtime.BlockSettings;

/**
 * Displays an input box which sends out strings as the user presses ENTER or the SEND button.
 * @author akarnokd, 2011.10.27.
 */
@Block(category = "user-interface", description = "Displays a input box where the user can send strings.")
public class InputBox extends AdvanceBlock {
	/** Title. */
    @Input("advance:string")
    private static final String TITLE = "title";
    /** Out. */
    @Output("advance:string")
    private static final String OUT = "out";

	/** The peer frame. */
	protected JInternalFrame frame;
	/** The text field. */
	protected JTextField text;
	/** The peer button. */
	protected JButton button;
	/** The message of the input. */
	protected JLabel message;
	/** The deferred button title. */
	protected String titleDefer = "Message:";
	@Override
	public void init(BlockSettings<XNElement, AdvanceRuntimeContext> settings) {
		super.init(settings);
		if (settings.constantValues.containsKey(TITLE)) {
			titleDefer = resolver().getString(settings.constantValues.get(TITLE));
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
		frame.setVisible(true);
		
		ActionListener sendAction = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				doSend();
			}

		};
		button.addActionListener(sendAction);
		text.addActionListener(sendAction);
		BlockVisualizer.getInstance().add(frame);
	}
	/**
	 * Send the contents of the text field.
	 */
	public void doSend() {
		final String txt = text.getText();
		scheduler().schedule(new Runnable() {
			@Override
			public void run() {
				XNElement e = new XNElement("string");
				e.content = txt;
				dispatchOutput(Collections.singletonMap(OUT, e));
			}
		});
	}
	@Override
	protected void invoke() {
		final String title = params.get(TITLE).content;
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
					BlockVisualizer.getInstance().remove(frame);
				}
			}
		});
		super.done();
	}
}
