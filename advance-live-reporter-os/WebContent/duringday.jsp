<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no">
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<meta http-equiv="Cache-Control" content="no-cache, no-store, must-revalidate">
<meta http-equiv="Pragma" content="no-cache">

<title>ADVANCE Live Reporter</title>
<link href="images/favicon.ico" rel="shortcut icon" type="image/x-icon">

<link rel="stylesheet" type='text/css' href="css/hubdepot-header.css">
<link rel="stylesheet" type='text/css' href="css/charts/common.css">
<link rel="stylesheet" type='text/css' href="css/charts/duringday-chart.css">
<link rel="stylesheet" type='text/css' href="css/dialog/sle.css">
<link rel="stylesheet" type='text/css' href="css/dialog/um.css">

<link rel="stylesheet" type="text/css" href="css/datetime/jquery-ui.css">
<link rel="stylesheet" type="text/css" href="css/datetime/jquery-ui-timepicker-addon.css" >
<link rel="stylesheet" type="text/css" href="css/jqtransform/jqtransform.css" >


<script src="js/lib/jquery-1.8.1.js" type="text/javascript" ></script>
<script src="js/lib/jquery-ui-1.10.3.js" type="text/javascript"></script>

<script src="js/dialog/baus.js" type="text/javascript"></script>
<script src="js/dialog/sle.js" type="text/javascript"></script>
<script src="js/dialog/um.js" type="text/javascript"></script>
<script src="js/charts/duringday-chart.js" type="text/javascript"></script>
<script src="js/pages/duringday-page.js" type="text/javascript"></script>

<script src="js/datetime/jquery-ui-timepicker-addon.js" type="text/javascript"></script>
<script src="js/datetime/jquery-ui-sliderAccess.js" type="text/javascript"></script>
<script src="js/lib/moment.js" type="text/javascript"></script>
<script src="js/lib/jquery.jqtransform.js" type="text/javascript"></script>

</head>
<body>
<center>
<%@include file='header.jspf' %>

<div id="chart"></div>

<input id="during_horizont" type="hidden" name="during_horizont" value="">
<input id="during_snips" type="hidden" name="during_snips" value="">
<input id="during_cursor" type="hidden" name="during_cursor" value="">
</center>
</body>

<script type="text/javascript">
   $.duringdaypage("init");
</script>
</html>
