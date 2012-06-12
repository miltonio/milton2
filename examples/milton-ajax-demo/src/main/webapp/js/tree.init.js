// Make sure the tree is showing the requested folder on first load

var urlParts;
var urlPartNum = 0;
var currentInitialPath = "";

// Called when the tree is loaded. Traverse the parts of the user selected
// path and ensure each node is opened
function checkInitialLoad() {
    var initialPath = getFolderPathFromHref();
    urlParts = initialPath.split("/");
    loadNextPart();
}

function loadNextPart() {
    if( urlPartNum >= urlParts.length) {
        loadFolder(currentInitialPath);
        return;
    }
    var nextPart = urlParts[urlPartNum];
    if( nextPart.length == 0 ) {
        loadFolder(currentInitialPath);
        return;
    }
    urlPartNum = urlPartNum + 1;
    currentInitialPath = currentInitialPath + nextPart + "/";
    var nodeId = "#" + toNodeId(currentInitialPath);
    $("#tree").jstree("open_node", nodeId, function() {
        // call back after node opened
        loadNextPart();
    });
}



// // Determine the selected folder from the current window location, by
// looking for an anchor
function getFolderPathFromHref() {
    // Might include anchor like: index.html#Pictures/2004-01-08/
    var s = window.location.href;
    if( s.indexOf("#") >= 0 ) {
        s = s.split("#")[1];
    } else {
        s = "";
    }
    if( s ) {
        return s;
    } else {
        return "";
    }
}

