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

import java.net.URI;
import java.net.URL;

/**
 * Describes a SOAP communication channel.
 * @author karnokd, 2011.09.20.
 */
public class AdvanceSOAPChannel extends AdvanceCreateModifyInfo {
	/** The unique channel identifier. */
	public int id;
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
}
