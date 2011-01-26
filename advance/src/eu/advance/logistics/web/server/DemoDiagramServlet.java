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
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.zip.GZIPOutputStream;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import eu.advance.logistics.applet.DataDiagram;
import eu.advance.logistics.applet.DataDiagramValues;
import eu.advance.logistics.applet.DataSignal;
import eu.advance.logistics.applet.Result;

/**
 * The demo simple diagram servlet.
 * @author karnokd
 */
public class DemoDiagramServlet extends HttpServlet {
	/** */
	private static final long serialVersionUID = -5326966378382490296L;
	
	@Override
	protected void service(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		GZIPOutputStream gout = new GZIPOutputStream(response.getOutputStream());
		ObjectOutputStream oos = new ObjectOutputStream(gout);
		
		// compose diagram values here -----------------------------
		
		DataDiagram dd = new DataDiagram();
		long now = System.currentTimeMillis();
		dd.startTime = new Timestamp(now);
		dd.endTime = new Timestamp(now + 30 * 60 * 1024);
		
		DataDiagramValues<DataSignal> ddv = new DataDiagramValues<DataSignal>();
		
		ddv.minimum = BigDecimal.ZERO;
		ddv.maximum = BigDecimal.ONE;
		
		DataSignal dds1 = new DataSignal();
		dds1.signalName = "Signal 1";
		dds1.timestamp = new Timestamp(now + 5 * 60 * 1000);
		dds1.value = new BigDecimal(0.25);
		ddv.values = Lists.newArrayList();
		ddv.values.add(dds1);
		
		DataSignal dds2 = new DataSignal();
		dds2.signalName = "Signal 1";
		dds2.value = new BigDecimal(0.75);
		dds2.timestamp = new Timestamp(now + 10 * 60 * 1000);
		ddv.values.add(dds2);
		
		DataSignal dds3 = new DataSignal();
		dds3.signalName = "Signal 1";
		dds3.value = new BigDecimal(0.0);
		dds3.timestamp = new Timestamp(now + 15 * 60 * 1000);
		ddv.values.add(dds3);
		
		DataSignal dds4 = new DataSignal();
		dds4.signalName = "Signal 1";
		dds4.value = new BigDecimal(1.0);
		dds4.timestamp = new Timestamp(now + 20 * 60 * 1000);
		ddv.values.add(dds4);
		
		DataSignal dds5 = new DataSignal();
		dds5.signalName = "Signal 1";
		dds5.value = new BigDecimal(0.1);
		dds5.timestamp = new Timestamp(now + 25 * 60 * 1000);
		ddv.values.add(dds5);
		
		dd.signals = Maps.newHashMap();
		dd.signals.put("Demo", ddv);
		
		dd.actions = Maps.newHashMap();
		dd.alarms = Maps.newHashMap();
		dd.labels = Maps.newHashMap();
		dd.statuses = Lists.newArrayList();
		
		oos.writeObject(new Result<DataDiagram>(dd));
		// ---------------------------------------------------------
		
		oos.flush();
		gout.finish();
		gout.flush();
	}
}
