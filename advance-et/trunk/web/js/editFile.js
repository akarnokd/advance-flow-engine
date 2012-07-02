var xml; //XML file
var nodeEditor; //Node Editor div
var toolbar; //Toolbar div
var separator; //Separator div
var board; //JXG.Board
var minx = 0; //The lower point value on x-axis
var maxx = 0; //The highest point value on x-axis
var activePoint; //Reference to the point that is being modified
var typeOfContent;
var selectedNode;
var treeContainer; //#tree-container div
var JXGPoints = new Array(); //Array containing JXGBoard's points
//Associative array which stores the names of the node attributes
var acceptedAttributes = {
    "label" : 0, 
    "question": 1, 
    "ri": 2,  
    "value-mg": 3
};
var treeContainerInitWidth;
var fileName = undefined;
var doc = undefined;
var pageTitle = '<div class="page-title"><p>Node values editor</p></div>';
var isMobileBrowser = undefined;

$(document).ready(function(){
    
    doc = $(this);
    nodeEditor = $('#node-editor');
    toolbar= $('#node-editor-toolbar');
    separator = $('#separator');
    treeContainer = $('#tree-container');
    treeContainerInitWidth = parseInt(treeContainer.css('width'));
    fileName = $('#content').children('input[name="fileName"]').val();
    populateTree(fileName);
    isMobile();
    
    doc.on('click', 'li div', function(){
        divNode = $(this);
        siblingUl = $(this).siblings('ul');
        if(siblingUl.size() === 1){
            siblingUl.slideToggle(200);
            divNode.hasClass('open') ? divNode.removeClass('open') : divNode.addClass('open');
        }
        return false;
    });
    
    /**
     * Node click handler. Checks if the clicked node isn't the root node 
     * and sets nodeEditor's properly content
     */
    $('.node').live('click', function(event){
        node = $(this);
        
        if(node.attr('id')!=='root-node' && !node.hasClass('selected')){
            storeChanges();
            $('#root .selected').removeClass('selected');
            node.addClass('selected');
            selectedNode = node;
            if(!nodeEditor.prev().is('.page-title'))
                $(pageTitle).insertBefore(nodeEditor);
            nodeEditor.empty().removeClass('padded').height('83%'); //Remove elements from nodeEditor
            if(hasChildrenWithRelativeInfluences(node)){
                //Show jQueryUI's slider
                typeOfContent = 'slider';
                setSlider(node);   
            }else{
                //Show JSXGraph
                typeOfContent = 'graph';
                setGraph(node);
            }
        }
        
        event.preventDefault();
    });
    
    $('button.zoom').live('click', function(){
        manageZoom($(this).attr('id'));
    });
    
    /**
     * When the save button is clicked the values contained into nodeEditor obj
     * where stores into the DOM tree
     */
    $('button#save').live('click', function(){
        
        storeChanges();
        
        var newXml = $('<div></div>'); //Using a regular HTML tag as root element works even with Explorer!
        //var newXml = $('<xml></xml>'); Doesn't work with explorer
        generateXmlTree(newXml, $('#root').children('li'));
        
        //Execute an ajax call to save XML file
        $.ajax({
            url: 'do.saveXml?fileName=' + fileName,
            type: 'post',
            contentType: 'text/xml',
            data: $.parseXML(newXml.html()),
            processData: false,
            error: function(jqXHR, textStatus, errorThrown){
                alert('Si è verificato un\'errore durante il salvataggio dei dati. '
                    + 'In caso l\'errore si ripresenti contatta l\'amministratore.');
            }
        });
    });
    
    separator.live('click', function(){
        
        if(!treeContainer.is(':animated') && !isMobileBrowser){
            
            var treeContainerWidth = treeContainer.outerWidth() === 0 ? treeContainerInitWidth : 0;
            var nodeEditorContainerWidth = nodeEditor.parents('.container').width() +
            (treeContainerWidth === 0 ? treeContainerInitWidth : -treeContainerInitWidth);
            
            treeContainer.animate({
                width: treeContainerWidth
            }, 500);
            
            if(typeOfContent === 'graph'){
                storeChanges();
                $('#jxgbox').css({
                    'display': 'none'
                });
                nodeEditor.empty().css('background', 'url(../images/ajax-loader.gif) #D0D0D0 center no-repeat');
            }
            
            $('#node-editor-container').animate({
                width: nodeEditorContainerWidth
            }, 500, function(){
                if(typeOfContent === 'graph'){
                    nodeEditor.css('background-image', 'none');
                    setGraph(selectedNode);    
                }
            });
        }
        
    });
    
    doc.on('blur', 'p[contenteditable="true"]', function(){
        var paragraph = $(this);
        var container = paragraph.parents('.component');
        var childrenNodes = selectedNode.siblings('ul').children('li');
        if(container.is('#first-component')){
            childrenNodes.eq(0).children('a').text(paragraph.text());   
        }else if(container.is('#second-component')){
            childrenNodes.eq(1).children('a').text(paragraph.text());   
        }
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
            alert('Failed to load tree.\nTry again or check the file.');
        },
        success: function(xmlDoc){
            xml = $(xmlDoc);
            var tree = $('#tree');
            root = xml.children('node');
            
            if(root.size() === 1){
                tree.append('<ul id="root"><li><div></div>' +
                    '<a href="#" class="node" id="root-node" label="' +
                    root.attr('label') + '">' + root.attr('label') + '</a><ul></ul></li></ul>');
                if(root.children('node').size() > 0){
                    appendNodes(root, tree.children('ul').children('li').children('ul'));
                }
            }else{
                alert('Error while parsing tree.\nTry again or check the file.');
            }
            
        }
    });  
}

/**
 * Transform XML tree in an HTML tree
 * @param nodeElement - jQuery object of an XML doc node
 * @param tree - Html where te XML tree will be appended
 */
var appendNodes  = function(nodeElement, tree){
    //    console.log(nodeElement.children('node').size());
    $.each(nodeElement.children('node'), function(index){
        node = $(this);
        var anchorTag = $('<a class="node">' + node.attr('label') + '</a>');
        anchorTag.attr({
            'href': isMobileBrowser ? 'prova.html' : 'prova.html',
            'ri': node.attr('ri'),
            'label': node.attr('label')
        });
        //        console.log('Processing node: ' + node.attr('label'));
        if(node.children('node').size() > 0){
            liTag = $('<li><div></div><ul></ul></li>');
            liTag.children('div').after(anchorTag);
            tree.append(liTag);
            //Call himself
            appendNodes(node, tree.children('li').children('ul').eq(index));
        }else{
            anchorTag.attr({
                'question': node.attr('question'),
                'value-mg': node.attr('value-mg')
            });
            tree.append($('<li></li>').append(anchorTag));
        }
    });
};

/**
 * Check for node's children with R.I. (Relative Influences)
 * @param node - parent node whose children have to be checked
 * @return true if there are 2 children with R.I. otherwise false
 */
var hasChildrenWithRelativeInfluences = function(node){
    var childrenCount = 0;
    
    childNodes = node.siblings('ul').children('li').children('.node');
    if(childNodes.size() > 0){
        $.each(childNodes, function(){
            childRi = $(this).attr('ri');
            if(childRi != undefined){
                ++childrenCount;
            }
        });   
    }
    
    return childrenCount === 2;
}

/**
 * Set a jQuery UI slider object for Relative Influences editing
 * @param node - node whose children have the Relative Influences values
 */
var setSlider = function(node){
    
    if(!nodeEditor.hasClass('padded')) nodeEditor.addClass('padded');
    
    var childrenNodes = node.siblings('ul').children('li');
    var firstChild = childrenNodes.eq(0).children('a');
    var secondChild = childrenNodes.eq(1).children('a');
    componentString = '<div class="component"><div class="value"></div>' 
    + '<p contenteditable="true" spellcheck="false"></p></div>';
    var firstComponent = $(componentString).attr(
        'id', 'first-component');
    var secondComponent = $(componentString).attr(
        'id', 'second-component');
    
    slider = $('<div></div>').attr('id', 'slider');
    slider.slider({
        range: "min",
        value: firstChild.attr('ri') == undefined ? 0 : firstChild.attr('ri'),
        min: 0.00,
        max: 1.00,
        step: 0.01,
        slide: function( event, ui ) {
            firstComponent.children('.value').text(ui.value);
            secondComponent.children('.value').text(Math.round((1.00 - ui.value)*100)/100);
        }
    });
    
    firstComponent.children('.value').text(firstChild.attr('ri'));
    firstComponent.children('p').text(firstChild.attr('label'));
    secondComponent.children('.value').text(secondChild.attr('ri'));
    secondComponent.children('p').text(secondChild.attr('label'));
    
    nodeEditor.append(firstComponent);
    nodeEditor.append('<div class="clear"></div>');
    nodeEditor.append(slider);
    nodeEditor.append('<div class="clear"></div>');
    nodeEditor.append(secondComponent);
    
    setToolbar(typeOfContent);
}

/**
 * Set a JSXGraph -> http://jsxgraph.uni-bayreuth.de/wp/
 * @param node - Dom node without children and with Relative Influences values
 * stored. Usually is a leaf node of xml file
 */
var setGraph = function(node){
    //Clear nodeEditor obj from content and fill with the graph container object
    var jxgDiv = $('<div id="jxgbox" class="jxgbox" style="width: 100%; height: 100%"></div>');
    nodeEditor.append(jxgDiv);
    if(nodeEditor.hasClass('padded')) nodeEditor.removeClass('padded');
    
    /*
     * Thanks to Ben Nadel & Giovanni Dal Maso
     * Create an array of point coordinates from the XML node's values
     */
    var pattern = /\(([-+]?[0-9]*\.?[0-9]+) ([-+]?[0-9]*\.?[0-9]+)\)/g;
    var values = node.attr('value-mg');
    
    var xmlPoints = new Array();
    minx = 0;
    maxx = 0;
        
    while ((matches = pattern.exec(values))){
        var x = Number(matches[1]);
        var y = Number(matches[2]);
        xmlPoints.push(x);
        xmlPoints.push(y);        
    }
    
    //Set minx & maxx
    setXValues(xmlPoints);

    //Create the JXG.Board
    board = JXG.JSXGraph.initBoard('jxgbox', {
        boundingbox: [minx, 1, maxx, 0], 
        axis: true, 
        showCopyright: false, 
        showNavigation: false,
        pan: true,
        zoom: 'wheel'
    });
    board.unsuspendUpdate();
    
    /*
     * Iterate through xmlPoints array, add an JXG.Point to the board for 
     * every couple of coordinates and push it into JXGPoints array
     */
    JXGPoints = new Array();
    for (i=0; i<xmlPoints.length; i += 2) {
        JXGPoints.push(board.create('point', [ xmlPoints[i], xmlPoints[i+1] ], {
            fixed: false, 
            size: 3
        }));
    }
    
    //Add events to every board's point
    for(i in JXGPoints){
        
        JXG.addEvent(JXGPoints[i].rendNode, 'mousedown', function(){
            activePoint = this;
        }, JXGPoints[i]);
        
        JXG.addEvent(JXGPoints[i].rendNode, 'mouseup', function(){
            mouseUpHandler(this);
            setXValues(JXGPoints); 
        }, JXGPoints[i]);
    }
    
    //Create the line between board's points
    for(i = 0; i < JXGPoints.length-1; i++){
        board.create('line', 
            [JXGPoints[i],JXGPoints[i + 1]], 
            {
                strokeColor: '#388d3c',
                straightFirst:false, 
                straightLast:false,
                fixed: true
            });   
    }
    
    //Calls the mouse up handler function on board's mouseup
    board.addHook(function(){
        if(activePoint != undefined){
            mouseUpHandler(activePoint);
        }
    }, 'mouseup');
    
    setToolbar(typeOfContent);
    $('#zoom-100').click();

}

/**
 * Clear the toolbar and fill it with buttons according to the type of content
 * @param typeOfContent - String describing the type of content in the &lt;div 
 * id="node-editor" &gt;. Accepted values are "graph" or "slider"
 */
var setToolbar = function(typeOfContent){
    
    toolbar.empty();
    
    var button = '<button type="button"></button>';
    var buttonContainer = $('<div id ="button-container"></div>');
    buttonContainer.append($(button).attr('id', 'save').text('Save'));
    
    if(typeOfContent === 'graph'){
        buttonContainer.append($(button).attr('id', 'zoom-out').text('Zoom out').addClass('zoom'));
        buttonContainer.append($(button).attr('id', 'zoom-100').text('Zoom 100').addClass('zoom'));
        buttonContainer.append($(button).attr('id', 'zoom-in').text('Zoom in').addClass('zoom'));
    }
    
    toolbar.append(buttonContainer);
}

/**
 * Execute zoom on board
 * Zoom 100 reset the board zoom
 */
var manageZoom = function(buttonId){
    switch(buttonId){
        case 'zoom-out':
            board.zoomOut(minx+(maxx-minx)/2, 0.5);
            break;
        case 'zoom-100':
            //Add a zoom out for a better view
            board.setBoundingBox([minx, 1, maxx, 0], false).zoomOut(minx+(maxx-minx)/2, 0.5);
            break;
        case'zoom-in':
            board.zoomIn(minx+(maxx-minx)/2, 0.5);
            break;
    }
}

/**
 * Find max and min x coordinates in an array containing coordinates values
 * or JXG.Point objects
 */
var setXValues = function(pointsArray){
    if(typeof(pointsArray[0]) == 'object'){
        for(i = 0; i < pointsArray.length - 1; i+=2){
            minx = pointsArray[i].X() < minx ? pointsArray[i].X() : minx;
            maxx = pointsArray[i].X() > maxx ? pointsArray[i].X() : maxx;
        }
    }else{
        for(i = 0; i < pointsArray.length - 1; i+=2){
            minx = pointsArray[i] < minx ? pointsArray[i] : minx;
            maxx = pointsArray[i] > maxx ? pointsArray[i] : maxx;
        }   
    }
}

/**
 * An handler function for mouseup event
 * @param point - JXGPoint object that was being modified before mouseup event
 */
var mouseUpHandler = function(point){
    
    var yPos = Math.round(point.Y()*Math.pow(10, 2))/Math.pow(10, 2);
    var xPos = Math.round(point.X() * 100) / 100;
    
    var minXPos = xPos;
    var pointIndex = findObjInArray(point);
    if(pointIndex > 0){
        minXPos = JXGPoints[pointIndex - 1].X();
        if(minXPos > 0.0 && minXPos < 0.1) minXPos = Math.round(minXPos * 100) / 100;
    }
    //    if(JXGPoints.indexOf(point) > 0) minXPos = Math.round(JXGPoints[JXGPoints.indexOf(point) - 1].X() * 100) / 100;
    var maxXPos = xPos;
    if(pointIndex < JXGPoints.length - 1){
        maxXPos = JXGPoints[pointIndex + 1].X();   
        if(maxXPos > 0.0 && maxXPos < 0.1) maxXPos = Math.round(maxXPos * 100) / 100;
    }
    //    if(JXGPoints.indexOf(point) < JXGPoints.length - 1) maxXPos = Math.round(JXGPoints[JXGPoints.indexOf(point) + 1].X()) * 100 / 100;
   
    var newXPos = 0;
    var newYPos = 0;
            
    //Positions
    if(yPos < 0) newYPos = 0;
    else if(yPos > 1) newYPos = 1;
    else newYPos = yPos;
    if(xPos < minXPos) newXPos = minXPos;
    else if(xPos >maxXPos) newXPos = maxXPos;
    else newXPos = xPos;
            
    point.setPosition(JXG.COORDS_BY_USER, newXPos, newYPos);
        
    activePoint = undefined;
        
    setXValues(JXGPoints);
}

/**
 * Generate an XML node tag, populate it with attributes from a .node HTML tag,
 * and append it to an xmlNode
 * Call its self recursively in case the .node HTML tag has other .node children
 * tags
 * @param xmlNode - xml tag where append the new &lt;node&gt; tag
 * @param liTag - &lt;li&gt; HTML tag that contains the <a class="node"> tag
 */
var generateXmlTree = function(xmlNode, liTag){
    $.each(liTag, function(index){
        var li = $(this);
        var node = li.children('.node');
        var attr = undefined;
        var newNode = $('<node></node>');
        
        for (i=0; i<node.get(0).attributes.length; i++){
            attr = node.get(0).attributes.item(i);
            if(!isNaN(acceptedAttributes[attr.nodeName]))
                newNode.attr(attr.nodeName, attr.nodeValue);   
        }
        
        xmlNode.append(newNode);
        childrenLi = li.children('ul').children('li');
        
        if(childrenLi.size() > 0){
            generateXmlTree(xmlNode.children('node').eq(index), childrenLi);
        }
    });
}

/**
 * Applies the changes done in the node editor to the relative node
 */
var storeChanges = function(){
    //    console.log('Saving changes');
    if(typeOfContent === 'slider'){
        component = nodeEditor.children('.component');
        childrenNode = selectedNode.siblings('ul').children('li');
        $.each(childrenNode, function(index){
            childrenNode.eq(index).children('.node').attr({
                'ri' : Number(component.eq(index).children('.value').text()),
                'label' : String(component.eq(index).children('p').text())
            })
        });
    }else if(typeOfContent === 'graph'){
        var valuemg = '(';
        for(i in JXGPoints){
            valuemg+= '(' + Math.round(JXGPoints[i].X() * 100) / 100 + ' ' + Math.round(JXGPoints[i].Y() * 100) / 100 + ')';
        }
        valuemg += ')';
        selectedNode.attr('value-mg', valuemg);
    }
}

/**
 * Find object in an array
 */
var findObjInArray = function(point){
    var index = -1;
    for(var i=0; i<JXGPoints.length; i++) {
        if (JXGPoints[i] == point){
            index = i;
            break;
        }
    }
    return index;
}

/**
 * Mobile browser sniffer
 * Sniff only android, iphone, nokia, windowsphone
 */
var isMobile = function(){
    var userAgent = navigator.userAgent.toLowerCase();
    
    if(userAgent.indexOf('iphone') != -1){
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