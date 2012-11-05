/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ttsnetwork.elicitationtool;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collection;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.output.XMLOutputter;

/**
 *
 * @author farago
 */
public class GetFileListServlet extends HttpServlet {

    /**
     * Processes requests for both HTTP
     * <code>GET</code> and
     * <code>POST</code> methods.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/xml;charset=UTF-8");
        PrintWriter out = response.getWriter();
        try {
            Collection<FileItem> files = FileManager.getInstance().getFiles();

            Element el = new Element("root");
            Document doc = new Document(el);
            Element fileEl;

            for (FileItem file : files) {
                fileEl = new Element("file");
                fileEl.setAttribute("filename", file.getFilename() == null ? "" : file.getFilename());
                fileEl.setAttribute("user", file.getUser() == null ? "" : file.getUser());
                el.addContent(fileEl);
            }
            
            new XMLOutputter().output(doc, out);
        } finally {
            out.close();
        }
    }

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
        processRequest(request, response);
    }
}
