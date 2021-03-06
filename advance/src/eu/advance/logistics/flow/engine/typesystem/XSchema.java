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
import hu.akarnokd.utils.xml.XNElement.XAttributeName;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.stream.XMLStreamException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Objects;
import com.google.common.collect.HashMultiset;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multiset;
import com.google.common.collect.Sets;

import edu.umd.cs.findbugs.annotations.Nullable;
import eu.advance.logistics.flow.engine.inference.TypeRelation;


/**
 * A XML type description.
 *  @author akarnokd
 */
public final class XSchema {
	/** The logger. */
	protected static final Logger LOG = LoggerFactory.getLogger(XSchema.class);
	/** Utility class. */
	private XSchema() {
		// utility class
	}
	/**
	 * Create the XML type by parsing the given schema document.
	 * @param schema the schema tree.
	 * @param resolver the function which will return a schema XML for the supplied name
	 * @return the XML type representing the schema
	 */
	public static XType parse(XNElement schema, Func1<String, XNElement> resolver) {
		List<XNElement> roots = Lists.newArrayList(schema.childrenWithName("element", XNElement.XSD));
		if (roots.size() != 1) {
			throw new IllegalArgumentException("Zero or multi-rooted schema not supported:\r\n" + schema);
		}
		XNElement root = roots.get(0);

		
		List<XNElement> typedefs = new ArrayList<XNElement>();

		searchTypes(schema, typedefs, new HashSet<String>(), resolver);
		
		XType result = new XType();
		Map<String, XType> memory = new HashMap<String, XType>();

		setElement(root, typedefs, result, memory);
		
		return result;
	}
	/**
	 * Set the element type.
	 * @param root the element definition
	 * @param typedefs the list of various type definitions
	 * @param result the output type
	 * @param memory the type memory
	 * @return the created capability
	 */
	static XCapability setElement(XNElement root, List<XNElement> typedefs,
			XType result, Map<String, XType> memory) {
		XCapability c = new XCapability();
		c.name = new XName();
		c.name.name = root.get("name");
		// FIXME set semantic token and aliases
		c.cardinality = getCardinality(root);
		
		result.capabilities.add(c);
		
		String rootType = root.get("type");
		if (rootType != null) {
			// check for direct simple type
			XValueType rootSimpleType = getSimpleType(root.prefix, rootType);
			if (rootSimpleType != null) {
				c.valueType = rootSimpleType;
			} else {
				XNElement simpleType = findType(rootType, "simpleType", typedefs);
				if (simpleType != null) {
					setSimpleType(c, simpleType, typedefs);
				} else {
					XNElement complexType = findType(rootType, "complexType", typedefs);
					if (complexType != null) {
						 setComplexType(c, complexType, typedefs, memory);
					} else {
						if (rootType.equals(root.prefix + ":anyType")) {
							c.complexType = new XType(); // empty type
						} else {
							throw new AssertionError("Missing referenced type for element " + root.get("name") + " type " + rootType);
						}
					}
				}
			}
		} else {
			rootType = root.get("ref");
			// TODO implement reference mode! 
			if (rootType == null) {
				// check for local definitions
				XNElement simpleType = root.childElement("simpleType", XNElement.XSD);
				if (simpleType != null) {
					setSimpleType(c, simpleType, typedefs);
				} else {
					XNElement complexType = root.childElement("complexType", XNElement.XSD);
					if (complexType != null) {
						setComplexType(c, complexType, typedefs, memory);
					} else {
						throw new AssertionError("Strange element: " + root.get("name"));
					}
				}
			}
		}
		return c;
	}
	/**
	 * Enumerate the complex type definition and set it to the capability.
	 * @param c the current capability
	 * @param typedef the type definition
	 * @param types the list of other type definitions
	 * @param memory the type memory for already evaluated complex types
	 * @return the created complex type
	 */
	static XType setComplexType(XCapability c, 
			XNElement typedef, List<XNElement> types, Map<String, XType> memory) {
		String typeName = typedef.get("name"); // FIXME named local definitions?
		if (typeName != null) {
			XType t = memory.get(typeName);
			if (t != null) {
				c.complexType = t;
				return c.complexType;
			}
			c.complexType = new XType();
			memory.put(typeName, c.complexType);
		} else {
			c.complexType = new XType();
		}
		
		LinkedList<XNElement> seqs = new LinkedList<XNElement>();
		seqs.addAll(typedef.children());
		while (seqs.size() > 0) {
			XNElement sequence = seqs.removeFirst();
			if (sequence.namespace.equals(XNElement.XSD)) {
				if (sequence.name.equals("element")) {
					setElement(sequence, types, c.complexType, memory);
				} else
				if (sequence.name.equals("sequence")
						|| sequence.name.equals("choice") // FIXME not sure
						|| sequence.name.equals("group") // FIXME not sure
						|| sequence.name.equals("all") // FIXME not sure
				) {
					seqs.addAll(0, sequence.children());
				} else
				if (sequence.name.equals("simpleContent")) {
					XNElement restriction = sequence.childElement("restriction");
					if (restriction != null) {
						XCapability cap = new XCapability();
						cap.name = c.name;
						cap.cardinality = XCardinality.ONE;
						c.complexType.capabilities.add(cap);
						setSimpleRestriction(cap, sequence, types, restriction);
					} else {
						XNElement extension = sequence.childElement("extension");
						if (extension != null) {
							// copy existing definition
							String base = extension.get("base");
							XNElement simpleBase = findType(base, "simpleType", types);
							if (simpleBase != null) {
								XCapability cap = new XCapability();
								cap.name = c.name;
								cap.cardinality = XCardinality.ONE;
								setSimpleType(cap, simpleBase, types);
								c.complexType.capabilities.add(cap);
								setAttributes(c, extension, types, memory);
							} else {
								XNElement complexType = findType(base, "complexType", types);
								if (complexType != null) {
									setComplexType(c, complexType, types, memory);
									setAttributes(c, extension, types, memory);
								} else {
									if (!base.endsWith(":anyType")) {
										throw new AssertionError("Unknown extension base type " + base);
									}
								}
							}
						} else {
							throw new AssertionError("Unknown simple content type");
						}
					}
				} else
				if (sequence.name.equals("complexContent")) {
					XNElement restriction = sequence.childElement("restriction");
					if (restriction != null) {
						XCapability cap = new XCapability();
						cap.name = c.name;
						cap.cardinality = XCardinality.ONE;
						c.complexType.capabilities.add(cap);
						setSimpleRestriction(cap, sequence, types, restriction);
						setComplexType(c, restriction, types, memory);
					} else {
						XNElement extension = sequence.childElement("extension");
						if (extension != null) {
							String base = extension.get("base");
							XNElement simpleBase = findType(base, "simpleType", types);
							if (simpleBase != null) {
								c.complexType = c.complexType.copy();
								
								XCapability cap = new XCapability();
								cap.name = c.name;
								cap.cardinality = XCardinality.ONE;
								setSimpleType(cap, simpleBase, types);
								setComplexType(c, extension, types, memory);

								c.complexType.capabilities.add(cap);
							} else {
								XNElement complexType = findType(base, "complexType", types);
								if (complexType != null) {
									setComplexType(c, complexType, types, memory);
									XType baseCopy = c.complexType.copy();
									setComplexType(c, extension, types, memory);
									c.complexType.capabilities.addAll(0, baseCopy.capabilities);
								} else {
									if (!base.endsWith(":anyType")) {
										throw new AssertionError("Unknown extension base type " + base);
									}
								}
							}
						} else {
							throw new AssertionError("Unknown complexContent content type");
						}
					}					
				}
			}
		}
		setAttributes(c, typedef, types, memory);
		return c.complexType;
	}
	/**
	 * Set the attributes of a complex node.
	 * @param c the parent capability
	 * @param typedef the type definition of the capability
	 * @param types the list of types
	 * @param memory the memory for existing name types
	 */
	static void setAttributes(XCapability c, XNElement typedef,
			List<XNElement> types, Map<String, XType> memory) {
		for (XNElement attr : typedef.childrenWithName("attribute", XNElement.XSD)) {
			XCapability cap = setElement(attr, types, c.complexType, memory);
			String use = attr.get("use");
			if ("forbidden".equals(use)) {
				cap.cardinality = XCardinality.ZERO;
			} else
			if ("required".equals(use)) {
				cap.cardinality = XCardinality.ONE;
			} else {
				// if default is given, then the attribute counts as one
				if (attr.get("default") == null) {
					cap.cardinality = XCardinality.ZERO_OR_ONE;
				} else {
					cap.cardinality = XCardinality.ONE;
				}
			}
		}
		LinkedList<XNElement> attrgr = new LinkedList<XNElement>();
		Iterables.addAll(attrgr, typedef.childrenWithName("attributeGroup", XNElement.XSD));
		while (attrgr.size() > 0) {
			XNElement ag = attrgr.removeFirst();
			String ref = ag.get("ref");
			if (ref != null) {
				ag = findType(ref, "attributeGroup", types);
				if (ag == null) {
					throw new AssertionError("Unknown attribute group: " + ref);
				}
			}
			for (XNElement attr : typedef.childrenWithName("attribute", XNElement.XSD)) {
				setElement(attr, types, c.complexType, memory);
				XCapability cap = setElement(attr, types, c.complexType, memory);
				String use = attr.get("use");
				if ("optional".equals(use)) {
					cap.cardinality = XCardinality.ZERO_OR_ONE;
				} else
				if ("required".equals(use)) {
					cap.cardinality = XCardinality.ONE;
				} else {
					cap.cardinality = XCardinality.ZERO;
				}
			}
			Iterables.addAll(attrgr, ag.childrenWithName("attributeGroup", XNElement.XSD));
		}
	}
	/**
	 * Set the simple type from the given type definition.
	 * @param c the target capability
	 * @param typedef the current type definition
	 * @param types the list of other simple types
	 */
	static void setSimpleType(XCapability c, XNElement typedef, 
			List<XNElement> types) {
		XNElement restrict = typedef.childElement("restriction", XNElement.XSD);
		if (restrict != null) {
			setSimpleRestriction(c, typedef, types, restrict);
		} else {
			XNElement list = typedef.childElement("list", XNElement.XSD);
			if (list != null) {
				
				String itemType = list.get("itemType");
				XValueType primitiveType = getSimpleType(list.prefix, itemType);
				if (primitiveType != null) {
					c.complexType = new XType();
					
					XCapability c1 = new XCapability();
					c1.name = new XName();
					c1.valueType = primitiveType;
					c1.cardinality = XCardinality.ZERO_OR_MANY;
					c.complexType.capabilities.add(c1);
				} else {
					// find among children
					XNElement parent = findType(itemType, "simpleType", list.childrenWithName("simpleType", XNElement.XSD));
					if (parent == null) {
						parent = findType(itemType, "simpleType", types);
					}
					if (parent != null) {
						setSimpleType(c, parent, types);
					} else {
						throw new AssertionError("Could not find a parent single type " + itemType + " for type " + typedef.get("name"));
					}
				}
				
			} else {
				XNElement union = typedef.childElement("union", XNElement.XSD);
				if (union != null) {
					c.valueType = XValueType.STRING;
				} else {
					throw new AssertionError("Strange simpleType : " + typedef.get("name"));
				}
			}
		}
	}
	/**
	 * Set the capability based on a simple restriction type.
	 * @param c the capability
	 * @param typedef the type definition of the holding element
	 * @param types the available global types
	 * @param restrict the restriction element definition
	 */
	static void setSimpleRestriction(XCapability c, XNElement typedef,
			List<XNElement> types, XNElement restrict) {
		String base = restrict.get("base");
		XValueType primitiveType = getSimpleType(restrict.prefix, base);
		if (primitiveType == XValueType.REAL) {
			// if restiction present check for zero fraction
			XNElement frac = restrict.childElement("fractionDigits", XNElement.XSD);
			if (frac != null) {
				if ("0".equals(frac.get("value"))) {
					primitiveType = XValueType.INTEGER;
				}
			}
		}
		if (primitiveType != null) {
			c.valueType = primitiveType;
		} else {
			// find among children
			XNElement parent = findType(base, "simpleType", restrict.childrenWithName("simpleType", XNElement.XSD));
			if (parent == null) {
				parent = findType(base, "simpleType", types);
			}
			if (parent != null) {
				setSimpleType(c, parent, types);
			} else {
				throw new AssertionError("Could not find a parent single type " + base + " for type " + typedef.get("name"));
			}
		}
	}
	/**
	 * Search for type definitions and recursively load other schemas from the URI.
	 * @param root the current schema object
	 * @param typedefs the simple types, complex types and attribute groups
	 * @param memory the memory for already visited resources
	 * @param resolver the function to return a schema for the supplied name
	 */
	static void searchTypes(XNElement root, List<XNElement> typedefs, 
			Set<String> memory, Func1<String, XNElement> resolver) {
		Iterables.addAll(typedefs, root.childrenWithName("simpleType", XNElement.XSD));
		Iterables.addAll(typedefs, root.childrenWithName("complexType", XNElement.XSD));
		Iterables.addAll(typedefs, root.childrenWithName("attributeGroup", XNElement.XSD));
		Iterable<XNElement> includes = root.childrenWithName("include", XNElement.XSD);
		for (XNElement inc : includes) {
			String loc = inc.get("schemaLocation");
			if (loc != null && memory.add(loc)) {
				XNElement in = resolver.invoke(loc);
				if (in != null) {
					searchTypes(in, typedefs, memory, resolver);
				} else {
					throw new RuntimeException("Could not locate schema file for " + loc + " in:\r\n" + root);
				}
			}
		}
	}
	/**
	 * Close the given closeable silently.
	 * @param c the closeable object
	 */
	static void close0(@Nullable Closeable c) {
		if (c != null) {
			try { c.close(); } catch (IOException ex) {  }
		}
	}

	/**
	 * Find a specific type definition by its id.
	 * @param name the type name
	 * @param type the type definition to look for (e.g., simpleType, complexType, attributeGroup)
	 * @param types the list of types
	 * @return the target type definition or null if not found
	 */
	static XNElement findType(String name, String type, Iterable<XNElement> types) {
		for (XNElement e : types) {
			if (Objects.equal(e.get("name"), name) && e.name.equals(type)) {
				return e;
			}
		}
		return null;
	}
	/**
	 * Extract the cardinality value from an elements minOccurs and maxOccurs definition.
	 * @param e the element definition
	 * @return the cardinality
	 */
	public static XCardinality getCardinality(XNElement e) {
		String mino = e.get("minOccurs");
		String maxo = e.get("maxOccurs");
		if (mino == null) {
			mino = "1";
		}
		if (maxo == null) {
			maxo = "1";
		}
		if (mino.equals("0")) {
			if (maxo.equals("0")) {
				return XCardinality.ZERO;
			} else
			if (maxo.equals("1")) {
				return XCardinality.ZERO_OR_ONE;
			} else 
			if (maxo.equals("unbounded")) {
				return XCardinality.ZERO_OR_MANY;
			}
		} else
		if (mino.equals("1")) {
			if (maxo.equals("1")) {
				return XCardinality.ONE;
			} else
			if (maxo.equals("unbounded")) {
				return XCardinality.ONE_OR_MANY;
			}
		}
		throw new AssertionError("Cardinality not supported: " + mino + " .. " + maxo + "\r\n" + e);
	}
	/**
	 * Convert an XSD simple type into an XValueType.
	 * @param prefix the prefix
	 * @param xsdType the type string
	 * @return the value type
	 */
	public static XValueType getSimpleType(String prefix, String xsdType) {
		if (xsdType.equals(prefix + ":dateTime")) {
			return XValueType.TIMESTAMP;
		} else
		if (xsdType.equals(prefix + ":int")
			|| xsdType.equals(prefix + ":integer")
			|| xsdType.equals(prefix + ":nonNegativeInteger")
			|| xsdType.equals(prefix + ":long")
			|| xsdType.equals(prefix + ":short")
			|| xsdType.equals(prefix + ":byte")
			|| xsdType.equals(prefix + ":nonPositiveInteger")
			|| xsdType.equals(prefix + ":positiveInteger")
			|| xsdType.equals(prefix + ":unsignedInt")
			|| xsdType.equals(prefix + ":unsignedLong")
			|| xsdType.equals(prefix + ":unsignedShort")
			|| xsdType.equals(prefix + ":unsignedByte")
			|| xsdType.equals(prefix + ":negativeInteger")
		) {
			return XValueType.INTEGER;
		} else
		if (xsdType.equals(prefix + ":float")
			|| xsdType.equals(prefix + ":decimal")
			|| xsdType.equals(prefix + ":double")
		) {
			return XValueType.REAL;
		} else
		if (xsdType.equals(prefix + ":boolean")) {
			return XValueType.BOOLEAN;
		} else
		if (xsdType.equals(prefix + ":string")
			|| xsdType.equals(prefix + ":anyURI")
			|| xsdType.equals(prefix + ":base64Binary")
			|| xsdType.equals(prefix + ":hexBinary")
			|| xsdType.equals(prefix + ":QName")
			|| xsdType.equals(prefix + ":NOTATION")
			|| xsdType.equals(prefix + ":duration")
			|| xsdType.equals(prefix + ":time")
			|| xsdType.equals(prefix + ":date")
			|| xsdType.equals(prefix + ":gYearMonth")
			|| xsdType.equals(prefix + ":gYear")
			|| xsdType.equals(prefix + ":gMonthDay")
			|| xsdType.equals(prefix + ":gDay")
			|| xsdType.equals(prefix + ":gMonth")
			|| xsdType.equals(prefix + ":normalizedString")
			|| xsdType.equals(prefix + ":token")
			|| xsdType.equals(prefix + ":language")
			|| xsdType.equals(prefix + ":Name")
			|| xsdType.equals(prefix + ":NCName")
			|| xsdType.equals(prefix + ":NMTOKEN")
			|| xsdType.equals(prefix + ":NMTOKENS")
			|| xsdType.equals(prefix + ":ID")
			|| xsdType.equals(prefix + ":IDREF")
			|| xsdType.equals(prefix + ":ENTITY")
			|| xsdType.equals(prefix + ":IDREFS")
			|| xsdType.equals(prefix + ":ENTITIES")
		) {
			return XValueType.STRING;
		}
		return null;
	}
	/**
	 * Convenience method to compare two XTypes through its first capability only.
	 * XSDs are typically parsed into XTypes where the outer XType has only the
	 * root node as a capability. But most of the time, root node capability naming
	 * has nothing to do with its contents and would just cause false comparisons.
	 * @param t1 the first type
	 * @param t2 the second type
	 * @return the relation
	 */
	public static TypeRelation compare(XType t1, XType t2) {
		if (t1.capabilities.size() > 1) {
			throw new IllegalArgumentException("t1 should have only one capability, instead, it has " + t1.capabilities.size());
		}
		if (t2.capabilities.size() > 1) {
			throw new IllegalArgumentException("t2 should have only one capability, instead, it has " + t2.capabilities.size());
		}
		if (t1.capabilities.size() == 0 && t2.capabilities.size() == 0) {
			return TypeRelation.EQUAL;
		} else
		if (t1.capabilities.size() == 0) {
			XType ct2 = t2.capabilities.get(0).complexType;
			if (ct2 != null && ct2.capabilities.size() == 0) {
				return TypeRelation.EQUAL;
			}
			return TypeRelation.SUPER;
		} else
		if (t2.capabilities.size() == 0) {
			XType ct1 = t1.capabilities.get(0).complexType;
			if (ct1 != null && ct1.capabilities.size() == 0) {
				return TypeRelation.EQUAL;
			}
			return TypeRelation.EXTENDS;
		} else
		if ((t1.capabilities.get(0).complexType == null) != (t2.capabilities.get(0).complexType == null)) {
			if (isObject(t1.capabilities.get(0).complexType) && t2.capabilities.get(0).complexType == null) {
				return TypeRelation.SUPER;
			} else
			if (t1.capabilities.get(0).complexType == null && isObject(t2.capabilities.get(0).complexType)) {
				return TypeRelation.EXTENDS;
			}
			return TypeRelation.NONE;
		} else
		if (t1.capabilities.get(0).complexType != null && t2.capabilities.get(0).complexType != null) {
			return t1.capabilities.get(0).complexType.compareTo(t2.capabilities.get(0).complexType);
		} else
		if (t1.capabilities.get(0).valueType == t2.capabilities.get(0).valueType) {
			return TypeRelation.EQUAL;
		} else
		if (t1.capabilities.get(0).valueType == XValueType.REAL &&  t2.capabilities.get(0).valueType == XValueType.INTEGER) {
			return TypeRelation.SUPER;
		} else
		if (t1.capabilities.get(0).valueType == XValueType.INTEGER && t2.capabilities.get(0).valueType == XValueType.REAL) {
			return TypeRelation.EXTENDS;
		}
		return TypeRelation.NONE;
	}
	/**
	 * Check if the complex type represents a top-type (e.g., object).
	 * @param complexType the complex type
	 * @return true if represents an object
	 */
	protected static boolean isObject(XType complexType) {
		return complexType != null && complexType.capabilities.isEmpty();
	}
	/**
	 * Infer and generate an xml type from the given XML instance based on its structure.
	 * @param xmlRoot the XML instance root
	 * @return the associated type
	 */
	public static XType fromInstance(XNElement xmlRoot) {
		XType result = new XType();
		
		XCapability rcap = new XCapability();
		rcap.name = new XName();
		rcap.name.name = xmlRoot.name;
		rcap.name.semantics = new UriSemantics(xmlRoot.namespace);
		rcap.cardinality = XCardinality.ONE;
		
		result.capabilities.add(rcap);
		
		if (!xmlRoot.hasAttributes() && !xmlRoot.hasChildren()) {
			rcap.valueType = inferValueType(xmlRoot.content);			
		} else {
			XType rootType = new XType();
			rcap.complexType = rootType;
			// add attributes as capabilities
			for (XAttributeName an : xmlRoot.getAttributeNames()) {
				// except XSI attributes
				if (!XNElement.XSI.equals(an.namespace)) {
					XCapability c = new XCapability();
					c.name = new XName();
					c.name.name = an.name;
					c.name.semantics = new UriSemantics(an.namespace);
					c.valueType = inferValueType(xmlRoot.get(an.name, an.namespace));
					c.cardinality = XCardinality.ONE;
					rootType.capabilities.add(c);
				}
			}
			if (xmlRoot.hasChildren()) {
				analyseChildren(xmlRoot, rootType);
			}
		}
		return result;
	}
	/**
	 * Analyse the children of the given element.
	 * @param element the element to analyse
	 * @param elementType the element type to fill in with capabilities
	 */
	static void analyseChildren(XNElement element, XType elementType) {
		// group all elements
		Map<Pair<String, String>, List<XNElement>> elementGroups = Maps.newHashMap();
		for (XNElement e : element.children()) {
			Pair<String, String> name = Pair.of(e.name, e.namespace);
			List<XNElement> elements = elementGroups.get(name);
			if (elements == null) {
				elements = Lists.newArrayList();
				elementGroups.put(name, elements);
			}
			elements.add(e);
		}
		// check availability and type of attributes on each element
		for (Pair<String, String> eg : elementGroups.keySet()) {
			
			XCapability elementCapability = new XCapability();
			elementCapability.name = new XName();
			elementCapability.name.name = eg.first;
			elementCapability.name.semantics = new UriSemantics(eg.second);
			if (elementGroups.get(eg).size() > 1) {
				elementCapability.cardinality = XCardinality.ONE_OR_MANY;
			} else {
				elementCapability.cardinality = XCardinality.ONE;
			}
			elementType.capabilities.add(elementCapability);
			
			// aggregate individual element settings
			Map<XAttributeName, Set<XValueType>> valueTypes = Maps.newHashMap();
			Multiset<XAttributeName> attributeCounts = HashMultiset.create();
			XValueType contentSimpleType = null;
			boolean childrenPresent = false;
			List<XType> childTypes = Lists.newArrayList();
			for (XNElement e : elementGroups.get(eg)) {
				childrenPresent |= e.hasChildren();
				
				if (e.hasChildren()) {
					// analyse the complex element's structure
					XType childType = new XType();
					analyseChildren(e, childType);
					childTypes.add(childType);
				}
				
				for (XAttributeName an : e.getAttributeNames()) {
					Set<XValueType> vt = valueTypes.get(an);
					if (vt == null) {
						vt = Sets.newHashSet();
						valueTypes.put(an, vt);
					}
					vt.add(inferValueType(e.get(an.name, an.namespace)));
					attributeCounts.add(an);
				}
				// check if has content
				if (e.content != null) {
					XValueType c = inferValueType(e.content);
					// check if the type is consistent
					if (contentSimpleType == null) {
						contentSimpleType = c;
					} else
					if (contentSimpleType != c) {
						contentSimpleType = XValueType.STRING;
					}
				}
			}
			// check if the element represents a complex type
			if (childrenPresent || attributeCounts.size() > 0) {
				XType complexType = new XType();
				elementCapability.complexType = complexType;
				// add attributes
				for (Map.Entry<XAttributeName, Set<XValueType>> ans : valueTypes.entrySet()) {
					XCapability c = new XCapability();
					c.name = new XName();
					c.name.name = ans.getKey().name;
					c.name.semantics = new UriSemantics(ans.getKey().namespace);
					// there are more than one type inference?
					if (ans.getValue().size() > 1) {
						c.valueType = XValueType.STRING;
					} else {
						c.valueType = ans.getValue().iterator().next();
					}
					// check if there are as many attributes of this as there are elements
					if (elementGroups.get(eg).size() == attributeCounts.count(ans.getKey())) {
						c.cardinality = XCardinality.ONE;
					} else {
						c.cardinality = XCardinality.ZERO_OR_ONE;
					}
					complexType.capabilities.add(c);
				}
				if (contentSimpleType != null) {
					XCapability c = new XCapability();
					c.name = new XName();
					c.valueType = contentSimpleType;
					c.cardinality = XCardinality.ONE;
					complexType.capabilities.add(c);
				}
				joinChildTypes(childTypes, complexType);
				
			} else {
				elementCapability.valueType = contentSimpleType;
			}
			
		}
	}
	/**
	 * Join the child capabilities.
	 * @param childTypes the types of the current element's children
	 * @param complexType the current element's type
	 */
	static void joinChildTypes(List<XType> childTypes, XType complexType) {
		Map<XName, List<XCapability>> childCapabilities = Maps.newHashMap(); 
		for (XType childType : childTypes) {
			for (XCapability cap : childType.capabilities) {
				List<XCapability> cl = childCapabilities.get(cap.name);
				if (cl == null) {
					cl = Lists.newArrayList();
					childCapabilities.put(cap.name, cl);
				}
				cl.add(cap);
			}
		}
		for (Map.Entry<XName, List<XCapability>> ce : childCapabilities.entrySet()) {
			// cardinality join
			boolean zeroOrX = false;
			if (ce.getValue().size() < childTypes.size()) {
				zeroOrX = true;
			}
			boolean xOrMany = false;
			List<XType> subtypes = Lists.newArrayList();
			XValueType commonValueType = null;
			for (XCapability c : ce.getValue()) {
				if (c.cardinality == XCardinality.ONE_OR_MANY || c.cardinality == XCardinality.ZERO_OR_MANY) {
					xOrMany = true;
				}
				if (c.cardinality == XCardinality.ZERO_OR_MANY || c.cardinality == XCardinality.ZERO_OR_ONE) {
					zeroOrX = true;
				}
				if (c.complexType != null) {
					subtypes.add(c.complexType);
				}
				if (c.valueType != null) {
					if (commonValueType == null) {
						commonValueType = c.valueType;
					} else
					if (commonValueType != c.valueType) {
						commonValueType = XValueType.STRING;
					}
				}
			}
			XCardinality car = null;
			if (zeroOrX) {
				if (xOrMany) {
					car = XCardinality.ZERO_OR_MANY;
				} else {
					car = XCardinality.ZERO_OR_ONE;
				}
			} else {
				if (xOrMany) {
					car = XCardinality.ONE_OR_MANY;
				} else {
					car = XCardinality.ONE;
				}
			}
			XCapability cnew = new XCapability();
			cnew.name = ce.getKey();
			cnew.cardinality = car;
			
			if (subtypes.isEmpty()) {
				cnew.valueType = commonValueType;
			} else {
				XType tnew = new XType();
				if (commonValueType != null) {
					XCapability c = new XCapability();
					c.name = new XName();
					c.cardinality = XCardinality.ONE;
					c.valueType = commonValueType;
					tnew.capabilities.add(c);
				}
				cnew.complexType = tnew;
				
				joinChildTypes(subtypes, tnew);
			}
			
			
			complexType.capabilities.add(cnew);
		}
	}
	/** 
	 * Try to infer the value type from the supplied string.
	 * @param s the content string
	 * @return the value type
	 */
	static XValueType inferValueType(String s) {
		if (s != null) {
			if ("true".equals(s) || "false".equals(s)) {
				return XValueType.BOOLEAN;
			}
			try {
				new BigInteger(s);
				return XValueType.INTEGER;
			} catch (NumberFormatException ex) {
				// not a integer
			}
			try {
				new BigDecimal(s);
				return XValueType.REAL;
			} catch (NumberFormatException ex) {
				// not a real
			}
			if (s.length() >= 19) {
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
				try {
					sdf.parse(s.substring(0, 19));
					return XValueType.STRING;
				} catch (ParseException ex) {
					// not a date
				}
			}
		}
		return XValueType.STRING;
	}
	/**
	 * Computes the intersection of two types by ignoring the single capabilities of both t1 and t2 and using
	 * capabilities below that. Can be used to compute intersection on types whose root node name was different.
	 * The intersection type, if t1 and t2 are not relatedvia compare(), will receive the single capability with name from t1.
	 * @param t1 the first type
	 * @param t2 the second type
	 * @return the intersection type
	 */
	public static XType intersection(XType t1, XType t2) {
		TypeRelation rel = compare(t1, t2);
		if (rel == TypeRelation.EQUAL || rel == TypeRelation.SUPER) {
			return t1;
		} else
		if (rel == TypeRelation.EXTENDS) {
			return t2;
		}
		XCapability c0 = t1.capabilities.get(0);
		XCapability c1 = t2.capabilities.get(0);

		XType is = new XType();
		XCapability c = new XCapability();
		c.name = c0.name;
		c.cardinality = XCardinality.ONE;

		if ((c0.complexType != null) && (c1.complexType != null)) {
			c.complexType = c0.complexType.intersection(c1.complexType); 
			is.capabilities.add(c);
		} else {
			if (c0.valueType == c1.valueType) {
				return t1;
			} else
			if (c0.valueType == XValueType.REAL && c1.valueType == XValueType.INTEGER) {
				return t1;
			} else
			if (c1.valueType == XValueType.REAL && c0.valueType == XValueType.INTEGER) {
				return t2;
			}
			// otherwise, is will just remain empty
		}
		return is;
	}
	/**
	 * Computes the union of two types by ignoring the single capabilities of both t1 and t2 and using
	 * capabilities below that. Can be used to compute union on types whose root node name was different.
	 * The union type, if t1 and t2 are not related via compare(), will receive the single capability with name from t1.
	 * If the union can not be constructed due conflicting primitive types, null is returned
	 * @param t1 the first type
	 * @param t2 the second type
	 * @return the union type
	 */
	public static XType union(XType t1, XType t2) {
		TypeRelation rel = compare(t1, t2);
		if (rel == TypeRelation.EQUAL || rel == TypeRelation.EXTENDS) {
			return t1;
		} else
		if (rel == TypeRelation.SUPER) {
			return t2;
		}
		XCapability c0 = t1.capabilities.get(0);
		XCapability c1 = t2.capabilities.get(0);

		XType is = new XType();
		XCapability c = new XCapability();
		c.name = c0.name;
		c.cardinality = XCardinality.ONE;

		if ((c0.complexType != null) && (c1.complexType != null)) {
			c.complexType = c0.complexType.intersection(c1.complexType);
			if (c.complexType == null) {
				return null;
			}
			is.capabilities.add(c);
		} else {
			if (c0.valueType == c1.valueType) {
				return t1;
			} else {
				// automatically cast an integer into a real
				if (c0.valueType == XValueType.REAL && c1.valueType == XValueType.INTEGER) {
					return t1;
				} else
				if (c1.valueType == XValueType.REAL && c0.valueType == XValueType.INTEGER) {
					return t2;
				} else {
					return null; // conflicting primitive types
				}
			}
		}
		return is;
		
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
//		System.out.println(XSchema.parse(XNElement.parseXML("schemas/collection.xsd"), resolver));
//		System.out.println(XSchema.parse(XNElement.parseXML("schemas/pair.xsd"), resolver));
		System.out.println(XSchema.parse(XNElement.parseXML("schemas/map.xsd"), resolver));
		
		XNElement type = XNElement.parseXML("schemas/type.xsd");
		
		XType xt = XSchema.parse(type, resolver);
		
		System.out.println(XSchema.compare(xt, xt));
	}
}
