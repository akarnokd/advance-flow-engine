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

package eu.advance.logistics.flow.engine.test;

import hu.akarnokd.reactive4java.base.Observer;
import hu.akarnokd.reactive4java.reactive.Reactive;
import hu.akarnokd.reactive4java.swing.DefaultEdtScheduler;

import java.io.Closeable;
import java.net.URL;
import java.util.List;

import eu.advance.logistics.flow.engine.api.AdvanceEngineControl;
import eu.advance.logistics.flow.engine.api.AdvanceEngineVersion;
import eu.advance.logistics.flow.engine.api.ds.AdvanceRealm;
import eu.advance.logistics.flow.engine.api.ds.AdvanceUser;
import eu.advance.logistics.flow.engine.api.impl.HttpRemoteEngineControl;
import eu.advance.logistics.flow.engine.compiler.AdvanceCompilationResult;
import eu.advance.logistics.flow.engine.model.fd.AdvanceCompositeBlock;
import eu.advance.logistics.flow.engine.runtime.BlockDiagnostic;

/**
 * Simple remote connection and basic API calls.
 * @author akarnokd, 2011.10.11.
 */
public final class TestBasicRemote {

	/**
	 * Test class.
	 */
	private TestBasicRemote() {
	}

	/**
	 * Main program.
	 * @param args no arguments
	 * @throws Exception on error
	 */
	public static void main(String[] args) throws Exception {
		// indicate the usage of HTTP remote engine
		AdvanceEngineControl engine = new HttpRemoteEngineControl(new URL("localhost:8080"), "admin", "admin".toCharArray());

		// query version
		AdvanceEngineVersion version = engine.queryVersion();

		System.out.println(version);
		
		// query user settings
		AdvanceUser user = engine.getUser();

		// list realms
		List<AdvanceRealm> realms = engine.datastore().queryRealms();

		// download flow from a realm
		AdvanceCompositeBlock flow = engine.queryFlow(realms.get(0).name);

		// upload from into a realm
		engine.updateFlow(realms.get(0).name, flow, user.name);

		// verify the flow
		AdvanceCompilationResult result = engine.verifyFlow(flow);
		System.out.println(result.success());

		// start debugging a block
		Closeable c = Reactive.observeOn(engine.debugBlock("realm", "blockId"), new DefaultEdtScheduler()).register(new Observer<BlockDiagnostic>() {
		   @Override public void next(BlockDiagnostic value) {
		       System.out.println(value.state);
		    }
		   @Override public void error(Throwable t) {
		       t.printStackTrace();
		    }
		   @Override public void finish() { }
		});

		Thread.sleep(1000);
		// stop debugging a block
		c.close();
		
	}

}
