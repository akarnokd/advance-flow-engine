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

import hu.akarnokd.reactive4java.base.Func0;
import hu.akarnokd.utils.xml.XNElement;
import hu.akarnokd.utils.xml.XNSerializable;
import hu.akarnokd.utils.xml.XNSerializables;

import java.awt.Component;
import java.awt.Container;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.text.ParseException;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.xml.stream.XMLStreamException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;

import eu.advance.logistics.flow.engine.AdvanceCompiler;
import eu.advance.logistics.flow.engine.AdvanceCompilerSettings;
import eu.advance.logistics.flow.engine.AdvanceEngineConfig;
import eu.advance.logistics.flow.engine.AdvanceRuntimeContext;
import eu.advance.logistics.flow.engine.LocalEngineControl;
import eu.advance.logistics.flow.engine.api.AdvanceEngineControl;
import eu.advance.logistics.flow.engine.api.core.AdvanceControlException;
import eu.advance.logistics.flow.engine.api.ds.AdvanceDataStore;
import eu.advance.logistics.flow.engine.api.ds.AdvanceUser;
import eu.advance.logistics.flow.engine.api.ds.AdvanceUserRights;
import eu.advance.logistics.flow.engine.api.impl.CheckedEngineControl;
import eu.advance.logistics.flow.engine.model.fd.AdvanceType;
import eu.advance.logistics.flow.engine.test.BasicLocalEngine;

/**
 * The Local Login dialog.
 * @author akarnokd, 2011.10.10.
 */
public class CCLocalLogin extends JDialog {
	/** The logger. */
	protected static final Logger LOG = LoggerFactory.getLogger(CCLocalLogin.class);
	/** */
	private static final long serialVersionUID = -5735501222051384516L;
	/**
	 * The last local login record.
	 * @author akarnokd, 2011.10.10.
	 */
	public static class LastLocalLogin implements XNSerializable {
		/** The path to the configuration file. */
		public String path;
		/** The working directory. */
		public String workDir;
		/** The timestamp of the last login. */
		public Date timestamp;
		/** The user name used to login. */
		public String user;
		/** Creates a new instance of this class. */
		public static final Func0<LastLocalLogin> CREATOR = new Func0<LastLocalLogin>() {
			@Override
			public LastLocalLogin invoke() {
				return new LastLocalLogin();
			}
		};
		@Override
		public void load(XNElement source) {
			path = source.get("path");
			try {
				timestamp = XNElement.parseDateTime(source.get("timestamp"));
			} catch (ParseException ex) {
				LOG.error(ex.toString(), ex);
			}
			user = source.get("user");
			workDir = source.get("work-dir");
			if (workDir == null || workDir.isEmpty()) {
				workDir = new File(path).getParent();
			}
		}
		@Override
		public void save(XNElement destination) {
			destination.set("path", path, "timestamp", timestamp, "user", user, "workDir", workDir);
		}
	}
	/** The rows. */
	final List<LastLocalLogin> rows = Lists.newArrayList();
	/** The label manager. */
	protected final LabelManager labels;
	/** The previous logins. */
	protected AbstractTableModel model = new AbstractTableModel() {
		/** */
		private static final long serialVersionUID = -3383336618875298771L;
		@Override
		public int getColumnCount() {
			return 3;
		}
		@Override
		public int getRowCount() {
			return rows.size();
		}
		@Override
		public Object getValueAt(int rowIndex, int columnIndex) {
			LastLocalLogin e = rows.get(rowIndex);
			switch (columnIndex) {
			case 0:
				return e.path;
			case 1:
				return e.timestamp;
			case 2:
				return e.user;
			default:
				return null;
			}
		}
		@Override
		public Class<?> getColumnClass(int columnIndex) {
			return Arrays.<Class<?>>asList(String.class, Date.class, String.class).get(columnIndex);
		};
		@Override
		public String getColumnName(int column) {
			return Arrays.asList("Path", "Timestamp", "User").get(column);
		};
	};
	/** The table. */
	protected JTable table;
	/** The opened engine URL. */
	protected URL engineURL;
	/** The opened engine. */
	protected AdvanceEngineControl engine;
	/** The backing configuration. */
	protected AdvanceEngineConfig engineConfig;
	/** The new user field. */
	protected JTextField newUser;
	/** The new path field. */
	protected JTextField newPath;
	/** The working directory. */
	protected JTextField workDir;
	/** Delete the selected entries. */
	protected JButton deleteButton;
	/** The login button. */
	protected JButton login;
	/** The last login save location. */
	protected String config = "advance-ecc-local-logins.xml";
	/** The working directory. */
	protected final File workingDirectory;
	/**
	 * Create the GUI.
	 * @param labels the label manager.
	 * @param workingDirectory where the configuration is saved
	 */
	public CCLocalLogin(LabelManager labels, File workingDirectory) {
		this.labels = labels;
		this.workingDirectory = workingDirectory;
		init();
	}
	/** Create the GUI elements. */
	private void init() {
		Container c = getContentPane();
		setTitle("Local login");
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				saveConfig();
			}
		});
		setModalityType(ModalityType.APPLICATION_MODAL);
		
		GroupLayout gl = new GroupLayout(c);
		c.setLayout(gl);
		gl.setAutoCreateContainerGaps(true);
		gl.setAutoCreateGaps(true);
		
		
		table = new JTable(model);
		table.setAutoCreateRowSorter(true);
		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		table.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
			@Override
			public void valueChanged(ListSelectionEvent e) {
				int i = table.getSelectedRow();
				if (i >= 0) {
					int idx = table.convertRowIndexToModel(i);
					newPath.setText(rows.get(idx).path);
					newUser.setText(rows.get(idx).user);
					workDir.setText(rows.get(idx).workDir);
					enableLoginIf();
				}
			}
		});
		table.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if (e.getClickCount() > 1) {
					doLogin();
				}
			}
		});
		table.getColumnModel().getColumn(1).setCellRenderer(new DefaultTableCellRenderer() {
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
		
		JScrollPane tableScroll = new JScrollPane(table);
		
		newPath = new JTextField();
		JButton browse = new JButton(labels.get("Browse..."));
		browse.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				doBrowse();
			}
		});
		workDir = new JTextField();
		
		newUser = new JTextField();
		DocumentListener dl = new DocumentListener() {
			@Override
			public void changedUpdate(DocumentEvent e) {
				enableLoginIf();
			}
			@Override
			public void insertUpdate(DocumentEvent e) {
				enableLoginIf();
			}
			@Override
			public void removeUpdate(DocumentEvent e) {
				enableLoginIf();
			}
		};
		newPath.getDocument().addDocumentListener(dl);
		newUser.getDocument().addDocumentListener(dl);
		newPath.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				doLogin();
			}
		});
		newUser.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				doLogin();
			}
		});
		
		JLabel newPathLabel = new JLabel(labels.get("Path:"));
		JLabel newUserLabel = new JLabel(labels.get("User:"));
		
		JButton newEntry = new JButton(labels.get("New"));
		newEntry.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				doNew();
			}
		});
		deleteButton = new JButton(labels.get("Delete"));
		deleteButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (JOptionPane.showConfirmDialog(CCLocalLogin.this, 
						labels.get("Are you sure?"), 
						labels.get("Delete records"), 
						JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
					int[] sel = table.getSelectedRows();
					for (int i : sel) {
						int j = table.convertRowIndexToModel(i);
						rows.remove(j);
					}
					model.fireTableDataChanged();
				}
			}
		});

		
		login = new JButton(labels.get("Login"));
		login.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				doLogin();
			}
		});
		login.setEnabled(false);
		
		JButton close = new JButton(labels.get("Close"));
		close.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				doClose();
			}
		});
		
		JSeparator bottomSep = new JSeparator(JSeparator.HORIZONTAL);
		JButton help = new JButton(new ImageIcon(getClass().getResource("help.png")));
		help.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				doHelp();
			}
		});
		
		int hh = help.getPreferredSize().height;
		
		JLabel workDirLabel = new JLabel(labels.get("Working directory:"));
		
		gl.setHorizontalGroup(
			gl.createParallelGroup(Alignment.CENTER)
			.addComponent(tableScroll)
			.addGroup(
				gl.createSequentialGroup()
				.addComponent(newEntry)
				.addComponent(deleteButton)
			)
			.addGroup(
				gl.createSequentialGroup()
				.addGroup(
					gl.createParallelGroup()
					.addComponent(newPathLabel)
					.addComponent(workDirLabel)
					.addComponent(newUserLabel)
				)
				.addGroup(
					gl.createParallelGroup()
					.addComponent(newPath)
					.addComponent(workDir)
					.addComponent(newUser)
				)
				.addComponent(browse)
			)
			.addGroup(
				gl.createSequentialGroup()
				.addComponent(help, hh, hh, hh)
				.addComponent(bottomSep)
			)
			.addGroup(
				gl.createSequentialGroup()
				.addComponent(login)
				.addComponent(close)
			)
		);
		
		gl.setVerticalGroup(
			gl.createSequentialGroup()
			.addComponent(tableScroll, 0, 200, Short.MAX_VALUE)
			.addGroup(
				gl.createParallelGroup(Alignment.BASELINE)
				.addComponent(newEntry)
				.addComponent(deleteButton)
			)
			.addGroup(
				gl.createParallelGroup(Alignment.BASELINE)
				.addComponent(newPathLabel)
				.addComponent(newPath, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
				.addComponent(browse)
			)
			.addGroup(
				gl.createParallelGroup(Alignment.BASELINE)
				.addComponent(workDirLabel)
				.addComponent(workDir, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
			)
			.addGroup(
				gl.createParallelGroup(Alignment.BASELINE)
				.addComponent(newUserLabel)
				.addComponent(newUser)
			)
			.addGroup(
				gl.createParallelGroup(Alignment.CENTER)
				.addComponent(help)
				.addComponent(bottomSep, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
			)
			.addGroup(
				gl.createParallelGroup(Alignment.BASELINE)
				.addComponent(login)
				.addComponent(close)
			)
		);
		
		loadConfig();
		GUIUtils.autoResizeColWidth(table, model);
		pack();
	}
	/** Enable login if both path and user fields are non-empty. */
	void enableLoginIf() {
		login.setEnabled(!newPath.getText().isEmpty() && !newUser.getText().isEmpty());
	}
	/** Browse for config file. */
	void doBrowse() {
		File f = new File(".");
		if (!newPath.getText().isEmpty()) {
			f = new File(newPath.getText());
		}
		JFileChooser fc = new JFileChooser(f);
		fc.addChoosableFileFilter(new FileNameExtensionFilter(labels.get("Config files (*.xml)"), "xml"));
		if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
			newPath.setText(fc.getSelectedFile().getAbsolutePath());
			workDir.setText(fc.getSelectedFile().getParent());
		}
	}
	/** Load last login information. */
	void loadConfig() {
		File cf = new File(workingDirectory, config);
		if (cf.canRead()) {
			try {
				XNElement e = XNElement.parseXML(cf);
				rows.clear();
				rows.addAll(XNSerializables.parseList(e, "last-login", LastLocalLogin.CREATOR));
			} catch (IOException ex) {
				LOG.error(ex.toString(), ex);
			} catch (XMLStreamException ex) {
				LOG.error(ex.toString(), ex);
			}
		}
		cf = new File(workingDirectory, "login-info.xml");
		if (cf.canRead()) {
			try {
				XNElement e = XNElement.parseXML(cf);
				outer:
				for (XNElement xi : e.childrenWithName("item")) {
					String address = xi.get("address");
					if (address.startsWith("file:")) {
						LastLocalLogin ll = new LastLocalLogin();
						ll.path = address.substring(5);
						while (ll.path.startsWith("/")) {
							ll.path = ll.path.substring(1);
						}
						if (!new File(ll.path).exists()) {
							FileWriter w = new FileWriter(ll.path + "/flow-engine-config.xml");
							try {
								w.write(BasicLocalEngine.defaultConfigFile());
							} finally {
								w.close();
							}
						}
						ll.workDir = ll.path;
						ll.path += "/flow-engine-config.xml";
						ll.user = xi.get("username");
						ll.timestamp = new Date(xi.getLong("lastLogin"));
						
						for (LastLocalLogin llOld : rows) {
							if (llOld.path.equals(ll.path)) {
								continue outer;
							}
						}
						rows.add(ll);
					}
				}
			} catch (IOException ex) {
				LOG.error(ex.toString(), ex);
			} catch (XMLStreamException ex) {
				LOG.error(ex.toString(), ex);
			}
		}
	}
	/** Save a configuration. */
	void saveConfig() {
		try {
			XNSerializables.storeList("last-logins", "last-login", rows).save(new File(workingDirectory, config));
		} catch (IOException ex) {
			LOG.error(ex.toString(), ex);
		}			
	}
	/** Close the window. */
	void doClose() {
		WindowEvent e = new WindowEvent(this, WindowEvent.WINDOW_CLOSING);
		Toolkit.getDefaultToolkit().getSystemEventQueue().postEvent(e);
	}
	/** Show help. */
	void doHelp() {
		// TODO implement
	}
	/**
	 * Login.
	 */
	void doLogin() {
		if (!newPath.getText().isEmpty() && !newUser.getText().isEmpty()) {
			if (openLocalEngine(new File(newPath.getText()), newUser.getText(), workDir.getText())) {
				doUpdateRows();
				doClose();
			}
		}
	}
	/**
	 * Open a local engine.
	 * @param file the engine configuration file
	 * @param userName the logged-in user name
	 * @return if the opening was successful
	 * @param workDir the working directory
	 */
	protected boolean openLocalEngine(File file, String userName, String workDir) {
		engineConfig = new AdvanceEngineConfig();
		try {
			engineConfig.initialize(XNElement.parseXML(file), workDir);
			
			AdvanceCompilerSettings<XNElement, AdvanceType, AdvanceRuntimeContext> compilerSettings = engineConfig.createCompilerSettings();
			AdvanceCompiler<XNElement, AdvanceType, AdvanceRuntimeContext> compiler = 
					new AdvanceCompiler<XNElement, AdvanceType, AdvanceRuntimeContext>(compilerSettings);
			
			AdvanceDataStore datastore = engineConfig.datastore();
			if (datastore.queryUsers().isEmpty()) {
				addFirst(datastore);
			}
			engine = new LocalEngineControl(datastore, engineConfig.schemas, compiler, compiler, workDir) {
				@Override
				public void shutdown() throws IOException,
						AdvanceControlException {
					try {
						super.shutdown();
					} finally {
						engineConfig.close();
					}
				}
			};
			engine = new CheckedEngineControl(engine, userName);
			
			engineURL = file.toURI().toURL();
			return true;
		} catch (IOException ex) {
			LOG.error(ex.toString(), ex);
			errorMessage(ex.toString());
		} catch (XMLStreamException ex) {
			LOG.error(ex.toString(), ex);
			errorMessage(ex.toString());
		} catch (AdvanceControlException ex) {
			LOG.error(ex.toString(), ex);
			errorMessage(ex.toString());
		} catch (Throwable t) {
			GUIUtils.errorMessage(this, t);
		}
		return false;
	}
	/** 
	 * Add the first user for ane empty datastore.
	 * @param ds the datastore 
	 */
	void addFirst(AdvanceDataStore ds) {
		AdvanceUser u = new AdvanceUser();
		u.name = "admin";
		u.password("admin".toCharArray());
		u.thousandSeparator = ',';
		u.decimalSeparator = '.';
		u.dateFormat = "yyyy-MM-dd";
		u.dateTimeFormat = "yyyy-MM-dd HH:mm:ss";
		u.numberFormat = "#,###";
		u.enabled = true;
		u.passwordLogin = true;
		u.rights.addAll(Arrays.asList(AdvanceUserRights.values()));
		u.createdAt = new Date();
		u.createdBy = "setup";
		u.modifiedAt = new Date();
		u.modifiedBy = "setup";
		u.email = "admin@advance-logistics.eu";
		try {
			ds.updateUser(u);
		} catch (IOException ex) {
			LOG.error(ex.toString(), ex);
		} catch (AdvanceControlException ex) {
			LOG.error(ex.toString(), ex);
		}
	}
	/**
	 * Display the login dialog.
	 * @return true if user logged in successfully
	 */
	public boolean display() {
		engine = null;
		engineURL = null;
		setVisible(true);
		
		return engine != null;
	}
	/**
	 * Display an error dialog with the message.
	 * @param text the message
	 */
	void errorMessage(String text) {
		JOptionPane.showMessageDialog(this, text, labels.get("Error"), JOptionPane.ERROR_MESSAGE);
	}
	/**
	 * @return the engine
	 */
	public AdvanceEngineControl takeEngine() {
		AdvanceEngineControl e = engine;
		engine = null;
		return e;
	}
	/**
	 * Takes the engine config record.
	 * @return the engine config record
	 */
	public AdvanceEngineConfig takeConfig() {
		AdvanceEngineConfig ec = engineConfig;
		engineConfig = null;
		return ec;
	}
	/**
	 * @return the engineURL
	 */
	public URL takeEngineURL() {
		return engineURL;
	}
	/** Clear the fields. */
	void doNew() {
		newPath.setText("");
		newUser.setText("");
		workDir.setText("");
		table.clearSelection();
	}
	/**
	 * Update the last visit rows.
	 */
	void doUpdateRows() {
		int idx = 0;
		for (LastLocalLogin lll : rows) {
			if (lll.path.equals(newPath.getText()) 
					&& lll.user.equals(newUser.getText())
					&& lll.workDir.equals(workDir.getText())) {
				lll.timestamp = new Date();
				model.fireTableDataChanged();
				return;
			}
			idx++;
		}
		LastLocalLogin lll = new LastLocalLogin();
		lll.path = newPath.getText();
		lll.timestamp = new Date();
		lll.user = newUser.getText();
		lll.workDir = workDir.getText();
		
		rows.add(lll);
		model.fireTableRowsInserted(idx, idx);
	}
}
