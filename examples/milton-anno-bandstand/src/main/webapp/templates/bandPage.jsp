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
                <div class="span12">

                    <h2>Images</h2>
                    <ul class="thumbnails">
                        <c:forEach items="${model.page.childrenMap.images.childrenOfType.image}" var="image">
                            <li class="span4">
                                <a href="${image.href}" class="thumbnail">
                                    <img src="${image.href}" alt="">
                                </a>
                            </li>
                        </c:forEach>
                    </ul>

                </div>
            </div>            
            <div class="row">
                <div class="span6">
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
                <div class="span6">
                    <div class="well">
                        <h2>
                            Members 
                            <a href="newMember .container form"  class="pull-right btn btn-success modalLink" data-toggle="modal" data-target="#newItemModal">
                                <i class="icon-plus-sign"></i> Add
                            </a>
                        </h2>
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
                                    <td>${bm.musician.givenName} ${bm.musician.surName}</td>
                                </tr>
                            </c:forEach>                        
                            </tbody>
                        </table>
                    </div>
                    <div class="well">
                        <h2>Songs</h2>
                        <ul>
                            <c:forEach items="${model.page.childrenMap['songs'].children}" var="song">
                                <li>${song.link}</li>
                            </c:forEach>
                        </ul>
                    </div>                                
                </div>
            </div>

        </div> <!-- /container -->

        <%@include file="includes/theme-bottom.jsp" %>
    </body>
</html>
