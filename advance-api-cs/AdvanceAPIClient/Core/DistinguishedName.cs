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
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

namespace AdvanceAPIClient.Core
{

    /// <summary>
    /// Builds or splits a distinguished name specification into components.
    /// The class is immutable.
    /// The meaning of DN elements:<p>
    ///	CN=commonName<br>
    ///    OU=organizationUnit<br>
    ///    O=organizationName<br>
    ///     L=localityName<br>
    ///    ST=stateName<br>
    ///     C=country<br>
    /// <br>
    /// The capitalization does not matter but the order does.
    /// Some elements can be omitted from the list
    /// </summary>
    public class DistinguishedName
    {

        /// <summary>
        /// Common name (CN). Can be null denoting a non present item.
        /// </summary>
        public string CommonName { get { return this.commonName; } }
        private string commonName;

        /// <summary>
        /// Organization unit (OU). Can be null denoting a non present item.
        /// </summary>
        public string OrganizationUnit { get { return this.organizationUnit; } }
        private String organizationUnit;
        /// <summary>
        /// Organization name (O). Can be null denoting a non present item.
        /// </summary>
        public string OrganizationName { get { return this.organizationName; } }
        private String organizationName;
        /// <summary>
        /// Locality (city name) (L). Can be null denoting a non present item.
        /// </summary>
        public string LocalityName { get { return this.localityName; } }
        private String localityName;
        /// <summary>
        /// Name of the state or province (ST). Can be null denoting a non present item.
        /// </summary>
        public string StateName { get { return this.stateName; } }
        private String stateName;
        /// <summary>
        /// Two character country code (C). Can be null denoting a non present item
        /// </summary>
        public string Country { get { return this.country; } }
        private String country;
        /// <summary>
        /// On demand calculated string representation.
        /// <summary>
        private String description;
        /// <summary>
        /// On demand calculated hash code.
        /// <summary>
        private int hash;

        /// <summary>
        /// Constructor. Initializes the private fields from the parameters.
        /// </summary>
        /// <param name="commonName">common name (CN), can be null</param>
        /// <param name="organizationUnit">Organization unit (OU), can be null</param>
        /// <param name="organizationName">Organization name (O), can be null</param>
        /// <param name="localityName">city name (L), can be null</param>
        /// <param name="stateName">state name (ST), can be null</param>
        /// <param name="country">two character country code (C), can be null</param>
        /// <exception cref="ArgumentException">when country code is not two characters</exception>
        public DistinguishedName(String commonName, String organizationUnit,
                String organizationName, String localityName, String stateName,
                String country)
        {
            this.commonName = commonName;
            this.organizationUnit = organizationUnit;
            this.organizationName = organizationName;
            this.localityName = localityName;
            this.stateName = stateName;
            if (country != null && country.Length > 0 && country.Trim().Length != 2)
                throw new ArgumentException("The country parameter is not two characters long.");
            this.country = country;
            CreateDescription();
        }

        /// <summary>
        /// Constructor. Parses given string and sets private fields.
        /// </summary>
        /// <param name="dn">distinguished name string.</param>
        public DistinguishedName(String dn)
        {
            if (dn != null)
            {
                while (true)
                {
                    int idx = dn.IndexOf('=');
                    if (idx < 0)
                    {
                        break;
                    }
                    String name = dn.Substring(0, idx).Trim();
                    dn = dn.Substring(idx + 1);
                    int sidx = 0;
                    while (true)
                    {
                        idx = dn.IndexOf(',', sidx);
                        if (idx < 0)
                        {
                            break;
                        }
                        if (idx > 0 && dn[idx - 1] != '\\')
                        {
                            break;
                        }
                        sidx = idx + 1;
                    }

                    String value = idx < 0 ? dn : dn.Substring(0, idx);
                    String uname = name.ToUpper();

                    if ("CN".Equals(uname))
                        commonName = Unescape(value);
                    else if ("OU".Equals(uname))
                        organizationUnit = Unescape(value);
                    else if ("O".Equals(uname))
                        organizationName = Unescape(value);
                    else if ("L".Equals(uname))
                        localityName = Unescape(value);
                    else if ("ST".Equals(uname))
                        stateName = Unescape(value);
                    else if ("C".Equals(uname))
                    {
                        value = this.Unescape(value);
                        if (value.Length != 2)
                            throw new ArgumentException("The given country code is not two characters long.");
                        country = value;
                    }
                    dn = dn.Substring(idx + 1);
                }
            }
            this.CreateDescription();
        }

        /// <summary>
        /// Create description and hash code.
        /// </summary>
        private void CreateDescription()
        {
            StringBuilder b = new StringBuilder();
            this.EscapeField(b, "CN", commonName);
            this.EscapeField(b, "OU", organizationUnit);
            this.EscapeField(b, "O", organizationName);
            this.EscapeField(b, "L", localityName);
            this.EscapeField(b, "ST", stateName);
            this.EscapeField(b, "C", country);
            description = b.ToString();
            hash = description.GetHashCode();
        }

        public override string ToString()
        {
            return this.description;
        }

        public override int GetHashCode()
        {
            if (this.hash == 0)
                this.hash = ToString().GetHashCode();
            return this.hash;
        }

        public override bool Equals(Object obj)
        {
            return this.ToString().Equals(obj.ToString());
        }

        /// <summary>
        ///  Escape any comma in the given string and add it to the {@code StringBuilder}.
        ///  Automatically adds separator comma if the builder is not empty.
        /// </summary>
        /// <param name="b">Target StringBuilder</param>
        /// <param name="name">Field name</param>
        /// <param name="s">String to escape</param>
        private void EscapeField(StringBuilder b, String name, String s)
        {
            if (s != null && s.Length > 0)
            {
                if (b.Length > 0)
                    b.Append(',');
                b.Append(name).Append('=');
                for (int i = 0; i < s.Length; i++)
                {
                    if (s[i] == ',' || s[i] == '\\')
                        b.Append('\\');
                    b.Append(s[i]);
                }
            }
        }

        /// <summary>
        /// Unescape backslashes from string
        /// </summary>
        /// <param name="s">string</param>
        /// <returns>unescaped string</returns>
        private String Unescape(String s)
        {
            if (s != null)
            {
                StringBuilder b = new StringBuilder();
                char last = '\0';
                for (int i = 0; i < s.Length; i++)
                {
                    if (s[i] != '\\')
                        b.Append(s[i]);
                    else if (last == '\\')
                        b.Append(s[i]);
                    last = s[i];
                }
                return b.ToString();
            }
            else
                return null;
        }
    }
}
