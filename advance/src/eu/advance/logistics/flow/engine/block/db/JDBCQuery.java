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



import eu.advance.logistics.annotations.Block;
import eu.advance.logistics.annotations.Input;
import eu.advance.logistics.annotations.Output;
import eu.advance.logistics.flow.engine.api.core.Pool;
import eu.advance.logistics.flow.engine.block.AdvanceBlock;
import eu.advance.logistics.flow.engine.comm.JDBCConnection;
import eu.advance.logistics.flow.engine.xml.XElement;
import hu.akarnokd.reactive4java.reactive.Observer;
import hu.akarnokd.reactive4java.reactive.Reactive;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;
import java.util.logging.Logger;

/**
 * Issues an SQL query into the datasource once a trigger object arrives and
 * returns the rows converted into a mapping from column name to column value.
 * Signature: JDBCQuery(trigger, datasource, string, schema<t>) ->map<string,
 * object>
 *
 * @author TTS
 */
@Block(id = "JDBCQuery", category = "db", scheduler = "IO", description = "Issues an SQL query into the datasource once a trigger object arrives and returns the rows converted into a mapping from column name to column value.")
public class JDBCQuery extends AdvanceBlock {

	/**
	 * The logger.
	 */
	protected static final Logger LOGGER = Logger.getLogger(JDBCQuery.class.getName());
	/**
	 * In.
	 */
	@Input("advance:boolean")
	protected static final String TRIGGER = "trigger";
	/**
	 * In.
	 */
	@Input("advance:string")
	protected static final String DATASOURCE = "datasource";
	/**
	 * In.
	 */
	@Input("advance:string")
	protected static final String QUERY = "query";
	/**
	 * Out.
	 */
	@Output("advance:map<advance:string,advance:object>")
	protected static final String OUT = "out";

	@Override
	protected void invoke() {
		// called on trigger
	}

	@Override
	public Observer<Void> run() {
		addCloseable(Reactive.observeOn(getInput(TRIGGER), scheduler()).register(new Observer<XElement>() {

			@Override
			public void next(XElement value) {
				if (resolver().getBoolean(value)) {
					execute();
				}
			}

			@Override
			public void error(Throwable ex) {
			}

			@Override
			public void finish() {
			}
		}));

		return new RunObserver();
	}
	/** Execute the query. */
	private void execute() {
		JDBCConnection conn = null;
		try {
			final String dataSourceStr = getString(DATASOURCE);
			final Pool<JDBCConnection> ds = getPool(JDBCConnection.class, dataSourceStr);
			conn = ds.get();
			try {
				final String query = getString(QUERY);
				final Statement stm = conn.getConnection().createStatement();
				try {
					final ResultSet rs = stm.executeQuery(query);
					try {
						final ResultSetMetaData rsmd = rs.getMetaData();
						while (rs.next()) {
							dispatch(OUT, JDBCConverter.create(resolver(), rs, rsmd));
						}
					} finally {
						rs.close();
					}
				} finally {
					stm.close();
				}
			} finally {
				ds.put(conn);
			}
		} catch (Exception ex) {
			log(ex);
		}
	}
}
