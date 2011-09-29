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

package eu.advance.logistics.flow.engine.api.impl;

import hu.akarnokd.reactive4java.base.Func0;

import java.util.List;

import com.google.common.collect.Lists;

import eu.advance.logistics.flow.engine.model.XSerializable;
import eu.advance.logistics.flow.engine.xml.typesystem.XElement;

/**
 * Utility classes to create and parse XMLs used in the remote HTTP communications.
 * @author karnokd, 2011.09.29.
 */
public final class HttpRemoteUtils {

	/**
	 * Utility class.
	 */
	private HttpRemoteUtils() {
	}
	/**
	 * Create a request with the given function and name-value pairs.
	 * @param function the remote function name
	 * @param nameValue the array of String name and Object attributes.
	 * @return the request XML
	 */
	public static XElement createRequest(String function, Object... nameValue) {
		XElement result = new XElement(function);
		for (int i = 0; i < nameValue.length; i += 2) {
			result.set((String)nameValue[i], nameValue[i + 1]);
		}
		return result;
	}
	/**
	 * Create an update request and store the contents of the object into it.
	 * @param function the remote function name
	 * @param object the object to store.
	 * @return the request XML
	 */
	public static XElement createUpdate(String function, XSerializable object) {
		XElement result = new XElement(function);
		object.save(result);
		return result;
	}
	/**
	 * Parses an container for the given itemName elements and loads them into the
	 * given Java XSerializable object.
	 * @param <T> the element object type
	 * @param container the container XElement
	 * @param itemName the item name
	 * @param creator the function to create Ts
	 * @return the list of elements
	 */
	public static <T extends XSerializable> List<T> parseList(XElement container, 
			String itemName, Func0<T> creator) {
		List<T> result = Lists.newArrayList();
		for (XElement e : container.childrenWithName(itemName)) {
			T obj = creator.invoke();
			obj.load(e);
			result.add(obj);
		}
		return result;
	}
	/**
	 * Create an XSerializable object through the {@code creator} function
	 * and load it from the {@code item}.
	 * @param <T> the XSerializable object
	 * @param item the item to load from
	 * @param creator the function to create Ts
	 * @return the created and loaded object
	 */
	public static <T extends XSerializable> T parseItem(XElement item, Func0<T> creator) {
		T result = creator.invoke();
		result.load(item);
		return result;
	}
	/**
	 * Create an XElement with the given name and items stored from the source sequence.
	 * @param container the container name
	 * @param item the item name
	 * @param source the source of items
	 * @return the list in XElement
	 */
	public static XElement storeList(String container, String item, Iterable<? extends XSerializable> source) {
		XElement result = new XElement(container);
		for (XSerializable e : source) {
			e.save(result.add(item));
		}
		return result;
	}
	/**
	 * Store the value of a single serializable object with the given element name.
	 * @param itemName the item element name
	 * @param source the object to store
	 * @return the created XElement
	 */
	public static XElement storeItem(String itemName, XSerializable source) {
		XElement result = new XElement(itemName);
		source.save(result);
		return result;
	}

}
