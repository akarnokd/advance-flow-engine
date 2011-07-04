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

package eu.advance.logistics.flow.editor;

import java.awt.Graphics2D;
import java.util.List;

import com.google.common.collect.Lists;

/**
 * A rectangular region representing an ADVANCE block with inputs and outputs.
 * @author karnokd, 2011.07.04.
 */
public class Block {
	/** The top-left corner coordinate. */
	public int x;
	/** The top-left corner coordinate. */
	public int y;
	/** The display name. */
	public String name;
	/** The list of input parameter display names. */
	public final List<String> inputs = Lists.newArrayList();
	/** The list of output parameter display names. */
	public final List<String> outputs = Lists.newArrayList();
	/**
	 * Calculate the total width of the block.
	 * @param g2 the context
	 * @return the width in pixels
	 */
	public int getWidth(Graphics2D g2) {
		int w = g2.getFontMetrics().stringWidth(name) + 10;
		for (String s : inputs) {
			w = Math.max(w, g2.getFontMetrics().stringWidth(s));
		}
		for (String s : outputs) {
			w = Math.max(w, g2.getFontMetrics().stringWidth(s));
		}
		return w;
	}
	/**
	 * Calculate the total height of the block.
	 * @param g2 the graphics context
	 * @return the height in pixels
	 */
	public int getHeight(Graphics2D g2) {
		int h = g2.getFontMetrics().getHeight() + 6;
		if (inputs.size() > 0) {
			h += 5 + g2.getFontMetrics().getHeight() * inputs.size();
		}
		if (outputs.size() > 0) {
			h += 5 + g2.getFontMetrics().getHeight() * outputs.size();
		}
		return h;
	}
	/**
	 * Calculate the middle point of the indexth input parameter.
	 * @param g2 the graphics context
	 * @param index the input index
	 * @return the offset
	 */
	public int getInputY(Graphics2D g2, int index) {
		return 8 + g2.getFontMetrics().getHeight() * (2 * index + 3) / 2;
	}
	/**
	 * Calculate the middle point of the indexth output parameter.
	 * @param g2 the graphics context
	 * @param index the output index
	 * @return the offset
	 */
	public int getOutputY(Graphics2D g2, int index) {
		int h = g2.getFontMetrics().getHeight() + 3;
		if (inputs.size() > 0) {
			h += 5 + g2.getFontMetrics().getHeight() * inputs.size();
		}
		h += 5 + g2.getFontMetrics().getHeight() * (2 * index + 1) / 2;
		return h;
	}
}
