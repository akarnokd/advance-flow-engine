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

package eu.advance.logistics.flow.engine;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URL;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLParameters;
import javax.net.ssl.SSLPeerUnverifiedException;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.net.httpserver.Authenticator;
import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpsConfigurator;
import com.sun.net.httpserver.HttpsExchange;
import com.sun.net.httpserver.HttpsParameters;
import com.sun.net.httpserver.HttpsServer;

import eu.advance.logistics.flow.engine.util.KeystoreManager;

/**
 * Simple class to listen for HTTPS based engine control messages by using
 * Java's built-in HTTPS server.
 * @author karnokd, 2011.09.26.
 */
public final class AdvanceHttpsListener {
	/** The logger. */
	protected static final Logger LOG = LoggerFactory.getLogger(AdvanceHttpsListener.class);
	/** Utility class for now. */
	private AdvanceHttpsListener() {
		// for now
	}
	/**
	 * The main program.
	 * @param args no arguments at the moment
	 */
	public static void main(String[] args) {
//		System.setProperty("javax.net.debug", "ssl,handshake");
//		testHTTP();
//		testHTTPS();
		testHTTPSCert();
	}
	/** Test the internal http server. */
	static void testHTTP() {
		try {
			HttpServer server = HttpServer.create(new InetSocketAddress(8082), 10);
			server.createContext("/", new AdvanceHTTPHandler());
			server.start();
			LOG.debug("Waiting for server startup");
			TimeUnit.SECONDS.sleep(2);
			
			
			URL u = new URL("http://localhost:8082/");
			printResult(u.openStream());
			LOG.debug("Shutting down server");
			server.stop(2);

		} catch (IOException ex) {
			LOG.error(ex.toString(), ex);
		} catch (InterruptedException ex) {
			LOG.error(ex.toString(), ex);
		}
	}
	/**
	 * @param in the input stream
	 * @throws IOException on error
	 */
	protected static void printResult(InputStream in) throws IOException {
		try {
			BufferedReader bin = new BufferedReader(new InputStreamReader(in));
			String line = null;
			while ((line = bin.readLine()) != null) {
				LOG.info(line);
			}
		} finally {
			in.close();
		}
	}
	/** The handler for the built-in HTTP server. */
	static class AdvanceHTTPHandler implements HttpHandler {
		@Override
		public void handle(HttpExchange exchange) throws IOException {
			String result = "Hello world";
			exchange.sendResponseHeaders(200, result.length());
			OutputStream out = exchange.getResponseBody();
			out.write(result.getBytes());
			out.close();
		}
	}
	/** @return Prepare the keystore. */
	static KeystoreManager prepareKeystore() {
		KeystoreManager mgr = new KeystoreManager();
		mgr.load("conf/keystore", "advance".toCharArray());
		try {
			if (!mgr.getKeyStore().containsAlias("advance-server")) {
				mgr.generateRSACert("advance-server", "CN=localhost", "CN=localhost", "localhost", "advance".toCharArray());
				LOG.info("Creating key: advance-server");
			}
			if (!mgr.getKeyStore().containsAlias("advance-client")) {
				mgr.generateRSACert("advance-client", "CN=localhost-client", "CN=localhost-client", "localhost", "advance".toCharArray());
				LOG.info("Creating key: advance-client");
			}
			mgr.save("conf/keystore", "advance".toCharArray());
		} catch (KeyStoreException ex) {
			LOG.error(ex.toString(), ex);
		}
		return mgr;
	}
	/**
	 * Test a HTTPS connection.
	 */
	static void testHTTPS() {
		KeyStore ks = prepareKeystore().getKeyStore();
		try {
			KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
			kmf.init(ks, "advance".toCharArray());
			
			TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509");
			tmf.init(ks);
			
			final SSLContext ssl = SSLContext.getInstance("TLS");
			ssl.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);
			
			HttpsServer server = HttpsServer.create(new InetSocketAddress(8443), 10);
			server.setHttpsConfigurator(new HttpsConfigurator(ssl) {
				@Override
				public void configure(HttpsParameters params) {
					SSLContext c = getSSLContext();
					SSLEngine e = c.createSSLEngine();
					SSLParameters sslparams = c.getDefaultSSLParameters();

					params.setNeedClientAuth(false);
					params.setCipherSuites(e.getEnabledCipherSuites());
					params.setProtocols(e.getEnabledProtocols());
					
					params.setSSLParameters(sslparams);
				}
			});
			
			server.createContext("/", new AdvanceHTTPHandler());
			server.start();
			try {
				LOG.debug("Waiting for server startup");
				TimeUnit.SECONDS.sleep(2);
				
				runHttpsClient();
			} finally {			
				LOG.debug("Shutting down server");
				server.stop(2);
			}
		} catch (Exception ex) {
			LOG.error(ex.toString(), ex);
		}
	}
	/**
	 * Connect to the test HTTPS server via the already known certificate.
	 * @throws IOException on error
	 */
	protected static void runHttpsClient()
			throws IOException {
		try {
			KeyStore ks = prepareKeystore().getKeyStore();
			TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509");
			tmf.init(ks);
				
			SSLContext ctx = SSLContext.getInstance("TLS");
			ctx.init(null, tmf.getTrustManagers(), null);
			
			URL u = new URL("https://localhost:8443/");
			HttpsURLConnection c = (HttpsURLConnection)u.openConnection();
			c.setSSLSocketFactory(ctx.getSocketFactory());
			
			c.connect();
			
			printResult(c.getInputStream());
		} catch (Exception ex) {
			LOG.error(ex.toString(), ex);
		}
	}
	/**
	 * Test a HTTPS connection with both sides certificate.
	 */
	static void testHTTPSCert() {
		final KeyStore ks = prepareKeystore().getKeyStore();
		try {
			KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
			kmf.init(ks, "advance".toCharArray());
			
			TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509");
			tmf.init(ks);
			
			final SSLContext ssl = SSLContext.getInstance("TLS");
			ssl.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);
			
			HttpsServer server = HttpsServer.create(new InetSocketAddress(8443), 10);
			server.setHttpsConfigurator(new HttpsConfigurator(ssl) {
				@Override
				public void configure(HttpsParameters params) {
					SSLContext c = getSSLContext();
					SSLEngine e = c.createSSLEngine();
					SSLParameters sslparams = c.getDefaultSSLParameters();

					
					sslparams.setNeedClientAuth(true);
					sslparams.setCipherSuites(e.getEnabledCipherSuites());
					sslparams.setProtocols(e.getEnabledProtocols());
					
					params.setSSLParameters(sslparams);
				}
			});
			
			HttpContext ctx = server.createContext("/", new AdvanceHTTPHandler());
			
			ctx.setAuthenticator(new Authenticator() {
				@Override
				public Result authenticate(HttpExchange t) {
					if (t instanceof HttpsExchange) {
						try {
							HttpsExchange x = (HttpsExchange)t;
							
							Certificate[] certs = x.getSSLSession().getPeerCertificates();
							
							for (Certificate c : certs) {
								System.out.println(((X509Certificate)c).getSubjectDN());
								try {
									String alias = ks.getCertificateAlias(c);
									System.out.println(alias);
									if (alias == null) {
										return new Authenticator.Failure(403);
									}
								} catch (KeyStoreException ex) {
									LOG.error(ex.toString(), ex);
								}
							}
							
							return new Authenticator.Success(t.getPrincipal());
						} catch (SSLPeerUnverifiedException ex) {
							LOG.error(ex.toString(), ex);
						}
					}
					return new Authenticator.Failure(403);
				}
			});
			server.start();
			try {
				LOG.debug("Waiting for server startup");
				TimeUnit.SECONDS.sleep(2);
				
				runHttpsClientCert();
			} finally {			
				LOG.debug("Shutting down server");
				server.stop(2);
			}
		} catch (Exception ex) {
			LOG.error(ex.toString(), ex);
		}
	}
	/**
	 * Connect to the test HTTPS server via the already known certificate.
	 * @throws IOException on error
	 */
	protected static void runHttpsClientCert()
			throws IOException {
		try {
			KeyStore ks = prepareKeystore().getKeyStore();

			KeyStore clientKs = KeystoreManager.singleKey(ks, "advance-client", "advance".toCharArray());
			
			KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
			kmf.init(clientKs, "advance".toCharArray());
			
			TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509");
			tmf.init(ks);
				
			SSLContext ctx = SSLContext.getInstance("TLS");
			KeyManager[] kmgs = kmf.getKeyManagers();
			
			ctx.init(kmgs, tmf.getTrustManagers(), null);
			
			URL u = new URL("https://localhost:8443/");
			HttpsURLConnection c = (HttpsURLConnection)u.openConnection();
			SSLSocketFactory sf = ctx.getSocketFactory();
			c.setSSLSocketFactory(sf);
			
			c.connect();
			
			printResult(c.getInputStream());
		} catch (Exception ex) {
			LOG.error(ex.toString(), ex);
		}
	}

}
