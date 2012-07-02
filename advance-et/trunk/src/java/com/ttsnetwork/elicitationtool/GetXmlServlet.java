package com.ttsnetwork.elicitationtool;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author farago
 */
public class GetXmlServlet extends HttpServlet {

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
        response.setContentType("application/xml");
        OutputStream out = response.getOutputStream(); //Prendo lo stream di uscita
        try {
            FileInputStream fos = new FileInputStream(
                    new File(getServletContext().getInitParameter("XmlFilesRep")
                    + "\\" + request.getParameter("fileName"))); //Apro lo stream sul file
            byte[] buff = new byte[1024]; //Faccio un buffer
            int read; //Un int che terrà i byte letti
            while ((read = fos.read(buff)) != -1) { //Legge il file a gruppi di buff.length byte fino a che ce ne sono
                out.write(buff, 0, read); //Scrive nel file il pezzo buff, da 0 a read
            }
            fos.close();
        }catch(Exception ex){
            response.setStatus(301); //Notice an error to client
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
