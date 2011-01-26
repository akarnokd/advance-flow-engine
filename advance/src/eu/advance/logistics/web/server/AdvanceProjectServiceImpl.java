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
package eu.advance.logistics.web.server;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gwt.user.server.rpc.RemoteServiceServlet;

import eu.advance.logistics.web.model.AdvanceProjectService;
import eu.advance.logistics.xml.XML;

/**
 * The server side implementation of the advance project services.
 * @author karnokd
 *
 */
public class AdvanceProjectServiceImpl extends RemoteServiceServlet implements
		AdvanceProjectService {
	/** The logger. */
	static final Logger LOG = LoggerFactory.getLogger(AdvanceProjectServiceImpl.class.getName());
	/**
	 * 
	 */
	private static final long serialVersionUID = 6929308684463332904L;
	
	@Override
	public Map<String, String> getLabels(String languageCode) {
		Map<String, String> result = Maps.newHashMap();

		if (getLanguages().contains(languageCode)) {
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			try {
				DocumentBuilder db = dbf.newDocumentBuilder();
				InputStream in = getClass().getResourceAsStream("/labels_" + languageCode + ".xml");
				if (in != null) {
					try {
						Element root = db.parse(in).getDocumentElement();
						for (Element entry : XML.childrenWithName(root, "entry")) {
							result.put(entry.getAttribute("key"), entry.getTextContent());
						}
					} finally {
						in.close();
					}
				} else {
					LOG.error("Missing label file: " + "/labels_" + languageCode + ".xml");
				}
			} catch (ParserConfigurationException ex) {
				LOG.error(ex.toString(), ex);
			} catch (IOException ex) {
				LOG.error(ex.toString(), ex);
			} catch (SAXException ex) {
				LOG.error(ex.toString(), ex);
			}
		}
		return result;
	}
	@Override
	public List<String> getLanguages() {
		URL englishResource = getClass().getResource("/");
		List<String> result = Lists.newArrayList();
		
		if (englishResource != null) {
			try {
				File dir = new File(englishResource.toURI());
				if (dir != null) {
					File[] files = dir.listFiles(new FilenameFilter() {
						@Override
						public boolean accept(File dir, String name) {
							return name.startsWith("labels_") && name.endsWith(".xml");
						}
					});
					if (files != null) {
						for (File f : files) {
							String n = f.getName();
							result.add(n.substring(7, n.length() - 4));
						}
					}
				}
			} catch (URISyntaxException e) {
				LOG.error(e.toString(), e);
			}
		}
		
		return result;
	}
}
