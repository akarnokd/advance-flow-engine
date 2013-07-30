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

import hu.akarnokd.utils.crypto.KeystoreFault;
import hu.akarnokd.utils.crypto.KeystoreManager;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateExpiredException;
import java.security.cert.CertificateNotYetValidException;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPSClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;
import com.sshtools.j2ssh.SftpClient;
import com.sshtools.j2ssh.SshClient;
import com.sshtools.j2ssh.authentication.AuthenticationProtocolState;
import com.sshtools.j2ssh.authentication.PasswordAuthenticationClient;
import com.sshtools.j2ssh.sftp.SftpFile;
import com.sshtools.j2ssh.transport.HostKeyVerification;
import com.sshtools.j2ssh.transport.TransportProtocolException;
import com.sshtools.j2ssh.transport.publickey.SshPublicKey;

import edu.umd.cs.findbugs.annotations.NonNull;
import eu.advance.logistics.flow.engine.api.core.AdvanceControlException;
import eu.advance.logistics.flow.engine.api.core.PoolManager;
import eu.advance.logistics.flow.engine.api.ds.AdvanceDataStore;
import eu.advance.logistics.flow.engine.api.ds.AdvanceFTPDataSource;
import eu.advance.logistics.flow.engine.api.ds.AdvanceFTPProtocols;
import eu.advance.logistics.flow.engine.api.ds.AdvanceKeyStore;

/**
 * Manages FTP connection objects.
 * @author akarnokd, 2011.10.05.
 */
public class FTPPoolManager implements PoolManager<FTPConnection> {
	/** The logger. */
	protected static final Logger LOG = LoggerFactory.getLogger(JDBCPoolManager.class);
	/** The data source settings. */
	protected final AdvanceFTPDataSource ds;
	/** The datastore used to find the keystore for trusting an secure site. */
	protected final AdvanceDataStore datastore;
	/**
	 * Initializes the pool with the supplied data source.
	 * @param ds the data source
	 * @param datastore the data store
	 */
	public FTPPoolManager(AdvanceFTPDataSource ds, AdvanceDataStore datastore) {
		this.ds = ds;
		this.datastore = datastore;
	}
	@Override
	public FTPConnection create() throws Exception {
		switch (ds.protocol) {
		case FTP:
			return createRawFTP(ds);
		case FTPS:
			return createFTPS(ds, datastore);
		case SFTP:
			return createSFTP(ds, datastore);
		default:
			throw new IllegalArgumentException("Unsupported protocol " + ds.protocol);
		}
	}

	@Override
	public boolean verify(FTPConnection obj) throws Exception {
		try {
			obj.currentDir();
			obj.list();
			return true;
		} catch (IOException ex) {
			return false;
		}
	}

	@Override
	public void close(FTPConnection obj) throws Exception {
		obj.close();
	}
	/**
	 * Test the given data source for accessibility.
	 * @param ds the data source
	 * @param datastore the data store for managing trust
	 * @return the error message or an empty string if success
	 */
	public static String test(@NonNull AdvanceFTPDataSource ds, @NonNull AdvanceDataStore datastore) {
		FTPPoolManager pm = new FTPPoolManager(ds, datastore);
		try {
			FTPConnection conn = pm.create();
			try {
				if (pm.verify(conn)) {
					return "";
				}
				return "Failed to verify datasource due unknown reasons";
			} finally {
				pm.close(conn);
			}
		} catch (Exception ex) {
			return "";
		}
	}
	/**
	 * Create a raw FTP connection.
	 * @param ds the connection info
	 * @return the ftp connection
	 * @throws IOException if a network error occurs
	 */
	public static FTPConnection createRawFTP(AdvanceFTPDataSource ds) throws IOException {
		return initFTPClient(ds, new FTPClient());
	}
	/**
	 * Initialize the FTP(s) connection via login and settings and return a connection manager object.
	 * @param ds the data source settings
	 * @param client the FTP client representing the active connection.
	 * @return the ftp connection manager
	 * @throws IOException if a network or command error occurs
	 */
	private static FTPConnection initFTPClient(AdvanceFTPDataSource ds,
			final FTPClient client) throws IOException {
		int idx = ds.address.lastIndexOf(':');
		if (idx > 0) {
			client.connect(ds.address.substring(0, idx), Integer.parseInt(ds.address.substring(idx + 1)));
		} else {
			client.connect(ds.address);
		}
		if (ds.passive) {
			client.enterLocalPassiveMode();
		}
		if (!client.login(ds.userOrKey, new String(ds.password()))) {
			throw new IOException("Login failed with user " + ds.userOrKey);
		}
		if (!client.changeWorkingDirectory(ds.remoteDirectory)) {
			throw new IOException("Could not change directory to " + ds.remoteDirectory);
		}
		if (!client.setFileType(FTP.BINARY_FILE_TYPE)) {
			throw new IOException("Could not set the transfer mode to BINARY");
		}
		return new FTPConnection() {
			@Override
			public void close() throws IOException {
				try {
					if (client.logout()) {
						LOG.warn("Logout failed");
					}
				} finally {
					client.disconnect();
				}
			}
			@Override
			public void rename(String file, String newName) throws IOException {
				client.rename(file, newName);
			}
			@Override
			public AdvanceFTPProtocols protocol() {
				return client instanceof FTPSClient ? AdvanceFTPProtocols.FTPS : AdvanceFTPProtocols.FTP;
			}

			@Override
			public String currentDir() throws IOException {
				return client.printWorkingDirectory();
			}

			@Override
			public void changeDir(String newDir) throws IOException {
				if (!client.changeWorkingDirectory(newDir)) {
					throw new IOException("Couldn't change remote directory to " + newDir);
				}
			}

			@Override
			public byte[] retrieve(String file) throws IOException {
				ByteArrayOutputStream bout = new ByteArrayOutputStream();
				if (!client.retrieveFile(file, bout)) {
					throw new IOException("Retrieve failed on file " + file);
				}
				return bout.toByteArray();
			}

			@Override
			public void send(String file, byte[] data) throws IOException {
				if (client.storeFile(file, new ByteArrayInputStream(data))) {
					throw new IOException("Store failed on file " + file);
				}
			}

			@Override
			public List<FileInfo> list() throws IOException {
				List<FileInfo> result = Lists.newArrayList();
				for (FTPFile f : client.listFiles()) {
					if (f != null) {
						FileInfo fi = new FileInfo();
						fi.name = f.getName();
						fi.length = f.getSize();
						fi.isDirectory = f.isDirectory();
						fi.time = f.getTimestamp().getTime();
						result.add(fi);
					}
				}
				return result;
			}
			
		};
	}
	/**
	 * Create a FTPS (secure FTP) connection.
	 * @param ds the connection info
	 * @param datastore the data store
	 * @return the ftp connection
	 * @throws IOException if a network error occurs
	 */
	public static FTPConnection createFTPS(AdvanceFTPDataSource ds, AdvanceDataStore datastore) throws IOException {
		
		try {
			SSLContext context = SSLContext.getInstance("TLS");
			
			TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509");
			AdvanceKeyStore aks = datastore.queryKeyStore(ds.keyStore);
			KeystoreManager mgr = new KeystoreManager();
			mgr.load(aks.location, aks.password());
			tmf.init(mgr.getKeyStore());
			
			context.init(null, tmf.getTrustManagers(), null);
	
			FTPSClient client = new FTPSClient(context);
			
			return initFTPClient(ds, client);
			
		} catch (KeystoreFault ex) {
			LOG.error(ex.toString(), ex);
			throw new IOException(ex);
		} catch (KeyStoreException ex) {
			LOG.error(ex.toString(), ex);
			throw new IOException(ex);
		} catch (KeyManagementException ex) {
			LOG.error(ex.toString(), ex);
			throw new IOException(ex);
		} catch (NoSuchAlgorithmException ex) {
			LOG.error(ex.toString(), ex);
			throw new IOException(ex);
		} catch (AdvanceControlException ex) {
			LOG.error(ex.toString(), ex);
			throw new IOException(ex);
		}
	}
	/**
	 * Create a FTP over SSH connection.
	 * @param ds the connection info
	 * @param datastore the datastore to test the host key.
	 * @return the ftp connection
	 * @throws IOException if a network error occurs
	 */
	public static FTPConnection createSFTP(final AdvanceFTPDataSource ds, final AdvanceDataStore datastore) throws IOException {
		final SshClient ssh = new SshClient();
		int idx = ds.address.lastIndexOf(':');
		
		HostKeyVerification verify = new HostKeyVerification() {
			@Override
			public boolean verifyHost(String host, SshPublicKey pk)
					throws TransportProtocolException {
				try {
					AdvanceKeyStore aks = datastore.queryKeyStore(ds.keyStore);
					KeystoreManager mgr = new KeystoreManager();
					mgr.load(aks.location, aks.password());
					
					X509Certificate certificate = mgr.getCertificate(host);
					try {
						// FIXME SFTP: maybe not enough for verification
						if (certificate != null) {
							certificate.checkValidity();
							byte[] sshKey = pk.getEncoded();
							byte[] certKey = certificate.getPublicKey().getEncoded();
							if (Arrays.equals(sshKey, certKey)) {
								return true;
							}
						}
					} catch (CertificateNotYetValidException ex) {
						LOG.error(ex.toString(), ex);
					} catch (CertificateExpiredException ex) {
						LOG.error(ex.toString(), ex);
					}
				} catch (KeystoreFault ex) {
					LOG.error(ex.toString(), ex);
				} catch (AdvanceControlException ex) {
					LOG.error(ex.toString(), ex);
				} catch (IOException ex) {
					LOG.error(ex.toString(), ex);
				}
				return false;
			}
		};
		
		if (idx > 0) {
			ssh.connect(ds.address.substring(0, idx), Integer.parseInt(ds.address.substring(idx + 1)), verify);
		} else {
			ssh.connect(ds.address, verify);
		}
		
		PasswordAuthenticationClient auth = new PasswordAuthenticationClient();
		auth.setUsername(ds.userOrKey);
		auth.setPassword(new String(ds.password()));
		
		if (ssh.authenticate(auth) != AuthenticationProtocolState.COMPLETE) {
			ssh.disconnect();
			throw new IOException("Authentication failed for user " + ds.name);
		}
		
		final SftpClient sftp = ssh.openSftpClient();
		
		return new FTPConnection() {
			@Override
			public void close() throws IOException {
				IOException deferred = null;
				try {
					sftp.quit();
				} catch (IOException ex) {
					LOG.error(ex.toString(), ex);
					deferred = ex;
				}
				ssh.disconnect();
				if (deferred != null) {
					throw deferred;
				}
			}
			@Override
			public void rename(String file, String newName) throws IOException {
				sftp.rename(file, newName);
			}
			@Override
			public AdvanceFTPProtocols protocol() {
				return AdvanceFTPProtocols.SFTP;
			}

			@Override
			public String currentDir() throws IOException {
				return sftp.pwd();
			}

			@Override
			public void changeDir(String newDir) throws IOException {
				sftp.cd(newDir);
			}

			@Override
			public byte[] retrieve(String file) throws IOException {
				ByteArrayOutputStream bout = new ByteArrayOutputStream();
				sftp.get(file, bout);
				return bout.toByteArray();
			}

			@Override
			public void send(String file, byte[] data) throws IOException {
				sftp.put(new ByteArrayInputStream(data), file);
			}

			@Override
			public List<FileInfo> list() throws IOException {
				List<FileInfo> result = Lists.newArrayList();
				List<?> list = sftp.ls();
				for (Object o : list) {
					SftpFile f = (SftpFile)o;
					if (f.isDirectory() || f.isFile()) {
						FileInfo fi = new FileInfo();
						fi.name = f.getFilename();
						fi.isDirectory = f.isDirectory();
						fi.length = f.getAttributes().getSize().longValue();
						fi.time = new Date(f.getAttributes().getModifiedTime().longValue() * 1000);
						result.add(fi);
					}
				}
				return result;
			}
			
		};
	}

}
