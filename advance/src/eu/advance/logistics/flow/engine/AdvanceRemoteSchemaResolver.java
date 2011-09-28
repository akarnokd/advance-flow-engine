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

package eu.advance.logistics.flow.engine;

import hu.akarnokd.reactive4java.base.Func1;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.Map;

import javax.xml.stream.XMLStreamException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Maps;

import eu.advance.logistics.flow.engine.model.UnresolvableSchemaURIException;
import eu.advance.logistics.flow.engine.xml.typesystem.SchemaParser;
import eu.advance.logistics.flow.engine.xml.typesystem.XElement;
import eu.advance.logistics.flow.engine.xml.typesystem.XType;

/**
 * Class to resolve schemas via in memory mapping. May be used to operate on schemas
 * downloaded from a remote flow engine.
 * @author karnokd, 2011.09.28.
 */
public class AdvanceRemoteSchemaResolver implements AdvanceSchemaResolver {
	/** The logger. */
	protected static final Logger LOG = LoggerFactory.getLogger(AdvanceRemoteSchemaResolver.class);
	/** The list of schema locations. */
	protected final Map<String, XType> schemaTypes = Maps.newHashMap();
	/**
	 * Constructor with the collection of schema locations.
	 * @param schemas the map from schema URI to the schema XML.
	 */
	public AdvanceRemoteSchemaResolver(final Map<String, XElement> schemas) {
		for (Map.Entry<String, XElement> e : schemas.entrySet()) {
			schemaTypes.put(e.getKey(), SchemaParser.parse(e.getValue(), new Func1<String, XElement>() {
				@Override
				public XElement invoke(String param1) {
					return schemas.get(param1);
				}
			}));
		}
	}
	/**
	 * Perform the retrieval and parsing of the schema file.
	 * @param url the URL to load from
	 * @param schemaURI the original URI
	 * @return the parsed schema
	 */
	XType resolveSchemaLoad(URL url, URI schemaURI) {
		try {
			BufferedInputStream bin = new BufferedInputStream(url.openStream());
			try {
				return SchemaParser.parse(XElement.parseXML(bin), new URLResolver(url));
			} finally {
				bin.close();
			}
		} catch (XMLStreamException ex) {
			LOG.error(ex.toString(), ex);
			throw new UnresolvableSchemaURIException(schemaURI, ex);
		} catch (IOException ex) {
			LOG.error(ex.toString(), ex);
			throw new UnresolvableSchemaURIException(schemaURI, ex);
		}
	}
	/**
	 * Resolve a schema URI link.
	 * @param schemaURI the schema URI.
	 * @return the parsed schema
	 */
	@Override
	public XType resolve(URI schemaURI) {
		String s = schemaURI.getScheme(); 
		if ("advance".equals(s)) {
			String u = schemaURI.getSchemeSpecificPart();
			XType result = schemaTypes.get(u);
			if (result != null) {
				return result;
			}
			throw new UnresolvableSchemaURIException(schemaURI);
		}
		if ("res".equals(s)) {
			String u = schemaURI.getSchemeSpecificPart();
			if (!u.startsWith("/")) {
				u = "/" + u;
			}
			URL url = getClass().getResource(u);
			if (url != null) {
				return resolveSchemaLoad(url, schemaURI);
			}
		} else
		if ("http".equals(s) || "https".equals(s) || "ftp".equals(s) || "file".equals(s)) {
			try {
				URL url = schemaURI.toURL();
				return resolveSchemaLoad(url, schemaURI);
			} catch (MalformedURLException ex) {
				LOG.error(schemaURI.toString(), ex);
				throw new UnresolvableSchemaURIException(schemaURI, ex);
			}
		}
		LOG.error(schemaURI.toString());
		throw new UnresolvableSchemaURIException(schemaURI);
	}

}
