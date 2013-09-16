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

package eu.advance.logistics.flow.engine.cc;

import hu.akarnokd.reactive4java.base.Pair;

import java.awt.Container;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.util.Arrays;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.GroupLayout.Group;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPasswordField;

import edu.umd.cs.findbugs.annotations.NonNull;

/**
 * The key-password enter dialog.
 * @author akarnokd, 2011.10.18.
 */
public class CCKeyPasswordDialog extends JDialog {
	/** */
	private static final long serialVersionUID = 3523254711098098294L;
	/** The password. */
	protected JPasswordField password;
	/** The password again. */
	protected JPasswordField passwordAgain;
	/** Was the dialog approved? */
	protected boolean approved;
	/**
	 * Construct the dialog.
	 * @param labels the label manager
	 * @param keyStore the keystore name
	 * @param alias the key alias
	 */
	public CCKeyPasswordDialog(@NonNull final LabelManager labels, String keyStore, String alias) {
		setTitle(labels.get("Enter key password"));
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		setModal(true);
		setResizable(false);
		
		Container c = getContentPane();
		GroupLayout gl = new GroupLayout(c);
		c.setLayout(gl);
		gl.setAutoCreateContainerGaps(true);
		gl.setAutoCreateGaps(true);

		JButton okay = new JButton(labels.get("OK"));
		JButton cancel = new JButton(labels.get("Cancel"));
		
		password = new JPasswordField(15);
		passwordAgain = new JPasswordField(15);
		
		Pair<Group, Group> g = GUIUtils.createForm(gl, 2,
			labels.get("Keystore:"), keyStore,
			labels.get("Key"), alias,
			labels.get("Password:"), password,
			labels.get("Password again:"), passwordAgain
		);
		
		gl.setHorizontalGroup(
			gl.createParallelGroup(Alignment.CENTER)
			.addGroup(g.first)
			.addGroup(
				gl.createSequentialGroup()
				.addComponent(okay)
				.addComponent(cancel)
			)
		);
		gl.setVerticalGroup(
			gl.createSequentialGroup()
			.addGroup(g.second)
			.addGroup(
				gl.createParallelGroup(Alignment.BASELINE)
				.addComponent(okay)
				.addComponent(cancel)
			)
		);
		pack();
		
		ActionListener okAction = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				char[] p1 = password.getPassword();
				char[] p2 = passwordAgain.getPassword();
				if (passwordAgain.isVisible() && Arrays.equals(p1, p2)) {
					approved = true;
					close();
					return;
				}
				GUIUtils.errorMessage(CCKeyPasswordDialog.this, labels.get("The passwords mismatch!"));
			}
		};
		okay.addActionListener(okAction);
		password.addActionListener(okAction);
		passwordAgain.addActionListener(okAction);
		cancel.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				close();
			}
		});
	}
	/** Close the dialog. */
	void close() {
		Toolkit.getDefaultToolkit().getSystemEventQueue().postEvent(new WindowEvent(this, WindowEvent.WINDOW_CLOSING));
	}
	/**
	 * Display the dialog.
	 * @return the password chars or null if the dialog was cancelled
	 */
	public char[] display() {
		setLocationRelativeTo(null);
		approved = false;
		setVisible(true);
		if (approved) {
			return password.getPassword();
		}
		return null;
	}
}
