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

package eu.advance.logistics.flow.engine;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import edu.umd.cs.findbugs.annotations.NonNull;
import eu.advance.logistics.flow.engine.runtime.Block;
import eu.advance.logistics.flow.engine.runtime.BlockRegistryEntry;

/**
 * Class that locates and creates blocks based on their name.
 * @author akarnokd, 2011.09.28.
 * @param <T> the runtime type of the dataflow
 * @param <X> the type system type
 * @param <C> the runtime context
 */
public class AdvanceDefaultBlockResolver<T, X, C> implements AdvanceBlockResolver<T, X, C> {
	/** The logger. */
	protected static final Logger LOG = LoggerFactory.getLogger(AdvanceDefaultBlockResolver.class);
	/** The block descriptions. */
	protected final Map<String, BlockRegistryEntry> blocks = Maps.newHashMap();
	/** The classloader user to initialize the block instances. */
	protected final ClassLoader classLoader;
	/**
	 * Constructor. Initializes the block registry.
	 * @param blocks the map from block name to block description
	 */
	public AdvanceDefaultBlockResolver(Map<String, BlockRegistryEntry> blocks) {
		this(blocks, Thread.currentThread().getContextClassLoader());
	}
	/**
	 * Constructor. Initializes the block registry.
	 * @param blocks the map from block name to block description
	 * @param classLoader the class loader used to initialize block instances
	 */
	public AdvanceDefaultBlockResolver(Map<String, BlockRegistryEntry> blocks, ClassLoader classLoader) {
		this.blocks.putAll(blocks);
		this.classLoader = classLoader != null ? classLoader : ClassLoader.getSystemClassLoader();
	}
	@Override
	public BlockRegistryEntry lookup(@NonNull String id) {
		return blocks.get(id);
	}
	@Override
	public List<String> blocks() {
		return Lists.newArrayList(blocks.keySet());
	}
	@Override
	public Block<T, X, C> create(@NonNull String id) {
		try {
			BlockRegistryEntry e = blocks.get(id);
			Class<?> clazz = Class.forName(e.clazz, true, classLoader);
			if (Block.class.isAssignableFrom(clazz)) {
				try {
					Constructor<?> c = clazz.getConstructor();
					// FIXME is ther any way to actually check the type parameters of the resolved blocks?
					@SuppressWarnings("unchecked")
					Block<T, X, C> b = Block.class.cast(c.newInstance()); 
					return b;
				} catch (NoSuchMethodException ex) {
					LOG.error("Missing default constructor.", ex);
				} catch (SecurityException ex) {
					LOG.error(ex.toString(), ex);
				} catch (InstantiationException ex) {
					LOG.error(ex.toString(), ex);
				} catch (IllegalAccessException ex) {
					LOG.error(ex.toString(), ex);
				} catch (IllegalArgumentException ex) {
					LOG.error(ex.toString(), ex);
				} catch (InvocationTargetException ex) {
					LOG.error(ex.toString(), ex);
				}
			} else {
				LOG.error("Block " + id + " of class " + e.clazz + " is not an AdvanceBlock");
			}
		} catch (ClassNotFoundException ex) {
			LOG.error(ex.toString(), ex);
		}
		return null;
	}

}
