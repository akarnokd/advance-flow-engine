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
 * The SCADA alarm entry for reports.
 * @author karnokd
 *
 */
public class DataAlarm implements Serializable {
	/** */
	private static final long serialVersionUID = 7564167357889566606L;
	/** The alarm type id. */
	public long alarm;
	/** The alarm name. */
	public String alarmName;
	/** The start timestamp. */
	public Timestamp start;
	/** The end timestamp. */
	public Timestamp end;
	/** Does this alarm represent a warning condition? */
	public boolean isWarning;
}
