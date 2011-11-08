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

package eu.advance.logistics.flow.engine.cc;

import hu.akarnokd.reactive4java.base.Action0;
import hu.akarnokd.reactive4java.base.Func1;
import hu.akarnokd.reactive4java.base.Pair;
import hu.akarnokd.reactive4java.interactive.Interactive;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.GroupLayout.Group;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

import com.google.common.collect.Iterables;

import edu.umd.cs.findbugs.annotations.NonNull;
import eu.advance.logistics.flow.engine.api.ds.AdvanceKeyStore;
import eu.advance.logistics.flow.engine.api.ds.AdvanceSOAPChannel;

/**
 * The SOAP channel details.
 * @author akarnokd, 2011.10.13.
 */
public class CCSOAPDetails extends JPanel implements
		CCLoadSave<AdvanceSOAPChannel> {
	/** */
	private static final long serialVersionUID = 447371924967453848L;
	/** Text field. */
	protected JTextField name;
	/** Text field. */
	protected JTextField endpoint;
	/** Text field. */
	protected JTextField targetURI;
	/** Text field. */
	protected JTextField targetNamespace;
	/** Text field. */
	protected JTextField methodName;
	/** Is encrypted? */
	protected JCheckBox encrypted;
	/** The available keystores. */
	protected JComboBox keystores;
	/** Manage keystores button. */
	protected JButton manageKeyStores;
	/** Text field. */
	protected JTextField alias;
	/** Password field. */
	protected JPasswordField password;
	/** The label manager. */
	protected final LabelManager labels;
	/** The encrypted panel. */
	protected JPanel encryptedPanel;
	/**
	 * Creates the panel GUI.
	 * @param labels the label manager
	 */
	public CCSOAPDetails(@NonNull final LabelManager labels) {
		this.labels = labels;
		name = new JTextField();
		endpoint = new JTextField();
		targetURI = new JTextField();
		targetNamespace = new JTextField();
		methodName = new JTextField();
		encrypted = new JCheckBox(labels.get("Encrypted channel"));
		keystores = new JComboBox();
		manageKeyStores = new JButton(labels.get("Manage keystores..."));
		alias = new JTextField();
		password = new JPasswordField();
		
		GroupLayout gl = new GroupLayout(this);
		setLayout(gl);
		gl.setAutoCreateGaps(true);
		
		Pair<Group, Group> gs = GUIUtils.createForm(gl, 2,
				labels.get("Name:"), name,
				labels.get("Endpoint:"), endpoint,
				labels.get("Target object URI:"), targetURI,
				labels.get("Namespace URI:"), targetNamespace,
				labels.get("Method name:"), methodName
		);
		
		encryptedPanel = new JPanel();

		ComponentTitledBorder border = new ComponentTitledBorder(
				encrypted, encryptedPanel, BorderFactory.createEtchedBorder());
		encryptedPanel.setBorder(border);
		encrypted.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				enableDisableEncrypted();
			}

		});
		
		createEncryptedPanel(encryptedPanel);
		enableDisableEncrypted();
		
		gl.setHorizontalGroup(
			gl.createParallelGroup()
			.addGroup(gs.first)
			.addComponent(encryptedPanel)
		);
		gl.setVerticalGroup(gs.second.addComponent(encryptedPanel));
	}
	/**
	 * Enable/disable components of the encrypted panel.
	 */
	void enableDisableEncrypted() {
		boolean en = encrypted.isSelected();
		for (Component c : encryptedPanel.getComponents()) {
			c.setEnabled(en);
		}
	}
	/**
	 * Create the encrypted panel.
	 * @param panel the target panel
	 */
	private void createEncryptedPanel(JPanel panel) {
		GroupLayout gl = new GroupLayout(panel);
		panel.setLayout(gl);
		gl.setAutoCreateContainerGaps(true);
		gl.setAutoCreateGaps(true);
		
		JLabel keystoreLabel = new JLabel(labels.get("Keystore:"));
		JLabel aliasLabel = new JLabel(labels.get("Alias:"));
		JLabel passwordLabel = new JLabel(labels.get("Password:"));
		
		gl.setHorizontalGroup(
			gl.createParallelGroup()
			.addGroup(
				gl.createSequentialGroup()
				.addGroup(
					gl.createParallelGroup()
					.addComponent(keystoreLabel)
					.addComponent(aliasLabel)
				)
				.addGroup(
					gl.createParallelGroup()
					.addGroup(
						gl.createSequentialGroup()
						.addComponent(keystores)
						.addComponent(manageKeyStores)
					)
					.addGroup(
						gl.createSequentialGroup()
						.addComponent(alias)
						.addComponent(passwordLabel)
						.addComponent(password)
					)
				)
			)
		);
		gl.setVerticalGroup(
			gl.createSequentialGroup()
			.addGroup(
				gl.createParallelGroup(Alignment.BASELINE)
				.addComponent(keystoreLabel)
				.addComponent(keystores, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
				.addComponent(manageKeyStores)
			)
			.addGroup(
				gl.createParallelGroup(Alignment.BASELINE)
				.addComponent(aliasLabel)
				.addComponent(alias)
				.addComponent(passwordLabel)
				.addComponent(password)
			)
		);
	}
	@Override
	public void load(AdvanceSOAPChannel value) {
		name.setText(value.name);
		name.setEditable(false);
		endpoint.setText(value.endpoint.toString());
		targetURI.setText(value.targetObject.toString());
		targetNamespace.setText(value.targetNamespace.toString());
		methodName.setText(value.method);
		encrypted.setSelected(value.encrypted);
		if (value.encrypted) {
			keystores.setSelectedItem(value.keyStore);
			alias.setText(value.keyAlias);
			enableDisableEncrypted();
		} else {
			alias.setText("");
		}
		password.setText("");
	}

	@Override
	public AdvanceSOAPChannel save() {
		AdvanceSOAPChannel result = new AdvanceSOAPChannel();
		result.name = name.getText();
		if (result.name.isEmpty()) {
			GUIUtils.errorMessage(this, labels.get("Please enter a name!"));
			return null;
		}
		try {
			result.endpoint = new URL(endpoint.getText());
		} catch (MalformedURLException ex) {
			GUIUtils.errorMessage(this, ex);
			return null;
		}
		try {
			result.targetObject = new URI(targetURI.getText());
		} catch (URISyntaxException ex) {
			GUIUtils.errorMessage(this, ex);
			return null;
		}
		if (!targetNamespace.getText().isEmpty()) {
			try {
				result.targetNamespace = new URI(targetNamespace.getText());
			} catch (URISyntaxException ex) {
				GUIUtils.errorMessage(this, ex);
				return null;
			}
		}
		result.method = methodName.getText();
		
		if (encrypted.isSelected()) {
			result.encrypted = true;
			result.keyStore = (String)keystores.getSelectedItem();
			result.keyAlias = alias.getText();
			if (result.keyAlias.isEmpty()) {
				GUIUtils.errorMessage(this, labels.get("Please enter a key alias!"));
				return null;
			}
			char[] pw = password.getPassword();
			if (pw != null && pw.length > 0) {
				result.password(pw);
			}
		}
		
		return result;
	}

	@Override
	public void onAfterSave() {
		name.setEditable(false);
	}
	/**
	 * Set the keystores.
	 * @param keystores the keystore
	 */
	public void setKeyStores(Iterable<AdvanceKeyStore> keystores) {
		this.keystores.setModel(new DefaultComboBoxModel(
		Iterables.toArray(Interactive.select(keystores, new Func1<AdvanceKeyStore, String>() {
			@Override
			public String invoke(AdvanceKeyStore param1) {
				return param1.name;
			}
		}), String.class)));
	}
	/**
	 * Set the action for the manage keystores button.
	 * @param action the action
	 */
	public void setManageKeyStores(@NonNull final Action0 action) {
		manageKeyStores.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				action.invoke();
			}
		});
	}
}
