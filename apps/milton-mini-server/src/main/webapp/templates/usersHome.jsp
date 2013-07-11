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
                            Users
                            <a href="/users/new?editMode .container form.editProfile" class="btn pull-right newItem modalLink" data-toggle="modal" data-target="#newItemModal" ><i class="icon-plus-sign"></i> Add</a>
                        </h1>
                        <p>Here are all the users in our database</p>
                        <ul>
                            <c:forEach items="${model.page.children.getSortByName()}" var="user">    
                                <li>${user.link}</li>
                            </c:forEach>
                        </ul>
                    </div>

                </div>
                
            </div>            

        </div> <!-- /container -->
    
        <%@include file="includes/theme-bottom.jsp" %>

    </body>
</html>

