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

namespace AdvanceAPIClient.Classes.Model
{
    /// <summary>
    /// composite block description for the flow description of {@code flow-description.xsd}.
    /// </summary>
    public class AdvanceCompositeBlock : XmlReadWrite
    {

        /// <summary>
        /// Unique identifier of this block among the current level of blocks
        /// </summary>
        public String Id;
        /// <summary>
        /// user-entered documentation of this composite block.
        /// </summary>

        public string Documentation;
        /// <summary>
        /// parent block of this composite block.
        /// </summary>
        public AdvanceCompositeBlock Parent;
        /// <summary>
        /// user-entered keywords for easier finding of this block
        /// </summary>
        public List<string> Keywords = new List<string>();
        /// <summary>
        /// optional boundary-parameter of this composite block which lets other internal or external blocks bind to this block.
        /// </summary>
        public ICollection<AdvanceCompositeBlockParameterDescription> Inputs { get { return this.inputs.Values; } }
        protected Dictionary<string, AdvanceCompositeBlockParameterDescription> inputs = new Dictionary<string, AdvanceCompositeBlockParameterDescription>();
        /// <summary>
        /// optional boundary parameter of this composite block which lets other internal or external blocks to bind to this block.
        /// </summary>
        public ICollection<AdvanceCompositeBlockParameterDescription> Outputs { get { return this.outputs.Values; } }

        protected Dictionary<string, AdvanceCompositeBlockParameterDescription> outputs = new Dictionary<string, AdvanceCompositeBlockParameterDescription>();
        /// <summary>
        /// optional sub-elements of this block
        /// </summary>
        public Dictionary<string, AdvanceBlockReference> Blocks = new Dictionary<string, AdvanceBlockReference>();
        /// <summary>
        /// Optional composite inner block
        /// </summary>
        public Dictionary<string, AdvanceCompositeBlock> Composites = new Dictionary<string, AdvanceCompositeBlock>();
        /// <summary>
        /// Optional constant inner block
        /// </summary>
        public Dictionary<string, AdvanceConstantBlock> Constants = new Dictionary<string, AdvanceConstantBlock>();
        /// <summary>
        /// Binding definition of internal blocks and/or boundary parameters. 
        /// You may bind output of the blocks to many input parameters.
        /// </summary>
        public List<AdvanceBlockBind> Bindings = new List<AdvanceBlockBind>();
        /// <summary>
        /// Visual properties for the Flow Editor
        /// </summary>
        public AdvanceBlockVisuals Visuals = new AdvanceBlockVisuals();
        /// <summary>
        /// Definitions of various generic type parameters.
        /// </summary>
        protected Dictionary<string, AdvanceTypeVariable> TypeVariables = new Dictionary<string, AdvanceTypeVariable>();
        /// <summary>
        /// Shared type variables
        /// </summary>
        protected Dictionary<string, AdvanceType> sharedTypeVariables = new Dictionary<string, AdvanceType>();

        public AdvanceCompositeBlock() : base() { }

        /// <summary>
        /// Fill object data from xml node
        /// </summary>
        /// <param name="source">Source Xml</param>
        protected override void LoadFromXmlNode(XmlNode source)
        {
            this.Id = GetAttribute(source, "id");
            this.Documentation = GetAttribute(source, "documentation", null);
            this.Keywords = GetListAttribute(source, "keywords");
            this.Visuals = CreateFromXml<AdvanceBlockVisuals>(source);
            HashSet<string> usedIds = new HashSet<string>();
            foreach (XmlNode n in GetChildren(source, "*"))
            {
                switch (n.Name)
                {
                    case "block":
                        AdvanceBlockReference b = CreateFromXml<AdvanceBlockReference>(n);
                        b.parent = this;
                        if (usedIds.Contains(b.Id))
                            this.ThrowDuplicatedIdentifierException(n, b.Id);
                        else
                        {
                            this.Blocks.Add(b.Id, b);
                            usedIds.Add(b.Id);
                        }
                        break;
                    case "composite-block":
                        AdvanceCompositeBlock cb = CreateFromXml<AdvanceCompositeBlock>(n);
                        cb.Parent = this;
                        if (usedIds.Contains(cb.Id))
                            this.ThrowDuplicatedIdentifierException(n, cb.Id);
                        else
                        {
                            this.Composites.Add(cb.Id, cb);
                            usedIds.Add(cb.Id);
                        }
                        break;
                    case "constant":
                        AdvanceConstantBlock c = CreateFromXml<AdvanceConstantBlock>(n);
                        if (usedIds.Contains(c.Id))
                            this.ThrowDuplicatedIdentifierException(n, c.Id);
                        else
                        {
                            this.Constants.Add(c.Id, c);
                            usedIds.Add(c.Id);
                        }
                        break;
                    case "bind":
                        AdvanceBlockBind bb = CreateFromXml<AdvanceBlockBind>(n);
                        bb.Parent = this;
                        this.Bindings.Add(bb);
                        break;
                    case "type-variable":
                        AdvanceTypeVariable tv = CreateFromXml<AdvanceTypeVariable>(n);
                        if (!this.AddTypeVariable(tv))
                            this.ThrowDuplicatedIdentifierException(n, tv.Name);
                        break;
                    case "input":
                        AdvanceCompositeBlockParameterDescription inp = CreateFromXml<AdvanceCompositeBlockParameterDescription>(n);
                        if (!this.AddInput(inp))
                            this.ThrowDuplicatedIdentifierException(n, inp.Id);
                        break;
                    case "output":
                        AdvanceCompositeBlockParameterDescription outp = CreateFromXml<AdvanceCompositeBlockParameterDescription>(n);
                        if (!this.AddOutput(outp))
                            this.ThrowDuplicatedIdentifierException(n, outp.Id);
                        break;
                }
            }
            this.LinkTypeVariables();
        }

        /// <summary>
        /// Fills object's xml
        /// </summary>
        /// <param name="node">Empty node for the object with name "tagname"</param>
        protected override void FillXmlElement(XmlElement node)
        {
            AddAttribute(node, "id", this.Id);
            AddAttribute(node, "documentation", this.Documentation);
            AddListAttribute(node, "keywords", this.Keywords);
            this.Visuals.AddToElement(node);
            foreach (AdvanceTypeVariable tv in this.TypeVariables.Values)
                tv.AddToXML("type-variable", node);
            foreach (AdvanceCompositeBlockParameterDescription item in this.inputs.Values)
                item.AddToXML("input", node);
            foreach (AdvanceCompositeBlockParameterDescription item in this.outputs.Values)
                item.AddToXML("output", node);
            foreach (AdvanceBlockReference item in this.Blocks.Values)
                item.AddToXML("block", node);
            foreach (AdvanceCompositeBlock item in this.Composites.Values)
                item.AddToXML("composite-block", node);
            foreach (AdvanceConstantBlock item in this.Constants.Values)
                item.AddToXML("constant", node);
            foreach (AdvanceBlockBind item in this.Bindings)
                item.AddToXML("bind", node);
        }

        /// <summary>
        ///  Check if the given binding exists
        /// </summary>
        /// <param name="srcBlock"> source block or "" if it is the composite</param>
        /// <param name="srcParam">source parameter</param>
        /// <param name="dstBlock">destination block or "" if it is the composite</param>
        /// <param name="dstParam">destination parameter.</param>
        /// <returns>binding is present</returns>
        public bool hasBinding(String srcBlock, String srcParam, String dstBlock, String dstParam)
        {
            foreach (AdvanceBlockBind bb in this.Bindings)
            {
                if (bb.SourceBlock.Equals(srcBlock)
                        && bb.SourceParameter.Equals(srcParam)
                        && bb.DestinationBlock.Equals(dstBlock)
                        && bb.DestinationParameter.Equals(dstParam))
                    return true;
            }
            return false;
        }

        /// <summary>
        /// Removes an input parameter with given identifier.
        /// </summary>
        /// <param name="name">input name</param>
        public void RemoveInput(string name)
        {
            this.inputs.Remove(name);
        }

        /// <summary>
        /// Removes an output parameter with given identifier.
        /// </summary>
        /// <param name="name">output name</param>
        public void RemoveOutput(String name)
        {
            this.outputs.Remove(name);
        }

        /// <summary>
        /// Test if the given input parameter exists in this composite
        /// </summary>
        /// <param name="name">input name</param>
        /// <returns>true if exists</returns>
        public bool HasInput(String name)
        {
            return this.inputs.ContainsKey(name);
        }

        /// <summary>
        /// Returns the given input parameter
        /// </summary>
        /// <param name="name">input name</param>
        /// <returns>Input parameter description or null if not present</returns>
        public AdvanceCompositeBlockParameterDescription GetInput(string name)
        {
            AdvanceCompositeBlockParameterDescription ret;
            return this.inputs.TryGetValue(name, out ret) ? ret : null;
        }

        /// <summary>
        /// Test if the given input parameter exists in this composite
        /// </summary>
        /// <param name="name">input name</param>
        /// <returns>true if exists</returns>
        public bool HasOutput(String name)
        {
            return this.outputs.ContainsKey(name);
        }

        /// <summary>
        /// Returns the given output parameter
        /// </summary>
        /// <param name="name">output name</param>
        /// <returns>Output parameter description or null if not present</returns>

        public AdvanceCompositeBlockParameterDescription GetOutput(string name)
        {
            AdvanceCompositeBlockParameterDescription ret;
            return this.outputs.TryGetValue(name, out ret) ? ret : null;
        }

        /// <summary>
        /// Add parameter description as an input parameter
        /// </summary>
        /// <param name="p">description</param>
        /// <returns>True if parameter is new</returns>
        public bool AddInput(AdvanceCompositeBlockParameterDescription p)
        {
            if (this.inputs.ContainsKey(p.Id))
                return false;
            else
            {
                this.inputs.Add(p.Id, p);
                this.LinkParameterType(p);
                return true;
            }
        }

        /// <summary>
        /// Add parameter description as an input parameter
        /// </summary>
        /// <param name="p">description</param>
        /// <returns>True if parameter is new</returns>
        public bool AddOutput(AdvanceCompositeBlockParameterDescription p)
        {
            if (this.outputs.ContainsKey(p.Id))
                return false;
            else
            {
                this.outputs.Add(p.Id, p);
                this.LinkParameterType(p);
                return true;
            }
        }

        /// <summary>
        /// Adds a type variable to this composite
        /// </summary>
        /// <param name="tv">type variable</param>
        /// <returns>true if it was new</returns>
        public bool AddTypeVariable(AdvanceTypeVariable tv)
        {
            if (this.TypeVariables.ContainsKey(tv.Name))
                return false;
            else
            {
                AdvanceType t = new AdvanceType();
                t.TypeVariable = tv;
                t.TypeVariableName = tv.Name;
                this.AddSharedTypeVariable(tv.Name, t);

                return true;
            }
        }
        /**
         * Adds a shared type variable and type.
         * @param name the variable name
         * @param t the type, if null, an IllegalArgumentException is thrown
         */
        protected void AddSharedTypeVariable(String name, AdvanceType t)
        {
            if (t == null)
                this.ThrowException("Illegal argument AdvanceType " + name + " is null");
            if (t.TypeVariable == null)
            {
                t.TypeVariable = new AdvanceTypeVariable();
                t.TypeVariable.Name = name;
            }

            this.TypeVariables.Add(name, t.TypeVariable);
            this.sharedTypeVariables.Add(name, t);
        }

        /// <summary>
        /// Establish a direct link between type variables referencing other type variables.
        /// </summary>
        protected void LinkTypeVariables()
        {
            HashSet<AdvanceType> visited = new HashSet<AdvanceType>();
            List<List<AdvanceType>> queue = new List<List<AdvanceType>>();
            foreach (AdvanceTypeVariable tv in this.TypeVariables.Values)
                queue.Add(tv.Bounds);
            while (queue.Count > 0)
            {
                List<AdvanceType> bounds = queue[0];
                queue.RemoveAt(0);
                for (int i = 0; i < bounds.Count; i++)
                {
                    AdvanceType t = bounds[i];
                    switch (t.Kind)
                    {
                        case TypeKind.VARIABLE_TYPE:
                            AdvanceType st;
                            if (this.sharedTypeVariables.TryGetValue(t.TypeVariableName, out st))
                                bounds[i] = st;
                            else
                                this.ThrowMissingTypeVariableException(null, t.TypeVariableName);
                            break;
                        case TypeKind.PARAMETRIC_TYPE:
                            if (!visited.Contains(t))
                            {
                                visited.Add(t);
                                queue.Add(t.TypeArguments);
                            }
                            break;
                    }
                }
            }
        }
        /// <summary>
        /// Remove unused type variables
        /// </summary>
        public void RemoveUnusedTypeVariables()
        {
            HashSet<AdvanceType> visited = new HashSet<AdvanceType>();
            HashSet<string> used = new HashSet<string>();
            List<AdvanceType> queue = new List<AdvanceType>();
            foreach (AdvanceCompositeBlockParameterDescription p in this.inputs.Values)
                queue.Add(p.Type);
            foreach (AdvanceCompositeBlockParameterDescription p in this.outputs.Values)
                queue.Add(p.Type);
            while (queue.Count > 0)
            {
                AdvanceType t = queue[0];
                queue.RemoveAt(0);
                switch (t.Kind)
                {
                    case TypeKind.VARIABLE_TYPE: used.Add(t.TypeVariableName); break;
                    case TypeKind.PARAMETRIC_TYPE:
                        if (!visited.Contains(t))
                        {
                            visited.Add(t);
                            queue.AddRange(t.TypeArguments);
                        }
                        break;
                }
            }
            // TODO remove unused
        }

        /// <summary>
        /// Link variable types in the parameter to the common types
        /// </summary>
        /// <param name="p"></param>
        protected void LinkParameterType(AdvanceCompositeBlockParameterDescription p)
        {
            switch (p.Type.Kind)
            {
                case TypeKind.VARIABLE_TYPE:
                    AdvanceType t;
                    if (this.sharedTypeVariables.TryGetValue(p.Type.TypeVariableName, out t))
                        p.Type = t;
                    else
                        this.AddSharedTypeVariable(p.Type.TypeVariableName, p.Type);
                    break;
                case TypeKind.PARAMETRIC_TYPE:
                    HashSet<AdvanceType> visited = new HashSet<AdvanceType>();
                    List<List<AdvanceType>> queue = new List<List<AdvanceType>>();
                    queue.Add(p.Type.TypeArguments);
                    while (queue.Count > 0)
                    {
                        List<AdvanceType> args = queue[0];
                        queue.RemoveAt(0);
                        for (int i = 0; i < args.Count; i++)
                        {
                            AdvanceType at = args[i];
                            switch (at.Kind)
                            {
                                case TypeKind.VARIABLE_TYPE:
                                    AdvanceType st;
                                    if (this.sharedTypeVariables.TryGetValue(at.TypeVariableName, out st))
                                        args[i] = st;
                                    else
                                        this.AddSharedTypeVariable(at.TypeVariableName, at);
                                    break;
                                case TypeKind.PARAMETRIC_TYPE:
                                    if (!visited.Contains(at))
                                    {
                                        visited.Add(at);
                                        queue.Add(at.TypeArguments);
                                    }
                                    break;
                            }
                        }
                    }
                    break;
            }
        }
    }
}

