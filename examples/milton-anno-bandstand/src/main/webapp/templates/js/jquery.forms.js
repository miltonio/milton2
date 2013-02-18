


(function( $ ) {
    
    var methods = {
        init : function( options ) { 
            var container = this;
            
            var config = $.extend( {}, options);
            var inputs = container.find("input,select,textarea").not("[type=submit]");
            log("val", inputs, container);
            inputs.jqBootstrapValidation({
                submitSuccess: function(form, e) {
                    e.stopPropagation();
                    e.cancel=true;
                    e.preventDefault();                        
                    postForm(form);                        
                    return false;
                }
            });                
            
        }
    };    
    
    $.fn.forms = function(method) {        
        if ( methods[method] ) {
            return methods[ method ].apply( this, Array.prototype.slice.call( arguments, 1 ));
        } else if ( typeof method === 'object' || ! method ) {
            return methods.init.apply( this, arguments );
        } else {
            $.error( 'Method ' +  method + ' does not exist on jQuery.tooltip' );
        }           
    };
})( jQuery );
