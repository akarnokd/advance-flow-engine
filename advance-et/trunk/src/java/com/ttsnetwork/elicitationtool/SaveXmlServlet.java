package com.ttsnetwork.elicitationtool;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

/**
 *
 * @author farago
 */
public class SaveXmlServlet extends HttpServlet {

    /**
     * Handles the HTTP
     * <code>POST</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        try {
            File newXml = new File(FileViewData.getXmlFileRepository(), 
                   request.getParameter("fileName"));
            FileOutputStream out = new FileOutputStream(newXml);
            SAXBuilder builder = new SAXBuilder();
            InputStream in = request.getInputStream();
            new XMLOutputter(Format.getPrettyFormat()).output(builder.build(in), out);
            out.close();
            in.close();
//            System.out.println("Ok! New file located here: " + newXml.getCanonicalPath());
        } catch (Exception ex) {
            throw new ServletException();
        }

    }
}
