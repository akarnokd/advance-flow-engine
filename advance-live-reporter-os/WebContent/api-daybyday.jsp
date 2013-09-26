<%@page import="java.util.Date"%>
<%@page import="eu.advance.logistics.live.reporter.model.User"%>
<%@page import="org.joda.time.DateTime"%>
<%@page import="org.joda.time.DateMidnight"%>
<%@page import="java.text.ParseException"%>
<%@ page language="java" contentType="text/html; charset=utf-8"	pageEncoding="ISO-8859-1"%>
<%@page import="net.sf.json.JSONArray"%>
<%@page import="net.sf.json.JSONObject"%>
<%@page import="eu.advance.logistics.live.reporter.model.*"%>
<%@page import="eu.advance.logistics.live.reporter.charts.*"%>


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
    
    hubDepotSwitch.setChartView(user);
    hubDepotSwitch.setHubDepotInfo(request.getParameter("store_type"), request.getParameter("store_id"));
    hubDepotSwitch.setUnit(request.getParameter("unit"));
    hubDepotSwitch.setDateTime(request.getParameter("datetime"));
    
    DayByData dayByData = new DayByData(hubDepotSwitch);
		int horizon = 3;
		DateTime fromDay = new DateTime(hubDepotSwitch.getDateTime());
		if (fromDay.getHourOfDay() < 6) {
			fromDay = fromDay.minusDays(1);
		}
		long hubId = hubDepotSwitch.hubId();
	
    switch(hubDepotSwitch.getTypeStatus())
    {
      case HUB:
      {
        //DayByTestDb.createHubData(dayByData);
				HubDepotSummary.setHubPrediction(hubId, fromDay.toDateMidnight(), horizon, dayByData);
        break;
      }
      case DEPOT:
      {
        //DayByTestDb.createDepotData(dayByData);
				HubDepotSummary.setDepotPrediction(hubId, hubDepotSwitch.depotId(), fromDay.toDateMidnight(), horizon, dayByData);
        break;
      }
    }

    result = DayByChart.getDayByDayJSON(dayByData);
    
  }
  catch(NumberFormatException e)
  {
    JSONObject errType = new JSONObject();
    errType.put(ErrorType.ENUM_CODE_NUMBER_FORMAT.name().toLowerCase(), ErrorType.ENUM_CODE_NUMBER_FORMAT.getMessage());
    result.put("error", errType);
  }
  catch(IllegalArgumentException e)
  {
    JSONObject errType = new JSONObject();
    errType.put(ErrorType.ENUM_CODE_ILLEGAL_FORMAT.name().toLowerCase(), ErrorType.ENUM_CODE_ILLEGAL_FORMAT.getMessage());
    result.put("error", errType);
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
