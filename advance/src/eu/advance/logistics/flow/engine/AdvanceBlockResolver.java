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
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Maps;

import edu.umd.cs.findbugs.annotations.NonNull;
import eu.advance.logistics.flow.engine.model.rt.AdvanceBlock;
import eu.advance.logistics.flow.engine.model.rt.AdvanceBlockRegistryEntry;
import eu.advance.logistics.flow.engine.model.rt.AdvanceBlockSettings;

/**
 * Class that locates and creates blocks based on their name.
 * @author akarnokd, 2011.09.28.
 */
public class AdvanceBlockResolver {
	/** The logger. */
	protected static final Logger LOG = LoggerFactory.getLogger(AdvanceBlockResolver.class);
	/** The block descriptions. */
	protected final Map<String, AdvanceBlockRegistryEntry> blocks = Maps.newHashMap();
	/**
	 * Constructor. Initializes the block registry.
	 * @param blocks the map from block name to block description
	 */
	public AdvanceBlockResolver(Map<String, AdvanceBlockRegistryEntry> blocks) {
		this.blocks.putAll(blocks);
	}
	/**
	 * Locate the block description in the repository (e.g., block-repository.xml).
	 * @param id the block identifier
	 * @return the block
	 */
	public AdvanceBlockRegistryEntry lookup(@NonNull String id) {
		return blocks.get(id);
	}
	/**
	 * Create a concrete block by using the given settings.
	 * @param settings the block contextuals settings
	 * @return the new block instance 
	 */
	public AdvanceBlock create(AdvanceBlockSettings settings) {
		try {
			Class<?> clazz = Class.forName(settings.description.clazz);
			if (AdvanceBlock.class.isAssignableFrom(clazz)) {
				try {
					Constructor<?> c = clazz.getConstructor(
							AdvanceBlockSettings.class);
					return AdvanceBlock.class.cast(c.newInstance(settings));
				} catch (NoSuchMethodException ex) {
					LOG.error("Missing constructor of {AdvanceBlockSettings}", ex);
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
				LOG.error("Block " + settings.instance.type + " of class " + settings.description.clazz + " is not an AdvanceBlock");
			}
		} catch (ClassNotFoundException ex) {
			LOG.error(ex.toString(), ex);
		}
		return null;
	}

}
