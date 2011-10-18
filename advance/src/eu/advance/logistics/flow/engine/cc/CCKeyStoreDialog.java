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
import hu.akarnokd.reactive4java.base.Option;
import hu.akarnokd.reactive4java.interactive.Interactive;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.AbstractTableModel;

import com.google.common.collect.Lists;

import edu.umd.cs.findbugs.annotations.NonNull;
import eu.advance.logistics.flow.engine.api.AdvanceKeyEntry;
import eu.advance.logistics.flow.engine.api.AdvanceKeyStore;
import eu.advance.logistics.flow.engine.api.AdvanceKeyStoreExport;

/**
 * The keystore details dialog.
 * @author akarnokd, 2011.10.18.
 */
public class CCKeyStoreDialog extends JPanel implements CCLoadSave<AdvanceKeyStore> {
	/** */
	private static final long serialVersionUID = -7322563942713078574L;
	/** The label manager. */
	protected final LabelManager labels;
	/** The name. */
	protected JTextField name;
	/** The keystore location. */
	protected JTextField location;
	/** The keystore password. */
	protected JPasswordField password;
	/** The browse button. */
	protected JButton browse;
	/** Generate a new key. */
	protected JButton generate;
	/** Import/export. */
	protected JButton importExport;
	/** Delete keys. */
	protected JButton delete;
	/** The table. */
	protected JTable table;
	/** The model. */
	protected AbstractTableModel model;
	/** The model rows. */
	protected final List<AdvanceKeyEntry> rows = Lists.newArrayList();
	/** The number of records. */
	protected JLabel records;
	/** The callbacks for various key management functions. */
	public CCKeyManager keyManager;
	/**
	 * Constructs the GUI.
	 * @param labels the labels
	 */
	public CCKeyStoreDialog(@NonNull final LabelManager labels) {
		this.labels = labels;
		
		GroupLayout gl = new GroupLayout(this);
		this.setLayout(gl);
		
		gl.setAutoCreateContainerGaps(true);
		gl.setAutoCreateGaps(true);
		
		
		generate = new JButton(labels.get("Generate..."));
		importExport = new JButton(labels.get("Import/Export"));
		importExport.setIcon(new ImageIcon(getClass().getResource("down.png")));
		importExport.setHorizontalTextPosition(SwingConstants.TRAILING);
		delete = new JButton(labels.get("Delete"));
		browse = new JButton("Browse...");
		
		generate.setVisible(false);
		importExport.setVisible(false);
		delete.setVisible(false);
		
		final JPopupMenu popup = new JPopupMenu();
		
		JMenuItem mnuImportCert = new JMenuItem(labels.get("Import certificate..."));
		JMenuItem mnuImportKey = new JMenuItem(labels.get("Import private key..."));
		JMenuItem mnuExportCert = new JMenuItem(labels.get("Export certificate..."));
		mnuExportCert.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				exportCertificate(name.getText(), getSelectedKeys().iterator());
			}
		});
		JMenuItem mnuExportKey = new JMenuItem(labels.get("Export private key..."));
		JMenuItem mnuExportRSA = new JMenuItem(labels.get("Create RSA signing request..."));
		JMenuItem mnuImportRSA = new JMenuItem(labels.get("Import RSA signing response..."));
		
		popup.add(mnuImportCert);
		popup.add(mnuImportKey);
		popup.addSeparator();
		popup.add(mnuExportCert);
		popup.add(mnuExportKey);
		popup.addSeparator();
		popup.add(mnuExportRSA);
		popup.add(mnuImportRSA);
		
		importExport.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				popup.show(importExport, 0, importExport.getHeight());
			}
		});
		
		name = new JTextField();
		location = new JTextField();
		password = new JPasswordField();
		
		records = new JLabel(labels.format("Records: %d", 0));
		
		createModel();
		table = new JTable(model);
		table.setAutoCreateRowSorter(true);
		model.addTableModelListener(new TableModelListener() {
			@Override
			public void tableChanged(TableModelEvent e) {
				records.setText(labels.format("Records: %d", table.getRowCount()));
			}
		});
		
		JScrollPane scroll = new JScrollPane(table);
		
		JLabel nameLabel = new JLabel(labels.get("Name:"));
		JLabel passwordLabel = new JLabel(labels.get("Password:"));
		JLabel locationLabel = new JLabel(labels.get("Location:"));
		
		gl.setHorizontalGroup(
			gl.createParallelGroup(Alignment.CENTER)
			.addGroup(
				gl.createParallelGroup()
				.addGroup(
					gl.createSequentialGroup()
					.addComponent(nameLabel)
					.addComponent(name)
					.addGap(30)
					.addComponent(passwordLabel)
					.addComponent(password)
				)
				.addGroup(
					gl.createSequentialGroup()
					.addComponent(locationLabel)
					.addComponent(location)
					.addComponent(browse)
				)
				.addComponent(scroll)
			)
			.addComponent(records)
			.addGroup(
				gl.createSequentialGroup()
				.addComponent(generate)
				.addComponent(importExport)
				.addComponent(delete)
			)
		);
		gl.setVerticalGroup(
			gl.createSequentialGroup()
			.addGroup(
				gl.createParallelGroup(Alignment.BASELINE)
				.addComponent(nameLabel)
				.addComponent(name)
				.addComponent(passwordLabel)
				.addComponent(password)
			)
			.addGroup(
				gl.createParallelGroup(Alignment.BASELINE)
				.addComponent(locationLabel)
				.addComponent(location)
				.addComponent(browse)
			)
			.addComponent(scroll)
			.addComponent(records)
			.addGroup(
				gl.createParallelGroup(Alignment.BASELINE)
				.addComponent(generate)
				.addComponent(importExport)
				.addComponent(delete)
			)
		);
	}
	/**
	 * Create the table model.
	 */
	private void createModel() {
		model = new AbstractTableModel() {
			/** */
			private static final long serialVersionUID = 5645880663760000296L;
			/** The classes. */
			final Class<?>[] classes = { String.class, String.class, Date.class };
			/** The names. */
			final String[] columns = { "Type", "Name", "Date" };
			@Override
			public Object getValueAt(int rowIndex, int columnIndex) {
				AdvanceKeyEntry e = rows.get(rowIndex);
				switch (columnIndex) {
				case 0:
					return e.type.toString();
				case 1:
					return e.name;
				case 2:
					return e.createdAt;
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
				return 3;
			}
			@Override
			public Class<?> getColumnClass(int columnIndex) {
				return classes[columnIndex];
			}
			@Override
			public String getColumnName(int column) {
				return labels.get(columns[column]);
			}
		};
	}
	/** 
	 * Show the browse button?
	 * @param visible visible?
	 */
	public void showBrowse(boolean visible) {
		browse.setVisible(visible);
	}
	@Override
	public void load(AdvanceKeyStore value) {
		name.setText(value.name);
		location.setText(value.location);
		password.setText("");
		generate.setVisible(true);
		importExport.setVisible(true);
		delete.setVisible(true);
	}
	@Override
	public AdvanceKeyStore save() {
		AdvanceKeyStore result = new AdvanceKeyStore();
		
		result.name = name.getText();
		if (result.name.isEmpty()) {
			GUIUtils.errorMessage(this, labels.get("Please enter a name!"));
			return null;
		}
		result.location = location.getText();
		if (result.location.isEmpty()) {
			GUIUtils.errorMessage(this, labels.get("Please enter a location!"));
			return null;
		}
		char[] p = password.getPassword();
		if (p != null && p.length > 0) {
			result.password(p);
		}
		
		return result;
	}
	@Override
	public void onAfterSave() {
		name.setEditable(false);
		generate.setVisible(true);
		importExport.setVisible(true);
		delete.setVisible(true);
	}
	/**
	 * Query the keys of the given keystore asynchronously.
	 * @param keyStore the keystore
	 */
	public void queryKeys(final String keyStore) {
		GUIUtils.getWorker(new WorkItem() {
			/** The result. */
			Option<List<AdvanceKeyEntry>> result;
			@Override
			public void run() {
				try {
					result = Option.some(keyManager.queryKeys(keyStore));
				} catch (Throwable t) {
					result = Option.error(t);
				}
			}
			@Override
			public void done() {
				if (Option.isError(result)) {
					GUIUtils.errorMessage(CCKeyStoreDialog.this, Option.getError(result));
				} else {
					setKeys(result.value());
				}
			}
		}).execute();
	}
	/**
	 * Delete the selected keys.
	 * @param keyStore the keystore
	 */
	public void deleteKeys(final String keyStore) {
		final Iterable<String> keys = Interactive.select(getSelectedKeys(), new Func1<AdvanceKeyEntry, String>() {
			@Override
			public String invoke(AdvanceKeyEntry param1) {
				return param1.name;
			}
		});
		GUIUtils.getWorker(new WorkItem() {
			Throwable t;
			@Override
			public void run() {
				try {
					keyManager.deleteKeys(keyStore, keys);
				} catch (Throwable t) {
					this.t = t;
				}
			}
			@Override
			public void done() {
				if (t != null) {
					GUIUtils.errorMessage(CCKeyStoreDialog.this, t);
				}
			}
		}).execute();
		
	}
	/**
	 * Returns the list of selected key entries.
	 * @return the list of selected key entries
	 */
	public List<AdvanceKeyEntry> getSelectedKeys() {
		int[] sel = table.getSelectedRows();
		final List<AdvanceKeyEntry> result = Lists.newArrayList();
		for (int i : sel) {
			int idx = table.convertRowIndexToModel(i);
			result.add(rows.get(idx));
		}
		return result;
	}
	/**
	 * Display the key entries.
	 * @param keys the list of keys
	 */
	public void setKeys(List<AdvanceKeyEntry> keys) {
		this.rows.clear();
		this.rows.addAll(keys);
		model.fireTableDataChanged();
	}
	/**
	 * Set the key manager.
	 * @param keyManager the key manager
	 */
	public void setKeyManager(@NonNull CCKeyManager keyManager) {
		this.keyManager = keyManager;
	}
	/**
	 * Export the given sequence of certificates from the keystore.
	 * @param keyStore the keystore
	 * @param items the items
	 */
	protected void exportCertificate(final String keyStore, final Iterator<AdvanceKeyEntry> items) {
		if (!items.hasNext()) {
			return;
		}
		AdvanceKeyEntry ks = items.next();
		JFileChooser fc = new JFileChooser();
		fc.setDialogTitle(labels.format("Export certificate of key %s", ks.name));
		fc.setFileFilter(new FileNameExtensionFilter("Certificates (*.CER)", "cer", "crt", "cert", "der"));
		if (fc.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
			final File f = fc.getSelectedFile();
			
			final AdvanceKeyStoreExport request = new AdvanceKeyStoreExport();
			request.keyStore = keyStore;
			request.keyAlias = ks.name;
			
			GUIUtils.getWorker(new WorkItem() {
				/** The exception. */
				Throwable t;
				@Override
				public void run() {
					try {
						String s = keyManager.exportCertificate(request);
						PrintWriter out = new PrintWriter(new FileWriter(f));
						try {
							out.print(s);
						} finally {
							out.close();
						}
					} catch (Throwable t) {
						this.t = t;
					}
				}
				@Override
				public void done() {
					if (t != null) {
						GUIUtils.errorMessage(CCKeyStoreDialog.this, t);
					} else {
						exportCertificate(keyStore, items);
					}
				}
			});
		}
	}
}
