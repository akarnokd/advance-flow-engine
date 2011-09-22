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

package eu.advance.logistics.flow.engine.util;

/**
 * Builds or splits a distinguished name specification into components.
 * The class is immutable.
 * The meaning of DN elements:<p>
 * 	CN=commonName<br>
 *  OU=organizationUnit<br>
 *  O=organizationName<br>
 *  L=localityName<br>
 *  ST=stateName<br>
 *  C=country<br>
 * <br>
 * The capitalization does not matter but the order does.
 * Some elements can be ommited from the list
 * @author karnokd, 2007.12.06.
 * @version $Revision 1.0$
 */
public class DistinguishedName {
	/**
	 * The common name (CN). Can be null denoting a non present item.
	 */
	private String commonName;
	/**
	 * The organization unit (OU). Can be null denoting a non present item.
	 */
	private String organizationUnit;
	/**
	 * The organization name (O). Can be null denoting a non present item.
	 */
	private String organizationName;
	/**
	 * The locality (city name) (L). Can be null denoting a non present item.
	 */
	private String localityName;
	/**
	 * Name of the state or province (ST). Can be null denoting a non present item.
	 */
	private String stateName;
	/**
	 * The two character country code (C). Can be null denoting a non present item
	 */
	private String country;
	/**
	 * The on demand calculated string representation.
	 */
	private String description;
	/**
	 * The on demand calculated hash code.
	 */
	private int hash;
	/**
	 * Constructor. Initializes the private fields from the parameters.
	 * Throws IllegalArgumentException when the country code is not two characters when given
	 * @param commonName the common name (CN), can be null
	 * @param organizationUnit the organization unit (OU), can be null
	 * @param organizationName the organization name (O), can be null
	 * @param localityName the city name (L), can be null
	 * @param stateName the state name (ST), can be null
	 * @param country the two character country code (C), can be null
	 */
	public DistinguishedName(String commonName, String organizationUnit,
			String organizationName, String localityName, String stateName,
			String country) {
		this.commonName = commonName;
		this.organizationUnit = organizationUnit;
		this.organizationName = organizationName;
		this.localityName = localityName;
		this.stateName = stateName;
		if (country != null && country.length() > 0 && country.trim().length() != 2) {
			throw new IllegalArgumentException("The country parameter is not two characters long.");
		}
		this.country = country;
		createDescription();
	}
	/**
	 * Constructor. Parses the given string and sets the private fields.
	 * @param dn the distinguished name string.
	 */
	public DistinguishedName(String dn) {
		if (dn != null) {
			while (true) {
				int idx = dn.indexOf('=');
				if (idx < 0) {
					break;
				}
				String name = dn.substring(0, idx).trim();
				dn = dn.substring(idx + 1);
				int sidx = 0;
				while (true) {
					idx = dn.indexOf(',', sidx);
					if (idx < 0) {
						break;
					}
					if (idx > 0 && dn.charAt(idx - 1) != '\\') {
						break;
					}
					sidx = idx + 1;
				}
				
				String value = idx < 0 ? dn : dn.substring(0, idx);
				
				if ("CN".equalsIgnoreCase(name)) {
					commonName = unescape(value);
				} else
				if ("OU".equalsIgnoreCase(name)) {
					organizationUnit = unescape(value);
				} else
				if ("O".equalsIgnoreCase(name)) {
					organizationName = unescape(value);
				} else
				if ("L".equalsIgnoreCase(name)) {
					localityName = unescape(value);
				} else
				if ("ST".equalsIgnoreCase(name)) {
					stateName = unescape(value);
				} else
				if ("C".equalsIgnoreCase(name)) {
					value = unescape(value);
					if (value.length() != 2) {
						throw new IllegalArgumentException("The given country code is not two characters long.");
					}
					country = value;
				}
				dn = dn.substring(idx + 1);
			}
		}
		createDescription();
	}
	/**
	 * Create the description and hash code.
	 */
	private void createDescription() {
		StringBuilder b = new StringBuilder();
		escapeField(b, "CN", commonName);
		escapeField(b, "OU", organizationUnit);
		escapeField(b, "O", organizationName);
		escapeField(b, "L", localityName);
		escapeField(b, "ST", stateName);
		escapeField(b, "C", country);
		description = b.toString();
		hash = description.hashCode();
	}
	/**
	 * @return the commonName (CN)
	 */
	public String getCommonName() {
		return commonName;
	}
	/**
	 * @return the country (C)
	 */
	public String getCountry() {
		return country;
	}
	/**
	 * @return the localityName (L)
	 */
	public String getLocalityName() {
		return localityName;
	}
	/**
	 * @return the organizationName (O)
	 */
	public String getOrganizationName() {
		return organizationName;
	}
	/**
	 * @return the organizationUnit (OU)
	 */
	public String getOrganizationUnit() {
		return organizationUnit;
	}
	/**
	 * @return the stateName (S)
	 */
	public String getStateName() {
		return stateName;
	}
	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		return description;
	}
	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		if (hash == 0) {
			hash = toString().hashCode();
		}
		return hash;
	}
	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean equals(Object obj) {
		return obj instanceof DistinguishedName && toString().equals(obj.toString());
	}
	/**
	 * Escape any comma in the given string and add it to the stringbuilder.
	 * Automatically adds separator comma if the builder is not empty.
	 * @param b the target StringBuilder, cannot be null
	 * @param name the field name, cannot be null
	 * @param s the String to escape, can be null
	 */
	private void escapeField(StringBuilder b, String name, String s) {
		if (s != null && s.length() > 0) {
			if (b.length() > 0) {
				b.append(',');
			}
			b.append(name).append('=');
			for (int i = 0; i < s.length(); i++) {
				char c = s.charAt(i);
				if (c == ',' || c == '\\') {
					b.append('\\');
				}
				b.append(c);
			}
		}
	}
	/**
	 * Unescape the backslashes from the given string.
	 * @param s the string, can be null
	 * @return the unescaped string, null if s was null
	 */
	private String unescape(String s) {
		if (s != null) {
			StringBuilder b = new StringBuilder();
			char last = '\0';
			for (int i = 0; i < s.length(); i++) {
				char c = s.charAt(i);
				if (c != '\\') {
					b.append(c);
				} else
				if (last == '\\') {
					b.append(c);
				}
				last = c;
			}
			return b.toString();
		}
		return null;
	}
}