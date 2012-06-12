/**
 * initFileManPanels
 */
function initFileManPanels() {
    var container = $('.layout');

    function layout() {
        container.layout({
            resize: false,
            type: 'border',
            vgap: 0,
            hgap: 0
        });
    }

    $('.south').resizable({
        handles: 'n',
        stop: layout,
        resize: layout
    });

    $('.west').resizable({
        handles: 'e',
        stop: layout,
        resize: layout

    });

    //getter
    var maxWidth = $( ".west" ).resizable( "option", "maxWidth" );
    //setter
    $( ".west" ).resizable( "option", "maxWidth", 380 );

    //getter
    var maxWidth = $( ".south" ).resizable( "option", "maxHeight" );
    //setter
    $( ".south" ).resizable( "option", "maxHeight", 550 );

    $(window).resize(layout);
    layout();
    layout();
}