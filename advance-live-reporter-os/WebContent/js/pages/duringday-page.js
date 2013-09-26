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
	var duringDayPage = {
			
			chartId : "chart",
			horizontId : "during_horizont",
			snipsId : "during_snips",
			cursorId : "during_cursor",
			
			store :{
				type: "",
				id:""
			}, 
			unit : "",
			datetime : "",
			orient : "",
			
			init: function()
			{
				var mySelf = this;
				$.headoption("init", "during");
				
				mySelf.store = $.headoption("getActiveStore");
				mySelf.unit = $.headoption("getActiveUnit");
				mySelf.datetime = $.headoption("getActualDate");
				mySelf.orient = $.headoption("getActiveOrient");
				
				$("#f_store").on("change.store", function(event)
				{
					mySelf.store = event.active;
					mySelf.callDuringChart();
					//console.log(mySelf.store.type);
				});

				$("#f_unit").on("click.unit", function(event)
				{
					mySelf.unit = event.active;
					mySelf.callDuringChart();
					//console.log(mySelf.unit);
				});
				
				$("#f_date_time").on("change.datetime", function(event)
				{
					mySelf.datetime = event.active;
					mySelf.callDuringChart();
					//console.log(mySelf.datetime);
				});
				
				$("#f_orient").on("click.orient", function(event)
				{
					mySelf.orient = event.active;
					mySelf.callDuringChart();
				});

				
				$("#handler_settings").on("settings.baus", function(event)
				{
					mySelf.callDuringChart();
				});
				
				mySelf.callDuringChart();
			},
			
			
			callDuringChart : function()
			{
				var mySelf = this;
				
				$("#" + mySelf.chartId).duringdaychart("clearBar");
				$("div#chart").duringdaychart("duringDayBar", {
				  url : "api-duringday.jsp",
				  data : {
						store_type : mySelf.store.type,
						store_id : mySelf.store.id,
						unit : mySelf.unit,
						datetime : mySelf.datetime,
						orient : mySelf.orient
				  },
				  horizontId : mySelf.horizontId,
				  snipsId : mySelf.snipsId,
				  cursorId : mySelf.cursorId
				});
			}
	};
	
	
	$.duringdaypage = function(method, param)
	{
		var op = Object.create(duringDayPage);
		
		if (method === "init")
			op[method]();
	};
	

})(jQuery, window, document);
