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

package eu.advance.logistics.flow.engine.block.prediction;

import eu.advance.logistics.annotations.Block;
import eu.advance.logistics.annotations.Input;
import eu.advance.logistics.annotations.Output;
import eu.advance.logistics.flow.engine.block.AdvanceBlock;
import eu.advance.logistics.flow.engine.xml.XElement;

/**
 * Configuration block for the K-Means ARX learner.
 * @author karnokd, 2012.02.27.
 */
@Block(id = "KMeansARXConfig", 
category = "prediction", scheduler = "IO", 
description = "K-means ARX model configuration block.")
public class KMeansARXConfig extends AdvanceBlock {
	/** The model order. */
    @Input("advance:integer")
    protected static final String MODEL_ORDER = "order";
	/** Number of ARX models. */
    @Input("advance:integer")
    protected static final String CLUSTER_COUNT = "clusters";
	/** Maximum iterations. */
    @Input(value = "advance:integer", defaultConstant = "<integer>10</integer>")
    protected static final String MAX_ITER = "maxIterations";
	/** Horizon. */
    @Input(value = "advance:integer", defaultConstant = "<integer>10</integer>")
    protected static final String HORIZON = "horizon";
	/** Normalize output. */
    @Input(value = "advance:boolean", defaultConstant = "<boolean>true</boolean>")
    protected static final String NORMALIZE = "normalize";
	/** The splitting factor between training and testing. */
    @Input(value = "advance:real", defaultConstant = "<real>0.75</real>")
    protected static final String SPLIT = "split";
    /** The configuration. */
    @Output("advance:kmeans_arx_config")
    protected static final String OUT = "out";
	@Override
	protected void invoke() {
		XElement result = new XElement("kmeans_arx_config");
		
		result.set("model-order", getInt(MODEL_ORDER));
		result.set("cluster-count", getInt(CLUSTER_COUNT));
		result.set("max-iteration", getInt(MAX_ITER));
		result.set("horizon", getInt(HORIZON));
		result.set("normalize", getBoolean(NORMALIZE));
		result.set("split", getDouble(SPLIT));
		
		dispatch(OUT, result);
	}

}
