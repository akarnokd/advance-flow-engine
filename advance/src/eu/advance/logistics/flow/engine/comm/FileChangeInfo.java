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

import eu.advance.logistics.flow.engine.api.Copyable;
import eu.advance.logistics.flow.engine.xml.typesystem.XElement;
import eu.advance.logistics.flow.engine.xml.typesystem.XSerializable;

/**
 * Record to store a FileInfo and a change reason.
 * <p>In case of NEW or MODIFIED type the {@code file} contains the current file status.
 * In case of a DELETED type, the {@code file} contains the last known file status.</p>
 * @author akarnokd, 2011.10.06.
 */
public class FileChangeInfo implements XSerializable, Copyable<FileChangeInfo> {
	/** The change type. */
	public FileChangeType type;
	/** The file info. */
	public FileInfo file;
	@Override
	public FileChangeInfo copy() {
		FileChangeInfo fci = new FileChangeInfo();
		fci.type = type;
		fci.file = file.copy();
		return fci;
	}

	@Override
	public void load(XElement source) {
		type = FileChangeType.valueOf(source.get("change-type"));
		file = new FileInfo();
		file.load(source);
	}

	@Override
	public void save(XElement destination) {
		destination.set("change-type", type);
		file.save(destination);
	}

}
