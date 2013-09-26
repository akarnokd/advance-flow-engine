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

    public class AdvanceUser : AdvanceCreateModifyInfo, ICopiable<AdvanceUser>, IPassword, IIdentifiable<string>
    {
        /// <summary>
        /// Is the user enabled?
        /// </summary>
        public bool IsEnabled;

        /// <summary>
        /// user's name
        /// </summary>
        public string Id { get { return this.Name; } }
        public string Name;

        /// <summary>
        /// email address
        /// </summary>
        public string Email;

        /// <summary>
        /// pager number
        /// </summary>
        public string Pager;

        /// <summary>
        /// sms number
        /// </summary>
        public string Sms;

        /// <summary>
        /// Date format string
        /// </summary>
        public string DateFormat;

        /// <summary>
        /// The date-time format string.
        /// </summary>
        public string DateTimeFormat;

        /// <summary>
        /// Number format string
        /// </summary>
        public string NumberFormat;

        /// <summary>
        /// Thousand separator character
        /// </summary>
        public char ThousandSeparator = ',';

        /// <summary>
        /// Decimal separator character
        /// </summary>
        public char DecimalSeparator = '.';

        /// <summary>
        /// Is user logging in via username/password? 
        /// </summary>
        public bool PasswordLogin;

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
        /// Keystore where the user certificate is locate
        /// </summary>
        public string KeyStore;

        /// <summary>
        /// Certificate alias.
        /// </summary>
        public string KeyAlias;

        /// <summary>
        /// Set of enabled user rights.
        /// </summary>
        public HashSet<AdvanceUserRights> Rights = new HashSet<AdvanceUserRights>();

        /// <summary>
        /// realms and set of rights.
        /// </summary>
        public Dictionary<string, HashSet<AdvanceUserRealmRights>> RealmRights = new Dictionary<string, HashSet<AdvanceUserRealmRights>>();
        
        public AdvanceUser() : base() { }

        /// <summary>
        /// Fill object data from xml node
        /// </summary>
        /// <param name="source">Source Xml</param>
        protected override void LoadFromXmlNode(XmlNode source)
        {
            base.LoadFromXmlNode(source);
            this.IsEnabled = GetBoolAttribute(source, "enabled", false);
            this.Name = GetAttribute(source, "name");
            this.Email = GetAttribute(source, "email", null);
            this.Pager = GetAttribute(source, "pager", null);
            this.Sms = GetAttribute(source, "sms", null);
            this.DateFormat = GetAttribute(source, "date-format", null);
            this.DateTimeFormat = GetAttribute(source, "date-time-format", null);
            this.NumberFormat = GetAttribute(source, "number-format", null);
            this.ThousandSeparator = GetAttribute(source, "thousand-separator", ",")[0];
            this.DecimalSeparator = GetAttribute(source, "decimal-separator", ".")[0];
            this.PasswordLogin = GetBoolAttribute(source, "password-login", false);
            this.password = GetPassword(source, "password");
            this.KeyStore = GetAttribute(source, "keystore", null);
            this.KeyAlias = GetAttribute(source, "keyalias", null);
            this.Rights.Clear();
            XmlNode rNode = GetChildNode(source, "rights");
            if (rNode != null)
                foreach (XmlNode node in GetChildren(rNode, "right"))
                    this.Rights.Add(GetEnumAttribute<AdvanceUserRights>(node, "value", AdvanceUserRights.UNKNOWN));
            this.RealmRights.Clear();
            rNode = GetChildNode(source, "realm-rights");
            if (rNode != null)
                foreach (XmlNode realmNode in GetChildren(rNode, "realm"))
                {
                    string realm = GetAttribute(realmNode, "name", "");
                    HashSet<AdvanceUserRealmRights> rset;
                    if (!this.RealmRights.TryGetValue(realm, out rset))
                    {
                        rset = new HashSet<AdvanceUserRealmRights>();
                        this.RealmRights.Add(realm, rset);
                    }
                    foreach (XmlNode node in GetChildren(realmNode, "right"))
                        rset.Add(GetEnumAttribute<AdvanceUserRealmRights>(node, "value", AdvanceUserRealmRights.UNKNOWN));
                }
        }

        /// <summary>
        /// Fills  object's xml
        /// </summary>
        /// <param name="node">Empty node for the object with name "tagname"</param>
        protected override void FillXmlElement(XmlElement node)
        {
            base.FillXmlElement(node);
			AddAttribute(node, "name", this.Name);
			AddBoolAttribute(node, "enabled", this.IsEnabled);
			AddAttribute(node, "email", this.Email);
			AddAttribute(node, "pager", this.Pager);
			AddAttribute(node, "sms", this.Sms);
            AddAttribute(node, "date-format", this.DateFormat);
            AddAttribute(node, "date-time-format", this.DateTimeFormat);
            AddAttribute(node, "number-format", this.NumberFormat);
            AddAttribute(node, "thousand-separator", this.ThousandSeparator);
		    AddAttribute(node, "decimal-separator", this.DecimalSeparator);
		    AddBoolAttribute(node, "password-login", this.PasswordLogin);
		    SetPassword(node, "password", this.password);
		    AddAttribute(node, "keystore", this.KeyStore);
		    AddAttribute(node, "keyalias", this.KeyAlias);
            XmlElement rNode = AddNode(node, "rights");
		    foreach (AdvanceUserRights r in this.Rights) 
                AddAttributeNode(rNode, "right", "value", r);
            rNode = AddNode(node, "realm-rights");
            foreach (KeyValuePair<string, HashSet<AdvanceUserRealmRights>> rr in this.RealmRights)
            {
			    XmlElement rrNode = AddAttributeNode(rNode, "realm", "name", rr.Key);
			    foreach (AdvanceUserRealmRights urr in rr.Value) 
                    AddAttributeNode(rrNode, "right", "value", urr);
			}
		}

        public AdvanceUser Copy()
        {
            AdvanceUser ret = new AdvanceUser();
            ret.IsEnabled = this.IsEnabled;
            ret.Name = this.Name;
            ret.Email = this.Email;
            ret.Pager = this.Pager;
            ret.Sms = this.Sms;
            ret.DateFormat = this.DateFormat;
            ret.DateTimeFormat = this.DateTimeFormat;
            ret.NumberFormat = this.NumberFormat;
            ret.ThousandSeparator = this.ThousandSeparator;
            ret.DecimalSeparator = this.DecimalSeparator;
            ret.PasswordLogin = this.PasswordLogin;
            ret.Password = this.password;
            ret.KeyStore = this.KeyStore;
            ret.KeyAlias = this.KeyAlias;
            ret.Rights = new HashSet<AdvanceUserRights>(this.Rights);
            ret.RealmRights = new Dictionary<string, HashSet<AdvanceUserRealmRights>>(this.RealmRights);
            return ret;
        }

    }
}
