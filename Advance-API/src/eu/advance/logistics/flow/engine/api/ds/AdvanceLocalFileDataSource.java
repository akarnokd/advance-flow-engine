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

package eu.advance.logistics.flow.engine.api.ds;

import hu.akarnokd.reactive4java.base.Func0;
import hu.akarnokd.utils.database.SQLResult;
import hu.akarnokd.utils.xml.XNElement;
import hu.akarnokd.utils.xml.XNSerializable;

import java.sql.ResultSet;
import java.sql.SQLException;

import eu.advance.logistics.flow.engine.api.core.Copyable;
import eu.advance.logistics.flow.engine.api.core.Identifiable;

/**
 * A local file or directory data source.
 * @author akarnokd, 2011.09.20.
 */
public class AdvanceLocalFileDataSource extends AdvanceCreateModifyInfo 
implements XNSerializable, Copyable<AdvanceLocalFileDataSource>, Identifiable<String> {
	/** The name of the data source as used by blocks. */
	public String name;
	/** The directory where the file source(s) are located. */
	public String directory;
	/** The function to create a new instance of this class. */
	public static final Func0<AdvanceLocalFileDataSource> CREATOR = new Func0<AdvanceLocalFileDataSource>() {
		@Override
		public AdvanceLocalFileDataSource invoke() {
			return new AdvanceLocalFileDataSource();
		}
	};
	/** The function to select a new instance of this class. */
	public static final SQLResult<AdvanceLocalFileDataSource> SELECT = new SQLResult<AdvanceLocalFileDataSource>() {
		@Override
		public AdvanceLocalFileDataSource invoke(ResultSet rs) throws SQLException {
			AdvanceLocalFileDataSource lfds = new AdvanceLocalFileDataSource();
			lfds.name = rs.getString("name");
			lfds.directory = rs.getString("directory");

			AdvanceCreateModifyInfo.load(rs, lfds);      
			return lfds;
		}
	};
	@Override
	public void load(XNElement source) {
		name = source.get("name");
		directory = source.get("directory");
		super.load(source);
	}
	@Override
	public void save(XNElement destination) {
		destination.set("name", name);
		destination.set("directory", directory);
		super.save(destination);
	}
	@Override
	public AdvanceLocalFileDataSource copy() {
		AdvanceLocalFileDataSource result = new AdvanceLocalFileDataSource();
		
		result.name = name;
		result.directory = directory;
		
		assignTo(result);
		
		return result;
	}
	@Override
	public String id() {
		return name;
	}
}
