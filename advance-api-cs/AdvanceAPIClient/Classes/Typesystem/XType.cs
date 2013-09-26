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

    /// <summary>
    /// Definition of an XML type: basically a root element or an element with complex type.
    /// </summary>
    public class XType : IXComparable<XType>
    {
        /// <summary>
        /// Capability set of the type
        /// </summary>
        public List<XCapability> Capabilities = new List<XCapability>();

        /// <summary>
        /// Compare
        /// </summary>
        /// <param name="o">Other instance</param>
        /// <returns></returns>
        public TypeRelation CompareTo(XType o)
        {
            return this.CompareTo(o, new XTypeRecursionTracker());
        }

        /// <summary>
        /// Create a copy of this XType object
        /// </summary>
        /// <returns></returns>
        public XType Copy()
        {
            XType result = new XType();
            result.Capabilities.AddRange(Capabilities);
            return result;
        }

        /// <summary>
        /// Perform type comparison by using the given memory to avoid infinite recursion.
        /// </summary>
        /// <param name="o">type to check against</param>
        /// <param name="memory">memory to keep track the traversed types</param>
        /// <returns>relation</returns>
        public TypeRelation CompareTo(XType o, XTypeRecursionTracker memory)
        {
            memory.EnterFirst(this);
            memory.EnterSecond(o);
            int equal = 0;
            int ext = 0;
            int sup = 0;
        primary:
            foreach (XCapability c0 in this.Capabilities)
            {
                if (c0.ComplexType != null)
                {
                    int fidx = memory.IndexFirst(c0.ComplexType);
                    // is this a recursive call?
                    if (fidx >= 0)
                    {
                        foreach (XCapability c1 in o.Capabilities)
                        {
                            if (c1.ComplexType != null)
                            {
                                int sidx = memory.IndexSecond(c1.ComplexType);
                                if (sidx == fidx)
                                {
                                    equal++;
                                    goto primary;
                                }
                            }
                        }
                        // we don't have any recursive type on the other side
                        goto primary;
                    }
                }
            inner:
                foreach (XCapability c1 in o.Capabilities)
                {
                    if (c0.Name.CompareTo(c1.Name) != TypeRelation.NONE)
                    {
                        switch (c0.CompareTo(c1, memory))
                        {
                            case TypeRelation.EQUAL:
                                equal++;
                                goto inner;
                            case TypeRelation.EXTENDS:
                                ext++;
                                goto inner;
                            case TypeRelation.SUPER:
                                sup++;
                                goto inner;
                            default:
                                break;
                        }
                    }
                }
            }
            memory.LeaveFirst();
            memory.LeaveSecond();
            // common
            int all = equal + ext + sup;
            if (all < Capabilities.Count && all < o.Capabilities.Count)
            {
                return TypeRelation.NONE;
            }
            int diff = Capabilities.Count - o.Capabilities.Count;

            if (all == equal)
                return Utils.CompareToTypeRelation(diff);
            else if (all == equal + ext && diff >= 0)
                return TypeRelation.EXTENDS;
            else if (all == equal + sup && diff <= 0)
                return TypeRelation.SUPER;

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
            b.Append(indent).Append("XType");
            b.Append(" [");
            if (this.Capabilities.Count > 0)
            {
                b.AppendLine();
                foreach (XCapability c in this.Capabilities)
                    c.ToStringPretty(indent + "  ", b, memory);
                b.Append(indent).Append("]");
            }
            else
            {
                b.Append("]");
            }
            b.AppendLine();
        }

        /// <summary>
        /// Computes the intersection type of {@code this} type and the {@code other} type where
        /// this extends intersect and other extends intersect. 
        /// <p>If this extends other then other is returned;</p> 
        /// <p>if other extends this, this is returned;</p>
        /// <p>otherwise, a common subset is computed and a new XType is returned.</p> 
        /// </summary>
        /// <param>other type</param>
        ///<returns> the intersection type</returns>
        public XType Intersection(XType other)
        {
            return this.Intersection(other, new HashSet<XType>());
        }

        /// <summary>
        /// Computes the intersection type of {@code this} type and the {@code other} type where
        /// this extends intersect and other extends intersect. 
        /// <p>If this extends other then other is returned;</p> 
        /// <p>if other extends this, this is returned;</p>
        /// <p>otherwise, a common subset is computed and a new XType is returned.</p> 
        /// </summary>
        /// <param>other type</param>
        /// <param name="memory">types already used</param>
        ///<returns> the intersection type</returns>
        public XType Intersection(XType other, HashSet<XType> memory)
        {
            TypeRelation rel = this.CompareTo(other);
            if (rel == TypeRelation.EXTENDS)
                return other;
            else if (rel == TypeRelation.SUPER || rel == TypeRelation.EQUAL)
                return this;
            XType ret = new XType();
            memory.Add(this);
            foreach (XCapability c0 in this.Capabilities)
            {
                if (c0.ComplexType == null || !memory.Contains(c0.ComplexType))
                {
                inner:
                    foreach (XCapability c1 in other.Capabilities)
                    {
                        // the same member?
                        if (c0.Name.CompareTo(c1.Name) != TypeRelation.NONE)
                        {
                            XCapability c2 = new XCapability();
                            c2.Name = c0.Name;
                            // both complex types?
                            if ((c0.ComplexType != null) && (c1.ComplexType != null))
                            {
                                c2.ComplexType = c0.ComplexType.Intersection(c1.ComplexType, memory);
                            }
                            else
                            {
                                if (c0.ValueType == c1.ValueType)
                                {
                                    c2.ValueType = c0.ValueType;
                                }
                                else
                                {
                                    c2.ComplexType = new XType(); // object
                                }
                            }
                            switch (Utils.CompareCardinality(c0.Cardinality, c1.Cardinality))
                            {
                                case TypeRelation.SUPER:
                                case TypeRelation.EQUAL:
                                    c2.Cardinality = c0.Cardinality;
                                    break;
                                case TypeRelation.EXTENDS:
                                    c2.Cardinality = c1.Cardinality;
                                    break;
                                default:
                                    break;
                            }
                            ret.Capabilities.Add(c2);
                            goto inner;
                        }
                    }
                }
            }
            memory.Remove(this);
            return ret;
        }

        /// <summary>
        /// Computes the union type of {@code this} and {@code other}, meaning {@code union extends this} and
        /// {@code union extends other}.
        ///  The union function may return null if no union type could be created due conflicting
        ///  primitive types.
        /// </summary>
        /// <param name="other">other type</param>
        /// <returns>union type </returns>
        public XType Union(XType other)
        {
            return Union(other, new HashSet<XType>());
        }

        /// <summary>
        /// Computes the union type of {@code this} and {@code other}, meaning {@code union extends this} and
        /// {@code union extends other}.
        /// The union function may return null if no union type could be created due conflicting
        /// primitive types.
        /// </summary>
        /// <param name="other">other type</param>
        /// <param name="memory">types already used</param>
        /// <returns>union type </returns>

        public XType Union(XType other, HashSet<XType> memory)
        {
            TypeRelation rel = this.CompareTo(other);
            if (rel == TypeRelation.EXTENDS || rel == TypeRelation.EQUAL)
                return this;
            else if (rel == TypeRelation.SUPER)
                return other;
            XType ret = new XType();
            memory.Add(this);
        outer:
            foreach (XCapability c0 in this.Capabilities)
            {
                if (c0.ComplexType == null || !memory.Contains(c0.ComplexType))
                {
                    foreach (XCapability c1 in other.Capabilities)
                    {
                        // the same member?
                        if (c0.Name.CompareTo(c1.Name) != TypeRelation.NONE)
                        {
                            XCapability c2 = new XCapability();
                            c2.Name = c0.Name;
                            // both complex types?
                            if ((c0.ComplexType != null) && (c1.ComplexType != null))
                            {
                                c2.ComplexType = c0.ComplexType.Union(c1.ComplexType, memory);
                                if (c2.ComplexType == null)
                                {
                                    return null; // can't union the types
                                }
                            }
                            else
                            {
                                if (c0.ValueType == c1.ValueType)
                                {
                                    c2.ValueType = c0.ValueType;
                                }
                                else
                                {
                                    return null; // can't union the types
                                }
                            }
                            switch (Utils.CompareCardinality(c0.Cardinality, c1.Cardinality))
                            {
                                case TypeRelation.EXTENDS:
                                case TypeRelation.EQUAL:
                                    c2.Cardinality = c0.Cardinality;
                                    break;
                                case TypeRelation.SUPER:
                                    c2.Cardinality = c1.Cardinality;
                                    break;
                                default:
                                    break;
                            }
                            ret.Capabilities.Add(c2);
                            goto outer;
                        }
                    }
                    // not found in c1
                    ret.Capabilities.Add(c0);
                }
            }
        outer2:
            foreach (XCapability c1 in other.Capabilities)
            {
                foreach (XCapability c0 in this.Capabilities)
                {
                    if (c0.ComplexType == null || !memory.Contains(c0.ComplexType))
                    {
                        if (c0.Name.CompareTo(c1.Name) != TypeRelation.NONE)
                        {
                            goto outer2;
                        }
                    }
                }
                ret.Capabilities.Add(c1);
            }
            memory.Remove(this);
            return ret;
        }
    }
}
