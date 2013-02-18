<!-- Le javascript
================================================== -->
<!-- Placed at the end of the document so the pages load faster -->
<script src="//code.jquery.com/jquery.js"></script>
<script src="/templates/js/jqBootstrapValidation/jqBootstrapValidation.js"></script>
<script src="/templates/js/common.js"></script>
<script src="/templates/js/jquery.forms.js"></script>
<script src="/templates/themes/bootstrap/js/bootstrap.min.js"></script>

<script type="text/javascript" language="javascript">
    
    var targetModalLink;
    
    $(function() {
        $("body").on("click", 'a.modalLink', function(e) {            
            log("clicked1");
            targetModalLink = $(e.target);
            log("clicked2");
        });
        
        $(document).ajaxSuccess(function(e, xml, options) {
            log("ajax done", e, xml, options);
        });
        
        $('body').on('hidden', '.modal', function () {
            $(this).removeData('modal');
        });   
        
        $("body").on("show", function(e) {
            log("show");
            var modal = $(e.target);
            var forms = modal.find("form");
            forms.find(":input").not(':button, :submit, :reset, :hidden')
            .val('')
            .removeAttr('checked')
            .removeAttr('selected');
            
        });
        
        $("body").on("shown", function(e) {
            log("shown");
            var modal = $(e.target);
            var forms = modal.find("form");
            log("running forms init", modal, forms);
            forms.find("legend a.btn").remove();
            modal.find("h3").html(forms.find("legend").text());
                
            forms.forms({
                callback: function() {
                    modal.modal("hide");                    
                    var cont = targetModalLink.closest(".well, .container");
                    log("cont", cont);
                    if( cont.attr("id")) {                        
                        var url = window.location.pathname;
                        log("url", url);
                        url  += "?" + Math.random() + " #" + cont.attr("id") + " > *";
                        log("reload: ", cont, url );
                        cont.load(url);
                    } else {
                        window.location.reload();
                    }
                }
            });    
            modal.find("a.btn-primary").click(function() {
                forms.submit();
            });
        });
    });            
</script>
-->