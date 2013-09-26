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

namespace AdvanceAPIClient.Core
{
    /// <summary>
    /// Enumeration for defining user rights.
    /// </summary>
    public enum AdvanceUserRights
    {
        /// <summary>
        /// Right to list users
        /// </summary>
        LIST_USERS,
        /// <summary>
        /// Right to create new user
        /// </summary>
        CREATE_USER,
        /// <summary>
        /// Right to modify user
        /// </summary>
        MODIFY_USER,
        /// <summary>
        /// Right to delet user
        /// </summary>
        DELETE_USER,
        /// <summary>
        /// Right to  List realm
        /// </summary>
        LIST_REALMS,
        /// <summary>
        /// Right to create new realm
        /// </summary>
        CREATE_REALM,
        /// <summary>
        /// Right to modify properties of realm
        /// </summary>
        MODIFY_REALM,
        /// <summary>
        /// Right to delete realm
        /// </summary>
        DELETE_REALM,
        /// <summary>
        /// Right to list keystores
        /// </summary>
        LIST_KEYSTORES,
        /// <summary>
        /// Right to create keystore
        /// </summary>
        CREATE_KEYSTORE,
        /// <summary>
        /// Right to delete keystore
        /// </summary>
        DELETE_KEYSTORE,
        /// <summary>
        /// Right to modify keystore
        /// </summary>
        MODIFY_KEYSTORE,
        /// <summary>
        /// Right to list keys
        /// </summary>
        LIST_KEYS,
        /// <summary>
        /// Right to delet key
        /// </summary>
        DELETE_KEY,
        /// <summary>
        /// Right to export key certificate
        /// </summary>
        EXPORT_CERTIFICATE,
        /// <summary>
        /// Right to export private key
        /// </summary>
        EXPORT_PRIVATE_KEY,
        /// <summary>
        /// Right to import key certificate
        /// </summary>
        IMPORT_CERTIFICATE,
        /// <summary>
        /// Right to import private key
        /// </summary>
        IMPORT_PRIVATE_KEY,
        /// <summary>
        /// Right to generate key 
        /// </summary> 
        GENERATE_KEY,
        /// <summary>
        /// Right to list notification groups
        /// </summary>
        LIST_NOTIFICATION_GROUPS,
        /// <summary>
        /// Right to modify existing notification groups
        /// </summary>
        MODIFY_NOTIFICATION_GROUPS,
        /// <summary>
        /// Right to list JDBC data sources
        /// </summary>
        LIST_JDBC_DATA_SOURCES,
        /// <summary>
        /// Right to create JDBC data sources
        /// </summary>
        CREATE_JDBC_DATA_SOURCE,
        /// <summary>
        /// Right to modify existing JDBC data sources
        /// </summary>
        MODIFY_JDBC_DATA_SOURCE,
        /// <summary>
        /// Right to delete JDBC data sources
        /// </summary>
        DELETE_JDBC_DATA_SOURCE,
        /// <summary>
        /// Right to list Java Messaging Endpoints
        /// </summary>
        LIST_JMS_ENDPOINTS,
        /// <summary>
        /// Right to create new Java Messaging Endpoints
        /// </summary>
        CREATE_JMS_ENDPOINT,
        /// <summary>
        /// Right to modify Java Messaging Endpoints
        /// </summary>
        MODIFY_JMS_ENDPOINT,
        /// <summary>
        /// Right to delete Java Messaging Endpoints
        /// </summary>
        DELETE_JMS_ENDPOINT,
        /// <summary>
        /// Right to list SOAP channels
        /// </summary>
        LIST_SOAP_ENDPOINTS,
        /// <summary>
        /// Right to create SOAP channels
        /// </summary>
        CREATE_SOAP_CHANNEL,
        /// <summary>
        /// Right to modify existing SOAP channels
        /// </summary>
        MODIFY_SOAP_ENDPOINT,
        /// <summary>
        /// Right to delete SOAP channels
        /// </summary>
        DELETE_SOAP_ENDPOINT,
        /// <summary>
        /// Right to list web data sources
        /// </summary>
        LIST_WEB_DATA_SOURCES,
        /// <summary>
        /// Right to create web data sources
        /// </summary>
        CREATE_WEB_DATA_SOURCE,
        /// <summary>
        /// Right to modify existing web data sources
        /// </summary>
        MODIFY_WEB_DATA_SOURCE,
        /// <summary>
        /// Right to delete web data sources
        /// </summary>
        DELETE_WEB_DATA_SOURCE,
        /// <summary>
        /// Right to list FTP data sources
        /// </summary>
        LIST_FTP_DATA_SOURCES,
        /// <summary>
        /// Right to create new FTP data sources
        /// </summary>
        CREATE_FTP_DATA_SOURCE,
        /// <summary>
        /// Right to modify existing FTP data sources
        /// </summary>
        MODIFY_FTP_DATA_SOURCE,
        /// <summary>
        /// Right to delete FTP data sources
        /// </summary>
        DELETE_FTP_DATA_SOURCE,
        /// <summary>
        /// Right to list local file data sources
        /// </summary>
        LIST_LOCAL_FILE_DATA_SOURCES,
        /// <summary>
        /// Right to create local file data sources
        /// </summary>
        CREATE_LOCAL_FILE_DATA_SOURCE,
        /// <summary>
        /// Right to modify existing local file data sources
        /// </summary>
        MODIFY_LOCAL_FILE_DATA_SOURCE,
        /// <summary>
        /// Right to delete local file data sources
        /// </summary>
        DELETE_LOCAL_FILE_DATA_SOURCE,
        /// <summary>
        /// Right to list local available blocks
        /// </summary>  
        LIST_BLOCKS,
        /// <summary>
        /// Right to list local available schemas
        /// </summary>  
        LIST_SCHEMAS,
        /// <summary>
        /// Right to create new schema
        /// </summary>  
        CREATE_SCHEMA,
        /// <summary>
        /// Right to modify existing schema
        /// </summary>  
        MODIFY_SCHEMA,
        /// <summary>
        /// Right to delete schema
        /// </summary>  
        DELETE_SCHEMA,
        /// <summary>
        /// Right to shout down engine
        /// </summary>  
        SHUTDOWN,
        /// <summary>
        /// Right to list email boxes
        /// </summary>  
        LIST_EMAIL,
        /// <summary>
        /// Right to create new email entry
        /// </summary>  
        CREATE_EMAIL,
        /// <summary>
        /// Right to modify email entry
        /// </summary>  
        MODIFY_EMAIL,
        /// <summary>
        /// Right to delete email entry
        /// </summary>  
        DELETE_EMAIL,
        /// <summary>
        /// Used for invalid right
        /// </summary>
        UNKNOWN
    };

    /// <summary>
    /// Allowed user realm rights
    /// </summary>
    public enum AdvanceUserRealmRights
    {
        /// <summary>
        /// User allowed to see realm in listings
        /// </summary>
        LIST,
        /// <summary>
        /// User allowed to start a realm
        /// </summary>
        START,
        /// <summary>
        /// User allowed to stop a realm
        /// </summary>
        STOP,
        /// <summary>
        /// User allowed to see flow within realm
        /// </summary>
        READ,
        /// <summary>
        /// User allowed to update flow within realm
        /// </summary>
        WRITE,
        /// <summary>
        /// User allowed to debug flow within realm
        /// </summary>
        DEBUG,
        /** Send or receive global inputs or outputs. */
        /// <summary>
        /// User allowed to send or receive global inputs or outputs
        /// </summary>
        IO,
        /// <summary>
        /// Used for invalid right
        /// </summary>
        UNKNOWN
    };

    /// <summary>
    /// Authentication type
    /// </summary>
    public enum AdvanceLoginType
    {
        /// <summary>
        /// No login required
        /// </summary>
        NONE,
        /// <summary>
        /// Basic 
        /// </summary>
        BASIC,
        /// <summary>
        /// Client certificate
        /// </summary>
        CERTIFICATE
    }

    /// <summary>
    ///  Notification group types
    /// </summary>
    public enum AdvanceNotificationGroupType
    {
        /// <summary>
        /// E-mail group
        /// </summary>
        EMAIL,
        ////<summary>
        /// Pager group
        /// </summary>
        PAGER,
        /// <summary>
        /// SMS group
        /// </summary>
        SMS
    }

    /// <summary>
    /// ADVANCE block state used by the block diagnostic port.
    /// </summary>
    public enum BlockState
    {
        /// <summary>
        /// Block received all inputs and is now computing
        /// </summary>
        START,
        /// <summary>
        /// Computation has finished
        /// </summary>
        FINISH,
        /// <summary>
        /// Used for unknown state
        /// </summary>
        UNKNOWN
    }

    /// <summary>
    /// Enum representing the kind of an Advance type.
    /// </summary>
    public enum TypeKind
    {
        /// <summary>
        /// A concrete and exact type, e.g., advance:integer and such.
        /// </summary>
        CONCRETE_TYPE,

        /// <summary>
        /// A concrete basetype with one or more generic type parameter, such as advance:collection.
        /// </summary>
        PARAMETRIC_TYPE,
        /// <summary>
        ///  An arbitrary type variable with optional type constraints
        /// </summary>
        VARIABLE_TYPE
    }

    /// <summary>
    /// Relation indicator.
    /// </summary>
    public enum TypeRelation
    {
        /// <summary>
        /// In <code>A.compareTo(B)</code> A extends B.
        /// </summary>
        EXTENDS,
        /// <summary>
        /// In <code>A.compareTo(B)</code> A = B.
        /// </summary>
        EQUAL,
        /// <summary>
        /// In <code>A.compareTo(B)</code> A super B &lt;=> B extends A.
        /// </summary>     
        SUPER,
        /// <summary>
        /// In <code>A.compareTo(B)</code> no direct relation is present
        /// </summary>
        NONE
    }

    public enum XCardinality
    {
        /// <summary>
        /// Not occurring (e.g., explicitly forbidden)
        /// </summary>
        ZERO,
        /// <summary>
        /// Zero or one occurrence.
        /// </summary>
        ZERO_OR_ONE,
        /// <summary>
        /// Zero or any number of occurrence
        /// </summary>
        ZERO_OR_MANY,
        /// <summary>
        /// Exactly one occurrence. 
        /// </summary>
        ONE,
        /// <summary>
        /// One or more
        /// </summary>
        ONE_OR_MANY
    }

    /// <summary>
    /// enumeration for simple XML value types.
    /// </summary>
    public enum XValueType
    {
        /// <summary>
        /// Represents binary value type
        /// </summary>
        BOOLEAN,
        /// <summary>
        /// Represents integral value type (of any precision). 
        /// </summary>
        INTEGER,
        /// <summary>
        /// Represents float value of any precision). 
        /// </summary>
        REAL,
        /// <summary>
        /// exact timestamp value
        /// </summary>
        TIMESTAMP,
        /// <summary>
        /// Represents arbitrary text type
        /// </summary>
        STRING,
        /// <summary>
        /// Represens complex type
        /// </summary>
        COMPLEX
    }

    /// <summary>
    /// Key type within the keystore.
    /// </summary>
    public enum AdvanceKeyType
    {
        /// <summary>
        /// Certificate
        /// </summary>
        CERTIFICATE,
        /// <summary>
        /// Private key
        /// </summary>
        PRIVATE_KEY
    }

    /// <summary>
    ///  enumeration of email box protocols.
    /// </summary>
    public enum AdvanceEmailReceiveProtocols
    {
        /// <summary>
        /// box will not receive emails. 
        /// </summary>
        NONE,
        /// <summary>
        /// Post Office Protocol v3.
        /// </summary>
        POP3,
        /// <summary>
        /// Post Office Protocol v3 over SSL.
        /// </summary>
        POP3S,
        /// <summary>
        /// IMAP
        /// </summary>
        IMAP,
        /// <summary>
        /// IMAP over SSL
        /// </summary>
        IMAPS
    }

    /// <summary>
    /// enumeration for protocols of sending email.
    /// </summary>
    public enum AdvanceEmailSendProtocols
    {
        /// <summary>
        /// box will not send emails.
        /// </summary>
        NONE,
        /// <summary>
        /// Simple Mail Transfer Protocol
        /// </summary>
        SMTP,
        /// <summary>
        /// Simple Mail Transfer Protocol over SSL
        /// </summary>
        SMTPS
    }

    /// <summary>
    /// File transfer protocols supported by ADVANCE
    /// </summary>
    public enum AdvanceFTPProtocols
    {
        /// <summary>
        /// Regular FTP
        /// </summary>
        FTP,
        /// <summary>
        /// FTP over SSL
        /// </summary>
        FTPS,
        /// <summary>
        /// SSH File Transfer protocol
        /// </summary>
        SFTP
    }

    /// <summary>
    /// status enumeration of the ADVANCE Flow Engine Realm.
    /// </summary>
    public enum AdvanceRealmStatus
    {
        /// <summary>
        /// realm is stopped
        /// </summary>
        STOPPED,
        /// <summary>
        /// realm is about to stop.
        /// </summary>

        STOPPING,
        /// <summary>
        /// realm is executing a flow
        /// </summary>
        RUNNING,
        /// <summary>
        /// realm is about to run
        /// </summary>
        STARTING,
        /// <summary>
        /// Indicates that realm was running when last shutdown happened and will automatically resume. 
        /// </summary>
        RESUME,
        /// <summary>
        /// realm verification failed 
        /// </summary>
        ERROR
    }

}
