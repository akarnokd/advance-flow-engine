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
package eu.advance.logistics.flow.engine.block.filtering;

import eu.advance.logistics.annotations.Block;
import eu.advance.logistics.annotations.Input;
import eu.advance.logistics.annotations.Output;
import eu.advance.logistics.flow.engine.block.AdvanceBlock;
import eu.advance.logistics.flow.engine.xml.XElement;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import org.xml.sax.InputSource;

/**
 * Filter the elements of the collection via the XPath expression and return a
 * filtered collection. Signature: FilterCollection(collection<t>, xpath) ->
 * collection<t>
 *
 * @author TTS
 */
@Block(id = "FilterCollection", 
	category = "data-filtering", 
	scheduler = "IO", 
	description = "Filter the elements of the collection via the XPath expression and return a filtered collection.", 
	parameters = { "T" } 
)
public class FilterCollection extends AdvanceBlock {

    /**
     * The logger.
     */
    protected static final Logger LOGGER = Logger.getLogger(Filter.class.getName());
    /**
     * In.
     */
    @Input("advance:collection<?T>")
    protected static final String COLLECTION = "collection";
    /**
     * In.
     */
    @Input("advance:string")
    protected static final String PATH = "path";
    /**
     * Out.
     */
    @Output("advance:collection<?T>")
    protected static final String OUT = "out";

    @Override
    protected void invoke() {
        final XPath xpath = XPathFactory.newInstance().newXPath();
        final InputSource inputSource = new InputSource(new StringReader(resolver().getString(get(PATH))));
        final List<XElement> collection = resolver().getList(get(COLLECTION));

        final List<XElement> result = new ArrayList<XElement>();
        for (XElement xel : collection) {
            try {
                final String expression = resolver().getString(xel);
                final String value = (String) xpath.evaluate(expression, inputSource, XPathConstants.NODESET);

                result.add(resolver().create(value));
            } catch (XPathExpressionException ex) {
                log(ex);
            }
        }

        dispatch(OUT, resolver().create(result));
    }
}
