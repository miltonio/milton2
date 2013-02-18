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
            <form class="form-horizontal" method="POST" action="${pagePath}">
                <fieldset>
                    <legend>Edit the band details</legend>
                    <div class="control-group">
                        <label class="control-label" >Band name</label>
                        <div class="controls">
                            <input id="name" type="text" name="name" value="${model.page.source.name}"/>
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
    </body>
</html>
