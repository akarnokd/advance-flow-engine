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

package eu.advance.logistics.flow.engine.comm;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;

/**
 * Class representing the ability to load, save and list files from local directory.
 * @author akarnokd, 2011.10.06.
 */
public class LocalConnection implements FileAccess {
	/** The logger. */
	protected static final Logger LOG = LoggerFactory.getLogger(LocalConnection.class);
	/** The base directory. */
	protected final File baseDir;
	/**
	 * Constructor. Initializes the base directory.
	 * @param baseDir the base directory, all methods will operate relative to this directory
	 */
	public LocalConnection(String baseDir) {
		this(new File(baseDir));
	}
	/**
	 * Constructor. Initializes the base directory.
	 * @param baseDir the base directory, all methods will operate relative to this directory
	 */
	public LocalConnection(File baseDir) {
		this.baseDir = baseDir;
	}
	/**
	 * Retrieve a file relative to the connection's base directory.
	 * @param fileName the file name
	 * @return the file content as byte array
	 * @throws IOException if the file cannot be found or accessed
	 */
	@Override
	public byte[] retrieve(String fileName) throws IOException {
		File f = new File(baseDir, fileName);
		byte[] result = new byte[(int)f.length()];
		try {
			FileInputStream fin = new FileInputStream(f);
			try {
				fin.read(result);
				return result;
			} finally {
				fin.close();
			}
		} catch (IOException ex) {
			LOG.error("Problem with file " + f, ex);
			throw ex;
		}
	}
	/**
	 * Store a file in the given filename relative to the base directory.
	 * @param fileName the target filename
	 * @param data the data to save
	 * @throws IOException if the file can't be created or modified
	 */
	@Override
	public void send(String fileName, byte[] data) throws IOException {
		File f = new File(baseDir, fileName);
		try {
			FileOutputStream fout = new FileOutputStream(f);
			try {
				fout.write(data);
			} finally {
				fout.close();
			}
		} catch (IOException ex) {
			LOG.error("Problem with file " + f, ex);
			throw ex;
		}
	}
	/**
	 * List the contents of the base directory.
	 * @return the list of files in the base directory
	 */
	@Override
	public List<FileInfo> list() {
		List<FileInfo> result = Lists.newArrayList();
		File[] files = baseDir.listFiles();
		if (files != null) {
			for (File f : files) {
				if (f.isDirectory() || f.isFile()) {
					FileInfo fi = new FileInfo();
					
					fi.name = f.getName();
					fi.isDirectory = f.isDirectory();
					fi.length = f.length();
					fi.time = new Date(f.lastModified());
					
					result.add(fi);
				}
			}
		}
		return result;
	}
	@Override
	public void rename(String file, String newName) throws IOException {
		if (!new File(file).renameTo(new File(newName))) {
			throw new IOException("Rename failed: " + file + " -> " + newName);
		}
	}
}
