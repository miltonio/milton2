/*
    FAQ v1.1 Plugin
    
    A dynamic FAQ builder using the power of jQuery.

	Updated December 3rd, 2008 
		- Changed the span to a div - Symantically correct
		- Added an incrementor in case of a duplicate header name
		- Changed the regular expression to anything other than alphanumeric
		- Corrected the syntax to accept the faqHeader variable

    Example HTML Syntax:
    --------------------
    <div id="faq">
        <h2>Title 1</h2>
        <div>Some content</div>
        <h2>Title 2</h2>
        <div>Some content</div>
    </div>

    Example Script Syntax:
    --------------------
    <script type="text/javascript" src="http://ajax.googleapis.com/ajax/libs/jquery/1.2.6/jquery.min.js"></script>
    <script type="text/javascript" src="jquery.faq.js"></script>    
	<script type="text/javascript" >
	    $().ready(function() {
			$('#faq').makeFAQ({
				indexTitle: "My Index",
				displayIndex: true,
				faqHeader: "h2"
			});
	    });
	</script>

    Visit http://www.dscoduc.com for questions, comments, issues.
    Creative Commons Attribution-Share Alike
*/

(function($) { 
    $.fn.makeFAQ = function(options) {
        var defaults = {
            indexTitle: "Index",    // Change to whatever you want to be displayed
            faqHeader: ":header",   // default grabs any header element - h1,h2, etc...
            displayIndex: true      // Display Index
        };
        var options = $.extend(defaults, options);

        return this.each(function() {
            // load the parent object only once
            var $obj = $(this);

            // wrap parent in faqRoot div
            $obj.wrap("<div id='faqRoot'></div>");
            
            // Add index div
            if(options.displayIndex) {
                $obj.before("<div id='faqindex'><h2>" + options.indexTitle + "</h2><ul></ul></div>");
            };

            // Get header children using the obj ID
            var $faqEntries = $obj.children(options.faqHeader);
			// counting integer - ensures unique id names
			var i = 0;
            // enumerate through each entry and perform several tasks
            $faqEntries.each(function () {
                // load object only once
                var $entry = $(this);

                // Get entry name
                var entryName = $entry.text();
                // strip whitespaces and special characters
                var entryNameSafe = entryName.replace(/\W/g,"") + i;
				// Increment counter
                i++;

                // build index line for entry
                var itemHTML = "<li><a id='" + entryNameSafe.toString() + "Index' href='#" + entryNameSafe.toString() + "' >" + entryName + "</a></li>";
                // append the index line to the index
                $('#faqindex ul').append(itemHTML);

                // add click event for index entry
                if(options.displayIndex) {
                    $('#' + entryNameSafe.toString() + 'Index').click( function(){ 
                        // slide down the selected index before jumping to the bookmark    
                        $('#' + entryNameSafe.toString()).next('span').slideDown('fast');
                        // make sure it gets the faqopened class
                        $('#' + entryNameSafe.toString()).addClass('faqopened');
                     });
                };

                // add class to faq entry content
                $entry.next("div").addClass('faqcontent');

                // add title, name and id to entry
                $entry.attr({
                    title: "Click to expand/collapse",
                    name: entryNameSafe,
                    id: entryNameSafe                    
                    })
                    // add class
                    .addClass("faqclosed")
                    
                    // Add click event to entry
                    .click( function() {
                        $entry.next('div').slideToggle('fast');
                        $entry.toggleClass('faqopened');
                        })
                        // Collapse the span tag of the entry
                        .next('div').css({ 
                            display: "none"
                        });
            }); // end enumeration of each faq entry

        }); // end this each
    }; // end function
})(jQuery);