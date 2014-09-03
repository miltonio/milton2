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
                <div class="span6 forms">
                    <form class="form-horizontal editProfile" method="POST" action="${pagePath}" >
                        <fieldset>
                            <legend>Calendar details <a class="btn pull-right" href="${pagePath}"><i class="icon-arrow-left"></i> Cancel</a></legend>
                            <div class="control-group">
                                <label class="control-label" >Name</label>
                                <div class="controls">
                                    <input id="name" required="" type="text" name="name" value="${model.page.source.name}"/>
                                </div>
                            </div>
                            
                            <div class="control-group">
                                <div class="controls">
                                    <button type="submit" class="btn"><i class="icon-ok-sign"></i> Save</button>
                                </div>
                            </div>                            
                        </fieldset>                   
                    </form>
                </div>
            </div>
        </div> <!-- /container -->

        <%@include file="includes/theme-bottom.jsp" %>

        <script type="text/javascript" language="javascript">
            $(function() {
                log("running muso forms");
                $("form.editProfile").forms({
                    callback: function() {
                        $("form.editMusician legend").after("<div class='alert'><button type='button' class='close' data-dismiss='alert'>&times;</button>Saved ok. <a href='.'>Return to the view page</a></div>")
                    }
                });
                log("init change password", $("form.change-password"));
                $("form.change-password").forms({
                    callback: function() {
                        $("form.change-password legend").after("<div class='alert'><button type='button' class='close' data-dismiss='alert'>&times;</button>Saved ok. <a href='.'>Password changed</a></div>")
                    },
                    validate: function() {
                        var form = $("form.change-password");
                        if (form.find("input[name=password]").val() !== form.find("input[name=confirmPassword]").val()) {
                            alert("The passwords dont match");
                            return false;
                        }
                        return true;
                    }
                });
            });
        </script>             
    </body>
</html>
