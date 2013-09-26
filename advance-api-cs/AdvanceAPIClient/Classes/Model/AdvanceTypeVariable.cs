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
    /// The type variable definition of an Advance block.
    /// </summary>
    public class AdvanceTypeVariable : XmlReadWrite
    {
        /// <summary>
        /// type parameter name
        /// </summary>
        public string Name;
        /// <summary>
        /// Upper bounds of this type parameter, e.g., T super SomeObject1 &amp; SomeObject2. 
        /// </summary>
        public List<AdvanceType> Bounds = new List<AdvanceType>();
        /// <summary>
        /// Indicator if the bounds are representing the upper bound.
        /// </summary>
        public bool IsUpperBound;
        /// <summary>
        /// Documentation explaining this type variable.
        /// </summary>
        public Uri Documentation;

        public AdvanceTypeVariable() : base() {}

        /// <summary>
        /// Create a deep copy of this type variable
        /// </summary>
        /// <returns>new type variable</returns>
        public AdvanceTypeVariable Copy()
        {
            AdvanceTypeVariable result = new AdvanceTypeVariable();
            result.Name = this.Name;
            result.IsUpperBound = this.IsUpperBound;
            result.Documentation = this.Documentation;
            foreach (AdvanceType b in this.Bounds)
            {
                result.Bounds.Add(b.Copy());
            }
            return result;
        }

        public override string ToString()
        {
            StringBuilder b = new StringBuilder();

            b.Append(this.Name);
            if (this.Bounds.Count > 0)
            {
                if (this.IsUpperBound)
                    b.Append(" super ");
                else
                    b.Append(" extends ");
                int i = 0;
                foreach (AdvanceType bound in this.Bounds)
                {
                    if (i > 0) b.Append(" & ");
                    b.Append(bound);
                }
            }
            return b.ToString();
        }

        /// <summary>
        /// Fill object data from xml node
        /// </summary>
        /// <param name="source"></param>
        protected override void LoadFromXmlNode(XmlNode source)
        {
            this.Name = GetAttribute(source, "name", null);
            this.IsUpperBound = false;
            foreach (XmlNode node in GetChildren(source, "upper-bound"))
            {
                this.IsUpperBound = true;
                this.Bounds.Add(CreateFromXml<AdvanceType>(node));
            }
            foreach (XmlNode node in GetChildren(source, "lower-bound"))
            {
                if (this.IsUpperBound)
                    this.ThrowException("Type variable has multiple bound types! ");
                this.Bounds.Add(CreateFromXml<AdvanceType>(node));
            }
            this.Documentation = GetUriAttribute(source, "documentation");
        }

        /// <summary>
        /// Fills  object's xml
        /// </summary>
        /// <param name="node">Empty node for the object with name "tagname"</param>
        protected override void FillXmlElement(XmlElement node)
        {
            AddAttribute(node, "name", this.Name);
            string boundName = this.IsUpperBound ? "upper-bound" : "lower-bound";
            foreach (AdvanceType t in this.Bounds)
                t.AddToXML(boundName, node);
            if (this.Documentation != null)
                AddAttribute(node, "documentation", this.Documentation);
        }
    }
}


