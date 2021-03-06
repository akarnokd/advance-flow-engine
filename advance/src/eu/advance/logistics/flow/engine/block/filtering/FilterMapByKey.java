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


import hu.akarnokd.utils.xml.XNElement;

import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.xml.sax.InputSource;

import eu.advance.logistics.annotations.Block;
import eu.advance.logistics.annotations.Input;
import eu.advance.logistics.annotations.Output;
import eu.advance.logistics.flow.engine.AdvanceBlock;

/**
 * Filter the keys of the map via the XPath expression and return a map with
 * only those elements where the filter matched the key. Signature:
 * FilterMapByKey(map<t, u>, xpath) -> map<t, u>
 *
 * @author TTS
 */
@Block(id = "FilterMapByKey", 
	category = "data-filtering", 
	scheduler = "IO", 
	description = "Filter the keys of the map via the XPath expression and return a map with only those elements where the filter matched the key", 
	parameters = { "K", "V" } 
)
public class FilterMapByKey extends AdvanceBlock {

    /**
     * The logger.
     */
    protected static final Logger LOGGER = Logger.getLogger(Filter.class.getName());
    /**
     * In.
     */
    @Input("advance:map<?K, ?V>")
    protected static final String MAP = "map";
    /**
     * In.
     */
    @Input("advance:string")
    protected static final String PATH = "path";
    /**
     * Out.
     */
    @Output("advance:map<?K, ?V>")
    protected static final String OUT = "out";
    
    @Override
    protected void invoke() {
        final XPath xpath = XPathFactory.newInstance().newXPath();
        final InputSource inputSource = new InputSource(new StringReader(resolver().getString(get(PATH))));
        final Map<XNElement, XNElement> map = resolver().getMap(get(MAP));
        final Set<XNElement> keysSet = map.keySet();
        
        final Map<XNElement, XNElement> result = new HashMap<XNElement, XNElement>();
        for (XNElement key : keysSet) {
            
            try {
                final String expression = resolver().getString(key);
                xpath.evaluate(expression, inputSource, XPathConstants.NODESET);
                
                result.put(key, map.get(key));
            } catch (XPathExpressionException ex) {
                log(ex);
            }
        }
        
        dispatch(OUT, resolver().create(result));
    }
}
