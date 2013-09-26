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

using AdvanceAPIClient.Classes;
using AdvanceAPIClient.Classes.Model;
using AdvanceAPIClient.Classes.Runtime;

namespace AdvanceAPIClient
{

    /// <summary>
    /// API for interacting with the ADVANCE Flow Engine remotely.
    /// </summary>
    public interface IAdvanceEngineControl
    {
        /// <summary>
        /// datastore interface
        /// </summary>
        /// <returns>datastore interface</returns>
        /// <exception cref="AdvanceIOException">if network connection failed</exception>
        /// <exception cref="AdvanceXMLException">On Xml parsing error</exception>
        /// <exception cref="AdvanceControlException">if user is not allowed to list keys.</exception>
        IDataStore Datastore { get; }

        /// <summary>
        /// Retrieve user settings.
        /// </summary>
        /// <returns>token representing the connection</returns>
        /// <exception cref="AdvanceIOException">if network connection failed</exception>
        /// <exception cref="AdvanceXMLException">On Xml parsing error</exception>
        /// <exception cref="AdvanceControlException">if  username or password is incorrect</exception>
        AdvanceUser GetUser();

        /// <summary>
        /// Retrieve a list of supported block types.
        /// </summary>
        /// <returns>list of supported block types</returns>
        /// <exception cref="AdvanceIOException">if network connection failed</exception>
        /// <exception cref="AdvanceXMLException">On Xml parsing error</exception>
        /// <exception cref="AdvanceControlException">if user is not allowed to list blocks.</exception>
        List<AdvanceBlockDescription> QueryBlocks();

        /// <summary>
        /// Engine version information
        /// </summary>
        /// <returns>Engine version information</returns>
        /// <exception cref="AdvanceIOException">if network connection failed</exception>
        /// <exception cref="AdvanceXMLException">On Xml parsing error</exception>
        /// <exception cref="AdvanceControlException">if user is not authenticated.</exception>
        AdvanceEngineVersion QueryVersion();

        /// <summary>
        /// Query the list of schemas known by the engine.
        /// </summary>
        /// <returns>list and value of the known schemas</returns>
        /// <exception cref="AdvanceIOException">if network connection fails</exception>
        /// <exception cref="AdvanceXMLException">On Xml parsing error</exception>
        /// <exception cref="AdvanceControlException">if user is not allowed to list schemas.</exception>
        List<AdvanceSchemaRegistryEntry> QuerySchemas();

        /// <summary>
        /// Query specified schema.
        /// </summary>
        /// <param name="name">schema's filename (without path)</param>
        /// <returns>schema as XNElement</returns>
        /// <exception cref="AdvanceIOException">if network connection failed</exception>
        /// <exception cref="AdvanceXMLException">On Xml parsing error</exception>
        /// <exception cref="AdvanceControlException">if user is not allowed to lis schemas.</exception>
        AdvanceSchemaRegistryEntry QuerySchema(string name);

        /// <summary>
        /// Add or modify a schema in the flow engine.
        /// </summary>
        /// <param name="name">the schema's filename (without path)</param>
        /// <param name="schema">schema content</param>
        /// <exception cref="AdvanceIOException">if network connection failed</exception>
        /// <exception cref="AdvanceControlException">if user is not allowed to add new schemas.</exception>
        void UpdateSchema(string name, XmlElement schema);

        /// <summary>
        /// Delete the specified schema.
        /// </summary>
        /// <param name="name">schema's filename (without path)</param>
        /// <exception cref="AdvanceIOException">if network connection failed</exception>
        /// <exception cref="AdvanceControlException">if user is not allowed to delete schemas.</exception>
        void DeleteSchema(string name);

        /// <summary>
        /// Delete a key entry from a keystore.
        /// </summary>
        /// <param name="keyStore">key store name</param>
        /// <param name="keyAlias">key alias</param>
        /// <exception cref="AdvanceIOException">if network connection failed</exception>
        /// <exception cref="AdvanceControlException">if user is not allowed to delete key.</exception>
        void DeleteKeyEntry(string keyStore, string keyAlias);

        /// <summary>
        /// Generate a new key with given properties.
        /// </summary>
        /// <param name="key">key generation properties</param>
        /// <exception cref="AdvanceIOException">if network connection failed</exception>
        /// <exception cref="AdvanceControlException">if user is not allowed to generate keys.</exception>
        void GenerateKey(AdvanceGenerateKey key);

        /// <summary>
        /// Export a certificate from a designated key store.
        /// </summary>
        /// <param name="request">represents the key store and key alias to export</param>
        /// <returns>certificate in textual CER format.</returns>
        /// <exception cref="AdvanceIOException">if network connection failed</exception>
        /// <exception cref="AdvanceXMLException">On Xml parsing error</exception>
        /// <exception cref="AdvanceControlException">if user is not allowed to export.</exception>
        string ExportCertificate(AdvanceKeyStoreExport request);

        /// <summary>
        /// Export a private from a designated key store.
        /// </summary>
        /// <param name="request">represents the key store and key alias to export</param>
        /// <returns>private key in textual PEM format.</returns>
        /// <exception cref="AdvanceIOException">if network connection failed</exception>
        /// <exception cref="AdvanceXMLException">On Xml parsing error</exception>
        /// <exception cref="AdvanceControlException">if user is not allowed to export.</exception>
        string ExportPrivateKey(AdvanceKeyStoreExport request);

        /// <summary>
        /// Import a certificate into a designated key store.
        /// </summary>
        /// <param name="request">represents the key store and key alias to import</param>
        /// <param name="data">certificate in textual CER format.</param>
        /// <exception cref="AdvanceIOException">if network connection failed</exception>
        /// <exception cref="AdvanceControlException">if user is not allowed to export.</exception>
        void ImportCertificate(AdvanceKeyStoreExport request, string data);

        /// <summary>
        /// Import a private key into a designated key store.
        /// </summary>
        /// <param name="request">represents the key store and key alias to import</param>
        /// <param name="data">ckey in textual PEM format.</param>
        /// <exception cref="AdvanceIOException">if network connection failed</exception>
        /// <exception cref="AdvanceControlException">if user is not allowed to export.</exception>
        void ImportPrivateKey(AdvanceKeyStoreExport request, string keyData, string certData);

        /// <summary>
        /// Export a signing request of the given private key.
        /// </summary>
        /// <param name="request">represents the key store and key alias to export</param>
        /// <param name="data">signing request in textual format.</param>
        /// <exception cref="AdvanceIOException">if network connection failed</exception>
        /// <exception cref="AdvanceXMLException">On Xml parsing error</exception>
        /// <exception cref="AdvanceControlException">if user is not allowed to export.</exception>
        string ExportSigningRequest(AdvanceKeyStoreExport request);

        /// <summary>
        /// Import a signing response into the designated key store.
        /// </summary>
        /// <param name="request">represents the key store and key alias to export</param>
        /// <param name="data">the signing response in textual format.</param>
        /// <exception cref="AdvanceIOException">if network connection failed</exception>
        /// <exception cref="AdvanceControlException">if user is not allowed to export.</exception>
        void ImportSigningResponse(AdvanceKeyStoreExport request, string data);

        /// <summary>
        /// Test the JDBC data source connection.
        /// </summary>
        /// <param name="dataSourceName">data source identifier</param>
        /// <returns>error message or an empty string if successful</returns>
        /// <exception cref="AdvanceIOException">if network connection failed</exception>
        /// <exception cref="AdvanceXMLException">On Xml parsing error</exception>
        /// <exception cref="AdvanceControlException">if user is not allowed to test connection.</exception>
        string TestJDBCDataSource(string dataSourceName);

        /// <summary>
        /// Test a JMS endpoint configuration.
        /// </summary>
        /// <param name="jmsName">identifier of the JMS endpoint to test.</param>
        /// <returns>error message or an empty string if successful</returns>
        /// <exception cref="AdvanceIOException">if network connection failed</exception>
        /// <exception cref="AdvanceControlException">if user is not allowed to test connection.</exception>
        string TestJMSEndpoint(string jmsName);

        /// <summary>
        /// Test FTP data source.
        /// </summary>
        /// <param name="ftpName">data source identifier</param>
        /// <returns>error message or an empty string if successful</returns>
        /// <exception cref="AdvanceIOException">if network connection failed</exception>
        /// <exception cref="AdvanceXMLException">On Xml parsing error</exception>
        /// <exception cref="AdvanceControlException">if user is not allowed to test connection.</exception>
        string TestFTPDataSource(string ftpName);

        /// <summary>
        /// List keys of the given keystore.
        /// </summary>
        /// <param name="keyStore">keystore name</param>
        /// <returns>list of key entries</returns>
        /// <exception cref="AdvanceIOException">if network connection failed</exception>
        /// <exception cref="AdvanceXMLException">On Xml parsing error</exception>
        /// <exception cref="AdvanceControlException">if user is not allowed to list keys.</exception>
        List<AdvanceKeyEntry> QueryKeys(string keyStore);

 
        /// <summary>
        /// Stop a realm's execution.
        /// </summary>
        /// <param name="name">realm's name</param>
        /// <param name="byUser">user who is stopping the realm</param>
        /// <exception cref="AdvanceIOException">if network connection failed</exception>
        /// <exception cref="AdvanceControlException">if user is not allowed to list keys.</exception>
        void StopRealm(string name, string byUser);

        /// <summary>
        /// Start a realm's execution.
        /// </summary>
        /// <param name="name">realm's name</param>
        /// <param name="byUser">user who is starting the realm</param>
        /// <exception cref="AdvanceIOException">if network connection failed</exception>
        /// <exception cref="AdvanceControlException">if user is not allowed to start the realm or other problems arise.</exception>
        void StartRealm(string name, string byUser);

        /// <summary>
        /// Retrieve the current flow, if any, from the given realm.
        /// </summary>
        /// <param name="realm">the target realm</param>
        /// <returns>composite block representing the flow, or a completely empty composite block</returns>
        /// <exception cref="AdvanceIOException">if network connection failed</exception>
        /// <exception cref="AdvanceXMLException">On Xml parsing error</exception>
        /// <exception cref="AdvanceControlException">if user is not allowed to list keys.</exception>

        /// <exception cref="AdvanceControlException">if user is not allowed to query the flow.</exception>
        AdvanceCompositeBlock QueryFlow(string realm);

        /// <summary>
        /// Update a flow in a the given realm.
        /// </summary>
        /// <param name="realm">target realm</param>
        /// <param name="flow"> new flow to upload</param>
        /// <param name="byUser">user who modifies the realm</param>
        /// <exception cref="AdvanceIOException">if network connection failed</exception>
        /// <exception cref="AdvanceControlException">if user is not allowed to update a flow.</exception>
        void UpdateFlow(string realm, AdvanceCompositeBlock flow, string byUser);

        /// <summary>
        /// Verify the given flow.
        /// </summary>
        /// <param name="flow">flow to verify</param>
        /// <returns>results of the compilation in terms of errors and computed types of wires</returns>
        /// <exception cref="AdvanceIOException">if network connection failed</exception>
        /// <exception cref="AdvanceXMLException">On Xml parsing error</exception>
        /// <exception cref="AdvanceControlException">if user is not allowed to update a flow.</exception>
        AdvanceCompilationResult VerifyFlow(AdvanceCompositeBlock flow);

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
        IObservable<BlockDiagnostic> DebugBlock(String realm, String blockId);

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
        IObservable<PortDiagnostic> DebugParameter(string realm, string blockId, string port);

        /// <summary>
        /// Inject a value into the given  realm/block/input port.
        /// </summary>
        /// <param name="realm">realm</param>
        /// <param name="blockId">block unique identifier (as in the flow)</param>
        /// <param name="port">port name</param>
        /// <param name="value">as XNElement to inject</param>
        /// <exception cref="AdvanceIOException">if network connection failed</exception>
        /// <exception cref="AdvanceControlException">if user is not allowed to debug in the realm or referenced parameter is missing.</exception>
        void InjectValue(string realm, string blockId, string port, XmlElement value);

        /// <summary>
        /// Shut down the flow engine.
        /// </summary>
        /// <param name="port">port name</param>
        /// <param name="value">as XNElement to inject</param>
        /// <exception cref="AdvanceIOException">if network connection failed</exception>
        /// <exception cref="AdvanceControlException">if user is not allowed to shut down the engine.</exception>
        void Shutdown();

        /// <summary>
        /// Return the compilation result of the given realm.
        /// This function may be used to find out why a server-side verification failed
        /// or to query the compilation result after a flow upload and realm startup.
        /// </summary>
        /// <param name="realm">realm to query</param>
        /// <returns>compilation result or null if no compilation was performed since the last startup of the engine</returns>
        /// <exception cref="AdvanceIOException">if network connection failed</exception>
        /// <exception cref="AdvanceControlException">if user is not allowed to read a flow.</exception>
        AdvanceCompilationResult QueryCompilationResult(string realm);

        /// <summary>
        /// Retrieves the list of available global input and output ports of the given realm's flow description.
        /// </summary>
        /// <param name="realm">target realm</param>
        /// <returns>list of ports</returns>
        /// <exception cref="AdvanceIOException">if network connection failed</exception>
        /// <exception cref="AdvanceControlException">if user is not allowed to read a flow.</exception>
        List<AdvancePortSpecification> QueryPorts(string realm);

        /// <summary>
        /// ends a sequence of values to the same or multiple ports in a batch.
        /// </summary>
        /// <param name="realm">target realm</param>
        /// <param name="portValues">equence of port id and value XML pairs</param>
        /// <exception cref="AdvanceIOException">if network connection failed</exception>
        /// <exception cref="AdvanceControlException">if user is not has no right to access the realm/ports</exception>
        void SendPort(string realm, Dictionary<string, XmlNode> portValues);

        /// <summary>
        /// Observe the output of the specified port in the given realm.
        /// </summary>
        /// <param name="realm">target realm</param>
        /// <param name="portId">port identifier of an input port returned by the queryPorts() method.</param>
        /// <returns>observable which will send out the values</returns>
        /// <exception cref="AdvanceIOException">if network connection failed</exception>
        /// <exception cref="AdvanceControlException">if user is not has no right to access the realm/ports</exception>
        IObservable<XmlNode> ReceivePort(string realm, string portId);
    }

}
