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

import java.util.List;

import com.google.common.collect.Lists;

import edu.umd.cs.findbugs.annotations.Nullable;
import eu.advance.logistics.util.Strings;
import eu.advance.logistics.xml.typesystem.XElement;

/**
 * The input or output parameter description of a flow composite block element.
 * @author karnokd, 2011.06.21.
 */
public class AdvanceCompositeBlockParameterDescription extends
		AdvanceBlockParameterDescription {
	/** The user-entered documentation of this parameter. */
	@Nullable
	public String documentation;
	/** The user-entered keywords for easier finding of this parameter. */
	public final List<String> keywords = Lists.newArrayList();
	
	/**
	 * Load a parameter description from an XML element which conforms the {@code flow-description.xsd}.
	 * @param root the element of a input or output
	 */
	@Override
	public void load(XElement root) {
		super.load(root);
		documentation = root.get("documentation");
		String kw = root.get("keywords");
		if (kw != null) {
			keywords.addAll(Strings.trim(Strings.split(kw, ',')));
		}
	}
}
