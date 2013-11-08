<%@page import="net.sf.json.JSONArray"%>
<%@page import="hu.akarnokd.utils.lang.*"%>
<%@page import="java.util.Date"%>
<%@page import="eu.advance.logistics.live.reporter.charts.WarehouseSwitch"%>
<%@page import="eu.advance.logistics.live.reporter.charts.HubDepotSwitch"%>
<%@page import="eu.advance.logistics.live.reporter.charts.ErrorType"%>
<%@page import="eu.advance.logistics.live.reporter.model.User"%>
<%@page import="net.sf.json.JSONObject"%>
<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
	pageEncoding="ISO-8859-1"%>
<%
  response.setContentType("application/json");
  JSONObject result = new JSONObject();

  User user = null;
  synchronized(session)
  {
    user = (User) session.getAttribute("USER");
  }
  if(user == null)
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
      
      WarehouseSwitch warehouseSwitch = new WarehouseSwitch(user.hub, user.name);
      String whName = ObjectUtils.nvl(request.getParameter("warehouse_name"), warehouseSwitch.getFirstWarehousePairTop());
      warehouseSwitch.setWarehouse(whName);      
      
      int i = 0, pos = 0;
      String whTop = warehouseSwitch.getWarehousePairTop();
      JSONArray dataArr = new JSONArray();
      JSONObject o; 
      for(String keyItem : warehouseSwitch.getWarehousePairMap().keySet())
      {
        if(keyItem.equals(whTop))
        {
          pos = i;
        }
        i++;
        
        o = new JSONObject();
        o.put("top", keyItem);
        o.put("bottom", warehouseSwitch.getWarehousePairMap().get(keyItem));
        dataArr.add(o);
      }
      
      result.put("size", warehouseSwitch.getWarehousePairMap().size());
      result.put("position", pos);
      result.put("dataList", dataArr);
      
      
    } catch (IllegalArgumentException e)
    {
      JSONObject errType = new JSONObject();
      errType.put(ErrorType.ENUM_CODE_ILLEGAL_FORMAT.name().toLowerCase(), ErrorType.ENUM_CODE_ILLEGAL_FORMAT.getMessage());
      result.put("error", errType);
    }

  }
  
  out.print(result.toString());  
%>