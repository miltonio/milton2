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
                <script type="text/javascript" language="javascript">
                    $(function() {
                        log("running muso forms");
                        $("form.editMusician").forms();
                    });            
                </script>                        
            </form>
        </div> <!-- /container -->

        <%@include file="includes/theme-bottom.jsp" %>

    </body>
</html>
