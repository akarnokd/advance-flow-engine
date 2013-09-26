<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"
    
%><%
	synchronized (session) {
		session.invalidate();
	}
	response.sendRedirect("index.jsp");
%>