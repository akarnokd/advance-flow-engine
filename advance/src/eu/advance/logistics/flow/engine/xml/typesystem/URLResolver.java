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

package eu.advance.logistics.flow.engine.xml.typesystem;

import hu.akarnokd.reactive4java.base.Func1;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

import javax.xml.stream.XMLStreamException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Utility class to resolve XML schemas relative to a base URL.
 * @author karnokd, 2011.09.28.
 */
public class URLResolver implements Func1<String, XElement> {
	/** The logger. */
	protected static final Logger LOG = LoggerFactory.getLogger(URLResolver.class);
	/** The base URL for the resolution. */
	protected final String baseURL;
	/**
	 * Create a resolver with the given base url.
	 * @param baseURL The base path of the URL without the trailing slash.
	 */
	public URLResolver(String baseURL) {
		this.baseURL = baseURL;
	}
	/**
	 * Create a resolver with the given concrete file's URL.
	 * @param url the parent of this URL will be used
	 */
	public URLResolver(URL url) {
		String urlString = url.toString();
		// trim the concrete file and use its parent directory for the schema base
		int idx = urlString.lastIndexOf("/");
		if (idx >= 0) {
			urlString = urlString.substring(0, idx);
		}
		this.baseURL = urlString;
	}
	@Override
	public XElement invoke(String param1) {
		try {
			URL u = new URL(baseURL + "/" + param1);
			InputStream in = u.openStream();
			try {
				return XElement.parseXML(in);
			} finally {
				in.close();
			}
		} catch (MalformedURLException ex) {
			LOG.error(ex.toString(), ex);
		} catch (IOException ex) {
			LOG.error(ex.toString(), ex);
		} catch (XMLStreamException ex) {
			LOG.error(ex.toString(), ex);
		}
		return null;
	}
}
