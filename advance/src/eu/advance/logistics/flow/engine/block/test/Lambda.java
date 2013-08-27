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

package eu.advance.logistics.flow.engine.block.test;

import hu.akarnokd.reactive4java.base.Option;
import hu.akarnokd.utils.xml.XNElement;

import java.util.Map;

import javax.script.Bindings;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.script.SimpleBindings;

import eu.advance.logistics.flow.engine.AdvanceBlock;
import eu.advance.logistics.flow.engine.runtime.BlockDiagnostic;
import eu.advance.logistics.flow.engine.runtime.BlockState;

/**
 * A generic block which takes a javascript function and applies the rest of the parameters to it.
 * @author akarnokd, 2011.11.14.
 */
public abstract class Lambda extends AdvanceBlock {
	@Override
	protected void invoke() {
		ScriptEngineManager sef = new ScriptEngineManager();
		ScriptEngine se = sef.getEngineByName("js");
		if (se != null) {
			Bindings bind = new SimpleBindings();
			bind.put("_block", this);
			String spn = scriptParamName();
			String script = getString(spn);
			
			for (Map.Entry<String, XNElement> e : params.entrySet()) {
				if (!spn.equals(e.getKey())) {
					bind.put(e.getKey(), e.getValue());
				}
			}
			
			try {
				scriptValue(params, se.eval(script, bind));
			} catch (ScriptException ex) {
				LOG.error(ex.toString(), ex);
				diagnostic.next(new BlockDiagnostic("", description().id, Option.<BlockState>error(ex)));
			}
		} else {
			LOG.error("Missing js script engine.", new AssertionError("Missing js script engine."));
		}
	}
	/**
	 * Returns the script parameter name.
	 * @return the script parameterName
	 */
	protected abstract String scriptParamName();
	/**
	 * Handle the value returned by the script.
	 * @param params the initial parameters
	 * @param o the object returned by the script
	 */
	protected abstract void scriptValue(Map<String, XNElement> params, Object o);
}
