<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="x" uri="http://java.sun.com/jsp/jstl/xml" %>
<!DOCTYPE html>
<html lang="en">
    <head>
        <%@include file="/templates/includes/theme-top.jsp" %>
    </head>

    <body>

        <%@include file="/templates/includes/theme-nav.jsp" %>

        <div class="container">            
            <form class="form-horizontal editMusician" method="POST" action="${pagePath}" >
                <fieldset>
                    <legend>Musician details <a class="btn pull-right" href="${pagePath}"><i class="icon-arrow-left"></i> Cancel</a></legend>
                    <div class="control-group">
                        <label class="control-label" >Muso name</label>
                        <div class="controls">
                            <input id="name" required="" type="text" name="name" value="${model.page.source.name}"/>
                        </div>
                    </div>
                    <div class="control-group">
                        <label class="control-label" >First name</label>
                        <div class="controls">
                            <input id="givenName" required="" type="text" name="givenName" value="${model.page.source.givenName}"/>
                        </div>
                    </div>
                    <div class="control-group">
                        <label class="control-label" >Surname</label>
                        <div class="controls">
                            <input id="surName" required="" type="text" name="surName" value="${model.page.source.surName}"/>
                        </div>
                    </div>
                    <div class="control-group">
                        <label class="control-label" >mail</label>
                        <div class="controls">
                            <input id="mail" required="" type="text" name="mail" value="${model.page.source.mail}"/>
                        </div>
                    </div>
                    <div class="control-group">
                        <label class="control-label" >telephonenumber</label>
                        <div class="controls">
                            <input id="telephonenumber" required="" type="text" name="telephonenumber" value="${model.page.source.telephonenumber}"/>
                        </div>
                    </div>
                    <div class="control-group">
                        <label class="control-label" >Password</label>
                        <div class="controls">
                            <input id="name" required="" type="password" name="password" value="${model.page.source.password}"/>
                        </div>
                    </div>
                    <div class="control-group">
                        <label class="control-label" >Description</label>
                        <div class="controls">
                            <textarea id="description" name="description">${model.page.source.description}</textarea>
                        </div>
                    </div>
                    <div class="control-group">
                        <div class="controls">
                            <button type="submit" class="btn"><i class="icon-ok-sign"></i> Save</button>
                        </div>
                    </div>                            
                </fieldset>                   
            </form>
        </div> <!-- /container -->

        <%@include file="includes/theme-bottom.jsp" %>

        <script type="text/javascript" language="javascript">
            $(function() {
                log("running muso forms");
                $("form.editMusician").forms({
                    callback: function() {
                        $("form.editMusician legend").after("<div class='alert'><button type='button' class='close' data-dismiss='alert'>&times;</button>Saved ok. <a href='.'>Return to the view page</a></div>")
                    }
                });
            });            
        </script>             
    </body>
</html>
