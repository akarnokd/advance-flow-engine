<%-- 
    Document   : fileList
    Created on : 23-feb-2012, 14.18.16
    Author     : farago
--%>

<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@page import="java.io.File"%>
<%@page import="java.util.Calendar"%>
<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE HTML>
<c:set var="fileList" scope="page" 
value='<%= new File(getServletContext().getInitParameter("XmlFilesRep")).list()%>'/>
<c:set var="userBean" scope="page" value="${sessionScope.userBean}"/>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <meta name="viewport" content="target-densitydpi=device-dpi, user-scalable=no, width=device-width" />
        <link href='http://fonts.googleapis.com/css?family=Open+Sans:400,700' rel='stylesheet' type='text/css'>
        <link rel="stylesheet" type="text/css" href="../css/general.css">
        <link rel="stylesheet" type="text/css" href="../css/fileList.css">
        <script type="text/javascript" src="https://ajax.googleapis.com/ajax/libs/jquery/1.7.1/jquery.min.js"></script>
        <script type="text/javascript" src="../js/fileList.js"></script>
        <title>File List | Advance Elicitation Tool</title>
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
                <div class="page-title">
                    <p>
                        Choose a file
                        <a href="../doc/AdvanceElicitationTool_QuickGuide.pdf"
                           target="_blank" title="Quick Guide">
                            ?
                        </a>
                    </p>
                </div>
                <div id="main-content">
                    <ul id="file-list">
                        <c:forEach var="fileName" items="${pageScope.fileList}">
                            <li>
                                <a href="#" class="file">${fileName}</a>
                            </li>
                        </c:forEach>
                    </ul>
                </div>
                <div class="toolbar">
                    <form method="post" id="toolbar-form">
                        <button type="button" id="download-button"
                                <c:if test="${!pageScope.userBean.allowToDownload}">
                                    disabled="disabled"
                                </c:if>>Download</button>
                        <button type="button" id="edit-button"
                                <c:if test="${!pageScope.userBean.allowToEdit}">
                                    disabled="disabled"
                                </c:if>>Edit</button>
                        <button type="button" id="view-button"
                                <c:if test="${!pageScope.userBean.allowToView}">
                                    disabled="disabled"
                                </c:if>>View</button>
                        <button type="button" id="delete-button"
                                <c:if test="${!pageScope.userBean.allowToDelete}">
                                    disabled="disabled"
                                </c:if>>Delete</button>
                        <button type="button" id="upload-button"
                                <c:if test="${!pageScope.userBean.allowToUpload}">
                                    disabled="disabled"
                                </c:if>>Upload</button>
                        <input type="hidden" name="fileName">
                    </form>
                </div>
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
        <div id="overlay-box"></div>
        <div id="upload-box">
            <form action="do.uploadXml" enctype="multipart/form-data" 
                  method="post" id="upload-form" novalidate="novalidate">
                <div class="form-header">
                    <p>Upload a file</p>
                    <button type="button" id="close-box-button">X</button>
                    <div class="clear"></div>
                </div>
                <div class="form-content">
                    <p>Choose a file from your hard disk and click 'Ok' to upload it</p>
                    <input size="25" type="file" name="file" required="required"/>
                    <button type="submit">Ok</button>
                </div>
            </form>
        </div>
    </body>
</html>
