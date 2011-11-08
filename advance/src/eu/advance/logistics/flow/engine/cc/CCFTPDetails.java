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

import hu.akarnokd.reactive4java.base.Action1;
import hu.akarnokd.reactive4java.base.Func1;
import hu.akarnokd.reactive4java.base.Pair;
import hu.akarnokd.reactive4java.interactive.Interactive;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.GroupLayout.Group;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JTextField;

import edu.umd.cs.findbugs.annotations.NonNull;
import eu.advance.logistics.flow.engine.api.ds.AdvanceFTPDataSource;
import eu.advance.logistics.flow.engine.api.ds.AdvanceFTPProtocols;
import eu.advance.logistics.flow.engine.api.ds.AdvanceKeyStore;
import eu.advance.logistics.flow.engine.api.ds.AdvanceLoginType;

/**
 * The FTP connection details.
 * @author akarnokd, 2011.10.13.
 */
public class CCFTPDetails extends JPanel implements
		CCLoadSave<AdvanceFTPDataSource> {
	/** */
	private static final long serialVersionUID = 6201768105859035639L;
	/** The login subpanel. */
	protected LoginTypePanel login;
	/** The test button. */
	protected JButton test;
	/** The name. */
	protected JTextField name;
	/** The protocol. */
	protected JComboBox protocol;
	/** The address. */
	protected JTextField address;
	/** The remote directory. */
	protected JTextField directory;
	/** The label manager. */
	protected final LabelManager labels;
	/**
	 * Create the GUI panel.
	 * @param labels the label manager
	 */
	public CCFTPDetails(@NonNull final LabelManager labels) {
		this.labels = labels;
		login = new LoginTypePanel(labels);
		login.setBorder(BorderFactory.createTitledBorder(labels.get("Authentication")));
		test = new JButton(labels.get("Test"));
		test.setVisible(false);
		
		name = new JTextField();
		protocol = new JComboBox(AdvanceFTPProtocols.values());
		address = new JTextField();
		directory = new JTextField();
		
		GroupLayout gl = new GroupLayout(this);
		setLayout(gl);
		gl.setAutoCreateGaps(true);
		
		Pair<Group, Group> g = GUIUtils.createForm(gl, 2,
				labels.get("Name:"), name,
				labels.get("Protocol:"), protocol,
				labels.get("Address:"), address,
				labels.get("Directory:"), directory
		);
		gl.setHorizontalGroup(
			gl.createParallelGroup(Alignment.CENTER)
			.addGroup(
				gl.createParallelGroup()
				.addGroup(g.first)
				.addComponent(login)
			)
			.addComponent(test)
		);
		g.second.addComponent(login).addComponent(test);
		gl.setVerticalGroup(g.second);
	}
	@Override
	public void load(AdvanceFTPDataSource value) {
		name.setText(value.name);
		name.setEditable(false);
		protocol.setSelectedItem(value.protocol);
		address.setText(value.address);
		directory.setText(value.remoteDirectory);
		login.setUserPassword(null);
		login.setKeyPassword(null);
		switch (value.loginType) {
		case NONE:
			login.none.setSelected(true);
			login.setAlias("");
			login.setKeyStore("");
			break;
		case BASIC:
			login.viaPassword.setSelected(true);
			login.setUserName(value.userOrKey);
			login.setAlias("");
			login.setKeyStore("");
			break;
		case CERTIFICATE:
			login.viaCertificate.setSelected(true);
			login.setUserName("");
			login.setKeyPassword(value.password());
			login.setAlias(value.userOrKey);
			login.setKeyStore(value.keyStore);
			break;
		default:
		}
		test.setVisible(true);
	}

	@Override
	public AdvanceFTPDataSource save() {
		AdvanceFTPDataSource result = new AdvanceFTPDataSource();
		
		result.name = name.getText();
		if (result.name.isEmpty()) {
			GUIUtils.errorMessage(this, labels.get("Please enter a name!"));
			return null;
		}
		result.protocol = (AdvanceFTPProtocols)protocol.getSelectedItem();
		result.address = address.getText();
		if (result.address.isEmpty()) {
			GUIUtils.errorMessage(this, labels.get("Please enter an address!"));
			return null;
		}
		result.remoteDirectory = directory.getText();
		
		result.loginType = login.getLoginType();
		if (result.loginType == AdvanceLoginType.BASIC) {
			result.userOrKey = login.getUserName();
			char[] p = login.getUserPassword();
			if (p != null && p.length > 0) {
				result.password(p);
			}
		} else
		if (result.loginType == AdvanceLoginType.CERTIFICATE) {
			result.keyStore = login.getKeyStore();
			result.userOrKey = login.getAlias();
			char[] p = login.getKeyPassword();
			if (p != null && p.length > 0) {
				result.password(p);
			}
		}
		
		return result;
	}

	@Override
	public void onAfterSave() {
		test.setVisible(true);
		name.setEditable(false);
	}
	/**
	 * Set the keystores.
	 * @param keystores the keystore
	 */
	public void setKeyStores(Iterable<AdvanceKeyStore> keystores) {
		login.setKeyStores(
			Interactive.select(keystores, new Func1<AdvanceKeyStore, String>() {
				@Override
				public String invoke(AdvanceKeyStore param1) {
					return param1.name;
				}
			})
		);
	}
	/**
	 * Set the test action for the Test button.
	 * @param action the action receiving the JDBC name
	 */
	public void setTestAction(@NonNull final Action1<String> action) {
		test.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				action.invoke(name.getText());
			}
		});
	}
}
