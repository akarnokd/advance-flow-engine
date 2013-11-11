/*
 * Copyright 2010-2013 The Advance EU 7th Framework project consortium
 *
 * This file is part of Advance.
 *
 * Advance is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of
 * the License, or (at your option) any later version.
 *
 * Advance is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with Advance.  If not, see
 * <http://www.gnu.org/licenses/>.
 *
 *
 * @author csirobi, 2013.02.04.
 */



(function($, window, document, undefined)
{
	var warehouseL3Page = {
			
			active : {},
			normal: false,			
			
			init: function()
			{
				var mySelf = this;
				
				$.warehousemenu("init", "l3");
				mySelf.active = $.warehousemenu("getActiveMenu");
				
				mySelf.handlerForL3Option();
				mySelf.handlerForOrder();
				mySelf.refreshPage();
				
				$(window).on("orientationchange", function()
				{
					if(mySelf.normal)
					{
						mySelf.callChart("orient");
					}
				});
				
				mySelf.callChart("init");
				
				window.setTimeout(function()
				{
					mySelf.normal = true;
				}, 500);
				
			},
			
			handlerForL3Option : function()
			{
				var mySelf = this;
				var cl = "click.option";
				
				$("#hand_option").on(cl, function(event)
				{
					mySelf.active.option = event.active;
					mySelf.callChart("l3option");
				});
			},
			
			handlerForOrder : function()
			{
				var mySelf = this;
				var cl = "click.order";
				
				$("#hand_order").on(cl, function(event)
				{
					mySelf.active.order = event.active;
					mySelf.callChart("order");
				});
			},
			
			refreshPage : function()
			{
				var mySelf = this;
				$("#warehouse-page-refresh").on("click", function(event){
					mySelf.callChart("orient");
				});
			},
			
			callChart : function(modeType)
			{
				var mySelf = this;
				var s = mySelf.active.warehouse;
				var r = mySelf.active.order.split("_");
				var l3o = mySelf.active.option.split("_");
				var storageId = 0;
				var l3Warehouse = $('input[id="warehouse_l3_name"]').val();
				
				if(modeType == "init")
				{
					storageId = $('input[id="warehouse_l3_storage_id"]').val();
				} else if(modeType != "init")
				{
					storageId = $("div#k2").warehousel3chart("getActualStorageId");
					if(storageId != 0)
						$('input[id="warehouse_l3_storage_id"]').val(storageId);
				}
				
				$("div#k2").warehousel3chart("clearBar");
				$("div#k2").warehousel3chart("warehouseL3Bar", {
				  url : "api-warehouse-l3.jsp",
				  data : {
				  	mode : modeType,
				  	warehouse_name : s,
				  	storage_order : r[1],
				  	warehouse_l3_option : l3o[1],
				  	warehouse_l3_storage_id : storageId,
				  	warehouse_l3_name : l3Warehouse
				  }
				});
			}
	};
	
	
	$.warehousel3page = function(method, param)
	{
		var op = Object.create(warehouseL3Page);
		
		if (method === "init")
			op[method]();
	};
	

})(jQuery, window, document);
