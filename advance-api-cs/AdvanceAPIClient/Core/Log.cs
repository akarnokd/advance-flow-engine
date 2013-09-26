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
using System.Diagnostics;
using System.Globalization;
using System.Timers;


namespace AdvanceAPIClient.Core
{
    /// <summary>
    /// Log and debug handler class
    /// </summary>
    public static class Log
    {
        /// <summary>
        /// Log directory 
        /// </summary>
        private static string directory = "LOG/";
        /// <summary>
        /// Application prefix 
        /// </summary>
        private static string applicationPrefix = "app";
        /// <summary>
        /// Debug file name prefix
        /// </summary>
        private static string debugName = "debug";
        /// <summary>
        /// Error log file name prefixe
        /// </summary>
        private static string logName = "error";
        /// <summary>
        /// 
        /// </summary>
        private static string sessionId = null;
 
        /// <summary>
        /// Set log directory and delete old application's old log 
        /// </summary>
        /// <param name="dir">logdir or null if no logging required</param>
        /// <param name="applPref">new app prefix or null to keep old value</param>
        /// <param name="defSessionId">session identifier</param>
        /// <param name="logPeriod">If  > 0, Log files older than this value in days will be deleted</param>
        public static void SetParameters(string dir, string applPref, string sId, int logPeriod)
        {
            directory = Utils.FixDirectory(dir, null);
            if (applPref != null)
                applicationPrefix = applPref;
            if (logPeriod > 0)
                ClearOldLogs(logPeriod);
            sessionId = sId;
        }

        /// <summary>
        /// Returns a subdirectory of log directtory
        /// </summary>
        /// <param name="subDirName">Subdirectory name</param>
        /// <returns>Full path</returns>
        public static string GetWorkDir(string subDirName)
        {
            string dir = directory + subDirName + Path.DirectorySeparatorChar;
            if (!Directory.Exists(dir))
                Directory.CreateDirectory(dir);
            return dir;
        }

        /// <summary>
        /// Write to Debug file
        /// </summary>
        /// <param name="label">information label</param>
        /// <param name="debugInfo">debug information</param>
        public static void Debug(string label, Dictionary<string, string> debugInfo)
        {
            StringBuilder b = new StringBuilder(label);
            foreach (KeyValuePair<string, string> di in debugInfo)
                b.Append("\r\n\t" + di.Key + "=" + di.Value);
            WriteLog(debugName, b.ToString());
        }   
     
        /// <summary>
        /// Write exception to log file
        /// </summary>
        /// <param name="e">Exception</param>
        public static string LogException(Exception e)
        {
            return WriteLog(logName, FormatException(e));
        }

        /// <summary>
        /// Write message to log file
        /// </summary>
        /// <param name="logString">Üzenet szövege</param>
        /// <returns>LOG Azonosító</returns>
        public static string LogString(string logString)
        {
            return WriteLog(logName, logString);
        }

        /// <summary>
        /// Format exception data
        /// </summary>
        /// <param name="e">Exception</param>
        public static string FormatException(Exception e)
        {
            string line = "";
            if (e != null)
            {
                e = e.GetBaseException();
                line += e.Message + "\r\n";
                line += "  Source: " + e.Source + "\r\n";
                line += "  Target: " + e.TargetSite + "\r\n";
                line += "   Stack: " + e.StackTrace + "\r\n";
                line += "    Type: " + e.GetType().ToString() + "\r\n";
            }
            return line;
        }

        /// <summary>
        /// Write an entry to log file
        /// </summary>
        /// <param name="name">log file nameájl név prefixe</param>
        /// <param name="line">A bejegyzés szövege</param>
        public static string WriteLog(string name, string line)
        {
            if (directory != null)
            {
                string logId = ((string.IsNullOrEmpty(sessionId)) ? "" : sessionId += "_") + DateTime.Now.ToString("u", DateTimeFormatInfo.InvariantInfo);
                lock (typeof(Log))
                {
                    try
                    {
                        using (StreamWriter sw = File.AppendText(directory + applicationPrefix + name + "_" + DateTime.Now.ToString("yyMMdd") + ".log"))
                        {
                            sw.WriteLine("[" + logId + "] " + line);
                        }
                    }
                    catch
                    {
                    }
                }
                return logId;
            }
            else return "";
        }

        /// <summary>
        /// Delet old logfiles
        /// </summary>
        /// <param name="logPeriod">File experion in days</param>
        public static void ClearOldLogs(int logPeriod)
        {
            DateTime date;
            DateTime old = DateTime.Now.AddDays(-logPeriod);
            foreach (FileInfo fi in new DirectoryInfo(directory).GetFiles("*.log"))
            {
                string[] fileNameParts = Path.GetFileNameWithoutExtension(fi.Name).Split('_');
                if (fileNameParts.Length > 2 && fileNameParts[0] == applicationPrefix &&
                    DateTime.TryParseExact(fileNameParts[fileNameParts.Length - 1], "yyMMdd", null, DateTimeStyles.None, out date) && date < old)
                {
                    Console.WriteLine(fi.Name);
                    try
                    {
                        fi.Delete();
                    }
                    catch
                    {
                    }
                }
            }
        }

     }
}
