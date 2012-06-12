function initButtons() {
    log('initButtons');
    $("button").not(".ui-button").wrapInner("<span class='ui-button-text'></span>");
    $("a.button").wrapInner("<span class='ui-button-text'></span>");
    $("button").not(".ui-button").addClass("ui-button ui-widget ui-state-default ui-corner-all ui-button-text-only");
    $("a.button").addClass("ui-button ui-widget ui-state-default ui-corner-all ui-button-text-only");
    $("input").not(".ui-widget-content").addClass("ui-widget-content");
}
