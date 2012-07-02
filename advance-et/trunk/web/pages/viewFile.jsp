<%-- 
    Document   : viewFile
    Created on : 24-apr-2012, 10.24.19
    Author     : farago
--%>

<%@page import="java.util.Calendar"%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <meta name="viewport" content="target-densitydpi=device-dpi, user-scalable=yes, initial-scale=1.0, width=device-width" />
        <script type="text/javascript" src="https://ajax.googleapis.com/ajax/libs/jquery/1.7.1/jquery.min.js"></script>
        <script type="text/javascript" src="../js/jquery.jsPlumb-1.3.8-all-min.js"></script>
        <script type="text/javascript" src="../js/viewFile.js"></script>
        <link href='http://fonts.googleapis.com/css?family=Open+Sans:400,700' rel='stylesheet' type='text/css'>
        <link rel="stylesheet" href="../css/general.css">
        <link rel="stylesheet" href="../css/viewFile.css">
        <title>View ${param.fileName} | Advance Elicitation Tool</title>
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
                        File ${param.fileName}
                        <a href="../doc/AdvanceElicitationTool_QuickGuide.pdf"
                           target="_blank" title="Quick Guide">
                            ?
                        </a>
                    </p>
                </div>
                <div id="tree">
                    <input type="hidden" name="filename" value="${param.fileName}">
                </div>
                <div id="toolbar">
                    <form action="fileList.jsp" method="post">
                        <button type="submit">Back</button>
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
    </body>
</html>
