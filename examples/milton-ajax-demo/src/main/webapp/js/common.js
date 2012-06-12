function endsWith(str, suffix) {
    return str.match(suffix+"$")==suffix;
}
function startsWith(str, prefix) {
    return str.indexOf(prefix) === 0;
}

/**
 * Adds a contains function to String objects
 */
String.prototype.contains = function(it) {
    return this.indexOf(it) != -1;
};

String.prototype.startsWith = function(prefix) {
    return this.indexOf(prefix) === 0;
};
String.prototype.endsWith = function(suffix) {
    return this.match(suffix+"$")==suffix;
};

function ensureObject(target) {
    if( typeof target == "string") {
        target = $("#" + target);
    }
    return target;
}

$.extend({
    URLEncode:function(c){
        var o='';
        var x=0;
        c=c.toString();
        var r=/(^[a-zA-Z0-9_.]*)/;
        while(x<c.length){
            var m=r.exec(c.substr(x));
            if(m!=null && m.length>1 && m[1]!=''){
                o+=m[1];
                x+=m[1].length;
            }else{
                var d=c.charCodeAt(x);
                var h=d.toString(16);
                o+='%'+(h.length<2?'0':'')+h.toUpperCase();

                //                if(c[x]==' ')o+='+';
                //                else{
                //                    var d=c.charCodeAt(x);
                //                    var h=d.toString(16);
                //                    o+='%'+(h.length<2?'0':'')+h.toUpperCase();
                //                }
                x++;
            }
        }
        return o;
    },
    URLDecode:function(s){
        var o=s;
        var binVal,t;
        var r=/(%[^%]{2})/;
        while((m=r.exec(o))!=null && m.length>1 && m[1]!=''){
            b=parseInt(m[1].substr(1),16);
            t=String.fromCharCode(b);
            o=o.replace(m[1],t);
        }
        return o;
    }
});


/**
 * varargs function to output to console.log if console is available
 */
function log() {
    if( typeof(console) != "undefined" ) {
        console.log(arguments);
    }
}

function pad2(i) {
    var j = i - 0; // force to be a number
    if( j < 10 ) {
        return "0" + j;
    } else {
        return i;
    }
}


function toFileSize(num) {
    if( num > 1000000 ) {
        return Math.round(num/1000000) + 'Mb';
    } else {
        return Math.round(num/1000) + 'Kb';
    }
}

function toDisplayDateNoTime(dt) {
    return (dt.day+1) + "/" + (dt.month+1) + "/" + (dt.year+1900);
}

function doLogout() {
    $.ajax({
        type: 'POST',
        url: "/index.html",
        data: "_clydelogout=true",
        dataType: "text",
        success: function() {
            window.location = "/index.html";
        },
        error: function() {
            alert('There was a problem logging you out');
        }
    });
}


function now() {
    var dt = new Date();
    return {
        day: dt.getDay(),
        month: dt.getMonth(),
        year: dt.getYear()
    };
}



function reverseDateOrd(post1,post2){
    return dateOrd(post1,post2) * -1;
}

function dateOrd(post1,post2){
    var n = post1.date;
    var m = post2.date;

    if( n.year < m.year ) {
        return -1;
    } else if( n.year > m.year ) {
        return 1;
    }
    if( n.month < m.month ) {
        return -1;
    } else if( n.month > m.month ) {
        return 1;
    }
    if( n.day < m.day ) {
        return -1;
    } else if( n.day > m.day ) {
        return 1;
    }
    if( n.hours < m.hours ) {
        return -1;
    } else if( n.hours > m.hours ) {
        return 1;
    }
    if( n.minutes < m.minutes ) {
        return -1;
    } else if( n.minutes> m.minutes ) {
        return 1;
    }
    if( n.seconds < m.seconds ) {
        return -1;
    } else if( n.seconds> m.seconds ) {
        return 1;
    }
    return 0;
}

function isNumber(n) {
    return !isNaN(parseFloat(n)) && isFinite(n);
}

function ajaxLoadingOn(sel) {
    log('ajax ON', sel);
    $("#ajaxLoading").dialog({
        modal: true,
        width: "400px",
        resizable: false,
        dialogClass: "noTitle"
    });
}

function ajaxLoadingOff(sel) {
    log('ajax OFF', sel);
    $("#ajaxLoading").dialog('close');
}

/** Displays a modal with a title and message
 */
function showThankyou(title, message) {
    log("showThankyou");
    $(".modal").dialog("close");
    $('#thankyou h3').html(title);
    $('#thankyou p').html(message);
    $('#thankyou').dialog({
        modal: true,
        width: 500
    });
}

/**
 * DHTML date validation script. Courtesy of SmartWebby.com (http://www.smartwebby.com/dhtml/)
 */
// Declaring valid date character, minimum year and maximum year
var dtCh= "/";
var minYear=1900;
var maxYear=2100;

function isInteger(s){
    var i;
    for (i = 0; i < s.length; i++){
        // Check that current character is number.
        var c = s.charAt(i);
        if (((c < "0") || (c > "9"))) return false;
    }
    // All characters are numbers.
    return true;
}

function stripCharsInBag(s, bag){
    var i;
    var returnString = "";
    // Search through string's characters one by one.
    // If character is not in bag, append to returnString.
    for (i = 0; i < s.length; i++){
        var c = s.charAt(i);
        if (bag.indexOf(c) == -1) returnString += c;
    }
    return returnString;
}

function daysInFebruary (year){
    // February has 29 days in any year evenly divisible by four,
    // EXCEPT for centurial years which are not also divisible by 400.
    return (((year % 4 == 0) && ( (!(year % 100 == 0)) || (year % 400 == 0))) ? 29 : 28 );
}
function DaysArray(n) {
    for (var i = 1; i <= n; i++) {
        this[i] = 31
        if (i==4 || i==6 || i==9 || i==11) {
            this[i] = 30
            }
        if (i==2) {
            this[i] = 29
            }
    }
    return this
}

function isDate(dtStr){
    var daysInMonth = DaysArray(12)
    var pos1=dtStr.indexOf(dtCh)
    var pos2=dtStr.indexOf(dtCh,pos1+1)
    var strDay=dtStr.substring(0,pos1)
    var strMonth=dtStr.substring(pos1+1,pos2)
    var strYear=dtStr.substring(pos2+1)
    strYr=strYear
    if (strDay.charAt(0)=="0" && strDay.length>1) strDay=strDay.substring(1)
    if (strMonth.charAt(0)=="0" && strMonth.length>1) strMonth=strMonth.substring(1)
    for (var i = 1; i <= 3; i++) {
        if (strYr.charAt(0)=="0" && strYr.length>1) strYr=strYr.substring(1)
    }
    month=parseInt(strMonth)
    day=parseInt(strDay)
    year=parseInt(strYr)
    if (pos1==-1 || pos2==-1){
        log("The date format should be : dd/mm/yyyy");
        return false
    }
    if (strMonth.length<1 || month<1 || month>12){
        log("Please enter a valid month");
        return false
    }
    if (strDay.length<1 || day<1 || day>31 || (month==2 && day>daysInFebruary(year)) || day > daysInMonth[month]){
        log("Please enter a valid day");
        return false
    }
    if (strYear.length != 4 || year==0 || year<minYear || year>maxYear){
        log("Please enter a valid 4 digit year between "+minYear+" and "+maxYear);
        return false
    }
    if (dtStr.indexOf(dtCh,pos2+1)!=-1 || isInteger(stripCharsInBag(dtStr, dtCh))==false){
        log("Please enter a valid date");
        return false
    }
    return true
}

function ValidateForm(){
    var dt=document.frmSample.txtDate
    if (isDate(dt.value)==false){
        dt.focus()
        return false
    }
    return true
}

