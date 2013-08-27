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

import java.util.Hashtable;
import java.util.List;

import javax.jms.JMSException;
import javax.jms.Queue;
import javax.jms.QueueConnection;
import javax.jms.QueueConnectionFactory;
import javax.jms.QueueSender;
import javax.jms.QueueSession;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.directory.InitialDirContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;

import edu.umd.cs.findbugs.annotations.NonNull;
import eu.advance.logistics.flow.engine.api.PoolManager;
import eu.advance.logistics.flow.engine.api.core.MultiIOException;
import eu.advance.logistics.flow.engine.api.ds.AdvanceJMSEndpoint;


/**
 * Manages connection to a JMS queue.
 * <p>The created JMS session does not use transaction and requires client acknowledgement.</p>
 * @author akarnokd, 2011.10.05.
 */
public class JMSPoolManager implements PoolManager<JMSConnection> {
	/** The logger. */
	protected static final Logger LOG = LoggerFactory.getLogger(JDBCPoolManager.class);
	/** The JMS endpoint configuration. */
	protected final AdvanceJMSEndpoint endpoint;
	/**
	 * Constructor. Initializes the endpoint configuration
	 * @param endpoint the endpoint configuration
	 */
	public JMSPoolManager(@NonNull AdvanceJMSEndpoint endpoint) {
		this.endpoint = endpoint.copy();
	}
	@Override
	public JMSConnection create() throws Exception {
		Hashtable<Object, Object> env = new Hashtable<Object, Object>();
		env.put(Context.INITIAL_CONTEXT_FACTORY, endpoint.driver);
		env.put(Context.PROVIDER_URL, endpoint.url);
		
		Context ctx = null;
		QueueConnectionFactory f = null;
		QueueConnection conn = null;
		QueueSession session = null;
		
		LOG.debug("Create JMS InitialDirContext");
		ctx = new InitialDirContext(env);
		LOG.debug("Lookup JMS QueueConnectionFactory");
		f = (QueueConnectionFactory)ctx.lookup(endpoint.queueManager);
		
		LOG.debug("Prepare JMS connection");
		if (endpoint.user != null && !endpoint.user.isEmpty()) {
			conn = f.createQueueConnection(endpoint.user, new String(endpoint.password()));
		} else {
			conn = f.createQueueConnection();
		}
		LOG.debug("Start JMS connection");
		conn.start();
		
		LOG.debug("Create JMS session");
		session = conn.createQueueSession(false, Session.CLIENT_ACKNOWLEDGE);
		
		LOG.debug("Create JMS queue");
		Queue queue = session.createQueue(endpoint.queue);
		
		return new JMSConnection(ctx, conn, session, queue);
	}

	@Override
	public boolean verify(JMSConnection obj) throws Exception {
		try {
			QueueSender sender = obj.createSender();
			TextMessage message = obj.session().createTextMessage("<test/>");
			sender.send(message);
			return true;
		} catch (JMSException ex) {
			LOG.warn(ex.toString(), ex);
		}
		return false;
	}

	@Override
	public void close(JMSConnection obj) throws Exception {
		List<Throwable> lst = Lists.newArrayList();
		try {
			obj.session().close();
		} catch (JMSException ex) {
			lst.add(ex);
		}
		try {
			obj.connection().close();
		} catch (JMSException ex) {
			lst.add(ex);
		}
		try {
			obj.context().close();
		} catch (NamingException ex) {
			lst.add(ex);
		}
		if (!lst.isEmpty()) {
			throw new MultiIOException(lst);
		}
	}
	/**
	 * Test if the supplied data source can be accessed.
	 * @param endpoint the endpoint settings
	 * @return the error message or empty string
	 */
	public static String test(@NonNull AdvanceJMSEndpoint endpoint) {
		JMSPoolManager mgr = new JMSPoolManager(endpoint);
		try {
			JMSConnection conn = mgr.create();
			try {
				if (mgr.verify(conn)) {
					return "";
				}
				return "Verification failed on JMS connection due unknown reasons";
			} finally {
				mgr.close(conn);
			}
		} catch (Exception ex) {
			LOG.error(ex.toString(), ex);
			return ex.toString();
		}
	}
}
