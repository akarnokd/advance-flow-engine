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

package eu.advance.logistics.flow.engine.block.db;

import hu.akarnokd.reactive4java.base.Action1;
import hu.akarnokd.reactive4java.base.Observer;
import hu.akarnokd.reactive4java.scheduler.SingleLaneExecutor;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import com.google.common.collect.Lists;

import eu.advance.logistics.annotations.Block;
import eu.advance.logistics.annotations.Input;
import eu.advance.logistics.annotations.Output;
import eu.advance.logistics.flow.engine.api.core.Pool;
import eu.advance.logistics.flow.engine.block.AdvanceBlock;
import eu.advance.logistics.flow.engine.comm.JDBCConnection;
import eu.advance.logistics.flow.engine.runtime.ConstantPort;
import eu.advance.logistics.flow.engine.xml.XElement;

/**
 * Queries and returns a batch of rows from the database and waits for a trigger
 * to continue.
 *
 * @author karnokd, 2012.02.22.
 */
@Block(id = "JDBCThrottledBatchQuery", category = "db", 
scheduler = "IO", 
description = "Retrieves and returns a batch of rows from the database and waits for a trigger to continue with the next set of rows.")
public class JDBCThrottledBatchQuery extends AdvanceBlock {

    /**
     * The initial trigger.
     */
    @Input("advance:boolean")
    protected static final String TRIGGER = "trigger";
    /**
     * The data source reference.
     */
    @Input("advance:string")
    protected static final String DATASOURCE = "datasource";
    /**
     * The SQL query.
     */
    @Input("advance:string")
    protected static final String QUERY = "query";
    /**
     * Trigger to request the next batch.
     */
    @Input("advance:boolean")
    protected static final String NEXT = "next";
    /**
     * The batch size.
     */
    @Input("advance:integer")
    protected static final String SIZE = "size";
    /**
     * The output map.
     */
    @Output("advance:collection<advance:map<advance:string,advance:object>>")
    protected static final String OUT = "out";
    /**
     * Indicator that the query has finished.
     */
    @Output("advance:boolean")
    protected static final String DONE = "done";
    /**
     * The data source.
     */
    protected final AtomicReference<String> datasource = new AtomicReference<String>();
    /**
     * The query.
     */
    protected final AtomicReference<String> query = new AtomicReference<String>();
    /**
     * The batch size.
     */
    protected final AtomicReference<Integer> size = new AtomicReference<Integer>();
    /**
     * The pool used.
     */
    protected Pool<JDBCConnection> pool;
    /**
     * The running connection.
     */
    protected JDBCConnection conn;
    /**
     * The statement.
     */
    protected PreparedStatement pstmt;
    /**
     * The resultset.
     */
    protected ResultSet rs;
    /**
     * The resultset metadata.
     */
    protected ResultSetMetaData rsm;
    /**
     * The single lane executor.
     */
    protected SingleLaneExecutor<Runnable> queue;
    /**
     * The batch size.
     */
    protected int batchSize;
	@Override
	public Observer<Void> run() {
		queue = SingleLaneExecutor.create(scheduler(), new Action1<Runnable>() {
			@Override
			public void invoke(Runnable value) {
				value.run();
			}
		});
		addCloseable(queue);
		
		observeInput(DATASOURCE, new Action1<XElement>() {
			@Override
			public void invoke(XElement value) {
				datasource.set(resolver().getString(value));
				JDBCThrottledBatchQuery.this.invoke();
			}
		});
		observeInput(QUERY, new Action1<XElement>() {
			@Override
			public void invoke(XElement value) {
				query.set(resolver().getString(value));
				JDBCThrottledBatchQuery.this.invoke();
			}
		});
		observeInput(SIZE, new Action1<XElement>() {
			@Override
			public void invoke(XElement value) {
				size.set(resolver().getInt(value));
				JDBCThrottledBatchQuery.this.invoke();
			}
		});
		observeInput(NEXT, new Action1<XElement>() {
			@Override
			public void invoke(XElement value) {
				scheduleNext();
			}
		});
		
		
		if (getInput(TRIGGER) instanceof ConstantPort<?, ?>) {
			final boolean runNow = getBoolean(TRIGGER);
			if (runNow) {
				return new RunObserver() {
					@Override
					public void next(Void value) {
						addCloseable(scheduler().schedule(new Runnable() {
							@Override
							public void run() {
								JDBCThrottledBatchQuery.this.invoke();
							}
						}));
					}
				};
			}
			return new RunObserver();
		}
    	observeInput(TRIGGER, new Action1<XElement>() {
    		@Override
    		public void invoke(XElement value) {
    			if (resolver().getBoolean(value)) {
    				JDBCThrottledBatchQuery.this.invoke();
    			}
    		}
    	});
    	return new RunObserver();
	}
	@Override
	public void done() {
		closeConnection();
	}
	/**
	 * Close the connection.
	 */
	synchronized void closeConnection() {
		if (rs != null) {
			try {
				rs.close();
			} catch (SQLException ex) {
				LOG.error(ex.toString(), ex);
			}
			rs = null;
		}
		if (pstmt != null) {
			try {
				pstmt.close();
			} catch (SQLException ex) {
				LOG.error(ex.toString(), ex);
			}
			pstmt = null;
		}
		if (conn != null && pool != null) {
			pool.put(conn);
			conn = null;
			pool = null;
		}
	}
	/**
	 * Initialize the data connection.
     *
	 * @return true if the connection was created
	 */
	synchronized boolean initConnection() {
		if (pool != null) {
			return false;
		}
		String ds = datasource.get();
		if (ds == null) {
			return false;
		}
		String q = query.get();
		if (q == null) {
			return false;
		}
		Integer s = size.get();
		if (s == null) {
			return false;
		}
		
		try {
			try {
				pool = settings.context.pools.get(JDBCConnection.class, ds);
				
				conn = pool.get();
			} catch (Exception ex) {
				log(ex);
				return false;
			}
			
			batchSize = s;
			
			pstmt = conn.getConnection().prepareStatement(q);
			
			rs = pstmt.executeQuery();
			
			rsm = rs.getMetaData();
			
		} catch (SQLException ex) {
			log(ex);
			closeConnection();
			return false;
		}
		
		return true;
	}
	
	@Override
	protected void invoke() {
		if (!initConnection()) {
			return;
		}
	}
	/**
	 * Schedule the next query.
	 */
	void scheduleNext() {
		queue.add(new Runnable() {
			@Override
			public void run() {
				queryNext();
			}
		});
	}
	/**
	 * Query the next batch of values.
	 */
	void queryNext() {
		synchronized (this) {
			if (pool == null) {
				return;
			}
		}
		int row = 0;
		List<XElement> result = Lists.newLinkedList();
		try {
			while (rs.next() && row < batchSize) {
				result.add(JDBCConverter.create(resolver(), rs, rsm));
				row++;
			}
		} catch (SQLException ex) {
			log(ex);
			dispatch(OUT, resolver().create(result));
			dispatch(DONE, resolver().create(true));
			closeConnection();
			return;
		}
		dispatch(OUT, resolver().create(result));
		if (row < batchSize) {
			dispatch(DONE, resolver().create(true));
			closeConnection();
		}
	}
}
