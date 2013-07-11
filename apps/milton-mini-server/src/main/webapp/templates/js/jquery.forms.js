


(function($) {

    var methods = {
        init: function(options) {
            var container = this;

            var config = $.extend({
                callback: function(resp, form) {
                    log("Completed POST", resp, form);
                },
                validate: function(form) {
                    return true;
                }
            }, options);
            var inputs = container.find("input,select,textarea").not("[type=submit]");
            log("jqBootstrapValidation", inputs, container);
            inputs.jqBootstrapValidation({
                submitSuccess: function(form, e) {
                    log("submit syuccess");
                    e.stopPropagation();
                    e.cancel = true;
                    e.preventDefault();
                    if (!config.validate(form)) {
                        log("validate method returned false");
                        return false;
                    }
                    postForm(form, config.callback);
                    return false;
                }
            });

        }
    };

    $.fn.forms = function(method) {
        if (methods[method]) {
            return methods[ method ].apply(this, Array.prototype.slice.call(arguments, 1));
        } else if (typeof method === 'object' || !method) {
            return methods.init.apply(this, arguments);
        } else {
            $.error('Method ' + method + ' does not exist on jQuery.tooltip');
        }
    };
})(jQuery);
