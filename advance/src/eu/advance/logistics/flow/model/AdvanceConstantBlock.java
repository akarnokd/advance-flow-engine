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

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import eu.advance.logistics.util.Strings;
import eu.advance.logistics.xml.typesystem.XElement;

/**
 * @author karnokd, 2011.06.24.
 */
public class AdvanceConstantBlock {
	/** The unique identifier of this block among the current level of blocks. */
	@NonNull
	public String id;
	/** The content type of this block. */
	@NonNull
	public URI type;
	/** Optional display text for this attribute. Can be used as a key into a translation table. */
	@Nullable
	public String displayName;
	/** The constant value. */
	@NonNull
	public XElement value;
	/** The user-entered documentation of this parameter. */
	@Nullable
	public String documentation;
	/** The user-entered keywords for easier finding of this parameter. */
	public final List<String> keywords = Lists.newArrayList();
	/**
	 * Load a parameter description from an XML element which conforms the {@code block-description.xsd}.
	 * @param root the root element of an input/output node.
	 */
	public void load(XElement root) {
		id = root.get("id");
		displayName = root.get("displayname");
		String t = root.get("type");
		if (t != null) {
			try {
				type = new URI(t);
			} catch (URISyntaxException ex) {
				throw new RuntimeException(ex);
			}
		}
		documentation = root.get("documentation");
		String kw = root.get("keywords");
		if (kw != null) {
			keywords.addAll(Strings.trim(Strings.split(kw, ',')));
		}
		value = root.children().iterator().next();
	}
}
