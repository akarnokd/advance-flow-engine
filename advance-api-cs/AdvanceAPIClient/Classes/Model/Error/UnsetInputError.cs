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
using AdvanceAPIClient.Classes.Model;

namespace AdvanceAPIClient.Classes.Error
{
    /// <summary>
    /// A block input marked as required is not wired to a source.
    /// </summary>
    public class UnsetInputError : CompilationErrorBase
    {
        public const string TYPE_NAME = "unsetinputerror";
        /// <summary>
        ///  Block ID where this happened. 
        /// </summary>
        public string Id;
        /// <summary>
        /// Missing block type
        /// </summary>
        public string Type;
        /// <summary>
        /// Name of input
        /// </summary>
        public string Input;

        /// <summary>
        /// Empty constructor
        /// </summary>
        public UnsetInputError() : base() { }
 
        /// <summary>
        /// Returns error message in text form
        /// </summary>
        /// <returns>Message text</returns>
        public override string ToString()
        {
            return "The varargs input " + this.Type + ":" + this.Input + " count is not set by the block reference " + this.Id;
        }

        /// <summary>
        /// Fill object data from xml node
        /// </summary>
        /// <param name="source"></param>
        protected override void LoadFromXmlNode(XmlNode source)
        {
            base.LoadFromXmlNode(source);
            this.Id = GetAttribute(source, "id", "?");
            this.Type = GetAttribute(source, "block-type", "?");
            this.Input = GetAttribute(source, "input", "?");
        }

        /// <summary>
        /// Fills object's xml
        /// </summary>
        /// <param name="node">Empty node for the object with name "tagname"</param>
        protected override void FillXmlElement(XmlElement node)
        {
            base.FillXmlElement(node);
            AddAttribute(node, "id", this.Id);
            AddAttribute(node, "block-type", this.Id);
            AddAttribute(node, "input", this.Input);
        }
    }

}
