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
import hu.akarnokd.reactive4java.base.Option;
import hu.akarnokd.reactive4java.base.Pair;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dialog;
import java.awt.Frame;
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
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileNameExtensionFilter;

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
import eu.advance.logistics.flow.engine.model.AdvanceCompilationError;
import eu.advance.logistics.flow.engine.model.fd.AdvanceCompositeBlock;
import eu.advance.logistics.flow.engine.model.rt.AdvanceBlockRegistryEntry;
import eu.advance.logistics.flow.engine.model.rt.AdvanceCompilationResult;
import eu.advance.logistics.flow.engine.xml.typesystem.XElement;

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
	protected final	File configFile = new File("conf/advance-flow-engine-control-center-config.xml");
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
	/** The properties. */
	protected Properties props = new Properties();
	/** The last directory. */
	protected File lastDirectory = new File(".");
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
		if (props.getProperty("main-last-directory") != null) {
			lastDirectory = new File(props.getProperty("main-last-directory"));
		}
	}
	/**
	 * Apply the state to the target frame named by the prefix within the properties.
	 * @param target the target frame
	 * @param props the properties
	 * @param prefix the prefix
	 * @return true if a saved size was restored
	 */
	public static boolean applyFrameState(Frame target, Properties props, String prefix) {
		if (props.getProperty(prefix + "x") != null) {
			int x = getInt(prefix + "x", props);
			int y = getInt(prefix + "y", props);
			int w = getInt(prefix + "w", props);
			int h = getInt(prefix + "h", props);
			target.setBounds(x, y, w, h);
			if (props.getProperty(prefix + "s") != null) {
				int s = getInt(prefix + "s", props);
				target.setExtendedState(s);
			}
			return true;
		}
		return false;
	}
	/**
	 * Apply the state to the target frame named by the prefix within the properties.
	 * @param target the target frame
	 * @param props the properties
	 * @param prefix the prefix
	 * @return true if the state was restored
	 */
	public static boolean applyFrameState(Dialog target, Properties props, String prefix) {
		if (props.getProperty(prefix + "x") != null) {
			int x = getInt(prefix + "x", props);
			int y = getInt(prefix + "y", props);
			int w = getInt(prefix + "w", props);
			int h = getInt(prefix + "h", props);
			target.setBounds(x, y, w, h);
			return true;
		}
		return false;
	}
	/**
	 * Store the state of the target frame.
	 * @param source the source frame
	 * @param props the properties
	 * @param prefix the prefix
	 */
	public static void storeFrameState(Frame source, Properties props, String prefix) {
		Rectangle r = source.getBounds();
		props.setProperty(prefix + "x", Integer.toString(r.x));
		props.setProperty(prefix + "y", Integer.toString(r.y));
		props.setProperty(prefix + "w", Integer.toString(r.width));
		props.setProperty(prefix + "h", Integer.toString(r.height));
		props.setProperty(prefix + "s", Integer.toString(source.getExtendedState()));
	}
	/**
	 * Store the state of the target frame.
	 * @param source the source frame
	 * @param props the properties
	 * @param prefix the prefix
	 */
	public static void storeFrameState(Dialog source, Properties props, String prefix) {
		Rectangle r = source.getBounds();
		props.setProperty(prefix + "x", Integer.toString(r.x));
		props.setProperty(prefix + "y", Integer.toString(r.y));
		props.setProperty(prefix + "w", Integer.toString(r.width));
		props.setProperty(prefix + "h", Integer.toString(r.height));
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
		props.setProperty("main-last-directory", lastDirectory.getAbsolutePath());
	}
	/** Save the configuration. */
	public void saveConfig() {
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
			saveConfig();
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
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
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
		locateMenu("Engine").addSeparator();
		addItem(fromMethod(this, "doExit"), "Engine", "Exit");
		
		addItem(fromMethod(this, "doManageLocalKeyStores"), "Keystores", "Manage local keystores...");
		menusToEnable.put(addItem(fromMethod(this, "doManageKeyStores"), "Keystores", "Manage engine keystores..."), AdvanceUserRights.LIST_KEYSTORES);
		
		menusToEnable.put(addItem(fromMethod(this, "doDownloadFlow"), "Flow", "Download..."), AdvanceUserRights.LIST_KEYSTORES);
		menusToEnable.put(addItem(fromMethod(this, "doUploadFlow"), "Flow", "Upload..."), AdvanceUserRights.LIST_REALMS);
		locateMenu("Flow").addSeparator();
		menusToEnable.put(addItem(fromMethod(this, "doVerifyFlow"), "Flow", "Verify..."), AdvanceUserRights.LIST_REALMS);
		menusToEnable.put(addItem(fromMethod(this, "doDebugFlow"), "Flow", "Debug..."), AdvanceUserRights.LIST_REALMS);
		menusToEnable.put(addItem(fromMethod(this, "doLastCompilationResult"), "Flow", "Last compilation result..."), AdvanceUserRights.LIST_REALMS);
		
		menusToEnable.put(addItem(fromMethod(this, "doManageRealms"), "Administration", "Manage realms..."), AdvanceUserRights.LIST_REALMS);
		menusToEnable.put(addItem(fromMethod(this, "doManageUsers"), "Administration", "Manage users..."), AdvanceUserRights.LIST_USERS);
		menusToEnable.put(addItem(fromMethod(this, "doManageNotificationGroups"), "Administration", "Manage notification groups..."), AdvanceUserRights.LIST_NOTIFICATION_GROUPS);
		menusToEnable.put(addItem(fromMethod(this, "doListBlocks"), "Administration", "List blocks..."), AdvanceUserRights.LIST_BLOCKS);
		locateMenu("Administration").addSeparator();
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
	 * Locate a particular menu via the path.
	 * @param titles the menu titles
	 * @return the menu located
	 */
	protected JMenu locateMenu(String... titles) {
		JMenu m = findOrCreateMenu(titles[0]);
		for (int i = 1; i < titles.length; i++) {
			m = findOrCreateMenu(m, titles[i]);
		}
		return m;
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
			GUIUtils.errorMessage(this, ex);
		} catch (AdvanceControlException ex) {
			GUIUtils.errorMessage(this, ex);
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
		displayFrame(f, "manageengineks-", "Manage engine keystores");

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
		displayFrame(f, "managelocalks-", "Manage local keystores");

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
				GUIUtils.errorMessage(frame, error);
			}
		}
	}
	/**
	 * Display the generic list frame.
	 * @param f the frame object
	 * @param prefix the state save prefix
	 * @param title the title
	 */
	void displayFrame(final GenericListingFrame<?> f, final String prefix, String title) {
		f.setTitle(get(title));
		f.fireTableStructureChanged();
		setEngineInfo(f.engineInfo);
		f.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				storeFrameState(f, props, prefix);
			}
		});
		if (!applyFrameState(f, props, prefix)) {
			f.pack();
		}
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
									GUIUtils.errorMessage(f, t);
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
							GUIUtils.errorMessage(f, t);
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
							GUIUtils.errorMessage(f, t);
						} else {
							f.refresh();
						}
					}
				}).execute();
			}
		});
		if (user.rights.contains(AdvanceUserRights.DELETE_REALM)) {
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
		}
		
		f.setColumnCount(4);
		
		displayFrame(f, "managerealms-", "Manage realms");
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
		
		if (user.rights.contains(AdvanceUserRights.CREATE_USER)) {
			f.setExtraButton(0, "Create...", new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					final CCDetailDialog<?> d = createUserDialog(f.getRows(), null);
					setEngineInfo(d.engineInfo);
					d.pack();
					d.setLocationRelativeTo(f);
					d.setVisible(true);
					
					d.addWindowListener(new WindowAdapter() {
						@Override
						public void windowClosing(WindowEvent e) {
							storeFrameState(d, props, "ccuserdetails-");
						}
					});
					applyFrameState(d, props, "ccuserdetails-");
				}
			});
		}
		f.setDisplayItem(new Action1<AdvanceUser>() {
			@Override
			public void invoke(AdvanceUser value) {
				CCDetailDialog<?> d = createUserDialog(f.getRows(), value);
				setEngineInfo(d.engineInfo);
				d.pack();
				d.setLocationRelativeTo(f);
				d.setVisible(true);
			}
		});
		if (user.rights.contains(AdvanceUserRights.DELETE_USER)) {
			f.setExtraButton(1, "Delete", new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					deleteItems(f, new Func1<AdvanceUser, Throwable>() {
						@Override
						public Throwable invoke(AdvanceUser param1) {
							try {
								engine.datastore().deleteUser(param1.name, user.name);
								return null;
							} catch (Throwable t) {
								return t;
							}
						}
					});
				}
			});
		}		
		f.setColumnCount(5);
		displayFrame(f, "manageusers-", "Manage users");
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
		
		if (user.rights.contains(AdvanceUserRights.CREATE_JDBC_DATA_SOURCE)) {
			f.setExtraButton(0, "Create...", new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					final CCDetailDialog<?> d = createJDBCDialog(f.getRows(), null);
					setEngineInfo(d.engineInfo);
					d.pack();
					d.setLocationRelativeTo(f);
					d.setVisible(true);
				}
			});
		}
		f.setDisplayItem(new Action1<AdvanceJDBCDataSource>() {
			@Override
			public void invoke(AdvanceJDBCDataSource value) {
				CCDetailDialog<?> d = createJDBCDialog(f.getRows(), value);
				setEngineInfo(d.engineInfo);
				d.pack();
				d.setLocationRelativeTo(f);
				d.setVisible(true);
			}
		});
		if (user.rights.contains(AdvanceUserRights.DELETE_JDBC_DATA_SOURCE)) {
			f.setExtraButton(1, "Delete", new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					deleteItems(f, new Func1<AdvanceJDBCDataSource, Throwable>() {
						@Override
						public Throwable invoke(AdvanceJDBCDataSource param1) {
							try {
								engine.datastore().deleteJDBCDataSource(param1.name);
								return null;
							} catch (Throwable t) {
								return t;
							}
						}
					});
				}
			});
		}
		
		
		displayFrame(f, "managejdbc-", "Manage JDBC data sources");
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
		if (user.rights.contains(AdvanceUserRights.CREATE_SOAP_CHANNEL)) {
			f.setExtraButton(0, "Create...", new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					createSOAPDialog(f, f.getRows(), null);
				}
			});
		}
		f.setDisplayItem(new Action1<AdvanceSOAPChannel>() {
			@Override
			public void invoke(AdvanceSOAPChannel value) {
				createSOAPDialog(f, f.getRows(), value);
			}
		});
		if (user.rights.contains(AdvanceUserRights.DELETE_SOAP_CHANNEL)) {
			f.setExtraButton(1, "Delete", new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					deleteItems(f, new Func1<AdvanceSOAPChannel, Throwable>() {
						@Override
						public Throwable invoke(AdvanceSOAPChannel param1) {
							try {
								engine.datastore().deleteSOAPChannel(param1.name);
								return null;
							} catch (Throwable t) {
								return t;
							}
						}
					});
				}
			});
		}
		f.setColumnCount(4);
		displayFrame(f, "managesoap-", "Manage SOAP channels");
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
		if (user.rights.contains(AdvanceUserRights.CREATE_JMS_ENDPOINT)) {
			f.setExtraButton(0, "Create...", new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					createJMSDialog(f, f.getRows(), null);
				}
			});
		}
		f.setDisplayItem(new Action1<AdvanceJMSEndpoint>() {
			@Override
			public void invoke(AdvanceJMSEndpoint value) {
				createJMSDialog(f, f.getRows(), value);
			}
		});
		if (user.rights.contains(AdvanceUserRights.DELETE_JMS_ENDPOINT)) {
			f.setExtraButton(1, "Delete", new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					deleteItems(f, new Func1<AdvanceJMSEndpoint, Throwable>() {
						@Override
						public Throwable invoke(AdvanceJMSEndpoint param1) {
							try {
								engine.datastore().deleteJMSEndpoint(param1.name);
								return null;
							} catch (Throwable t) {
								return t;
							}
						}
					});
				}
			});
		}
		f.setColumnCount(5);
		displayFrame(f, "managejdbc-", "Manage JDBC data sources");
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
		if (user.rights.contains(AdvanceUserRights.CREATE_WEB_DATA_SOURCE)) {
			f.setExtraButton(0, "Create...", new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					createWebDialog(f, f.getRows(), null);
				}
			});
		}
		f.setDisplayItem(new Action1<AdvanceWebDataSource>() {
			@Override
			public void invoke(AdvanceWebDataSource value) {
				createWebDialog(f, f.getRows(), value);
			}
		});
		if (user.rights.contains(AdvanceUserRights.DELETE_WEB_DATA_SOURCE)) {
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
		}
		displayFrame(f, "manageweb-", "Manage Web data sources");
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
						GUIUtils.errorMessage(f, t);
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
		if (user.rights.contains(AdvanceUserRights.CREATE_FTP_DATA_SOURCE)) {
			f.setExtraButton(0, "Create...", new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					createFTPDialog(f, f.getRows(), null);
				}
			});
		}
		f.setDisplayItem(new Action1<AdvanceFTPDataSource>() {
			@Override
			public void invoke(AdvanceFTPDataSource value) {
				createFTPDialog(f, f.getRows(), value);
			}
		});
		if (user.rights.contains(AdvanceUserRights.DELETE_FTP_DATA_SOURCE)) {
			f.setExtraButton(1, "Delete", new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					deleteItems(f, new Func1<AdvanceFTPDataSource, Throwable>() {
						@Override
						public Throwable invoke(AdvanceFTPDataSource param1) {
							try {
								engine.datastore().deleteFTPDataSource(param1.name);
								return null;
							} catch (Throwable t) {
								return t;
							}
						}
					});
				}
			});
		}
		f.setColumnCount(4);
		displayFrame(f, "manageftp-", "Manage FTP data sources");
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
		if (user.rights.contains(AdvanceUserRights.CREATE_LOCAL_FILE_DATA_SOURCE)) {
			f.setExtraButton(0, "Create...", new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					createLocalDialog(f, f.getRows(), null);
				}
			});
		}
		f.setDisplayItem(new Action1<AdvanceLocalFileDataSource>() {
			@Override
			public void invoke(AdvanceLocalFileDataSource value) {
				createLocalDialog(f, f.getRows(), value);
			}
		});
		if (user.rights.contains(AdvanceUserRights.DELETE_LOCAL_FILE_DATA_SOURCE)) {
			f.setExtraButton(1, "Delete", new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					deleteItems(f, new Func1<AdvanceLocalFileDataSource, Throwable>() {
						@Override
						public Throwable invoke(AdvanceLocalFileDataSource param1) {
							try {
								engine.datastore().deleteLocalFileDataSource(param1.name);
								return null;
							} catch (Throwable t) {
								return t;
							}
						}
					});
				}
			});
		}
		f.setColumnCount(4);
		displayFrame(f, "managelocal-", "Manage Local data sources");
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
		f.setExtraButton(0, "Download...", new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				doDownloadFlowAction(f, f.getSelectedItems().iterator());
			}
		});
		f.setColumnCount(4);
		displayFrame(f, "managedownload-", "Download Flow");
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
		f.table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		f.setExtraButton(0, "Upload...", new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				List<AdvanceRealm> list = f.getSelectedItems();
				if (list.size() == 1) {
					doUploadFlowAction(f, list.get(0).name);
				}
			}
		});
		displayFrame(f, "manageupload-", "Upload Flow");
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
		displayFrame(f, "manageemail-", "Email boxes");
	}
	/**
	 * Login into a local engine.
	 */
	void doLocalLogin() {
		final CCLocalLogin login = new CCLocalLogin(this);
		login.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				storeFrameState(login, props, "locallogin-");
			}
		});
		login.setLocationRelativeTo(null);
		applyFrameState(login, props, "locallogin-");
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
				GUIUtils.errorMessage(this, ex);
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
	 * @param f the parent frame.
	 * @param list the available list
	 * @param selected the selected item or null to indicate a new item should be created
	 * @return the dialog created
	 */
	CCDetailDialog<AdvanceWebDataSource> createWebDialog(final JFrame f,
			final List<AdvanceWebDataSource> list, final AdvanceWebDataSource selected) {
		final CCWebDetails wd = new CCWebDetails(this);
		final CCDetailDialog<AdvanceWebDataSource> dialog = new CCDetailDialog<AdvanceWebDataSource>(this, wd);
		dialog.setTitle(this.get("Web Data Source Details"));
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
					/** The keystore list. */
					List<AdvanceKeyStore> keyStores;
					@Override
					public void run() {
						try {
							e = engine.datastore().queryWebDataSource(name);
							keyStores = engine.datastore().queryKeyStores();
						} catch (Throwable t) {
							this.t = t;
						}
					}
					@Override
					public void done() {
						if (t != null) {
							GUIUtils.errorMessage(dialog, t);
						} else {
							wd.setKeyStores(keyStores);
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
			wd.name.setEditable(false);
		}
		dialog.buttons.setClose(new Action0() {
			@Override
			public void invoke() {
				dialog.close();
			}
		});
		dialog.buttons.setRefresh(new Action0() {
			@Override
			public void invoke() {
				retrieveAction.invoke();
			}
		});
		
		Func1<AdvanceWebDataSource, Throwable> saver = new Func1<AdvanceWebDataSource, Throwable>() {
			@Override
			public Throwable invoke(AdvanceWebDataSource param1) {
				try {
					engine.datastore().updateWebDataSource(param1);
					return null;
				} catch (Throwable t) {
					return t;
				}
			}
		};
		
		dialog.buttons.setSave(createSaver(wd, dialog, false, saver));
		dialog.buttons.setSaveAndClose(createSaver(wd, dialog, true, saver));
		
		GUIUtils.getWorker(new RetrieverWorkItem<List<AdvanceKeyStore>>(dialog, f) {
			@Override
			public List<AdvanceKeyStore> invoke() throws Throwable {
				return engine.datastore().queryKeyStores();
			}
			@Override
			public void setter(List<AdvanceKeyStore> value) {
				wd.setKeyStores(value);
			}
		}).execute();
		
		return dialog;
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
	/** Show the results of the last compilation. */
	void doLastCompilationResult() {
		LOG.error("Implement!");
	}
	/** Debug a flow. */
	void doDebugFlow() {
		LOG.error("Implement!");
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
						GUIUtils.errorMessage(CCMain.this, t);
					}
				}
			}).execute();
		}
	}
	/**
	 * Construct a web dialog.
	 * @param list the available list
	 * @param selected the selected item or null to indicate a new item should be created
	 * @return the dialog created
	 */
	CCDetailDialog<AdvanceUser> createUserDialog(final List<AdvanceUser> list, final AdvanceUser selected) {
		
		final CCUserDetails ud = new CCUserDetails(this);
		final CCDetailDialog<AdvanceUser> dialog = new CCDetailDialog<AdvanceUser>(this, ud);
		dialog.setTitle(get("Web Data Source Details"));
		dialog.pager.setItemName(new Func1<AdvanceUser, String>() {
			@Override
			public String invoke(AdvanceUser param1) {
				return param1.name + " [" + param1.email + "]";
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
					AdvanceUser e;
					/** The realms. */
					List<AdvanceRealm> realms;
					/** The keystores. */
					List<AdvanceKeyStore> keystores;
					@Override
					public void run() {
						try {
							e = engine.datastore().queryUser(name);
							realms = engine.datastore().queryRealms();
							keystores = engine.datastore().queryKeyStores();
						} catch (Throwable t) {
							this.t = t;
						}
					}
					@Override
					public void done() {
						if (t != null) {
							GUIUtils.errorMessage(dialog, t);
						} else {
							ud.setRealms(realms);
							ud.setKeyStores(keystores);
							ud.load(e);
							dialog.createModify.set(e);
							dialog.pack();
						}
						dialog.pager.setEnabled(true);
					}
				}).execute();
			}
		};
		
		dialog.pager.setSelect(new Action1<AdvanceUser>() {
			@Override
			public void invoke(AdvanceUser value) {
				retrieveAction.invoke();
			}
		});
		dialog.buttons.showRefresh(selected != null);
		dialog.showPager(selected != null);
		if (selected == null) {
			dialog.showCreateModify(false);
		} else {
			dialog.pager.setSelectedItem(selected);
			ud.name.setEditable(false);
		}
		dialog.buttons.setClose(new Action0() {
			@Override
			public void invoke() {
				dialog.close();
			}
		});
		dialog.buttons.setRefresh(new Action0() {
			@Override
			public void invoke() {
				retrieveAction.invoke();
			}
		});
		
		
		Func1<AdvanceUser, Throwable> saver = new Func1<AdvanceUser, Throwable>() {
			@Override
			public Throwable invoke(AdvanceUser param1) {
				try {
					engine.datastore().updateUser(param1);
					return null;
				} catch (Throwable t) {
					return t;
				}
			}
		};
		
		dialog.buttons.setSave(createSaver(ud, dialog, false, saver));
		dialog.buttons.setSaveAndClose(createSaver(ud, dialog, true, saver));
		
		return dialog;
	}
	/**
	 * Construct a detailed dialog.
	 * @param <K> the identifier type of the record
	 * @param <T> the expected record type
	 * @param <V> the dialog type
	 * @param list the available list
	 * @param selected the selected item or null to indicate a new item should be created
	 * @param detailPanel the panel containing the detail fields
	 * @param namer the function to convert an entry into string
	 * @param retriever the function to retrieve a record through its id
	 * @param saver the function to save a record and report back an error
	 * @return the dialog created
	 */
	<K, T extends AdvanceCreateModifyInfo & Identifiable<K>, V extends JComponent & CCLoadSave<T>> 
	CCDetailDialog<T> createDetailDialog(
			final List<T> list, 
			final T selected,
			final V detailPanel,
			final Func1<T, String> namer,
			final Func1<? super K, ? extends Option<? extends T>> retriever,
			final Func1<T, Throwable> saver
			) {
		final CCDetailDialog<T> dialog = new CCDetailDialog<T>(this, detailPanel);
		dialog.pager.setItemName(namer);
		dialog.pager.setItems(list);
		
		final Action0 retrieveAction = new Action0() {
			@Override
			public void invoke() {
				final K id = dialog.pager.getSelectedItem().id(); 
				dialog.pager.setEnabled(false);
				GUIUtils.getWorker(new WorkItem() {
					/** The error. */
					Throwable t;
					/** The data. */
					T e;
					@Override
					public void run() {
						Option<? extends T> result = retriever.invoke(id);
						if (Option.isError(result)) {
							t = Option.getError(result);
						} else {
							e = result.value();
						}
					}
					@Override
					public void done() {
						if (t != null) {
							GUIUtils.errorMessage(dialog, t);
						} else {
							detailPanel.load(e);
							dialog.createModify.set(e);
							dialog.pack();
						}
						dialog.pager.setEnabled(true);
					}
				}).execute();
			}
		};
		
		dialog.pager.setSelect(new Action1<T>() {
			@Override
			public void invoke(T value) {
				retrieveAction.invoke();
			}
		});
		dialog.buttons.showRefresh(selected != null);
		dialog.showPager(selected != null);
		if (selected == null) {
			dialog.showCreateModify(false);
		} else {
			dialog.pager.setSelectedItem(selected);
		}
		dialog.buttons.setClose(new Action0() {
			@Override
			public void invoke() {
				dialog.close();
			}
		});
		dialog.buttons.setRefresh(new Action0() {
			@Override
			public void invoke() {
				retrieveAction.invoke();
			}
		});
		
		
		dialog.buttons.setSave(createSaver(detailPanel, dialog, false, saver));
		dialog.buttons.setSaveAndClose(createSaver(detailPanel, dialog, true, saver));
		
		return dialog;
	}
	/**
	 * Create a web saver action.
	 * @param <K> the identifier type
	 * @param <T> the identifiable object type
	 * @param ud the details panel.
	 * @param dialog the dialog
	 * @param close close dialog
	 * @param saveFunction the function to save the value and return a potential exeption
	 * @return the action
	 */
	<K, T extends Identifiable<K>> Action0 createSaver(final CCLoadSave<T> ud, 
			final CCDetailDialog<T> dialog, 
			final boolean close,
			final Func1<T, Throwable> saveFunction) {
		return new Action0() {
			@Override
			public void invoke() {
				final T e = ud.save();
				if (e == null) {
					return;
				}
				
				GUIUtils.getWorker(new WorkItem() {
					/** The exception. */
					protected Throwable t;
					@Override
					public void run() {
						t = saveFunction.invoke(e);
					}
					@Override
					public void done() {
						if (t != null) {
							GUIUtils.errorMessage(dialog, t);
						} else {
							if (close) {
								dialog.close();
							} else {
								dialog.showCreateModify(true);
								dialog.showPager(true);
								dialog.buttons.showRefresh(true);
								dialog.pack();
								updatePager(dialog.pager, e);
								ud.onAfterSave();
							}
						}
					}
				}).execute();
			}
		};
	}
	/**
	 * The action to invoke a testXYZ method and report its string result or exception.
	 * @author karnokd, 2011.10.13.
	 */
	public abstract static class TestAction implements Action1<String> {
		/** The exception. */
		protected Throwable t;
		/** The call result. */
		protected String result;
		/** The parent component. */
		protected final Component parent;
		/**
		 * Constructor with parent.
		 * @param parent the parent
		 */
		public TestAction(Component parent) {
			this.parent = parent;
		}
		/**
		 * The test method to execute.
		 * @param value the object id to test
		 * @return the test results
		 * @throws Throwable on error
		 */
		public abstract String run(String value) throws Throwable;
		@Override
		public void invoke(final String value) {
			GUIUtils.getWorker(new WorkItem() {
				/** The exception. */
				Throwable t;
				/** The call result. */
				String result;
				@Override
				public void run() {
					try {
						result = TestAction.this.run(value);
					} catch (Throwable t) {
						this.t = t;
					}
				}
				@Override
				public void done() {
					if (t != null) {
						GUIUtils.errorMessage(parent, t);
					} else
					if (!result.isEmpty()) {
						GUIUtils.errorMessage(parent, result);
					}
				}
			}).execute();
		}
	}
	/**
	 * Create a JDBC detail dialog.
	 * @param list the available list of items
	 * @param selected the selected item
	 * @return the dialog
	 */
	CCDetailDialog<AdvanceJDBCDataSource> createJDBCDialog(List<AdvanceJDBCDataSource> list, AdvanceJDBCDataSource selected) {
		CCJDBCDetails d = new CCJDBCDetails(this);
		d.setTestAction(new TestAction(d) {
			@Override
			public String run(String value) throws Throwable {
				return engine.testJDBCDataSource(value);

			}
		});
		return createDetailDialog(list, selected,
			d,
			new Func1<AdvanceJDBCDataSource, String>() {
				@Override
				public String invoke(AdvanceJDBCDataSource param1) {
					return param1.name + " [" + param1.url + "]";
				}
			},
			new Func1<String, Option<AdvanceJDBCDataSource>>() {
				@Override
				public Option<AdvanceJDBCDataSource> invoke(String param1) {
					try {
						return Option.some(engine.datastore().queryJDBCDataSource(param1));
					} catch (Throwable t) {
						return Option.error(t);
					}
				}
			},
			new Func1<AdvanceJDBCDataSource, Throwable>() {
				@Override
				public Throwable invoke(AdvanceJDBCDataSource param1) {
					try {
						engine.datastore().updateJDBCDataSource(param1);
						return null;
					} catch (Throwable t) {
						return t;
					}
				}
			}
		);
	}
	/**
	 * Create a SOAP detail dialog.
	 * @param f the parent frame
	 * @param list the list of available entries.
	 * @param selected the selected entry
	 * @return the dialog object
	 */
	CCDetailDialog<AdvanceSOAPChannel> createSOAPDialog(final JFrame f,
			List<AdvanceSOAPChannel> list, AdvanceSOAPChannel selected) {
		final CCSOAPDetails d = new CCSOAPDetails(this);
		
		final CCDetailDialog<AdvanceSOAPChannel> dialog = createDetailDialog(list, selected,
				d,
				new Func1<AdvanceSOAPChannel, String>() {
					@Override
					public String invoke(AdvanceSOAPChannel param1) {
						return param1.name + " [" + param1.endpoint + "]";
					}
				},
				new Func1<String, Option<AdvanceSOAPChannel>>() {
					@Override
					public Option<AdvanceSOAPChannel> invoke(String param1) {
						try {
							return Option.some(engine.datastore().querySOAPChannel(param1));
						} catch (Throwable t) {
							return Option.error(t);
						}
					}
				},
				new Func1<AdvanceSOAPChannel, Throwable>() {
					@Override
					public Throwable invoke(AdvanceSOAPChannel param1) {
						try {
							engine.datastore().updateSOAPChannel(param1);
							return null;
						} catch (Throwable t) {
							return t;
						}
					}
				}
			);
		
		GUIUtils.getWorker(new RetrieverWorkItem<List<AdvanceKeyStore>>(dialog, f) {
			@Override
			public List<AdvanceKeyStore> invoke() throws Throwable {
				return engine.datastore().queryKeyStores();
			}
			@Override
			public void setter(List<AdvanceKeyStore> value) {
				d.setKeyStores(value);
			}
		}).execute();
		return dialog;
	}
	/**
	 * Retrieves a value before showing a dialog.
	 * @author karnokd, 2011.10.13.
	 *
	 * @param <T> the value type to retrieve.
	 */
	public abstract class RetrieverWorkItem<T> implements WorkItem {
		/** The result or error option. */
		protected Option<T> result;
		/** The dialog to display. */
		protected final CCDetailDialog<?> dialog;
		/** The parent component to place realtive to. */
		protected final Component parent;
		/**
		 * Constructor, sets the dialog and parent.
		 * @param dialog the dialog
		 * @param parent the parent
		 */
		public RetrieverWorkItem(CCDetailDialog<?> dialog, Component parent) {
			this.dialog = dialog;
			this.parent = parent;
		}
		/**
		 * The body function to invoke in background.
		 * @return the object value
		 * @throws Throwable on error
		 */
		public abstract T invoke() throws Throwable;
		@Override
		public void run() {
			try {
				result = Option.some(invoke());
			} catch (Throwable t) {
				result = Option.error(t);
			}
		}
		/**
		 * Set the retrieved value in EDT.
		 * @param value the value to set
		 */
		public abstract void setter(T value);
		@Override
		public void done() {
			if (Option.isSome(result)) {
				setter(result.value());
				setEngineInfo(dialog.engineInfo);
				dialog.pack();
				dialog.setLocationRelativeTo(parent);
				dialog.setVisible(true);
			} else {
				GUIUtils.errorMessage(parent, Option.getError(result));
			}
		}
	}
	/**
	 * Create a SOAP detail dialog.
	 * @param f the parent frame
	 * @param list the list of available entries.
	 * @param selected the selected entry
	 * @return the dialog object
	 */
	CCDetailDialog<AdvanceJMSEndpoint> createJMSDialog(final JFrame f,
			List<AdvanceJMSEndpoint> list, AdvanceJMSEndpoint selected) {
		final CCJMSDetails d = new CCJMSDetails(this);
		d.setTestAction(new TestAction(d) {
			@Override
			public String run(String value) throws Throwable {
				return engine.testJMSEndpoint(value);

			}
		});
		
		final CCDetailDialog<AdvanceJMSEndpoint> dialog = createDetailDialog(list, selected,
				d,
				new Func1<AdvanceJMSEndpoint, String>() {
					@Override
					public String invoke(AdvanceJMSEndpoint param1) {
						return param1.name + " [" + param1.url + "]";
					}
				},
				new Func1<String, Option<AdvanceJMSEndpoint>>() {
					@Override
					public Option<AdvanceJMSEndpoint> invoke(String param1) {
						try {
							return Option.some(engine.datastore().queryJMSEndpoint(param1));
						} catch (Throwable t) {
							return Option.error(t);
						}
					}
				},
				new Func1<AdvanceJMSEndpoint, Throwable>() {
					@Override
					public Throwable invoke(AdvanceJMSEndpoint param1) {
						try {
							engine.datastore().updateJMSEndpoint(param1);
							return null;
						} catch (Throwable t) {
							return t;
						}
					}
				}
			);
		
		setEngineInfo(dialog.engineInfo);
		dialog.pack();
		dialog.setLocationRelativeTo(f);
		dialog.setVisible(true);
		
		return dialog;
	}
	/**
	 * Create the FTP details dialog.
	 * @param f the parent frame
	 * @param list the list of options
	 * @param selected the selected option
	 */
	void createFTPDialog(JFrame f, List<AdvanceFTPDataSource> list, AdvanceFTPDataSource selected) {
		final CCFTPDetails d = new CCFTPDetails(this);
		d.setTestAction(new TestAction(d) {
			@Override
			public String run(String value) throws Throwable {
				return engine.testFTPDataSource(value);

			}
		});
		
		final CCDetailDialog<AdvanceFTPDataSource> dialog = createDetailDialog(list, selected,
				d,
				new Func1<AdvanceFTPDataSource, String>() {
					@Override
					public String invoke(AdvanceFTPDataSource param1) {
						String prot = "ftp";
						if (param1.protocol == AdvanceFTPProtocols.FTPS) {
							prot = "ftps";
						} else
						if (param1.protocol == AdvanceFTPProtocols.SFTP) {
							prot = "sftp";
						}
						return param1.name + " [" + prot + "://" + param1.address + "/" + param1.remoteDirectory + "]";
					}
				},
				new Func1<String, Option<AdvanceFTPDataSource>>() {
					@Override
					public Option<AdvanceFTPDataSource> invoke(String param1) {
						try {
							return Option.some(engine.datastore().queryFTPDataSource(param1));
						} catch (Throwable t) {
							return Option.error(t);
						}
					}
				},
				new Func1<AdvanceFTPDataSource, Throwable>() {
					@Override
					public Throwable invoke(AdvanceFTPDataSource param1) {
						try {
							engine.datastore().updateFTPDataSource(param1);
							return null;
						} catch (Throwable t) {
							return t;
						}
					}
				}
			);
		
		GUIUtils.getWorker(new RetrieverWorkItem<List<AdvanceKeyStore>>(dialog, f) {
			@Override
			public List<AdvanceKeyStore> invoke() throws Throwable {
				return engine.datastore().queryKeyStores();
			}
			@Override
			public void setter(List<AdvanceKeyStore> value) {
				d.setKeyStores(value);
			}
		}).execute();
	}
	/**
	 * Create the FTP details dialog.
	 * @param f the parent frame
	 * @param list the list of options
	 * @param selected the selected option
	 */
	void createLocalDialog(JFrame f, List<AdvanceLocalFileDataSource> list, AdvanceLocalFileDataSource selected) {
		final CCLocalDetails d = new CCLocalDetails(this);
		
		final CCDetailDialog<AdvanceLocalFileDataSource> dialog = createDetailDialog(list, selected,
				d,
				new Func1<AdvanceLocalFileDataSource, String>() {
					@Override
					public String invoke(AdvanceLocalFileDataSource param1) {
						return param1.name + " [" + param1.directory + "]";
					}
				},
				new Func1<String, Option<AdvanceLocalFileDataSource>>() {
					@Override
					public Option<AdvanceLocalFileDataSource> invoke(String param1) {
						try {
							return Option.some(engine.datastore().queryLocalFileDataSource(param1));
						} catch (Throwable t) {
							return Option.error(t);
						}
					}
				},
				new Func1<AdvanceLocalFileDataSource, Throwable>() {
					@Override
					public Throwable invoke(AdvanceLocalFileDataSource param1) {
						try {
							engine.datastore().updateLocalFileDataSource(param1);
							return null;
						} catch (Throwable t) {
							return t;
						}
					}
				}
			);
		setEngineInfo(dialog.engineInfo);
		dialog.pack();
		dialog.setLocationRelativeTo(f);
		dialog.setVisible(true);
	}
	/** Open the notification management screen. */
	void doManageNotificationGroups() {
		final CCGroups g = new CCGroups(this, engine.datastore());

		final String prefix = "managegroups-";
		
		
		g.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				storeFrameState(g, props, prefix);
			}
		});
		if (!applyFrameState(g, props, prefix)) {
			g.pack();
		}
		setEngineInfo(g.engineInfo);
		g.setLocationRelativeTo(this);
		g.setVisible(true);
		g.refresh();
		
	}
	/**
	 * Download a flow.
	 * @param frame the target frame
	 * @param realms the sequence of realms
	 */
	void doDownloadFlowAction(final JFrame frame, final Iterator<AdvanceRealm> realms) {
		if (realms.hasNext()) {
			AdvanceRealm r = realms.next();
			final String rname = r.name; 
			JFileChooser fc = new JFileChooser(lastDirectory);
			fc.setFileFilter(new FileNameExtensionFilter("XML files (*.xml)", "xml"));
			fc.setDialogTitle(format("Save flow of realm %s", rname));
			fc.setAcceptAllFileFilterUsed(true);
			if (fc.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
				final File f = fc.getSelectedFile();
				lastDirectory = f.getParentFile();
				GUIUtils.getWorker(new WorkItem() {
					/** The exception. */
					Throwable t;
					/** The returned flow. */
					AdvanceCompositeBlock b;
					@Override
					public void run() {
						try {
							b = engine.queryFlow(rname);
							if (b != null) {
								b.serializeFlow().save(f);
							}
						} catch (Throwable t) {
							this.t = t;
						}
					}
					@Override
					public void done() {
						if (t != null) {
							GUIUtils.errorMessage(frame, t);
						} else {
							if (b == null) {
								GUIUtils.infoMessage(frame, format("Realm %s was empty.", rname));
							}
							doDownloadFlowAction(frame, realms);
						}
					}
				}).execute();
			}
		}
	}
	/**
	 * Upload the selected file into the realm.
	 * @param frame the parent frame
	 * @param realm the target realm
	 */
	void doUploadFlowAction(final JFrame frame, final String realm) {
		JFileChooser fc = new JFileChooser(lastDirectory);
		fc.setFileFilter(new FileNameExtensionFilter("XML files (*.xml)", "xml"));
		fc.setDialogTitle(format("Upload flow into realm %s", realm));
		fc.setAcceptAllFileFilterUsed(true);
		if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
			final File f = fc.getSelectedFile();
			lastDirectory = f.getParentFile();
			GUIUtils.getWorker(new WorkItem() {
				/** The exception. */
				Throwable t;
				/** The compilation result. */
				AdvanceCompilationResult r;
				@Override
				public void run() {
					try {
						AdvanceCompositeBlock b = AdvanceCompositeBlock.parseFlow(XElement.parseXML(f));
						engine.updateFlow(realm, b, user.name);
						r = engine.queryCompilationResult(realm);
					} catch (Throwable t) {
						this.t = t;
					}
				}
				@Override
				public void done() {
					if (t != null) {
						GUIUtils.errorMessage(frame, t);
					} else {
						if (r != null && !r.success()) {
							StringBuilder b = new StringBuilder();
							for (AdvanceCompilationError e : r.errors) {
								b.append(e);
								b.append("\r\n");
							}
							GUIUtils.errorMessage(frame, b.toString());
						}
					}
				}
			}).execute();
		}
	}
	/** Verify a flow. */
	void doVerifyFlow() {
		JFileChooser fc = new JFileChooser(lastDirectory);
		fc.setFileFilter(new FileNameExtensionFilter("XML files (*.xml)", "xml"));
		fc.setDialogTitle(get("Verify flow"));
		fc.setAcceptAllFileFilterUsed(true);
		if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
			final File f = fc.getSelectedFile();
			lastDirectory = f.getParentFile();
			GUIUtils.getWorker(new WorkItem() {
				/** The exception. */
				Throwable t;
				/** The compilation result. */
				AdvanceCompilationResult r;
				@Override
				public void run() {
					try {
						AdvanceCompositeBlock b = AdvanceCompositeBlock.parseFlow(XElement.parseXML(f));
						r = engine.verifyFlow(b);
					} catch (Throwable t) {
						this.t = t;
					}
				}
				@Override
				public void done() {
					if (t != null) {
						GUIUtils.errorMessage(CCMain.this, t);
					} else {
						if (!r.success()) {
							StringBuilder b = new StringBuilder("<html><pre>");
							for (AdvanceCompilationError e : r.errors) {
								b.append(e);
								b.append("\r\n");
							}
							GUIUtils.errorMessage(CCMain.this, b.toString());
						} else {
							GUIUtils.infoMessage(CCMain.this, get("Verification successful!"));
						}
					}
				}
			}).execute();
		}
	}
	/** List supported blocks. */
	void doListBlocks() {
		final CCBlockList g = new CCBlockList(this, engine) {
			/** */
			private static final long serialVersionUID = -692098329697915848L;

			@Override
			public void export() {
				JFileChooser fc = new JFileChooser(lastDirectory);
				fc.setFileFilter(new FileNameExtensionFilter("XML files (*.xml)", "xml"));
				fc.setDialogTitle(get("Export block registry"));
				fc.setAcceptAllFileFilterUsed(true);
				if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
					final File f = fc.getSelectedFile();
					lastDirectory = f.getParentFile();
					final Component c = this;
					GUIUtils.getWorker(new WorkItem() {
						/** The exception. */
						Throwable t;
						@Override
						public void run() {
							try {
								AdvanceBlockRegistryEntry.serializeRegistry(engine.queryBlocks()).save(f);
							} catch (Throwable t) {
								this.t = t;
							}
						}
						@Override
						public void done() {
							if (t != null) {
								GUIUtils.errorMessage(c, t);
							}
						}
					}).execute();
				}
			}
		};
		final String prefix = "blocks-";
		
		g.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				storeFrameState(g, props, prefix);
			}
		});
		if (!applyFrameState(g, props, prefix)) {
			g.pack();
		}
		setEngineInfo(g.engineInfo);
		g.setLocationRelativeTo(this);
		g.setVisible(true);
		g.refresh();
	}
}
