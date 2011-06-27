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

import hu.akarnokd.reactive4java.base.Func1;
import hu.akarnokd.reactive4java.base.Option;
import hu.akarnokd.reactive4java.interactive.Interactive;
import hu.akarnokd.reactive4java.reactive.DefaultObservable;
import hu.akarnokd.reactive4java.reactive.Observable;
import hu.akarnokd.reactive4java.reactive.Observer;

import java.io.Closeable;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import eu.advance.logistics.flow.model.AdvanceBlockDescription;
import eu.advance.logistics.flow.model.AdvanceBlockParameterDescription;
import eu.advance.logistics.flow.model.AdvanceConstantBlock;
import eu.advance.logistics.flow.model.AdvanceResolver;
import eu.advance.logistics.util.ReactiveEx;
import eu.advance.logistics.xml.typesystem.XElement;

/**
 * The generic ADVANCE block.
 * @author karnokd, 2011.06.22.
 */
public abstract class AdvanceBlock {
	/** The logger. */
	protected static final Logger LOG = LoggerFactory.getLogger(AdvanceBlock.class);
	/** The global identifier of this block. */ 
	private final int gid;
	/** The input ports. */
	public final List<AdvancePort> inputs;
	/** The output ports. */
	public final List<AdvanceBlockPort> outputs;
	/** The original description of this block. */
	private AdvanceBlockDescription description;
	/** The block diagnostic observable. */
	private DefaultObservable<AdvanceBlockDiagnostic> diagnostic;
	/** The close the observer of the inputs. */
	private Closeable functionClose;
	/**
	 * Constructor.  
	 * @param gid The global identifier of this block. 
	 */
	public AdvanceBlock(int gid) {
		this.gid = gid;
		inputs = Lists.newArrayList();
		outputs = Lists.newArrayList();
	}
	/** 
	 * Initialize the block with the given definition and body function.
	 * @param desc the description of the block
	 * @param constantParams the map of those parameters who have a constant input instead of other output ports
	 */
	public void init(
			AdvanceBlockDescription desc, 
			final Map<String, AdvanceConstantBlock> constantParams) {
		this.description = desc;
		for (AdvanceBlockParameterDescription in : desc.inputs.values()) {
			AdvanceConstantBlock cb = constantParams.get(in.id); 
			if (cb == null) {
				AdvanceBlockPort p = new AdvanceBlockPort(this, in.id);
				p.type = AdvanceResolver.resolveSchema(in.type);
				inputs.add(p);
			} else {
				AdvanceConstantPort p = new AdvanceConstantPort(this, in.id);
				p.value = cb.value;
				p.type = AdvanceResolver.resolveSchema(in.type);
				inputs.add(p);
			}
		}
		for (AdvanceBlockParameterDescription out : desc.outputs.values()) {
			AdvanceBlockPort p = new AdvanceBlockPort(this, out.id);
			p.type = AdvanceResolver.resolveSchema(out.type);
			outputs.add(p);
		}
		diagnostic = new DefaultObservable<AdvanceBlockDiagnostic>(false, false);
	}
	/** 
	 * Schedule the execution of the body function. 
	 * @return the observer to trigger in the run phase
	 */
	public Observer<Void> run() {
		List<AdvancePort> reactivePorts = Lists.newArrayList(Interactive.where(inputs, new Func1<AdvancePort, Boolean>() {
			@Override
			public Boolean invoke(AdvancePort param1) {
				return !(param1 instanceof AdvanceConstantPort);
			}
		}));
		
		if (inputs.size() == 0 || reactivePorts.size() == 0) {
			return new Observer<Void>() {

				@Override
				public void next(Void value) {
					if (inputs.size() == 0) {
						invokeBody(Lists.<XElement>newArrayList());
					} else {
						invokeBody(Lists.newArrayList(Interactive.select(
								inputs,
								new Func1<AdvancePort, XElement>() {
									@Override
									public XElement invoke(AdvancePort param1) {
										return ((AdvanceConstantPort)param1).value;
									}
								}
						)));
					}
					
				}

				@Override
				public void error(Throwable ex) {
					diagnostic.next(new AdvanceBlockDiagnostic(AdvanceBlock.this, Option.<AdvanceBlockState>error(ex)));
				}

				@Override
				public void finish() {
					LOG.info("Finish? " + description.id);
				}
				
			};
		}
		functionClose = ReactiveEx.combine(reactivePorts).register(new Observer<List<XElement>>() {
			@Override
			public void next(List<XElement> value) {
				invokeBody(value);
			}

			@Override
			public void error(Throwable ex) {
				diagnostic.next(new AdvanceBlockDiagnostic(AdvanceBlock.this, Option.<AdvanceBlockState>error(ex)));
			}

			@Override
			public void finish() {
				LOG.info("Finish? " + description.id);
			}
		});
		return new Observer<Void>() {
			@Override
			public void next(Void value) {
				// no operation in this case
			}

			@Override
			public void error(Throwable ex) {
				diagnostic.next(new AdvanceBlockDiagnostic(AdvanceBlock.this, Option.<AdvanceBlockState>error(ex)));
			}

			@Override
			public void finish() {
				LOG.info("Finish? " + description.id);
			}
			
		};
	}
	/**
	 * Invoke the body function when all elements are available.
	 * @param value the list of input parameters
	 */
	void invokeBody(List<XElement> value) {
		diagnostic.next(new AdvanceBlockDiagnostic(AdvanceBlock.this, Option.some(AdvanceBlockState.START)));
		try {
			Map<String, XElement> funcIn = Maps.newHashMap();
			for (int i = 0; i < inputs.size(); i++) {
				AdvancePort p = inputs.get(i);
				if (p instanceof AdvanceConstantPort) {
					funcIn.put(p.name(), ((AdvanceConstantPort) p).value);
				} else {
					funcIn.put(p.name(), value.get(i));
				}
			}
			
			Map<String, XElement> funcOut = invoke(funcIn);
			
			boolean valid = true;
			for (int i = 0; i < outputs.size(); i++) {
				if (!funcOut.containsKey(outputs.get(i).name)) {
					diagnostic.next(new AdvanceBlockDiagnostic(AdvanceBlock.this, Option.<AdvanceBlockState>error(new IllegalArgumentException(outputs.get(i).name + " missing"))));
					LOG.error("missing output '" + outputs.get(i).name + "' at the block type " + description.id);
					valid = false;
				}
			}
			if (valid) {
				for (int i = 0; i < outputs.size(); i++) {
					AdvanceBlockPort p = outputs.get(i);
					p.next(funcOut.get(p.name));
				}						
				diagnostic.next(new AdvanceBlockDiagnostic(AdvanceBlock.this, Option.some(AdvanceBlockState.FINISH)));
			}
		} catch (Throwable t) {
			diagnostic.next(new AdvanceBlockDiagnostic(AdvanceBlock.this, Option.<AdvanceBlockState>error(t)));
		}
	}
	/**
	 * The body function to invoke.
	 * @param params the parameters
	 * @return the result
	 */
	protected abstract Map<String, XElement> invoke(Map<String, XElement> params);
	/** Terminate the block. */
	public void done() {
		if (functionClose != null) {
			try {
				functionClose.close();
			} catch (IOException ex) {
				LOG.info("", ex);
			}
		}
		diagnostic.finish();
	}
	/** @return the block global id. */
	public int getGid() {
		return gid;
	}
	/** @return the block's description. */
	public AdvanceBlockDescription getDescription() {
		return description;
	}
	/**
	 * @return the diagnostic port for watch the invocation of the body function
	 */
	public Observable<AdvanceBlockDiagnostic> getDiagnosticPort() {
		return diagnostic;
	}
}
