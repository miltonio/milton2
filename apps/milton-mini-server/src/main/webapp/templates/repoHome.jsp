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
                    <div class="well" id="">                        
                        <div class="btn-group pull-right">
                            <a href="${f.href}" class="btn dropdown-toggle" data-toggle="dropdown"><i class="icon-list"></i></a>
                            <ul class="dropdown-menu" role="menu" aria-labelledby="dropdownMenu">
                                <li>
                                    <a tabindex="-1" href="#"><i class="icon-folder-open"></i>
                                        New Folder</a>
                                </li>
                                <li><a tabindex="-1" href="#"><div class="input" id='myUploaded' style="width: 150px"></div>        </a></li>
                            </ul>                  
                        </div>

                        <h1>
                            ${model.page.name}                          
                        </h1>                        
                    </div>
                </div>
            </div>  
            <div class="row">                
                <table id="fileList" class="bar table table-striped vcenter">
                    <thead>
                        <tr>
                            <th colspan="2">Files: ${model.page.name}</th>
                            <th>Modified</th>
                            <th>Size</th>
                            <th>Tools</th>
                        </tr>
                    </thead>
                    <tbody>                
                        <c:forEach items="${model.page.subFolders}" var="f">    
                            <tr>
                                <td style="width: 70px">
                                    <a href="${dir.href}">                            
                                        <img  height="48" width="48" alt="" src="/static/icons/folder.png"/>
                                    </a>
                                </td>
                                <td class="left">
                                    <a href="${f.href}">${f.name}</a>
                                </td>
                                <td><abbr title="$formatter.formatDateISO8601($f.modifiedDate)" class="timeago">${f.createDate}</abbr></td>
                                <td></td>
                                <td>
                                    <div class="btn-group">
                                        <a href="${f.href}" class="btn dropdown-toggle" data-toggle="dropdown"><i class="icon-list"></i></a>
                                        <ul class="dropdown-menu" role="menu" aria-labelledby="dropdownMenu">
                                            <li><a tabindex="-1" href="#">Delete</a></li>
                                            <li><a tabindex="-1" href="#">Rename</a></li>
                                        </ul>                  
                                    </div>
                                </td>
                            </tr>
                        </c:forEach>  

                        <c:forEach items="${model.page.files}" var="f">
                            <c:set var="lightBoxClass" value="lightbox"/>
                            <c:if test="${f.contentLength < 100000}">
                                <c:set var="lightBoxClass" value=""/>
                            </c:if>
                            <tr class="file ${lightBoxClass}">
                                <c:set var="imgClass" value="${formatter.ifTrue($f.is('image'), 'image', '' )}"/>
                                <td style="width: 70px">
                                    <a href="${f.href}" class="${imgClass}"><img height="48" width="48" alt="" src=""/></a>
                                </td>
                                <td class="left">
                                    <a href="${f.href}" class="${imgClass}">${f.name} - ${formatter.class}</a>
                                </td>
                                <td><abbr title="$formatter.formatDateISO8601($!f.modifiedDate)" class="timeago">${f.modifiedDate}</abbr></td>
                                <td>${f.contentLength}</td>
                                <td>
                                    <div class="btn-group">
                                        <a href="${f.href}" class="btn dropdown-toggle" data-toggle="dropdown"><i class="icon-list"></i></a>
                                        <ul class="dropdown-menu" role="menu" aria-labelledby="dropdownMenu">
                                            <li><a tabindex="-1" href="#">Delete</a></li>
                                            <li><a tabindex="-1" href="#">Rename</a></li>
                                            <li><a tabindex="-1" href="#">Download</a></li>
                                        </ul>                  
                                    </div>
                                </td>
                            </tr>
                        </c:forEach> 
                    </tbody>
                </table>
                <script type="text/javascript" src="/templates/js/types.js">// </script>
                <script type="text/javascript" src="/templates/js/jquery.ui.widget.js"></script>                
                <script type="text/javascript" src="/templates/js/jquery.fileupload.js"></script>
                <script type="text/javascript" src="/templates/js/jquery.milton-upload.js"></script>
                <script type="text/javascript" src="/templates/js/jquery.lightbox-0.5.js"></script>     
                <script type="text/javascript" src="/templates/js/jquery.timeago.js"></script>     
                <script type="text/javascript" src="/templates/js/myfiles.js"></script>     
            </div>


        </div> <!-- /container -->

        <%@include file="includes/theme-bottom.jsp" %>

    </body>
</html>

