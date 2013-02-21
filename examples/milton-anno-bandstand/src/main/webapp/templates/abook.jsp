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
            <ul>
                <c:forEach items="${model.page.children}" var="contact">
                <li>
                    ${contact.source.musician.name}
                </li>
                </c:forEach>
            </ul>
        </div> <!-- /container -->

        <%@include file="includes/theme-bottom.jsp" %>
        
    </body>
</html>
