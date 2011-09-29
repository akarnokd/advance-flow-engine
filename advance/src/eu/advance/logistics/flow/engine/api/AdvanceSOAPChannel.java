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

package eu.advance.logistics.flow.engine.api;

import hu.akarnokd.reactive4java.base.Func0;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import org.slf4j.LoggerFactory;

import eu.advance.logistics.flow.engine.model.XSerializable;
import eu.advance.logistics.flow.engine.xml.typesystem.XElement;

/**
 * Describes a SOAP communication channel.
 * @author karnokd, 2011.09.20.
 */
public class AdvanceSOAPChannel extends AdvanceCreateModifyInfo implements XSerializable {
	/** The name used to reference this channel from blocks. */
	public String name;
	/** The endpoint URL. */
	public URL endpoint;
	/** The target object URI. */
	public URI targetObject;
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
	public char[] password;
	/**
	 * Create a new instance of the class.
	 */
	public static final Func0<AdvanceSOAPChannel> CREATOR = new Func0<AdvanceSOAPChannel>() {
		@Override
		public AdvanceSOAPChannel invoke() {
			return new AdvanceSOAPChannel();
		}
	};
	@Override
	public void load(XElement source) {
		name = source.get("name");
		try {
			endpoint = new URL(source.get("endpoint"));
		} catch (MalformedURLException ex) {
			LoggerFactory.getLogger(AdvanceSOAPChannel.class).error(ex.toString(), ex);
		}
		try {
			targetObject = new URI(source.get("target-object"));
		} catch (URISyntaxException ex) {
			LoggerFactory.getLogger(AdvanceSOAPChannel.class).error(ex.toString(), ex);
		}
		method = source.get("method");
		encrypted = source.getBoolean("encrypted");
		keyStore = source.get("keystore");
		keyAlias = source.get("keyalias");
		password = getPassword(source, "password");
		super.load(source);
	}
	@Override
	public void save(XElement destination) {
		destination.set("name", name);
		destination.set("endpoint", endpoint);
		destination.set("target-object", targetObject);
		destination.set("method", method);
		destination.set("encrypted", encrypted);
		destination.set("keystore", keyStore);
		destination.set("keyalias", keyAlias);
		setPassword(destination, "password", password);
		super.save(destination);
	}
	/** @return create a copy of this object without the password. */
	public AdvanceSOAPChannel copy() {
		AdvanceSOAPChannel result = new AdvanceSOAPChannel();
		result.name = name;
		result.endpoint = endpoint;
		result.targetObject = targetObject;
		result.method = method;
		result.encrypted = encrypted;
		result.keyStore = keyStore;
		result.keyAlias = keyAlias;
		assignTo(result);
		return result;
	}
}
