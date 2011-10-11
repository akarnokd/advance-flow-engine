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

package eu.advance.logistics.flow.engine.test;

import java.io.IOException;
import java.util.Arrays;
import java.util.Date;

import eu.advance.logistics.flow.engine.AdvanceCompiler;
import eu.advance.logistics.flow.engine.AdvanceEngineConfig;
import eu.advance.logistics.flow.engine.api.AdvanceControlException;
import eu.advance.logistics.flow.engine.api.AdvanceDataStore;
import eu.advance.logistics.flow.engine.api.AdvanceEngineControl;
import eu.advance.logistics.flow.engine.api.AdvanceRealm;
import eu.advance.logistics.flow.engine.api.AdvanceRealmStatus;
import eu.advance.logistics.flow.engine.api.AdvanceUser;
import eu.advance.logistics.flow.engine.api.AdvanceUserRights;
import eu.advance.logistics.flow.engine.api.impl.CheckedEngineControl;
import eu.advance.logistics.flow.engine.api.impl.LocalEngineControl;

/**
 * Creates a basic local flow engine with a constant configuration.
 * @author karnokd, 2011.10.11.
 */
public final class BasicLocalEngine {
	/**
	 * Utility class.
	 */
	private BasicLocalEngine() {
		// TODO Auto-generated constructor stub
	}
	/**
	 * Create a default local flow engine and access it via the given username.
	 * @param userName the target username
	 * @return the engine control
	 */
	public static AdvanceEngineControl create(String userName) {
		final AdvanceEngineConfig config = AdvanceEngineConfig.defaultConfig();
		AdvanceCompiler compiler = new AdvanceCompiler(config.schemaResolver, 
				config.blockResolver, config.schedulerMap);
		AdvanceDataStore datastore = config.datastore();
		try {
			if (datastore.queryUsers().isEmpty()) {
				addFirst(datastore);
			}
		} catch (IOException ex) {
			// ignored
		} catch (AdvanceControlException ex) {
			// ignored
		}
		AdvanceEngineControl engine = new LocalEngineControl(datastore, config.schemas, compiler, compiler) {
			@Override
			public void shutdown() throws IOException,
					AdvanceControlException {
				super.shutdown();
				config.close();
			}
		};
		return new CheckedEngineControl(engine, userName);
	}
	/**
	 * Add first elements to the datastore.
	 * @param ds the datastore
	 */
	private static void addFirst(AdvanceDataStore ds) {
		AdvanceUser u = new AdvanceUser();
		u.name = "admin";
		u.password("admin".toCharArray());
		u.thousandSeparator = ',';
		u.decimalSeparator = '.';
		u.dateFormat = "yyyy-MM-dd";
		u.dateTimeFormat = "yyyy-MM-dd HH:mm:ss";
		u.numberFormat = "#,###";
		u.enabled = true;
		u.passwordLogin = true;
		u.rights.addAll(Arrays.asList(AdvanceUserRights.values()));
		u.createdAt = new Date();
		u.createdBy = "setup";
		u.modifiedAt = new Date();
		u.modifiedBy = "setup";
		u.email = "admin@advance-logistics.eu";
		try {
			ds.updateUser(u);
		} catch (IOException ex) {
			// ignore
		} catch (AdvanceControlException ex) {
			// ignore
		}
		
		AdvanceRealm realm = new AdvanceRealm();
		realm.name = "DEFAULT";
		realm.status = AdvanceRealmStatus.STOPPED;
		realm.createdAt = new Date();
		realm.createdBy = "setup";
		realm.modifiedAt = new Date();
		realm.modifiedBy = "setup";
		
		try {
			ds.updateRealm(realm);
		} catch (IOException ex) {
			// ignore
		} catch (AdvanceControlException ex) {
			// ignore
		}
	}
}
