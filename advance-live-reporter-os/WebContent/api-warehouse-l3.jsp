<%@page import="java.util.Date"%>
<%@page import="eu.advance.logistics.live.reporter.charts.*"%>
<%@page import="org.joda.time.DateTime"%>
<%@page import="eu.advance.logistics.live.reporter.model.*"%>
<%@page import="java.util.List"%>

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
    hubDepotSwitch.setDateTime(new Date());

    WarehouseSwitch warehouseSwitch = new WarehouseSwitch(user.hub, user.name);
    warehouseSwitch.setWarehouse(request.getParameter("warehouse_name"));
    warehouseSwitch.setStorageAreaOrder(request.getParameter("storage_order"));
    warehouseSwitch.setL3WarehouseOption(request.getParameter("warehouse_l3_option"));
    warehouseSwitch.setL3Warehouse(request.getParameter("warehouse_l3_name"));
    
    switch(L3WarehouseMode.valueOf(request.getParameter("mode").toUpperCase()))
    {
      case INIT:
      case ORIENT:
      case ORDER:  
      {
    	  warehouseSwitch.setL3SelectedStorageId(Integer.parseInt(request.getParameter("warehouse_l3_storage_id")));
        break;
      }
      case L3OPTION:
      {
    		int atStorageId = Integer.parseInt(request.getParameter("warehouse_l3_storage_id"));
    		
    		WarehouseSide actualSide = WarehouseSwitch.warehouseSideFromOption(warehouseSwitch.getL3WarehouseOption());
    		WarehouseSide prevSide = WarehouseSwitch.oppositeWarehouseSide(actualSide); 
    		long oppositeStorageId = warehouseSwitch.getL3OppositeStorageId(atStorageId, prevSide);
    
    		warehouseSwitch.setL3SelectedStorageId(oppositeStorageId);
    	  break;
      }
    }
    
    
    List<L3DepotStorageData> depotStorageList = warehouseSwitch.getL3DepotStorageList(); 
	WarehouseSummary.setDepotDetails(depotStorageList, warehouseSwitch.hubId(),
	new DateTime(hubDepotSwitch.getDateTime()), warehouseSwitch.getWarehouse(),
	HubDepotDataCache.get(session.getServletContext()));

    result = L3Chart.getJSONtoChart(warehouseSwitch, depotStorageList);
    
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
