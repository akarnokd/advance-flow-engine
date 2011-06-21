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

package eu.advance.logistics.flow.model;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import eu.advance.logistics.util.Strings;
import eu.advance.logistics.xml.typesystem.XElement;

/**
 * The ADVANCE block description record.
 * @author karnokd, 2011.06.21.
 */
public class AdvanceBlockDescription {
	/** The unique block identifier or name. */
	@NonNull
	public String id;
	/** The optional display text for this block. Can be used as a key into a translation table. */
	@Nullable
	public String displayName;
	/** Optional reference to the documentation of this block. May point to a Wiki page. */
	@Nullable
	public URI documentation;
	/** The keywords associated with this block type. */
	public final List<String> keywords = Lists.newArrayList();
	/** The category for this block. */
	@Nullable
	public String category;
	/** Definition of an input parameter. Producer blocks may not have any input parameters. */
	public final Map<String, AdvanceBlockParameterDescription> inputs = Maps.newLinkedHashMap();
	/** The definition of an output parameter. Consumer blocks may not have any output parameters. */
	public final Map<String, AdvanceBlockParameterDescription> outputs = Maps.newLinkedHashMap();
	/**
	 * Load the contents from an XML element with a schema of <code>block-description.xsd</code>.
	 * @param root the root element
	 */
	public void load(XElement root) {
		id = root.get("id");
		displayName = root.get("displayname");
		String doc = root.get("documentation");
		if (doc != null) {
			try {
				documentation = new URI(doc);
			} catch (URISyntaxException ex) {
				throw new RuntimeException(ex);
			}
		}
		String kw = root.get("keywords");
		if (kw != null) {
			keywords.addAll(Strings.trim(Strings.split(kw, ',')));
		}
		category = root.get("category");
		for (XElement inp : root.childrenWithName("input")) {
			AdvanceBlockParameterDescription bpd = new AdvanceBlockParameterDescription();
			bpd.load(inp);
			inputs.put(bpd.id, bpd);
		}
		for (XElement outp : root.childrenWithName("output")) {
			AdvanceBlockParameterDescription bpd = new AdvanceBlockParameterDescription();
			bpd.load(outp);
			inputs.put(bpd.id, bpd);
		}
	}
	/**
	 * Parse an XML tree which contains block descriptions as a list.
	 * @param root the root element conforming the {@code block-description-list.xsd}.
	 * @return the list of block definitions
	 */
	public static List<AdvanceBlockDescription> parse(XElement root) {
		List<AdvanceBlockDescription> result = Lists.newArrayList();
		
		for (XElement e : root.childrenWithName("block-description")) {
			AdvanceBlockDescription abd = new AdvanceBlockDescription();
			abd.load(e);
			result.add(abd);
		}
		
		return result;
	}
}
