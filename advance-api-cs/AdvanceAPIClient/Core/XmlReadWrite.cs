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
using System.Text;
using System.Linq;
using System.IO;
using System.Xml;
using System.Collections;
using System.Collections.Generic;
using System.Runtime.Serialization;


namespace AdvanceAPIClient.Core
{
    /// <summary>
    /// Abstract class of XML serializable classes
    /// </summary>
    abstract public class XmlReadWrite
    {
        /// <summary>
        /// XML tag name. 
        /// </summary>
        public virtual string TagName { get { return (this.tagName == null) ? this.GetType().Name : this.tagName; ; } }
        protected string tagName;

        /// <summary>
        /// Simple constructor.  
        /// </summary>
        /// <param name="tagName">XML tag name</param>
        public XmlReadWrite() {}
 
        /// <summary>
        /// Fills object data from Xml node
        /// </summary>
        /// <param name="source"></param>
        public void ReadXml(XmlNode source)
        {
            if (source != null)
            {
                this.tagName = source.Name;
                this.LoadFromXmlNode(source);
            }
            else
                this.ThrowException("Missing or invalid root tag");
        }

        /// <summary>
        /// Fills object data from Xml file
        /// </summary>
        /// <param name="fileName">File name</param>
        public void ReadFromFile(string fileName)
        {
            if (File.Exists(fileName))
            {
                using (Stream rs = File.OpenRead(fileName))
                {
                    XmlNode root = ReadFromStream(rs);
                    this.ReadXml(root);
                }
            }
        }

        /// <summary>
        /// Writes object's data to Xml file
        /// </summary>
        /// <param name="fileName">File name</param>
        public void WriteToFile(string fileName)
        {
            this.WriteToStream(File.Create(fileName));
        }

        /// <summary>
        /// Writes object's data to Xml stream
        /// </summary>
        /// <param name="ws">Stream writer</param>
        public void WriteToStream(Stream ws)
        {
                XmlDocument doc = new XmlDocument();
                this.AddToXML(this.TagName, doc);
                WriteToStream(true, doc, ws);
        }

        /// <summary>
        /// Adds new node to the xml with object data
        /// </summary>
        /// <param name="tagName">actual tag name</param>
        /// <param name="parentNode">Parent XML element</param>
        public XmlElement AddToXML(string tagName, XmlNode parentNode)
        {
            XmlDocument doc = (parentNode is XmlDocument) ? (XmlDocument)parentNode : (parentNode as XmlElement).OwnerDocument;
            XmlElement node = doc.CreateElement(tagName);
            this.FillXmlElement(node);
            parentNode.AppendChild(node);
            return node;
        }

        /// <summary>
        /// Adds object data to existing Xml element
        /// </summary>
        /// <param name="node">XML element</param>
        public void AddToElement(XmlElement node)
        {
            this.FillXmlElement(node);
        }

        /// <summary>
        /// ??? used for exception messages
        /// </summary>
        /// <param name="node">Xml nude</param>
        /// <returns>Information string</returns>
        public static string GetXPath(XmlNode node)
        {
            return (node == null) ? "" : node.Name;
        }

        /// <summary>
        /// Throw AdvanceXMLException with message
        /// </summary>
        /// <param name="message">message</param>
        protected void ThrowException(string message)
        {
            throw new AdvanceXMLException(message);
        }

        /// <summary>
        /// Throw AdvanceXMLException with message
        /// </summary>
        /// <param name="message">message</param>
        protected void ThrowException(string messageformat, params string[] pars)
        {
            this.ThrowException(string.Format(messageformat, pars));
        }

        /// <summary>
        /// Throw AdvanceXMLException with message and inner exception
        /// </summary>
        /// <param name="message">message</param>
        /// <param name="e">inner exception</param>
        protected void ThrowException(string message, Exception e)
        {
            throw new AdvanceXMLException(message, e);
        }

        /// <summary>
        /// Throw AdvanceXMLException for Duplicated identifier
        /// </summary>
        /// <param name="node">node</param>
        /// <param name="id">duplicated id</param>
        protected void ThrowDuplicatedIdentifierException(XmlNode node, string id)
        {
            this.ThrowException("Duplicated identifier in " + GetXPath(node) + ": " + id);
        }

        /// <summary>
        /// Throw AdvanceXMLException for missing type variable identifier
        /// </summary>
        /// <param name="node">node</param>
        /// <param name="id">missing id</param>
        protected void ThrowMissingTypeVariableException(XmlNode node, string id)
        {
            this.ThrowException("Missing Type Variable in " + GetXPath(node) + ": " + id);
        }

        /// <summary>
        /// Throw AdvanceXMLException for Unresolvable Schema Uri
        /// </summary>
        /// <param name="schemaUri">Unresolvable schema uri</param>
        protected void ThrowUnresolvableSchemaUriException(Uri schemaUri)
        {
            this.ThrowException("Unresolvable Schema Uri: " + schemaUri);
        }

        /// <summary>
        /// Load object data from xml node
        /// </summary>
        /// <param name="source"></param>
        protected abstract void LoadFromXmlNode(XmlNode source);

        /// <summary>
        /// Fills object's xml
        /// </summary>
        /// <param name="node">Empty node for the object with name "tagname"</param>
        protected abstract void FillXmlElement(XmlElement node);

        /// <summary>
        /// Read XML attribute
        /// </summary>
        /// <param name="node">Source Xml node</param>
        /// <param name="attr">Attribute name</param>
        /// <param name="defValue">Default value</param>
        /// <returns>Attribute actual value</returns>
        public static string GetAttribute(XmlNode node, string attr, string defValue)
        {
            XmlAttribute attribute = node.Attributes[attr];
            if (attribute == null || attribute.Value == null)
                return defValue;
            else
                return attribute.Value;
        }

        /// <summary>
        /// Read mandatory XML attribute
        /// </summary>
        /// <param name="node">Source Xml node</param>
        /// <param name="attr">Attribute name</param>
        /// <returns>Attribute actual value</returns>
        public static string GetAttribute(XmlNode node, string attr)
        {
            XmlAttribute attribute = node.Attributes[attr];
            if (attribute == null || attribute.Value == null)
                throw new AdvanceXMLException(string.Format("Missing mandatory attribute '{0}' from {1} tag", attr, node.Name));
            else
                return attribute.Value;
        }


        /// <summary>
        /// Add attribute to Xml node
        /// </summary>
        /// <param name="node">Target Xml node</param>
        /// <param name="attr">Attribute name</param>
        /// <param name="value">Attribute value</param>
        public static void AddAttribute(XmlElement node, string attr, object value)
        {
            if (value != null)
            {
                if (value is bool)
                    AddBoolAttribute(node, attr, (bool)value);
                else
                    node.SetAttribute(attr, value.ToString());
            }
        }

        /// <summary>
        /// Add attribute to Xml node
        /// </summary>
        /// <param name="node">Target Xml node</param>
        /// <param name="attr">Attribute name</param>
        /// <param name="value">Attribute value</param>
        public static void AddBoolAttribute(XmlElement node, string attr, bool value)
        {
            node.SetAttribute(attr, value ? "true" : "false");
        }

        private static bool GetBoolValue(string val, bool defValue)
        {
            if (!string.IsNullOrEmpty(val))
                return
                   val.ToLower() == "true" ||
                   val == "1" ||
                   val.ToLower()[0] == 'y';
            else
                return defValue;

        }

        /// <summary>
        /// Read integer attribute
        /// </summary>
        /// <param name="node">Source Xml node</param>
        /// <param name="attr">Attribute name</param>
        /// <param name="defValue">Default value</param>
        /// <returns>Actual attribute value</returns>
        public static bool GetBoolAttribute(XmlNode node, string attr, bool defValue)
        {
            XmlAttribute attribute = node.Attributes[attr];
            if (attribute != null)
                return GetBoolValue(attribute.Value, defValue);
            else
                return defValue;
        }
         
        /// <summary>
        /// Read integer attribute
        /// </summary>
        /// <param name="node">Source Xml node</param>
        /// <param name="defValue">Default value</param>
        /// <returns>Actual attribute value</returns>
        public static bool GetBoolContent(XmlNode node, bool defValue)
        {
            return GetBoolValue(GetContent(node, null), defValue);
        }


        /// <summary>
        /// Read and check uri attribute
        /// </summary>
        /// <param name="node">Target Xml node</param>
        /// <param name="attr">Attribute name</param>
        /// <returns>Uri or null</returns>
        public static Uri GetUriAttribute(XmlNode node, string attr)
        {
            string s = GetAttribute(node, "documentation", null);
            if (s != null)
            {
                try
                {
                    return new Uri(s);
                }
                catch (Exception e)
                {
                    throw new AdvanceXMLException("Invalid documentation uri: " + s, e);
                }
            }
            else
                return null;
        }

        /// <summary>
        /// Read integer attribute
        /// </summary>
        /// <param name="node">Source Xml node</param>
        /// <param name="attr">Attribute name</param>
        /// <param name="defValue">Default value</param>
        /// <returns>Actual attribute value</returns>
        public static int GetIntAttribute(XmlNode node, string attr, int defValue)
        {
            int val;
            XmlAttribute attribute = node.Attributes[attr];
            if ((attribute != null) && !String.IsNullOrEmpty(attribute.Value) &&
                int.TryParse(attribute.Value, out val))
                return val;
            else
                return defValue;
        }

        /// <summary>
        /// Read mandatory integer attribute
        /// </summary>
        /// <param name="node">Source Xml node</param>
        /// <param name="attr">Attribute name</param>
        /// <returns>Actual attribute value</returns>
        public static int GetIntAttribute(XmlNode node, string attr)
        {
            string s = GetAttribute(node, attr);
            int val;
            if (String.IsNullOrEmpty(s) || !int.TryParse(s, out val))
                throw new AdvanceXMLException(string.Format("Invalid integer attribute '{0}={1}' in {2} tag", attr, s, node.Name));
            else
                return val;
        }

        /// <summary>
        /// Add string list attribute to Xml node
        /// </summary>
        /// <param name="node">Target Xml node</param>
        /// <param name="attr">Attribute name</param>
        /// <param name="value">Attribute list value</param>
        public void AddListAttribute(XmlElement node, string attr, List<string> value)
        {
            if (value.Count > 0)
                AddAttribute(node, attr, string.Join(",", value));
        }

        /// <summary>
        /// Read coma separeted string list attribute
        /// </summary>
        /// <param name="node">Source Xml node</param>
        /// <param name="attr">Attribute name</param>
        /// <returns>List of strings</returns>
        public List<string> GetListAttribute(XmlNode node, string attr)
        {
            string attrval = GetAttribute(node, attr, null);
            string s = GetAttribute(node, attr, null);
            List<string> ret = new List<string>();
            if (s != null)
                foreach (string x in s.Split(','))
                    ret.Add(x.Trim());
            return ret;
        }

        /// <summary>
        /// Read datetime attribute
        /// </summary>
        /// <param name="node">Source Xml node</param>
        /// <param name="attr">Attribute name</param>
        /// <param name="defValue">Default value</param>
        /// <returns>Actual attribute value</returns>
        public DateTime GetDateTimeAttribute(XmlNode node, string attr, DateTime defValue)
        {
            string attrval = GetAttribute(node, attr, null);
            DateTime dt;
            if (attrval != null && DateTime.TryParse(attrval, out dt))
                return dt;
            else
                return defValue;
        }

        /// <summary>
        /// Read datetime attribute
        /// </summary>
        /// <param name="node">Source Xml node</param>
        /// <param name="attr">Attribute name</param>
        /// <param name="defValue">Default value</param>
        /// <returns>Actual attribute value</returns>
        public DateTime GetDateTimeAttribute(XmlNode node, string attr)
        {
            string attrval = GetAttribute(node, attr);
            DateTime dt;
            if (attrval != null && DateTime.TryParse(attrval, out dt))
                return dt;
            else
                throw new AdvanceXMLException(string.Format("Invalid date/time attribute '{0}={1}' in {2} tag", attr, attrval, node.Name));

        }

        /// <summary>
        /// Add datetime attribute
        /// </summary>
        /// <param name="node">Target Xml node</param>
        /// <param name="attr">Attribute name</param>
        /// <param name="value">Default value</param>
        /// <returns>Actual attribute value</returns>
        public void AddDateTimeAttribute(XmlElement node, string attr, DateTime value)
        {
            AddAttribute(node, attr, value.ToString());
        }

        /// <summary>
        /// Read enum type attribute
        /// </summary>
        /// <param name="node">Xml node</param>
        /// <param name="attr">attribute name</param>
        /// <param name="defValue">Default value</param>
        /// <returns>Actual attribute value</returns>
        public static T GetEnumAttribute<T>(XmlNode node, string attr, T defValue) 
        {
            XmlAttribute attribute = node.Attributes[attr];
            if ((attribute != null) && !String.IsNullOrEmpty(attribute.Value))
                return Utils.NameToEnum<T>(attribute.Value, defValue);
            else
                return defValue;
        }

        /// <summary>
        /// Get child nodes with given tagname
        /// </summary>
        /// <param name="node">Xml-node</param>
        /// <param name="tagName">Tag name or *</param>
        /// <returns>Gyerek node-ok</returns>
        public static XmlNodeList GetChildren(XmlNode node, string tagName)
        {
            return node.SelectNodes("./" + tagName);
        }

        /// <summary>
        /// Get single child node with given tagname 
        /// </summary>
        /// <param name="node">Xml-node</param>
        /// <param name="tagName">Tag name</param>
        /// <returns>Child node</returns>
        public static XmlNode GetChildNode(XmlNode node, string tagName)
        {
            return node.SelectSingleNode("./" + tagName);
        }

        /// <summary>
        /// Adds text content to existing node
        /// </summary>
        /// <param name="node">Xml element</param>
        /// <param name="str">Content</param>
        public static void AddContent(XmlElement node, string str)
        {
            if (str != null)
            {
                if (str.IndexOfAny(new char[] { '<', '>', '\'', '"', '&' }) >= 0)
                    node.AppendChild(node.OwnerDocument.CreateCDataSection(str));
                else
                    node.AppendChild(node.OwnerDocument.CreateTextNode(str));
            }
        }

        /// <summary>
        /// Get Xml node content
        /// </summary>
        /// <param name="node">Xml node</param>
        /// <param name="defValue">Default value</param>
        /// <returns>Content</returns>
        public static string GetContent(XmlNode node, string defValue)
        {
            XmlNode text = node.SelectSingleNode("./text()");
            if ((text == null) || String.IsNullOrEmpty(text.Value.Trim()))
                return defValue;
            else
                return text.Value.Trim();
        }

        /// <summary>
        /// Get child Xml node content
        /// </summary>
        /// <param name="node">Xml node</param>
        /// <param name="childName">Child node namee</param>
        /// <param name="defValue">Default value</param>
        /// <returns>Child content</returns>
        public static string GetChildContent(XmlNode node, string childName, string defValue)
        {
            XmlNode child = node.SelectSingleNode("./" + childName + "/text()");
            if ((child == null) || String.IsNullOrEmpty(child.Value.Trim()))
                return defValue;
            else
                return child.Value.Trim();
        }

        /// <summary>
        /// Add node with content
        /// </summary>
        /// <param name="node">Parent Xml node</param>
        /// <param name="tagName">Node name</param>
        /// <param name="str">Content</param>
        /// <returns>Added element</returns>
        public static XmlElement AddTextNode(XmlElement node, string tagName, string str)
        {
            XmlElement newNode = null;
            if (!String.IsNullOrEmpty(str))
            {
                newNode = CreateTextNode(node.OwnerDocument, tagName, str);
                node.AppendChild(newNode);
            }
            return newNode;
        }

        /// <summary>
        /// Add node with attribute
        /// </summary>
        /// <param name="node">Parent Xml node</param>
        /// <param name="tagName">Node name</param>
        /// <param name="attrName">Attribute name/param>
        /// <param name="str">Attribute value</param>
        /// <returns>Added element</returns>
        public static XmlElement AddAttributeNode(XmlNode node, string tagName, string attrName, object val)
        {
            XmlElement newNode = null;
            if (val != null)
            {
                newNode = node.OwnerDocument.CreateElement(tagName);
                newNode.SetAttribute(attrName, val.ToString());
                node.AppendChild(newNode);
            }
            return newNode;
        }

        /// <summary>
        /// Add empty node 
        /// </summary>
        /// <param name="node">Parent Xml node</param>
        /// <param name="tagName">Node name</param>
        /// <returns>Added element</returns>
        public static XmlElement AddNode(XmlNode node, string tagName)
        {
            XmlElement newNode = ((node is XmlDocument) ? (node as XmlDocument) : node.OwnerDocument).CreateElement(tagName);
            node.AppendChild(newNode);
            return newNode;
        }


        /// <summary>
        /// Creates a text node
        /// </summary>
        /// <param name="doc">Xml document</param>
        /// <param name="tagName">Tag name</param>
        /// <param name="str">Content</param>
        /// <returns>Xml element</returns>
        public static XmlElement CreateTextNode(XmlDocument doc, string tagName, string str)
        {
            XmlElement text = doc.CreateElement(tagName);
            AddContent(text, str);
            return text;
        }
   
        /// <summary>
        /// Deep copy of object using Xml serialization
        /// </summary>
        /// <typeparam name="T">Extension of XmlReadWrite type</typeparam>
        /// <param name="source">Object to copy</param>
        /// <returns>New object</returns>
        public static T Copy<T>(T source) where T : XmlReadWrite, new()
        {
            XmlDocument doc = new XmlDocument();
            source.AddToXML(source.TagName, doc);
            T target = new T();
            target.LoadFromXmlNode(doc.DocumentElement);
            return target;
        }

        /// <summary>
        /// Writes xml document to output stram
        /// </summary>
        /// <param name="fullDoc">Create full xml document with declaration and formatting</param>
        /// <param name="root">Root xml node</param>
        /// <param name="writer">Output stream writer</param>
        public static void WriteToStream(bool fullDoc, XmlNode root, Stream writer)
        {
            XmlWriterSettings settings = new XmlWriterSettings();
            settings.Indent = fullDoc;
            settings.IndentChars = ("\t");
            settings.OmitXmlDeclaration = !fullDoc;
            settings.CloseOutput = true;
            settings.Encoding = Encoding.UTF8;
            XmlWriter xw = XmlWriter.Create(writer, settings);
            try
            {
                XmlDocument doc;
                if (root is XmlDocument)
                    doc = root as XmlDocument;
                else
                {
                    doc = new XmlDocument();
                    root.AppendChild(root);
                }
                doc.Save(xw);
            }
            finally
            {
                xw.Close();
            }
        }

        /// <summary>
        /// Get root node from input stream
        /// </summary>
        /// <param name="reader"></param>
        /// <returns></returns>
        public static XmlNode ReadFromStream(Stream reader)
        {
            try
            {
                XmlDocument doc = new XmlDocument();
                doc.Load(reader);
                return doc.DocumentElement;
            }
            finally
            {
                reader.Close();
            }
        }

        /// <summary>
        /// Reads next xml fregment from xml stream
        /// </summary>
        /// <param name="reader">Xml reader</param>
        /// <returns>Xml node</returns>
        public static XmlNode ReadFregment(XmlReader reader)
        {
            if (!reader.EOF)
            {
                XmlDocument doc = new XmlDocument();
                XmlDocumentFragment xdf = doc.CreateDocumentFragment();
                xdf.InnerXml = reader.ReadOuterXml();
                return xdf;
            }
            else
                return null;
         }

        /// <summary>
        /// Create function request Xml document with no parameter
        /// </summary>
        /// <param name="funcName">Function name</param>
        /// <returns>Xml document</returns>
        public static XmlDocument CreateFunctionRequest(string funcName)
        {
            XmlDocument doc = new XmlDocument();
            XmlElement root = doc.CreateElement(funcName);
            doc.AppendChild(root);
            return doc;
        }

        /// <summary>
        /// Create function request Xml document with content parameter
        /// </summary>
        /// <param name="funcName">Function name</param>
        /// <param name="content">Xml string content</param>
        /// <returns>Xml document</returns>
        public static XmlDocument CreateFunctionRequest(string funcName, string content)
        {
            XmlDocument doc = new XmlDocument();
            doc.AppendChild(CreateTextNode(doc, funcName, content));
            return doc;
        }

        /// <summary>
        /// Create function request Xml document with complex and simple attribute parameters
        /// </summary>
        /// <param name="funcName">Function name</param>
        /// <param name="pars">Complex parameters</param>
        /// <param name="attrs">Simple attributes</param>
        /// <returns>Xml document</returns>
        public static XmlDocument CreateFunctionRequest(string funcName, IList<XmlReadWrite> pars, Dictionary<string, object> attrs)
        {
            XmlDocument doc = CreateFunctionRequest(funcName);
            if (attrs != null)
                foreach (KeyValuePair<string, object> a in attrs)
                    AddAttribute(doc.DocumentElement, a.Key, a.Value);
            if (pars != null)
                foreach (XmlReadWrite p in pars)
                    p.AddToXML(p.TagName, doc.DocumentElement);
            return doc;
        }

        /// <summary>
        /// Create function request Xml document with one or more complex parameter
        /// </summary>
        /// <param name="funcName">Function name</param>
        /// <param name="pars">One or more complex parameter</param>
        /// <returns>Xml document</returns>
        public static XmlDocument CreateFunctionRequest(string funcName, params XmlReadWrite[] pars)
        {
            return CreateFunctionRequest(funcName, pars, null);
        }

        /// <summary>
        /// Create function request Xml document with one simple attribute parameter
        /// </summary>
        /// <param name="funcName">Function name</param>
        /// <param name="attrName">Prameter attribute name</param>
        /// <param name="attrVal">Parameter value</param>
        /// <returns>Xml document</returns>
        public static XmlDocument CreateFunctionRequest(string funcName, string attrName, object attrVal)
        {
            XmlDocument doc = CreateFunctionRequest(funcName);
            AddAttribute(doc.DocumentElement, attrName, attrVal);
            return doc;
        }

        /// <summary>
        /// Returns node name or document's root element name 
        /// </summary>
        /// <param name="node">Xml node</param>
        /// <returns>Name</returns>
        public static string RootName(XmlNode node)
        {
            return (node is XmlDocument) ? (node as XmlDocument).DocumentElement.Name : node.Name;
        }

        /// <summary>
        /// Creates object from xml node
        /// </summary>
        /// <typeparam name="T">Object class</typeparam>
        /// <param name="source">Source Xml node</param>
        /// <returns>Object</returns>
        public static T CreateFromXml<T>(XmlNode source) where T : XmlReadWrite, new()
        {
            T ret = new T();
            ret.tagName = source.Name;
            ret.LoadFromXmlNode(source);
            return ret;
        }

        /// <summary>
        /// Creates object from xml node
        /// </summary>
        /// <typeparam name="T">Object class</typeparam>
        /// <param name="source">Source Xml node</param>
        /// <returns>Object</returns>
        public static object CreateFromXml(Type T, XmlNode source)
        {
            if (T.Equals(typeof(XmlNode)) ||
                (T.Equals(typeof(XmlElement)) && (source is XmlElement)) ||
                (T.Equals(typeof(XmlDocument)) && (source is XmlDocument)))
                return source;
            else if (T.Equals(typeof(XmlElement)) && (source is XmlDocument))
                return (source as XmlDocument).DocumentElement;
            else
            {
                object ret = (XmlReadWrite)Activator.CreateInstance(T);
                if (ret is XmlReadWrite)
                {
                    (ret as XmlReadWrite).tagName = source.Name;
                    (ret as XmlReadWrite).LoadFromXmlNode(source);
                }
                return ret;
            }
        }

        /// <summary>
        /// Writes object's data to file
        /// </summary>
        /// <param name="path">File path</param>
        /// <param name="obj">Object to serialize</param>
        public static void WriteToFile(string path, object obj)
        {
            if (obj is XmlReadWrite)
                (obj as XmlReadWrite).WriteToFile(path);
            else 
               new XmlObjectSerializer(obj).WriteToFile(path);
        }

        /// <summary>
        /// Appends object's data to stream
        /// </summary>
        /// <param name="stream">Outpur stream</param>
        /// <param name="obj">Object to serialize</param>
        public static void AddToStream(Stream stream, object obj)
        {
            XmlReadWrite xmlobj = (obj is XmlReadWrite) ? (obj as XmlReadWrite) : new XmlObjectSerializer(obj);
            MemoryStream ms = new MemoryStream();
            XmlDocument doc = new XmlDocument();
            xmlobj.AddToXML(xmlobj.TagName, doc);
            WriteToStream(false, doc, ms); 
            byte[] bytes = ms.ToArray();
            stream.Write(bytes, 0, bytes.Length);
        }

        /// <summary>
        /// Creates object list from xml node
        /// </summary>
        /// <typeparam name="T">Object class</typeparam>
        /// <param name="source">Source Xml node</param>
        /// <param name="tagName">object tag name</param>
        /// <returns>Object list</returns>
        public static List<T> CreateListFromXml<T>(XmlNode source, string groupName, string tagName) where T : XmlReadWrite, new()
        {
            List<T> ret = new List<T>();
            XmlNode groupNode = groupName.Equals(source.Name) ? source : GetChildNode(source, groupName);
            if (groupNode != null)
            {
                foreach (XmlNode node in GetChildren(groupNode, tagName))
                    ret.Add(CreateFromXml<T>(node));
            }
            return ret;
        }

        /// <summary>
        /// Creates Xml string from object's data
        /// </summary>
        /// <returns>object sring</returns>
        public override string ToString()
        {
            MemoryStream ms = new MemoryStream();
            this.WriteToStream(ms);
            byte[] bytes = ms.ToArray();
            UTF8Encoding enc = new UTF8Encoding();
            return enc.GetString(bytes);
        }

    }

 }
