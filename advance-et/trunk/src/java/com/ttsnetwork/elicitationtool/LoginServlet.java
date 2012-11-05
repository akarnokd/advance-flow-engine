package com.ttsnetwork.elicitationtool;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;

/**
 *
 * @author farago
 */
public class LoginServlet extends HttpServlet {

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
        if (request.getSession().getAttribute("logged") == null) {

            try {

                String username = null;
                String password = null;
                UserBean userBean = null;

                SAXBuilder builder = new SAXBuilder();
                Document doc = builder.build(
                        new File(getServletContext().getRealPath("users/users.xml")));
                List users = doc.getRootElement().getChildren("user");

                Iterator i = users.iterator();
                List<Element> userPermissions = new ArrayList<Element>();
                while (i.hasNext()) {
                    Element user = (Element) i.next();
                    username = user.getAttributeValue("name");
                    password = user.getAttributeValue("password");

                    if (username.equals(request.getParameter("userName"))
                            && password.equals(request.getParameter("password"))) {
                        userPermissions = user.getChildren("permission");
                        break;
                    }
                }

                if (userPermissions.size() > 0) {
                    userBean = new UserBean();
                    userBean.setName(username);
                    for (Element permission : userPermissions) {
                        String type = permission.getAttributeValue("type");
                        if (type.equals("edit")) {
                            userBean.setAllowToEdit(
                                    permission.getAttributeValue("allow").equals("true"));
                        } else if (type.equals("view")) {
                            userBean.setAllowToView(
                                    permission.getAttributeValue("allow").equals("true"));
                        } else if (type.equals("upload")) {
                            userBean.setAllowToUpload(
                                    permission.getAttributeValue("allow").equals("true"));
                        } else if (type.equals("delete")) {
                            userBean.setAllowToDelete(
                                    permission.getAttributeValue("allow").equals("true"));
                        } else if (type.equals("download")) {
                            userBean.setAllowToDownload(
                                    permission.getAttributeValue("allow").equals("true"));
                        }
                    }
                }

                if (userBean != null) {
                    request.getSession().setAttribute("userBean", userBean);
                }

            } catch (JDOMException ex) {
                System.out.println("Advance Elicitation Tool Login servlet "
                        + "threw an exception on date " + new Date());
                ex.printStackTrace();
            }

        }
        response.sendRedirect(getServletConfig().getInitParameter("resultPage"));
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        ((HttpServletResponse) response).sendRedirect(getServletConfig().getInitParameter("errorPage"));
    }
}