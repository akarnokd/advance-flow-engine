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
using System.Reflection;

namespace AdvanceClient
{
    public class InterfaceDescription
    {
        private string name;
        private object classObj;
        private Dictionary<string, MethodInfo> methods;
 
        public InterfaceDescription(string name, object classObj)
        {
            this.name = name;
            this.classObj = classObj;
            this.methods = new Dictionary<string,MethodInfo>();
            foreach (MethodInfo mi in this.classObj.GetType().GetMethods())
            {
                string pars = "";
                foreach (ParameterInfo pi in mi.GetParameters())
                {
                    if (pars != "") pars += ", ";
                    pars += pi.ParameterType.Name + " " + pi.Name;
                }
                this.methods.Add(mi.Name + "(" + pars + ")", mi);
            }
        }

        public ICollection<String> GetMethods() { return this.methods.Keys; }

        public ParameterInfo[] GetMethodParameters(string method)
        {
            MethodInfo mi;
            return this.methods.TryGetValue(method, out mi) ? mi.GetParameters() : null;
        }

        public ParameterInfo GetMethodParameter(string method, int i)
        {
            ParameterInfo[] pars = GetMethodParameters(method);
            return pars[i];
        }

        public object InvokeMethod(string method, params object[] args)
        {
            MethodInfo mi;
            if (this.methods.TryGetValue(method, out mi))
                return this.classObj.GetType().InvokeMember(mi.Name, BindingFlags.InvokeMethod, null, this.classObj, args);
            else
                return method + " method not found";
        }

        public static string NameTail(string name)
        {
            if (name == null)
                return null;
            else
            {
                string[] parts = name.Split('.');
                return parts[parts.Length - 1];
            }
        }
      
        public override string ToString()
        {
 	        return this.name;
        }
    }

}
