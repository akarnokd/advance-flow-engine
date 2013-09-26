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
    /// Represents a constant block in the flow descriptor.
    /// </summary>
    public class AdvanceConstantBlock : XmlReadWrite
    {
        /// <summary>
        /// unique identifier of this block among the current level of blocks
        /// </summary>
        public string Id;
        /// <summary>
        /// content type of this block
        /// </summary>
        public string TypeString;
        /// <summary>
        /// Type
        /// </summary>
        public XType Type;
        /// <summary>
        /// display text for this attribute. Can be used as a key into a translation table.
        /// </summary>
        public string DisplayName;
        /// <summary>
        /// constant value
        /// </summary>
        public XmlNode Value;
        /// <summary>
        /// user-entered documentation of this parameter
        /// </summary>
        public String Documentation;
        /** The user-entered keywords for easier finding of this parameter. */
        public List<String> Keywords = new List<string>();
        /** The visual properties for the Flow Editor. */
        public AdvanceBlockVisuals Visuals = new AdvanceBlockVisuals();

        public AdvanceConstantBlock() : base() { }

        /// <summary>
        /// Fill object data from xml node
        /// </summary>
        /// <param name="source"></param>
        protected override void LoadFromXmlNode(XmlNode source)
        {
            this.Id = GetAttribute(source, "id");
            this.DisplayName = GetAttribute(source, "displayname", null);
            this.TypeString = GetAttribute(source, "type");
            this.Documentation = GetAttribute(source, "documentation", null);
            this.Keywords = GetListAttribute(source, "keywords");
            this.Visuals = CreateFromXml<AdvanceBlockVisuals>(source);
            this.Value = GetChildNode(source, "*");
        }

        /// <summary>
        /// Fills object's xml
        /// </summary>
        /// <param name="node">Empty node for the object with name "tagname"</param>
        protected override void FillXmlElement(XmlElement node)
        {
            AddAttribute(node, "id", this.Id);
            AddAttribute(node, "type", this.TypeString);
            AddAttribute(node, "documentation", this.Documentation);
            AddListAttribute(node, "keywords", this.Keywords);
            this.Visuals.AddToElement(node);
            node.AppendChild(node.OwnerDocument.ImportNode(this.Value, true));

        }
        /// <summary>
        /// Parse type string into an advance type declaration.
        /// </summary>
        /// <returns></returns>
        public AdvanceType GetAdvanceType() 
        {
		AdvanceType at = new AdvanceType();
		List<string> tokens = new List<string>();
        StringBuilder b = new StringBuilder();
        for (int i = 0; i < this.TypeString.Length; i++) {
            char c = this.TypeString[i];
            if (c == ',' || c == '<' || c == '>') {
                tokens.Add(b.ToString().Trim());
                tokens.Add(c.ToString());
                b.Clear();
            } 
            else 
                b.Append(c);
        }
        if (b.Length > 0) 
            tokens.Add(b.ToString());
        // uri[<uri[<...>][,uri[<...>]]>]
        
        at.TypeURI = new Uri(tokens[0]);
        if (tokens.Count > 3 && tokens[1].Equals("<"))
        {
            tokens.RemoveAt(0);
            int pointer = 2;
            ParseUriList(at, tokens, ref pointer);
        }
		return at;
	}

        /// <summary>
        /// Parses the token sequence into a list of type arguments.
        /// </summary>
        /// <param name="argsFor">parent type</param>
        /// <param name="tokens"> list of remaining tokens</param>
        /// <param name="pointer">pointer in tokenlist</param>
        private static void ParseUriList(AdvanceType argsFor, List<string> tokens, ref int pointer)
        {
            while (pointer < tokens.Count)
            {
                AdvanceType at = new AdvanceType();
                argsFor.TypeArguments.Add(at);
                at.TypeURI = new Uri(tokens[pointer++]);
                if (tokens.Count > pointer)
                    switch (tokens[pointer++])
                    {
                        case "<": ParseUriList(at, tokens, ref pointer); break;
                        case ">": return;
                        default: break;
                    }
            }
        }
    }
}
