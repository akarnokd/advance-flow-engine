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

import java.util.Arrays;
import java.util.Properties;

import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.Store;

/**
 * Testing IMAP(s) connection.
 * <a href='http://www.rgagnon.com/javadetails/java-receive-email-using-imap.html'>Example.</a>
 * @author akarnokd, 2011.10.07.
 */
public final class IMAPEndpoint {

	/**
	 * Utility class for now.
	 */
	private IMAPEndpoint() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param args no arguments
	 * @throws Exception ignored
	 */
	public static void main(String[] args) throws Exception {
		Properties props = new Properties();
		props.setProperty("mail.store.protocol", "imaps");
		
		Session session = Session.getInstance(props, null);
		
		Store store = session.getStore("imaps");
		
		store.connect("imap.gmail.com", "", "");
		try {
			Folder inbox = store.getFolder("Inbox");
			inbox.open(Folder.READ_ONLY);
			try {
				Message[] messages = inbox.getMessages();
				for (Message msg : messages) {
					if (!msg.isSet(Flags.Flag.SEEN)) {
						System.out.print("*New* ");
					}
					Object content = msg.getContent();
					int count = 0;
					if (content instanceof Multipart) {
						Multipart multipart = (Multipart) content;
						count = multipart.getCount();
						
					}
					System.out.printf("From: %s, Subject: %s, Attachments: %s%n", 
							Arrays.toString(msg.getFrom()), msg.getSubject(), count);
					// msg.setFlag(Flags.Flag.SEEN, true);
					// msg.setFlag(Flags.Flag.DELETED, true);
				}
			} finally {
				inbox.close(false);
			}
		} finally {
			store.close();
		}
	}

}
