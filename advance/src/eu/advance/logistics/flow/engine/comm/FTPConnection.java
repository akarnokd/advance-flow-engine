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

import java.io.Closeable;
import java.io.IOException;

import eu.advance.logistics.flow.engine.api.AdvanceFTPProtocols;

/**
 * Represents an FTP(s) or an SFTP connection with standard file and directory functions.
 * @author akarnokd, 2011.10.05.
 */
public interface FTPConnection extends Closeable, FileAccess {
	/** @return the protocol used by this connection. */
	AdvanceFTPProtocols protocol();
	/** 
	 * @return the current directory.
	 * @throws IOException if a network eror occurs 
	 */
	String currentDir() throws IOException;
	/**
	 * Change the directory.
	 * @param newDir the new directory
	 * @throws IOException if a network error occurs 
	 */
	void changeDir(String newDir) throws IOException;
}
