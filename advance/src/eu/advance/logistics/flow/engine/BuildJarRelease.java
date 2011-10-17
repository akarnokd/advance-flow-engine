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

package eu.advance.logistics.flow.engine;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import com.google.common.collect.Sets;


/**
 * Creates a JAR file of the engine and a JAR file containing all dependencies.
 * @author akarnokd, 2011.09.28.
 */
public final class BuildJarRelease {
	/**
	 * Main program, no arguments.
	 * @param args no arguments.
	 */
	public static void main(String[] args) {
		try {
			ZipOutputStream zout = new ZipOutputStream(
					new BufferedOutputStream(
							new FileOutputStream("advance-flow-engine-" + AdvanceFlowEngine.VERSION + ".jar"), 1024 * 1024));
			try {
				zout.setLevel(9);
				addMainContent(zout);
				addFile("LICENSE.txt", "LICENSE.txt", zout);
			} finally {
				zout.close();
			}
			zout = new ZipOutputStream(
					new BufferedOutputStream(
							new FileOutputStream("advance-flow-engine-control-center-" + AdvanceFlowEngine.VERSION + ".jar"), 1024 * 1024));
			try {
				zout.setLevel(9);
				addMainContent(zout);
				addFile("META-INF/MANIFEST.MF", "META-INF/MANIFEST.MF.control-center", zout);
				addFile("LICENSE.txt", "LICENSE.txt", zout);
			} finally {
				zout.close();
			}
			zout = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream("advance-flow-engine-" + AdvanceFlowEngine.VERSION + ".zip"), 1024 * 1024));
			try {
				zout.setLevel(9);
				processDirectory(".\\", ".\\conf", zout, new FilenameFilter() {
					@Override
					public boolean accept(File dir, String name) {
						return !name.equals("advance-flow-engine-control-center-config.xml");
					}
				});
				processDirectory(".\\", ".\\schemas", zout, new FilenameFilter() {
					@Override
					public boolean accept(File dir, String name) {
						return name.toLowerCase().endsWith(".xml") || name.toLowerCase().endsWith(".xsd");
					}
				});
				addFile("LICENSE.txt", "LICENSE.txt", zout);
				addFile("README.txt", "README.txt", zout);
				addFile("advance-flow-engine-" + AdvanceFlowEngine.VERSION + ".jar", "advance-flow-engine-" + AdvanceFlowEngine.VERSION + ".jar", zout);
				
			} finally {
				zout.close();
			}
			zout = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream("advance-flow-engine-full-" + AdvanceFlowEngine.VERSION + ".zip"), 1024 * 1024));
			try {
				zout.setLevel(9);
				processDirectory(".\\", ".\\conf", zout, new FilenameFilter() {
					@Override
					public boolean accept(File dir, String name) {
						return !name.equals("advance-flow-engine-control-center-config.xml");
					}
				});
				processDirectory(".\\", ".\\schemas", zout, new FilenameFilter() {
					@Override
					public boolean accept(File dir, String name) {
						return name.toLowerCase().endsWith(".xml") || name.toLowerCase().endsWith(".xsd");
					}
				});
				addFile("LICENSE.txt", "LICENSE.txt", zout);
				addFile("README.txt", "README.txt", zout);
				addFile("advance-flow-engine-" + AdvanceFlowEngine.VERSION + ".jar", "advance-flow-engine-" + AdvanceFlowEngine.VERSION + ".jar", zout);
				addFile("advance-flow-engine-control-center-" + AdvanceFlowEngine.VERSION + ".jar", "advance-flow-engine-control-center-" + AdvanceFlowEngine.VERSION + ".jar", zout);
				addLibs(zout);
				
			} finally {
				zout.close();
			}
			zout = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream("advance-flow-engine-javadoc-" + AdvanceFlowEngine.VERSION + ".zip"), 1024 * 1024));
			try {
				zout.setLevel(9);
				processDirectory(".\\test\\javadoc\\", ".\\test\\javadoc", zout, null);
				
			} finally {
				zout.close();
			}
			
			zout = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream("advance-flow-engine-control-center-" + AdvanceFlowEngine.VERSION + ".zip"), 1024 * 1024));
			try {
				zout.setLevel(9);
				processDirectory(".\\", ".\\conf", zout, new FilenameFilter() {
					@Override
					public boolean accept(File dir, String name) {
						return !name.equals("advance-flow-engine-control-center-config.xml");
					}
				});
				processDirectory(".\\", ".\\schemas", zout, new FilenameFilter() {
					@Override
					public boolean accept(File dir, String name) {
						return name.toLowerCase().endsWith(".xml") || name.toLowerCase().endsWith(".xsd");
					}
				});
				addFile("advance-flow-engine-control-center-" + AdvanceFlowEngine.VERSION + ".jar", "advance-flow-engine-control-center-" + AdvanceFlowEngine.VERSION + ".jar", zout);
				addFile("LICENSE.txt", "LICENSE.txt", zout);
				addFile("README.txt", "README.txt", zout);

				addLibs(zout);

			} finally {
				zout.close();
			}

		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}
	/**
	 * Add dependent libraries as files.
	 * @param zout the output stream
	 * @throws IOException on error
	 */
	static void addLibs(ZipOutputStream zout) throws IOException {
		Set<String> except = Sets.newHashSet(
				"reactive4java-gwt-0.94.jar", 
				"mysql-connector-java-5.1.16-bin.jar",
				"monetdb-1.17-jdbc.jar"
		);
		File[] files = new File("lib").listFiles();
		if (files != null) {
			for (File f : files) {
				if (f.getName().endsWith(".jar") && !except.contains(f.getName())) {
					addFile("lib/" + f.getName(), "lib/" + f.getName(), zout);
				}
			}
		}
	}
	/**
	 * Add dependent JAR contents to the output stream.
	 * @param zout the output
	 * @throws IOException on error
	 */
	static void addDependencies(ZipOutputStream zout) throws IOException {
		Set<String> except = Sets.newHashSet(
				"reactive4java-gwt-0.94", 
				"gwt-servlet",
				"mysql-connector-java-5.1.16-bin",
				"guava-r09-gwt",
				"monetdb-1.17-jdbc"
		);
		File[] files = new File("lib").listFiles();
		Set<String> memory = Sets.newHashSet();
		if (files != null) {
			for (File f : files) {
				if (f.getName().endsWith(".jar") && !except.contains(f.getName())) {
					copyZip(f.getAbsolutePath(), zout, memory);
				}
			}
		}
	}
	/**
	 * Copy the contents of the zip file into the output stream.
	 * @param fileName the target filename
	 * @param zout the output stream
	 * @param memory memory of seen directories
	 * @throws IOException on error
	 */
	static void copyZip(String fileName, ZipOutputStream zout, Set<String> memory) throws IOException {
		File f = new File(fileName);
		ZipInputStream zin = new ZipInputStream(new BufferedInputStream(new FileInputStream(f), 1024 * 1024));
		try {
			while (true) {
				ZipEntry ze = zin.getNextEntry();
				if (ze == null) {
					break;
				}
				if (memory.add(ze.getName())) {
					String name = ze.getName();
					if (ze.getName().startsWith("META-INF")) {
						name = "META-INF/" + f.getName() + name.substring(8);
					}
					ZipEntry ze2 = new ZipEntry(name);
					System.out.printf("Adding %s as %s%n", ze.getName(), name);
					ze2.setTime(ze.getTime());
					zout.putNextEntry(ze2);
					if (!ze.isDirectory()) {
						byte[] buffer = new byte[8192];
						while (true) {
							int read = zin.read(buffer);
							if (read < 0) {
								break;
							} else
							if (read > 0) {
								zout.write(buffer, 0, read);
							}
						}
					}
				}
			}
		} finally {
			zin.close();
		}
	}
	/**
	 * Add the main content to the JAR file.
	 * @param zout the zip output stream
	 * @throws IOException on error
	 */
	private static void addMainContent(ZipOutputStream zout) throws IOException {
		processDirectory(".\\bin\\", ".\\bin", zout, new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				String path = dir.getAbsolutePath().replace("\\", "/");
				return path.contains("eu/advance/logistics/flow/engine") 
						&& !name.contains("BuildJarRelease")
						&& !path.contains("eu/advance/logistics/flow/engine/test")
						;
			}
		});
		processDirectory(".\\src\\", ".\\src", zout, new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				String path = dir.getAbsolutePath().replace("\\", "/");
				return name.endsWith(".java") 
						&& path.contains("eu/advance/logistics/flow/engine") 
						&& !name.contains("BuildJarRelease")
						&& !path.contains("eu/advance/logistics/flow/engine/test")
						;
			}
		});
	}
	
	/** Utility class. */
	private BuildJarRelease() {
	}
	/**
	 * Loads an entire file from the filesystem.
	 * @param f the file to load
	 * @return the bytes of file or an empty array
	 */
	public static byte[] load(File f) {
		if (f.canRead()) {
			byte[] buffer = new byte[(int)f.length()];
			try {
				RandomAccessFile fin = new RandomAccessFile(f, "r");
				try {
					fin.readFully(buffer);
					return buffer;
				} finally {
					fin.close();
				}
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		} else {
			System.err.println("File inaccessible: " + f);
		}
		return new byte[0];
	}
	/**
	 * Add the given fileName to the zip stream with the given entry name.
	 * @param entryName the entry name
	 * @param fileName the file name and path
	 * @param zout the output stream
	 * @throws IOException on error
	 */
	static void addFile(String entryName, String fileName, ZipOutputStream zout)
	throws IOException {
		ZipEntry mf = new ZipEntry(entryName);
		File mfm = new File(fileName);
		mf.setSize(mfm.length());
		mf.setTime(mfm.lastModified());
		zout.putNextEntry(mf);
		zout.write(load(mfm));
	}
	/**
	 * Process the contents of the given directory.
	 * @param baseDir the base directory
	 * @param currentDir the current directory
	 * @param zout the output stream
	 * @param filter the optional file filter
	 * @throws IOException on error
	 */
	static void processDirectory(String baseDir, String currentDir, ZipOutputStream zout,
			FilenameFilter filter) throws IOException {
		File[] files = new File(currentDir).listFiles(new FileFilter() {
			@Override
			public boolean accept(File pathname) {
				return !pathname.isHidden();
			}
		});
		if (files != null) {
			for (File f : files) {
				if (f.isDirectory()) {
					processDirectory(baseDir, f.getPath(), zout, filter);
				} else {
					String fpath = f.getPath();
					String fpath2 = fpath.substring(baseDir.length());
					
					if (filter == null || filter.accept(f.getParentFile(), f.getName())) {
						System.out.printf("Adding %s as %s%n", fpath, fpath2);
						ZipEntry ze = new ZipEntry(fpath2.replace('\\', '/'));
						ze.setSize(f.length());
						ze.setTime(f.lastModified());
						zout.putNextEntry(ze);
						
						zout.write(load(f));
					}
				}
			}
		}
	}
}
