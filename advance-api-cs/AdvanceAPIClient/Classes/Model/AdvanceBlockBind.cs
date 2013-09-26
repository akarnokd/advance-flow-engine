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
using AdvanceAPIClient.Classes.Typesystem;

namespace AdvanceAPIClient.Classes.Model
{
    /// <summary>
    /// Definition for binding parameters of blocks in the {@code flow-description.xsd}
    /// </summary>
    public class AdvanceBlockBind : XmlReadWrite
    {
        /// <summary>
        /// Identifier of this binding. May be used to communicate problematic bindings for the Flow Editor / Compiler. */
        /// </summary>
        public string Id;
        /// <summary>
        /// parent composite block
        /// </summary>
        public AdvanceCompositeBlock Parent;
        /// <summary>
        /// source block identifier for the binding. If {@code ""}, the source-parameter refers to the enclosing composite-block's input parameter.
        /// </summary>
        public string SourceBlock;
        /// <summary>
        /// source parameter identifier of the source-block or the enclosing composite-block's input parameter.
        /// </summary>
        public string SourceParameter;
        /// <summary>
        /// destination block identifier for the binding. If {@code ""}, the destination-parameter refers to the enclosing composite-block's output parameter.
        /// </summary>
        public string DestinationBlock;
        /// <summary>
        /// destination parameter identifier of the destination-block or the enclosing composite-block's output parameter.
        /// </summary>
        public string DestinationParameter;

        public AdvanceBlockBind() : base() { }

        /// <summary>
        /// Fill object data from xml node
        /// </summary>
        /// <param name="source"></param>
        protected override void LoadFromXmlNode(XmlNode source)
        {
            this.Id = GetAttribute(source, "id");
            this.SourceBlock = GetAttribute(source, "source-block", "");
            this.SourceParameter = GetAttribute(source, "source-parameter", null);
            this.DestinationParameter = GetAttribute(source, "destination-block", "");
            this.DestinationParameter = GetAttribute(source, "destination-parameter", null);
        }

        /// <summary>
        /// Fills object's xml
        /// </summary>
        /// <param name="node">Empty node for the object with name "tagname"</param>
        protected override void FillXmlElement(XmlElement node)
        {
            AddAttribute(node, "id", this.Id);
            AddAttribute(node, "source-block", this.SourceBlock);
            AddAttribute(node, "source-parameter", this.SourceParameter);
            AddAttribute(node, "destination-block", this.DestinationBlock);
            AddAttribute(node, "destination-parameter", this.DestinationParameter);
        }

        /// <summary>
        /// Test if a source block is given or this binding refers to a parameter of the enclosing composite block
        /// </summary>
        /// <returns></returns>
        public bool HasSourceBlock()
        {
            return !string.IsNullOrEmpty(this.SourceBlock);
        }

        /// <summary>
        /// Test if a destination block is given or this binding refers to a parameter of the enclosing composite block
        /// </summary>
        /// <returns></returns>
        public bool HasDestinationBlock()
        {
            return !string.IsNullOrEmpty(this.DestinationBlock);
        }

        public override string ToString()
        {
            return string.Format("{ id = {0}, source-block = {1}, source-parameter = {2}, destination-block = {3}, destination-parameter = {4} }",
                    this.Id, this.SourceBlock, this.SourceParameter, this.DestinationBlock, this.DestinationParameter);
        }
    }
}
