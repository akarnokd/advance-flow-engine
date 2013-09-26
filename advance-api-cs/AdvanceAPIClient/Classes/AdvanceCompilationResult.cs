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
using AdvanceAPIClient.Classes.Model;
using AdvanceAPIClient.Classes.Error;

namespace AdvanceAPIClient.Classes
{
    /// <summary>
    /// Record to store compiler errors, warnings and the computed types of various wires.
    /// </summary>
    public class AdvanceCompilationResult : XmlReadWrite
    {
        /// <summary>
        /// True if no error ocured
        /// </summary>
        public bool Success { get { return this.errors.Count == 0; } }
        /// <summary>
        /// List of compilation errors.
        /// </summary>
        public List<CompilationErrorBase> Errors { get { return new List<CompilationErrorBase>(this.errors); } }
        private List<CompilationErrorBase> errors = new List<CompilationErrorBase>();
        /// <summary>
        /// Inferred wire types.
        /// </summary>   
        public ICollection<AdvanceType> WireTypes { get { return this.wireTypes.Values; } }
        private Dictionary<string, AdvanceType> wireTypes = new Dictionary<string, AdvanceType>();

        /// <summary>
        /// Fill object data from xml node
        /// </summary>
        /// <param name="source"></param>
        protected override void LoadFromXmlNode(XmlNode source)
        {
            XmlNode errs = GetChildNode(source, "errors");
            if (errs != null)
                foreach (XmlNode e in GetChildren(errs, "error"))
                    this.errors.Add(CompilationErrorBase.CreateErrorFromXml(e));

            XmlNode wTypes = GetChildNode(source, "wire-types");
            if (wTypes != null)
                foreach (XmlNode e in GetChildren(errs, "wire-type"))
                    this.wireTypes.Add(GetAttribute(e, "wire-id"), CreateFromXml<AdvanceType>(e));
        }

        /// <summary>
        /// Fills object's xml
        /// </summary>
        /// <param name="node">Empty node for the object with name "tagname"</param>
        protected override void FillXmlElement(XmlElement node)
        {
            XmlElement errors = AddNode(node, "errors");
            foreach (CompilationErrorBase e in this.errors)
                e.AddToXML("error", errors);
            XmlElement types = AddNode(node, "wire-types");
            foreach (AdvanceType at in this.wireTypes.Values)
            {
                XmlElement e = at.AddToXML("wire-type", types);
                AddAttribute(e, "wire-id", at.Id);
            }
        }

    }
}
