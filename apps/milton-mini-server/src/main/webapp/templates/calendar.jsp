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
                <div class="span9">
                    <div class="well" id="bandsDiv">                        
                        <h1>
                            Calendar ${model.page.name}
                            <a href="new?editMode .container form" class="btn pull-right newItem modalLink" data-toggle="modal" data-target="#newItemModal" ><i class="icon-plus-sign"></i> Add</a>
                        </h1>
                            <p>
                                Ctag: ${model.page.CTag}
                            </p>
                        <table class="table table-bordered table-striped">
                            <thead>
                                <tr>
                                    <th></th>
                                    <th>Start</th>
                                    <th>End</th>
                                    <th>Summary</th>
                                    <th>UniqueId</th>
                                    <th>Modified</th>
                                </tr>
                            </thead>
                            <tbody>
                                <c:forEach items="${model.page.children.getSortByName()}" var="event">    
                                    <tr>
                                        <td>${event.link}</td>
                                        <td>${event.source.startDate}</td>
                                        <td>${event.source.endDate}</td>
                                        <td>${event.source.summary}</td>
                                        <td>${event.uniqueId}</td>
                                        <td>${event.modifiedDate}</td>
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

