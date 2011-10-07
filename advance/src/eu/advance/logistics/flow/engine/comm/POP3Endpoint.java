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

import java.io.BufferedReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.mail.BodyPart;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.Store;

import org.apache.commons.net.pop3.POP3Client;
import org.apache.commons.net.pop3.POP3MessageInfo;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

/**
 * Represents the functions of a POP3 based e-mail account.
 * @author karnokd, 2011.10.07.
 */
public final class POP3Endpoint {
	/** Utility for now. */
	private POP3Endpoint() {
		
	}
	/**
	 * A POP3 header parser.
	 * @author karnokd, 2011.10.07.
	 */
	public static class POP3Header {
		/** The map of header properties. */
		protected final Map<String, String> properties = Maps.newHashMap();
		/**
		 * Constructor. Parses the header from the buffered reader.
		 * @param in the input reader
		 * @throws IOException if a network error occurs while parsing
		 */
		public POP3Header(BufferedReader in) throws IOException {
			String line = null;
			while ((line = in.readLine()) != null) {
				int idx = line.indexOf(':');
				if (idx > 0) {
					properties.put(line.substring(0, idx).toLowerCase(), line.substring(idx + 2));
				}
			}
		}
		/** @return the subject. */
		public String getSubject() {
			return properties.get("subject");
		}
		/** @return the sender. */
		public String getFrom() {
			return properties.get("from");
		}
		/** @return the recipient. */
		public String getTo() {
			return properties.get("to");
		}
		/** @return the message date. */
		public String getDate() {
			return properties.get("date");
		}
		/**
		 * Retrieve an arbitrary key.
		 * @param key the key name
		 * @return the value or null if not present
		 */
		public String get(String key) {
			return properties.get(key);
		}
		@Override
		public String toString() {
			return properties.toString();
		}
	}
	/**
	 * Test program.
	 * @param args no arguments
	 * @throws Exception on error
	 */
	public static void main(String[] args) throws Exception {
//		viaCommonsNet();
		viaJavaMail();
	}
	/**
	 * Retrieve mails via commons-net POP3Client.
	 * @throws IOException on error
	 */
	static void viaCommonsNet() throws IOException {
		POP3Client client = new POP3Client();
		client.connect("freemail.hu");
		if (client.login("karnok.d", "")) {
			POP3MessageInfo[] list = client.listMessages();
			for (POP3MessageInfo mi : list) {
				List<String> lines = Lists.newArrayList();
				String line = null;
				
				POP3Header header = new POP3Header((BufferedReader)client.retrieveMessageTop(mi.number, 1));
				System.out.println(header);
				System.out.println();
				BufferedReader r = (BufferedReader)client.retrieveMessage(mi.number);
				while ((line = r.readLine()) != null) {
					lines.add(line);
				}
				for (String s : lines) {
					System.out.println(s);
				}
				System.out.println();
			}
			client.logout();
		} else {
			System.out.println(client.getReplyString());
		}
		client.disconnect();
	}
	/**
	 * Retrieve messages via JavaMail.
	 * @throws Exception on error
	 */
	static void viaJavaMail() throws Exception {
		Session session = Session.getInstance(System.getProperties(), null);
		Store store = session.getStore("pop3");
		
		store.connect("freemail.hu", "", "");
		try {
			Folder folder = store.getDefaultFolder();
			Folder inbox = folder.getFolder("INBOX");
			inbox.open(Folder.READ_ONLY);
			try {
				Message[] messages = inbox.getMessages();
				for (Message msg : messages) {
					int attachCnt = 0;
					Object content = msg.getContent();
					if (content instanceof Multipart) {
						Multipart multipart = (Multipart)content;
						for (int i = 0; i < multipart.getCount(); i++) {
							BodyPart bp = multipart.getBodyPart(i);
							bp.toString();
							attachCnt++;
						}
						
					}
					System.out.printf("From: %s, Subject: %s, Attachment: %s%n",
						Arrays.toString(msg.getFrom()),
						msg.getSubject(),
						attachCnt
					);
				}
			} finally {
				inbox.close(false);
			}
		} finally {
			store.close();
		}
	}
}
