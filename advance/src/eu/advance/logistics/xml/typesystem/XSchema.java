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
package eu.advance.logistics.xml.typesystem;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import javax.xml.stream.XMLStreamException;


/**
 * A XML type description.
 * @author karnokd
 */
public final class XSchema {
	/** The XSD namespace. */
	public static final String XSD = "http://www.w3.org/2001/XMLSchema";
	/** The XSD instance URI. */
	public static final String XSI = "http://www.w3.org/2001/XMLSchema-instance";
	/** Utility class. */
	private XSchema() {
		// utility class
	}
	/**
	 * @param args no arguments
	 * @throws Exception ignored
	 */
	public static void main(String[] args) throws Exception {
		XElement schema1 = XElement.parseXML("test/type1.xsd");
		XElement schema2 = XElement.parseXML("test/type2.xsd");
		
		System.out.println(schema1);
		XType t1 = parse(schema1);
		System.out.println(t1);
		System.out.println(schema2);
		XType t2 = parse(schema2);
		System.out.println(t2);
		System.out.println(t1.compareTo(t2));
		System.out.println(t2.compareTo(t1));
		System.out.println(t1.compareTo(t1));
	}
	/**
	 * Create the XML type by parsing the given schema document.
	 * @param schema the schema tree.
	 * @return the XML type representing the schema
	 */
	public static XType parse(XElement schema) {
		List<XElement> roots = schema.childrenWithName("element", XSD);
		if (roots.size() != 1) {
			throw new IllegalArgumentException("Zero or multi-rooted schema not supported");
		}
		XElement root = roots.get(0);

		
		List<XElement> typedefs = new ArrayList<XElement>();

		searchTypes(schema, typedefs, new HashSet<String>());
		
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
	static XCapability setElement(XElement root, List<XElement> typedefs,
			XType result, Map<String, XType> memory) {
		XCapability c = new XCapability();
		c.name = new XName();
		c.name.name = root.get("name");
		// FIXME set semantic token and aliases
		c.cardinality = getNumericity(root);
		result.capabilities.add(c);
		
		String rootType = root.get("type");
		if (rootType != null) {
			// check for direct simple type
			XValueType rootSimpleType = getSimpleType(root.prefix, rootType);
			if (rootSimpleType != null) {
				c.valueType = rootSimpleType;
			}
		} else {
			rootType = root.get("ref");
			XElement simpleType = findType(rootType, "simpleType", typedefs);
			if (simpleType != null) {
				setSimpleType(c, simpleType, typedefs);
			} else {
				XElement complexType = findType(rootType, "complexType", typedefs);
				if (complexType != null) {
					 setComplexType(c, complexType, typedefs, memory);
				} else {
					// check for local definitions
					simpleType = root.childElement("simpleType", XSD);
					if (simpleType != null) {
						setSimpleType(c, simpleType, typedefs);
					} else {
						complexType = root.childElement("complexType", XSD);
						if (complexType != null) {
							setComplexType(c, complexType, typedefs, memory);
						} else {
							throw new AssertionError("Strange element: " + root.get("name") + ", " + rootType);
						}
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
			XElement typedef, List<XElement> types, Map<String, XType> memory) {
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
		
		LinkedList<XElement> seqs = new LinkedList<XElement>();
		seqs.addAll(typedef.children);
		while (seqs.size() > 0) {
			XElement sequence = seqs.removeFirst();
			if (sequence.namespace.equals(XSD)) {
				if (sequence.name.equals("element")) {
					setElement(sequence, types, c.complexType, memory);
				} else
				if (sequence.name.equals("sequence")
						|| sequence.name.equals("choice") // FIXME not sure
						|| sequence.name.equals("group") // FIXME not sure
						|| sequence.name.equals("all") // FIXME not sure
				) {
					seqs.addAll(0, sequence.children);
				} else
				if (sequence.name.equals("simpleContent")) {
					XElement restriction = sequence.childElement("restriction");
					if (restriction != null) {
						XCapability cap = new XCapability();
						// FIXME interpretation?!
						cap.name = c.name;
						cap.cardinality = XCardinality.ONE;
						c.complexType.capabilities.add(cap);
						setSimpleRestriction(cap, sequence, types, restriction);
					} else {
						XElement extension = sequence.childElement("extension");
						if (extension != null) {
							// copy existing definition
							String base = extension.get("base");
							XElement simpleBase = findType(base, "simpleType", types);
							if (simpleBase != null) {
								XCapability cap = new XCapability();
								// FIXME interpretation?!
								cap.name = c.name;
								cap.cardinality = XCardinality.ONE;
								setSimpleType(cap, simpleBase, types);
								c.complexType.capabilities.add(cap);
								setAttributes(c, extension, types, memory);
							} else {
								XElement complexType = findType(base, "complexType", types);
								if (complexType != null) {
									setComplexType(c, complexType, types, memory);
									setAttributes(c, extension, types, memory);
								} else {
									throw new AssertionError("Unknown extension base type " + base);
								}
							}
						} else {
							throw new AssertionError("Unknown simple content type");
						}
					}
				} else
				if (sequence.name.equals("complexContent")) {
					XElement restriction = sequence.childElement("restriction");
					if (restriction != null) {
						XCapability cap = new XCapability();
						// FIXME interpretation?!
						cap.name = c.name;
						cap.cardinality = XCardinality.ONE;
						c.complexType.capabilities.add(cap);
						setSimpleRestriction(cap, sequence, types, restriction);
						setComplexType(c, restriction, types, memory);
					} else {
						XElement extension = sequence.childElement("extension");
						if (extension != null) {
							String base = extension.get("base");
							XElement simpleBase = findType(base, "simpleType", types);
							if (simpleBase != null) {
								XCapability cap = new XCapability();
								// FIXME interpretation?!
								cap.name = c.name;
								cap.cardinality = XCardinality.ONE;
								setSimpleType(cap, simpleBase, types);
								setComplexType(c, extension, types, memory);
								c.complexType = c.complexType.copy();
								c.complexType.capabilities.add(cap);

							} else {
								XElement complexType = findType(base, "complexType", types);
								if (complexType != null) {
									setComplexType(c, complexType, types, memory);
									c.complexType = c.complexType.copy();
									setComplexType(c, extension, types, memory);
								} else {
									throw new AssertionError("Unknown extension base type " + base);
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
	static void setAttributes(XCapability c, XElement typedef,
			List<XElement> types, Map<String, XType> memory) {
		for (XElement attr : typedef.childrenWithName("attribute", XSD)) {
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
		LinkedList<XElement> attrgr = new LinkedList<XElement>();
		attrgr.addAll(typedef.childrenWithName("attributeGroup", XSD));
		while (attrgr.size() > 0) {
			XElement ag = attrgr.removeFirst();
			String ref = ag.get("ref");
			if (ref != null) {
				ag = findType(ref, "attributeGroup", types);
				if (ag == null) {
					throw new AssertionError("Unknown attribute group: " + ref);
				}
			}
			for (XElement attr : typedef.childrenWithName("attribute", XSD)) {
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
			attrgr.addAll(ag.childrenWithName("attributeGroup", XSD));
		}
	}
	/**
	 * Set the simple type from the given type definition.
	 * @param c the target capability
	 * @param typedef the current type definition
	 * @param types the list of other simple types
	 */
	static void setSimpleType(XCapability c, XElement typedef, 
			List<XElement> types) {
		XElement restrict = typedef.childElement("restriction", XSD);
		if (restrict != null) {
			setSimpleRestriction(c, typedef, types, restrict);
		} else {
			XElement list = typedef.childElement("list", XSD);
			if (list != null) {
				
				String itemType = list.get("itemType");
				XValueType primitiveType = getSimpleType(list.prefix, itemType);
				if (primitiveType != null) {
					c.complexType = new XType();
					
					XCapability c1 = new XCapability();
					c1.name = new XName();
					c1.valueType = primitiveType;
					c1.cardinality = XCardinality.ZERO_OR_MANY; // FIXME list numericity unknown
					c.complexType.capabilities.add(c1);
				} else {
					// find among children
					XElement parent = findType(itemType, "simpleType", list.childrenWithName("simpleType", XSD));
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
				XElement union = typedef.childElement("union", XSD);
				if (union != null) {
					c.valueType = XValueType.STRING; // FIXME, almost always comes to a common strung
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
	static void setSimpleRestriction(XCapability c, XElement typedef,
			List<XElement> types, XElement restrict) {
		String base = restrict.get("base");
		XValueType primitiveType = getSimpleType(restrict.prefix, base);
		if (primitiveType != null) {
			c.valueType = primitiveType;
		} else {
			// find among children
			XElement parent = findType(base, "simpleType", restrict.childrenWithName("simpleType", XSD));
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
	 */
	static void searchTypes(XElement root, List<XElement> typedefs, 
			Set<String> memory) {
		List<XElement> includes = root.childrenWithName("include", XSD);
		for (XElement inc : includes) {
			String loc = inc.get("schemaLocation");
			if (loc != null && memory.add(loc)) {
				InputStream in = null;
				try {
					try {
						in = new URI(loc).toURL().openStream();
					} catch (IOException ex) {
						throw new RuntimeException(ex);
					} catch (URISyntaxException ex) {
						try {
							in = new FileInputStream(loc);
						} catch (IOException exc) {
							throw new RuntimeException(ex);
						}
					}
					searchTypes(XElement.parseXML(in), typedefs, memory);
				} catch (XMLStreamException ex) {
					throw new RuntimeException(ex);
				} finally {
					if (in != null) {
						try { in.close(); } catch (IOException ex) { ex.printStackTrace(); }
					}
				}
			}
		}
		typedefs.addAll(root.childrenWithName("simpleType", XSD));
		typedefs.addAll(root.childrenWithName("complexType", XSD));
		typedefs.addAll(root.childrenWithName("attributeGroup", XSD));
	}
	

	/**
	 * Find a specific type definition by its id.
	 * @param name the type name
	 * @param type the type definition to look for (e.g., simpleType, complexType, attributeGroup)
	 * @param types the list of types
	 * @return the target type definition or null if not found
	 */
	static XElement findType(String name, String type, List<XElement> types) {
		for (XElement e : types) {
			if (Objects.equals(e.get("name"), name) && e.name.equals(type)) {
				return e;
			}
		}
		return null;
	}
	/**
	 * Extract the numericity value from an elements minOccurs and maxOccurs definition.
	 * @param e the element definition
	 * @return the numericity
	 */
	static XCardinality getNumericity(XElement e) {
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
			throw new AssertionError("0 to Max numericity not supported: " + maxo);
		} else
		if (mino.equals("1")) {
			if (maxo.equals("1")) {
				return XCardinality.ONE;
			} else
			if (maxo.equals("unbounded")) {
				throw new AssertionError("1 to Max numericity not supported: " + maxo);
			}
		}
		throw new AssertionError("Min numericity not supported: " + mino);
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
			|| xsdType.equals(prefix + ":decimal")
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
}
