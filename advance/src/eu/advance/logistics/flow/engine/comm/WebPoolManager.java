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

package eu.advance.logistics.flow.engine.comm;

import hu.akarnokd.utils.pool.PoolManager;
import eu.advance.logistics.flow.engine.api.ds.AdvanceDataStore;
import eu.advance.logistics.flow.engine.api.ds.AdvanceWebDataSource;

/**
 * A Web connection manager for pooling.
 * @author akarnokd, 2011.10.06.
 */
public class WebPoolManager implements PoolManager<WebConnection> {
	/** The channel configuration. */
	protected final AdvanceWebDataSource channel;
	/** The datastore for accessing the keystores. */
	protected final AdvanceDataStore datastore;
	/**
	 * Constructs the pool manager with the given configuration.
	 * @param channel the endpoint settings
	 * @param datastore the datastore for accessing the keystores
	 */
	public WebPoolManager(AdvanceWebDataSource channel, AdvanceDataStore datastore) {
		this.channel = channel;
		this.datastore = datastore;
	}
	@Override
	public WebConnection create() throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean verify(WebConnection obj) throws Exception {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void close(WebConnection obj) throws Exception {
		// TODO Auto-generated method stub
		
	}

}
