/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ttsnetwork.elicitationtool;

import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author farago
 */
public class LogoutServlet extends HttpServlet {
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.getSession().removeAttribute("logged");
        response.getWriter().write("User not logged anymore");
    }
}
