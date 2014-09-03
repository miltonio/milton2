


(function( $ ) {
    
    var methods = {
        init : function( options ) { 
            var container = this;
            
            var config = $.extend( {
                url: "./",
                useJsonPut: true,
                buttonText: "Add files...",
                oncomplete: function(data) {
                    log("finished upload", data);
                }
                }, options);  
                
            log("init milton uploads", container);
            var actionUrl = config.url;
            if( config.useJsonPut ) {
                actionUrl += "_DAV/PUT?overwrite=true";
            }
            log("upload to url: ", actionUrl);
            var form = $("<form action='" + actionUrl + "' method='POST' enctype='multipart/form-data' style='position: relative'><input type='hidden' name='overwrite' value='true'></form>");
            var buttonBar = $("<div class='row fileupload-buttonbar' style='position:relative; overflow: hidden'></div>");
            var fileInput = $("<input type='file' name='files[]' id='fileupload' style='opacity: 0; font-size: 50px; min-width: 100%; z-index: 6; position: absolute; right: 0' />");
            form.append(buttonBar);
            
            var fileInputContainer = $("<div class='muploadBtn' style='position: relative'></div>");            
            fileInputContainer.append($("<span>" + config.buttonText + "</span>"));
            
            fileInputContainer.append(fileInput);
            buttonBar.append(fileInputContainer);
            fileInputContainer.append("<div class='progress progress-info progress-striped' style='position: absolute; left: 0; top: 0; width: 100%; height: 100%; opacity: 0.5; z-index: 3'><div class='bar' style='height: 100%'></div></div>");
            container.append(form);
            var loading = $("<img src='/static/common/loading.gif' style='position: absolute; right: 5px; top: 5px'/>");
            loading.hide();
            fileInputContainer.append(loading);

            log("init fileupload", fileInput);
            fileInput.fileupload({
                dataType: 'json',
                progressInterval: 10,
                complete: function(e, data) {
                    loading.hide();
                },
                done: function (e, data) {
                    log("done", data);
                    //log("done", data.result[0], data.result[0].href);
                    var href = null; 
                    var name = null;
                    if( data.result && data.result[0]) {
                        href = data.result[0].href;
                        if( href ) {
                            name = getFileName(href);
                        }
                    }                    
                    config.oncomplete(data, name, href);
                    $('.progress').hide();
                    loading.hide();
                },
                progressall: function (e, data) {  
                    var progress = parseInt(data.loaded / data.total * 100, 10);                    
                    $('.progress').show();
                    $('.progress .bar', buttonBar).css('width',progress + '%');
                    log("progress", e, data, progress);
                    loading.show();     
                    log("show loading", loading);
                }        
            });
            log("done fileupload init");
        },
        setUrl : function( url ) {
            log("setUrl", this, url);
            var newAction = url + "_DAV/PUT?overwrite=true";
            this.find("form").attr("action", newAction);
        }
    };    
    
    $.fn.mupload = function(method) {        
        log("mupload", this);
        if ( methods[method] ) {
            return methods[ method ].apply( this, Array.prototype.slice.call( arguments, 1 ));
        } else if ( typeof method === 'object' || ! method ) {
            return methods.init.apply( this, arguments );
        } else {
            $.error( 'Method ' +  method + ' does not exist on jQuery.tooltip' );
        }           
    };
})( jQuery );
