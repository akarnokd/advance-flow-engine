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
using System.Globalization;
using System.Xml;

using AdvanceAPIClient.Core;

namespace AdvanceAPIClient.Classes
{
    /// <summary>
    /// creation/modification time and user information
    /// </summary>
    public abstract class AdvanceCreateModifyInfo : XmlReadWrite
    {
        /// <summary>
        /// Creation timestamp of object.
        /// </summary>
        public DateTime CreatedAt = new DateTime(0);
        /// <summary>
        /// Modification timestamp of the object.
        /// </summary>
        public DateTime ModifiedAt = new DateTime(0);
        /// <summary>
        /// Creator user
        /// </summary>
        public string CreatedBy;
        /// <summary>
        /// User who made last modification
        /// </summary>
        public string ModifiedBy;

        public AdvanceCreateModifyInfo() : base() { }

        /// <summary>
        /// Returns the password characters from the encoded attribute in the given source element
        /// </summary>
        /// <param name="source">source element</param>
        /// <param name="attrname">attribute name</param>
        /// <returns>password or null if no password</returns>
        public static char[] GetPassword(XmlNode source, string attrname)
        {
            string pwd = GetAttribute(source, attrname, null);
            return DecodePassword(pwd);
        }

        /// <summary>
        /// Decodes a Base64 encoded password
        /// </summary>
        /// <param name="codedPwd">raw password</param>
        /// <returns>password</returns>
        protected static char[] DecodePassword(string codedPwd)
        {
            if (codedPwd != null)
            {
                try
                {
                    byte[] bytes = Convert.FromBase64String(codedPwd);
                    return Encoding.UTF8.GetString(bytes).ToCharArray();
                }
                catch (Exception ex)
                {
                    Log.LogException(ex);
                }
            }
            return null;
        }

        /// <summary>
        /// Encodes a Base64 encoded password
        /// </summary>
        /// <param name="pwd">raw password</param>
        /// <returns>password</returns>
        protected static string EncodePassword(char[] pwd)
        {
            if (pwd != null)
            {
                try
                {
                    byte[] bytes = Encoding.UTF8.GetBytes(pwd);
                    return Convert.ToBase64String(bytes);
                }
                catch (Exception ex)
                {
                    Log.LogException(ex);
                }
            }
            return null;
        }

        /// <summary>
        /// Encodes the password into the given {@code source} element under the given {@code name}
        /// </summary>
        /// <param name="destination">destination Xml</param>
        /// <param name="attrname">attribute name</param>
        /// <param name="password">password characters</param>
        public static void SetPassword(XmlElement destination, string attrname, char[] password)
        {
            if (password != null)
                AddAttribute(destination, attrname, EncodePassword(password));
        }

        /// <summary>
        /// Fill object data from xml node
        /// </summary>
        /// <param name="source">Source Xml</param>
        protected override void LoadFromXmlNode(XmlNode source)
        {
            this.CreatedAt = GetDateTimeAttribute(source, "created-at");
            this.CreatedBy = GetAttribute(source, "created-by", null);
            this.ModifiedAt = GetDateTimeAttribute(source, "modified-at");
            this.ModifiedBy = GetAttribute(source, "modified-by", null);
        }

        /// <summary>
        /// Fills  object's xml
        /// </summary>
        /// <param name="node">Empty node for the object with name "tagname"</param>
        protected override void FillXmlElement(XmlElement node)
        {
            AddDateTimeAttribute(node, "created-at", this.CreatedAt);
            AddAttribute(node, "created-by", this.CreatedBy);
            AddDateTimeAttribute(node, "modified-at", this.ModifiedAt);
            AddAttribute(node, "modified-by", this.ModifiedBy);
        }

        /// <summary>
        /// Assign the administrative values to the other record.
        /// </summary>
        /// <param name="other"></param>
        public void AssignTo(AdvanceCreateModifyInfo other)
        {
            other.CreatedAt = new DateTime(this.CreatedAt.Ticks);
            other.CreatedBy = this.CreatedBy;
            other.ModifiedAt = new DateTime(this.ModifiedAt.Ticks);
            other.ModifiedBy = this.ModifiedBy;
        }

    }
}
