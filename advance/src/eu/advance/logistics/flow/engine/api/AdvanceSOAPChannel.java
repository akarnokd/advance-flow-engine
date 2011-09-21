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

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import eu.advance.logistics.flow.model.XSerializable;
import eu.advance.logistics.xml.typesystem.XElement;

/**
 * Describes a SOAP communication channel.
 * @author karnokd, 2011.09.20.
 */
public class AdvanceSOAPChannel extends AdvanceCreateModifyInfo implements XSerializable {
	/** The unique channel identifier. */
	public int id = Integer.MIN_VALUE;
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
	@Override
	public void load(XElement source) {
		id = source.getInt("id");
		name = source.get("name");
		try {
			endpoint = new URL(source.get("endpoint"));
		} catch (MalformedURLException ex) {
			// FIXME ignored
		}
		try {
			targetObject = new URI(source.get("target-object"));
		} catch (URISyntaxException ex) {
			// FIXME ignored
		}
		method = source.get("method");
		encrypted = "true".equals(source.get("encrypted"));
		keyStore = source.get("keystore");
		keyAlias = source.get("keyalias");
		super.load(source);
	}
	@Override
	public void save(XElement destination) {
		destination.set("id", id);
		destination.set("name", name);
		destination.set("endpoint", endpoint);
		destination.set("target-object", targetObject);
		destination.set("method", method);
		destination.set("encrypted", encrypted);
		destination.set("keystore", keyStore);
		destination.set("keyalias", keyAlias);
		super.save(destination);
	}
}
