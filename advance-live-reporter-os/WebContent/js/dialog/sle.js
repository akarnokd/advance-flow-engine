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
	var dialogSle = {
			
			dialogId : "dialog_sle",
			msgDialogId : "dialog_sle_msg",
			chDialogId : "dialog_sle_choice",
			activeHub : "",
			activeWarehouse : "",
			isModified : false,
			selectedRow : "selected-row",
			rowHeight: 30,
			
			chkboxes : ["sle_chk_standard", "sle_chk_priority"],
			checked : ["./images/hubdepot/dialog_unchecked.png", "./images/hubdepot/dialog_checked.png"], 
			deleted : "./images/hubdepot/dialog_delete.png",
			toUp : "./images/hubdepot/dialog_to_up.png",
			toDown : "./images/hubdepot/dialog_to_down.png",
			imgWidth : 26,
			imgHeight: 26,
			
			callDialogSle : function(params)
			{
				var mySelf = this;
				
				// Main modal dialog 
      	$("#" + mySelf.dialogId).dialog(
				{
          autoOpen: true,
          modal: true,
          width: 890,
          height: 620,
          resizable: false,
          
          open : function(event, ui)
          {
          	mySelf.createSelectors();
          	mySelf.createUpDownButton();
          	mySelf.loadHubs();
          	mySelf.addUpListener();
          	mySelf.addDownListener();
          },
          
          buttons:
          {
          	addLeft : {
          		class: "sle-left",
          		text : "Add left",
          		click : function()
          		{
          			var depotRow = mySelf.createDepotRowLeft();
          			$('div#dialog_sle div[id^="sle_left"]').append(depotRow);
          			mySelf.emptyRowScrollRefresh(depotRow);
          			
          			mySelf.addChkboxListener(depotRow);
          			mySelf.addDeleteListener(depotRow);
          			mySelf.addInputListener(depotRow);
          			mySelf.addSelectRowListener(depotRow);
          			mySelf.isModified = true;
          		}
          	},
          	
          	"Ok" : function()
          	{
          		if(mySelf.canSave())
          		{
          			mySelf.saveAjaxLayout()
          				.done(function(result)
          				{
          					mySelf.isModified = false;
          					$("#" + mySelf.dialogId).dialog("close");
          				})

          				.fail(function(jqXHR, textStatus, errorThrown)
          				{
          					$.headoption("showErrorMessage");
          				});							
          		}
          		else
          		{
          			var ss = "The layout has changed, but not valid.<br/>There is empty depot id or capacity.";
          			mySelf.messageModal(ss);
          		}
          	},
          	
          	"Cancel" : function()
          	{
          		$("#" + mySelf.dialogId).dialog("close");
          	},
          	
          	addRight : {
          		class: "sle-right",
          		text : "Add right",
          		click : function()
          		{
          			var depotRow = mySelf.createDepotRowRight();
          			$('div#dialog_sle div[id^="sle_right"]').append(depotRow);
          			mySelf.emptyRowScrollRefresh(depotRow);          			
          			
          			mySelf.addChkboxListener(depotRow);
          			mySelf.addDeleteListener(depotRow);
          			mySelf.addInputListener(depotRow);
          			mySelf.addSelectRowListener(depotRow);
          			mySelf.isModified = true;
          		}
          	}
          	
          },
          
          close : function()
          {
          	mySelf.isModified = false;
          	mySelf.clearContent();
          	mySelf.clearUpDownButton();
          	mySelf.clearSelectors();
          }
        });
				
			},
			
			/*
			 * Create the hub and the warehouse selectors and labels
			 */
			createSelectors: function()
			{
				var mySelf = this;
				
				var divH = $("<div>").attr("class", "label-head label-hub").html("Hub");
				$("div#dialog_sle div#sle_header").append(divH);
				var selH = $("<select>").attr("id", "sle_hubs").attr("class", "selector");
				$("div#dialog_sle div#sle_header").append(selH);
				
				var divS = $("<div>").attr("class", "label-head label-warehouse").html("Warehouse");
				$("div#dialog_sle div#sle_header").append(divS);
				var selS = $("<select>").attr("id", "sle_warehouses").attr("class", "selector");
				$("div#dialog_sle div#sle_header").append(selS);
				
				mySelf.addHubListener();
      	mySelf.addWarehouseListener();
			},
			
			/*
			 * Clear the hub and the warehouse selectors and labels
			 */
			clearSelectors : function()
			{
      	$("div#dialog_sle div#sle_header div.label-head").remove();
      	$("div#dialog_sle select#sle_hubs").remove();
      	$("div#dialog_sle select#sle_warehouses").remove();
			},
			
			createUpDownButton : function()
			{
				var mySelf = this;
				var img;
				
				img = $("<img>");
				img.attr("id", "sle_up_left").attr("src", mySelf.toUp);
				$("div.sle-up-down.up-down-left").append(img);

				img = $("<img>");
				img.attr("id", "sle_down_left").attr("src", mySelf.toDown);
				$("div.sle-up-down.up-down-left").append(img);
				
				img = $("<img>");
				img.attr("id", "sle_up_right").attr("src", mySelf.toUp);
				$("div.sle-up-down.up-down-right").append(img);

				img = $("<img>");
				img.attr("id", "sle_down_right").attr("src", mySelf.toDown);
				$("div.sle-up-down.up-down-right").append(img);
				
			},
			
			clearUpDownButton : function()
			{
				$("div.sle-up-down img#sle_up_left").remove();
				$("div.sle-up-down img#sle_down_left").remove();
				$("div.sle-up-down img#sle_up_right").remove();
				$("div.sle-up-down img#sle_down_right").remove();
			},
			
			/*
			 * Load hubs unto the hub selector 
			 */
			loadHubs: function()
			{
				var mySelf = this;
				$("div#dialog_sle select#sle_hubs option").remove();
				
				$.ajax({
					url : "api-layout.jsp",
					type : "POST",
					data : {
						action : "get_hubs"
					},
					dataType : "json",
					success : function(result) {
						$(result).each(function(idx, elem)
						{
							var opt = $("<option>");
							opt.attr("value", elem.id);
							opt.text(elem.name);
							if(idx == 0)
							{
								opt.attr("selected", "selected");
								mySelf.activeHub = elem.id;
							}
							$("select#sle_hubs").append(opt);
						});
						
						mySelf.loadWarehouses(mySelf.activeHub);
					}
				});
				
			},
			
			/*
			 * Load warehouses into the warehouse selector, based on selected hub
			 */
			loadWarehouses : function(hubParam)
			{
				var mySelf = this;
				$("div#dialog_sle select#sle_warehouses option").remove();
				
				$.ajax({
					url : "api-layout.jsp",
					type : "POST",
					data : {
						action : "get_warehouses",
						hub : hubParam
					},
					dataType : "json",
					success : function(result)
					{
						$(result).each(function(idx, elem)
						{
							var opt = $("<option>");
							opt.attr("value", elem);
							opt.text(elem);
							if(idx == 0)
							{
								opt.attr("selected", "selected");
								mySelf.activeWarehouse = elem;
							}
							$("select#sle_warehouses").append(opt);
						});

						mySelf.loadLayout(mySelf.activeHub, mySelf.activeWarehouse);
						
					}
				});
				
			},
			
			/*
			 * Load layout of the selected warehouse
			 */
			loadLayout : function(hubParam, warehouseParam)
			{
				var mySelf = this;
				mySelf.clearContent();
				
				$.ajax({
					url : "api-layout.jsp",
					type : "POST",
					data : {
						action : "get_layout",
						hub : hubParam,
						warehouse: warehouseParam
					},
					dataType : "json",
					success : function(result)
					{
						$(result.left).each(function(idxDepot, elemDepot)
						{
							var depotRow = mySelf.createDepotRowLeft(elemDepot);
							$('div#dialog_sle div[id^="sle_left"]').append(depotRow);
						});
						
						$(result.right).each(function(idxDepot, elemDepot)
						{
							var depotRow = mySelf.createDepotRowRight(elemDepot);
							$('div#dialog_sle div[id^="sle_right"]').append(depotRow);
						});
						
          	mySelf.addChkboxListener();
          	mySelf.addDeleteListener();
          	mySelf.addInputListener();
          	mySelf.addSelectRowListener();
          	
					}
				});
			},
			
			/*
			 * Create the left side layout of depots
			 */
			createDepotRowLeft : function(depot)
			{
				var mySelf = this;
				var row, cell, img, inp;
				var depotId = "", depotCapacity = "", flags = [0, 0, 0];
				
				if( depot !== undefined)
				{
					depotId = depot.depot;
					depotCapacity = depot.capacity;
					var flg = depot.flags;
					for(var i = 2; i > -1; i--)
					{
						var p = Math.pow(2, i);
						flags[i] = Math.floor(flg / p);
						flg %= p;
					}
				}
				
				row = $("<div>");
				row.attr("class", "depot-row");
				
				cell = $("<div>");
				cell.attr("class", "depot-cell");
				inp = $("<input>");
				inp.attr("id", "sle_depot_id").attr("type", "text").attr("value", depotId).attr("class", "depot-input");
				row.append(cell.append(inp));
				
				cell = $("<div>");
				cell.attr("class", "depot-cell");
				inp = $("<input>");
				inp.attr("id", "sle_depot_capacity").attr("type", "text").attr("value", depotCapacity).attr("class", "depot-input");
				row.append(cell.append(inp));
				
				$.each(mySelf.chkboxes, function(idx, elem)
				{
					cell = $("<div>");
					cell.attr("class", "depot-cell");
					img = $("<img>");
					img.attr("id", elem).attr("src", mySelf.checked[flags[idx]]).attr("adv_chk", flags[idx]);
					img.attr("width", mySelf.imgWidth).attr("height", mySelf.imgHeight);
					row.append(cell.append(img));
				});
				
				cell = $("<div>");
				cell.attr("class", "depot-cell");
				img = $("<img>");
				img.attr("id", "sle_deleted").attr("src", mySelf.deleted).attr("width", mySelf.imgWidth).attr("height", mySelf.imgHeight);
				row.append(cell.append(img));
				
				return row;				
			},
			
			/*
			 * Create the right side layout of depots
			 */
			createDepotRowRight : function(depot)
			{
				var mySelf = this;
				var row, cell, img, inp;
				var depotId = "", depotCapacity = "", flags = [0, 0, 0];
				
				if( depot !== undefined)
				{
					depotId = depot.depot;
					depotCapacity = depot.capacity;
					var flg = depot.flags;
					for(var i = 2; i > -1; i--)
					{
						var p = Math.pow(2, i);
						flags[i] = Math.floor(flg / p);
						flg %= p;
					}
				}
				
				row = $("<div>");
				row.attr("class", "depot-row");
				
				cell = $("<div>");
				cell.attr("class", "depot-cell");
				img = $("<img>");
				img.attr("id", "sle_deleted").attr("src", mySelf.deleted).attr("width", mySelf.imgWidth).attr("height", mySelf.imgHeight);
				row.append(cell.append(img));
				
				$.each(mySelf.chkboxes, function(idx, elem)
				{
					cell = $("<div>");
					cell.attr("class", "depot-cell");
					img = $("<img>");
					img.attr("id", elem).attr("src", mySelf.checked[flags[idx]]).attr("adv_chk", flags[idx]);
					img.attr("width", mySelf.imgWidth).attr("height", mySelf.imgHeight);
					row.append(cell.append(img));
				});

				cell = $("<div>");
				cell.attr("class", "depot-cell");
				inp = $("<input>");
				inp.attr("id", "sle_depot_capacity").attr("type", "text").attr("value", depotCapacity).attr("class", "depot-input");
				row.append(cell.append(inp));
				
				cell = $("<div>");
				cell.attr("class", "depot-cell");
				inp = $("<input>");
				inp.attr("id", "sle_depot_id").attr("type", "text").attr("value", depotId).attr("class", "depot-input");
				row.append(cell.append(inp));
				
				return row;				
			},
			
			/*
			 * Error, warning message modal dialog
			 */
			messageModal : function(msg)
			{
				var mySelf = this;
				
				$("div#" + mySelf.msgDialogId).dialog(
				{
          autoOpen: true,
          modal: true,
          width: 300,
          height: 200,
          resizable: false,
          
          open : function(event, ui)
          {
          	$("div#" + mySelf.msgDialogId + " p#sle_message").html("");
          	$("div#" + mySelf.msgDialogId + " p#sle_message").html(msg);
          },
          buttons:
          {
          	Ok: function()
          	{
          		$( this ).dialog( "close" );
          	}
          }
					
				});
			},
			
			/*
			 * Choice modal dialog, when layout modified and save
			 */
			modifiedSave : function(selector, changedVal, msg)
			{
				var mySelf = this;
				
				$("div#" + mySelf.chDialogId).dialog(
				{
          autoOpen: true,
          modal: true,
          width: 300,
          height: 200,
          resizable: false,
					
          open : function(event, ui)
          {
          	$("div#" + mySelf.chDialogId + " p#sle_choice").html("");
          	$("div#" + mySelf.chDialogId + " p#sle_choice").html(msg);
          },
          buttons:
          {
          	"Save": function()
          	{
        			mySelf.saveAjaxLayout()
        				.done(function(result)
        				{
          				if(selector == "hub")
          				{
          					mySelf.activeHub = changedVal;
          					mySelf.loadWarehouses(mySelf.activeHub);
          				}
          				else if(selector == "warehouse")
          				{
          					mySelf.activeWarehouse = changedVal;
          					mySelf.loadLayout(mySelf.activeHub, mySelf.activeWarehouse);
          				}
            	  })
            	  
        				.fail(function(jqXHR, textStatus, errorThrown)
        				{
        					$.headoption("showErrorMessage");
        				});
          		
        			mySelf.isModified = false;
          		$( this ).dialog( "close" );
          	},
          	
          	"Continue without saving": function()
          	{
      				if(selector == "hub")
      				{
      					mySelf.activeHub = changedVal;
      					mySelf.loadWarehouses(mySelf.activeHub);
      				}
      				else if(selector == "warehouse")
      				{
      					mySelf.activeWarehouse = changedVal;
      					mySelf.loadLayout(mySelf.activeHub, mySelf.activeWarehouse);
      				}
          		
      				mySelf.isModified = false;
          		$( this ).dialog( "close" );
          	},
          }
          
				});
				
			},

			/*
			 * Choice modal dialog, when layout modified but can not save
			 */
			modifiedNotSave : function(selector, changedVal, msg)
			{
				var mySelf = this;
				
				$("div#" + mySelf.chDialogId).dialog(
				{
          autoOpen: true,
          modal: true,
          width: 300,
          height: 200,
          resizable: false,
					
          open : function(event, ui)
          {
          	$("div#" + mySelf.chDialogId + " p#sle_choice").html("");
          	$("div#" + mySelf.chDialogId + " p#sle_choice").html(msg);
          },
          buttons:
          {
          	"Stay here": function()
          	{
      				if(selector == "hub")
      				{
      					$("div#dialog_sle select#sle_hubs").val(mySelf.activeHub);
      				}
      				else if(selector == "warehouse")
      				{
      					$("div#dialog_sle select#sle_warehouses").val(mySelf.activeWarehouse);
      				}
          		
      				$( this ).dialog( "close" );      				
          	},
          	
          	"Continue without saving": function()
          	{
      				if(selector == "hub")
      				{
      					mySelf.activeHub = changedVal;
      					mySelf.loadWarehouses(mySelf.activeHub);
      				}
      				else if(selector == "warehouse")
      				{
      					mySelf.activeWarehouse = changedVal;
      					mySelf.loadLayout(mySelf.activeHub, mySelf.activeWarehouse);
      				}
          		
      				mySelf.isModified = false;
          		$( this ).dialog( "close" );
          	},
          }
          
				});
			},
			
			
			/*
			 * Clear the layout content
			 */
			clearContent : function()
			{
				$("div#dialog_sle div#sle_left div.depot-row").remove();
				$("div#dialog_sle div#sle_right div.depot-row").remove();
			},
			
			/*
			 * Checkbox listener
			 */
			addChkboxListener : function(depotRow)
			{
				var mySelf = this;
				var isChk, updChk;
				
				if(depotRow !== undefined)
				{
					$(depotRow).find('img[id^="sle_chk_"]').on("click", function(event)
					{
						isChk = $(event.target).attr("adv_chk") * 1.0;
						updChk = (isChk + 1) % 2;
						$(event.target).attr("src", mySelf.checked[updChk]).attr("adv_chk", updChk);
						mySelf.isModified = true;
					});
				}
				else
				{
					$('div#dialog_sle img[id^="sle_chk_"]').on("click", function(event)
					{
						isChk = $(event.target).attr("adv_chk") * 1.0;
						updChk = (isChk + 1) % 2;
						$(event.target).attr("src", mySelf.checked[updChk]).attr("adv_chk", updChk);
						mySelf.isModified = true;
					});
				}
			},
			
			/*
			 * Delete listener
			 */
			addDeleteListener : function(depotRow)
			{
				var mySelf = this;
				
				if(depotRow !== undefined)
				{
					$(depotRow).find('img[id^="sle_deleted"]').on("click", function(event)
					{
						$(depotRow).remove();
						mySelf.isModified = true;
					});
				}
				else
				{
					$('div#dialog_sle img[id^="sle_deleted"]').on("click", function(event)
					{
						$(event.target).parents(".depot-row").remove();
						mySelf.isModified = true;
					});
				}

			},

			/*
			 * Move up listener
			 */
			addUpListener : function()
			{
				var mySelf = this;
				
				$('div#dialog_sle img[id^="sle_up_"]').on("click", function(event)
				{
					var selRow = $("div#dialog_sle div." + mySelf.selectedRow);
					if(selRow.size() > 0)
					{
						var previous = selRow.prev();
						selRow.insertBefore(previous);
						mySelf.upDownScrollRefresh(selRow.position().top);
						
						mySelf.isModified = true;
					}
					
				});
			},

			/*
			 * Move down listener
			 */
			addDownListener : function()
			{
				var mySelf = this;
				
				$('div#dialog_sle img[id^="sle_down_"]').on("click", function(event)
				{
					var selRow = $("div#dialog_sle div." + mySelf.selectedRow);
					if(selRow.size() > 0)
					{
						var next = selRow.next();
						selRow.insertAfter(next);
						mySelf.upDownScrollRefresh(selRow.position().top);
						
						mySelf.isModified = true;
					}
					
				});
			},
			
			/*
			 * Number inupt box listener
			 */
			addInputListener : function(depotRow)
			{
				var mySelf = this;
				
				if(depotRow !== undefined)
				{
					$(depotRow).find('input[id^="sle_depot_"]').on("keypress", function(event)
					{
						var i = ( event.which != 8 && 
											event.which != 0 && 
											(event.which < 48 || event.which > 57)) ? false : true;
			
						mySelf.isModified = true;
						return i;
					});
				}
				else
				{
					$('div#dialog_sle input[id^="sle_depot_"]').on("keypress", function(event)
					{
						var i = ( event.which != 8 && 
											event.which != 0 && 
											(event.which < 48 || event.which > 57)) ? false : true;
			
						mySelf.isModified = true;
						return i;
					});
				}
				
			},
			
			/*
			 * Depot row selector listener
			 */
			addSelectRowListener : function(depotRow)
			{
				var mySelf = this;
				
				if(depotRow !== undefined)
				{
					$(depotRow).on("click", function(event)
					{
						$('div#dialog_sle div.depot-row').toggleClass(mySelf.selectedRow, false);
						$(depotRow).toggleClass(mySelf.selectedRow);
						mySelf.isModified = true;
					});
				}
				else
				{
					$('div#dialog_sle div.depot-row').on("click", function(event)
					{
						$('div#dialog_sle div.depot-row').toggleClass(mySelf.selectedRow, false);
						var par = $(event.target).parents(".depot-row");
						par.toggleClass(mySelf.selectedRow);
						mySelf.isModified = true;
					});
				}
				
			},
			
			/*
			 * Move the scroll bar based on the up/down buttons and the position of the selected row
			 */
			upDownScrollRefresh: function(rowPos)
			{
				var mySelf = this;
				var contentScroll = $("div#dialog_sle div#sle_content").scrollTop();
				
				if(rowPos < contentScroll)
				{
					$("div#dialog_sle div#sle_content").scrollTop(contentScroll - (4 * mySelf.rowHeight));
				}
				else if(contentScroll + (10 * mySelf.rowHeight) < rowPos)
				{
					$("div#dialog_sle div#sle_content").scrollTop(contentScroll + (4 * mySelf.rowHeight));
				}

			},
			
			/*
			 * Move the scroll bar based on the position of the new empty row
			 */
			emptyRowScrollRefresh: function(depotRow)
			{
				var mySelf = this;
				
  			$('div#dialog_sle div.depot-row').toggleClass(mySelf.selectedRow, false);
				$(depotRow).toggleClass(mySelf.selectedRow);
				$("div#dialog_sle div#sle_content").scrollTop(depotRow.position().top);
			},
			
			/*
			 * Hub selector listener
			 */
			addHubListener : function()
			{
				var mySelf = this;
				
				$("div#dialog_sle select#sle_hubs").on("change", function(event)
				{
					if(mySelf.isModified == false)
					{
						mySelf.activeHub = $(event.target).val();
						mySelf.loadWarehouses(mySelf.activeHub);
					}
					else
					{
						// Modified AND it can save 
						if(mySelf.canSave())
						{
							mySelf.modifiedSave("hub", $(event.target).val(), "The layout has changed.<br/>Would you like to save it?");
						}
						// Modified BUT it can not save
						else
						{
							mySelf.modifiedNotSave("hub", $(event.target).val(), "The layout has changed, but not valid.<br/>What would you like?");
						}
					}
				});
			},
			
			/*
			 * Warehouse selector listener 
			 */
			addWarehouseListener : function()
			{
				var mySelf = this;

				$("div#dialog_sle select#sle_warehouses").on("change", function(event)
				{
					if(mySelf.isModified == false)
					{
						mySelf.activeWarehouse = $(event.target).val();
						mySelf.loadLayout(mySelf.activeHub, mySelf.activeWarehouse);
					}
					else
					{
						// Modified AND it can save 
						if(mySelf.canSave())
						{
							mySelf.modifiedSave("warehouse", $(event.target).val(), "The layout has changed.<br/>Would you like to save it?");
						}
						// Modified BUT it can not save
						else
						{
							mySelf.modifiedNotSave("warehouse", $(event.target).val(), "The layout has changed, but not valid.<br/>What would you like?");
						}
					}
				});
				
			},
			
			/*
			 * Save ajax calling
			 */
			saveAjaxLayout : function()
			{
				var mySelf = this;
				
				return $.ajax({
					url : "api-layout.jsp",
					type : "POST",
					data : {
						action : "save_layout",
						hub : mySelf.activeHub,
						warehouse: mySelf.activeWarehouse,
						json : mySelf.saveToJSON()
					},
					dataType : "json",
				});
				
			},
			
			/*
			 * Check to be able to save layout
			 */
			canSave : function()
			{
				var mySelf = this;
				var isOk = true;
				var emptyRow = null;
				
				$("div#dialog_sle input#sle_depot_id").each(function(idx, elem)
				{
					if($(elem).val().length == 0)
					{
						$(elem).focus();
						emptyRow = $(elem).parents(".depot-row");
						isOk = false;
					}
				});
				
				$("div#dialog_sle input#sle_depot_capacity").each(function(idx, elem)
				{
					if($(elem).val().length == 0)
					{
						$(elem).focus();
						emptyRow = $(elem).parents(".depot-row");
						isOk = false;
					}
				});
				
				if(emptyRow != null)
				{
					$('div#dialog_sle div.depot-row').toggleClass(mySelf.selectedRow, false);
					emptyRow.toggleClass(mySelf.selectedRow);
				}
				
				return isOk;
			},
			
			/*
			 * Save layout to JSON string
			 */
			saveToJSON : function()
			{
				var mySelf = this;
				var result ={};
				
				$("div#dialog_sle div.depot-table").each(function(idxTable, elemTable)
				{
					var side = elemTable.id.split("_")[1];
					result[side] = new Array();
					
					$(elemTable).find("div.depot-row").each(function(idxRow, elemRow)
					{
						var rec = {};
						rec["depot"] = $(elemRow).find("input#sle_depot_id").val();
						rec["capacity"] = $(elemRow).find("input#sle_depot_capacity").val();
						
						var flg = 0;
						for(var i = 0; i < 2; i++)
						{
							var isChk = $(elemRow).find("img#" + mySelf.chkboxes[i]).attr("adv_chk") * 1.0;
							flg += isChk * Math.pow(2, i);
						}
						rec["flags"] = flg;
						
						result[side].push(rec);
					});
					
				});
						
				//console.log(JSON.stringify(result));
				return JSON.stringify(result);
			}
	};
	
	$.dialogsle = function(method, params)
	{
		var op = Object.create(dialogSle);
		
		if (method === "callDialogSle")
			op[method](params);
	};
	
})(jQuery, window, document);