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

import hu.akarnokd.reactive4java.base.Func0;

import java.text.ParseException;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.advance.logistics.flow.engine.api.core.Copyable;
import eu.advance.logistics.flow.engine.xml.typesystem.XElement;
import eu.advance.logistics.flow.engine.xml.typesystem.XSerializable;

/**
 * Record class representing a file or directory on a local or remote (s)FTP(s) site.
 * @author akarnokd, 2011.10.05.
 */
public class FileInfo implements XSerializable, Copyable<FileInfo> {
	/** The logger. */
	protected static final Logger LOG = LoggerFactory.getLogger(FileInfo.class);
	/** Is it a directory? */
	public boolean isDirectory;
	/** The name. */
	public String name;
	/** The file length. */
	public long length;
	/** The related time (e.g., last modify). */
	public Date time;
	/** Creates a new instance of this class. */
	public static final Func0<FileInfo> CREATOR = new Func0<FileInfo>() {
		@Override
		public FileInfo invoke() {
			return new FileInfo();
		}
	};
	@Override
	public void load(XElement source) {
		isDirectory = source.getBoolean("dir");
		name = source.get("name");
		length = source.getLong("length");
		try {
			time = XElement.parseDateTime(source.get("time"));
		} catch (ParseException ex) {
			LOG.error(ex.toString(), ex);
		}
	}
	@Override
	public void save(XElement destination) {
		destination.set("dir", isDirectory, "name", name, "length", length, "time", time);
	}
	@Override
	public FileInfo copy() {
		FileInfo result = new FileInfo();
		
		result.isDirectory = isDirectory;
		result.name = name;
		result.length = length;
		result.time = new Date(time.getTime());
		
		return result;
	}
}
