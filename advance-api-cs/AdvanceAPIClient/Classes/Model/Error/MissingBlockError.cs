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
    /// Combination (e.g., union) of two types could not be created.
    /// </summary>
    public class MissingBlockError : CompilationErrorBase
    {
        public const string TYPE_NAME = "missingblockerror";
        /// <summary>
        /// wire identifier
        /// </summary>
        public string Id;
        /// <summary>
        /// Missing block type
        /// </summary>
        public string Type;
         /// <summary>
        /// Empty constructor
        /// </summary>
        public MissingBlockError() : base() { }
 
        /// <summary>
        /// Returns error message in text form
        /// </summary>
        /// <returns>Message text</returns>
        public override string ToString()
        {
            return "Missing block type " + this.Type + " referenced by ID " + this.Id;
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
        }

        /// <summary>
        /// Fills object's xml
        /// </summary>
        /// <param name="node">Empty node for the object with name "tagname"</param>
        protected override void FillXmlElement(XmlElement node)
        {
            base.FillXmlElement(node);
            AddAttribute(node, "id", this.Id);
            AddAttribute(node, "block-type", this.Type);
        }

    }

}
