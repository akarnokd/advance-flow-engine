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
    public class AdvanceJMSEndpoint : AdvanceCreateModifyInfo, IPassword, ICopiable<AdvanceJMSEndpoint>, IIdentifiable<string>
{
        /// <summary>
        /// name used by blocks to reference this data source
        /// </summary>
        public string Id { get { return this.Name; } }
        public string Name;
        /// <summary>
        /// JDBC driver
        /// </summary>
        public string Driver;
        /// <summary>
        /// connection url
        /// </summary>
        public String Url;
        /// <summary>
        /// User name used for login
        /// </summary>
        public string User;
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
        /// Queue manager name
        /// </summary>
        public string QueueManager;
        /// <summary>
        /// Queue name
        /// </summary>
          public string Queue;
        /// <summary>
        /// communication pool size
        /// </summary>
        public int PoolSize;

        public AdvanceJMSEndpoint() : base() { }

        /// <summary>
        /// Fill object data from xml node
        /// </summary>
        /// <param name="source">Source Xml</param>
        protected override void LoadFromXmlNode(XmlNode source)
        {
            base.LoadFromXmlNode(source);
            this.Name = GetAttribute(source, "name");
            this.User = GetAttribute(source, "user", null);
            this.password = GetPassword(source, "password");
            this.Driver = GetAttribute(source, "driver", null);
            this.Url = GetAttribute(source, "url", null);
            this.PoolSize = GetIntAttribute(source, "poolsize", 5);
            this.Queue = GetAttribute(source, "queue", null);
            this.QueueManager = GetAttribute(source, "queue_manager", null);
        }

        /// <summary>
        /// Fills  object's xml
        /// </summary>
        /// <param name="node">Empty node for the object with name "tagname"</param>
        protected override void FillXmlElement(XmlElement node)
        {
            base.FillXmlElement(node);
            AddAttribute(node, "name", this.Name);
            AddAttribute(node, "user", this.User);
            SetPassword(node, "password", password);
            AddAttribute(node, "driver", this.Driver);
            AddAttribute(node, "url", this.Url);
            AddAttribute(node, "poolsize", this.PoolSize);
            AddAttribute(node, "queue", this.Queue);
            AddAttribute(node, "queue_manager", this.QueueManager);
        }

        public AdvanceJMSEndpoint Copy()
        {
            AdvanceJMSEndpoint result = new AdvanceJMSEndpoint();
            this.AssignTo(result);
            result.Name = this.Name;
            result.password = this.Password;
            result.Driver = this.Driver;
            result.Url = this.Url;
            result.PoolSize = this.PoolSize;
            result.Queue = this.Queue;
            result.QueueManager = this.QueueManager;
            return result;
        }

    }
}
