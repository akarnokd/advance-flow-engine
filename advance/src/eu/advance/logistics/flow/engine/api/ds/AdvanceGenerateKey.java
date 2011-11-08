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
import eu.advance.logistics.flow.engine.util.DistinguishedName;
import eu.advance.logistics.flow.engine.xml.typesystem.XElement;

/**
 * Request to generate a new key.
 * @author akarnokd, 2011.09.20.
 */
public class AdvanceGenerateKey extends AdvanceKeyStoreExport {
	/** The key algorithm. */
	public String algorithm;
	/** The key bit size. */
	public int keySize;
	/** The issuer's distinguished name. */
	public DistinguishedName issuerDn;
	/** The subject's distinguished name. */
	public DistinguishedName subjectDn;
	/** The domain name. */
	public String domain;
	/** The user who modifies the record. */
	public String modifiedBy;
	/** Creates a new instance of this class. */
	public static final Func0<AdvanceGenerateKey> CREATOR = new Func0<AdvanceGenerateKey>() {
		@Override
		public AdvanceGenerateKey invoke() {
			return new AdvanceGenerateKey();
		}
	};
	@Override
	public void load(XElement source) {
		algorithm = source.get("algorithm");
		keySize = source.getInt("keysize");
		issuerDn = new DistinguishedName(source.get("issuer-dn"));
		subjectDn = new DistinguishedName(source.get("subject-dn"));
		domain = source.get("domain");
		modifiedBy = source.get("modified-by");
		super.load(source);
	}
	@Override
	public void save(XElement destination) {
		destination.set("algorithm", algorithm, "keysize", keySize, 
				"issuer-dn", issuerDn, "subject-dn", subjectDn, "domain", domain, "modified-by", modifiedBy);
		super.save(destination);
	}
	
}
