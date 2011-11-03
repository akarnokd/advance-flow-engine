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
import java.io.IOException;
import java.security.KeyStore;
import java.util.Arrays;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.DefaultComboBoxModel;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.GroupLayout.Group;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
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
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.AbstractTableModel;
import javax.xml.stream.XMLStreamException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;

import edu.umd.cs.findbugs.annotations.NonNull;
import eu.advance.logistics.flow.engine.api.AdvanceCreateModifyInfo;
import eu.advance.logistics.flow.engine.api.AdvanceGenerateKey;
import eu.advance.logistics.flow.engine.api.AdvanceJDBCDrivers;
import eu.advance.logistics.flow.engine.api.AdvanceKeyEntry;
import eu.advance.logistics.flow.engine.api.AdvanceKeyStore;
import eu.advance.logistics.flow.engine.api.AdvanceKeyStoreExport;
import eu.advance.logistics.flow.engine.model.rt.AdvanceSchedulerPreference;
import eu.advance.logistics.flow.engine.model.rt.AdvanceSchedulerPriority;
import eu.advance.logistics.flow.engine.util.KeystoreManager;
import eu.advance.logistics.flow.engine.xml.typesystem.XElement;

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
	protected JComboBox clientKeyStore;
	/** The server keys and certificate. */
	protected JComboBox serverKeyStore;
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
	protected JComboBox dsDriver;
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
	/** The connection pool size. */
	protected JFormattedTextField dsPoolsize;
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
	/** The last directory getter-setter. */
	protected final CCGetterSetter<File> lastDir;
	/** The CPU scheduler. */
	protected SchedulerPanel cpuScheduler;
	/** The IO scheduler. */
	protected SchedulerPanel ioScheduler;
	/** The sequential scheduler. */
	protected SchedulerPanel sequentialScheduler;
	/** The current filename. */
	protected File fileName;
	/** The tabs. */
	protected JTabbedPane tabs;
	/** The working directory for the configuration elements. */
	protected File workDir;
	/**
	 * The label manager.
	 * @param labels the label manager
	 * @param dc the dialog creator
	 * @param lastDir the last dir
	 */
	public CCEngineDialog(@NonNull final LabelManager labels, 
			@NonNull final CCDialogCreator dc,
			@NonNull final CCGetterSetter<File> lastDir) {
		this.labels = labels;
		this.dc = dc;
		this.lastDir = lastDir;
		tabs = new JTabbedPane();
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		
		tabs.add(labels.get("Keystores"), createKeystores());
		tabs.add(labels.get("Listener"), createListener());
		tabs.add(labels.get("Datastore"), createDatastore());
		tabs.add(labels.get("Blocks & Schemas"), createBlocksAndSchemas());
		tabs.add(labels.get("Schedulers"), createSchedulers());
		
		JMenuBar mainMenu = new JMenuBar();
		
		JMenu file = new JMenu(labels.get("File"));
		JMenuItem fileNew = new JMenuItem(labels.get("New..."));
		fileNew.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				doNew();
			}
		});
		JMenuItem fileOpen = new JMenuItem(labels.get("Open..."));
		fileOpen.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				doOpen();
			}
		});
		JMenuItem fileSave = new JMenuItem(labels.get("Save"));
		fileSave.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				doSave();
			}
		});
		JMenuItem fileSaveAs = new JMenuItem(labels.get("Save as..."));
		fileSaveAs.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				doSaveAs();
			}
		});
		JMenuItem fileExit = new JMenuItem(labels.get("Exit"));
		fileExit.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				dispose();
			}
		});
		
		file.add(fileNew);
		file.add(fileOpen);
		file.addSeparator();
		file.add(fileSave);
		file.add(fileSaveAs);
		file.addSeparator();
		file.add(fileExit);
		
		mainMenu.add(file);
		
		setJMenuBar(mainMenu);
		
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
		
		serverKeyStore = new JComboBox();
		serverKeyAlias = new JTextField();
		serverPassword = new JPasswordField();
		serverPasswordAgain = new JPasswordField();
		
		clientKeyStore = new JComboBox();
		
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
			.addComponent(sp, 0, 250, Short.MAX_VALUE)
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
		dsDriver = new JComboBox(AdvanceJDBCDrivers.values());
		dsDriver.setSelectedItem(AdvanceJDBCDrivers.GENERIC);
		dsDriver.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				AdvanceJDBCDrivers b = (AdvanceJDBCDrivers)dsDriver.getSelectedItem();
				if (b != null) {
					dsUrl.setText(b.urlTemplate);
				}
			}
		});
		
		dsCustomDriverLabel = new JLabel(labels.get("Custom driver:"));
		
		dsUser = new JTextField();
		dsPassword = new JPasswordField();
		dsPasswordAgain = new JPasswordField();
		
		dsSchema = new JTextField();
		dsPoolsize = new JFormattedTextField(5);
		JLabel dsPoolsizeLabel = new JLabel(labels.get("Pool size:"));
		
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
					.addComponent(dsPoolsizeLabel)
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
					.addComponent(dsPoolsize)
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
			.addGroup(
				gl.createParallelGroup(Alignment.BASELINE)
				.addComponent(dsPoolsizeLabel)
				.addComponent(dsPoolsize, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
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
		dsPoolsize.setEnabled(!s);
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
		
		final JLabel blocksCount = new JLabel(labels.format("Records: %d", 0));
		final JLabel schemasCount = new JLabel(labels.format("Records: %d", 0));
		
		blocksModel.addTableModelListener(new TableModelListener() {
			@Override
			public void tableChanged(TableModelEvent e) {
				blocksCount.setText(labels.format("Records: %d", blocks.getRowCount()));
			}
		});
		schemasModel.addTableModelListener(new TableModelListener() {
			@Override
			public void tableChanged(TableModelEvent e) {
				schemasCount.setText(labels.format("Records: %d", schemas.getRowCount()));
			}
		});
		
		
		gl.setHorizontalGroup(
			gl.createParallelGroup(Alignment.CENTER)
			.addComponent(spBlocks)
			.addGroup(
				gl.createSequentialGroup()
				.addComponent(blockAdd)
				.addComponent(blockDelete)
			)
			.addComponent(blocksCount)
			.addComponent(middle)
			.addComponent(spSchemas)
			.addGroup(
				gl.createSequentialGroup()
				.addComponent(schemaAdd)
				.addComponent(schemaDelete)
			)
			.addComponent(schemasCount)
		);
		gl.setVerticalGroup(
			gl.createSequentialGroup()
			.addComponent(spBlocks, 0, 100, Short.MAX_VALUE)
			.addComponent(blocksCount)
			.addGroup(
				gl.createParallelGroup(Alignment.BASELINE)
				.addComponent(blockAdd)
				.addComponent(blockDelete)
			)
			.addComponent(middle, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
			.addComponent(spSchemas, 0, 100, Short.MAX_VALUE)
			.addComponent(schemasCount)
			.addGroup(
				gl.createParallelGroup(Alignment.BASELINE)
				.addComponent(schemaAdd)
				.addComponent(schemaDelete)
			)
		);
		
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
			ks.locationPrefix = workDir.getAbsolutePath() + "/";
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
					File prevLoc = new File(workDir, prev.location);
					mgr.load(prevLoc.getAbsolutePath(), prev.password());
					if (!prev.location.equals(next.location)) {
						prevLoc.delete();
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
				File nextLoc = new File(workDir, next.location);
				mgr.save(nextLoc.getAbsolutePath(), next.password());
				next.locationPrefix = workDir.getAbsolutePath() + "/";
			}
			
			@Override
			public void setParent(Component c) {
				throw new UnsupportedOperationException();
			}
			
			@Override
			public void setCurrentDir(File dir) {
				lastDir.set(dir);
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
				return lastDir.get();
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
		
		dialog.showCreateModify(false);
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

		DefaultComboBoxModel m1 = new DefaultComboBoxModel();
		DefaultComboBoxModel m2 = new DefaultComboBoxModel();
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
		EnterFileDialog dialog = new EnterFileDialog(false);
		dialog.setLocationRelativeTo(this);
		dialog.setVisible(true);
		if (dialog.approve) {
			blocksList.add(dialog.path.getText());
			blocksModel.fireTableDataChanged();
		}
	}
	/** Add schemas directory. */
	void doAddSchemas() {
		EnterFileDialog dialog = new EnterFileDialog(true);
		dialog.setLocationRelativeTo(this);
		dialog.setVisible(true);
		if (dialog.approve) {
			schemasList.add(dialog.path.getText());
			schemasModel.fireTableDataChanged();
		}
	}
	/**
	 * The dialog to enter a file name or browse for one.
	 * @author akarnokd, 2011.10.25.
	 */
	class EnterFileDialog extends JDialog {
		/** */
		private static final long serialVersionUID = -4773280755818890611L;
		/** Select directories only. */
		protected final boolean dirOnly;
		/** Approve? */
		protected boolean approve;
		/** The entered path. */
		protected JTextField path;
		/**
		 * Creates the dialog.
		 * @param dirOnly the directory only?
		 */
		public EnterFileDialog(boolean dirOnly) {
			this.dirOnly = dirOnly;
			setDefaultCloseOperation(DISPOSE_ON_CLOSE);
			
			path = new JTextField(30);
			
			ActionListener okAction = new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					doOk();
				}
			};
			
			JButton ok = new JButton(labels.get("OK"));
			ok.addActionListener(okAction);
			path.addActionListener(okAction);
			JButton cancel = new JButton(labels.get("Cancel"));
			cancel.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					doCancel();
				}
			});
			JButton browse = new JButton(labels.get("Browse..."));
			browse.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					doBrowse();
				}
			});
			
			JLabel pathLabel = new JLabel(labels.get("Path:"));
			
			GroupLayout gl = createLayout(getContentPane());
			
			gl.setHorizontalGroup(
				gl.createParallelGroup(Alignment.CENTER)
				.addGroup(
					gl.createSequentialGroup()
					.addComponent(pathLabel)
					.addComponent(path)
				)
				.addGroup(
					gl.createSequentialGroup()
					.addComponent(ok)
					.addComponent(browse)
					.addComponent(cancel)
				)
			);
			gl.setVerticalGroup(
				gl.createSequentialGroup()
				.addGroup(
					gl.createParallelGroup(Alignment.BASELINE)
					.addComponent(pathLabel)
					.addComponent(path)
				)
				.addGroup(
					gl.createParallelGroup(Alignment.BASELINE)
					.addComponent(ok)
					.addComponent(browse)
					.addComponent(cancel)
				)
			);
			setResizable(false);
			setModal(true);
			pack();
		}
		/** OK button action. */
		void doOk() {
			if (path.getText().isEmpty()) {
				GUIUtils.errorMessage(this, labels.get("Please enter a path!"));
				return;
			}
			approve = true;
			dispose();
		}
		/** Cancel button action. */
		void doCancel() {
			dispose();
		}
		/** Browse button action. */
		void doBrowse() {
			JFileChooser fc = new JFileChooser(lastDir.get());
			fc.setDialogTitle(dirOnly ? labels.get("Select a directory") : labels.get("Select a file"));
			fc.setFileSelectionMode(dirOnly ? JFileChooser.DIRECTORIES_ONLY : JFileChooser.FILES_ONLY);
			if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
				lastDir.set(dirOnly ? fc.getSelectedFile() : fc.getSelectedFile().getParentFile());
				path.setText(fc.getSelectedFile().toString());
			}
		}
		
	}
	/**
	 * The scheduler settings panel.
	 * @author akarnokd, 2011.10.25.
	 */
	class SchedulerPanel extends JPanel {
		/** */
		private static final long serialVersionUID = -8563147471843994750L;
		/** The preference type. */
		protected final AdvanceSchedulerPreference preference;
		/** All cores. */
		protected JRadioButton allCores;
		/** Fixed number of cores. */
		protected JRadioButton numCores;
		/** The number. */
		protected JFormattedTextField number;
		/** Priority. */
		protected JComboBox priority;
		/** The priority mode. */
		protected JRadioButton priorityMode;
		/** The priority percent. */
		protected JRadioButton priorityPercent;
		/** The priority number. */
		protected JFormattedTextField priorityNumber;
		/**
		 * Create the panel.
		 * @param preference the preference.
		 */
		public SchedulerPanel(AdvanceSchedulerPreference preference) {
			this.preference = preference;
			GroupLayout gl = createLayout(this);
			
			allCores = new JRadioButton(labels.get("All cores"));
			numCores = new JRadioButton(labels.get("Fixed core number:"));
			ButtonGroup bg = new ButtonGroup();
			bg.add(allCores);
			bg.add(numCores);
			
			number = new JFormattedTextField(1);
			priority = new JComboBox(AdvanceSchedulerPriority.values());
			priority.setSelectedItem(AdvanceSchedulerPriority.NORMAL);
			priorityMode = new JRadioButton(labels.get("Priority level:"));
			priorityPercent = new JRadioButton(labels.get("Priority:"));
			priorityNumber = new JFormattedTextField(50);
			JLabel priorityPercentLabel = new JLabel("%");

			ButtonGroup bg2 = new ButtonGroup();
			bg2.add(priorityMode);
			bg2.add(priorityPercent);

			priorityMode.setSelected(true);
			
			gl.setHorizontalGroup(
				gl.createParallelGroup()
				.addGroup(
					gl.createSequentialGroup()
					.addComponent(allCores)
					.addComponent(numCores)
					.addComponent(number)
				)
				.addGroup(
					gl.createSequentialGroup()
					.addComponent(priorityMode)
					.addComponent(priority)
					.addComponent(priorityPercent)
					.addComponent(priorityNumber)
					.addComponent(priorityPercentLabel)
				)
			);
			
			gl.setVerticalGroup(
				gl.createSequentialGroup()
				.addGroup(
					gl.createParallelGroup(Alignment.BASELINE)
					.addComponent(allCores)
					.addComponent(numCores)
					.addComponent(number)
				)
				.addGap(30)
				.addGroup(
					gl.createParallelGroup(Alignment.BASELINE)
					.addComponent(priorityMode)
					.addComponent(priority)
					.addComponent(priorityPercent)
					.addComponent(priorityNumber)
					.addComponent(priorityPercentLabel)
				)
			);
		}
	}
	/**
	 * Create the schedulers panel.
	 * @return the panel
	 */
	private JPanel createSchedulers() {
		JPanel p = new JPanel();
		
		GroupLayout gl = createLayout(p);
		
		cpuScheduler = new SchedulerPanel(AdvanceSchedulerPreference.CPU);
		cpuScheduler.allCores.setSelected(true);
		cpuScheduler.setBorder(BorderFactory.createTitledBorder(labels.get("CPU scheduler")));
		
		ioScheduler = new SchedulerPanel(AdvanceSchedulerPreference.IO);
		ioScheduler.number.setValue(32);
		ioScheduler.numCores.setSelected(true);
		ioScheduler.setBorder(BorderFactory.createTitledBorder(labels.get("I/O scheduler")));
		
		sequentialScheduler = new SchedulerPanel(AdvanceSchedulerPreference.SEQUENTIAL);
		sequentialScheduler.setBorder(BorderFactory.createTitledBorder(labels.get("Sequential scheduler")));
		sequentialScheduler.allCores.setEnabled(false);
		sequentialScheduler.numCores.setSelected(true);
		sequentialScheduler.numCores.setEnabled(false);
		sequentialScheduler.number.setEditable(false);
		
		gl.setHorizontalGroup(
			gl.createParallelGroup()
			.addComponent(cpuScheduler)
			.addComponent(ioScheduler)
			.addComponent(sequentialScheduler)
		);
		gl.setVerticalGroup(
			gl.createSequentialGroup()
			.addComponent(cpuScheduler)
			.addComponent(ioScheduler)
			.addComponent(sequentialScheduler)
		);
		
		return p;
	}
	/** Resets the schedulers to their default values. */
	protected void clearSchedulers() {
		cpuScheduler.allCores.setSelected(true);
		cpuScheduler.number.setValue(null);
		cpuScheduler.priorityMode.setSelected(true);
		cpuScheduler.priority.setSelectedItem(AdvanceSchedulerPriority.NORMAL);
		cpuScheduler.priorityNumber.setValue(50);
		
		ioScheduler.numCores.setSelected(true);
		ioScheduler.number.setValue(32);
		ioScheduler.priorityMode.setSelected(true);
		ioScheduler.priority.setSelectedItem(AdvanceSchedulerPriority.NORMAL);
		ioScheduler.priorityNumber.setValue(50);

		sequentialScheduler.priority.setSelectedItem(AdvanceSchedulerPriority.NORMAL);
		sequentialScheduler.priorityNumber.setValue(50);
	}
	/**
	 * Clear values from the listeners.
	 */
	void clearListener() {
		certAuthPort.setValue(8443);
		basicAuthPort.setValue(8444);
		serverKeyAlias.setText("");
		serverKeyStore.setSelectedIndex(-1);
		serverPassword.setText("");
		serverPasswordAgain.setText("");
		clientKeyStore.setSelectedIndex(-1);
	}
	/**
	 * Load the specific configuration.
	 * @param config the configuration XML
	 */
	public void load(XElement config) {
		keystores.clear();
		blocksList.clear();
		schemasList.clear();
		clearSchedulers();
		clearListener();

		for (XElement xkeystore : config.childrenWithName("keystore")) {
			AdvanceKeyStore aks = new AdvanceKeyStore();
			aks.name = xkeystore.get("name");
			aks.location = xkeystore.get("file");
			aks.locationPrefix = workDir.getAbsolutePath() + "/";

			aks.password(AdvanceCreateModifyInfo.getPassword(xkeystore, "password"));
			keystores.add(aks);
		}
		updateKeyStoreLists();
		
		XElement xlistener = config.childElement("listener");
		certAuthPort.setValue(xlistener.getInt("cert-auth-port"));
		basicAuthPort.setValue(xlistener.getInt("basic-auth-port"));
		serverKeyAlias.setText(xlistener.get("server-keyalias"));
		serverKeyStore.setSelectedItem(xlistener.get("server-keystore"));
		serverPassword.setText(new String(AdvanceCreateModifyInfo.getPassword(xlistener, "server-password")));
		serverPasswordAgain.setText(new String(AdvanceCreateModifyInfo.getPassword(xlistener, "server-password")));
		clientKeyStore.setSelectedItem(xlistener.get("client-keystore"));
		
		for (XElement xblocks : config.childrenWithName("block-registry")) {
			blocksList.add(xblocks.get("file"));
		}
		for (XElement xschemas : config.childrenWithName("schemas")) {
			schemasList.add(xschemas.get("location"));
		}
		
		XElement xdatastore = config.childElement("datastore");
		String driver = xdatastore.get("driver");
		char[] p = AdvanceCreateModifyInfo.getPassword(xdatastore, "password");
		if (p == null) {
			dsPassword.setText("");
			dsPasswordAgain.setText("");
		} else {
			dsPassword.setText(new String(p));
			dsPasswordAgain.setText(new String(p));
		}
		dsUrl.setText(xdatastore.get("url"));
		if ("LOCAL".equals(driver)) {
			dsLocal.setSelected(true);
			dsDriver.setSelectedIndex(-1);
			dsCustomDriver.setText("");
			dsSchema.setText("");
			dsUser.setText("");
		} else {
			boolean found = false;
			for (AdvanceJDBCDrivers d : AdvanceJDBCDrivers.values()) {
				if (d.driverClass.equals(driver)) {
					dsDriver.setSelectedItem(d);
					dsCustomDriver.setVisible(false);
					dsCustomDriverLabel.setVisible(false);
					found = true;
					break;
				}
			}
			if (!found) {
				dsDriver.setSelectedItem(AdvanceJDBCDrivers.GENERIC);
				dsCustomDriver.setText(driver);
				dsCustomDriver.setVisible(true);
				dsCustomDriverLabel.setVisible(true);
			}
			dsUser.setText(xdatastore.get("user"));
			dsSchema.setText(xdatastore.get("schema"));
			dsPoolsize.setValue(xdatastore.getInt("poolsize"));
		}
		
		for (XElement xscheduler : config.childrenWithName("scheduler")) {
			AdvanceSchedulerPreference type = AdvanceSchedulerPreference.valueOf(xscheduler.get("type"));
			switch (type) {
			case CPU:
				initScheduler(cpuScheduler, xscheduler);
				break;
			case IO:
				initScheduler(ioScheduler, xscheduler);
				break;
			case SEQUENTIAL:
				initScheduler(sequentialScheduler, xscheduler);
				break;
			default:
			}
		}
		
		doDatastoreTypeChange();
		keystoreModel.fireTableDataChanged();
		blocksModel.fireTableDataChanged();
		schemasModel.fireTableDataChanged();
	}
	/**
	 * Initialize the given scheduler panel from the source.
	 * @param panel the target panel
	 * @param xscheduler the source
	 */
	void initScheduler(SchedulerPanel panel, XElement xscheduler) {
		String concur = xscheduler.get("concurrency");
		String priority = xscheduler.get("priority");
		if ("ALL_CORES".equals(concur)) {
			panel.allCores.setSelected(true);
		} else {
			panel.numCores.setSelected(true);
			panel.number.setValue(Integer.parseInt(concur));
		}
		int p = Thread.NORM_PRIORITY;
		if (priority.length() > 0 && Character.isLetter(priority.charAt(0))) {
			AdvanceSchedulerPriority pi = AdvanceSchedulerPriority.valueOf(priority);
			panel.priority.setSelectedItem(pi);
			panel.priorityNumber.setValue(pi.priority * 10);
			panel.priorityMode.setSelected(true);
		} else {
			// priority in percent
			p = Thread.MIN_PRIORITY + Integer.parseInt(priority) * (Thread.MAX_PRIORITY - Thread.MIN_PRIORITY) / 100;
			panel.priorityPercent.setSelected(true);
			panel.priorityNumber.setValue(p);
		}
	}
	/**
	 * Save the dialog values.
	 * @return the created XElement or null if a validation error occurs
	 */
	public XElement save() {
		XElement result = new XElement("flow-engine-config");
		
		if (keystores.size() == 0) {
			GUIUtils.errorMessage(this, labels.get("You'll have to specify at least one keystore!"));
			return null;
		}
		
		XElement xlistener = result.add("listener");
		if (certAuthPort.getValue() == null) {
			GUIUtils.errorMessage(this, labels.get("Please enter a certificate-authentication based port number!"));
			return null;
		}
		if (basicAuthPort.getValue() == null) {
			GUIUtils.errorMessage(this, labels.get("Please enter a basic-authentication based port number!"));
			return null;
		}
		if (serverKeyStore.getSelectedItem() == null) {
			GUIUtils.errorMessage(this, labels.get("Please select the server keystore!"));
			return null;
		}
		if (clientKeyStore.getSelectedItem() == null) {
			GUIUtils.errorMessage(this, labels.get("Please select the client keystore!"));
			return null;
		}
		if (serverKeyAlias.getText().isEmpty()) {
			GUIUtils.errorMessage(this, labels.get("Please select the server key alias!"));
			return null;
		}
		
		xlistener.set("cert-auth-port", certAuthPort.getValue(), "basic-auth-port", basicAuthPort.getValue());
		xlistener.set("server-keystore", serverKeyStore.getSelectedItem(), "server-keyalias", serverKeyAlias.getText());
		xlistener.set("client-keystore", clientKeyStore.getSelectedItem());
		
		char[] p1 = serverPassword.getPassword();
		char[] p2 = serverPasswordAgain.getPassword();
		if (!Arrays.equals(p1, p2)) {
			GUIUtils.errorMessage(this, labels.get("The server key passwords mismatch!"));
			return null;
		}
		
		AdvanceCreateModifyInfo.setPassword(xlistener, "server-password", p1);
		
		for (String s : blocksList) {
			result.add("block-registry").set("file", s);
		}
		
		XElement xdatastore = result.add("datastore");

		if (dsUrl.getText().isEmpty()) {
			GUIUtils.errorMessage(this, labels.get("Please enter the datastore URL!"));
			return null;
		}

		char[] p3 = dsPassword.getPassword();
		char[] p4 = dsPasswordAgain.getPassword();
		if (p3 != null && p4 != null && !Arrays.equals(p3, p4)) {
			GUIUtils.errorMessage(this, labels.get("The datastore passwords mismatch!"));
			return null;
		}
		if (dsLocal.isSelected()) {
			xdatastore.set("driver", "LOCAL", "url", dsUrl.getText());
		} else {
			AdvanceJDBCDrivers d = (AdvanceJDBCDrivers)dsDriver.getSelectedItem();
			if (d == AdvanceJDBCDrivers.GENERIC) {
				if (dsCustomDriver.getText().isEmpty()) {
					GUIUtils.errorMessage(this, labels.get("Please enter the fully qualified class name of the datastore JDBC driver!"));
					return null;
				}
				xdatastore.set("driver", dsCustomDriver.getText());
			} else {
				xdatastore.set("driver", d.driverClass);
			}
			xdatastore.set("user", dsUser.getText());
			xdatastore.set("schema", dsSchema.getText());
			if (dsPoolsize.getValue() == null) {
				GUIUtils.errorMessage(this, labels.get("Please enter the JDBC pool size!"));
				return null;
			}
			xdatastore.set("poolsize", dsPoolsize.getValue());
		}
			
		if (p3 != null && p3.length > 0) {
			AdvanceCreateModifyInfo.setPassword(xdatastore, "password", p3);
		}
		
		for (AdvanceKeyStore aks : keystores) {
			XElement xkeystore = result.add("keystore");
			xkeystore.set("name", aks.name, "file", aks.location);
			AdvanceCreateModifyInfo.setPassword(xkeystore, "password", aks.password());
			if (aks.name.equals(serverKeyStore.getSelectedItem())) {
				try {
					KeyStore ks = aks.open();
					if (ks.getKey(serverKeyAlias.getText(), p1) == null) {
						GUIUtils.errorMessage(this, labels.get("The specified server key alias is missing or not a private key!"));
						return null;
					}
				} catch (Throwable t) {
					LOG.error(t.toString(), t);
					GUIUtils.errorMessage(this, labels.get("The server key could not be opened!"));
					return null;
				}
			}
		}
		
		for (String s : schemasList) {
			result.add("schemas").set("location", s);
		}
		if (!getSchedulerPanel(cpuScheduler, result.add("scheduler"))) {
			return null;
		}
		if (!getSchedulerPanel(ioScheduler, result.add("scheduler"))) {
			return null;
		}
		if (!getSchedulerPanel(sequentialScheduler, result.add("scheduler"))) {
			return null;
		}
		
		return result;
	}
	/**
	 * Store the scheduler panel contents into the XElement.
	 * @param panel the panel
	 * @param xscheduler the scheduler
	 * @return true if the validation succeded
	 */
	boolean getSchedulerPanel(SchedulerPanel panel, XElement xscheduler) {
		xscheduler.set("type", panel.preference);
		if (panel.allCores.isSelected()) {
			xscheduler.set("concurrency", "ALL_CORES");
		} else {
			if (panel.number.getValue() == null) {
				GUIUtils.errorMessage(this, labels.get("Please enter the core numbers!"));
				tabs.setSelectedIndex(4);
				panel.number.requestFocus();
				return false;
			}
			xscheduler.set("concurrency", panel.number.getValue());
		}
		if (panel.priorityMode.isSelected()) {
			xscheduler.set("priority", panel.priority.getSelectedItem());
		} else {
			if (panel.priorityNumber.getValue() == null) {
				GUIUtils.errorMessage(this, labels.get("Please enter the priority percent!"));
				tabs.setSelectedIndex(4);
				panel.number.requestFocus();
				return false;
			}
			int n = (Integer)panel.priorityNumber.getValue();
			if (n < 0) {
				n = 1;
			} else
			if (n > 100) {
				n = 100;
			}
			xscheduler.set("priority", n);
		}
		return true;
	}
	/**
	 * Open a configuration.
	 */
	public void doOpen() {
		JFileChooser fc = new JFileChooser(lastDir.get());
		fc.setFileFilter(new FileNameExtensionFilter("Engine config (*.XML)", "xml"));
		if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
			fileName = fc.getSelectedFile();
			lastDir.set(fileName.getParentFile());

			workDir = fc.getSelectedFile().getParentFile();
			
			doSelectWorkDir();
			
			try {
				load(XElement.parseXML(fileName));
			} catch (IOException ex) {
				LOG.error(ex.toString(), ex);
			} catch (XMLStreamException ex) {
				LOG.error(ex.toString(), ex);
			}

		}
	}
	/**
	 * Select the working directory.
	 */
	public void doSelectWorkDir() {
		JFileChooser fc;
		fc = new JFileChooser(lastDir.get());
		fc.setDialogTitle(labels.get("Select working directory"));
		fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		workDir = lastDir.get();
		if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
			workDir = fc.getSelectedFile();
		}
	}
	/**
	 * Create a new config.
	 */
	public void doNew() {
		fileName = null;
		keystores.clear();
		blocksList.clear();
		schemasList.clear();
		clearSchedulers();
		clearListener();
		updateKeyStoreLists();
		doSelectWorkDir();
	}
	/**
	 * Save the settings.
	 */
	public void doSave() {
		if (fileName == null) {
			doSaveAs();
		} else {
			saveToFile(fileName);
		}
	}
	/**
	 * Save the settings under a different name.
	 */
	public void doSaveAs() {
		JFileChooser fc = new JFileChooser(lastDir.get());
		fc.setFileFilter(new FileNameExtensionFilter("Engine config (*.XML)", "xml"));
		if (fc.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
			fileName = fc.getSelectedFile();
			lastDir.set(fileName.getParentFile());
			saveToFile(fileName);
		}
	}
	/**
	 * Save to the given filename.
	 * @param fileName the filename
	 */
	void saveToFile(File fileName) {
		XElement e = save();
		try {
			if (e != null) {
				e.save(fileName);
			}
		} catch (IOException ex) {
			GUIUtils.errorMessage(this, ex);
		}
	}
}
