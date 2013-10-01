﻿/*
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
    /// Wire's destination is bound to an output port of a composite block (not the parent block).
    /// </summary>
    public class DestinationToCompositeOutputError : CompilationErrorBase, IHasBinding
    {
        public const string TYPE_NAME = "destinationtocompositeoutputerror";
        /// <summary>
        /// wire identifier
        /// </summary>
        public AdvanceBlockBind Binding { get { return this.binding; } set { this.binding = value; } }
        private AdvanceBlockBind binding;
  
        /// <summary>
        /// Empty constructor
        /// </summary>
        public DestinationToCompositeOutputError() : base() { }
 
        /// <summary>
        /// Returns error message in text form
        /// </summary>
        /// <returns>Message text</returns>
        public override string ToString()
        {
		return "Wire " + this.binding.Id + " destination is bound to an output port of a composite block (" + this.binding.DestinationBlock + ", " + this.binding.DestinationParameter + ")";
        }

     }

}