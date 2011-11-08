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

package eu.advance.logistics.flow.engine;

import hu.akarnokd.reactive4java.base.Func1;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.Collection;
import java.util.List;

import javax.xml.stream.XMLStreamException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;

import eu.advance.logistics.flow.engine.model.fd.UnresolvableSchemaURIException;
import eu.advance.logistics.flow.engine.xml.typesystem.XSchema;
import eu.advance.logistics.flow.engine.xml.typesystem.XElement;
import eu.advance.logistics.flow.engine.xml.typesystem.XType;

/**
 * Class to resolve schemas and load them as XTypes from local file system.
 * If the schemas starting with {@code advance} cannot be resolved as a local file, the
 * code attempts to resolve it against the classpath root.
 * @author akarnokd, 2011.09.28.
 */
public class AdvanceLocalSchemaResolver implements AdvanceSchemaResolver {
	/** The logger. */
	protected static final Logger LOG = LoggerFactory.getLogger(AdvanceLocalSchemaResolver.class);
	/** The list of schema locations. */
	protected final List<String> schemas = Lists.newArrayList();
	/**
	 * Constructor with the collection of schema locations.
	 * @param schemaLocations the schema location directories
	 */
	public AdvanceLocalSchemaResolver(Collection<String> schemaLocations) {
		this.schemas.addAll(schemaLocations);
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
				return XSchema.parse(XElement.parseXML(bin), new Func1<String, XElement>() {
					@Override
					public XElement invoke(String param1) {
						for (String base : schemas) {
							File f = new File(base, param1);
							if (f.canRead()) {
								try {
									return XElement.parseXML(f);
								} catch (IOException ex) {
									LOG.error(ex.toString(), ex);
								} catch (XMLStreamException ex) {
									LOG.error(ex.toString(), ex);
								}
							}
						}
						return null;
					}
				});
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
			for (String schemaDir : schemas) {
				File f = new File(schemaDir + "/" + u + ".xsd");
				if (f.exists()) {
					try {
						return resolveSchemaLoad(f.toURI().toURL(), schemaURI);
					} catch (MalformedURLException ex) {
						LOG.error(f.toString(), ex);
						throw new UnresolvableSchemaURIException(schemaURI, ex);
					}
				}
			}
			URL f = getClass().getResource("/" + u + ".xsd");
			if (f != null) {
				return resolveSchemaLoad(f, schemaURI);
			}
			for (String schemaPath : schemas) {
				f = getClass().getResource("/" + schemaPath + "/" + u + ".xsd");
				if (f != null) {
					return resolveSchemaLoad(f, schemaURI);
				}
			}
		} else
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
