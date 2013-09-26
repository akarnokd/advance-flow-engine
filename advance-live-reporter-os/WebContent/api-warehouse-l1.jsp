<%@page import="java.util.Date"%>
<%@page import="org.joda.time.DateTime"%>
<%@page import="eu.advance.logistics.live.reporter.model.*"%>
<%@page
	import="eu.advance.logistics.live.reporter.charts.*"%>
<%@page import="net.sf.json.JSONObject"%>
<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
	pageEncoding="ISO-8859-1"%>
<%
  response.setContentType("application/json");
  JSONObject result = new JSONObject();

  User user = null;
  synchronized (session)
  {
    user = (User) session.getAttribute("USER");
  }
  if (user == null)
  {
    JSONObject errType = new JSONObject();
    errType.put(ErrorType.TIME_OUT.name().toLowerCase(), ErrorType.TIME_OUT.getMessage());
    result.put("error", errType);
  } else
  {
    try
    {
      HubDepotSwitch hubDepotSwitch = new HubDepotSwitch(user.hub, user.name);
      hubDepotSwitch.setDateTime(new Date());
      
      WarehouseSwitch warehouseSwitch = new WarehouseSwitch(user.hub, request.getParameter("warehouse_name"), user.name);
      
      L1OverallData overallData = new L1OverallData(warehouseSwitch);
      L1AtHubData atHubData = new L1AtHubData(warehouseSwitch);

      WarehouseSummary.setOverall(overallData, warehouseSwitch.hubId(), new DateTime(hubDepotSwitch.getDateTime()),
    		  HubDepotDataCache.get(session.getServletContext()));

      WarehouseSummary.setAtHubDetails(atHubData, warehouseSwitch.hubId(), new DateTime(hubDepotSwitch.getDateTime()),
    		  HubDepotDataCache.get(session.getServletContext()));

      result = L1Chart.getJSONtoChart(overallData, atHubData);
    } catch (IllegalArgumentException e)
    {
      JSONObject errType = new JSONObject();
      errType.put(ErrorType.ENUM_CODE_ILLEGAL_FORMAT.name().toLowerCase(), ErrorType.ENUM_CODE_ILLEGAL_FORMAT.getMessage());
      result.put("error", errType);
    }
  }

  out.print(result.toString());
%>