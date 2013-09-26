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
	var dialogBaus = {
			
			callDialogBaus : function(params)
			{
				var isModified = false;
				
				$("#dialog_baus").dialog(
				{
          autoOpen: true,
          modal: true,
          width: 300,
          height: 185,
          resizable: false,
          
          open : function(event, ui)
          {
          	$('#dialog_baus input[id="baus_value"]').val(params.bausValue);
          },
          
          buttons:
          {
          	"Ok" : function()
          	{
          		var bv = $('#dialog_baus input[id="baus_value"]').val();
							if( (bv != null) && ($.isNumeric(bv) ) )
							{
								$.ajax({
									url: "api-baus.jsp",
									type : "POST",
									dataType : "json",
									data : {
										mode : "update",
										baus : bv,
										store_type: params.store.type,
										store_id : params.store.id,
										unit : params.unit,
										datetime : params.datetime,
									},
									success: function(result)
									{
										if(result.baus !== undefined)
										{
											isModified = true;
											$("#dialog_baus").dialog("close");
										}
									}
									
								});
							}
          	},
          	"Cancel" : function()
          	{
          		$("#dialog_baus").dialog("close");
          	}
          },
          
          close : function()
          {
          	if(isModified)
          	{
							$("#handler_settings").triggerHandler({
								type : "settings.baus",
								active : baus,
							});
          	}
          }
        });
				
			},
	};
	
	$.dialogbaus = function(method, params)
	{
		var op = Object.create(dialogBaus);
		
		if (method === "callDialogBaus")
			op[method](params);
	};
	
})(jQuery, window, document);