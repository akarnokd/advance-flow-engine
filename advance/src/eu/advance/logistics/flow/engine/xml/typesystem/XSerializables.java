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

import hu.akarnokd.reactive4java.base.Func0;
import hu.akarnokd.reactive4java.base.Func1;
import hu.akarnokd.reactive4java.reactive.Observable;
import hu.akarnokd.reactive4java.reactive.Reactive;

import java.util.List;

import com.google.common.collect.Lists;


/**
 * Utility classes to handle XSerializable object creation and conversion.
 * @author karnokd, 2011.09.29.
 */
public final class XSerializables {
	/**
	 * Utility class.
	 */
	private XSerializables() {
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
		return createUpdate(itemName, source);
	}
	/**
	 * Convert a sequence of XElements into an XSerializable object instance via help of the {@code creator} function.
	 * @param <T> the object type
	 * @param source the source stream of XElements
	 * @param creator the function to create the XSerializable object
	 * @return the sequence of XSerializable objects
	 */
	public static <T extends XSerializable> Observable<T> parse(Observable<XElement> source, final Func0<T> creator) {
		return Reactive.select(source, new Func1<XElement, T>() {
			@Override
			public T invoke(XElement param1) {
				T result = creator.invoke();
				result.load(param1);
				return result;
			}
		});
	}
	/**
	 * Convert the sequence of XSerializable objects into XElements.
	 * @param <T> the object type
	 * @param source the source of XSerializable objects
	 * @param elementName the element name to use for the generated XElements
	 * @param namespace the target namespace
	 * @return the sequence of XElements
	 */
	public static <T extends XSerializable> Observable<XElement> serialize(
			Observable<T> source, 
			final String elementName, final String namespace) {
		return Reactive.select(source, new Func1<T, XElement>() {
			@Override
			public XElement invoke(T param1) {
				XElement e = new XElement(elementName);
				e.namespace = namespace;
				param1.save(e);
				return e;
			}
		});
	}
	/**
	 * Convert the sequence of XSerializable objects into XElements.
	 * @param <T> the object type
	 * @param source the source of XSerializable objects
	 * @param elementName the element name to use for the generated XElements
	 * @return the sequence of XElements
	 */
	public static <T extends XSerializable> Observable<XElement> serialize(
			Observable<T> source, 
			final String elementName) {
		return Reactive.select(source, new Func1<T, XElement>() {
			@Override
			public XElement invoke(T param1) {
				XElement e = new XElement(elementName);
				param1.save(e);
				return e;
			}
		});
	}
	/**
	 * Parse a sequence of a sequence of XElements (e.g., list of XElements) into a sequence.
	 * of list of an XSerializable objects
	 * @param <T> the object type
	 * @param source the source sequence
	 * @param creator the object creator
	 * @return the parsed sequence
	 */
	public static <T extends XSerializable> Observable<List<T>> parseList(
			Observable<? extends Iterable<XElement>> source, 
			final Func0<T> creator) {
		return Reactive.select(source, new Func1<Iterable<XElement>, List<T>>() {
			@Override
			public List<T> invoke(Iterable<XElement> param1) {
				List<T> result = Lists.newArrayList();
				for (XElement e : param1) {
					T obj = creator.invoke();
					obj.load(e);
					result.add(obj);
				}
				return result;
			}
		});
	}
	/**
	 * Serialize a sequence of sequence of XSerializable objects into a sequence of list
	 * of XElements.
	 * @param <T> the object type
	 * @param source the source sequence
	 * @param elementName the element name 
	 * @return the sequence of list of XElements
	 */
	public static <T extends XSerializable> Observable<List<XElement>> serializeList(
			Observable<? extends Iterable<T>> source,
			final String elementName
			) {
		return Reactive.select(source, new Func1<Iterable<T>, List<XElement>>() {
			@Override
			public List<XElement> invoke(Iterable<T> param1) {
				List<XElement> result = Lists.newArrayList();
				for (T e : param1) {
					XElement x = new XElement(elementName);
					e.save(x);
					result.add(x);
				}
				return result;
			}
		});
	}
	/**
	 * Serialize a sequence of sequence of XSerializable objects into a sequence of list
	 * of XElements.
	 * @param <T> the object type
	 * @param source the source sequence
	 * @param elementName the element name 
	 * @param namespace the element namespace
	 * @return the sequence of list of XElements
	 */
	public static <T extends XSerializable> Observable<List<XElement>> serializeList(
			Observable<? extends Iterable<T>> source,
			final String elementName,
			final String namespace
			) {
		return Reactive.select(source, new Func1<Iterable<T>, List<XElement>>() {
			@Override
			public List<XElement> invoke(Iterable<T> param1) {
				List<XElement> result = Lists.newArrayList();
				for (T e : param1) {
					XElement x = new XElement(elementName);
					x.namespace = namespace;
					e.save(x);
					result.add(x);
				}
				return result;
			}
		});
	}
}