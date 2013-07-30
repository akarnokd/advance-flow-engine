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
import hu.akarnokd.utils.crypto.DistinguishedName;

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
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFormattedTextField;
import javax.swing.JPasswordField;
import javax.swing.JSeparator;
import javax.swing.JTextField;

import edu.umd.cs.findbugs.annotations.NonNull;
import eu.advance.logistics.flow.engine.api.ds.AdvanceGenerateKey;

/**
 * Generate key dialog. 
 * @author akarnokd, 2011.10.20.
 */
public class CCKeyGenDialog extends JDialog {
	/** */
	private static final long serialVersionUID = 7856228299216172427L;
	/** Engine info panel. */
	protected EngineInfoPanel engineInfo;
	/** The top separator. */
	protected JSeparator topSeparator;
	/** The label manager. */
	protected final LabelManager labels;
	/** The key name. */
	protected JTextField name;
	/** The algorithm. */
	protected JComboBox<String> algorithm;
	/** The key length. */
	protected JFormattedTextField keyLength;
	/** The password. */
	protected JPasswordField password;
	/** The password again. */
	protected JPasswordField passwordAgain;
	/** The domain. */
	protected JTextField domain;
	/** The distinguished name fields. */
	protected class DNFields {
		/** Common name. */
		JTextField commonName = new JTextField(15);
		/** Organization unit. */
		JTextField orgUnit = new JTextField(15);
		/** Organization. */
		JTextField org = new JTextField(15);
		/** Locality. */
		JTextField locality = new JTextField(15);
		/** State. */
		JTextField state = new JTextField(15);
		/** Country. */
		JTextField country = new JTextField(15);
	}
	/** The issuer DN. */
	protected DNFields issuer;
	/** The subject DN. */
	protected DNFields subject;
	/** Generate. */
	protected JButton gen;
	/** Cancel. */
	protected JButton cancel;
	/** Was the dialog approved? */
	protected AdvanceGenerateKey key;
	/** The bottom help panel. */
	private HelpPanel bottom;
	/**
	 * Constructs the GUI.
	 * @param labels the label manager
	 */
	public CCKeyGenDialog(@NonNull final LabelManager labels) {
		this.labels = labels;
		setTitle(labels.get("Generate key"));
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		setResizable(false);
		setModal(true);
		
		Container c = getContentPane();
		GroupLayout gl = new GroupLayout(c);
		c.setLayout(gl);
		gl.setAutoCreateContainerGaps(true);
		gl.setAutoCreateGaps(true);
		
		engineInfo = new EngineInfoPanel(labels);
		
		topSeparator = new JSeparator(JSeparator.HORIZONTAL);
		
		issuer = new DNFields();
		subject = new DNFields();
		name = new JTextField();
		algorithm = new JComboBox<>(new String[] { "RSA", "DSA" });
		keyLength = new JFormattedTextField(1024);
		password = new JPasswordField();
		passwordAgain = new JPasswordField();
		domain = new JTextField();
		
		gen = new JButton(labels.get("Generate"));
		gen.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				doGenerate();
			}
		});
		cancel = new JButton(labels.get("Cancel"));
		cancel.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				doClose();
			}
		});
		
		Pair<Group, Group> tg = GUIUtils.createForm(gl, 2,
				labels.get("Name:"), name,
				labels.get("Algorithm:"), algorithm,
				labels.get("Key length:"), keyLength,
				labels.get("Password:"), password,
				labels.get("Password again:"), passwordAgain,
				labels.get("Domain:"), domain
		);

		JSeparator middle = new JSeparator(JSeparator.HORIZONTAL);
		bottom = new HelpPanel();
		
		Pair<Group, Group> bg = GUIUtils.createForm(gl, 3,
				labels.get("Distinguished name of"), labels.get("Issuer"), labels.get("Subject"),
				labels.get("Common name:"), issuer.commonName, subject.commonName,
				labels.get("Organization unit:"), issuer.orgUnit, subject.orgUnit,
				labels.get("Organization:"), issuer.org, subject.org,
				labels.get("Locality:"), issuer.locality, subject.locality,
				labels.get("State:"), issuer.state, subject.state,
				labels.get("Country:"), issuer.country, subject.country
		);
		
		gl.setHorizontalGroup(
			gl.createParallelGroup(Alignment.CENTER)
			.addComponent(engineInfo)
			.addComponent(topSeparator)
			.addGroup(
				gl.createParallelGroup()
				.addGroup(tg.first)
				.addComponent(middle)
				.addGroup(bg.first)
			)
			.addComponent(bottom)
			.addGroup(
				gl.createSequentialGroup()
				.addComponent(gen)
				.addComponent(cancel)
			)
		);
		
		gl.setVerticalGroup(
			gl.createSequentialGroup()
			.addComponent(engineInfo)
			.addComponent(topSeparator)
			.addGroup(tg.second)
			.addComponent(middle, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
			.addGroup(bg.second)
			.addComponent(bottom)
			.addGroup(
				gl.createParallelGroup(Alignment.BASELINE)
				.addComponent(gen)
				.addComponent(cancel)
			)
		);

		pack();
	}
	/**
	 * Set the key generation properties.
	 */
	void doGenerate() {
		AdvanceGenerateKey k = new AdvanceGenerateKey();

		k.keyAlias = name.getText();
		if (k.keyAlias.isEmpty()) {
			GUIUtils.errorMessage(this, labels.get("Please enter a key name!"));
			return;
		}
		k.algorithm = (String)algorithm.getSelectedItem();
		k.keySize = (Integer)keyLength.getValue();
		k.domain = domain.getText();
		char[] p1 = password.getPassword();
		char[] p2 = passwordAgain.getPassword();
		if (!Arrays.equals(p1, p2)) {
			GUIUtils.errorMessage(this, labels.get("Passwords mismatch!"));
			return;
		}
		k.password(p1);
		if (issuer.commonName.getText().isEmpty()) {
			GUIUtils.errorMessage(this, labels.get("Please enter the issuer common name!"));
			return;
		}
		if (subject.commonName.getText().isEmpty()) {
			GUIUtils.errorMessage(this, labels.get("Please enter the subject common name!"));
			return;
		}
		
		k.issuerDn = new DistinguishedName(
			issuer.commonName.getText(),
			issuer.orgUnit.getText(),
			issuer.org.getText(),
			issuer.locality.getText(),
			issuer.state.getText(),
			issuer.country.getText()
		);
		k.subjectDn = new DistinguishedName(
			subject.commonName.getText(),
			subject.orgUnit.getText(),
			subject.org.getText(),
			subject.locality.getText(),
			subject.state.getText(),
			subject.country.getText()
		);
		
		key = k;
		doClose();
	}
	/** Close the dialog. */
	void doClose() {
		Toolkit.getDefaultToolkit().getSystemEventQueue().postEvent(new WindowEvent(this, WindowEvent.WINDOW_CLOSING));
	}
	/**
	 * Display the dialog.
	 * @return the key parameters or null if the dialog was cancelled.
	 */
	public AdvanceGenerateKey display() {
		key = null;
		setVisible(true);
		return key;
	}
	/**
	 * Show the engine info panel?
	 * @param visible visible
	 */
	public void showEngineInfo(boolean visible) {
		engineInfo.setVisible(visible);
		topSeparator.setVisible(visible);
	}
}
