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

package eu.advance.logistics.flow.engine;

import hu.akarnokd.reactive4java.base.Func2;
import hu.akarnokd.reactive4java.base.Option;
import hu.akarnokd.utils.pool.BoundedPool;
import hu.akarnokd.utils.pool.Pool;
import hu.akarnokd.utils.pool.UnlimitedPool;

import java.io.File;

import eu.advance.logistics.flow.engine.api.ds.AdvanceDataStore;
import eu.advance.logistics.flow.engine.api.ds.AdvanceEmailBox;
import eu.advance.logistics.flow.engine.api.ds.AdvanceFTPDataSource;
import eu.advance.logistics.flow.engine.api.ds.AdvanceJDBCDataSource;
import eu.advance.logistics.flow.engine.api.ds.AdvanceJMSEndpoint;
import eu.advance.logistics.flow.engine.api.ds.AdvanceLocalFileDataSource;
import eu.advance.logistics.flow.engine.api.ds.AdvanceSOAPEndpoint;
import eu.advance.logistics.flow.engine.api.ds.AdvanceWebDataSource;
import eu.advance.logistics.flow.engine.comm.EmailConnection;
import eu.advance.logistics.flow.engine.comm.EmailPoolManager;
import eu.advance.logistics.flow.engine.comm.FTPConnection;
import eu.advance.logistics.flow.engine.comm.FTPPoolManager;
import eu.advance.logistics.flow.engine.comm.JDBCConnection;
import eu.advance.logistics.flow.engine.comm.JDBCPoolManager;
import eu.advance.logistics.flow.engine.comm.JMSConnection;
import eu.advance.logistics.flow.engine.comm.JMSPoolManager;
import eu.advance.logistics.flow.engine.comm.LocalConnection;
import eu.advance.logistics.flow.engine.comm.LocalPoolManager;
import eu.advance.logistics.flow.engine.comm.SOAPConnection;
import eu.advance.logistics.flow.engine.comm.SOAPPoolManager;
import eu.advance.logistics.flow.engine.comm.WebConnection;
import eu.advance.logistics.flow.engine.comm.WebPoolManager;

/**
 * The implementation of the pool creator function for the AdvancePools class.
 * @author akarnokd, 2011.11.09.
 */
public class AdvancePoolCreator implements
		Func2<Class<?>, String, Option<? extends Pool<?>>> {
	/**
	 * The datastore where from poll the pool configurations.
	 */
	protected final AdvanceDataStore datastore;
	/**
	 * Constructor.
	 * @param datastore the datastore to use for pool configurations
	 */
	public AdvancePoolCreator(AdvanceDataStore datastore) {
		this.datastore = datastore;
	}
	@Override
	public Option<? extends Pool<?>> invoke(Class<?> param1, String param2) {
		try {
			if (JDBCConnection.class.equals(param1)
					|| java.sql.Connection.class.equals(param1) 
					|| AdvanceJDBCDataSource.class.equals(param1)) {
				AdvanceJDBCDataSource ds = datastore.queryJDBCDataSource(param2);
				if (ds != null) {
					return Option.some(new BoundedPool<JDBCConnection>(
							ds.poolSize, new JDBCPoolManager(ds)));
				}
			} else
			if (FTPConnection.class.equals(param1) || AdvanceFTPDataSource.class.equals(param1)) {
				AdvanceFTPDataSource ds = datastore.queryFTPDataSource(param2);
				if (ds != null) {
					return Option.some(new UnlimitedPool<FTPConnection>(new FTPPoolManager(ds, datastore)));
				}
			} else
			if (JMSConnection.class.equals(param1) || AdvanceJMSEndpoint.class.equals(param1)) {
				AdvanceJMSEndpoint ds = datastore.queryJMSEndpoint(param2);
				if (ds != null) {
					return Option.some(new BoundedPool<JMSConnection>(ds.poolSize, new JMSPoolManager(ds)));
				}
			} else
			if (LocalConnection.class.equals(param1)
					|| File.class.equals(param1)
					|| AdvanceLocalFileDataSource.class.equals(param1)) {
				AdvanceLocalFileDataSource ds = datastore.queryLocalFileDataSource(param2);
				if (ds != null) {
					return Option.some(new UnlimitedPool<LocalConnection>(new LocalPoolManager(ds)));
				}
			} else
			if (SOAPConnection.class.equals(param1) || AdvanceSOAPEndpoint.class.equals(param1)) {
				AdvanceSOAPEndpoint ds = datastore.querySOAPEndpoint(param2);
				if (ds != null) {
					return Option.some(new UnlimitedPool<SOAPConnection>(new SOAPPoolManager(ds, datastore)));
				}
			} else
			if (WebConnection.class.equals(param1) || AdvanceWebDataSource.class.equals(param1)) {
				AdvanceWebDataSource ds = datastore.queryWebDataSource(param2);
				if (ds != null) {
					return Option.some(new UnlimitedPool<WebConnection>(new WebPoolManager(ds, datastore)));
				}
			} else
			if (EmailConnection.class.equals(param1) || AdvanceEmailBox.class.equals(param1)) {
				AdvanceEmailBox ds = datastore.queryEmailBox(param2);
				if (ds != null) {
					return Option.some(new UnlimitedPool<EmailConnection>(new EmailPoolManager(ds, datastore)));
				}
			}
			
		} catch (Throwable t) {
			return Option.error(t);
		}
		return null;
	}
}
