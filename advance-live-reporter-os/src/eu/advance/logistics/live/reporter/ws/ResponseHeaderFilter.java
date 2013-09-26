/*
 * Copyright 2010-2013 The Advance EU 7th Framework project consortium
 *
 * This file is part of Advance.
 *
 * Advance is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of
 * the License, or (at your option) any later version.
 *
 * Advance is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with Advance.  If not, see
 * <http://www.gnu.org/licenses/>.
 *
 */

package eu.advance.logistics.live.reporter.ws;

import java.io.IOException;
import java.util.Enumeration;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;
/**
 * A parameterable cache control filter for resources.
 * http://www.onjava.com/pub/a/onjava/2004/03/03/filters.html
 * @author karnokd, Jun 6, 2008
 * @version $Revision 1.0$
 */
public class ResponseHeaderFilter implements Filter {
	/** The filter configuration. */
	FilterConfig fc;
	@Override
	public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
    throws IOException, ServletException {
	    HttpServletResponse response = (HttpServletResponse)res;
	    // set the provided HTTP response parameters
		for (Enumeration<?> e = fc.getInitParameterNames();
		    e.hasMoreElements();) {
		  String headerName = (String)e.nextElement();
		  response.addHeader(headerName, fc.getInitParameter(headerName));
		}
		// pass the request/response on
		chain.doFilter(req, response);
	}
	@Override
	public void init(FilterConfig filterConfig) {
		this.fc = filterConfig;
	}
	@Override
	public void destroy() {
		this.fc = null;
	}
}
