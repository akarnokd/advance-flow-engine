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
import eu.advance.logistics.flow.engine.api.core.Copyable;
import eu.advance.logistics.flow.engine.api.core.Identifiable;
import eu.advance.logistics.flow.engine.xml.typesystem.XElement;
import eu.advance.logistics.flow.engine.xml.typesystem.XSerializable;


/**
 * The status information about a realm.
 * @author akarnokd, 2011.09.19.
 */
public class AdvanceRealm extends AdvanceCreateModifyInfo 
implements XSerializable, Copyable<AdvanceRealm>, Identifiable<String> {
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
	@Override
	public void load(XElement source) {
		status = AdvanceRealmStatus.valueOf(source.get("status"));
		name = source.get("name");
		super.load(source);
	}
	@Override
	public void save(XElement destination) {
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
