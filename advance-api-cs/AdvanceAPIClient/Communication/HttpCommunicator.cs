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
using System.Reactive.Concurrency;

namespace AdvanceAPIClient.Communication
{
    public class HttpCommunicator : AbstractXmlCommunicator
    {
        private Uri uri;
        private HttpAuthentication authentication;

        public HttpCommunicator(Uri uri, HttpAuthentication authentication)
        {
            this.uri = uri;
            this.authentication = authentication;
        }

        protected override AbstractResponse DoCommunication(XmlNode request)
        {
            try
            {
                HttpWebRequest httpReq = this.authentication.GetHttpRequest(this.uri, request != null);
                if (request != null)
                {
                    Stream reqStream = httpReq.GetRequestStream();
                    XmlReadWrite.WriteToStream(true, request, reqStream);
                    HttpWebResponse httpResp = (HttpWebResponse)httpReq.GetResponse();
                    if (httpResp.StatusCode < HttpStatusCode.BadRequest)
                        return new HttpResponse(httpResp);
                    else
                        throw new AdvanceIOException(string.Format("Response with {0} ({1}) Http status code", httpResp.StatusCode.ToString(), (int)httpResp.StatusCode));
                }
                else
                    throw new AdvanceIOException("Request generation failed");
            }
            catch (Exception e)
            {
                throw new AdvanceIOException(this.uri + " communication failed", e);
            }
        }
    }
}
