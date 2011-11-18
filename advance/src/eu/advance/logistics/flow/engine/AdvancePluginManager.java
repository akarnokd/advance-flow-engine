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

package eu.advance.logistics.flow.engine;

import hu.akarnokd.reactive4java.reactive.DefaultObservable;
import hu.akarnokd.reactive4java.reactive.Observable;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.JarURLConnection;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLConnection;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

import javax.xml.stream.XMLStreamException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import eu.advance.logistics.flow.engine.model.rt.AdvanceBlockRegistryEntry;
import eu.advance.logistics.flow.engine.xml.typesystem.XElement;


/**
 * The ADVANCE plugin manager to load and watch plugins from a plugin directory.
 * <p>A plugin is a JAR file with a {@code block-registry.xml} found in its root directory.</p>
 * @author akarnokd, 2011.11.17.
 */
public class AdvancePluginManager implements Runnable {
	/** The logger. */
	protected static final Logger LOG = LoggerFactory.getLogger(AdvancePluginManager.class);
	/** The plugin directory. */
	protected final String directory;
	/**
	 * The observable to notify listeners if a plugin changed.
	 */
	protected final DefaultObservable<PluginChangeEvent> pluginChanged = new DefaultObservable<PluginChangeEvent>();
	/** The list of known plugins. */
	protected final List<AdvancePluginDetails> plugins = Lists.newArrayList();
	/**
	 * Contains information about a plugin. 
	 * @author akarnokd, 2011.11.17.
	 */
	public class AdvancePluginDetails {
		/** The filename. */
		public final String filename;
		/** The last known size. */
		public final long size;
		/** The last modification time. */
		public final long modified;
		/** The plugin object. */
		private AdvancePlugin plugin;
		/**
		 * Constructor. Initializes the fields.
		 * @param filename the plugin filename (within the work directory)
		 * @param size the file size
		 * @param modified the last modification date
		 */
		public AdvancePluginDetails(String filename, long size, long modified) {
			this.filename = filename;
			this.size = size;
			this.modified = modified;
		}
		/**
		 * Returns the {@code File} object representing this plugin.
		 * @return the file object
		 */
		public File getFile() {
			return new File(directory, filename);
		}
		@Override
		public boolean equals(Object obj) {
			if (obj instanceof AdvancePluginDetails) {
				AdvancePluginDetails pd = (AdvancePluginDetails) obj;
				return pd.filename.equals(filename);
				
			}
			return false;
		}
		@Override
		public int hashCode() {
			return filename.hashCode();
		}
		@Override
		public String toString() {
			return String.format("Plugin: %s (%s, %d bytes)", filename, new Date(modified), size);
		}
		/**
		 * Open and load this plugin.
		 * @return the plugin
		 */
		public synchronized AdvancePlugin open() {
			if (plugin == null) {
				final Map<String, AdvanceBlockRegistryEntry> blocks = Maps.newHashMap();
				final Map<String, XElement> schemas = Maps.newHashMap();
				try {
					final URLClassLoader c = createClassLoader(getFile().toURI().toURL());
					for (URL u : c.getURLs()) {
						try {
							blocks.putAll(getBlockRegistry(new URL("jar:" + u + "!/")));
							schemas.putAll(getSchemas(new URL("jar:" + u + "!/")));
						} catch (FileNotFoundException ex) {
							LOG.debug(ex.toString(), ex);
						} catch (IOException ex) {
							LOG.error(ex.toString(), ex);
						} catch (XMLStreamException ex) {
							LOG.error(ex.toString(), ex);
						}
					}
					final AdvanceDefaultBlockResolver br = new AdvanceDefaultBlockResolver(blocks, c);
					plugin = new AdvancePlugin() {
						@Override
						public AdvancePluginDetails details() {
							return AdvancePluginDetails.this;
						}
						@Override
						public AdvanceDefaultBlockResolver blockResolver() {
							return br;
						}
						@Override
						public Map<String, XElement> schemas() {
							return schemas;
						}
					};
				} catch (IOException ex) {
					LOG.error(ex.toString(), ex);
				}
			}
			return plugin;
		}
	}
	/**
	 * The Advance Plugin service provider interface.
	 * @author akarnokd, 2011.11.17.
	 */
	public interface AdvancePlugin {
		/**
		 * Returns the associated plugin details. 
		 * @return The associated plugin details. 
		 */
		AdvancePluginDetails details();
		/**
		 * Returns a block resolver for this plugin.
		 * @return the block resolver
		 */
		AdvanceBlockResolver blockResolver();
		/**
		 * The map of schemas supported by this plugin.
		 * @return the schema map 
		 */
		Map<String, XElement> schemas();
	}
	/**
	 * The plugin change event type.
	 * @author akarnokd, 2011.11.17.
	 */
	public enum PluginChangeEventType {
		/** A new plugin was added. */
		ADDED,
		/** An existing plugin was modified. */
		MODIFIED,
		/** A plugin was removed. */
		REMOVED
	}
	/**
	 * The plugin change event.
	 * @author akarnokd, 2011.11.17.
	 */
	public static class PluginChangeEvent {
		/** The change type. */
		public final PluginChangeEventType type;
		/** The plugin affected. */
		public final AdvancePluginDetails plugin;
		/**
		 * Constructor. Initializes the fields
		 * @param type the event type
		 * @param plugin the affected plugin
		 */
		public PluginChangeEvent(PluginChangeEventType type,
				AdvancePluginDetails plugin) {
			this.type = type;
			this.plugin = plugin;
		}
		
	}
	/**
	 * Constructor.
	 * @param directory the plugin directory
	 */
	public AdvancePluginManager(String directory) {
		this.directory = directory;
	}
	
	@Override
	public void run() {
		List<AdvancePluginDetails> current = scanForPlugins();
		List<AdvancePluginDetails> old = plugins();
		List<AdvancePluginDetails> next = merge(old, current);
		synchronized (plugins) {
			plugins.clear();
			plugins.addAll(next);
		}
	}
	/**
	 * The observable for plugin change events.
	 * @return the observable for plugin change events.
	 */
	public Observable<PluginChangeEvent> pluginChangeEvents() {
		return pluginChanged;
	}
	/**
	 * Compare the state of the old and new plugins and send out
	 * a notification if a plugin has changed.
	 * @param old the old list of plugins
	 * @param newer the newer list of plugins
	 * @return true if any plugin changed
	 */
	protected List<AdvancePluginDetails> merge(List<AdvancePluginDetails> old, List<AdvancePluginDetails> newer) {
		Set<AdvancePluginDetails> oldSet = Sets.newHashSet(old);
		List<AdvancePluginDetails> result = Lists.newArrayList();
		outer:
		for (AdvancePluginDetails pold : old) {
			for (AdvancePluginDetails pnew : newer) {
				if (pold.filename.equals(pnew.filename)) {
					if (pold.modified != pnew.modified || pold.size != pnew.size) {
						result.add(pnew);
					} else {
						result.add(pold);
					}
					continue outer;
 				}
			}
			notify(PluginChangeEventType.REMOVED, pold);
		}
		for (AdvancePluginDetails pnew : newer) {
			if (!oldSet.contains(pnew)) {
				result.add(pnew);
			}
		}
		return result;
	}
	/**
	 * Send out the notification.
	 * @param type the notification type
	 * @param plugin the affected plugin
	 */
	protected void notify(PluginChangeEventType type, AdvancePluginDetails plugin) {
		pluginChanged.next(new PluginChangeEvent(type, plugin));
	}
	/**
	 * Scan the plugin directory for plugins.
	 * @return the list of plugins
	 */
	protected List<AdvancePluginDetails> scanForPlugins() {
		File dir = new File(directory);
		File[] entries = dir.listFiles();
		List<AdvancePluginDetails> result = Lists.newArrayList();
		if (entries != null) {
			for (File f : entries) {
				if (f.getName().toLowerCase().endsWith(".jar")) {
					try {
						JarFile jf = new JarFile(f);
						try {
							if (jf.getEntry("block-registry.xml") != null) {
								AdvancePluginDetails d = new AdvancePluginDetails(
										f.getName(), f.length(), f.lastModified());
								result.add(d);
							}
						} finally {
							jf.close();
						}
					} catch (IOException ex) {
						
					}
				}
			}
		}
		return result;
	}
	/**
	 * Create a classloader for the specified plugin url and adding its dependencies as well.
	 * @param pluginURL the url to the plugin jar
	 * @return the classloader
	 */
	public static URLClassLoader createClassLoader(URL pluginURL) {
		try {
			List<URL> urls = Lists.newArrayList();
			urls.add(pluginURL);
			URLConnection c = pluginURL.openConnection();
			if (c instanceof JarURLConnection) {
				JarURLConnection juc = (JarURLConnection) c;
				juc.connect();
				JarFile jf = juc.getJarFile();
				try {
					processJar(pluginURL, urls, jf);
				} finally {
					jf.close();
				}
				
			} else
			if (pluginURL.getProtocol().equals("file")) {
				JarFile jf = new JarFile(new File(pluginURL.toURI()));
				try {
					processJar(pluginURL, urls, jf);
				} finally {
					jf.close();
				}
			} else {
				throw new IllegalArgumentException("Unsupported plugin url: " + pluginURL);
			}
			return new URLClassLoader(urls.toArray(new URL[0]), ClassLoader.getSystemClassLoader());
		} catch (URISyntaxException ex) {
			throw new IllegalArgumentException(ex);
		} catch (IOException ex) {
			throw new IllegalArgumentException(ex);
		}
	}

	/**
	 * Process the contents of the given Jar file.
	 * @param baseURL the base URL
	 * @param urls the list of output urls
	 * @param juc the jar file
	 * @throws IOException on error 
	 */
	protected static void processJar(URL baseURL, List<URL> urls, JarFile juc)
			throws IOException {
		Manifest mf = juc.getManifest();
		String cp = mf.getMainAttributes().getValue(Attributes.Name.CLASS_PATH);
		if (cp != null) {
			
			// derive the parent directory from the url;
			String p = baseURL.getPath();
			int idx = p.lastIndexOf('/');
			if (idx >= 0) {
				p = p.substring(0, idx);
			}
			
			// add URLs for the dependencies
			String[] dependencies = cp.split("\\s+");
			for (String d : dependencies) {
				urls.add(new URL(baseURL.getProtocol(), baseURL.getHost(), p + "/" + d));
			}
		}
	}
	/**
	 * Extract the {@code block-registry.xml} from the given base URL.
	 * @param u the URL
	 * @return the block registry map
	 * @throws IOException if the registry could't be loaded
	 * @throws XMLStreamException if the registry couldn't be parsed
	 */
	public static Map<String, AdvanceBlockRegistryEntry> getBlockRegistry(URL u) 
			throws IOException, XMLStreamException {
		URL registryFile = new URL(u, "block-registry.xml");
		InputStream in = registryFile.openStream();
		try {
			Map<String, AdvanceBlockRegistryEntry> result = Maps.newHashMap();
			for (AdvanceBlockRegistryEntry e : AdvanceBlockRegistryEntry.parseRegistry(XElement.parseXML(in))) {
				result.put(e.id, e);
			}
			return result;
		} finally {
			in.close();
		}
	}
	/**
	 * Extract the XSDs from the specified URL directory.
	 * @param u the target URL referencing a directory
	 * @return the map from the schema file name to its parsed content.
	 * @throws IOException if an access error occurs
	 * @throws XMLStreamException if a parse error occurs
	 */
	public static Map<String, XElement> getSchemas(URL u) 
			throws IOException, XMLStreamException {
		Map<String, XElement> result = Maps.newHashMap();
		if (u.getProtocol().equals("file")) {
			try {
				File directory = new File(u.toURI());
				if (directory.isDirectory()) {
					File[] files = directory.listFiles();
					if (files != null) {
						for (File f : files) {
							if (f.getName().toLowerCase().endsWith(".xsd")) {
								result.put(f.getName(), XElement.parseXML(f));
							}
						}
					}
				} else {
					throw new IOException("The URL does not point to a directory: " + u);
				}
			} catch (URISyntaxException ex) {
				throw new IOException(ex); 
			}
		} else
		if (u.getProtocol().equals("jar")) {
			JarURLConnection juc = (JarURLConnection)u.openConnection();
			juc.connect();
			JarFile jf =  juc.getJarFile();
			try {
				Enumeration<JarEntry> je = jf.entries();
				while (je.hasMoreElements()) {
					JarEntry e = je.nextElement();
					if (e.getName().indexOf('/') < 0 
							&& e.getName().indexOf('\\') < 0
							&& e.getName().toLowerCase().endsWith(".xsd")) {
						InputStream in = jf.getInputStream(e);
						try {
							result.put(e.getName(), XElement.parseXML(in));
						} finally {
							in.close();
						}
					}
				}
			} finally {
				jf.close();
			}
		} else {
			throw new IOException("Unsupported protocol: " + u);
		}
		return result;
	}
	/**
	 * Create an URL from the given JAR filename location.
	 * <p>Example: a file {@code c:\lib\mylib.jar} will be encoded as {@code jar:file:/c:/lib/mylib.jar}.</p>
	 * @param fileName the filename
	 * @return the URL of the JAR file which can be used in the load() method.
	 * @throws MalformedURLException on error
	 */
	public static URL jarFile(String fileName) throws MalformedURLException {
		return new URL("jar:file:" + fileName.replace("\\", "/") + "!/");
	}
	/**
	 * Returns a list of detected plugins.
	 * @return the list of the current plugins
	 */
	public List<AdvancePluginDetails> plugins() {
		synchronized (plugins) {
			return Lists.newArrayList(plugins);
		}
	}
	/**
	 * Test program.
	 * @param args no arguments
	 * @throws Exception ignored
	 */
	public static void main(String[] args) throws Exception {
		AdvancePluginManager m = new AdvancePluginManager("plugins");
		m.run();
		List<AdvancePluginDetails> plugins2 = m.plugins();
		for (AdvancePluginDetails p : plugins2) {
			System.out.println(p);
			AdvancePlugin plugin = p.open();
			System.out.printf("  Registry: %s%n", plugin.blockResolver().blocks().size());
			System.out.printf("  Schemas : %s%n", plugin.schemas().size());
		}
	}
}