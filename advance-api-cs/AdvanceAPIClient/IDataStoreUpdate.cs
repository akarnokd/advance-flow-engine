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
    /// Interface having only the {@code updateXYZ} methods of the datastore.
    /// This will help implement the SQL dialect specific insert or update operations on a JDBC datastore.
    /// The users of the updateXYZ methods must set the {@code modifiedBy} field.</p>
    /// </summary>
    public interface IDataStoreUpdate
    {
        /// <summary>
        /// Saves block state
        /// </summary>
        /// <param name="realm">target realm</param>
        /// <param name="blockId">arget block identifier</param>
        /// <param name="state">state Xml element or null</param>
        /// <exception cref="AdvanceIOException">if network connection failed</exception>
        /// <exception cref="AdvanceXMLException">On Xml parsing error</exception>
        /// <exception cref="AdvanceControlException">if  username or password is incorrect</exception>
        void UpdateBlockState(string realm, string blockId, XmlNode state);

        /// <summary>
        /// Update an email box record.
        /// </summary>
        /// <param name="box">new box record</param>
        /// <exception cref="AdvanceIOException">if network connection failed</exception>
        /// <exception cref="AdvanceXMLException">On Xml parsing error</exception>
        /// <exception cref="AdvanceControlException">if  username or password is incorrect</exception>
        void UpdateEmailBox(AdvanceEmailBox box);

        /// <summary>
        /// Update flow descriptor in the given realm.
        /// </summary>
        /// <param name="realm">target realm</param>
        /// <param name="flow">flow descriptor XML</param>
        /// <exception cref="AdvanceIOException">if network connection failed</exception>
        /// <exception cref="AdvanceXMLException">On Xml parsing error</exception>
        /// <exception cref="AdvanceControlException">if  username or password is incorrect</exception>
        void UpdateFlow(string realm, XmlNode flow);

        /// <summary>
        /// Updates FTP data source.
        /// </summary>
        /// <param name="dataSource">data source object</param>
        /// <exception cref="AdvanceIOException">if network connection failed</exception>
        /// <exception cref="AdvanceXMLException">On Xml parsing error</exception>
        /// <exception cref="AdvanceControlException">if  username or password is incorrect</exception>
        void UpdateFTPDataSource(AdvanceFTPDataSource dataSource);
        /// <summary>
        /// Updates a JDBC data source.
        /// </summary>
        /// <param name="dataSource">data source object</param>
        /// <exception cref="AdvanceIOException">if network connection failed</exception>
        /// <exception cref="AdvanceXMLException">On Xml parsing error</exception>
        /// <exception cref="AdvanceControlException">if  username or password is incorrect</exception>
        void UpdateJDBCDataSource(AdvanceJDBCDataSource dataSource);
        /// <summary>
        /// Updates a JMS endpoint settings.
        /// </summary>
        /// <param name="AdvanceJMSEndpoint">endpoint object</param>
        /// <exception cref="AdvanceIOException">if network connection failed</exception>
        /// <exception cref="AdvanceXMLException">On Xml parsing error</exception>
        /// <exception cref="AdvanceControlException">if  username or password is incorrect</exception>
        void UpdateJMSEndpoint(AdvanceJMSEndpoint endpoint);

        /// <summary>
        /// Updates key store properties.
        /// </summary>
        /// <param name="keyStore">key store properties</param>
        /// <exception cref="AdvanceIOException">if network connection failed</exception>
        /// <exception cref="AdvanceXMLException">On Xml parsing error</exception>
        /// <exception cref="AdvanceControlException">if  username or password is incorrect</exception>
        void UpdateKeyStore(AdvanceKeyStore keyStore);

        /// <summary>
        /// Updates a local file data source object
        /// </summary>
        /// <param name="dataSource">data source object</param>
        /// <exception cref="AdvanceIOException">if network connection failed</exception>
        /// <exception cref="AdvanceXMLException">On Xml parsing error</exception>
        /// <exception cref="AdvanceControlException">if  username or password is incorrect</exception>
        void UpdateLocalFileDataSource(AdvanceLocalFileDataSource dataSource);

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
        void UpdateNotificationGroups(Dictionary<AdvanceNotificationGroupType, Dictionary<string, ICollection<string>>> groups);

        /// <summary>
        /// Update properties of a realm.
        /// </summary>
        /// <param name="realm">realm record</param>
        /// <exception cref="AdvanceIOException">if network connection failed</exception>
        /// <exception cref="AdvanceXMLException">On Xml parsing error</exception>
        /// <exception cref="AdvanceControlException">if  username or password is incorrect</exception>
        void UpdateRealm(AdvanceRealm realm);

        /// <summary>
        /// Updates SOAP endpoint.
        /// </summary>
        /// <param name="endpoint">new endpoint settings</param>
        /// <exception cref="AdvanceIOException">if network connection failed</exception>
        /// <exception cref="AdvanceXMLException">On Xml parsing error</exception>
        /// <exception cref="AdvanceControlException">if  username or password is incorrect</exception>
        void UpdateSOAPEndpoint(AdvanceSOAPEndpoint endpoint);

        /// <summary>
        /// Updates user's settings.
        /// </summary>
        /// <param name="user">target user object</param>
        /// <exception cref="AdvanceIOException">if network connection failed</exception>
        /// <exception cref="AdvanceXMLException">On Xml parsing error</exception>
        /// <exception cref="AdvanceControlException">if  username or password is incorrect</exception>
        void UpdateUser(AdvanceUser user);

        /// <summary>
        /// Updates a web data source.
        /// </summary>
        /// <param name="dataSource">data source object</param>
        /// <exception cref="AdvanceIOException">if network connection failed</exception>
        /// <exception cref="AdvanceXMLException">On Xml parsing error</exception>
        /// <exception cref="AdvanceControlException">if  username or password is incorrect</exception>
        void UpdateWebDataSource(AdvanceWebDataSource dataSource);

    }
}
