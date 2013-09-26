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

namespace AdvanceAPIClient.Core
{
    public class AdvanceControlException : Exception
    {
        private const long serialVersionUID = -8958246930488574550L;

        /// <summary>
        /// Default constructor. 
        /// </summary>
        public AdvanceControlException() { }

        /// <summary>
        /// Message constructor
        /// </summary>
        /// <param name="message">Exception message</param>
        public AdvanceControlException(String message) : base(message) { }

        /// <summary>
        /// Exception constructor
        /// </summary>
        /// <param name="e">Exception clause</param>
        public AdvanceControlException(Exception e) : base("", e) { }


        /// <summary>
        /// Message and exception constructor
        /// </summary>
        /// <param name="message">Exception message</param>
        /// <param name="cause">Exception clause</param>

    }
}
