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
import hu.akarnokd.reactive4java.base.Pair;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.Arrays;
import java.util.List;

import javax.swing.ButtonGroup;
import javax.swing.DefaultComboBoxModel;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.GroupLayout.Group;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.AbstractTableModel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;

import edu.umd.cs.findbugs.annotations.NonNull;
import eu.advance.logistics.flow.engine.api.AdvanceGenerateKey;
import eu.advance.logistics.flow.engine.api.AdvanceJDBCDrivers;
import eu.advance.logistics.flow.engine.api.AdvanceKeyEntry;
import eu.advance.logistics.flow.engine.api.AdvanceKeyStore;
import eu.advance.logistics.flow.engine.api.AdvanceKeyStoreExport;
import eu.advance.logistics.flow.engine.util.KeystoreManager;

/**
 * Create or manage existing properties of a local flow engine.
 * @author akarnokd, 2011.10.24.
 */
public class CCEngineDialog extends JFrame {
	/** */
	private static final long serialVersionUID = -7772881259616154816L;
	/** The logger. */
	protected static final Logger LOG = LoggerFactory.getLogger(CCEngineDialog.class);
	/** The label manager. */
	protected final LabelManager labels;
	/** The certificate login port. */
	protected JFormattedTextField certAuthPort;
	/** The basic login port. */
	protected JFormattedTextField basicAuthPort;
	/** The certificates of the client. */
	protected JComboBox<String> clientKeyStore;
	/** The server keys and certificate. */
	protected JComboBox<String> serverKeyStore;
	/** The server's key password. */
	protected JPasswordField serverPassword;
	/** The server's key password. */
	protected JPasswordField serverPasswordAgain;
	/** The server's key alias. */
	protected JTextField serverKeyAlias;
	/** The keystores table. */
	protected JTable keystoreTable;
	/** The keystores model. */
	protected AbstractTableModel keystoreModel;
	/** The keystores list. */
	protected final List<AdvanceKeyStore> keystores = Lists.newArrayList();
	/** Local connection. */
	protected JRadioButton dsLocal;
	/** JDBC connection. */
	protected JRadioButton dsJDBC;
	/** The connection URL. */
	protected JTextField dsUrl;
	/** The custom driver. */
	protected JTextField dsCustomDriver;
	/** The set of predefined drivers. */
	protected JComboBox<AdvanceJDBCDrivers> dsDriver;
	/** The custom driver label. */
	protected JLabel dsCustomDriverLabel;
	/** The user name. */
	protected JTextField dsUser;
	/** The password. */
	protected JPasswordField dsPassword;
	/** The password again. */
	protected JPasswordField dsPasswordAgain;
	/** The schema. */
	protected JTextField dsSchema;
	/** The driver label. */
	protected JLabel dsDriverLabel;
	/** The user label. */
	protected JLabel dsUserLabel;
	/** The schema label. */
	protected JLabel dsSchemaLabel;
	/** The list of blocks. */
	protected JTable blocks;
	/** The block model. */
	protected AbstractTableModel blocksModel;
	/** The list of schemas. */
	protected JTable schemas;
	/** The scehmas model. */
	protected AbstractTableModel schemasModel;
	/** The blocks list. */
	protected final List<String> blocksList = Lists.newArrayList();
	/** The schemas list. */
	protected final List<String> schemasList = Lists.newArrayList();
	/** The dialog creator. */
	protected final CCDialogCreator dc;
	/**
	 * The label manager.
	 * @param labels the label manager
	 * @param dc the dialog creator
	 */
	public CCEngineDialog(@NonNull final LabelManager labels, @NonNull final CCDialogCreator dc) {
		this.labels = labels;
		this.dc = dc;
		JTabbedPane tabs = new JTabbedPane();
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		
		tabs.add(labels.get("Keystores"), createKeystores());
		tabs.add(labels.get("Listener"), createListener());
		tabs.add(labels.get("Datastore"), createDatastore());
		tabs.add(labels.get("Blocks & Schemas"), createBlocksAndSchemas());
		tabs.add(labels.get("Schedulers"), createSchedulers());
		
		Container c = getContentPane();
		c.setLayout(new BorderLayout());
		c.add(tabs, BorderLayout.CENTER);
		pack();
	}
	/**
	 * Create a listener panel.
	 * @return the panel
	 */
	private JPanel createListener() {
		JPanel p = new JPanel();
		
		GroupLayout gl = createLayout(p);
		
		basicAuthPort = new JFormattedTextField(8444);
		certAuthPort = new JFormattedTextField(8443);
		
		serverKeyStore = new JComboBox<String>();
		serverKeyAlias = new JTextField();
		serverPassword = new JPasswordField();
		serverPasswordAgain = new JPasswordField();
		
		clientKeyStore = new JComboBox<String>();
		
		Pair<Group, Group> g = GUIUtils.createForm(gl, 2,
			labels.get("Basic authentication port:"), basicAuthPort,
			labels.get("Certificate authentication port:"), certAuthPort,
			labels.get("Server keystore:"), serverKeyStore,
			labels.get("Server key alias:"), serverKeyAlias,
			labels.get("Server key password:"), serverPassword,
			labels.get("Server key password again:"), serverPasswordAgain,
			labels.get("Client key store:"), clientKeyStore
		);
		gl.setHorizontalGroup(g.first);
		gl.setVerticalGroup(g.second);
		
		return p;
	}
	/**
	 * Create a keystores panel.
	 * @return the panel
	 */
	private JPanel createKeystores() {
		JPanel p = new JPanel();
		
		GroupLayout gl = createLayout(p);

		keystoreModel = new AbstractTableModel() {
			/** */
			private static final long serialVersionUID = 5848853914650306897L;
			/** The column names. */
			protected String[] names = { "Name", "Location" };
			/** The classes. */
			protected Class<?>[] classes = { String.class, String.class };
			@Override
			public Object getValueAt(int rowIndex, int columnIndex) {
				AdvanceKeyStore ks = keystores.get(rowIndex);
				switch (columnIndex) {
				case 0:
					return ks.name;
				case 1:
					return ks.location;
				default:
					return null;
				}
			}
			
			@Override
			public int getRowCount() {
				return keystores.size();
			}
			
			@Override
			public int getColumnCount() {
				return 2;
			}
			@Override
			public Class<?> getColumnClass(int columnIndex) {
				return classes[columnIndex];
			}
			@Override
			public String getColumnName(int column) {
				return labels.get(names[column]);
			}
		};
		
		keystoreTable = new JTable(keystoreModel);
		keystoreTable.setAutoCreateRowSorter(true);
		
		final JLabel records = new JLabel(labels.format("Records: %d", 0));
		
		JScrollPane sp = new JScrollPane(keystoreTable);
		
		JButton create = new JButton(labels.get("Add..."));
		create.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				doAddKeystore();
			}
		});
		JButton remove = new JButton(labels.get("Remove"));
		remove.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				for (AdvanceKeyStore ks : doRemoveSelected(keystoreTable, keystoreModel, keystores)) {
					File f = new File(ks.location);
					if (!f.delete()) {
						LOG.info("Could not delete keystore file " + f);
					}
				}
				updateKeyStoreLists();
			}
		});
		keystoreTable.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if (e.getClickCount() > 1 && keystoreTable.getSelectedRow() >= 0) {
					doEditKeystore();
				}
			}
		});
		
		gl.setHorizontalGroup(
			gl.createParallelGroup(Alignment.CENTER)
			.addComponent(sp)
			.addComponent(records)
			.addGroup(
				gl.createSequentialGroup()
				.addComponent(create)
				.addComponent(remove)
			)
		);
		gl.setVerticalGroup(
			gl.createSequentialGroup()
			.addComponent(sp)
			.addComponent(records)
			.addGroup(
				gl.createParallelGroup(Alignment.BASELINE)
				.addComponent(create)
				.addComponent(remove)
			)
		);
		
		keystoreModel.addTableModelListener(new TableModelListener() {
			@Override
			public void tableChanged(TableModelEvent e) {
				records.setText(labels.format("Records: %d", keystoreTable.getRowCount()));
			}
		});
		
		
		
		return p;
	}
	/**
	 * The create/edit keystore dialog. 
	 * @author akarnokd, 2011.10.24.
	 */
	public class EditKeystoreDialog extends JDialog {
		/** */
		private static final long serialVersionUID = -7306217057387171466L;
		/** The name. */
		JTextField name;
		/** The location. */
		JTextField location;
		/** The password. */
		JPasswordField password;
		/** The password again. */
		JPasswordField passwordAgain;
		/** The user pressed OK. */
		boolean approved;
		/**
		 * Creates the dialog.
		 */
		public EditKeystoreDialog() {
			setResizable(false);
			setDefaultCloseOperation(DISPOSE_ON_CLOSE);
			setModal(true);
			name = new JTextField(20);
			location = new JTextField(20);
			password = new JPasswordField();
			passwordAgain = new JPasswordField();
			
			ActionListener okAction = new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					if (name.getText().isEmpty()) {
						GUIUtils.errorMessage(EditKeystoreDialog.this, labels.get("Please enter a name!"));
						return;
					}
					if (location.getText().isEmpty()) {
						GUIUtils.errorMessage(EditKeystoreDialog.this, labels.get("Please enter a location!"));
						return;
					}
					char[] p1 = password.getPassword();
					char[] p2 = passwordAgain.getPassword();
					if (!Arrays.equals(p1, p2)) {
						GUIUtils.errorMessage(EditKeystoreDialog.this, labels.get("Passwords mismatch!"));
						return;
					}
					approved = true;
					dispose();
				}
			};
			
			name.addActionListener(okAction);
			location.addActionListener(okAction);
			password.addActionListener(okAction);
			passwordAgain.addActionListener(okAction);
			
			JButton ok = new JButton(labels.get("OK"));
			ok.addActionListener(okAction);
			JButton cancel = new JButton(labels.get("Cancel"));
			cancel.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					dispose();
				}
			});
			
			GroupLayout gl = createLayout(getContentPane());
			
			Pair<Group, Group> g = GUIUtils.createForm(gl, 2,
				labels.get("Name:"), name,
				labels.get("Location:"), location,
				labels.get("Password:"), password,
				labels.get("Password again:"), passwordAgain
			);
			
			gl.setHorizontalGroup(
				gl.createParallelGroup(Alignment.CENTER)
				.addGroup(g.first)
				.addGroup(
					gl.createSequentialGroup()
					.addComponent(ok)
					.addComponent(cancel)
				)
			);
			gl.setVerticalGroup(
				gl.createSequentialGroup()
				.addGroup(g.second)
				.addGroup(
					gl.createParallelGroup(Alignment.BASELINE)
					.addComponent(ok)
					.addComponent(cancel)
				)
			);
			pack();
		}
	}
	/**
	 * Create a datastores panel.
	 * @return the panel
	 */
	private JPanel createDatastore() {
		JPanel p = new JPanel();
		
		GroupLayout gl = createLayout(p);
		
		dsLocal = new JRadioButton(labels.get("Local"));
		dsJDBC = new JRadioButton(labels.get("JDBC"));
		
		ButtonGroup bg = new ButtonGroup();
		bg.add(dsLocal);
		bg.add(dsJDBC);
		
		dsUrl = new JTextField();
		dsCustomDriver = new JTextField();
		dsDriver = new JComboBox<AdvanceJDBCDrivers>(AdvanceJDBCDrivers.values());
		dsDriver.setSelectedItem(AdvanceJDBCDrivers.GENERIC);
		
		dsCustomDriverLabel = new JLabel(labels.get("Custom driver:"));
		
		dsUser = new JTextField();
		dsPassword = new JPasswordField();
		dsPasswordAgain = new JPasswordField();
		
		dsSchema = new JTextField();
		
		dsDriverLabel = new JLabel(labels.get("Driver:"));
		final JLabel dsUrlLabel = new JLabel(labels.get("URL:"));
		dsUserLabel = new JLabel(labels.get("User:"));
		final JLabel dsPasswordLabel = new JLabel(labels.get("Password:"));
		final JLabel dsPasswordAgainLabel = new JLabel(labels.get("Password again:"));
		dsSchemaLabel = new JLabel(labels.get("Schema:"));
		
		gl.setHorizontalGroup(
			gl.createParallelGroup()
			.addComponent(dsLocal)
			.addComponent(dsJDBC)
			.addGroup(
				gl.createSequentialGroup()
				.addGroup(
					gl.createParallelGroup()
					.addComponent(dsDriverLabel)
					.addComponent(dsCustomDriverLabel)
					.addComponent(dsUrlLabel)
					.addComponent(dsUserLabel)
					.addComponent(dsPasswordLabel)
					.addComponent(dsPasswordAgainLabel)
					.addComponent(dsSchemaLabel)
				)
				.addGroup(
					gl.createParallelGroup()
					.addComponent(dsDriver)
					.addComponent(dsCustomDriver)
					.addComponent(dsUrl)
					.addComponent(dsUser)
					.addComponent(dsPassword)
					.addComponent(dsPasswordAgain)
					.addComponent(dsSchema)
				)
			)
		);
		gl.setVerticalGroup(
			gl.createSequentialGroup()
			.addComponent(dsLocal)
			.addComponent(dsJDBC)
			.addGroup(
				gl.createParallelGroup(Alignment.BASELINE)
				.addComponent(dsDriverLabel)
				.addComponent(dsDriver, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
			)
			.addGroup(
				gl.createParallelGroup(Alignment.BASELINE)
				.addComponent(dsCustomDriverLabel)
				.addComponent(dsCustomDriver, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
			)
			.addGroup(
				gl.createParallelGroup(Alignment.BASELINE)
				.addComponent(dsUrlLabel)
				.addComponent(dsUrl, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
			)
			.addGroup(
				gl.createParallelGroup(Alignment.BASELINE)
				.addComponent(dsUserLabel)
				.addComponent(dsUser, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
			)
			.addGroup(
				gl.createParallelGroup(Alignment.BASELINE)
				.addComponent(dsPasswordLabel)
				.addComponent(dsPassword, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
			)
			.addGroup(
				gl.createParallelGroup(Alignment.BASELINE)
				.addComponent(dsPasswordAgainLabel)
				.addComponent(dsPasswordAgain, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
			)
			.addGroup(
				gl.createParallelGroup(Alignment.BASELINE)
				.addComponent(dsSchemaLabel)
				.addComponent(dsSchema, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
			)
		);
		
		dsDriver.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				dsCustomDriver.setVisible(dsDriver.getSelectedItem() == AdvanceJDBCDrivers.GENERIC);
				dsCustomDriverLabel.setVisible(dsCustomDriver.isVisible());
			}
		});
		
		ActionListener radioAction = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				doDatastoreTypeChange();
			}

		};
		dsLocal.addActionListener(radioAction);
		dsJDBC.addActionListener(radioAction);
		
		
		dsLocal.setSelected(true);
		doDatastoreTypeChange();
		return p;
	}
	/**
	 * Adjust the settings of the fields based on the datastore type.
	 */
	public void doDatastoreTypeChange() {
		boolean s = dsLocal.isSelected();
		dsUser.setEnabled(!s);
		dsSchema.setEnabled(!s);
		dsCustomDriver.setEnabled(!s);
		dsDriver.setEnabled(!s);
		dsUserLabel.setEnabled(!s);
		dsSchemaLabel.setEnabled(!s);
		dsCustomDriverLabel.setEnabled(!s);
		dsDriverLabel.setEnabled(!s);
	}
	/**
	 * Create the base layout.
	 * @param p the container
	 * @return the group layout
	 */
	public GroupLayout createLayout(Container p) {
		GroupLayout gl = new GroupLayout(p);
		p.setLayout(gl);
		gl.setAutoCreateContainerGaps(true);
		gl.setAutoCreateGaps(true);
		return gl;
	}
	/**
	 * Create a blocks and schemas panel.
	 * @return the panel
	 */
	private JPanel createBlocksAndSchemas() {
		JPanel p = new JPanel();
		
		GroupLayout gl = createLayout(p);
		
		JSeparator middle = new JSeparator(JSeparator.HORIZONTAL);

		blocksModel = new AbstractTableModel() {
			/**	 */
			private static final long serialVersionUID = -6608055522948999420L;
			@Override
			public Object getValueAt(int rowIndex, int columnIndex) {
				return blocksList.get(rowIndex);
			}
			
			@Override
			public int getRowCount() {
				return blocksList.size();
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
				return labels.get("Block registry file");
			}
		};
		schemasModel = new AbstractTableModel() {
			/** */
			private static final long serialVersionUID = 1918304317932600411L;
			@Override
			public Object getValueAt(int rowIndex, int columnIndex) {
				return schemasList.get(rowIndex);
			}
			
			@Override
			public int getRowCount() {
				return schemasList.size();
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
				return labels.get("Schema directory");
			}
		};

		blocks = new JTable(blocksModel);
		schemas = new JTable(schemasModel);
		
		JScrollPane spBlocks = new JScrollPane(blocks);
		JScrollPane spSchemas = new JScrollPane(schemas);
		
		JButton blockAdd = new JButton(labels.get("Add..."));
		blockAdd.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				doAddBlocks();
			}
		});
		JButton blockDelete = new JButton(labels.get("Delete"));
		blockDelete.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				doRemoveSelected(blocks, blocksModel, blocksList);
			}
		});

		JButton schemaAdd = new JButton(labels.get("Add..."));
		schemaAdd.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				doAddSchemas();
			}
		});
		JButton schemaDelete = new JButton(labels.get("Delete"));
		schemaDelete.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				doRemoveSelected(schemas, schemasModel, schemasList);
			}
		});

		blocks.setAutoCreateRowSorter(true);
		schemas.setAutoCreateRowSorter(true);
		
		gl.setHorizontalGroup(
			gl.createParallelGroup(Alignment.CENTER)
			.addComponent(spBlocks)
			.addGroup(
				gl.createSequentialGroup()
				.addComponent(blockAdd)
				.addComponent(blockDelete)
			)
			.addComponent(middle)
			.addComponent(spSchemas)
			.addGroup(
				gl.createSequentialGroup()
				.addComponent(schemaAdd)
				.addComponent(schemaDelete)
			)
		);
		gl.setVerticalGroup(
			gl.createSequentialGroup()
			.addComponent(spBlocks)
			.addGroup(
				gl.createParallelGroup(Alignment.BASELINE)
				.addComponent(blockAdd)
				.addComponent(blockDelete)
			)
			.addComponent(middle)
			.addComponent(spSchemas)
			.addGroup(
				gl.createParallelGroup(Alignment.BASELINE)
				.addComponent(schemaAdd)
				.addComponent(schemaDelete)
			)
		);
		
		return p;
	}
	/**
	 * Create the schedulers panel.
	 * @return the panel
	 */
	private JPanel createSchedulers() {
		JPanel p = new JPanel();
		
		createLayout(p);

		return p;
	}
	/**
	 * Add a new keystore.
	 */
	protected void doAddKeystore() {
		EditKeystoreDialog d = new EditKeystoreDialog();
		d.setTitle("Add keystore");
		d.setLocationRelativeTo(this);
		d.setVisible(true);
		if (d.approved) {
			AdvanceKeyStore ks = new AdvanceKeyStore();
			ks.name = d.name.getText();
			ks.location = d.location.getText();
			ks.password(d.password.getPassword());
			keystores.add(ks);
			keystoreModel.fireTableDataChanged();
			updateKeyStoreLists();
			
			try {
				KeystoreManager mgr = new KeystoreManager();
				mgr.create();
				mgr.save(ks.location, ks.password());
			} catch (Throwable t) {
				GUIUtils.errorMessage(this, t);
			}
		}
	}
	/**
	 * Remove the selected rows of the table.
	 * @param <T> the list element type
	 * @param table the table
	 * @param model the model
	 * @param items the items
	 * @return the list of removed items
	 */
	protected <T> List<T> doRemoveSelected(JTable table, AbstractTableModel model, List<T> items) {
		int[] sel = table.getSelectedRows();
		List<T> result = Lists.newArrayList();
		if (sel.length > 0 && JOptionPane.showConfirmDialog(this, labels.get("Are you sure"), 
				labels.get("Delete rows"), JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
			for (int i = 0; i < sel.length; i++) {
				sel[i] = table.convertRowIndexToModel(sel[i]);
			}
			Arrays.sort(sel);
			for (int i = sel.length - 1; i >= 0; i--) {
				result.add(items.remove(i));
			}
			model.fireTableDataChanged();
		}
		return result;
	}
	/**
	 * Edit a selected keystore.
	 */
	protected void doEditKeystore() {
		int idx = keystoreTable.convertRowIndexToModel(keystoreTable.getSelectedRow());
		AdvanceKeyStore ks = keystores.get(idx);
		
		final CCKeyStoreDialog d = new CCKeyStoreDialog(labels);
		
		CCKeyManager mgr = new CCKeyManager() {
			
			/**
			 * Query the keystore internally.
			 * @param name the keystore name
			 * @return the keystore data or null
			 */
			protected AdvanceKeyStore queryKeyStoreD(String name) {
				for (AdvanceKeyStore ks : keystores) {
					if (ks.name.equals(name)) {
						return ks;
					}
				}
				return null;
			}
			@Override
			public void updateKeyStore(AdvanceKeyStore keyStore) throws Exception {
				AdvanceKeyStore prev = queryKeyStoreD(keyStore.name);
				AdvanceKeyStore next = keyStore.copy();
				KeystoreManager mgr = new KeystoreManager();
				if (prev != null) {
					if (next.password() == null) {
						next.password(prev.password());
					}
					mgr.load(prev.location, prev.password());
					if (!prev.location.equals(next.location)) {
						File f = new File(prev.location);
						f.delete();
					}
					for (int i = 0; i < keystores.size(); i++) {
						AdvanceKeyStore ks = keystores.get(i);
						if (ks.name.equals(next.name)) {
							keystores.set(i, next);
							break;
						}
					}
				} else {
					mgr.create();
					keystores.add(next);
				}
				mgr.save(next.location, next.password());
			}
			
			@Override
			public void setParent(Component c) {
				throw new UnsupportedOperationException();
			}
			
			@Override
			public void setCurrentDir(File dir) {
				throw new UnsupportedOperationException();
			}
			
			@Override
			public List<AdvanceKeyEntry> queryKeys(String keyStore) throws Exception {
				return queryKeyStoreD(keyStore).queryKeys();
			}
			
			@Override
			public List<AdvanceKeyStore> queryKeyStores() throws Exception {
				throw new UnsupportedOperationException();
			}
			
			@Override
			public AdvanceKeyStore queryKeyStore(String name) throws Exception {
				return queryKeyStoreD(name);
			}
			
			@Override
			public void deleteKeys(String keyStore, Iterable<String> keys)
					throws Exception {
				queryKeyStoreD(keyStore).deleteKey(keys);
			}
			@Override
			public void generateKey(AdvanceGenerateKey key) throws Exception {
				queryKeyStoreD(key.keyStore).generateKey(key);
			}
			@Override
			public String exportCertificate(AdvanceKeyStoreExport request)
					throws Exception {
				return queryKeyStoreD(request.keyStore).exportCertificate(request.keyAlias);
			}
			@Override
			public String exportKey(AdvanceKeyStoreExport request) throws Exception {
				return queryKeyStoreD(request.keyStore).exportPrivateKey(request.keyAlias, request.password());
			}
			@Override
			public void importCertificate(AdvanceKeyStoreExport request, String data)
					throws Exception {
				queryKeyStoreD(request.keyStore).importCertificate(request.keyAlias, data);
			}
			@Override
			public void importKey(AdvanceKeyStoreExport request, String keyData,
					String certData) throws Exception {
				queryKeyStoreD(request.keyStore).importPrivateKey(request.keyAlias, request.password(), keyData, certData);
			}
			@Override
			public String exportSigningRequest(AdvanceKeyStoreExport request)
					throws Exception {
				return queryKeyStoreD(request.keyStore).exportSigningRequest(request.keyAlias, request.password());
			}
			@Override
			public void importSigningResponse(AdvanceKeyStoreExport request,
					String data) throws Exception {
				queryKeyStoreD(request.keyStore).importSigningResponse(request.keyAlias, request.password(), data);
			}
			
			@Override
			public void deleteKeyStore(String name) throws Exception {
				throw new UnsupportedOperationException();
			}
			@Override
			public File getCurrentDir() {
				throw new UnsupportedOperationException();
			}
		};
		
		d.setKeyManager(mgr);
		d.showBrowse(true);
		
		final CCDetailDialog<AdvanceKeyStore> dialog = dc.createDetailDialog(keystores, ks,
				d,
				new Func1<AdvanceKeyStore, String>() {
					@Override
					public String invoke(AdvanceKeyStore param1) {
						return param1.name + " [" + param1.location + "]";
					}
				},
				new Func1<String, Option<AdvanceKeyStore>>() {
					@Override
					public Option<AdvanceKeyStore> invoke(final String param1) {
						try {
							SwingUtilities.invokeLater(new Runnable() {
								@Override
								public void run() {
									d.queryKeys(param1);
								}
							});
							return Option.some(d.keyManager.queryKeyStore(param1));
						} catch (Throwable t) {
							return Option.error(t);
						}
					}
				},
				new Func1<AdvanceKeyStore, Throwable>() {
					@Override
					public Throwable invoke(AdvanceKeyStore param1) {
						try {
							d.keyManager.updateKeyStore(param1);
							return null;
						} catch (Throwable t) {
							return t;
						}
					}
				}
			);
		
		dialog.showEngineInfo(false);
		dialog.setResizable(true);
		dialog.pack();
		dialog.setLocationRelativeTo(this);
		dialog.setTitle(labels.get("Keystore details"));
		dialog.setVisible(true);
		
	}
	/**
	 * Update the keystore listings.
	 */
	void updateKeyStoreLists() {
		Object s1 = serverKeyStore.getSelectedItem();
		Object s2 = clientKeyStore.getSelectedItem();

		DefaultComboBoxModel<String> m1 = new DefaultComboBoxModel<String>();
		DefaultComboBoxModel<String> m2 = new DefaultComboBoxModel<String>();
		for (AdvanceKeyStore ks : keystores) {
			m1.addElement(ks.name);
			m2.addElement(ks.name);
		}
		serverKeyStore.setModel(m1);
		clientKeyStore.setModel(m2);
		serverKeyStore.setSelectedItem(s1);
		clientKeyStore.setSelectedItem(s2);
	}
	/** Add block file. */
	void doAddBlocks() {
		
	}
	/** Add schemas directory. */
	void doAddSchemas() {
		
	}
}
