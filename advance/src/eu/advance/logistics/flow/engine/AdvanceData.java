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

package eu.advance.logistics.flow.engine;

import hu.akarnokd.reactive4java.base.Func1;
import hu.akarnokd.reactive4java.base.Option;
import hu.akarnokd.reactive4java.base.Pair;
import hu.akarnokd.utils.xml.XNElement;
import hu.akarnokd.utils.xml.XNElement.XAttributeName;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Objects;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import edu.umd.cs.findbugs.annotations.NonNull;
import eu.advance.logistics.flow.engine.runtime.DataResolver;


/**
 * Convenience utils to extract or create values from various default ADVANCE XNElement objects.
 * @author akarnokd, 2011.11.04.
 */
public final class AdvanceData implements DataResolver<XNElement> {
	/**
	 * The attribute name to store the original element namespace in collections and wrappers.
	 */
	private static final String ORIGINAL_NS = "original-ns";
	/**
	 * The attribute name to store the original element name in collections and wrappers.
	 */
	private static final String ORIGINAL_NAME = "original-name";
	/** The logger. */
	protected static final Logger LOG = LoggerFactory.getLogger(AdvanceData.class);
	/** The ADVANCE namespace. */
	public static final String ADVANCE_NS = "http://www.advance-logistics.eu";
	/**
	 * Utility class.
	 */
	public AdvanceData() {
	}
	@Override
	public boolean getBoolean(XNElement e) {
		return "true".equals(e.content) || "1".equals(e.content);
	}
	/**
	 * Extract the integer value from an {@code advance:integer} type object.
	 * @param e the element
	 * @return the value
	 */
	@Override
	public int getInt(XNElement e) {
		return Integer.parseInt(e.content);
	}
	/**
	 * Extract the long value from an {@code advance:integer} type object.
	 * @param e the element
	 * @return the value
	 */
	@Override
	public long getLong(XNElement e) {
		return Long.parseLong(e.content);
	}
	/**
	 * Extract an arbitrarily large integer value from an {@code advance:integer} type object.
	 * @param e the element
	 * @return the value
	 */
	@Override
	public BigInteger getBigInteger(XNElement e) {
		return new BigInteger(e.content);
	}
	/**
	 * Extract an arbitrarily large number value from an {@code advance:real} type object.
	 * @param e the element
	 * @return the value
	 */
	@Override
	public BigDecimal getBigDecimal(XNElement e) {
		return new BigDecimal(e.content);
	}
	/**
	 * Extract an arbitrarily large number value from an {@code advance:real} type object.
	 * @param e the element
	 * @return the value
	 */
	@Override
	public float getFloat(XNElement e) {
		return Float.parseFloat(e.content);
	}
	/**
	 * Extract an arbitrarily large number value from an {@code advance:real} type object.
	 * @param e the element
	 * @return the value
	 */
	@Override
	public double getDouble(XNElement e) {
		return Double.parseDouble(e.content);
	}
	/**
	 * Extract a string from an {@code advance:string} type object.
	 * @param e the element
	 * @return the value
	 */
	@Override
	public String getString(XNElement e) {
		return e.content;
	}
	/**
	 * Extract a string from an {@code advance:timestamp} type object.
	 * @param e the element
	 * @return the value
	 * @throws ParseException if the timestamp format is incorrect
	 */
	@Override
	public Date getTimestamp(XNElement e) throws ParseException {
		return XNElement.parseDateTime(e.content);
	}
	/**
	 * Returns true if the supplied XNElement represents an {@code advance:integer} type object.
	 * @param e the element
	 * @return true if the element is of type {@code advance:integer}
	 */
	@Override
	public boolean isInt(XNElement e) {
		return "integer".equals(e.name) && e.content != null;
	}
	/**
	 * Returns true if the supplied XNElement represents an {@code advance:string} type object.
	 * @param e the element
	 * @return true if the element is of type {@code advance:string}
	 */
	@Override
	public boolean isString(XNElement e) {
		return "string".equals(e.name) && e.content != null;
	}
	/**
	 * Creates a {@code advance:boolean} typed XNElement.
	 * @param value the value
	 * @return the XNElement
	 */
	@Override
	public XNElement create(boolean value) {
		return new XNElement("boolean", value);
	}
	/**
	 * Creates a {@code advance:integer} typed XNElement.
	 * @param value the value
	 * @return the XNElement
	 */
	@Override
	public XNElement create(long value) {
		return new XNElement("integer", value);
	}
	/**
	 * Creates a {@code advance:integer} typed XNElement.
	 * @param value the value
	 * @return the XNElement
	 */
	@Override
	public XNElement create(BigInteger value) {
		return new XNElement("integer", value);
	}
	/**
	 * Creates a {@code advance:real} typed XNElement.
	 * @param value the value
	 * @return the XNElement
	 */
	@Override
	public XNElement create(double value) {
		return new XNElement("real", value);
	}
	/**
	 * Creates a {@code advance:real} typed XNElement.
	 * @param value the value
	 * @return the XNElement
	 */
	@Override
	public XNElement create(BigDecimal value) {
		return new XNElement("real", value);
	}
	/**
	 * Creates a {@code advance:string} typed XNElement.
	 * @param value the value
	 * @return the XNElement
	 */
	@Override
	public XNElement create(String value) {
		return new XNElement("string", (Object)value);
	}
	/**
	 * Creates a {@code advance:timestamp} typed XNElement.
	 * @param value the value
	 * @return the XNElement
	 */
	@Override
	public XNElement create(Date value) {
		return new XNElement("timestamp", value);
	}
	/**
	 * Creates a {@code advance:object} typed XNElement.
	 * @return the XNElement
	 */
	@Override
	public XNElement createObject() {
		return new XNElement("object");
	}
	/** An object. */
	public static final URI OBJECT = uri("advance:object");
	/** A boolean. */
	public static final URI BOOLEAN = uri("advance:boolean");
	/** An integer. */
	public static final URI INTEGER = uri("advance:integer");
	/** A real. */
	public static final URI REAL = uri("advance:real");
	/** A string. */
	public static final URI STRING = uri("advance:string");
	/** A timestamp. */
	public static final URI TIMESTAMP = uri("advance:timestamp");
	/** A collection. */
	public static final URI COLLECTION = uri("advance:collection");
	/** A map. */
	public static final URI MAP = uri("advance:map");
	/** A map. */
	public static final URI PAIR = uri("advance:pair");
	/** A map. */
	public static final URI WRAPPER = uri("advance:wrapper");
	/** A type constructor. */
	public static final URI TYPE = uri("advance:type");
	// TODO ------------------------------------------------------------------
	// TODO only for the duration of the demo!
	/** A type constructor. */
	public static final URI PALLET = uri("advance:pallet");
	/** A type constructor. */
	public static final URI HALF_PALLET = uri("advance:half-pallet");
	/** A type constructor. */
	public static final URI FULL_PALLET = uri("advance:full-pallet");
	/** A type constructor. */
	public static final URI TRUCK = uri("advance:truck");
	// TODO ------------------------------------------------------------------
	/** The list of base types. */
	public static final List<URI> BASE_TYPES;
	static {
		List<URI> btList = Lists.newArrayList();
		for (Field f : AdvanceData.class.getDeclaredFields()) {
			if (URI.class.isAssignableFrom(f.getType())) {
				try {
					btList.add((URI)f.get(null));
				} catch (IllegalArgumentException ex) {
					LOG.error(ex.toString(), ex);
				} catch (IllegalAccessException ex) {
					LOG.error(ex.toString(), ex);
				}
			}
		}
		BASE_TYPES = Collections.unmodifiableList(btList);
	}
	/**
	 * Creates an URI object. Throws a runtime exception instead of the checked URISyntaxException.
	 * @param uri the URI string
	 * @return the URI object
	 */
	private static URI uri(String uri) {
		try {
			return new URI(uri);
		} catch (URISyntaxException ex) {
			LOG.error(ex.toString(), ex);
			throw new IllegalArgumentException(ex);
		}
	}
	/**
	 * Returns the indexth element from the advance:collection.
	 * @param collection the collection
	 * @param index the item index
	 * @return the element
	 */
	@Override
	public XNElement getItem(XNElement collection, int index) {
		return unrename(Iterables.get(collection.childrenWithName("item"), index));
	}
	/**
	 * Returns an iterable sequence of the items in an advance:collection.
	 * <p>Note: the returned iterable and its contents affect the source collection, e.g.,
	 * the iterator's remove() method removes the actual element from the collection.</p>
	 * @param collection the collection
	 * @return the iterable sequence
	 */
	@Override
	public Iterable<XNElement> getItems(XNElement collection) {
		return collection.childrenWithName("item");
	}
	/**
	 * Create an advance:collection from the supplied items.
	 * @param items the sequence of items
	 * @return the XNElement
	 */
	@Override
	public XNElement create(Iterable<? extends XNElement> items) {
		XNElement result = new XNElement("collection");
		
		for (XNElement e : items) {
			result.add(rename(e, "item"));
		}

		return result;
	}
	/**
	 * Creates a new element with the contents of the source element but with different element name.
	 * <p>Can be used to wrap elements into collections, which enforce strict element naming.</p>
	 * @param src the source element
	 * @param newName the new element name
	 * @return the new element
	 */
	public static XNElement rename(XNElement src, String newName) {
		XNElement result = new XNElement(newName);
		// save the original name and namespace
		addRename(result, src.name, src.namespace);
		
		copy(src, result);
		return result;
	}
	/**
	 * Add a new rename level to the element's rename list.
	 * @param dst the destination
	 * @param oldName the new name
	 * @param oldNamespace the new namespace
	 */
	protected static void addRename(XNElement dst, String oldName, String oldNamespace) {
		String n = dst.get(ORIGINAL_NAME, ADVANCE_NS);
		String ns = dst.get(ORIGINAL_NS, ADVANCE_NS);
		if (n == null) {
			dst.set(ORIGINAL_NAME, ADVANCE_NS, oldName);
			dst.set(ORIGINAL_NS, ADVANCE_NS, oldNamespace);
		} else {
			if (ns == null) {
				ns = "";
			}
			dst.set(ORIGINAL_NAME, ADVANCE_NS, n + "," + oldName);
			dst.set(ORIGINAL_NS, ADVANCE_NS, ns + "," + oldNamespace);
		}
	}
	/**
	 * Copy the content, attributes and children into the destination.
	 * @param source the source XML
	 * @param destination the destination XML
	 */
	protected static void copy(XNElement source, XNElement destination) {
		destination.attributes().putAll(source.attributes());
		destination.content = source.content;
		destination.set(source.get());
		for (XNElement c : source.children()) {
			destination.add(c.copy());
		}
	}
	/**
	 * Copy the content, attributes and children into the destination, except the rename-specific attributes.
	 * @param source the source XML
	 * @param destination the destination XML
	 */
	protected static void copy2(XNElement source, XNElement destination) {
		for (Map.Entry<XAttributeName, String> e : source.attributes().entrySet()) {
			XAttributeName an = e.getKey();
			if (!Objects.equal(an.namespace, ADVANCE_NS) 
					|| (!an.name.equals(ORIGINAL_NAME) && (!an.name.equals(ORIGINAL_NS)))) {
				destination.attributes().put(an, e.getValue());
			}
			
		}
		destination.content = source.content;
		destination.set(source.get());
		for (XNElement c : source.children()) {
			destination.add(c.copy());
		}
	}
	/**
	 * Creates a new element with the contents of the source element but with different element name.
	 * <p>Can be used to wrap elements into collections, which enforce strict element naming.</p>
	 * @param src the source element
	 * @param newName the new element name
	 * @param newNamespace the new namespace
	 * @return the new element
	 */
	public static XNElement rename(XNElement src, String newName, String newNamespace) {
		XNElement result = new XNElement(newName, newNamespace);
		
		// save the original name and namespace
		addRename(result, src.name, src.namespace);
		
		copy(src, result);
		
		return result;
	}
	/**
	 * Creates a copy of the given source XML with the saved original name and namespace restored.
	 * <p>Can be used to unwrap elements from various collections.</p>
	 * @param src the source XNElement
	 * @return the unrenamed element
	 */
	public static XNElement unrename(XNElement src) {
		Pair<String, String> pn = realName(src);
		XNElement result = new XNElement(pn.first, pn.second);
		copy2(src, result);
		return result;
	}
	/**
	 * Create an advance:collection from the supplied XNElement items.
	 * @param items the array of items
	 * @return the collection XNElement
	 */
	@Override
	public XNElement create(XNElement... items) {
		return create(Arrays.asList(items));
	}
	/**
	 * Creates a map from the list of the subsequent key and values.
	 * @param keyValuePairs the key and values
	 * @return the map XML
	 */
	@Override
	public XNElement createMap(XNElement... keyValuePairs) {
		return createMap(Arrays.asList(keyValuePairs));
	}
	/**
	 * Creates a map from the list of the subsequent key and values.
	 * @param keyValuePairs the sequence
	 * @return the map XML
	 */
	@Override
	public XNElement createMap(Iterable<? extends XNElement> keyValuePairs) {
		Iterator<? extends XNElement> it = keyValuePairs.iterator();
		Map<XNElement, XNElement> map = Maps.newLinkedHashMap();
		while (it.hasNext()) {
			XNElement key = it.next();
			if (it.hasNext()) {
				XNElement value = it.next();
				map.put(key, value);
			}
		}
		return create(map);
	}
	/**
	 * Creates a XML map from the supplied map.
	 * @param map the map of key value XML pairs.
	 * @return the map XML
	 */
	@Override
	public XNElement create(Map<XNElement, XNElement> map) {
		XNElement result = new XNElement("map");
		for (Map.Entry<XNElement, XNElement> e : map.entrySet()) {
			XNElement item = result.add("item");
			addRename(item, "pair", null);
			item.add(rename(e.getKey(), "first"));
			item.add(rename(e.getValue(), "second"));
		}
		return result;
	}
	/**
	 * Creates a pair XML from the supplied first and second element.
	 * @param first the first element
	 * @param second the second element
	 * @return the pair XML
	 */
	public XNElement createPair(XNElement first, XNElement second) {
		XNElement result = new XNElement("pair");
		result.add(rename(first, "first"));
		result.add(rename(second, "second"));
		return result;
	}
	/**
	 * Create a pair XML from the supplied pair of two XMLs.
	 * @param pair the pair of XMLs
	 * @return the pair XML
	 */
	public XNElement create(Pair<XNElement, XNElement> pair) {
		return createPair(pair.first, pair.second);
	}
	/**
	 * Extract the pair components of the supplied pair XML.
	 * @param pair the pair XML
	 * @return the pair of two XMLs
	 */
	public Pair<XNElement, XNElement> getPair(XNElement pair) {
		return Pair.of(unrename(pair.childElement("first")), unrename(pair.childElement("second")));
	}
	/**
	 * Extract the shared pair components of the supplied pair XML.
	 * <p>Note: this method does not create a copy of the pair elements, i.e., modification of the elements will result
	 * in modification of the original source and the getXPath() will return the full path.</p>
	 * @param pair the pair XML
	 * @return the pair of two XMLs
	 */
	public Pair<XNElement, XNElement> getSharedPair(XNElement pair) {
		return Pair.of(pair.childElement("first"), pair.childElement("second"));
	}
	/**
	 * Extract a mapping from the supplied XML map.
	 * @param map the XML map
	 * @return the shared mapping
	 */
	@Override
	public Map<XNElement, XNElement> getMap(XNElement map) {
		Map<XNElement, XNElement> result = Maps.newLinkedHashMap();
		for (XNElement item : getItems(map)) {
			result.put(unrename(item.childElement("first")), unrename(item.childElement("second")));
		}
		return result;
	}
	/**
	 * Extract a shared mapping from the supplied XML map.
	 * <p>Note: this method does not create a copy of the pair elements, i.e., modification of the elements will result
	 * in modification of the original source and the getXPath() will return the full path.</p>
	 * @param map the XML map
	 * @return the shared mapping
	 */
	public Map<XNElement, XNElement> getSharedMap(XNElement map) {
		Map<XNElement, XNElement> result = Maps.newLinkedHashMap();
		for (XNElement item : getItems(map)) {
			result.put(item.childElement("first"), item.childElement("second"));
		}
		return result;
	}
	/**
	 * Returns a list of the advance:collection items.
	 * @param collection the collection XML.
	 * @return the list of the collection items XML.
	 */
	@Override
	public List<XNElement> getList(XNElement collection) {
		List<XNElement> result = Lists.newArrayList();
		for (XNElement e : getItems(collection)) {
			result.add(unrename(e));
		}
		return result;
	}
	/**
	 * Returns a shared list of the advance:collection items.
	 * <p>Note: the changes made to the elements are affecting the original elements, but
	 * adding or removing elements from the list will not affect the original collection.</p>
	 * @param collection the XML collection
	 * @return the list of the XML elements inside
	 */
	public List<XNElement> getSharedList(XNElement collection) {
		return Lists.newArrayList(getItems(collection));
	}
	/**
	 * Returns the value of the specified key from the {@code advance:map}.
	 * <p>Note that due the way the map is represented, the key lookup is O(N).</p>
	 * @param map the map XML
	 * @param key the key XML
	 * @return the map value or null if not present
	 */
	public XNElement getMapValue(XNElement map, XNElement key) {
		for (XNElement item : getItems(map)) {
			XNElement k = item.childElement("first");
			if (wrappedEquals(k, key)) {
				return unrename(item.childElement("second"));
			}
		}
		return null;
	}
	/**
	 * Returns the list of keys of the specified value from the {@code advance:map}.
	 * @param map the map XML
	 * @param value the value to lookup
	 * @return the list of keys
	 */
	public List<XNElement> getMapKeys(XNElement map, XNElement value) {
		List<XNElement> result = Lists.newArrayList();
		for (XNElement item : getItems(map)) {
			XNElement v = item.childElement("second");
			if (wrappedEquals(v, value)) {
				result.add(unrename(item.childElement("first")));
			}
		}
		return result;
	}
	/**
	 * Convenience method to test if a key is in the {@code advance:map}.
	 * @param map the XML map
	 * @param key the key XML
	 * @return true if in the map
	 */
	public boolean containsKey(XNElement map, XNElement key) {
		return getMapValue(map, key) != null;
	}
	/**
	 * Convenience method to test if a value is in the {@code advance:map}.
	 * @param map the XML map
	 * @param value the value XML
	 * @return true if in the map
	 */
	public boolean containsValue(XNElement map, XNElement value) {
		for (XNElement item : getItems(map)) {
			XNElement v = item.childElement("second");
			if (wrappedEquals(v, value)) {
				return true;
			}
		}
		return false;
	}
	/**
	 * Convenience method to test if a value is in the {@code advance:map}.
	 * @param map the XML map
	 * @param key the key XML
	 * @param value the value XML
	 * @return true if in the map
	 */
	public boolean containsEntry(XNElement map, XNElement key, XNElement value) {
		for (XNElement item : getItems(map)) {
			XNElement k = item.childElement("first");
			XNElement v = item.childElement("second");
			if (wrappedEquals(k, key) && wrappedEquals(v, value)) {
				return true;
			}
		}
		return false;
	}
	/**
	 * Returns the real name of the XML element considering if it is wrapped.
	 * @param element the XML element
	 * @return the pair of name and namespace
	 */
	public static Pair<String, String> realName(XNElement element) {
		String n1 = element.get(ORIGINAL_NAME, ADVANCE_NS);
		String n2 = element.get(ORIGINAL_NS, ADVANCE_NS);
		if (n1 == null) {
			return Pair.of(element.name, element.namespace);
		}
		int idx1 = n1.lastIndexOf(',');
		if (n2 != null) {
			int idx2 = n2.lastIndexOf(',');
			return Pair.of(n1.substring(idx1 + 1), n2.substring(idx2 + 1));
		}
		return Pair.of(n1.substring(idx1 + 1), null);
	}
	/**
	 * Returns true if both XML are the same, considering the notion of original names if present.
	 * @param first the first XNElement
	 * @param second the second XNElement
	 * @return true if equals
	 */
	public static boolean wrappedEquals(XNElement first, XNElement second) {
		Pair<String, String> n1 = realName(first);
		Pair<String, String> n2 = realName(second);
		if (!n1.equals(n2)) {
			return false;
		}
		for (Map.Entry<XAttributeName, String> e : first.attributes().entrySet()) {
			XAttributeName an = e.getKey();
			if (!Objects.equal(an.namespace, ADVANCE_NS) || (!an.name.equals(ORIGINAL_NAME) && (!an.name.equals(ORIGINAL_NS)))) {
				if (second.get(an.name, an.namespace) == null) {
					return false;
				}
			}
		}
		for (Map.Entry<XAttributeName, String> e : second.attributes().entrySet()) {
			XAttributeName an = e.getKey();
			if (!Objects.equal(an.namespace, ADVANCE_NS) || (!an.name.equals(ORIGINAL_NAME) && (!an.name.equals(ORIGINAL_NS)))) {
				if (first.get(an.name, an.namespace) == null) {
					return false;
				}
			}
		}
		if (!Objects.equal(first.content, second.content)) {
			return false;
		}
		if (first.children().size() != second.children().size()) {
			return false;
		}
		for (int i = 0; i < first.children().size(); i++) {
			XNElement c1 = first.children().get(i);
			XNElement c2 = second.children().get(i);
			if (!wrappedEquals(c1, c2)) {
				return false;
			}
		}
		return true;
	}
	/**
	 * Create an {@code advance:collection} from all children of the container.
	 * @param container the container XML
	 * @param collection the output collection
	 * @return collection the output collection
	 */
	public static XNElement toCollection(XNElement container,
			XNElement collection) {
		for (XNElement e : container.children()) {
			collection.add(rename(e, "item"));
		}
		return collection;
	}
	/**
	 * Create an {@code advance:collection} from the specified children of the container.
	 * @param container the container XML
	 * @param itemFilter the function which tells for each of the children whether to include it in the output collection.
	 * @param collection the output collection
	 * @return collection the output collection
	 */
	public XNElement toCollection(XNElement container,
			Func1<XNElement, Boolean> itemFilter, XNElement collection) {
		for (XNElement e : container.children()) {
			if (itemFilter.invoke(e)) {
				collection.add(rename(e, "item"));
			}
		}
		return collection;
	}
	/**
	 * Takes an {@code advance:collection} and turns it into a regular container by unwrapping the items into their original names.
	 * @param collection the source collection
	 * @param container the output of the elements
	 * @return the output container
	 */
	@NonNull 
	public XNElement toContainer(@NonNull XNElement collection, XNElement container) {
		for (XNElement e : getItems(collection)) {
			container.add(unrename(e));
		}
		return container;
	}
	/**
	 * Convenience method to create a collection by filtering for elements by the supplied name, ignoring any namespace.
	 * @param container the source container
	 * @param itemName the item name to filter
	 * @param collection the output collection
	 * @return the output collection
	 */
	public XNElement toCollection(XNElement container, final String itemName, XNElement collection) {
		return toCollection(container, new Func1<XNElement, Boolean>() {
			@Override
			public Boolean invoke(XNElement param1) {
				return Objects.equal(itemName, param1.name);
			}
		}, collection);
	}
	/**
	 * Convenience method to create a collection by filtering for elements by the supplied name and namespace.
	 * @param container the source container
	 * @param itemName the item name to filter
	 * @param itemNamespace the item namespace
	 * @param collection the output collection
	 * @return the output collection
	 */
	public XNElement toCollection(XNElement container, final String itemName, final String itemNamespace, XNElement collection) {
		return toCollection(container, new Func1<XNElement, Boolean>() {
			@Override
			public Boolean invoke(XNElement param1) {
				return Objects.equal(itemName, param1.name) 
						&& Objects.equal(itemNamespace, param1.namespace);
			}
		}, collection);
	}
	@Override
	public XNElement get(XNElement value) {
		return value;
	}
	@Override
	public List<URI> baseTypes() {
		return BASE_TYPES;
	}
	/** The none option. */
	private static final XNElement NONE = new XNElement("option");
	/** @return creates a NONE option. */
	public static XNElement createNone() {
		return NONE;
	}
	/**
	 * Creates a some option of the given value.
	 * @param value the value to wrap
	 * @return the SOME option
	 */
	public static XNElement createSome(XNElement value) {
		XNElement some = new XNElement("option");
		some.add(rename(value, "value"));
		return some;
	}
	/**
	 * Returns the wrapped value from an option XML or none.
	 * @param value the value to unwrap
	 * @return the option
	 */
	public static Option<XNElement> getOption(XNElement value) {
		if (value.children().size() == 1) {
			return Option.some(value.children().get(0).copy());
		}
		return Option.none();
	}
	/**
	 * Creates an XML option from the supplied option value.
	 * @param value the optioned value
	 * @return the XNElement representing the option
	 */
	public static XNElement createOption(Option<XNElement> value) {
		if (Option.isSome(value)) {
			return createSome(value.value());
		}
		return NONE;
	}
}
