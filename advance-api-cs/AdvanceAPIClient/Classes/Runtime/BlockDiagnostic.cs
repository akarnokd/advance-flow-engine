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

namespace AdvanceAPIClient.Classes.Runtime
{
    /// <summary>
    ///  Diagnostic port for the ADVANCE blocks.
    /// </summary>
    public class BlockDiagnostic : XmlReadWrite
    {
        /// <summary>
        /// Block identifier
        /// </summary>
        public string BlockId;
        /// <summary>
        /// Target ream
        /// </summary>
        public string Realm;
        /// <summary>
        /// Possible copy of the value within the port
        /// </summary>
        public BlockState State;
        /// <summary>
        /// Timestamp when the port received this value
        /// </summary>
        public DateTime Timestamp = new DateTime();

        /// <summary>
        /// Create an empty block diagnostic object to be filled in via load().
        /// </summary>
        private BlockDiagnostic() : base() { }

        /// <summary>
        /// Full consrtuktor
        /// </summary>
        /// <param name="realm">Realm in question</param>
        /// <param name="blockId">Affected block identifier</param>
        /// <param name="state">Block state</param>
        public BlockDiagnostic(String realm, String blockId, BlockState state) : base()
        {
            this.Realm = realm;
            this.BlockId = blockId;
            this.State = state;
        }

        protected override void LoadFromXmlNode(XmlNode source)
        {
            this.Realm = GetAttribute(source, "realm", null);
            this.BlockId = GetAttribute(source, "block-id", null);
            this.Timestamp = GetDateTimeAttribute(source, "timestamp");
            this.State = GetEnumAttribute(source, "state", BlockState.UNKNOWN);
        }

        protected override void FillXmlElement(XmlElement parent)
        {
            AddAttribute(parent, "realm", this.Realm);
            AddAttribute(parent, "block-id", this.BlockId);
            AddDateTimeAttribute(parent, "timestamp", this.Timestamp);
            AddAttribute(parent, "state", this.State.ToString());
        }
	}

}

