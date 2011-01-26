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

package eu.advance.logistics.flow.editor;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The advance flow editor application.
 * @author karnokd
 */
public class AdvanceFlowEditor extends JFrame implements MainWindowCallback {
	/** */
	private static final long serialVersionUID = -2751169825854810625L;
	/** The logger. */
	protected static final Logger LOG = LoggerFactory.getLogger(AdvanceFlowEditor.class);
	/** The flow editor component. */
	protected FlowEditorPanel editor;
	/** The close adapter. */
	protected WindowAdapter closeAdapter;
	/**
	 * Constructs the GUI.
	 */
	public AdvanceFlowEditor() {
		setTitle("Advance Flow Editor");
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		setSize(800, 600);
		
		editor = new FlowEditorPanel(this);
		getContentPane().add(editor, BorderLayout.CENTER);
	}
	/** Perform the exit steps, e.g., save the current flow model, save editor settings, etc. */
	@Override
	public void exit() {
		dispose();
	}
	@Override
	public void setCloseHandler(final ActionListener onClose) {
		removeWindowListener(closeAdapter);
		closeAdapter = null;
		if (onClose != null) {
			closeAdapter = new WindowAdapter() {
				@Override
				public void windowClosing(WindowEvent e) {
					ActionEvent ae = new ActionEvent(e.getSource(), e.getID(), "CLOSE", System.currentTimeMillis(), 0);
					onClose.actionPerformed(ae);
				};
			};
			addWindowListener(closeAdapter);
		}
	}
	/**
	 * The program startup.
	 * @param args no arguments at the moment.
	 */
	public static void main(String[] args) {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				AdvanceFlowEditor afe = new AdvanceFlowEditor();
				afe.setLocationRelativeTo(null);
				afe.setVisible(true);
			}
		});
	}
}
