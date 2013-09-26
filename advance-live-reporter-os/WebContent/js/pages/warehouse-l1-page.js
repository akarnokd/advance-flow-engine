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
	var warehouseL1Page = {
			
			active : {},
			normal: false,
			
			init: function()
			{
				var mySelf = this;
				
				$.warehousemenu("init", "l1");
				mySelf.active = $.warehousemenu("getActiveMenu");
				
				mySelf.handlerFor("warehouse");
				mySelf.refreshPage();
				
				$(window).on("orientationchange", function()
				{
					if(mySelf.normal)
					{
						mySelf.callChart();
					}
				});
				
				mySelf.callChart();
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
			
			callChart : function()
			{
				var mySelf = this;
				var s = mySelf.active.warehouse.split("_");
				
				$("div#k2").warehousel1chart("clearBar");
				$("div#k2").warehousel1chart("warehouseL1Bar", {
				  url : "api-warehouse-l1.jsp",
				  data : {
					  warehouse_name : s[1]
				    }
				});
			}
	};
	
	
	$.warehousel1page = function(method, param)
	{
		var op = Object.create(warehouseL1Page);
		
		if (method === "init")
			op[method]();
	};
	

})(jQuery, window, document);
