function endsWith(str, suffix) {
    return str.match(suffix + "$") == suffix;
}
function startsWith(str, prefix) {
    return str.indexOf(prefix) === 0;
}

/**
 * ReplaceAll by Fagner Brack (MIT Licensed)
 * Replaces all occurrences of a substring in a string
 */
String.prototype.replaceAll = function(token, newToken, ignoreCase) {
    var str, i = -1, _token;
    if ((str = this.toString()) && typeof token === "string") {
        _token = ignoreCase === true ? token.toLowerCase() : undefined;
        while ((i = (
                _token !== undefined ?
                str.toLowerCase().indexOf(
                _token,
                i >= 0 ? i + newToken.length : 0
                ) : str.indexOf(
                token,
                i >= 0 ? i + newToken.length : 0
                )
                )) !== -1) {
            str = str.substring(0, i)
                    .concat(newToken)
                    .concat(str.substring(i + token.length));
        }
    }
    return str;
};


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
                alert("Error submitting form " + resp);
            }
        });                
    } catch(e) {
        log("exception sending forum comment", e);
    }      
}
            
function ajaxLoadingOff() {
    // todo
}

function getFileName(path) {
    var arr = path.split('/');
    if( arr.length === 1) {
        return "";
    }    
    var name = arr[arr.length - 1];
    if (name == null || name.length == 0) { // might be empty if trailing slash
        name = arr[arr.length - 2];
    }
    if (name.contains("#")) {
        var pos = name.lastIndexOf("#");
        name = name.substring(0, pos);
    }

    path = path.replaceAll(" ", "%20"); // safari bug. path is returned encoded from window.location.pathname
    return name;
}

function getFolderPath(path) {
    var pos = path.lastIndexOf("/");
    return path.substring(0, pos);
}

/**
 * just removed the server portion of the href
 */
function getPathFromHref(href) {
    // eg http://blah.com/a/b -->> /a/b
    var path = href.substring(8); // drop protocol
    var pos = path.indexOf("/");
    path = path.substring(pos);
    return path;
}

