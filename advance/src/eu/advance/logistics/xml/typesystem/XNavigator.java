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

import hu.akarnokd.reactive4java.base.Func1;
import hu.akarnokd.reactive4java.base.Pair;
import hu.akarnokd.reactive4java.interactive.Interactive;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NoSuchElementException;

import javax.xml.stream.XMLStreamException;

import org.jaxen.BaseXPath;
import org.jaxen.FunctionCallException;
import org.jaxen.JaxenRuntimeException;
import org.jaxen.Navigator;
import org.jaxen.UnsupportedAxisException;
import org.jaxen.XPath;
import org.jaxen.pattern.Pattern;
import org.jaxen.saxpath.SAXPathException;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterators;
import com.google.common.collect.Maps;

import eu.advance.logistics.util.Triplet;
import eu.advance.logistics.xml.typesystem.XElement.XAttributeName;

/**
 * An implementation for Jaxen's XPath engine.
 * @author karnokd, 2011.07.19.
 */
public class XNavigator implements Navigator {
	/** */
	private static final long serialVersionUID = 7493682578566309878L;

	@Override
	public Iterator<?> getChildAxisIterator(Object contextNode)
			throws UnsupportedAxisException {
		if (contextNode instanceof XElement) {
			final XElement e = (XElement) contextNode;
			if (e.content != null) {
				return Iterators.concat(Iterators.singletonIterator(Pair.of(e, e.content)), 
						e.children.iterator());
			}
			return e.children.iterator();
		}
		return Collections.emptyIterator();
	}

	@Override
	public Iterator<?> getDescendantAxisIterator(Object contextNode)
			throws UnsupportedAxisException {
		if (contextNode instanceof XElement) {
			final XElement e = (XElement) contextNode;
			return new Iterator<Object>() {
				/** The current iterator. */
				Iterator<?> current = getChildAxisIterator(e);
				/** The stack for recursive descent. */
				final Deque<Iterator<?>> stack = new LinkedList<Iterator<?>>();
				@Override
				public boolean hasNext() {
					if (!current.hasNext()) {
						while (!current.hasNext() && !stack.isEmpty()) {
							current = stack.pop();
						}
					}
					return current.hasNext();
				}
				@Override
				public Object next() {
					if (hasNext()) {
						Object o = current.next();
						stack.push(current);
						try {
							current = getChildAxisIterator(o);
						} catch (UnsupportedAxisException ex) {
							throw new JaxenRuntimeException(ex);
						}
						return o;
					}
					throw new NoSuchElementException();
				}
				@Override
				public void remove() {
					throw new UnsupportedOperationException();
				}
			};
		}		
		return Collections.emptyIterator();
	}

	@Override
	public Iterator<?> getParentAxisIterator(Object contextNode)
			throws UnsupportedAxisException {
		Object parent = getParentNode(contextNode);
		if (parent != null) {
			return Iterators.singletonIterator(parent);
		}
		return Collections.emptyIterator();
	}

	@Override
	public Iterator<?> getAncestorAxisIterator(final Object contextNode)
			throws UnsupportedAxisException {
		// recursive iterator of all patents.
		return new Iterator<Object>() {
			/** The current ancestor in the iteration. */
			Object current = getParentNode(contextNode);
			@Override
			public boolean hasNext() {
				return current != null;
			}
			@Override
			public Object next() {
				try {
					if (hasNext()) {
						Object result = current;
						current = getParentNode(current);
						return result;
					}
					throw new NoSuchElementException();
				} catch (UnsupportedAxisException ex) {
					throw new JaxenRuntimeException(ex);
				}
			}
			@Override
			public void remove() {
				throw new UnsupportedOperationException();
			}
		};
	}

	@Override
	public Iterator<?> getFollowingSiblingAxisIterator(final Object contextNode)
			throws UnsupportedAxisException {
		Object parent = getParentNode(contextNode);
		if (parent == null) {
			return Collections.emptyIterator();
		}
		return Iterators.filter(getChildAxisIterator(parent), new Predicate<Object>() {
			/** Indicator that the first occurrence of the context node has been found. */
			private boolean foundFirst;
			@Override
			public boolean apply(Object input) {
				if (input == contextNode) {
					foundFirst = true;
				}
				return foundFirst && input != contextNode; // found previously but is not the current
			}
		});
	}

	@Override
	public Iterator<?> getPrecedingSiblingAxisIterator(final Object contextNode)
			throws UnsupportedAxisException {
		Object parent = getParentNode(contextNode);
		if (parent == null) {
			return Collections.emptyIterator();
		}
		Iterator<?> children = getChildAxisIterator(parent);
		List<Object> backwards = new ArrayList<Object>();
		while (children.hasNext()) {
			Object o = children.next();
			if (o != contextNode) {
				backwards.add(o);
			} else {
				break;
			}
		}
		Collections.reverse(backwards);
		return backwards.iterator();
	}

	@Override
	public Iterator<?> getFollowingAxisIterator(final Object contextNode)
			throws UnsupportedAxisException {
		// depth first iteration of all following nodes
		return new Iterator<Object>() {
			@Override
			public boolean hasNext() {
				// TODO Auto-generated method stub
				return false;
			}
			@Override
			public Object next() {
				// TODO Auto-generated method stub
				return null;
			}
			@Override
			public void remove() {
				throw new UnsupportedOperationException();
			}
		};
	}

	@Override
	public Iterator<?> getPrecedingAxisIterator(Object contextNode)
			throws UnsupportedAxisException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Iterator<?> getAttributeAxisIterator(Object contextNode)
			throws UnsupportedAxisException {
		if (contextNode instanceof XElement) {
			final XElement e = (XElement) contextNode;
			return 
			Interactive.select(
				Interactive.where(e.attributes.entrySet(), 
						new Func1<Map.Entry<XAttributeName, String>, Boolean>() {
					@Override
					public Boolean invoke(Entry<XAttributeName, String> input) {
						return input.getKey().namespace == null 
								|| !input.getKey().namespace.startsWith("http://www.w3.org/2000/xmlns/");
					}
				}),
				new Func1<Map.Entry<XAttributeName, String>, Triplet<XElement, XAttributeName, String>>() {
					@Override
					public Triplet<XElement, XAttributeName, String> invoke(
							Entry<XAttributeName, String> param1) {
						return Triplet.of(e, param1.getKey(), param1.getValue());
					}
				}
			).iterator();
		}
		return Collections.emptyIterator();
	}

	@Override
	public Iterator<?> getNamespaceAxisIterator(Object contextNode) {
		// the nearest XElement
		if (contextNode instanceof Pair<?, ?>) {
			contextNode = ((Pair<?, ?>)contextNode).first;
		}
		XElement e = (XElement)contextNode;
		Map<String, String> namespaces = Maps.newHashMap();
		namespaces.put("xml", "http://www.w3.org/XML/1998/namespace");
		while (e != null) {
			if (e.namespace != null) {
				namespaces.put(e.prefix, e.namespace);
			}
			// select conventional attribute names
			for (XAttributeName at : e.attributes.keySet()) {
				if (at.namespace == null || !at.namespace.startsWith("http://www.w3.org/2000/xmlns/")) {
					namespaces.put(at.prefix, at.namespace);
				}
			}
			// select namespace declaration attributes
			for (XAttributeName at : e.attributes.keySet()) {
				if (at.namespace != null && !at.namespace.startsWith("http://www.w3.org/2000/xmlns/")) {
					namespaces.put(at.name, e.attributes.get(at));
				}
			}
			e = e.parent;
		}
		return namespaces.entrySet().iterator(); // FIXME usage of the resulting values???
	}

	@Override
	public Iterator<?> getSelfAxisIterator(Object contextNode)
			throws UnsupportedAxisException {
		return Iterators.singletonIterator(contextNode);
	}

	@Override
	public Iterator<?> getDescendantOrSelfAxisIterator(Object contextNode)
			throws UnsupportedAxisException {
		return Iterators.concat(Iterators.singletonIterator(contextNode), getDescendantAxisIterator(contextNode));
	}

	@Override
	public Iterator<?> getAncestorOrSelfAxisIterator(Object contextNode)
			throws UnsupportedAxisException {
		return Iterators.concat(Iterators.singletonIterator(contextNode), getAncestorAxisIterator(contextNode));
	}

	@Override
	public Object getDocument(String uri) throws FunctionCallException {
		try {
			URI ouri = new URI(uri);
			InputStream in = ouri.toURL().openStream();
			try {
				return XElement.parseXML(in);
			} finally {
				in.close();
			}
		} catch (URISyntaxException ex) {
			throw new FunctionCallException(ex);
		} catch (IOException ex) {
			throw new FunctionCallException(ex);
		} catch (XMLStreamException ex) {
			throw new FunctionCallException(ex);
		}
	}

	@Override
	public Object getDocumentNode(Object contextNode) {
		// FIXME document node is the outermost element?!
		XElement e = null;
		if (contextNode instanceof Pair<?, ?>) {
			e = (XElement)(((Pair<?, ?>)contextNode).first);
		}
		if (contextNode instanceof Triplet<?, ?, ?>) {
			e = (XElement)(((Triplet<?, ?, ?>)contextNode).first);
		}
		if (contextNode instanceof XElement) {
			e = (XElement) contextNode;
			while (e.parent != null) {
				e = e.parent;
			}
		}
		return e;
	}

	@Override
	public Object getParentNode(Object contextNode)
			throws UnsupportedAxisException {
		if (contextNode instanceof Triplet<?, ?, ?>) {
			Triplet<?, ?, ?> triplet = (Triplet<?, ?, ?>) contextNode;
			return triplet.first;
		} else
		if (contextNode instanceof Pair<?, ?>) {
			Pair<?, ?> pair = (Pair<?, ?>) contextNode;
			return pair.first;
		}
		return ((XElement)contextNode).parent;
	}

	@Override
	public String getElementNamespaceUri(Object element) {
		return ((XElement)element).namespace;
	}

	@Override
	public String getElementName(Object element) {
		return ((XElement)element).name;
	}

	@Override
	public String getElementQName(Object element) {
		XElement e = (XElement)element;
		return e.prefix != null ? e.prefix + ":" + e.name : e.name;
	}

	@Override
	public String getAttributeNamespaceUri(Object attr) {
		return ((XAttributeName)((Triplet<?, ?, ?>)attr).second).namespace;
	}

	@Override
	public String getAttributeName(Object attr) {
		return ((XAttributeName)((Triplet<?, ?, ?>)attr).second).name;
	}

	@Override
	public String getAttributeQName(Object attr) {
		XAttributeName e = ((XAttributeName)((Triplet<?, ?, ?>)attr).second);
		return e.prefix != null ? e.prefix + ":" + e.name : e.name;
	}

	@Override
	public String getProcessingInstructionTarget(Object pi) {
		throw new UnsupportedOperationException();
	}

	@Override
	public String getProcessingInstructionData(Object pi) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean isDocument(Object object) {
		return (object instanceof XElement) && ((XElement)object).parent == null;
	}

	@Override
	public boolean isElement(Object object) {
		return object instanceof XElement;
	}

	@Override
	public boolean isAttribute(Object object) {
		return object instanceof Triplet<?, ?, ?>;
	}

	@Override
	public boolean isNamespace(Object object) {
		return object instanceof Map.Entry<?, ?>;
	}

	@Override
	public boolean isComment(Object object) {
		return false;
	}

	@Override
	public boolean isText(Object object) {
		return object instanceof Pair<?, ?>;
	}

	@Override
	public boolean isProcessingInstruction(Object object) {
		return false;
	}

	@Override
	public String getCommentStringValue(Object comment) {
		throw new UnsupportedOperationException();
	}

	@Override
	public String getElementStringValue(Object element) {
		String s = ((XElement)element).content;
		return s != null ? s : "";
	}

	@Override
	public String getAttributeStringValue(Object attr) {
		return (String)((Triplet<?, ?, ?>)attr).third;
	}

	@Override
	public String getNamespaceStringValue(Object ns) {
		return (String)((Map.Entry<?, ?>)ns).getValue();
	}

	@Override
	public String getTextStringValue(Object text) {
		return (String)((Pair<?, ?>)text).second;
	}

	@Override
	public String getNamespacePrefix(Object ns) {
		return (String)((Map.Entry<?, ?>)ns).getKey();
	}

	@Override
	public String translateNamespacePrefixToUri(String prefix, Object element) {
		Iterator<?> namespaces = getNamespaceAxisIterator(element);
		while (namespaces.hasNext()) {
			Map.Entry<?, ?> me = (Map.Entry<?, ?>)namespaces.next();
			if (me.getKey().equals(prefix)) {
				return me.getValue().toString();
			}
		}
		return null;
	}

	@Override
	public XPath parseXPath(String xpath) throws SAXPathException {
		return new BaseXPath(xpath, new XNavigator());
	}

	@Override
	public Object getElementById(Object contextNode, String elementId) {
		// FIXME not captured by XElement
		return null;
	}

	@Override
	public short getNodeType(Object node) {
        if (isElement(node)) {
            return Pattern.ELEMENT_NODE;
        } else 
        if (isAttribute(node)) {
            return Pattern.ATTRIBUTE_NODE;
        } else 
        if (isText(node)) {
            return Pattern.TEXT_NODE;
        } else 
        if (isNamespace(node)) {
            return Pattern.NAMESPACE_NODE;
        } else {
            return Pattern.UNKNOWN_NODE;
        }
	}

}
