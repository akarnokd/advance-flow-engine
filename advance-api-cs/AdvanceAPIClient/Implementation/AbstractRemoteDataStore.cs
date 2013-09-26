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

using AdvanceAPIClient.Core;
using AdvanceAPIClient.Classes;
using AdvanceAPIClient.Communication;
using System.Xml;

namespace AdvanceAPIClient.Implementation
{
    public abstract class AbstractRemoteDataStore : IDataStore
    {
        protected AbstractXmlCommunicator communicator;

        public AbstractRemoteDataStore(AbstractXmlCommunicator communicator)
        {
            this.communicator = communicator;
        }

        #region IDataStoreUpdate interface functions
        /// <summary>
        /// Saves block state
        /// </summary>
        /// <param name="realm">target realm</param>
        /// <param name="blockId">arget block identifier</param>
        /// <param name="state">state Xml element or null</param>
        /// <exception cref="AdvanceIOException">if network connection failed</exception>
        /// <exception cref="AdvanceXMLException">On Xml parsing error</exception>
        /// <exception cref="AdvanceControlException">if  username or password is incorrect</exception>
        public void UpdateBlockState(string realm, string blockId, XmlNode state)
        {
            XmlDocument req = XmlReadWrite.CreateFunctionRequest("update-block-state");
            XmlReadWrite.AddAttribute(req.DocumentElement, "realm", realm);
            XmlReadWrite.AddAttribute(req.DocumentElement, "block-id", blockId);
  		    if (state != null) 
                req.DocumentElement.AppendChild(req.ImportNode(state, true));
            this.communicator.Send(req);
        }

        /// <summary>
        /// Update an email box record.
        /// </summary>
        /// <param name="box">new box record</param>
        /// <exception cref="AdvanceIOException">if network connection failed</exception>
        /// <exception cref="AdvanceXMLException">On Xml parsing error</exception>
        /// <exception cref="AdvanceControlException">if  username or password is incorrect</exception>
        public void UpdateEmailBox(AdvanceEmailBox box)
        {
            this.communicator.Send(XmlReadWrite.CreateFunctionRequest("update-email-box", box));
        }

        /// <summary>
        /// Update flow descriptor in the given realm.
        /// </summary>
        /// <param name="realm">target realm</param>
        /// <param name="flow">flow descriptor XML</param>
        /// <exception cref="AdvanceIOException">if network connection failed</exception>
        /// <exception cref="AdvanceXMLException">On Xml parsing error</exception>
        /// <exception cref="AdvanceControlException">if  username or password is incorrect</exception>
        public void UpdateFlow(string realm, XmlNode flow)
        {
            XmlDocument req = XmlReadWrite.CreateFunctionRequest("update-flow", "realm", realm);
            req.DocumentElement.AppendChild(req.ImportNode(flow, true)); 
            this.communicator.Send(req);
        }

        /// <summary>
        /// Updates FTP data source.
        /// </summary>
        /// <param name="dataSource">data source object</param>
        /// <exception cref="AdvanceIOException">if network connection failed</exception>
        /// <exception cref="AdvanceXMLException">On Xml parsing error</exception>
        /// <exception cref="AdvanceControlException">if  username or password is incorrect</exception>
        public void UpdateFTPDataSource(AdvanceFTPDataSource dataSource)
        {
            this.communicator.Send(XmlReadWrite.CreateFunctionRequest("update-ftp-data-source", dataSource));
        }

        /// <summary>
        /// Updates a JDBC data source.
        /// </summary>
        /// <param name="dataSource">data source object</param>
        /// <exception cref="AdvanceIOException">if network connection failed</exception>
        /// <exception cref="AdvanceXMLException">On Xml parsing error</exception>
        /// <exception cref="AdvanceControlException">if  username or password is incorrect</exception>
        public void UpdateJDBCDataSource(AdvanceJDBCDataSource dataSource)
        {
            this.communicator.Send(XmlReadWrite.CreateFunctionRequest("update-jdbc-data-source", dataSource));
        }

        /// <summary>
        /// Updates a JMS endpoint settings.
        /// </summary>
        /// <param name="AdvanceJMSEndpoint">endpoint object</param>
        /// <exception cref="AdvanceIOException">if network connection failed</exception>
        /// <exception cref="AdvanceXMLException">On Xml parsing error</exception>
        /// <exception cref="AdvanceControlException">if  username or password is incorrect</exception>
        public void UpdateJMSEndpoint(AdvanceJMSEndpoint endpoint)
        {
            this.communicator.Send(XmlReadWrite.CreateFunctionRequest("update-jms-endpoint", endpoint));
        }

        /// <summary>
        /// Updates key store properties.
        /// </summary>
        /// <param name="keyStore">key store properties</param>
        /// <exception cref="AdvanceIOException">if network connection failed</exception>
        /// <exception cref="AdvanceXMLException">On Xml parsing error</exception>
        /// <exception cref="AdvanceControlException">if  username or password is incorrect</exception>
        public void UpdateKeyStore(AdvanceKeyStore keyStore)
        {
            this.communicator.Send(XmlReadWrite.CreateFunctionRequest("update-keystore", keyStore));
        }

        /// <summary>
        /// Updates a local file data source object
        /// </summary>
        /// <param name="dataSource">data source object</param>
        /// <exception cref="AdvanceIOException">if network connection failed</exception>
        /// <exception cref="AdvanceXMLException">On Xml parsing error</exception>
        /// <exception cref="AdvanceControlException">if  username or password is incorrect</exception>
        public void UpdateLocalFileDataSource(AdvanceLocalFileDataSource dataSource)
        {
            this.communicator.Send(XmlReadWrite.CreateFunctionRequest("update-local-file-data-source", dataSource));
        }

        /// <summary>
        /// Updates notification groups
        /// </summary>
        /// <remarks>
        /// Note that this update is considered complete, e.g., the existing group
        /// settings will be deleted and replaced by the contents of the map.
        /// </remarks>
        /// <param name="groups">notification group names with set of notification address by notification group types</param>
        /// <exception cref="AdvanceIOException">if network connection failed</exception>
        /// <exception cref="AdvanceXMLException">On Xml parsing error</exception>
        /// <exception cref="AdvanceControlException">if  username or password is incorrect</exception>
        public void UpdateNotificationGroups(Dictionary<AdvanceNotificationGroupType, Dictionary<string, ICollection<string>>> groups)
        {
            XmlDocument req = XmlReadWrite.CreateFunctionRequest("update-notification-groups");
            AdvanceNotificationGroup.FillXmlElementFromDict(req.DocumentElement, groups);
            this.communicator.Send(req);
        }

        /// <summary>
        /// Update properties of a realm.
        /// </summary>
        /// <param name="realm">realm record</param>
        /// <exception cref="AdvanceIOException">if network connection failed</exception>
        /// <exception cref="AdvanceXMLException">On Xml parsing error</exception>
        /// <exception cref="AdvanceControlException">if  username or password is incorrect</exception>
        public void UpdateRealm(AdvanceRealm realm)
        {
            this.communicator.Send(XmlReadWrite.CreateFunctionRequest("update-realm", realm));
        }

        /// <summary>
        /// Updates SOAP endpoint.
        /// </summary>
        /// <param name="endpoint">new endpoint settings</param>
        /// <exception cref="AdvanceIOException">if network connection failed</exception>
        /// <exception cref="AdvanceXMLException">On Xml parsing error</exception>
        /// <exception cref="AdvanceControlException">if  username or password is incorrect</exception>
        public void UpdateSOAPEndpoint(AdvanceSOAPEndpoint endpoint)
        {
            this.communicator.Send(XmlReadWrite.CreateFunctionRequest("update-soap-channel", endpoint));
        }

        /// <summary>
        /// Updates user's settings.
        /// </summary>
        /// <param name="user">target user object</param>
        /// <exception cref="AdvanceIOException">if network connection failed</exception>
        /// <exception cref="AdvanceXMLException">On Xml parsing error</exception>
        /// <exception cref="AdvanceControlException">if  username or password is incorrect</exception>
        public void UpdateUser(AdvanceUser user)
        {
            this.communicator.Send(XmlReadWrite.CreateFunctionRequest("update-user", user));
        }

        /// <summary>
        /// Updates a web data source.
        /// </summary>
        /// <param name="dataSource">data source object</param>
        /// <exception cref="AdvanceIOException">if network connection failed</exception>
        /// <exception cref="AdvanceXMLException">On Xml parsing error</exception>
        /// <exception cref="AdvanceControlException">if  username or password is incorrect</exception>
        public void UpdateWebDataSource(AdvanceWebDataSource dataSource)
        {
            this.communicator.Send(XmlReadWrite.CreateFunctionRequest("update-web-data-source", dataSource));
        }

        #endregion IDataStoreUpdate interface functions

        #region IDataStore interface functions
        /// <summary>
        /// Creates new realm.
        /// </summary>
        /// <param name="realm">realm name</param>
        /// <param name="byUser">user who creates the object</param>
        /// <exception cref="AdvanceIOException">if network connection failed</exception>
        /// <exception cref="AdvanceXMLException">On Xml parsing error</exception>
        /// <exception cref="AdvanceControlException">if  username or password is incorrect</exception>
        public void CreateRealm(string realm, string byUser)
        {
            Dictionary<string, object> attrs = new Dictionary<string, object>();
            attrs.Add("realm", realm);
            attrs.Add("by-user", byUser);
            this.communicator.Send(XmlReadWrite.CreateFunctionRequest("update-realm", null, attrs));
        }

        /// <summary>
        /// Delete block states of all blocks in the specified realm.
        /// </summary>
        /// <param name="realm">target realm</param>
        /// <exception cref="AdvanceIOException">if network connection failed</exception>
        /// <exception cref="AdvanceControlException">if  username or password is incorrect</exception>
        public void DeleteBlockStates(string realm)
        {
            this.communicator.Send(XmlReadWrite.CreateFunctionRequest("delete-block-states", "realm", realm));
        }

        /// <summary>
        /// Delete an email box record.
        /// </summary>
        /// <param name="name">box name</param>
        /// <exception cref="AdvanceIOException">if network connection failed</exception>
        /// <exception cref="AdvanceControlException">if  username or password is incorrect</exception>
        public void DeleteEmailBox(string name)
        {
            this.communicator.Send(XmlReadWrite.CreateFunctionRequest("delete-email-box", "name", name));
        }

        /// <summary>
        /// Delete FTP data source object.
        /// </summary>
        /// <param name="ftpName">data source identifier</param>
        /// <exception cref="AdvanceIOException">if network connection failed</exception>
        /// <exception cref="AdvanceControlException">if  username or password is incorrect</exception>
        public void DeleteFTPDataSource(string ftpName)
        {
            this.communicator.Send(XmlReadWrite.CreateFunctionRequest("delete-ftp-data-source", "ftp-name", ftpName));
        }

        /// <summary>
        /// Delete aJDBC data source.
        /// </summary>
        /// <param name="dataSourceName">JDBC data source identifier</param>
        /// <exception cref="AdvanceIOException">if network connection failed</exception>
        /// <exception cref="AdvanceControlException">if  username or password is incorrect</exception>
        public void DeleteJDBCDataSource(string dataSourceName)
        {
            this.communicator.Send(XmlReadWrite.CreateFunctionRequest("update-jdbc-data-source", dataSourceName));
        }

        /// <summary>
        /// Delete a JMS endpoint configuration.
        /// </summary>
        /// <param name="jmsName">box name</param>
        /// <exception cref="AdvanceIOException">if network connection failed</exception>
        /// <exception cref="AdvanceControlException">if  username or password is incorrect</exception>
        public void DeleteJMSEndpoint(string jmsName)
        {
            this.communicator.Send(XmlReadWrite.CreateFunctionRequest("delete-jdbc-data-source", "data-source-name", jmsName));
        }

        /// <summary>
        /// Delete a key store.
        /// </summary>
        /// <param name="keyStore">key store name</param>
        /// <exception cref="AdvanceIOException">if network connection failed</exception>
        /// <exception cref="AdvanceControlException">if  username or password is incorrect</exception>
        public void DeleteKeyStore(string keyStore)
        {
            this.communicator.Send(XmlReadWrite.CreateFunctionRequest("delete-keystore", "keystore", keyStore));
        }

        /// <summary>
        /// Delete a local file data source record.
        /// </summary>
        /// <param name="fileNname">file data source identifier</param>
        /// <exception cref="AdvanceIOException">if network connection failed</exception>
        /// <exception cref="AdvanceControlException">if  username or password is incorrect</exception>
        public void DeleteLocalFileDataSource(String fileName)
        {
            this.communicator.Send(XmlReadWrite.CreateFunctionRequest("delete-local-file-data-source", "file-name", fileName));
        }

        /// <summary>
        /// Delete a realm.
        /// </summary>
        /// <param name="reamm">realm name</param>
        /// <exception cref="AdvanceIOException">if network connection failed</exception>
        /// <exception cref="AdvanceControlException">if  username or password is incorrect</exception>
        public void DeleteRealm(string realm)
        {
            this.communicator.Send(XmlReadWrite.CreateFunctionRequest("delete-realm", "realm", realm));
        }

        /// <summary>
        /// Delete the SOAP channel.
        /// </summary>
        /// <param name="name">Channel name</param>
        /// <exception cref="AdvanceIOException">if network connection failed</exception>
        /// <exception cref="AdvanceControlException">if  username or password is incorrect</exception>
        public void DeleteSOAPEndpoint(string name)
        {
            this.communicator.Send(XmlReadWrite.CreateFunctionRequest("delete-soap-channel", "name", name));
        }

        /// <summary>
        /// Delete auser.
        /// </summary>
        /// <param name="userName">User's identifier to delete</param>
        /// <param name="byUser">user who is deletes it</param>
        /// <exception cref="AdvanceIOException">if network connection failed</exception>
        /// <exception cref="AdvanceControlException">if  username or password is incorrect</exception>
        public void DeleteUser(string userName, string byUser)
        {
            this.communicator.Send(XmlReadWrite.CreateFunctionRequest("delete-user", "user-name", userName));
        }

        /// <summary>
        /// Delete a web data source.
        /// </summary>
        /// <param name="webName">web data source identifier</param>
        /// <exception cref="AdvanceIOException">if network connection failed</exception>
        /// <exception cref="AdvanceControlException">if  username or password is incorrect</exception>
        public void DeleteWebDataSource(string webName)
        {
            this.communicator.Send(XmlReadWrite.CreateFunctionRequest("delete-web-data-source", "web-name", webName));
        }

        /// <summary>
        /// Enable/disable a user.
        /// </summary>
        /// <param name="userName">User's identifier</param>
        /// <param name="enabled">if true enable otherwise disable</param>
        /// <param name="byUser">user who is changes it</param>
        /// <exception cref="AdvanceIOException">if network connection failed</exception>
        /// <exception cref="AdvanceControlException">if  username or password is incorrect</exception>
        public void EnableUser(string userName, bool enabled, string byUser)
        {
            Dictionary<string, object> attrs = new Dictionary<string, object>();
            attrs.Add("user-name", userName);
            attrs.Add("enabled", enabled);
            attrs.Add("by-user", byUser);
            this.communicator.Send(XmlReadWrite.CreateFunctionRequest("enable-user", null, attrs));
        }

        /// <summary>
        /// Check if the user has the expected right
        /// </summary>
        /// <param name="userName">user's name</param>
        /// <param name="expected">expected right</param>
        /// <returns>true if has the expected right</returns>
        public bool HasUserRight(string userName, AdvanceUserRights expected)
        {
            Dictionary<string, object> attrs = new Dictionary<string, object>();
            attrs.Add("user-name", userName);
            attrs.Add("expected", expected);
            XmlNode resp = this.communicator.Query(XmlReadWrite.CreateFunctionRequest("has-user-right", null, attrs));
            return XmlReadWrite.GetBoolContent(resp, false); 
        }

        /// <summary>
        /// Check if the user has the expected realm right
        /// </summary>
        /// <param name="userName">User name</param>
        /// <param name="realm">Realm name</param>
        /// <param name="expected">Expected realm right</param>
        /// <exception cref="AdvanceIOException">if network connection failed</exception>
        /// <exception cref="AdvanceXMLException">On Xml parsing error</exception>
        /// <exception cref="AdvanceControlException">if  username or password is incorrect</exception>
        public bool HasUserRight(string userName, string realm, AdvanceUserRealmRights expected)
        {
            Dictionary<string, object> attrs = new Dictionary<string, object>();
            attrs.Add("user-name", userName);
            attrs.Add("realm", realm);
            attrs.Add("expected", expected);
            XmlNode resp = this.communicator.Query(XmlReadWrite.CreateFunctionRequest("has-user-right", null, attrs));
            return XmlReadWrite.GetBoolContent(resp, false); 
        }

        /// <summary>
        /// Query block state.
        /// </summary>
        /// <param name="realm">realm name</param>
        /// <param name="blockId"> block identifier</param>
        /// <returns>Flow descriptor xml element</returns>
        /// <exception cref="AdvanceIOException">if network connection failed</exception>
        /// <exception cref="AdvanceXMLException">On Xml parsing error</exception>
        /// <exception cref="AdvanceControlException">if  username or password is incorrect</exception>
        public XmlNode QueryBlockState(string realm, string blockId)
        {
            Dictionary<string, object> attrs = new Dictionary<string, object>();
            attrs.Add("realm", realm);
            attrs.Add("block-id", blockId);
            return this.communicator.Query(XmlReadWrite.CreateFunctionRequest("query-block-state",null, attrs));
         }

        /// <summary>
        /// Query flow descriptor of the given realm.
        /// </summary>
        /// <param name="realm">realm name</param>
        /// <returns>Flow descriptor xml element</returns>
        /// <exception cref="AdvanceIOException">if network connection failed</exception>
        /// <exception cref="AdvanceXMLException">On Xml parsing error</exception>
        /// <exception cref="AdvanceControlException">if  username or password is incorrect</exception>
        public XmlNode QueryFlow(string realm)
        {
            return this.communicator.Query(XmlReadWrite.CreateFunctionRequest("query-flow", "realm", realm));
        }

        /// <summary>
        /// Query emaol box
        /// </summary>
        /// <param name="name">box name</param>
        /// <returns>email box object</returns>
        /// <exception cref="AdvanceIOException">if network connection failed</exception>
        /// <exception cref="AdvanceXMLException">On Xml parsing error</exception>
        /// <exception cref="AdvanceControlException">if  username or password is incorrect</exception>
        public AdvanceEmailBox QueryEmailBox(string name)
        {
            XmlNode resp = this.communicator.Query(XmlReadWrite.CreateFunctionRequest("query-email-box", "name", name));
            return XmlReadWrite.CreateFromXml<AdvanceEmailBox>(resp);
        }

        /// <summary>
        /// Query all email boxes
        /// </summary>
        /// <returns>List email box objects</returns>
        /// <exception cref="AdvanceIOException">if network connection failed</exception>
        /// <exception cref="AdvanceXMLException">On Xml parsing error</exception>
        /// <exception cref="AdvanceControlException">if  username or password is incorrect</exception>
        public List<AdvanceEmailBox> QueryEmailBoxes()
        {
            XmlNode resp = this.communicator.Query(XmlReadWrite.CreateFunctionRequest("query-email-boxes"));
            return XmlReadWrite.CreateListFromXml<AdvanceEmailBox>(resp,"email-boxes", "email-box");
        }

        /// <summary>
        /// Query JDBC data source
        /// </summary>
        /// <param name="name">FTP data source name</param>
        /// <returns>JJDBC data source object</returns>
        /// <exception cref="AdvanceIOException">if network connection failed</exception>
        /// <exception cref="AdvanceXMLException">On Xml parsing error</exception>
        /// <exception cref="AdvanceControlException">if  username or password is incorrect</exception>
        public AdvanceFTPDataSource QueryFTPDataSource(string name)
        {
            XmlNode resp = this.communicator.Query(XmlReadWrite.CreateFunctionRequest("query-ftp-data-source", "name", name));
            return XmlReadWrite.CreateFromXml<AdvanceFTPDataSource>(resp);
        }

        /// <summary>
        /// Query all FTP data sources
        /// </summary>
        /// <returns>List of FTP data source objects</returns>
        /// <exception cref="AdvanceIOException">if network connection failed</exception>
        /// <exception cref="AdvanceXMLException">On Xml parsing error</exception>
        /// <exception cref="AdvanceControlException">if  username or password is incorrect</exception>
        public List<AdvanceFTPDataSource> QueryFTPDataSources()
        {
            XmlNode resp = this.communicator.Query(XmlReadWrite.CreateFunctionRequest("query-ftp-data-sources"));
            return XmlReadWrite.CreateListFromXml<AdvanceFTPDataSource>(resp, "ftp-data-sources", "ftp-source");
        }

        /// <summary>
        /// Query JDBC data source
        /// </summary>
        /// <param name="name">JDBC data source name</param>
        /// <returns>JJDBC data source object</returns>
        /// <exception cref="AdvanceIOException">if network connection failed</exception>
        /// <exception cref="AdvanceXMLException">On Xml parsing error</exception>
        /// <exception cref="AdvanceControlException">if  username or password is incorrect</exception>
        public AdvanceJDBCDataSource QueryJDBCDataSource(string name)
        {
            XmlNode resp = this.communicator.Query(XmlReadWrite.CreateFunctionRequest("query-jdbc-data-source", "name", name));
            return XmlReadWrite.CreateFromXml<AdvanceJDBCDataSource>(resp);
        }

        /// <summary>
        /// Query all JDBC data sources
        /// </summary>
        /// <returns>List of JDBC data source objects</returns>
        /// <exception cref="AdvanceIOException">if network connection failed</exception>
        /// <exception cref="AdvanceXMLException">On Xml parsing error</exception>
        /// <exception cref="AdvanceControlException">if  username or password is incorrect</exception>
        public List<AdvanceJDBCDataSource> QueryJDBCDataSources()
        {
            XmlNode resp = this.communicator.Query(XmlReadWrite.CreateFunctionRequest("query-jdbc-data-sources"));
            return XmlReadWrite.CreateListFromXml<AdvanceJDBCDataSource>(resp,"jdbc-sources", "jdbc-source");
        }

        /// <summary>
        /// Query JMS endpoint
        /// </summary>
        /// <param name="name">JMS endpoint name</param>
        /// <returns>JMS endpoint object</returns>
        /// <exception cref="AdvanceIOException">if network connection failed</exception>
        /// <exception cref="AdvanceXMLException">On Xml parsing error</exception>
        /// <exception cref="AdvanceControlException">if  username or password is incorrect</exception>
        public AdvanceJMSEndpoint QueryJMSEndpoint(string name)
        {
            XmlNode resp = this.communicator.Query(XmlReadWrite.CreateFunctionRequest("query-jms-endpoint", "name", name));
            return XmlReadWrite.CreateFromXml<AdvanceJMSEndpoint>(resp);
        }

        /// <summary>
        /// Query all JMS endpoints
        /// </summary>
        /// <returns>List of JMS endpoints</returns>
        /// <exception cref="AdvanceIOException">if network connection failed</exception>
        /// <exception cref="AdvanceXMLException">On Xml parsing error</exception>
        /// <exception cref="AdvanceControlException">if  username or password is incorrect</exception>
        public List<AdvanceJMSEndpoint> QueryJMSEndpoints()
        {
            XmlNode resp = this.communicator.Query(XmlReadWrite.CreateFunctionRequest("query-jms-endpoints"));
            return XmlReadWrite.CreateListFromXml<AdvanceJMSEndpoint>(resp,"jms-endpoints", "endpoint");
        }

        /// <summary>
        /// Query an individual key store.
        /// </summary>
        /// <param name="name">Key store name</param>
        /// <exception cref="AdvanceIOException">if network connection failed</exception>
        /// <exception cref="AdvanceXMLException">On Xml parsing error</exception>
        /// <exception cref="AdvanceControlException">if  username or password is incorrect</exception>
        public AdvanceKeyStore QueryKeyStore(string name)
        {
            XmlNode resp = this.communicator.Query(XmlReadWrite.CreateFunctionRequest("query-keystores", "name", name));
            return XmlReadWrite.CreateFromXml<AdvanceKeyStore>(resp);
        }

        /// <summary>
        /// Query all available key stores.
        /// </summary>
        /// <returns>list of available key stores.</returns>
        /// <exception cref="AdvanceIOException">if network connection failed</exception>
        /// <exception cref="AdvanceXMLException">On Xml parsing error</exception>
        /// <exception cref="AdvanceControlException">if  username or password is incorrect</exception>
        public List<AdvanceKeyStore> QueryKeyStores()
        {
            XmlNode resp = this.communicator.Query(XmlReadWrite.CreateFunctionRequest("query-keystores"));
            return XmlReadWrite.CreateListFromXml<AdvanceKeyStore>(resp,"keystores", "keystore");
        }

        /// <summary>
        /// Query local file data source
        /// </summary>
        /// <param name="name">local file data source name</param>
        /// <returns>local file data source</returns>
        /// <exception cref="AdvanceIOException">if network connection failed</exception>
        /// <exception cref="AdvanceXMLException">On Xml parsing error</exception>
        /// <exception cref="AdvanceControlException">if  username or password is incorrect</exception>
        public AdvanceLocalFileDataSource QueryLocalFileDataSource(string name)
        {
            XmlNode resp = this.communicator.Query(XmlReadWrite.CreateFunctionRequest("query-local-file-data-source", "name", name));
            return XmlReadWrite.CreateFromXml<AdvanceLocalFileDataSource>(resp);
        }

        /// <summary>
        /// Query all local file data sources
        /// </summary>
        /// <returns>list of local file data sources</returns>
        /// <exception cref="AdvanceIOException">if network connection failed</exception>
        /// <exception cref="AdvanceXMLException">On Xml parsing error</exception>
        /// <exception cref="AdvanceControlException">if  username or password is incorrect</exception>
        public List<AdvanceLocalFileDataSource> QueryLocalFileDataSources()
        {
            XmlNode resp = this.communicator.Query(XmlReadWrite.CreateFunctionRequest("query-local-file-data-sources"));
            return XmlReadWrite.CreateListFromXml<AdvanceLocalFileDataSource>(resp, "local-sources", "local-source");
        }

        /// <summary>
        /// Query notification group
        /// </summary>
        /// <param name="type">group type</param>
        /// <param name="name">group name</param>
        /// <returns>set of contact information</returns>
        /// <exception cref="AdvanceIOException">if network connection failed</exception>
        /// <exception cref="AdvanceXMLException">On Xml parsing error</exception>
        /// <exception cref="AdvanceControlException">if  username or password is incorrect</exception>
        public List<string> QueryNotificationGroup(AdvanceNotificationGroupType type, string name)
        {
            Dictionary<string, object> attrs = new Dictionary<string, object>();
            attrs.Add("type", type);
            attrs.Add("name", name);
            XmlNode resp = this.communicator.Query(XmlReadWrite.CreateFunctionRequest("query-notification-group", null, attrs));
            AdvanceNotificationGroup ng = XmlReadWrite.CreateFromXml<AdvanceNotificationGroup>(resp);
		    return ng.Contacts;      
        }

        /// <summary>
        /// Query all notification groups
        /// </summary>
        ///<returns>Notification addrasses by  notification group names and by type</returns>
        /// <exception cref="AdvanceIOException">if network connection failed</exception>
        /// <exception cref="AdvanceXMLException">On Xml parsing error</exception>
        /// <exception cref="AdvanceControlException">if  username or password is incorrect</exception>
        public Dictionary<AdvanceNotificationGroupType, Dictionary<string, List<string>>> QueryNotificationGroups()
        {
            XmlNode resp = this.communicator.Query(XmlReadWrite.CreateFunctionRequest("query-notification-groups"));
            return AdvanceNotificationGroup.LoadDictFomXml(resp);
        }

        /// <summary>
        /// Query reaml
        /// </summary>
        /// <param name="realm">realm name</param>
        ///<returns>realm object</returns>
        /// <exception cref="AdvanceIOException">if network connection failed</exception>
        /// <exception cref="AdvanceXMLException">On Xml parsing error</exception>
        /// <exception cref="AdvanceControlException">if  username or password is incorrect</exception>
        public AdvanceRealm QueryRealm(string realm)
        {
            XmlNode resp = this.communicator.Query(XmlReadWrite.CreateFunctionRequest("query-realm", "realm", realm));
            return XmlReadWrite.CreateFromXml<AdvanceRealm>(resp);
        }

        /// <summary>
        /// Query all realms
        /// </summary>
        ///<returns>realm objects</returns>
        /// <exception cref="AdvanceIOException">if network connection failed</exception>
        /// <exception cref="AdvanceXMLException">On Xml parsing error</exception>
        /// <exception cref="AdvanceControlException">if  username or password is incorrect</exception>
        public List<AdvanceRealm> QueryRealms()
        {
            XmlNode resp = this.communicator.Query(XmlReadWrite.CreateFunctionRequest("query-realms"));
            return XmlReadWrite.CreateListFromXml<AdvanceRealm>(resp, "realms", "realm");
        }

        /// <summary>
        /// Query SOAP endpoint
        /// </summary>
        /// <param name="name">SOAP endpoint name</param>
        ///<returns>SOAP endpoint object</returns>
        /// <exception cref="AdvanceIOException">if network connection failed</exception>
        /// <exception cref="AdvanceXMLException">On Xml parsing error</exception>
        /// <exception cref="AdvanceControlException">if  username or password is incorrect</exception>
        public AdvanceSOAPEndpoint QuerySOAPEndpoint(string name)
        {
            XmlNode resp = this.communicator.Query(XmlReadWrite.CreateFunctionRequest("query-soap-channel", "name", name));
            return XmlReadWrite.CreateFromXml<AdvanceSOAPEndpoint>(resp);
        }

        /// <summary>
        /// Query all SOAP endpoints
        /// </summary>
        ///<returns>List of SOAP endpoint objects</returns>
        /// <exception cref="AdvanceIOException">if network connection failed</exception>
        /// <exception cref="AdvanceXMLException">On Xml parsing error</exception>
        /// <exception cref="AdvanceControlException">if  username or password is incorrect</exception>
        public List<AdvanceSOAPEndpoint> QuerySOAPEndpoints()
        {
            XmlNode resp = this.communicator.Query(XmlReadWrite.CreateFunctionRequest("query-soap-channels"));
            return XmlReadWrite.CreateListFromXml<AdvanceSOAPEndpoint>(resp, "channels", "channel");
        }

        /// <summary>
        /// Query user.
        /// </summary>
        /// <param name="userName">User identifier</param>
        /// <returns>User object</returns>
        /// <exception cref="AdvanceIOException">if network connection failed</exception>
        /// <exception cref="AdvanceXMLException">On Xml parsing error</exception>
        /// <exception cref="AdvanceControlException">if  username or password is incorrect</exception>
        public AdvanceUser QueryUser(string userName)
        {
            XmlNode resp = this.communicator.Query(XmlReadWrite.CreateFunctionRequest("query-user", "user-name", userName));
            return XmlReadWrite.CreateFromXml<AdvanceUser>(resp);
        }

        /// <summary>
        /// Query all users.
        /// </summary>
        /// <returns>user list</returns>
        /// <exception cref="AdvanceIOException">if network connection failed</exception>
        /// <exception cref="AdvanceXMLException">On Xml parsing error</exception>
        /// <exception cref="AdvanceControlException">if  username or password is incorrect</exception>
        public List<AdvanceUser> QueryUsers()
        {
            XmlNode resp = this.communicator.Query(XmlReadWrite.CreateFunctionRequest("query-users"));
            return XmlReadWrite.CreateListFromXml<AdvanceUser>(resp, "users", "user");
        }

        /// <summary>
        /// Query Web data source properties
        /// </summary>
        ///<param name="name">Web data source name</param>
        ///<returns>Web data source object</returns>
        /// <exception cref="AdvanceIOException">if network connection failed</exception>
        /// <exception cref="AdvanceXMLException">On Xml parsing error</exception>
        /// <exception cref="AdvanceControlException">if  username or password is incorrect</exception>
        public AdvanceWebDataSource QueryWebDataSource(string name)
        {
            XmlNode resp = this.communicator.Query(XmlReadWrite.CreateFunctionRequest("query-web-data-source", "name", name));
            return XmlReadWrite.CreateFromXml<AdvanceWebDataSource>(resp);
        }

        /// <summary>
        /// Query list of web data sources.
        /// </summary>
        /// <returns>web data source list</returns>
        /// <exception cref="AdvanceIOException">if network connection failed</exception>
        /// <exception cref="AdvanceXMLException">On Xml parsing error</exception>
        /// <exception cref="AdvanceControlException">if  username or password is incorrect</exception>
        public List<AdvanceWebDataSource> QueryWebDataSources()
        {
            XmlNode resp = this.communicator.Query(XmlReadWrite.CreateFunctionRequest("query-web-data-sources"));
            return XmlReadWrite.CreateListFromXml<AdvanceWebDataSource>(resp, "web-sources", "web-source");
        }

        #endregion IDataStore interface functions

    }

}
