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

namespace AdvanceAPIClient
{
    /// <summary>
    /// Interface for performing datastore related operations.
    /// </summary>
    public interface IDataStore : IDataStoreUpdate
    {
        /// <summary>
        /// Creates new realm.
        /// </summary>
        /// <param name="realm">realm name</param>
        /// <param name="byUser">user who creates the object</param>
        /// <exception cref="AdvanceIOException">if network connection failed</exception>
        /// <exception cref="AdvanceXMLException">On Xml parsing error</exception>
        /// <exception cref="AdvanceControlException">if  username or password is incorrect</exception>
        void CreateRealm(string realm, string byUser);

        /// <summary>
        /// Delete block states of all blocks in the specified realm.
        /// </summary>
        /// <param name="realm">target realm</param>
        /// <exception cref="AdvanceIOException">if network connection failed</exception>
        /// <exception cref="AdvanceControlException">if  username or password is incorrect</exception>
        void DeleteBlockStates(string realm);

        /// <summary>
        /// Delete an email box record.
        /// </summary>
        /// <param name="name">box name</param>
        /// <exception cref="AdvanceIOException">if network connection failed</exception>
        /// <exception cref="AdvanceControlException">if  username or password is incorrect</exception>
        void DeleteEmailBox(string name);

        /// <summary>
        /// Delete FTP data source object.
        /// </summary>
        /// <param name="ftpName">data source identifier</param>
        /// <exception cref="AdvanceIOException">if network connection failed</exception>
        /// <exception cref="AdvanceControlException">if  username or password is incorrect</exception>
        void DeleteFTPDataSource(string ftpName);

        /// <summary>
        /// Delete aJDBC data source.
        /// </summary>
        /// <param name="dataSourceName">JDBC data source identifier</param>
        /// <exception cref="AdvanceIOException">if network connection failed</exception>
        /// <exception cref="AdvanceControlException">if  username or password is incorrect</exception>
        void DeleteJDBCDataSource(string dataSourceName);

        /// <summary>
        /// Delete a JMS endpoint configuration.
        /// </summary>
        /// <param name="jmsName">box name</param>
        /// <exception cref="AdvanceIOException">if network connection failed</exception>
        /// <exception cref="AdvanceControlException">if  username or password is incorrect</exception>
        void DeleteJMSEndpoint(string jmsName);

        /// <summary>
        /// Delete a key store.
        /// </summary>
        /// <param name="keyStore">key store name</param>
        /// <exception cref="AdvanceIOException">if network connection failed</exception>
        /// <exception cref="AdvanceControlException">if  username or password is incorrect</exception>
        void DeleteKeyStore(string keyStore);

        /// <summary>
        /// Delete a local file data source record.
        /// </summary>
        /// <param name="fileNname">file data source identifier</param>
        /// <exception cref="AdvanceIOException">if network connection failed</exception>
        /// <exception cref="AdvanceControlException">if  username or password is incorrect</exception>
        void DeleteLocalFileDataSource(String fileName);

        /// <summary>
        /// Delete a realm.
        /// </summary>
        /// <param name="reamm">realm name</param>
        /// <exception cref="AdvanceIOException">if network connection failed</exception>
        /// <exception cref="AdvanceControlException">if  username or password is incorrect</exception>
        void DeleteRealm(string realm);

        /// <summary>
        /// Delete the SOAP channel.
        /// </summary>
        /// <param name="name">Channel name</param>
        /// <exception cref="AdvanceIOException">if network connection failed</exception>
        /// <exception cref="AdvanceControlException">if  username or password is incorrect</exception>
        void DeleteSOAPEndpoint(string name);

        /// <summary>
        /// Delete auser.
        /// </summary>
        /// <param name="userName">User's identifier to delete</param>
        /// <param name="byUser">user who is deletes it</param>
        /// <exception cref="AdvanceIOException">if network connection failed</exception>
        /// <exception cref="AdvanceControlException">if  username or password is incorrect</exception>
        void DeleteUser(string userName, string byUser);

        /// <summary>
        /// Delete a web data source.
        /// </summary>
        /// <param name="webName">web data source identifier</param>
        /// <exception cref="AdvanceIOException">if network connection failed</exception>
        /// <exception cref="AdvanceControlException">if  username or password is incorrect</exception>
        void DeleteWebDataSource(string webName);

        /// <summary>
        /// Enable/disable a user.
        /// </summary>
        /// <param name="userName">User's identifier</param>
        /// <param name="enabled">if true enable otherwise disable</param>
        /// <param name="byUser">user who is changes it</param>
        /// <exception cref="AdvanceIOException">if network connection failed</exception>
        /// <exception cref="AdvanceControlException">if  username or password is incorrect</exception>
        void EnableUser(string userName, bool enabled, string byUser);

        /// <summary>
        /// Check if the user has the expected right
        /// </summary>
        /// <param name="userName">User name</param>
        /// <param name="expected">Expected right</param>
        /// <exception cref="AdvanceIOException">if network connection failed</exception>
        /// <exception cref="AdvanceXMLException">On Xml parsing error</exception>
        /// <exception cref="AdvanceControlException">if  username or password is incorrect</exception>
        bool HasUserRight(string userName, AdvanceUserRights expected);

        /// <summary>
        /// Check if the user has the expected realm right
        /// </summary>
        /// <param name="userName">User name</param>
        /// <param name="realm">Realm name</param>
        /// <param name="expected">Expected realm right</param>
        /// <exception cref="AdvanceIOException">if network connection failed</exception>
        /// <exception cref="AdvanceXMLException">On Xml parsing error</exception>
        /// <exception cref="AdvanceControlException">if  username or password is incorrect</exception>
        bool HasUserRight(string userName, string realm, AdvanceUserRealmRights expected);

        /// <summary>
        /// Query block state.
        /// </summary>
        /// <param name="realm">realm name</param>
        /// <param name="blockId"> block identifier</param>
        /// <returns>Flow descriptor xml element</returns>
        /// <exception cref="AdvanceIOException">if network connection failed</exception>
        /// <exception cref="AdvanceXMLException">On Xml parsing error</exception>
        /// <exception cref="AdvanceControlException">if  username or password is incorrect</exception>
        XmlNode QueryBlockState(string realm, string blockId);

        /// <summary>
        /// Query flow descriptor of the given realm.
        /// </summary>
        /// <param name="realm">realm name</param>
        /// <returns>Flow descriptor xml element</returns>
        /// <exception cref="AdvanceIOException">if network connection failed</exception>
        /// <exception cref="AdvanceXMLException">On Xml parsing error</exception>
        /// <exception cref="AdvanceControlException">if  username or password is incorrect</exception>
        XmlNode QueryFlow(string realm);

        /// <summary>
        /// Query emaol box
        /// </summary>
        /// <param name="name">box name</param>
        /// <returns>email box object</returns>
        /// <exception cref="AdvanceIOException">if network connection failed</exception>
        /// <exception cref="AdvanceXMLException">On Xml parsing error</exception>
        /// <exception cref="AdvanceControlException">if  username or password is incorrect</exception>
        AdvanceEmailBox QueryEmailBox(string name);

        /// <summary>
        /// Query all email boxes
        /// </summary>
        /// <returns>List email box objects</returns>
        /// <exception cref="AdvanceIOException">if network connection failed</exception>
        /// <exception cref="AdvanceXMLException">On Xml parsing error</exception>
        /// <exception cref="AdvanceControlException">if  username or password is incorrect</exception>
        List<AdvanceEmailBox> QueryEmailBoxes();

        /// <summary>
        /// Query JDBC data source
        /// </summary>
        /// <param name="name">FTP data source name</param>
        /// <returns>JJDBC data source object</returns>
        /// <exception cref="AdvanceIOException">if network connection failed</exception>
        /// <exception cref="AdvanceXMLException">On Xml parsing error</exception>
        /// <exception cref="AdvanceControlException">if  username or password is incorrect</exception>
        AdvanceFTPDataSource QueryFTPDataSource(String name);

        /// <summary>
        /// Query all FTP data sources
        /// </summary>
        /// <returns>List of FTP data source objects</returns>
        /// <exception cref="AdvanceIOException">if network connection failed</exception>
        /// <exception cref="AdvanceXMLException">On Xml parsing error</exception>
        /// <exception cref="AdvanceControlException">if  username or password is incorrect</exception>
        List<AdvanceFTPDataSource> QueryFTPDataSources();

        /// <summary>
        /// Query JDBC data source
        /// </summary>
        /// <param name="name">JDBC data source name</param>
        /// <returns>JJDBC data source object</returns>
        /// <exception cref="AdvanceIOException">if network connection failed</exception>
        /// <exception cref="AdvanceXMLException">On Xml parsing error</exception>
        /// <exception cref="AdvanceControlException">if  username or password is incorrect</exception>
        AdvanceJDBCDataSource QueryJDBCDataSource(string name);

        /// <summary>
        /// Query all JDBC data sources
        /// </summary>
        /// <returns>List of JDBC data source objects</returns>
        /// <exception cref="AdvanceIOException">if network connection failed</exception>
        /// <exception cref="AdvanceXMLException">On Xml parsing error</exception>
        /// <exception cref="AdvanceControlException">if  username or password is incorrect</exception>
        List<AdvanceJDBCDataSource> QueryJDBCDataSources();

        /// <summary>
        /// Query JMS endpoint
        /// </summary>
        /// <param name="name">JMS endpoint name</param>
        /// <returns>JMS endpoint object</returns>
        /// <exception cref="AdvanceIOException">if network connection failed</exception>
        /// <exception cref="AdvanceXMLException">On Xml parsing error</exception>
        /// <exception cref="AdvanceControlException">if  username or password is incorrect</exception>
        AdvanceJMSEndpoint QueryJMSEndpoint(string name);

        /// <summary>
        /// Query all JMS endpoints
        /// </summary>
        /// <returns>List of JMS endpoints</returns>
        /// <exception cref="AdvanceIOException">if network connection failed</exception>
        /// <exception cref="AdvanceXMLException">On Xml parsing error</exception>
        /// <exception cref="AdvanceControlException">if  username or password is incorrect</exception>
        List<AdvanceJMSEndpoint> QueryJMSEndpoints();

        /// <summary>
        /// Query an individual key store.
        /// </summary>
        /// <param name="name">Key store name</param>
        /// <exception cref="AdvanceIOException">if network connection failed</exception>
        /// <exception cref="AdvanceXMLException">On Xml parsing error</exception>
        /// <exception cref="AdvanceControlException">if  username or password is incorrect</exception>
        AdvanceKeyStore QueryKeyStore(string name);

        /// <summary>
        /// Query all available key stores.
        /// </summary>
        /// <returns>list of available key stores.</returns>
        /// <exception cref="AdvanceIOException">if network connection failed</exception>
        /// <exception cref="AdvanceXMLException">On Xml parsing error</exception>
        /// <exception cref="AdvanceControlException">if  username or password is incorrect</exception>
        List<AdvanceKeyStore> QueryKeyStores();

        /// <summary>
        /// Query local file data source
        /// </summary>
        /// <param name="name">local file data source name</param>
        /// <returns>local file data source</returns>
        /// <exception cref="AdvanceIOException">if network connection failed</exception>
        /// <exception cref="AdvanceXMLException">On Xml parsing error</exception>
        /// <exception cref="AdvanceControlException">if  username or password is incorrect</exception>
        AdvanceLocalFileDataSource QueryLocalFileDataSource(string name);

        /// <summary>
        /// Query all local file data sources
        /// </summary>
        /// <returns>list of local file data sources</returns>
        /// <exception cref="AdvanceIOException">if network connection failed</exception>
        /// <exception cref="AdvanceXMLException">On Xml parsing error</exception>
        /// <exception cref="AdvanceControlException">if  username or password is incorrect</exception>
        List<AdvanceLocalFileDataSource> QueryLocalFileDataSources();

        /// <summary>
        /// Query notification group
        /// </summary>
        /// <param name="type">group type</param>
        /// <param name="name">group name</param>
        /// <returns>set of contact information</returns>
        /// <exception cref="AdvanceIOException">if network connection failed</exception>
        /// <exception cref="AdvanceXMLException">On Xml parsing error</exception>
        /// <exception cref="AdvanceControlException">if  username or password is incorrect</exception>
        List<string> QueryNotificationGroup(AdvanceNotificationGroupType type, string name);

        /// <summary>
        /// Query all notification groups
        /// </summary>
        ///<returns>Notification addrasses by  notification group names and by type</returns>
        /// <exception cref="AdvanceIOException">if network connection failed</exception>
        /// <exception cref="AdvanceXMLException">On Xml parsing error</exception>
        /// <exception cref="AdvanceControlException">if  username or password is incorrect</exception>
        Dictionary<AdvanceNotificationGroupType, Dictionary<string, List<string>>> QueryNotificationGroups();

        /// <summary>
        /// Query reaml
        /// </summary>
        /// <param name="realm">realm name</param>
        ///<returns>realm object</returns>
        /// <exception cref="AdvanceIOException">if network connection failed</exception>
        /// <exception cref="AdvanceXMLException">On Xml parsing error</exception>
        /// <exception cref="AdvanceControlException">if  username or password is incorrect</exception>
        AdvanceRealm QueryRealm(string realm);

        /// <summary>
        /// Query all realms
        /// </summary>
        ///<returns>realm objects</returns>
        /// <exception cref="AdvanceIOException">if network connection failed</exception>
        /// <exception cref="AdvanceXMLException">On Xml parsing error</exception>
        /// <exception cref="AdvanceControlException">if  username or password is incorrect</exception>
        List<AdvanceRealm> QueryRealms();

        /// <summary>
        /// Query SOAP endpoint
        /// </summary>
        /// <param name="name">SOAP endpoint name</param>
        ///<returns>SOAP endpoint object</returns>
        /// <exception cref="AdvanceIOException">if network connection failed</exception>
        /// <exception cref="AdvanceXMLException">On Xml parsing error</exception>
        /// <exception cref="AdvanceControlException">if  username or password is incorrect</exception>
        AdvanceSOAPEndpoint QuerySOAPEndpoint(string name);

        /// <summary>
        /// Query all SOAP endpoints
        /// </summary>
        ///<returns>List of SOAP endpoint objects</returns>
        /// <exception cref="AdvanceIOException">if network connection failed</exception>
        /// <exception cref="AdvanceXMLException">On Xml parsing error</exception>
        /// <exception cref="AdvanceControlException">if  username or password is incorrect</exception>
        List<AdvanceSOAPEndpoint> QuerySOAPEndpoints();

        /// <summary>
        /// Query user.
        /// </summary>
        /// <param name="userName">User identifier</param>
        /// <returns>User object</returns>
        /// <exception cref="AdvanceIOException">if network connection failed</exception>
        /// <exception cref="AdvanceXMLException">On Xml parsing error</exception>
        /// <exception cref="AdvanceControlException">if  username or password is incorrect</exception>
        AdvanceUser QueryUser(string userName);

        /// <summary>
        /// Query all users.
        /// </summary>
        /// <returns>user list</returns>
        /// <exception cref="AdvanceIOException">if network connection failed</exception>
        /// <exception cref="AdvanceXMLException">On Xml parsing error</exception>
        /// <exception cref="AdvanceControlException">if  username or password is incorrect</exception>
        List<AdvanceUser> QueryUsers();

        /// <summary>
        /// Query Web data source properties
        /// </summary>
        ///<param name="name">Web data source name</param>
        ///<returns>Web data source object</returns>
        /// <exception cref="AdvanceIOException">if network connection failed</exception>
        /// <exception cref="AdvanceXMLException">On Xml parsing error</exception>
        /// <exception cref="AdvanceControlException">if  username or password is incorrect</exception>
        AdvanceWebDataSource QueryWebDataSource(string name);

        /// <summary>
        /// Query list of web data sources.
        /// </summary>
        /// <returns>web data source list</returns>
        /// <exception cref="AdvanceIOException">if network connection failed</exception>
        /// <exception cref="AdvanceXMLException">On Xml parsing error</exception>
        /// <exception cref="AdvanceControlException">if  username or password is incorrect</exception>
        List<AdvanceWebDataSource> QueryWebDataSources();
    }

}
