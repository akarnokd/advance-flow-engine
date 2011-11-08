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

import hu.akarnokd.reactive4java.base.Action1;
import hu.akarnokd.reactive4java.base.Pair;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.GroupLayout.Group;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

import com.google.common.base.Objects;

import edu.umd.cs.findbugs.annotations.NonNull;
import eu.advance.logistics.flow.engine.api.ds.AdvanceJDBCDataSource;
import eu.advance.logistics.flow.engine.api.ds.AdvanceJDBCDrivers;

/**
 * The JDBC details panel.
 * @author akarnokd, 2011.10.12.
 */
public class CCJDBCDetails extends JPanel implements
		CCLoadSave<AdvanceJDBCDataSource> {
	/** */
	private static final long serialVersionUID = 3509273005064205330L;
	/** The name. */
	protected JTextField name;
	/** The driver. */
	protected JComboBox driver;
	/** The custom driver label. */
	protected JLabel customDriverLabel;
	/** The custom driver field. */
	protected JTextField customDriver;
	/** The URL. */
	protected JTextField url;
	/** The user. */
	protected JTextField user;
	/** The password. */
	protected JPasswordField password;
	/** The pool size. */
	protected JFormattedTextField pool;
	/** Test the connection. */
	protected JButton test;
	/** The label manager. */
	protected final LabelManager labels;
	/**
	 * Create the GUI.
	 * @param labels the label manager.
	 */
	public CCJDBCDetails(@NonNull final LabelManager labels) {
		this.labels = labels;
		GroupLayout gl = new GroupLayout(this);
		setLayout(gl);
		gl.setAutoCreateGaps(true);

		name = new JTextField();
		url = new JTextField();
		driver = new JComboBox(AdvanceJDBCDrivers.values());
		driver.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				AdvanceJDBCDrivers si = (AdvanceJDBCDrivers)driver.getSelectedItem();
				url.setText(si.urlTemplate);
				customDriver.setVisible(si == AdvanceJDBCDrivers.GENERIC);
				customDriverLabel.setVisible(si == AdvanceJDBCDrivers.GENERIC);
			}
		});
		customDriver = new JTextField();
		customDriver.setVisible(driver.getSelectedItem() == AdvanceJDBCDrivers.GENERIC);
		customDriverLabel = new JLabel();
		customDriverLabel.setVisible(driver.getSelectedItem() == AdvanceJDBCDrivers.GENERIC);
		
		user = new JTextField();
		password = new JPasswordField();
		pool = new JFormattedTextField(5);
		
		test = new JButton(labels.get("Test"));
		test.setVisible(false);

		Pair<Group, Group> g = GUIUtils.createForm(gl, 2,
				labels.get("Name:"), name,
				labels.get("Driver:"), driver,
				customDriverLabel, customDriver,
				labels.get("URL:"), url,
				labels.get("User:"), user,
				labels.get("Password:"), password,
				labels.get("Connection pool size:"), pool
		);
		
		gl.setHorizontalGroup(
			gl.createParallelGroup(Alignment.CENTER)
			.addGroup(g.first)
			.addComponent(test)
		);
		g.second.addComponent(test);
		gl.setVerticalGroup(g.second);
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
	@Override
	public void load(AdvanceJDBCDataSource value) {
		name.setText(value.name);
		name.setEditable(false);
		boolean found = false;
		for (AdvanceJDBCDrivers d : AdvanceJDBCDrivers.values()) {
			if (Objects.equal(d.driverClass, value.driver)) {
				driver.setSelectedItem(d);
				found = true;
				break;
			}
		}
		customDriverLabel.setVisible(!found);
		customDriver.setVisible(!found);
		if (!found) {
			customDriver.setText(value.driver);
		} else {
			customDriver.setText("");
		}
		url.setText(value.url);
		user.setText(value.user);
		password.setText("");
		pool.setValue(value.poolSize);
		test.setVisible(true);
	}

	@Override
	public AdvanceJDBCDataSource save() {
		AdvanceJDBCDataSource result = new AdvanceJDBCDataSource();
		
		result.name = name.getText();
		
		if (result.name.isEmpty()) {
			GUIUtils.errorMessage(this, labels.get("Please enter a name!"));
			return null;
		}
		
		AdvanceJDBCDrivers d = (AdvanceJDBCDrivers)driver.getSelectedItem();
		if (d == AdvanceJDBCDrivers.GENERIC) {
			if (customDriver.getText().isEmpty()) {
				GUIUtils.errorMessage(this, labels.get("Please enter the fully qualified class name of the driver!"));
				return null;
			}
			result.driver = customDriver.getText();
		} else {
			result.driver = d.driverClass;
		}
		result.url = url.getText();
		if (result.url.isEmpty()) {
			GUIUtils.errorMessage(this, labels.get("Please enter URL!"));
			return null;
		}
		result.user = user.getText();
		char[] p = password.getPassword();
		if (p != null && p.length > 0) {
			result.password(p);
		}
		result.poolSize = (Integer)pool.getValue();
		
		return result;
	}

	@Override
	public void onAfterSave() {
		name.setEditable(false);
		test.setVisible(true);
	}

}
