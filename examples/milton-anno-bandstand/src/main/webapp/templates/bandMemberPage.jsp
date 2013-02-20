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
            <form class="form-horizontal editForm" method="POST" action="${pagePath}">
                <fieldset>
                    <legend>
                        Edit the band member
                        <a class="btn pull-right" href="."><i class="icon-edit"></i> Cancel</a>
                    </legend>
                    <div class="control-group">
                        <label class="control-label" >Select Musician</label>
                        <div class="controls">
                            <select name="musician" required="true">
                                <c:forEach items="${model.page.root.child('musicians').children.getSortByName()}" var="muso">
                                    <option value="${muso.name}" >${muso.displayName}</option>
                                </c:forEach>
                            </select>
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
