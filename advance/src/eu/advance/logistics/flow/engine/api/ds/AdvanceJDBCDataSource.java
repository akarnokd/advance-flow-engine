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

package eu.advance.logistics.flow.engine.api.ds;

import hu.akarnokd.reactive4java.base.Func0;
import eu.advance.logistics.flow.engine.api.core.Copyable;
import eu.advance.logistics.flow.engine.api.core.HasPassword;
import eu.advance.logistics.flow.engine.api.core.Identifiable;
import eu.advance.logistics.flow.engine.xml.typesystem.XElement;
import eu.advance.logistics.flow.engine.xml.typesystem.XSerializable;

/**
 * Description of JDBC data store records.
 * @author akarnokd, 2011.09.20.
 */
public class AdvanceJDBCDataSource extends AdvanceCreateModifyInfo 
implements XSerializable, HasPassword, Copyable<AdvanceJDBCDataSource>, Identifiable<String> {
	/** The name used by blocks to reference this data source. */
	public String name;
	/** The JDBC driver. */
	public String driver;
	/** The connection url. */
	public String url;
	/** The user who connects. */
	public String user;
	/** 
	 * The password for connection.
	 * <p>Note that passwords are never returned from the 
	 * control API calls and are always {@code null}.</p> 
	 * <p>An empty password should be an empty {@code char} array. To keep
	 * the current password, use {@code null}.</p>
	 */
	private char[] password;
	/** The default schema. */
	public String schema;
	/** The connection pool size. */
	public int poolSize = 5;
	/** The function to create a new instance of this class. */
	public static final Func0<AdvanceJDBCDataSource> CREATOR = new Func0<AdvanceJDBCDataSource>() {
		@Override
		public AdvanceJDBCDataSource invoke() {
			return new AdvanceJDBCDataSource();
		}
	};
	@Override
	public void load(XElement source) {
		name = source.get("name");
		driver = source.get("driver");
		url = source.get("url");
		user = source.get("user");
		password = getPassword(source, "password");
		poolSize = source.getInt("poolsize");
		schema = source.get("schema");
		super.load(source);
	}
	@Override
	public void save(XElement destination) {
		destination.set("name", name);
		destination.set("driver", driver);
		destination.set("url", url);
		destination.set("user", user);
		setPassword(destination, "password", password);
		destination.set("poolsize", poolSize);
		destination.set("schema", schema);
		super.save(destination);
	}
	@Override
	public AdvanceJDBCDataSource copy() {
		AdvanceJDBCDataSource result = new AdvanceJDBCDataSource();
		
		result.name = name;
		result.driver = driver;
		result.url = url;
		result.user = user;
		result.poolSize = poolSize;
		result.schema = schema;
		result.password = password != null ? password.clone() : null;
		
		assignTo(result);
		
		return result;
	}
	@Override
	public char[] password() {
		return password != null ? password.clone() : null;
	}
	@Override
	public void password(char[] newPassword) {
		password = newPassword != null ? newPassword.clone() : null;
	}
	@Override
	public String id() {
		return name;
	}
}
