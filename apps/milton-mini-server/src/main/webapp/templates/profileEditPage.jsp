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
                            <legend>Profile details <a class="btn pull-right" href="${pagePath}"><i class="icon-arrow-left"></i> Cancel</a></legend>
                            <div class="control-group">
                                <label class="control-label" >Login name</label>
                                <div class="controls">
                                    <input id="name" required="" type="text" name="name" value="${model.page.source.name}"/>
                                </div>
                            </div>
                            <div class="control-group">
                                <label class="control-label" >First name</label>
                                <div class="controls">
                                    <input id="firstName" required="" type="text" name="firstName" value="${model.page.source.firstName}"/>
                                </div>
                            </div>
                            <div class="control-group">
                                <label class="control-label" >Surname</label>
                                <div class="controls">
                                    <input id="surName" required="" type="text" name="surName" value="${model.page.source.surName}"/>
                                </div>
                            </div>
                            <div class="control-group">
                                <label class="control-label" >mail</label>
                                <div class="controls">
                                    <input id="email" required="" type="text" name="email" value="${model.page.source.email}"/>
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
                                
                <div class="span6">
                    <form class="form-horizontal change-password" method="POST" action="${pagePath}" >
                        <fieldset>
                            <legend>Change password</legend>
                            <div class="control-group">
                                <label class="control-label" >New password</label>
                                <div class="controls">
                                    <input id="password" required="" type="password" name="password" value=""/>
                                </div>
                            </div>
                            <div class="control-group">
                                <label class="control-label" >Confirm</label>
                                <div class="controls">
                                    <input id="confirmPassword" required="" type="password" name="confirmPassword" value=""/>
                                </div>
                            </div>
                            
                            <div class="control-group">
                                <div class="controls">
                                    <button type="submit" class="btn"><i class="icon-ok-sign"></i> Change password</button>
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
                        if( form.find("input[name=password]").val() !== form.find("input[name=confirmPassword]").val() ) {
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
