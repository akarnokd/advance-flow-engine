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
	
	var warehouseL2Methods = {
			svgNS : "http://www.w3.org/2000/svg",
			padding : 10,
			workArea : {},
			drawSize : {},
			displaySide : ["leftSide", "rightSide"],
			columnInfo :
			{
				initZeroX : 5,
				initZeroY : 15,
				topMargin: 5,
				middleMargin : 5,
				height : 20,
				width :
				{
					circle : 20,
					text : 30,
					bar : 0,
					barPadding : 3
				}
			},
			
			warehouseL2Bar : function(elem, index, options)
			{
				var mySelf = this;
				mySelf.elem = elem;
				mySelf.$elem = $(elem);
				mySelf.mode = "warehouse-l2";

				var settings = {};
				settings = {
					url : "",
					type : "POST",
					data : {},
					headTitle : "",
					handSilentRect : ""
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
											(result.leftSide !== undefined) &&
											(result.rightSide !== undefined))
									{
										mySelf.clearBar(elem, index);
										
										mySelf.workArea.holderId = mySelf.defineHolder(mySelf.$elem, index, mySelf.mode + "-holder");
										
										// For every displaySide, define the size
										mySelf.workArea.frameId = {};
										mySelf.workArea.svgId = {};
										$.each(mySelf.displaySide, function(index, value)
										{
											mySelf.workArea.frameId[index] = mySelf.workArea.holderId + "_frame_" + index;
											mySelf.createDIV(mySelf.workArea.holderId, mySelf.workArea.frameId[index], "frame");
											
											mySelf.workArea.svgId[index] = mySelf.workArea.holderId + "_svg_" + index;
											mySelf.createSVG(mySelf.workArea.frameId[index], mySelf.workArea.svgId[index]);
										});
										
										var noOfBar = ( $(result.lefSide).size() > $(result.rightSide).size() ) ?
												$(result.lefSide).size() : $(result.rightSide).size();
										mySelf.drawSize = mySelf.setGraphSize(noOfBar);
										mySelf.setHeadTitleSize(settings.headTitle);
										
										// a.) Draw title of info and bars to the left and to the right display side
										$.each(mySelf.displaySide, function(index, value)
										{
											if(result.direction[value].align == "left")
											{
												mySelf.drawLeftSideTitle(index, settings.headTitle, result.direction[value]);
											}
											else if(result.direction[value].align == "right")
											{
												mySelf.drawRightSideTitle(index, settings.headTitle, result.direction[value]);
											}
										});
										
										// b.) Draw info and bars to the left and to the right display side
										$.each(mySelf.displaySide, function(index, value)
										{
											if(result.direction[value].align == "left")
											{
												mySelf.drawLeftSideAlign(index, result.direction[value].align, result[value]);
											}
											else if(result.direction[value].align == "right")
											{
												mySelf.drawRightSideAlign(index, result.direction[value].align, result[value]);
											}
										});
										
										// c.) Add the trigger hander to the silent div
										var divId = mySelf.$elem.attr("id");
										$('div[id^="' + divId + '_frame"] rect[id^="silent_"]').on("click", function(event)
										{
											var rect = $(event.target);
											$("#" + settings.handSilentRect).triggerHandler({
												type : "click.silent",
												storage_id : rect.attr("storage_id"),
												storage_info: rect.attr("storage_info")
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
			
			createGroup :function(appendId, groupId, storageData)
			{
				var mySelf = this;
				var myGroup = document.createElementNS(mySelf.svgNS, "g");
				myGroup.setAttribute("id", groupId);
				myGroup.setAttribute("storage_info", storageData.warehouseLayout);
				myGroup.setAttribute("storage_id", storageData.id);
				document.getElementById(appendId).appendChild(myGroup);
			},
			
			/*
			 * Set the Graph size
			 */
			setGraphSize : function(noOfBar)
			{
				var mySelf = this;
				var workArea = mySelf.workArea;
				var columnInfo = mySelf.columnInfo;
				var drawSize = {};
				
				var bodyWidth  = $(window).width() - 2 * 16 - (2 * mySelf.padding);
				var bodyHeight = $(window).height() - 2 * 16 - mySelf.padding - 150 - 20;
				
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
				
				drawSize.widthFrame = Math.floor((drawSize.holderWidth - 2 * 16 - 1) / 2);
				drawSize.heightFrame = noOfBar * (columnInfo.topMargin + columnInfo.height) + columnInfo.topMargin;
				
				$.each(mySelf.displaySide, function(index, value)
				{
					$("#" + workArea.frameId[index]).width(drawSize.widthFrame);
					$("#" + workArea.frameId[index]).height(drawSize.heightFrame);
					mySelf.setSVGViewBox(workArea.svgId[index], drawSize.widthFrame, drawSize.heightFrame);
				});
				
				columnInfo.width.bar = Math.floor((drawSize.widthFrame - columnInfo.width.circle - columnInfo.width.text - 4 * columnInfo.middleMargin) / 2);
				
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
			
			drawSilentRect : function(appendId, rectId, storageData, zeroX, zeroY)
			{
				var mySelf = this;
				var columnInfo = mySelf.columnInfo;
				
				var myRect = document.createElementNS(mySelf.svgNS, "rect");
				myRect.setAttribute("id", rectId);
				myRect.setAttributeNS(null, "class", "silent");
				myRect.setAttributeNS(null, "x", zeroX);
				myRect.setAttributeNS(null, "y", zeroY);
				myRect.setAttributeNS(null, "width", columnInfo.width.circle + columnInfo.width.text);
				myRect.setAttributeNS(null, "height", columnInfo.height);
				myRect.setAttribute("storage_info", storageData.warehouseLayout);
				myRect.setAttribute("storage_id", storageData.id);
				document.getElementById(appendId).appendChild(myRect);
			},

			/*
			 * Set the title divs 
			 */
			setHeadTitleSize : function(headTitleId)
			{
				var mySelf = this;
				var drawSize = mySelf.drawSize;
				
				// If there was not any previous content..  
				if($("#" + headTitleId).hasClass("head-title") == false)
				{
					$("#" + headTitleId).addClass("head-title").width(drawSize.holderWidth - 2 * 16);
					// Create divs & set size of them for left/right side
					$.each(mySelf.displaySide, function(index, value)
					{
						// Left/right outer div
						var div = $("<div>").attr("id", headTitleId + "_" + index);
						div.attr("class", "side-title side-title_" + index).width(drawSize.widthFrame - 3);
						$("#" + headTitleId).append(div);
						
						// Backgorund div (inside outer div) to make symmetrical width for title text
						var divBack = $("<div>").attr("id", headTitleId + "_" + index + "_back");
						divBack.attr("class", "back-title back-title_" + index).width(drawSize.widthFrame - 10);
						
						$(div).append(divBack);
						
						// Columns div of title text
						for(var i = 0; i < 3; i++)
						{
							var innerDiv = $("<div>").attr("id", headTitleId + "_" + index + "_back_" + i);
							innerDiv.attr("class", "column");
							
							$(divBack).append(innerDiv);
						}
					});
				}
				//.. else resize & delete contant 
				else
				{
					$("#" + headTitleId).width(drawSize.holderWidth - 2 * 16);
					$.each(mySelf.displaySide, function(index, value)
					{
						$("#" + headTitleId + "_" + index).width(drawSize.widthFrame - 3);
						$("#" + headTitleId + "_" + index + "_back").width(drawSize.widthFrame - 10);
						
						for(var i = 0; i < 3; i++)
							$("#" + headTitleId + "_" + index + "_back_" + i).html("");
					});
				}
			},
			
			drawLeftSideTitle : function(index, headTitleId, directionSide)
			{
				var mySelf = this;
				var columnInfo = mySelf.columnInfo;
				var sizeArray = [columnInfo.width.circle + columnInfo.width.text + columnInfo.middleMargin - 1,
				                 columnInfo.width.bar - 2,
				                 columnInfo.width.bar - 6];
				var textArray = [directionSide.warehouseInfo, directionSide.at_hub, directionSide.coming];
				
				for(var i = 0; i < 3; i++)
				{
					var innerDiv = headTitleId + "_" + index + "_back_" + i;
					$("#" + innerDiv).css({
						"width" : sizeArray[i],
						"text-align" : directionSide.align,
					});
					$("#" + innerDiv).html(textArray[i]);
				}
			},
			
			drawRightSideTitle : function(index, headTitleId, directionSide)
			{
				var mySelf = this;
				var columnInfo = mySelf.columnInfo;
				var sizeArray = [columnInfo.width.bar - 6,
				                 columnInfo.width.bar - 2,
				                 columnInfo.width.circle + columnInfo.width.text + columnInfo.middleMargin - 1];
				var textArray = [directionSide.coming, directionSide.at_hub, directionSide.warehouseInfo];
				
				for(var i = 0; i < 3; i++)
				{
					var innerDiv = headTitleId + "_" + index + "_back_" + i;
					$("#" + innerDiv).css({
						"width" : sizeArray[i],
						"text-align" : directionSide.align,
					});
					$("#" + innerDiv).html(textArray[i]);
				}
			},
			
			drawCircle : function(appendId, bgColor, zeroX, zeroY)
			{
				var mySelf = this;
				var columnInfo = mySelf.columnInfo; 
				
				var myCircle = document.createElementNS(mySelf.svgNS, "circle");
				myCircle.setAttributeNS(null, "class", "default");
				myCircle.setAttributeNS(null, "style", "fill:"  + bgColor);
				myCircle.setAttributeNS(null, "cx", zeroX);
				myCircle.setAttributeNS(null, "cy", zeroY);
				myCircle.setAttributeNS(null, "r", columnInfo.width.circle / 2.0);
				document.getElementById(appendId).appendChild(myCircle);
			},
			
			drawText : function(appendId, align, value, style, zeroX, zeroY)
			{
				var mySelf = this;
				var textAlign = (align == "left") ? "start" : "end";
				
				var myText = document.createElementNS(mySelf.svgNS, "text");
				myText.setAttributeNS(null, "class", style);
				myText.setAttributeNS(null, "text-anchor", textAlign);
				myText.setAttributeNS(null, "x", zeroX);
				myText.setAttributeNS(null, "y", zeroY);
				myText.textContent = value;
				document.getElementById(appendId).appendChild(myText);
			},

			/*
			 * Draw the Bar chart
			 */
			drawBarGraph : function(appendId, align, storageData, zeroX, zeroY)
			{
				var mySelf = this;
				var columnInfo = mySelf.columnInfo;				

				var myRect = document.createElementNS(mySelf.svgNS, "rect");
				myRect.setAttributeNS(null, "class", "default");
				myRect.setAttributeNS(null, "style", "fill:" + storageData.bgColor);
				myRect.setAttributeNS(null, "x", zeroX);
				myRect.setAttributeNS(null, "y", zeroY);
				myRect.setAttributeNS(null, "width", columnInfo.width.bar);
				myRect.setAttributeNS(null, "height", columnInfo.height);
				document.getElementById(appendId).appendChild(myRect);
				
				if(align == "left")
				{
					var myPath = document.createElementNS(mySelf.svgNS, "path");
					myPath.setAttributeNS(null, "transform", "translate(" + (zeroX + columnInfo.width.barPadding) + ",0)");
					var points = "M0, "  + (zeroY + 7)  + " H" + mySelf.backTo(storageData.standardNormal);
					points    += "M0, "  + (zeroY + 14) + " H" + mySelf.backTo(storageData.priorityNormal);
					myPath.setAttributeNS(null, "d", points);
					myPath.setAttributeNS(null, "class", "contour");
					document.getElementById(appendId).appendChild(myPath);			
				}
				else if(align == "right")
				{
					var myPath = document.createElementNS(mySelf.svgNS, "path");
					myPath.setAttributeNS(null, "transform", "translate(" + (zeroX + columnInfo.width.bar - columnInfo.width.barPadding) + ",0)");
					var points = "M0, "  + (zeroY + 7)  + " H" + (-1.0 * mySelf.backTo(storageData.standardNormal));
					points    += "M0, "  + (zeroY + 14) + " H" + (-1.0 * mySelf.backTo(storageData.priorityNormal));
					myPath.setAttributeNS(null, "d", points);
					myPath.setAttributeNS(null, "class", "contour");
					document.getElementById(appendId).appendChild(myPath);			
				}
			},

			
			drawLeftSideAlign : function(index, align, storageChartList)
			{
				var mySelf = this;
				var svgId = mySelf.workArea.svgId[index];
				var columnInfo = mySelf.columnInfo;
				
				var zeroY = columnInfo.initZeroY;
				$(storageChartList).each(function(objIndex, objElement)
				{
					var zeroX = columnInfo.initZeroX + columnInfo.width.circle / 2.0;
					mySelf.drawCircle(svgId, objElement.overColor, zeroX, zeroY);
					
					zeroX += (columnInfo.width.circle / 2.0) + columnInfo.middleMargin;
					mySelf.drawText(svgId, align, objElement.id, "write-info", zeroX, zeroY + 5);
					
					var gId = "silent_" + index +"_"+ objIndex;
					mySelf.drawSilentRect(svgId, gId, objElement, columnInfo.initZeroX, zeroY - 10);
					
					zeroX += columnInfo.width.text;
					mySelf.drawBarGraph(svgId, align, objElement.at_hub, zeroX, zeroY - 10);
					zeroX += columnInfo.width.bar + columnInfo.middleMargin;
					mySelf.drawBarGraph(svgId, align, objElement.coming, zeroX, zeroY - 10);
					
					zeroY += columnInfo.topMargin + columnInfo.height;
				});
			},
			
			drawRightSideAlign : function(index, align, storageChartList)
			{
				var mySelf = this;
				var svgId = mySelf.workArea.svgId[index];
				var columnInfo = mySelf.columnInfo;
				
				var zeroY = columnInfo.initZeroY;
				$(storageChartList).each(function(objIndex, objElement)
				{
					var zeroX = columnInfo.initZeroX;
					mySelf.drawBarGraph(svgId, align, objElement.coming, zeroX, zeroY - 10);
					zeroX += columnInfo.width.bar + columnInfo.middleMargin;
					mySelf.drawBarGraph(svgId, align, objElement.at_hub, zeroX, zeroY - 10);
					
					zeroX += columnInfo.width.bar + columnInfo.width.text;
					mySelf.drawText(svgId, align, objElement.id, "write-info", zeroX, zeroY + 5);
					
					zeroX += columnInfo.middleMargin + columnInfo.width.circle / 2.0;
					mySelf.drawCircle(svgId, objElement.overColor, zeroX, zeroY);
					
					var gId = "silent_" + index +"_"+ objIndex;
					var zeroRect = zeroX - columnInfo.width.circle / 2.0 - columnInfo.width.text;
					mySelf.drawSilentRect(svgId, gId, objElement, zeroRect, zeroY - 10);
					
					zeroY += columnInfo.topMargin + columnInfo.height;
				});

			},

			
			/*
			 * Scale with value
			 */
			backTo : function(value)
			{
				var mySelf = this;
				var result = 0;

				if (mySelf.columnInfo.width.bar > 0)
					result = 1.0 * value * (mySelf.columnInfo.width.bar - 2 * mySelf.columnInfo.width.barPadding);

				return result;
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
					
					$("#" + divId).removeAttr("class");
					$("#" + divId).removeAttr("style");
					$("#" + divId).removeAttr("adv_type");
				}
			}

		};
	
	
	/*
	 * Entry point of plug-in
	 */
	$.fn.warehousel2chart = function(method, options)
	{
		if(method === "clearBar")
		{
			$.ajaxSetup({
				cache : false
			});
			
			return this.each(function(index)
			{
				var main = Object.create(warehouseL2Methods);
				main[method](this, index);
			});
		}
		else if (method === "warehouseL2Bar")
		{
			$.ajaxSetup({
				cache : false
			});

			if (options !== undefined)
				return this.each(function(index)
				{
					var main = Object.create(warehouseL2Methods);
					main[method](this, index, options);
				});

		} else
		{
			$.error("There is not this method: " + method);
		}
	};
	
})(jQuery, window, document);