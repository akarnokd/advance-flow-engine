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
    /// XML name record.
    /// </summary>
    public class XName : IXComparable<XName>
    {
        /// <summary>
        /// Name
        /// </summary>
        public string Name;
        /// <summary>
        /// Associated semantics
        /// </summary>
        public IXSemantics Semantics;
        /// <summary>
        /// Other aliases for this capability under the given semantics
        /// </summary>
        public HashSet<string> Aliases;

        public TypeRelation CompareTo(XName o)
        {
            if (this.Name.Equals(o.Name)
                || (o.Aliases != null && o.Aliases.Contains(this.Name))
                || (this.Aliases != null && this.Aliases.Contains(o.Name)))
            {
                if (this.Semantics != null && o.Semantics != null)
                {
                    return Semantics.CompareTo(o.Semantics);
                }
                else
                    return TypeRelation.EQUAL;
            }
            else
                return TypeRelation.NONE;
        }

        /// <summary>
        /// Assign values from another instance.
        /// </summary>
        /// <param name="XName"></param>
        public void Assign(XName other)
        {
            this.Name = other.Name;
            this.Semantics = other.Semantics;
            this.Aliases = other.Aliases == null ? null : new HashSet<string>(other.Aliases);
        }


        public override string ToString()
        {
            StringBuilder b = new StringBuilder();
            b.Append("XName { name = ").Append(this.Name);
            if (this.Semantics != null)
                b.Append(", semantics = ").Append(this.Semantics);
            if (this.Aliases != null && this.Aliases.Count > 0)
                b.Append(", aliases = ").Append(this.Aliases);
            b.Append(" }");
            return b.ToString();
        }

        public override bool Equals(Object obj)
        {
            if (obj is XName)
            {
                XName other = (XName)obj;
                return (this.Name.Equals(other.Name) && this.Semantics.Equals(other.Semantics));
            }
            return false;
        }

        public override int GetHashCode()
        {
            return (this.Name + "|" + this.Semantics).GetHashCode();
        }
    }
}
