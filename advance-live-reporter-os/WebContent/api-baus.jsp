<%@page import="eu.advance.logistics.live.reporter.db.MasterDB"%>
<%@page import="eu.advance.logistics.live.reporter.charts.TypeStatus"%>
<%@page import="java.text.ParseException"%>
<%@page import="eu.advance.logistics.live.reporter.charts.ErrorType"%>
<%@page import="eu.advance.logistics.live.reporter.charts.HubDepotSwitch"%>
<%@page import="eu.advance.logistics.live.reporter.model.User"%>
<%@page import="net.sf.json.JSONObject"%>
<%
response.setContentType("application/json");
JSONObject result = new JSONObject();

User user = null;
HubDepotSwitch hubDepotSwitch = null;

synchronized(session)
{
  user = (User)session.getAttribute("USER");
}
if(user == null)
{
  response.sendRedirect("index.jsp");
  return;
}
else
{
  try
  {
    hubDepotSwitch = new HubDepotSwitch(user.hub, user.name);
    
    hubDepotSwitch.setHubDepotInfo(request.getParameter("store_type"), request.getParameter("store_id"));
    hubDepotSwitch.setUnit(request.getParameter("unit"));
    hubDepotSwitch.setDateTime(request.getParameter("datetime"));  
  
    if(request.getParameter("mode").compareTo("fetch") == 0)
    {
      result.put("baus", hubDepotSwitch.getMax());
    }
    else if(request.getParameter("mode").compareTo("update") == 0)
    {
      String baus = request.getParameter("baus");
      if (baus != null)
      {
        if (hubDepotSwitch.getTypeStatus() == TypeStatus.HUB)
        {
          MasterDB.setHubSummaryValueMax(user.hub, user.name, hubDepotSwitch.getUnit(), Integer.parseInt(baus));
        } else
        {
          MasterDB.setDepotSummaryValueMax(user.hub, hubDepotSwitch.depotId(), user.name, hubDepotSwitch.getUnit(), Integer.parseInt(baus));
        }
      }
      
      result.put("baus", "done");
    }

    
  } catch(NumberFormatException e) {
    JSONObject errType = new JSONObject();
    errType.put(ErrorType.ENUM_CODE_NUMBER_FORMAT.name().toLowerCase(), ErrorType.ENUM_CODE_NUMBER_FORMAT.getMessage());
    result.put("error", errType);
  } catch(IllegalArgumentException e) {
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
