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

using System.Reactive.Disposables;
using System.Reactive.Linq;
using System.Reactive.Subjects;
using System.Reactive.Concurrency;


using AdvanceAPIClient.Core;

namespace AdvanceAPIClient.Communication
{
    /// <summary>
    /// Base interface for XML communication.
    /// </summary>
    public abstract class AbstractXmlCommunicator
    {
        protected enum ResponseType { NONE, FULL_XML, XML_FRAGMENT }

        public bool Debug
        {
            protected get { return this.debugDir != null; }
            set { this.debugDir = value ? Log.GetWorkDir("XmlCommunicator") : null; }
        }
        private string debugDir;

        private void CreateDebugFile(string name, XmlNode content)
        {
            if (debugDir != null)
                XmlReadWrite.WriteToStream(true, content, File.Create(Utils.GetFilenameForCreate(this.debugDir, name, "xml")));
        }

        /// <summary>
        /// Abstract function to send request to server
        /// </summary>
        /// <param name="request">Request Xml</param>
        /// <returns>Response stream</returns>
        protected abstract AbstractResponse DoCommunication(XmlNode request);

        /// <summary>
        /// Syncron function call 
        /// </summary>
        /// <param name="request">Request in Xml form or null if no input parameter</param>
        /// <returns>Response in Xml form</returns>
        /// <exception cref="AdvanceIOException">if network connection failed</exception>
        public XmlNode Query(XmlNode request)
        {
            return this.Communication(ResponseType.FULL_XML, request);
        }

        /// <summary>
        /// Syncron function call without response
        /// </summary>
        /// <param name="request">Request in Xml form</param>
        /// <exception cref="AdvanceIOException">if network connection failed</exception>
        public void Send(XmlNode request)
        {
            this.Communication(ResponseType.NONE, request);
        }

        /// <summary>
        /// Receive XML responses continuously.
        /// Returnes a complete XML, but its parsing is done per each of the root's children,
        /// e.g., an XML of &lt;a>&lt;b/>&lt;b/>&lt/a> will produce two b elements.
        ///	Observers should call ub\nthrow a CancellationException to stop the reception
        /// </summary>
        /// <param name="request">Request in Xml form or null if no input parameter</param>
        /// <param name="scheduler">Reactive scheduler</param>
        /// <returns>Response in Xml form</returns>
        /// <exception cref="AdvanceIOException">if network connection failed</exception>
        public IObservable<T> Receive<T>(XmlNode request, IScheduler scheduler)
        {
            return new XmlNodeTracker<T>(() => this.DoCommunication(request), scheduler);
        }
        
        private XmlNode Communication(ResponseType respType, XmlNode request)
        {
            XmlDocument debugDoc = new XmlDocument();
            try
            {
                if (this.Debug)
                {
                    XmlNode node = (request is XmlDocument) ? (request as XmlDocument).DocumentElement : request;
                    XmlReadWrite.AddNode(debugDoc, "http_comm_" + node.Name);
                    debugDoc.DocumentElement.AppendChild(debugDoc.ImportNode(node, true));
                }
                AbstractResponse commResponse = this.DoCommunication(request);
                XmlNode resp = null;
                if (respType != ResponseType.NONE)
                {
                    resp = commResponse.Content();
                    if (this.Debug && resp != null)
                    {
                        XmlElement node = XmlReadWrite.AddNode(debugDoc.DocumentElement, "response");
                        node.AppendChild(debugDoc.ImportNode(resp, true));
                    }
                    this.CreateDebugFile(debugDoc.DocumentElement.Name, debugDoc);
                    return resp;
                }
                else
                    return null;
            }
            catch (Exception e)
            {
                if (this.Debug)
                {
                    XmlElement err = XmlReadWrite.AddTextNode(debugDoc.DocumentElement, "error", e.Message);
                    Exception innerEx = e.InnerException;
                    while (innerEx != null)
                    {
                        XmlReadWrite.AddTextNode(err, "inner-exception", innerEx.Message);
                        innerEx = innerEx.InnerException;
                    }
                    XmlReadWrite.AddTextNode(err, "stack_trace", e.StackTrace);
                    this.CreateDebugFile(debugDoc.DocumentElement.Name, debugDoc);
                }
                throw e;
            }
        }

     }

}
