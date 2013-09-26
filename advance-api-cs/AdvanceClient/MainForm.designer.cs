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
namespace AdvanceClient
{
    partial class MainForm
    {
        /// <summary>
        /// Required designer variable.
        /// </summary>
        private System.ComponentModel.IContainer components = null;

        /// <summary>
        /// Clean up any resources being used.
        /// </summary>
        /// <param name="disposing">true if managed resources should be disposed; otherwise, false.</param>
        protected override void Dispose(bool disposing)
        {
            if (disposing && (components != null))
            {
                components.Dispose();
            }
            base.Dispose(disposing);
        }

        #region Windows Form Designer generated code

        /// <summary>
        /// Required method for Designer support - do not modify
        /// the contents of this method with the code editor.
        /// </summary>
        private void InitializeComponent()
        {
            this.cmbMethods = new System.Windows.Forms.ComboBox();
            this.label1 = new System.Windows.Forms.Label();
            this.label2 = new System.Windows.Forms.Label();
            this.btnInvoke = new System.Windows.Forms.Button();
            this.rtxtResults = new System.Windows.Forms.RichTextBox();
            this.label3 = new System.Windows.Forms.Label();
            this.label4 = new System.Windows.Forms.Label();
            this.textOutput = new System.Windows.Forms.TextBox();
            this.btnBrowse = new System.Windows.Forms.Button();
            this.label5 = new System.Windows.Forms.Label();
            this.cmbInterfaces = new System.Windows.Forms.ComboBox();
            this.labelServer = new System.Windows.Forms.Label();
            this.textConnectionUrl = new System.Windows.Forms.TextBox();
            this.btnConect = new System.Windows.Forms.Button();
            this.textUser = new System.Windows.Forms.TextBox();
            this.textPw = new System.Windows.Forms.TextBox();
            this.dgvParameters = new System.Windows.Forms.DataGridView();
            this.observe_btn = new System.Windows.Forms.Button();
            this.chk_Cert = new System.Windows.Forms.CheckBox();
            ((System.ComponentModel.ISupportInitialize)(this.dgvParameters)).BeginInit();
            this.SuspendLayout();
            // 
            // cmbMethods
            // 
            this.cmbMethods.FormattingEnabled = true;
            this.cmbMethods.Location = new System.Drawing.Point(74, 124);
            this.cmbMethods.Name = "cmbMethods";
            this.cmbMethods.Size = new System.Drawing.Size(369, 21);
            this.cmbMethods.TabIndex = 0;
            this.cmbMethods.SelectedIndexChanged += new System.EventHandler(this.cmbMethods_SelectedIndexChanged);
            // 
            // label1
            // 
            this.label1.AutoSize = true;
            this.label1.Location = new System.Drawing.Point(22, 127);
            this.label1.Name = "label1";
            this.label1.Size = new System.Drawing.Size(46, 13);
            this.label1.TabIndex = 1;
            this.label1.Text = "Method:";
            // 
            // label2
            // 
            this.label2.AutoSize = true;
            this.label2.Location = new System.Drawing.Point(11, 137);
            this.label2.Name = "label2";
            this.label2.Size = new System.Drawing.Size(63, 13);
            this.label2.TabIndex = 2;
            this.label2.Text = "Parameters:";
            // 
            // btnInvoke
            // 
            this.btnInvoke.Location = new System.Drawing.Point(471, 127);
            this.btnInvoke.Name = "btnInvoke";
            this.btnInvoke.Size = new System.Drawing.Size(90, 23);
            this.btnInvoke.TabIndex = 4;
            this.btnInvoke.Text = "Invoke";
            this.btnInvoke.UseVisualStyleBackColor = true;
            this.btnInvoke.Click += new System.EventHandler(this.btnInvoke_Click);
            // 
            // rtxtResults
            // 
            this.rtxtResults.Anchor = ((System.Windows.Forms.AnchorStyles)((((System.Windows.Forms.AnchorStyles.Top | System.Windows.Forms.AnchorStyles.Bottom) 
            | System.Windows.Forms.AnchorStyles.Left) 
            | System.Windows.Forms.AnchorStyles.Right)));
            this.rtxtResults.Location = new System.Drawing.Point(12, 337);
            this.rtxtResults.Name = "rtxtResults";
            this.rtxtResults.Size = new System.Drawing.Size(584, 194);
            this.rtxtResults.TabIndex = 5;
            this.rtxtResults.Text = "";
            // 
            // label3
            // 
            this.label3.AutoSize = true;
            this.label3.Location = new System.Drawing.Point(12, 321);
            this.label3.Name = "label3";
            this.label3.Size = new System.Drawing.Size(71, 13);
            this.label3.TabIndex = 6;
            this.label3.Text = "Return value:";
            // 
            // label4
            // 
            this.label4.AutoSize = true;
            this.label4.Location = new System.Drawing.Point(16, 48);
            this.label4.Name = "label4";
            this.label4.Size = new System.Drawing.Size(65, 13);
            this.label4.TabIndex = 7;
            this.label4.Text = "Work folder:";
            // 
            // textOutput
            // 
            this.textOutput.Location = new System.Drawing.Point(19, 64);
            this.textOutput.Name = "textOutput";
            this.textOutput.ReadOnly = true;
            this.textOutput.Size = new System.Drawing.Size(489, 20);
            this.textOutput.TabIndex = 8;
            this.textOutput.TextChanged += new System.EventHandler(this.textOutput_TextChanged);
            // 
            // btnBrowse
            // 
            this.btnBrowse.Location = new System.Drawing.Point(521, 64);
            this.btnBrowse.Name = "btnBrowse";
            this.btnBrowse.Size = new System.Drawing.Size(75, 23);
            this.btnBrowse.TabIndex = 9;
            this.btnBrowse.Text = "Browse";
            this.btnBrowse.UseVisualStyleBackColor = true;
            this.btnBrowse.Click += new System.EventHandler(this.btnBrowse_Click);
            // 
            // label5
            // 
            this.label5.AutoSize = true;
            this.label5.Location = new System.Drawing.Point(16, 106);
            this.label5.Name = "label5";
            this.label5.Size = new System.Drawing.Size(52, 13);
            this.label5.TabIndex = 10;
            this.label5.Text = "Interface:";
            // 
            // cmbInterfaces
            // 
            this.cmbInterfaces.FormattingEnabled = true;
            this.cmbInterfaces.Location = new System.Drawing.Point(74, 103);
            this.cmbInterfaces.Name = "cmbInterfaces";
            this.cmbInterfaces.Size = new System.Drawing.Size(369, 21);
            this.cmbInterfaces.TabIndex = 11;
            this.cmbInterfaces.SelectedIndexChanged += new System.EventHandler(this.cmbInterfaces_SelectedIndexChanged);
            // 
            // labelServer
            // 
            this.labelServer.Anchor = ((System.Windows.Forms.AnchorStyles)((System.Windows.Forms.AnchorStyles.Top | System.Windows.Forms.AnchorStyles.Right)));
            this.labelServer.AutoSize = true;
            this.labelServer.Location = new System.Drawing.Point(15, 12);
            this.labelServer.Name = "labelServer";
            this.labelServer.Size = new System.Drawing.Size(57, 13);
            this.labelServer.TabIndex = 13;
            this.labelServer.Text = "Server Url:";
            // 
            // textConnectionUrl
            // 
            this.textConnectionUrl.Location = new System.Drawing.Point(78, 5);
            this.textConnectionUrl.Name = "textConnectionUrl";
            this.textConnectionUrl.Size = new System.Drawing.Size(312, 20);
            this.textConnectionUrl.TabIndex = 14;
            // 
            // btnConect
            // 
            this.btnConect.Location = new System.Drawing.Point(523, 7);
            this.btnConect.Name = "btnConect";
            this.btnConect.Size = new System.Drawing.Size(75, 23);
            this.btnConect.TabIndex = 15;
            this.btnConect.Text = "Conect";
            this.btnConect.UseVisualStyleBackColor = true;
            this.btnConect.Click += new System.EventHandler(this.btnConect_Click);
            // 
            // textUser
            // 
            this.textUser.Location = new System.Drawing.Point(408, 5);
            this.textUser.Name = "textUser";
            this.textUser.Size = new System.Drawing.Size(100, 20);
            this.textUser.TabIndex = 16;
            // 
            // textPw
            // 
            this.textPw.Location = new System.Drawing.Point(408, 29);
            this.textPw.Name = "textPw";
            this.textPw.Size = new System.Drawing.Size(100, 20);
            this.textPw.TabIndex = 17;
            this.textPw.UseSystemPasswordChar = true;
            // 
            // dgvParameters
            // 
            this.dgvParameters.AllowUserToAddRows = false;
            this.dgvParameters.AllowUserToDeleteRows = false;
            this.dgvParameters.AutoSizeColumnsMode = System.Windows.Forms.DataGridViewAutoSizeColumnsMode.AllCells;
            this.dgvParameters.ColumnHeadersHeightSizeMode = System.Windows.Forms.DataGridViewColumnHeadersHeightSizeMode.AutoSize;
            this.dgvParameters.Location = new System.Drawing.Point(12, 174);
            this.dgvParameters.Name = "dgvParameters";
            this.dgvParameters.Size = new System.Drawing.Size(553, 130);
            this.dgvParameters.TabIndex = 3;
            this.dgvParameters.CellBeginEdit += new System.Windows.Forms.DataGridViewCellCancelEventHandler(this.dgvParameters_CellBeginEdit);
            this.dgvParameters.CellClick += new System.Windows.Forms.DataGridViewCellEventHandler(this.dgvParameters_CellClick);
            // 
            // observe_btn
            // 
            this.observe_btn.Location = new System.Drawing.Point(523, 311);
            this.observe_btn.Name = "observe_btn";
            this.observe_btn.Size = new System.Drawing.Size(75, 23);
            this.observe_btn.TabIndex = 18;
            this.observe_btn.Text = "Stop";
            this.observe_btn.UseVisualStyleBackColor = true;
            this.observe_btn.Click += new System.EventHandler(this.observe_btn_Click);
            // 
            // chk_Cert
            // 
            this.chk_Cert.AutoSize = true;
            this.chk_Cert.Location = new System.Drawing.Point(520, 32);
            this.chk_Cert.Name = "chk_Cert";
            this.chk_Cert.Size = new System.Drawing.Size(73, 17);
            this.chk_Cert.TabIndex = 19;
            this.chk_Cert.Text = "Certificate";
            this.chk_Cert.UseVisualStyleBackColor = true;
            this.chk_Cert.CheckedChanged += new System.EventHandler(this.chk_Cert_CheckedChanged);
            // 
            // MainForm
            // 
            this.AutoScaleDimensions = new System.Drawing.SizeF(6F, 13F);
            this.AutoScaleMode = System.Windows.Forms.AutoScaleMode.Font;
            this.ClientSize = new System.Drawing.Size(610, 543);
            this.Controls.Add(this.chk_Cert);
            this.Controls.Add(this.observe_btn);
            this.Controls.Add(this.textPw);
            this.Controls.Add(this.textUser);
            this.Controls.Add(this.btnConect);
            this.Controls.Add(this.textConnectionUrl);
            this.Controls.Add(this.labelServer);
            this.Controls.Add(this.cmbInterfaces);
            this.Controls.Add(this.label5);
            this.Controls.Add(this.btnBrowse);
            this.Controls.Add(this.textOutput);
            this.Controls.Add(this.label4);
            this.Controls.Add(this.label3);
            this.Controls.Add(this.rtxtResults);
            this.Controls.Add(this.btnInvoke);
            this.Controls.Add(this.dgvParameters);
            this.Controls.Add(this.label2);
            this.Controls.Add(this.label1);
            this.Controls.Add(this.cmbMethods);
            this.Name = "MainForm";
            this.Text = "Advance Interface Tester";
            ((System.ComponentModel.ISupportInitialize)(this.dgvParameters)).EndInit();
            this.ResumeLayout(false);
            this.PerformLayout();

        }

        #endregion

        private System.Windows.Forms.ComboBox cmbMethods;
        private System.Windows.Forms.Label label1;
        private System.Windows.Forms.Label label2;
        private System.Windows.Forms.Button btnInvoke;
        private System.Windows.Forms.RichTextBox rtxtResults;
        private System.Windows.Forms.Label label3;
        private System.Windows.Forms.Label label4;
        private System.Windows.Forms.TextBox textOutput;
        private System.Windows.Forms.Button btnBrowse;
        private System.Windows.Forms.Label label5;
        private System.Windows.Forms.ComboBox cmbInterfaces;
        private System.Windows.Forms.Label labelServer;
        private System.Windows.Forms.TextBox textConnectionUrl;
        private System.Windows.Forms.Button btnConect;
        private System.Windows.Forms.TextBox textUser;
        private System.Windows.Forms.TextBox textPw;
        private System.Windows.Forms.DataGridView dgvParameters;
        private System.Windows.Forms.Button observe_btn;
        private System.Windows.Forms.CheckBox chk_Cert;
    }
}

