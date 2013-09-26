<%@page import="hu.akarnokd.utils.crypto.BCrypt"%>
<%@page import="hu.akarnokd.utils.xml.XElement"%>
<%@page import="eu.advance.logistics.live.reporter.db.UserDB"%>
<%@page import="java.util.*"%>
<%@page import="eu.advance.logistics.live.reporter.db.MasterDB"%>
<%@ page language="java" contentType="application/json; charset=utf-8"	pageEncoding="ISO-8859-1"%>
<%@page import="net.sf.json.JSONArray"%>
<%@page import="net.sf.json.JSONObject"%>
<%@page import="eu.advance.logistics.live.reporter.model.*"%>
<%@page import="eu.advance.logistics.live.reporter.charts.*"%>
<%
response.setContentType("application/json");
JSONObject result = new JSONObject();

User user = null;

synchronized (session) {
    user = (User)session.getAttribute("USER");
}
if (user == null || !user.admin) {
    JSONObject errType = new JSONObject();
    errType.put(ErrorType.TIME_OUT.name().toLowerCase(), ErrorType.TIME_OUT.getMessage());
    result.put("error", errType);
} else {
	
  String action = request.getParameter("action");
  try
  {
    if ("get_hubs".equals(action)) {
  		
      JSONArray arr = new JSONArray();
      JSONObject ob;
  	  for (Hub h : MasterDB.hubs())
  	  {
  	    ob = new JSONObject();
  	    ob.put("id", h.id);
  	    ob.put("name", h.name);
  	    arr.add(ob);
  	  }
  	  if(arr.isEmpty())
  	  {
  	    result.put("error", "Either Hub or Depot is empty.");
  	  }
  	  else
  	  {
  	    result.put("hubs", arr);
  	  }

  	} else
  	if ("get_depots".equals(action)) {

      JSONArray arr = new JSONArray();
      JSONObject ob;
      for (Depot d : MasterDB.depots())
  	  {
  	    ob = new JSONObject();
  	    ob.put("id", d.id);
  	    ob.put("name", d.name);
  	    arr.add(ob);
  	  }
  	  if(arr.isEmpty())
  	  {
  	    result.put("error", "Either Hub or Depot is empty.");
  	  }
  	  else
  	  {
  	    result.put("depots", arr);
  	  }
  	  
  	} else
  	if ("get_users".equals(action)) {
  	  result = allUserToClient(user);
  	  
  	} else
  	if ("get_user".equals(action)) {
  		String idStr = request.getParameter("id");
  		
  	} else
  	if ("delete_user".equals(action)) {
  		String idStr = request.getParameter("id");
  		UserDB.deleteUser(Long.parseLong(idStr));
  		result.put("done", "done");
  		
  	} else
  	if ("save_user".equals(action)) {
  	  
  		String jsonStr = request.getParameter("json");
  		JSONObject uJson = JSONObject.fromObject(jsonStr);
  		
  		long userId = Long.parseLong((uJson.getString("id").equals("N/A")) ? "0" : uJson.getString("id"));
  		User u;
  		if(userId == 0)
  		{
  		  u = new User();
  		  u.id = userId;
  		  u.password = BCrypt.hashpw(uJson.getString("password"), BCrypt.gensalt());
  		}
  		else
  		{
  		  u = UserDB.getUser(userId);
  		  if(uJson.getString("password").equals("") == false)
  		    u.password = BCrypt.hashpw(uJson.getString("password"), BCrypt.gensalt());
  		}
  		
  		u.name = uJson.getString("name");
  		u.hub = Long.parseLong(uJson.getString("hub"));
  		u.depot = (uJson.getString("depot").equals("-1")) ? null : Long.parseLong(uJson.getString("depot"));
  		u.admin = (uJson.getString("admin").equals("0")) ? false : true;
  		u.view = UserView.valueOf(uJson.getString("view").toUpperCase());
  		
  		UserDB.saveUser(u);
  		result.put("done", "done");
  	}

  }
  catch (NumberFormatException e)
  {
    result.clear();
    result.put("error", e.toString());
  }


}
out.print(result.toString());
%>

<%!
static JSONObject allUserToClient(User u)
{
  LinkedHashMap<Long, String> mapHubs = new LinkedHashMap<Long, String>();
  LinkedHashMap<Long, String> mapDepots = new LinkedHashMap<Long, String>();

  for (Hub h : MasterDB.hubs())
  {
    mapHubs.put(h.id, h.name);
  }
  for (Depot d : MasterDB.depots())
  {
    mapDepots.put(d.id, d.name);
  }

  JSONObject result = new JSONObject();
  JSONArray recordList = new JSONArray();
  JSONObject record = new JSONObject();

  if ((mapHubs.isEmpty()) || (mapDepots.isEmpty()))
  {
    result.put("error", "Either Hub or Depot is empty.");
  } else
  {
    for (User user : UserDB.getAll())
    {
      record.clear();
      record.put("id", Long.valueOf(user.id));
      record.put("name", user.name);
      record.put("hub", XElement.sanitize(mapHubs.get(user.hub)));
      record.put("depot", (user.depot == null) ? "N/A" : XElement.sanitize(mapDepots.get(user.depot)));
      record.put("admin", (user.admin == true) ? 1 : 0);
      record.put("view", user.view.name().toLowerCase());
      
      record.put("hubId", user.hub);
      record.put("depotId", (user.depot == null) ? Long.valueOf(-1) : user.depot);

      recordList.add(record);
    }
    result.put("users", recordList);
    result.put("logged", Long.valueOf(u.id));
    
  }
  return result;
}
%>