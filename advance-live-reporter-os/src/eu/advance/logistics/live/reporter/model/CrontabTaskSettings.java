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

package eu.advance.logistics.live.reporter.model;

import hu.akarnokd.reactive4java.base.Func1;
import hu.akarnokd.utils.collection.ParameterHashMap;
import hu.akarnokd.utils.xml.XElement;

import org.joda.time.DateTime;
import org.joda.time.ReadableDateTime;
import org.joda.time.Seconds;

/**
 * A crontab task configuration record.
 * <p>Use the <code>invoke()</code> method to test if the start conditions
 * of this task have been met once a minute.</p>
 * <p>Note that at least 60 seconds must pass between tests to be successful.</p>
 * @author karnokd, 2013.05.27.
 */
public class CrontabTaskSettings implements Func1<ReadableDateTime, Boolean> {
	/** The task identifier. */
	public String id;
	/** The time filter. */
	public CrontabTime time;
	/** When the last time check occurred. */
	public DateTime lastCheck;
	/** The runnable task class. */
	public String clazz;
	/** The parameters. */
	public final ParameterHashMap parameters;
	/**
	 * Load from an XML node.
	 * @param xtask the xml node
	 */
	public void load(XElement xtask) {
		id = xtask.get("id");
		time = new CrontabTime();
		time.load(xtask);
		clazz = xtask.get("class");
		for (XElement xparam : xtask.childrenWithName("param")) {
			parameters.put(xparam.get("name"), xparam.get("value"));
		}
	}
	@Override
	public Boolean invoke(ReadableDateTime rdt) {
		if (Seconds.secondsBetween(lastCheck, rdt).getSeconds() >= 60) {
			lastCheck = rdt.toDateTime();
			return time.invoke(rdt);
		}
		return false;
	}
	/**
	 * Default constructor.
	 */
	public CrontabTaskSettings() { 
		lastCheck = new DateTime();
		parameters = new ParameterHashMap();
	}
	/**
	 * Copy constructor.
	 * @param other the other settings
	 */
	public CrontabTaskSettings(CrontabTaskSettings other) {
		this.lastCheck = other.lastCheck;
		this.parameters = new ParameterHashMap(other.parameters);
		this.time = new CrontabTime(other.time);
		this.clazz = other.clazz;
		this.id = other.id;
	}
}
