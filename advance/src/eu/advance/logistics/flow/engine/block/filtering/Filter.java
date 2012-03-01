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
import java.io.StringReader;
import java.util.logging.Logger;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import org.xml.sax.InputSource;

/**
 * AApplies the XPath expression to the incoming value and only those are
 * forwarded which maches it. Signature: Filter(t, xpath) -> t
 *
 * @author TTS
 */
@Block(id = "Filter", category = "data-filtering", scheduler = "IO", description = "Applies the XPath expression to the incoming value and only those are forwarded which maches it")
public class Filter extends AdvanceBlock {

    /**
     * The logger.
     */
    protected static final Logger LOGGER = Logger.getLogger(Filter.class.getName());
    /**
     * In.
     */
    @Input("advance:object")
    protected static final String OBJECT = "object";
    /**
     * In.
     */
    @Input("advance:string")
    protected static final String PATH = "path";
    /**
     * Out.
     */
    @Output("advance:object")
    protected static final String OUT = "out";

    @Override
    protected void invoke() {
        final XPath xpath = XPathFactory.newInstance().newXPath();
        final InputSource inputSource = new InputSource(new StringReader(resolver().getString(get(PATH))));
        final String expression = resolver().getString(get(OBJECT));

        try {
            final String result = (String) xpath.evaluate(expression, inputSource, XPathConstants.NODESET);

            dispatch(OUT, resolver().create(result));
        } catch (XPathExpressionException ex) {
            log(ex);
        }
    }
}
