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
                            Bands
                            <a href="/bands/new?editMode .container form" class="btn pull-right newItem modalLink" data-toggle="modal" data-target="#newItemModal" ><i class="icon-plus-sign"></i> Add</a>
                        </h1>
                        <p>Here are all the bands in our database</p>
                        <ul>
                            <c:forEach items="${model.page.child('bands').children.getSortByName()}" var="band">    
                                <li>${band.link}</li>
                            </c:forEach>
                        </ul>
                    </div>

                </div>
                <div class="span6">
                    <div class="well" id="musosDiv">
                        <h1>
                            Musicians
                            <a href="/musicians/new?editMode .container form" class="btn pull-right newItem modalLink" data-toggle="modal" data-target="#newItemModal" ><i class="icon-plus-sign"></i> Add</a>
                        </h1>
                        <p>Here are all the musicians in our database</p>
                        <ul>
                            <c:forEach items="${model.page.child('musicians').children.getSortByName()}" var="muso">    
                                <li><a href="${muso.href}">${muso.source.givenName} ${muso.source.surName}</a></li>
                            </c:forEach>
                        </ul>
                    </div>
                </div>
            </div>            

        </div> <!-- /container -->
    
        <%@include file="includes/theme-bottom.jsp" %>

    </body>
</html>

