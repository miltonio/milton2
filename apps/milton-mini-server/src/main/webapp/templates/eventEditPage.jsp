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
                    <form class="form-horizontal event-edit" method="POST" action="${pagePath}" >
                        <fieldset>
                            <legend>Event details <a class="btn pull-right" href="${pagePath}"><i class="icon-arrow-left"></i> Cancel</a></legend>
                            <div class="control-group">
                                <label class="control-label" >Start Date</label>
                                <div class="controls">
                                    <input id="startDate" required="" type="datetime" name="startDate" value="${model.page.source.startDate}"/>
                                </div>
                            </div>
                            <div class="control-group">
                                <label class="control-label" >End date</label>
                                <div class="controls">
                                    <input id="endDate" required="" type="datetime" name="endDate" value="${model.page.source.endDate}"/>
                                </div>
                            </div>
                            <div class="control-group">
                                <label class="control-label" for="timezone" >Timezone</label>
                                <div class="controls">
                                    <select name="timezone" class="required">
                                        <option value="">[Please select]</option>
                                        <c:forEach items="${model.timezoneIdList}" var="tz">
                                            <option value="${tz}">${tz}</option>
                                        </c:forEach>
                                    </select>
                                </div>
                            </div>
                            <div class="control-group">
                                <label class="control-label" for="location" >Location</label>
                                <div class="controls">
                                    <input id="location" required="" type="text" name="location" value="${model.page.source.location}"/>
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
                $("form.event-edit").forms({
                    callback: function(form) {
                        form.after("<div class='alert'><button type='button' class='close' data-dismiss='alert'>&times;</button>Saved ok. <a href='.'>Return to the view page</a></div>")
                    }
                });
            });
        </script>             
    </body>
</html>
