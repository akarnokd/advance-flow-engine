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
using System.IO;
using System.Xml;

namespace AdvanceAPIClient.Core
{
    public static class Utils
    {
        /// <summary>
        /// Convert string to enum type 
        /// /// </summary>
        /// <param name="attr">attribute name</param>
        /// <param name="defValue">Default value</param>
        /// <returns>Actual attribute value</returns>
        public static T NameToEnum<T>(string name, T defValue)
        {
            try
            {
                return (T)Enum.Parse(defValue.GetType(), name);
            }
            catch
            {
                return defValue;
            }
        }

        /// <summary>
        /// Computes the relation between two cardinality values. 
        /// </summary>
        /// <param name="n1">first cardinality value</param>
        /// <param name="n2">second cardinality value</param>
        /// <returns>relation type</returns>
        public static TypeRelation CompareCardinality(XCardinality n1, XCardinality n2)
        {
              return CompareToTypeRelation((int)n1 - (int)n2);
        }

        public static TypeRelation CompareToTypeRelation(int comp)
        {
            if (comp < 0)
                return TypeRelation.EXTENDS;
            else if (comp > 0)
                return TypeRelation.SUPER;
            else
                return TypeRelation.EQUAL;
        }

        /// <summary>
        /// Fixes directory name and concats sub directory name and created path if necessary
        /// </summary>
        /// <param name="dir">Base directory</param>
        /// <param name="subdir"> Sub directory name</param>
        /// <returns>Fixed directory path</returns>
        public static string FixDirectory(string dir, string subdir)
        {
            dir = dir.Replace('/', Path.DirectorySeparatorChar).Replace('\\', Path.DirectorySeparatorChar);
            if (dir[dir.Length - 1] != Path.DirectorySeparatorChar)
                dir += Path.DirectorySeparatorChar;
            if (!string.IsNullOrEmpty(subdir))
                dir += subdir + Path.DirectorySeparatorChar;
            if (!Directory.Exists(dir))
                Directory.CreateDirectory(dir);
            return dir;
        }

        /// <summary>
        /// Serarches for free filename
        /// </summary>
        /// <param name="dir">Directory name</param>
        /// <param name="name">Base file name</param>
        /// <param name="ext">Extension</param>
        /// <returns></returns>
        public static string GetFilenameForCreate(string dir, string name, string ext)
        {
            int i = 0;
            string ret = dir + name + "." + ext;
            while (File.Exists(ret))
                ret = dir + name + (++i) + "." + ext;
            return ret;
        }

    }
}
