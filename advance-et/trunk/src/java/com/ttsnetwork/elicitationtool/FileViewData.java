package com.ttsnetwork.elicitationtool;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;

/**
 *
 * @author dalmaso
 */
public class FileViewData extends HttpServlet {

    public static File getXmlFileRepository() {
        try {
            Context initCtx = new InitialContext();
            Context envCtx = (Context) initCtx.lookup("java:comp/env");
            String dirname = (String) envCtx.lookup("advanceet.filerep");
            if (dirname != null) {
                return new File(dirname);
            }
        } catch (NamingException ex) {
            ex.printStackTrace();
        }
        return new File("C:\\AdvanceET\\Files");
    }

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
        response.setContentType("application/json");
        PrintWriter out = response.getWriter();
        try {
            FileInputStream fos = new FileInputStream(
                    new File(FileViewData.getXmlFileRepository(),
                    request.getParameter("fileName"))); //Apro lo stream sul file

            SAXBuilder builder = new SAXBuilder();
            Document doc = builder.build(fos);
            fos.close();

            JsonElement je = adapt(doc.getRootElement());
            if (je == null) {
                JsonObject root = new JsonObject();
                root.addProperty("name", "(none)");
                je = root;
            }
            Gson gson = new Gson();
            out.print(gson.toJson(je));
        } catch (Exception ex) {
            throw new ServletException(ex);
        } finally {
            out.close();
        }
    }

    private JsonElement adapt(Element e) {
        String name = e.getAttributeValue("label");
        if (name == null) {
            return null;
        }
        JsonObject jobject = new JsonObject();
        jobject.addProperty("name", name);
        jobject.addProperty("rectId", "");
        jobject.addProperty("linkToParent", "");
        List children = e.getChildren("node");
        if (!children.isEmpty()) {
            JsonArray jchildren = new JsonArray();
            for (Object child : children) {
                JsonElement jchild = adapt((Element) child);
                if (jchild != null) {
                    jchildren.add(jchild);
                }
            }
            if (jchildren.size() != 0) {
                jobject.add("children", jchildren);
            }
        }

        return jobject;
    }

    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /**
     * Handles the HTTP
     * <code>GET</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
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

    /**
     * Returns a short description of the servlet.
     *
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>
}
