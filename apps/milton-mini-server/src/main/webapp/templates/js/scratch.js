$(function() {
    log("init scratch page");
    $(".btn-submit-test").click(function(e) {
        e.preventDefault();
        sendTest();
    });
});
function sendTest() {
    var inputXml = $("#input-xml").val();
    log("input", inputXml);
    $.ajax({
        type: $("#test-method").val(),
        url: $("#test-url").val(),
        cache: false,
//        timeout: configurationURL.timeOut,
        error: function(objAJAXRequest, strError) {
            alert("error");
            return false;
        },
        beforeSend: function(req) {
            // req.setRequestHeader('Authorization', basicAuth(globalLoginUsername, globalLoginPassword));
//            req.setRequestHeader('X-client', globalXClientHeader);
            req.setRequestHeader('Depth', '1');
        },
//        username: (globalSettings.usejqueryauth == true ? globalLoginUsername : null),
//        password: (globalSettings.usejqueryauth == true ? globalLoginPassword : null),
        contentType: 'text/xml; charset=utf-8',
        processData: true,
        data: inputXml,
        dataType: 'xml',
        complete: function(xml, status) {
            log("complete", xml);
            $(".test-status").text(status);
            $(".test-output").text(formatXml(xml.responseText));
        }
    });
}

function formatXml(xml) {
    var reg = /(>)(<)(\/*)/g;
    var wsexp = / *(.*) +\n/g;
    var contexp = /(<.+>)(.+\n)/g;
    xml = xml.replace(reg, '$1\n$2$3').replace(wsexp, '$1\n').replace(contexp, '$1\n$2');
    var pad = 0;
    var formatted = '';
    var lines = xml.split('\n');
    var indent = 0;
    var lastType = 'other';
    // 4 types of tags - single, closing, opening, other (text, doctype, comment) - 4*4 = 16 transitions 
    var transitions = {
        'single->single'    : 0,
        'single->closing'   : -1,
        'single->opening'   : 0,
        'single->other'     : 0,
        'closing->single'   : 0,
        'closing->closing'  : -1,
        'closing->opening'  : 0,
        'closing->other'    : 0,
        'opening->single'   : 1,
        'opening->closing'  : 0, 
        'opening->opening'  : 1,
        'opening->other'    : 1,
        'other->single'     : 0,
        'other->closing'    : -1,
        'other->opening'    : 0,
        'other->other'      : 0
    };

    for (var i=0; i < lines.length; i++) {
        var ln = lines[i];
        var single = Boolean(ln.match(/<.+\/>/)); // is this line a single tag? ex. <br />
        var closing = Boolean(ln.match(/<\/.+>/)); // is this a closing tag? ex. </a>
        var opening = Boolean(ln.match(/<[^!].*>/)); // is this even a tag (that's not <!something>)
        var type = single ? 'single' : closing ? 'closing' : opening ? 'opening' : 'other';
        var fromTo = lastType + '->' + type;
        lastType = type;
        var padding = '';

        indent += transitions[fromTo];
        for (var j = 0; j < indent; j++) {
            padding += '    ';
        }

        formatted += padding + ln + '\n';
    }

    return formatted;
};