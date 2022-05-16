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
          Alarm events for user configuration: ${configName} <small>${description}</small>
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
          <th class="col-md-2">Time</th>
          <th class="col-md-1">Alarm Id</th>
          <th class="col-md-4">Alarm Name</th>
          <th class="col-md-1">Active</th>
          <th class="col-md-1">Oscillating</th>
          <th class="col-md-1"></th>
        </tr>
        </thead>

        <tbody>
        <c:forEach var="item" items="${alarmdefinitions}">
            <td>
              <script type="text/javascript">
                document.write('${item.serverTime}'.replace("T", " "));
              </script>
            </td>
            <td>${item.id}</td>
            <td>${item.faultFamily} : ${item.faultMember} : ${item.faultCode}</td>
            <td>
              <c:choose>
                <c:when test="${item.active == true}">
                  <span class="label label-danger">
                  <i class="fa fa-bell"></i> ACTIVE
                  </span>
                </c:when>
                <c:otherwise>
                  <span class="label label-success">
                  <i class="fa fa-bell"></i> TERMINATED
                  </span>
                </c:otherwise>
              </c:choose>
            </td>
            <td>
              <c:choose>
                <c:when test="${item.oscillating == true}">
                  <span class="label label-danger">
                  <i class="fa fa-bell"></i> OSCILLATING
                  </span>
                </c:when>
              </c:choose>
            </td>
            <td>
                <a href="/c2mon-web-ui/alarmviewer/id/${item.id}" class="view-tag btn btn-default btn-sm">
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
            if(oldUrl.includes("c2mon-web-ui/laseralarmevents")) {
               history.go(-1);
            }else{
               location.href = "${alarmdefinitionviewer}"
            }
        });
    });
</script>