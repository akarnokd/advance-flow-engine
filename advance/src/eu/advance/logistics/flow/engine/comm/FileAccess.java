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

package eu.advance.logistics.flow.engine.comm;

import java.io.IOException;
import java.util.List;

/**
 * The functions to access files relative to a base directory.
 * @author karnokd, 2011.10.06.
 */
public interface FileAccess {
	/**
	 * Retrieve the contents of a file.
	 * @param file the file
	 * @return the byte array of the file contents
	 * @throws IOException on error
	 */
	byte[] retrieve(String file) throws IOException;
	/**
	 * Send a new file.
	 * @param file the file name
	 * @param data the file contents
	 * @throws IOException if a network error occurs 
	 */
	void send(String file, byte[] data) throws IOException;
	/**
	 * List the contents of a remote directory.
	 * @return the list of files and directories.
	 * @throws IOException if a network error occurs
	 */
	List<FileInfo> list() throws IOException;
}
