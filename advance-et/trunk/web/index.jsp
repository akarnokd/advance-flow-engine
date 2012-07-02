<%-- 
    Document   : index
    Created on : 21-feb-2012, 9.56.30
    Author     : farago

    Login page for Advance Elicitation Tool
--%>

<%@page import="java.util.Calendar"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<c:set var="error" scope="page" value="${param.error == '1'}"/>
<!DOCTYPE HTML>

<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <meta name="viewport" content="target-densitydpi=device-dpi, user-scalable=no, width=device-width" />
        <link href='http://fonts.googleapis.com/css?family=Open+Sans:400,700' rel='stylesheet' type='text/css'>
        <link rel="stylesheet" type="text/css" href="css/general.css"/>
        <link rel="stylesheet" type="text/css" href="css/index.css"/>
        <link rel="shortcut icon" href="/ElicitationTool/favicon.ico">
        <title>Advance Elicitation Tool | Advance Research Project</title>
    </head>
    <body>
        <div id="wrapper">
            <div id="page-header">
                <div id="logo-1" class="logo">
                    <img src="images/header/logo.png" 
                         alt="ADVANCE RESEARCH PROJECT ELICITATION TOOL LOGO"
                         id="logo">
                </div>
                <div id="logo-2" class="logo">
                    <img src="images/header/advlogo_transparent_80.png"
                         id="advance-project-logo">
                </div>
                <div class="clear"></div>
            </div>
            <div id="content">
                <form action="do.login" method="post">
                    <fieldset>
                        <label 
                            <c:if test="${pageScope.error}">class="error"</c:if>>
                            Username</label>
                        <input type="text" name="userName"
                               <c:if test="${pageScope.error}">class="error"</c:if>/>
                        <label
                            <c:if test="${pageScope.error}">class="error"</c:if>>
                            Password</label>
                        <input type="password" name="password"
                               <c:if test="${pageScope.error}">class="error"</c:if>/>
                        <div id="button-container">
                            <button type="submit">Enter</button>
                        </div>
                    </fieldset>
                </form>
            </div>
            <div id="footer">
                <p>
                    Copyright &copy; 2010-<%= Calendar.getInstance().get(Calendar.YEAR)%> 
                    <a href="http://www.advance-logistics.eu/">
                        The Advance EU 7th Framework project consortium
                    </a>
                </p>
                <p>
                    Copyright &copy; <%= Calendar.getInstance().get(Calendar.YEAR)%> 
                    Advance - 
                    <a href="http://www.ttsnetwork.net">
                        Technology Transfer System S.r.l.
                    </a>
                </p>
            </div>
        </div>
    </body>
</html>
