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

import hu.akarnokd.utils.xml.XNElement;
import hu.akarnokd.utils.xml.XNSerializable;

import java.util.Collection;
import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import eu.advance.logistics.flow.engine.inference.TypeKind;
import eu.advance.logistics.flow.engine.util.Strings;

/**
 * The composite block description for the flow description of {@code flow-description.xsd}.
 * @author akarnokd, 2011.06.21.
 */
public class AdvanceCompositeBlock implements XNSerializable {
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
	protected final Map<String, AdvanceCompositeBlockParameterDescription> inputs = Maps.newLinkedHashMap();
	/** The optional boundary parameter of this composite block which lets other internal or external blocks to bind to this block. */
	protected final Map<String, AdvanceCompositeBlockParameterDescription> outputs = Maps.newLinkedHashMap();
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
	/** The definitions of various generic type parameters. */
	protected final Map<String, AdvanceTypeVariable> typeVariables = Maps.newLinkedHashMap();
	/** The shared type variables. */
	protected final Map<String, AdvanceType> sharedTypeVariables = Maps.newHashMap();
	/**
	 * Load the contents from an XML element with a schema of <code>flow-description.xsd</code> and typed as {@code composite-block}.
	 * @param source the root element
	 */
	@Override
	public void load(XNElement source) {
		id = source.get("id");
		documentation = source.get("documentation");
		String kw = source.get("keywords");
		if (kw != null) {
			keywords.addAll(Strings.trim(Strings.split(kw, ',')));
		}
		Set<String> ids = Sets.newHashSet();
		for (XNElement e : source.children()) {
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
		
		for (XNElement tp : source.childrenWithName("type-variable")) {
			AdvanceTypeVariable bpd = new AdvanceTypeVariable();
			bpd.load(tp);
			if (!addTypeVariable(bpd)) {
				throw new DuplicateIdentifierException(tp.getXPath(), bpd.name);
			}
		}
		linkTypeVariables();

		for (XNElement inp : source.childrenWithName("input")) {
			AdvanceCompositeBlockParameterDescription bpd = new AdvanceCompositeBlockParameterDescription();
			bpd.load(inp);
			if (!addInput(bpd)) {
				throw new DuplicateIdentifierException(inp.getXPath(), bpd.id);
			}
		}
		for (XNElement outp : source.childrenWithName("output")) {
			AdvanceCompositeBlockParameterDescription bpd = new AdvanceCompositeBlockParameterDescription();
			bpd.load(outp);
			if (!addOutput(bpd)) {
				throw new DuplicateIdentifierException(outp.getXPath(), bpd.id);
			}
		}
		
	}
	@Override
	public void save(XNElement destination) {
		destination.set("id", id);
		destination.set("documentation", documentation);
		if (keywords.size() > 0) {
			destination.set("keywords", Strings.join(keywords, ","));
		} else {
			destination.set("keywords", null);
		}
		
		// store content
		for (AdvanceTypeVariable tv : typeVariables.values()) {
			tv.save(destination.add("type-variable"));
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
	 * Parse a {@code flow-description.xsd} based XML.
	 * @param flow the flow XML tree
	 * @return the composite block
	 */
	public static AdvanceCompositeBlock parseFlow(XNElement flow) {
		AdvanceCompositeBlock result = new AdvanceCompositeBlock();
		XNElement x = flow.childElement("composite-block");
		if (x != null) {
			result.load(x);
		}
		return result;
	}
	/**
	 * Serialize the given composite block into a full {@code flow-description.xsd} based XML.
	 * @param root the root composite block.
	 * @return the serialized XML
	 */
	public static XNElement serializeFlow(AdvanceCompositeBlock root) {
		XNElement result = new XNElement("flow-description");
		root.save(result.add("composite-block"));
		return result;
	}
	/**
	 * @return serialize this composite block into a full a full {@code flow-description.xsd} based XML.
	 */
	public XNElement serializeFlow() {
		return serializeFlow(this);
	}
	/**
	 * Removes the input parameter with the given identifier.
	 * @param name the input name identifier
	 */
	public void removeInput(String name) {
		inputs.remove(name);
	}
	/**
	 * Removes the output parameter with the given identifier.
	 * @param name the input name identifier
	 */
	public void removeOutput(String name) {
		outputs.remove(name);
	}
	/**
	 * Test if the given input parameter exists in this composite.
	 * @param name the input name
	 * @return true if exists
	 */
	public boolean hasInput(String name) {
		return inputs.containsKey(name);
	}
	/**
	 * Returns the given input parameter.
	 * @param name the input name
	 * @return the input parameter description or null if not present
	 */
	@Nullable
	public AdvanceCompositeBlockParameterDescription getInput(String name) {
		return inputs.get(name);
	}
	/**
	 * Test if the given output parameter exists in this composite.
	 * @param name the output name
	 * @return true if exists
	 */
	public boolean hasOutput(String name) {
		return outputs.containsKey(name);
	}
	/**
	 * Returns the given output parameter.
	 * @param name the output name
	 * @return the output parameter description or null if not present
	 */
	@Nullable
	public AdvanceCompositeBlockParameterDescription getOutput(String name) {
		return outputs.get(name);
	}
	/**
	 * @return the collection of all inputs
	 */
	public Collection<AdvanceCompositeBlockParameterDescription> inputs() {
		return inputs.values();
	}
	/**
	 * @return the collection of all outputs
	 */
	public Collection<AdvanceCompositeBlockParameterDescription> outputs() {
		return outputs.values();
	}
	/**
	 * Add the given parameter description as an input parameter.
	 * @param p the description
	 * @return true if the parameter was new
	 */
	public boolean addInput(@NonNull AdvanceCompositeBlockParameterDescription p) {
		if (inputs.containsKey(p.id)) {
			return false;
		}
		inputs.put(p.id, p);

		linkParameterType(p);
		
		return true;
	}
	/**
	 * Add the given parameter description as a output parameter.
	 * @param p the description
	 * @return true if the parameter was new
	 */
	public boolean addOutput(AdvanceCompositeBlockParameterDescription p) {
		if (outputs.containsKey(p.id)) {
			return false;
		}
		outputs.put(p.id, p);

		linkParameterType(p);

		return true;
	}
	/**
	 * Adds a type variable to this composite.
	 * @param tv the type variable
	 * @return true if it was added
	 */
	public boolean addTypeVariable(AdvanceTypeVariable tv) {
		// report duplication
		if (typeVariables.containsKey(tv.name)) {
			return false;
		}
		
		AdvanceType t = new AdvanceType();
		t.typeVariable = tv;
		t.typeVariableName = tv.name;

		addSharedTypeVariable(tv.name, t);
		
		return true;
	}
	/**
	 * Adds a shared type variable and type.
	 * @param name the variable name
	 * @param t the type, if null, an IllegalArgumentException is thrown
	 */
	protected void addSharedTypeVariable(String name, AdvanceType t) {
		if (t == null) {
			throw new IllegalArgumentException("AdvanceType " + name + " is null");
		}
		// create the type variable if not specified
		if (t.typeVariable == null) {
			t.typeVariable = new AdvanceTypeVariable();
			t.typeVariable.name = name;
		}
		
		typeVariables.put(name, t.typeVariable);
		sharedTypeVariables.put(name, t);
	}
	/**
	 * Establish a direct link between type variables referencing other type variables.
	 */
	protected void linkTypeVariables() {
		Set<AdvanceType> visited = Sets.newHashSet();
		Deque<List<AdvanceType>> queue = Lists.newLinkedList();
		for (AdvanceTypeVariable tv : typeVariables.values()) {
			queue.add(tv.bounds);
		}
		while (!queue.isEmpty()) {
			List<AdvanceType> bounds = queue.removeFirst();
			for (int i = 0; i < bounds.size(); i++) {
				AdvanceType t = bounds.get(i);
				if (t.kind() == TypeKind.VARIABLE_TYPE) {
					AdvanceType st = sharedTypeVariables.get(t.typeVariableName);
					if (st == null) {
						throw new MissingTypeVariableException("", t.typeVariableName);
					}
					bounds.set(i, st);
				} else
				if (t.kind() == TypeKind.PARAMETRIC_TYPE) {
					if (visited.add(t)) {
						queue.add(t.typeArguments);
					}
				}
			}
		}
	}
	/**
	 * Remove any unused type variable.
	 */
	public void removeUnusedTypeVariables() {
		Set<AdvanceType> visited = Sets.newHashSet();
		Set<String> used = Sets.newHashSet();
		Deque<AdvanceType> queue = Lists.newLinkedList();
		for (AdvanceCompositeBlockParameterDescription p : inputs()) {
			queue.add(p.type);
		}
		for (AdvanceCompositeBlockParameterDescription p : outputs()) {
			queue.add(p.type);
		}
		while (!queue.isEmpty()) {
			AdvanceType t = queue.removeFirst();
			if (t.kind() == TypeKind.VARIABLE_TYPE) {
				used.add(t.typeVariableName);
			} else
			if (t.kind() == TypeKind.PARAMETRIC_TYPE) {
				if (visited.add(t)) {
					queue.addAll(t.typeArguments);
				}
			}
		}
	}
	/**
	 * Link the variable types in the parameter to the common types
	 * and create type variables as necessary.
	 * @param p the parameter
	 */
	protected void linkParameterType(AdvanceCompositeBlockParameterDescription p) {
		if (p.type.kind() == TypeKind.VARIABLE_TYPE) {
			AdvanceType t = sharedTypeVariables.get(p.type.typeVariableName);
			if (t == null) {
				addSharedTypeVariable(p.type.typeVariableName, p.type);
			} else {
				p.type = t;
			}
		} else
		if (p.type.kind() == TypeKind.PARAMETRIC_TYPE) {
			Set<AdvanceType> visited = Sets.newHashSet();
			Deque<List<AdvanceType>> queue = Lists.newLinkedList();
			queue.add(p.type.typeArguments);
			while (!queue.isEmpty()) {
				List<AdvanceType> args = queue.removeFirst();
				for (int i = 0; i < args.size(); i++) {
					AdvanceType t = args.get(i);
					if (t.kind() == TypeKind.VARIABLE_TYPE) {
						AdvanceType st = sharedTypeVariables.get(t.typeVariableName);
						if (st == null) {
							addSharedTypeVariable(t.typeVariableName, t);
						} else {
							args.set(i, st);
						}
					} else
					if (t.kind() == TypeKind.PARAMETRIC_TYPE) {
						if (visited.add(t)) {
							queue.add(t.typeArguments);
						}
					}
				}
			}
		}
	}
}
