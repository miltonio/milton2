
<div class="modal hide fade" id="newItemModal">
    <div class="modal-header"><button type="button" class="close" data-dismiss="modal" aria-hidden="true">&times;</button><h3>Modal header</h3></div>
    <div class="modal-body">

    </div>
    <div class="modal-footer">
        <a href="#" class="btn" data-dismiss="modal">Close</a>
        <a href="#" class="btn btn-primary">Save changes</a>
    </div>            
</div>    


<script type="text/javascript" language="javascript">
    
    var targetModalLink;
    
    $(function() {
        $("body").on("click", 'a.modalLink', function(e) {            
            targetModalLink = $(e.target);
        });
                
        $('body').on('hidden', '.modal', function () {
            $(this).removeData('modal');
        });   
        
        $("body").on("show", function(e) {
            var modal = $(e.target);
            var forms = modal.find("form");
            forms.find(":input").not(':button, :submit, :reset, :hidden')
            .val('')
            .removeAttr('checked')
            .removeAttr('selected');
            
        });
        
        $("body").on("shown", function(e) {
            var modal = $(e.target);
            var forms = modal.find("form");
            forms.find("legend a.btn").remove();
            modal.find("h3").html(forms.find("legend").text());
                
            forms.forms({
                callback: function() {
                    modal.modal("hide");                    
                    var cont = targetModalLink.closest(".well, .container");
                    if( cont.attr("id")) {                        
                        var url = window.location.pathname;
                        url  += "?" + Math.random() + " #" + cont.attr("id") + " > *";
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
