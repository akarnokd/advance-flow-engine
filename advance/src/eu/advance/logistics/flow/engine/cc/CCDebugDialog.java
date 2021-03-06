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
import hu.akarnokd.reactive4java.base.Observer;
import hu.akarnokd.reactive4java.base.Option;
import hu.akarnokd.reactive4java.base.Pair;
import hu.akarnokd.reactive4java.base.Scheduler;
import hu.akarnokd.reactive4java.interactive.Interactive;
import hu.akarnokd.reactive4java.reactive.Reactive;
import hu.akarnokd.reactive4java.scheduler.CachedThreadPoolScheduler;
import hu.akarnokd.reactive4java.swing.DefaultEdtScheduler;
import hu.akarnokd.utils.lang.Tuple3;
import hu.akarnokd.utils.xml.XNElement;
import hu.akarnokd.utils.xml.XNSerializables;

import java.awt.Component;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Date;
import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.DefaultComboBoxModel;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.GroupLayout.Group;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTable;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import edu.umd.cs.findbugs.annotations.NonNull;
import eu.advance.logistics.flow.engine.api.AdvanceEngineControl;
import eu.advance.logistics.flow.engine.api.ds.AdvanceRealm;
import eu.advance.logistics.flow.engine.model.fd.AdvanceBlockParameterDescription;
import eu.advance.logistics.flow.engine.model.fd.AdvanceBlockReference;
import eu.advance.logistics.flow.engine.model.fd.AdvanceCompositeBlock;
import eu.advance.logistics.flow.engine.runtime.BlockDiagnostic;
import eu.advance.logistics.flow.engine.runtime.BlockRegistryEntry;
import eu.advance.logistics.flow.engine.runtime.PortDiagnostic;

/**
 * Debug flow dialog.
 * @author akarnokd, 2011.10.21.
 */
public class CCDebugDialog extends JFrame {
	/** */
	private static final long serialVersionUID = -8697812222150928984L;
	/** The logger. */
	protected static final Logger LOG = LoggerFactory.getLogger(CCDebugDialog.class);
	/** The engine information panel. */
	public EngineInfoPanel engineInfo;
	/** The label manager. */
	protected final LabelManager labels;
	/** The number of records. */
	protected JLabel records;
	/** The debug table. */
	protected JTable table;
	/** The debug model. */
	protected AbstractTableModel model;
	/** Apply filter button. */
	protected JButton applyFilter;
	/** Clear filter button. */
	protected JButton clearFilter;
	/** The filter function. */
	protected JComboBox<String> filter;
	/** The maximum number of rows. */
	protected JFormattedTextField rowLimit;
	/** The realm list. */
	protected JComboBox<String> realms;
	/** The block list. */
	protected JComboBox<String> blocks;
	/** The port list. */
	protected JComboBox<String> ports;
	/** Refresh realms. */
	protected JButton refreshRealms;
	/** Watch at block level. */
	protected JButton watchBlock;
	/** Watch at port level. */
	protected JButton watchPort;
	/** The actions. */
	protected JButton actions;
	/** The engine. */
	protected final AdvanceEngineControl engine;
	/** The rows. */
	final List<CCDebugRow> rows = Lists.newArrayList();
	/** The block types. */
	final Map<String, BlockRegistryEntry> blockTypes = Maps.newHashMap();
	/** The action menu. */
	private JPopupMenu actionMenu;
	/** List of active watchers. */
	final List<CCWatchSettings> watchers = Lists.newArrayList();
	/**
	 * The list of components which must be enabled when there is selection and disabled otherwise.
	 */
	private final List<Component> selectionDependant = Lists.newArrayList();
	/** The scheduler for blocking watches. */
	protected final Scheduler scheduler;
	/** The EDT. */
	protected final Scheduler edt = new DefaultEdtScheduler();
	/** The block references. */
	protected final List<AdvanceBlockReference> blockRefs = Lists.newArrayList();
	/** The block parameter description. */
	protected final List<AdvanceBlockParameterDescription> portList = Lists.newArrayList();
	/** The last directory. */
	protected File lastDir = new File(".");
	/** 
	 * Constructs the GUI dialog.
	 * @param labels the label manager
	 * @param engine the flow engine controls
	 */
	public CCDebugDialog(@NonNull final LabelManager labels, @NonNull final AdvanceEngineControl engine) {
		this.labels = labels;
		this.engine = engine;
		setTitle(labels.get("Debug blocks and ports"));
		
		scheduler = new CachedThreadPoolScheduler();
		
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		
		Container c = getContentPane();
		GroupLayout gl = new GroupLayout(c);
		c.setLayout(gl);
		gl.setAutoCreateContainerGaps(true);
		gl.setAutoCreateGaps(true);
		
		records = new JLabel(labels.format("Records %d / %d", 0, 0));
		JSeparator topSeparator = new JSeparator(JSeparator.HORIZONTAL);
		realms = new JComboBox<>();
		realms.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				String realm = (String)realms.getSelectedItem();
				if (realm != null && !realm.isEmpty()) {
					getFlow(realm);
				}				
			}
		});
		blocks = new JComboBox<>();
		blocks.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				String block = (String)blocks.getSelectedItem();
				if (block != null && !block.isEmpty() && blocks.getSelectedIndex() >= 0) {
					AdvanceBlockReference r = blockRefs.get(blocks.getSelectedIndex());
					BlockRegistryEntry re = blockTypes.get(r.type);
					if (re != null) {
						DefaultComboBoxModel<String> pm = new DefaultComboBoxModel<>();
						portList.clear();
						for (AdvanceBlockParameterDescription p : re.inputs.values()) {
							portList.add(p);
							pm.addElement(p.id + "<- " + p.type);
						}
						for (AdvanceBlockParameterDescription p : re.outputs.values()) {
							portList.add(p);
							pm.addElement(p.id + "-> " + p.type);
						}
						ports.setModel(pm);
					}
				}
			}
		});
		ports = new JComboBox<>();
		
		filter = new JComboBox<>();
		filter.setEditable(true);
		applyFilter = new JButton(new ImageIcon(getClass().getResource("filter.png")));
		clearFilter = new JButton(new ImageIcon(getClass().getResource("clear.png")));
		refreshRealms = new JButton(labels.get("Refresh"));
		refreshRealms.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				getRealmsAndBlocks();
			}
		});
		watchBlock = new JButton(labels.get("Watch"));
		watchBlock.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				String realm = (String)realms.getSelectedItem();
				if (realm == null || realm.isEmpty()) {
					GUIUtils.errorMessage(CCDebugDialog.this, labels.get("Please select a realm"));
					return;
				}
				String block = (String)blocks.getSelectedItem();
				if (block == null || block.isEmpty()) {
					GUIUtils.errorMessage(CCDebugDialog.this, labels.get("Please select a block"));
					return;
				}
				watchBlock(realm, blockRefs.get(blocks.getSelectedIndex()));
			}
		});
		watchPort = new JButton(labels.get("Watch"));
		watchPort.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				String realm = (String)realms.getSelectedItem();
				if (realm == null || realm.isEmpty()) {
					GUIUtils.errorMessage(CCDebugDialog.this, labels.get("Please select a realm"));
					return;
				}
				String block = (String)blocks.getSelectedItem();
				if (block == null || block.isEmpty()) {
					GUIUtils.errorMessage(CCDebugDialog.this, labels.get("Please select a block"));
					return;
				}
				int index = ports.getSelectedIndex();
				if (index < 0) {
					GUIUtils.errorMessage(CCDebugDialog.this, labels.get("Please select a port"));
					return;
				}
				watchPort(realm, blockRefs.get(blocks.getSelectedIndex()), portList.get(index).id);
			}
		});
		
		rowLimit = new JFormattedTextField(1024);
		rowLimit.setColumns(5);
		JLabel rowLimitLabel = new JLabel(labels.get("Row limit:"));
		
		JSeparator middleSeparator = new JSeparator(JSeparator.HORIZONTAL);
		
		actionMenu = new JPopupMenu();

		actions = new JButton(labels.get("Actions"), new ImageIcon(getClass().getResource("down.png")));
		actions.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				actionMenu.show(actions, 0, actions.getHeight());
			}
		});
		
		model = new AbstractTableModel() {
			/** */
			private static final long serialVersionUID = -2712438269456984029L;
			/** Column names. */
			final String[] cols = { "Realm", "Block", "Port", "Timestamp", "Value" };
			/** Column classes. */
			final Class<?>[] classes = { String.class, String.class, String.class, Date.class, String.class };
			@Override
			public Object getValueAt(int rowIndex, int columnIndex) {
				CCDebugRow r = rows.get(rowIndex);
				switch (columnIndex) {
				case 0:
					return r.watch.realm;
				case 1:
					return r.watch.block;
				case 2:
					return r.watch.port;
				case 3:
					return r.timestamp;
				case 4:
					return Option.isError(r.value) ? Option.getError(r.value).toString() : (Option.isNone(r.value) ? "" : r.value.value().toString());
				default:
					return null;
				}
			}
			
			@Override
			public int getRowCount() {
				return rows.size();
			}
			
			@Override
			public int getColumnCount() {
				return cols.length;
			}
			@Override
			public Class<?> getColumnClass(int columnIndex) {
				return classes[columnIndex];
			}
			@Override
			public String getColumnName(int column) {
				return labels.get(cols[column]);
			}
		};
		model.addTableModelListener(new TableModelListener() {
			@Override
			public void tableChanged(TableModelEvent e) {
				records.setText(labels.format("Records %d / %d", table.getRowCount(), rows.size()));
			}
		});
		
		table = new JTable(model);
		table.setAutoCreateRowSorter(true);
		
		table.getColumnModel().getColumn(3).setCellRenderer(new DefaultTableCellRenderer() {
			/**
			 * 
			 */
			private static final long serialVersionUID = -7615930689752608644L;

			@Override
			public Component getTableCellRendererComponent(JTable table,
					Object value, boolean isSelected, boolean hasFocus,
					int row, int column) {
				JLabel label = (JLabel)super.getTableCellRendererComponent(table, value, isSelected, hasFocus,
						row, column);
				
				label.setText(value.toString());
				return label;
			}
		});
		table.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if (e.getClickCount() > 1) {
					viewData();
				}
			}
		});
		
		engineInfo = new EngineInfoPanel(labels);
		
		JScrollPane scroll = new JScrollPane(table);
		
		JLabel filterLabel = new JLabel(labels.get("Filter:"));
		
		Pair<Group, Group> g = GUIUtils.createForm(gl, 3,
				labels.get("Realm:"), realms, refreshRealms,
				labels.get("Block:"), blocks, watchBlock,
				labels.get("Port:"), ports, watchPort
		);
		gl.setHorizontalGroup(
			gl.createParallelGroup(Alignment.CENTER)
			.addComponent(engineInfo)
			.addComponent(topSeparator)
			.addGroup(
				gl.createParallelGroup()
				.addGroup(g.first)
				.addComponent(middleSeparator)
				.addGroup(
					gl.createSequentialGroup()
					.addComponent(filterLabel)
					.addComponent(filter)
					.addComponent(applyFilter, 25, 25, 25)
					.addComponent(clearFilter, 25, 25, 25)
					.addGap(30)
					.addComponent(rowLimitLabel)
					.addComponent(rowLimit, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
					.addComponent(actions)
				)
				.addComponent(scroll)
			)
		);
		gl.setVerticalGroup(
			gl.createSequentialGroup()
			.addComponent(engineInfo)
			.addComponent(topSeparator, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
			.addGroup(g.second)
			.addComponent(middleSeparator, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
			.addGroup(
				gl.createParallelGroup(Alignment.BASELINE)
				.addComponent(filterLabel)
				.addComponent(filter, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
				.addComponent(applyFilter, 25, 25, 25)
				.addComponent(clearFilter, 25, 25, 25)
				.addComponent(rowLimitLabel)
				.addComponent(rowLimit, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
				.addComponent(actions)
			)
			.addComponent(scroll)
		);
		table.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
			@Override
			public void valueChanged(ListSelectionEvent e) {
				applySelectionState();
			}
		});
		pack();
		createMenu();
		getRealmsAndBlocks();
		applySelectionState();
//		test();
	}
	/** Create test data. */
	void test() {
		CCDebugRow r = new CCDebugRow();
		r.watch = new CCWatchSettings();
		r.watch.realm = "DEFAULT";
		r.watch.block = "1";
		r.watch.blockType = "Merge";
		r.watch.port = "in";
		r.timestamp = new Date();
		r.value = Option.some("Test");
		rows.add(r);
		
		r = new CCDebugRow();
		r.watch = new CCWatchSettings();
		r.watch.realm = "DEFAULT";
		r.watch.block = "1";
		r.watch.blockType = "Merge";
		r.watch.port = "in";
		r.timestamp = new Date();
		r.value = Option.some(XNSerializables.storeList("block-registry", "block", BlockRegistryEntry.parseDefaultRegistry()));
		rows.add(r);
		
		r = new CCDebugRow();
		r.watch = new CCWatchSettings();
		r.watch.realm = "DEFAULT";
		r.watch.block = "1";
		r.watch.blockType = "Merge";
		r.watch.port = "in";
		r.timestamp = new Date();
		r.value = Option.error(new Exception());
		rows.add(r);
		
		r = new CCDebugRow();
		r.watch = new CCWatchSettings();
		r.watch.realm = "DEFAULT";
		r.watch.block = "1";
		r.watch.blockType = "Merge";
		r.watch.port = "in";
		r.timestamp = new Date();
		r.value = Option.none();
		rows.add(r);
		model.fireTableDataChanged();
	}
	/** Create the popup menu. */
	private void createMenu() {
		addMenu("Display value...", "doDisplayValue", true);
		actionMenu.addSeparator();
		JMenu export = new JMenu(labels.get("Export"));
		actionMenu.add(export);
		
		addMenu(export, "Selected entries...", "doExportSelectedEntries", true);
		addMenu(export, "Selected values...", "doExportSelectedValues", true);

		export.addSeparator();
		
		addMenu(export, "Unselected entries...", "doExportUnselectedEntries", false);
		addMenu(export, "Unselected values...", "doExportUnselectedValues", false);

		export.addSeparator();
		
		addMenu(export, "All entries...", "doExportAllEntries", false);
		addMenu(export, "All values...", "doExportAllValues", false);

		actionMenu.addSeparator();
		
		JMenu clear = new JMenu(labels.get("Clear"));
		actionMenu.add(clear);
		addMenu(clear, "Selected", "doClearSelected", true);
		addMenu(clear, "Unselected", "doRetainUnselected", false);
		addMenu(clear, "All", "doClearAll", false);
		
		
		JMenu stopWatch = new JMenu(labels.get("Stop watching"));
		actionMenu.add(stopWatch);
		addMenu(stopWatch, "All", "doStopWatchAll", true);
		addMenu(stopWatch, "Realm", "doStopWatchRealm", true);
		addMenu(stopWatch, "Block", "doStopWatchBlock", true);
		addMenu(stopWatch, "Block type", "doStopWatchBlockType", true);
		addMenu(stopWatch, "Port", "doStopWatchPort", true);
		
	}
	/**
	 * Adds a menu item to the action popup.
	 * @param title the title
	 * @param method the method
	 * @param whenSelected enabled when rows are selected?
	 */
	private void addMenu(String title, String method, boolean whenSelected) {
		JMenuItem mi = new JMenuItem(labels.get(title));
		mi.addActionListener(GUIUtils.createFromMethod(this, method));
		actionMenu.add(mi);
		if (whenSelected) {
			selectionDependant.add(mi);
		}
	}
	/**
	 * Adds a menu item to the parent menu.
	 * @param parent the parent menu
	 * @param title the title
	 * @param method the method
	 * @param whenSelected enabled when rows are selected?
	 */
	private void addMenu(JMenu parent, String title, String method, boolean whenSelected) {
		JMenuItem mi = new JMenuItem(labels.get(title));
		mi.addActionListener(GUIUtils.createFromMethod(this, method));
		parent.add(mi);
		if (whenSelected) {
			selectionDependant.add(mi);
		}
	}
	/** Apply the selection state to the appropriate components. */
	protected void applySelectionState() {
		boolean en = table.getSelectedRow() >= 0;
		for (Component c : selectionDependant) {
			c.setEnabled(en);
		}
	}
	/**
	 * Watch the given block in the given realm.
	 * @param realm the realm
	 * @param block the block
	 */
	protected void watchBlock(final String realm, final AdvanceBlockReference block) {
		try {
			final CCWatchSettings ws = new CCWatchSettings();
			watchers.add(ws);
			ws.realm = realm;
			ws.block = block.id;
			ws.blockType = block.type;
			ws.handler = Reactive.observeOn(Reactive.registerOn(engine.debugBlock(realm, ws.block), scheduler), edt).register(new Observer<BlockDiagnostic>() {
	
				@Override
				public void next(BlockDiagnostic value) {
					CCDebugRow r = new CCDebugRow();
					r.watch = ws;
					r.timestamp = value.timestamp;
					r.value = value.state;
					addRow(r);
				}

				@Override
				public void error(Throwable ex) {
					CCDebugRow r = new CCDebugRow();
					r.watch = ws;
					r.timestamp = new Date();
					r.value = Option.error(ex);
					addRow(r);
				}
	
				@Override
				public void finish() {
					CCDebugRow r = new CCDebugRow();
					r.watch = ws;
					r.timestamp = new Date();
					r.value = Option.none();
					addRow(r);
				}
				
			});
			CCDebugRow r = new CCDebugRow();
			r.watch = ws;
			r.timestamp = new Date();
			r.value = Option.none();
			addRow(r);
		} catch (Throwable t) {
			GUIUtils.errorMessage(this, t);
		}
	}
	/**
	 * Watch the given block in the given realm.
	 * @param realm the realm
	 * @param block the block
	 * @param port the port to watch
	 */
	protected void watchPort(final String realm, final AdvanceBlockReference block, final String port) {
		try {
			final CCWatchSettings ws = new CCWatchSettings();
			watchers.add(ws);
			ws.realm = realm;
			ws.block = block.id;
			ws.blockType = block.type;
			ws.port = port;
			ws.handler = Reactive.observeOn(Reactive.registerOn(engine.debugParameter(realm, ws.block, port), scheduler), edt).register(new Observer<PortDiagnostic>() {
	
				@Override
				public void next(PortDiagnostic value) {
					CCDebugRow r = new CCDebugRow();
					r.watch = ws;
					r.timestamp = value.timestamp;
					r.value = value.value;
					addRow(r);
				}

				@Override
				public void error(Throwable ex) {
					CCDebugRow r = new CCDebugRow();
					r.watch = ws;
					r.timestamp = new Date();
					r.value = Option.error(ex);
					addRow(r);
				}
	
				@Override
				public void finish() {
					CCDebugRow r = new CCDebugRow();
					r.watch = ws;
					r.timestamp = new Date();
					r.value = Option.none();
					addRow(r);
				}
				
			});
			CCDebugRow r = new CCDebugRow();
			r.watch = ws;
			r.timestamp = new Date();
			r.value = Option.none();
			addRow(r);
		} catch (Throwable t) {
			GUIUtils.errorMessage(this, t);
		}
	}
	/**
	 * Add a new row and optionally remove the outbound rows.
	 * @param r the new row
	 */
	void addRow(CCDebugRow r) {
		rows.add(r);
		model.fireTableRowsInserted(rows.size() - 1, rows.size() - 1);
		if (rows.size() > (Integer)rowLimit.getValue()) {
			rows.remove(0);
			model.fireTableRowsDeleted(0, 0);
		}
	}
	/** Terminate the debug. */
	void close() {
		for (CCWatchSettings ws : watchers) {
			try {
				ws.handler.close();
			} catch (IOException ex) {
				LOG.error(ex.toString(), ex);
			}
		}
	}
	/**
	 * Retrieve the list of realms and block types.
	 */
	void getRealmsAndBlocks() {
		GUIUtils.getWorker(new WorkItem() {
			/** The realms. */
			List<String> list;
			/** The the registry. */
			Map<String, BlockRegistryEntry> map = Maps.newHashMap();
			/** An exception. */
			Throwable t;
			@Override
			public void run() {
				try {
					list = Lists.newArrayList(Interactive.select(engine.datastore().queryRealms(), new Func1<AdvanceRealm, String>() {
						@Override
						public String invoke(AdvanceRealm param1) {
							return param1.name;
						}
					}));
					for (BlockRegistryEntry e : engine.queryBlocks()) {
						map.put(e.id, e);
					}
				} catch (Throwable t) {
					this.t = t;
				}
			}
			@Override
			public void done() {
				if (t != null) {
					GUIUtils.errorMessage(CCDebugDialog.this, t);
				} else {
					realms.setModel(new DefaultComboBoxModel<>(list.toArray(new String[0])));
					realms.setSelectedIndex(-1);
					blockTypes.clear();
					blockTypes.putAll(map);
				}
			}
		}).execute();
	}
	/**
	 * Retrieve the flow and extract all blocks.
	 * @param realm the target realm.
	 */
	void getFlow(final String realm) {
		GUIUtils.getWorker(new WorkItem() {
			/** The exception. */
			Throwable t;
			/** The block references. */
			final List<AdvanceBlockReference> refs = Lists.newArrayList();
			@Override
			public void run() {
				try {
					Deque<AdvanceCompositeBlock> deque = Lists.newLinkedList();
					deque.add(engine.queryFlow(realm));
					while (!deque.isEmpty()) {
						AdvanceCompositeBlock b = deque.removeFirst();
						
						refs.addAll(b.blocks.values());
						
						deque.addAll(b.composites.values());
					}
				} catch (Throwable t) {
					this.t = t;
				}
			}
			@Override
			public void done() {
				if (t != null) {
					GUIUtils.errorMessage(CCDebugDialog.this, t);
				} else {
					blockRefs.clear();
					blockRefs.addAll(refs);
					DefaultComboBoxModel<String> cm = new DefaultComboBoxModel<>();
					for (AdvanceBlockReference r : refs) {
						cm.addElement(r.id + ": " + r.type + " " + r.keywords);
					}
					blocks.setModel(cm);
					blocks.setSelectedIndex(-1);
					if (refs.size() == 0) {
						GUIUtils.infoMessage(CCDebugDialog.this, labels.get("No blocks in realm " + realm));
					}
				}				
			}
		}).execute();
	}
	/** Display the value. */
	void doDisplayValue() {
		for (int idx : table.getSelectedRows()) {
			displayData(idx);
		}
	}
	/** Export selected. */
	void doExportSelectedEntries() {
		doExport(selectedRows(), true);
	}
	/** Export selected. */
	void doExportSelectedValues() {
		doExport(selectedRows(), false);
	}
	/** Export selected. */
	void doExportUnselectedEntries() {
		doExport(unselectedRows(), true);
	}
	/** Export selected. */
	void doExportUnselectedValues() {
		doExport(unselectedRows(), false);
	}
	/** Export all. */
	void doExportAllEntries() {
		doExport(rows, true);
	}
	/** Export all. */
	void doExportAllValues() {
		doExport(rows, false);
	}
	/**
	 * Export the given row contents into the given file.
	 * @param rows the rows
	 * @param full add entry info (realm, timestamp, etc.)
	 */
	void doExport(List<CCDebugRow> rows, final boolean full) {
		JFileChooser fc = new JFileChooser(lastDir);
		fc.setFileFilter(new FileNameExtensionFilter("XML files (*.XML)", "xml"));
		fc.setDialogTitle(labels.get("Export"));
		if (fc.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
			final File f = fc.getSelectedFile();

			lastDir = f.getParentFile();
			
			final List<CCDebugRow> rows2 = Lists.newArrayList(rows);
			
			GUIUtils.getWorker(new WorkItem() {
				/** The exception. */
				Throwable t;
				@Override
				public void run() {
					try {
						XNElement result = new XNElement("debug-export");
						for (CCDebugRow dr : rows2) {
							XNElement parent = result;
							if (full) {
								XNElement entry = result.add("entry");
								entry.set("realm", dr.watch.realm, "block", dr.watch.block, 
										"block-type", dr.watch.blockType, "port", dr.watch.port,
										"timestamp", dr.timestamp
								);
								parent = entry;
							}
							if (Option.isError(dr.value)) {
								parent.add("error").content = Option.getError(dr.value).toString();
							} else
							if (Option.isNone(dr.value)) {
								parent.add("none");
							} else {
								Object o = dr.value.value();
								if (o instanceof XNElement) {
									parent.add(((XNElement)o).copy());
								} else {
									parent.add("value").content = o.toString();
								}
							}
						}
						result.save(f);
					} catch (Throwable t) {
						this.t = t;
					}
				}
				@Override
				public void done() {
					if (t != null) {
						GUIUtils.errorMessage(CCDebugDialog.this, t);
					}
				}
			}
			).execute();
		}
	}
	/** Clear selected. */
	void doClearSelected() {
		int[] sel = table.getSelectedColumns();
		for (int i = sel.length - 1; i >= 0; i--) {
			sel[i] = table.convertRowIndexToModel(sel[i]);
		}
		Arrays.sort(sel);
		for (int i = sel.length - 1; i >= 0; i--) {
			rows.remove(i);
		}
		model.fireTableDataChanged();
	}
	/** Clear all. */
	void doClearAll() {
		rows.clear();
		model.fireTableDataChanged();
	}
	/** Retain unselected. */
	void doRetainUnselected() {
		int[] sel = table.getSelectedColumns();
		Set<Integer> sels = Sets.newHashSet();
		for (int i = sel.length - 1; i >= 0; i--) {
			sels.add(table.convertRowIndexToModel(sel[i]));
		}
		for (int i = rows.size() - 1; i >= 0; i--) {
			if (!sels.contains(i)) {
				rows.remove(i);
			}
		}
		model.fireTableDataChanged();
	}
	/** @return the selected row objects. */
	List<CCDebugRow> selectedRows() {
		List<CCDebugRow> result = Lists.newArrayList();
		int[] sel = table.getSelectedColumns();
		for (int idx : sel) {
			result.add(rows.get(table.convertRowIndexToModel(idx)));
		}
		return result;
	}
	/** @return the selected row objects. */
	List<CCDebugRow> unselectedRows() {
		List<CCDebugRow> result = Lists.newArrayList();
		int[] sel = table.getSelectedColumns();
		Set<Integer> sr = Sets.newHashSet();
		for (int idx : sel) {
			sr.add(table.convertRowIndexToModel(idx));
		}
		for (int i = 0; i < rows.size(); i++) {
			if (sr.contains(i)) {
				result.add(rows.get(i));
			}
		}
		return result;
	}
	/** Stop watch all. */
	void doStopWatchAll() {
		for (CCWatchSettings ws : watchers) {
			try {
				ws.handler.close();
			} catch (IOException ex) {
				LOG.error(ex.toString(), ex);
			}
		}
		watchers.clear();
	}
	/** Stop watch realm. */
	void doStopWatchRealm() {
		Set<String> realms = Sets.newHashSet(
						Interactive.select(selectedRows(), new Func1<CCDebugRow, String>() {
			@Override
			public String invoke(CCDebugRow param1) {
				return param1.watch.realm;
			}
		}));
		for (int i = watchers.size() - 1; i >= 0; i--) {
			CCWatchSettings ws = watchers.get(i);
			if (realms.contains(ws.realm)) {
				watchers.remove(i);
				try {
					ws.handler.close();
				} catch (IOException ex) {
					LOG.error(ex.toString(), ex);
				}
			}
		}
	}
	/** Stop watch block. */
	void doStopWatchBlock() {
		Set<Pair<String, String>> realmsAndBlock = Sets.newHashSet(
						Interactive.select(selectedRows(), new Func1<CCDebugRow, Pair<String, String>>() {
			@Override
			public Pair<String, String> invoke(CCDebugRow param1) {
				return Pair.of(param1.watch.realm, param1.watch.block);
			}
		}));
		for (int i = watchers.size() - 1; i >= 0; i--) {
			CCWatchSettings ws = watchers.get(i);
			if (realmsAndBlock.contains(Pair.of(ws.realm, ws.block))) {
				watchers.remove(i);
				try {
					ws.handler.close();
				} catch (IOException ex) {
					LOG.error(ex.toString(), ex);
				}
			}
		}
	}
	/** Stop watch block type. */
	void doStopWatchBlockType() {
		Set<String> toDelete = Sets.newHashSet(
						Interactive.select(selectedRows(), new Func1<CCDebugRow, String>() {
			@Override
			public String invoke(CCDebugRow param1) {
				return param1.watch.blockType;
			}
		}));
		for (int i = watchers.size() - 1; i >= 0; i--) {
			CCWatchSettings ws = watchers.get(i);
			if (toDelete.contains(ws.blockType)) {
				watchers.remove(i);
				try {
					ws.handler.close();
				} catch (IOException ex) {
					LOG.error(ex.toString(), ex);
				}
			}
		}
	}
	/** Stop watch port. */
	void doStopWatchPort() {
		Set<Tuple3<String, String, String>> realmsAndBlock = Sets.newHashSet(
				Interactive.select(selectedRows(), new Func1<CCDebugRow, Tuple3<String, String, String>>() {
			@Override
			public Tuple3<String, String, String> invoke(CCDebugRow param1) {
				return Tuple3.of(param1.watch.realm, param1.watch.block, param1.watch.port);
			}
		}));
		for (int i = watchers.size() - 1; i >= 0; i--) {
			CCWatchSettings ws = watchers.get(i);
			if (ws.port != null && realmsAndBlock.contains(Tuple3.of(ws.realm, ws.block, ws.port))) {
				watchers.remove(i);
				try {
					ws.handler.close();
				} catch (IOException ex) {
					LOG.error(ex.toString(), ex);
				}
			}
		}
		
	}
	/**
	 * View the first selected row.
	 */
	void viewData() {
		int idx = table.getSelectedRow();
		if (idx >= 0) {
			displayData(idx);
		}
	}
	/**
	 * Display the data of the indexth row.
	 * @param idx the view index
	 */
	public void displayData(int idx) {
		idx = table.convertRowIndexToModel(idx);
		
		CCValueDialog d = new CCValueDialog(labels, rows.get(idx));
		engineInfo.set(d.engineInfo);
		d.pack();
		d.setLocationRelativeTo(this);
		d.setVisible(true);
	}
}
