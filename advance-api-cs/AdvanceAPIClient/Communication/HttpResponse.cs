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
using System.Net;

using AdvanceAPIClient.Core;

namespace AdvanceAPIClient.Communication
{
    public class HttpResponse : AbstractResponse, IDisposable
    {
        private HttpWebResponse httpResp;

        public HttpResponse(HttpWebResponse httpResp)
        {
            this.httpResp = httpResp;
        }

        protected override Stream GetResponseStream()
        {
            return this.httpResp.GetResponseStream();
        }

        public override void Close()
        {
            httpResp.Close();
        }
    }
}
