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

package eu.advance.logistics.live.reporter.ws;

import hu.akarnokd.utils.xml.XElement;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.xml.stream.XMLStreamException;

import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.advance.logistics.live.reporter.model.CrontabTask;
import eu.advance.logistics.live.reporter.model.CrontabTaskSettings;

/**
 * The servlet that runs in the background and 
 * executes scheduled actions.
 * @author karnokd, 2013.05.27.
 */
public class CrontabServlet extends HttpServlet {
	/** */
	private static final long serialVersionUID = -8268526525566885446L;
	/** The logger. */
	protected static final Logger LOGGER = LoggerFactory.getLogger(CrontabServlet.class);
	/** The scheduler. */
	protected ScheduledExecutorService scheduler;
	/** The task pool. */
	protected ExecutorService taskPool;
	/**
	 * The crontab task mapping.
	 */
	protected final Map<String, CrontabTaskSettings> tasks = new LinkedHashMap<>();
	/** The registry of running tasks. */
	protected final ConcurrentMap<String, String> runningTask = new ConcurrentHashMap<>();
	@Override
	public void init(ServletConfig config) throws ServletException {
		LOGGER.info("Crontab Servlet Init");
		super.init(config);

		taskPool = Executors.newCachedThreadPool();
		
		ScheduledThreadPoolExecutor sexec = new ScheduledThreadPoolExecutor(1);
		sexec.setRemoveOnCancelPolicy(true);
		this.scheduler = sexec;
		
		loadTasks();
		
		sexec.scheduleAtFixedRate(new Runnable() {
			@Override
			public void run() {
				checkTasks();
			}
		}, 15, 15, TimeUnit.SECONDS);
	}
	/**
	 * Load the tasks definition.
	 */
	protected void loadTasks() {
		tasks.clear();
		try {
			XElement xtasks = XElement.parseXML(CrontabServlet.class.getResource("/crontab.xml"));
			for (XElement xtask : xtasks.childrenWithName("task")) {
				CrontabTaskSettings task = new CrontabTaskSettings();
				task.load(xtask);
				tasks.put(task.id, task);
			}
		} catch (IOException | XMLStreamException ex) {
			LOGGER.error(ex.toString(), ex);
		}
	}
	/**
	 * Check and run tasks.
	 */
	protected void checkTasks() {
		DateTime now = new DateTime();
		for (CrontabTaskSettings t : tasks.values()) {
			if (!runningTask.containsKey(t.id)) {
				if (t.invoke(now)) {
					runTask(new CrontabTaskSettings(t));
				}
			}
		}
	}
	/**
	 * Prepare the execution of a crontab task.
	 * @param t the task definition
	 */
	protected void runTask(final CrontabTaskSettings t) {
		LOGGER.debug("Running task " + t.id);
		runningTask.put(t.id, t.id);
		
		try {
			Class<?> clazz = Class.forName(t.clazz);
			if (!CrontabTask.class.isAssignableFrom(clazz)) {
				LOGGER.error("Class " + clazz + " is not a CrontabTask");
				return;
			}
			final CrontabTask ti = CrontabTask.class.cast(clazz.newInstance());
			
			taskPool.execute(new Runnable() {
				@Override
				public void run() {
					try {
						ti.execute(t);
					} catch (Throwable t) {
						LOGGER.error(t.toString(), t);
					} finally {
						runningTask.remove(t.id);
						LOGGER.debug("Finished task " + t.id);
					}
				}
			});
		} catch (ClassNotFoundException | IllegalAccessException | InstantiationException ex) {
			LOGGER.error(ex.toString(), ex);
		}
	}
	@Override
	public void destroy() {
		ScheduledExecutorService sexec = scheduler;
		scheduler = null;
		if (sexec != null) {
			sexec.shutdown();
		}
		
		ExecutorService exec = taskPool;
		taskPool = null;
		if (exec != null) {
			exec.shutdown();
		}
		super.destroy();
		LOGGER.info("Crontab Servlet Destroy");
	}
}
