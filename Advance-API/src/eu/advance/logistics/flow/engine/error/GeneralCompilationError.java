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

package eu.advance.logistics.flow.engine.error;

import hu.akarnokd.utils.xml.XNElement;
import eu.advance.logistics.flow.engine.model.AdvanceCompilationError;

/**
 * A general compilation error indicator when the error details are stored in XML.
 * @author akarnokd, 2011.09.30.
 */
public class GeneralCompilationError implements AdvanceCompilationError {
	/** The error contents. */
	public XNElement content;
	/**
	 * Constructor.
	 * @param content the wrapped content
	 */
	public GeneralCompilationError(XNElement content) {
		this.content = content.copy();
	}
	@Override
	public void load(XNElement source) {
		source.add(content.copy());
	}
	@Override
	public void save(XNElement destination) {
		destination.set("type", getClass().getSimpleName());
		destination.add(content.copy());
	}
}
