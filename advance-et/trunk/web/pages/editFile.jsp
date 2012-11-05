<%--
    Document   : editFile
    Created on : 24-feb-2012, 12.20.08
    Author     : farago
--%>

<%@page import="com.ttsnetwork.elicitationtool.FileItem"%>
<%@page import="com.ttsnetwork.elicitationtool.FileManager"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@page import="java.util.Calendar"%>
<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE HTML>

<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <%-- <meta name="viewport" content="target-densitydpi=device-dpi, user-scalable=no, initial-scale=1.0, width=device-width" /> --%>

        <script type="text/javascript" src="https://ajax.googleapis.com/ajax/libs/jquery/1.8.2/jquery.min.js"></script>
        <script type="text/javascript" src="https://ajax.googleapis.com/ajax/libs/jqueryui/1.9.0/jquery-ui.min.js"></script>
        <script type="text/javascript" src="http://jsxgraph.uni-bayreuth.de/distrib/jsxgraphcore.js"></script>
        <script type="text/javascript" src="../js/editFile.js"></script>
<!--        <link rel="stylesheet" type="text/css" href="../css/jquery-ui-1.8.18.custom.css">-->
        <link href='http://fonts.googleapis.com/css?family=Open+Sans:400,700' rel='stylesheet' type='text/css'>
        <link rel="stylesheet" type="text/css" href="../css/jquery-ui-1.9.0.custom.min.css">
        <link rel="stylesheet" type="text/css" href="../css/general.css">
        <link rel="stylesheet" type="text/css" href="../css/editFile.css">
        <title>Edit Tree | Advance Elicitation Tool</title>
    </head>
    <body>
        <div id="wrapper">
            <div id="page-header">
                <div id="logo-1" class="logo">
                    <img src="../images/header/logo.png"
                         alt="ADVANCE RESEARCH PROJECT ELICITATION TOOL LOGO"
                         id="logo">
                </div>
                <div id="logo-2" class="logo">
                    <img src="../images/header/advlogo_transparent_80.png"
                         id="advance-project-logo">
                </div>
                <div class="clear"></div>
            </div>
            <div id="content">
                <c:set scope="page" var="item" value='<%= FileManager.getInstance().getItem(request.getParameter("fileName"))%>'/>
                <c:choose>
                    <c:when test="${empty pageScope.item}">
                        <c:redirect url="/pages/fileList.jsp"/>
                    </c:when>
                    <c:when test="${pageScope.item.user == userBean.name || empty pageScope.item.user}">
                        <c:set target="${pageScope.item}" property="user" value="${userBean.name}"/>
                    </c:when>
                    <c:when test="${pageScope.item.user != userBean.name}">
                        <c:redirect url="/pages/fileList.jsp"/>
                    </c:when>
                </c:choose>
                <div id="tree-container" class="container">
                    <div class="page-title">
                        <p>
                            XML Tree
                            <a href="../doc/AdvanceElicitationTool_QuickGuide.pdf"
                               target="_blank" title="Quick Guide">
                                ?
                            </a>
                        </p>
                    </div>
                    <div id="tree">
                    </div>
                    <div id="tree-toolbar" class="toolbar">
                        <form action="do.unsetUser" method="post">
                            <input type="hidden" name="fileName" value="${param.fileName}">
                            <button type="submit" data-role="button">Back</button>
                            <button type="button" id="save-button" disabled="disabled">Save</button>
                            <button type="button" id="reload-button">Reload</button>
                        </form>
                    </div>
                </div>
                <div id="separator"></div>
                <div id="node-editor-container" class="container">
                    <div class="page-title">
                        <p>
                            Node values editor
                        </p>
                    </div>
                    <div id="node-editor"></div>
                    <div id="node-editor-toolbar" class="toolbar"></div>
                </div>
                <div class="clear"></div>
                <input type="hidden" name="fileName" value="${param.fileName}"/>
            </div>
            <%@ include file="/WEB-INF/jspf/footer.jspf" %>
        </div>
        <div id="dialog-confirm" title="Empty the recycle bin?">
            <p><span class="ui-icon ui-icon-alert" style="float: left; margin: 0 7px 20px 0;"></span>These items will be permanently deleted and cannot be recovered. Are you sure?</p>
        </div>
    </body>
</html>
