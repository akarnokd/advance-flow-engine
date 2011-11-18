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

package eu.advance.logistics.flow.engine.runtime;

import hu.akarnokd.reactive4java.base.Func0;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import javax.xml.stream.XMLStreamException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;

import eu.advance.logistics.flow.engine.model.fd.AdvanceBlockDescription;
import eu.advance.logistics.flow.engine.xml.XElement;
import eu.advance.logistics.flow.engine.xml.XSerializable;

/**
 * The block registry entry of the block-registry.xml and xsd.
 * @author akarnokd, 2011.07.05.
 */
public class BlockRegistryEntry extends AdvanceBlockDescription 
implements XSerializable {
	/** The logger. */
	protected static final Logger LOG = LoggerFactory.getLogger(BlockRegistryEntry.class);
	/** The implementation class. */
	public String clazz;
	/** The preferred scheduler. */
	public SchedulerPreference scheduler;
	/** Creates an instance of this class. */
	public static final Func0<BlockRegistryEntry> CREATOR = new Func0<BlockRegistryEntry>() {
		@Override
		public BlockRegistryEntry invoke() {
			return new BlockRegistryEntry();
		}
	};
	/**
	 * Default constructor.
	 */
	public BlockRegistryEntry() {
		// empty
	}
	/**
	 * Copy constructor with the option to copy settings from another registry but use the given description.
	 * @param other the other registry
	 * @param desc the description to use instead
	 */
	public BlockRegistryEntry(BlockRegistryEntry other, AdvanceBlockDescription desc) {
		this.clazz = other.clazz;
		this.scheduler = other.scheduler;
		super.assign(desc);
	}
	@Override
	public void load(XElement root) {
		super.load(root);
		clazz = root.get("class");
		String s = root.get("scheduler");
		if (s != null) {
			scheduler = SchedulerPreference.valueOf(s);
		} else {
			scheduler = SchedulerPreference.IO;
		}
	}
	@Override
	public void save(XElement destination) {
		super.save(destination);
		destination.set("class", clazz);
		destination.set("scheduler", scheduler.name());
	}
	@Override
	public BlockRegistryEntry copy() {
		BlockRegistryEntry result = new BlockRegistryEntry();
		XElement e = new XElement("e");
		save(e);
		result.load(e);
		return result;
	}
	/**
	 * Parse an XML tree which contains block registry descriptions as a list.
	 * @param root the root element conforming the {@code block-registry.xsd}.
	 * @return the list of block registry definitions
	 */
	public static List<BlockRegistryEntry> parseRegistry(XElement root) {
		List<BlockRegistryEntry> result = Lists.newArrayList();
		
		for (XElement e : root.childrenWithName("block-description")) {
			BlockRegistryEntry abd = new BlockRegistryEntry();
			abd.load(e);
			result.add(abd);
		}
		
		return result;
	}
	/**
	 * Serialize the given source of registry entries.
	 * @param entries the source of registry entries
	 * @return the XElement representation of the block registry
	 */
	public static XElement serializeRegistry(Iterable<BlockRegistryEntry> entries) {
		XElement result = new XElement("block-registry");
		for (BlockRegistryEntry e : entries) {
			e.save(result.add("block-description"));
		}
		return result;
	}
	/**
	 * @return Parse the default block registry under {@code eu.advance.logistics.flow.engine.schemas/block-registry.xml}.
	 */
	public static List<BlockRegistryEntry> parseDefaultRegistry() {
		try {
			InputStream in = BlockRegistryEntry.class.getResourceAsStream("/block-registry.xml");
			try {
				return parseRegistry(XElement.parseXML(in));
			} finally {
				in.close();
			}
		} catch (IOException ex) {
			LoggerFactory.getLogger(BlockRegistryEntry.class).error(ex.toString(), ex);
		} catch (XMLStreamException ex) {
			LoggerFactory.getLogger(BlockRegistryEntry.class).error(ex.toString(), ex);
		}
		return Lists.newArrayList();
	}
	@Override
	public String toString() {
		XElement e = new XElement("block-description");
		save(e);
		return e.toString();
	}
}
