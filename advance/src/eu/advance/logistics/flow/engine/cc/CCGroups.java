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

import hu.akarnokd.reactive4java.base.Action0;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Toolkit;
import java.awt.event.WindowEvent;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.AbstractTableModel;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import edu.umd.cs.findbugs.annotations.NonNull;
import eu.advance.logistics.flow.engine.api.ds.AdvanceDataStore;
import eu.advance.logistics.flow.engine.api.ds.AdvanceNotificationGroupType;

/**
 * The Notification groups editor.
 * @author akarnokd, 2011.10.13.
 */
public class CCGroups extends JFrame {
	/** */
	private static final long serialVersionUID = 5984112593977949929L;
	/** The label manager. */
	protected final LabelManager labels;
	/** The split pane. */
	protected JSplitPane split;
	/** The group types. */
	protected JComboBox groupTypes;
	/** The notification groups. */
	protected final Map<AdvanceNotificationGroupType, Map<String, Collection<String>>> map = Maps.newHashMap();
	/** The current ng type. */
	protected AdvanceNotificationGroupType currentType;
	/** The list of groups. */
	protected final List<String> groupList = Lists.newArrayList();
	/** The current group. */
	protected String currentGroup;
	/** The list of contacts. */
	protected final List<String> contactList = Lists.newArrayList();
	/** Refresh. */
	protected JButton refresh;
	/** Save. */
	protected JButton save;
	/** Save and Close. */
	protected JButton saveAndClose;
	/** Close. */
	protected JButton close;
	/** The datastore. */
	protected final AdvanceDataStore datastore;
	/** The current group. */
	protected JLabel currentGroupLabel;
	/** The engine info panel. */
	protected EngineInfoPanel engineInfo;
	/** The contacts panel. */
	private ListingPanel contactsPanel;
	/** The groups panel. */
	private ListingPanel groupsPanel;
	/**
	 * Constructor. Creates the dialog.
	 * @param labels the label manager
	 * @param datastore the datastore
	 */
	public CCGroups(@NonNull final LabelManager labels, final AdvanceDataStore datastore) {
		this.labels = labels;
		this.datastore = datastore;
		setTitle(labels.get("Notification groups"));
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		
		Container c = getContentPane();
		GroupLayout gl = new GroupLayout(c);
		c.setLayout(gl);
		gl.setAutoCreateContainerGaps(true);
		gl.setAutoCreateGaps(true);
		
		JLabel typeLabel = new JLabel(labels.get("Group type:"));
		split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, createGroups(), createContacts());
		groupTypes = new JComboBox(AdvanceNotificationGroupType.values());
		
		groupTypes.addActionListener(GUIUtils.createFromMethod(this, "doGroupTypeChange"));
		
		refresh = new JButton(labels.get("Refresh"));
		refresh.addActionListener(GUIUtils.createFromMethod(this, "refresh"));
		save = new JButton(labels.get("Save"));
		save.addActionListener(GUIUtils.createFromMethod(this, "doSave"));
		saveAndClose = new JButton(labels.get("Save & Close"));
		saveAndClose.addActionListener(GUIUtils.createFromMethod(this, "doSaveAndClose"));
		close = new JButton(labels.get("Close"));
		close.addActionListener(GUIUtils.createFromMethod(this, "close"));
		
		engineInfo = new EngineInfoPanel(labels);
		
		gl.setHorizontalGroup(
			gl.createParallelGroup(Alignment.CENTER)
			.addComponent(engineInfo)
			.addGroup(
				gl.createSequentialGroup()
				.addComponent(typeLabel)
				.addComponent(groupTypes, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
			)
			.addComponent(split)
			.addGroup(
				gl.createSequentialGroup()
				.addComponent(refresh)
				.addGap(20)
				.addComponent(save)
				.addComponent(saveAndClose)
				.addGap(20)
				.addComponent(close)
			)
		);
		gl.setVerticalGroup(
			gl.createSequentialGroup()
			.addComponent(engineInfo)
			.addGroup(
				gl.createParallelGroup(Alignment.BASELINE)
				.addComponent(typeLabel)
				.addComponent(groupTypes, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
			)
			.addComponent(split)
			.addGroup(
				gl.createParallelGroup(Alignment.BASELINE)
				.addComponent(refresh)
				.addComponent(save)
				.addComponent(saveAndClose)
				.addComponent(close)
			)
		);
	}
	/** The group type change event. */
	protected void doGroupTypeChange() {
		groupList.clear();
		Map<String, Collection<String>> map2 = map.get(getGroupType());
		if (map2 != null) {
			groupList.addAll(map2.keySet());
		}
		contactList.clear();
		groupsPanel.model.fireTableDataChanged();
		contactsPanel.model.fireTableDataChanged();
		currentGroup = null;
		currentGroupLabel.setText("-");
	}
	/**
	 * A listings panel.
	 * @author akarnokd, 2011.10.14.
	 */
	protected class ListingPanel extends JPanel {
		/** */
		private static final long serialVersionUID = 523876521133218132L;
		/** The group filter. */
		protected JTextField filter;
		/** The group table. */
		protected JTable table;
		/** The group model. */
		protected AbstractTableModel model;
		/** The add button. */
		protected JButton add;
		/** The delete button. */
		protected JButton delete;
		/**
		 * Create the panel GUI.
		 * @param model the table model
		 */
		public ListingPanel(AbstractTableModel model) {
			GroupLayout gl = new GroupLayout(this);
			setLayout(gl);
			gl.setAutoCreateContainerGaps(true);
			gl.setAutoCreateGaps(true);
			
			this.model = model; 
			table = new JTable(model);
			table.setAutoCreateRowSorter(true);
			
			JScrollPane sp = new JScrollPane(table);
			
			JLabel filterLabel = new JLabel(labels.get("Filter:"));
			filter = new JTextField();
			
			add = new JButton(labels.get("Add"));
			delete = new JButton(labels.get("Delete"));
			
			final JLabel records = new JLabel(labels.format("Records: %d", 0));
			
			model.addTableModelListener(new TableModelListener() {
				@Override
				public void tableChanged(TableModelEvent e) {
					records.setText(labels.format("Records: %d", table.getRowCount()));
				}
			});
			
			gl.setHorizontalGroup(
				gl.createParallelGroup(Alignment.CENTER)
				.addGroup(
					gl.createSequentialGroup()
					.addComponent(filterLabel)
					.addComponent(filter)
				)
				.addComponent(sp, 0, 250, Short.MAX_VALUE)
				.addComponent(records)
				.addGroup(
					gl.createSequentialGroup()
					.addComponent(add)
					.addComponent(delete)
				)
			);
			gl.setVerticalGroup(
				gl.createSequentialGroup()
				.addGroup(
					gl.createParallelGroup(Alignment.BASELINE)
					.addComponent(filterLabel)
					.addComponent(filter)
				)
				.addComponent(sp, 0, 250, Short.MAX_VALUE)
				.addComponent(records)
				.addGroup(
					gl.createParallelGroup(Alignment.BASELINE)
					.addComponent(add)
					.addComponent(delete)
				)
			);
		}
	}
	/** @return create the groups panel .*/
	JPanel createGroups() {
		groupsPanel = new ListingPanel(
			new AbstractTableModel() {
				/** */
				private static final long serialVersionUID = -7043791737237275469L;
	
				@Override
				public Object getValueAt(int rowIndex, int columnIndex) {
					AdvanceNotificationGroupType gt = (AdvanceNotificationGroupType)groupTypes.getSelectedItem();
					if (columnIndex == 0) {
						return groupList.get(rowIndex);
					}
					Map<String, Collection<String>> c = map.get(gt);
					if (c != null) {
						Collection<String> c2 = c.get(groupList.get(rowIndex));
						if (c2 != null) {
							return c2.size();
						}
					}
					return 0;
				}
				
				@Override
				public int getRowCount() {
					return groupList.size();
				}
				
				@Override
				public int getColumnCount() {
					return 2;
				}
				@Override
				public Class<?> getColumnClass(int columnIndex) {
					return (new Class<?>[] { String.class, Integer.class })[columnIndex];
				}
				@Override
				public String getColumnName(int column) {
					return (new String[] { labels.get("Group name"), labels.get("Contacts") })[column];
				}
			}
		);
		
		groupsPanel.table.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
			@Override
			public void valueChanged(ListSelectionEvent e) {
				int idx = groupsPanel.table.getSelectedRow();
				if (idx >= 0) {
					idx = groupsPanel.table.convertRowIndexToModel(idx);
					currentGroup = groupList.get(idx);
					currentGroupLabel.setText(currentGroup);
					contactList.clear();
					Map<String, Collection<String>> gs = map.get(getGroupType());
					if (gs != null) {
						Collection<String> cg = gs.get(currentGroup);
						if (cg != null) {
							contactList.addAll(cg);
						}
					}
					contactsPanel.model.fireTableDataChanged();
					contactsPanel.add.setEnabled(true);
					contactsPanel.delete.setEnabled(true);
				} else {
					contactsPanel.add.setEnabled(false);
					contactsPanel.delete.setEnabled(false);
				}
			}
		});
		
		groupsPanel.add.addActionListener(GUIUtils.createFromMethod(this, "doGroupsAdd"));
		groupsPanel.delete.addActionListener(GUIUtils.createFromMethod(this, "doGroupsDelete"));
		
		return groupsPanel;
	}
	/** Add a new group. */
	void doContactsAdd() {
		String s = JOptionPane.showInputDialog(this, labels.get("Enter contact information:"), 
				labels.get("Add contact"), JOptionPane.QUESTION_MESSAGE);
		if (s != null) {
			Map<String, Collection<String>> g = map.get(getGroupType());
			Collection<String> c = g.get(currentGroup);
			if (!c.contains(s)) {
				c.add(s);
				contactList.add(s);
				int idx = contactList.size() - 1;
				contactsPanel.model.fireTableDataChanged();
				idx = contactsPanel.table.convertRowIndexToView(idx);
				if (idx >= 0) {
					contactsPanel.table.getSelectionModel().addSelectionInterval(idx, idx);
				}
				int idx2 = groupList.indexOf(currentGroup);
				groupsPanel.model.fireTableRowsUpdated(idx2, idx2);
			}
		}
	}
	/** Delete groups. */
	void doContactsDelete() {
		if (JOptionPane.showConfirmDialog(this, labels.get("Are you sure?"), 
				labels.get("Delete contacts"), JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
			int[] sel = contactsPanel.table.getSelectedRows();
			Arrays.sort(sel);
			for (int i = sel.length - 1; i >= 0; i--) {
				int i2 = contactsPanel.table.convertRowIndexToModel(sel[i]);
				String k = contactList.remove(i2);
				map.get(getGroupType()).get(currentGroup).remove(k);
			}
			contactsPanel.model.fireTableDataChanged();
			int idx2 = groupList.indexOf(currentGroup);
			groupsPanel.model.fireTableRowsUpdated(idx2, idx2);
		}
	}
	/** Add a new group. */
	void doGroupsAdd() {
		String s = JOptionPane.showInputDialog(this, labels.get("Enter a group name:"), 
				labels.get("Add group"), JOptionPane.QUESTION_MESSAGE);
		if (s != null) {
			Map<String, Collection<String>> g = map.get(getGroupType());
			if (g == null) {
				g = Maps.newHashMap();
				map.put(getGroupType(), g);
			}
			if (!g.containsKey(s)) {
				g.put(s, Sets.<String>newHashSet());
				groupList.add(s);
				currentGroup = s;
				currentGroupLabel.setText(currentGroup);
				groupsPanel.model.fireTableDataChanged();
				groupsPanel.table.clearSelection();
				int idx = groupsPanel.table.convertRowIndexToView(groupList.size() - 1);
				if (idx >= 0) {
					groupsPanel.table.getSelectionModel().addSelectionInterval(idx, idx);
				}
			}
		}
	}
	/** Delete groups. */
	void doGroupsDelete() {
		if (JOptionPane.showConfirmDialog(this, labels.get("Are you sure?"), 
				labels.get("Delete groups"), JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
			int[] sel = groupsPanel.table.getSelectedRows();
			Arrays.sort(sel);
			for (int i = sel.length - 1; i >= 0; i--) {
				int i2 = groupsPanel.table.convertRowIndexToModel(sel[i]);
				map.get(getGroupType()).remove(groupList.get(i2));
				groupList.remove(i2);
			}
			groupsPanel.model.fireTableDataChanged();
			currentGroup = null;
			currentGroupLabel.setText("-");
			contactList.clear();
			contactsPanel.model.fireTableDataChanged();
		}
	}
	/** @return create the contacts. */
	JPanel createContacts() {
		JPanel p = new JPanel();
		p.setLayout(new BorderLayout());
		contactsPanel = new ListingPanel(
			new AbstractTableModel() {
				/** */
				private static final long serialVersionUID = -7043791737237275469L;

				@Override
				public Object getValueAt(int rowIndex, int columnIndex) {
					return contactList.get(rowIndex);
				}
				
				@Override
				public int getRowCount() {
					return contactList.size();
				}
				
				@Override
				public int getColumnCount() {
					return 1;
				}
				@Override
				public Class<?> getColumnClass(int columnIndex) {
					return String.class;
				}
				@Override
				public String getColumnName(int column) {
					return labels.get("Contact");
				}
			}
		);
		p.add(contactsPanel, BorderLayout.CENTER);
		
		currentGroupLabel = new JLabel("-");
		currentGroupLabel.setHorizontalAlignment(SwingConstants.CENTER);
		p.add(currentGroupLabel, BorderLayout.PAGE_START);
		
		contactsPanel.add.addActionListener(GUIUtils.createFromMethod(this, "doContactsAdd"));
		contactsPanel.delete.addActionListener(GUIUtils.createFromMethod(this, "doContactsDelete"));
		contactsPanel.add.setEnabled(false);
		contactsPanel.delete.setEnabled(false);
		
		return p;
	}
	/** Close the window. */
	public void close() {
		WindowEvent e = new WindowEvent(this, WindowEvent.WINDOW_CLOSING);
		Toolkit.getDefaultToolkit().getSystemEventQueue().postEvent(e);
	}
	/** Retrieve the current data. */
	public void refresh() {
		GUIUtils.getWorker(new WorkItem() {
			/** The exception. */
			Throwable t;
			/** The result. */
			Map<AdvanceNotificationGroupType, Map<String, Collection<String>>> ng;
			@Override
			public void run() {
				try {
					ng = datastore.queryNotificationGroups();
				} catch (Throwable t) {
					this.t = t;
				}
			}
			@Override
			public void done() {
				if (t != null) {
					GUIUtils.errorMessage(CCGroups.this, t);
				} else {
					currentGroupLabel.setText("-");
					groupList.clear();
					map.clear();
					map.putAll(ng);
					Map<String, Collection<String>> ge = ng.get(getGroupType());
					if (ge != null) {
						groupList.addAll(ge.keySet());
					}
					contactList.clear();
					groupsPanel.model.fireTableDataChanged();
					contactsPanel.model.fireTableDataChanged();
				}
				
			}
		}).execute();
	}
	/** @return get the group type. */
	public AdvanceNotificationGroupType getGroupType() {
		return (AdvanceNotificationGroupType)groupTypes.getSelectedItem();
	}
	/** Save the groups. */
	void doSave() {
		doSaveAction(null);
	}
	/** Save and close. */
	void doSaveAndClose() {
		doSaveAction(new Action0() {
			@Override
			public void invoke() {
				close();
			}
		});
	}
	/**
	 * The save routine.
	 * @param action the action to take on successful save
	 */
	void doSaveAction(final Action0 action) {
		final Map<AdvanceNotificationGroupType, Map<String, Collection<String>>> s = copy(map);
		GUIUtils.getWorker(new WorkItem() {
			Throwable t;
			@Override
			public void run() {
				try {
					datastore.updateNotificationGroups(s);
				} catch (Throwable t) {
					this.t = t;
				}
			}
			@Override
			public void done() {
				if (t != null) {
					GUIUtils.errorMessage(CCGroups.this, t);
				} else {
					if (action != null) {
						action.invoke();
					}
				}
			}
		}).execute();
	}
	/**
	 * Create a deep copy of the given map.
	 * @param source the source
	 * @return the copy
	 */
	Map<AdvanceNotificationGroupType, Map<String, Collection<String>>> copy(Map<AdvanceNotificationGroupType, Map<String, Collection<String>>> source) {
		Map<AdvanceNotificationGroupType, Map<String, Collection<String>>> result = Maps.newHashMap();
		for (Map.Entry<AdvanceNotificationGroupType, Map<String, Collection<String>>> e : source.entrySet()) {
			Map<String, Collection<String>> k = Maps.newHashMap();
			result.put(e.getKey(), k);
			for (Map.Entry<String, Collection<String>> e2 : e.getValue().entrySet()) {
				k.put(e2.getKey(), Sets.newHashSet(e2.getValue()));
			}
		}
		return result;
	}
}
