/**
 * varargs function to output to console.log if console is available
 */
function log() {
    if( typeof(console) != "undefined" ) {
        if (navigator.appName == 'Microsoft Internet Explorer' ) {
            if( typeof(JSON) == "undefined") {
                if( arguments.length == 1 ) {
                    console.log(arguments[0]);
                } else if( arguments.length == 2 ) {
                    console.log(arguments[0], arguments[1]);
                } else if( arguments.length > 2 ) {
                    console.log(arguments[0], arguments[1], arguments[2]);
                }
                
            } else {
                var msg = "";
                for( i=0; i<arguments.length; i++) {
                    msg += JSON.stringify(arguments[i]) + ",";
                }
                console.log(msg);
            }
        } else {
            console.log(arguments);
        }
    }
}

function postForm(form, callback) {
    var serialised = form.serialize();
    form.trigger("preSubmitForm", serialised);
    try {                    
        $.ajax({
            type: 'POST',
            url: form.attr("action"),
            data: serialised,
            dataType: "json",
            success: function(resp) {
                ajaxLoadingOff();                            
                if( resp && resp.status) {
                    log("save success", resp)
                    callback(resp, form)
                } else {
                    log("status indicates failure", resp)
                    try {                                    
                        var messagesContainer = form;
                        if( resp.messages && resp.messages.length > 0 ) {
                            for( i=0; i<resp.messages.length; i++) {
                                var msg = resp.messages[i];
                                messagesContainer.append("<p>" + msg + "</p>");
                            }
                        } else {
                            messagesContainer.append("<p>Sorry, we couldnt process your request</p>");
                        }
                        messagesContainer.show(100);
                        showFieldMessages(resp.fieldMessages, form)
                    } catch(e) {
                        log("ex", e);
                    }
                    alert("Sorry, an error occured and the form could not be processed. Please check for validation messages");
                }                            
            },
            error: function(resp) {
                ajaxLoadingOff();
                log("error posting form", form, resp);
                alert("err " + resp);
            }
        });                
    } catch(e) {
        log("exception sending forum comment", e);
    }      
}
            
function ajaxLoadingOff() {
    // todo
}