<%@page import="java.util.*"%>
<%@page import="eu.advance.logistics.live.reporter.db.MasterDB"%>
<%@ page language="java" contentType="application/json; charset=utf-8"	pageEncoding="ISO-8859-1"%>
<%@page import="net.sf.json.JSONArray"%>
<%@page import="net.sf.json.JSONObject"%>
<%@page import="eu.advance.logistics.live.reporter.model.*"%>
<%@page import="eu.advance.logistics.live.reporter.charts.*"%>
<%
response.setContentType("application/json");
Object result = new JSONObject();

User user = null;

synchronized (session) {
    user = (User)session.getAttribute("USER");
}
if (user == null) {
    JSONObject errType = new JSONObject();
    errType.put(ErrorType.TIME_OUT.name().toLowerCase(), ErrorType.TIME_OUT.getMessage());
    ((JSONObject)result).put("error", errType);
} else {
	String action = request.getParameter("action");
	if ("get_hubs".equals(action)) {
		List<Hub> hubs = MasterDB.hubs();
		JSONArray a = new JSONArray();
		for (Hub h : hubs) {
			JSONObject jo = new JSONObject();
			jo.put("id", h.id);
			jo.put("name", h.name);
			
			a.add(jo);
		}
		result = a;
	} else
	if ("get_warehouses".equals(action)) {
		String hubStr = request.getParameter("hub");
		if (hubStr != null) {
			List<Warehouse> sl = MasterDB.warehouses(Long.parseLong(hubStr));
			JSONArray ja = new JSONArray();
			for (Warehouse s : sl) {
				ja.add(s.warehouse);
			}
			result = ja;
		}
	} else
	if ("get_layout".equals(action)) {
		String hubStr = request.getParameter("hub");
		String whStr = request.getParameter("warehouse");
		if (hubStr != null && whStr != null) {
			List<StorageArea> bs = MasterDB.storageAreas(Long.parseLong(hubStr), whStr);
			Collections.sort(bs, StorageArea.INDEX_REVERSE);
			
			JSONObject jo = new JSONObject();
			JSONArray left = new JSONArray();
			JSONArray right = new JSONArray();
			
			for (StorageArea b : bs) {
				JSONObject bo = new JSONObject();
				bo.put("depot", b.depot);
				bo.put("flags", b.flags());
				bo.put("capacity", b.capacity);
				if (b.side == StorageSide.LEFT) {
					left.add(bo);
				} else {
					right.add(bo);
				}
			}
			
			jo.put("left", left);
			jo.put("right", right);
			
			result = jo;
		}
	} else
	if ("save_layout".equals(action)) {
		String hubStr = request.getParameter("hub");
		String whStr = request.getParameter("warehouse");
		String jsonStr = request.getParameter("json");
		if (hubStr != null && whStr != null && jsonStr != null) {
			JSONObject jo = JSONObject.fromObject(jsonStr);
			JSONArray left = jo.getJSONArray("left");
			JSONArray right = jo.getJSONArray("right");
			
			long hub = Long.parseLong(hubStr);
			
			List<StorageArea> areas = new ArrayList<StorageArea>();

			for (JSONArray sj : new JSONArray[] { left, right }) {
				for (int i = 0; i < sj.size(); i++) {
					JSONObject bo = sj.getJSONObject(i);
					StorageArea b = new StorageArea();
					b.hub = hub;
					b.warehouse = whStr;
					b.side = sj == left ? StorageSide.LEFT : StorageSide.RIGHT;
					b.index = sj.size() - 1 - i;
					b.depot = bo.getInt("depot");
					b.capacity = bo.getInt("capacity");
					b.flags(bo.getInt("flags"));
					
					areas.add(b);
				}
			}
			
			
			MasterDB.saveStorageAreas(hub, whStr, areas);
		}
	}
}
out.print(result.toString());
%>