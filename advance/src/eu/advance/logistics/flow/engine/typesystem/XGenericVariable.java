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

import java.util.Iterator;
import java.util.List;

import com.google.common.collect.Lists;

import eu.advance.logistics.flow.engine.xml.XElement;
import eu.advance.logistics.flow.engine.xml.XSerializable;


/**
 * An XML type variable.
 * @author akarnokd, 2011.11.15.
 */
public class XGenericVariable implements XSerializable {
	/** The variable name. */
	public String name;
	/** Does this have an upper bound? */
	public boolean isUpper;
	/** The bounds. */
	public final List<XGenericType> bounds = Lists.newArrayList();
	@Override
	public String toString() {
		StringBuilder b = new StringBuilder();
		b.append(name);
		if (!bounds.isEmpty()) {
			if (isUpper) {
				b.append(" super ");
			} else {
				b.append(" extends ");
			}
		}
		for (int i = 0; i < bounds.size(); i++) {
			if (i > 0) {
				b.append(" & ");
			}
			b.append(bounds.get(i));
		}
		return b.toString();
	}
	/**
	 * Create a deep copy of this variable.
	 * @return the copy
	 */
	public XGenericVariable copy() {
		XGenericVariable result = new XGenericVariable();
		result.name = name;
		result.isUpper = isUpper;
		for (XGenericType gt : bounds) {
			result.bounds.add(gt.copy());
		}
		return result;
	}
	@Override
	public void load(XElement source) {
		name = source.get("name");
		Iterator<XElement> ub = source.childrenWithName("upper-bound").iterator();
		Iterator<XElement> lb = source.childrenWithName("upper-bound").iterator();
		if (ub.hasNext()) {
			isUpper = true;
		}
		Iterator<XElement> it = isUpper ? ub : lb;
		while (it.hasNext()) {
			XGenericType ta = new XGenericType();
			ta.load(it.next());
			bounds.add(ta);
			return;
		}
	}
	@Override
	public void save(XElement destination) {
		throw new UnsupportedOperationException();
	}
}