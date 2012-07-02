/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ttsnetwork.elicitationtool;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author farago
 */
public class DownloadXmlServlet extends HttpServlet {

    /** 
     * Handles the HTTP <code>POST</code> method.
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        File newXml = new File(getServletContext().getInitParameter("XmlFilesRep")
                    + "/" + request.getParameter("fileName"));
        FileInputStream in = new FileInputStream(newXml);
        ServletOutputStream out = response.getOutputStream();
        response.setContentType("application/xml");
        response.setHeader("Content-Disposition", "attachment; filename=\""
                + newXml.getName() + "\"");
        byte[] buff = new byte[1024];
        int read = 0;
        while((read = in.read(buff)) != -1){
            out.write(buff, 0, read);
        }
        
        out.close();
        in.close();
        
    }
}
