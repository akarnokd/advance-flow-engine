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

import hu.akarnokd.utils.xml.XNElement;

import java.io.IOException;
import java.io.StringReader;
import java.util.Arrays;
import java.util.Date;
import java.util.Map;

import javax.xml.stream.XMLStreamException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import eu.advance.logistics.flow.engine.AdvanceBlockResolver;
import eu.advance.logistics.flow.engine.AdvanceCompiler;
import eu.advance.logistics.flow.engine.AdvanceCompilerSettings;
import eu.advance.logistics.flow.engine.AdvanceData;
import eu.advance.logistics.flow.engine.AdvanceDefaultBlockResolver;
import eu.advance.logistics.flow.engine.AdvanceEngineConfig;
import eu.advance.logistics.flow.engine.AdvancePluginManager;
import eu.advance.logistics.flow.engine.AdvanceRuntimeContext;
import eu.advance.logistics.flow.engine.AdvanceTypeFunctions;
import eu.advance.logistics.flow.engine.LocalEngineControl;
import eu.advance.logistics.flow.engine.api.AdvanceEngineControl;
import eu.advance.logistics.flow.engine.api.AdvanceFlowCompiler;
import eu.advance.logistics.flow.engine.api.core.AdvanceControlException;
import eu.advance.logistics.flow.engine.api.ds.AdvanceDataStore;
import eu.advance.logistics.flow.engine.api.ds.AdvanceRealm;
import eu.advance.logistics.flow.engine.api.ds.AdvanceRealmStatus;
import eu.advance.logistics.flow.engine.api.ds.AdvanceUser;
import eu.advance.logistics.flow.engine.api.ds.AdvanceUserRights;
import eu.advance.logistics.flow.engine.api.impl.CheckedEngineControl;
import eu.advance.logistics.flow.engine.model.fd.AdvanceType;
import eu.advance.logistics.flow.engine.runtime.BlockRegistryEntry;

/**
 * Creates a basic local flow engine with a constant configuration.
 * @author akarnokd, 2011.10.11.
 */
public final class BasicLocalEngine {
	/** The logger. */
	protected static final Logger LOG = LoggerFactory.getLogger(BasicLocalEngine.class);
	/**
	 * Utility class.
	 */
	private BasicLocalEngine() {
		// TODO Auto-generated constructor stub
	}
	/**
	 * Create a default local flow engine and access it via the given username.
	 * @param userName the target username
	 * @param workDir the working directory for the configuration, keystores and schemas
	 * @return the engine control
	 */
	public static AdvanceEngineControl create(String userName, String workDir) {
		final AdvanceEngineConfig config = defaultConfig(workDir);
		AdvanceCompilerSettings<XNElement, AdvanceType, AdvanceRuntimeContext> compilerSettings = config.createCompilerSettings();
		AdvanceCompiler<XNElement, AdvanceType, AdvanceRuntimeContext> compiler = 
				new AdvanceCompiler<XNElement, AdvanceType, AdvanceRuntimeContext>(compilerSettings);
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
		AdvanceEngineControl engine = new LocalEngineControl(datastore, config.schemas, compiler, compiler, workDir) {
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
	 * Create a simple configuration with local resources.
	 * @param workDir the working directory
	 * @return the configuration
	 */
	public static AdvanceEngineConfig defaultConfig(String workDir) {
		String defaultConfigText = defaultConfigFile();
		
		AdvanceEngineConfig config = new AdvanceEngineConfig();
		try {
			config.initialize(XNElement.parseXML(new StringReader(defaultConfigText)), workDir);
		} catch (XMLStreamException ex) {
			LOG.error(ex.toString(), ex);
		}
		return config;
	}
	/**
	 * Composes the default configuration file XML.
	 * @return the file contents as string
	 */
	public static String defaultConfigFile() {
		return "<?xml version='1.0' encoding='UTF-8'?>"
		+ "<flow-engine-config>"
		+ " <listener cert-auth-port='8443' server-keyalias='advance-server' basic-auth-port='8444' server-password='YWR2YW5jZQ==' server-keystore='DEFAULT' client-keystore='DEFAULT'/>"
		+ " <datastore driver='LOCAL' url='datastore.xml'/>"
		+ " <keystore name='DEFAULT' file='keystore' password='YWR2YW5jZQ=='/>"
		+ " <scheduler type='CPU' concurrency='ALL_CORES' priority='NORMAL'/>"
		+ " <scheduler type='SEQUENTIAL' concurrency='1' priority='NORMAL'/>"
		+ " <scheduler type='IO' concurrency='32' priority='NORMAL'/>"
		+ "</flow-engine-config>"
		;
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
			LOG.error(ex.toString(), ex);
		} catch (AdvanceControlException ex) {
			LOG.error(ex.toString(), ex);
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
			LOG.error(ex.toString(), ex);
		} catch (AdvanceControlException ex) {
			LOG.error(ex.toString(), ex);
		}
	}
	/**
	 * Creates a compiler with the locally available block registry and schemas.
	 * @return the compiler
	 */
	public static AdvanceFlowCompiler<XNElement, AdvanceType, AdvanceRuntimeContext> createCompiler() {
		
		Map<String, BlockRegistryEntry> bm = Maps.newHashMap();
		
		for (BlockRegistryEntry e : BlockRegistryEntry.parseDefaultRegistry()) {
			bm.put(e.id, e);
		}
		
		AdvanceDefaultBlockResolver<XNElement, AdvanceType, AdvanceRuntimeContext> br = 
				new AdvanceDefaultBlockResolver<XNElement, AdvanceType, AdvanceRuntimeContext>(bm);
		Map<String, AdvanceBlockResolver<XNElement, AdvanceType, AdvanceRuntimeContext>> brMap = Maps.newHashMap();
		for (String s : bm.keySet()) {
			brMap.put(s, br);
		}
		
		AdvanceCompilerSettings<XNElement, AdvanceType, AdvanceRuntimeContext> compilerSettings = 
				new AdvanceCompilerSettings<XNElement, AdvanceType, AdvanceRuntimeContext>();
		// engine-local schemas
		compilerSettings.defaultSchemas = Lists.newArrayList();
		// default blocks
		compilerSettings.defaultBlocks = brMap; 
		compilerSettings.schedulers = Maps.newHashMap();
		// without plugins
		compilerSettings.pluginManager = new AdvancePluginManager<XNElement, AdvanceType, AdvanceRuntimeContext>("");
		compilerSettings.resolver = new AdvanceData();
		compilerSettings.typeFunctions = new AdvanceTypeFunctions();
		AdvanceCompiler<XNElement, AdvanceType, AdvanceRuntimeContext> compiler = 
				new AdvanceCompiler<XNElement, AdvanceType, AdvanceRuntimeContext>(compilerSettings);

		return compiler;
	}
}
