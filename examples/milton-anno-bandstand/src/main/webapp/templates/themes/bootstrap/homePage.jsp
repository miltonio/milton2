<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="x" uri="http://java.sun.com/jsp/jstl/xml" %>
<!DOCTYPE html>
<html lang="en">
    <head>
        <meta charset="utf-8">
        <title>Bootstrap, from Twitter</title>
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        <meta name="description" content="">
        <meta name="author" content="">

        <!-- Le styles -->
        <link href="/templates/themes/bootstrap/css/bootstrap.css" rel="stylesheet">
        <style>
            body {
                padding-top: 60px; /* 60px to make the container go all the way to the bottom of the topbar */
            }
        </style>
        <link href="/static/css/bootstrap-responsive.css" rel="stylesheet">

        <!-- HTML5 shim, for IE6-8 support of HTML5 elements -->
        <!--[if lt IE 9]>
          <script src="/js/html5shiv.js"></script>
        <![endif]-->

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
                <div class="span4">
                    <h1>Bands</h1>
                    <p>Here are all the bands in our database</p>
                    <ul>
                        <c:forEach items="${model.page.child('bands').children}" var="band">    
                            <li>${band.link}</li>
                        </c:forEach>
                    </ul>

                </div>
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

        <!-- Le javascript
        ================================================== -->
        <!-- Placed at the end of the document so the pages load faster -->
        <script src="//code.jquery.com/jquery.js"></script>
        <script src="/templates/themes/bootstrap/js/bootstrap.min.js"></script>
    </body>
</html>
