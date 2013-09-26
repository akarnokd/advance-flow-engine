<%@page import="eu.advance.logistics.live.reporter.charts.HubDepotDataCache"%>
<%@page import="org.joda.time.DateTime"%>
<%@page import="eu.advance.logistics.live.reporter.charts.HubDepotSummary"%>
<%@page import="eu.advance.logistics.live.reporter.model.User"%>
<%@page import="java.text.ParseException"%>
<%@ page language="java" contentType="text/html; charset=utf-8" pageEncoding="ISO-8859-1"%>
<%@page import="net.sf.json.JSONArray"%>
<%@page import="net.sf.json.JSONObject"%>
<%@page import="eu.advance.logistics.live.reporter.charts.*"%>

<%
response.setContentType("application/json");
JSONObject result = new JSONObject();

User user = null;
synchronized(session)
{
  user = (User)session.getAttribute("USER");
}
if (user == null)
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
    
    hubDepotSwitch.setChartView(user);
    hubDepotSwitch.setHubDepotInfo(request.getParameter("store_type"), request.getParameter("store_id"));
    hubDepotSwitch.setUnit(request.getParameter("unit"));
    hubDepotSwitch.setDateTime(request.getParameter("datetime"));

    SumData sumData = new SumData(hubDepotSwitch);

    switch(hubDepotSwitch.getTypeStatus())
    {
      case HUB:
  		//   	SumTestDb.createHubData(sumData);
    	HubDepotSummary.setHubSummary(user.hub, 
    			new DateTime(hubDepotSwitch.getDateTime()), 
    			hubDepotSwitch.getUnit(), sumData, 
    			HubDepotDataCache.get(session.getServletContext()));

        break;
      case DEPOT:
        //SumTestDb.createDepotData(sumData);
        HubDepotSummary.setDepotSummary(user.hub, 
        		hubDepotSwitch.getHubDepotInfo().id, 
        		new DateTime(hubDepotSwitch.getDateTime()), 
        		hubDepotSwitch.getUnit(), sumData, 
        		HubDepotDataCache.get(session.getServletContext()));
        
        break;
    }
    
    result = SumChart.getSummaryJSON(sumData);
    
  } catch(NumberFormatException e) {
    
    JSONObject errType = new JSONObject();
    errType.put(ErrorType.ENUM_CODE_NUMBER_FORMAT.name().toLowerCase(), ErrorType.ENUM_CODE_NUMBER_FORMAT.getMessage());
    result.put("error", errType);
  } catch(IllegalArgumentException e) {
    JSONObject errType = new JSONObject();
    errType.put(ErrorType.ENUM_CODE_ILLEGAL_FORMAT.name().toLowerCase(), ErrorType.ENUM_CODE_ILLEGAL_FORMAT.getMessage());
    result.put("error", errType);
    e.printStackTrace();
  }
  catch(ParseException e)
  {
    JSONObject errType = new JSONObject();
    errType.put(ErrorType.DATE_PARSE_FORMAT.name().toLowerCase(), ErrorType.DATE_PARSE_FORMAT.getMessage());
    result.put("error", errType);
  }
}

out.print(result.toString());
%>
