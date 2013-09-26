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
using AdvanceAPIClient.Classes;
using AdvanceAPIClient.Communication;

namespace AdvanceAPIClient.Implementation
{

    /// <summary>
    /// API for interacting with the ADVANCE Flow Engine remotely via Http communication.
    /// </summary>
    public class HttpRemoteEngineControl : AbstractRemoteEngineControl
    {
        /// <summary>
        /// 
        /// </summary>
        /// <param name="remote">remote datastore Uri</param>
        /// <param name="auth">authentication object</param>
        public HttpRemoteEngineControl(Uri uri, HttpAuthentication auth)
        {
            this.Init(uri, auth);
        }

        /// <summary>
        /// Constructor for Basic authentication
        /// </summary>
        /// <param name="uri">Remote access</param>
        /// <param name="username"></param>
        /// <param name="password"></param>
        public HttpRemoteEngineControl(Uri uri, string username, char[] password)
        {
            this.Init(uri, new HttpAuthentication(username, password));
        }

        /// <summary>
        /// Constructor for basic authentication with checking
        /// </summary>
        /// <param name="uri">Remote access</param>
        /// <param name="certFile">Certification file path</param>
        /// <param name="keyFile">Key file path</param>

        public HttpRemoteEngineControl(Uri uri, string certFile, string keyFile)
        {
            if (uri.Scheme == Uri.UriSchemeHttps && certFile != null && keyFile != null)
            {
                HttpAuthentication auth = new HttpAuthentication(certFile, keyFile);
                this.Init(uri, auth);
            }
            else
                throw new ArgumentException("Use HTTPS remote address for certificate autentication!");
        }

        /// <summary>
        /// Constructor with existing communicator
        /// </summary>
        /// <param name="communicator">Communicator</param>
        public HttpRemoteEngineControl(HttpCommunicator communicator)
        {
            this.communicator = communicator;
            datastore = new HttpRemoteDataStore(this.communicator);
        }

        private void Init(Uri uri, HttpAuthentication auth)
        {
            this.communicator = new HttpCommunicator(uri, auth);
            datastore = new HttpRemoteDataStore(this.communicator);
        }

    }
}
