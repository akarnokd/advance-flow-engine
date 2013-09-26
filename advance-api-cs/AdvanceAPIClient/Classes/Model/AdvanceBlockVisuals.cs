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
using AdvanceAPIClient.Interfaces;

namespace AdvanceAPIClient.Classes.Model
{

    /// <summary>
    /// Record to store visualization properties of a block, constant or composite block
    /// </summary>
    public class AdvanceBlockVisuals : XmlReadWrite
    {
        /// <summary>
        /// x location on scree
        /// </summary>
        public int X;
        /// <summary>
        /// y location on scree
        /// </summary>
        public int Y;

        public AdvanceBlockVisuals() : base() {}

        /// <summary>
        /// Fill object data from xml node
        /// </summary>
        /// <param name="source"></param>
        protected override void LoadFromXmlNode(XmlNode source)
        {
            this.X = GetIntAttribute(source, "x", 0);
            this.Y = GetIntAttribute(source, "x", 0);
        }

        /// <summary>
        /// Fills object's xml
        /// </summary>
        /// <param name="node">Empty node for the object with name "tagname"</param>
        protected override void FillXmlElement(XmlElement node)
        {
            AddAttribute(node, "x", this.X);
            AddAttribute(node, "y", this.Y);
        }

    }
}
