<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>
<%@ taglib prefix="c2mon" tagdir="/WEB-INF/tags"%>

<!-- JSP variables -->
<c:url var="home" value="../" />
<c:url var="alarmstateviewer" value="../form" />

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
          <a href="<c:url value="${alarmstateviewer}"/>">${title}</a>
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
          Active alarms for user configuration: ${configName} <small>${description}</small>
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
          <th width="100">Time</th>
          <th width="100">Alarm Id</th>
          <th width="200">Alarm Name</th>
          <th width="100">Active</th>
          <th width="100">Oscillating</th>
        </tr>
        </thead>

        <tbody>
        <c:forEach var="item" items="${activeAlarms}">

          <tr>
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
          </tr>
        </c:forEach>
        </tbody>
      </table>
    </div>
  </div>
</c2mon:template>
