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

namespace AdvanceAPIClient.Classes.Model
{
    /// <summary>
    /// ADVANCE block description record.
    /// </summary>
    public class AdvanceBlockDescription : XmlReadWrite
    {
        public const string TAG_NAME = "block-description";
        /// <summary>
        /// Unique block identifier or name
        /// </summary>
        public string Id;
        /// <summary>
        /// Optional display text for this block. Can be used as a key into a translation table.
        /// </summary>
        public string DisplayName;
        /// <summary>
        /// Optional reference to the documentation of this block. May point to a Wiki page.
        /// </summary>
        public Uri Documentation;
        /// <summary>
        /// Short description 
        /// </summary>
        public string Tooltip;
        /// <summary>
        /// keywords associated with this block type
        /// </summary>
        public List<string> Keywords = new List<string>();
        /// <summary>
        /// Category for this block
        /// </summary>
        public string Category;
        /// <summary>
        /// Definition of input parameter. Producer blocks may not have any input parameters.
        /// </summary>
        public Dictionary<string, AdvanceBlockParameterDescription> Inputs = new Dictionary<string, AdvanceBlockParameterDescription>();
        /// <summary>
        /// Definition of output parameter. Consumer blocks may not have any output parameters.
        /// </summary>
        public Dictionary<string, AdvanceBlockParameterDescription> Outputs = new Dictionary<string, AdvanceBlockParameterDescription>();
        /// <summary>
        /// Definitions of various generic type parameters
        /// </summary>
        public Dictionary<string, AdvanceTypeVariable> TypeVariables = new Dictionary<string, AdvanceTypeVariable>();
        /// <summary>
        /// True if any of the input was defined to be varargs.
        /// </summary>
        public bool HasVarargs;

        /// <summary>
        /// Empty constructor
        /// </summary>
        public AdvanceBlockDescription() : base() { }

        /// <summary>
        /// Create copy of this block description with separate type graph
        /// </summary>
        /// <returns>New block description</returns>
        public AdvanceBlockDescription Copy()
        {
            return XmlReadWrite.Copy<AdvanceBlockDescription>(this);
        }

        /// <summary>
        /// Assign (shared) values from the other block description.
        /// </summary>
        /// <param name="other"></param>
        public void Assign(AdvanceBlockDescription other)
        {
            this.Id = other.Id;
            this.Category = other.Category;
            this.DisplayName = other.DisplayName;
            this.Keywords.AddRange(other.Keywords);
            this.Documentation = other.Documentation;
            foreach (KeyValuePair<string, AdvanceBlockParameterDescription> kvp in other.Inputs)
                this.Inputs[kvp.Key] = kvp.Value;
            foreach (KeyValuePair<string, AdvanceBlockParameterDescription> kvp in other.Outputs)
                this.Outputs[kvp.Key] = kvp.Value;
            foreach (KeyValuePair<string, AdvanceTypeVariable> kvp in other.TypeVariables)
                this.TypeVariables[kvp.Key] = kvp.Value;
            this.Tooltip = other.Tooltip;
        }

        /// <summary>
        /// Fill object data from xml node
        /// </summary>
        /// <param name="source"></param>
        protected override void LoadFromXmlNode(XmlNode source)
        {
            this.Id = GetAttribute(source, "id");
            this.DisplayName = GetAttribute(source, "displayname", null);
            this.Documentation = GetUriAttribute(source, "documentation");
            this.Tooltip = GetAttribute(source, "tooltip", null);
            this.Keywords = GetListAttribute(source, "keywords");
            this.Category = GetAttribute(source, "category", null);

            List<AdvanceType> typeRefs = new List<AdvanceType>();
            Dictionary<String, AdvanceType> sharedTypes = new Dictionary<string, AdvanceType>();

            foreach (XmlNode tp in GetChildren(source, "type-variable"))
            {
                AdvanceTypeVariable bpd = CreateFromXml<AdvanceTypeVariable>(tp);
                if (this.TypeVariables.ContainsKey(bpd.Name))
                    this.ThrowDuplicatedIdentifierException(tp, this.Id);
                typeRefs.AddRange(bpd.Bounds);
                AdvanceType st = new AdvanceType();
                st.TypeVariableName = bpd.Name;
                st.TypeVariable = bpd;
                sharedTypes.Add(bpd.Name, st);
            }

            //	throw new MissingTypeVariableException(root.getXPath(), b.typeVariableName);
            while (typeRefs.Count > 0)
            {
                AdvanceType tvb = typeRefs[0];
                typeRefs.RemoveAt(0);
                if (tvb.TypeVariableName != null)
                {
                    if (!this.TypeVariables.TryGetValue(tvb.TypeVariableName, out tvb.TypeVariable))
                        this.ThrowMissingTypeVariableException(source, tvb.TypeVariableName);
                }
                else
                    typeRefs.AddRange(tvb.TypeArguments);
            }

            List<AdvanceType> typeParams = new List<AdvanceType>();
            this.HasVarargs = false;
            foreach (XmlNode inode in GetChildren(source, "input"))
            {
                AdvanceBlockParameterDescription bpd = CreateFromXml<AdvanceBlockParameterDescription>(inode);
                this.HasVarargs |= bpd.Varargs;
                if (this.Inputs.ContainsKey(bpd.Id))
                    this.ThrowDuplicatedIdentifierException(inode, bpd.Id);
                else
                    this.Inputs.Add(bpd.Id, bpd);
                // use the shared type object instead of an individual type
                if (sharedTypes.ContainsKey(bpd.Type.TypeVariableName))
                    bpd.Type = sharedTypes[bpd.Type.TypeVariableName];
                typeParams.Add(bpd.Type);
            }
            foreach (XmlNode onode in GetChildren(source, "output"))
            {
                AdvanceBlockParameterDescription bpd = CreateFromXml<AdvanceBlockParameterDescription>(onode);
                if (this.Outputs.ContainsKey(bpd.Id))
                    this.ThrowDuplicatedIdentifierException(onode, bpd.Id);
                else
                    this.Inputs.Add(bpd.Id, bpd);
                // use the shared type object instead of an individual type
                if (sharedTypes.ContainsKey(bpd.Type.TypeVariableName))
                {
                    bpd.Type = sharedTypes[bpd.Type.TypeVariableName];
                }
                typeParams.Add(bpd.Type);
            }

            while (typeParams.Count > 0)
            {
                AdvanceType at = typeParams[0];
                typeParams.RemoveAt(0);
                if (at.Kind == TypeKind.VARIABLE_TYPE && at.TypeVariable == null)
                    this.TypeVariables.TryGetValue(at.TypeVariableName, out at.TypeVariable);
                else if (at.Kind == TypeKind.PARAMETRIC_TYPE)
                {
                    for (int i = 0; i < at.TypeArguments.Count; i++)
                    {
                        AdvanceType ta = at.TypeArguments[i];
                        if (ta.Kind == TypeKind.VARIABLE_TYPE)
                        {
                            AdvanceType sv;
                            if (!sharedTypes.TryGetValue(ta.TypeVariableName, out sv))
                                this.ThrowMissingTypeVariableException(source, ta.TypeVariableName);
                            at.TypeArguments[i] = sv;
                        }
                    }
                    typeParams.AddRange(at.TypeArguments);
                }
            }
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
            AddAttribute(node, "tooltip", this.Tooltip);
            AddListAttribute(node, "keywords", this.Keywords);
            AddAttribute(node, "category", this.Category);
            foreach (AdvanceTypeVariable item in this.TypeVariables.Values)
                item.AddToXML("type-variable", node);
            foreach (AdvanceBlockParameterDescription item in this.Inputs.Values)
                item.AddToXML("input", node);
            foreach (AdvanceBlockParameterDescription item in this.Outputs.Values)
                item.AddToXML("output", node);
        }

        /// <summary>
        /// Use this block reference to derive a custom block based on the variable argument counts.
        /// </summary>
        /// <param name="?">original reference</param>
        /// <returns>derived description</returns>
        public AdvanceBlockDescription Derive(AdvanceBlockReference reference)
        {
            if (reference.Varargs.Count == 0)
                return this;

            AdvanceBlockDescription result = new AdvanceBlockDescription();
            result.Assign(this);
            result.Inputs = new Dictionary<string, AdvanceBlockParameterDescription>();
            foreach (KeyValuePair<string, AdvanceBlockParameterDescription> e in this.Inputs)
            {
                int count;
                if (reference.Varargs.TryGetValue(e.Key, out count))
                    for (int i = 1; i <= count; i++)
                    {
                        AdvanceBlockParameterDescription value = e.Value.Copy();
                        value.Type = e.Value.Type; // keep shared type
                        value.Id = e.Key + i;
                        value.Varargs = false;
                        result.Inputs.Add(value.Id, value);
                    }
                else
                    result.Inputs.Add(e.Key, e.Value);
            }

            return result;
        }
    }
}
