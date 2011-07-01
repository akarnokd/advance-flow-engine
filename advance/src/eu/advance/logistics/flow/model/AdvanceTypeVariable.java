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

package eu.advance.logistics.flow.model;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

import com.google.common.collect.Lists;

import eu.advance.logistics.xml.typesystem.XElement;

/**
 * The type variable definition of an Advance block.
 * It allows
 * @author karnokd, 2011.07.01.
 */
public class AdvanceTypeVariable {
	/** The type parameter name. */
	public String name;
	/** The upper bounds of this type parameter, e.g., T super SomeObject1 &amp; SomeObject2. */
	public final List<AdvanceType> bounds = Lists.newArrayList();
	/** Indicator if the bounds are representing the upper bound. */
	public boolean isUpperBound;
	/** The documentation explaining this type variable. */
	public URI documentation;
	/**
	 * Load the contents from an XML element with a schema of <code>block-description.xsd</code>.
	 * @param root the root element
	 */
	public void load(XElement root) {
		name = root.get("name");
		for (XElement ub : root.childrenWithName("upper-bound")) {
			isUpperBound = true;
			AdvanceType t = new AdvanceType();
			t.load(ub);
			bounds.add(t);
		}
		for (XElement lb : root.childrenWithName("lower-bound")) {
			if (isUpperBound) {
				throw new IllegalArgumentException("Type variable has multiple bound types! " + root);
			}
			AdvanceType t = new AdvanceType();
			t.load(lb);
			bounds.add(t);
		}
		String u = root.get("documentation");
		if (u != null) {
			try {
				documentation = new URI(u);
			} catch (URISyntaxException ex) {
				throw new RuntimeException(ex);
			}
		}
	}
}