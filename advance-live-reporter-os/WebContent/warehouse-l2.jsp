<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no">
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<meta http-equiv="Cache-Control" content="no-cache, no-store, must-revalidate">
<meta http-equiv="Pragma" content="no-cache">
<title>Warehouse overview</title>

<link rel="stylesheet" type="text/css" href="css/warehouse-header.css">
<link rel="stylesheet" type="text/css" href="css/charts/common.css">
<link rel="stylesheet" type="text/css" href="css/charts/warehouse-l2-chart.css">


<script src="js/lib/jquery-1.8.1.js" type="text/javascript"></script>

<script src="js/charts/warehouse-l2-chart.js" type="text/javascript"></script>
<script src="js/pages/warehouse-l2-page.js" type="text/javascript"></script>

</head>
<body>
<center>
	<div id="k1">
		<%@include file="warehouse-header.jspf"  %>
		<div id="k1_title"></div>
	</div>
	<div id="k2"></div>
	<div id="hand_silent_rect"></div>
	
	<form id="l2_l3_form" method="post" action="warehouse-l3.jsp" accept-charset="UTF-8">
		<input type="hidden" name="warehouse_name" value="">
		<input type="hidden" name="warehouse_option" value="">
		<input type="hidden" name="storage_order" value="">
		<input type="hidden" name="warehouse_l3_option" value="">
		<input type="hidden" name="warehouse_l3_storage_id" value="">
		<input type="hidden" name="warehouse_l3_name" value="">
	</form>
</center>
</body>

<script type="text/javascript">
	$.warehousel2page("init");
</script>
</html>