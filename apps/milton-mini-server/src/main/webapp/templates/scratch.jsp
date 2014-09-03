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
                        <h1>Test Webdav stuff</h1>
                        <input type="method" id="test-method" style="width: 100%" class="" value="PROPFIND" placeholder="method" />
                        <input type="url" id="test-url" style="width: 100%" class="" value="/users/admin/cals/" placeholder="path" />
                        <br/>
                        <textarea id="input-xml" style="width: 100%" rows="10">
                        </textarea>
                        <button class="btn btn-primary btn-submit-test">Submit</button>

                        <hr/>
                        Example input
                        <br/>
                        <textarea style="width: 100%" rows="3"><?xml version="1.0" encoding="utf-8"?><D:propfind xmlns:D="DAV:"><D:prop><D:current-user-principal/></D:prop></D:propfind>
                        </textarea>       
                        
                        <textarea style="width: 100%" rows="3"><?xml version="1.0" encoding="utf-8" ?>
   <D:principal-property-search xmlns:D="DAV:">
     <D:property-search>
       <D:prop>
         <D:displayname/>
       </D:prop>
       <D:match>doE</D:match>
     </D:property-search>
     <D:prop xmlns:B="http://www.example.com/ns/">
       <D:displayname/>
       <B:department/>
       <B:phone/>
       <B:office/>
       <B:salary/>
     </D:prop>
   </D:principal-property-search>                            
                        </textarea>
<textarea style="width: 100%" rows="3"><?xml version="1.0" encoding="utf-8" ?>
   <D:principal-property-search-set xmlns:D="DAV:">
   </D:principal-property-search-set>
                        </textarea>                        
                    </div>

                </div>
                <div class="span6">
                    <div class="panel" id="output-xml">
                        <h3 class="test-status">Result status goes here</h3>
                        <pre class="test-output">Output goes here</pre>
                    </div>                    
                </div>

            </div>            

        </div> <!-- /container -->

        <%@include file="includes/theme-bottom.jsp" %>
        <script type="text/javascript" language="javascript" src="/templates/js/scratch.js">//           
        </script>           

    </body>
</html>

