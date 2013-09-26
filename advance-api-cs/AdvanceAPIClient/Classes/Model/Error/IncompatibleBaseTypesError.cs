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
    /// Wire binds two parametric types which have incompatible base types or number of arguments.
    /// </summary>
    public class IncompatibleBaseTypesError : CompilationErrorBase, IHasBinding, IHasTypes
    {
        public const string TYPE_NAME = "incompatiblebasetypeserror";
        /// <summary>
        /// wire identifier
        /// </summary>
        public AdvanceBlockBind Binding { get { return this.binding; } set { this.binding = value; } }
        private AdvanceBlockBind binding;
        /// <summary>
        /// Left side of the binding
        /// </summary>
        public AdvanceType Left;
        /// <summary>
        /// Right side of the binding
        /// </summary>
        public AdvanceType Right;
        /// <summary>
        /// sequence of associated types.
        /// </summary>
        public IList<AdvanceType> Types { get { return new AdvanceType[] { this.Left, this.Right }; } }

        /// <summary>
        /// Empty constructor
        /// </summary>
        public IncompatibleBaseTypesError() : base() { }

        /// <summary>
        /// Constructor.
        /// wire binds two parametric types with different type argument count
        /// </summary>
        /// <param name="binding">actual binding causing the problem</param>
        /// <param name="left">left side of binding</param>
        /// <param name="right">right side of  binding</param>
        public IncompatibleBaseTypesError(AdvanceBlockBind binding, AdvanceType left, AdvanceType right)
        {
            this.binding = binding;
            this.Left = left;
            this.Right = right;
        }

 
        /// <summary>
        /// Returns error message in text form
        /// </summary>
        /// <returns>Message text</returns>
        public override string ToString()
        {
            return "Incompatible base types of wire " + this.binding.Id + ": " + this.Left + " vs. " + this.Right;
        }

        /// <summary>
        /// Fill object data from xml node
        /// </summary>
        /// <param name="source"></param>
        protected override void LoadFromXmlNode(XmlNode source)
        {
            base.LoadFromXmlNode(source);
            this.Left = CreateFromXml<AdvanceType>(GetChildNode(source, "left-type"));
            this.Right = CreateFromXml<AdvanceType>(GetChildNode(source, "right-type"));
        }

        /// <summary>
        /// Fills object's xml
        /// </summary>
        /// <param name="node">Empty node for the object with name "tagname"</param>
        protected override void FillXmlElement(XmlElement node)
        {
            base.FillXmlElement(node);
            this.Left.AddToXML("left-type", node);
            this.Right.AddToXML("right-type", node);
        }
    }

}
