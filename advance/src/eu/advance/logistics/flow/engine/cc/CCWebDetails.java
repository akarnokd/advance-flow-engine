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
import hu.akarnokd.reactive4java.interactive.Interactive;

import java.net.MalformedURLException;
import java.net.URL;

import javax.swing.BorderFactory;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import edu.umd.cs.findbugs.annotations.NonNull;
import eu.advance.logistics.flow.engine.api.AdvanceKeyStore;
import eu.advance.logistics.flow.engine.api.AdvanceLoginType;
import eu.advance.logistics.flow.engine.api.AdvanceWebDataSource;

/**
 * The panel consisting of the body of the web details dialog.
 * @author karnokd, 2011.10.11.
 */
public class CCWebDetails extends JPanel implements CCLoadSave<AdvanceWebDataSource> {
	/** */
	private static final long serialVersionUID = -3261084268651747727L;
	/** The login panel. */
	public LoginTypePanel login;
	/** The name. */
	protected JTextField name;
	/** The url. */
	protected JTextField url;
	/**
	 * Create the panel GUI.
	 * @param labels the label manager
	 */
	public CCWebDetails(@NonNull final LabelManager labels) {
		login = new LoginTypePanel(labels);
		login.setBorder(BorderFactory.createTitledBorder(labels.get("Authentication")));
		name = new JTextField();
		url = new JTextField();
		JLabel nameLabel = new JLabel(labels.get("Name:"));
		JLabel urlLabel = new JLabel(labels.get("URL:"));

		GroupLayout gl = new GroupLayout(this);
		setLayout(gl);
		gl.setAutoCreateGaps(true);
		
		gl.setHorizontalGroup(
			gl.createParallelGroup()
			.addGroup(
				gl.createSequentialGroup()
				.addGroup(
					gl.createParallelGroup()
					.addComponent(nameLabel)
					.addComponent(urlLabel)
				)
				.addGroup(
					gl.createParallelGroup()
					.addComponent(name)
					.addComponent(url)
				)
			)
			.addComponent(login)
		);
		gl.setVerticalGroup(
			gl.createSequentialGroup()
			.addGroup(
				gl.createParallelGroup(Alignment.BASELINE)
				.addComponent(nameLabel)
				.addComponent(name, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
			)
			.addGroup(
				gl.createParallelGroup(Alignment.BASELINE)
				.addComponent(urlLabel)
				.addComponent(url, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
			)
			.addComponent(login)
		);
	}
	/** @return the name of the web data source. */
	public String getWebName() {
		return name.getText();
	}
	/** @return the URL of the web data source. */
	public String getURL() {
		return url.getText();
	}
	/**
	 * Set the web data source name.
	 * @param value the name
	 */
	public void setWebName(String value) {
		name.setText(value);
	}
	/**
	 * Set the data source URL.
	 * @param value the URL
	 */
	public void setURL(String value) {
		url.setText(value);
	}
	/**
	 * Save the panel values.
	 * @return the object or null if an error occurred.
	 */
	@Override
	public AdvanceWebDataSource save() {
		final AdvanceWebDataSource e = new AdvanceWebDataSource();
		e.name = getWebName();
		try {
			e.url = new URL(getURL());
		} catch (MalformedURLException ex) {
			GUIUtils.errorMessage(ex);
			return null;
		}
		e.loginType = login.getLoginType();
		if (e.loginType == AdvanceLoginType.BASIC) {
			e.userOrKeyAlias = login.getUserName();
			char[] p = login.getUserPassword();
			if (p != null && p.length > 0) {
				e.password(p);
			}
		} else
		if (e.loginType == AdvanceLoginType.CERTIFICATE) {
			e.keyStore = login.getKeyStore();
			e.userOrKeyAlias = login.getAlias();
			char[] p = login.getKeyPassword();
			if (p != null && p.length > 0) {
				e.password(p);
			}
		}
		return e;
	}
	/**
	 * Set the keystore name list.
	 * @param keyStores the keystore sequence
	 */
	public void setKeyStores(Iterable<AdvanceKeyStore> keyStores) {
		login.setKeyStores(Interactive.select(keyStores, new Func1<AdvanceKeyStore, String>() {
			@Override
			public String invoke(AdvanceKeyStore param1) {
				return param1.name;
			}
		}));
	}
	/**
	 * Load the panel from the supplied data source.
	 * @param e the object to load from
	 */
	@Override
	public void load(AdvanceWebDataSource e) {
		setWebName(e.name);
		setURL(e.url.toString());
		switch (e.loginType) {
		case NONE:
			login.none.setSelected(true);
			break;
		case BASIC:
			login.viaPassword.setSelected(true);
			login.setUserName(e.userOrKeyAlias);
			login.setUserPassword(e.password());
			break;
		case CERTIFICATE:
			login.viaCertificate.setSelected(true);
			login.setKeyPassword(e.password());
			login.setAlias(e.userOrKeyAlias);
			login.setKeyStore(e.keyStore);
			break;
		default:
		}
	}
	@Override
	public void onAfterSave() {
		name.setEditable(false);
	}
}
