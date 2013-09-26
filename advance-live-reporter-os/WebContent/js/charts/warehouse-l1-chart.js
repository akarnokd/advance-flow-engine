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

	var warehouseL1Methods = {
		svgNS : "http://www.w3.org/2000/svg",
		padding : 10,
		type : "",
		workArea : {},
		drawSize : {},
		
		warehouseL1Bar : function(elem, index, options)
		{
			var mySelf = this;
			mySelf.elem = elem;
			mySelf.$elem = $(elem);
			mySelf.mode = "warehouse-l1";

			var settings = {};
			settings = {
				url : "",
				type : "POST",
				data : {},
				text : {
					zeroYTitle: 22,
				},
				coord : {
					overall : {
						zeroY : 60,
						scaleTick : 3,
						scaleTextX : -24,
						scaleTextY : 15,
					},
					athub : {
						zeroY : 195,
						scaleTick : 3,
						scaleCol  : 90,
						scaleTextX : -24,
						scaleTextY : 20,
					}
				},
				bar : {
					overall :{
						zeroY : 70,
						height : 22
					},
					athub : {
						zeroY : [110, 127, 152, 170],
						height : 12
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
										(result.coordinate !== undefined) &&
										(result.bars !== undefined))
								{
									mySelf.clearBar(elem, index);
									
									mySelf.workArea.holderId = mySelf.defineHolder(mySelf.$elem, index, mySelf.mode + "-holder");
									
									$(result.bars).each(function(barIndex, barElement)
									{
										mySelf.workArea.frameId = mySelf.workArea.holderId + "_frame_" + barIndex;
										mySelf.createDIV(mySelf.workArea.holderId, mySelf.workArea.frameId, "frame");
										
										mySelf.workArea.outerDetailsId = mySelf.workArea.holderId + "_details_" + barIndex;
										mySelf.createDIV(mySelf.workArea.holderId, mySelf.workArea.outerDetailsId, "out-details");
										
										mySelf.workArea.svgId = mySelf.workArea.holderId + "_svg_" + barIndex;
										mySelf.createSVG(mySelf.workArea.frameId, mySelf.workArea.svgId);
										mySelf.drawSize = mySelf.setGraphSize();
										
										mySelf.drawText(barElement.unit, "title", 0, settings.text.zeroYTitle);
										var zeroX = mySelf.padding;
										
										mySelf.drawBarGraph(barIndex, barElement.overall, settings.bar.overall, zeroX);
										mySelf.drawCoordinate(result.coordinate.overall, settings.coord.overall, zeroX, "overall");
										
										mySelf.drawAtHubGraph(barElement.athub, settings.bar.athub, zeroX);
										mySelf.drawCoordinate(result.coordinate.athub, settings.coord.athub, zeroX, "athub");
										
										
										$.each(result.status, function(statusIndex, statusElement)
										{
											mySelf.workArea.detailsId = mySelf.workArea.holderId + "_details_" + barIndex + "_" + statusIndex;
											mySelf.createDIV(mySelf.workArea.outerDetailsId, mySelf.workArea.detailsId, "details " + statusIndex);
											mySelf.createDetails(statusIndex, statusElement);
											mySelf.setDetails(statusIndex, barElement[statusIndex]);
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
		createDetails : function(statusIndex, statusElement)
		{
			var mySelf = this;
			var workArea = mySelf.workArea;

			var table = $("<table>");
			var tbody = $("<tbody>");
			table.append(tbody);
			
			if(statusIndex == "overall")
			{
				$(statusElement).each(function(index, element)
						{
							var tRow, tCell, tDiv;
							tRow = $("<tr>");

							tDiv = $("<div>").attr("class", element.info);
							tCell = $("<td>").attr("class", "legend").append(tDiv);
							tRow.append(tCell);
							tCell = $("<td>").html(element.message);
							tRow.append(tCell);
							tCell = $("<td>").attr("id", workArea.detailsId + "_" + element.info);
							tCell.attr("class", "details-right").html("");
							tRow.append(tCell);

							tbody.append(tRow);
						});
			}
			else if(statusIndex == "athub")
			{
				$(statusElement).each(function(index, element)
						{
							var tRow, tCell, tDiv;
							var tdCSS = (index == 1) ? " distance" : " normal";
							
							tRow = $("<tr>").attr("id", workArea.detailsId + "_" + element.info);
							
							tDiv = $("<div>").attr("class", "at_hub");
							tCell = $("<td>").attr("class", "legend" + tdCSS).append(tDiv);
							tRow.append(tCell);
							tCell = $("<td>").attr("class", tdCSS).html(element.message);
							tRow.append(tCell);
							tCell = $("<td>").attr("class", "details-right" + tdCSS).html("");
							tRow.append(tCell);
							tCell = $("<td>").attr("class", "details-right" + tdCSS).html("");
							tRow.append(tCell);
							tCell = $("<td>").attr("class", "details-right" + tdCSS).html("");
							tRow.append(tCell);

							tbody.append(tRow);
						});
			}
			
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
			
			drawSize.widthFrame = drawSize.holderWidth - $("#" + workArea.outerDetailsId).width() - 2 * 16 - 4;
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
		 * Draw the Bar chart
		 */
		drawBarGraph : function(barId, barItem, barConst, zeroX)
		{
			var mySelf = this;
			var workArea = mySelf.workArea;
			var offSetX = 0;
			var zeroY = barConst.zeroY;
			var height = barConst.height;
			
			// Backgorund of Left hub status 
			var zeroRect = document.createElementNS(mySelf.svgNS, "rect");
			zeroRect.setAttributeNS(null, "class", "zeroArea");
			zeroRect.setAttributeNS(null, "x", 0);
			zeroRect.setAttributeNS(null, "y", zeroY - 7);
			zeroRect.setAttributeNS(null, "width", zeroX + mySelf.backTo(barItem[0].normalValue));
			zeroRect.setAttributeNS(null, "height", height + 14);
			document.getElementById(mySelf.workArea.svgId).appendChild(zeroRect);

			// For a concrete bar: Draw different state of items
			// Predicted status is handled different case than other ones...
			$(barItem).each(
					function(index, element)
					{
						// Draw every status
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
					});
		},
		
		
		/*
		 * Draw the At hub graph 
		 */
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
		drawCoordinate : function(coordResult, coordConst, zeroX, style)
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
				var from = (style == "overall") ? 0 : 1;
				for(var i = from; i < 2; i++)
				{
					var myText = document.createElementNS(mySelf.svgNS, "text");
					myText.setAttributeNS(null, "class", "write");
					myText.setAttributeNS(null, "x", (i == 0) ? zeroX : (drawSize.widthGraph + coordConst.scaleTextX) );
					myText.setAttributeNS(null, "y", (style == "overall") ? zeroY - coordConst.scaleTextY - 5  : zeroY + coordConst.scaleTextY + 5);
					
					var myTextNode = (style == "overall") ? 
							document.createTextNode("\u00D7" + coordResult.scale) :
							document.createTextNode("\u00D7 " + coordResult.scaleInfo);
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
					myCoord.setAttributeNS(null, "y1", (style == "overall") ? zeroY - coordConst.scaleTick : zeroY - coordConst.scaleCol);
					myCoord.setAttributeNS(null, "x2", scaleX);
					myCoord.setAttributeNS(null, "y2", zeroY + coordConst.scaleTick);
					document.getElementById(workArea.svgId).appendChild(myCoord);
					
					// Draw the value
					var myText = document.createElementNS(mySelf.svgNS, "text");
					myText.setAttributeNS(null, "class", "write");
					myText.setAttributeNS(null, "x", scaleX - 2);
					myText.setAttributeNS(null, "y", (style == "overall") ? zeroY - 5 : zeroY + 15);
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
		setDetails : function(statusIndex, barParts)
		{
			var mySelf = this;
			var workArea = mySelf.workArea;

			if(statusIndex == "overall")
			{
				$(barParts).each(function(index, element)
						{
							var i = workArea.detailsId + "_" + element.info;
							$('td[id="' + i + '"]').text(mySelf.roundValue(element.value));
						});
				
			}
			else if(statusIndex == "athub")
			{
				var idx = ["id", "value", "percent"];
				
				$(barParts).each(function(index, element)
				{
					$("#" + workArea.detailsId + "_" + element.info + " td.details-right").each(function(tdIndex, tdElement)
							{
								var val = (tdIndex == 1) ? mySelf.roundValue(element[idx[tdIndex]]) : element[idx[tdIndex]];
								$(tdElement).html(val);
			 			  });
					
				});
				
			}
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
	$.fn.warehousel1chart = function(method, options)
	{
		if(method === "clearBar")
		{
			$.ajaxSetup({
				cache : false
			});
			
			return this.each(function(index)
			{
				var main = Object.create(warehouseL1Methods);
				main[method](this, index);
			});
		}
		else if (method === "warehouseL1Bar")
		{
			$.ajaxSetup({
				cache : false
			});

			if (options !== undefined)
				return this.each(function(index)
				{
					var main = Object.create(warehouseL1Methods);
					main[method](this, index, options);
				});

		} else
		{
			$.error("There is not this method: " + method);
		}
	};

})(jQuery, window, document);