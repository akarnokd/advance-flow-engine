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
    public class AdvanceLocalFileDataSource : AdvanceCreateModifyInfo, ICopiable<AdvanceLocalFileDataSource>, IIdentifiable<string>
    {
        /// <summary>
        /// name used by blocks to reference this data source
        /// </summary>
        public string Id { get { return this.Name; } }
        public string Name;
        /// <summary>
        /// Directory where the file source(s) are located.
        /// </summary>
        public string FileDirectory;

        public AdvanceLocalFileDataSource() : base() { }

        /// <summary>
        /// Fill object data from xml node
        /// </summary>
        /// <param name="source">Source Xml</param>
        protected override void LoadFromXmlNode(XmlNode source)
        {
            base.LoadFromXmlNode(source);
            this.Name = GetAttribute(source, "name");
            this.FileDirectory = GetAttribute(source, "directory", null);
        }

        /// <summary>
        /// Fills  object's xml
        /// </summary>
        /// <param name="node">Empty node for the object with name "tagname"</param>
        protected override void FillXmlElement(XmlElement node)
        {
            base.FillXmlElement(node);
            AddAttribute(node, "name", this.Name);
            AddAttribute(node, "directory", this.FileDirectory);
        }

        public AdvanceLocalFileDataSource Copy()
        {
            AdvanceLocalFileDataSource result = new AdvanceLocalFileDataSource();
            this.AssignTo(result);
            result.Name = this.Name;
            result.FileDirectory = this.FileDirectory;
            return result;
        }

    }
}

