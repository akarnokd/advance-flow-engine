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

package eu.advance.logistics.flow.engine;

import java.io.IOException;

import javax.xml.stream.XMLStreamException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.advance.logistics.flow.engine.xml.typesystem.XElement;

/**
 * The Advance Data Flow Engine main program.
 * The engine should be run as a standalone program to be able to:
 * <ul>
 * <li>Run it independently of the web application. This allows it to be placed beside the web application or onto a completely different server.</li>
 * <li>JVM or Java crash in the flow engine or in the web application should not affect the other party.</li>
 * </ul>
 * @author karnokd
 */
public class AdvanceFlowEngine implements Runnable {
	/** The logger. */
	protected static final Logger LOG = LoggerFactory.getLogger(AdvanceFlowEngine.class);
	/** The version of the flow engine. */
	public static final String VERSION = "0.01.068";
	@Override
	public void run() {
		// TODO Auto-generated method stub
		LOG.info("Advance Flow Engine Started");
		
		final AdvanceEngineConfig config = new AdvanceEngineConfig();
		try {
			XElement xconfig = XElement.parseXML("conf/flow_engine_config.xml");
			config.initialize(xconfig);
		} catch (IOException ex) {
			LOG.error(ex.toString(), ex);
		} catch (XMLStreamException ex) {
			LOG.error(ex.toString(), ex);
		}
		
		
		LOG.info("Advance Flow Engine Terminated");
	}
	/**
	 * The main program.
	 * @param args no arguments at the moment
	 */
	public static void main(String[] args) {
		AdvanceFlowEngine afe = new AdvanceFlowEngine();
		afe.run();
	}
}
