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
            <a class="btn pull-right" href="?editMode=true"><i class="icon-edit"></i> Edit</a>
            <h1>${model.page.source.givenName} ${model.page.source.surName}</h1>
            <p>Phone: ${model.page.source.telephonenumber}</p>
            <p>Email: ${model.page.source.mail}</p>
            <p>ContactUid: ${model.page.source.contactUid}</p>
            <p>${model.page.source.description}</p>
        </div> <!-- /container -->

        <%@include file="includes/theme-bottom.jsp" %>
    </body>
</html>
