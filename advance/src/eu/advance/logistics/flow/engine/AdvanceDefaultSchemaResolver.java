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
import hu.akarnokd.reactive4java.base.Pair;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.xml.stream.XMLStreamException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import eu.advance.logistics.flow.engine.xml.typesystem.XElement;
import eu.advance.logistics.flow.engine.xml.typesystem.XSchema;
import eu.advance.logistics.flow.engine.xml.typesystem.XType;

/**
 * Class to resolve schemas and load them as XTypes from local file system.
 * If the schemas starting with {@code advance} cannot be resolved as a local file, the
 * code attempts to resolve it against the classpath root.
 * @author akarnokd, 2011.09.28.
 */
public class AdvanceDefaultSchemaResolver implements AdvanceSchemaResolver {
	/** The logger. */
	protected static final Logger LOG = LoggerFactory.getLogger(AdvanceDefaultSchemaResolver.class);
	/** The list of schema locations. */
	protected final List<String> schemaLocations = Lists.newArrayList();
	/** The list of schema locations. */
	protected final Map<String, XElement> schemaXMLs = Maps.newHashMap();
	/**
	 * Constructor with the collection of schema locations.
	 * @param schemaLocations the schema location directories
	 * @param schemaXMLs the preloaded schema XMLs
	 */
	public AdvanceDefaultSchemaResolver(
			final Collection<String> schemaLocations, final Map<String, XElement> schemaXMLs) {
		this.schemaLocations.addAll(schemaLocations);
		this.schemaXMLs.putAll(schemaXMLs);
	}
	@Override
	public XType resolve(String typeName) {
		try {
			Pair<XElement, URL> type = resolveXML(typeName);
			if (type != null) {
				return process(type.first, type.second != null ? getParent(type.second) : null);
			}
		} catch (IOException ex) {
			LOG.error(ex.toString(), ex);
		} catch (XMLStreamException ex) {
			LOG.error(ex.toString(), ex);
		}
		return new XType();
	}
	/**
	 * Resolve the XML file represented by the {@code typeName}.
	 * @param typeName the type name
	 * @return the pair of XML and the URL where the XML was found, null if not found
	 * @throws IOException on I/O error
	 * @throws XMLStreamException on Parse error
	 */
	protected Pair<XElement, URL> resolveXML(String typeName) throws IOException, XMLStreamException {
		if (typeName.startsWith("advance:")) {
			String name = typeName.substring(typeName.indexOf(':') + 1);
			URL resource = getClass().getResource("/" + name + ".xsd");
			return Pair.of(XElement.parseXML(resource), resource);
		} else
		if (typeName.startsWith("http:") 
				|| typeName.startsWith("https:") 
				|| typeName.startsWith("file:")
				|| typeName.startsWith("ftp:")) {
			URL url = new URL(typeName);
			return Pair.of(XElement.parseXML(url), url);
		}
		XElement s = schemaXMLs.get(typeName);
		if (s != null) {
			return Pair.of(s, null);
		}
		for (String dir : schemaLocations) {
			File sf = new File(dir, typeName);
			if (sf.canRead()) {
				return Pair.of(XElement.parseXML(sf), sf.toURI().toURL());
			}
		}
		URL resource = getClass().getResource("/" + typeName);
		if (resource != null) {
			return Pair.of(XElement.parseXML(resource), resource);
		}
		return Pair.of(s, null);
	}
	/**
	 * Parses the given schema definition.
	 * <p>All dependencies are resolved within this resolver as well.</p>
	 * @param schemaDef the schema XML
	 * @param context the relative context for this schema to resolve any locally referenced schemas
	 * @return the type
	 */
	protected XType process(XElement schemaDef, final URL context) {
		return XSchema.parse(schemaDef, new Func1<String, XElement>() {
			@Override
			public XElement invoke(String param1) {
				try {
					Pair<XElement, URL> type = null;
					if (param1.contains(":")) {
						type = resolveXML(param1);
					} else {
						if (context == null) {
							return schemaXMLs.get(param1);
						}
						type = resolveXML(context + "/" + param1);
					}
					return type != null ? type.first : null;
				} catch (IOException ex) {
					LOG.error(ex.toString(), ex);
					return null;
				} catch (XMLStreamException ex) {
					LOG.error(ex.toString(), ex);
					return null;
				}
			}
		});
	}
	/**
	 * Extract the parent directory from the supplied URL.
	 * @param u the URL to extract
	 * @return the new URL
	 * @throws MalformedURLException in case the new URL is invalid
	 */
	protected URL getParent(URL u) throws MalformedURLException {
		String p = u.getPath();
		int idx = p.lastIndexOf('/');
		if (idx >= 0) {
			p = p.substring(0, idx);
			return new URL(u.getProtocol(), u.getHost(), p);
		}
		return u;
	}
}
