var overlayBox  = undefined;
var uploadBox  = undefined;
var selectedFile = undefined; //File selected
var toolbarForm  = undefined; //Toolbox form object
var uploadForm  = undefined; //Toolbox form object
var timer = undefined; //Timer span
var timeCount = undefined;
var fileListInterval = undefined;
var timerInterval = undefined;
var fileList = undefined;

$(document).ready(function(){
    
    var doc = $(this);
    overlayBox = $('#overlay-box');
    uploadBox = $('#upload-box');
    toolbarForm = $('#toolbar-form');
    uploadForm = $('#upload-form');
    fileList = $('#file-list');
    
    timer = $('#timer');
    timeCount = 15;
    setTimerText();
    
    doc.on('click', 'a.file', function(){
        if(selectedFile !== undefined) selectedFile.removeClass('selected');
        selectedFile = $(this);
        selectedFile.addClass('selected');
        return false;
    });
    
    overlayBox.bind('click', function(){
        if(!overlayBox.is(':animated')){
            fadeOverlayBox(0);
        }
    });
    
    doc.on('keyup', overlayBox, function(event){
        if(event.keyCode === 27){
            if(overlayBox.is(':visible') && !overlayBox.is(':animated')){
                fadeOverlayBox(0);
            }
        }
    });
    
    $('#toolbar-form button').bind('click', function(){
        sniffClickedButton($(this));
    });
    
    $('#close-box-button').bind('click', function(){
        fadeOverlayBox(0);
    });
        
    uploadForm.submit(function(){
        iFrame = $('<iframe name="fake-ajax-iframe" id="fake-ajax-iframe" '
            + 'src="about:blank"></iframe>');
        uploadBox.append(iFrame);
        uploadForm.attr('target', 'fake-ajax-iframe');
        iFrame.load(function(){
            uploadMessage = iFrame.contents().find('body').children('p');
            if(uploadMessage.attr('id') === 'error-message'){
                alert(uploadMessage.text());
            }else if(uploadMessage.attr('id') === 'success-message'){
                $('<li><a href="#" class="file">' + uploadMessage.text() + 
                    '</a></li>').css('display', 'none').appendTo('#file-list').
                slideDown(500);
                /*
                $('#file-list').append('<li><a href="#" class="file">'
                    + uploadMessage.text() + '</a></li>');*/
                uploadBox.find('input[type="file"]').val('');
                if(selectedFile !== undefined){
                    selectedFile.removeClass('selected');
                    selectedFile = undefined;   
                }
                fadeOverlayBox(0);
            }
            iFrame.remove();
        });
    });
    
    fileListInterval = window.setInterval(updateFileList, 16000);
    timerInterval = window.setInterval(updateTimer, 1000);
    
    doc.on('click', '.page-title button', function(){
        clearInterval(timerInterval);
        clearInterval(fileListInterval);
        
        //Call to update list function
        if(updateList()){
            timeCount = 15;
            setTimerText();
            fileListInterval = window.setInterval(updateFileList, 16000);
            timerInterval = window.setInterval(updateTimer, 1000);   
        }
    });
});

/**
 * Fade overlaybox, toolbox and set correct class to selectedFile
 * @param opacity degree for overlayBox
 */
var fadeOverlayBox = function(opacity){
    if(opacity === 0.5){
        overlayBox.fadeTo(500, opacity);
        uploadBox.fadeIn(500);
    }else{
        overlayBox.fadeOut(500);
        uploadBox.fadeOut(500);
    }
}

/**
 * Finds out the clicked button and executes the related form submission
 */
var sniffClickedButton = function(buttonObj){
    
    var buttonId = buttonObj.attr('id');
    switch(buttonId){
        case 'edit-button':
            if(selectedFile != undefined) executeSubmission('editFile.jsp', selectedFile.text(), false);
            break;
        case 'download-button':
            if(selectedFile != undefined) executeSubmission('do.downloadXml', selectedFile.text(), false);
            break;
        case 'view-button':
            if(selectedFile != undefined) executeSubmission('viewFile.jsp', selectedFile.text(), false);
            break;
            break;
        case 'delete-button':
            if(selectedFile != undefined) executeSubmission('do.deleteXml', selectedFile.text(), true);
            break;
        case 'upload-button':
            if(!overlayBox.is(':animated')){
                fadeOverlayBox(0.5);
            }
            break;
    }
        
}

/**
 * Manage the submission according to the given URL
 * @param url - Url for the ajax call [String]
 * @param fileName - The name of the file for whose the call is executed [String]
 * @param isAsyncRequest - Value that specify if the call has to be asynchronous [Boolean]
 * @return true if the call is succesfull, false otherwise
 */
var executeSubmission = function(url, fileName, isAjaxCall){
    var isSuccessfullRequest = false;
    if(url.length > 0 && url !== undefined && typeof isAjaxCall === 'boolean'){
        if(isAjaxCall){
            
            $.ajax({
                url: url + '?fileName=' + fileName,
                type: 'post',
                error: function(jqXHR, textStatus, errorThrown){
                    isSuccessfullRequest = false;
                    alert(jqXHR.responseText);
                //                    alert('An error occurred while deleting the file. Retry '
                //                        + 'or contact the administrator.');
                },
                success: function(){
                    isSuccessfullRequest = true;
                    selectedFile.parents('li').eq(0).slideToggle(500, function(){
                        $(this).remove();
                    });
                }
            });
         
        }else{
            toolbarForm.children('input[name="fileName"]').val(fileName);
            toolbarForm.attr('action', url).submit();
        }
    }
    return isSuccessfullRequest;
}

var updateFileList = function(){
    clearInterval(timerInterval);
    //Call to update list function
    if(updateList()){
        timeCount = 15;
        setTimerText();
        timerInterval = setInterval(updateTimer, 1000);   
    }
}

var updateTimer = function(){
    if(--timeCount < 0) timeCount = 15;
    setTimerText(timeCount);
}

var setTimerText = function(){
    timer/*.children('span')*/.text(timeCount);
}

/**
 * 
 */
var updateList = function(){
    $.ajax({
        url: 'do.getFileList',
        type: 'post',
        dataType: 'xml',
        error: function(jqXHR, textStatus, errorThrown){
            alert('Si è verificato un\'errore');
        },
        success: function(data, textStatus, jqXHR){
            var xml = $(data);
            fileList.empty();
            var liElement = $('<li>');
            var temporaryContainer = $('<div>');
            
            
            $.each(xml.find('file'), function(){
                var file = $(this);
                console.log(file.attr('user'));
                var user = $.trim(file.attr('user'));
                if(user !== undefined && user !== null && user.length > 0){
                    var newElement = $('<div>' + file.attr('filename') + '</div>').append($('<img>').attr('src', '../images/locked.png')).append($('<span>').text(user + ' is editing'));
                }else{
                    var newElement = $('<a>').attr('href', '#').text(file.attr('filename'));
                }
                newElement.addClass('file');
                temporaryContainer.append(liElement.clone().append(newElement));
            });
            fileList.html(temporaryContainer.html());
        }
        
    });
    return true;
}