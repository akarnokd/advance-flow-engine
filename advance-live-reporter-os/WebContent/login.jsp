<%@page import="java.util.Date"%>
<%@page
	import="eu.advance.logistics.live.reporter.charts.WarehouseSwitch"%>
<%@page
	import="eu.advance.logistics.live.reporter.model.UserView"%>
<%@page
	import="eu.advance.logistics.live.reporter.charts.HubDepotSwitch"%>
<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
	pageEncoding="ISO-8859-1"
	import="eu.advance.logistics.live.reporter.db.UserDB,eu.advance.logistics.live.reporter.model.User,hu.akarnokd.reactive4java.base.Option,hu.akarnokd.utils.crypto.BCrypt"%>
<%
	String userName = request.getParameter("user");
	String password = request.getParameter("password");
	User u = UserDB.getUser(userName);
	if (u != null) {
		if (u.verify(password)) {
	switch (u.view) {
	
	case HUB:
		synchronized (session) {
			session.setAttribute("USER", u);
		}
		response.sendRedirect("summary.jsp");
// 		response.sendRedirect("duringday.jsp");
		break;
	
	case WAREHOUSE:
		synchronized (session) {
			session.setAttribute("USER", u);
		}
		response.sendRedirect("warehouse-l1.jsp");
		break;
		
	default:
	}
		} else {
	synchronized (session) {
		session.setAttribute("loggedFailed", new Boolean(true));
	}
	response.sendRedirect("index.jsp");
		}
	} else {
		synchronized (session) {
	session.setAttribute("loggedFailed", new Boolean(true));
		}
		response.sendRedirect("index.jsp");
	}
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<meta http-equiv="Cache-Control"
	content="no-cache, no-store, must-revalidate" />
<meta http-equiv="Pragma" content="no-cache" />
<title>ADVANCE Live Reporter</title>
<link rel="stylesheet" type='text/css' href='css/advance.css' />
<link href="images/favicon.ico" rel="shortcut icon" type="image/x-icon" />
</head>
<body>
	<center>
		<img src='images/Advance_logo.png'>
		<h1>ADVANCE Live Reporter</h1>
		<h2>Invalid login</h2>
		<p>Unable to log you in. Invalid user name or password.</p>
		<form action='index.jsp'>
			<input type='submit' value='Back' />
		</form>
	</center>
</body>
</html>