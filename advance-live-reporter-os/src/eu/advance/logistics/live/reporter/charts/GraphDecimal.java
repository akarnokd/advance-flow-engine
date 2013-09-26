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
package eu.advance.logistics.live.reporter.charts;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;

/**
 * Extends the BigDecimal because of defining scale and rounding mode in one place.
 * @author csirobi
 */
public class GraphDecimal extends BigDecimal {
	/** */
	private static final long serialVersionUID = 1L;
	/** The base scale. */
	private static final int SCALE = 6;
	/**
	 * Constructor.
	 * @param val the big integer value
	 */
	public GraphDecimal(BigInteger val) {
		super(val);
	}
	/**
	 * Constructor from char array.
	 * @param in the array
	 */
	public GraphDecimal(char[] in) {
		super(in);
	}
	/**
	 * Constructor from double value.
	 * @param val the value
	 */
	public GraphDecimal(double val)	{
		super(val);
	}
	/**
	 * Constructor from int value.
	 * @param val the value
	 */
	public GraphDecimal(int val) {
		super(val);
	}

	/**
	 * Constructor from long.
	 * @param val the value
	 */
	public GraphDecimal(long val) {
		super(val);
	}
	/**
	 * Constructor from string.
	 * @param val the value
	 */
	public GraphDecimal(String val)	{
		super(val);
	}
	/**
	 * Constructor from another decimal.
	 * @param val the value
	 */
	public GraphDecimal(BigDecimal val)	{
		this(val.toString());
	}

	@Override
	public GraphDecimal add(BigDecimal augend) {
		return new GraphDecimal(super.add(augend));
	}

	@Override
	public GraphDecimal divide(BigDecimal divisor) {
		return new GraphDecimal(super.divide(divisor, SCALE, RoundingMode.HALF_UP));
	}

	@Override
	public GraphDecimal multiply(BigDecimal multiplicand) {
		return new GraphDecimal(super.multiply(multiplicand));
	}

	@Override
	public GraphDecimal subtract(BigDecimal subtrahend) {
		return new GraphDecimal(super.subtract(subtrahend));
	}
}
