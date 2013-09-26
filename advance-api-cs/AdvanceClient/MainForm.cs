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
using System.ComponentModel;
using System.Data;
using System.Drawing;
using System.Linq;
using System.Text;
using System.Xml;
using System.IO;
using System.Windows.Forms;
using System.Reflection;

using AdvanceAPIClient;
using AdvanceAPIClient.Implementation;
using AdvanceAPIClient.Core;
using AdvanceAPIClient.Communication;
using AdvanceAPIClient.Classes.Runtime;

namespace AdvanceClient
{
    public partial class MainForm : Form
    {
   
        private DataTable gridTable = new DataTable();
        private string outputDir;
        private IDisposable unsubscriber;
        private string certFile;
        private string keyFile;

        public MainForm()
        {
            InitializeComponent();

            this.gridTable.Columns.Add(new DataColumn("Seq", typeof(int)));
            this.gridTable.Columns.Add(new DataColumn("Name", typeof(string)));
            this.gridTable.Columns.Add(new DataColumn("Type", typeof(string)));
            this.gridTable.Columns.Add(new DataColumn("Value", typeof(object)));

            this.textConnectionUrl.Text = "https://localhost:8444";
            this.textUser.Text = "admin";
            this.textPw.Text = "admin";
            this.textOutput.Text = "e:/Advance/Work/";
            this.btnInvoke.Enabled = false;
            string home = Environment.ExpandEnvironmentVariables("%HOMEDRIVE%%HOMEPATH%");
            this.certFile = home + "/.advance-flow-editor-ws/LocalEngine/advance-server.cer";
            this.keyFile = home + "/.advance-flow-editor-ws/LocalEngine/advance-server.pem";
        }

        private void AddMessageLine(string msg)
        {
            this.rtxtResults.Text += msg + "\r\n";
        }

        public void AddErrMessage(string msg, Exception e)
        {
            if (msg != null)
                this.AddMessageLine(msg);
            if (e != null)
            {
                if (e is AdvanceTrackingFinished)
                    this.observe_btn_Click(null, null);
                this.AddMessageLine(e.Message);
                Exception iex = e.InnerException;
                while (iex != null)
                {
                    this.AddMessageLine("\t" + iex.Message);
                    iex = iex.InnerException;
                }
                this.AddMessageLine("-------------------------------------------------");
                this.AddMessageLine(e.StackTrace);

                this.rtxtResults.Text += msg + "\r\n";
            }
        }

        private void cmbInterfaces_SelectedIndexChanged(object sender, EventArgs e)
        {
            InterfaceDescription selectedInterface = this.cmbInterfaces.SelectedItem as InterfaceDescription;

            this.cmbMethods.Items.Clear();
            this.cmbMethods.Items.Add("-= Select a method to invoke =-");
            foreach (string m in selectedInterface.GetMethods())
                this.cmbMethods.Items.Add(m);
            this.cmbMethods.SelectedIndex = 0;
        }

        private void cmbMethods_SelectedIndexChanged(object sender, EventArgs e)
        {
            ClearControls();
            InterfaceDescription selectedInterface = this.cmbInterfaces.SelectedItem as InterfaceDescription;
            ParameterInfo[] pars = selectedInterface.GetMethodParameters(this.cmbMethods.SelectedItem as string);
            if (pars != null)
                UpdateParams(pars);
        }

        private void btnInvoke_Click(object sender, EventArgs e)
        {
            InterfaceDescription selectedInterface = this.cmbInterfaces.SelectedItem as InterfaceDescription;
            string method = this.cmbMethods.SelectedItem as string;

            object[] ps = GetActualParameters(selectedInterface.GetMethodParameters(method));
            try
            {
                this.rtxtResults.Clear();
                this.AddMessageLine("Invoke '" + method + "' from '" + selectedInterface);
                object response = selectedInterface.InvokeMethod(method, ps);
                if (response != null)
                {
                    Type respType = response.GetType();
                    if (respType.IsPrimitive || respType.Equals(typeof(string)))
                        this.AddMessageLine(response.ToString());
                    else
                    {
                        string fn = (this.outputDir == "") ? null : Utils.GetFilenameForCreate(this.outputDir, method, "xml");
                        if (fn != null)
                            this.AddMessageLine("Output in file: " + fn);
                        if (response is IObservable<XmlNode>)
                            this.StartObservable<XmlNode>(response as IObservable<XmlNode>, fn);
                        else if (response is IObservable<XmlNode>)
                            this.StartObservable<XmlReadWrite>(response as IObservable<XmlReadWrite>, fn);
                        else
                        {
                            if (fn != null)
                                XmlReadWrite.WriteToFile(fn, response);
                            XmlReadWrite xmlObj = (response is XmlReadWrite) ? (response as XmlReadWrite) : new XmlObjectSerializer(response);
                            this.AddMessageLine(xmlObj.ToString());
                        }
                    }
                }
            }
            catch (Exception ex)
            {
                this.AddErrMessage("Error", ex);
            }
        }

        private object[] GetActualParameters(IList<ParameterInfo> pars)
        {
            object[] ret = new object[pars.Count];
            try
            {
                for (int i = 0; i < pars.Count; ++i)
                    ret[i] = GetActualParameter(pars[i].ParameterType, this.gridTable.Rows[i]["Value"]);
            }
            catch (Exception ex)
            {
                this.rtxtResults.Text = "Error on converting parameters! - " + ex.Message;
            }
            return ret;
        }

        private object GetActualParameter(Type parType, object value)
        {
            if (parType.Equals(typeof(String)))
                return value;
            else if (parType.IsPrimitive)
                return Convert.ChangeType(value, parType);
            else
            {
                XmlNode node = XmlReadWrite.ReadFromStream(File.OpenRead((string)value));
                return XmlReadWrite.CreateFromXml(parType, node);
            }
        }  

        private void ClearControls()
        {
            this.gridTable.Clear();
            this.dgvParameters.Hide();
            this.dgvParameters.DataSource = null;

            this.rtxtResults.Clear();
            this.btnInvoke.Enabled = false;
            this.observe_btn.Hide();
        }

        private void UpdateParams(IList<ParameterInfo> pars)
        {
            int seq = 1;
            foreach (ParameterInfo pi in pars)
                this.gridTable.Rows.Add(new object[] {seq++, pi.Name, pi.ParameterType.Name, ""});
            this.dgvParameters.DataSource = this.gridTable;
            this.btnInvoke.Enabled = true;
            this.dgvParameters.Visible = true;
         }

        private void dgvParameters_CellBeginEdit(object sender, DataGridViewCellCancelEventArgs e)
        {
            if (e.ColumnIndex != 3)
                e.Cancel = true;
            else
                this.FillFileCell(e.RowIndex);
        }

        private void dgvParameters_CellClick(object sender, DataGridViewCellEventArgs e)
        {
             this.FillFileCell(e.RowIndex);
        }

        private void FillFileCell(int row)
        {
            int seq = (int)this.gridTable.Rows[row]["Seq"] - 1;
            InterfaceDescription selectedInterface = this.cmbInterfaces.SelectedItem as InterfaceDescription;
            ParameterInfo par = selectedInterface.GetMethodParameter(this.cmbMethods.SelectedItem as string, seq);
            if (!par.ParameterType.Equals(typeof(String)) && !par.ParameterType.IsPrimitive)
            {
                OpenFileDialog openFileDialog = new OpenFileDialog();
                openFileDialog.DefaultExt = ".xml";
                openFileDialog.Filter = "XML files(*.xml)|*.xml";
                openFileDialog.Multiselect = false;
                openFileDialog.RestoreDirectory = true;
                if (openFileDialog.ShowDialog() == System.Windows.Forms.DialogResult.OK)
                    this.gridTable.Rows[row]["Value"] = openFileDialog.FileName;
            }
        }

        private void btnBrowse_Click(object sender, EventArgs e)
        {
            FolderBrowserDialog folderBrowserDialog = new FolderBrowserDialog();
            if (folderBrowserDialog.ShowDialog() == System.Windows.Forms.DialogResult.OK)
                this.textOutput.Text = folderBrowserDialog.SelectedPath;             
        }

        private void btnConect_Click(object sender, EventArgs e)
        {
            this.cmbInterfaces.Items.Clear();
            HttpRemoteEngineControl ecClass = this.chk_Cert.Checked ?
                new HttpRemoteEngineControl(new Uri(this.textConnectionUrl.Text), this.textUser.Text, this.textPw.Text.ToCharArray())
                :
                new HttpRemoteEngineControl(new Uri(this.textConnectionUrl.Text), this.textUser.Text, this.textPw.Text.ToCharArray())
                ;
            ecClass.Debug = true;
            this.cmbInterfaces.Items.Add(new InterfaceDescription("Advance Engine Control", ecClass));

            this.cmbInterfaces.Items.Add(new InterfaceDescription("Advance Data Strore", ecClass.Datastore));
            this.cmbInterfaces.SelectedIndex = 0;
        }

        private void textOutput_TextChanged(object sender, EventArgs e)
        {
            try
            {
                this.outputDir = Utils.FixDirectory(this.textOutput.Text, "Output");
                Log.SetParameters(Utils.FixDirectory(this.textOutput.Text, "LOG"), "test", "TEST", 10);
            }
            catch
            {
                this.outputDir = "";
                this.textOutput.Text = "*** Invalid directory: " + this.textOutput.Text;
            }
        }

        private void StartObservable<T>(IObservable<T> observable, string fn)
        {
            XmlObjectReporter<T> reporter = new XmlObjectReporter<T>(this.AddErrMessage, fn);
            observable.Subscribe(reporter);
            this.unsubscriber = reporter;
            this.observe_btn.Show();
        }

        private void observe_btn_Click(object sender, EventArgs e)
        {
            if (this.unsubscriber != null)
                this.unsubscriber.Dispose();
            this.unsubscriber = null;
            this.observe_btn.Hide();
        }

        private void chk_Cert_CheckedChanged(object sender, EventArgs e)
        {
            if (this.chk_Cert.Checked)
                this.textConnectionUrl.Text = this.textConnectionUrl.Text.Replace(":8444", ":8443");
            else
                this.textConnectionUrl.Text = this.textConnectionUrl.Text.Replace(":8443", ":8444");
        }

     }

 }
