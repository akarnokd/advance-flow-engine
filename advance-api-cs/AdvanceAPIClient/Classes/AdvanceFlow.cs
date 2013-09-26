﻿/*
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
    /// <summary>
    /// composite block description for the flow description of {@code flow-description.xsd}.
    /// </summary>
    public class AdvanceFlow : XmlReadWrite
    {
        public AdvanceCompositeBlock Content;

        public AdvanceFlow() : base() { }

        public AdvanceFlow(AdvanceCompositeBlock content) : this() 
        {
            this.Content = content;
        }

        /// <summary>
        /// Fill object data from xml node
        /// </summary>
        /// <param name="source">Source Xml</param>
        protected override void LoadFromXmlNode(XmlNode source)
        {
            this.Content.AddToXML("composite-block", source);
        }

        /// <summary>
        /// Fills object's xml
        /// </summary>
        /// <param name="node">Empty node for the object with name "tagname"</param>
        protected override void FillXmlElement(XmlElement node)
        {
            XmlNode root = GetChildNode(node, "composite-block");
            this.Content = (root == null) ? new AdvanceCompositeBlock() : CreateFromXml<AdvanceCompositeBlock>(root);
        }
 
    }
}
