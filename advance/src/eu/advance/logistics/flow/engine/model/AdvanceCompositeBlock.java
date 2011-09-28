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

package eu.advance.logistics.flow.engine.model;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import eu.advance.logistics.flow.engine.util.Strings;
import eu.advance.logistics.flow.engine.xml.typesystem.XElement;

/**
 * The composite block description for the flow description of {@code flow-description.xsd}.
 * @author karnokd, 2011.06.21.
 */
public class AdvanceCompositeBlock implements XSerializable {
	/** The unique identifier of this block among the current level of blocks. */
	@NonNull
	public String id;
	/** The user-entered documentation of this composite block. */
	@Nullable
	public String documentation;
	/** The parent block of this composite block. */
	public AdvanceCompositeBlock parent;
	/** The user-entered keywords for easier finding of this block. */
	public final List<String> keywords = Lists.newArrayList();
	/** The optional boundary-parameter of this composite block which lets other internal or external blocks bind to this block. */
	public final Map<String, AdvanceCompositeBlockParameterDescription> inputs = Maps.newLinkedHashMap();
	/** The optional boundary parameter of this composite block which lets other internal or external blocks to bind to this block. */
	public final Map<String, AdvanceCompositeBlockParameterDescription> outputs = Maps.newLinkedHashMap();
	/** The optional sub-elements of this block. */
	public final Map<String, AdvanceBlockReference> blocks = Maps.newLinkedHashMap();
	/** Optional composite inner block. */
	public final Map<String, AdvanceCompositeBlock> composites = Maps.newLinkedHashMap();
	/** Optional constant inner block. */
	public final Map<String, AdvanceConstantBlock> constants = Maps.newLinkedHashMap();
	/** The binding definition of internal blocks and/or boundary parameters. You may bind the output of the blocks to many input parameters. */
	public final List<AdvanceBlockBind> bindings = Lists.newArrayList();
	/** The visual properties for the Flow Editor. */
	public final AdvanceBlockVisuals visuals = new AdvanceBlockVisuals();
	/**
	 * Load the contents from an XML element with a schema of <code>flow-description.xsd</code> and typed as {@code composite-block}.
	 * @param source the root element
	 */
	@Override
	public void load(XElement source) {
		id = source.get("id");
		documentation = source.get("documentation");
		String kw = source.get("keywords");
		if (kw != null) {
			keywords.addAll(Strings.trim(Strings.split(kw, ',')));
		}
		Set<String> ids = Sets.newHashSet();
		for (XElement e : source.children()) {
			if (e.name.equals("input")) {
				AdvanceCompositeBlockParameterDescription p = new AdvanceCompositeBlockParameterDescription();
				p.load(e);
				if (inputs.put(p.id, p) != null) {
					throw new DuplicateIdentifierException(e.getXPath(), p.id);
				}
			} else
			if (e.name.equals("output")) {
				AdvanceCompositeBlockParameterDescription p = new AdvanceCompositeBlockParameterDescription();
				p.load(e);
				if (outputs.put(p.id, p) != null) {
					throw new DuplicateIdentifierException(e.getXPath(), p.id);
				}
			} else
			if (e.name.equals("block")) {
				AdvanceBlockReference p = new AdvanceBlockReference();
				p.load(e);
				p.parent = this;
				if (blocks.put(p.id, p) != null || !ids.add(p.id)) {
					throw new DuplicateIdentifierException(e.getXPath(), p.id);
				}
			} else
			if (e.name.equals("composite-block")) {
				AdvanceCompositeBlock p = new AdvanceCompositeBlock();
				p.load(e);
				p.parent = this;
				if (composites.put(p.id, p) != null || !ids.add(p.id)) {
					throw new DuplicateIdentifierException(e.getXPath(), p.id);
				}
			} else
			if (e.name.equals("constant")) {
				AdvanceConstantBlock p = new AdvanceConstantBlock();
				p.load(e);
				if (constants.put(p.id, p) != null || !ids.add(p.id)) {
					throw new DuplicateIdentifierException(e.getXPath(), p.id);
				}
			} else
			if (e.name.equals("bind")) {
				AdvanceBlockBind p = new AdvanceBlockBind();
				p.parent = this;
				p.load(e);
				bindings.add(p);
			}
		}
		visuals.load(source);
	}
	@Override
	public void save(XElement destination) {
		destination.set("id", id);
		destination.set("documentation", documentation);
		if (keywords.size() > 0) {
			destination.set("keywords", Strings.join(keywords, ","));
		} else {
			destination.set("keywords", null);
		}
		for (AdvanceCompositeBlockParameterDescription item : inputs.values()) {
			item.save(destination.add("input"));
		}
		for (AdvanceCompositeBlockParameterDescription item : outputs.values()) {
			item.save(destination.add("output"));
		}
		for (AdvanceBlockReference item : blocks.values()) {
			item.save(destination.add("block"));
		}
		for (AdvanceCompositeBlock item : composites.values()) {
			item.save(destination.add("composite-block"));
		}
		for (AdvanceConstantBlock item : constants.values()) {
			item.save(destination.add("constant"));
		}
		for (AdvanceBlockBind item : bindings) {
			item.save(destination.add("bind"));
		}
		visuals.save(destination);
	}
	/**
	 * Check if the given binding exists.
	 * @param srcBlock the source block or "" if it is the composite
	 * @param srcParam the source parameter
	 * @param dstBlock the destination block or "" if it is the composite
	 * @param dstParam the destination parameter.
	 * @return true if the binding is present
	 */
	public boolean hasBinding(String srcBlock, String srcParam, String dstBlock, String dstParam) {
		for (AdvanceBlockBind bb : bindings) {
			if (bb.sourceBlock.equals(srcBlock) 
					&& bb.sourceParameter.equals(srcParam)
					&& bb.destinationBlock.equals(dstBlock)
					&& bb.destinationParameter.equals(dstParam)
			) {
				return true;
			}
		}
		return false;
	}
	/**
	 * Parse a {@code flow-descriptor.xsd} based XML.
	 * @param flow the flow XML tree
	 * @return the composite block
	 */
	public static AdvanceCompositeBlock parseFlow(XElement flow) {
		AdvanceCompositeBlock result = new AdvanceCompositeBlock();
		result.load(flow.childElement("composite-block"));
		return result;
	}
	/**
	 * Serialize the given composite block into a full {@code flow-descriptor.xsd} based XML.
	 * @param root the root composite block.
	 * @return the serialized XML
	 */
	public static XElement serializeFlow(AdvanceCompositeBlock root) {
		XElement result = new XElement("flow-descriptor");
		root.save(result.add("composite-block"));
		return result;
	}
}
