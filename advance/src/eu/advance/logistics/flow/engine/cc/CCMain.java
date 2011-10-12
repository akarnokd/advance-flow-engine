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

import hu.akarnokd.reactive4java.base.Action0;
import hu.akarnokd.reactive4java.base.Action1;
import hu.akarnokd.reactive4java.base.Func1;
import hu.akarnokd.reactive4java.base.Func2;
import hu.akarnokd.reactive4java.base.Pair;

import java.awt.Container;
import java.awt.GridBagLayout;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

import eu.advance.logistics.flow.engine.AdvanceFlowEngine;
import eu.advance.logistics.flow.engine.api.AdvanceControlException;
import eu.advance.logistics.flow.engine.api.AdvanceCreateModifyInfo;
import eu.advance.logistics.flow.engine.api.AdvanceEmailBox;
import eu.advance.logistics.flow.engine.api.AdvanceEngineControl;
import eu.advance.logistics.flow.engine.api.AdvanceEngineVersion;
import eu.advance.logistics.flow.engine.api.AdvanceFTPDataSource;
import eu.advance.logistics.flow.engine.api.AdvanceFTPProtocols;
import eu.advance.logistics.flow.engine.api.AdvanceJDBCDataSource;
import eu.advance.logistics.flow.engine.api.AdvanceJMSEndpoint;
import eu.advance.logistics.flow.engine.api.AdvanceKeyStore;
import eu.advance.logistics.flow.engine.api.AdvanceLocalFileDataSource;
import eu.advance.logistics.flow.engine.api.AdvanceRealm;
import eu.advance.logistics.flow.engine.api.AdvanceSOAPChannel;
import eu.advance.logistics.flow.engine.api.AdvanceUser;
import eu.advance.logistics.flow.engine.api.AdvanceUserRealmRights;
import eu.advance.logistics.flow.engine.api.AdvanceUserRights;
import eu.advance.logistics.flow.engine.api.AdvanceWebDataSource;
import eu.advance.logistics.flow.engine.api.Identifiable;

/**
 * The main window of the engine control center.
 * @author karnokd, 2011.10.07.
 */
public class CCMain extends JFrame implements LabelManager {
	/** The logger. */
	protected static final Logger LOG = LoggerFactory.getLogger(CCMain.class);
	/** */
	private static final long serialVersionUID = 4606185730689523838L;
	/** The engine control. */
	protected AdvanceEngineControl engine;
	/** The engine URL. */
	protected URL engineURL;
	/** The engine version. */
	protected AdvanceEngineVersion version;
	/** The logged in user. */
	protected AdvanceUser user;
	/** The config file. */
	protected final	File configFile = new File("advance-flow-engine-control-center-config.xml");
	/** The menus to enable with rights. */
	public Multimap<JMenuItem, AdvanceUserRights> menusToEnable = HashMultimap.create();
	/** The conencted URL. */
	private JLabel urlLabel;
	/** The connected version. */
	private JLabel verLabel;
	/** The connected user. */
	private JLabel userLabel;
	/** We opened a local engine. */
	protected boolean localEngine;
	/**
	 * Returns a label for the given key.
	 * @param key the key
	 * @return the label
	 */
	@Override
	public String get(String key) {
		return key;
	}
	/**
	 * Format a label with the given values.
	 * @param key the label key
	 * @param values the values
	 * @return the formatted string
	 */
	@Override
	public String format(String key, Object... values) {
		return String.format(get(key), values);
	}
	/** Load the configuration. */
	protected void loadConfig() {
		if (configFile.canRead()) {
			Properties props = new Properties();
			try {
				FileInputStream fin = new FileInputStream(configFile);
				try {
					props.loadFromXML(fin);
					applyConfig(props);
				} finally {
					fin.close();
				}
			} catch (IOException ex) {
				LOG.error(ex.toString(), ex);
			}
		}
	}
	/**
	 * Apply the configuration values.
	 * @param props the properties
	 */
	protected void applyConfig(Properties props) {
		applyFrameState(this, props, "main-");
	}
	/**
	 * Apply the state to the target frame named by the prefix within the properties.
	 * @param target the target frame
	 * @param props the properties
	 * @param prefix the prefix
	 */
	public static void applyFrameState(JFrame target, Properties props, String prefix) {
		int x = getInt(prefix + "x", props);
		int y = getInt(prefix + "y", props);
		int w = getInt(prefix + "w", props);
		int h = getInt(prefix + "h", props);
		int s = getInt(prefix + "s", props);
		target.setBounds(x, y, w, h);
		target.setExtendedState(s);
	}
	/**
	 * Store the state of the target frame.
	 * @param source the source frame
	 * @param props the properties
	 * @param prefix the prefix
	 */
	public static void storeFrameState(JFrame source, Properties props, String prefix) {
		Rectangle r = source.getBounds();
		props.setProperty(prefix + "x", Integer.toString(r.x));
		props.setProperty(prefix + "y", Integer.toString(r.y));
		props.setProperty(prefix + "w", Integer.toString(r.width));
		props.setProperty(prefix + "h", Integer.toString(r.height));
		props.setProperty(prefix + "s", Integer.toString(source.getExtendedState()));
	}
	/**
	 * Retrieve an integer property value.
	 * @param key the key
	 * @param props the properties
	 * @return the value
	 */
	protected static int getInt(String key, Properties props) {
		return Integer.parseInt(props.getProperty(key));
	}
	/**
	 * Retrieve an integer property value.
	 * @param key the key
	 * @param props the properties
	 * @param defaultValue the default value
	 * @return the value
	 */
	protected static int getInt(String key, Properties props, int defaultValue) {
		String v = props.getProperty(key);
		return v != null ? Integer.parseInt(v) : defaultValue;
	}
	/**
	 * Store the configuration values.
	 * @param props the properties
	 */
	protected void storeConfig(Properties props) {
		storeFrameState(this, props, "main-");
	}
	/** Save the configuration. */
	public void saveConfig() {
		Properties props = new Properties();
		try {
			storeConfig(props);
			FileOutputStream fout = new FileOutputStream(configFile);
			try {
				props.storeToXML(fout, "Advance Flow Engine Control Center configuration");
			} finally {
				fout.close();
			}
		} catch (IOException ex) {
			LOG.error(ex.toString(), ex);
		}
	}
	
	/**
	 * Exit the application.
	 */
	void doExit() {
		try {
			disconnectEngine();
		} finally {
			try {
				saveConfig();
			} finally {
				dispose();
			}
		}
		
	}
	/**
	 * Disconnect from the current engine.
	 */
	void disconnectEngine() {
		if (engine != null && localEngine) {
			try {
				engine.shutdown();
			} catch (AdvanceControlException ex) {
				LOG.error(ex.toString(), ex);
			} catch (IOException ex) {
				LOG.error(ex.toString(), ex);
			}
		}
		engine = null;
		engineURL = null;
		user = null;
		version = null;
	}
	/**
	 * Constructor. Initializes the main window.
	 */
	public CCMain() {
		super();
		setTitle(format("ADVANCE Flow Engine Control Center v%s", AdvanceFlowEngine.VERSION));
		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				doExit();
			}
		});
		
		initGUI();
		
		pack();
		setLocationRelativeTo(null);
		loadConfig();
	}
	/**
	 * Main program.
	 * @param args no arguments
	 */
	public static void main(String[] args) {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				CCMain f = new CCMain();
				f.setVisible(true);
			}
		});
	}
	/**
	 * Initialize the GUI.
	 */
	protected void initGUI() {
		setJMenuBar(new JMenuBar());
		addItem(fromMethod(this, "doLocalLogin"), "Engine", "Login embedded...");
		addItem(fromMethod(this, "doRemoteLogin"), "Engine", "Login remote...");
		addItem(fromMethod(this, "doExit"), "Engine", "Exit");
		
		addItem(fromMethod(this, "doManageLocalKeyStores"), "Keystores", "Manage local keystores...");
		menusToEnable.put(addItem(fromMethod(this, "doManageKeyStores"), "Keystores", "Manage engine keystores..."), AdvanceUserRights.LIST_KEYSTORES);
		
		menusToEnable.put(addItem(fromMethod(this, "doDownloadFlow"), "Flow", "Download..."), AdvanceUserRights.LIST_KEYSTORES);
		menusToEnable.put(addItem(fromMethod(this, "doUploadFlow"), "Flow", "Upload..."), AdvanceUserRights.LIST_REALMS);
		menusToEnable.put(addItem(fromMethod(this, "doVerifyFlow"), "Flow", "Verify..."), AdvanceUserRights.LIST_REALMS);
		menusToEnable.put(addItem(fromMethod(this, "doDebugFlow"), "Flow", "Debug..."), AdvanceUserRights.LIST_REALMS);
		menusToEnable.put(addItem(fromMethod(this, "doLastCompilationResult"), "Flow", "Last compilation result..."), AdvanceUserRights.LIST_REALMS);
		
		menusToEnable.put(addItem(fromMethod(this, "doManageRealms"), "Administration", "Manage realms..."), AdvanceUserRights.LIST_REALMS);
		menusToEnable.put(addItem(fromMethod(this, "doManageUsers"), "Administration", "Manage users..."), AdvanceUserRights.LIST_USERS);
		menusToEnable.put(addItem(fromMethod(this, "doManageNotificationGroups"), "Administration", "Manage notification groups..."), AdvanceUserRights.LIST_NOTIFICATION_GROUPS);
		menusToEnable.put(addItem(fromMethod(this, "doShutdown"), "Administration", "Shutdown"), AdvanceUserRights.SHUTDOWN);
		
		menusToEnable.put(addItem(fromMethod(this, "doJDBCDataSources"), "Data sources", "JDBC..."), AdvanceUserRights.LIST_JDBC_DATA_SOURCES);
		menusToEnable.put(addItem(fromMethod(this, "doSOAPDataSources"), "Data sources", "SOAP..."), AdvanceUserRights.LIST_SOAP_CHANNELS);
		menusToEnable.put(addItem(fromMethod(this, "doJMSDataSources"), "Data sources", "JMS..."), AdvanceUserRights.LIST_JMS_ENDPOINTS);
		menusToEnable.put(addItem(fromMethod(this, "doWebDataSources"), "Data sources", "Web..."), AdvanceUserRights.LIST_WEB_DATA_SOURCES);
		menusToEnable.put(addItem(fromMethod(this, "doFTPDataSources"), "Data sources", "FTP..."), AdvanceUserRights.LIST_FTP_DATA_SOURCES);
		menusToEnable.put(addItem(fromMethod(this, "doLocalDataSources"), "Data sources", "Local..."), AdvanceUserRights.LIST_LOCAL_FILE_DATA_SOURCES);
		menusToEnable.put(addItem(fromMethod(this, "doEmailDataSources"), "Data sources", "Email..."), AdvanceUserRights.LIST_EMAIL);
		
		ImageIcon icon = new ImageIcon(getClass().getResource("advlogo_192x128.png"));
		JLabel label = new JLabel(icon);
		
		JPanel cp = new JPanel();
		
		Container c = getContentPane();
		c.setLayout(new GridBagLayout());
		c.add(cp);
		
		GroupLayout gl = new GroupLayout(cp);
		cp.setLayout(gl);
		
		gl.setAutoCreateContainerGaps(true);
		gl.setAutoCreateGaps(true);

		JLabel urlLabel0 = new JLabel(get("Engine:"));
		JLabel verLabel0 = new JLabel(get("Version:"));
		JLabel userLabel0 = new JLabel(get("User:"));
		
		urlLabel = new JLabel();
		verLabel = new JLabel();
		userLabel = new JLabel();
		
		gl.setHorizontalGroup(
			gl.createParallelGroup(Alignment.CENTER)
			.addGroup(
				gl.createSequentialGroup()
				.addGroup(
					gl.createParallelGroup()
					.addComponent(urlLabel0)
					.addComponent(verLabel0)
					.addComponent(userLabel0)
				)
				.addGroup(
					gl.createParallelGroup()
					.addComponent(urlLabel)
					.addComponent(verLabel)
					.addComponent(userLabel)
				)
			)
			.addComponent(label)
		);
		
		gl.setVerticalGroup(
			gl.createSequentialGroup()
			.addGroup(
				gl.createParallelGroup(Alignment.BASELINE)
				.addComponent(urlLabel0)
				.addComponent(urlLabel)
			)
			.addGroup(
				gl.createParallelGroup(Alignment.BASELINE)
				.addComponent(verLabel0)
				.addComponent(verLabel)
			)
			.addGroup(
				gl.createParallelGroup(Alignment.BASELINE)
				.addComponent(userLabel0)
				.addComponent(userLabel)
			)
			.addComponent(label)
		);
		
		
		enableDisableMenus();
	}
	/**
	 * Create an action from a local declared method name.
	 * @param target the target object
	 * @param name the method name
	 * @return the action
	 */
	protected Action0 fromMethod(final Object target, String name) {
		try {
			final Method m = getClass().getDeclaredMethod(name);
			return new Action0() {
				@Override
				public void invoke() {
					try {
						m.invoke(target);
					} catch (IllegalAccessException ex) {
						LOG.error(ex.toString(), ex);
					} catch (IllegalArgumentException ex) {
						LOG.error(ex.toString(), ex);
					} catch (InvocationTargetException ex) {
						LOG.error(ex.toString(), ex);
					}
				}
			};
		} catch (NoSuchMethodException ex) {
			LOG.error(ex.toString(), ex);
			return null;
		}
	}
	/**
	 * Add a menu item with the supplied menu path and action.
	 * @param action the action
	 * @param path the menu path
	 * @return the created menu item
	 */
	public JMenuItem addItem(final Action0 action, String... path) {
		JMenuItem mi = new JMenuItem(get(path[path.length - 1]));
		if (action != null) {
			mi.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					action.invoke();
				}
			});
		} else {
			mi.setEnabled(false);
		}
		
		JMenu level0 = findOrCreateMenu(get(path[0]));
		JMenu parent = level0;
		for (int i = 1; i < path.length - 1; i++) {
			parent = findOrCreateMenu(level0, get(path[i]));
		}
		
		parent.add(mi);
		
		return mi;
	}
	/**
	 * Find or create a root menu.
	 * @param name the menu name
	 * @return the menu object
	 */
	protected JMenu findOrCreateMenu(String name) {
		for (int i = 0; i < getJMenuBar().getMenuCount(); i++) {
			if (getJMenuBar().getMenu(i).getText().equals(name)) {
				return getJMenuBar().getMenu(i);
			}
		}
		JMenu m = new JMenu(name);
		getJMenuBar().add(m);
		return m;
	}
	/**
	 * Find or create a new menu within the parent.
	 * @param parent the parent menu
	 * @param name the menu name
	 * @return the created menu object
	 */
	protected JMenu findOrCreateMenu(JMenu parent, String name) {
		for (int i = 0; i < parent.getItemCount(); i++) {
			if (parent.getItem(i).getText().equals(name)) {
				return (JMenu)parent.getItem(i);
			}
		}
		JMenu m = new JMenu(name);
		parent.add(m);
		return m;
	}
	/**
	 * Perform actions after opening a connection to an engine.
	 */
	void doAfterOpen() {
		try {
			version = engine.queryVersion();
		} catch (IOException ex) {
			GUIUtils.errorMessage(ex);
		} catch (AdvanceControlException ex) {
			GUIUtils.errorMessage(ex);
		}
	}
	/**
	 * Create a cell title function from the name String and type Class objects.
	 * @param array the array of String, Class objects
	 * @return the function which returns an array element as Pair
	 */
	Func1<Integer, Pair<String, ? extends Class<?>>> from(final Object... array) {
		return new Func1<Integer, Pair<String, ? extends Class<?>>>() {
			@Override
			public Pair<String, ? extends Class<?>> invoke(Integer param1) {
				return Pair.of((String)array[param1 * 2], (Class<?>)array[param1 * 2 + 1]);
			}
		};
	}
	/**
	 * Do manage keystores.
	 */
	void doManageKeyStores() {
		final GenericListingFrame<AdvanceKeyStore> f = new GenericListingFrame<AdvanceKeyStore>(this);

		f.setCellTitleFunction(from("Name", String.class, "Location", String.class, "Created", String.class, "Modified", String.class));
		
		f.setCellValueFunction(new Func2<AdvanceKeyStore, Integer, Object>() { 
			@Override
			public Object invoke(AdvanceKeyStore param1, Integer param2) {
				switch (param2) {
				case 0:
					return param1.name;
				case 1:
					return param1.location;
				case 2:
					return createdAtBy(param1);
				case 3:
					return modifiedAtBy(param1);
				default:
					return null;
				}
			}
		});
		f.setColumnCount(4);
		
		f.setRetrieveFunction(new Action1<String>() { 
			@Override
			public void invoke(String value) {
				GUIUtils.getWorker(new ListWorkItem<AdvanceKeyStore>(f) {
					@Override
					public List<AdvanceKeyStore> retrieve() throws Exception {
						return engine.datastore().queryKeyStores();
					}
				}).execute();
			}
		});
		displayFrame(f, "Manage engine keystores");

	}
	/**
	 * Do manage keystores.
	 */
	void doManageLocalKeyStores() {
		final GenericListingFrame<AdvanceKeyStore> f = new GenericListingFrame<AdvanceKeyStore>(this);

		f.setCellTitleFunction(from("Name", String.class, "Location", String.class, "Created", String.class, "Modified", String.class));
		
		f.setCellValueFunction(new Func2<AdvanceKeyStore, Integer, Object>() { 
			@Override
			public Object invoke(AdvanceKeyStore param1, Integer param2) {
				switch (param2) {
				case 0:
					return param1.name;
				case 1:
					return param1.location;
				case 2:
					return createdAtBy(param1);
				case 3:
					return modifiedAtBy(param1);
				default:
					return null;
				}
			}
		});
		f.setColumnCount(4);
		f.showEngineInfo(false);
		f.setRetrieveFunction(new Action1<String>() { 
			@Override
			public void invoke(String value) {
				GUIUtils.getWorker(new ListWorkItem<AdvanceKeyStore>(f) {
					@Override
					public List<AdvanceKeyStore> retrieve() throws Exception {
						return engine.datastore().queryKeyStores();
					}
				}).execute();
			}
		});
		displayFrame(f, "Manage local keystores");

	}
	/**
	 * A list retrieving work item.
	 * @author karnokd, 2011.10.07.
	 * @param <T> the element type of the list
	 */
	public abstract class ListWorkItem<T> implements WorkItem {
		/** The response. */
		protected List<T> list;
		/** The error. */
		protected Throwable error;
		/** The target frame to update. */
		final GenericListingFrame<T> frame;
		/**
		 * Constructor.
		 * @param frame the frame to set the result on
		 */
		public ListWorkItem(GenericListingFrame<T> frame) {
			this.frame = frame;
		}
		/** 
		 * @return the list to relay.
		 * @throws Exception on error 
		 */
		public abstract List<T> retrieve() throws Exception;
		@Override
		public void run() {
			try {
				list = retrieve();
			} catch (Exception ex) {
				error = ex;
			}
		}
		@Override
		public void done() {
			if (error == null) {
				frame.setRows(list);
				frame.autoSizeTable();
			} else {
				GUIUtils.errorMessage(error);
			}
		}
	}
	/**
	 * Display the generic list frame.
	 * @param f the frame object
	 * @param title the title
	 */
	void displayFrame(final GenericListingFrame<?> f, String title) {
		f.setTitle(get(title));
		f.fireTableStructureChanged();
		setEngineInfo(f.engineInfo);
		f.pack();
		f.setLocationRelativeTo(this);
		f.setVisible(true);
		f.refresh();
	}
	/**
	 * Set the engine information.
	 * @param panel the panel
	 */
	void setEngineInfo(EngineInfoPanel panel) {
		panel.setEngineURL(engineURL.toString());
		panel.setEngineVersion(version.toString());
	}
	/**
	 * The string representation of a create info.
	 * @param param1 the object
	 * @return the string
	 */
	String createdAtBy(AdvanceCreateModifyInfo param1) {
		return param1.createdAt + " by " + param1.createdBy;
	}
	/**
	 * The string representation of a create info.
	 * @param param1 the object
	 * @return the string
	 */
	String modifiedAtBy(AdvanceCreateModifyInfo param1) {
		return param1.modifiedAt + " by " + param1.modifiedBy;
	}
	/**
	 * Do manage realms.
	 */
	void doManageRealms() {
		final GenericListingFrame<AdvanceRealm> f = new GenericListingFrame<AdvanceRealm>(this);
		f.setCellTitleFunction(from("Name", String.class, "Created", String.class, "Modified", String.class, "Status", String.class));
		f.setCellValueFunction(new Func2<AdvanceRealm, Integer, Object>() {
			@Override
			public Object invoke(AdvanceRealm param1, Integer param2) {
				switch (param2) {
				case 0:
					return param1.name;
				case 1:
					return createdAtBy(param1);
				case 2:
					return modifiedAtBy(param1);
				case 3:
					return param1.status.toString();
				default:
					return null;
				}
			}
		});
		f.setRetrieveFunction(new Action1<String>() {
			@Override
			public void invoke(String value) {
				GUIUtils.getWorker(new ListWorkItem<AdvanceRealm>(f) {
					@Override
					public List<AdvanceRealm> retrieve() throws Exception {
						return engine.datastore().queryRealms();
					}
				}).execute();
			}
		});
		
		if (user.rights.contains(AdvanceUserRights.CREATE_REALM)) {
			f.setExtraButton(0, "Create...", new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					final String value = JOptionPane.showInputDialog(f, get("Enter realm name:"), "Create realm", JOptionPane.QUESTION_MESSAGE);
					if (value != null) {
						GUIUtils.getWorker(new WorkItem() {
							/** The exception. */
							Throwable t;
							@Override
							public void run() {
								try {
									engine.datastore().createRealm(value, user.name);
								} catch (Throwable t) {
									this.t = t;
								}
							}
							@Override
							public void done() {
								if (t != null) {
									GUIUtils.errorMessage(t);
								} else {
									f.refresh();
								}
							}
						}).execute();
					}
				}
			});
		}
		f.setExtraButton(1, "Start", new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				final List<AdvanceRealm> sel = f.getSelectedItems();
				GUIUtils.getWorker(new WorkItem() {
					/** The error. */
					Throwable t;
					@Override
					public void run() {
						for (AdvanceRealm r : sel) {
							if (user.realmRights.containsEntry(r.name, AdvanceUserRealmRights.START)) {
								try {
									engine.startRealm(r.name, user.name);
								} catch (Throwable t) {
									this.t = t;
									break;
								}
							}
						}
					}
					@Override
					public void done() {
						if (t != null) {
							GUIUtils.errorMessage(t);
						} else {
							f.refresh();
						}
					}
				}).execute();
			}
		});
		f.setExtraButton(2, "Stop", new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				final List<AdvanceRealm> sel = f.getSelectedItems();
				GUIUtils.getWorker(new WorkItem() {
					/** The error. */
					Throwable t;
					@Override
					public void run() {
						for (AdvanceRealm r : sel) {
							if (user.realmRights.containsEntry(r.name, AdvanceUserRealmRights.STOP)) {
								try {
									engine.stopRealm(r.name, user.name);
								} catch (Throwable t) {
									this.t = t;
									break;
								}
							}
						}
					}
					@Override
					public void done() {
						if (t != null) {
							GUIUtils.errorMessage(t);
						} else {
							f.refresh();
						}
					}
				}).execute();
			}
		});
		f.setExtraButton(3, "Delete", new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				deleteItems(f, new Func1<AdvanceRealm, Throwable>() {
					@Override
					public Throwable invoke(AdvanceRealm param1) {
						try {
							engine.datastore().deleteRealm(param1.name);
							return null;
						} catch (Throwable t) {
							return t;
						}
					}
				});
			}
		});
		
		f.setColumnCount(4);
		displayFrame(f, "Manage realms");
	}
	/**
	 * Do manage realms.
	 */
	void doManageUsers() {
		final GenericListingFrame<AdvanceUser> f = new GenericListingFrame<AdvanceUser>(this);
		f.setCellTitleFunction(from("E", Boolean.class, "Name", String.class, "Email", String.class, 
				"Created", String.class, "Modified", String.class));
		f.setCellValueFunction(new Func2<AdvanceUser, Integer, Object>() {
			@Override
			public Object invoke(AdvanceUser param1, Integer param2) {
				switch (param2) {
				case 0:
					return param1.enabled;
				case 1:
					return param1.name;
				case 2:
					return param1.email;
				case 3:
					return createdAtBy(param1);
				case 4:
					return modifiedAtBy(param1);
				default:
					return null;
				}
			}
		});
		f.setRetrieveFunction(new Action1<String>() {
			@Override
			public void invoke(String value) {
				GUIUtils.getWorker(new ListWorkItem<AdvanceUser>(f) {
					@Override
					public List<AdvanceUser> retrieve() throws Exception {
						return engine.datastore().queryUsers();
					}
				}).execute();
			}
		});
		f.setColumnCount(5);
		displayFrame(f, "Manage users");
	}
	/**
	 * Do manage JDBC data sources.
	 */
	void doJDBCDataSources() {
		final GenericListingFrame<AdvanceJDBCDataSource> f = new GenericListingFrame<AdvanceJDBCDataSource>(this);
		f.setCellTitleFunction(from("Name", String.class, "Driver", String.class, "URL", String.class, 
				"Created", String.class, "Modified", String.class));
		f.setCellValueFunction(new Func2<AdvanceJDBCDataSource, Integer, Object>() {
			@Override
			public Object invoke(AdvanceJDBCDataSource param1, Integer param2) {
				switch (param2) {
				case 0:
					return param1.name;
				case 1:
					return param1.driver;
				case 2:
					return param1.url;
				case 3:
					return createdAtBy(param1);
				case 4:
					return modifiedAtBy(param1);
				default:
					return null;
				}
			}
		});
		f.setRetrieveFunction(new Action1<String>() {
			@Override
			public void invoke(String value) {
				GUIUtils.getWorker(new ListWorkItem<AdvanceJDBCDataSource>(f) {
					@Override
					public List<AdvanceJDBCDataSource> retrieve() throws Exception {
						return engine.datastore().queryJDBCDataSources();
					}
				}).execute();
			}
		});
		f.setColumnCount(5);
		displayFrame(f, "Manage JDBC data sources");
	}
	/**
	 * Do manage SOAP data sources.
	 */
	void doSOAPDataSources() {
		final GenericListingFrame<AdvanceSOAPChannel> f = new GenericListingFrame<AdvanceSOAPChannel>(this);
		f.setCellTitleFunction(from("Name", String.class, "Endpoint", String.class, 
				"Created", String.class, "Modified", String.class));
		f.setCellValueFunction(new Func2<AdvanceSOAPChannel, Integer, Object>() {
			@Override
			public Object invoke(AdvanceSOAPChannel param1, Integer param2) {
				switch (param2) {
				case 0:
					return param1.name;
				case 1:
					return param1.endpoint;
				case 2:
					return createdAtBy(param1);
				case 3:
					return modifiedAtBy(param1);
				default:
					return null;
				}
			}
		});
		f.setRetrieveFunction(new Action1<String>() {
			@Override
			public void invoke(String value) {
				GUIUtils.getWorker(new ListWorkItem<AdvanceSOAPChannel>(f) {
					@Override
					public List<AdvanceSOAPChannel> retrieve() throws Exception {
						return engine.datastore().querySOAPChannels();
					}
				}).execute();
			}
		});
		f.setColumnCount(4);
		displayFrame(f, "Manage SOAP channels");
	}
	/**
	 * Do manage JDBC data sources.
	 */
	void doJMSDataSources() {
		final GenericListingFrame<AdvanceJMSEndpoint> f = new GenericListingFrame<AdvanceJMSEndpoint>(this);
		f.setCellTitleFunction(from("Name", String.class, "Driver", String.class, "URL", String.class, 
				"Created", String.class, "Modified", String.class));
		f.setCellValueFunction(new Func2<AdvanceJMSEndpoint, Integer, Object>() {
			@Override
			public Object invoke(AdvanceJMSEndpoint param1, Integer param2) {
				switch (param2) {
				case 0:
					return param1.name;
				case 1:
					return param1.driver;
				case 2:
					return param1.url;
				case 3:
					return createdAtBy(param1);
				case 4:
					return modifiedAtBy(param1);
				default:
					return null;
				}
			}
		});
		f.setRetrieveFunction(new Action1<String>() {
			@Override
			public void invoke(String value) {
				GUIUtils.getWorker(new ListWorkItem<AdvanceJMSEndpoint>(f) {
					@Override
					public List<AdvanceJMSEndpoint> retrieve() throws Exception {
						return engine.datastore().queryJMSEndpoints();
					}
				}).execute();
			}
		});
		f.setColumnCount(5);
		displayFrame(f, "Manage JDBC data sources");
	}
	/**
	 * Do manage WEB data sources.
	 */
	void doWebDataSources() {
		final GenericListingFrame<AdvanceWebDataSource> f = new GenericListingFrame<AdvanceWebDataSource>(this);
		f.setCellTitleFunction(from("Name", String.class, "URL", String.class, 
				"Created", String.class, "Modified", String.class));
		f.setCellValueFunction(new Func2<AdvanceWebDataSource, Integer, Object>() {
			@Override
			public Object invoke(AdvanceWebDataSource param1, Integer param2) {
				switch (param2) {
				case 0:
					return param1.name;
				case 1:
					return param1.url;
				case 2:
					return createdAtBy(param1);
				case 3:
					return modifiedAtBy(param1);
				default:
					return null;
				}
			}
		});
		f.setRetrieveFunction(new Action1<String>() {
			@Override
			public void invoke(String value) {
				GUIUtils.getWorker(new ListWorkItem<AdvanceWebDataSource>(f) {
					@Override
					public List<AdvanceWebDataSource> retrieve() throws Exception {
						return engine.datastore().queryWebDataSources();
					}
				}).execute();
			}
		});
		f.setColumnCount(4);
		f.setExtraButton(0, "Create...", new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				CCDetailDialog<?> d = createWebDialog(CCMain.this, f.getRows(), null);
				setEngineInfo(d.engineInfo);
				d.pack();
				d.setLocationRelativeTo(f);
				d.setVisible(true);
			}
		});
		f.setDisplayItem(new Action1<AdvanceWebDataSource>() {
			@Override
			public void invoke(AdvanceWebDataSource value) {
				CCDetailDialog<?> d = createWebDialog(CCMain.this, f.getRows(), value);
				setEngineInfo(d.engineInfo);
				d.pack();
				d.setLocationRelativeTo(f);
				d.setVisible(true);
			}
		});
		f.setExtraButton(1, "Delete", new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				deleteItems(f, new Func1<AdvanceWebDataSource, Throwable>() {
					@Override
					public Throwable invoke(AdvanceWebDataSource param1) {
						try {
							engine.datastore().deleteWebDataSource(param1.name);
							return null;
						} catch (Throwable t) {
							return t;
						}
					}
				});
			}
		});
		displayFrame(f, "Manage Web data sources");
	}
	/**
	 * Delete the selected items of the given frame via the function.
	 * @param f the frame
	 * @param deleteItemFunction the delete function
	 * @param <T> the element type
	 */
	<T> void deleteItems(final GenericListingFrame<T> f, final Func1<T, Throwable> deleteItemFunction) {
		if (JOptionPane.showConfirmDialog(f, get("Are you sure?"), 
				get("Delete"), JOptionPane.YES_NO_OPTION) == JOptionPane.OK_OPTION) {
			final List<T> sel = f.getSelectedItems();
			GUIUtils.getWorker(new WorkItem() {
				/** The exception. */
				protected Throwable t;
				@Override
				public void done() {
					if (t != null) {
						GUIUtils.errorMessage(t);
						f.refresh();
					} else {
						f.removeItems(sel);
					}
				}
				@Override
				public void run() {
					for (T e : sel) {
						t = deleteItemFunction.invoke(e);
						if (t != null) {
							break;
						}
					}
				}
			}).execute();
		}
	}
	/**
	 * Do manage FTP data sources.
	 */
	void doFTPDataSources() {
		final GenericListingFrame<AdvanceFTPDataSource> f = new GenericListingFrame<AdvanceFTPDataSource>(this);
		f.setCellTitleFunction(from("Name", String.class, "URL", String.class, 
				"Created", String.class, "Modified", String.class));
		f.setCellValueFunction(new Func2<AdvanceFTPDataSource, Integer, Object>() {
			@Override
			public Object invoke(AdvanceFTPDataSource param1, Integer param2) {
				switch (param2) {
				case 0:
					return param1.name;
				case 1:
					String prot = "ftp";
					if (param1.protocol == AdvanceFTPProtocols.FTPS) {
						prot = "ftps";
					} else
					if (param1.protocol == AdvanceFTPProtocols.SFTP) {
						prot = "sftp";
					}
					return prot + "://" + param1.address + "/" + param1.remoteDirectory;
				case 2:
					return createdAtBy(param1);
				case 3:
					return modifiedAtBy(param1);
				default:
					return null;
				}
			}
		});
		f.setRetrieveFunction(new Action1<String>() {
			@Override
			public void invoke(String value) {
				GUIUtils.getWorker(new ListWorkItem<AdvanceFTPDataSource>(f) {
					@Override
					public List<AdvanceFTPDataSource> retrieve() throws Exception {
						return engine.datastore().queryFTPDataSources();
					}
				}).execute();
			}
		});
		f.setColumnCount(4);
		displayFrame(f, "Manage FTP data sources");
	}
	/**
	 * Do manage Local data sources.
	 */
	void doLocalDataSources() {
		final GenericListingFrame<AdvanceLocalFileDataSource> f = new GenericListingFrame<AdvanceLocalFileDataSource>(this);
		f.setCellTitleFunction(from("Name", String.class, "URL", String.class, 
				"Created", String.class, "Modified", String.class));
		f.setCellValueFunction(new Func2<AdvanceLocalFileDataSource, Integer, Object>() {
			@Override
			public Object invoke(AdvanceLocalFileDataSource param1, Integer param2) {
				switch (param2) {
				case 0:
					return param1.name;
				case 1:
					return param1.directory;
				case 2:
					return createdAtBy(param1);
				case 3:
					return modifiedAtBy(param1);
				default:
					return null;
				}
			}
		});
		f.setRetrieveFunction(new Action1<String>() {
			@Override
			public void invoke(String value) {
				GUIUtils.getWorker(new ListWorkItem<AdvanceLocalFileDataSource>(f) {
					@Override
					public List<AdvanceLocalFileDataSource> retrieve() throws Exception {
						return engine.datastore().queryLocalFileDataSources();
					}
				}).execute();
			}
		});
		f.setColumnCount(4);
		displayFrame(f, "Manage FTP data sources");
	}
	/**
	 * Do manage realms.
	 */
	void doDownloadFlow() {
		final GenericListingFrame<AdvanceRealm> f = new GenericListingFrame<AdvanceRealm>(this);
		f.setCellTitleFunction(from("Name", String.class, "Created", String.class, "Modified", String.class, "Status", String.class));
		f.setCellValueFunction(new Func2<AdvanceRealm, Integer, Object>() {
			@Override
			public Object invoke(AdvanceRealm param1, Integer param2) {
				switch (param2) {
				case 0:
					return param1.name;
				case 1:
					return createdAtBy(param1);
				case 2:
					return modifiedAtBy(param1);
				case 3:
					return param1.status.toString();
				default:
					return null;
				}
			}
		});
		f.setRetrieveFunction(new Action1<String>() {
			@Override
			public void invoke(String value) {
				GUIUtils.getWorker(new ListWorkItem<AdvanceRealm>(f) {
					@Override
					public List<AdvanceRealm> retrieve() throws Exception {
						return engine.datastore().queryRealms();
					}
				}).execute();
			}
		});
		f.setColumnCount(4);
		displayFrame(f, "Download Flow");
	}
	/**
	 * Do manage realms.
	 */
	void doUploadFlow() {
		final GenericListingFrame<AdvanceRealm> f = new GenericListingFrame<AdvanceRealm>(this);
		f.setCellTitleFunction(from("Name", String.class, "Created", String.class, "Modified", String.class, "Status", String.class));
		f.setCellValueFunction(new Func2<AdvanceRealm, Integer, Object>() {
			@Override
			public Object invoke(AdvanceRealm param1, Integer param2) {
				switch (param2) {
				case 0:
					return param1.name;
				case 1:
					return createdAtBy(param1);
				case 2:
					return modifiedAtBy(param1);
				case 3:
					return param1.status.toString();
				default:
					return null;
				}
			}
		});
		f.setRetrieveFunction(new Action1<String>() {
			@Override
			public void invoke(String value) {
				GUIUtils.getWorker(new ListWorkItem<AdvanceRealm>(f) {
					@Override
					public List<AdvanceRealm> retrieve() throws Exception {
						return engine.datastore().queryRealms();
					}
				}).execute();
			}
		});
		f.setColumnCount(4);
		displayFrame(f, "Upload Flow");
	}
	/**
	 * Create the email data sources listing.
	 */
	void doEmailDataSources() {
		final GenericListingFrame<AdvanceEmailBox> f = new GenericListingFrame<AdvanceEmailBox>(this);
		f.setCellTitleFunction(from(
				"Name", String.class, 
				"Address", String.class,
				"Created", String.class, 
				"Modified", String.class 
				));
		f.setCellValueFunction(new Func2<AdvanceEmailBox, Integer, Object>() {
			@Override
			public Object invoke(AdvanceEmailBox param1, Integer param2) {
				switch (param2) {
				case 0: 
					return param1.name;
				case 1:
					return param1.sendAddress + " " + param1.receiveAddress;
				case 2:
					return createdAtBy(param1);
				case 3:
					return modifiedAtBy(param1);
				default:
					return null;
				}
			}
		});
		f.setRetrieveFunction(new Action1<String>() {
			@Override
			public void invoke(String value) {
				GUIUtils.getWorker(new ListWorkItem<AdvanceEmailBox>(f) {
					@Override
					public List<AdvanceEmailBox> retrieve() throws Exception {
						return engine.datastore().queryEmailBoxes();
					}
				}).execute();
			}
		});
		f.setColumnCount(4);
		displayFrame(f, "Email boxes");
	}
	/**
	 * Login into a local engine.
	 */
	void doLocalLogin() {
		CCLocalLogin login = new CCLocalLogin(this);
		login.setLocationRelativeTo(null);
		if (login.display()) {
			disconnectEngine();
			engine = login.takeEngine();
			engineURL = login.takeEngineURL();
			try {
				version = engine.queryVersion();
				user = engine.getUser();
				localEngine = true;
				enableDisableMenus();
			} catch (Exception ex) {
				LOG.error(ex.toString(), ex);
				GUIUtils.errorMessage(ex);
			}

			urlLabel.setText(engineURL.toString());
			verLabel.setText(version.toString());
			userLabel.setText(user.name + " <" + user.email + ">");
		}
	}
	/**
	 * Enable/disable menus based on the user settings.
	 */
	void enableDisableMenus() {
		for (Map.Entry<JMenuItem, Collection<AdvanceUserRights>> e : menusToEnable.asMap().entrySet()) {
			JMenuItem mi = e.getKey();
			if (user != null) {
				for (AdvanceUserRights r : e.getValue()) {
					if (user.rights.contains(r)) {
						mi.setEnabled(true);
						break;
					}
				}
			} else {
				mi.setEnabled(false);
			}
		}
	}
	/**
	 * Construct a web dialog.
	 * @param labels the label manager
	 * @param list the available list
	 * @param selected the selected item or null to indicate a new item should be created
	 * @return the dialog created
	 */
	CCDetailDialog<AdvanceWebDataSource> createWebDialog(LabelManager labels, 
			final List<AdvanceWebDataSource> list, final AdvanceWebDataSource selected) {
		final CCWebDetails wd = new CCWebDetails(labels);
		final CCDetailDialog<AdvanceWebDataSource> dialog = new CCDetailDialog<AdvanceWebDataSource>(labels, wd);
		dialog.setTitle(labels.get("Web Data Source Details"));
		dialog.pager.setItemName(new Func1<AdvanceWebDataSource, String>() {
			@Override
			public String invoke(AdvanceWebDataSource param1) {
				return param1.name + " [" + param1.url + "]";
			}
		});
		dialog.pager.setItems(list);
		
		final Action0 retrieveAction = new Action0() {
			@Override
			public void invoke() {
				final String name = dialog.pager.getSelectedItem().name; 
				dialog.pager.setEnabled(false);
				GUIUtils.getWorker(new WorkItem() {
					/** The error. */
					Throwable t;
					/** The data. */
					AdvanceWebDataSource e;
					@Override
					public void run() {
						try {
							e = engine.datastore().queryWebDataSource(name);
						} catch (Throwable t) {
							this.t = t;
						}
					}
					@Override
					public void done() {
						if (t != null) {
							GUIUtils.errorMessage(t);
						} else {
							wd.load(e);
							dialog.createModify.set(e);
							dialog.pack();
						}
						dialog.pager.setEnabled(true);
					}
				}).execute();
			}
		};
		
		dialog.pager.setSelect(new Action1<AdvanceWebDataSource>() {
			@Override
			public void invoke(AdvanceWebDataSource value) {
				retrieveAction.invoke();
			}
		});
		dialog.buttons.showRefresh(selected != null);
		dialog.showPager(selected != null);
		if (selected == null) {
			dialog.showCreateModify(false);
		} else {
			dialog.pager.setSelectedItem(selected);
			wd.name.setEnabled(false);
		}
		dialog.buttons.setClose(new Action0() {
			@Override
			public void invoke() {
				dialog.dispose();
			}
		});
		dialog.buttons.setRefresh(new Action0() {
			@Override
			public void invoke() {
				retrieveAction.invoke();
			}
		});
		
		dialog.buttons.setSave(createWebSaver(wd, dialog, false));
		dialog.buttons.setSaveAndClose(createWebSaver(wd, dialog, true));
		
		return dialog;
	}
	/**
	 * Create a web saver action.
	 * @param wd the details panel.
	 * @param dialog the dialog
	 * @param close close dialog
	 * @return the action
	 */
	Action0 createWebSaver(final CCWebDetails wd, 
			final CCDetailDialog<AdvanceWebDataSource> dialog, 
			final boolean close) {
		return new Action0() {
			@Override
			public void invoke() {
				final AdvanceWebDataSource e = wd.save();
				if (e == null) {
					return;
				}
				
				GUIUtils.getWorker(new WorkItem() {
					/** The exception. */
					protected Throwable t;
					@Override
					public void run() {
						try {
							engine.datastore().updateWebDataSource(e);
						} catch (Throwable t) {
							LOG.error(t.toString(), t);
							this.t = t;
						}
					}
					@Override
					public void done() {
						if (t != null) {
							GUIUtils.errorMessage(t);
						} else {
							if (close) {
								dialog.dispose();
							} else {
								wd.name.setEditable(false);
								dialog.showCreateModify(true);
								dialog.showPager(true);
								dialog.buttons.showRefresh(true);
								dialog.pack();
								updatePager(dialog.pager, e);
							}
						}
					}
				}).execute();
			}
		};
	}
	/**
	 * Update a pager by adding the given item if not exists then selecting
	 * that item.
	 * @param <K> the item identifier type
	 * @param <T> the element type
	 * @param pager the pager
	 * @param item the item to select
	 */
	<K, T extends Identifiable<K>> void updatePager(Pager<T> pager, T item) {
		List<T> list = pager.getItems();
		boolean found = false;
		for (T s : list) {
			if (s.id().equals(item.id())) {
				found = true;
				break;
			}
		}
		if (!found) {
			list.add(item);
		}
		pager.setItems(list);
		pager.setSelectedItem(item);
	}
	/** Open the remote login dialog. */
	void doRemoteLogin() {
		LOG.error("Implement!");
	}
	/** Open the notification management screen. */
	void doManageNotificationGroups() {
		LOG.error("Implement!");
	}
	/** Show the results of the last compilation. */
	void doLastCompilationResult() {
		LOG.error("Implement!");
	}
	/** Debug a flow. */
	void doDebugFlow() {
		LOG.error("Implement!");
	}
	/** Verify a flow. */
	void doVerifyFlow() {
		doVerifyFlow();
	}
	/** Shutdown engine. */
	void doShutdown() {
		int c = JOptionPane.showConfirmDialog(this, get("Are you sure?"), "Shutting down engine " + engineURL, JOptionPane.YES_NO_OPTION);
		if (c == JOptionPane.YES_OPTION) {
			GUIUtils.getWorker(new WorkItem() {
				/** The error. */
				Throwable t;
				@Override
				public void run() {
					try {
						engine.shutdown();
					} catch (Throwable t) {
						this.t = t;
					}
				}
				@Override
				public void done() {
					if (t != null) {
						GUIUtils.errorMessage(t);
					}
				}
			}).execute();
		}
	}
}
