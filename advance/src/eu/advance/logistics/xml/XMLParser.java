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

package eu.advance.logistics.xml;

import java.io.InputStream;
import java.io.Reader;
import java.text.ParseException;
import java.util.Date;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

/**
 * A simplified parser based on the StAX API.
 * To read elements with subelements xor string content.
 * @author karnokd, 2008.02.05.
 * @version $Revision 1.0$
 */
public class XMLParser {
	/**
	 * The underlying reader.
	 */
	private XMLStreamReader reader;
	/**
	 * The name of the current element.
	 */
	private QName name;
	/**
	 * Stream XML from inputstream.
	 * @param in the input stream
	 * @throws XMLStreamException on error
	 */
	public XMLParser(InputStream in) throws XMLStreamException {
		XMLInputFactory factory = XMLInputFactory.newInstance();
		reader = factory.createXMLStreamReader(in);
	}
	/**
	 * Stream XML from reader.
	 * @param in reader
	 * @throws XMLStreamException on error
	 */
	public XMLParser(Reader in) throws XMLStreamException {
		XMLInputFactory factory = XMLInputFactory.newInstance();
		reader = factory.createXMLStreamReader(in);
	}
	/**
	 * Stream XML from another reader.
	 * @param reader the stream reader
	 */
	public XMLParser(XMLStreamReader reader) {
		this.reader = reader;
	}
	/**
	 * Position the stream to the next element start.
	 * @throws XMLStreamException on error
	 */
	public void nextElement() throws XMLStreamException {
		while (reader.hasNext()) {
			int type = reader.next();
			if (type == XMLStreamConstants.START_ELEMENT) {
				name = reader.getName();
				break;
			} else
			if (type == XMLStreamConstants.END_DOCUMENT) {
				throw new XMLStreamException("Unexpected end of document");
			} else
			if (type == XMLStreamConstants.COMMENT
					|| type == XMLStreamConstants.PROCESSING_INSTRUCTION
					|| type == XMLStreamConstants.SPACE
					|| type == XMLStreamConstants.CDATA
					|| type == XMLStreamConstants.CHARACTERS) {
				continue;
			}
		}
	}
	/**
	 * Try next element to be the given local name.
	 * @param localName the local name
	 * @return true if the next element is of name given
	 * @throws XMLStreamException on error
	 */
	public boolean tryNextElement(String localName) throws XMLStreamException {
		while (reader.hasNext()) {
			int type = reader.next();
			if (type == XMLStreamConstants.START_ELEMENT) {
				name = reader.getName();
				if (name.getLocalPart().equals(localName)) {
					return true;
				}
			} else
			if (type == XMLStreamConstants.COMMENT
					|| type == XMLStreamConstants.PROCESSING_INSTRUCTION
					|| type == XMLStreamConstants.SPACE
					|| type == XMLStreamConstants.CDATA
					|| type == XMLStreamConstants.CHARACTERS) {
				continue;
			}
			return false;
		}
		return false;
	}
	/**
	 * Loop for an end element.
	 * @throws XMLStreamException on error
	 */
	public void endElement() throws XMLStreamException {
		while (reader.hasNext()) {
			int type = reader.next();
			if (type == XMLStreamConstants.END_ELEMENT) {
				return;
			}
		}
	}
	/**
	 * Get local name of the current element.
	 * @return the local name
	 */
	public String getLocalName() {
		return name.getLocalPart();
	}
	/**
	 * Get the attribute value as string.
	 * @param localName the local name
	 * @return the attribute value which might be null.
	 */
	public String getAttributeString(String localName) {
		return reader.getAttributeValue(null, localName);
	}
	/**
	 * Get the attribute value as string.
	 * @param qname the qualified name of attribute
	 * @return the attribute value or null.
	 */
	public String getAttributeString(QName qname) {
		return reader.getAttributeValue(qname.getNamespaceURI(), qname.getLocalPart());
	}
	/**
	 * Get the QName of the current element.
	 * @return the QName of the current element.
	 */
	public QName getName() {
		return name;
	}
	/**
	 * Get the current text value as string.
	 * @return the string
	 * @throws XMLStreamException on error
	 */
	public String getString() throws XMLStreamException {
		return reader.getElementText();
	}
	/**
	 * @return the current string value parsed as integer
	 * @throws XMLStreamException if the value cannot be parsed as integer
	 */
	public int getInt() throws XMLStreamException {
		try {
			return Integer.parseInt(reader.getElementText());
		} catch (NumberFormatException ex) {
			throw new XMLStreamException(ex);
		}
	}
	/**
	 * @return the current string value parsed as integer or null if value is empty
	 * @throws XMLStreamException if the value cannot be parsed as integer
	 */
	public Integer getInteger() throws XMLStreamException {
		String value = reader.getElementText();
		if (!value.isEmpty()) {
			try {
				return Integer.valueOf(value);
			} catch (NumberFormatException ex) {
				throw new XMLStreamException(ex);
			}
		}
		return null;
	}
	/**
	 * @return the current string value parsed as double
	 * @throws XMLStreamException if the value cannot be parsed as double
	 */
	public double getDbl() throws XMLStreamException {
		try {
			return Double.parseDouble(reader.getElementText());
		} catch (NumberFormatException ex) {
			throw new XMLStreamException(ex);
		}
	}
	/**
	 * @return the current string value parsed as double or null if value is empty
	 * @throws XMLStreamException if the value cannot be parsed as double
	 */
	public Double getDouble() throws XMLStreamException {
		String value = reader.getElementText();
		if (!value.isEmpty()) {
			try {
				return Double.valueOf(value);
			} catch (NumberFormatException ex) {
				throw new XMLStreamException(ex);
			}
		}
		return null;
	}
	/**
	 * @return the value as boolean
	 * @throws XMLStreamException if value is empty or not a boolean
	 */
	public boolean getBool() throws XMLStreamException {
		String value = reader.getElementText();
		if (value.isEmpty()) {
			throw new XMLStreamException("Empty value");
		}
		if ("true".equals(value) || "on".equals(value) || "1".equals(value)) {
			return true;
		} else
		if ("false".equals(value) || "off".equals(value) || "0".equals(value)) {
			return false;
		}
		throw new XMLStreamException("Non boolean value");
	}
	/**
	 * @return value as boolean object or null if value is empty
	 * @throws XMLStreamException if value is not boolean type
	 */
	public Boolean getBoolean() throws XMLStreamException {
		String value = reader.getElementText();
		if (value.isEmpty()) {
			throw new XMLStreamException("Empty value");
		}
		if ("true".equals(value) || "on".equals(value) || "1".equals(value)) {
			return true;
		} else
		if ("false".equals(value) || "off".equals(value) || "0".equals(value)) {
			return false;
		}
		throw new XMLStreamException("Non boolean value");
	}
	/**
	 * Get the value as an dateTime.
	 * @return the date
	 * @throws XMLStreamException if value cannot be parsed as date
	 */
	public Date getDate() throws XMLStreamException {
		String value = reader.getElementText();
		if (!value.isEmpty()) {
			try {
				return XsdDateTime.parse(value);
			} catch (ParseException ex) {
				throw new XMLStreamException(ex);
			}
		}
		return null;
	}
	/**
	 * Get the value as an UTC dateTime.
	 * @return the dateTime as long
	 * @throws XMLStreamException if value cannot be parsed as dateTime or value is empty
	 */
	public long getDateUTC() throws XMLStreamException {
		String value = reader.getElementText();
		if (!value.isEmpty()) {
			try {
				return XsdDateTime.parse(value).getTime();
			} catch (ParseException ex) {
				throw new XMLStreamException(ex);
			}
		}
		throw new XMLStreamException("Empty value");
	}
	/**
	 * Get attribute value as integer.
	 * @param localName the local name of the attribute
	 * @return the integer value
	 * @throws XMLStreamException if value is undefined or cannot be parsed to int
	 */
	public int getAttributeInt(String localName) throws XMLStreamException {
		String value = reader.getAttributeValue(null, localName);
		if (value != null) {
			try {
				return Integer.parseInt(value);
			} catch (NumberFormatException ex) {
				throw new XMLStreamException(ex);
			}
		} 
		throw new XMLStreamException("No attribute defined.");
	}
	/**
	 * Get attribute value as integer object.
	 * @param localName the local name of the attribute
	 * @return the integer value
	 * @throws XMLStreamException if value is undefined or cannot be parsed to int
	 */
	public Integer getAttributeInteger(String localName) throws XMLStreamException {
		String value = reader.getAttributeValue(null, localName);
		if (value != null && !value.isEmpty()) {
			try {
				return Integer.valueOf(value);
			} catch (NumberFormatException ex) {
				throw new XMLStreamException(ex);
			}
		} 
		return null;
	}
	/**
	 * Close the underlying reader.
	 * @throws XMLStreamException on error
	 */
	public void close() throws XMLStreamException {
		reader.close();
	}
}
