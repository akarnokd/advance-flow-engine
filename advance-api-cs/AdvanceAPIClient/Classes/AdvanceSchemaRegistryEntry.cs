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

namespace AdvanceAPIClient.Classes
{
    /// <summary>
    /// schema registry used to ask the engine about known schemas
    /// </summary>
    public class AdvanceSchemaRegistryEntry : XmlReadWrite
    {
        /// <summary>
        /// schema filename (without path)
        /// </summary>
        public string Name;
        /// <summary>
        /// schema content
        /// </summary>
        public XmlNode Schema;

        public AdvanceSchemaRegistryEntry() : base() {}

        /// <summary>
        /// Fill object data from xml node
        /// </summary>
        /// <param name="source">Source Xml</param>
        protected override void LoadFromXmlNode(XmlNode source)
        {
            this.Name = GetAttribute(source, "name", null);
            XmlNode sNode = GetChildNode(source, "shema");
            if (sNode != null)
                this.Schema = sNode.CloneNode(true);
        }

        /// <summary>
        /// Fills  object's xml
        /// </summary>
        /// <param name="node">Empty node for the object with name "tagname"</param>
        protected override void FillXmlElement(XmlElement node)
        {
            AddAttribute(node, "name", this.Name);
            if (this.Schema != null)
                node.AppendChild(node.OwnerDocument.ImportNode(this.Schema, true));
        }
 
    }
}
