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


// Utility
if (typeof Object.create !== 'function')
{
	Object.create = function(o)
	{
		if (arguments.length > 1)
		{
			throw new Error(
					'Object.create implementation only accepts the first parameter.');
		}
		function F()
		{
		}
		F.prototype = o;
		return new F();
	};
}

(function($, window, document, undefined)
{

	var duringDayMethods = {
		svgNS : "http://www.w3.org/2000/svg",
		cursorIcon: {
			top: "./images/hubdepot/highlighter_top.png",
			bar: "./images/hubdepot/highlighter_bar.png",
			bottom:"./images/hubdepot/highlighter_btm.png"
		},
		currentIcon: {
			top: "./images/hubdepot/current_top.png",
			bottom:"./images/hubdepot/current_btm.png"
		},
		snipIcon: {
			on: "./images/hubdepot/vis_on.png",
			off: "./images/hubdepot/vis_off.png"
		},

		workArea : {},
		type : "",
		unit : "",
		orient: "",
		amount : {pred: "predicted", act: "actual"},
		snip : {special : 0, priority: 1, standard: 2, all : 3},
		visibleSnip : [],
		actHorizont : {row: "", col: ""},
		cursor: {xPosAt: 0, xCoordPx: 0},
		
		// Chart parameters: 
		// svg area width
		svgAreaWidth: 990,
	  // svg area height
		svgAreaHeight: 365,
	  // ruler width (fixed)
		rulerFixWidth: 48,
	  // space width between the ruler and the first time slot of chart
		leftSpaceWidth: 20, 
		// top padding between the top cursor triangle and the axis
		axisTopPadding: 15,
		// effective chart height
		chartHeight: 317,
		// bottom padding between the axis and the bottom cursor triangle
		axisBottomPadding: 3,
		// cursor triangle height
		cursorTriangHeight: 8,
		// the X coord of the first time slot of chart
		chartStartAtPx: 0,
		// number of quarter time slot
		quarterSlots: 0,
		// the first quarter time slot of data
		quarterChartFrom: 0,
		// distance between two quarter time slot (in pixel)
		quarterDistPx: 0,
		
		duringDayBar : function(elem, index, options)
		{
			var mySelf = this;
			mySelf.elem = elem;
			mySelf.$elem = $(elem);
			mySelf.mode = "duringday";

			var settings = {};
			settings = {
				url : "",
				type : "POST",
				data : {},
				horizontId : "",				
				snipsId : "",
				cursorId : "",
			};

			if (typeof options === 'string')
				settings.url = options;
			else
				$.extend(settings, options);
			
			$("body").removeClass("show-error");
			$("body").addClass("loading");

			mySelf.callAjax(settings)
					.done(
							function(result)
							{
								if ((result.direction !== undefined) &&
										(result.status !== undefined) &&
										(result.xcoord !== undefined) &&
										(result.ycoord !== undefined) &&
										(result.bars !== undefined))
								{
									mySelf.clearBar(elem, index);
									
									mySelf.type = result.direction.type;
									mySelf.unit = result.direction.unit;
									mySelf.orient = result.direction.orient;
									
									if($('input[id^="' + settings.horizontId + '"]').val() != "")
									{
										var temp = [];
										temp = $('input[id^="' + settings.horizontId + '"]').val().split("_");
										mySelf.actHorizont.row = temp[0];
										mySelf.actHorizont.col = temp[1];
									}
									else
									{
										mySelf.actHorizont.row = "";
										mySelf.actHorizont.col = "";
									}
									
									mySelf.visibleSnip = $('input[id^="' + settings.snipsId + '"]').val().split("_");
									if(mySelf.visibleSnip[0].length == 0)
										mySelf.visibleSnip = [];
									
									mySelf.quarterSlots = result.xcoord.quarterSlots * 1.0;
									mySelf.quarterChartFrom = result.xcoord.quarterChartFrom * 1.0;
									
									mySelf.workArea.holderId = mySelf.defineHolder(mySelf.$elem, index, mySelf.mode + "-holder");
									
									mySelf.workArea.titleId = mySelf.workArea.holderId + "_title";
									mySelf.createDIV(mySelf.workArea.holderId, mySelf.workArea.titleId, "title");
									$("#" + mySelf.workArea.titleId).addClass("title").html(result.direction.name);
									
									mySelf.workArea.frameId = mySelf.workArea.holderId + "_frame";
									mySelf.createDIV(mySelf.workArea.holderId, mySelf.workArea.frameId, "frame");
									
									mySelf.workArea.detailsId = mySelf.workArea.holderId + "_details";
									mySelf.createDIV(mySelf.workArea.holderId, mySelf.workArea.detailsId, "details");

									mySelf.workArea.svgAreaId = mySelf.workArea.holderId + "_svg-area";
									mySelf.createDIV(mySelf.workArea.frameId, mySelf.workArea.svgAreaId, "svg-area");
									
									mySelf.workArea.svgId = mySelf.workArea.holderId + "_svg";
									mySelf.createSVG(mySelf.workArea.svgAreaId, mySelf.workArea.svgId);

									mySelf.workArea.svgXCoordId = mySelf.workArea.holderId + "_svg_xcoord";
									mySelf.createGroup(mySelf.workArea.svgId, mySelf.workArea.svgXCoordId);
									mySelf.workArea.svgYCoordId = mySelf.workArea.holderId + "_svg_ycoord";
									mySelf.createGroup(mySelf.workArea.svgId, mySelf.workArea.svgYCoordId);
									
									mySelf.workArea.svgGrTotalId = mySelf.workArea.holderId + "_svg_gr_total";
									mySelf.workArea.svgGrVisibleId = mySelf.workArea.holderId + "_svg_gr_visible";
									mySelf.workArea.svgGrHorizontId = mySelf.workArea.holderId + "_svg_gr_horizont";
									
									mySelf.workArea.cursorAreaId = mySelf.workArea.holderId + "_cursor-area";
									mySelf.createDIV(mySelf.workArea.frameId, mySelf.workArea.cursorAreaId, "cursor-area");
									
									mySelf.workArea.cursorId = mySelf.workArea.holderId + "_cursor";
									mySelf.createDIV(mySelf.workArea.cursorAreaId, mySelf.workArea.cursorId, "cursor");
									
									// Set the svg part
									mySelf.setGraphSize();
									mySelf.setXCoord(result.xcoord);
									mySelf.setYCoord(result.ycoord);
									
									var barLength = $(result.bars).size();
									if(barLength > 0)
									{
										mySelf.setTotalCharts(result.bars);
										mySelf.setVisibleCharts(result.bars);
										
									  // Set the cursor part
										mySelf.setCursorArea(result.xcoord);
										mySelf.setCursorDraw();
										mySelf.setCursorPosition(settings.cursorId, result.xcoord);

										// Refresh the horizont chart (after cursor is set)
										mySelf.refreshHorizontChart(result.bars);

										// Set the details part
										mySelf.createDetailsLeft(result.status.legendInfo);
										mySelf.createDetailsRight(result.status.legendSnip);
										mySelf.setFixPartDetails(result.bars);
										mySelf.setSwitchPartDetails(result.bars);
										mySelf.setCursorPartDetails(result.bars);
										
										// Set the listeners
										mySelf.switchHorizontListener(settings.horizontId, result.bars);
										mySelf.switchSnipListener(settings.snipsId, result.bars);
										mySelf.cursorDragListener(settings.cursorId, result.bars);
									}
									
								} else if (result.error !== undefined)
								{
									var v = "";
									$.each(result.error, function(key, value){ v = value; });
									$("div.error-msgbox p#error_msg").html(v);
									$("body").addClass("show-error");
								}
								
							})
							
					.fail(function(jqXHR, textStatus, errorThrown)
							{
								$("body").addClass("show-error");
							})							
					
					.always(function()
							{
								$("body").removeClass("loading");						
							});
		},


		callAjax : function(settings)
		{
			return $.ajax({
				url : settings.url,
				type : settings.type,
				data : settings.data,
				dataType : "json"
			});

		},
		
		defineHolder : function($elem, index, style)
		{
			var mySelf = this; 
			var holderId = null;
			if ($elem.is("div"))
			{
				if ($elem.attr("id") === undefined)
					$elem.attr("id", mySelf.mode + "_" + index);
				holderId = $elem.attr("id");
				$("#" + holderId).addClass(style);
				$("#" + holderId).attr("adv_type", mySelf.mode);
			}
			return holderId;
		},
		
		createDIV : function(appendId, childId, style)
		{
			var childDiv = $("<div>");
			childDiv.attr("id", childId);
			childDiv.attr("class", style);
			$("#" + appendId).append(childDiv);
		},
		
		createSVG : function(appendId, svgId)
		{
			var mySelf = this;
			var mySvg = document.createElementNS(mySelf.svgNS, "svg");
			mySvg.setAttribute("id", svgId);
			mySvg.setAttribute("xmlns", mySelf.svgNS);
			mySvg.setAttribute("xmlns:xlink", "http://www.w3.org/1999/xlink");
			mySvg.setAttribute("version", "1.2");
			mySvg.setAttribute("baseProfile", "tiny");
			document.getElementById(appendId).appendChild(mySvg);
		},

		createGroup : function(appendId, groupId)
		{
			var mySelf = this;
			
			var myGroup = document.createElementNS(mySelf.svgNS, "g");
			myGroup.setAttribute("id", groupId);
			document.getElementById(appendId).appendChild(myGroup);
		},
		
		insertBeforeGroup : function(parentId, beforeId, groupId)
		{
			var mySelf = this;
			var beforeObj = document.getElementById(beforeId);
			
			var myGroup = document.createElementNS(mySelf.svgNS, "g");
			myGroup.setAttribute("id", groupId);
			document.getElementById(parentId).insertBefore(myGroup, beforeObj);
		},

		/*
		 * Creates the left side of the Details div.
		 */
		createDetailsLeft: function(legendInfo)
		{
			var mySelf = this;
			var workArea = mySelf.workArea;
			var actHorizont = mySelf.actHorizont;
			var headCols = ["", "", "Selected", "Latest"];
			var colCSS = ["icon-cell font-legend", "descript-cell-left font-legend", "number-cell font-legend", "number-cell font-legend"];
			
			var coverDiv = $("<div>").attr("id", workArea.detailsId + "_left").attr("class", "div-cover cover-left");
			var table = $("<table>");
			var tbody = $("<tbody>");
			table.append(tbody);
			
			var tCell, tDiv;
			
			var tRow = $("<tr>");
			$.each(headCols, function(idx, elem)
			{
				tCell = $("<td>").html(elem).attr("class", colCSS[idx]);
				tRow.append(tCell);
			});
			tbody.append(tRow);
			
			for(var rowAt = 0; rowAt < 4; rowAt++)
			{
				tRow = $("<tr>");
				
				// 0. column
				if(rowAt == 2)
					tDiv = $("<div>").attr("class", legendInfo[rowAt].info.replace("_", "-") + " " + mySelf.orient + "-standard");
				else
					tDiv = $("<div>").attr("class", legendInfo[rowAt].info.replace("_", "-"));
				tCell = $("<td>").html("").attr("class", colCSS[0]); tRow.append(tCell.append(tDiv));
				
				// 1. column
				tCell = $("<td>").html(legendInfo[rowAt].message).attr("class", colCSS[1]); tRow.append(tCell);
				
				// 2. column
				tCell = $("<td>").html("").attr("id", legendInfo[rowAt].info + "_sel").attr("class", (rowAt < 2) ? colCSS[2] : colCSS[2] + " cursor-cell"); tRow.append(tCell);
				
				// 3. column
				tCell = $("<td>").html("").attr("id", legendInfo[rowAt].info + "_lat").attr("class", (rowAt < 2) ? colCSS[3] : colCSS[3] + " cursor-cell"); tRow.append(tCell);
				
				tbody.append(tRow);
			}
			
			$("#" + workArea.detailsId).append(coverDiv.append(table));
			$("div#" + workArea.detailsId + " td#act_" + actHorizont.row + "_" + actHorizont.col).toggleClass("act-selected");
			
		},
		
		/*
		 * Creates the right side of the Details div.
		 */
		createDetailsRight: function(legendSnip)
		{
			var mySelf = this;
			var workArea = mySelf.workArea;
			var headCols = ["", "", "", "Selected", "Latest"];
			var colCSS = ["icon-cell font-legend", "icon-cell font-legend", "descript-cell-right font-legend", "number-cell font-legend", "number-cell font-legend"];
			
			var coverDiv = $("<div>").attr("id", workArea.detailsId + "_right").attr("class", "div-cover cover-right");
			var table = $("<table>");
			var tbody = $("<tbody>");
			table.append(tbody);
			
			var tCell, tDiv, tImg;
			
			var tRow = $("<tr>");
			$.each(headCols, function(idx, elem)
			{
				tCell = $("<td>").html(elem).attr("class", colCSS[idx]);
				tRow.append(tCell);
			});
			tbody.append(tRow);

			for(var rowAt = 0; rowAt < 3; rowAt++)
			{
				tRow = $("<tr>");
				
				tImg = $("<img>").attr("id", "switch_" + legendSnip[rowAt].info).attr("style", "cursor:pointer");
				if($.inArray(legendSnip[rowAt].info, mySelf.visibleSnip) != -1)
					tImg.attr("src", mySelf.snipIcon.on);
				else
					tImg.attr("src", mySelf.snipIcon.off);
				tCell = $("<td>").html("").attr("class", colCSS[0]); tRow.append(tCell.append(tImg));
				
				tDiv = $("<div>").attr("class", "snip-default " + mySelf.orient + "-" + legendSnip[rowAt].info);
				tCell = $("<td>").html("").attr("class", colCSS[1]); tRow.append(tCell.append(tDiv));
				
				tCell = $("<td>").html(legendSnip[rowAt].message).attr("class", colCSS[2]); tRow.append(tCell);
				tCell = $("<td>").html("").attr("id", legendSnip[rowAt].info + "_sel").attr("class", colCSS[3]); tRow.append(tCell);
				tCell = $("<td>").html("").attr("id", legendSnip[rowAt].info + "_lat").attr("class", colCSS[4]); tRow.append(tCell);
				
				tbody.append(tRow);
			}
			
			$("#" + workArea.detailsId).append(coverDiv.append(table));
		},
		
		/*
		 * Sets the fix values to the details div.
		 */
		setFixPartDetails: function(rBars)
		{
			var mySelf = this;
			var barSize = $(rBars).size();
			var v ;
			
			v = rBars[barSize -1][mySelf.amount.pred][mySelf.snip.all].value;
			$("#" + mySelf.workArea.detailsId + " td#pred_total_lat").html(mySelf.roundValue(v));
			v = rBars[barSize -1][mySelf.amount.act][mySelf.snip.all].value;
			$("#" + mySelf.workArea.detailsId + " td#act_total_lat").html(mySelf.roundValue(v));

			v = rBars[barSize -1][mySelf.amount.act][mySelf.snip.special].value;
			$("#" + mySelf.workArea.detailsId + " td#special_lat").html(mySelf.roundValue(v));
			v = rBars[barSize -1][mySelf.amount.act][mySelf.snip.priority].value;
			$("#" + mySelf.workArea.detailsId + " td#priority_lat").html(mySelf.roundValue(v));
			v = rBars[barSize -1][mySelf.amount.act][mySelf.snip.standard].value;
			$("#" + mySelf.workArea.detailsId + " td#standard_lat").html(mySelf.roundValue(v));
		},

		/*
		 * Sets that variable values to the details div which are modified by switch icons.
		 */
		setSwitchPartDetails:  function(rBars)
		{
			var mySelf = this;
			var barSize = $(rBars).size();
			
			var predVis = 0, actVis = 0;
			$.each(mySelf.visibleSnip, function(idx, elem)
			{
				predVis += (1.0 * rBars[barSize -1][mySelf.amount.pred][mySelf.snip[elem]].value);
				actVis  += (1.0 * rBars[barSize -1][mySelf.amount.act][mySelf.snip[elem]].value);
			});

			$("#" + mySelf.workArea.detailsId + " td#pred_visible_lat").html(mySelf.roundValue(predVis));
			$("#" + mySelf.workArea.detailsId + " td#act_visible_lat").html(mySelf.roundValue(actVis));
			
			predVis = 0, actVis = 0;
			$.each(mySelf.visibleSnip, function(idx, elem)
			{
				predVis += (1.0 * rBars[mySelf.cursor.xPosAt][mySelf.amount.pred][mySelf.snip[elem]].value);
				actVis  += (1.0 * rBars[mySelf.cursor.xPosAt][mySelf.amount.act][mySelf.snip[elem]].value);
			});

			$("#" + mySelf.workArea.detailsId + " td#pred_visible_sel").html(mySelf.roundValue(predVis));
			$("#" + mySelf.workArea.detailsId + " td#act_visible_sel").html(mySelf.roundValue(actVis));
			
		},
		
		/*
		 * Sets that variable values to the details div which are modified by cursor.
		 */
		setCursorPartDetails: function(rBars)
		{
			var mySelf = this;
			var v;
			
			v = rBars[mySelf.cursor.xPosAt][mySelf.amount.pred][mySelf.snip.all].value;
			$("#" + mySelf.workArea.detailsId + " td#pred_total_sel").html(mySelf.roundValue(v));
			v = rBars[mySelf.cursor.xPosAt][mySelf.amount.act][mySelf.snip.all].value;
			$("#" + mySelf.workArea.detailsId + " td#act_total_sel").html(mySelf.roundValue(v));

			predVis = 0, actVis = 0;
			$.each(mySelf.visibleSnip, function(idx, elem)
			{
				predVis += (1.0 * rBars[mySelf.cursor.xPosAt][mySelf.amount.pred][mySelf.snip[elem]].value);
				actVis  += (1.0 * rBars[mySelf.cursor.xPosAt][mySelf.amount.act][mySelf.snip[elem]].value);
			});

			$("#" + mySelf.workArea.detailsId + " td#pred_visible_sel").html(mySelf.roundValue(predVis));
			$("#" + mySelf.workArea.detailsId + " td#act_visible_sel").html(mySelf.roundValue(actVis));
			
			v = rBars[mySelf.cursor.xPosAt][mySelf.amount.act][mySelf.snip.special].value;
			$("#" + mySelf.workArea.detailsId + " td#special_sel").html(mySelf.roundValue(v));
			v = rBars[mySelf.cursor.xPosAt][mySelf.amount.act][mySelf.snip.priority].value;
			$("#" + mySelf.workArea.detailsId + " td#priority_sel").html(mySelf.roundValue(v));
			v = rBars[mySelf.cursor.xPosAt][mySelf.amount.act][mySelf.snip.standard].value;
			$("#" + mySelf.workArea.detailsId + " td#standard_sel").html(mySelf.roundValue(v));
		},
		
		/*
		 * Defines listener for switch on-of snips.
		 */
		switchSnipListener : function(snipsId, rBars)
		{
			var mySelf = this;
			var workArea = mySelf.workArea;
			var vSnip;
			
			$('div#' + workArea.detailsId + ' ' + 'img[id^="switch_"]').on("click", function(e)
			{
				vSnip = [];
				if( $(this).attr("src") == mySelf.snipIcon.on)
					$(this).attr("src", mySelf.snipIcon.off);
				else
					$(this).attr("src", mySelf.snipIcon.on);
				
				$.each(mySelf.snip, function(key, value)
				{
					if ($("div#" + workArea.detailsId + " " + "img#switch_" + key).attr("src") == mySelf.snipIcon.on)
						vSnip.push(key);
				});
				
				mySelf.visibleSnip = vSnip;
				$('input[id^="' + snipsId + '"]').val(vSnip.join("_"));
				
				mySelf.setVisibleCharts(rBars);
				mySelf.setSwitchPartDetails(rBars);
				mySelf.refreshHorizontChart(rBars);
			});
			
		},
		
		/*
		 * Sets the Graph size of the svg.
		 */
		setGraphSize : function()
		{
			var mySelf = this;
			var workArea = mySelf.workArea;  
			var bodyWidth = $(window).width() - 2 * 16;
			
			if(1010 < bodyWidth)
			{
				$("#" + workArea.holderId).width(bodyWidth);
				mySelf.svgAreaWidth = $("#" + workArea.svgAreaId).width();
				mySelf.svgAreaHeight = $("#" + workArea.svgAreaId).height();
			}	
			
			// Calculate the guarter distance 
			var chartWidth = mySelf.svgAreaWidth - (2 * mySelf.rulerFixWidth) - (2 * mySelf.leftSpaceWidth);
			mySelf.quarterDistPx = Math.floor(chartWidth / mySelf.quarterSlots);
			// Modify the space width between ruler and the chart 
			var realSpace = mySelf.svgAreaWidth - (2 * mySelf.rulerFixWidth) - (mySelf.quarterDistPx * mySelf.quarterSlots);
			mySelf.leftSpaceWidth = Math.floor(realSpace / 2);
			// Set the starting point of the chart
			mySelf.chartStartAtPx = mySelf.rulerFixWidth + mySelf.leftSpaceWidth;
			
			mySelf.setSVGViewBox(workArea.svgId, mySelf.svgAreaWidth, mySelf.svgAreaHeight);
			
		},
		
		/*
		 * Sets the SVG ViewBox.
		 */
		setSVGViewBox : function(svgId, w, h)
		{
			$("#" + svgId).width(w);
			$("#" + svgId).height(h);

			var viewBox = "0 0 " + w + " " + h;
			document.getElementById(svgId).setAttribute("viewBox", viewBox);
		},
		
		/*
		 * Sets the "xcoord" group of svg based on xCoord input data.
		 */
		setXCoord: function(xCoord)
		{
			var mySelf = this;
			var appendId = mySelf.workArea.svgXCoordId;
			var quarterLine = xCoord.quarterLine * 1.0;
			var x;
			
			for(var i = 0; i < mySelf.quarterSlots + 1; i++)
			{
				if( (i % quarterLine) == 0)
				{	
					x = mySelf.chartStartAtPx + i * mySelf.quarterDistPx;
					mySelf.drawLine(appendId, x,
							mySelf.cursorTriangHeight,
							x,
							mySelf.cursorTriangHeight + mySelf.axisTopPadding + mySelf.chartHeight + mySelf.axisBottomPadding);
				}
			}
			
			$(xCoord.displayHours).each(function(index, element)
			{
				var hourSlot = 4;
				x = mySelf.chartStartAtPx + (index * mySelf.quarterDistPx * hourSlot);
				mySelf.drawText(appendId, x, mySelf.svgAreaHeight - 3, element, "write");
			});
			
			if(xCoord.quarterChartWidth >= 0)
			{
				x = mySelf.chartStartAtPx + ((xCoord.quarterChartFrom + xCoord.quarterChartWidth) * mySelf.quarterDistPx);
				mySelf.drawLine(appendId, x,
						mySelf.cursorTriangHeight,
						x,
						mySelf.cursorTriangHeight + mySelf.axisTopPadding + mySelf.chartHeight + mySelf.axisBottomPadding);

				mySelf.drawImg(appendId, x - 6,
						0,
						11, mySelf.cursorTriangHeight, mySelf.currentIcon.top);
				mySelf.drawImg(appendId, x - 6,
						mySelf.cursorTriangHeight + mySelf.axisTopPadding + mySelf.chartHeight + mySelf.axisBottomPadding,
						11, mySelf.cursorTriangHeight, mySelf.currentIcon.bottom);
			}
			
		},
		
		/*
		 * Sets the "ycoord" group of svg based on yCoord input data.
		 */
		setYCoord: function(yCoord)
		{
			var mySelf = this;
			var appendId = mySelf.workArea.svgYCoordId;
			var noOfTick = 1.0 * yCoord.noOfTick;
			var textObj, textHeight;
			
			// Left side coordinate
			mySelf.drawLine(appendId, mySelf.rulerFixWidth,
					mySelf.cursorTriangHeight,
					mySelf.rulerFixWidth,
					mySelf.cursorTriangHeight + mySelf.axisTopPadding + mySelf.chartHeight);
			
			// Left-top scale
			mySelf.drawText(appendId, (mySelf.rulerFixWidth / 2), mySelf.cursorTriangHeight + 3, "\u00D7" + yCoord.scale, "write");
			
			// Left-bottom scale
			mySelf.drawText(appendId, (mySelf.rulerFixWidth / 2), mySelf.svgAreaHeight - 3, "\u00D7" + yCoord.scale, "write");
			
			// Right side coordinate
			mySelf.drawLine(appendId, mySelf.svgAreaWidth - mySelf.rulerFixWidth,
					mySelf.cursorTriangHeight,
					mySelf.svgAreaWidth - mySelf.rulerFixWidth,
					mySelf.cursorTriangHeight + mySelf.axisTopPadding + mySelf.chartHeight);
			
			// Right-top scale
			mySelf.drawText(appendId, mySelf.svgAreaWidth - (mySelf.rulerFixWidth / 2), mySelf.cursorTriangHeight + 3, "\u00D7" + yCoord.scale, "write");
			
			// Right-bottom scale
			mySelf.drawText(appendId, mySelf.svgAreaWidth - (mySelf.rulerFixWidth / 2), mySelf.svgAreaHeight - 3, "\u00D7" + yCoord.scale, "write");
			
			// Draw the tick & value of it
			var zeroY = mySelf.cursorTriangHeight + mySelf.axisTopPadding + mySelf.chartHeight;
			for ( var i = 0; i < (noOfTick + 1); i++)
			{
				var scaleY = zeroY - mySelf.backTo(i * yCoord.normalScale);
				
				mySelf.drawLine(appendId, mySelf.rulerFixWidth - 4,
						scaleY,
						mySelf.svgAreaWidth - mySelf.rulerFixWidth + 3,
						scaleY);
				
				textObj = mySelf.drawText(appendId, mySelf.rulerFixWidth - 15, scaleY, i * yCoord.tickValue, "write");
				textHeight = textObj.getBBox().height / 2.0;
				textObj.setAttributeNS(null, "y", scaleY + textHeight - 5);
				
				textObj = mySelf.drawText(appendId, mySelf.svgAreaWidth - mySelf.rulerFixWidth + 15, scaleY, i * yCoord.tickValue, "write");
				textHeight = textObj.getBBox().height / 2.0;
				textObj.setAttributeNS(null, "y", scaleY + textHeight - 5);
			}	
			
		},
		
		/*
		 * Sets the "total" group of svg based on bars input data.
		 */
		setTotalCharts: function(bars)
		{
			var mySelf = this, workArea = mySelf.workArea;
			var x, yPred, yActual;
			var pointsPred = "", pointsActual = "";
			
			var zeroY = mySelf.cursorTriangHeight + mySelf.axisTopPadding + mySelf.chartHeight;
			$(bars).each(function(iSlot, eSlot)
			{
				x = mySelf.chartStartAtPx + (mySelf.quarterChartFrom * mySelf.quarterDistPx) + (iSlot * mySelf.quarterDistPx);
				yPred   = zeroY - mySelf.backTo(eSlot[mySelf.amount.pred][mySelf.snip.all].normalValue);
				yActual = zeroY - mySelf.backTo(eSlot[mySelf.amount.act][mySelf.snip.all].normalValue);
				
				pointsPred   += x + "," + yPred + " ";
				pointsActual += x + "," + yActual + " ";
				
			});
			
			mySelf.insertBeforeGroup(workArea.svgId, workArea.svgXCoordId, workArea.svgGrTotalId);
			mySelf.drawPolyLine(workArea.svgGrTotalId, pointsPred, "line-default predicted-total");
			mySelf.drawPolyLine(workArea.svgGrTotalId, pointsActual, "line-default " + mySelf.orient + "-actual-total");
		},
		
		/*
		 * Sets the "visible" group of svg based on bars input data.
		 */
		setVisibleCharts: function(bars)
		{
			var mySelf = this, workArea = mySelf.workArea;
			var snipLength = $(mySelf.visibleSnip).length;

			// Clear the visible group, if exists 
			if( $("#" + workArea.svgGrVisibleId) !== undefined)
			{
				$("#" + workArea.svgGrVisibleId).remove();
			}
			
			// If there is any visible snip.. 
			if(0 < snipLength)
			{
				var x, zeroY;
				var yTemp = 0, points = {}, pointsPred = ""; 
				
				// Initialize
				$.each(mySelf.visibleSnip, function(idx, elem)
				{
					points[elem] = ""; 
				});
				
				x = mySelf.chartStartAtPx + (mySelf.quarterChartFrom * mySelf.quarterDistPx);
				zeroY = mySelf.cursorTriangHeight + mySelf.axisTopPadding + mySelf.chartHeight;
				// Set the x, zeroY as starting point
				$.each(mySelf.visibleSnip, function(idx, elem)
				{
					points[elem] += x + "," + zeroY + " "; 
				});

				// For every timeSlot.. 
				$(bars).each(function(iSlot, eSlot)
				{
					x = mySelf.chartStartAtPx + (mySelf.quarterChartFrom * mySelf.quarterDistPx) + (iSlot * mySelf.quarterDistPx);
					
					// Calculating points of every visible snip
					yTemp = 0;
					// For every visible snip (special, priority, standard) at this timeSlot
					$.each(mySelf.visibleSnip, function(idx, elem)
					{
						yTemp += mySelf.backTo(eSlot[mySelf.amount.act][mySelf.snip[elem]].normalValue);
						points[elem] += x + "," + (zeroY - yTemp) + " "; 
					});
					
					// Calculating points of predicted visible
					yTemp = 0;
					if( snipLength != 3)
					{
						// For every predicted visible snip (special, priority, standard) at this timeSlot
						$.each(mySelf.visibleSnip, function(idx, elem)
						{
							yTemp += mySelf.backTo(eSlot[mySelf.amount.pred][mySelf.snip[elem]].normalValue);
						});
						pointsPred += x + "," + (zeroY - yTemp) + " ";
					}
					
				});
				
				// Set the x, zeroY as ending point
				$.each(mySelf.visibleSnip, function(idx, elem)
				{
					points[elem] += x + "," + zeroY; 
				});
				
				// Insert the visible group to the svg and draw polylines in backward way
				mySelf.insertBeforeGroup(workArea.svgId, workArea.svgGrTotalId, workArea.svgGrVisibleId);
				var maxIdx = snipLength - 1;
				$.each(mySelf.visibleSnip, function(idx, elem)
				{
					mySelf.drawPolyLine(workArea.svgGrVisibleId, points[mySelf.visibleSnip[maxIdx - idx]], "area-default " + mySelf.orient + "-" + mySelf.visibleSnip[maxIdx - idx]);
				});
				if( snipLength != 3)
				{
					mySelf.drawPolyLine(workArea.svgGrVisibleId, pointsPred, "line-default predicted-visible");
				}
			}
		},
		
		/*
		 * Draws the real horizont group of svg based on bars input data.
		 */
		drawHorizontChart: function(timeSlotBars)
		{
			var mySelf = this;
			var workArea = mySelf.workArea;
			var actHorizont = mySelf.actHorizont;
			var y = 0, points = "";
			
			var xFrom = mySelf.chartStartAtPx + (mySelf.quarterChartFrom * mySelf.quarterDistPx);
			var xTo = mySelf.chartStartAtPx + (mySelf.quarterChartFrom * mySelf.quarterDistPx) + (mySelf.quarterSlots * mySelf.quarterDistPx);
			var zeroY = mySelf.cursorTriangHeight + mySelf.axisTopPadding + mySelf.chartHeight;
			
			// Clear the visible horizont group, if exists 
			mySelf.clearHorizontChart();
			
			// Real horizont is total, or visible but every snip is shown
			if((actHorizont.row == "total") || ((actHorizont.row == "visible") && mySelf.visibleSnip.length == 3))
			{
				y = zeroY - mySelf.backTo(timeSlotBars[mySelf.amount.act][mySelf.snip.all].normalValue);
			}
			// Real horizont is visible and less than 3 is shown
			else if(actHorizont.row == "visible")
			{
				var yTemp = 0;
				$.each(mySelf.visibleSnip, function(idx, elem)
				{
					yTemp += mySelf.backTo(timeSlotBars[mySelf.amount.act][mySelf.snip[elem]].normalValue);
				});
				y = zeroY - yTemp;
			}
			
			points = xFrom + "," + zeroY + " " + xFrom + "," + y + " " + xTo + "," + y + " " + xTo + "," + zeroY;
			mySelf.insertBeforeGroup(workArea.svgId, workArea.svgGrVisibleId, workArea.svgGrHorizontId);
			mySelf.drawPolyLine(workArea.svgGrHorizontId, points, "area-default act-horizont");
			
		},
		
		/*
		 * Clears the real horizont group of svg.
		 */
		clearHorizontChart: function()
		{
			var mySelf = this;
			var workArea = mySelf.workArea;
			
			// Clear the visible group, if exists 
			if( $("#" + workArea.svgGrHorizontId) !== undefined)
			{
				$("#" + workArea.svgGrHorizontId).remove();
			}
		},
		
		/*
		 * Refresh the real horizont chart group of svg.
		 */
		refreshHorizontChart: function(rBars)
		{
			var mySelf = this;
			var actHorizont = mySelf.actHorizont;
			
			if((actHorizont.row == "") && (actHorizont.col == ""))
			{
				mySelf.clearHorizontChart();
			}
			else if(actHorizont.col == "lat")
			{
				var s = $(rBars).size() - 1;
				mySelf.drawHorizontChart(rBars[s]);
			}
			else if(actHorizont.col == "sel")
			{
				mySelf.drawHorizontChart(rBars[mySelf.cursor.xPosAt]);
			}
		},
		
		/*
		 * Defines listener for choosing the type of real horizont.
		 */
		switchHorizontListener: function(horizontId, rBars)
		{
			var mySelf = this;
			var workArea = mySelf.workArea;
			var actHorizont = mySelf.actHorizont;
			var cl, temp, tempArr;
			
			$('div#' + workArea.detailsId + ' ' + 'td[id^="act_"]').on("click", function(e)
			{
				$('div#' + workArea.detailsId + ' ' + 'td[id^="act_"]').toggleClass("act-selected", false);
				
				if( "act_" + actHorizont.row + "_" + actHorizont.col != e.target.id)
					$(this).toggleClass("act-selected");
				
				cl = $('div#' + workArea.detailsId + ' ' + 'td[id^="act_"].act-selected');
				
				if($(cl).size() > 0)
				{
					temp = cl.attr("id");
					tempArr = temp.split("_");
					actHorizont.row = tempArr[1];
					actHorizont.col = tempArr[2];
				}
				else
				{
					actHorizont.row = "";
					actHorizont.col = "";
				}
				
				$('input[id^="' + horizontId + '"]').val(actHorizont.row + "_" + actHorizont.col);
				mySelf.refreshHorizontChart(rBars);
			});			
			
		},
		
		/*
		 * Sets the position and the width of the cursor div area.
		 */
		setCursorArea: function(xCoord)
		{
			var mySelf = this;
			var halfCursorPx = 6;
			var x = mySelf.chartStartAtPx + (mySelf.quarterChartFrom * mySelf.quarterDistPx) - halfCursorPx; 
			var areaWidth = (xCoord.quarterChartWidth < 0) ? 0 : 1.0 * xCoord.quarterChartWidth;
			var rWidth = mySelf.quarterDistPx * areaWidth + 2 * halfCursorPx + 3;
			
			$("#" + mySelf.workArea.cursorAreaId).css(
					{
						left: x,
						width: rWidth
					});
		},
		
		/*
		 * Sets the images of the cursor div.
		 */
		setCursorDraw : function()
		{
			var mySelf = this;
			var appendId = mySelf.workArea.cursorId;
			var cBar = mySelf.axisTopPadding + mySelf.chartHeight + mySelf.axisBottomPadding; 
			
			var childImg = $("<img>");
			childImg.attr("src", mySelf.cursorIcon.top);
			childImg.attr("height", mySelf.cursorTriangHeight).attr("width", "11");
			$("#" + appendId).append(childImg);

			childImg = $("<img>");
			childImg.attr("src", mySelf.cursorIcon.bar);
			childImg.attr("height", cBar).attr("width", "11");
			$("#" + appendId).append(childImg);

			childImg = $("<img>");
			childImg.attr("src", mySelf.cursorIcon.bottom);
			childImg.attr("height", mySelf.cursorTriangHeight).attr("width", "11");
			$("#" + appendId).append(childImg);
			
		},
		
		/*
		 * Sets the position of the cursor div inside the cursor area div.
		 */
		setCursorPosition: function(cursorId, xCoord)
		{
			var mySelf = this;
			var initPosition = $('input[id^="' + cursorId + '"]').val() * 1.0;
			
			if(xCoord.quarterChartWidth < 0)
			{
				mySelf.cursor.xPosAt = 0;
				$('input[id^="' + cursorId + '"]').val(mySelf.cursor.xPosAt);
			}
			else if( (initPosition == -1) || (xCoord.quarterChartWidth < initPosition) )
			{
				mySelf.cursor.xPosAt = 1.0 * xCoord.quarterChartWidth;
				$('input[id^="' + cursorId + '"]').val(mySelf.cursor.xPosAt);
			}
			else
			{
				mySelf.cursor.xPosAt = 1.0 * initPosition;
			}
			mySelf.cursor.xCoordPx = mySelf.cursor.xPosAt * mySelf.quarterDistPx;
				
			$("#" + mySelf.workArea.cursorId).css(
			{
				left: mySelf.cursor.xCoordPx,
			});
		},
		
		/*
		 * Defines listener for dragging the cursor.
		 */
		cursorDragListener : function(cursorId, rBars)
		{
			var mySelf = this;
			
		  $("#" + mySelf.workArea.cursorId).draggable({
		    containment: "#" + mySelf.workArea.cursorAreaId,
		    axis: "x",
		    grid: [mySelf.quarterDistPx, 0],
		    start: function(event, ui)
		    {
		    	$("#" + mySelf.workArea.cursorAreaId).toggleClass("pointer-cursor", true);
		    },
		    drag: function(event, ui)
		    {
		    	if(ui.position.left != mySelf.cursor.xCoordPx)
		    	{
		      	mySelf.cursor.xCoordPx = ui.position.left;
		      	mySelf.cursor.xPosAt = Math.floor(mySelf.cursor.xCoordPx / mySelf.quarterDistPx); 
		      	
		      	$('input[id^="' + cursorId + '"]').val(mySelf.cursor.xPosAt);
		      	mySelf.setCursorPartDetails(rBars);
		  			
		  			if(mySelf.actHorizont.col == "sel")
		  			{
		  				mySelf.drawHorizontChart(rBars[mySelf.cursor.xPosAt]);
		  			}
		      	
		    	}
		    },
		    stop: function(event, ui)
		    {
		    	$("#" + mySelf.workArea.cursorAreaId).toggleClass("pointer-cursor", false);
		    },
		  });
		},
		
		/*
		 * Draws image.
		 */
		drawImg: function(appendId, x1, y1, width, height, link)
		{
			var mySelf = this;
			
			var myImg = document.createElementNS(mySelf.svgNS, "image");
			myImg.setAttributeNS(null, "x", x1);
			myImg.setAttributeNS(null, "y", y1);
			myImg.setAttributeNS(null, "width", width);
			myImg.setAttributeNS(null, "height", height);
			myImg.setAttributeNS("http://www.w3.org/1999/xlink","href", link);
			myImg.setAttributeNS(null, "visibility", "visible");
			document.getElementById(appendId).appendChild(myImg);			
		},
		
		/*
		 * Draws text.
		 */
		drawText : function(appendId, x1, y1, value, style)
		{
			var mySelf = this;
			
			var myText = document.createElementNS(mySelf.svgNS, "text");
			myText.setAttributeNS(null, "class", style);
			myText.setAttributeNS(null, "x", x1);
			myText.setAttributeNS(null, "y", y1);
			myText.setAttributeNS(null, "text-anchor", "middle");
			myText.textContent = value;
			document.getElementById(appendId).appendChild(myText);
			
			return myText;
		},
		
		/*
		 * Draws line.
		 */
		drawLine : function(appendId, x1, y1, x2, y2)
		{
			var mySelf = this;
			var style = "coord";
			
			var myCoord = document.createElementNS(mySelf.svgNS, "line");
			myCoord.setAttributeNS(null, "class", style);
			myCoord.setAttributeNS(null, "x1", x1);
			myCoord.setAttributeNS(null, "y1", y1);
			myCoord.setAttributeNS(null, "x2", x2);
			myCoord.setAttributeNS(null, "y2", y2);
			document.getElementById(appendId).appendChild(myCoord);
		},

		/*
		 * Draws polyline.
		 */
		drawPolyLine: function(appendId, points, style)
		{
			var mySelf = this;
			
			var myPoly = document.createElementNS(mySelf.svgNS, "polyline");
			myPoly.setAttributeNS(null, "points", points);
			myPoly.setAttributeNS(null, "class", style);
			document.getElementById(appendId).appendChild(myPoly);
		},

		/*
		 * Draws polyline and inserts it before the parentId.
		 */
		insertBeforePolyLine: function(parentId, beforeId, points, style)
		{
			var mySelf = this;
			var beforeObj = document.getElementById(beforeId);
			
			var myPoly = document.createElementNS(mySelf.svgNS, "polyline");
			myPoly.setAttributeNS(null, "points", points);
			myPoly.setAttributeNS(null, "class", style);
			document.getElementById(parentId).insertBefore(myPoly, beforeObj);
		},
		
		/*
		 * Scales value.
		 */
		backTo : function(value)
		{
			var mySelf = this;
			var result = 0;

			if (mySelf.chartHeight !== undefined)
				result = 1.0 * value * mySelf.chartHeight;

			return result;
		},
		
		/*
		 * Round the visible value based on the unit type.
		 */
		roundValue : function(value)
		{
			var result = 0;
			var decimals = 0;
			result = Math.round(1.0 * value * Math.pow(10, decimals)) / Math.pow(10, decimals);
			return result.toFixed(decimals);			
		},
		
		/*
		 * Clears any type of Bar from the div.
		 */
		clearBar : function(elem, index)
		{
			var mySelf = this;
			mySelf.$elem = $(elem);
			
			if( mySelf.$elem.attr("adv_type") !== undefined)
			{
				var divId = mySelf.$elem.attr("id");
				
				$('div[id^="' + divId + '_title"]').remove();
				$('div[id^="' + divId + '_frame"]').remove();
				$('div[id^="' + divId + '_details"]').remove();
				
				$("#" + divId).removeAttr("class");
				$("#" + divId).removeAttr("style");
				$("#" + divId).removeAttr("adv_type");
			}
		}

	};

	/*
	 * Entry point of plug-in.
	 */
	$.fn.duringdaychart = function(method, options)
	{
		if(method === "clearBar")
		{
			$.ajaxSetup({
				cache : false
			});
			
			return this.each(function(index)
			{
				var main = Object.create(duringDayMethods);
				main[method](this, index);
			});
		}
		else if ((method === "duringDayBar"))
		{
			$.ajaxSetup({
				cache : false
			});

			if (options !== undefined)
				return this.each(function(index)
				{
					var main = Object.create(duringDayMethods);
					main[method](this, index, options);
				});

		} else
		{
			$.error("There is not this method: " + method);
		}
	};

})(jQuery, window, document);