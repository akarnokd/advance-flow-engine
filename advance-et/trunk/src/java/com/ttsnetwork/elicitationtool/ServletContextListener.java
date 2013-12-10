/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ttsnetwork.elicitationtool;

import javax.servlet.ServletContextEvent;

/**
 * Web application lifecycle listener.
 *
 * @author farago
 */
public class ServletContextListener implements javax.servlet.ServletContextListener {

    public void contextInitialized(ServletContextEvent sce) {
        sce.getServletContext().setAttribute("userNameListManager", new UserNameListManager());
    }

    public void contextDestroyed(ServletContextEvent sce) {
        sce.getServletContext().removeAttribute("userNameListManager");
    }
}
