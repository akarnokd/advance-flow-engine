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

package eu.advance.logistics.flow.engine.model.fd;

import hu.akarnokd.utils.xml.XNElement;
import hu.akarnokd.utils.xml.XNSerializable;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

import com.google.common.collect.Lists;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import eu.advance.logistics.flow.engine.typesystem.XType;
import eu.advance.logistics.flow.engine.util.Strings;

/**
 * Represents a constant block in the flow descriptor.
 * @author akarnokd, 2011.06.24.
 */
public class AdvanceConstantBlock implements XNSerializable {
	/** The unique identifier of this block among the current level of blocks. */
	@NonNull
	public String id;
	/** The content type of this block. */
	@NonNull
	public String typeString;
	/** The type. */
	@NonNull
	public XType type;
	/** Optional display text for this attribute. Can be used as a key into a translation table. */
	@Nullable
	public String displayName;
	/** The constant value. */
	@NonNull
	public XNElement value;
	/** The user-entered documentation of this parameter. */
	@Nullable
	public String documentation;
	/** The user-entered keywords for easier finding of this parameter. */
	public final List<String> keywords = Lists.newArrayList();
	/** The visual properties for the Flow Editor. */
	public final AdvanceBlockVisuals visuals = new AdvanceBlockVisuals();
	/**
	 * Load a parameter description from an XML element which conforms the {@code block-description.xsd}.
	 * @param source the root element of an input/output node.
	 */
	@Override
	public void load(XNElement source) {
		id = source.get("id");
		displayName = source.get("displayname");
		typeString = source.get("type");
		documentation = source.get("documentation");
		String kw = source.get("keywords");
		if (kw != null) {
			keywords.addAll(Strings.trim(Strings.split(kw, ',')));
		}
		value = source.children().iterator().next();
		visuals.load(source);
	}
	@Override
	public void save(XNElement destination) {
		destination.set("id", id);
		destination.set("type", typeString);
		destination.set("documentation", documentation);
		if (keywords.size() > 0) {
			destination.set("keywords", Strings.join(keywords, ","));
		} else {
			destination.set("keywords", null);
		}
		destination.add(value.copy());
		visuals.save(destination);
	}
	/**
	 * Parse the type string into an advance type declaration.
	 * @return the advance type
	 * @throws URISyntaxException if any of the string uri's is malformed
	 */
	public AdvanceType getAdvanceType() throws URISyntaxException {
		AdvanceType at = new AdvanceType();
		
        List<String> tokens = Lists.newArrayList();
        StringBuilder b = new StringBuilder();
        for (int i = 0; i < typeString.length(); i++) {
            char c = typeString.charAt(i);
            if (c == ',' || c == '<' || c == '>') {
                tokens.add(b.toString().trim());
                b.setLength(0);
                tokens.add(String.valueOf(c));
            } else {
                b.append(c);
            }
        }
        if (b.length() > 0) {
            tokens.add(b.toString());
        }
        // uri[<uri[<...>][,uri[<...>]]>]
        
        at.typeURI = new URI(tokens.get(0));
        
        if (tokens.size() > 3 && tokens.get(1).equals("<")) {
            parseURIList(at, tokens.subList(2, tokens.size()));
        }
		
		
		return at;
	}
	/**
	 * Parses the token sequence into a list of type arguments.
	 * @param argsFor the parent
	 * @param tokens the list of remaining tokens
	 * @throws URISyntaxException if the type URI is malformed
	 */
    static void parseURIList(AdvanceType argsFor, List<String> tokens) throws URISyntaxException {
        int i = 0;
        do {
        	AdvanceType at = new AdvanceType();
        	argsFor.typeArguments.add(at);
        	at.typeURI = new URI(tokens.get(i));
            if (tokens.size() > i + 1) {
                final String tok = tokens.get(i + 1);
                if (tok.equals("<")) {
                    parseURIList(at, tokens.subList(i + 2, tokens.size()));
                } else
                if (tok.equals(">")) {
                    break;
                } else {
                    i++;
                }
                   
            } 
        } while (i < tokens.size());
    }
}
