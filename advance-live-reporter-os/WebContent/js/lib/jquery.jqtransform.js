/*
 * jqTransform
 * by mathieu vilaplana mvilaplana@dfc-e.com
 * Designer ghyslain armand garmand@dfc-e.com
 *
 * Version 1.0 25.09.08
 * Version 1.1 06.08.09
 *
 * Modified by csirobi, 12.04.13:
 *  Call-back event handling and tablet compatible css
 *  
 ******************************************** */
 
(function($){
	var defaultOptions = {preloadImg:true};
	
	/***************************
	  Labels
	***************************/
	var jqTransformGetLabel = function(objfield){
		var selfForm = $(objfield.get(0).form);
		var oLabel = objfield.next();
		if(!oLabel.is('label')) {
			oLabel = objfield.prev();
			if(oLabel.is('label')){
				var inputname = objfield.attr('id');
				if(inputname){
					oLabel = selfForm.find('label[for="'+inputname+'"]');
				} 
			}
		}
		if(oLabel.is('label')){return oLabel.css('cursor','pointer');}
		return false;
	};
	
	/* Hide all open selects */
	var jqTransformHideSelect = function(oTarget){
		var ulVisible = $('.jqTransformSelectWrapper ul:visible');
		ulVisible.each(function(){
			var oSelect = $(this).parents(".jqTransformSelectWrapper:first").find("select").get(0);
			//do not hide if click on the label object associated to the select
			if( !(oTarget && oSelect.oLabel && oSelect.oLabel.get(0) == oTarget.get(0)) ){$(this).hide();}
		});
	};
	/* Check for an external click */
	var jqTransformCheckExternalClick = function(event) {
		if ($(event.target).parents('.jqTransformSelectWrapper').length === 0) { jqTransformHideSelect($(event.target)); }
	};

	/* Apply document listener */
	var jqTransformAddDocumentListener = function (){
		$(document).mousedown(jqTransformCheckExternalClick);
	};	
			
	/* Add a new handler for the reset action */
	var jqTransformReset = function(f){
		var sel;
		$('.jqTransformSelectWrapper select', f).each(function(){sel = (this.selectedIndex<0) ? 0 : this.selectedIndex; $('ul', $(this).parent()).each(function(){$('a:eq('+ sel +')', this).click();});});
		$('a.jqTransformCheckbox, a.jqTransformRadio', f).removeClass('jqTransformChecked');
		$('input:checkbox, input:radio', f).each(function(){if(this.checked){$('a', $(this).parent()).addClass('jqTransformChecked');}});
	};
	
	var jqTransSelectEnabled = function(optEn)
	{
		if(optEn == true)
		{
			$("div.jqTransformSelectWrapper").toggleClass("modal-lock", false);
		}
		else
		{
			$("div.jqTransformSelectWrapper").toggleClass("modal-lock", false);
			$("div.jqTransformSelectWrapper").toggleClass("modal-lock");			
		}
	};

	
	/***************************
	  Select 
	 ***************************/	
	$.fn.jqTransSelect = function(opt){
		return this.each(function(index){
			var $select = $(this);

			if($select.hasClass('jqTransformHidden')) {return;}
			if($select.attr('multiple')) {return;}

			var oLabel  =  jqTransformGetLabel($select);
			/* First thing we do is Wrap it */
			var $wrapper = $select
				.addClass('jqTransformHidden')
				.wrap('<div class="jqTransformSelectWrapper"></div>')
				.parent()
				.css({zIndex: 10-index})
			;
			
			$("div.jqTransformSelectWrapper").append('<div class="disabled"></div>');
			
			
			/* Now add the html for the select */
			$wrapper.prepend('<div id="design-selector"><span id="active-select"></span><a href="#" class="jqTransformSelectOpen"></a></div><ul></ul>');
			var $ul = $('ul', $wrapper).css('width',$select.width()).hide();
			/* Now we add the options */
			$('option', this).each(function(i, v){
				var adv_x = $(v).attr("value");
				var oLi = $('<li><a href="#" index="'+ i +'"' + ' adv_x="' + adv_x + '">'+ $(this).html() +'</a></li>');
				$ul.append(oLi);
			});
			
			/* Add click handler to the a */
			$ul.find('a').click(function(){
					$('a.selected', $wrapper).removeClass('selected');
					$(this).addClass('selected');	
					
					// Fetch the value of the selected a
					var adv_x = $(this).attr("adv_x");
					
					/* Fire the onchange event */
					if ($select[0].selectedIndex != $(this).attr('index') && $select[0].onchange)
					{
						$select[0].selectedIndex = $(this).attr('index'); $select[0].onchange();
					}
					$select[0].selectedIndex = $(this).attr('index');
					
					// Add the new text and adv_x value to the span
					$('span:eq(0)', $wrapper).html($(this).html());
					$('span:eq(0)', $wrapper).attr("adv_x", adv_x);
					
					$ul.hide();
					
					// Call back the change function defined by user
					if( (opt["change"] !== undefined) && ((typeof opt["change"] === "function")))
					{
						opt["change"](adv_x);
					} 					
					
					return false;
			});
			/* Set the default */
			$('a:eq('+ this.selectedIndex +')', $ul).click();
			$('span:first', $wrapper).click(function(){$("a.jqTransformSelectOpen",$wrapper).trigger('click');});
			oLabel && oLabel.click(function(){$("a.jqTransformSelectOpen",$wrapper).trigger('click');});
			this.oLabel = oLabel;
			
			/* Apply the click handler to the Open */
			var oLinkOpen = $('a.jqTransformSelectOpen', $wrapper)
				.click(function(){
					//Check if box is already open to still allow toggle, but close all other selects
					if( $ul.css('display') == 'none' ) {jqTransformHideSelect();} 
					if($select.attr('disabled')){return false;}

					$ul.slideToggle('fast', function(){					
						var offSet = ($('a.selected', $ul).offset().top - $ul.offset().top);
					});
					return false;
				})
			;

			// Set the new width
			var iSelectWidth = $select.outerWidth();
			var oSpan = $('span:first',$wrapper);
			//var newWidth = (iSelectWidth > oSpan.innerWidth())?iSelectWidth+oLinkOpen.outerWidth():$wrapper.width();
			var newWidth = iSelectWidth+oLinkOpen.outerWidth();
			$wrapper.css('width',newWidth);
			$ul.css('width',newWidth-2);
			oSpan.css({width:iSelectWidth});
			
			// Calculate the height if necessary, less elements that the default height
			//show the ul to calculate the block, if ul is not displayed li height value is 0
			$ul.css({display:'block',visibility:'hidden'});
			var iSelectHeight = ($('li',$ul).length)*($('li:first',$ul).height());//+1 else bug ff
			(iSelectHeight < $ul.height()) && $ul.css({height:iSelectHeight,'overflow':'hidden'});//hidden else bug with ff
			$ul.css({display:'none',visibility:'visible'});
			
		});
	};
	$.fn.jqTransform = function(options){
		var opt = $.extend({},defaultOptions,options);
		
		if( (opt["setEnabled"] !== undefined) && (typeof opt["setEnabled"] !== "function" ))
		{
			jqTransSelectEnabled(opt["setEnabled"]);
		}
		else
		{
			/* each form */
			 return this.each(function(){
				var selfForm = $(this);
				
				if($(this).is("select"))
				{
					if( $(this).jqTransSelect(opt).length > 0 )
					{
						jqTransformAddDocumentListener();
						selfForm.bind('reset',function()
								{
									var action = function()
									{
										jqTransformReset(this);
									};
									window.setTimeout(action, 10);
								});
					}
				}
				
			}); /* End Form each */
		}
				
	};/* End the Plugin */

})(jQuery);
				   