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
import eu.advance.logistics.flow.engine.api.ds.AdvanceJMSDrivers;
import eu.advance.logistics.flow.engine.api.ds.AdvanceJMSEndpoint;

/**
 * The JMS details.
 * @author akarnokd, 2011.10.13.
 */
public class CCJMSDetails extends JPanel implements
		CCLoadSave<AdvanceJMSEndpoint> {
	/** */
	private static final long serialVersionUID = -6973780683687929477L;
	/** Text field. */
	protected JTextField name;
	/** The available drivers. */
	protected JComboBox driver;
	/** Text field. */
	protected JTextField customDriver;
	/** The url. */
	protected JTextField url;
	/** Text field. */
	protected JTextField user;
	/** The password. */
	protected JPasswordField password;
	/** Text field. */
	protected JTextField queueManager;
	/** Text field. */
	protected JTextField queue;
	/** The pool size. */
	protected JFormattedTextField pool;
	/** The custom driver label. */
	protected JLabel customDriverLabel;
	/** The pest button. */
	protected JButton test;
	/**
	 * Construct the panel.
	 * @param labels the label manager
	 */
	public CCJMSDetails(@NonNull final LabelManager labels) {
		GroupLayout gl = new GroupLayout(this);
		setLayout(gl);
		gl.setAutoCreateGaps(true);
		
		name = new JTextField();
		driver = new JComboBox(AdvanceJMSDrivers.values());
		customDriver = new JTextField();
		url = new JTextField();
		user = new JTextField();
		password = new JPasswordField();
		queueManager = new JTextField();
		queue = new JTextField();
		pool = new JFormattedTextField(5);
		pool.setColumns(2);
		customDriverLabel = new JLabel();
		customDriverLabel.setVisible(driver.getSelectedItem() == AdvanceJMSDrivers.GENERIC);
		
		driver.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				AdvanceJMSDrivers si = (AdvanceJMSDrivers)driver.getSelectedItem();
				url.setText(si.urlTemplate);
				customDriver.setVisible(si == AdvanceJMSDrivers.GENERIC);
				customDriverLabel.setVisible(si == AdvanceJMSDrivers.GENERIC);
			}
		});
		
		test = new JButton(labels.get("Test"));
		test.setVisible(false);

		Pair<Group, Group> g = GUIUtils.createForm(gl, 2,
			labels.get("Name:"), name,
			labels.get("Driver:"), driver,
			customDriverLabel, customDriver,
			labels.get("URL:"), url,
			labels.get("User:"), user,
			labels.get("Password:"), password,
			labels.get("Queue manager:"), queueManager,
			labels.get("Queue:"), queue,
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
	@Override
	public void load(AdvanceJMSEndpoint value) {
		name.setText(value.name);
		name.setEditable(false);
		boolean found = false;
		for (AdvanceJMSDrivers d : AdvanceJMSDrivers.values()) {
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
		queueManager.setText(value.queueManager);
		queue.setText(value.queue);
		pool.setValue(value.poolSize);
		test.setVisible(true);
	}

	@Override
	public AdvanceJMSEndpoint save() {
		AdvanceJMSEndpoint result = new AdvanceJMSEndpoint();
		
		result.name = name.getText();
		
		if (result.name.isEmpty()) {
			GUIUtils.errorMessage(this, "Please enter a name!");
			return null;
		}
		
		AdvanceJMSDrivers d = (AdvanceJMSDrivers)driver.getSelectedItem();
		if (d == AdvanceJMSDrivers.GENERIC) {
			if (customDriver.getText().isEmpty()) {
				GUIUtils.errorMessage(this, "Please enter the fully qualified class name of the driver!");
				return null;
			}
			result.driver = customDriver.getText();
		} else {
			result.driver = d.driverClass;
		}
		result.url = url.getText();
		if (result.url.isEmpty()) {
			GUIUtils.errorMessage(this, "Please enter URL!");
			return null;
		}
		result.user = user.getText();
		char[] p = password.getPassword();
		if (p != null && p.length > 0) {
			result.password(p);
		}
		result.queueManager = queueManager.getText();
		result.queue = queueManager.getText();
		result.poolSize = (Integer)pool.getValue();
		
		return result;
	}

	@Override
	public void onAfterSave() {
		name.setEditable(false);
		test.setVisible(true);
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
