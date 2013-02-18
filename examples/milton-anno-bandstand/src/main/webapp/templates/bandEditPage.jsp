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
            <form class="form-horizontal">
                <fieldset>
                    <legend>Edit the band details</legend>
                    <div class="control-group">
                        <label class="control-label" >Band name</label>
                        <div class="controls">
                            <input id="name" type="text" name="name" value="${model.page.source.name}"/>
                        </div>
                    </div>
                    <div class="control-group">
                        <label class="control-label" >Description</label>
                        <div class="controls">
                            <textarea id="description" name="description">${model.page.source.description}</textarea>
                        </div>
                    </div>
                    <div class="control-group">
                        <div class="controls">
                            <button type="submit" class="btn"><i class="icon-ok-sign"></i> Save</button>
                        </div>
                    </div>                            
                </fieldset>
            </form>
        </div> <!-- /container -->

        <%@include file="includes/theme-bottom.jsp" %>
    </body>
</html>
