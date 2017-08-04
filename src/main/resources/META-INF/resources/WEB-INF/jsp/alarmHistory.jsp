<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>
<%@ taglib prefix="c2mon" tagdir="/WEB-INF/tags"%>

<!-- JSP variables -->
<c:set var="alarm" value="${history[0]}" />
<c:url var="home" value="../" />
<c:url var="historyviewer" value="../alarmhistoryviewer/form" />
<c:url var="tagviewer" value="../tagviewer/${alarm.tagId}" />
<c:url var="taghistory" value="../historyviewer/${alarm.tagId}" />
<c:url var="alarmviewer" value="../alarmviewer/${alarm.id}" />

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
      </p>
    </div>
  </div>

  <div class="row">
    <div class="col-lg-12">
      <table class="table table-striped table-bordered">
        <thead>
        <tr>
          <th width="250">Timestamp</th>
          <th width="150">Status</th>
          <th >Info</th>
        </tr>
        </thead>

        <tbody>
        <c:forEach var="item" items="${history}">

          <tr>
            <td>
              <script type="text/javascript">
                document.write('${item.timestamp}'.replace("T", " "));
              </script>
            </td>
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
            <td>${item.info}</td>
          </tr>
        </c:forEach>

        </tbody>
      </table>
    </div>
  </div>

  <script type="text/javascript">

    function viewTagHistory() {
      var url = '${taghistory}' + '?' + window.location.href.split('?')[1];
      window.location.href = url;
    }

  </script>

</c2mon:template>
