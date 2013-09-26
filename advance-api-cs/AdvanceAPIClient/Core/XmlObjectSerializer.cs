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
using System.Collections;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Xml;
using System.Reflection;

namespace AdvanceAPIClient.Core
{
    /// <summary>
    /// Szekvenciáis és struktúra írás közötti átmenetet biztosító osztály
    /// </summary>
    public class XmlObjectSerializer : XmlReadWrite
    {
        private object content;

        public XmlObjectSerializer() : base() { }

        public XmlObjectSerializer(object content) : base()  
        {
            this.content = content;
        }

        /// <summary>
        /// Fill object data from xml node
        /// </summary>
        /// <param name="source"></param>
        protected override void LoadFromXmlNode(XmlNode source)
        {
            this.content = GetObject(source);
        }

        /// <summary>
        /// Fills object's xml
        /// </summary>
        /// <param name="node">Empty node for the object with name "tagname"</param>
        protected override void FillXmlElement(XmlElement node)
        {
            AddObject(node, this.TagName, this.content);
        }

        public static void AddObject(XmlElement node, string tagName, object obj)
        {
            XmlElement objNode = AddAttributeNode(node, tagName, "otype", obj.GetType().FullName);
            if (obj is XmlReadWrite)
                (obj as XmlReadWrite).AddToElement(objNode);
            else if (obj is XmlNode)
            {
                XmlNode xNode = (obj is XmlDocument) ? (obj as XmlDocument).DocumentElement : (obj as XmlNode);
                node.AppendChild(node.OwnerDocument.ImportNode(xNode, true));
            }
            else if (obj is IList)
            {
                foreach (var subObj in (obj as IList))
                    AddObject(objNode, "element", subObj);
            }
            else if (obj is IDictionary)
            {
                foreach (KeyValuePair<object, object> subObj in (obj as IDictionary))
                {
                    XmlElement kvpNode = AddNode(objNode, "key-value-pair");
                    AddObject(kvpNode, "key", subObj.Key);
                    AddObject(kvpNode, "value", subObj.Value);
                }
            }
            else
                AddContent(objNode, obj.ToString());
        }

        public static object GetObject(XmlNode node)
        {
            string typeStr = GetAttribute(node, "otype", typeof(string).FullName);
            Type oType = Type.GetType(typeStr);
            if (oType.IsPrimitive)
                return Convert.ChangeType(GetContent(node, ""), oType);
            else
            {
                object ret = Activator.CreateInstance(oType);
                if (ret is XmlNode)
                {
                    XmlNode xNode = GetChildNode(node, "*");
                    if (ret is XmlDocument)
                        (ret as XmlDocument).AppendChild((ret as XmlDocument).ImportNode(xNode, true));
                    else
                        return xNode;
                }
                if (ret is XmlReadWrite)
                    return CreateFromXml(oType, node);
                else if (ret is IList)
                    foreach (XmlNode subNode in GetChildren(node, "element"))
                        (ret as IList).Add(GetObject(subNode));
                else if (ret is IDictionary)
                    foreach (XmlNode subNode in GetChildren(node, "key-value-pair"))
                    {
                        var key = GetObject(GetChildNode(subNode, "key"));
                        var val = GetObject(GetChildNode(subNode, "value"));
                        (ret as IDictionary).Add(key, val);
                    }
                else
                {
                    ConstructorInfo cInf = oType.GetConstructor(new Type[] { typeof(string) });
                    if (cInf != null)
                        return cInf.Invoke(new object[] { GetContent(node, null) });
                }
                return ret;
            }
        }

        private static void AddLine(ref string msg, params object[] parts) 
        {
            msg += string.Join("", parts) + "\r\n";
        }

        public static string GetObjectString(string pref, object obj)
        {
            string ret = "";
            AddLine (ref ret, pref, "**** START:", obj.GetType().Name);
            if (obj is XmlNode)
            {
                XmlNode xNode = (obj is XmlDocument) ? (obj as XmlDocument).DocumentElement : (obj as XmlNode);
                AddLine(ref ret, xNode.InnerText);
            }
            else if (obj is IList)
            {
                AddLine(ref ret, pref, "Count=", (obj as IList).Count);
                foreach (var subObj in (obj as IList))
                     AddLine(ref ret, pref, "--- element:" + GetObjectString(pref + "..", subObj));
            }
            else if (obj is IDictionary)
            {
                AddLine(ref ret, pref, "Count=" + (obj as IDictionary).Count);
                foreach (KeyValuePair<object, object> subObj in (obj as IDictionary))
                {
                    AddLine(ref ret, pref, "--- key:" + GetObjectString(pref + "..", subObj.Key));
                    AddLine(ref ret, pref, "--- value:" + GetObjectString(pref + "..", subObj.Value));
                }
            }
            else
                AddLine(ref ret, pref, obj);
            AddLine(ref ret, pref, "**** END:", obj.GetType().Name);
            return ret;
        }

        public override string ToString()
        {
            return GetObjectString("", this.content);
        }

    }
}
