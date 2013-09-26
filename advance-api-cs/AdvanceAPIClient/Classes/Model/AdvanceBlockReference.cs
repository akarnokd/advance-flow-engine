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
    /// The block reference for the {@code flow-description.xsd} used within composite blocks.
    /// </summary>
    public class AdvanceBlockReference : XmlReadWrite
    {
        /// <summary>
        /// Unique identifier of this block among the current level of blocks
        /// </summary>
        public string Id;
        /// <summary>
        /// block type identifier referencing a block description
        /// </summary>
        public string Type;
        /// <summary>
        /// user-entered documentation of this composite block.
        /// </summary>
        public string Documentation;
        /// <summary>
        /// parent block of this composite block
        /// </summary>
        public AdvanceCompositeBlock parent;
        /// <summary>
        /// user-entered keywords for easier finding of this block.
        /// </summary>
        public List<string> Keywords = new List<string>();
        /// <summary>
        /// visual properties for the Flow Editor
        /// </summary>
        public AdvanceBlockVisuals Visuals;
        /// <summary>
        /// Contains number of parameters for the varargs inputs
        /// </summary>
        public Dictionary<string, int> Varargs = new Dictionary<string, int>();


        /// <summary>
        /// Fill object data from xml node
        /// </summary>
        /// <param name="source"></param>
        protected override void LoadFromXmlNode(XmlNode source)
        {
 		    this.Id = GetAttribute(source, "id");
		    this.Type = GetAttribute(source, "type");
		    this.Documentation = GetAttribute(source, "documentation", null);
            this.Keywords = GetListAttribute(source, "keywords");
		    this.Visuals = CreateFromXml<AdvanceBlockVisuals>(source);
		    foreach (XmlNode node in GetChildren(source, "vararg"))
			    this.Varargs.Add(GetAttribute(node, "name"), GetIntAttribute(node, "count", 0));
		}


        /// <summary>
        /// Fills object's xml
        /// </summary>
        /// <param name="node">Empty node for the object with name "tagname"</param>
        protected override void FillXmlElement(XmlElement node)
        {
            AddAttribute(node, "id", this.Id);
            AddAttribute(node, "type", this.Type);
            AddAttribute(node, "documentation", this.Documentation);
            AddListAttribute(node, "keywords", this.Keywords);
            this.Visuals.AddToElement(node);
            foreach (KeyValuePair<string, int> va in this.Varargs)
            {
                XmlElement e = AddAttributeNode(node, "vararg", "name", va.Key);
                AddAttribute(e, "count", va.Value.ToString());
            }
        }
    }
}
