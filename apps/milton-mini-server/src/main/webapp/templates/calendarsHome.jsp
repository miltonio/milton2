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
            <div class="row">
                <div class="span6">
                    <div class="well" id="bandsDiv">                        
                        <h1>
                            Calendars
                            <a href="new?editMode .container form" class="btn pull-right newItem modalLink" data-toggle="modal" data-target="#newItemModal" ><i class="icon-plus-sign"></i> Add</a>
                        </h1>
                        <ul>
                            <c:forEach items="${model.page.children.getSortByName()}" var="calendar">    
                                <li>${calendar.link}</li>
                            </c:forEach>
                        </ul>                        

                    </div>

                </div>
                
            </div>            

        </div> <!-- /container -->
    
        <%@include file="includes/theme-bottom.jsp" %>

    </body>
</html>

