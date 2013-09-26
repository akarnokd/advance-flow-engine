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
using System.IO;
using System.Xml;
using AdvanceAPIClient.Core;

namespace AdvanceAPIClient.Communication
{
    public abstract class AbstractResponse : IDisposable
    {
        private XmlReader xmlReader = null;

        protected abstract Stream GetResponseStream();

        public XmlNode Content()
        {
            return XmlReadWrite.ReadFromStream(this.GetResponseStream());
        }

        public XmlNode NextNode()
        {
            if (this.xmlReader == null)
                this.xmlReader = XmlReader.Create(this.GetResponseStream());
            return XmlReadWrite.ReadFregment(this.xmlReader);
        }

        public abstract void Close();

        public void Dispose()
        {
            try
            {
                this.Close();
                if (this.xmlReader != null)
                    this.xmlReader.Close();
            }
            catch { }

        }
    }
}
