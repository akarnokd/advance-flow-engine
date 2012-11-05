package com.ttsnetwork.elicitationtool;

import java.io.File;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author farago
 */
public class CheckFileServlet extends HttpServlet {

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
        File selectedFile = new File(FileViewData.getXmlFileRepository(),
                request.getParameter("fileName"));
        String action = request.getParameter("action");
        String resultPage = getServletConfig().getInitParameter("resultPage");

        if (selectedFile.exists()) {
            resultPage += action;
        } else {
            resultPage += "fileList.jsp";
        }

        System.out.println(resultPage);
        
        getServletContext().getRequestDispatcher(resultPage).forward(request, response);
    }
}
