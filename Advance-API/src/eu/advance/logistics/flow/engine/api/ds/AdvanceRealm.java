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
 * The status information about a realm.
 * @author akarnokd, 2011.09.19.
 */
public class AdvanceRealm extends AdvanceCreateModifyInfo 
implements XNSerializable, Copyable<AdvanceRealm>, Identifiable<String> {
	/** The realm status. */
	public AdvanceRealmStatus status;
	/** The name of the realm. */
	public String name;
	/**
	 * Lambda function to create a new object of this class.
	 */
	public static final Func0<AdvanceRealm> CREATOR = new Func0<AdvanceRealm>() {
		@Override
		public AdvanceRealm invoke() {
			return new AdvanceRealm();
		}
	};
	/** The function to select a new instance of this class. */
	public static final SQLResult<AdvanceRealm> SELECT = new SQLResult<AdvanceRealm>() {
		@Override
		public AdvanceRealm invoke(ResultSet rs) throws SQLException {
			AdvanceRealm realm = new AdvanceRealm();
			realm.name = rs.getString("name");
			realm.status = AdvanceRealmStatus.valueOf(rs.getString("status"));

			AdvanceCreateModifyInfo.load(rs, realm);
			return realm;
		}
	};
	@Override
	public void load(XNElement source) {
		status = AdvanceRealmStatus.valueOf(source.get("status"));
		name = source.get("name");
		super.load(source);
	}
	@Override
	public void save(XNElement destination) {
		destination.set("status", status);
		destination.set("name", name);
		super.save(destination);
	}
	@Override
	public AdvanceRealm copy() {
		AdvanceRealm result = new AdvanceRealm();
		result.status = status;
		result.name = name;
		
		assignTo(result);
		
		return result;
	}
	@Override
	public String id() {
		return name;
	}
}
