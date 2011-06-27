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

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import eu.advance.logistics.util.Strings;
import eu.advance.logistics.xml.typesystem.XElement;

/**
 * The block reference for the {@code flow-description.xsd} used within composite blocks.
 * @author karnokd, 2011.06.21.
 */
public class AdvanceBlockReference {
	/** The unique identifier of this block among the current level of blocks. */
	@NonNull
	public String id;
	/** The block type identifier referencing a block description. */
	@NonNull
	public String type;
	/** The user-entered documentation of this composite block. */
	@Nullable
	public String documentation;
	/** The parent block of this composite block. */
	public AdvanceCompositeBlock parent;
	/** The user-entered keywords for easier finding of this block. */
	public final List<String> keywords = Lists.newArrayList();
	/**
	 * Load a block reference from an XML element which conforms the {@code flow-description.xsd}.
	 * @param root the element of a input or output
	 */
	public void load(XElement root) {
		id = root.get("id");
		type = root.get("type");
		documentation = root.get("documentation");
		String kw = root.get("keywords");
		if (kw != null) {
			keywords.addAll(Strings.trim(Strings.split(kw, ',')));
		}
	}
}
