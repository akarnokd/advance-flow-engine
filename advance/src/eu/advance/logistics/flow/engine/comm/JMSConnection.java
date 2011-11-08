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

import javax.jms.JMSException;
import javax.jms.Queue;
import javax.jms.QueueConnection;
import javax.jms.QueueReceiver;
import javax.jms.QueueSender;
import javax.jms.QueueSession;
import javax.naming.Context;

/**
 * An immutable class that wraps the various objects of a JMS based connection:
 * the context, connection, session, queue, etc.
 * @author akarnokd, 2011.10.05.
 */
public class JMSConnection {
	/** The JNDI context of the JMS driver. */
	private final Context ctx;
	/** The queue connection. */
	private final QueueConnection conn;
	/** The queue session. */
	private final QueueSession session;
	/** The queue reference. */
	private final Queue queue;
	/**
	 * Initialize the connection with the supplied JMS objects.
	 * @param ctx the JNDI context
	 * @param conn the queue connection
	 * @param session the session
	 * @param queue the target queue
	 */
	public JMSConnection(Context ctx, QueueConnection conn, QueueSession session, Queue queue) {
		this.ctx = ctx;
		this.conn = conn;
		this.session = session;
		this.queue = queue;
	}
	/**
	 * @return creates a receiver for the target queue
	 * @throws JMSException if error occurs
	 */
	public QueueReceiver createReceiver() throws JMSException {
		return session.createReceiver(queue);
	}
	/**
	 * @return creates a sender for the target queue
	 * @throws JMSException if error occurs
	 */
	public QueueSender createSender()  throws JMSException {
		return session.createSender(queue);
	}
	/** @return the queue object. */
	public Queue queue() {
		return queue;
	}
	/** @return the session object. */
	public QueueSession session() {
		return session;
	}
	/** @return the connection object. */
	public QueueConnection connection() {
		return conn;
	}
	/** @return the JNDI context. */
	public Context context() {
		return ctx;
	}
}
