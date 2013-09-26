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
	var warehouseL2Page = {
			
			active : {},
			normal: false,
			
			init: function()
			{
				var mySelf = this;
				
				$.warehousemenu("init", "l2");
				mySelf.active = $.warehousemenu("getActiveMenu");
				
				mySelf.handlerFor("option");
				mySelf.handlerFor("order");
				mySelf.refreshPage();

				$(window).on("orientationchange", function()
				{
					if(mySelf.normal)
					{
						mySelf.callChart();
					}
				});
						
				mySelf.callChart();
				mySelf.handlerStorageClick();
				
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
					mySelf.callChart();
				});
			},
			
			refreshPage : function()
			{
				var mySelf = this;
				$("#warehouse-page-refresh").on("click", function(event){
					mySelf.callChart();
				});
			},
			
			handlerStorageClick : function()
			{
				var mySelf = this;
				
				$("#hand_silent_rect").on("click.silent", function(event)
				{
					var s = mySelf.active.warehouse.split("_");
					var o = mySelf.active.option.split("_");
					var r = mySelf.active.order.split("_");
					var l3o = event.storage_info.split("_");
					
					$('#l2_l3_form input[name="warehouse_name"]').val(s[1]);
					$('#l2_l3_form input[name="warehouse_option"]').val(o[1]);
					$('#l2_l3_form input[name="storage_order"]').val(r[1]);
					$('#l2_l3_form input[name="warehouse_l3_option"]').val(l3o[2]);
					$('#l2_l3_form input[name="warehouse_l3_storage_id"]').val(event.storage_id);
					
					$("#l2_l3_form").submit();
				});
			},
			
			
			callChart : function()
			{
				var mySelf = this;
				var s = mySelf.active.warehouse.split("_");
				var o = mySelf.active.option.split("_");
				var r = mySelf.active.order.split("_");
				
				$("div#k2").warehousel2chart("clearBar");
				$("div#k2").warehousel2chart("warehouseL2Bar", {
				  url : "api-warehouse-l2.jsp",
				  data : {
					  warehouse_name : s[1],
					  warehouse_option : o[1],
					  storage_order : r[1]
				  },
				  headTitle : "k1_title",
				  handSilentRect : "hand_silent_rect"
				});

			}
			
	};
	
	$.warehousel2page = function(method, param)
	{
		var op = Object.create(warehouseL2Page);
		
		if (method === "init")
			op[method]();
	};

})(jQuery, window, document);