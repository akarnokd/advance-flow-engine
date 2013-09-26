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
using AdvanceAPIClient.Interfaces;

namespace AdvanceAPIClient.Classes.Model
{
    /// <summary>
    /// Type parameter bound definition for a generic type parameter
    /// </summary>
    public class AdvanceType : XmlReadWrite, IType
    {
        /// <summary>
        /// Counter used for type variables
        /// </summary>
        public int Id;
        /// <summary>
        /// Reference to another type parameter within the same set of declarations
        /// </summary>
        public string TypeVariableName;
        /// <summary>
        /// actual type variable object
        /// </summary>
        public AdvanceTypeVariable TypeVariable;
        /// <summary>
        /// Existing type definition schema 
        /// </summary>
        public Uri TypeURI;
        /// <summary>
        /// XML type of the target schema
        /// </summary>
        public XType Type;
        /// <summary>
        /// Type arguments used by the type
        /// </summary>
        /// <typeparam name="AdvanceType"></typeparam>
        /// <param name="?"></param>
        /// <returns></returns>
        public List<AdvanceType> TypeArguments = new List<AdvanceType>();

        public TypeKind Kind
        {
            get
            {
                if (this.TypeURI == null)
                    return TypeKind.VARIABLE_TYPE;
                else if (this.TypeArguments.Count == 0)
                    return TypeKind.CONCRETE_TYPE;
                else
                    return TypeKind.PARAMETRIC_TYPE;
            }
        }


        public AdvanceType() : base() { }

        public override string ToString()
        {
            if (this.TypeURI != null)
            {
                if (this.TypeArguments.Count == 0)
                    return this.TypeURI.ToString();
                else
                {
                    StringBuilder b = new StringBuilder(this.TypeURI.ToString());
                    b.Append("<");
                    int i = 0;
                    foreach (AdvanceType ta in this.TypeArguments)
                    {
                        if (i > 0) b.Append(", ");
                        b.Append(ta);
                        i++;
                    }
                    b.Append(">");
                    return b.ToString();
                }
            }
            else if (this.Id > 0)
                return string.Format("{0}[{1}]", this.TypeVariableName, this.Id);
            else
                return this.TypeVariableName;
        }

        /// <summary>
        /// Creates new instance of this type
        /// </summary>
        /// <returns>new instance of this type declaration</returns>
        public AdvanceType Copy()
        {
            AdvanceType result = new AdvanceType();
            result.Id = this.Id;
            result.TypeVariableName = this.TypeVariableName;
            result.TypeURI = this.TypeURI;
            result.Type = Type;
            if (this.TypeVariable != null)
                result.TypeVariable = this.TypeVariable.Copy();
            foreach (AdvanceType ta in this.TypeArguments)
            {
                result.TypeArguments.Add(ta.Copy());
            }
            return result;
        }

        /// <summary>
        /// Create a fresh type variable with given name.
        /// </summary>
        /// <param name="name">Type variable name</param>
        /// <returns>type variable</returns>
        public static AdvanceType Fresh(string name)
        {
            return CreateType(name);
        }

        /// <summary>
        /// Fill object data from xml node
        /// </summary>
        /// <param name="source"></param>
        protected override void LoadFromXmlNode(XmlNode source)
        {
            this.Id = GetIntAttribute(source, "type-id", 0);
            this.TypeVariableName = GetAttribute(source, "type-variable", null);
            this.TypeURI = GetUriAttribute(source, "type");
            if ((this.TypeURI != null) == (this.TypeVariableName != null))
                this.ThrowException("Only one of the type-variable and type arguments should be defined! " + source.InnerText);
            foreach (XmlNode ta in GetChildren(source, "type-argument"))
                this.TypeArguments.Add(CreateFromXml<AdvanceType>(ta));
        }

        /// <summary>
        /// Fills object's xml
        /// </summary>
        /// <param name="node">Empty node for the object with name "tagname"</param>
        protected override void FillXmlElement(XmlElement node)
        {
            if (this.Id > 0)
                AddAttribute(node, "type-id", this.Id);
            AddAttribute(node, "type-variable", this.TypeVariableName);
            if (this.TypeURI != null)
                AddAttribute(node, "type", this.TypeURI);
            foreach (AdvanceType at in this.TypeArguments)
                at.AddToXML("type-argument", node);
        }

        /// <summary>
        /// Creates a concrete (e.g., no type arguments or parametric type 
        /// with the supplied base type and type arguments.
        /// </summary>
        /// <remarks>
        /// {@code AdvanceType.type} is not resolved.
        /// </remarks>
        /// <example>
        /// CreateType(COLLECTION, CreateType(STRING))
        /// </example>
        /// <param name="baseType">Base type</param>
        /// <param name="types">type arguments</param>
        /// <returns>created type</returns>
        public static AdvanceType CreateType(Uri baseType, params AdvanceType[] types)
        {
            return CreateType(baseType, new List<AdvanceType>(types));
        }

        /// <summary>
        /// Creates an unbounded variable type.
        /// </summary>
        /// <param name="name">variable name</param>
        /// <returns>created type</returns>
        public static AdvanceType CreateType(string name)
        {
            AdvanceType t = new AdvanceType();
            t.TypeVariableName = name;
            t.TypeVariable = new AdvanceTypeVariable();
            t.TypeVariable.Name = name;
            return t;
        }

        /// <summary>
        /// Creates a bounded variable type.
        /// </summary>
        /// <param name="name">variable name</param>
        /// <param name="upperBound">True if type bounds are the upper bounds, e.g., {@code T super B1, B2, B3}</param>
        /// <param name="bound1">first bound</param>
        /// <param name="boundsRest">more bounds</param>
        /// <returns>created type</returns>
        public static AdvanceType CreateType(string name, bool upperBound, AdvanceType bound1, params AdvanceType[] boundsRest)
        {
            AdvanceType t = CreateType(name);
            t.TypeVariable.IsUpperBound = upperBound;
            t.TypeVariable.Bounds.Add(bound1);
            t.TypeVariable.Bounds.AddRange(boundsRest);
            return t;
        }

        /// <summary>
        /// Creates a bounded variable type.
        /// </summary>
        /// <param name="name">variable name</param>
        /// <param name="upperBound">True if type bounds are the upper bounds, e.g., {@code T super B1, B2, B3}</param>
        /// <param name="bounds">type bounds</param>
        /// <returns></returns>
        public static AdvanceType CreateType(string name, bool upperBound, ICollection<AdvanceType> bounds)
        {
            AdvanceType t = CreateType(name);
            t.TypeVariable.IsUpperBound = upperBound;
            t.TypeVariable.Bounds.AddRange(bounds);
            return t;
        }

        /// <summary>
        /// Creates a concrete (e.g., no type arguments or parametric type with the supplied base type and type arguments.
        /// </summary>
        /// <example>createType(COLLECTION, createType(STRING)</example>
        /// <param name="baseType">base type</param>
        /// <param name="types">type arguments.</param>
        /// <returns>created new type</returns>
        public static AdvanceType CreateType(Uri baseType, ICollection<AdvanceType> types)
        {
            AdvanceType t = new AdvanceType();
            t.TypeURI = baseType;
            t.TypeArguments = new List<AdvanceType>(types);
            return t;
        }

        /// <summary>
        /// Creates an {@code advance:type} type constructor XML
        /// </summary>
        /// <param name="type">type to convert to XML</param>
        /// <returns>Type Xml</returns>
        public static XmlNode CreateType(AdvanceType type)
        {
            XmlDocument doc = new XmlDocument();
            XmlElement node = doc.CreateElement("type");
            type.FillXmlElement(node);
            return node;
        }

        /// <summary>
        /// Converts the XML type declaration into an actual AdvanceType object.
        /// </summary>
        /// <param name="typeXml">type XML</param>
        /// <returns>Type object</returns>
        public static AdvanceType GetType(XmlNode typeXml)
        {
            return CreateFromXml<AdvanceType>(typeXml);
        }
    }
}
