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
using AdvanceAPIClient.Classes.Model;

namespace AdvanceAPIClient.Classes
{
    /// <summary>
    /// composite block description for the flow description of {@code flow-description.xsd}.
    /// </summary>
    public class AdvanceNotificationGroup : XmlReadWrite
    {
        public AdvanceNotificationGroupType Type;

        public string Name;

        public List<string> Contacts = new List<string>();

        public AdvanceNotificationGroup() : base() { }

        public AdvanceNotificationGroup(AdvanceNotificationGroupType type, string name, ICollection<string> contacts)
        {
            this.Type = type;
            this.Name = name;
            this.Contacts = new List<string>(contacts);
        }

        /// <summary>
        /// Fill object data from xml node
        /// </summary>
        /// <param name="source">Source Xml</param>
        protected override void LoadFromXmlNode(XmlNode source)
        {
            this.Type = GetEnumAttribute<AdvanceNotificationGroupType>(source, "type", AdvanceNotificationGroupType.EMAIL);
            this.Name = GetAttribute(source, "name", null);
            HashSet<string> ret = new HashSet<string>();
            foreach (XmlNode node in XmlReadWrite.GetChildren(source, "contact"))
                ret.Add(XmlReadWrite.GetAttribute(node, "value"));
        }

        /// <summary>
        /// Fills object's xml
        /// </summary>
        /// <param name="node">Empty node for the object with name "tagname"</param>
        protected override void FillXmlElement(XmlElement node)
        {
            AddAttribute(node, "type", this.Type);
            AddAttribute(node, "name", this.Name);
            foreach (string c in this.Contacts)
                AddAttributeNode(node, "contact", "value", c);
        }

        /// <summary>
        /// Returns groups by type in dictionary form
        /// </summary>
        /// <param name="root">Groups Xml node</param>
        /// <returns>groups by type</returns>
        public static Dictionary<AdvanceNotificationGroupType, Dictionary<string, List<string>>> LoadDictFomXml(XmlNode root)
        {
            List<AdvanceNotificationGroup> groups = CreateListFromXml<AdvanceNotificationGroup>(root, "groups", "group");
            Dictionary<AdvanceNotificationGroupType, Dictionary<string, List<string>>> ret = new Dictionary<AdvanceNotificationGroupType, Dictionary<string, List<string>>>();
            Dictionary<string, List<string>> typeGroups;
            foreach (XmlNode gNode in GetChildren(root, "group"))
            {
                AdvanceNotificationGroup ng = CreateFromXml<AdvanceNotificationGroup>(gNode);
                if (!ret.TryGetValue(ng.Type, out typeGroups))
                {
                    typeGroups = new Dictionary<string, List<string>>();
                    ret.Add(ng.Type, typeGroups);
                }
                typeGroups.Add(ng.Name, ng.Contacts);
            }
            return ret;
        }

        public static void FillXmlElementFromDict(XmlElement root, Dictionary<AdvanceNotificationGroupType, Dictionary<string, ICollection<string>>> dict)
        {
            foreach (KeyValuePair<AdvanceNotificationGroupType, Dictionary<string, ICollection<string>>> typeGroups in dict)
                foreach (KeyValuePair<string, ICollection<string>> gData in typeGroups.Value)
                {
                    AdvanceNotificationGroup ng = new AdvanceNotificationGroup(typeGroups.Key, gData.Key, gData.Value);
                    ng.AddToXML("group", root);
                }
        }

    }


}
