<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>
<%@ taglib prefix="c2mon" tagdir="/WEB-INF/tags"%>

<!-- JSP variables -->
<c:url var="home" value="../" />
<c:url var="alarmdefinitionviewer" value="./form" />

<c2mon:template title="${title}">

  <style type="text/css">
    .page-header {
      margin-top: -20px !important;
    }

    tr.invalid {
      color: #000000;
      background: #D9EDF7 !important;
    }
  </style>

  <div class="row">
    <div class="col-lg-12">
      <ul class="breadcrumb">
        <li>
          <a href="<c:url value="${home}"/>"> Home </a>
          <span class="divider"></span>
        </li>
        <li>
          <a id="goback" href="#">${title}</a>
          <span class="divider"></span>
        </li>
        <li>${configName}</li>
      </ul>
    </div>
  </div>

  <div class="row">
    <div class="col-lg-12">
      <div class="page-header">
        <h2>
          Alarm definitions for user configuration: ${configName} <small>${description}</small>
        </h2>
      </div>
    </div>
  </div>

  <div class="row">
    <div class="col-lg-12">
      <p class="pull-left btn-toolbar">
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
          <th class="col-md-1">Last Updated</th>
          <th class="col-md-1">Alarm Id</th>
          <th class="col-md-4">Alarm Name</th>
          <th class="col-md-1">System Name</th>
          <th class="col-md-1">Priority</th>
          <th class="col-md-1">Enabled</th>
          <th class="col-md-4">Problem Description</th>
          <th class="col-md-1"></th>
        </tr>
        </thead>

        <tbody>
        <c:forEach var="item" items="${alarmdefinitions}">
          <tr>
            <td>
              <script type="text/javascript">
                document.write('${item.lastUpdated}'.replace("T", " "));
              </script>
            </td>
            <td>${item.alarmId}</td>
            <td>${item.faultFamily} : ${item.faultMember} : ${item.faultCode}</td>
            <td>${item.systemName}</td>
            <td>${item.priority}</td>
            <td>
              <c:choose>
                <c:when test="${item.enabled == true}">
                  <span class="label label-success">
                  <i class="fa fa-bell"></i> ENABLED
                  </span>
                </c:when>
                <c:otherwise>
                  <span class="label label-danger">
                  <i class="fa fa-bell"></i> DISABLED
                  </span>
                </c:otherwise>
              </c:choose>
            </td>
            <td>${item.problemDescription}</td>
            <td>
                <a href="/c2mon-web-ui/alarmviewer/id/${item.alarmId}" class="view-tag btn btn-default btn-sm">
                  <i class="fa fa-external-link"></i>
                  View Alarm
                </a>
            </td>
          </tr>
        </c:forEach>
        </tbody>
      </table>
    </div>
  </div>
</c2mon:template>
<script type="text/javascript">
    $(document).ready(function() {
        $("#goback").click(function() {
            oldUrl = document.referrer;
            if(oldUrl.includes("c2mon-web-ui/laseralarmdefinitions")) {
               history.go(-1);
            }else{
               location.href = "${alarmdefinitionviewer}"
            }
        });
    });
</script>