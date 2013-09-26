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
    public class AdvanceEmailBox : AdvanceCreateModifyInfo, IPassword, ICopiable<AdvanceEmailBox>, IIdentifiable<string>
    {
        /// <summary>
        /// Identifier for referencing this box
        /// </summary>
        public string Id { get { return this.Name; } }
        public string Name;
        /// <summary>
        /// Receive protocol
        /// </summary>
        public AdvanceEmailReceiveProtocols ReceiveProtocol;
        /// <summary>
        /// Send protocol
        /// </summary>
        public AdvanceEmailSendProtocols SendProtocol;
        /// <summary>
        /// Login type
        /// </summary>
        public AdvanceLoginType LoginType;
        /// <summary>
        /// Send address[:port]
        /// </summary>
        public string SendAddress;
        /// <summary>
        /// receve address[:port]
        /// </summary>
        public string ReceiveAddress;
        /// <summary>
        /// Folder name on server
        /// </summary>
        public string Folder;
        /// <summary>
        /// Sender's email address
        /// </summary>
        public string Email;
        /// <summary>
        /// Keystore for certificate authentication
        /// </summary>
        public string KeyStore;
        /// <summary>
        /// User name or key alias for authentication
        /// </summary>
        public string User;
        /// <summary>
        /// user password or key password for authentication
        /// </summary>
        public char[] Password
        {
            get { return (this.password == null) ? null : (char[])this.password.Clone(); }
            set { this.password = (value == null) ? null : (char[])value.Clone(); }
        }
        private char[] password;

        public AdvanceEmailBox() : base() { }


        /// <summary>
        /// Fill object data from xml node
        /// </summary>
        /// <param name="source">Source Xml</param>
        protected override void LoadFromXmlNode(XmlNode source)
        {
            base.LoadFromXmlNode(source);
            this.Name = GetAttribute(source, "name");
            this.SendProtocol = GetEnumAttribute<AdvanceEmailSendProtocols>(source, "send", AdvanceEmailSendProtocols.NONE);
            this.ReceiveProtocol = GetEnumAttribute<AdvanceEmailReceiveProtocols>(source, "receive", AdvanceEmailReceiveProtocols.NONE);
            this.SendAddress = GetAttribute(source, "send-address", null);
            this.ReceiveAddress = GetAttribute(source, "receive-address", null);
            this.Folder = GetAttribute(source, "folder", null);
            this.KeyStore = GetAttribute(source, "keystore", null);
            this.User = GetAttribute(source, "user", null);
            this.LoginType = GetEnumAttribute<AdvanceLoginType>(source, "login-type", AdvanceLoginType.NONE);
            this.Email = GetAttribute(source, "email", null);
            this.password = GetPassword(source, "password");
        }

        /// <summary>
        /// Fills  object's xml
        /// </summary>
        /// <param name="node">Empty node for the object with name "tagname"</param>
        protected override void FillXmlElement(XmlElement node)
        {
            base.FillXmlElement(node);
            AddAttribute(node, "name", this.Name);
            AddAttribute(node, "send", this.SendProtocol);
            AddAttribute(node, "receive", this.ReceiveProtocol);
            AddAttribute(node, "send-address", this.SendAddress);
            AddAttribute(node, "receive-address", this.ReceiveAddress);
            AddAttribute(node, "folder", this.Folder);
            AddAttribute(node, "keystore", this.KeyStore);
            AddAttribute(node, "user", this.User);
            AddAttribute(node, "login-type", this.LoginType);
            AddAttribute(node, "email", this.Email);
            SetPassword(node, "password", password);
        }

        public AdvanceEmailBox Copy()
        {
            AdvanceEmailBox result = new AdvanceEmailBox();
            this.AssignTo(result);
            result.Name = this.Name;
            result.SendProtocol = this.SendProtocol;
            result.ReceiveProtocol = this.ReceiveProtocol;
            result.SendAddress = this.SendAddress;
            result.ReceiveAddress = this.ReceiveAddress;
            result.Folder = this.Folder;
            result.Email = this.Email;
            result.KeyStore = this.KeyStore;
            result.User = this.User;
            result.LoginType = this.LoginType;
            result.password = this.Password; return result;
        }
    }
}
