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

package eu.advance.logistics.flow.engine.runtime;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URI;
import java.text.ParseException;
import java.util.Date;
import java.util.List;
import java.util.Map;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import eu.advance.logistics.flow.engine.xml.XElement;

/**
 * A basic interface to resolve typical Java types from the runtime type data.
 * @author akarnokd, 2011.11.18.
 * @param <T> the runtime type of the data to be resolved
 */
public interface DataResolver<T> {
	/**
	 * Extract the integer value from the suppied runtime object.
	 * @param e the element
	 * @return the value
	 */
	boolean getBoolean(T e);
	/**
	 * Extract the integer value from the suppied runtime object.
	 * @param e the element
	 * @return the value
	 */
	int getInt(T e);
	/**
	 * Extract the long value from the suppied runtime object.
	 * @param e the element
	 * @return the value
	 */
	long getLong(T e);
	/**
	 * Extract an arbitrarily large integer value from the suppied runtime object.
	 * @param e the element
	 * @return the value
	 */
	BigInteger getBigInteger(T e);
	/**
	 * Extract an arbitrarily large number value from the suppied runtime object.
	 * @param e the element
	 * @return the value
	 */
	BigDecimal getBigDecimal(T e);
	/**
	 * Extract an arbitrarily large number value from the suppied runtime object.
	 * @param e the element
	 * @return the value
	 */
	float getFloat(T e);
	/**
	 * Extract an arbitrarily large number value from the suppied runtime object.
	 * @param e the element
	 * @return the value
	 */
	double getDouble(T e);
	/**
	 * Extract a string from  the suppied runtime object.
	 * @param e the element
	 * @return the value
	 */
	String getString(T e);
	/**
	 * Extract a string from an the suppied runtime object.
	 * @param e the element
	 * @return the value
	 * @throws ParseException if the timestamp format is incorrect
	 */
	Date getTimestamp(T e) throws ParseException;
	/**
	 * Returns true if the supplied runtime object represents an integer type object.
	 * @param e the element
	 * @return true if the element is of type {@code advance:integer}
	 */
	boolean isInt(T e);
	/**
	 * Returns true if the supplied runtime object represents an string type object.
	 * @param e the element
	 * @return true if the element is of type {@code advance:string}
	 */
	boolean isString(T e);
	/**
	 * Creates the runtime representation of the value.
	 * @param value the value
	 * @return the XElement
	 */
	T create(boolean value);
	/**
	 * Creates the runtime representation of the value.
	 * @param value the value
	 * @return the runtime object
	 */
	T create(long value);
	/**
	 * Creates the runtime representation of the value.
	 * @param value the value
	 * @return the runtime object
	 */
	T create(BigInteger value);
	/**
	 * Creates the runtime representation of the value.
	 * @param value the value
	 * @return the runtime object
	 */
	T create(double value);
	/**
	 * Creates the runtime representation of the value.
	 * @param value the value
	 * @return the runtime object
	 */
	T create(BigDecimal value);
	/**
	 * Creates the runtime representation of the value.
	 * @param value the value
	 * @return the runtime object
	 */
	T create(String value);
	/**
	 * Creates the runtime representation of the value.
	 * @param value the value
	 * @return the runtime object
	 */
	T create(Date value);
	/**
	 * Creates the runtime representation of the value.
	 * @return the runtime object
	 */
	T createObject();
	/**
	 * Create a collection of Ts from the supplied values.
	 * @param ts the list of values
	 * @return the object representing the collection of the values
	 */
	T create(T... ts);
	/**
	 * Create a collection of Ts from the supplied values.
	 * @param ts the list of values
	 * @return the object representing the collection of the values
	 */
	T create(Iterable<? extends T> ts);
	/**
	 * Returns a list representation of the elements in the supplied collection.
	 * @param collection the collection
	 * @return the list of the collection elements
	 */
	List<T> getList(T collection);
	/**
	 * Retrieve an indexed item from the collection.
	 * @param collection the collection object
	 * @param index the index
	 * @return the collection content
	 */
	T getItem(T collection, int index);
	/**
	 * Returns a sequence of the items in the collection.
	 * @param collection the collection
	 * @return the items
	 */
	Iterable<T> getItems(T collection);
	/**
	 * Create a map from an array of key-value pairs.
	 * @param keyValue the key value pairs
	 * @return the object representing a map
	 */
	T createMap(T... keyValue);
	/**
	 * Create a map from the supplied subsequent key-value pairs.
	 * @param keyValue the sequence of key and value pairs
	 * @return the output map
	 */
	T createMap(Iterable<? extends T> keyValue);
	/**
	 * Create a map from an existing map.
	 * @param map the source map
	 * @return the output map
	 */
	T create(Map<T, T> map);
	/**
	 * Returns the contents of the supplied map as a Java Map.
	 * @param map the map
	 * @return the output map
	 */
	Map<T, T> getMap(T map);
	/**
	 * Create a runtime value from the supplied XML representation if possible.
	 * @param value the XML value
	 * @return the runtime object or null if can't be mapped
	 */
	@Nullable
	T get(@NonNull XElement value);
	/**
	 * Returns a list of the base type URIs.
	 * @return the list of base type URIs
	 */
	List<URI> baseTypes();
}
