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

import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;

import javax.xml.stream.XMLStreamException;

import eu.advance.logistics.xml.typesystem.SchemaParser;
import eu.advance.logistics.xml.typesystem.XElement;
import eu.advance.logistics.xml.typesystem.XType;

/**
 * Utility class to resolve varios ADVANCE objects such as XML schemas in the block and flow descriptors.
 * @author karnokd, 2011.06.22.
 */
public final class AdvanceResolver {
	/**
	 * Utility class.
	 */
	private AdvanceResolver() {
	}
	/**
	 * Resolve a schema URI link.
	 * @param schemaURI the schema URI.
	 * @return the parsed schema
	 */
	public static XType resolveSchema(URI schemaURI) {
		String s = schemaURI.getScheme(); 
		if ("advance".equals(s)) {
			String u = schemaURI.getSchemeSpecificPart();
			URL url = AdvanceResolver.class.getResource("schemas/" + u + ".xsd");
			return resolveSchemaLoad(url, schemaURI);
		}
		if ("res".equals(s)) {
			String u = schemaURI.getSchemeSpecificPart();
			if (!u.startsWith("/")) {
				u = "/" + u;
			}
			URL url = AdvanceResolver.class.getResource(u);
			if (url != null) {
				return resolveSchemaLoad(url, schemaURI);
			}
		} else
		if ("http".equals(s) || "https".equals(s) || "ftp".equals(s) || "file".equals(s)) {
			try {
				URL url = schemaURI.toURL();
				return resolveSchemaLoad(url, schemaURI);
			} catch (MalformedURLException ex) {
				throw new UnresolvableSchemaURIException(schemaURI, ex);
			}
		}
		throw new UnresolvableSchemaURIException(schemaURI);
	}
	/**
	 * Perform the retrieval and parsing of the schema file.
	 * @param url the URL to load from
	 * @param schemaURI the original URI
	 * @return the parsed schema
	 */
	static XType resolveSchemaLoad(URL url, URI schemaURI) {
		try {
			BufferedInputStream bin = new BufferedInputStream(url.openStream());
			try {
				return SchemaParser.parse(XElement.parseXML(bin), "schemas");
			} finally {
				bin.close();
			}
		} catch (XMLStreamException ex) {
			throw new UnresolvableSchemaURIException(schemaURI, ex);
		} catch (IOException ex) {
			throw new UnresolvableSchemaURIException(schemaURI, ex);
		}
	}
}
