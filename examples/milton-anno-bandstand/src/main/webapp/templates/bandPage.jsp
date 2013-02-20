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
            <h1>${model.page.displayName}</h1>            
            <p>${model.page.source.description}</p>
            <div class="row">
                <div class="span4 offset1">
                    <div class="well">
                        <h2>Gigs</h2>
                        <table>
                            <thead>
                                <tr>
                                    <th>Date</th>
                                    <th>Title</th>
                                </tr>
                            </thead>
                            <tbody>
                            <c:forEach items="${model.page.source.gigs}" var="gig">    
                                <tr>
                                    <td>${gig.startDate}</td>                            
                                    <td>${gig.displayName}</td>
                                </tr>
                            </c:forEach>                        
                            </tbody>
                        </table>
                    </div>
                </div>
                <div class="span2"></div>
                <div class="span4">
                    <div class="well">
                        <h2>Band Members</h2>
                        <table>
                            <thead>
                                <tr>
                                    <th>Date</th>
                                    <th>Title</th>
                                </tr>
                            </thead>
                            <tbody>
                            <c:forEach items="${model.page.source.bandMembers}" var="bm">    
                                <tr>
                                    <td>${bm.musician.name}</td>
                                </tr>
                            </c:forEach>                        
                            </tbody>
                        </table>

                    </div>

                </div>
            </div>
        </div> <!-- /container -->

        <%@include file="includes/theme-bottom.jsp" %>
    </body>
</html>
