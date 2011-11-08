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

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import eu.advance.logistics.flow.engine.util.Strings;
import eu.advance.logistics.flow.engine.xml.typesystem.XElement;
import eu.advance.logistics.flow.engine.xml.typesystem.XSerializable;

/**
 * The ADVANCE block description record.
 * @author akarnokd, 2011.06.21.
 */
public class AdvanceBlockDescription implements XSerializable {
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
	/** The definitions of various generic type parameters. */
	public final Map<String, AdvanceTypeVariable> typeVariables = Maps.newLinkedHashMap();
	/**
	 * @return a copy of this block description with separate type graph.
	 */
	public AdvanceBlockDescription copy() {
		AdvanceBlockDescription result = new AdvanceBlockDescription();

		// serialization copy trick
		XElement bs = new XElement("block");
		save(bs);
		result.load(bs);
		
		return result;
	}
	/**
	 * Load the contents from an XML element with a schema of <code>block-description.xsd</code>.
	 * @param root the root element
	 */
	@Override
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

		Deque<AdvanceType> typeRefs = Lists.newLinkedList();
		
		Map<String, AdvanceType> sharedTypes = Maps.newHashMap();
		
		for (XElement tp : root.childrenWithName("type-variable")) {
			AdvanceTypeVariable bpd = new AdvanceTypeVariable();
			bpd.load(tp);
			if (typeVariables.put(bpd.name, bpd) != null) {
				throw new DuplicateIdentifierException(tp.getXPath(), bpd.name);
			}
			typeRefs.addAll(bpd.bounds);
			AdvanceType st = new AdvanceType();
			st.typeVariableName = bpd.name;
			st.typeVariable = bpd;
			sharedTypes.put(bpd.name, st);
		}

//		throw new MissingTypeVariableException(root.getXPath(), b.typeVariableName);
		while (!typeRefs.isEmpty()) {
			AdvanceType tvb = typeRefs.pop();
			if (tvb.typeVariableName != null) {
				tvb.typeVariable = typeVariables.get(tvb.typeVariableName);
				if (tvb.typeVariable == null) {
					throw new MissingTypeVariableException(root.getXPath(), tvb.typeVariableName);
				}
			} else {
				typeRefs.addAll(tvb.typeArguments);
			}
		}
		LinkedList<AdvanceType> typeParams = Lists.newLinkedList();
		
		for (XElement inp : root.childrenWithName("input")) {
			AdvanceBlockParameterDescription bpd = new AdvanceBlockParameterDescription();
			bpd.load(inp);
			if (inputs.put(bpd.id, bpd) != null) {
				throw new DuplicateIdentifierException(inp.getXPath(), bpd.id);
			}
			// use the shared type object instead of an individual type
			if (sharedTypes.containsKey(bpd.type.typeVariableName)) {
				bpd.type = sharedTypes.get(bpd.type.typeVariableName);
			}
			typeParams.add(bpd.type);
		}
		for (XElement outp : root.childrenWithName("output")) {
			AdvanceBlockParameterDescription bpd = new AdvanceBlockParameterDescription();
			bpd.load(outp);
			if (outputs.put(bpd.id, bpd) != null) {
				throw new DuplicateIdentifierException(outp.getXPath(), bpd.id);
			}
			// use the shared type object instead of an individual type
			if (sharedTypes.containsKey(bpd.type.typeVariableName)) {
				bpd.type = sharedTypes.get(bpd.type.typeVariableName);
			}
			typeParams.add(bpd.type);
		}
		
		while (!typeParams.isEmpty()) {
			AdvanceType at = typeParams.removeFirst();
			if (at.getKind() == AdvanceTypeKind.VARIABLE_TYPE && at.typeVariable == null) {
				at.typeVariable = typeVariables.get(at.typeVariableName);
			} else
			if (at.getKind() == AdvanceTypeKind.PARAMETRIC_TYPE) {
				int i = 0;
				for (AdvanceType ta : Lists.newArrayList(at.typeArguments)) {
					if (ta.getKind() == AdvanceTypeKind.VARIABLE_TYPE) {
						AdvanceType sv = sharedTypes.get(ta.typeVariableName);
						if (sv == null) {
							throw new MissingTypeVariableException(root.getXPath(), ta.typeVariableName);
						}
						at.typeArguments.set(i, sv);
					}
					i++;
				}
				typeParams.addAll(at.typeArguments);
			}
		}
		
	}
	@Override
	public void save(XElement destination) {
		destination.set("id", id);
		destination.set("displayname", displayName);
		destination.set("documentation", documentation);
		if (keywords.size() > 0) {
			destination.set("keywords", Strings.join(keywords, ","));
		} else {
			destination.set("keywords", null);
		}
		destination.set("category", category);
		for (AdvanceTypeVariable item : typeVariables.values()) {
			item.save(destination.add("type-variable"));
		}
		for (AdvanceBlockParameterDescription item : inputs.values()) {
			item.save(destination.add("input"));
		}
		for (AdvanceBlockParameterDescription item : outputs.values()) {
			item.save(destination.add("output"));
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
	/**
	 * Serialize the list of block descriptions into an XElement.
	 * @param list the list of block descriptions
	 * @return the XElement
	 */
	public static XElement serialize(Iterable<AdvanceBlockDescription> list) {
		XElement result = new XElement("block-description-list");
		for (AdvanceBlockDescription item : list) {
			item.save(result.add("block-description"));
		}
		return result;
	}
}
