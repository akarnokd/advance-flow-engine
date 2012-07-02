package com.ttsnetwork.elicitationtool;

import java.io.File;
import java.io.IOException;
import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Check for file existence when a user want to edit it
 * @author farago
 */
public class CheckFileFilter implements Filter {

    // The filter configuration object we are associated with.  If
    // this value is null, this filter instance is not currently
    // configured. 
    private FilterConfig filterConfig = null;

    public CheckFileFilter() {
    }

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
        File file = new File(filterConfig.getServletContext().getInitParameter("XmlFilesRep") 
                + "/" + request.getParameter("fileName"));
        if (file.exists()) {
//            System.out.println("File exists!");
//            System.out.println("Page: " + req.getRequestURL());
            chain.doFilter(request, response);
        } else {
//            System.out.println("File doesn't exists!");
            ((HttpServletResponse) response).sendRedirect(
                    filterConfig.getInitParameter("errorPage"));
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
        //TODO ???
    }

    /**
     * Init method for this filter 
     */
    public void init(FilterConfig filterConfig) {
        this.filterConfig = filterConfig;
    }
}
