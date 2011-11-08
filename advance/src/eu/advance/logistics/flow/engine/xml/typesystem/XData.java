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

package eu.advance.logistics.flow.engine.xml.typesystem;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.util.Arrays;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Convenience utils to extract or create values from various default ADVANCE XElement objects.
 * @author akarnokd, 2011.11.04.
 */
public final class XData {
	/** The logger. */
	protected static final Logger LOG = LoggerFactory.getLogger(XData.class);
	/**
	 * Utility class.
	 */
	private XData() {
		// TODO Auto-generated constructor stub
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
		return new XElement("string", value);
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
	public static XElement create() {
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
		return collection.children().get(index);
	}
	/**
	 * Returns an iterable sequence of the items in an advance:collection.
	 * @param collection the collection
	 * @return the iterable sequence
	 */
	public static Iterable<XElement> getItems(XElement collection) {
		return collection.children();
	}
	/**
	 * Create an advance:collection from the supplied items.
	 * @param items the sequence of items
	 * @return the XElement
	 */
	public static XElement create(Iterable<XElement> items) {
		XElement result = new XElement("collection");
		
		for (XElement e : items) {
			XElement item = e.add("item");
			item.attributes.putAll(e.attributes);
			item.content = e.content;
			for (XElement c : e) {
				item.add(c.copy());
			}
		}

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
}
