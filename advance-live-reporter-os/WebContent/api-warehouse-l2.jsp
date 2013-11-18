<%@page import="eu.advance.logistics.live.reporter.charts.*"%>
<%@page import="java.util.Date"%>
<%@page import="org.joda.time.DateTime"%>
<%@page import="eu.advance.logistics.live.reporter.model.*"%>
<%@page import="eu.advance.logistics.live.reporter.db.*"%>
<%@page import="java.util.List"%>
<%@page import="java.util.Map"%>
<%@page import="net.sf.json.JSONObject"%>
<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1"%>

<%
	response.setContentType("application/json");
JSONObject result = new JSONObject();

User user = null;
synchronized(session)
{
  user = (User)session.getAttribute("USER");
}
if(user == null)
{
  JSONObject errType = new JSONObject();
  errType.put(ErrorType.TIME_OUT.name().toLowerCase(), ErrorType.TIME_OUT.getMessage());
  result.put("error", errType);
}
else
{
  try
  {
    HubDepotSwitch hubDepotSwitch = new HubDepotSwitch(user.hub, user.name);
    
    WarehouseSwitch warehouseSwitch = new WarehouseSwitch(user.hub, user.name);
    warehouseSwitch.setWarehouse(request.getParameter("warehouse_name"));
    warehouseSwitch.setL2WarehouseOption(request.getParameter("warehouse_option"));
    warehouseSwitch.setStorageAreaOrder(request.getParameter("storage_order"));
    
    Map<L2DisplaySide, List<L2StorageRawData>> storageRawMap = warehouseSwitch.getL2StorageRawMap();
	WarehouseSummary.warehouseDetails(storageRawMap, warehouseSwitch.hubId(),
	new DateTime(hubDepotSwitch.getDateTime()), warehouseSwitch.getWarehouse(),
	HubDepotDataCache.get(session.getServletContext()));
    
	  result = L2Chart.getJSONtoChart(warehouseSwitch, storageRawMap);
    
  }
  catch(IllegalArgumentException e)
  {
    JSONObject errType = new JSONObject();
    errType.put(ErrorType.ENUM_CODE_ILLEGAL_FORMAT.name().toLowerCase(), ErrorType.ENUM_CODE_ILLEGAL_FORMAT.getMessage());
    result.put("error", errType);
  }
}

out.print(result.toString());
%>