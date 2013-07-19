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
                    <div class="well" id="">                        
                        <h1>
                            Repository: ${model.repo.name}                          
                        </h1>
                    </div>
                    <ul>
                        <c:forEach items="${model.page.children.getSortByName()}" var="node">    
                            <li>${node.link}</li>
                        </c:forEach>                        
                    </ul>
                </div>
            </div>            

        </div> <!-- /container -->

        <%@include file="includes/theme-bottom.jsp" %>

    </body>
</html>

