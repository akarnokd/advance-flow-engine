package com.ttsnetwork.elicitationtool;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

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
            File newXml = new File(getServletContext().getInitParameter("XmlFilesRep")
                    + "/" + request.getParameter("fileName"));
            FileOutputStream out = new FileOutputStream(newXml);
            InputStream in = request.getInputStream();
            byte[] buff = new byte[1024];
            int read;
            while ((read = in.read(buff)) != -1) {
                out.write(buff, 0, read);
            }
            out.close();
            in.close();
//            System.out.println("Ok! New file located here: " + newXml.getCanonicalPath());
        } catch (Exception ex) {
            throw new ServletException();
        }

    }
}
