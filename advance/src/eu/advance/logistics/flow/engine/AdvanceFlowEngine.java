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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.Arrays;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLParameters;
import javax.net.ssl.SSLPeerUnverifiedException;
import javax.net.ssl.TrustManagerFactory;
import javax.xml.stream.XMLStreamException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.net.httpserver.Authenticator;
import com.sun.net.httpserver.BasicAuthenticator;
import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpsConfigurator;
import com.sun.net.httpserver.HttpsExchange;
import com.sun.net.httpserver.HttpsParameters;
import com.sun.net.httpserver.HttpsServer;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import eu.advance.logistics.flow.engine.api.AdvanceControlException;
import eu.advance.logistics.flow.engine.api.AdvanceDataStore;
import eu.advance.logistics.flow.engine.api.AdvanceXMLExchange;
import eu.advance.logistics.flow.engine.api.AdvanceKeyStore;
import eu.advance.logistics.flow.engine.api.AdvanceUser;
import eu.advance.logistics.flow.engine.api.impl.HttpEngineControlListener;
import eu.advance.logistics.flow.engine.api.impl.LocalEngineControl;
import eu.advance.logistics.flow.engine.util.KeystoreManager;
import eu.advance.logistics.flow.engine.xml.typesystem.XElement;

/**
 * The Advance Data Flow Engine main program.
 * The engine should be run as a standalone program to be able to:
 * <ul>
 * <li>Run it independently of the web application. This allows it to be placed beside the web application or onto a completely different server.</li>
 * <li>JVM or Java crash in the flow engine or in the web application should not affect the other party.</li>
 * </ul>
 *  @author akarnokd
 */
public class AdvanceFlowEngine implements Runnable {
	/**
	 * The attribute name to store the control token in the current session.
	 */
	private static final String LOGIN_USERNAME = "advance-login-username";
	/** The logger. */
	protected static final Logger LOG = LoggerFactory.getLogger(AdvanceFlowEngine.class);
	/** The version of the flow engine. */
	public static final String VERSION = "0.03.119";
	/** The configuration. */
	private AdvanceEngineConfig config;
	/** The basic server. */
	private HttpsServer basicServer;
	/** The certificate server. */
	private HttpsServer certServer;
	/** The listener to translate XML requests into control requests. */
	private HttpEngineControlListener engineListener;
	@Override
	public void run() {
		LOG.info("Advance Flow Engine Started");
		
		config = new AdvanceEngineConfig();
		try {
			XElement xconfig = XElement.parseXML("conf/flow_engine_config.xml");
			config.initialize(xconfig);
			AdvanceCompiler compiler = new AdvanceCompiler(config.schemaResolver, config.blockResolver, config.schedulerMap);
			LocalEngineControl control = new LocalEngineControl(config.localDataStore, config.schemas, compiler, compiler) {
				@Override
				public void shutdown() throws IOException,
						AdvanceControlException {
					LOG.info("Shutting down basic server.");
					basicServer.stop(5);
					LOG.info("Shutting down certificate server.");
					certServer.stop(5);
					LOG.info("Server listeners shut down.");
					super.shutdown();
					config.close();
				}
			};
			engineListener = new HttpEngineControlListener(control);
			initListeners();
		} catch (IOException ex) {
			LOG.error(ex.toString(), ex);
		} catch (XMLStreamException ex) {
			LOG.error(ex.toString(), ex);
		}
		
		
		LOG.info("Advance Flow Engine Terminated");
	}
	/** Initialize the listeners. */
	public void initListeners() {
		try {
			AdvanceKeyStore aks = config.keystores.get(config.listener.serverKeyStore);
			if (aks == null) {
				LOG.error("Missing server keystore " + config.listener.serverKeyStore);
			}
			KeystoreManager smgr = new KeystoreManager();
			smgr.load(aks.location, aks.password());
			
			KeyStore sks = KeystoreManager.singleKey(smgr.getKeyStore(), config.listener.serverKeyAlias, config.listener.serverKeyPassword);
			
			AdvanceKeyStore aks2 = config.keystores.get(config.listener.clientKeyStore);
			if (aks2 == null) {
				LOG.error("Missing client keystore " + config.listener.clientKeyStore);
			}
			KeystoreManager cmgr = new KeystoreManager();
			cmgr.load(aks2.location, aks2.password());
	
			KeyStore cks = cmgr.getKeyStore();
			
			KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
			kmf.init(sks, config.listener.serverKeyPassword);
			
			TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509");
			tmf.init(cks);
			
			HttpHandler handler = createHandler();
			
			basicServer = createServer(config.listener.basicPort, kmf, null);
			HttpContext basicCtx = basicServer.createContext("/", handler);
			setBasicAuthenticator(basicCtx, config.localDataStore);
			
			certServer = createServer(config.listener.certificatePort, kmf, tmf);
			
			HttpContext certCtx = certServer.createContext("/", handler);
			setCertAuthenticator(certCtx, cks, config.localDataStore);
			
			basicServer.start();
			certServer.start();
		} catch (NoSuchAlgorithmException ex) {
			LOG.error(ex.toString(), ex);
		} catch (KeyStoreException ex) {
			LOG.error(ex.toString(), ex);
		} catch (UnrecoverableKeyException ex) {
			LOG.error(ex.toString(), ex);
		}
	}
	/** @return create a XML request handler. */
	protected HttpHandler createHandler() {
		return new HttpHandler() {
			@Override
			public void handle(final HttpExchange request) throws IOException {
				XElement xrequest = null;
				InputStream in = request.getRequestBody();
				try {
					xrequest = XElement.parseXML(in);
				} catch (XMLStreamException ex) {
					LOG.error(ex.toString(), ex);
					sendResponse(request, 400, ex.toString());
					return;
				} finally {
					in.close();
				}
				final XElement frequest = xrequest;
				try {
					final String userName = (String)request.getAttribute(LOGIN_USERNAME);
					
					request.getResponseHeaders().add("Content-Type", "text/xml;charset=utf-8");
					final OutputStream out = request.getResponseBody();
					try {
						engineListener.dispatch(new AdvanceXMLExchange() {
							/** Is there a multiple response? */
							private boolean multiResponse;
							/** Send out the headers? */
							private boolean first;
							@Override
							public void next(XElement value) throws IOException {
								if (first) {
									request.sendResponseHeaders(200, 0);
									first = false;
								}
								value.save(out);
							}
							
							@Override
							public void finishMany() throws IOException {
								if (multiResponse) {
									out.write("</multiple-fragments>".getBytes("UTF-8"));
								} else {
									LOG.error("startMany was not called!");
								}
							}
							
							@Override
							public String userName() {
								return userName;
							}
							
							@Override
							public XElement request() {
								return frequest;
							}
							
							@Override
							public void startMany() throws IOException {
								multiResponse = true;
								if (first) {
									request.sendResponseHeaders(200, 0);
									first = false;
								}
								out.write("<?xml version='1.0' encoding='UTF-8'?><multiple-fragments>".getBytes("UTF-8"));
							}
						});
					} finally {
						out.close();
					}
				} catch (IOException ex) {
					LOG.error(ex.toString(), ex);
				} catch (AdvanceControlException ex) {
					LOG.error(ex.toString(), ex);
					sendResponse(request, 403, ex.toString());
				}
			}
			/**
			 * Send a text message response.
			 * @param exch the exchange object
			 * @param code the HTTP result code
			 * @param message the message text
			 * @throws IOException on error
			 */
			void sendResponse(HttpExchange exch, int code, String message) throws IOException {
				exch.getResponseHeaders().add("Content-Type", "text/plain;charset=utf-8");
				byte[] msg = message.getBytes("UTF-8");
				exch.sendResponseHeaders(code, msg.length);
				OutputStream out = exch.getResponseBody();
				try {
					out.write(msg);
				} finally {
					out.close();
				}
			}
		};
	}
	/**
	 * Create an username/password based authenticator with the backing datastore.
	 * @param ctx the HTTP context
	 * @param datastore the datastore
	 */
	protected void setBasicAuthenticator(HttpContext ctx, final AdvanceDataStore datastore) {
		ctx.setAuthenticator(new BasicAuthenticator("/") {
			final ThreadLocal<HttpExchange> exchanges = new ThreadLocal<HttpExchange>();
			@Override
			public Result authenticate(HttpExchange exch) {
				exchanges.set(exch);
				try {
					return super.authenticate(exch);
				} finally {
					exchanges.remove();
				}
			}
			@Override
			public boolean checkCredentials(String user, String password) {
				try {
					AdvanceUser u = datastore.queryUser(user);
					if (u != null) {
						if (Arrays.equals(password.toCharArray(), u.password())) {
							HttpExchange exch = exchanges.get();
							
							exch.setAttribute(LOGIN_USERNAME, u.name);
							
							return true;
						}
					} else {
						LOG.error("Missing user: " + user);
					}
				} catch (IOException ex) {
					LOG.error(ex.toString(), ex);
				} catch (AdvanceControlException ex) {
					LOG.error(ex.toString(), ex);
				}
				return false;
			}
		});
	}
	/**
	 * Set a certificate based authenticator fo the given context.
	 * @param ctx the target HTTP context
	 * @param clientKeyStore the keystore where the certificates should be located
	 * @param datastore the datastore to access the user's settings
	 */
	protected void setCertAuthenticator(HttpContext ctx, final KeyStore clientKeyStore, 
			final AdvanceDataStore datastore) {
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
								String alias = clientKeyStore.getCertificateAlias(c);
								LOG.debug("Found client alias: " + alias);
								if (alias != null) {
									for (AdvanceUser u : datastore.queryUsers()) {
										if (!u.passwordLogin && u.keyAlias.equals(alias)) {
											t.setAttribute(LOGIN_USERNAME, u.name);
											return new Authenticator.Success(t.getPrincipal());
										}
									}
								}
							} catch (KeyStoreException ex) {
								LOG.error(ex.toString(), ex);
							}
						}
					} catch (SSLPeerUnverifiedException ex) {
						LOG.error(ex.toString(), ex);
					} catch (AdvanceControlException ex) {
						LOG.error(ex.toString(), ex);
					} catch (IOException ex) {
						LOG.error(ex.toString(), ex);
						return new Authenticator.Failure(500);
					}
				}
				return new Authenticator.Failure(403);
			}
		});

	}
	/**
	 * Create a HTTPS server with the given TLS properties.
	 * @param port the port number
	 * @param kmf the key manager factory for the server certificate
	 * @param tmf the optional trust manager factory for the client certificates
	 * @return the initialized server but without handlers or startup
	 */
	protected HttpsServer createServer(final int port, 
			@NonNull final KeyManagerFactory kmf, 
			@Nullable final TrustManagerFactory tmf) {
		try {
			final SSLContext ssl = SSLContext.getInstance("TLS");
			if (tmf != null) {
				ssl.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);
			} else {
				ssl.init(kmf.getKeyManagers(), null, null);
			}
			
			HttpsServer server = HttpsServer.create(new InetSocketAddress(port), 10);
			server.setHttpsConfigurator(new HttpsConfigurator(ssl) {
				@Override
				public void configure(HttpsParameters params) {
					SSLContext c = getSSLContext();
					SSLEngine e = c.createSSLEngine();
					SSLParameters sslparams = c.getDefaultSSLParameters();
	
					sslparams.setNeedClientAuth(tmf != null);
					sslparams.setCipherSuites(e.getEnabledCipherSuites());
					sslparams.setProtocols(e.getEnabledProtocols());
					
					params.setSSLParameters(sslparams);
				}
			});
			return server;
		} catch (KeyManagementException ex) {
			LOG.error(ex.toString(), ex);
		} catch (NoSuchAlgorithmException ex) {
			LOG.error(ex.toString(), ex);
		} catch (IOException ex) {
			LOG.error(ex.toString(), ex);
		}
		return null;
	}
	
	/**
	 * The main program.
	 * @param args no arguments at the moment
	 */
	public static void main(String[] args) {
		AdvanceFlowEngine afe = new AdvanceFlowEngine();
		afe.run();
	}
}
