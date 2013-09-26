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

	var warehouseL3Methods = {
		svgNS : "http://www.w3.org/2000/svg",
		padding : 10,
		type : "",
		workArea : {},
		drawSize : {},
		
		warehouseL3Bar : function(elem, index, options)
		{
			var mySelf = this;
			mySelf.elem = elem;
			mySelf.$elem = $(elem);
			mySelf.mode = "warehouse-l3";

			var settings = {};
			settings = {
				url : "",
				type : "POST",
				data : {},
				text : {
					zeroYTitle: 35,
				},
				coord : {
					zeroY : 175,
					scaleTick : 3,
					scaleCol  : 125,
					scaleTextX : -24,
					scaleTextY : 30
				},
				bar : {
					future :{
						zeroY : 57,
						height : 24
					},
					athub : {
						zeroY : [97, 137],
						height : 24
						}
				}
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
								if ((result.status !== undefined) &&
										(result.charts !== undefined))
								{
									mySelf.clearBar(elem, index);
									
									mySelf.workArea.holderId = mySelf.defineHolder(mySelf.$elem, index, mySelf.mode + "-holder");
									
									// For every chart...
									$(result.charts).each(function(chartIndex, chartElement)
									{
										mySelf.workArea.chartId = mySelf.workArea.holderId + "_ch_" + chartIndex;
										mySelf.createDIV(mySelf.workArea.holderId, mySelf.workArea.chartId, "chart");
										$("#" + mySelf.workArea.chartId).attr("storage_id", chartElement.direction.storageId);
										
										mySelf.workArea.titleId = mySelf.workArea.chartId + "_title";
										mySelf.createDIV(mySelf.workArea.chartId, mySelf.workArea.titleId, "title");
										mySelf.setTitle(chartElement.direction);
										
										// For every part of chart (all, standard, priority) 
										$(chartElement.bars).each(function(barIndex, barElement)
										{
											mySelf.workArea.frameId = mySelf.workArea.chartId + "_frame_" + barIndex;
											mySelf.createDIV(mySelf.workArea.chartId, mySelf.workArea.frameId, "frame");
											
											mySelf.workArea.outerDetailsId = mySelf.workArea.chartId + "_details_" + barIndex;
											mySelf.createDIV(mySelf.workArea.chartId, mySelf.workArea.outerDetailsId, "out-details");
											
											mySelf.workArea.svgId = mySelf.workArea.chartId + "_svg_" + barIndex;
											mySelf.createSVG(mySelf.workArea.frameId, mySelf.workArea.svgId);
											mySelf.drawSize = mySelf.setGraphSize();

											mySelf.drawText(barElement.unit, "title", 0, settings.text.zeroYTitle);
											var zeroX = mySelf.padding;
											
											mySelf.drawCoordinate(chartElement.coordinate, settings.coord, zeroX);
											mySelf.drawBarGraph(barIndex, barElement.future, settings.bar.future, zeroX);
											mySelf.drawAtHubGraph(barElement.athub, settings.bar.athub, zeroX);
											
											$.each(result.status, function(statusIndex, statusElement)
											{
												// If the first element is not the jumpStorageId..
												if(statusIndex != "jumpStorageId")
												{
													mySelf.workArea.detailsId = mySelf.workArea.outerDetailsId + "_" + statusIndex;
													mySelf.createDIV(mySelf.workArea.outerDetailsId, mySelf.workArea.detailsId, "details " + statusIndex);
													mySelf.createDetails(statusElement);
													mySelf.setDetails(barElement[statusIndex]);
												}
											});
											
										});

									});
									

									// If(there is jumpStorageId) AND (this id is in the JSON List)
 								  if( (result.status.jumpStorageId != 0) && ($('div[storage_id="' + result.status.jumpStorageId + '"] ').length) )
 								  {
 								  	$(elem).scrollTop(0);
 								  	var holderOffset = $(elem).offset().top;
 								    var chartOffset = $('div[storage_id="' + result.status.jumpStorageId + '"] ').offset().top;
 								    var chartScroll = chartOffset - holderOffset;
 								    $(elem).scrollTop(chartScroll);
 								  }
 								  else
 								  {
 								  	$(elem).scrollTop(0);
 								  }
 								  
 								 $("body").removeClass("loading");
									
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

		/*
		 * Create the table fo Details div
		 */
		createDetails : function(statusElement)
		{
			var mySelf = this;
			var workArea = mySelf.workArea;

			var table = $("<table>");
			var tbody = $("<tbody>");
			table.append(tbody);

			$(statusElement).each(function(index, element)
			{
				var tRow, tCell, tDiv;

				tRow = $("<tr>").attr("id", workArea.detailsId + "_" + element.info);
				tDiv = $("<div>").attr("class", element.info);
				tCell = $("<td>").attr("class", "legend").append(tDiv);
				tRow.append(tCell);
				tCell = $("<td>").html(element.message);
				tRow.append(tCell);
				tCell = $("<td>").attr("class", "details-right").html("");
				tRow.append(tCell);

				tbody.append(tRow);
			});
			
			$("#" + workArea.detailsId).append(table);

		},
		
		setTitle : function(direction)
		{
			var mySelf = this;
			var workArea = mySelf.workArea;  
			$("#" + workArea.titleId).html(direction.storageId + " (" + direction.warehouseName + "), " + direction.depotName);
		},
		
		/*
		 * Set the Graph size
		 */
		setGraphSize : function()
		{
			var mySelf = this;
			var workArea = mySelf.workArea;  
			var drawSize = {};
			
			var bodyWidth  = $(window).width() - 2 * 16 - (2 * mySelf.padding);
			var bodyHeight = $(window).height() - 2 * 16 - mySelf.padding - 150;
			
			if( ($("#" + workArea.holderId).width() < bodyWidth) && ($("#" + workArea.holderId).height() < bodyHeight) )
			{
				drawSize.holderWidth = bodyWidth;
				drawSize.holderHeight = bodyHeight;
				$("#" + workArea.holderId).width(drawSize.holderWidth);
				$("#" + workArea.holderId).height(drawSize.holderHeight);
			}
			else
			{
				drawSize.holderWidth  = $("#" + workArea.holderId).width();
				drawSize.holderHeight = $("#" + workArea.holderId).height();
			}
			
			drawSize.widthFrame = drawSize.holderWidth - $("#" + workArea.outerDetailsId).width() - 2 * 16 - 10;
			drawSize.heightFrame = $("#" + mySelf.workArea.frameId).height();
			$("#" + workArea.frameId).width(drawSize.widthFrame);
			$("#" + workArea.frameId).height(drawSize.heightFrame);
			
			drawSize.widthGraph = drawSize.widthFrame - (2 * mySelf.padding);
			drawSize.heightGraph = drawSize.heightFrame;
			
			mySelf.setSVGViewBox(workArea.svgId, drawSize.widthFrame, drawSize.heightFrame);
			
			return drawSize;
		},
		
		/*
		 * Set the SVG ViewBox
		 */
		setSVGViewBox : function(svgId, w, h)
		{
			$("#" + svgId).width(w);
			$("#" + svgId).height(h);

			var viewBox = "0 0 " + w + " " + h;
			document.getElementById(svgId).setAttribute("viewBox", viewBox);
		},
		
		drawText : function(value, style, zeroX, zeroY)
		{
			var mySelf = this;
			var workArea = mySelf.workArea;
			
			var myText = document.createElementNS(mySelf.svgNS, "text");
			myText.setAttributeNS(null, "class", style);
			myText.setAttributeNS(null, "x", zeroX);
			myText.setAttributeNS(null, "y", zeroY);
			myText.textContent = value;
			document.getElementById(workArea.svgId).appendChild(myText);
		},

		/*
		 * Draw the Bar chart
		 */
		drawBarGraph : function(barId, barItem, barConst, zeroX)
		{
			var mySelf = this;
			var workArea = mySelf.workArea;
			var offSetX = 0;
			var zeroY = barConst.zeroY;
			var height = barConst.height;

			// 1.) For a concrete bar: Draw different state of items
			// Predicted status is handled different case than other ones...
			var itemSize = $(barItem).size();
			$(barItem).each(
					function(index, element)
					{
						// Draw every status, except Predicted
						if(index < itemSize)
						{
							var myRect = document.createElementNS(mySelf.svgNS, "rect");
							myRect.setAttributeNS(null, "id", "bar_" + barId + "_" + index);
							myRect.setAttributeNS(null, "adv_amount", element.value);
							myRect.setAttributeNS(null, "class", "default " + element.info);
							myRect.setAttributeNS(null, "transform", "translate(" + zeroX + ",0)");
							
							myRect.setAttributeNS(null, "x", offSetX);
							offSetX += mySelf.backTo(element.normalValue);
							
							myRect.setAttributeNS(null, "y", zeroY);
							myRect.setAttributeNS(null, "width", mySelf.backTo(element.normalValue));
							myRect.setAttributeNS(null, "height", height);
							document.getElementById(workArea.svgId).appendChild(myRect);
						}
					});
		
		},
		
		
		drawAtHubGraph : function(barColumn, barConst, zeroX)
		{
			var mySelf = this;
			
			$(barColumn).each(function(index, element)
			{
				var myColumn = document.createElementNS(mySelf.svgNS, "rect");
				myColumn.setAttributeNS(null, "class", "at_hub");
				myColumn.setAttributeNS(null, "x", zeroX);
				myColumn.setAttributeNS(null, "y", barConst.zeroY[index]);
				myColumn.setAttributeNS(null, "width", mySelf.backTo(element.normalValue));
				myColumn.setAttributeNS(null, "height", barConst.height);
				document.getElementById(mySelf.workArea.svgId).appendChild(myColumn);
			});

		},

		
		/*
		 * Draw the Horizontal Coordinate Axis
		 */
		drawCoordinate : function(coordResult, coordConst, zeroX)
		{
			var mySelf = this;
			var workArea = mySelf.workArea;
			var drawSize = mySelf.drawSize;
			
			var zeroY = coordConst.zeroY;
			var noOfTick = 1.0 * coordResult.noOfTotalTick;

			
				// 1.) draw horizontal line of coordinate
				var myCoord = document.createElementNS(mySelf.svgNS, "line");
				myCoord.setAttributeNS(null, "class", "coord");
				myCoord.setAttributeNS(null, "x1", zeroX);
				myCoord.setAttributeNS(null, "y1", zeroY);
				myCoord.setAttributeNS(null, "x2", drawSize.widthFrame);
				myCoord.setAttributeNS(null, "y2", zeroY);
				document.getElementById(workArea.svgId).appendChild(myCoord);

				// 2.) draw the unit text of it
				// The nominal cap. is only at the right side 
				for(var i = 0; i < 2; i++)
				{
					var myText = document.createElementNS(mySelf.svgNS, "text");
					myText.setAttributeNS(null, "class", "write");
					myText.setAttributeNS(null, "x", (i == 0) ? zeroX : (drawSize.widthGraph + coordConst.scaleTextX) );
					myText.setAttributeNS(null, "y", zeroY + coordConst.scaleTextY);
					
					var myTextNode = document.createTextNode("\u00D7 " + coordResult.scaleInfo);
					myText.appendChild(myTextNode);
					document.getElementById(workArea.svgId).appendChild(myText);
					// Set the correct position ,but it works after rendering...
					var width = myText.getBBox().width;
					myText.setAttributeNS(null, "x", (i == 0) ? zeroX : (drawSize.widthGraph - width) );
				}
				
				// 3.) draw the scaleTick & value of it
				for ( var i = 0; i < (noOfTick + 1); i++)
				{
					var scaleX = mySelf.backTo(i * coordResult.normalScale) + zeroX;
					var myCoord = document.createElementNS(mySelf.svgNS, "line");
					myCoord.setAttributeNS(null, "class", "coord");
					myCoord.setAttributeNS(null, "x1", scaleX);
					myCoord.setAttributeNS(null, "y1", zeroY - coordConst.scaleCol);
					myCoord.setAttributeNS(null, "x2", scaleX);
					myCoord.setAttributeNS(null, "y2", zeroY + coordConst.scaleTick);
					document.getElementById(workArea.svgId).appendChild(myCoord);
					
					// Draw the value
					var myText = document.createElementNS(mySelf.svgNS, "text");
					myText.setAttributeNS(null, "class", "write");
					myText.setAttributeNS(null, "x", scaleX - 2);
					myText.setAttributeNS(null, "y", zeroY + 15);
					myText.textContent = 1.0 * i * coordResult.tickUnit;
					document.getElementById(workArea.svgId).appendChild(myText);
					// Set the correct position ,but it works after rendering...
					var width = myText.getBBox().width;
					myText.setAttributeNS(null, "x", scaleX - (width / 2.0));
				}
			
		},
		
		
		/*
		 * Set the details for Legend
		 */
		setDetails : function(barParts)
		{
			var mySelf = this;
			var workArea = mySelf.workArea;
			
			$(barParts).each(function(index, element)
			{
				$("#" + workArea.detailsId + "_" + element.info + " td.details-right").html(mySelf.roundValue(element.value));
			});
		},

		/*
		 * Scale with value
		 */
		backTo : function(value)
		{
			var mySelf = this;
			var result = 0;

			if (mySelf.drawSize.widthGraph !== undefined)
				result = 1.0 * value * mySelf.drawSize.widthGraph;

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
				
				$('div[id^="' + divId + '_ch"]').remove();
				
				$("#" + divId).removeAttr("class");
				$("#" + divId).removeAttr("style");
				$("#" + divId).removeAttr("adv_type");
			}
		},
		
		/*
		 * Get the actual displayed chart and its storage id
		 */
		getActualStorageId : function(elem)
		{
			var holderId = $(elem).attr("id");
		  var holderOffset = $("#" + holderId).offset().top;
		  var storageId = 0;
		  
		  // If there is any chart div with storageId..
		  if($("div[storage_id]").length)
		  {
			  // If the scroll is at the top..
			  if( 0 <= ($("div[storage_id]").first().offset().top - holderOffset) )
			  {
				  storageId = $("div[storage_id]").first().attr("storage_id");
			  }
			  //.. else if the scroll is at the bottom..
			  else if( ($("div[storage_id]").last().offset().top - holderOffset) < 0)
			  {
				  storageId = $("div[storage_id]").last().attr("storage_id");
			  }
			  //.. the main case: find the last negative and first positive offset 
			  else
			  {
			  	var found = false;
			  	var lastNegative = $("div[storage_id]").first();
				  // for every chart except the fiirst
			  	$("div[storage_id]:not(:first)").each(function(index, element)
					{
				  	if(found == false)
				  	{
					  	var chartOffset = $(element).offset().top - holderOffset;
					  	
					  	if(0 <= chartOffset)
					  	{
					  		if( (-1.0 * (lastNegative.offset().top - holderOffset)) < chartOffset )
					  			storageId = lastNegative.attr("storage_id");
					  		else
					  			storageId = $(element).attr("storage_id");
					  		
					  		found = true;
					  	}
					  	else
					  	{
					  		lastNegative = $(element);
					  	}
				  	}
					});
			  }
		  }
		  
		  return storageId;
		}

	};

	/*
	 * Entry point of plug-in
	 */
	$.fn.warehousel3chart = function(method, options)
	{
		if(method === "clearBar")
		{
			$.ajaxSetup({
				cache : false
			});
			
			return this.each(function(index)
			{
				var main = Object.create(warehouseL3Methods);
				main[method](this, index);
			});
		}
		else if (method === "getActualStorageId")
		{
			$.ajaxSetup({
				cache : false
			});

			var main = Object.create(warehouseL3Methods);
			return main[method](this);
			
		}
		else if (method === "warehouseL3Bar")
		{
			$.ajaxSetup({
				cache : false
			});

			if (options !== undefined)
				return this.each(function(index)
				{
					var main = Object.create(warehouseL3Methods);
					main[method](this, index, options);
				});

		} else
		{
			$.error("There is not this method: " + method);
		}
	};

})(jQuery, window, document);