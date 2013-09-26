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
    /// Represents the connection information to a POP3(s)/IMAP(s) based email-box to 
    /// </summary>
    public class AdvanceSOAPEndpoint : AdvanceCreateModifyInfo, IPassword, ICopiable<AdvanceSOAPEndpoint>, IIdentifiable<string>
    {
        /// <summary>
        /// Identifier for referencing this box
        /// </summary>
        public string Id { get { return this.Name; } }
        public string Name;
        /// <summary>
        /// user password or key password for authentication
        /// </summary>
        public char[] Password
        {
            get { return (this.password == null) ? null : (char[])this.password.Clone(); }
            set { this.password = (value == null) ? null : (char[])value.Clone(); }
        }
        private char[] password;

        /// <summary>
        /// endpoint URL
        /// </summary>
        public Uri Endpoint;
        /// <summary>
        /// target object URI
        /// </summary>
        public Uri TargetObject;
        /// <summary>
        /// target namespace
        /// </summary>
        public Uri TargetNamespace;
        /// <summary>
        /// remote method
        /// </summary>
        public string Method;
        /// <summary>
        /// If true, communication should be encrypted
        /// </summary>
        public bool IsEncrypted;
        /// <summary>
        /// Keystore for the encryption key
        /// </summary>
        public string KeyStore;
        /// <summary>
        /// Key alias for the encryption
        /// </summary>
        public string KeyAlias;

        public AdvanceSOAPEndpoint() : base() { }

        /// <summary>
        /// Fill object data from xml node
        /// </summary>
        /// <param name="source">Source Xml</param>
        protected override void LoadFromXmlNode(XmlNode source)
        {
            base.LoadFromXmlNode(source);
            base.LoadFromXmlNode(source);
            this.Name = GetAttribute(source, "name");
            this.password = GetPassword(source, "password");
            this.Endpoint = GetUriAttribute(source, "endpoint");
            this.TargetObject = GetUriAttribute(source, "target-object");
            this.TargetNamespace = GetUriAttribute(source, "target-namespace");
            this.Method = GetAttribute(source, "method", null);
            this.IsEncrypted = GetBoolAttribute(source, "encrypted", false);
            this.KeyStore = GetAttribute(source, "keystore");
            this.KeyAlias = GetAttribute(source, "keyalias", null);
        }

        /// <summary>
        /// Fills  object's xml
        /// </summary>
        /// <param name="node">Empty node for the object with name "tagname"</param>
        protected override void FillXmlElement(XmlElement node)
        {
            base.FillXmlElement(node);
            AddAttribute(node, "name", this.Name);
            SetPassword(node, "password", this.password);
            AddAttribute(node, "endpoint", this.Endpoint);
            AddAttribute(node, "target-object", this.TargetObject);
            AddAttribute(node, "target-namespace", this.TargetNamespace);
            AddAttribute(node, "method", this.Method);
            AddBoolAttribute(node, "encrypted", this.IsEncrypted);
            AddAttribute(node, "keystore", this.KeyStore);
            AddAttribute(node, "keyalias", this.KeyAlias);
        }

        public AdvanceSOAPEndpoint Copy()
        {
            AdvanceSOAPEndpoint result = new AdvanceSOAPEndpoint();
            this.AssignTo(result);
            result.Name = this.Name;
            result.password = this.Password;
            result.Endpoint = this.Endpoint;
            result.TargetObject = this.TargetObject;
            result.TargetNamespace = this.TargetNamespace;
            result.Method = this.Method;
            result.IsEncrypted = this.IsEncrypted;
            result.KeyStore = this.KeyStore;
            result.KeyAlias = this.KeyAlias;
            return result;

        }
    }
}
