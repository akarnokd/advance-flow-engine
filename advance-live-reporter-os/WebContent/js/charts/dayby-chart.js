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

	var dayByMethods = {
		svgNS : "http://www.w3.org/2000/svg",
		type : "",
		unit : "",
		workArea : {},
		// Params only the chart in the svg
		chartSize : {
			leftMargin : 140,
			rightMargin : 48,
			topMargin : 85,
			bottomMargin : 27,
			sidePadding : 8,
			middlePadding : 16,
			//Modify based on window width/height
			chartWidth : 0,
			chartHeight : 0,
			initZeroX : 0,
			initZeroY : 0,
			barWidth : {
				  hub : 250,
				  depot : 125
				},
		},
		
		daybyBar : function(elem, index, options)
		{
			var mySelf = this;
			mySelf.elem = elem;
			mySelf.$elem = $(elem);
			mySelf.mode = "daybyday";

			var settings = {};
			settings = {
				url : "",
				type : "POST",
				data : {},
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
								if ((result.coordinate !== undefined) &&
										(result.direction !== undefined) &&
										(result.status !== undefined) &&
										(result.bars !== undefined))
								{
									mySelf.clearBar(elem, index);
									
									mySelf.type = result.direction.type;
									mySelf.unit = result.direction.unit;
									mySelf.workArea.holderId = mySelf.defineHolder(mySelf.$elem, index, mySelf.mode + "-holder");
									
									mySelf.workArea.frameId = mySelf.workArea.holderId + "_frame";
									mySelf.createDIV(mySelf.workArea.holderId, mySelf.workArea.frameId, "frame");

									mySelf.workArea.outerDetailsId = mySelf.workArea.holderId + "_out-details";
									mySelf.createDIV(mySelf.workArea.holderId, mySelf.workArea.outerDetailsId, "out-details");
									
									mySelf.workArea.svgId = mySelf.workArea.holderId + "_svg";
									mySelf.createSVG(mySelf.workArea.frameId, mySelf.workArea.svgId);
									mySelf.setGraphSize();
									
									mySelf.workArea.legendId = mySelf.workArea.holderId + "_legend";
									mySelf.createDIV(mySelf.workArea.outerDetailsId, mySelf.workArea.legendId, "legend");
									mySelf.createLegend(result.status);
									
									mySelf.drawText(0, 30, result.direction.name, "title");

									var zeroX = mySelf.chartSize.initZeroX;
									var zeroY = mySelf.chartSize.initZeroY;

									$(result.bars).each(function(barIndex, barElement)
									{
										mySelf.workArea.detailsInfoId = mySelf.workArea.holderId + "_details_" + barIndex + "_info";
										mySelf.createDIV(mySelf.workArea.outerDetailsId, mySelf.workArea.detailsInfoId, "details-info");
										mySelf.createDetailsInfo(barElement, zeroX);
										
										$(result.direction.orient).each(function(orientIndex, orientElement)
										{
											mySelf.drawBarGraph(orientElement, barIndex, barElement[orientElement], zeroX, zeroY);
											
											mySelf.workArea.detailsId = mySelf.workArea.holderId + "_details_" + barIndex + "_" + orientElement;
											mySelf.createDIV(mySelf.workArea.outerDetailsId, mySelf.workArea.detailsId, "details");
											mySelf.createDetails(barElement[orientElement], zeroX);
											
											zeroX += mySelf.chartSize.barWidth[mySelf.type];											
										});
										zeroX += mySelf.chartSize.middlePadding;
									});
									
									mySelf.drawCoordinate(result.coordinate);
									
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
		
		createLegend : function(status)
		{
			var mySelf = this;
			var workArea = mySelf.workArea;

			var table = $("<table>");
			var tbody = $("<tbody>");
			table.append(tbody);

			$(status).each(function(index, element)
			{
				var tRow, tCell, tDiv;
				tRow = $("<tr>").attr("class", "font-legend");

				tDiv = $("<div>").attr("class", "snip " + element.info);
				
				if(mySelf.type == "depot")
				{	
					var depotDiv = $("<div>").attr("class", "origin-snip origin-" + element.info);
					tDiv.append(depotDiv);
				}
				
				tCell = $("<td>").append(tDiv);
				tRow.append(tCell);
				tCell = $("<td>").html(element.message);
				tRow.append(tCell);
				
				tbody.append(tRow);
			});
			$("#" + workArea.legendId).append(table);
			
		},
		
		createDetails : function(units, zeroX)
		{
			
			var mySelf = this;
			var workArea = mySelf.workArea;
			var chartSize = mySelf.chartSize;

			$("#" + workArea.detailsId).css("left", zeroX);
			$("#" + workArea.detailsId).css("width", chartSize.barWidth[mySelf.type]);
			
			var table = $("<table>");
			var tbody = $("<tbody>");
			table.append(tbody);
			
			$($(units).toArray().reverse()).each(function(index, element)
			{
				var tRow, tCell;
				tRow = $("<tr>").attr("class", "font-legend");
				tCell = $("<td>").html(mySelf.roundValue(element.value));
				tRow.append(tCell);
				
				tbody.append(tRow);
			});
			$("#" + workArea.detailsId).append(table);
			
		},
		
		createDetailsInfo : function(bar, zeroX)
		{
			var mySelf = this;
			var workArea = mySelf.workArea;
			var chartSize = mySelf.chartSize;

			$("#" + workArea.detailsInfoId).css("left", zeroX);
			$("#" + workArea.detailsInfoId).css("width", chartSize.barWidth.hub);

			
			var dayDiv = $("<div>").attr("class", "day font-legend").html(bar.day);
			$("#" + workArea.detailsInfoId).append(dayDiv);
			
			if(mySelf.type == "depot")
			{
				var infoDiv;
				infoDiv = $("<div>").attr("class", "info font-info").html(bar.origin_text);
				$("#" + workArea.detailsInfoId).append(infoDiv);
				
				infoDiv = $("<div>").attr("class", "info font-info").html(bar.destin_text);
				$("#" + workArea.detailsInfoId).append(infoDiv);
			}
			
		},

		/*
		 * Set the Graph size
		 */
		setGraphSize : function()
		{
			var mySelf = this;
			var workArea = mySelf.workArea;  
			var bodyWidth = $(window).width() - 2 * 16;
			
			if(1010 < bodyWidth) 
			{
				$("#" + workArea.holderId).width(bodyWidth);
			}	
			var frameWidth = $("#" + workArea.frameId).width();
			var frameHeight = $("#" + workArea.frameId).height();
				
			mySelf.chartSize.chartWidth = frameWidth - mySelf.chartSize.leftMargin - mySelf.chartSize.rightMargin;
			mySelf.chartSize.chartHeight = frameHeight - mySelf.chartSize.topMargin - mySelf.chartSize.bottomMargin;
				
			mySelf.chartSize.initZeroX = mySelf.chartSize.leftMargin + mySelf.chartSize.sidePadding;
			mySelf.chartSize.initZeroY = frameHeight - mySelf.chartSize.bottomMargin;
				
			var usedBars = mySelf.chartSize.chartWidth -  2 * mySelf.chartSize.sidePadding - 2 * mySelf.chartSize.middlePadding;
			mySelf.chartSize.barWidth.hub = (1.0 * usedBars) / 3.0;
			mySelf.chartSize.barWidth.depot = mySelf.chartSize.barWidth.hub / 2.0;
				
			mySelf.setSVGViewBox(workArea.svgId, frameWidth, frameHeight);
			
		},
		
		/*
		 * Set the SVG ViewBox
		 */
		setSVGViewBox : function(svgId, w, h)
		{
			$("#" + svgId).width(w);
			$("#" + svgId).height(h);

			// http://stackoverflow.com/questions/10390346/why-is-jquery-auto-lower-casing-attribute-values
			var viewBox = "0 0 " + w + " " + h;
			document.getElementById(svgId).setAttribute("viewBox", viewBox);
		},
		
		drawText : function(x1, y1, value, style)
		{
			var mySelf = this;
			var workArea = mySelf.workArea;
			
			var myText = document.createElementNS(mySelf.svgNS, "text");
			myText.setAttributeNS(null, "class", style);
			myText.setAttributeNS(null, "x", x1);
			myText.setAttributeNS(null, "y", y1);
			myText.textContent = value;
			document.getElementById(workArea.svgId).appendChild(myText);
			
			return myText;
		},
		
		drawLine : function(x1, y1, x2, y2)
		{
			var mySelf = this;
			var workArea = mySelf.workArea;
			var style = "coord";
			
			var myCoord = document.createElementNS(mySelf.svgNS, "line");
			myCoord.setAttributeNS(null, "class", style);
			myCoord.setAttributeNS(null, "x1", x1);
			myCoord.setAttributeNS(null, "y1", y1);
			myCoord.setAttributeNS(null, "x2", x2);
			myCoord.setAttributeNS(null, "y2", y2);
			document.getElementById(workArea.svgId).appendChild(myCoord);
		},

		/*
		 * Draw the column bar chart
		 */
		drawBarGraph : function(orient, barId, barUnits, zeroX, zeroY)
		{
			var mySelf = this;
			var workArea = mySelf.workArea;
			var chartSize = mySelf.chartSize;
			var offSetY = zeroY;

			// For a concrete bar: Draw different state of items
			var unitSize = $(barUnits).size();
			$(barUnits).each(
					function(index, element)
					{
						if(index != unitSize - 1)
						{
							var myRect = document.createElementNS(mySelf.svgNS, "rect");
							myRect.setAttributeNS(null, "id", "bar_" + orient + "_" + barId + "_" + index);
							myRect.setAttributeNS(null, "class", "default " + orient + "-" + element.info);
							
							myRect.setAttributeNS(null, "transform", "translate(" + zeroX + ",0)");
							myRect.setAttributeNS(null, "x", 0);
							offSetY -= mySelf.backTo(element.normalValue);
							myRect.setAttributeNS(null, "y", offSetY);
							myRect.setAttributeNS(null, "width", chartSize.barWidth[mySelf.type]);
							myRect.setAttributeNS(null, "height", mySelf.backTo(element.normalValue));
							document.getElementById(workArea.svgId).appendChild(myRect);
						}
					});
		},

		/*
		 * Draw Coordinates and scaleTicks
		 */
		drawCoordinate : function(coordResult)
		{
			var mySelf = this;
			var chartSize = mySelf.chartSize;
			var noOfTick = 1.0 * coordResult.noOfTick;
			var textObj, textWidth, textHeight;

			// Left side coordinate
			mySelf.drawLine(chartSize.leftMargin,
					chartSize.topMargin - 20,
					chartSize.leftMargin,
					chartSize.initZeroY);

			// Left-top scale
			textObj = mySelf.drawText(chartSize.leftMargin, chartSize.topMargin - 20, "\u00D7" + coordResult.scale, "write");
			textWidth = textObj.getBBox().width;
			textObj.setAttributeNS(null, "x", chartSize.leftMargin - 8 - textWidth);
			
			// Left-bottom scale
			textObj = mySelf.drawText(chartSize.leftMargin, chartSize.initZeroY + 20, "\u00D7" + coordResult.scale, "write");
			textWidth = textObj.getBBox().width;
			textObj.setAttributeNS(null, "x", chartSize.leftMargin - 8 - textWidth);
			
			// Right side coordinate
			mySelf.drawLine(chartSize.leftMargin + chartSize.chartWidth,
					chartSize.topMargin - 20,
					chartSize.leftMargin + chartSize.chartWidth,
					chartSize.initZeroY);
			
			// Right-top scale
			textObj = mySelf.drawText(chartSize.leftMargin + chartSize.chartWidth, chartSize.topMargin - 20, "\u00D7" + coordResult.scale, "write");
			textWidth = textObj.getBBox().width;
			textObj.setAttributeNS(null, "x", chartSize.leftMargin + chartSize.chartWidth + 2);

			// Right-bottom scale
			textObj = mySelf.drawText(chartSize.leftMargin + chartSize.chartWidth, chartSize.initZeroY + 20, "\u00D7" + coordResult.scale, "write");
			textWidth = textObj.getBBox().width;
			textObj.setAttributeNS(null, "x", chartSize.leftMargin + chartSize.chartWidth + 2);
			
			// Draw the tick & value of it
			var zeroY = chartSize.initZeroY;
			for ( var i = 0; i < (noOfTick + 1); i++)
			{
				var scaleY = zeroY - mySelf.backTo(i * coordResult.normalScale);
				
				mySelf.drawLine(chartSize.leftMargin - 4,
						scaleY,
						chartSize.leftMargin + chartSize.chartWidth + 3,
						scaleY);
				
				textObj = mySelf.drawText(chartSize.leftMargin, scaleY, i * coordResult.tickValue, "write");
				textWidth = textObj.getBBox().width;
				textHeight = textObj.getBBox().height / 2.0;
				textObj.setAttributeNS(null, "x", chartSize.leftMargin - 8 - textWidth);
				textObj.setAttributeNS(null, "y", scaleY + textHeight - 5);
				
				textObj = mySelf.drawText(chartSize.leftMargin + chartSize.chartWidth, scaleY, i * coordResult.tickValue, "write");
				textWidth = textObj.getBBox().width;
				textHeight = textObj.getBBox().height / 2.0;
				textObj.setAttributeNS(null, "x", chartSize.leftMargin + chartSize.chartWidth + 26 - textWidth);
				textObj.setAttributeNS(null, "y", scaleY + textHeight - 5);
			}
			
			
		},

		/*
		 * Scale with value for both Single and MultiBar
		 */
		backTo : function(value)
		{
			var mySelf = this;
			var result = 0;

			if (mySelf.chartSize.chartHeight !== undefined)
				result = 1.0 * value * mySelf.chartSize.chartHeight;

			return result;
		},
		
		/*
		 * Round the visible value based on the unit type.
		 */
		roundValue : function(value)
		{
			var result = 0;
			
			result = Math.round(1.0 * value * Math.pow(10, 2)) / Math.pow(10, 2);
			return result.toFixed(2);			
		},
		
		/*
		 * Clear any type of Bar from the div
		 */
		clearBar : function(elem, index)
		{
			var mySelf = this;
			mySelf.$elem = $(elem);
			
			if( mySelf.$elem.attr("adv_type") !== undefined)
			{
				var divId = mySelf.$elem.attr("id");
				
				$('div[id^="' + divId + '_frame"]').remove();
				$('div[id^="' + divId + '_out-details"]').remove();
				
				$("#" + divId).removeAttr("class");
				$("#" + divId).removeAttr("style");
				$("#" + divId).removeAttr("adv_type");
			}
		}

	};

	/*
	 * Entry point of plug-in
	 */
	$.fn.daybychart = function(method, options)
	{
		if(method === "clearBar")
		{
			$.ajaxSetup({
				cache : false
			});
			
			return this.each(function(index)
			{
				var main = Object.create(dayByMethods);
				main[method](this, index);
			});
		}
		else if ((method === "daybyBar"))
		{
			$.ajaxSetup({
				cache : false
			});

			if (options !== undefined)
				return this.each(function(index)
				{
					var main = Object.create(dayByMethods);
					main[method](this, index, options);
				});

		} else
		{
			$.error("There is not this method: " + method);
		}
	};

})(jQuery, window, document);