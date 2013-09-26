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
    public class CompilationErrorBase : XmlReadWrite
    {
        public string TypeName;

        public CompilationErrorBase() : base() { this.tagName = "error"; }

        /// <summary>
        /// Returns error message in text form
        /// </summary>
        /// <returns>Message text</returns>
        public override string ToString()
        {
            return "Compillation error type=" + this.TypeName;
        }

        /// <summary>
        /// Fill object data from xml node
        /// </summary>
        /// <param name="source">Source Xml node</param>
        protected override void LoadFromXmlNode(XmlNode source)
        {
            this.TypeName = GetAttribute(source, "type");
            if (this is IHasBinding)
                (this as IHasBinding).Binding = CreateFromXml<AdvanceBlockBind>(GetChildNode(source, "binding"));
        }

        /// <summary>
        /// Fills object's xml
        /// </summary>
        /// <param name="node">Empty node for the object with name "tagname"</param>
        protected override void FillXmlElement(XmlElement node)
        {
            AddAttribute(node, "type", this.TypeName);
            AddAttribute(node, "message", this);
            if (this is IHasBinding)
                (this as IHasBinding).Binding.AddToXML("binding", node);
        }

        public static CompilationErrorBase CreateErrorFromXml(XmlNode source)
        {
            switch (GetAttribute(source, "type").ToLower())
            {
                case CombinedTypeError.TYPE_NAME: return CreateFromXml<CombinedTypeError>(source);
                case ConcreteVsParametricTypeError.TYPE_NAME: return CreateFromXml<ConcreteVsParametricTypeError>(source);
                case ConstantOutputError.TYPE_NAME: return CreateFromXml<ConstantOutputError>(source);
                case DestinationToCompositeInputError.TYPE_NAME: return CreateFromXml<DestinationToCompositeInputError>(source);
                case DestinationToCompositeOutputError.TYPE_NAME: return CreateFromXml<DestinationToCompositeOutputError>(source);
                case DestinationToOutputError.TYPE_NAME: return CreateFromXml<DestinationToOutputError>(source);
                case IncompatibleBaseTypesError.TYPE_NAME: return CreateFromXml<IncompatibleBaseTypesError>(source);
                case IncompatibleTypesError.TYPE_NAME: return CreateFromXml<IncompatibleTypesError>(source);
                case MissingBlockError.TYPE_NAME: return CreateFromXml<MissingBlockError>(source);
                case MissingDestinationError.TYPE_NAME: return CreateFromXml<MissingDestinationError>(source);
                case MissingDestinationPortError.TYPE_NAME: return CreateFromXml<MissingDestinationPortError>(source);
                case MissingSourceError.TYPE_NAME: return CreateFromXml<MissingSourceError>(source);
                case MissingSourcePortError.TYPE_NAME: return CreateFromXml<MissingSourcePortError>(source);
                case SourceToCompositeInputError.TYPE_NAME: return CreateFromXml<SourceToCompositeInputError>(source);
                case SourceToCompositeOutputError.TYPE_NAME: return CreateFromXml<SourceToCompositeOutputError>(source);
                case SourceToInputBindingError.TYPE_NAME: return CreateFromXml<SourceToInputBindingError>(source);
                case TypeArgumentCountError.TYPE_NAME: return CreateFromXml<TypeArgumentCountError>(source);
                case MissingVarargsError.TYPE_NAME: return CreateFromXml<MissingVarargsError>(source);
                case NonVarargsError.TYPE_NAME: return CreateFromXml<NonVarargsError>(source);
                case UnsetVarargsError.TYPE_NAME: return CreateFromXml<UnsetVarargsError>(source);
                case UnsetInputError.TYPE_NAME: return CreateFromXml<UnsetInputError>(source);
                default: return CreateFromXml<CompilationErrorBase>(source);
            }
        }

    }
}
