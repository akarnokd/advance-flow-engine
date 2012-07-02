package com.ttsnetwork.elicitationtool;

import java.io.IOException;
import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author farago
 */
public class PermissionFilter implements Filter {

    private static final boolean debug = true;
    // The filter configuration object we are associated with.  If
    // this value is null, this filter instance is not currently
    // configured. 
    private FilterConfig filterConfig = null;

    /**
     *
     * @param request The servlet request we are processing
     * @param response The servlet response we are creating
     * @param chain The filter chain we are processing
     *
     * @exception IOException if an input/output error occurs
     * @exception ServletException if a servlet error occurs
     */
    public void doFilter(ServletRequest request, ServletResponse response,
            FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;
        UserBean userBean =
                (UserBean) req.getSession().getAttribute("userBean");
        boolean allowed = false;

        if ((req.getRequestURL().indexOf("saveXml") != -1
                || req.getRequestURL().indexOf("/pages/editFile.jsp") != -1
                || req.getRequestURL().indexOf("getXml") != -1)
                && userBean.isAllowToEdit()) {
            allowed = true;
        } else if (req.getRequestURL().indexOf("downloadXml") != -1
                && userBean.isAllowToDownload()) {
            allowed = true;
        } else if (req.getRequestURL().indexOf("deleteXml") != -1
                && userBean.isAllowToDelete()) {
            allowed = true;
        } else if (req.getRequestURL().indexOf("uploadXml") != -1
                && userBean.isAllowToUpload()) {
            allowed = true;
        } else if (req.getRequestURL().indexOf("/pages/viewFile.jsp") != -1
                && userBean.isAllowToView()) {
            allowed = true;
        }

        if (allowed) {
            chain.doFilter(request, response);
        } else {
            if (req.getRequestURL().indexOf("deleteXml") != -1) {
                resp.setStatus(300);
                resp.getWriter().write("You don\'t have the permission to delete files.");
            } else {
                resp.sendRedirect(filterConfig.getInitParameter("errorPage"));
            }
        }

    }

    /**
     * Return the filter configuration object for this filter.
     */
    public FilterConfig getFilterConfig() {
        return (this.filterConfig);
    }

    /**
     * Set the filter configuration object for this filter.
     *
     * @param filterConfig The filter configuration object
     */
    public void setFilterConfig(FilterConfig filterConfig) {
        this.filterConfig = filterConfig;
    }

    /**
     * Destroy method for this filter
     */
    public void destroy() {
    }

    /**
     * Init method for this filter
     */
    public void init(FilterConfig filterConfig) {
        this.filterConfig = filterConfig;
        if (filterConfig != null) {
            if (debug) {
            }
        }
    }
}
