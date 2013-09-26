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

namespace AdvanceAPIClient.Classes
{
    /**
     * Contains version information and engine details.
     * @author akarnokd, 2011.09.19.
     */
    public class AdvanceEngineVersion : XmlReadWrite
    {
        /// <summary>
        /// Minor version number. 
        /// When displayed, this should be a two digit zero padded number, e.g., 1 is 1.01 and 20 is 1.20. 
        /// </summary>
        public int MinorVersion;
        /// <summary>
        /// Major version number. No padding is required
        /// </summary>
        public int MajorVersion;
        /// <summary>
        /// Build number. When displayed, this should be a three digit zero padded number, e.g., 1 is 1.00.001.
        /// </summary>
        public int BuildNumber;

        public string VersionString
        {
            get
            {
                return String.Format("{0}.{1}.{2}", this.MajorVersion, this.MinorVersion, this.BuildNumber);
            }
            set
            {
                string[] parts = value.Split('.');
                if (parts.Length != 3 ||
                   !int.TryParse(parts[0], out this.MajorVersion) ||
                   !int.TryParse(parts[1], out this.MinorVersion) ||
                   !int.TryParse(parts[2], out this.BuildNumber))
                    this.ThrowException("Illagal version format: " + value);
            }
        }

        public AdvanceEngineVersion() : base() { }

        /// <summary>
        /// Fill object data from xml node
        /// </summary>
        /// <param name="source"></param>
        protected override void LoadFromXmlNode(XmlNode source)
        {
            this.VersionString = GetAttribute(source, "version");
        }

        /// <summary>
        /// Fills  object's xml
        /// </summary>
        /// <param name="node">Empty node for the object with name "tagname"</param>
        protected override void FillXmlElement(XmlElement node)
        {
            AddAttribute(node, "version", this.VersionString);
        }

        public override string ToString()
        {
            return this.VersionString;
        }
    }
}
