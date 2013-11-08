<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no">
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<meta http-equiv="Cache-Control" content="no-cache, no-store, must-revalidate">
<meta http-equiv="Pragma" content="no-cache">
<title>Warehouse details</title>

<link rel="stylesheet" type="text/css" href="css/warehouse-header.css">
<link rel="stylesheet" type="text/css" href="css/charts/common.css">
<link rel="stylesheet" type="text/css" href="css/charts/warehouse-l3-chart.css">


<script src="js/lib/jquery-1.8.1.js" type="text/javascript"></script>

<script src="js/charts/warehouse-l3-chart.js" type="text/javascript"></script>
<script src="js/pages/warehouse-l3-page.js" type="text/javascript"></script>

</head>
<body>
<center>
	<div id="k1">
		<%@include file="warehouse-header.jspf"  %>
	</div>
	<div id="k2"></div>
	
	<input id="warehouse_l3_storage_id" type="hidden" name="warehouse_l3_storage_id" value="<%= warehouseSwitch.getL3SelectedStorageId()%>">
</center>
</body>

<script type="text/javascript">
	$.warehousel3page("init");
</script>

</html>