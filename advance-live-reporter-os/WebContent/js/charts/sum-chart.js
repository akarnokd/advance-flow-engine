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

	var chartMethods = {
		svgNS : "http://www.w3.org/2000/svg",
		padding : 10,
		type : "",
		unit : "",
		mode : "",
		chartView : "",
		workArea : {},
		drawSize : {},
		
		summaryBar : function(elem, index, options)
		{
			var mySelf = this;
			mySelf.elem = elem;
			mySelf.$elem = $(elem);
			mySelf.mode = "summary";

			var settings = {};
			settings = {
				url : "",
				type : "POST",
				data : {},
				text : {
					zeroYTitle: 30,
				},
				coord : {
					hub: {
						zeroYTop : 69,
						zeroYBottom : 124,
						scaleTick : 3,
						scaleTextX : -24,
						scaleTextY : 15,
					},
					depot : {
						zeroYTop : 75,
						zeroYMiddle : 133,
						zeroYBottom : 190,
						scaleTick : 3,
						scaleTextX : -24,
						scaleTextY : 20,
					}
				},
				bar : {
					hub :{
						zeroY : {
							single: 80,
						},
						height : 33
					},
					depot : {
						zeroY : {
							origin: 87,
							destin: 145
						},
						height : 33
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
								if ((result.coordinate !== undefined) &&
										(result.direction !== undefined) &&
										(result.status !== undefined) &&
										(result.bars !== undefined))
								{
									mySelf.clearBar(elem, index);
									
									mySelf.chartView = result.direction.chartView;
									mySelf.type = result.direction.type;
									mySelf.unit = result.direction.unit;
									mySelf.workArea.holderId = mySelf.defineHolder(mySelf.$elem, index, mySelf.mode + "-holder");
									
									$(result.bars).each(function(barIndex, barElement)
									{
										mySelf.workArea.frameId = mySelf.workArea.holderId + "_frame_" + barIndex;
										mySelf.createDIV(mySelf.workArea.holderId, mySelf.workArea.frameId, result.direction.type + "-frame");
										
										mySelf.workArea.outerDetailsId = mySelf.workArea.holderId + "_details_" + barIndex;
										mySelf.createDIV(mySelf.workArea.holderId, mySelf.workArea.outerDetailsId, result.direction.type + "-out-details");

										mySelf.workArea.svgId = mySelf.workArea.holderId + "_svg_" + barIndex;
										mySelf.createSVG(mySelf.workArea.frameId, mySelf.workArea.svgId);
										mySelf.drawSize = mySelf.setGraphSize();
										if(mySelf.drawSize.wideScreen)
										{
											$("#" + mySelf.workArea.holderId).width(mySelf.drawSize.bodyWidth);
											$("#" + mySelf.workArea.frameId).width(mySelf.drawSize.widthFrame);
										}
										
										mySelf.setSVGViewBox(mySelf.workArea.svgId, mySelf.drawSize.widthFrame, mySelf.drawSize.heightFrame);
										
										var zeroX = mySelf.backTo(result.direction.normalZeroX) + mySelf.padding;

										if(result.direction.type == "hub")
										{
											var textTitle = barElement.name + ": " + barElement.unit;
											mySelf.drawText(textTitle, "title", 0, settings.text.zeroYTitle);
											
											var leftValue = mySelf.backTo(barElement.single[0].normalValue);

											mySelf.drawCoordinate(barIndex, result.coordinate, settings.coord[result.direction.type], zeroX, "top");
											mySelf.drawCoordinate(barIndex, result.coordinate, settings.coord[result.direction.type], zeroX - leftValue, "bottom");
										}
										else if(result.direction.type == "depot")
										{
											var textTitle = barElement.name + ": " + barElement.unit;
											mySelf.drawText(textTitle, "title", 0, settings.text.zeroYTitle);

											mySelf.drawCoordinate(barIndex, result.coordinate, settings.coord[result.direction.type], zeroX, "top");
											mySelf.drawCoordinate(barIndex, result.coordinate, settings.coord[result.direction.type], zeroX, "middle");
											mySelf.drawCoordinate(barIndex, result.coordinate, settings.coord[result.direction.type], zeroX, "bottom");
										}
										
										$(result.direction.orient).each(function(orientIndex, orientElement)
										{
											var height = settings.bar[result.direction.type]["height"];
											var zeroY = settings.bar[result.direction.type]["zeroY"][orientElement];
											mySelf.drawBarGraph(orientIndex, barElement[orientElement], height, zeroX, zeroY);

											mySelf.workArea.detailsId = mySelf.workArea.holderId + "_details_" + barIndex + "_" + orientElement;
											mySelf.createDIV(mySelf.workArea.outerDetailsId, mySelf.workArea.detailsId, result.direction.type + "-details " + orientElement);
											mySelf.createDetails(result.status);
											mySelf.setDetails(barElement[orientElement]);
										});
									});
									
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
		createDetails : function(status)
		{
			var mySelf = this;
			var workArea = mySelf.workArea;

			var table = $("<table>");
			var tbody = $("<tbody>");
			table.append(tbody);

			$(status).each(function(index, element)
			{
				var tRow, tCell, tDiv;
				tRow = $("<tr>");

				tDiv = $("<div>").attr("class", element.info);
				tCell = $("<td>").append(tDiv);
				tRow.append(tCell);
				tCell = $("<td>").html(element.message);
				tRow.append(tCell);
				tCell = $("<td>").attr("id", workArea.detailsId + "_" + element.info);
				tCell.attr("class", "details-right");
				tCell.html("");
				tRow.append(tCell);

				tbody.append(tRow);
			});
			$("#" + workArea.detailsId).append(table);

		},
		
		/*
		 * Set the Graph size
		 */
		setGraphSize : function()
		{
			var mySelf = this;
			var workArea = mySelf.workArea;  
			var drawSize = {};
			
			drawSize.bodyWidth = $("body").width();
			if(1000 < drawSize.bodyWidth)
			{
				drawSize.wideScreen = true;
				drawSize.widthFrame = drawSize.bodyWidth - $("#" + workArea.outerDetailsId).width() - mySelf.padding - 4;
			}
			else
			{	
				drawSize.wideScreen = false;
				drawSize.widthFrame = $("#" + workArea.frameId).width();
			}
			
			drawSize.heightFrame = $("#" + mySelf.workArea.frameId).height();
			
			drawSize.widthGraph = drawSize.widthFrame - (2 * mySelf.padding);
			drawSize.heightGraph = drawSize.heightFrame;
			
			return drawSize;
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
		 * Draw the Bar chart based on the chartView
		 */
		drawBarGraph : function(barId, barItem, height, zeroX, zeroY)
		{
			var mySelf = this;
			var workArea = mySelf.workArea;
			var offSetX = 0;
			var predStartX = 0;
			
			// Backgorund of Left hub status 
			var zeroRect = document.createElementNS(mySelf.svgNS, "rect");
			zeroRect.setAttributeNS(null, "class", "zeroArea");
			zeroRect.setAttributeNS(null, "x", 0);
			zeroRect.setAttributeNS(null, "y", zeroY - 7);
			zeroRect.setAttributeNS(null, "width", (mySelf.chartView == "hub_user") ? zeroX : zeroX + mySelf.backTo(barItem[0].normalValue));
			zeroRect.setAttributeNS(null, "height", height + 14);
			document.getElementById(mySelf.workArea.svgId).appendChild(zeroRect);

			// 2.) For a concrete bar: Draw different state of items
			// Predicted status is handled different case than other ones...
			var itemSize = $(barItem).size();
			$(barItem).each(
					function(index, element)
					{
						// Draw every status, except Predicted
						if(index < itemSize - 1)
						{
							var myRect = document.createElementNS(mySelf.svgNS, "rect");
							myRect.setAttributeNS(null, "id", "bar_" + barId + "_" + index);
							myRect.setAttributeNS(null, "adv_amount", mySelf.roundValue(element.value));
							myRect.setAttributeNS(null, "class", "default " + element.info);
							myRect.setAttributeNS(null, "transform", "translate(" + zeroX + ",0)");
							if(mySelf.chartView == "hub_user")
							{
								if (index == 0)
								{
									myRect.setAttributeNS(null, "x", mySelf.backTo(-1.0 * element.normalValue));
								} else
								{
									myRect.setAttributeNS(null, "x", offSetX);
									offSetX += mySelf.backTo(element.normalValue);
									
									if(index == 2)
										predStartX = offSetX;
								}
							} else if(mySelf.chartView == "depot_user")
							{
								myRect.setAttributeNS(null, "x", (index == 0) ? 0 : offSetX);
								offSetX += mySelf.backTo(element.normalValue);
								
								if(index == 2)
									predStartX = offSetX;								
							}
							
							myRect.setAttributeNS(null, "y", zeroY);
							myRect.setAttributeNS(null, "width", mySelf.backTo(element.normalValue));
							myRect.setAttributeNS(null, "height", height);
							document.getElementById(workArea.svgId).appendChild(myRect);
						}
						// Draw Predicted status
						else
						{
							// Count the difference
							var diffPredict = mySelf.backTo(element.normalValue) - (offSetX - predStartX);
							if(0 < diffPredict)
							{
								var myRect = document.createElementNS(mySelf.svgNS, "rect");
								myRect.setAttributeNS(null, "id", "bar_" + barId + "_" + index);
								myRect.setAttributeNS(null, "adv_amount", mySelf.roundValue(element.value));
								myRect.setAttributeNS(null, "class", "default " + element.info);
								myRect.setAttributeNS(null, "transform", "translate(" + zeroX + ",0)");
								
								myRect.setAttributeNS(null, "x", offSetX);
								offSetX += diffPredict;

								myRect.setAttributeNS(null, "y", zeroY);
								myRect.setAttributeNS(null, "width", diffPredict);
								myRect.setAttributeNS(null, "height", height);
								document.getElementById(workArea.svgId).appendChild(myRect);
							}
							else
							{
								offSetX = predStartX + mySelf.backTo(element.normalValue);
							}
						}
					});
			
			// 3.) Draw path of Predicted status (top & bottom) for dashed contour			
			var myPath = document.createElementNS(mySelf.svgNS, "path");
			myPath.setAttributeNS(null, "transform", "translate(" + zeroX + ",0)");
			var points =  "M" + predStartX + ", "  + (zeroY - 0.5) + " H" + offSetX;
			points    += " M" + offSetX + ", "  + (zeroY + height + 0.5) + " H" + predStartX;
			myPath.setAttributeNS(null, "d", points);
			myPath.setAttributeNS(null, "class", "contourDash");
			document.getElementById(mySelf.workArea.svgId).appendChild(myPath);			

			// 4.) Draw Single line at the end of Predicted status
			var mySingle = document.createElementNS(mySelf.svgNS, "line");
			mySingle.setAttributeNS(null, "transform", "translate(" + zeroX + ",0)");
			mySingle.setAttributeNS(null, "class", "coord");
			mySingle.setAttributeNS(null, "x1", offSetX);
			mySingle.setAttributeNS(null, "y1", zeroY - 7);
			mySingle.setAttributeNS(null, "x2", offSetX);
			mySingle.setAttributeNS(null, "y2", zeroY + height + 7);
			document.getElementById(workArea.svgId).appendChild(mySingle);
			
			// 5.) Set the value tooltip
			$('svg[id="' + workArea.svgId + '"] rect[id^="bar_"]').tipsy({
				gravity : 's',
				title : 'adv_amount'
			});			
		},

		
		/*
		 * Draw the Top/Middle/Bottom Horizontal Coordinate Axis based on the chartView
		 */
		drawCoordinate : function(barIndex, coordResult, coordConst, zeroX, style)
		{
			var mySelf = this;
			var workArea = mySelf.workArea;
			var drawSize = mySelf.drawSize;
			var zeroY = 0, noOfTick = 1, unitText = "";
			
			if(mySelf.type == "hub")
			{
				if(style == "top")
				{
					zeroY = coordConst.zeroYTop;
					noOfTick = 1.0 * coordResult.noOfAliveTick;
				}
				else if(style == "bottom")
				{
					zeroY = coordConst.zeroYBottom;
					noOfTick = 1.0 * coordResult.noOfTotalTick[barIndex];
				}
			}
			else if(mySelf.type == "depot")
			{
				noOfTick = 1.0 * coordResult.noOfAliveTick;
				if(style == "top")
				{
					zeroY = coordConst.zeroYTop;
					//unitText = (mySelf.chartView == "hub_user") ? " Inbound" : " Outbound";
					unitText = " Inbound" ;
				}
				else if(style == "middle")
				{
					zeroY = coordConst.zeroYMiddle;
				}
				else if(style == "bottom")
				{
					zeroY = coordConst.zeroYBottom;
					//unitText = (mySelf.chartView == "hub_user") ? " Outbound" : " Inbound";
					unitText = " Outbound" ;
				}
			}

				
				// 1.) draw horizontal line of coordinate
				var myCoord = document.createElementNS(mySelf.svgNS, "line");
				myCoord.setAttributeNS(null, "class", "coord");
				myCoord.setAttributeNS(null, "x1", (style == "middle") ? mySelf.padding : zeroX);
				myCoord.setAttributeNS(null, "y1", zeroY);
				myCoord.setAttributeNS(null, "x2", drawSize.widthFrame);
				myCoord.setAttributeNS(null, "y2", zeroY);
				document.getElementById(workArea.svgId).appendChild(myCoord);
				
				// 2.) draw the unit text of it, if the style is either top or bottom
				if(style != "middle")
				{
					for(var i = 0; i < 2; i++)
					{
						var myText = document.createElementNS(mySelf.svgNS, "text");
						myText.setAttributeNS(null, "class", "write");
						myText.setAttributeNS(null, "x", (i == 0) ? zeroX : (drawSize.widthGraph + coordConst.scaleTextX) );
						myText.setAttributeNS(null, "y", (style == "top") ? zeroY - coordConst.scaleTextY - 5  : zeroY + coordConst.scaleTextY + 15);
						
						var myTextNode = (i == 0) ? 
								document.createTextNode("\u00D7" + coordResult.scale) :
								document.createTextNode("\u00D7" + coordResult.scale + unitText);
						myText.appendChild(myTextNode);
						document.getElementById(workArea.svgId).appendChild(myText);
						// Set the correct position ,but it works after rendering...
						var width = myText.getBBox().width;
						myText.setAttributeNS(null, "x", (i == 0) ? zeroX : (drawSize.widthGraph - width) );
					}
				}

				// 3.) draw the scaleTick & value of it
				for ( var i = 0; i < (noOfTick + 1); i++)
				{
					var scaleX = mySelf.backTo(i * coordResult.normalScale) + zeroX;
					var myCoord = document.createElementNS(mySelf.svgNS, "line");
					myCoord.setAttributeNS(null, "class", "coord");
					myCoord.setAttributeNS(null, "x1", scaleX);
					myCoord.setAttributeNS(null, "y1", zeroY - coordConst.scaleTick);
					myCoord.setAttributeNS(null, "x2", scaleX);
					myCoord.setAttributeNS(null, "y2", zeroY + coordConst.scaleTick);
					document.getElementById(workArea.svgId).appendChild(myCoord);

					// Draw the value if the style is either top or bottom
					if(style != "middle")
					{
						var myText = document.createElementNS(mySelf.svgNS, "text");
						myText.setAttributeNS(null, "class", "write");
						myText.setAttributeNS(null, "x", scaleX - 2);
						myText.setAttributeNS(null, "y", (style == "top") ? zeroY - 5 : zeroY + 15);
						myText.textContent = 1.0 * i * coordResult.tickValue;
						document.getElementById(workArea.svgId).appendChild(myText);
						// Set the correct position ,but it works after rendering...
						var width = myText.getBBox().width;
						myText.setAttributeNS(null, "x", scaleX - (width / 2.0));
					}
				}
				
				// 4.) draw tick of closed hub at depot & middle
				if( (mySelf.type == "depot") && (style == "middle") )
				{
					noOfTick = 1.0 * coordResult.noOfClosedTick;
					for ( var i = 1; i < (noOfTick + 1); i++)
					{
						var scaleX = zeroX - mySelf.backTo(i * coordResult.normalScale);
						var myCoord = document.createElementNS(mySelf.svgNS, "line");
						myCoord.setAttributeNS(null, "class", "coord");
						myCoord.setAttributeNS(null, "x1", scaleX);
						myCoord.setAttributeNS(null, "y1", zeroY - coordConst.scaleTick);
						myCoord.setAttributeNS(null, "x2", scaleX);
						myCoord.setAttributeNS(null, "y2", zeroY + coordConst.scaleTick);
						document.getElementById(workArea.svgId).appendChild(myCoord);
					}						
				}
			
		},
		
		
		/*
		 * Set the details for Legend
		 */
		setDetails : function(barItem)
		{
			var mySelf = this;
			var workArea = mySelf.workArea;

			$(barItem).each(function(index, element)
			{
				var i = workArea.detailsId + "_" + element.info;
				$('td[id="' + i + '"]').text(mySelf.roundValue(element.value));
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
			var decimals = 0;
			result = Math.round(1.0 * value * Math.pow(10, decimals)) / Math.pow(10, decimals);
			return result.toFixed(decimals);			
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
				$('div[id^="' + divId + '_details"]').remove();
				
				$("#" + divId).removeAttr("class");
				$("#" + divId).removeAttr("style");
				$("#" + divId).removeAttr("adv_type");
			}
		}

	};

	/*
	 * Entry point of plug-in
	 */
	$.fn.sumchart = function(method, options)
	{
		if(method === "clearBar")
		{
			$.ajaxSetup({
				cache : false
			});
			
			return this.each(function(index)
			{
				var main = Object.create(chartMethods);
				main[method](this, index);
			});
		}
		else if (method === "summaryBar")
		{
			$.ajaxSetup({
				cache : false
			});

			if (options !== undefined)
				return this.each(function(index)
				{
					var main = Object.create(chartMethods);
					main[method](this, index, options);
				});

		} else
		{
			$.error("There is not this method: " + method);
		}
	};

})(jQuery, window, document);