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

package eu.advance.logistics.flow.engine.api.impl;

import hu.akarnokd.reactive4java.base.Closeables;
import hu.akarnokd.reactive4java.base.Scheduler;
import hu.akarnokd.reactive4java.reactive.Observable;
import hu.akarnokd.reactive4java.reactive.Observer;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.advance.logistics.flow.engine.api.AdvanceHttpAuthentication;
import eu.advance.logistics.flow.engine.api.AdvanceXMLCommunicator;
import eu.advance.logistics.flow.engine.api.ds.AdvanceLoginType;
import eu.advance.logistics.flow.engine.util.Base64;
import eu.advance.logistics.flow.engine.util.KeystoreManager;
import eu.advance.logistics.flow.engine.xml.XElement;

/**
 * Send and receive requests through HTTP or HTTPS,
 * with none, basic or certificate authentication.
 * @author akarnokd, 2011.09.27.
 */
public class HttpCommunicator implements AdvanceXMLCommunicator {
	/** The logger. */
	protected static final Logger LOG = LoggerFactory.getLogger(HttpCommunicator.class);
	/** The authentication record. */
	public AdvanceHttpAuthentication authentication;
	/** The endpoint URL. */
	public URL url;
	/** The encryption protocol. */
	public String baseProtocol = "TLS";
	/**
	 * Prepare the connection.
	 * @return the connection object
	 * @throws IOException a keystore related error occurs
	 */
	public HttpURLConnection prepare() throws IOException {
		boolean isHttps = "https".equals(url.getProtocol());
		if (authentication.loginType == AdvanceLoginType.CERTIFICATE && !isHttps) {
			throw new IllegalStateException("Certificate login works only with HTTPS endpoint!");
		}
		try {
			HttpURLConnection c = null;
			if (isHttps) {
				KeyManagerFactory kmf = null;
				TrustManagerFactory tmf = null;
				if (authentication.certStore != null) {
					tmf = TrustManagerFactory.getInstance("SunX509");
					tmf.init(authentication.certStore);
				}
				
				HttpsURLConnection conn = (HttpsURLConnection)url.openConnection();
				if (authentication.loginType == AdvanceLoginType.BASIC) {
					StringBuilder userPass = new StringBuilder();
					userPass.append(authentication.name).append(":").append(authentication.password());
					conn.setRequestProperty("Authorization", "Basic " + Base64.encodeBytes(userPass.toString().getBytes("UTF-8")));
				} else
				if (authentication.loginType == AdvanceLoginType.CERTIFICATE) {
					KeyStore ks = KeystoreManager.singleKey(authentication.authStore, authentication.name, authentication.password());
					kmf = KeyManagerFactory.getInstance("SunX509");
					kmf.init(ks, authentication.password());
				}
				
				SSLContext ctx = SSLContext.getInstance(baseProtocol);
				ctx.init(kmf != null ? kmf.getKeyManagers() : null, 
						tmf != null ? tmf.getTrustManagers() : null, null);
				
				conn.setSSLSocketFactory(ctx.getSocketFactory());
				conn.setDoInput(true);
				conn.setDoOutput(true);
				
				c = conn;
			} else {
				HttpURLConnection conn = (HttpURLConnection)url.openConnection();
				if (authentication.loginType == AdvanceLoginType.BASIC) {
					StringBuilder userPass = new StringBuilder();
					userPass.append(authentication.name).append(":").append(authentication.password());
					conn.setRequestProperty("Authorization", "Basic " + Base64.encodeBytes(userPass.toString().getBytes("UTF-8")));
				}
				c = conn;
			}
			return c;
		} catch (NoSuchAlgorithmException ex) {
			LOG.error(ex.toString(), ex);
			throw new IOException(ex);
		} catch (KeyStoreException ex) {
			LOG.error(ex.toString(), ex);
			throw new IOException(ex);
		} catch (UnrecoverableKeyException ex) {
			LOG.error(ex.toString(), ex);
			throw new IOException(ex);
		} catch (KeyManagementException ex) {
			LOG.error(ex.toString(), ex);
			throw new IOException(ex);
		}
	}
	/**
	 * Send an XML message via POST.
	 * @param message the message to send
	 * @throws IOException on error
	 */
	@Override
	public void send(XElement message) throws IOException {
		HttpURLConnection c = prepare();
		try {
			c.setRequestMethod("POST");
			c.setRequestProperty("Content-Type", "text/xml;charset=utf-8");
			c.connect();
			
			OutputStream out = c.getOutputStream();
			try {
				message.save(out);
			} finally {
				out.close();
			}
			if (c.getResponseCode() < 400) {
				InputStream in = c.getInputStream();
				try {
					byte[] buffer = new byte[8192];
					while (true) {
						if (in.read(buffer) < 0) {
							break;
						}
					}
				} finally {
					in.close();
				}
			} else {
				getHttpError(c);
			}
		} finally {		
			c.disconnect();
		}
	}
	/**
	 * Send and receive an XML message via POST.
	 * @param message the message to send
	 * @return the message returned
	 * @throws IOException on error
	 */
	@Override
	public XElement query(XElement message) throws IOException {
		HttpURLConnection c = prepare();
		try {
			c.setRequestMethod("POST");
			c.setRequestProperty("Content-Type", "text/xml;charset=utf-8");
			c.connect();
			
			OutputStream out = c.getOutputStream();
			try {
				message.save(out);
			} finally {
				out.close();
			}
			if (c.getResponseCode() < 400) {
				InputStream in = c.getInputStream();
				try {
					XElement result = XElement.parseXML(in);
					return result;
				} finally {
					in.close();
				}
			} else {
				getHttpError(c);
				return null;
			}
		} catch (XMLStreamException ex) {
			LOG.error(ex.toString(), ex);
			throw new IOException(ex);
		} finally {		
			c.disconnect();
		}
	}
	/**
	 * Receive an XML message via GET.
	 * @return the message returned
	 * @throws IOException on error
	 */
	@Override
	public XElement query() throws IOException {
		HttpURLConnection c = prepare();
		try {
			c.setRequestMethod("GET");
			c.connect();
			
			if (c.getResponseCode() < 400) {
				InputStream in = c.getInputStream();
				try {
					XElement result = XElement.parseXML(in);
					return result;
				} finally {
					in.close();
				}
			} else {
				getHttpError(c);
				return null;
			}
		} catch (XMLStreamException ex) {
			LOG.error(ex.toString(), ex);
			throw new IOException(ex);
		} finally {		
			c.disconnect();
		}
	}
	/**
	 * Retrieve data from the URL.
	 * @return the data bytes
	 * @throws IOException if a network or authentication error occurs
	 */
	public byte[] get() throws IOException {
		HttpURLConnection c = prepare();
		try {
			c.setRequestMethod("GET");
			c.connect();
			
			InputStream in = c.getInputStream();
			try {
				int len = c.getContentLength();
				ByteArrayOutputStream out = new ByteArrayOutputStream(len > 0 ? len : 8192);
				byte[] buffer = new byte[8192];
				while (true) {
					int read = in.read(buffer);
					if (read > 0) {
						out.write(buffer, 0, read);
					} else
					if (read < 0) {
						break;
					}
				}
				return out.toByteArray();
			} finally {
				in.close();
			}
		} finally {		
			c.disconnect();
		}
	}
	@Override
	public Observable<XElement> receive(final XElement request, final Scheduler scheduler) {
		return new Observable<XElement>() {
			@Override
			public Closeable register(final Observer<? super XElement> observer) {
				try {
					final HttpURLConnection c = prepare();
					try {
						c.setRequestMethod("POST");
						c.setRequestProperty("Content-Type", "text/xml;charset=utf-8");
						c.connect();
						
						OutputStream out = c.getOutputStream();
						try {
							request.save(out);
						} finally {
							out.close();
						}
						final AtomicBoolean closed = new AtomicBoolean();
						if (c.getResponseCode() < 400) {
							final InputStream in = c.getInputStream();
							final Closeable cs = scheduler.schedule(new Runnable() {
								@Override
								public void run() {
									try {
										try {
											XMLInputFactory inf = XMLInputFactory.newInstance();
											XMLStreamReader ir = inf.createXMLStreamReader(in);
											try {
												ir.nextTag();
												while (ir.hasNext()) {
													XElement e = XElement.parseXMLFragment(ir);
													observer.next(e);
												}
												observer.finish();
											} finally {
												ir.close();
											}
										} finally {
											in.close();
										}
									} catch (IOException ex) {
										LOG.error(ex.toString(), ex);
										if (!closed.get()) {
											observer.error(ex);
										}
									} catch (XMLStreamException ex) {
										LOG.error(ex.toString(), ex);
										if (!closed.get()) {
											observer.error(ex);
										}
									} finally {
										c.disconnect();
									}
								}
							});
							return new Closeable() {
								@Override
								public void close() throws IOException {
									closed.set(true);
									try {
										cs.close();
									} catch (IOException ex) {
										LOG.warn(ex.toString(), ex);
									}
									try {
										in.close();
									} catch (IOException ex) {
										LOG.warn(ex.toString(), ex);
									}
									c.disconnect();
								}
							};
						} else {
							getHttpError(c);
							return new Closeable() {
								@Override
								public void close() throws IOException {
									// nothing to do
								}
							};
						}
					} catch (IOException ex) {
						LOG.error(ex.toString(), ex);
						observer.error(ex);
						return Closeables.emptyCloseable();
					}
				} catch (IOException ex) {
					LOG.error(ex.toString(), ex);
					observer.error(ex);
					return Closeables.emptyCloseable();
				}
			}
		};
	}
	/**
	 * Extracts the error contents from the connection and throws an IOException with it.
	 * @param c the connection
	 * @throws IOException the exception thrown
	 */
	public static void getHttpError(final HttpURLConnection c)
			throws IOException {
		BufferedReader in = new BufferedReader(new InputStreamReader(c.getErrorStream(), "UTF-8"));
		StringBuilder b = new StringBuilder();
		try {
			char[] buf = new char[8192];
			while (!Thread.currentThread().isInterrupted()) {
				int read = in.read(buf);
				if (read > 0) {
					b.append(buf, 0, read);
				} else
				if (read < 0) {
					break;
				}
			}
		} finally {
			in.close();
		}
		throw new IOException(b.toString());
	}
}
