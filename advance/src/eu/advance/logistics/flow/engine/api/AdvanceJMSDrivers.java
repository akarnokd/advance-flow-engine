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

import edu.umd.cs.findbugs.annotations.Nullable;

/**
 * Contains some predefined constants for JMS drivers.
 * @author karnokd, 2011.09.20.
 */
public enum AdvanceJMSDrivers {
	/** Generic driver. */
	GENERIC(null, null),
	/** Open JMS driver. */
	OPEN_JMS("org.exolab.jms.jndi.InitialContextFactory", "tcp://{host}:{port 3035}/"),
	/** Websphere MQ driver. */
	WEBSPHERE_MQ("com.ibm.mq.jms.context.WMQInitialContextFactory", "{host}:{port 1414}/SYSTEM.DEF.SVRCONN"),
	/** Apache MQ driver. */
	ACTIVE_MQ("org.apache.activemq.jndi.ActiveMQInitialContextFactory", "tcp://{host}:{port 61616}")
	;
	/** The driver class. */
	@Nullable
	public final String driverClass;
	/** The URL prefix. */
	@Nullable
	public final String urlTemplate;
	/**
	 * Constructor.
	 * @param driverClass the driver class
	 * @param urlTemplate the URL template
	 */
	AdvanceJMSDrivers(@Nullable String driverClass, @Nullable String urlTemplate) {
		this.driverClass = driverClass;
		this.urlTemplate = urlTemplate;
	}
}
