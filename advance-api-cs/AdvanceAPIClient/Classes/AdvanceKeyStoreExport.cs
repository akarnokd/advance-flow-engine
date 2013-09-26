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
using System.Globalization;
using System.Xml;

using AdvanceAPIClient.Core;
using AdvanceAPIClient.Interfaces;

namespace AdvanceAPIClient.Classes
{
    /// <summary>
    /// Request for exporting a certificate from a keystore
    /// </summary>
    public class AdvanceKeyStoreExport : XmlReadWrite, IPassword
    {
        /// <summary>
        /// key store name
        /// </summary>
        public string KeyStore;
        /// <summary>
        /// key alias
        /// </summary>
        public string KeyAlias;
        /// <summary>
        /// Key password
        /// An empty password should be an empty {@code char} array. To keep
        /// current password, use {@code null}.</p>
        /// </summary>
        public char[] Password
        {
            get { return this.keyPassword != null ? (char[])this.keyPassword.Clone() : null; }
            set { this.keyPassword = value != null ? (char[])value.Clone() : null; }
        }

        private char[] keyPassword;

        /// <summary>
        /// Fill object data from xml node
        /// </summary>
        /// <param name="source">Source Xml</param>
        protected override void LoadFromXmlNode(XmlNode source)
        {
            this.KeyStore = GetAttribute(source, "keystore", null);
            this.KeyAlias = GetAttribute(source, "keyalias", null);
            this.keyPassword = AdvanceCreateModifyInfo.GetPassword(source, "password");
        }

        /// <summary>
        /// Fills  object's xml
        /// </summary>
        /// <param name="node">Empty node for the object with name "tagname"</param>
        protected override void FillXmlElement(XmlElement node)
        {
            AddAttribute(node, "keystore", this.KeyStore);
            AddAttribute(node, "keyalias", this.KeyAlias);
            AdvanceCreateModifyInfo.SetPassword(node, "password", this.keyPassword);
        }

    }
}
