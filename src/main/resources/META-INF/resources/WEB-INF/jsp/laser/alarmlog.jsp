<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>
<%@ taglib prefix="c2mon" tagdir="/WEB-INF/tags"%>

<!-- JSP variables -->
<c:url var="home" value="../" />
<c:url var="historyviewer" value="../alarmlog/form" />
<c:url var="csvviewer" value="./csv/${alarm.id}" />
<c:url var="tagviewer" value="../tagviewer/id/${alarm.tagId}" />
<c:url var="taghistory" value="../historyviewer/${alarm.tagId}" />
<c:url var="alarmviewer" value="../alarmviewer/id/${alarm.id}" />

<c2mon:template title="${title}">

  <style type="text/css">
    .page-header {
      margin-top: -20px !important;
    }

    tr.invalid {
      color: #000000;
      background: #D9EDF7 !important;
    }

    .pagination {
      display: inline-block;
    }

    .pagination a {
      color: black;
      float: left;
      padding: 8px 16px;
      text-decoration: none;
    }

    .pagination a.active {
      background-color: #4CAF50;
      color: white;
    }

    .pagination a:hover:not(.active) {background-color: #ddd;}
  </style>

  <div class="row">
    <div class="col-lg-12">
      <ul class="breadcrumb">
        <li>
          <a href="<c:url value="${home}"/>">
            Home
          </a>
          <span class="divider"></span>
        </li>
        <li>
          <a href="<c:url value="${historyviewer}"/>">Alarm History</a>
          <span class="divider"></span>
        </li>
        <li>${alarm.id}</li>
      </ul>
    </div>
  </div>

  <div class="row">
    <div class="col-lg-12">
      <div class="page-header">
        <h2>
          Alarm History: ${alarm.faultFamily} : ${alarm.faultMember} : ${alarm.faultCode} <small>${description}</small>
        </h2>
      </div>
    </div>
  </div>

  <div class="row">
    <div class="col-lg-12">
      <p class="pull-left btn-toolbar">
        <a href="${alarmviewer}" class="btn btn-default btn-large">
          <span class="glyphicon glyphicon-tags"></span>
          &nbsp;View Alarm
        </a>
        <a href="${tagviewer}" class="btn btn-default btn-large">
          <span class="glyphicon glyphicon-tags"></span>
          &nbsp;View Tag
        </a>
        <a onclick="viewTagHistory()" class="btn btn-default btn-large">
          <span class="glyphicon glyphicon-tags"></span>
          &nbsp;View Tag History
        </a>
        <a href="${csvviewer}" class="btn btn-default btn-large">
          <span class="glyphicon glyphicon-stats"></span>
          &nbsp;View CSV
        </a>
      </p>
    </div>
  </div>

  <div class="row">
    <div class="col-lg-12">
      <table class="table table-striped table-bordered">
        <thead>
        <tr>
          <th class="col-md-2">Timestamp</th>
          <th class="col-md-1">Status</th>
          <th class="col-md-2">Alarm Prefix</th>
          <th class="col-md-2">Alarm Suffix</th>
          <th class="col-md-2">Alarm Timestamp</th>
          <th class="col-md-4">Alarm User</th>
        </tr>
        </thead>

        <tbody>
        <c:forEach var="item" items="${history}">

          <tr>
            <td>
              <script type="text/javascript">
                document.write('${item.tagservertime}'.replace("T", " "));
              </script>
            </td>
            <td>
             <c:choose>
               <c:when test="${item.tagValue == '\"DOWN\"'}">
                 <span class="label label-danger">
                 <i class="fa fa-bell"></i> ${item.tagValue}
                 </span>
               </c:when>
               <c:when test="${item.tagValue == 'true'}">
                 <span class="label label-danger">
                 <i class="fa fa-bell"></i> ACTIVE
                 </span>
               </c:when>
               <c:when test="${item.tagValue == '\"STARTUP\"'}">
                 <span class="label label-warning">
                 <i class="fa fa-bell"></i> ${item.tagValue}
                 </span>
               </c:when>
               <c:when test="${item.tagValue == '\"RUNNING\"'}">
                 <span class="label label-success">
                 <i class="fa fa-bell"></i> ${item.tagValue}
                 </span>
               </c:when>
               <c:when test="${item.tagValue == 'false'}">
                 <span class="label label-success">
                 <i class="fa fa-bell"></i> TERMINATED
                 </span>
               </c:when>
               <c:otherwise>
                 ${item.tagValue}
               </c:otherwise>
             </c:choose>
            </td>
            <td>${item.alarmPrefix}</td>
            <td>${item.alarmSuffix}</td>
            <td>${item.alarmTimestamp}</td>
            <td>${item.alarmUser}</td>
          </tr>
        </c:forEach>
        </tbody>
      </table>
      <div class="pagination">
        <a id="pageback" href="#" onclick="replacePageNo(${pageNumber - 1})">&laquo;</a>
             <c:forEach var = "i" begin = "${pageNumber}" end = "${pageNumber + 10 <= totalPages ? pageNumber + 10 : pageNumber + (totalPages - pageNumber)}">
                <a id = "page${i}" href="#" onclick="replacePageNo(${i})">${i}</a>
             </c:forEach>
        <a id="pageforward" href="#" onclick="replacePageNo(${pageNumber + 1})">&raquo;</a>
      </div>
    </div>
  </div>

  <script type="text/javascript">

    function viewTagHistory() {
      var url = '${taghistory}' + '?' + window.location.href.split('?')[1];
      window.location.href = url;
    }

    <!-- Hide/Show next/previous page controls -->

    if(${pageNumber <= 1}){
        var pageBack = document.getElementById('pageback');
        pageBack.style.display = "none";
    }

    if(${pageNumber} >= ${totalPages}){
        var pageForward = document.getElementById('pageforward');
        pageForward.style.display = "none";
    }

    <!-- Add focus to current page number -->
    var currPage = document.getElementById('page' + ${pageNumber});

    if(currPage != null){
        currPage.focus();
    }

    <!-- Change page on click -->
    function replacePageNo(paramValue){
       var url = window.location.href;
       var paramName = "PAGENO";

       if (paramValue == null) {
           paramValue = '';
       }

       var pattern = new RegExp('\\b('+paramName+'=).*?(&|#|$)');
       if (url.search(pattern)>=0) {
           url = url.replace(pattern,'$1' + paramValue + '$2');
       }else{
           url = url.replace(/[?#]$/,'');
           url = url + (url.indexOf('?')>0 ? '&' : '?') + paramName + '=' + paramValue;
       }
       location.href = url;
    }

  </script>

</c2mon:template>
