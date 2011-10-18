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

import hu.akarnokd.reactive4java.base.Func1;
import hu.akarnokd.reactive4java.base.Pair;
import hu.akarnokd.reactive4java.interactive.Interactive;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Group;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JTextField;

import edu.umd.cs.findbugs.annotations.NonNull;
import eu.advance.logistics.flow.engine.api.AdvanceEmailBox;
import eu.advance.logistics.flow.engine.api.AdvanceEmailReceiveProtocols;
import eu.advance.logistics.flow.engine.api.AdvanceEmailSendProtocols;
import eu.advance.logistics.flow.engine.api.AdvanceKeyStore;

/**
 * The email box details.
 * @author akarnokd, 2011.10.17.
 */
public class CCEmailDetails extends JPanel implements CCLoadSave<AdvanceEmailBox> {
	/** */
	private static final long serialVersionUID = -9043464884810863021L;
	/** The label manager. */
	protected final LabelManager labels;
	/** The login type. */
	protected LoginTypePanel login;
	/** The send protocol. */
	protected JComboBox<AdvanceEmailSendProtocols> sendProtocol;
	/** The send address. */
	protected JTextField sendAddress;
	/** The receive protocol. */
	protected JComboBox<AdvanceEmailReceiveProtocols> receiveProtocol;
	/** The receive address. */
	protected JTextField receiveAddress;
	/** The remote folder. */
	protected JTextField folder;
	/** The email address. */
	protected JTextField email;
	/** The name. */
	protected JTextField name;
	/**
	 * Constructs the panel.
	 * @param labels the label manager
	 */
	public CCEmailDetails(@NonNull final LabelManager labels) {
		this.labels = labels;
		GroupLayout gl = new GroupLayout(this);
		setLayout(gl);
		gl.setAutoCreateGaps(true);
		
		name = new JTextField();
		email = new JTextField();
		folder = new JTextField("INBOX");
		sendProtocol = new JComboBox<AdvanceEmailSendProtocols>(AdvanceEmailSendProtocols.values());
		sendAddress = new JTextField();
		receiveProtocol = new JComboBox<AdvanceEmailReceiveProtocols>(AdvanceEmailReceiveProtocols.values());
		receiveAddress = new JTextField();
		login = new LoginTypePanel(labels);
		
		Pair<Group, Group> g = GUIUtils.createForm(gl, 2, 
			labels.get("Name:"), name,
			labels.get("Sender:"), email,
			labels.get("Folder:"), folder,
			labels.get("Send via:"), sendProtocol,
			labels.get("Send address:"), sendAddress,
			labels.get("Receive via:"), receiveProtocol,
			labels.get("Receive address:"), receiveAddress
		);
		gl.setHorizontalGroup(
			gl.createParallelGroup()
			.addGroup(g.first)
			.addComponent(login)
		);
		gl.setVerticalGroup(
			gl.createSequentialGroup()
			.addGroup(g.second)
			.addComponent(login)
		);
	}
	@Override
	public void load(AdvanceEmailBox value) {
		name.setText(value.name);
		email.setText(value.email);
		folder.setText(value.folder);
		receiveAddress.setText(value.receiveAddress);
		receiveProtocol.setSelectedItem(value.receive);
		sendAddress.setText(value.sendAddress);
		sendProtocol.setSelectedItem(value.send);
		
		login.clear();
		login.setLoginType(value.login);
		switch (value.login) {
		case BASIC:
			login.setUserName(value.user);
			break;
		case CERTIFICATE:
			login.setAlias(value.user);
			login.setKeyStore(value.keyStore);
			break;
		default:
		}
	}

	@Override
	public AdvanceEmailBox save() {
		AdvanceEmailBox result = new AdvanceEmailBox();
		
		result.name = name.getText();
		if (result.name.isEmpty()) {
			GUIUtils.errorMessage(this, labels.get("Please enter a name!"));
			return null;
		}
		result.send = (AdvanceEmailSendProtocols) sendProtocol.getSelectedItem();
		
		if (result.send != AdvanceEmailSendProtocols.NONE) {
			result.email = email.getText();
			result.sendAddress = sendAddress.getText();
			if (result.email.isEmpty()) {
				GUIUtils.errorMessage(this, labels.get("Please enter the sender email address!"));
				return null;
			} else
			if (result.sendAddress.isEmpty()) {
				GUIUtils.errorMessage(this, labels.get("Please enter a send service address!"));
				return null;
			}
		}
		result.receive = (AdvanceEmailReceiveProtocols) receiveProtocol.getSelectedItem();
		if (result.receive != AdvanceEmailReceiveProtocols.NONE) {
			result.receiveAddress = receiveAddress.getText();
			result.folder = folder.getText();
			if (result.receiveAddress.isEmpty()) {
				GUIUtils.errorMessage(this, labels.get("Please enter a receive service address!"));
				return null;
			}
			if (result.folder.isEmpty()) {
				GUIUtils.errorMessage(this, labels.get("Please enter the receive inbox folder name!"));
				return null;
			}
		}
		result.login = login.getLoginType();
		switch (result.login) {
		case BASIC:
			result.user = login.getUserName();
			char[] p = login.getUserPassword();
			if (p != null && p.length > 0) {
				result.password(p);
			}
			if (result.user.isEmpty()) {
				GUIUtils.errorMessage(this, labels.get("Please enter the user name!"));
				return null;
			}
			break;
		case CERTIFICATE:
			result.user = login.getAlias();
			p = login.getKeyPassword();
			if (p != null && p.length > 0) {
				result.password(p);
			}
			result.keyStore = login.getKeyStore();
			if (result.keyStore == null || result.keyStore.isEmpty()) {
				GUIUtils.errorMessage(this, labels.get("Please choose a keystore!"));
				return null;
			}
			if (result.user.isEmpty()) {
				GUIUtils.errorMessage(this, labels.get("Please enter the key alias!"));
			}
		default:
			
		}
		
		return result;
	}

	@Override
	public void onAfterSave() {
		name.setEditable(false);
	}
	/**
	 * Set the keystores.
	 * @param keyStores the sequence of keystores
	 */
	public void setKeyStores(Iterable<? extends AdvanceKeyStore> keyStores) {
		login.setKeyStores(Interactive.select(keyStores, new Func1<AdvanceKeyStore, String>() {
			@Override
			public String invoke(AdvanceKeyStore param1) {
				return param1.name;
			}
		}));
	}
}
