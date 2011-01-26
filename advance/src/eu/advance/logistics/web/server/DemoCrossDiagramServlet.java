/*
 * Copyright 2010-2012 The Advance EU 7th Framework project consortium
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
package eu.advance.logistics.web.server;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.zip.GZIPOutputStream;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import eu.advance.logistics.applet.DataDiagram;
import eu.advance.logistics.applet.Result;

/**
 * The demo simple cross-diagram servlet.
 * @author karnokd
 */
public class DemoCrossDiagramServlet extends HttpServlet {
	/** */
	private static final long serialVersionUID = -5326966378382490296L;
	
	@Override
	protected void service(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		GZIPOutputStream gout = new GZIPOutputStream(response.getOutputStream());
		ObjectOutputStream oos = new ObjectOutputStream(gout);
		
		// compose diagram values here -----------------------------
		oos.writeObject(new Result<DataDiagram>());
		// ---------------------------------------------------------
		
		oos.flush();
		gout.finish();
		gout.flush();
	}
}
