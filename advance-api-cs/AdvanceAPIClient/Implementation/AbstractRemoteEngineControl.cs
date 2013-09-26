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
using AdvanceAPIClient.Classes.Model;
using AdvanceAPIClient.Classes.Runtime;
using AdvanceAPIClient.Communication;

using System.Reactive.Concurrency;

namespace AdvanceAPIClient.Implementation
{
    public class AbstractRemoteEngineControl : IAdvanceEngineControl
    {
        protected AbstractXmlCommunicator communicator;
        /// <summary>
        /// datastore interface
        /// </summary>
        public IDataStore Datastore { get { return this.datastore; } }
        protected IDataStore datastore;

        public bool Debug { set { this.communicator.Debug = value; } }

        /// <summary>
        /// Retrieve user settings.
        /// </summary>
        /// <returns>token representing the connection</returns>
        /// <exception cref="AdvanceIOException">if network connection failed</exception>
        /// <exception cref="AdvanceXMLException">On Xml parsing error</exception>
        /// <exception cref="AdvanceControlException">if  username or password is incorrect</exception>
        public AdvanceUser GetUser()
        {
            XmlNode resp = this.communicator.Query(XmlReadWrite.CreateFunctionRequest("get-user"));
            return XmlReadWrite.CreateFromXml<AdvanceUser>(resp);
        }

        /// <summary>
        /// Retrieve a list of supported block types.
        /// </summary>
        /// <returns>list of supported block types</returns>
        /// <exception cref="AdvanceIOException">if network connection failed</exception>
        /// <exception cref="AdvanceXMLException">On Xml parsing error</exception>
        /// <exception cref="AdvanceControlException">if user is not allowed to list blocks.</exception>
        public List<AdvanceBlockDescription> QueryBlocks()
        {
            XmlNode resp = this.communicator.Query(XmlReadWrite.CreateFunctionRequest("query-blocks"));
            return XmlReadWrite.CreateListFromXml<AdvanceBlockDescription>(resp, "blocks", "block-description");
        }

        /// <summary>
        /// Engine version information
        /// </summary>
        /// <returns>Engine version information</returns>
        /// <exception cref="AdvanceIOException">if network connection failed</exception>
        /// <exception cref="AdvanceXMLException">On Xml parsing error</exception>
        /// <exception cref="AdvanceControlException">if user is not authenticated.</exception>
        public AdvanceEngineVersion QueryVersion()
        {
            XmlNode resp = this.communicator.Query(XmlReadWrite.CreateFunctionRequest("query-version"));
            return XmlReadWrite.CreateFromXml<AdvanceEngineVersion>(resp);
        }

        /// <summary>
        /// Query the list of schemas known by the engine.
        /// </summary>
        /// <returns>list and value of the known schemas</returns>
        /// <exception cref="AdvanceIOException">if network connection fails</exception>
        /// <exception cref="AdvanceXMLException">On Xml parsing error</exception>
        /// <exception cref="AdvanceControlException">if user is not allowed to list schemas.</exception>
        public List<AdvanceSchemaRegistryEntry> QuerySchemas()
        {
            XmlNode resp = this.communicator.Query(XmlReadWrite.CreateFunctionRequest("query-schemas"));
            return XmlReadWrite.CreateListFromXml<AdvanceSchemaRegistryEntry>(resp, "schemas", "schema");
        }

        /// <summary>
        /// Query specified schema.
        /// </summary>
        /// <param name="name">schema's filename (without path)</param>
        /// <returns>schema as XNElement</returns>
        /// <exception cref="AdvanceIOException">if network connection failed</exception>
        /// <exception cref="AdvanceXMLException">On Xml parsing error</exception>
        /// <exception cref="AdvanceControlException">if user is not allowed to lis schemas.</exception>
        public AdvanceSchemaRegistryEntry QuerySchema(string name)
        {
            XmlNode resp = this.communicator.Query(XmlReadWrite.CreateFunctionRequest("query-schema", "name", name));
            return XmlReadWrite.CreateFromXml<AdvanceSchemaRegistryEntry>(resp);
        }

        /// <summary>
        /// Add or modify a schema in the flow engine.
        /// </summary>
        /// <param name="name">the schema's filename (without path)</param>
        /// <param name="schema">schema content</param>
        /// <exception cref="AdvanceIOException">if network connection failed</exception>
        /// <exception cref="AdvanceControlException">if user is not allowed to add new schemas.</exception>
        public void UpdateSchema(string name, XmlElement schema)
        {
            XmlDocument qDoc = XmlReadWrite.CreateFunctionRequest("update-schema", "name", name);
            qDoc.DocumentElement.AppendChild(qDoc.ImportNode(schema, true));
            this.communicator.Send(qDoc);
        }

        /// <summary>
        /// Delete the specified schema.
        /// </summary>
        /// <param name="name">schema's filename (without path)</param>
        /// <exception cref="AdvanceIOException">if network connection failed</exception>
        /// <exception cref="AdvanceControlException">if user is not allowed to delete schemas.</exception>
        public void DeleteSchema(string name)
        {
            this.communicator.Send(XmlReadWrite.CreateFunctionRequest("delete-schema", "name", name));
        }

        /// <summary>
        /// Delete a key entry from a keystore.
        /// </summary>
        /// <param name="keyStore">key store name</param>
        /// <param name="keyAlias">key alias</param>
        /// <exception cref="AdvanceIOException">if network connection failed</exception>
        /// <exception cref="AdvanceControlException">if user is not allowed to delete key.</exception>
        public void DeleteKeyEntry(string keyStore, string keyAlias)
        {
            Dictionary<string, string> parameters = new Dictionary<string, string>();
            parameters.Add("keystore", keyStore);
            parameters.Add("keyalias", keyAlias);
            this.communicator.Send(XmlReadWrite.CreateFunctionRequest("delete-key-entry", null, parameters));
        }

        /// <summary>
        /// Generate a new key with given properties.
        /// </summary>
        /// <param name="key">key generation properties</param>
        /// <exception cref="AdvanceIOException">if network connection failed</exception>
        /// <exception cref="AdvanceControlException">if user is not allowed to generate keys.</exception>
        public void GenerateKey(AdvanceGenerateKey key)
        {
            this.communicator.Send(XmlReadWrite.CreateFunctionRequest("generate-key", key));
        }

        /// <summary>
        /// Export a certificate from a designated key store.
        /// </summary>
        /// <param name="request">represents the key store and key alias to export</param>
        /// <returns>certificate in textual CER format.</returns>
        /// <exception cref="AdvanceIOException">if network connection failed</exception>
        /// <exception cref="AdvanceXMLException">On Xml parsing error</exception>
        /// <exception cref="AdvanceControlException">if user is not allowed to export.</exception>
        public string ExportCertificate(AdvanceKeyStoreExport request)
        {
            XmlNode resp = this.communicator.Query(XmlReadWrite.CreateFunctionRequest("export-certificate", request));
            return XmlReadWrite.GetContent(resp, null);
        }

        /// <summary>
        /// Export a private from a designated key store.
        /// </summary>
        /// <param name="request">represents the key store and key alias to export</param>
        /// <returns>private key in textual PEM format.</returns>
        /// <exception cref="AdvanceIOException">if network connection failed</exception>
        /// <exception cref="AdvanceXMLException">On Xml parsing error</exception>
        /// <exception cref="AdvanceControlException">if user is not allowed to export.</exception>
        public string ExportPrivateKey(AdvanceKeyStoreExport request)
        {
            XmlNode resp = this.communicator.Query(XmlReadWrite.CreateFunctionRequest("export-private-key", request));
            return XmlReadWrite.GetContent(resp, null);
        }

        /// <summary>
        /// Import a certificate into a designated key store.
        /// </summary>
        /// <param name="request">represents the key store and key alias to import</param>
        /// <param name="data">certificate in textual CER format.</param>
        /// <exception cref="AdvanceIOException">if network connection failed</exception>
        /// <exception cref="AdvanceControlException">if user is not allowed to export.</exception>
        public void ImportCertificate(AdvanceKeyStoreExport request, string data)
        {
            this.communicator.Send(XmlReadWrite.CreateFunctionRequest("import-certificate", data));
        }

        /// <summary>
        /// Import a private key into a designated key store.
        /// </summary>
        /// <param name="request">represents the key store and key alias to import</param>
        /// <param name="data">ckey in textual PEM format.</param>
        /// <exception cref="AdvanceIOException">if network connection failed</exception>
        /// <exception cref="AdvanceControlException">if user is not allowed to export.</exception>
        public void ImportPrivateKey(AdvanceKeyStoreExport request, string keyData, string certData)
        {
            XmlDocument req = XmlReadWrite.CreateFunctionRequest("import-private-key", request);
            XmlReadWrite.AddTextNode(req.DocumentElement, "private-key", keyData);
            XmlReadWrite.AddTextNode(req.DocumentElement, "certificate", certData);
            this.communicator.Send(req);
        }

        /// <summary>
        /// Export a signing request of the given private key.
        /// </summary>
        /// <param name="request">represents the key store and key alias to export</param>
        /// <param name="data">signing request in textual format.</param>
        /// <exception cref="AdvanceIOException">if network connection failed</exception>
        /// <exception cref="AdvanceXMLException">On Xml parsing error</exception>
        /// <exception cref="AdvanceControlException">if user is not allowed to export.</exception>
        public string ExportSigningRequest(AdvanceKeyStoreExport request)
        {
            XmlNode resp = this.communicator.Query(XmlReadWrite.CreateFunctionRequest("export-signing-request", request));
            return XmlReadWrite.GetContent(resp, null);
        }

        /// <summary>
        /// Import a signing response into the designated key store.
        /// </summary>
        /// <param name="request">represents the key store and key alias to export</param>
        /// <param name="data">the signing response in textual format.</param>
        /// <exception cref="AdvanceIOException">if network connection failed</exception>
        /// <exception cref="AdvanceControlException">if user is not allowed to export.</exception>
        public void ImportSigningResponse(AdvanceKeyStoreExport request, string data)
        {
            XmlDocument req = XmlReadWrite.CreateFunctionRequest("import-signing-response", request);
            XmlReadWrite.AddContent(req.DocumentElement, data);
            this.communicator.Send(req);
        }

        /// <summary>
        /// Test the JDBC data source connection.
        /// </summary>
        /// <param name="dataSourceName">data source identifier</param>
        /// <returns>error message or an empty string if successful</returns>
        /// <exception cref="AdvanceIOException">if network connection failed</exception>
        /// <exception cref="AdvanceXMLException">On Xml parsing error</exception>
        /// <exception cref="AdvanceControlException">if user is not allowed to test connection.</exception>
        public string TestJDBCDataSource(string dataSourceName)
        {
            XmlNode resp = this.communicator.Query(XmlReadWrite.CreateFunctionRequest("test-jdbc-data-source", "data-source-source", dataSourceName));
            return XmlReadWrite.GetContent(resp, null);
        }

        /// <summary>
        /// Test a JMS endpoint configuration.
        /// </summary>
        /// <param name="jmsName">identifier of the JMS endpoint to test.</param>
        /// <returns>error message or an empty string if successful</returns>
        /// <exception cref="AdvanceIOException">if network connection failed</exception>
        /// <exception cref="AdvanceControlException">if user is not allowed to test connection.</exception>
        public string TestJMSEndpoint(string jmsName)
        {
            XmlNode resp = this.communicator.Query(XmlReadWrite.CreateFunctionRequest("test-jms-endpoint", "jms-name", jmsName));
            return XmlReadWrite.GetContent(resp, null);
        }

        /// <summary>
        /// Test FTP data source.
        /// </summary>
        /// <param name="ftpName">data source identifier</param>
        /// <returns>error message or an empty string if successful</returns>
        /// <exception cref="AdvanceIOException">if network connection failed</exception>
        /// <exception cref="AdvanceXMLException">On Xml parsing error</exception>
        /// <exception cref="AdvanceControlException">if user is not allowed to test connection.</exception>
        public string TestFTPDataSource(string ftpName)
        {
            XmlNode resp = this.communicator.Query(XmlReadWrite.CreateFunctionRequest("test-ftp-endpoint", "ftp-name", ftpName));
            return XmlReadWrite.GetContent(resp, null);
        }

        /// <summary>
        /// List keys of the given keystore.
        /// </summary>
        /// <param name="keyStore">keystore name</param>
        /// <returns>list of key entries</returns>
        /// <exception cref="AdvanceIOException">if network connection failed</exception>
        /// <exception cref="AdvanceXMLException">On Xml parsing error</exception>
        /// <exception cref="AdvanceControlException">if user is not allowed to list keys.</exception>
        public List<AdvanceKeyEntry> QueryKeys(string keyStore)
        {
            XmlNode resp = this.communicator.Query(XmlReadWrite.CreateFunctionRequest("query-keys", "keystore", keyStore));
            return XmlReadWrite.CreateListFromXml<AdvanceKeyEntry>(resp, "keys", "keyentry");
        }
 
        /// <summary>
        /// Stop a realm's execution.
        /// </summary>
        /// <param name="name">realm's name</param>
        /// <param name="byUser">user who is stopping the realm</param>
        /// <exception cref="AdvanceIOException">if network connection failed</exception>
        /// <exception cref="AdvanceControlException">if user is not allowed to list keys.</exception>
        public void StopRealm(string name, string byUser)
        {
            Dictionary<string, string> parameters = new Dictionary<string, string>();
            parameters.Add("name", name);
            parameters.Add("by-user", byUser);
            this.communicator.Send(XmlReadWrite.CreateFunctionRequest("stop-realm", null, parameters));
        }

        /// <summary>
        /// Start a realm's execution.
        /// </summary>
        /// <param name="name">realm's name</param>
        /// <param name="byUser">user who is starting the realm</param>
        /// <exception cref="AdvanceIOException">if network connection failed</exception>
        /// <exception cref="AdvanceControlException">if user is not allowed to start the realm or other problems arise.</exception>
        public void StartRealm(string name, string byUser)
        {
            Dictionary<string, string> parameters = new Dictionary<string, string>();
            parameters.Add("name", name);
            parameters.Add("by-user", byUser);
            this.communicator.Send(XmlReadWrite.CreateFunctionRequest("start-realm", null, parameters));
        }

        /// <summary>
        /// Retrieve the current flow, if any, from the given realm.
        /// </summary>
        /// <param name="realm">the target realm</param>
        /// <returns>composite block representing the flow, or a completely empty composite block</returns>
        /// <exception cref="AdvanceIOException">if network connection failed</exception>
        /// <exception cref="AdvanceXMLException">On Xml parsing error</exception>
        /// <exception cref="AdvanceControlException">if user is not allowed to list keys.</exception>
        /// <exception cref="AdvanceControlException">if user is not allowed to query the flow.</exception>
        public AdvanceCompositeBlock QueryFlow(string realm)
        {
            XmlNode resp = this.communicator.Query(XmlReadWrite.CreateFunctionRequest("query-flow", "realm", realm));
            AdvanceFlow flow = XmlReadWrite.CreateFromXml<AdvanceFlow>(resp);
            return flow.Content;
        }

        /// <summary>
        /// Update a flow in a the given realm.
        /// </summary>
        /// <param name="realm">target realm</param>
        /// <param name="block">Flow content to upload</param>
        /// <param name="byUser">user who modifies the realm</param>
        /// <exception cref="AdvanceIOException">if network connection failed</exception>
        /// <exception cref="AdvanceControlException">if user is not allowed to update a flow.</exception>
        public void UpdateFlow(string realm, AdvanceCompositeBlock block, string byUser)
        {
            Dictionary<string, object> attrs = new Dictionary<string, object>();
            attrs.Add("realm", realm);
            attrs.Add("by-user", byUser);
            List<XmlReadWrite> pars = new List<XmlReadWrite>();
            pars.Add(new AdvanceFlow(block));
            XmlDocument req = XmlReadWrite.CreateFunctionRequest("update-flow", pars, attrs);
            this.communicator.Send(req);
        }

        /// <summary>
        /// Verify the given flow.
        /// </summary>
        /// <param name="block">flow content to verify</param>
        /// <returns>results of the compilation in terms of errors and computed types of wires</returns>
        /// <exception cref="AdvanceIOException">if network connection failed</exception>
        /// <exception cref="AdvanceXMLException">On Xml parsing error</exception>
        /// <exception cref="AdvanceControlException">if user is not allowed to update a flow.</exception>
        public AdvanceCompilationResult VerifyFlow(AdvanceCompositeBlock block)
        {
            XmlNode resp = this.communicator.Query(XmlReadWrite.CreateFunctionRequest("verify-flow", new AdvanceFlow(block)));
            return XmlReadWrite.CreateFromXml<AdvanceCompilationResult>(resp);
        }

        /// <summary>
        /// Ask for the observable sequence of block diagnostic messages for the given block 
        /// within the given realm.
        /// </summary>
        /// <param name="realm">realm</param>
        /// <param name="blockId">block unique identifier (as in the flow)</param>
        /// <returns>observable for the diagnostic messages</returns>
        /// <exception cref="AdvanceIOException">if network connection failed</exception>
        /// <exception cref="AdvanceXMLException">On Xml parsing error</exception>
        /// <exception cref="AdvanceControlException">if user is not allowed to update a flow.</exception>
        public IObservable<BlockDiagnostic> DebugBlock(string realm, string blockId)
        {
            Dictionary<string, object> attrs = new Dictionary<string, object>();
            attrs.Add("realm", realm);
            attrs.Add("port-id", blockId);
            return this.communicator.Receive<BlockDiagnostic>(XmlReadWrite.CreateFunctionRequest("debug-block", null, attrs), new NewThreadScheduler());
        }

        /// <summary>
        /// Ask for the observable sequence of port messages of the given port/block/realm.
        /// </summary>
        /// <param name="realm">realm</param>
        /// <param name="blockId">block unique identifier (as in the flow)</param>
        /// <param name="port">port name</param>
        /// <returns>observable of parameter messages</returns>
        /// <exception cref="AdvanceIOException">if network connection failed</exception>
        /// <exception cref="AdvanceXMLException">On Xml parsing error</exception>
        /// <exception cref="AdvanceControlException">if user is not allowed to debug in the realm or referenced parameter is missing.</exception>
        public IObservable<PortDiagnostic> DebugParameter(string realm, string blockId, string port)
        {
            Dictionary<string, object> attrs = new Dictionary<string, object>();
            attrs.Add("realm", realm);
            attrs.Add("block-id", realm);
            attrs.Add("port", port);
            return this.communicator.Receive<PortDiagnostic>(XmlReadWrite.CreateFunctionRequest("debug-parameter", null, attrs), new NewThreadScheduler());
        }

        /// <summary>
        /// Inject a value into the given  realm/block/input port.
        /// </summary>
        /// <param name="realm">realm</param>
        /// <param name="blockId">block unique identifier (as in the flow)</param>
        /// <param name="port">port name</param>
        /// <param name="value">as XNElement to inject</param>
        /// <exception cref="AdvanceIOException">if network connection failed</exception>
        /// <exception cref="AdvanceControlException">if user is not allowed to debug in the realm or referenced parameter is missing.</exception>
        public void InjectValue(string realm, string blockId, string port, XmlElement value)
        {
            Dictionary<string, object> attrs = new Dictionary<string, object>();
            attrs.Add("realm", realm);
            attrs.Add("block-id", blockId);
            attrs.Add("port", port);
            XmlDocument req = XmlReadWrite.CreateFunctionRequest("inject-value", null, attrs);
            req.DocumentElement.AppendChild(req.ImportNode(value, true));
            this.communicator.Send(req);
        }

        /// <summary>
        /// Shut down the flow engine.
        /// </summary>
        /// <param name="port">port name</param>
        /// <param name="value">as XNElement to inject</param>
        /// <exception cref="AdvanceIOException">if network connection failed</exception>
        /// <exception cref="AdvanceControlException">if user is not allowed to shut down the engine.</exception>
        public void Shutdown()
        {
            this.communicator.Send(XmlReadWrite.CreateFunctionRequest("shutdown"));
        }

        /// <summary>
        /// Return the compilation result of the given realm.
        /// This function may be used to find out why a server-side verification failed
        /// or to query the compilation result after a flow upload and realm startup.
        /// </summary>
        /// <param name="realm">realm to query</param>
        /// <returns>compilation result or null if no compilation was performed since the last startup of the engine</returns>
        /// <exception cref="AdvanceIOException">if network connection failed</exception>
        /// <exception cref="AdvanceControlException">if user is not allowed to read a flow.</exception>
        public AdvanceCompilationResult QueryCompilationResult(string realm)
        {
            XmlNode resp = this.communicator.Query(XmlReadWrite.CreateFunctionRequest("query-compilation-result", "realm", realm));
            return XmlReadWrite.CreateFromXml<AdvanceCompilationResult>(resp);
        }

        /// <summary>
        /// Retrieves the list of available global input and output ports of the given realm's flow description.
        /// </summary>
        /// <param name="realm">target realm</param>
        /// <returns>list of ports</returns>
        /// <exception cref="AdvanceIOException">if network connection failed</exception>
        /// <exception cref="AdvanceControlException">if user is not allowed to read a flow.</exception>
        public List<AdvancePortSpecification> QueryPorts(string realm)
        {
            XmlNode resp = this.communicator.Query(XmlReadWrite.CreateFunctionRequest("query-ports", "realm", realm));
            return XmlReadWrite.CreateListFromXml<AdvancePortSpecification>(resp, "ports", "port");
        }

        /// <summary>
        /// ends a sequence of values to the same or multiple ports in a batch.
        /// </summary>
        /// <param name="realm">target realm</param>
        /// <param name="portValues">equence of port id and value XML pairs</param>
        /// <exception cref="AdvanceIOException">if network connection failed</exception>
        /// <exception cref="AdvanceControlException">if user is not has no right to access the realm/ports</exception>
        public void SendPort(string realm, Dictionary<string, XmlNode> portValues)
        {
            XmlDocument req = XmlReadWrite.CreateFunctionRequest("send-port", "realm", realm);
            foreach (KeyValuePair<string, XmlNode> pv in portValues)
            {
                XmlElement node = XmlReadWrite.AddAttributeNode(req.DocumentElement, "entry", "port", pv.Key);
                node.AppendChild(req.ImportNode(pv.Value, true));
            }
            this.communicator.Send(req);
        }

        /// <summary>
        /// Observe the output of the specified port in the given realm.
        /// </summary>
        /// <param name="realm">target realm</param>
        /// <param name="portId">port identifier of an input port returned by the queryPorts() method.</param>
        /// <returns>observable which will send out the values</returns>
        /// <exception cref="AdvanceIOException">if network connection failed</exception>
        /// <exception cref="AdvanceControlException">if user is not has no right to access the realm/ports</exception>
        public IObservable<XmlNode> ReceivePort(string realm, string portId)
        {
            Dictionary<string, object> attrs = new Dictionary<string, object>();
            attrs.Add("realm", realm);
            attrs.Add("port-id", portId);

            return this.communicator.Receive<XmlNode>(XmlReadWrite.CreateFunctionRequest("receve-port", null, attrs), new NewThreadScheduler());
        }
    }


}
