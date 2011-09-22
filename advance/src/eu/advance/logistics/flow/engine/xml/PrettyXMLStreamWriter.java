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

package eu.advance.logistics.flow.engine.xml;

import javax.xml.namespace.NamespaceContext;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

/**
 * A pretty printing xml stream writer wrapper.
 * Original idea: https://stax-utils.dev.java.net/source/browse/stax-utils/src/javanet/staxutils/IndentingXMLStreamWriter.java?rev=1.4&view=markup
 * @author karnokd, 2007.12.11.
 * @version $Revision 1.0$
 */
public class PrettyXMLStreamWriter implements XMLStreamWriter {
	/**
	 * The underlying writer.
	 */
	private XMLStreamWriter writer;
	/** How deeply nested the current scope is. The root element is depth 1. */
    private int depth = 0; // document scope

    /** stack[depth] indicates what's been written into the current scope. */
    private int[] stack = new int[] { 0, 0, 0, 0 }; // nothing written yet
    /** Markup is written. */
    private static final int WROTE_MARKUP = 1;
    /** Data is written. */
    private static final int WROTE_DATA = 2;
    /** The indentation. */
    private String indent = "  ";
    /** The newline. */
    private String newLine = "\n";

    /** newLine followed by copies of indent. */
    private char[] linePrefix = null;
    /**
     * Constructor. Initializes the newLine field to the platform specific newline character.
     * @param writer the writer object
     */
    public PrettyXMLStreamWriter(XMLStreamWriter writer) {
    	this.writer = writer;
    	this.newLine = String.format("%n");
    }
	/**
	 * Constructor. Initializes the private fields.
	 * @param writer the writer object, cannot be null
	 * @param indent the indentation string
	 * @param lineBreak the linebreak string
	 */
	public PrettyXMLStreamWriter(XMLStreamWriter writer, String indent, String lineBreak) {
		this.writer = writer;
		this.indent = indent;
		this.newLine = lineBreak;
	}
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void close() throws XMLStreamException {
		writer.close();
	}
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void flush() throws XMLStreamException {
		writer.flush();
	}
	/**
	 * {@inheritDoc}
	 */
	@Override
	public NamespaceContext getNamespaceContext() {
		return writer.getNamespaceContext();
	}
	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getPrefix(String uri) throws XMLStreamException {
		return writer.getPrefix(uri);
	}
	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object getProperty(String name) {
		return writer.getProperty(name);
	}
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setDefaultNamespace(String uri) throws XMLStreamException {
		writer.setDefaultNamespace(uri);
	}
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setNamespaceContext(NamespaceContext context)
			throws XMLStreamException {
		writer.setNamespaceContext(context);
	}
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setPrefix(String prefix, String uri)
			throws XMLStreamException {
		writer.setPrefix(prefix, uri);
	}
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void writeAttribute(String localName, String value)
			throws XMLStreamException {
		writer.writeAttribute(localName, value);
	}
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void writeAttribute(String namespaceURI, String localName,
			String value) throws XMLStreamException {
		writer.writeAttribute(namespaceURI, localName, value);
	}
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void writeAttribute(String prefix, String namespaceURI,
			String localName, String value) throws XMLStreamException {
		writer.writeAttribute(prefix, namespaceURI, localName, value);
	}
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void writeCData(String data) throws XMLStreamException {
		writer.writeCData(data);
		afterData();
	}
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void writeCharacters(String text) throws XMLStreamException {
		writer.writeCharacters(text);
		afterData();
	}
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void writeCharacters(char[] text, int start, int len)
			throws XMLStreamException {
		writer.writeCharacters(text, start, len);
		afterData();
	}
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void writeComment(String data) throws XMLStreamException {
		beforeMarkup();
		writer.writeComment(data);
		afterMarkup();
	}
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void writeDTD(String dtd) throws XMLStreamException {
		beforeMarkup();
		writer.writeDTD(dtd);
		afterMarkup();
	}
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void writeDefaultNamespace(String namespaceURI)
			throws XMLStreamException {
		writer.writeDefaultNamespace(namespaceURI);
	}
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void writeEmptyElement(String localName)
			throws XMLStreamException {
		beforeMarkup();
		writer.writeEmptyElement(localName);
		afterMarkup();
	}
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void writeEmptyElement(String namespaceURI, String localName)
			throws XMLStreamException {
		beforeMarkup();
		writer.writeEmptyElement(namespaceURI, localName);
		afterMarkup();
	}
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void writeEmptyElement(String prefix, String localName,
			String namespaceURI) throws XMLStreamException {
		beforeMarkup();
		writer.writeEmptyElement(prefix, localName, namespaceURI);
		afterMarkup();
	}
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void writeEndDocument() throws XMLStreamException {
		try {
			while (depth > 0) {
				writeEndElement();
			}
		} catch (Exception ex) {
			
		}
		writer.writeEndDocument();
		afterEndDocument();
	}
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void writeEndElement() throws XMLStreamException {
		beforeEndElement();
		writer.writeEndElement();
		afterEndElement();
	}
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void writeEntityRef(String name) throws XMLStreamException {
		writer.writeEntityRef(name);
		afterData();
	}
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void writeNamespace(String prefix, String namespaceURI)
			throws XMLStreamException {
		writer.writeNamespace(prefix, namespaceURI);
		
	}
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void writeProcessingInstruction(String target)
			throws XMLStreamException {
		beforeMarkup();
		writer.writeProcessingInstruction(target);
		afterMarkup();
	}
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void writeProcessingInstruction(String target, String data)
			throws XMLStreamException {
		beforeMarkup();
		writer.writeProcessingInstruction(target, data);
		afterMarkup();
	}
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void writeStartDocument() throws XMLStreamException {
		beforeMarkup();
		writer.writeStartDocument();
		afterMarkup();
	}
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void writeStartDocument(String version)
			throws XMLStreamException {
		beforeMarkup();
		writer.writeStartDocument(version);
		afterMarkup();
	}
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void writeStartDocument(String encoding, String version)
			throws XMLStreamException {
		beforeMarkup();
		writer.writeStartDocument(encoding, version);
		afterMarkup();
	}
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void writeStartElement(String localName)
			throws XMLStreamException {
		beforeStartElement();
		writer.writeStartElement(localName);
		afterStartElement();
	}
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void writeStartElement(String namespaceURI, String localName)
			throws XMLStreamException {
		beforeStartElement();
		writer.writeStartElement(namespaceURI, localName);
		afterStartElement();
	}
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void writeStartElement(String prefix, String localName,
			String namespaceURI) throws XMLStreamException {
		beforeStartElement();
		writer.writeStartElement(prefix, localName, namespaceURI);
		afterStartElement();
	}
	 /** Prepare to write markup, by writing a new line and indentation. */
    protected void beforeMarkup() {
        int soFar = stack[depth];
        if ((soFar & WROTE_DATA) == 0 // no data in this scope
                && (depth > 0 || soFar != 0)) { // not the first line
            try {
                writeNewLine(depth);
                if (depth > 0 && indent.length() > 0) {
                    afterMarkup(); // indentation was written
                }
            } catch (Exception e) {
            }
        }
    }

    /** Note that markup or indentation was written. */
    protected void afterMarkup() {
        stack[depth] |= WROTE_MARKUP;
    }

    /** Note that data were written. */
    protected void afterData() {
        stack[depth] |= WROTE_DATA;
    }

    /** Prepare to start an element, by allocating stack space. */
    protected void beforeStartElement() {
        beforeMarkup();
        if (stack.length <= depth + 1) {
            // Allocate more space for the stack:
            int[] newStack = new int[stack.length * 2];
            System.arraycopy(stack, 0, newStack, 0, stack.length);
            stack = newStack;
        }
        stack[depth + 1] = 0; // nothing written yet
    }

    /** Note that an element was started. */
    protected void afterStartElement() {
        afterMarkup();
        ++depth;
    }

    /** Prepare to end an element, by writing a new line and indentation. */
    protected void beforeEndElement() {
        if (depth > 0 && stack[depth] == WROTE_MARKUP) { // but not data
            try {
                writeNewLine(depth - 1);
            } catch (Exception ignored) {
            }
        }
    }

    /** Note that an element was ended. */
    protected void afterEndElement() {
        if (depth > 0) {
            --depth;
        }
    }

    /** Note that a document was ended. */
    protected void afterEndDocument() {
    	depth = 0;
        if (stack[depth] == WROTE_MARKUP) { // but not data
            try {
                writeNewLine(0);
            } catch (Exception ignored) {
            }
        }
        stack[depth] = 0; // start fresh
    }

    /** 
     * Write a line separator followed by indentation.
     * @param indentation the level of indentation.
     * @throws XMLStreamException on error.
     */
    protected void writeNewLine(int indentation) throws XMLStreamException {
        final int newLineLength = newLine.length();
        final int prefixLength = newLineLength + (indent.length() * indentation);
        if (prefixLength > 0) {
            if (linePrefix == null) {
                linePrefix = (newLine + indent).toCharArray();
            }
            while (prefixLength > linePrefix.length) {
                // make linePrefix longer:
                char[] newPrefix = new char[newLineLength
                        + ((linePrefix.length - newLineLength) * 2)];
                System.arraycopy(linePrefix, 0, newPrefix, 0, linePrefix.length);
                System.arraycopy(linePrefix, newLineLength, newPrefix, linePrefix.length,
                        linePrefix.length - newLineLength);
                linePrefix = newPrefix;
            }
            writer.writeCharacters(linePrefix, 0, prefixLength);
        }
    }
}
