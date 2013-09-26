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

namespace AdvanceAPIClient.Classes.Typesystem
{
    /// <summary>
    /// Tracks recursion for XType objects for comparison.
    /// </summary>
    public class XTypeRecursionTracker
    {
        /// <summary>
        /// First path of XTypes.
        /// </summary>
        List<XType> xs = new List<XType>();
        /// <summary>
        /// Second path of XTypes.
        /// </summary>
        List<XType> ys = new List<XType>();

        /// <summary>
        /// Enter the first path.
        /// </summary>
        /// <param name="type">Type</param>
        public void EnterFirst(XType type)
        {
            xs.Add(type);
        }

        /// <summary>
        /// Enter the second path.
        /// </summary>
        /// <param name="type">Type</param>
        public void EnterSecond(XType type)
        {
            ys.Add(type);
        }

        /// <summary>
        /// Remove first
        /// </summary>
        public void LeaveFirst()
        {
            xs.RemoveAt(xs.Count - 1);
        }

        /// <summary>
        /// Remove second
        /// </summary>
        public void LeaveSecond()
        {
            xs.RemoveAt(ys.Count - 1);
        }

        /// <summary>
        /// Check recursive index on first
        /// </summary>
        public int IndexFirst(XType type)
        {
            int idx = xs.LastIndexOf(type);
            if (idx < 0)
                return -1;
            else
                return xs.Count - idx - 1;
        }

        /// <summary>
        /// Check recursive index on first
        /// </summary>
        public int IndexSecond(XType type)
        {
            int idx = ys.LastIndexOf(type);
            if (idx < 0)
                return -1;
            else
                return ys.Count - idx - 1;
        }
    }
}
