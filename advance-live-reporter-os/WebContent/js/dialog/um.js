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
 * @author csirobi, 2013.08.04.
 */


(function($, window, document, undefined)
{
	var dialogUm = {
			
			dialogId : "dialog_um",
			dialogDetailsId : "dialog_um_details",
			dialogChoiceId : "dialog_um_choice",
			msgDialogId: "dialog_um_msg",
			userTable: "um_users",
			
			addUserId : "N/A",
			checked : ["./images/hubdepot/dialog_unchecked.png", "./images/hubdepot/dialog_checked.png"],
			
			callDialogUm : function()
			{
				var mySelf = this;
				
				// Main modal dialog
				$("#" + mySelf.dialogId).dialog(
				{
          autoOpen: true,
          modal: true,
          width: 840,
          height: 500,
          resizable: false,
          
          open : function(event, ui)
          {
          	mySelf.loadUsers();
          },
          
          buttons:
          {
          	"Add user" : function()
          	{
          		mySelf.userDetailsModal();
          	},
          	"Cancel" : function()
          	{
          		$("#" + mySelf.dialogId).dialog("close");
          	}
          },
          
          close : function()
          {
          }
        });
			},
			
			
			/*
			 * Load the Users into the main table.
			 */
			loadUsers : function()
			{
				var mySelf = this;
				
				$.ajax({
				  url : "api-users.jsp",
				  type : "POST",
					data : {
						action : "get_users"
					},
				  dataType : "json",
				  success : function(result)
				  {
				  	if ((result.users !== undefined) && (result.logged !== undefined))
				  	{
				  		mySelf.processUsersData(result.users, result.logged);
				  		mySelf.editButtonListener(result.users, result.logged);
				  		mySelf.deleteButtonListener(result.users);
				  	}
				  	else if (result.error !== undefined)
				  	{
				  		mySelf.messageModal(result.error);
				  	}
				  },
				  error : function()
				  {
				  	mySelf.messageModal();
				  }
				});
				
			},
			
			/*
			 * Process the user data come from ajax calling.
			 */
			processUsersData : function(rUsers, loggedId)
			{
				var mySelf = this;
				var tRow, tCell, chkImg, tButton;
				
				$("table#" + mySelf.userTable + " tr").remove();
				
				$(rUsers).each(function(index, element)
				{
					tRow = $("<tr>");
					
					tCell = $("<td>").attr("class", "id-td").html(element.id);
			    tRow.append(tCell);
			    tCell = $("<td>").attr("class", "name-td").html(element.name);
			    tRow.append(tCell);
			    tCell = $("<td>").attr("class", "hub-td").html(element.hub);
			    tRow.append(tCell);
			    tCell = $("<td>").attr("class", "depot-td").html(element.depot);
			    tRow.append(tCell);
			    
			    chkImg = $("<img>").attr("src", mySelf.checked[1.0 * element.admin]).attr("adv_chk", element.admin);
			    tCell = $("<td>").attr("class", "cell-chkbox admin-td").append(chkImg);
			    tRow.append(tCell);
			    
			    tCell = $("<td>").attr("class", "email-td").html(element.email);
			    tRow.append(tCell);
			    
			    tButton = $("<div>").attr("id", "edit_" + index).attr("class", "cell-button").html("Edit");
			    tCell = $("<td>").append(tButton);
			    tRow.append(tCell);
			    tButton = $("<div>").attr("id", (loggedId != element.id) ? "del_" + index : "logged_" + index)
			    	.attr("class", (loggedId != element.id) ? "cell-button" : "cell-button cell-logged")
			    	.html("Delete");
			    tCell = $("<td>").append(tButton);
			    tRow.append(tCell);
			    
			    $("table#" + mySelf.userTable).append(tRow);
				});
			},
			
			/*
			 * Edit button listener in the main table.
			 */
			editButtonListener : function(rUsers, loggedId)
			{
				var mySelf = this;
				
				$('#' + mySelf.dialogId + ' div[id^="edit_"]').on("click", function(event)
				{
					var temp = event.target.id.split("_");
					mySelf.userDetailsModal(rUsers[temp[1]], loggedId);
				});
			},
			
			/*
			 * Delete button listener in the main table.
			 */
			deleteButtonListener : function(rUsers)
			{
				var mySelf = this;
				
				$('#' + mySelf.dialogId + ' div[id^="del_"]').on("click", function(event)
				{
					var temp = event.target.id.split("_");
					var userId = rUsers[temp[1]].id;
					mySelf.areUSureModal(userId);
				});
				
			},
			
			/*
			 * Confirmation modal dialog.
			 */
			areUSureModal : function(userId)
			{
				var mySelf = this;
				
				$("#" + mySelf.dialogChoiceId).dialog(
				{
          autoOpen: true,
          modal: true,
          width: 300,
          height: 200,
          resizable: false,
          
          open : function(event, ui)
          {
          	$("div#" + mySelf.dialogChoiceId + " p#um_choice").html("This user will be deleted. <br/> Are you sure?");
          },
          
          buttons:
          {
          	"Yes, delete it" : function()
          	{
    					$.ajax({
    					  url : "api-users.jsp",
    					  type : "POST",
    					  data : {
    					  	action : "delete_user",
    					    id : userId
    					  },
    					  dataType : "json",
    					  success : function(result)
    					  {
    					  	$("#" + mySelf.dialogChoiceId).dialog("close");
    					  	mySelf.loadUsers();
    					  },
    					  error : function()
    					  {
    					  	mySelf.messageModal();
    					  }
    					});

          	},
          	"Cancel" : function()
          	{
          		$("#" + mySelf.dialogChoiceId).dialog("close");
          	}
          }
        });
				
			},
			
			/*
			 * Error message modal dialog.
			 */
			messageModal : function(msg)
			{
				var mySelf = this;
				var errMsg = (msg !== undefined) ? msg : "There seems to be a problem with the server.";
				
				$("div#" + mySelf.msgDialogId).dialog(
				{
          autoOpen: true,
          modal: true,
          width: 300,
          height: 200,
          resizable: false,
          
          open : function(event, ui)
          {
          	$("div#" + mySelf.msgDialogId + " p#um_message").html("");
          	$("div#" + mySelf.msgDialogId + " p#um_message").html(errMsg);
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
			 * Add/Edit User details modal dialog.
			 */
			userDetailsModal : function(userData, loggedId)
			{
				var mySelf = this;
				
				// User Details Modal
				$("#" + mySelf.dialogDetailsId).dialog(
				{
          autoOpen: true,
          modal: true,
          width: 480,
          height: 450,
          resizable: false,
          
          open : function(event, ui)
          {
          	if(userData !== undefined)
          	{
            	mySelf.refreshHubs(userData.hubId);
            	mySelf.refreshDepots(userData.depotId);
          		mySelf.refreshOthers(userData);
          		if(userData.id != loggedId)
          			mySelf.addChkboxHandler();
          	}
          	else
          	{
            	mySelf.refreshHubs();
            	mySelf.refreshDepots();
          		mySelf.refreshOthers();
          		mySelf.addChkboxHandler();
          	}
          	
          	mySelf.addViewHandler();
          	
          },
          
          buttons:
          {
          	"Save" : function()
          	{
          		mySelf.saveUser();
          	},
          	"Cancel" : function()
          	{
          		$("#" + mySelf.dialogDetailsId).dialog("close");
          	}
          },
          
          close : function()
          {
          	mySelf.removeChkboxHandler();
          	mySelf.removeViewHandler();
          }
          
        });
				
			},
			
			/*
			 * Add checkbox hander in the user details dialog.
			 */
			addChkboxHandler : function()
			{
				var mySelf = this;
				var isChk, updChk;
				
				$("div#dialog_um_details img#um_details_admin").toggleClass("cursor-pointer");
				
				$("div#dialog_um_details img#um_details_admin").on("click", function(event)
				{
					isChk = $(event.target).attr("adv_chk") * 1.0;
					updChk = (isChk + 1) % 2;
					$(event.target).attr("src", mySelf.checked[updChk]).attr("adv_chk", updChk);
				});
			},
			
			/*
			 * Remove checkbox hander from the user details dialog.
			 */
			removeChkboxHandler : function()
			{
				$("div#dialog_um_details img#um_details_admin").toggleClass("cursor-pointer", false);
				$("div#dialog_um_details img#um_details_admin").off();
			},
			
			/*
			 * Add screen view hander in the user details dialog.
			 */
			addViewHandler : function()
			{
				var eventVal;
				
				$("div#dialog_um_details select#um_details_screen").on("change", function(event)
				{
					eventVal = $(event.target).val();
					$("div#dialog_um_details select#um_details_depot").val("-1").prop("disabled", (eventVal != "hub") ? "disabled" : false);
				});
				
			},
			
			/*
			 * Remove screen view hander in the user details dialog.
			 */
			removeViewHandler : function()
			{
				$("div#dialog_um_details select#um_details_screen").off();
			},
			
			/*
			 * Refresh the hub list in the user details dialog.
			 */
			refreshHubs : function(hubId)
			{
				var mySelf = this;
				$("div#dialog_um_details select#um_details_hub option").remove();
				
				$.ajax({
				  url : "api-users.jsp",
				  type : "POST",
					data : {
						action : "get_hubs"
					},
				  dataType : "json",
				  success : function(result)
				  {
				  	if (result.hubs !== undefined)
				  	{
							$(result.hubs).each(function(idx, elem)
							{
								var opt = $("<option>");
								opt.attr("value", elem.id);
								opt.text(elem.name);
								
								$("select#um_details_hub").append(opt);
							});
							
							if(hubId !== undefined)
							{
								$("select#um_details_hub").val(hubId);
							}
							else if(hubId === undefined)
							{
								$("select#um_details_hub").val("1");
							}
							
				  	}
				  	else if (result.error !== undefined)
				  	{
				  		mySelf.messageModal(result.error);
				  	}
				  },
				  error : function()
				  {
				  	mySelf.messageModal();
				  }
				  
				});
				
			},
			
			/*
			 * Refresh the depot list in the user details dialog.
			 */
			refreshDepots : function(depotId)
			{
				var mySelf = this;
				$("div#dialog_um_details select#um_details_depot option").remove();
				
				var o = $("<option>");
				o.attr("value", "-1").text("None");
				$("select#um_details_depot").append(o);

				
				$.ajax({
				  url : "api-users.jsp",
				  type : "POST",
					data : {
						action : "get_depots"
					},
				  dataType : "json",
				  success : function(result)
				  {
				  	if (result.depots !== undefined)
				  	{
							$(result.depots).each(function(idx, elem)
							{
								var opt = $("<option>");
								opt.attr("value", elem.id);
								opt.text(elem.name);
								
								$("select#um_details_depot").append(opt);
							});
							
							if(depotId !== undefined)
							{
								$("select#um_details_depot").val(depotId);
							}
							else if(depotId === undefined)
							{
								$("select#um_details_depot").val("-1");
							}
				  	}
				  	else if (result.error !== undefined)
				  	{
				  		mySelf.messageModal(result.error);
				  	}
				  },
				  error : function()
				  {
				  	mySelf.messageModal();
				  }
				});
				
			},
			
			/*
			 * Refresh other fields in the user details dialog.
			 */
			refreshOthers : function(userData)
			{
				var mySelf = this;
				
				if(userData !== undefined)
				{
					$('div#dialog_um_details td[id="um_details_id"]').text(userData.id);
					$('div#dialog_um_details input[id="um_details_name"]').val(userData.name);
					$('div#dialog_um_details img[id="um_details_admin"]').attr("src", mySelf.checked[userData.admin]).attr("adv_chk", userData.admin);
					$('div#dialog_um_details input[id="um_details_email"]').val(userData.email);
					$('div#dialog_um_details select[id="um_details_screen"]').val(userData.view);
					$('div#dialog_um_details input[id="um_details_pwd_1"]').val("");
					$('div#dialog_um_details input[id="um_details_pwd_2"]').val("");
					
					$("div#dialog_um_details select#um_details_depot").prop("disabled", (userData.view != "hub") ? "disabled" : false);
				}
				else
				{
					$('div#dialog_um_details td[id="um_details_id"]').text(mySelf.addUserId);
					$('div#dialog_um_details input[id="um_details_name"]').val("");
					$('div#dialog_um_details img[id="um_details_admin"]').attr("src", mySelf.checked[0]).attr("adv_chk", "0");
					$('div#dialog_um_details input[id="um_details_email"]').val("");
					$('div#dialog_um_details select[id="um_details_screen"]').val("hub");
					$('div#dialog_um_details input[id="um_details_pwd_1"]').val("");
					$('div#dialog_um_details input[id="um_details_pwd_2"]').val("");
					
					$("div#dialog_um_details select#um_details_depot").prop("disabled", false);
				}
				
				$("div#dialog_um_details div#um_details_info").html("");
			},
			
			/*
			 * Save the user from the user details dialog.
			 */
			saveUser : function()
			{
				var mySelf = this;
				var result = {};
				
				result.id = $('div#dialog_um_details td[id="um_details_id"]').text();
				result.name = $('div#dialog_um_details input[id="um_details_name"]').val();
				result.hub = $('div#dialog_um_details select[id="um_details_hub"]').val();
				result.depot = $('div#dialog_um_details select[id="um_details_depot"]').val();
				result.admin =  $('div#dialog_um_details img[id="um_details_admin"]').attr("adv_chk");
				result.email = $('div#dialog_um_details input[id="um_details_email"]').val();
				result.view = $('div#dialog_um_details select[id="um_details_screen"]').val();				
				result.password = $('div#dialog_um_details input[id="um_details_pwd_1"]').val();
				var userPwd_2 = $('div#dialog_um_details input[id="um_details_pwd_2"]').val();
				
				if (result.name.length == 0)
				{
				  $("#um_details_info").html("The name field is empty.");
				} else if (result.password !== userPwd_2)
				{
				  $("#um_details_info").html("Password fields are different.");
				} else if ((result.id == mySelf.addUserId) && (result.password.length == 0) && (userPwd_2.length == 0))
				{
				  $("#um_details_info").html("Both password field is empty.");
				} else
				{
					$.ajax({
				    url : "api-users.jsp",
				    type : "POST",
				    data : {
				    	action : "save_user",
				      json : JSON.stringify(result)
				    },
				    dataType : "json",
				    success : function(result)
				    {
				    	if(result.done !== undefined)
				    	{
					    	$("#" + mySelf.dialogDetailsId).dialog("close");
					    	mySelf.loadUsers();
				    	}
					  	else if (result.error !== undefined)
					  	{
					  		mySelf.messageModal(result.error);
					  	}
				    },
					  error : function()
					  {
					  	mySelf.messageModal();
					  }
				  });
				}
			}
	};
	
	$.dialogum = function(method, params)
	{
		var op = Object.create(dialogUm);
		
		if (method === "callDialogUm")
			op[method]();
	};
	
})(jQuery, window, document);