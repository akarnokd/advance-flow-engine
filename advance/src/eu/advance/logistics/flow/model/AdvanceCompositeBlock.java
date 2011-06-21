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

import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import eu.advance.logistics.util.Strings;
import eu.advance.logistics.xml.typesystem.XElement;

/**
 * The composite block description for the flow description of {@code flow-description.xsd}.
 * @author karnokd, 2011.06.21.
 */
public class AdvanceCompositeBlock {
	/** The unique identifier of this block among the current level of blocks. */
	@NonNull
	public String id;
	/** The user-entered documentation of this composite block. */
	@Nullable
	public String documentation;
	/** The user-entered keywords for easier finding of this block. */
	public final List<String> keywords = Lists.newArrayList();
	/** The optional boundary-parameter of this composite block which lets other internal or external blocks bind to this block. */
	public final Map<String, AdvanceCompositeBlockParameterDescription> inputs = Maps.newLinkedHashMap();
	/** The optional boundary parameter of this composite block which lets other internal or external blocks to bind to this block. */
	public final Map<String, AdvanceCompositeBlockParameterDescription> outputs = Maps.newLinkedHashMap();
	/** The optional sub-elements of this block. */
	public final Map<String, AdvanceBlockReference> blocks = Maps.newLinkedHashMap();
	/** An optional composite inner block. */
	public final Map<String, AdvanceCompositeBlock> composites = Maps.newLinkedHashMap();
	/** The binding definition of internal blocks and/or boundary parameters. You may bind the output of the blocks to many input parameters. */
	public final List<AdvanceBlockBind> bindings = Lists.newArrayList();
	/**
	 * Load the contents from an XML element with a schema of <code>flow-description.xsd</code>.
	 * @param root the root element
	 */
	public void load(XElement root) {
		id = root.get("id");
		documentation = root.get("documentation");
		String kw = root.get("keywords");
		if (kw != null) {
			keywords.addAll(Strings.trim(Strings.split(kw, ',')));
		}
		for (XElement e : root.childElement("input")) {
			AdvanceCompositeBlockParameterDescription p = new AdvanceCompositeBlockParameterDescription();
			p.load(e);
			inputs.put(p.displayName, p);
		}
		for (XElement e : root.childElement("output")) {
			AdvanceCompositeBlockParameterDescription p = new AdvanceCompositeBlockParameterDescription();
			p.load(e);
			outputs.put(p.displayName, p);
		}
		for (XElement e : root.childrenWithName("block")) {
			AdvanceBlockReference p = new AdvanceBlockReference();
			p.load(e);
			blocks.put(p.id, p);
		}
		for (XElement e : root.childrenWithName("composite-block")) {
			AdvanceCompositeBlock p = new AdvanceCompositeBlock();
			p.load(e);
			composites.put(p.id, p);
		}
		for (XElement e : root.childrenWithName("bind")) {
			AdvanceBlockBind p = new AdvanceBlockBind();
			p.load(e);
			bindings.add(p);
		}
	}
}
