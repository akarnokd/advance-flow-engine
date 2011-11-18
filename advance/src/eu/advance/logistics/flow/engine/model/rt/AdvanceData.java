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

package eu.advance.logistics.flow.engine.model.rt;

import hu.akarnokd.reactive4java.base.Func1;
import hu.akarnokd.reactive4java.base.Pair;

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
import eu.advance.logistics.flow.engine.xml.typesystem.XElement;
import eu.advance.logistics.flow.engine.xml.typesystem.XElement.XAttributeName;


/**
 * Convenience utils to extract or create values from various default ADVANCE XElement objects.
 * @author akarnokd, 2011.11.04.
 */
public final class AdvanceData {
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
	private AdvanceData() {
	}
	/**
	 * Extract the integer value from an {@code advance:integer} type object.
	 * @param e the element
	 * @return the value
	 */
	public static boolean getBoolean(XElement e) {
		return "true".equals(e.content) || "1".equals(e.content);
	}
	/**
	 * Extract the integer value from an {@code advance:integer} type object.
	 * @param e the element
	 * @return the value
	 */
	public static int getInt(XElement e) {
		return Integer.parseInt(e.content);
	}
	/**
	 * Extract the long value from an {@code advance:integer} type object.
	 * @param e the element
	 * @return the value
	 */
	public static long getLong(XElement e) {
		return Long.parseLong(e.content);
	}
	/**
	 * Extract an arbitrarily large integer value from an {@code advance:integer} type object.
	 * @param e the element
	 * @return the value
	 */
	public static BigInteger getBigInteger(XElement e) {
		return new BigInteger(e.content);
	}
	/**
	 * Extract an arbitrarily large number value from an {@code advance:real} type object.
	 * @param e the element
	 * @return the value
	 */
	public static BigDecimal getBigDecimal(XElement e) {
		return new BigDecimal(e.content);
	}
	/**
	 * Extract an arbitrarily large number value from an {@code advance:real} type object.
	 * @param e the element
	 * @return the value
	 */
	public static float getFloat(XElement e) {
		return Float.parseFloat(e.content);
	}
	/**
	 * Extract an arbitrarily large number value from an {@code advance:real} type object.
	 * @param e the element
	 * @return the value
	 */
	public static double getDouble(XElement e) {
		return Double.parseDouble(e.content);
	}
	/**
	 * Extract a string from an {@code advance:string} type object.
	 * @param e the element
	 * @return the value
	 */
	public static String getString(XElement e) {
		return e.content;
	}
	/**
	 * Extract a string from an {@code advance:timestamp} type object.
	 * @param e the element
	 * @return the value
	 * @throws ParseException if the timestamp format is incorrect
	 */
	public static Date getTimestamp(XElement e) throws ParseException {
		return XElement.parseDateTime(e.content);
	}
	/**
	 * Returns true if the supplied XElement represents an {@code advance:integer} type object.
	 * @param e the element
	 * @return true if the element is of type {@code advance:integer}
	 */
	public static boolean isInt(XElement e) {
		return "integer".equals(e.name) && e.content != null;
	}
	/**
	 * Returns true if the supplied XElement represents an {@code advance:string} type object.
	 * @param e the element
	 * @return true if the element is of type {@code advance:string}
	 */
	public static boolean isString(XElement e) {
		return "string".equals(e.name) && e.content != null;
	}
	/**
	 * Creates a {@code advance:boolean} typed XElement.
	 * @param value the value
	 * @return the XElement
	 */
	public static XElement create(boolean value) {
		return new XElement("boolean", value);
	}
	/**
	 * Creates a {@code advance:integer} typed XElement.
	 * @param value the value
	 * @return the XElement
	 */
	public static XElement create(long value) {
		return new XElement("integer", value);
	}
	/**
	 * Creates a {@code advance:integer} typed XElement.
	 * @param value the value
	 * @return the XElement
	 */
	public static XElement create(BigInteger value) {
		return new XElement("integer", value);
	}
	/**
	 * Creates a {@code advance:real} typed XElement.
	 * @param value the value
	 * @return the XElement
	 */
	public static XElement create(double value) {
		return new XElement("real", value);
	}
	/**
	 * Creates a {@code advance:real} typed XElement.
	 * @param value the value
	 * @return the XElement
	 */
	public static XElement create(BigDecimal value) {
		return new XElement("real", value);
	}
	/**
	 * Creates a {@code advance:string} typed XElement.
	 * @param value the value
	 * @return the XElement
	 */
	public static XElement create(String value) {
		return new XElement("string", (Object)value);
	}
	/**
	 * Creates a {@code advance:timestamp} typed XElement.
	 * @param value the value
	 * @return the XElement
	 */
	public static XElement create(Date value) {
		return new XElement("timestamp", value);
	}
	/**
	 * Creates a {@code advance:object} typed XElement.
	 * @return the XElement
	 */
	public static XElement createObject() {
		return new XElement("object");
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
	public static XElement getItem(XElement collection, int index) {
		return unrename(Iterables.get(collection.childrenWithName("item"), index));
	}
	/**
	 * Returns an iterable sequence of the items in an advance:collection.
	 * <p>Note: the returned iterable and its contents affect the source collection, e.g.,
	 * the iterator's remove() method removes the actual element from the collection.</p>
	 * @param collection the collection
	 * @return the iterable sequence
	 */
	public static Iterable<XElement> getItems(XElement collection) {
		return collection.childrenWithName("item");
	}
	/**
	 * Create an advance:collection from the supplied items.
	 * @param items the sequence of items
	 * @return the XElement
	 */
	public static XElement create(Iterable<XElement> items) {
		XElement result = new XElement("collection");
		
		for (XElement e : items) {
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
	public static XElement rename(XElement src, String newName) {
		XElement result = new XElement(newName);
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
	protected static void addRename(XElement dst, String oldName, String oldNamespace) {
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
	protected static void copy(XElement source, XElement destination) {
		destination.attributes().putAll(source.attributes());
		destination.content = source.content;
		destination.set(source.get());
		for (XElement c : source.children()) {
			destination.add(c.copy());
		}
	}
	/**
	 * Copy the content, attributes and children into the destination, except the rename-specific attributes.
	 * @param source the source XML
	 * @param destination the destination XML
	 */
	protected static void copy2(XElement source, XElement destination) {
		for (Map.Entry<XAttributeName, String> e : source.attributes().entrySet()) {
			XAttributeName an = e.getKey();
			if (!Objects.equal(an.namespace, ADVANCE_NS) 
					|| (!an.name.equals(ORIGINAL_NAME) && (!an.name.equals(ORIGINAL_NS)))) {
				destination.attributes().put(an, e.getValue());
			}
			
		}
		destination.content = source.content;
		destination.set(source.get());
		for (XElement c : source.children()) {
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
	public static XElement rename(XElement src, String newName, String newNamespace) {
		XElement result = new XElement(newName, newNamespace);
		
		// save the original name and namespace
		addRename(result, src.name, src.namespace);
		
		copy(src, result);
		
		return result;
	}
	/**
	 * Creates a copy of the given source XML with the saved original name and namespace restored.
	 * <p>Can be used to unwrap elements from various collections.</p>
	 * @param src the source XElement
	 * @return the unrenamed element
	 */
	public static XElement unrename(XElement src) {
		Pair<String, String> pn = realName(src);
		XElement result = new XElement(pn.first, pn.second);
		copy2(src, result);
		return result;
	}
	/**
	 * Create an advance:collection from the supplied XElement items.
	 * @param items the array of items
	 * @return the collection XElement
	 */
	public static XElement create(XElement... items) {
		return create(Arrays.asList(items));
	}
	/**
	 * Creates a map from the list of the subsequent key and values.
	 * @param keyValuePairs the key and values
	 * @return the map XML
	 */
	public static XElement createMap(XElement... keyValuePairs) {
		return createMap(Arrays.asList(keyValuePairs));
	}
	/**
	 * Creates a map from the list of the subsequent key and values.
	 * @param keyValuePairs the sequence
	 * @return the map XML
	 */
	public static XElement createMap(Iterable<XElement> keyValuePairs) {
		Iterator<XElement> it = keyValuePairs.iterator();
		Map<XElement, XElement> map = Maps.newLinkedHashMap();
		while (it.hasNext()) {
			XElement key = it.next();
			if (it.hasNext()) {
				XElement value = it.next();
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
	public static XElement create(Map<XElement, XElement> map) {
		XElement result = new XElement("map");
		for (Map.Entry<XElement, XElement> e : map.entrySet()) {
			XElement item = result.add("item");
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
	public static XElement createPair(XElement first, XElement second) {
		XElement result = new XElement("pair");
		result.add(rename(first, "first"));
		result.add(rename(second, "second"));
		return result;
	}
	/**
	 * Create a pair XML from the supplied pair of two XMLs.
	 * @param pair the pair of XMLs
	 * @return the pair XML
	 */
	public static XElement create(Pair<XElement, XElement> pair) {
		return createPair(pair.first, pair.second);
	}
	/**
	 * Extract the pair components of the supplied pair XML.
	 * @param pair the pair XML
	 * @return the pair of two XMLs
	 */
	public static Pair<XElement, XElement> getPair(XElement pair) {
		return Pair.of(unrename(pair.childElement("first")), unrename(pair.childElement("second")));
	}
	/**
	 * Extract the shared pair components of the supplied pair XML.
	 * <p>Note: this method does not create a copy of the pair elements, i.e., modification of the elements will result
	 * in modification of the original source and the getXPath() will return the full path.</p>
	 * @param pair the pair XML
	 * @return the pair of two XMLs
	 */
	public static Pair<XElement, XElement> getSharedPair(XElement pair) {
		return Pair.of(pair.childElement("first"), pair.childElement("second"));
	}
	/**
	 * Extract a mapping from the supplied XML map.
	 * @param map the XML map
	 * @return the shared mapping
	 */
	public static Map<XElement, XElement> getMap(XElement map) {
		Map<XElement, XElement> result = Maps.newLinkedHashMap();
		for (XElement item : getItems(map)) {
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
	public static Map<XElement, XElement> getSharedMap(XElement map) {
		Map<XElement, XElement> result = Maps.newLinkedHashMap();
		for (XElement item : getItems(map)) {
			result.put(item.childElement("first"), item.childElement("second"));
		}
		return result;
	}
	/**
	 * Returns a list of the advance:collection items.
	 * @param collection the collection XML.
	 * @return the list of the collection items XML.
	 */
	public static List<XElement> getList(XElement collection) {
		List<XElement> result = Lists.newArrayList();
		for (XElement e : getItems(collection)) {
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
	public static List<XElement> getSharedList(XElement collection) {
		return Lists.newArrayList(getItems(collection));
	}
	/**
	 * Returns the value of the specified key from the {@code advance:map}.
	 * <p>Note that due the way the map is represented, the key lookup is O(N).</p>
	 * @param map the map XML
	 * @param key the key XML
	 * @return the map value or null if not present
	 */
	public static XElement getMapValue(XElement map, XElement key) {
		for (XElement item : getItems(map)) {
			XElement k = item.childElement("first");
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
	public static List<XElement> getMapKeys(XElement map, XElement value) {
		List<XElement> result = Lists.newArrayList();
		for (XElement item : getItems(map)) {
			XElement v = item.childElement("second");
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
	public static boolean containsKey(XElement map, XElement key) {
		return getMapValue(map, key) != null;
	}
	/**
	 * Convenience method to test if a value is in the {@code advance:map}.
	 * @param map the XML map
	 * @param value the value XML
	 * @return true if in the map
	 */
	public static boolean containsValue(XElement map, XElement value) {
		for (XElement item : getItems(map)) {
			XElement v = item.childElement("second");
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
	public static boolean containsEntry(XElement map, XElement key, XElement value) {
		for (XElement item : getItems(map)) {
			XElement k = item.childElement("first");
			XElement v = item.childElement("second");
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
	public static Pair<String, String> realName(XElement element) {
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
	 * @param first the first XElement
	 * @param second the second XElement
	 * @return true if equals
	 */
	public static boolean wrappedEquals(XElement first, XElement second) {
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
			XElement c1 = first.children().get(i);
			XElement c2 = second.children().get(i);
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
	public static XElement toCollection(XElement container,
			XElement collection) {
		for (XElement e : container.children()) {
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
	public static XElement toCollection(XElement container,
			Func1<XElement, Boolean> itemFilter, XElement collection) {
		for (XElement e : container.children()) {
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
	public static XElement toContainer(@NonNull XElement collection, XElement container) {
		for (XElement e : getItems(collection)) {
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
	public static XElement toCollection(XElement container, final String itemName, XElement collection) {
		return toCollection(container, new Func1<XElement, Boolean>() {
			@Override
			public Boolean invoke(XElement param1) {
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
	public static XElement toCollection(XElement container, final String itemName, final String itemNamespace, XElement collection) {
		return toCollection(container, new Func1<XElement, Boolean>() {
			@Override
			public Boolean invoke(XElement param1) {
				return Objects.equal(itemName, param1.name) 
						&& Objects.equal(itemNamespace, param1.namespace);
			}
		}, collection);
		
	}
	/**
	 * Test program.
	 * @param args no arguments
	 */
	public static void main(String[] args) {
		XElement c0 = create();
		XElement c1 = create(c0);
		XElement c2 = create(c1);
		System.out.println(c2);
		
		XElement c = create(create("abc"), create(1), create(true));
		System.out.println(c);
		XElement cont = toContainer(c, new XElement("container"));
		System.out.println(cont);
		System.out.println(toCollection(cont, create()));
		
		XElement map = createMap(create("abc"), create(1));
		System.out.println(map);
		
	}
}
