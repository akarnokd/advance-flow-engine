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

package eu.advance.logistics.flow.engine.xml.typesystem;

import hu.akarnokd.reactive4java.base.Func1;
import hu.akarnokd.reactive4java.interactive.Interactive;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Date;
import java.util.Deque;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import com.google.common.base.Objects;

import edu.umd.cs.findbugs.annotations.NonNull;
import eu.advance.logistics.flow.engine.xml.XsdDateTime;

/**
 * A simplified XML element model.
 * @author karnokd
 */
public class XElement implements Iterable<XElement> {
	/** The namespace:name record. */
	public static class XAttributeName {
		/** The name. */
		public final String name;
		/** The namespace. */
		public final String namespace;
		/** The namespace prefix. */
		public final String prefix;
		/** The hash. */
		private int hash;
		/**
		 * Construct an XName entitiy.
		 * @param name the name
		 * @param namespace the namespace
		 * @param prefix the namespace prefix
		 */
		public XAttributeName(String name, String namespace, String prefix) {
			this.name = name;
			this.namespace = namespace;
			this.prefix = prefix;
		}
		@Override
		public boolean equals(Object obj) {
			if (obj instanceof XAttributeName) {
				XAttributeName that = (XAttributeName)obj;
				return Objects.equal(this.name, that.name) && Objects.equal(this.namespace, that.namespace);
			}
			return false;
		}
		@Override
		public int hashCode() {
			if (hash == 0) {
				hash = Objects.hashCode(name, namespace);
			}
			return hash;
		}
		@Override
		public String toString() {
			return "{" + namespace + "}" + name;
		}
	}
	/** The element name. */
	public final String name;
	/** The optional associated namespace uri. */
	public String namespace;
	/** The prefix used by this element. */
	public String prefix;
	/** The content of a simple node. */
	public String content;
	/** The parent element. */
	public XElement parent;
	/** The attribute map. */
	protected final Map<XAttributeName, String> attributes = new LinkedHashMap<XAttributeName, String>();
	/** The child elements. */
	protected final List<XElement> children = new ArrayList<XElement>();
	/**
	 * Constructor. Sets the name.
	 * @param name the element name
	 */
	public XElement(String name) {
		this.name = name;
	}
	/**
	 * Retrieve the first attribute which has the given local attribute name.
	 * @param attributeName the attribute name
	 * @return the attribute value or null if no such attribute
	 */
	public String get(String attributeName) {
		// check first for a namespace-less attribute
		String attr = attributes.get(new XAttributeName(attributeName, null, null));
		if (attr == null) {
			// find any namespaced attribute
			for (XAttributeName n : attributes.keySet()) {
				if (Objects.equal(n.name, attributeName)) {
					return attributes.get(n);
				}
			}
		}
		return attr;
	}
	/**
	 * Retrieve the specific attribute.
	 * @param attributeName the attribute name
	 * @param attributeNamespace the attribute namespace URI
	 * @return the attribute value or null if not present
	 */
	public String get(String attributeName, String attributeNamespace) {
		return attributes.get(new XAttributeName(attributeName, attributeNamespace, null));
	}
	/**
	 * Retrieve the attribute names.
	 * @return the list of attribute names
	 */
	public List<XAttributeName> getAttributeNames() {
		return new ArrayList<XAttributeName>(attributes.keySet());
	}
	@Override
	public Iterator<XElement> iterator() {
		return children.iterator();
	}
	/**
	 * Returns an iterator which enumerates all children with the given name.
	 * @param name the name of the children to select
	 * @return the iterator
	 */
	public Iterable<XElement> childrenWithName(final String name) {
		return Interactive.where(children, new Func1<XElement, Boolean>() {
			@Override
			public Boolean invoke(XElement param1) {
				return Objects.equal(param1.name, name);
			}
		});
	}
	/**
	 * Returns an iterator which enumerates all children with the given name.
	 * @param name the name of the children to select
	 * @param namespace the namespace URI
	 * @return the iterator
	 */
	public Iterable<XElement> childrenWithName(final String name, final String namespace) {
		return Interactive.where(children, new Func1<XElement, Boolean>() {
			@Override
			public Boolean invoke(XElement param1) {
				return Objects.equal(param1.name, name) && Objects.equal(param1.namespace, namespace);
			}
		});
	}
	/**
	 * Returns the content of the first child which has the given name.
	 * @param name the child name
	 * @return the content or null if no such child
	 */
	public String childValue(String name) {
		for (XElement e : children) {
			if (e.name.equals(name)) {
				return e.content;
			}
		}
		return null;
	}
	/**
	 * Returns the content of the first child which has the given name.
	 * @param name the child name
	 * @param namespace the namespace URI
	 * @return the content or null if no such child
	 */
	public String childValue(String name, String namespace) {
		for (XElement e : children) {
			if (Objects.equal(e.name, name) && Objects.equal(e.namespace, namespace)) {
				return e.content;
			}
		}
		return null;
	}
	/**
	 * Returns the first child element with the given name.
	 * @param name the child name
	 * @return the XElement or null if not present
	 */
	public XElement childElement(String name) {
		for (XElement e : children) {
			if (e.name.equals(name)) {
				return e;
			}
		}
		return null;
	}
	/**
	 * Returns the first child element with the given name.
	 * @param name the child name
	 * @param namespace the namespace
	 * @return the XElement or null if not present
	 */
	public XElement childElement(String name, String namespace) {
		for (XElement e : children) {
			if (Objects.equal(e.name, name) && Objects.equal(e.namespace, namespace)) {
				return e;
			}
		}
		return null;
	}
	/**
	 * XML parzolása inputstream-ből és lightweight XElement formába.
	 * Nem zárja be az inputstreamet.
	 * @param in az InputStream objektum
	 * @return az XElement objektum
	 * @throws XMLStreamException kivétel esetén
	 */
	public static XElement parseXML(InputStream in) throws XMLStreamException {
		XMLInputFactory inf = XMLInputFactory.newInstance();
		XMLStreamReader ir = inf.createXMLStreamReader(in);
		return parseXML(ir);
	}
	/**
	 * Parse an XML from the given XML Stream reader.
	 * @param in the XMLStreamReader
	 * @return the parsed XElement tree
	 * @throws XMLStreamException if an error occurs
	 */
	private static XElement parseXML(XMLStreamReader in) throws XMLStreamException {
		XElement node = null;
		XElement root = null;
		final StringBuilder emptyBuilder = new StringBuilder();
		StringBuilder b = null;
		Deque<StringBuilder> stack = new LinkedList<StringBuilder>();
		
		while (in.hasNext()) {
			int type = in.next();
			switch(type) {
			case XMLStreamConstants.START_ELEMENT:
				if (b != null) {
					// a megkezdett szöveg elmentése
					stack.push(b);
					b = null;
				} else {
					// nem volt text elem, így az üres elmentése
					stack.push(emptyBuilder);
				}
				XElement n = new XElement(in.getName().getLocalPart());
				n.namespace = in.getNamespaceURI();
				n.prefix = in.getPrefix();
				n.parent = node;
//				n.depth = depth++;
				int attCount = in.getAttributeCount();
				if (attCount > 0) {
					for (int i = 0; i < attCount; i++) {
						n.attributes.put(new XAttributeName(
								in.getAttributeLocalName(i), 
								in.getAttributeNamespace(i),
								in.getAttributePrefix(i)
						), in.getAttributeValue(i));
					}
				}
				if (node != null) {
					node.children.add(n);
				}
				node = n;
				if (root == null) {
					root = n;
				}
				break;
			case XMLStreamConstants.CDATA:
			case XMLStreamConstants.CHARACTERS:
				if (node != null && !in.isWhiteSpace()) {
					/*
					if (node.value == null) {
						node.value = new StringBuilder();
					}
					node.value.append(ir.getText());
					*/
					if (b == null) {
						b = new StringBuilder();
					}
					b.append(in.getText());
				}
				break;
			case XMLStreamConstants.END_ELEMENT:
				// ha volt szöveg, akkor hozzárendeljük a csomópont értékéhez
				if (b != null) {
					node.content = b.toString();
				}
				if (node != null) {
					node = node.parent;
				}
				// kiszedjük a szülőjének builderjét
				b = stack.pop();
				// ha ez az üres, akkor a szülőnek (még) nem volt szöveges tartalma
				if (b == emptyBuilder) {
					b = null;
				}
				break;
			default:
				// ignore others.
			}
		}
		in.close();
		return root;
	}
	/**
	 * Parse an XML from the given Reader.
	 * @param in the Reader
	 * @return the parsed XElement tree
	 * @throws XMLStreamException if an error occurs
	 */
	public static XElement parseXML(Reader in) throws XMLStreamException {
		XMLInputFactory inf = XMLInputFactory.newInstance();
		XMLStreamReader ir = inf.createXMLStreamReader(in);
		return parseXML(ir);
	}
	/**
	 * Parse an XML from the given file.
	 * @param fileName the file name
	 * @return the parsed XElement tree
	 * @throws XMLStreamException if an error occurs
	 * @throws IOException if the file could not be found or other I/O error occurs
	 */
	public static XElement parseXML(String fileName) throws IOException, XMLStreamException {
		InputStream in = new FileInputStream(fileName);
		try {
			return parseXML(in);
		} finally {
			in.close();
		}
	}
	/**
	 * Parse an XML from the given file.
	 * @param file the file
	 * @return the parsed XElement tree
	 * @throws XMLStreamException if an error occurs
	 * @throws IOException if the file could not be found or other I/O error occurs
	 */
	public static XElement parseXML(File file) throws IOException, XMLStreamException {
		InputStream in = new FileInputStream(file);
		try {
			return parseXML(in);
		} finally {
			in.close();
		}
	}
	@Override
	public String toString() {
		StringBuilder b = new StringBuilder();
		toStringRep("", new HashMap<String, String>(), b);
		return b.toString();
	}
	/**
	 * Convert the element into a pretty printed string representation.
	 * @param indent the current line indentation
	 * @param nss the namespace cache
	 * @param out the output
	 */
	void toStringRep(String indent, Map<String, String> nss, StringBuilder out) {
		out.append(indent).append("<");
		if (prefix != null && prefix.length() > 0) {
			out.append(prefix).append(":");
		}
		out.append(name);
		if (namespace != null && !nss.containsKey(namespace)) {
			nss.put(namespace, prefix != null && prefix.length() > 0 ? prefix : "");
			
			out.append(" xmlns").append(prefix != null && prefix.length() > 0 ? ":" : "")
			.append(prefix != null && prefix.length() > 0 ? prefix : "").append("='").append(sanitize(namespace)).append("'");
		}
		if (attributes.size() > 0) {
			for (XAttributeName an : attributes.keySet()) {
				if (an.namespace != null && !nss.containsKey(an.namespace)) {
					nss.put(an.namespace, an.prefix != null && an.prefix.length() > 0 ? an.prefix : "");
					
					out.append(" xmlns").append(an.prefix != null && an.prefix.length() > 0 ? ":" : "")
					.append(an.prefix != null && an.prefix.length() > 0 ? an.prefix : "").append("='")
					.append(sanitize(an.namespace)).append("'");
				}
				out.append(" ");
				if (an.prefix != null && an.prefix.length() > 0) {
					out.append(an.prefix).append(":");
				}
				out.append(an.name).append("='").append(sanitize(attributes.get(an))).append("'");
			}
		}
		
		if (children.size() == 0) {
			if (content == null) {
				out.append("/>");
			} else {
				out.append(">");
				out.append(sanitize(content));
				out.append(indent).append("</");
				if (prefix != null && prefix.length() > 0) {
					out.append(prefix).append(":");
				}
				out.append(name);
				out.append(">");
			}
		} else {
			if (content == null) {
				out.append(String.format(">%n"));
			} else {
				out.append(">");
				out.append(sanitize(content));
				out.append(String.format("%n"));
			}
			for (XElement e : children) {
				e.toStringRep(indent + "  ", nss, out);
			}
			out.append(indent).append("</");
			if (prefix != null && prefix.length() > 0) {
				out.append(prefix).append(":");
			}
			out.append(name);
			out.append(">");
		}
		out.append(String.format("%n"));
	}
	/**
	 * Connverts all sensitive characters to its HTML entity equivalent.
	 * @param s the string to convert, can be null
	 * @return the converted string, or an empty string
	 */
	public static String sanitize(String s) {
		if (s != null) {
			StringBuilder b = new StringBuilder(s.length());
			for (int i = 0, count = s.length(); i < count; i++) {
				char c = s.charAt(i);
				switch (c) {
				case '<':
					b.append("&lt;");
					break;
				case '>':
					b.append("&gt;");
					break;
				case '\'':
					b.append("&#39;");
					break;
				case '"':
					b.append("&quot;");
					break;
				case '&':
					b.append("&amp;");
					break;
				default:
					b.append(c);
				}
			}
			return b.toString();
		}
		return "";
	}
	/**
	 * @return Creates a deep copy of this element.
	 */
	public XElement copy() {
		XElement e = new XElement(name);
		e.namespace = namespace;
		e.content = content;
		e.prefix = prefix;
		
		for (Map.Entry<XAttributeName, String> me : attributes.entrySet()) {
			XAttributeName an = new XAttributeName(me.getKey().name, me.getKey().namespace, me.getKey().prefix);
			e.attributes.put(an, me.getValue());
		}
		for (XElement c : children) {
			XElement c0 = c.copy();
			c0.parent = e;
			e.children.add(c0);
		}
		return e;
	}
	/** @return the iterable for all children. */
	@NonNull 
	public List<XElement> children() {
		return children;
	}
	/** @return if this node has children or not. */
	public boolean hasChildren() {
		return !children.isEmpty();
	}
	/** @return if this node has attributes or not. */
	public boolean hasAttributes() {
		return !attributes.isEmpty();
	}
	/**
	 * Add a new XElement with the given local name and no namespace.
	 * @param name the name of the new element
	 * @return the created XElement child
	 */
	public XElement add(@NonNull String name) {
		XElement e = new XElement(name);
		e.parent = this;
		children.add(e);
		return e;
	}
	/**
	 * Add the given existing child to this element.
	 * @param child the child element
	 * @return the child element
	 */
	public XElement add(@NonNull XElement child) {
		child.parent = this;
		children.add(child);
		return child;
	}
	/**
	 * Add a new XElement with the given local name and namespace.
	 * @param name the name of the new element
	 * @param namespace the namespace of the new element
	 * @return the created XElement child
	 */
	public XElement add(@NonNull String name, @NonNull String namespace) {
		XElement e = new XElement(name);
		e.namespace = namespace;
		e.parent = this;
		children.add(e);
		return e;
	}
	/**
	 * Set an attribute value identified by a local name and no namespace.
	 * @param name the attribute name
	 * @param value the value, if null, the attribute will be removed
	 */
	public void set(@NonNull String name, Object value) {
		if (value != null) {
			if (value instanceof Date) {
				attributes.put(new XAttributeName(name, null, null), XsdDateTime.format((Date)value));
			} else {
				attributes.put(new XAttributeName(name, null, null), value.toString());
			}
		} else {
			attributes.remove(new XAttributeName(name, null, null));
		}
	}
	/**
	 * Set an attribute value identified by a local name and namespace.
	 * @param name the attribute name
	 * @param namespace the attribute namespace
	 * @param value the value, if null, the attribute will be removed
	 */
	public void set(@NonNull String name, String namespace, String value) {
		if (value != null) {
			attributes.put(new XAttributeName(name, namespace, null), value);
		} else {
			attributes.remove(new XAttributeName(name, namespace, null));
		}
	}
	/**
	 * @return Construct the XPath expression to locate this element. 
	 */
	public String getXPath() {
		StringBuilder r = new StringBuilder();
		if (parent != null) {
			XElement q = this;
			XElement p = parent;
			while (p != null) {
				if (p.children.size() > 1) {
					int count = 0;
					int idx = 0;
					for (XElement c : p.children) {
						if (q.name.equals(c.name) && Objects.equal(q.namespace, c.namespace)) {
							if (c == q) {
								idx = count;
								break;
							}
							count++;
						}
					}
					if (idx > 0) {
						r.insert(0, ']');
						r.insert(0, idx);
						r.insert(0, '[');
						r.insert(0, p.name);
						r.insert(0, '/');
					} else {
						r.insert(0, p.name);
						r.insert(0, '/');
					}
				} else {
					r.insert(0, p.name);
					r.insert(0, '/');
				}
				
				q = p;
				p = p.parent;
			}
		}
		r.append('/').append(name);
		return r.toString();
	}
	/**
	 * Retrieve an integer attribute. Throws exception if the attribute is missing.
	 * @param attribute the attribute name
	 * @return the value
	 */
	public int getInt(String attribute) {
		return Integer.parseInt(get(attribute));
	}
	/**
	 * Retrieve an integer attribute or the default value if not exists.
	 * @param attribute the attribute name
	 * @param defaultValue the default value to return
	 * @return the value
	 */
	public int getInt(String attribute, int defaultValue) {
		String value = get(attribute);
		if (value == null) {
			return defaultValue;
		}
		return Integer.parseInt(value);
	}
	/**
	 * Retrieve an integer attribute or the default value if not exists.
	 * @param attribute the attribute name
	 * @param namespace the attribute namespace URI
	 * @return the value
	 */
	public int getInt(String attribute, String namespace) {
		return Integer.parseInt(get(attribute, namespace));
	}
	/**
	 * Retrieve an integer attribute or the default value if not exists.
	 * @param attribute the attribute name
	 * @param namespace the namespace URI
	 * @param defaultValue the default value to return
	 * @return the value
	 */
	public int getInt(String attribute, String namespace, int defaultValue) {
		String value = get(attribute, namespace);
		if (value == null) {
			return defaultValue;
		}
		return Integer.parseInt(value);
	}
	/**
	 * Retrieve an long attribute or throw an exception if not exists.
	 * @param attribute the attribute name
	 * @return the value
	 */
	public long getLong(String attribute) {
		return Long.parseLong(get(attribute));
	}
	/**
	 * Retrieve an long attribute or the default value if not exists.
	 * @param attribute the attribute name
	 * @param defaultValue the default value to return
	 * @return the value
	 */
	public long getLong(String attribute, long defaultValue) {
		String value = get(attribute);
		if (value == null) {
			return defaultValue;
		}
		return Long.parseLong(value);
	}
	/**
	 * Retrieve an integer attribute or throw an exception if not exists.
	 * @param attribute the attribute name
	 * @param namespace the attribute namespace URI
	 * @return the value
	 */
	public long getLong(String attribute, String namespace) {
		return Long.parseLong(get(attribute, namespace));
	}
	/**
	 * Retrieve an integer attribute or the default value if not exists.
	 * @param attribute the attribute name
	 * @param namespace the attribute namespace URI
	 * @param defaultValue the default value to return
	 * @return the value
	 */
	public long getLong(String attribute, String namespace, long defaultValue) {
		String value = get(attribute, namespace);
		if (value == null) {
			return defaultValue;
		}
		return Long.parseLong(value);
	}
	/**
	 * Save this XML into the given file.
	 * @param fileName the file name
	 * @throws IOException on error
	 */
	public void save(String fileName) throws IOException {
		save(new File(fileName));
	}
	/**
	 * Save this XML into the given file.
	 * @param file the file
	 * @throws IOException on error
	 */
	public void save(File file) throws IOException {
		PrintWriter out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), "UTF-8")));
		try {
			out.println("<?xml version='1.0' encoding='UTF-8'?>");
			out.print(toString());
		} finally {
			out.close();
		}
	}
	/**
	 * Save this XML into the given output stream.
	 * @param stream the output stream
	 * @throws IOException on error
	 */
	public void save(OutputStream stream) throws IOException {
		PrintWriter out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(stream, "UTF-8")));
		try {
			out.println("<?xml version='1.0' encoding='UTF-8'?>");
			out.print(toString());
		} finally {
			out.close();
		}
	}
	/**
	 * Save this XML into the given writer.
	 * @param stream the output writer
	 * @throws IOException on error
	 */
	public void save(Writer stream) throws IOException {
		PrintWriter out = new PrintWriter(new BufferedWriter(stream));
		try {
			out.println("<?xml version='1.0' encoding='UTF-8'?>");
			out.print(toString());
		} finally {
			out.close();
		}
	}
	/**
	 * Save the tree into the given XML stream writer.
	 * @param stream the stream writer
	 * @throws XMLStreamException if an error occurs
	 */
	public void save(XMLStreamWriter stream) throws XMLStreamException {
		// TODO implement
		stream.writeStartDocument("UTF-8", "1.0");
		
		saveInternal(stream);
		
		stream.writeEndDocument();
	}
	/**
	 * Store the element's content and recursively call itself for children.
	 * @param stream the stream output
	 * @throws XMLStreamException if an error occurs
	 */
	protected void saveInternal(XMLStreamWriter stream) throws XMLStreamException {
		if (namespace != null) {
			stream.writeStartElement(prefix, namespace, name);
		} else {
			stream.writeStartElement(name);
		}
		for (Map.Entry<XAttributeName, String> a : attributes.entrySet()) {
			XAttributeName an = a.getKey();
			if (an.namespace != null) {
				stream.writeAttribute(an.prefix, an.namespace, an.name, a.getValue());
			} else {
				stream.writeAttribute(an.name, a.getValue());
			}
		}
		if (content != null) {
			stream.writeCharacters(content);
		} else {
			for (XElement e : children) {
				e.saveInternal(stream);
			}
		}
		
		stream.writeEndElement();
	}
}