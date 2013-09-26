<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Expires" content="0">
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<meta http-equiv="Cache-Control" content="no-cache, no-store, must-revalidate">
<meta http-equiv="Pragma" content="no-cache">

<title>ADVANCE Live Reporter</title>

<link rel="stylesheet" type='text/css' href="css/hubdepot-header.css">
<link rel="shortcut icon" type="image/x-icon" href="images/favicon.ico">

<script src="js/lib/jquery-1.8.1.js" type="text/javascript" ></script>
</head>

<body>
<center>
<form id="login" action="login.jsp" method="post" accept-charset="UTF-8">

<div class="login-div">
	<input id="user" name="user" type="text" maxlength="20" class="login-input login-user"/>
	<input id="password" name="password" type="password" maxlength="20" class="login-input login-pwd"/>
	<input type="submit" name="submit" value="" class="login-submit"/>
</div> 
</form>
</center>

<%
Boolean isLogged = null;
synchronized(session)
{
  isLogged = (Boolean)session.getAttribute("loggedFailed");
  session.setAttribute("loggedFailed", new Boolean(false));
  
  if( (isLogged != null) && (isLogged.booleanValue()) )
  {
%>
	<center>
		<div class="failed-login-div">Unable to log you in. Invalid user name or password.</div>
	</center>
    
<%  }
}
%>

</body>
<script language="javascript">
$("#user").focus();
</script>
</html>
<% 
// } 
%>