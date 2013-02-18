<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="x" uri="http://java.sun.com/jsp/jstl/xml" %>
<!DOCTYPE html>
<html lang="en">
    <head>
        <%@include file="/templates/includes/theme-top.jsp" %>
    </head>

    <body>

        <div class="navbar navbar-inverse navbar-fixed-top">
            <div class="navbar-inner">
                <div class="container">
                    <button type="button" class="btn btn-navbar" data-toggle="collapse" data-target=".nav-collapse">
                        <span class="icon-bar"></span>
                        <span class="icon-bar"></span>
                        <span class="icon-bar"></span>
                    </button>
                    <a class="brand" href="#">BandStand</a>
                    <div class="nav-collapse collapse">
                        <ul class="nav">
                            <li class="active"><a href="#">Home</a></li>
                            <li><a href="#about">About</a></li>
                            <li><a href="#contact">Contact</a></li>
                        </ul>
                    </div><!--/.nav-collapse -->
                </div>
            </div>
        </div>

        <div class="container">
            <div class="row">
                <div class="span4 offset1">
                    <div class="well">
                        <button class="btn pull-right" data-toggle="modal" data-target="#newBandModal" ><i class="icon-plus-sign"></i> Add</button>
                        <h1>Bands</h1>
                        <p>Here are all the bands in our database</p>
                        <ul>
                            <c:forEach items="${model.page.child('bands').children}" var="band">    
                                <li>${band.link}</li>
                            </c:forEach>
                        </ul>
                    </div>

                </div>
                <div class="span2"></div>
                <div class="span4">
                    <h1>Musicians</h1>
                    <p>Here are all the musicians in our database</p>
                    <ul>
                        <c:forEach items="${model.page.child('musicians').children}" var="muso">    
                            <li>${muso.link}</li>
                        </c:forEach>
                    </ul>
                </div>
            </div>            

        </div> <!-- /container -->

        <div class="modal hide fade" id="newBandModal">
            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal" aria-hidden="true">&times;</button>
                <h3>Modal header</h3>
            </div>
            <div class="modal-body">
                <p>One fine body..</p>
            </div>
            <div class="modal-footer">
                <a href="#" class="btn" data-dismiss="modal">Close</a>
                <a href="#" class="btn btn-primary">Save changes</a>
            </div>
        </div>        

        <%@include file="includes/theme-bottom.jsp" %>

        <script type="text/javascript">
            $(function() {
                $("body").on("show","#newBandModal", function() {
                    $("#newBandModal .modal-body").html("Loading...");
                    $("#newBandModal .modal-body").load("/bands/.new?editMode .container form");
                });
            });
        </script>
    </body>
</html>

