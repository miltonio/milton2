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
                <div class="span6">
                    <div class="well login-container" id="">                        
                        <h1>Login</h1>
                        <form action="" name="frmLogin" method="post" class="login">
                            <p id="validationMessage" style="display: none"></p>
                            <table>
                                <tr>
                                    <td colspan="2"><input type="text" name="email" value="" placeholder="Enter your email address here" /></td>
                                </tr>
                                <tr>
                                    <td colspan="2"><input type="password" name="password" value="" placeholder="Enter your password here" /></td>
                                </tr>
                                <tr>
                                    <td><button><span>Login</span></button></td>
                                </tr>
                            </table>
                        </form>                        
                    </div>

                </div>

            </div>            

        </div> <!-- /container -->

        <%@include file="includes/theme-bottom.jsp" %>
        <script type="text/javascript" language="javascript">
            $(function() {
                log("running muso forms");
                $(".login-container").user({
                    urlSuffix: "/"
                });
            });
        </script>           

    </body>
</html>

