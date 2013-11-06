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
				
				mySelf.handlerFor("option");
				mySelf.handlerFor("order");
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
			
			handlerFor : function(group)
			{
				var mySelf = this;
				var cl = "click." + group;
				
				$("#hand_" + group).on(cl, function(event)
				{
					mySelf.active[group] = event.active;
					mySelf.callChart(group);
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
				var o = mySelf.active.option.split("_");
				var r = mySelf.active.order.split("_");
				var storageId = 0;
				
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
				  	warehouse_l3_option : o[1],
				  	storage_order : r[1],
				  	warehouse_l3_storage_id : storageId
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
