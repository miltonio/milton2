jQuery(document).ready(function() {
    initFiles();
    $("#myUploaded").mupload({
        url: window.location.pathname,
        buttonText: "Upload a file",
        oncomplete: function(data, name, href) {
            // reload the file list
            log("uploaded ok, now reload file list")
            reloadFileList();
        }
    });
    $(".newFolder").click(function(e) {
        var parentHref = window.location.pathname;
        showCreateFolder(parentHref, "New folder", "Please enter a name for the new folder", function() {
            reloadFileList();
        });
    });
    $(".importFromUrl").click(function() {
        showImportFromUrl();
    });
});

function reloadFileList() {
    $.get(window.location.pathname, "", function(resp) {
        log("got file list", resp);
        var html = $(resp);
        $("#fileList").replaceWith(html.find("#fileList"));
        initFiles();
    });

}

function initFiles() {
    log("initFiles");
    $('.lightbox a.image').each(function(i, n) {
        var href = $(n).attr("href");
        $(n).attr("href", href + "/alt-640-360.png");
    });
    $('a.image').lightBox({
        imageLoading: '/static/images/lightbox-ico-loading.gif',
        imageBtnClose: '/static/images/lightbox-btn-close.gif',
        imageBtnPrev: '/static/images/lightbox-btn-prev.gif',
        imageBtnNext: '/static/images/lightbox-btn-next.gif',
        imageBlank: '/static/images/lightbox-blank.gif',
        containerResizeSpeed: 350
    });
    jQuery("abbr.timeago").timeago();
    jQuery("table.bar .file a").each(function(index, node) {
        tag = $(node);
        var href = tag.attr("href");
        var icon = findIconByExt(href);
        $("img", tag).attr("src", icon);
    });
    $("#fileList tbody").on("click", "a.delete", function(e) {
        e.stopPropagation();
        e.preventDefault();
        var target = $(e.target);
        log("click target", target);
        target = target.closest("tr").find("> td a");
        var href = target.attr("href");
        log("click delete. href", href);
        var name = getFileName(href);
        var tr = target.closest("tr");
        confirmDelete(href, name, function() {
            log("deleted", tr);
            tr.remove();
            alert("Deleted " + name);
        });
    });
    $("#fileList tbody").on("click", "a.rename", function(e) {
        e.stopPropagation();
        e.preventDefault();
        var target = $(e.target);
        var href = target.attr("href");
        promptRename(href, function(resp) {
            window.location.reload();
        });
    });
}

function showCreateFolder(parentHref, title, text, callback, validatorFn) {
    log("showCreateFolder");
    var s = text;
    if (!s) {
        s = "Please enter a name for the new folder";
    }
    alertify.prompt(s, function(newName, form) {
        log("create folder", form);
        var msg = null;
        if (validatorFn) {
            msg = validatorFn(newName);
        }
        if (msg == null) {
            createFolder(newName, parentHref, function() {
                callback(newName);
                closeMyPrompt();
            });
        } else {
            alert(msg);
        }
        return false;
    });
}

function createFolder(name, parentHref, callback) {
    log("createFolder: name=", name, "parentHref=", parentHref);
    var encodedName = name; //$.URLEncode(name);
    //    ajaxLoadingOn();
    var url = "_DAV/MKCOL";
    if (parentHref) {        
        var s = parentHref;
        if (!s.endsWith("/")) {
            s += "/";
        }
        s += url;
        url = s;
    }
    $.ajax({
        type: 'POST',
        url: url,
        data: {
            name: encodedName
        },
        dataType: "json",
        success: function(resp) {
            $("body").trigger("ajaxLoading", {
                loading: false
            });
            if (callback) {
                callback(name, resp);
            }
        },
        error: function() {
            $("body").trigger("ajaxLoading", {
                loading: false
            });
            alert('There was a problem creating the folder');
        }
    });
}

/**
 *  Prompts the user for a new name, and the does a rename (ie move)
 */
function promptRename(sourceHref, callback) {
    log("promptRename", sourceHref);
    var currentName = getFileName(sourceHref);
    var newName = prompt("Please enter a new name for " + currentName, currentName);
    if (newName) {
        newName = newName.trim();
        if (newName.length > 0 && currentName != newName) {
            var currentFolder = getFolderPath(sourceHref);
            var dest = currentFolder;
            if (!dest.endsWith("/")) {
                dest += "/";
            }
            dest += newName;
            move(sourceHref, dest, callback);
        }
    }
}

function move(sourceHref, destHref, callback) {
    //    ajaxLoadingOn();    
    var url = "_DAV/MOVE";
    if (sourceHref) {
        var s = sourceHref;
        log("s", s);
        if (!s.endsWith("/")) {
            s += "/";
        }
        url = s + url;
    }
    log("move", sourceHref, destHref, "url=", url);
    $("body").trigger("ajaxLoading", {
        loading: true
    });
    $.ajax({
        type: 'POST',
        url: url,
        data: {
            destination: destHref
        },
        dataType: "json",
        success: function(resp) {
            $("body").trigger("ajaxLoading", {
                loading: false
            });
            if (callback) {
                callback(resp);
            }
        },
        error: function() {
            $("body").trigger("ajaxLoading", {
                loading: false
            });
            alert('There was a problem creating the folder');
        }
    });
}