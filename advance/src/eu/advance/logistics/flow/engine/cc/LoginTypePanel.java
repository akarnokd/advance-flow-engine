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

import javax.swing.ButtonGroup;
import javax.swing.DefaultComboBoxModel;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.GroupLayout.Alignment;

import com.google.common.collect.Iterables;

import edu.umd.cs.findbugs.annotations.NonNull;
import eu.advance.logistics.flow.engine.api.AdvanceLoginType;

/**
 * A composite panel with options to login via username/password or certificate.
 * @author karnokd, 2011.10.11.
 */
public class LoginTypePanel extends JPanel {
	/** */
	private static final long serialVersionUID = -3276024469526975422L;
	/** No login. */
	protected JRadioButton none;
	/** Via password. */
	protected JRadioButton viaPassword;
	/** Via certificate. */
	protected JRadioButton viaCertificate;
	/** The available keystores. */
	protected JComboBox<String> keystoreList;
	/** Manage keystores button. */
	protected JButton manageKeyStores;
	/** Username. */
	protected JTextField userName;
	/** Username label. */
	protected JLabel userNameLabel;
	/** User password. */
	protected JPasswordField userPassword;
	/** User password label. */
	protected JLabel userPasswordLabel;
	/** Keystore list label. */
	protected JLabel keystoreListLabel;
	/** Alias label. */
	protected JLabel aliasLabel;
	/** Alias. */
	protected JTextField alias;
	/** Key password. */
	protected JPasswordField keyPassword;
	/** Key password label. */
	protected JLabel keyPasswordLabel;
	/** The action to invoke when clicking on the manage keystores button. */
	protected Action0 onManageKeyStores;
	/** 
	 * Create the GUI.
	 * @param labels the label manager 
	 */
	public LoginTypePanel(final LabelManager labels) {
		GroupLayout gl = new GroupLayout(this);
		setLayout(gl);
		gl.setAutoCreateGaps(true);
		
		none = new JRadioButton(labels.get("None"));
		viaPassword = new JRadioButton(labels.get("Via password"));
		viaCertificate = new JRadioButton(labels.get("Via certificate"));
		
		userNameLabel = new JLabel(labels.get("Username:"));
		userName = new JTextField();
		userPasswordLabel = new JLabel(labels.get("Password:"));
		userPassword = new JPasswordField();
		
		keystoreListLabel = new JLabel(labels.get("Keystore:"));
		keystoreList = new JComboBox<String>();
		manageKeyStores = new JButton(labels.get("Manage keystores..."));
		manageKeyStores.addActionListener(GUIUtils.createFromMethod(this, "doManageKeyStores"));
		
		aliasLabel = new JLabel(labels.get("Key alias:"));
		alias = new JTextField();
		keyPasswordLabel = new JLabel(labels.get("Key password:"));
		keyPassword = new JPasswordField();
		
		ButtonGroup g = new ButtonGroup();
		g.add(none);
		g.add(viaPassword);
		g.add(viaCertificate);
		
		none.setSelected(true);
		
		gl.setHorizontalGroup(
			gl.createParallelGroup()
			.addComponent(none)
			.addComponent(viaPassword)
			.addGroup(
				gl.createSequentialGroup()
				.addGap(20)
				.addComponent(userNameLabel)
				.addComponent(userName)
				.addComponent(userPasswordLabel)
				.addComponent(userPassword)
			)
			.addComponent(viaCertificate)
			.addGroup(
				gl.createSequentialGroup()
				.addGap(20)
				.addComponent(keystoreListLabel)
				.addComponent(keystoreList)
				.addComponent(manageKeyStores)
			)
			.addGroup(
				gl.createSequentialGroup()
				.addGap(20)
				.addComponent(aliasLabel)
				.addComponent(alias)
				.addComponent(keyPasswordLabel)
				.addComponent(keyPassword)
			)
		);
		
		gl.setVerticalGroup(
			gl.createSequentialGroup()
			.addComponent(none)
			.addComponent(viaPassword)
			.addGroup(
				gl.createParallelGroup(Alignment.BASELINE)
				.addComponent(userNameLabel)
				.addComponent(userName, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
				.addComponent(userPasswordLabel)
				.addComponent(userPassword, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
			)
			.addComponent(viaCertificate)
			.addGroup(
				gl.createParallelGroup(Alignment.BASELINE)
				.addComponent(keystoreListLabel)
				.addComponent(keystoreList, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
				.addComponent(manageKeyStores)
			)
			.addGroup(
				gl.createParallelGroup(Alignment.BASELINE)
				.addComponent(aliasLabel)
				.addComponent(alias, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
				.addComponent(keyPasswordLabel)
				.addComponent(keyPassword, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
			)
		);
	}
	/**
	 * Set the list of available keystores.
	 * @param keystores the keystores sequence
	 */
	public void setKeyStores(Iterable<String> keystores) {
		keystoreList.setModel(new DefaultComboBoxModel<String>(Iterables.toArray(keystores, String.class)));
	}
	/** 
	 * Display the none option?
	 * @param visible show?
	 */
	public void showNone(boolean visible) {
		none.setVisible(visible);
	}
	/**
	 * Display the username entry fields?
	 * @param visible show?
	 */
	public void showUserName(boolean visible) {
		userName.setVisible(visible);
		userNameLabel.setVisible(visible);
	}
	/**
	 * Set the user name field.
	 * @param userName the value
	 */
	public void setUserName(String userName) {
		this.userName.setText(userName);
	}
	/**
	 * @return the user name field value
	 */
	public String getUserName() {
		return userName.getText();
	}
	/**
	 * Set the user password.
	 * @param password the password
	 */
	public void setUserPassword(char[] password) {
		if (password != null) {
			userPassword.setText(new String(password));
		} else {
			userPassword.setText("");
		}
	}
	/**
	 * @return the user password value
	 */
	public char[] getUserPassword() {
		return userPassword.getPassword();
	}
	/** @return the key password value. */
	public char[] getKeyPassword() {
		return keyPassword.getPassword();
	}
	/**
	 * Set the key password.
	 * @param password the password
	 */
	public void setKeyPassword(char[] password) {
		if (password != null) {
			keyPassword.setText(new String(password));
		} else {
			keyPassword.setText("");
		}
	}
	/** @return the selected keystore. */
	public String getKeyStore() {
		return (String)keystoreList.getSelectedItem();
	}
	/** @return the key alias value. */
	public String getAlias() {
		return alias.getText();
	}
	/**
	 * Set the key alias field.
	 * @param alias the value
	 */
	public void setAlias(String alias) {
		this.alias.setText(alias);
	}
	/**
	 * Set the action for the manage keystores button.
	 * @param onManageKeyStores the action
	 */
	public void setManageKeyStores(@NonNull Action0 onManageKeyStores) {
		this.onManageKeyStores = onManageKeyStores;
	}
	/** Manage keystore button. */
	protected void doManageKeyStores() {
		if (onManageKeyStores != null) {
			onManageKeyStores.invoke();
		}
	}
	/** @return the login type enum. */
	public AdvanceLoginType getLoginType() {
		if (none.isSelected()) {
			return AdvanceLoginType.NONE;
		} else
		if (viaPassword.isSelected()) {
			return AdvanceLoginType.BASIC;
		}
		return AdvanceLoginType.CERTIFICATE;
	}
	/**
	 * Set a keystore.
	 * @param keyStore the keystore
	 */
	public void setKeyStore(String keyStore) {
		keystoreList.setSelectedItem(keyStore);
	}
}
