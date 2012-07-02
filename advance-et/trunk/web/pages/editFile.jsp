<%-- 
    Document   : editFile
    Created on : 24-feb-2012, 12.20.08
    Author     : farago
--%>

<%@page import="java.util.Calendar"%>
<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE HTML>

<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <%-- <meta name="viewport" content="target-densitydpi=device-dpi, user-scalable=no, initial-scale=1.0, width=device-width" /> --%>

        <script type="text/javascript" src="https://ajax.googleapis.com/ajax/libs/jquery/1.7.1/jquery.min.js"></script>
        <script type="text/javascript" src="https://ajax.googleapis.com/ajax/libs/jqueryui/1.8.18/jquery-ui.min.js"></script>
        <script type="text/javascript" src="http://jsxgraph.uni-bayreuth.de/distrib/jsxgraphcore.js"></script>
        <script type="text/javascript" src="../js/editFile.js"></script>
        <link rel="stylesheet" type="text/css" href="../css/jquery-ui-1.8.18.custom.css">
        <link href='http://fonts.googleapis.com/css?family=Open+Sans:400,700' rel='stylesheet' type='text/css'>
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
                        <form action="" method="get">
                            <button type="submit" data-role="button">Back</button>
                        </form>
                    </div>
                </div>
                <div id="separator"></div>
                <div id="node-editor-container" class="container">
                    <div id="node-editor"></div>
                    <div id="node-editor-toolbar" class="toolbar"></div>
                </div>
                <div class="clear"></div>
                <input type="hidden" name="fileName" value="${param.fileName}"/>
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
