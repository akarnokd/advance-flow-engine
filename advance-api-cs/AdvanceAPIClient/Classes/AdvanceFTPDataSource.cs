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
    public class AdvanceFTPDataSource : AdvanceCreateModifyInfo, IPassword, ICopiable<AdvanceFTPDataSource>, IIdentifiable<string>
    {
        /// <summary>
        /// name used by blocks to reference this data source
        /// </summary>
        public string Id { get { return this.Name; } }
        public string Name;
        /// <summary>
        /// Protocol type
        /// </summary>
        public AdvanceFTPProtocols Protocol;
        /// <summary>
        /// FTP address
        /// </summary>
        public string Address;
        /// <summary>
        /// Remote base directory
        /// </summary>
        public string RemoteDirectory;
        /// <summary>
        /// User name used for login
        /// </summary>
        public String UserOrKey;
        /// <summary>
        /// User's password.
        /// <remarks>
        /// Note that passwords are never returned from the 
        /// control API calls and are always null
        /// An empty password should be an char[0] array. To keep
        /// the current password, use null
        /// </remarks>
        /// </summary>
        public char[] Password
        {
            get { return (this.password == null) ? null : (char[])this.password.Clone(); }
            set { this.password = (value == null) ? null : (char[])value.Clone(); }
        }
        private char[] password;

        /// <summary>
        /// Connection should be passive?
        /// </summary>
        public bool Passive;
        /// <summary>
        /// Keystore used to trust the server
        /// </summary>
        public string KeyStore;
        /// <summary>
        /// Login type
        /// </summary>
        public AdvanceLoginType LoginType;

        public AdvanceFTPDataSource() : base() { }

        /// <summary>
        /// Fill object data from xml node
        /// </summary>
        /// <param name="source">Source Xml</param>
        protected override void LoadFromXmlNode(XmlNode source)
        {
            base.LoadFromXmlNode(source);
            this.Name = GetAttribute(source, "name");
            this.Protocol = GetEnumAttribute<AdvanceFTPProtocols>(source, "protocol", AdvanceFTPProtocols.FTP);
            this.Address = GetAttribute(source, "address", null);
            this.RemoteDirectory = GetAttribute(source, "remote-directory", null);
            this.UserOrKey = GetAttribute(source, "user-or-key", null);
            this.password = GetPassword(source, "password");
            this.Passive = GetBoolAttribute(source, "passive", false);
            this.KeyStore = GetAttribute(source, "keystore", null);
            this.LoginType = GetEnumAttribute<AdvanceLoginType>(source, "login-type", AdvanceLoginType.NONE);
        }

        /// <summary>
        /// Fills  object's xml
        /// </summary>
        /// <param name="node">Empty node for the object with name "tagname"</param>
        protected override void FillXmlElement(XmlElement node)
        {
            base.FillXmlElement(node);
            AddAttribute(node, "name", this.Name);
            AddAttribute(node, "protocol", this.Protocol);
            AddAttribute(node, "address", this.Address);
            AddAttribute(node, "remote-directory", this.RemoteDirectory);
            AddAttribute(node, "user-or-key", this.UserOrKey);
            AddBoolAttribute(node, "passive", this.Passive);
            AddAttribute(node, "keystore", this.KeyStore);
            AddAttribute(node, "login-type", this.LoginType);
            SetPassword(node, "password", password);
        }

        public AdvanceFTPDataSource Copy()
        {
            AdvanceFTPDataSource result = new AdvanceFTPDataSource();
            this.AssignTo(result);
            result.Name = this.Name;
            result.Protocol = this.Protocol;
            result.Address = this.Address;
            result.RemoteDirectory = this.RemoteDirectory;
            result.UserOrKey = this.UserOrKey;
            result.Passive = this.Passive;
            result.password = this.Password;
            result.KeyStore = this.KeyStore;
            result.LoginType = this.LoginType;
            return result;
        }

    }
}

