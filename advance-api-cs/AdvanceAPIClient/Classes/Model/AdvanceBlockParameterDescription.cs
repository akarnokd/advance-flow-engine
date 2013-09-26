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
    public class AdvanceBlockParameterDescription : XmlReadWrite, ICopiable<AdvanceBlockParameterDescription>
    {
        /// <summary>
        /// Unique (among other inputs or outputs of this block) identifier of the input parameter. 
        /// This ID will be used by the block wiring within the flow description.
        /// </summary>
        public string Id;
        /// <summary>
        /// Optional display text for this attribute. Can be used as a key into a translation table.
        /// </summary>
        public string DisplayName;
        /// <summary>
        /// URI pointing to documentation describing this parameter.
        /// </summary>
        public Uri Documentation;
        /// <summary>
        /// Type variable
        /// </summary>
        public AdvanceType Type;
        /// <summary>
        /// Indicates that this input is required
        /// </summary>
        public bool Required;
        /// <summary>
        /// If non-null, it contains the default constant
        /// </summary>
        public XmlNode DefaultValue;
        /// <summary>
        /// Indicates that parameter is actually a variable argument
        /// </summary>
        public bool Varargs;

        public AdvanceBlockParameterDescription() : base() { }

        /// <summary>
        /// Fill object data from xml node
        /// </summary>
        /// <param name="source"></param>
        protected override void LoadFromXmlNode(XmlNode source)
        {
            this.Id = GetAttribute(source, "id");
            this.DisplayName = GetAttribute(source, "displayname", null);
            this.Documentation = GetUriAttribute(source, "documentation");
            this.Required = GetBoolAttribute(source, "required", true);
            XmlNode defNode = GetChildNode(source, "default");
            if (defNode != null)
            {
                XmlNodeList children = GetChildren(defNode, "*");
                if (children.Count == 1)
                    this.DefaultValue = children[0];
                else
                    Log.LogString("Missing default value: " + source.Name);
            }
            this.Varargs = GetBoolAttribute(source, "varargs", false);
            this.Type = XmlReadWrite.CreateFromXml<AdvanceType>(source);
        }

        /// <summary>
        /// Fills object's xml
        /// </summary>
        /// <param name="node">Empty node for the object with name "tagname"</param>
        protected override void FillXmlElement(XmlElement node)
        {
            AddAttribute(node, "id", this.Id);
            AddAttribute(node, "displayname", this.DisplayName);
            AddAttribute(node, "documentation", this.Documentation);
            this.Type.AddToElement(node);
        }

        /// <summary>
        /// Creates a copy of object
        /// </summary>
        /// <returns>Copy of object</returns>
        public AdvanceBlockParameterDescription Copy()
        {
            AdvanceBlockParameterDescription result = new AdvanceBlockParameterDescription();
            result.Id = this.Id;
            result.DisplayName = this.DisplayName;
            result.Documentation = this.Documentation;
            result.Required = this.Required;
            result.DefaultValue = this.DefaultValue;
            result.Varargs = this.Varargs;
            result.Type = Type.Copy();
            return result;
        }
    }
}
