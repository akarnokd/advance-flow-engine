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

package eu.advance.logistics.flow.engine.model.fd;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import eu.advance.logistics.flow.engine.util.Strings;
import eu.advance.logistics.flow.engine.xml.XElement;
import eu.advance.logistics.flow.engine.xml.XSerializable;

/**
 * The block reference for the {@code flow-description.xsd} used within composite blocks.
 * @author akarnokd, 2011.06.21.
 */
public class AdvanceBlockReference implements XSerializable {
	/** The logger. */
	protected static final Logger LOG = LoggerFactory.getLogger(AdvanceBlockReference.class);
	/** The unique identifier of this block among the current level of blocks. */
	@NonNull
	public String id;
	/** The block type identifier referencing a block description. */
	@NonNull
	public String type;
	/** The user-entered documentation of this composite block. */
	@Nullable
	public String documentation;
	/** The parent block of this composite block. */
	public AdvanceCompositeBlock parent;
	/** The user-entered keywords for easier finding of this block. */
	public final List<String> keywords = Lists.newArrayList();
	/** The visual properties for the Flow Editor. */
	public final AdvanceBlockVisuals visuals = new AdvanceBlockVisuals();
	/** Contains the number of parameters for the varargs inputs. */
	public final Map<String, Integer> varargs = Maps.newHashMap();
	/**
	 * Load a block reference from an XML element which conforms the {@code flow-description.xsd}.
	 * @param source the element of a input or output
	 */
	@Override
	public void load(XElement source) {
		id = source.get("id");
		type = source.get("type");
		documentation = source.get("documentation");
		String kw = source.get("keywords");
		if (kw != null) {
			keywords.addAll(Strings.trim(Strings.split(kw, ',')));
		}
		visuals.load(source);
		for (XElement e : source.childrenWithName("vararg")) {
			varargs.put(e.get("name"), e.getInt("count"));
		}
	}
	@Override
	public void save(XElement destination) {
		destination.set("id", id);
		destination.set("type", type);
		destination.set("documentation", documentation);
		if (keywords.size() > 0) {
			destination.set("keywords", Strings.join(keywords, ","));
		} else {
			destination.set("keywords", null);
		}
		visuals.save(destination);
		for (Map.Entry<String, Integer> va : varargs.entrySet()) {
			XElement e = destination.add("vararg");
			e.set("name", va.getKey(), "count", va.getValue());
		}
	}
}
