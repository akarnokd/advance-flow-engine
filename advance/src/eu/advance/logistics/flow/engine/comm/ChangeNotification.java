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

package eu.advance.logistics.flow.engine.comm;

import hu.akarnokd.reactive4java.base.Scheduler;
import hu.akarnokd.reactive4java.reactive.DefaultObservable;

import java.io.Closeable;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

/**
 * A poll based local or remote file change notifier.
 * Clients should register to this class as observers and they will receive the list
 * of changed files.
 * @author akarnokd, 2011.10.06.
 * @param <T> a concrete file access object type
 */
public class ChangeNotification<T extends FileAccess> extends DefaultObservable<List<FileChangeInfo>> implements Closeable {
	/** The logger. */
	protected static final Logger LOG = LoggerFactory.getLogger(ChangeNotification.class);
	/** The file access to use to check on file changes. */
	protected final Pool<T> access;
	/** The timer of the scheduled task. */
	protected Closeable timer;
	/** The last state of the target location. */
	protected List<FileInfo> last;
	/**
	 * Initialize the change notification via the given accessor, scheduler and poll interval.
	 * @param access the accessor functions
	 */
	public ChangeNotification(Pool<T> access) {
		this.access = access;
	}
	/**
	 * Initialize the polling.
	 * @param scheduler the scheduler to use for the polling.
	 * @param time the time interval between polls
	 * @param unit the 
	 */
	public void init(Scheduler scheduler, int time, TimeUnit unit) {
		timer = scheduler.schedule(new Runnable() {
			@Override
			public void run() {
				checkChanges();
			}
		}, 0, time, unit);
	}
	/**
	 * Check for changes in file properties.
	 */
	protected void checkChanges() {
		try {
			final T fa = access.get();
			try {
				if (last == null) {
					last = fa.list();
				} else {
					List<FileChangeInfo> result = Lists.newArrayList(); 
					List<FileInfo> curr = fa.list();
					Map<String, FileInfo> mapCurrent = mapByName(curr);
					for (FileInfo fl : last) {
						FileInfo fc = mapCurrent.remove(fl.name);
						if (fc != null) {
							if (fc.length != fl.length || !fc.time.equals(fl.time)) {
								FileChangeInfo fci = new FileChangeInfo();
								fci.type = FileChangeType.MODIFIED;
								fci.file = fc.copy();
								result.add(fci);
							}
						} else {
							FileChangeInfo fci = new FileChangeInfo();
							fci.type = FileChangeType.DELETED;
							fci.file = fl.copy();
							result.add(fci);
						}
					}
					for (FileInfo fc : mapCurrent.values()) {
						FileChangeInfo fci = new FileChangeInfo();
						fci.type = FileChangeType.NEW;
						fci.file = fc.copy();
						result.add(fci);
					}
					if (!result.isEmpty()) {
						next(result);
					}
				}
			} finally {
				access.put(fa);
			}
		} catch (Exception ex) {
			LOG.error(ex.toString(), ex);
		}
	}
	/**
	 * Create a map of filename to file info.
	 * @param files the file info list to map
	 * @return the map
	 */
	protected Map<String, FileInfo> mapByName(List<FileInfo> files) {
		Map<String, FileInfo> result = Maps.newHashMap();
		
		for (FileInfo fi : files) {
			result.put(fi.name, fi);
		}		
		return result;
	}
	@Override
	public void close() {
		if (timer != null) {
			try {
				timer.close();
				timer = null;
			} catch (IOException ex) {
				LOG.error(ex.toString(), ex);
			}
		}
		super.close();
	}
}
