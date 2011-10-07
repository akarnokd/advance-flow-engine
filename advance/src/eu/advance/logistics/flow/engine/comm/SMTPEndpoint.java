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

package eu.advance.logistics.flow.engine.comm;

import java.util.Date;
import java.util.Properties;

import javax.activation.DataHandler;
import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.Message.RecipientType;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.util.ByteArrayDataSource;

/**
 * An SMTP endpoint.
 * <a href='http://www.javaworld.com/javaworld/jw-10-2001/jw-1026-javamail.html?page=3'>Example</a>
 * @author karnokd, 2011.10.07.
 */
public final class SMTPEndpoint {
	/** Utility for now. */
	private SMTPEndpoint() {
		
	}
	/**
	 * @param args no arguments
	 * @throws Exception on error
	 */
	public static void main(String[] args) throws Exception {
		Properties props = new Properties();
		props.setProperty("mail.smtp.host", "mail.sztaki.hu");
		Session session = Session.getInstance(props, new Authenticator() {
			@Override
			protected PasswordAuthentication getPasswordAuthentication() {
				return new PasswordAuthentication("", "");
			}
		});

		Message msg = compose(session);
		
		Transport.send(msg);
	}
	/**
	 * Compose the test message.
	 * @param session the session object
	 * @return the message
	 * @throws Exception on error
	 */
	static Message compose(Session session) throws Exception {
		Message msg = new MimeMessage(session);
		msg.setFrom(new InternetAddress("karnok@sztaki.hu"));
		msg.setRecipient(Message.RecipientType.TO, new InternetAddress("karnok.d@freemail.hu"));
		msg.setSubject("Testing message and attachment");
		msg.setSentDate(new Date());

		MimeBodyPart body = new MimeBodyPart();
		body.setText("Hi!");
		
		Multipart multipart = new MimeMultipart();
		multipart.addBodyPart(body);
		
		MimeBodyPart attach = new MimeBodyPart();
		
		attach.setDataHandler(new DataHandler(new ByteArrayDataSource("<?xml version='1.0' encoding='UTF-8'?><example/>".getBytes("UTF-8"), "text/xml")));
		attach.setFileName("sample.xml");
		
		multipart.addBodyPart(attach);
		
		msg.setContent(multipart);

		return msg;
	}
	/**
	 * Send message via SMTPS.
	 * <a href='http://www.rgagnon.com/javadetails/java-0570.html'>Example</a>
	 * @throws Exception on error
	 */
	static void viaJavaMailSMTPS() throws Exception {
		Properties props = new Properties();
		props.put("mail.transport.protocol", "smtps");
		props.put("mail.smtps.host", "mail.sztaki.hu");
		props.put("mail.smtps.auth", "true");
		
		Session session = Session.getInstance(props);
		
		Transport transport = session.getTransport();
		
		Message msg = compose(session);
		
		transport.connect("mail.sztaki.hu", "", "");
		
		transport.sendMessage(msg, msg.getRecipients(RecipientType.TO));
		
		transport.close();
		
	}

}
