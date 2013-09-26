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
    /// Properties of a key in a key store.
    /// </summary>
    public class AdvanceKeyEntry : XmlReadWrite, ICopiable<AdvanceKeyEntry>, IIdentifiable<String>
    {
        /// <summary>
        /// Key type
        /// </summary>
        public AdvanceKeyType Type;
        /// <summary>
        /// Key name
        /// </summary>
        public string Id { get { return this.Name; } }
        public string Name;
        /// <summary>
        /// Creation date
        /// </summary>
        public DateTime CreatedAt;

        public AdvanceKeyEntry() : base() { }

        public AdvanceKeyEntry Copy()
        {
            AdvanceKeyEntry result = new AdvanceKeyEntry();
            result.Type = this.Type;
            result.Name = this.Name;
            result.CreatedAt = new DateTime(this.CreatedAt.Ticks);
            return result;
        }

        /// <summary>
        /// Fill object data from xml node
        /// </summary>
        /// <param name="source">Source Xml</param>
        protected override void LoadFromXmlNode(XmlNode source)
        {
            this.Type = GetEnumAttribute<AdvanceKeyType>(source, "type", AdvanceKeyType.PRIVATE_KEY);
            this.Name = GetAttribute(source, "name", null);
            this.CreatedAt = GetDateTimeAttribute(source, "created-at");
        }

        /// <summary>
        /// Fills  object's xml
        /// </summary>
        /// <param name="node">Empty node for the object with name "tagname"</param>
        protected override void FillXmlElement(XmlElement node)
        {
            AddAttribute(node, "type", this.Type);
            AddAttribute(node, "name", this.Name);
            AddDateTimeAttribute(node, "created-at", this.CreatedAt);
        }

    }
}
