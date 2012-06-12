function findType(file) {
    if( file.iscollection ) {
        return "folder";
    } else {
        if( file.contentType ) {
            if( file.contentType.indexOf("image") >= 0) {
                return "image";
            } else if (file.contentType.indexOf("html") >= 0) {
                return "html";
            } else if( file.contentType.indexOf("flv") >= 0 || file.contentType.indexOf("flash") >= 0 ) {
                return "flash";
            } else if( file.contentType.indexOf("video") >= 0 ) {
                return "video";
            } else if( file.contentType.indexOf("audio") >= 0) {
                return "audio";
            } else if( file.contentType.indexOf("directory") >= 0) {
                return "folder";
            } else {
                return getFileTypeByExt(file);
            }
        } else {
            return getFileTypeByExt(file);
        }
    }
}

function getFileTypeByExt(file) {
    var ex = getExt(file.name);
    if( ex == "jpg" || ex == "jpeg" || ex == "png" || ex == "png") {
        return "image";
    } else if( ex == "flv") {
        return "flv";
    } else if( ex == "mp3") {
        return "audio";
    } else if( ex == "html") {
        return "html";
    }
    return ex;
}

function findIcon(file) {
    if( isDisplayableFile(file)) {
        if( file.iscollection ) {
            return "folder.png";
        } else {
            return findIconByExt(file.href);
        }
    } else {
        return "hidden";
    }
}

function findIconByExt(filePath) {
    var ext = getExt(filePath);
    return ext + "_48x48-32.png";
}

function getExt(fileName) {
    var ext = /^.+\.([^.]+)$/.exec(fileName);
    ext = (ext == null) ? "" : ext[1];
    log('getExt', fileName, ext);
    return ext.toLowerCase();
}

function getFileName(path) {
    var arr = path.split('/');
    var name = arr[arr.length-1];
    if( name.length == 0 ) { // might be empty if trailing slash
        return arr[arr.length-2];
    } else {
        return name;
    }
}

function isDisplayableFile(file) {
    if( file.href ) {
        return isDisplayableFileHref(file.href);
    } else {
        if( file.targetHref ) { // for recent files
            return isDisplayableFileHref(file.targetHref);
        } else {
            return false;
        }
    }
}

// These file names either have particular meaning to shmego, or they are common
// garbage files.
function isDisplayableFileHref(href) {
    if( href == 'Thumbs.db' ) return false;
    if( endsWith(href, '/regs/') ) return false;
    if( endsWith(href, '.MOI') ) return false;
    if( endsWith(href, '.THM') ) return false;
    var name = getFileName(href);
    if( startsWith(name, "_sys_")) return false;
    return true;
}

function getContextMenuItems(item) {
    log("getContextMenuItems", item.allowedTemplateNames);
    var arr = {};
    arr["delete"] = {
        label : "Delete",
        "action" : function (node) {
            log("action clicked", name, node.data('jstree'), node.data('jstree').href);
            confirmDelete(node.data('jstree').href, node.data('jstree').name, new function() {
                //log('callback to refresh');
                //$("#tree").jstree( "refresh", node);
                // need to get parent
                });
        }
    };
    arr["refresh"] = {
        label : "Refresh",
        "action" : function (node) {
            log('refresh');
            $("#tree").jstree( "refresh", node);
        //$("#tree").jstree( "refresh" );
        //node.jstree("refresh");
        }
    };
    if(item.allowedTemplateNames) {
        arr["Add file"] = {
            label : "Add...",
            "action" : function() {
                doAddFile(item);
            }
        };

    //        for( i=0; i<item.allowedTemplateNames.length; i++) {
    //            var label = "addPage";
    //            var name = item.allowedTemplateNames[i];
    //            var url = currentFolderUrl + "_autoname.new?templateSelect=" + name;
    //        }
    }
    log('array', arr);
    return arr;
}

function doAddFile(item) {
    //alert('doAdd: ' + item.allowedTemplateNames);
    
    ul = $("ul.templates", $("#addPageModal"));
    ul.html("");
    for( i=0; i<item.allowedTemplateNames.length; i++) {
        var name = item.allowedTemplateNames[i];
        var url = currentFolderUrl + "_autoname.new?templateSelect=" + name;
        li = $("<li>");
        li.html("<a href='#' onclick=\"showNewPage('" + name + "')\">" + name + "</a>");
        ul.append(li);        
    }
    $("#addPageModal").dialog({
        modal: true,
        width: "400px",
        resizable: false,
        dialogClass: "noTitle"
    });
}

function showNewPage(templateName) {
    var url = currentFolderUrl + "_autoname.new?templateSelect=" + templateName;
    loadIframe(url);
    $( "#tabs" ).tabs('select', 1);
    $("#addPageModal").dialog("close");
}


