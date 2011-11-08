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

import eu.advance.logistics.flow.engine.api.ds.AdvanceLocalFileDataSource;

/**
 * A local "connection" manager to allow working with a connection pooling mechanisms.
 * @author akarnokd, 2011.10.06.
 */
public class LocalPoolManager implements PoolManager<LocalConnection> {
	/** The local file data source settings. */
	protected final AdvanceLocalFileDataSource ds;
	/**
	 * Initialize the pool manager with the supplied data source settings.
	 * @param ds the local file data source
	 */
	public LocalPoolManager(AdvanceLocalFileDataSource ds) {
		this.ds = ds.copy();
	}
	@Override
	public LocalConnection create() throws Exception {
		return new LocalConnection(ds.directory);
	}

	@Override
	public boolean verify(LocalConnection obj) throws Exception {
		return obj.baseDir.canRead();
	}

	@Override
	public void close(LocalConnection obj) throws Exception {
		// no operation needed
	}

}
