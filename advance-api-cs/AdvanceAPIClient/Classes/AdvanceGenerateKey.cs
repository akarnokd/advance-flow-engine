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
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Globalization;
using System.Xml;

using AdvanceAPIClient.Core;

namespace AdvanceAPIClient.Classes
{
    /// <summary>
    /// Request to generate a new key.
    /// </summary>
    public class AdvanceGenerateKey : AdvanceKeyStoreExport
    {
        /// <summary>
        /// Key algorithm
        ///</summary>
        public string Algorithm;
        /// <summary>
        /// key bit size
        /// </summary>
        public int KeySize;
        /// <summary>
        /// issuer's distinguished name
        /// </summary>
        public DistinguishedName IssuerDn;
        /// <summary>
        /// subject's distinguished name
        /// </summary>
        public DistinguishedName SubjectDn;
        /// <summary>
        /// domain name
        /// </summary>
        public string Domain;
        /// <summary>
        /// user who modifies the record
        /// </summary>
        public string ModifiedBy;

        /// <summary>
        /// Fill object data from xml node
        /// </summary>
        /// <param name="source">Source Xml</param>
        protected override void LoadFromXmlNode(XmlNode source)
        {
            this.Algorithm = GetAttribute(source, "algorithm", null);
            this.KeySize = GetIntAttribute(source, "keysize", 0);
            this.IssuerDn = new DistinguishedName(GetAttribute(source, "issuer-dn", null));
            this.SubjectDn = new DistinguishedName(GetAttribute(source, "subject-dn", null));
            this.Domain = GetAttribute(source, "domain", null);
            this.ModifiedBy = GetAttribute(source, "modified-by", null);
        }

        /// <summary>
        /// Fills  object's xml
        /// </summary>
        /// <param name="node">Empty node for the object with name "tagname"</param>
        protected override void FillXmlElement(XmlElement node)
        {
            AddAttribute(node, "algorithm", this.Algorithm);
            AddAttribute(node, "keysize", this.KeySize);
            AddAttribute(node, "issuer-dn", this.IssuerDn);
            AddAttribute(node, "subject-dn", this.SubjectDn);
            AddAttribute(node, "domain", this.Domain);
            AddAttribute(node, "modified-by", this.ModifiedBy);
        }

    }

}
