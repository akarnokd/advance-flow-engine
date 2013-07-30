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

import hu.akarnokd.reactive4java.base.Func1;
import hu.akarnokd.reactive4java.base.Pair;
import hu.akarnokd.reactive4java.interactive.Interactive;

import java.util.EnumSet;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.AbstractTableModel;

import com.google.common.collect.Lists;

import edu.umd.cs.findbugs.annotations.NonNull;
import eu.advance.logistics.flow.engine.api.ds.AdvanceKeyStore;
import eu.advance.logistics.flow.engine.api.ds.AdvanceRealm;
import eu.advance.logistics.flow.engine.api.ds.AdvanceUser;
import eu.advance.logistics.flow.engine.api.ds.AdvanceUserRealmRights;
import eu.advance.logistics.flow.engine.api.ds.AdvanceUserRights;

/**
 * The user details panel.
 * @author akarnokd, 2011.10.12.
 */
public class CCUserDetails extends JTabbedPane implements CCLoadSave<AdvanceUser> {
	/** */
	private static final long serialVersionUID = 396208563990242275L;
	/** The login panel. */
	public LoginTypePanel login;
	/** Is the user enabled? */
	protected JCheckBox enabled;
	/** The name. */
	protected JTextField name;
	/** The email address. */
	protected JTextField email;
	/** The pager. */
	protected JTextField pager;
	/** The sms. */
	protected JTextField sms;
	/** The date format. */
	protected JComboBox<String> dateFormat;
	/** The date-time format. */
	protected JComboBox<String> dateTimeFormat;
	/** The number format. */
	protected JComboBox<String> numberFormat;
	/** The thousand separator. */
	protected JTextField thousandSeparator;
	/** The decimal separator. */
	protected JTextField decimalSeparator;
	/** The rights table. */
	protected JTable rights;
	/** The realms table. */
	protected JTable realms;
	/**
	 * The available rights set.
	 */
	protected EnumSet<AdvanceUserRights> rightsSet = EnumSet.noneOf(AdvanceUserRights.class);
	/** The rights table model. */
	protected AbstractTableModel rightsModel;
	/** The realm rights model. */
	protected AbstractTableModel realmsModel;
	/** The list of realms and rights. */
	protected final List<Pair<String, EnumSet<AdvanceUserRealmRights>>> realmRights = Lists.newArrayList();
	/** The label manager. */
	protected final LabelManager labels;
	/** Filter rights. */
	protected JTextField filterRights;
	/** The number of rows in the rights. */
	protected JLabel rightsRows;
	/** Filter realms. */
	protected JTextField filterRealms;
	/** The number of rows in the realms. */
	protected JLabel realmsRows;
	/**
	 * Create the GUI panel.
	 * @param labels the label manager
	 */
	public CCUserDetails(@NonNull final LabelManager labels) {
		super();
		this.labels = labels;
		
		addTab(labels.get("General"), createGeneral());
		
		addTab(labels.get("Rights"), createRights());
		
		addTab(labels.get("Realms"), createRealms());
		
	}
	/** @return Create the rights panel. */
	protected JPanel createRights() {
		rightsModel = new AbstractTableModel() {
			/** */
			private static final long serialVersionUID = -2358536983820239947L;
			/** The right count. */
			AdvanceUserRights[] rows = AdvanceUserRights.values();
			@Override
			public int getColumnCount() {
				return 2;
			}
			@Override
			public int getRowCount() {
				return rows.length;
			}
			@Override
			public Object getValueAt(int rowIndex, int columnIndex) {
				switch (columnIndex) {
				case 0:
					return labels.get(rows[rowIndex].toString());
				case 1:
					return rightsSet.contains(rows[rowIndex]);
				default:
					return null;
				}
			}
			@Override
			public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
				if (aValue == Boolean.TRUE) {
					rightsSet.add(rows[rowIndex]);
				} else {
					rightsSet.remove(rows[rowIndex]);
				}
			}
			@Override
			public Class<?> getColumnClass(int columnIndex) {
				switch (columnIndex) {
				case 0:
					return String.class;
				case 1:
					return Boolean.class;
				default:
					return null;
				}
			};
			@Override
			public String getColumnName(int column) {
				switch (column) {
				case 0:
					return labels.get("Right");
				case 1:
					return labels.get("Granted");
				default:
					return null;
				}
			}
			@Override
			public boolean isCellEditable(int rowIndex, int columnIndex) {
				return columnIndex == 1;
			}
		};
		rights = new JTable(rightsModel);
		rights.setAutoCreateRowSorter(true);
		JScrollPane rightsScroll = new JScrollPane(rights);

		JPanel panel = new JPanel();
		
		GroupLayout gl = new GroupLayout(panel);
		panel.setLayout(gl);
		gl.setAutoCreateContainerGaps(true);
		gl.setAutoCreateGaps(true);
		
		JLabel filterLabel = new JLabel(labels.get("Filter:"));
		
		filterRights = new JTextField();
		rightsRows = new JLabel("Records: " + rights.getRowCount());
		
		gl.setHorizontalGroup(
			gl.createParallelGroup(Alignment.CENTER)
			.addGroup(
				gl.createSequentialGroup()
				.addComponent(filterLabel)
				.addComponent(filterRights)
			)
			.addComponent(rightsScroll)
			.addComponent(rightsRows)
		);
	
		gl.setVerticalGroup(
			gl.createSequentialGroup()
			.addGroup(
				gl.createParallelGroup(Alignment.BASELINE)
				.addComponent(filterLabel)
				.addComponent(filterRights)
			)
			.addComponent(rightsScroll, 100, 300, Short.MAX_VALUE)
			.addComponent(rightsRows)
		);
		GUIUtils.autoResizeColWidth(rights, rightsModel);
		return panel;
	}
	/** @return create the general panel. */
	protected JPanel createGeneral() {
		JPanel general = new JPanel();
		GroupLayout gl = new GroupLayout(general);
		general.setLayout(gl);
		gl.setAutoCreateContainerGaps(true);
		gl.setAutoCreateGaps(true);

		login = new LoginTypePanel(labels);
		login.showNone(false);
		login.showUserName(false);
		login.showKeyPassword(false);
		login.setBorder(BorderFactory.createTitledBorder("Login authentication"));
		
		name = new JTextField();
		email = new JTextField();
		pager = new JTextField();
		sms = new JTextField();
		dateFormat = new JComboBox<>(new String[] { 
				"yyyy-MM-dd", 
				"yyyy/MM/dd", 
				"MM/dd/YYYY", 
				"dd/MM/YYYY" 
		});
		dateTimeFormat = new JComboBox<>(new String[] {
			"yyyy-MM-dd HH:mm:ss",
			"yyyy-MM-dd HH:mm",
			"yyyy/MM/dd HH:mm:ss", 
			"yyyy/MM/dd HH:mm", 
			"MM/dd/YYYY HH:mm:ss", 
			"MM/dd/YYYY HH:mm", 
			"dd/MM/YYYY HH:mm:ss", 
			"dd/MM/YYYY HH:mm:ss" 
		});
		numberFormat = new JComboBox<>(new String[] {
			"#,###",
			"#,###.#",
			"#,###.##",
			"#,###.###",
			"#,###.0",
			"#,###.00",
			"#,###.000",
			"###",
		});
		thousandSeparator = new JTextField(1);
		decimalSeparator = new JTextField(1);

		JLabel nameLabel = new JLabel(labels.get("Name:"));
		JLabel emailLabel = new JLabel(labels.get("E-mail:"));
		JLabel pagerLabel = new JLabel(labels.get("Pager:"));
		JLabel smsLabel = new JLabel(labels.get("SMS:"));
		JLabel dateFormatLabel = new JLabel(labels.get("Date format:"));
		JLabel dateTimeFormatLabel = new JLabel(labels.get("Date and time format:"));
		JLabel numberFormatLabel = new JLabel(labels.get("Number format:"));
		JLabel thousandLabel = new JLabel(labels.get("Thousand separator:"));
		JLabel decimalLabel = new JLabel(labels.get("Decimal separator:"));
		
		enabled = new JCheckBox(labels.get("Enabled"));
		
		gl.setHorizontalGroup(
			gl.createParallelGroup()
			.addComponent(enabled)
			.addGroup(
				gl.createSequentialGroup()
				.addGroup(
					gl.createParallelGroup()
					.addComponent(nameLabel)
					.addComponent(emailLabel)
					.addComponent(pagerLabel)
					.addComponent(smsLabel)
					.addComponent(dateFormatLabel)
					.addComponent(dateTimeFormatLabel)
					.addComponent(numberFormatLabel)
					.addComponent(thousandLabel)
					.addComponent(decimalLabel)
				)
				.addGroup(
					gl.createParallelGroup()
					.addComponent(name)
					.addComponent(email)
					.addComponent(pager)
					.addComponent(sms)
					.addComponent(dateFormat, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
					.addComponent(dateTimeFormat, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
					.addComponent(numberFormat, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
					.addComponent(thousandSeparator, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
					.addComponent(decimalSeparator, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
				)
			)
			.addComponent(login)
		);
		gl.setVerticalGroup(
			gl.createSequentialGroup()
			.addComponent(enabled)
			.addGroup(
				gl.createParallelGroup(Alignment.BASELINE)
				.addComponent(nameLabel)
				.addComponent(name, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
			)
			.addGroup(
				gl.createParallelGroup(Alignment.BASELINE)
				.addComponent(emailLabel)
				.addComponent(email, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
			)
			.addGroup(
				gl.createParallelGroup(Alignment.BASELINE)
				.addComponent(pagerLabel)
				.addComponent(pager, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
			)
			.addGroup(
				gl.createParallelGroup(Alignment.BASELINE)
				.addComponent(smsLabel)
				.addComponent(sms, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
			)
			.addGroup(
				gl.createParallelGroup(Alignment.BASELINE)
				.addComponent(dateFormatLabel)
				.addComponent(dateFormat, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
			)
			.addGroup(
				gl.createParallelGroup(Alignment.BASELINE)
				.addComponent(dateTimeFormatLabel)
				.addComponent(dateTimeFormat, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
			)
			.addGroup(
				gl.createParallelGroup(Alignment.BASELINE)
				.addComponent(numberFormatLabel)
				.addComponent(numberFormat, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
			)
			.addGroup(
				gl.createParallelGroup(Alignment.BASELINE)
				.addComponent(thousandLabel)
				.addComponent(thousandSeparator, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
			)
			.addGroup(
				gl.createParallelGroup(Alignment.BASELINE)
				.addComponent(decimalLabel)
				.addComponent(decimalSeparator, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
			)
			.addComponent(login)
		);
		
		return general;
	}
	/**
	 * Set the list of realms.
	 * @param realms the realms
	 */
	public void setRealms(Iterable<AdvanceRealm> realms) {
		realmRights.clear();
		for (AdvanceRealm r : realms) {
			realmRights.add(Pair.of(r.name, EnumSet.noneOf(AdvanceUserRealmRights.class)));
		}
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
	 * Load values from the supplied object.
	 * @param u the user object
	 */
	@Override
	public void load(AdvanceUser u) {
		// TODO
		enabled.setSelected(u.enabled);
		name.setText(u.name);
		name.setEditable(false);
		email.setText(u.email);
		pager.setText(u.pager);
		sms.setText(u.sms);
		dateFormat.setSelectedItem(u.dateFormat);
		dateTimeFormat.setSelectedItem(u.dateTimeFormat);
		numberFormat.setSelectedItem(u.numberFormat);
		thousandSeparator.setText("" + u.thousandSeparator);
		decimalSeparator.setText("" + u.decimalSeparator);
		
		if (u.passwordLogin) {
			login.viaPassword.setSelected(true);
		} else {
			login.viaCertificate.setSelected(true);
		}

		login.setUserPassword(null);
		login.setKeyStore(u.keyStore);
		login.alias.setText(u.keyAlias);

		rightsSet.addAll(u.rights);
		
		for (Pair<String, EnumSet<AdvanceUserRealmRights>> r : realmRights) {
			r.second.addAll(u.realmRights.get(r.first));
		}
		
		rightsModel.fireTableDataChanged();
		
		realmsModel.fireTableDataChanged();
		
		GUIUtils.autoResizeColWidth(rights, rightsModel);
		GUIUtils.autoResizeColWidth(realms, realmsModel);
	}
	/** @return save the dialog values into the user object. */
	@Override
	public AdvanceUser save() {
		AdvanceUser u = new AdvanceUser();
		u.enabled = enabled.isSelected();
		u.name = name.getText();
		if (u.name == null || u.name.isEmpty()) {
			GUIUtils.errorMessage(this, labels.get("Please enter a user name!"));
			name.requestFocus();
			return null;
		}
		u.email = email.getText();
		u.pager = pager.getText();
		u.sms = sms.getText();
		u.dateFormat = (String)dateFormat.getSelectedItem();
		if (u.dateFormat == null || u.dateFormat.isEmpty()) {
			GUIUtils.errorMessage(this, labels.get("Please select a date format!"));
			dateFormat.requestFocus();
			return null;
		}
		u.dateTimeFormat = (String)dateTimeFormat.getSelectedItem();
		if (u.dateTimeFormat == null || u.dateTimeFormat.isEmpty()) {
			GUIUtils.errorMessage(this, labels.get("Please select a date and time format!"));
			dateTimeFormat.requestFocus();
			return null;
		}
		u.numberFormat = (String)numberFormat.getSelectedItem();
		if (u.numberFormat == null || u.numberFormat.isEmpty()) {
			GUIUtils.errorMessage(this, labels.get("Please select a number format!"));
			numberFormat.requestFocus();
			return null;
		}
		String s = thousandSeparator.getText();
		if (s.length() != 1) {
			GUIUtils.errorMessage(this, labels.get("Please enter a single thousand separator character!"));
			thousandSeparator.requestFocus();
			return null;
		}
		u.thousandSeparator = s.charAt(0);
		s = decimalSeparator.getText();
		if (s.length() != 1) {
			GUIUtils.errorMessage(this, labels.get("Please enter a single decimal separator character!"));
			decimalSeparator.requestFocus();
			return null;
		}
		u.decimalSeparator = s.charAt(0);
		
		if (login.viaPassword.isSelected()) {
			u.passwordLogin = true;
			char[] p = login.getUserPassword();
			if (p != null && p.length > 0) {
				u.password(p);
			}
		} else {
			u.keyStore = login.getKeyStore();
			u.keyAlias = login.getAlias();
		}
		
		u.rights.addAll(rightsSet);
		for (Pair<String, EnumSet<AdvanceUserRealmRights>> e : realmRights) {
			u.realmRights.putAll(e.first, e.second);
		}
		
		return u;
	}
	@Override
	public void onAfterSave() {
		name.setEditable(false);
	}
	/** @return create a realms panel. */
	protected JPanel createRealms() {
		realmsModel = new AbstractTableModel() {
			/** */
			private static final long serialVersionUID = -218253656067861921L;
			/** The columns. */
			AdvanceUserRealmRights[] cols = AdvanceUserRealmRights.values();
			@Override
			public Object getValueAt(int rowIndex, int columnIndex) {
				if (columnIndex == 0) {
					return realmRights.get(rowIndex).first;
				}
				return realmRights.get(rowIndex).second.contains(cols[columnIndex - 1]);
			}
			@Override
			public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
				if (aValue == Boolean.TRUE) {
					realmRights.get(rowIndex).second.add(cols[columnIndex - 1]);
				} else {
					realmRights.get(rowIndex).second.remove(cols[columnIndex - 1]);
				}
			}
			
			@Override
			public int getRowCount() {
				return realmRights.size();
			}
			
			@Override
			public int getColumnCount() {
				return 1 + cols.length;
			}
			@Override
			public Class<?> getColumnClass(int columnIndex) {
				if (columnIndex == 0) {
					return String.class;
				}
				return Boolean.class;
			}
			@Override
			public String getColumnName(int column) {
				if (column == 0) {
					return labels.get("Realm");
				}
				return labels.get(cols[column - 1].toString());
			}
			@Override
			public boolean isCellEditable(int rowIndex, int columnIndex) {
				return columnIndex > 0;
			}
		};
		
		realms = new JTable(realmsModel);
		
		JScrollPane realmsScroll = new JScrollPane(realms);

		JPanel panel = new JPanel();
		
		GroupLayout gl = new GroupLayout(panel);
		panel.setLayout(gl);
		gl.setAutoCreateContainerGaps(true);
		gl.setAutoCreateGaps(true);
		
		JLabel filterLabel = new JLabel(labels.get("Filter:"));
		
		filterRealms = new JTextField();
		realmsRows = new JLabel("Records: " + realms.getRowCount());
		
		gl.setHorizontalGroup(
			gl.createParallelGroup(Alignment.CENTER)
			.addGroup(
				gl.createSequentialGroup()
				.addComponent(filterLabel)
				.addComponent(filterRealms)
			)
			.addComponent(realmsScroll)
			.addComponent(realmsRows)
		);
	
		gl.setVerticalGroup(
			gl.createSequentialGroup()
			.addGroup(
				gl.createParallelGroup(Alignment.BASELINE)
				.addComponent(filterLabel)
				.addComponent(filterRealms)
			)
			.addComponent(realmsScroll, 100, 300, Short.MAX_VALUE)
			.addComponent(realmsRows)
		);
		
		return panel;
	}
}
