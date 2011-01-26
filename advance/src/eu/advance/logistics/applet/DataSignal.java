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
package eu.advance.logistics.applet;

import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.Timestamp;

/**
 * A signal entry.
 * @author karnokd
 *
 */
public class DataSignal implements Serializable {
	/** */
	private static final long serialVersionUID = 2911823515130168228L;
	/** The signal identifier. */
	public long signal;
	/** The signal name. */
	public String signalName;
	/** The signal's timestamp. */
	public Timestamp timestamp;
	/** The signal's value. */
	public BigDecimal value;
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof DataSignal) {
			return ((DataSignal)obj).timestamp.equals(timestamp);
		}
		return false;
	}
	@Override
	public int hashCode() {
		return timestamp.hashCode();
	}
}
