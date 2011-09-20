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

import eu.advance.logistics.util.DistinguishedName;

/**
 * Request to generate a new key.
 * @author karnokd, 2011.09.20.
 */
public interface AdvanceGenerateKey extends AdvanceKeyStoreExport {
	/** @return the key algorithm. */
	String algorithm();
	/** @return the key bit size. */
	int keySize();
	/** @return the issuer's distinguished name. */
	DistinguishedName issuerDn();
	/** @return the subject's distinguished name. */
	DistinguishedName subjectDn();
	/**
	 * @param newAlgorithm the new algorithm
	 */
	void algorithm(String newAlgorithm);
	/**
	 * @param newKeySize the new key size
	 */
	void keySize(int newKeySize);
	/**
	 * @param newDn the new issuer distinguished name
	 */
	void issuerDn(DistinguishedName newDn);
	/**
	 * @param newDn the new subject distinguished name
	 */
	void subjectDn(DistinguishedName newDn);
}
