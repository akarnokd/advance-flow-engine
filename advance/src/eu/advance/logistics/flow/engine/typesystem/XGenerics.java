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

package eu.advance.logistics.flow.engine.typesystem;

import hu.akarnokd.reactive4java.base.Func1;
import hu.akarnokd.reactive4java.base.Pair;
import hu.akarnokd.utils.xml.XNElement;

import java.io.File;
import java.io.IOException;
import java.util.Deque;
import java.util.Map;
import java.util.Set;

import javax.xml.stream.XMLStreamException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Objects;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import edu.umd.cs.findbugs.annotations.NonNull;

/**
 * A schema parser which can work with generic type definitions of an XSD schemas
 * encoded in {@code annotation/app-info/advance-type-info} nodes. 
 * @author akarnokd, 2011.11.15.
 */
public class XGenerics {
	/** The logger. */
	protected static final Logger LOG = LoggerFactory.getLogger(XSchema.class);
	/** The external schema resolver. */
	protected final Func1<String, XNElement> resolver;
	/** Maps the schema file to map of type definitions. */
	protected final Map<String, Map<String, XNElement>> types = Maps.newLinkedHashMap();
	/** The schema prefix. */
	protected String prefix;
	/**
	 * The stack for recursive types.
	 */
	protected Deque<XGenericBaseType> path = Lists.newLinkedList();
	/**
	 * Constructor.
	 * @param resolver the resolver
	 */
	public XGenerics(Func1<String, XNElement> resolver) {
		this.resolver = resolver;
	}
	/**
	 * Parse the given schema XML.
	 * @param schema the schema to parse
	 * @return the generic type representing the schema
	 */
	public XGenericBaseType parse(XNElement schema) {
		path.clear();
		types.clear();
		prefix = schema.prefix;
		loadIncludes(schema);
		
		XGenericBaseType result = new XGenericBaseType();
		
		for (XNElement e : schema.children()) {
			
			if (e.name.equals("element") && XNElement.XSD.equals(e.namespace)) {
				parseElement(e, result);
				break;
			} else
			if (e.name.equals("complexType") && XNElement.XSD.equals(e.namespace)) {
				parseComplexType(e, result);
				break;
			}
		}
		
		return result;
	}
	/**
	 * Assign the simple type to the target capability, considering the decimal-integer
	 * and anyType corner cases.
	 * @param capability  the target capability
	 * @param xsdType the type as defined in the XSD (e.g., with the prefix).
	 * @param restriction the optional restriction
	 */
	void assignSimpleType(XGenericCapability capability, String xsdType, XNElement restriction) {
		if (xsdType.equals(prefix + ":anyType")) {
			capability.complexType(new XGenericBaseType());
		} else {
			capability.valueType = XSchema.getSimpleType(prefix, xsdType);
			if (capability.valueType != null) {
				if (restriction != null) {
					fixDecimalType(restriction, capability);
				}
			} else {
				throw new IllegalArgumentException("Can't assign simple type: " + xsdType);
			}
		}
	}
	/**
	 * Parse an element definition.
	 * @param element the element
	 * @param out the output
	 */
	void parseElement(XNElement element, XGenericBaseType out) {
		XNElement elementDef = element;
		String ref = elementDef.get("ref");
		if (ref != null) {
			elementDef = findTypeDef(ref);
			if (elementDef == null) {
				throw new IllegalArgumentException("Missing element definition: " + element);
			}
		}
		XGenericCapability capability = new XGenericCapability();
		capability.name.name = elementDef.get("name");
		capability.cardinality = XSchema.getCardinality(elementDef);
		out.add(capability);
		
		// TODO generic type arguments
		XNElement typeAnnotation = getGenericTypeInfo(elementDef);
		if (typeAnnotation != null) {
			parseGenericTypes(capability, typeAnnotation);
		}
		
		String type = elementDef.get("type");
		if (type != null) {
			if (type.startsWith(prefix + ":")) {
				assignSimpleType(capability, type, null);
			} else {
				XNElement typeDef = findTypeDef(type);
				if (typeDef == null) {
					throw new IllegalArgumentException("Missing referenced element type: " + elementDef);
				}
				if (typeDef.hasName("simpleType", XNElement.XSD)) {
					parseSimpleType(typeDef, capability);
				} else
				if (typeDef.hasName("complexType", XNElement.XSD)) {
					capability.complexType(new XGenericBaseType());
					
					path.addLast(capability.complexType());
					parseComplexType(typeDef, capability.complexType());
					path.removeLast();
				} else {
					throw new IllegalArgumentException("Unsupported referenced element type: " + typeDef + "\r\n\r\n" + elementDef);
				}
			}
		} else {
			// local definition
			XNElement simpleType = elementDef.childElement("simpleType", XNElement.XSD);
			XNElement complexType = elementDef.childElement("complexType", XNElement.XSD);
			if (simpleType != null) {
				parseSimpleType(simpleType, capability);
			} else
			if (complexType != null) {
				capability.complexType(new XGenericBaseType());
				
				path.addLast(capability.complexType());
				parseComplexType(complexType, capability.complexType());
				path.removeLast();
			} else {
				capability.complexType(new XGenericBaseType());
			}
		}
		
	}
	/**
	 * Parse and fill in the generic types from the type annotation into a capability.
	 * @param capability the target capability
	 * @param typeAnnotation the type {@code advance-type-info} element.
	 */
	public void parseGenericTypes(XGenericCapability capability,
			XNElement typeAnnotation) {
		for (XNElement ta : typeAnnotation.childrenWithName("type")) {
			XGenericType gt = new XGenericType();
			gt.load(ta);
			capability.arguments.add(gt);
		}
	}
	/**
	 * Parse a complex type.
	 * @param element the type definition
	 * @param out the output of the type
	 */
	void parseComplexType(XNElement element, XGenericBaseType out) {
		parseTypeVariables(element, out);
		// one of the following: simpleContent, complexContent, group all, choice, sequence
		// then any of the following: attribute, attributeGroup
		
		for (XNElement e : element.children()) {
			if (e.hasName("simpleContent", XNElement.XSD)) {
				parseSimpleContent(e, out);
			} else
			if (e.hasName("complexContent", XNElement.XSD)) {
				parseComplexContent(e, out);
			}
		}
		parseComplexTypeBody(element, out);
	}
	/**
	 * Parse the body of a {@code complexType} definition or an {@code complexContent/extension}.
	 * @param element the outer definition element.
	 * @param out the output for the types
	 */
	void parseComplexTypeBody(XNElement element, XGenericBaseType out) {
		for (XNElement e : element.children()) {
			if (e.hasName("group", XNElement.XSD)) {
				// @ref to call in a group
				parseGroup(e, out);
			} else
			if (e.hasName("all", XNElement.XSD)) {
				// element*
				parseComposite(e, out);
			} else
			if (e.hasName("choice", XNElement.XSD)) {
				// element*, group*, choice*, sequence, any
				parseComposite(e, out);
			} else
			if (e.hasName("sequence", XNElement.XSD)) {
				// element*, group*, choice*, sequence, any
				parseComposite(e, out);
			} else
			if (e.hasName("attribute", XNElement.XSD)) {
				XGenericCapability attributeCapability = new XGenericCapability();
				parseAttribute(e, attributeCapability);
				out.add(attributeCapability);
			} else
			if (e.hasName("attributeGroup", XNElement.XSD)) {
				parseAttributeGroup(e, out);
			} else
			if (e.hasName("anyAttribute", XNElement.XSD)) {
				// any attribute ignored 
				continue;
			}
		}
	}
	/**
	 * Parse a group definition.
	 * <p>The model is {@code all | choice | sequence}.</p>
	 * @param group the group definition
	 * @param out the output for types
	 */
	void parseGroup(XNElement group, XGenericBaseType out) {
		XNElement groupDef = group;
		String ref = group.get("ref");
		if (ref != null) {
			groupDef = findTypeDef(ref);
			if (groupDef == null) {
				throw new IllegalArgumentException("Missing group: " + group);
			}
		}
		for (XNElement c : groupDef.children()) {
			if (c.hasName("all", XNElement.XSD)) {
				// restrict children cardinality to max 1
				XGenericBaseType type = new XGenericBaseType();
				parseComposite(c, type);
				for (XGenericCapability cap : type.capabilities()) {
					if (cap.cardinality == XCardinality.ZERO_OR_MANY) {
						cap.cardinality = XCardinality.ZERO_OR_ONE;
					} else
					if (cap.cardinality == XCardinality.ONE_OR_MANY) {
						cap.cardinality = XCardinality.ONE;
					}
					
					out.add(cap);
				}
				break;
			} else
			if (c.hasName("choice", XNElement.XSD)) {
				parseComposite(c, out);
				break;
			} else
			if (c.hasName("sequence", XNElement.XSD)) {
				parseComposite(c, out);
				break;
			}
		}
	}
	/**
	 * Parse a composite with model {@code element*, group*, choice*, sequence, any}.
	 * @param parent the parent composite
	 * @param out the output type
	 */
	void parseComposite(XNElement parent, XGenericBaseType out) {
		for (XNElement c : parent.children()) {
			// skip annotation an any
			if (c.hasName("annotation", XNElement.XSD)
					|| c.hasName("any", XNElement.XSD)) {
				continue;
			} else
			if (c.hasName("element", XNElement.XSD)) {
				// element*
				parseElement(c, out);
				continue;
			}
			XGenericCapability cap = new XGenericCapability();
			cap.cardinality = XSchema.getCardinality(c);
			if (c.hasName("group", XNElement.XSD)) {
				// @ref to call in a group
				parseGroup(c, out);
			} else
			if (c.hasName("choice", XNElement.XSD)) {
				// element*, group*, choice*, sequence, any
				cap.complexType(new XGenericBaseType());
				path.addLast(cap.complexType());
				parseComposite(c, cap.complexType());
				path.removeLast();
			} else
			if (c.hasName("sequence", XNElement.XSD)) {
				// element*, group*, choice*, sequence, any
				cap.complexType(new XGenericBaseType());
				path.addLast(cap.complexType());
				parseComposite(c, out);
				path.removeLast();
			}
			out.add(cap);
		}
	}
	/**
	 * Parse the complex content.
	 * @param complexContent the complex content element
	 * @param out the output
	 */
	void parseComplexContent(XNElement complexContent, XGenericBaseType out) {
		XNElement restriction = complexContent.childElement("restriction", XNElement.XSD);
		XNElement extension = complexContent.childElement("extension", XNElement.XSD);
		if ((restriction != null) == (extension != null)) {
			throw new IllegalArgumentException("Can't have both restriction and extensions missing or present at the same time: " + complexContent);
		}
		if (extension != null) {
			XGenericBaseType baseType = new XGenericBaseType();

			String base = extension.get("base");
			if (!base.equals(prefix + ":anyType")) {
				XNElement baseTypeDef = findTypeDef(base);
				if (baseTypeDef == null) {
					throw new IllegalArgumentException("Missing extension base " + complexContent);
				}
				parseComplexType(baseTypeDef, baseType);
			}
			
			parseComplexTypeBody(extension, baseType);
			
			out.add(baseType.capabilities());
			
			XNElement typeAnnotations = getGenericTypeInfo(extension);
			if (typeAnnotations != null) {
				XGenericCapability helper = new XGenericCapability();
				parseGenericTypes(helper, typeAnnotations);
				if (helper.arguments.size() != baseType.variables.size()) {
					throw new IllegalArgumentException("The usage argument count != baseType argument count:\r\n" + typeAnnotations + "\r\n" + baseType.variables);
				}
				// TODO add the type arguments somewhere!
			}
		} else
		if (restriction != null) {
			String base = restriction.get("base");
			
			XGenericBaseType baseType = new XGenericBaseType();
			XNElement baseTypeDef = findTypeDef(base);
			if (baseTypeDef == null) {
				throw new IllegalArgumentException("Missing restriction base " + complexContent);
			}
			
			parseComplexType(baseTypeDef, baseType);

			XGenericBaseType restrictions = new XGenericBaseType();
			parseComplexTypeBody(restriction, restrictions);

			// remove elements and attributes from baseType which are not present in restrictions
			XGenericBaseType newBaseType = new XGenericBaseType();
			for (XGenericCapability baseCap : baseType.capabilities()) {
				for (XGenericCapability restricCap : restrictions.capabilities()) {
					if (Objects.equal(restricCap.name, baseCap.name)) {
						// override properties
						baseCap.cardinality = restricCap.cardinality;
						baseCap.valueType = restricCap.valueType;
						baseCap.complexType(restricCap.complexType());
						baseCap.arguments.clear();
						baseCap.arguments.addAll(restricCap.arguments);
						newBaseType.add(baseCap);
						break;
					}
				}
			}
			out.add(newBaseType.capabilities());
			out.variables.addAll(baseType.variables);
		}
	}
	/**
	 * Parse the simple content.
	 * @param simpleContent the simple content element
	 * @param out the output
	 */
	void parseSimpleContent(XNElement simpleContent, XGenericBaseType out) {
		XNElement restriction = simpleContent.childElement("restriction", XNElement.XSD);
		XNElement extension = simpleContent.childElement("extension", XNElement.XSD);
		if ((restriction != null) == (extension != null)) {
			throw new IllegalArgumentException("Can't have both restriction and extensions missing or present at the same time: " + simpleContent);
		}
		if (extension != null) {
			String base = extension.get("base");
			// take the given type and extend it with attributes
			XGenericCapability capability = new XGenericCapability();
			capability.cardinality = XCardinality.ONE;
			if (base.startsWith(prefix + ":")) {
				assignSimpleType(capability, base, null);
			} else {
				XNElement simpleTypeDef = findTypeDef(base);
				if (simpleTypeDef != null) {
					parseSimpleType(simpleTypeDef, capability);
				} else {
					throw new IllegalArgumentException("Referenced simple type missing or unsupported:" + extension);
				}
			}
			out.add(capability);
			for (XNElement attributes : extension.children()) {
				if (attributes.hasName("attribute", XNElement.XSD)) {
					XGenericCapability attributeCapability = new XGenericCapability();
					parseAttribute(attributes, attributeCapability);
					out.add(attributeCapability);
				} else
				if (attributes.hasName("attributeGroup", XNElement.XSD)) {
					parseAttributeGroup(attributes, out);
				}
			}
		} else
		if (restriction != null) {
			String base = restriction.get("base");
			XGenericCapability capability = new XGenericCapability();
			capability.cardinality = XCardinality.ONE;
			if (base.startsWith(prefix)) {
				assignSimpleType(capability, base, restriction);
			} else {
				// TODO support
				throw new IllegalArgumentException("Restriction with non-base types not supported:" + restriction);
			}
			out.add(capability);
		}
	}
	/**
	 * Changes the capability type from REAL to INT in case the given restriction element
	 * lists a {@code fractionDigits} restriction with {@code value} of 0.
	 * @param restriction the {@code xsd:restriction} element.
	 * @param capability the target capability
	 */
	public void fixDecimalType(@NonNull XNElement restriction,
			@NonNull XGenericCapability capability) {
		if (capability.valueType == XValueType.REAL) {
			XNElement fractionDigits = restriction.childElement("fractionDigits", XNElement.XSD);
			if (fractionDigits != null) {
				if (fractionDigits.getInt("value") == 0) {
					capability.valueType = XValueType.INTEGER;
				}
			}
		}
	}
	/**
	 * Parse an attribute definition.
	 * @param attribute the attribute definition
	 * @param capability the capability for the output
	 */
	void parseAttribute(XNElement attribute, XGenericCapability capability) {
		XNElement attributeDef = attribute;
		String ref = attributeDef.get("ref");
		if (ref != null) {
			attributeDef = findTypeDef(ref);
			if (attributeDef == null) {
				throw new IllegalArgumentException("Missing referenced attribute " + attribute);
			}
		}
		capability.name.name = attributeDef.get("name");
		String use = attributeDef.get("use");
		if (use == null || "optional".equals(use)) {
			capability.cardinality = XCardinality.ZERO_OR_ONE;
		} else
		if ("required".equals(use)) {
			capability.cardinality = XCardinality.ONE;
		} else {
			capability.cardinality = XCardinality.ZERO;
		}
		String typeRef = attributeDef.get("type");
		if (typeRef != null) {
			if (typeRef.startsWith(prefix + ":")) {
				assignSimpleType(capability, typeRef, null);
			} else {
				XNElement simpleType = findTypeDef(typeRef);
				if (simpleType == null) {
					throw new IllegalArgumentException("Missing simple type " + attributeDef);
				}
				parseSimpleType(simpleType, capability);
			}
		} else {
			// local definition
			XNElement simpleType = attributeDef.childElement("simpleType", XNElement.XSD);
			if (simpleType != null) {
				parseSimpleType(simpleType, capability);
			} else {
				// missing, this attribute is of empty type now
				capability.complexType(new XGenericBaseType());
			}
		}
	}
	/**
	 * Parse the attribute group and place each individual attribute in the {@code out} base type.
	 * @param attributeGroup the definition
	 * @param out the output type
	 */
	void parseAttributeGroup(XNElement attributeGroup, XGenericBaseType out) {
		XNElement attributeGroupDef = attributeGroup;
		String ref = attributeGroupDef.get("ref");
		if (ref != null) {
			attributeGroupDef = findTypeDef(ref);
			if (attributeGroupDef == null) {
				throw new IllegalArgumentException("Missing attribute group: " + attributeGroup);
			}
		}
		for (XNElement e : attributeGroupDef.children()) {
			if (e.hasName("attribute", XNElement.XSD)) {
				XGenericCapability cap = new XGenericCapability();
				parseAttribute(e, cap);
				out.add(cap);
			} else
			if (e.hasName("attributeGroup", XNElement.XSD)) {
				parseAttributeGroup(e, out);
			}
		}
	}
	/**
	 * Parse a simple type and place its properties in the capability.
	 * @param simpleType the simple type definition
	 * @param capability the output
	 */
	void parseSimpleType(XNElement simpleType, XGenericCapability capability) {
		XNElement restrict = simpleType.childElement("restriction", XNElement.XSD);
		XNElement list = simpleType.childElement("list", XNElement.XSD);
		XNElement union = simpleType.childElement("union", XNElement.XSD);
		if (restrict != null) {
			String base = restrict.get("base");
			if (base.startsWith(prefix + ":")) {
				assignSimpleType(capability, base, restrict);
			} else {
				XNElement ref = findTypeDef(base);
				if (ref == null) {
					throw new IllegalArgumentException("Missing referenced simple type: " + simpleType);
				}
				parseSimpleType(ref, capability);
			}
		} else
		if (list != null) {
			capability.complexType(new XGenericBaseType());
			XGenericCapability cap = new XGenericCapability();
			cap.cardinality = XCardinality.ZERO_OR_MANY;
			capability.complexType().add(cap);
			
			String itemType = list.get("itemType");
			if (itemType != null) {
				if (itemType.startsWith(prefix + ":")) {
					assignSimpleType(cap, itemType, restrict);
				} else {
					XNElement ref = findTypeDef(itemType);
					if (ref == null) {
						throw new IllegalArgumentException("Missing referenced simple type: " + simpleType);
					}
					parseSimpleType(ref, cap);
				}
			}
		} else
		if (union != null) {
			capability.complexType(new XGenericBaseType());
			String memberTypes = union.get("memberTypes");
			if (memberTypes != null) {
				for (String type : memberTypes.split("\\s*,\\s*")) {
					XGenericCapability cap = new XGenericCapability();
					cap.cardinality = XCardinality.ONE;
					capability.complexType().add(cap);
					if (type.startsWith(prefix + ":")) { 
						assignSimpleType(cap, type, restrict);
					} else {
						XNElement ref = findTypeDef(type);
						if (ref == null) {
							throw new IllegalArgumentException("Missing referenced simple type: " + simpleType);
						}
						parseSimpleType(ref, cap);
					}
				}
			} else {
				for (XNElement mt : union.children()) {
					if (mt.hasName("simpleType", XNElement.XSD)) {
						XGenericCapability cap = new XGenericCapability();
						cap.cardinality = XCardinality.ONE;
						capability.complexType().add(cap);
						parseSimpleType(mt, cap);
					}
				}
			}
		}
	}
	/**
	 * Parse the type variables from the complex type definition.
	 * @param element the type element
	 * @param out the output for the generic information
	 */
	void parseTypeVariables(XNElement element, XGenericBaseType out) {
		XNElement typeInfo = getGenericTypeInfo(element);
		if (typeInfo != null) {
			for (XNElement tv : typeInfo.childrenWithName("type-variable")) {
				XGenericVariable var = new XGenericVariable();
				var.load(tv);
				out.variables.add(var);
			}
		}
	}
	/**
	 * Load the includes of the schema.
	 * @param schema the schema root
	 */
	void loadIncludes(XNElement schema) {
		Deque<Pair<XNElement, String>> queue = Lists.newLinkedList();
		queue.add(Pair.of(schema, ""));
		Set<String> memory = Sets.newHashSet();
		while (!queue.isEmpty()) {
			Pair<XNElement, String> e = queue.removeFirst();
			setTypes(e.first, e.second);
			
			for (XNElement inc : e.first.childrenWithName("include", XNElement.XSD)) {
				String schemaLoc = inc.get("schemaLocation");
				if (memory.add(schemaLoc)) {
					XNElement incSchema = resolver.invoke(schemaLoc);
					queue.add(Pair.of(incSchema, schemaLoc));
				}
			}
		}
	}
	/**
	 * Locate the type definition with the given name.
	 * @param name the type definition
	 * @return the type found or null
	 */
	XNElement findTypeDef(String name) {
		for (Map<String, XNElement> defs : types.values()) {
			XNElement t = defs.get(name);
			if (t != null) {
				return t;
			}
		}
		return null;
	}
	/**
	 * Set the available global types from the given schema root.
	 * @param schemaRoot the schema root
	 * @param target the target 'namespace'
	 */
	void setTypes(XNElement schemaRoot, String target) {
		for (XNElement e : schemaRoot.children()) {
			add(target, e.get("name"), e);
		}
	}
	/**
	 * Extract the type annotation from the {@code xs:annotation/xs:appinfo/advance-type-info} node.
	 * @param e the base element
	 * @return the type info element or null if not present
	 */
	XNElement getGenericTypeInfo(XNElement e) {
		XNElement annot = e.childElement("annotation", XNElement.XSD);
		if (annot != null) {
			XNElement appInfo = annot.childElement("appinfo", XNElement.XSD);
			if (appInfo != null) {
				return appInfo.childElement("advance-type-info");
			}
		}
		return null;
	}
	/**
	 * Add a type definition.
	 * @param target the target 'namespace'
	 * @param typeName the type name
	 * @param definition the definition
	 */
	public void add(String target, String typeName, XNElement definition) {
		Map<String, XNElement> defs = types.get(target);
		if (defs == null) {
			defs = Maps.newLinkedHashMap();
			types.put(target, defs);
		}
		defs.put(typeName, definition);
	}
	/**
	 * @param args no arguments
	 * @throws Exception ignored
	 */
	public static void main(String[] args) throws Exception {
		Func1<String, XNElement> resolver = new Func1<String, XNElement>() {
			@Override
			public XNElement invoke(String param1) {
				File f = new File("schemas", param1);
				if (f.canRead()) {
					try {
						return XNElement.parseXML(f);
					} catch (XMLStreamException ex) {
						LOG.error(ex.toString(), ex);
					} catch (IOException ex) {
						LOG.error(ex.toString(), ex);
					}
				}
				return null;
			}
		};
		XGenerics xg = new XGenerics(resolver);

		XNElement xsd = XNElement.parseXML(new File("schemas", "map.xsd"));
		System.out.println(xg.parse(xsd));
//		for (URI u : AdvanceData.BASE_TYPES) {
//			try {
//				xsd = XNElement.parseXML(new File("schemas", u.getSchemeSpecificPart() + ".xsd"));
//				System.out.println(xg.parse(xsd));
//			} catch (Throwable t) {
//				t.printStackTrace();
//			}
//		}
	}

}
