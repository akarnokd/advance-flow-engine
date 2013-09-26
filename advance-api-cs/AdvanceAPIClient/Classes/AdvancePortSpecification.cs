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
using AdvanceAPIClient.Classes.Model;

namespace AdvanceAPIClient.Classes
{

    public class AdvancePortSpecification : XmlReadWrite
    {
        /// <summary>
        /// Is this an input port?
        /// </summary>
        public bool IsInput;
        /// <summary>
        /// port identifier
        /// </summary>
        public String Id;
        /// <summary>
        /// port's type.
        /// </summary>
        public AdvanceType Type;

        public AdvancePortSpecification() : base() { }

        /// <summary>
        /// Fill object data from xml node
        /// </summary>
        /// <param name="source">Source Xml</param>
        protected override void LoadFromXmlNode(XmlNode source)
        {
            this.IsInput = GetBoolAttribute(source, "is-input", true);
            this.Id = GetAttribute(source, "id");
            this.Type = CreateFromXml<AdvanceType>(source);
        }

        /// <summary>
        /// Fills object's xml
        /// </summary>
        /// <param name="node">Empty node for the object with name "tagname"</param>
        protected override void FillXmlElement(XmlElement node)
        {
            AddBoolAttribute(node, "is-input", this.IsInput);
            AddAttribute(node, "id", this.Id);
            this.Type.AddToElement(node);
        }

    }
}
