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

package eu.advance.logistics.flow.engine.api;

import hu.akarnokd.reactive4java.base.Func0;
import eu.advance.logistics.flow.engine.model.XSerializable;
import eu.advance.logistics.flow.engine.xml.typesystem.XElement;

/**
 * Definition of a Java Messaging Service endpoint.
 * @author karnokd, 2011.09.20.
 */
public class AdvanceJMSEndpoint extends AdvanceCreateModifyInfo 
implements XSerializable, HasPassword, Copyable<AdvanceJMSEndpoint> {
	/** The name used by blocks to reference this endpoint. */
	public String name;
	/** The JMS driver. */
	public String driver;
	/** The connection URL. */
	public String url;
	/** The user name. */
	public String user;
	/**
	 * The password. 
	 * <p>Note that passwords are never returned from the 
	 * control API calls and are always {@code null}.</p> 
	 * <p>An empty password should be an empty {@code char} array. To keep
	 * the current password, use {@code null}.</p>
	 */
	private char[] password;
	/** The queue manager name. */
	public String queueManager;
	/** The queue name. */
	public String queue;
	/** The communication pool size. */
	public int poolSize;
	/** The function to create a new instance of this class. */
	public static final Func0<AdvanceJMSEndpoint> CREATOR = new Func0<AdvanceJMSEndpoint>() {
		@Override
		public AdvanceJMSEndpoint invoke() {
			return new AdvanceJMSEndpoint();
		}
	};
	@Override
	public void load(XElement source) {
		name = source.get("name");
		driver = source.get("driver");
		url = source.get("url");
		user = source.get("user");
		password = getPassword(source, "password");
		
		queueManager = source.get("queue-manager");
		queue = source.get("queue");
		poolSize = source.getInt("poolsize");
		
		super.load(source);
	}
	@Override
	public void save(XElement destination) {
		destination.set("name", name);
		destination.set("driver", driver);
		destination.set("url", url);
		destination.set("user", user);
		setPassword(destination, "password", password);
		destination.set("queue-manager", queueManager);
		destination.set("queue", queue);
		destination.set("poolsize", poolSize);
		
		super.save(destination);
	}
	@Override
	public AdvanceJMSEndpoint copy() {
		AdvanceJMSEndpoint result = new AdvanceJMSEndpoint();
		
		result.name = name;
		result.driver = driver;
		result.url = url;
		result.user = user;
		result.queueManager = queueManager;
		result.queue = queue;
		result.poolSize = poolSize;
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
}
