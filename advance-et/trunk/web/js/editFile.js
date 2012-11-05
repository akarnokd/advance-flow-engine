var xml; //XML file
var nodeEditor; //Node Editor div
var nodeEditorToolbar; //Toolbar div
var separator; //Separator div
var board; //JXG.Board
var minx = 0; //The lower point value on x-axis
var maxx = 0; //The highest point value on x-axis
var activePoint; //Reference to the point that is being modified
var typeOfContent;
var typeOfGraph;
var selectedNode;
var treeContainer; //#tree-container div
var JXGPoints = new Array(); //Array containing JXGBoard's points
var JXGLines = new Array(); //Array containing JXGBoard's points
//Associative array which stores the names of the node attributes
var acceptedAttributes = {
    "label" : 0, 
    "question": 1, 
    "ri": 2,  
    "value-mg": 3,
    "values": 4,
    "generic-type": 5
};
var treeContainerInitWidth;
var fileName = undefined;
var doc = undefined;
var pageTitle = '<div class="page-title"><p></p></div>';
var isMobileBrowser = undefined;
var saveButton = undefined; //store save jq button object
var modificationSaved = undefined; //store save jq button object
var confirmDialog = undefined; //Boolean flag to enable modification saving on DOM tree
var backForm = undefined;
var tabs = undefined;

$(document).ready(function(){
    
    doc = $(this);
    nodeEditor = $('#node-editor');
    nodeEditorToolbar= $('#node-editor-toolbar');
    separator = $('#separator');
    treeContainer = $('#tree-container');
    treeContainerInitWidth = parseInt(treeContainer.css('width'));
    fileName = $('#content').children('input[name="fileName"]').val();
    saveButton = $('#save-button');
    confirmDialog = $('#dialog-confirm');
    backForm = treeContainer.find('form');
    
    //Global boolean flags
    modificationSaved = false;
    
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
            
            if(areValuesModified()){
                if(typeOfContent === 'graph'){
                    if(confirm('Do you want to record MI?')){
                        $('#node-editor-toolbar button.record-values').click();
                    }
                }else if(typeOfContent === 'slider'){
                    if(confirm('Do you want to record RI?')){
                        $('#node-editor-toolbar button.record-values').click();
                    }
                }
            }else{
                if(isValidContent()){
                
                    if(typeOfGraph == 2 && areTabsUnchecked()){
                        return false;
                    }else{
                        switchNode();
                    }
                    
                }
            }
            
        }
        
        event.preventDefault();
    });
    
    $('button.zoom').live('click', function(){
        manageZoom($(this).attr('id'));
    });
    
    /**
     * When the save button is clicked the values contained into nodeEditor obj
     */
    doc.on('click','button#save-button', function(){
        saveXml(false, false);
    });
    
    /**
     * Handler for click event on RECORD RI & RECORD MG
     */
    doc.on('click', 'button.record-values', function(){
        if(isValidContent()){
            if(storeChanges()){
                if(typeOfGraph == 2 && areTabsUnchecked()){
                    return false;
                }else{
                    switchNode();
                    enableSaveButton();   
                }
            }
        }
    });
    
    /**
     * Handler for click event on the submit button
     * BACK BUTTON
     */
    treeContainer.on('click', 'button[type="submit"]', function(e){
        if(saveButton.attr('disabled') != 'disabled'){
            confirmDialog.dialog('open');
            e.preventDefault();
        }
    });
    
    /**
     * Handler for click event on reload button
     */
    treeContainer.on('click', 'button#reload-button', function(e){
        console.log(saveButton);
        if(saveButton.attr('disabled') != 'disabled'){
            if(confirm('Reloading the tree without saving the changes executed. Are you sure?')){
                populateTree(fileName);    
            }
        }else{
            populateTree(fileName);
        }
    });
    
    /**
     * Handler for click event on separator div
     */
    separator.on('click', function(){
        
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
    
    /**
     * Handler for click event tabs-toolbar button
     */
    nodeEditor.on('click', '#tabs-toolbar button', function(){
        var clickedButton = $(this);
        if(!clickedButton.hasClass('selected') && isValidContent()){
            if(areValuesModified()){    
                if(confirm('Do you want to record MG ?')){
                    storeChanges();
                }
            }
            var selectedIndex = Number(clickedButton.attr('id').substring(clickedButton.attr('id').indexOf('tab-button-') + 11));
            tabs[selectedIndex].checked = true;
            $('#jxgbox').remove();
            setGraph(tabs[selectedIndex]);
            $('#tabs-toolbar .selected').removeClass('selected');
            clickedButton.addClass('selected');
        }
    });
    
    //JQUERY-UI-DIALOG -- JQUERY-UI-DIALOG -- JQUERY-UI-DIALOG
    confirmDialog.dialog({
        autoOpen: false,
        resizable: false,
        modal: true,
        buttons: {
            "Save": function() {
                saveXml(true, false);
            },
            "Don\'t Save": function() {
                backForm.submit();
            },
            Cancel: function() {
                $( this ).dialog( "close" );
            }
        },
        width: '40%'
    });
    
});//DOCUMENT READY -- DOCUMENT READY -- DOCUMENT READY -- DOCUMENT READY

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
                
                typeOfContent = undefined;
                typeOfGraph = undefined;
                modificationSaved = false;
                saveButton.attr('disabled', 'disabled');
                nodeEditor.empty();
                nodeEditorToolbar.empty();
                tree.empty().append('<ul id="root"><li><div></div>' +
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
    $.each(nodeElement.children('node'), function(index){
        node = $(this);
        var anchorTag = $('<a class="node">' + node.attr('label') + '</a>');
        anchorTag.attr({
            //            'href': isMobileBrowser ? 'prova.html' : 'prova.html',
            'ri': node.attr('ri'),
            'label': node.attr('label')
        });
        if(node.children('node').size() > 0){
            liTag = $('<li><div></div><ul></ul></li>');
            liTag.children('div').after(anchorTag);
            tree.append(liTag);
            //Call himself
            appendNodes(node, liTag.children('ul'));
        }else{
            anchorTag.attr({
                'question': node.attr('question'),
                'values': node.attr('values'),
                'generic-type': node.attr('generic-type'),
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
    
    //    return childrenCount === 2;
    return childrenCount > 0;
}

/**
 * Set a jQuery UI slider object for Relative Influences editing
 * @param node - node whose children have the Relative Influences values
 */
var setSlider = function(node){
    
    var childNodes = node.siblings('ul').children('li').children('a');
    var componentString = '<div class="component"><div class="value"></div>' 
    + '<p contenteditable="true" spellcheck="false"></p></div>';
    var sliderTotValue = 0; 
    var oldSliderValue = 0;
    
    if(!nodeEditor.hasClass('padded')) nodeEditor.addClass('padded');
    
    $.each(childNodes, function(index){
        var component = $(componentString);
        component.children('.value').text(childNodes.eq(index).attr('ri'));
        component.children('p').text(childNodes.eq(index).attr('label'));
        nodeEditor.append(component);
        
        slider = $('<div></div>').attr('class', 'slider');
        slider.slider({
            range: "min",
            value: childNodes.eq(index).attr('ri') == undefined ? 0 : childNodes.eq(index).attr('ri'),
            min: 0.00,
            max: 1.00,
            step: 0.01,
            start: function( event, ui ) {
                oldSliderValue = ui.value;
            },
            stop: function( event, ui ) {
                sliderTotValue = sliderTotValue - oldSliderValue + $(this).slider('value');
                var sliders = nodeEditor.children('.slider');
                $.each(sliders, function(){
                    $(this).prev('.component').children('div').text(
                        Math.round($(this).slider('value') / sliderTotValue * 100) / 100);
                });
                oldSliderValue = 0;
                
            }
        });
        
        sliderTotValue+= slider.slider('value');
        nodeEditor.append(slider);
        nodeEditor.append($('<div class="clear"></div>'));
    });
    
    setToolbar(typeOfContent);
}

/**
 * Set a JSXGraph -> http://jsxgraph.uni-bayreuth.de/wp/
 * @param node - Dom node without children and with Relative Influences values
 * @param isMul - boolean var that indicat whether the node has multiple graph
 * stored. Usually is a leaf node of xml file
 */
var setGraph = function(nodeObj){
    
    //Clear nodeEditor obj from content and fill with the graph container object
    var jxgDiv = $('<div id="jxgbox" class="jxgbox"></div>');
    jxgDiv.addClass(nodeObj.graphClass);
    nodeEditor.append(jxgDiv);
    if(nodeEditor.hasClass('padded')) nodeEditor.removeClass('padded');
    
    minx = 0;
    maxx = 0;
    
    //Set minx & maxx
    setXValues(nodeObj.coordinates);

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
    for (i=0; i<nodeObj.coordinates.length; i += 2) {
        JXGPoints.push(board.create('point', [ nodeObj.coordinates[i], nodeObj.coordinates[i+1] ], {
            fixed: false, 
            size: 3,
            withLabel: false
        }));
    }
    
    //Add events to every board's point
    for(i in JXGPoints){
        
        JXG.addEvent(JXGPoints[i].rendNode, 'mousedown', function(){
            activePoint = this;
        }, JXGPoints[i]);
        
    }
    
    //Create the line between board's points
    for(i = 0; i < JXGPoints.length-1; i++){
        JXGLines.push(board.create('line', 
            [JXGPoints[i],JXGPoints[i + 1]], 
            {
                strokeColor: '#388d3c',
                straightFirst:false, 
                straightLast:false,
                fixed: true
            }));   
    }
    
    //Calls the mouse up handler function on board's mouseup
    board.addHook(function(e){
        if(activePoint != undefined){
            mouseUpHandler(e.which);
        }
    }, 'mouseup');
    
    //Add double-click event
    JXG.addEvent(jxgDiv.get(0), 'dblclick', function (e) {
        var coords = new JXG.Coords(JXG.COORDS_BY_SCREEN, board.getMousePosition(e), board);
        addPointToBoard(board, coords.usrCoords.slice(1));
    }, this);
    
    setToolbar(typeOfContent);
    $('#zoom-100').click();

}

/**
 * Clear the toolbar and fill it with buttons according to the type of content
 * @param typeOfContent - String describing the type of content in the &lt;div 
 * id="node-editor" &gt;. Accepted values are "graph" or "slider"
 */
var setToolbar = function(typeOfContent){
    
    nodeEditorToolbar.empty();
    
    var button = '<button type="button"></button>';
    var buttonContainer = $('<div id ="button-container"></div>');
    
    if(typeOfContent === 'graph'){
        if(typeOfGraph === 1 || typeOfGraph === 2){
            buttonContainer.append($(button).attr({
                'id': 'record-mg',
                'class': 'record-values'
            }).text('Record MG'));
            buttonContainer.append($(button).attr('id', 'zoom-out').text('Zoom out').addClass('zoom'));
            buttonContainer.append($(button).attr('id', 'zoom-100').text('Zoom 100').addClass('zoom'));
            buttonContainer.append($(button).attr('id', 'zoom-in').text('Zoom in').addClass('zoom'));
        }else if(typeOfGraph === 3){
            buttonContainer.append($(button).attr({
                'id': 'record-ri',
                'class': 'record-values'
            }).text('Record RI'));    
        }
    }else if(typeOfContent === 'slider'){
        buttonContainer.append($(button).attr({
            'id': 'record-ri',
            'class': 'record-values'
        }).text('Record RI'));
    }
    
    nodeEditorToolbar.append(buttonContainer);
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
 * Handler function for mouseup event
 * According to the mouse button that launched mouseup event set the correct position
 * for the point or remove it.
 * Set the correct positions for board's points and call setXvalues to update
 * the minimum and the maximum x-axis values for the point on the board
 * 
 * @param mouseButton - number to identificate the button of the mouse that was clicked
 */
var mouseUpHandler = function(mouseButton){

    if(mouseButton < 3){
        
        //Get coordinates of the active point
        var yPos = Math.round(activePoint.Y()*Math.pow(10, 2))/Math.pow(10, 2);
        var xPos = Math.round(activePoint.X() * 100) / 100;
        
        //Get the index of the point in the graph
        var pointIndex = getPointIndex(activePoint);
        
        //Set the minimum accepted value on x-axis
        var minXPos = xPos;
        if(pointIndex > 0){
            minXPos = JXGPoints[pointIndex - 1].X();
            if(minXPos > 0.0 && minXPos < 0.1) minXPos = Math.round(minXPos * 100) / 100;
        }
        
        //    if(JXGPoints.indexOf(point) > 0) minXPos = Math.round(JXGPoints[JXGPoints.indexOf(point) - 1].X() * 100) / 100;
        
        //Set the maximum accepted value on x-axis
        var maxXPos = xPos;
        if(pointIndex < JXGPoints.length - 1){
            maxXPos = JXGPoints[pointIndex + 1].X();   
            if(maxXPos > 0.0 && maxXPos < 0.1) maxXPos = Math.round(maxXPos * 100) / 100;
        }
        //    if(JXGPoints.indexOf(point) < JXGPoints.length - 1) maxXPos = Math.round(JXGPoints[JXGPoints.indexOf(point) + 1].X()) * 100 / 100;
   
        var newXPos = 0;
        var newYPos = 0;
            
        /* Calculate new point coordinates considering the maximum and the minimum
         * accepted values both graph's axis */
        if(yPos < 0) newYPos = 0;
        else if(yPos > 1) newYPos = 1;
        else newYPos = yPos;
        if(xPos < minXPos) newXPos = minXPos;
        else if(xPos >maxXPos) newXPos = maxXPos;
        else newXPos = xPos;
          
        activePoint.setPosition(JXG.COORDS_BY_USER, [newXPos, newYPos]);
        
    }else{
        removePointFromBoard();
    }
    
    activePoint = undefined;
        
    setXValues(JXGPoints);
}

/**
 * Update the lines on the board
 * Erase the old lines on the board an draw new lines between the points
 */
var drawLinesBetweenPoints = function(){
    
    $.each(JXGLines, function(i){
        board.removeObject(JXGLines[i]);
    });
    
    for(i = 0; i < JXGPoints.length-1; i++){
        JXGLines.push(board.create('line', 
            [JXGPoints[i],JXGPoints[i + 1]], 
            {
                strokeColor: '#388d3c',
                straightFirst:false, 
                straightLast:false,
                fixed: true
            }));   
    }
}

/**
 * Add a new point to the board and push the relative object 
 * into the array of JXGPoints
 * @param board - reference to JXGBoard object
 * @param newPointCoords - new point coordinates relative to the graph
 */
var addPointToBoard = function(board, newPointCoords){
    if(newPointCoords[1] >= 0 && newPointCoords[1] <=1){
        
        for(i in JXGPoints){
            if(JXGPoints[i].X() == newPointCoords[0]) newPointCoords[0]+= 0.1;   
            if(JXGPoints[i].X() > newPointCoords[0]){
                JXGPoints.splice(i,0,
                    board.create('point', newPointCoords,{
                        fixed: false, 
                        size: 3,
                        withLabel: false
                    }));
                JXG.addEvent(JXGPoints[i].rendNode, 'mousedown', function(){
                    activePoint = this;
                }, JXGPoints[i]);
            
                break;
            }
        }
    
        drawLinesBetweenPoints();
    }
}

/**
 * Remove the active point from the board
 */
var removePointFromBoard = function(){
    board.removeObject(activePoint);
    JXGPoints.splice(getPointIndex(activePoint), 1);
    
    drawLinesBetweenPoints();
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
 * Check whether a points on the graph has value equal to 1 on the y-axis
 * @return true if the typeOfContent is the slider or if the graph contains at
 * least 1 point with value equal to 1 on the y-axis
 */
var isValidContent = function(){
    var isValid = false;
    var message = 'Data cannot be saved.';
    if(typeOfContent === 'graph' && typeOfGraph > 0 && typeOfGraph < 3){
        var isValidMaxY = false;
        var isValidMinY = true;
        var minY = false;
        var maxY = false;
        $.each(JXGPoints, function(index){
            var yAxisValue = JXGPoints[index].Y();
            console.log('Point ' + (index + 1) + ': ' + yAxisValue);
            if(yAxisValue === 1){
                isValidMaxY = true;
                maxY = true;
            }else if(yAxisValue === 0){
                minY = true;
            }else if(yAxisValue > 1){
                isValidMaxY = false;
                message+= '\nPoint ' + (index + 1) + ' value on the y-axis is greater than 1.'
                + '\nThe maximum accepted values on the y-axis is 1.';
                return false;
            }else if(yAxisValue < 0){
                isValidMinY = false;
                message+= '\nPoint ' + (index + 1) + ' value on the y-axis is less than 0.'
                + '\nThe minimum accepted values on the y-axis is 0.';
                return false;
            }
        });
        
        isValid = isValidMaxY && isValidMinY && maxY;
        
        if(!maxY){
            message+= '\nAt least one point must have an y-axis value equal to 1.';
        }
        
        if(!isValid){
            alert(message);    
        }else{
            if(!minY){
                alert('WARNING! At least one point should have an y-axis value equal to 0.');
            }
        }
        
    }else isValid = true;
    
    return isValid;
}

/**
 * Save the xml on the server
 * 
 * @param submitBackForm - boolean to indicate whether submit back form after the ajax call or not
 */
var saveXml = function(submitBackForm, callPopulateTree){
    var newXml = $('<div></div>'); //Using a regular HTML tag as root element works even with Explorer!
    //var newXml = $('<xml></xml>'); Doesn't work with explorer
    generateXmlTree(newXml, $('#root').children('li'));
    saveButton.attr('disabled', 'disabled');
        
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
        },
        success: function(data, textStatus, jqXHR){
            saveButton.attr('disabled', 'disabled');
            if(submitBackForm) backForm.submit();
            if(callPopulateTree) populateTree(fileName);
        }
    });
}

/**
 * Applies the changes done in the node editor to the relative node (only in the HTML tree)
 * If the type of content is 'slider' iterate through sliders and assign their value to
 * the relative node in the tree editor
 * If the type of content is 'graph' assign the value of the point to the value-mg attribute
 * of the relative node in the tree editor
 */
var storeChanges = function(){
    
    var isStoringSuccessfull = false;
    
    if(typeOfContent === 'slider'){
        var component = nodeEditor.children('.component');
        var childrenNode = selectedNode.siblings('ul').children('li');
        $.each(childrenNode, function(index){
            childrenNode.eq(index).children('.node').attr({
                'ri' : Number(component.eq(index).children('.value').text()),
                'label' : String(component.eq(index).children('p').text())
            });
        });
        isStoringSuccessfull = true;
    }else if(typeOfContent === 'graph'){
        var valueMG = '';
        if(typeOfGraph == 1 || typeOfGraph == 3){
            selectedNode.attr('value-mg', getCurrentValueMG());
            isStoringSuccessfull = true;
        }else{
            var clickedButton = $('#tabs-toolbar button.selected');
            var selectedIndex = Number(clickedButton.attr('id').substring(clickedButton.attr('id').indexOf('tab-button-') + 11));
            valueMG = '(';
            for(i in JXGPoints){
                valueMG+= '(' + Math.round(JXGPoints[i].X() * 100) / 100 + ' ' + Math.round(JXGPoints[i].Y() * 100) / 100 + ')';
            }
            valueMG += ')';
                
            tabs[selectedIndex].coordinates = getCoordinates(valueMG, new Array());
                
            selectedNode.attr('value-mg', getCurrentValueMG());
            isStoringSuccessfull = true;
        }
    }
        
    return isStoringSuccessfull;
    
}

/**
* Find a specific point in the array that contains it
*/
var getPointIndex = function(point){
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
* Remove disable attribute to save button
*/
var enableSaveButton = function(){
    if(saveButton.attr('disabled') !== 'undefined' && saveButton.attr('disabled') !== false){
        saveButton.removeAttr('disabled');
    }
}

/**
* Check whether the values on the container (graph or slider) are different
* from values in the Tree or not.
* In case they are different it store changes
*/
var areValuesModified = function(){
    var areModified = false;
    if(typeOfContent === 'graph'){
        
        //        var areValuesModified = true;
        var treeValueMG = selectedNode.attr('value-mg');
        var nodeEditorValueMG = getCurrentValueMG();
        
        areModified = treeValueMG !== nodeEditorValueMG;
        
    }else if(typeOfContent === 'slider'){
        component = nodeEditor.children('.component');
        childrenNode = selectedNode.siblings('ul').children('li');
        $.each(childrenNode, function(index){
            if((childrenNode.eq(index).children('.node').attr('label') != String(component.eq(index).children('p').text()))
                || (childrenNode.eq(index).children('.node').attr('ri') != Number(component.eq(index).children('.value').text()))){
                areModified = true;
                return !areModified;
            }
        });
    }
    
    return areModified;
}

/**
* SIMPLE mobile browser sniffer
* Set isMobileBrowser global var
* <b>N.B.</b> Sniff only android, iphone, nokia, windowsphone
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

var defineGraphType = function(node){
    var values = node.attr('values');
    var genericType = node.attr('generic-type');
    var valueMG = node.attr('value-mg');
    
    if(values == 'integer'){
        if(genericType == 'g'){
            typeOfGraph = 1; // value-mg="((-20 1)(0 0.2)(5 0))"
        }else if(genericType == 'gd'){
            typeOfGraph = 2; //value-mg="(([tab1] ((-20 1)(0 0.2)(5 0))) (([tab2] ((-20 1)(0 0.2)(5 0)))"
        }
    }else if(values == 'nominal'){
        typeOfGraph = 3; // value-mg="(none 0) (a-few 0.2) (moderate-number 0.3) (quite-a-lot 0.6) (many 0.9) (loads 1))"
    }
    
    switch(typeOfGraph){
        case 1: //SINGLE GRAPH
            var graphObj = new Object();
            graphObj.coordinates = getCoordinates(node.attr('value-mg'), new Array());
            graphObj.graphClass = 'single-graph';
            graphObj.isMultipleGraph = function(){
                return this.graphClass == 'multiple-graph';
            }
            setGraph(graphObj);
            break;
        case 2: //TABS
            tabs = new Array();
            var splittedMG = valueMG.split(/\[(.*?)\]/g); //Match every char between square brackets
            for(var i = 1; i < splittedMG.length; i+= 2){
                var tab = new Object();
                tab.name = splittedMG[i];
                tab.coordinates = getCoordinates(splittedMG[i + 1], new Array());
                tab.graphClass = 'multiple-graph';
                tab.isMultipleGraph = function(){
                    return this.graphClass == 'multiple-graph';
                }
                tab.checked = false;
                tabs.push(tab);
            }
            tabs[0].checked = true;
            setGraph(tabs[0]);
            setTabToolbar();
            break;
        case 3: //SLIDERS
            var sliderPattern = /\((.*?)\)/g;
            var sliderData = new Array();
            
            var componentString = '<div class="component"><div class="value"></div>' 
            + '<p contenteditable="false" spellcheck="false"></p></div>';
            //            var sliderTotValue = 0; 
            //            var oldSliderValue = 0;
            
            while((sliderData = sliderPattern.exec(valueMG))){
                var currSliderData = sliderData[1].split(' ');
             
                var component = $(componentString);
                component.children('.value').text(Number(currSliderData[1]));
                component.children('p').text(currSliderData[0].replace('(', '').replace(')', ''));
                if(!nodeEditor.hasClass('padded')) nodeEditor.addClass('padded');
                nodeEditor.append(component);
        
                slider = $('<div></div>').attr('class', 'slider');
                slider.slider({
                    range: "min",
                    value: Number(currSliderData[1]),
                    min: 0.00,
                    max: 1.00,
                    step: 0.01,
                    start: function( event, ui ) {
                    /*
                        oldSliderValue = ui.value;
                     */
                    },
                    stop: function( event, ui ) {
                        /*
                        sliderTotValue = sliderTotValue - oldSliderValue + $(this).slider('value');
                        var sliders = nodeEditor.children('.slider');
                        $.each(sliders, function(){
                            $(this).prev('.component').children('div').text(
                                Math.round($(this).slider('value') / sliderTotValue * 100) / 100);
                        });
                        oldSliderValue = 0;
                     */
                        var slider = $(this);
                        slider.prev('.component').children('div').text(slider.slider('value'))
                
                    }
                });
                
                nodeEditor.append(slider);
                nodeEditor.append($('<div class="clear"></div>'));
                
            }
            setToolbar(typeOfContent);
            break;
    }
}

/**
* 
*/
var getCoordinates = function(valueMG, coordinatesArray){
    /*
 * Thanks to Ben Nadel & Giovanni Dal Maso
 * Create an array of point coordinates from the XML node's values
 */
    var pattern = /\(([-+]?[0-9]*\.?[0-9]+) ([-+]?[0-9]*\.?[0-9]+)\)/g;
    while ((matches = pattern.exec(valueMG))){
        var x = Number(matches[1]);
        var y = Number(matches[2]);
        coordinatesArray.push(x);
        coordinatesArray.push(y);        
    }
    return coordinatesArray;
}

/**
* 
*/
var setTabToolbar = function(){
    var tabToolbarDiv = $('<div class="toolbar" id="tabs-toolbar">');
    var button = $('<button>');
    for(var i in tabs){
        var currButton = button.clone();
        currButton.text('Graph ' + (Number(i) + 1));
        if(i == 0){
            currButton.addClass('selected');
        }
        currButton.attr('id', 'tab-button-' + i);
        tabToolbarDiv.append(currButton);
    }
    nodeEditor.prepend(tabToolbarDiv);
}

/**
* Retrieve values into the node-editor and construct the current mg-value according
* to the type of content
* 
* @return string - the current value-mg reconstructed
*/
var getCurrentValueMG = function(){
    var valueMG = '(';
    if(typeOfContent == 'slider'){
        
    }else if(typeOfContent == 'graph'){
    
        switch(typeOfGraph){
            case 1:
                for(i in JXGPoints){
                    valueMG+= '(' + Math.round(JXGPoints[i].X() * 100) / 100 + ' ' + Math.round(JXGPoints[i].Y() * 100) / 100 + ')';
                }
                valueMG += ')';
                break;
                selectedNode.attr('value-mg', valueMG);
            case 2:
                var clickedButton = $('#tabs-toolbar button.selected');
                var selectedIndex = Number(clickedButton.attr('id').substring(clickedButton.attr('id').indexOf('tab-button-') + 11));
                
                var valueMG = '(';
                for(var i in tabs){
                    valueMG+= '([' + tabs[i].name + ']';
                    
                    valueMGAttr = '(';
                    if(i == selectedIndex){    
                        for(var j in JXGPoints){
                            valueMGAttr+= '(' + Math.round(JXGPoints[j].X() * 100) / 100 + ' ' + Math.round(JXGPoints[j].Y() * 100) / 100 + ')';
                        }
                    }else{
                        for(var j = 0; j < tabs[i].coordinates.length; ++j){
                            valueMGAttr+= '(' + tabs[i].coordinates[j] + ' ' + tabs[i].coordinates[++j] + ')';
                        }
                    }
                    valueMGAttr += ')';
                    
                    valueMG+= valueMGAttr + ')';
                }
                valueMG += ')';
                
                break;
            case 3:
                var components = nodeEditor.children('.component');
                $.each(components, function(){
                    var component = $(this);
                    valueMG+='(' + $.trim(component.children('p').text()) + ' ' + Number($.trim(component.children('.value').text())) + ') ';
                });
                valueMG = valueMG.substr(0, valueMG.length - 1) + ')';
                break;
        }
        
    }
    
    return valueMG;
}

/**
 * Check for tabs that hasnt'been still checked
 */
var areTabsUnchecked = function(){
    var error = 0;
    var message = '';
    $.each(tabs, function(index){
        if(this.checked == false){
            message+= 'Graph ' + (index + 1) + ' need to be checked.\n';
            ++error;
            return false;
        }
    });
    
    if(error > 0) alert(message + 'Check them all before switch to another node.');
    
    return error > 0;
}

var switchNode = function(){
    $('#root .selected').removeClass('selected');
    node.addClass('selected');
    selectedNode = node;
            
    // Sets the node editor header text and insert it into the DOM
    pageTitle = nodeEditor.prev();
    //                pageTitle.children('p').text(node.attr('label').charAt(0).toUpperCase() + node.attr('label').slice(1, 49));

    nodeEditor.empty().removeClass('padded').height('83%'); //Remove elements from nodeEditor

    if(hasChildrenWithRelativeInfluences(node)){
        //Show jQueryUI's slider
        typeOfContent = 'slider';
        setSlider(node);   
    }else{
        //Show JSXGraph
        typeOfContent = 'graph';
        //                    setGraph(node);
        defineGraphType(node);
    }
}