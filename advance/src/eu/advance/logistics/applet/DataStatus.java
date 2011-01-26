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
import java.sql.Timestamp;

/**
 * The scada status change history entry.
 * @author karnokd
 */
public class DataStatus implements Serializable {
	/** */
	private static final long serialVersionUID = -1710521857973162136L;
	/** The status name. */
	public String name;
	/** The measured status type id. */
	public long statusId;
	/** The start timestamp. */
	public Timestamp start;
	/** The end timestamp. */
	public Timestamp end;
	/** The start status code. */
	public long startStatus;
	/** The end status code. */
	public long endStatus;
}
