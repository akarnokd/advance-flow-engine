var tree; //tree div
var elemWidth; //width of div node
var isMobileBrowser; //flag for mobile browsers
var wrapper = undefined; //wrapper div
var maxElementsNumber = undefined;
var elemMarginLeft = undefined; //Left margin for node div

$(function(){
    tree = $('#tree');
    wrapper = $('#wrapper');
    isMobile();
    
    populateTree(tree.children('input[name="filename"]').val());
    
    $(document).on('treePopulated',function(){
        elemWidth = $('.node').first().outerWidth();
        maxElementsNumber = Math.floor(tree.width() / elemWidth);
        setWrapperWidth(tree.children('#root').nextUntil('.clear'), 
            tree.children('#root').nextAll('.clear').first());
    });
    
});

/**
 * Get an xml file via a get request, parse it, and populate &lt;div id="tree"&gt;
 * @param fileName - the name of the file to be parsed
 */
var populateTree = function(fileName){
    $.ajax({
        url: 'do.getXml?fileName=' + fileName,
        type: 'POST',
        dataType: 'xml',
        error: function(jqXHR, textStatus, errorThrown){
            alert(errorThrown);
            alert('Failed to load tree.\nTry again or check the file.');
        },
        success: function(xmlDoc){
            xml = $(xmlDoc);
            root = xml.children('node');
            
            if(root.size() === 1){
                rootDiv = $('<div class=node id="root">' + root.attr('label') + '</div>');
                tree.append(rootDiv);
                elemWidth = rootDiv.outerWidth();
                if(root.children('node').size() > 0){
                    appendNodes(root.children('node'), tree, 0);
                    tree.trigger('treePopulated');
                }
            }else{
                alert('Error while parsing tree.\nTry again or check the file.');
            }
            
        }
    }); 
}

/**
 * Transform XML tree in an HTML tree
 * @param elements - collection of elements to be appended
 * @param tree - Html tag where the elements will be appended
 */
var appendNodes = function(elements, tree, divCount){
        
    var elChildren = new Array();
    /*
    if(tree.width() / elements.length < elemWidth){
        $('#wrapper').css('max-width', $('#wrapper').width() + ((elemWidth - (tree.width() / elements.length)) * elements.length) + 10 * elements.length);
    }
    */
    //    var elemMarginLeft = Math.floor((tree.width() - (elemWidth * elements.length)) / (elements.length));
    $.each(elements, function(index){
        var node = $(this);
        var htmlElId = 'div-' + divCount++;
        var htmlEl = $('<div class="node" id="' + htmlElId + '"></div>');
        
        //        var elemMarginLeftCurr = elemMarginLeft;
        //        if(index == 0) elemMarginLeftCurr = elemMarginLeft / 2;
        
        //htmlEl.css('margin-left', elemMarginLeftCurr);
        htmlEl.data('parent', node.data('parent'));
        tree.append(htmlEl.text(node.attr('label')));
        if(node.children('node').size() > 0){
            $.each(node.children('node'), function(){
                child = $(this);
                elChildren.push(child.data('parent', htmlElId)); 
            });
        }
    });
    
    tree.append('<div class="clear"></div>');
    
    if(elChildren.length > 0){
        appendNodes(elChildren, tree, divCount);
    }
}

var connectNodes = function(){
    $.each($('#tree').find('.node'), function(){
        var node = $(this);
        if(node.attr('id') !== 'root'){
            var src = node.data('parent') == undefined ? 'root' : node.data('parent');
            jsPlumb.connect(
            {
                source: src,
                target: node.attr('id'),
                anchors:["BottomCenter", "TopCenter"],
                endpoint: 'Blank',
                connector: 'Flowchart'
            });
        }
    });
}

/**
 * Mobile browser sniffer
 * Sniff only android, iphone, nokia, windowsphone
 */
var isMobile = function(){
    var userAgent = navigator.userAgent.toLowerCase();
    
    if(userAgent.indexOf('iphone') != -1){
        isMobileBrowser = true;
    }else if(userAgent.indexOf('ipad') != -1){
        isMobileBrowser = true;
    }else if(userAgent.indexOf('android') != -1){
        isMobileBrowser = true;
    }else if(userAgent.indexOf('opera mini') != -1){
        isMobileBrowser = true;
    }else if(userAgent.indexOf('opera mobi') != -1){
        isMobileBrowser = true;
    }else if(userAgent.indexOf('blackberry') != -1){
        isMobileBrowser = true;
    }else if(userAgent.indexOf('windows phone') != -1){
        isMobileBrowser = true;
    }else{
        isMobileBrowser = false;
    }
 
}

/**
 * Check tree layers and set wrapper width properly to contains them
 * @param elements - collection of node elements
 */
var setWrapperWidth = function(elements, boundNode){
    if(elements.length > maxElementsNumber){
        //Increment wrapper's width to be able to contain elements
        //(adds 10px of space between every )
        if(isMobileBrowser){
            var htmlEl = $('html');
            htmlEl.width(htmlEl.width() + 
                ((elemWidth - (tree.width() / elements.length)) * elements.length) 
                + 10 * (elements.length + 1));  
        }else{
            wrapper.width(wrapper.width() + 
                ((elemWidth - (tree.width() / elements.length)) * elements.length) 
                + 10 * (elements.length + 1));
        }
    }
    
    nextElements = boundNode.nextUntil('.clear', '.node');
    if(nextElements.size() > 0){
        setWrapperWidth(nextElements, boundNode.nextAll('.clear').first());
    }else{
        placeElements(tree.children('#root').nextUntil('.clear'), 
            tree.children('#root').nextAll('.clear').first());
    }
}

/**
 * 
 */
var placeElements = function(elements, boundNode){
    elemMarginLeft = Math.floor((tree.width() - (elemWidth * elements.length)) / (elements.length));
    
    $.each(elements, function(index){
        var elemMarginLeftCurr = elemMarginLeft;
        if(index == 0) elemMarginLeftCurr = elemMarginLeft / 2;
        $(this).css('margin-left', elemMarginLeftCurr);        
    });
    
    nextElements = boundNode.nextUntil('.clear', '.node');
    if(nextElements.size() > 0){
        placeElements(nextElements, boundNode.nextAll('.clear').first());   
    }else{
        $('#content').css('visibility', 'visible');
        setTimeout(500, connectNodes());
    }
}