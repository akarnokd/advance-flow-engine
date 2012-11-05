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
public class DeleteXmlServlet extends HttpServlet {

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
        boolean errorOccured = true;
        String errorMessage = null;
        File xmlFile = new File(FileViewData.getXmlFileRepository(),
                request.getParameter("fileName"));
        System.out.println(xmlFile);

        if (((UserBean) request.getSession().getAttribute("userBean")).isAllowToDelete()) {
            if (xmlFile.delete()) {
                errorOccured = false;
            } else {
                errorMessage = "Cannot delete the file. Contact the administrator.";
            }
        } else {
            errorMessage = "You don\'t have the permission to delete files.";
        }

        if (errorOccured) {
            //Set response to show error with jQuery
            response.setCharacterEncoding("UTF-8");
            response.setStatus(300);
            response.getWriter().write(errorMessage);
        }

    }
}
