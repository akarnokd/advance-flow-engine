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
using System.Security;
using System.Net;
using System.IO;
using System.Security.Cryptography;
using System.Security.Cryptography.X509Certificates;
using Org.BouncyCastle.Crypto;
using System.Security.AccessControl;

using Org.BouncyCastle.Crypto.Parameters;
using Org.BouncyCastle.OpenSsl;
using Org.BouncyCastle.Security;

using AdvanceAPIClient.Core;
using AdvanceAPIClient.Interfaces;
using System.Net.Security;

namespace AdvanceAPIClient.Communication
{

    /// <summary>
    /// Record to hold authentication information for basic or certificate based HTTP(s) connection.
    /// </summary>
    public class HttpAuthentication : IPassword
    {
        /// <summary>
        /// Authentication type
        /// </summary>
        public AdvanceLoginType Type;
        /// <summary>
        /// User name for basic authentication or the key alias for certificate credentials.
        /// </summary>
        private string userName;

        /// <summary>
        /// User's password.
        /// </summary>
        public char[] Password
        {
            get { return (this.password == null) ? null : (char[])this.password.Clone(); }
            set { this.password = (value == null) ? null : (char[])value.Clone(); }
        }
        private char[] password;

        private X509Certificate2 clientCertification;

        public HttpAuthentication(AdvanceLoginType type)
        {
            this.Type = type;
            ServicePointManager.ServerCertificateValidationCallback = ValidateCertificate;
        }

        public HttpAuthentication(string userName, char[] password) : this(AdvanceLoginType.BASIC)
        {
            this.userName = userName;
            this.password = password;
        }

        public HttpAuthentication(string certFile, string keyFile)
        {
            this.clientCertification = new X509Certificate2(certFile);

            RsaPrivateCrtKeyParameters keyPair;
            using (var reader = File.OpenText(keyFile))
            {
                keyPair = (RsaPrivateCrtKeyParameters)(new PemReader(reader).ReadObject());
            }

            var temp = new RSACryptoServiceProvider();
            temp.ImportParameters(DotNetUtilities.ToRSAParameters(keyPair));

            // This step is required as somehow the "temp" by itself doesn't work.
            RSACryptoServiceProvider rcsp = new RSACryptoServiceProvider(new CspParameters(1, "Microsoft Strong Cryptographic Provider",
                new Guid().ToString(),
                new CryptoKeySecurity(), null));

            rcsp.ImportCspBlob(temp.ExportCspBlob(true));

            this.clientCertification.PrivateKey = rcsp;
        }

        public HttpWebRequest GetHttpRequest(Uri uri, bool hasContent)
        {
            HttpWebRequest req = (HttpWebRequest)WebRequest.Create(uri);
            req.AuthenticationLevel = System.Net.Security.AuthenticationLevel.MutualAuthRequested;
            req.ContentType = "text/xml;charset=utf-8";
            req.Method = hasContent ? WebRequestMethods.Http.Post : WebRequestMethods.Http.Get;
            switch (this.Type)
            {
                case AdvanceLoginType.BASIC:
                    string authInfo = this.userName + ":" + new string(this.password);
                    authInfo = Convert.ToBase64String(Encoding.Default.GetBytes(authInfo));
                    req.Headers["Authorization"] = "Basic " + authInfo;
                    break;
                case AdvanceLoginType.CERTIFICATE:
                    req.ClientCertificates.Add(clientCertification);
                    break;
                default: break;
            }
            return req;
        }

        private static bool ValidateCertificate(object sender, X509Certificate cert, X509Chain chain, System.Net.Security.SslPolicyErrors sslPolicyErrors)
        {
            return true;
        }

    }
}
