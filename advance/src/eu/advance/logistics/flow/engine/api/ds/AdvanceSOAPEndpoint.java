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

package eu.advance.logistics.flow.engine.api.ds;

import hu.akarnokd.reactive4java.base.Func0;
import hu.akarnokd.utils.xml.XNElement;
import hu.akarnokd.utils.xml.XNSerializable;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import org.slf4j.LoggerFactory;

import eu.advance.logistics.flow.engine.api.core.Copyable;
import eu.advance.logistics.flow.engine.api.core.HasPassword;
import eu.advance.logistics.flow.engine.api.core.Identifiable;

/**
 * Describes a SOAP communication channel.
 * @author akarnokd, 2011.09.20.
 */
public class AdvanceSOAPEndpoint extends AdvanceCreateModifyInfo 
implements XNSerializable, HasPassword, Copyable<AdvanceSOAPEndpoint>, Identifiable<String> {
	/** The name used to reference this channel from blocks. */
	public String name;
	/** The endpoint URL. */
	public URL endpoint;
	/** The target object URI. */
	public URI targetObject;
	/** The target namespace. */
	public URI targetNamespace;
	/** The remote method. */
	public String method;
	/** The communication should be encrypted. */
	public boolean encrypted;
	/** The keystore for the encryption key. */
	public String keyStore;
	/** The key alias for the encryption. */
	public String keyAlias;
	/**
	 * The password.
	 * <p>Note that passwords are never returned from the 
	 * control API calls and are always {@code null}.</p> 
	 * <p>An empty password should be an empty {@code char} array. To keep
	 * the current password, use {@code null}.</p>
	 * @return  
	 */
	private char[] password;
	/**
	 * Create a new instance of the class.
	 */
	public static final Func0<AdvanceSOAPEndpoint> CREATOR = new Func0<AdvanceSOAPEndpoint>() {
		@Override
		public AdvanceSOAPEndpoint invoke() {
			return new AdvanceSOAPEndpoint();
		}
	};
	@Override
	public void load(XNElement source) {
		name = source.get("name");
		try {
			endpoint = new URL(source.get("endpoint"));
		} catch (MalformedURLException ex) {
			LoggerFactory.getLogger(AdvanceSOAPEndpoint.class).error(ex.toString(), ex);
		}
		try {
			targetObject = new URI(source.get("target-object"));
		} catch (URISyntaxException ex) {
			LoggerFactory.getLogger(AdvanceSOAPEndpoint.class).error(ex.toString(), ex);
		}
		try {
			targetNamespace = new URI(source.get("target-namespace"));
		} catch (URISyntaxException ex) {
			LoggerFactory.getLogger(AdvanceSOAPEndpoint.class).error(ex.toString(), ex);
		}
		method = source.get("method");
		encrypted = source.getBoolean("encrypted");
		keyStore = source.get("keystore");
		keyAlias = source.get("keyalias");
		password = getPassword(source, "password");
		super.load(source);
	}
	@Override
	public void save(XNElement destination) {
		destination.set("name", name);
		destination.set("endpoint", endpoint);
		destination.set("target-object", targetObject);
		destination.set("target-namespace", targetNamespace);
		destination.set("method", method);
		destination.set("encrypted", encrypted);
		destination.set("keystore", keyStore);
		destination.set("keyalias", keyAlias);
		setPassword(destination, "password", password);
		super.save(destination);
	}
	@Override
	public AdvanceSOAPEndpoint copy() {
		AdvanceSOAPEndpoint result = new AdvanceSOAPEndpoint();
		result.name = name;
		result.endpoint = endpoint;
		result.targetObject = targetObject;
		result.method = method;
		result.targetNamespace = targetNamespace;
		result.encrypted = encrypted;
		result.keyStore = keyStore;
		result.keyAlias = keyAlias;
		result.password = password != null ? password.clone() : null;
		assignTo(result);
		return result;
	}
	@Override
	public char[] password() {
		return password != null ? password.clone() : null;
	}
	@Override
	public void password(char[] newPassword) {
		password = newPassword != null ? newPassword.clone() : null;
	}
	@Override
	public String id() {
		return name;
	}
}
