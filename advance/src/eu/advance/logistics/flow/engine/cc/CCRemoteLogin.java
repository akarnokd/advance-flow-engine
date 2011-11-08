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
import hu.akarnokd.reactive4java.base.Func0;
import hu.akarnokd.reactive4java.base.Func1;
import hu.akarnokd.reactive4java.interactive.Interactive;

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
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.text.ParseException;
import java.util.Date;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.xml.stream.XMLStreamException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Objects;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

import edu.umd.cs.findbugs.annotations.NonNull;
import eu.advance.logistics.flow.engine.api.AdvanceEngineControl;
import eu.advance.logistics.flow.engine.api.AdvanceHttpAuthentication;
import eu.advance.logistics.flow.engine.api.ds.AdvanceCreateModifyInfo;
import eu.advance.logistics.flow.engine.api.ds.AdvanceKeyStore;
import eu.advance.logistics.flow.engine.api.ds.AdvanceLoginType;
import eu.advance.logistics.flow.engine.api.impl.HttpRemoteEngineControl;
import eu.advance.logistics.flow.engine.util.KeystoreManager;
import eu.advance.logistics.flow.engine.xml.typesystem.XElement;
import eu.advance.logistics.flow.engine.xml.typesystem.XSerializable;
import eu.advance.logistics.flow.engine.xml.typesystem.XSerializables;

/**
 * The remote logind dialog.
 * @author akarnokd, 2011.10.20.
 */
public class CCRemoteLogin extends JDialog {
	/** The logger. */
	protected static final Logger LOG = LoggerFactory.getLogger(CCRemoteLogin.class);
	/** */
	private static final long serialVersionUID = -7743868608105682507L;
	/** The label manager. */
	protected final LabelManager labels;
	/** The login type. */
	protected LoginTypePanel login;
	/** The previous logins. */
	protected JTable table;
	/** The table model. */
	protected AbstractTableModel model;
	/** The count of elements. */
	protected JLabel records;
	/** The selected/new address. */
	protected JTextField address;
	/** The login button. */
	protected JButton loginButton;
	/** The cancel button. */
	protected JButton cancelButton;
	/** The connected engine. */
	protected AdvanceEngineControl engine;
	/** The opened engine URL. */
	protected URL engineURL;
	/** The remote login details. */
	class RemoteLogin implements XSerializable {
		/** The remote address. */
		String address;
		/** The user. */
		String user;
		/** The password. */
		char[] password;
		/** Keystore. */
		String keystore;
		/** Login type. */
		AdvanceLoginType type;
		/** Last login date. */
		Date timestamp;
		/** The verification keystore. */
		String verify;
		/** The concrete certificate file. */
		String cert;
		@Override
		public void load(XElement source) {
			address = source.get("address");
			verify = source.get("verify");
			cert = source.get("cert");
			user = source.get("user");
			password = AdvanceCreateModifyInfo.getPassword(source, "password");
			keystore = source.get("keystore");
			String stype = source.get("type");
			if (stype == null || stype.isEmpty()) {
				type = AdvanceLoginType.BASIC;
			} else {
				type = AdvanceLoginType.valueOf(stype);
			}
			try {
				timestamp = XElement.parseDateTime(source.get("timestamp"));
			} catch (ParseException ex) {
				LOG.error(ex.toString(), ex);
			}
		}
		@Override
		public void save(XElement destination) {
			destination.set("address", address, "user", user, 
					"keystore", keystore, "type", type, 
					"timestamp", timestamp, "verify", verify, "cert", cert);
			AdvanceCreateModifyInfo.setPassword(destination, "password", password);
		}
	}
	/** The rows. */
	final List<RemoteLogin> rows = Lists.newArrayList();
	/** The help panel. */
	protected HelpPanel help;
	/** New entry. */
	protected JButton newButton;
	/** Delete selected entries. */
	protected JButton deleteButton;
	/** The keystore to use for verifying the server key. */
	protected JComboBox serverVerify;
	/** The server certificate file. */
	protected JTextField serverCert;
	/**
	 * The remote logins.
	 */
	protected final String configFile = "advance-ecc-remote-logins.xml";
	/** The list of keystores. */
	protected final List<AdvanceKeyStore> keystores = Lists.newArrayList();
	/** The login action. */
	protected Action0 loginAction;
	/** The working directory. */
	protected final File workingDirectory;
	/** The last load/save directory. */
	protected final CCGetterSetter<File> lastDirectory;
	/**
	 * Create the GUI.
	 * @param labels the label manager
	 * @param workingDirectory the working directory
	 * @param lastDirectory the last file open/save directory
	 */
	public CCRemoteLogin(@NonNull final LabelManager labels, File workingDirectory, 
			CCGetterSetter<File> lastDirectory) {
		this.labels = labels;
		this.workingDirectory = workingDirectory;
		this.lastDirectory = lastDirectory;
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		setTitle(labels.get("Remote login"));
		
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				save();
			}
		});
		
		login = new LoginTypePanel(labels);
		login.showNone(false);
		login.setBorder(BorderFactory.createTitledBorder(labels.get("Authentication")));
		address = new JTextField();
		records = new JLabel(labels.format("Records: %d", 0));
		
		serverVerify = new JComboBox();
		serverCert = new JTextField();
		JLabel serverCertLabel = new JLabel(labels.get("Server certificate file:"));
		
		JButton browseCert = new JButton(labels.get("Browse..."));
		browseCert.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				doBrowseCert();
			}
		});
		
		Container c = getContentPane();
		GroupLayout gl = new GroupLayout(c);
		c.setLayout(gl);
		gl.setAutoCreateContainerGaps(true);
		gl.setAutoCreateGaps(true);
		
		newButton = new JButton(labels.get("New"));
		newButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				address.getText();
				login.clear();
				login.setLoginType(AdvanceLoginType.BASIC);
			}
		});
		deleteButton = new JButton(labels.get("Delete"));
		deleteButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (JOptionPane.showConfirmDialog(CCRemoteLogin.this, 
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
		
		model = new AbstractTableModel() {
			/** */
			private static final long serialVersionUID = -1160295295811079774L;
			/** The column names. */
			final String[] cols = { labels.get("Address"), labels.get("User"), labels.get("Date")};
			/** The column classes. */
			final Class<?>[] classes = { String.class, String.class, Date.class };
			@Override
			public Object getValueAt(int rowIndex, int columnIndex) {
				RemoteLogin r = rows.get(rowIndex);
				switch (columnIndex) {
				case 0:
					return r.address;
				case 1:
					return r.user;
				case 2:
					return r.timestamp;
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
				return cols[column];
			}
		};
		
//		setModal(true);
		
		table = new JTable(model);
		table.setAutoCreateRowSorter(true);
		JScrollPane scroll = new JScrollPane(table);
		JLabel addressLabel = new JLabel(labels.get("Address:"));
		help = new HelpPanel();
		table.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if (e.getClickCount() > 1 && table.getSelectedRow() >= 0) {
					doLogin();
				}
			}
		});
		
		loginButton = new JButton(labels.get("Login"));
		loginButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				doLogin();
			}
		});
		cancelButton = new JButton(labels.get("Cancel"));
		cancelButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				close();
			}
		});
		
		model.addTableModelListener(new TableModelListener() {
			@Override
			public void tableChanged(TableModelEvent e) {
				records.setText(labels.format("Records: %d", table.getRowCount()));
			}
		});
		address.getDocument().addDocumentListener(new DocumentListener() {
			@Override
			public void removeUpdate(DocumentEvent e) {
				loginButton.setEnabled(!address.getText().isEmpty());
			}
			
			@Override
			public void insertUpdate(DocumentEvent e) {
				loginButton.setEnabled(!address.getText().isEmpty());
			}
			
			@Override
			public void changedUpdate(DocumentEvent e) {
				loginButton.setEnabled(!address.getText().isEmpty());
			}
		});
		
		login.setLoginType(AdvanceLoginType.BASIC);
		
		table.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
			
			@Override
			public void valueChanged(ListSelectionEvent e) {
				int idx = table.getSelectedRow();
				if (idx >= 0) {
					idx = table.convertRowIndexToModel(idx);
					RemoteLogin rl = rows.get(idx);
					
					address.setText(rl.address);
					if (rl.verify == null || rl.verify.isEmpty()) {
						serverVerify.setSelectedIndex(0);
					} else {
						serverVerify.setSelectedItem(rl.verify);
					}
					serverCert.setText(rl.cert);

					login.clear();
					login.setLoginType(rl.type);
					if (rl.type == AdvanceLoginType.BASIC) {
						login.setUserName(rl.user);
						login.setUserPassword(rl.password);
					} else {
						login.setKeyStore(rl.keystore);
						login.setAlias(rl.user);
						login.setKeyPassword(rl.password);
					}
				} else {
					login.clear();
					address.setText("");
					serverCert.setText("");
					serverVerify.setSelectedIndex(0);
				}
			}
		});
		table.getColumnModel().getColumn(2).setCellRenderer(new DefaultTableCellRenderer() {
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
		JLabel serverVerifyLabel = new JLabel(labels.get("Server verification keystore:"));
		
		gl.setHorizontalGroup(
			gl.createParallelGroup(Alignment.CENTER)
			.addComponent(scroll)
			.addGroup(
				gl.createSequentialGroup()
				.addComponent(newButton)
				.addComponent(deleteButton)
			)
			.addGroup(
				gl.createSequentialGroup()
				.addComponent(addressLabel)
				.addComponent(address)
			)
			.addGroup(
				gl.createSequentialGroup()
				.addComponent(serverVerifyLabel)
				.addComponent(serverVerify)
			)
			.addGroup(
				gl.createSequentialGroup()
				.addComponent(serverCertLabel)
				.addComponent(serverCert)
				.addComponent(browseCert)
			)
			.addComponent(login)
			.addComponent(help)
			.addGroup(
				gl.createSequentialGroup()
				.addComponent(loginButton)
				.addComponent(cancelButton)
			)
		);
		gl.setVerticalGroup(
			gl.createSequentialGroup()
			.addComponent(scroll, 0, 250, Short.MAX_VALUE)
			.addGroup(
				gl.createParallelGroup(Alignment.BASELINE)
				.addComponent(newButton)
				.addComponent(deleteButton)
			)
			.addGroup(
				gl.createParallelGroup(Alignment.BASELINE)
				.addComponent(addressLabel)
				.addComponent(address, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
			)
			.addGroup(
				gl.createParallelGroup(Alignment.BASELINE)
				.addComponent(serverVerifyLabel)
				.addComponent(serverVerify, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
			)
			.addGroup(
				gl.createParallelGroup(Alignment.BASELINE)
				.addComponent(serverCertLabel)
				.addComponent(serverCert)
				.addComponent(browseCert)
			)
			.addComponent(login)
			.addComponent(help, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
			.addGroup(
				gl.createParallelGroup(Alignment.BASELINE)
				.addComponent(loginButton)
				.addComponent(cancelButton)
			)	
		);
		load();
		pack();
	}
	/**
	 * Load values of previous logins.
	 */
	protected void load() {
		rows.clear();
		File cf = new File(workingDirectory, configFile);
		if (cf.canRead()) {
			try {
				for (RemoteLogin rl : XSerializables.parseList(XElement.parseXML(cf), "remote-login", new Func0<RemoteLogin>() {
					@Override
					public RemoteLogin invoke() {
						return new RemoteLogin();
					}
				})) {
					addToRows(rl);
				}
			} catch (IOException ex) {
				LOG.error(ex.toString(), ex);
			} catch (XMLStreamException ex) {
				LOG.error(ex.toString(), ex);
			}
		}
		cf = new File(workingDirectory, "login-info.xml");
		if (cf.canRead()) {
			try {
				XElement e = XElement.parseXML(cf);
				for (XElement xi : e.childrenWithName("item")) {
					String address = xi.get("address");
					if (!address.startsWith("file:")) {
						RemoteLogin ll = new RemoteLogin();
						ll.address = address;
						ll.type = AdvanceLoginType.BASIC;
						ll.user = xi.get("username");
						ll.password = xi.get("password").toCharArray();
						ll.timestamp = new Date(xi.getLong("lastLogin"));
						ll.cert = xi.get("cert");
						
						addToRows(ll);
					}
				}
			} catch (IOException ex) {
				LOG.error(ex.toString(), ex);
			} catch (XMLStreamException ex) {
				LOG.error(ex.toString(), ex);
			}
		}
		GUIUtils.autoResizeColWidth(table, model);
	}
	/**
	 * Add to the available rows but only if no duplication is present.
	 * @param rl the remote loing settings
	 */
	void addToRows(RemoteLogin rl) {
		for (RemoteLogin rl2 : rows) {
			if (rl2.address.equals(rl.address) 
					&& rl2.user.equals(rl.user) 
					&& rl2.type == rl.type 
					&& Objects.equal(rl2.cert, rl.cert)) {
				return;
			}
		}
		rows.add(rl);
	}
	/**
	 * Save values of the logins.
	 */
	protected void save() {
		try {
			XSerializables.storeList("remote-logins", "remote-login", rows).save(new File(workingDirectory, configFile));
		} catch (IOException ex) {
			LOG.error(ex.toString(), ex);
		}
	}
	/**
	 * Close the dialog.
	 */
	protected void close() {
		Toolkit.getDefaultToolkit().getSystemEventQueue().postEvent(new WindowEvent(this, WindowEvent.WINDOW_CLOSING));
	}
	/**
	 * Set the available keystores.
	 * @param keystores the keystores
	 */
	public void setKeyStores(Iterable<AdvanceKeyStore> keystores) {
		this.keystores.clear();
		Iterables.addAll(this.keystores, keystores);
		login.setKeyStores(Interactive.select(keystores, new Func1<AdvanceKeyStore, String>() {
			@Override
			public String invoke(AdvanceKeyStore param1) {
				return param1.name;
			}
		}));
		DefaultComboBoxModel model = new DefaultComboBoxModel();
		model.addElement("");
		for (AdvanceKeyStore ks : keystores) {
			model.addElement(ks.name);
		}
		serverVerify.setModel(model);
	}
	/**
	 * Set the fields.
	 * @param rl the object
	 * @return true if validation succeded
	 */
	boolean setFields(RemoteLogin rl) {
		rl.address = address.getText();
		rl.type = login.getLoginType();
		rl.timestamp = new Date();
		if (serverVerify.getSelectedIndex() > 0) {
			rl.verify = (String)serverVerify.getSelectedItem();
		}
		rl.cert = serverCert.getText();
		if (rl.type == AdvanceLoginType.BASIC) {
			rl.user = login.getUserName();
			if (rl.user.isEmpty()) {
				GUIUtils.errorMessage(this, labels.get("Please enter an user name!"));
				return false;
			}
			rl.password = login.getUserPassword();
		} else {
			rl.user = login.getAlias();
			if (rl.user.isEmpty()) {
				GUIUtils.errorMessage(this, labels.get("Please enter a key alias name!"));
				return false;
			}
			rl.password = login.getKeyPassword();
			rl.keystore = login.getKeyStore();
			if (rl.keystore == null || rl.keystore.isEmpty()) {
				GUIUtils.errorMessage(this, labels.get("Please select a keystore!"));
				return false;
			}
		}
		
		return true;
	}
	/**
	 * Perform the login.
	 */
	void doLogin() {
		RemoteLogin selected = null;
		for (RemoteLogin rl : rows) {
			if (Objects.equal(rl.address, address.getText())) {
				if (setFields(rl)) {
					selected = rl;
					GUIUtils.autoResizeColWidth(table, model);
					break;
				}
			}
		}
		if (selected == null) {
			RemoteLogin rl = new RemoteLogin();
			if (setFields(rl)) {
				rows.add(rl);
				model.fireTableDataChanged();
				GUIUtils.autoResizeColWidth(table, model);
				selected = rl;
			}
		}
		final RemoteLogin s = selected;
		GUIUtils.getWorker(new WorkItem() {
			/** The login exception. */
			Throwable t;
			/** The engine control. */
			AdvanceEngineControl ec;
			/** The connection URL. */
			URL u;
			@Override
			public void run() {
				try {
					AdvanceHttpAuthentication auth = new AdvanceHttpAuthentication();

					auth.name = s.user;
					auth.password(s.password);
					auth.loginType = s.type;
					if (s.type == AdvanceLoginType.CERTIFICATE) {
						for (AdvanceKeyStore aks : keystores) {
							if (aks.name.equals(s.keystore)) {
								auth.password(s.password);
								auth.authStore = aks.open();
								break;
							}
						}
						if (auth.authStore == null) {
							t = new IllegalArgumentException("Missing authentication keystore " + s.keystore);
						}
					}
					for (AdvanceKeyStore aks : keystores) {
						if (aks.name.equals(s.verify)) {
							auth.certStore = aks.open();
							break;
						}
					}
					u = new URL(s.address);
					if ((s.verify == null || s.verify.isEmpty()) && s.cert != null && !s.cert.isEmpty()) {
	                    KeystoreManager mgr = new KeystoreManager();
	                    mgr.create();
	                    FileInputStream in = new FileInputStream(s.cert);
	                    try {
	                            mgr.importCertificate(u.getHost(), in);
	                    } finally {
	                            in.close();
	                    }

	                    auth.certStore = mgr.getKeyStore();
					}
					ec = new HttpRemoteEngineControl(u, auth);
					// try to retrieve the user
					ec.getUser();
				} catch (Throwable t) {
					this.t = t;
				}
			}
			@Override
			public void done() {
				if (t != null) {
					GUIUtils.errorMessage(CCRemoteLogin.this, t);
				} else {
					engine = ec;
					engineURL = u;
					close();
					loginAction.invoke();
				}
			}
		}).execute();
	}
	/**
	 * Display the dialog and return the control if the user logged in.
	 * @return the engine control
	 */
	public boolean display() {
		engine = null;
		setVisible(true);
		return engine != null;
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
	 * @return the engineURL
	 */
	public URL takeEngineURL() {
		return engineURL;
	}
	/**
	 * Set the action to perform on a successful login.
	 * @param action the action.
	 */
	public void setLoginAction(Action0 action) {
		this.loginAction = action;
		
	}
	/**
	 * Browse for server certificate.
	 */
	void doBrowseCert() {
		JFileChooser fc = new JFileChooser(lastDirectory.get());
		fc.setFileFilter(new FileNameExtensionFilter("Certificates (*.CER)", "cer", "cert", "crt"));
		if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
			File f = fc.getSelectedFile();
			lastDirectory.set(f.getParentFile());
			serverCert.setText(f.getAbsolutePath());
		}
	}
}
