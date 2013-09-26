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
using System.Xml;

using AdvanceAPIClient.Core;

namespace AdvanceAPIClient.Classes.Runtime
{
    /// <summary>
    /// Diagnostic port for advance regular ports.
    /// </summary>
    public class PortDiagnostic : XmlReadWrite
    {
        /// <summary>
        /// Block identifier
        /// </summary>
        public string BlockId;
        /// <summary>
        /// Target ream
        /// </summary>
        public string Realm;
        /// <summary>
        /// Affected port
        /// </summary>
        public string Port;
        /// <summary>
        /// Possible copy of the value within the port
        /// </summary>
        public object Value;
        /// <summary>
        /// Timestamp when the port received this value
        /// </summary>
        public DateTime Timestamp = new DateTime();


        public PortDiagnostic() : base() { }

        /// <summary>
        /// Full constructor
        /// </summary>
        /// <param name="realm">realm</param>
        /// <param name="blockId">affected block</param>
        /// <param name="port">affected port</param>
        /// <param name="value">Value</param>
        public PortDiagnostic(string realm, string blockId, string port, object value)
        {
            this.Realm = realm;
            this.BlockId = blockId;
            this.Port = port;
            this.Value = value;
        }


        protected override void LoadFromXmlNode(XmlNode source)
        {
            this.Realm = GetAttribute(source, "realm", null);
            this.BlockId = GetAttribute(source, "block-id", null);
            this.Port = GetAttribute(source, "port", null);
            this.Timestamp = GetDateTimeAttribute(source, "timestamp");
            XmlNodeList children = GetChildren(source, "*");
            if (children == null || children.Count == 0)
                this.Value = null;
            else this.Value = children[0];
        }

        protected override void FillXmlElement(XmlElement parent)
        {
            AddAttribute(parent, "realm", this.Realm);
            AddAttribute(parent, "block-id", this.BlockId);
            AddAttribute(parent, "port", this.Port);
            AddDateTimeAttribute(parent, "timestamp", this.Timestamp);
            if (this.Value != null)
            {
                if (this.Value is XmlNode)
                    parent.AppendChild(parent.OwnerDocument.ImportNode(this.Value as XmlNode, true));
                else if (this.Value is XmlReadWrite)
                    (this.Value as XmlReadWrite).AddToXML(this.Value.GetType().Name, parent);
                else
                    AddContent(parent, this.Value.GetType().ToString() + " " + this.Value.ToString());

            }
        }

    }
}
