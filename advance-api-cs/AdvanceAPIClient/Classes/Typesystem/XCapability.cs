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
using AdvanceAPIClient.Interfaces;

namespace AdvanceAPIClient.Classes.Typesystem
{
    ///<summary>
    /// XML capability description, basically the class field description.
    /// Attributes and inner elements share the same description
    /// and probably won't overlap in terms of a name.
    /// </summary>
    public class XCapability : IXComparable<XCapability>
    {
        /// <summary>
        /// element name
        /// </summary>
        public XName Name;
        /// <summary>
        /// element cardinality
        /// </summary>
        public XCardinality Cardinality;
        /// <summary>
        /// element's type. 
        /// </summary>
        public XValueType ValueType;
        /// <summary>
        /// element's complex type if ValueType = COMPLEX
        /// </summary>
        public XType ComplexType;

        public TypeRelation CompareTo(XCapability o)
        {
            return this.CompareTo(o, new XTypeRecursionTracker());
        }

        public TypeRelation CompareTo(XCapability o, XTypeRecursionTracker memory)
        {
            int equal = 0;
            int ext = 0;
            int sup = 0;
            switch (this.Name.CompareTo(o.Name))
            {
                case TypeRelation.EQUAL:
                    equal++;
                    break;
                case TypeRelation.EXTENDS:
                    ext++;
                    break;
                case TypeRelation.SUPER:
                    sup++;
                    break;
                case TypeRelation.NONE:
                    return TypeRelation.NONE;
                default:
                    break;
            }
            if (this.ComplexType != null && o.ComplexType != null)
            {
                switch (this.ComplexType.CompareTo(o.ComplexType, memory))
                {
                    case TypeRelation.EQUAL:
                        equal++;
                        break;
                    case TypeRelation.EXTENDS:
                        ext++;
                        break;
                    case TypeRelation.SUPER:
                        sup++;
                        break;
                    case TypeRelation.NONE:
                        return TypeRelation.NONE;
                    default:
                        break;
                }
            }
            else if ((this.ComplexType == null) != (this.ComplexType == null))
                return TypeRelation.NONE;
            if (this.ValueType != XValueType.COMPLEX && this.ValueType == o.ValueType)
                equal++;
            switch (Utils.CompareCardinality(this.Cardinality, o.Cardinality))
            {
                case TypeRelation.EQUAL:
                    equal++;
                    break;
                case TypeRelation.EXTENDS:
                    ext++;
                    break;
                case TypeRelation.SUPER:
                    sup++;
                    break;
                case TypeRelation.NONE:
                    return TypeRelation.NONE;
                default:
                    break;
            }

            int all = equal + ext + sup;
            if (all == equal)
            {
                return TypeRelation.EQUAL;
            }
            if (all == equal + ext)
            {
                return TypeRelation.EXTENDS;
            }
            if (all == equal + sup)
            {
                return TypeRelation.SUPER;
            }
            // mixed content, inconclusive
            return TypeRelation.NONE;
        }

        public override string ToString()
        {
            StringBuilder b = new StringBuilder();
            this.ToStringPretty("", b, new HashSet<XType>());
            return b.ToString();
        }

        /// <summary>
        /// Pretty print the contents of this XCapability.
        /// </summary>
        /// <param name="indent">current indentation</param>
        /// <param name="b">output buffer</param>
        /// <param name="memory">types already expressed won't be detailed again</param>
        public void ToStringPretty(string indent, StringBuilder b, HashSet<XType> memory)
        {
            b.Append(indent).AppendLine("XCapability {");
            b.Append(indent).AppendLine("  name = " + this.Name + ",");
            b.Append(indent).AppendLine("  cardinality = " + this.Cardinality + ",");
            if (this.ValueType != XValueType.COMPLEX)
                b.Append(indent).Append("  valueType = " + this.ValueType);
            if (this.ComplexType != null)
            {
                if (!memory.Contains(this.ComplexType))
                {
                    memory.Add(this.ComplexType);
                    b.Append(indent).AppendLine("  complexType = ");
                    this.ComplexType.ToStringPretty(indent + "    ", b, memory);
                    memory.Remove(this.ComplexType);
                }
                else
                    b.Append(indent).AppendLine("  complexType = XType ...");
            }
            if (this.ComplexType == null && this.ValueType == XValueType.COMPLEX)
                b.Append(indent).AppendLine("  type could not be determined");
            b.Append(indent).AppendLine("}");
        }
    }
}
