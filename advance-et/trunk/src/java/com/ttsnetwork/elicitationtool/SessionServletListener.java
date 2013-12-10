/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ttsnetwork.elicitationtool;

import java.util.Collection;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

/**
 * Web application lifecycle listener.
 *
 * @author farago
 */
public class SessionServletListener implements HttpSessionListener {
    
    public void sessionCreated(HttpSessionEvent se) {
        
    }
    
    public void sessionDestroyed(HttpSessionEvent se) {
        String userName = ((UserBean) se.getSession().getAttribute("userBean")).getName();
        Collection<FileItem> files = FileManager.getInstance().getFiles();
        for (FileItem fileItem : files) {
            if(fileItem.getUser()!= null && fileItem.getUser().equals(userName)) {
                fileItem.setUser(null);
            }
        }
        ((UserNameListManager)se.getSession().getServletContext().getAttribute("userNameListManager")).removeUserName(userName);
    }
}
