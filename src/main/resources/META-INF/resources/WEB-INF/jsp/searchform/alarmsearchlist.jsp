<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<%@ taglib prefix="c2mon" tagdir="/WEB-INF/tags"%>

<!-- JSP variables -->
<c:url var="home" value="../" />
<c:url var="genericForm" value="../form" />

<c2mon:template title="${title}">

    <style type="text/css">
    .hiddenRow tr {
      padding: 0 !important;
      cursor: default;
    }

    .hiddenRow:hover {
      background-color: #fff;
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
              <a href="<c:url value="${equipmentviewer}"/>">${title}</a>
            </li>
          </ul>
        </div>
     </div>

     <h3>Alarm Search Result</h3>

    <div class="table-responsive">
      <table class="table table-bordered table-hover" style="border-collapse: collapse;">
        <thead>
          <tr>
            <th class="col-md-1">ID</th>
            <th class="col-md-5">Fault Family</th>
            <th class="col-md-5">Fault Member</th>
            <th class="col-md-5">Fault Code</th>
          </tr>
        </thead>

        <tbody class="searchable">

          <c:forEach items="${alarms}" var="entry">
            <c:set var="alarm" value="${entry}"></c:set>

            <tr data-toggle="collapse" data-target="#collapse-tag-${alarm.id}" class="accordion-toggle clickable">
              <td>${alarm.id}</td>
              <td>${alarm.faultFamily}</td>
              <td>${alarm.faultMember}</td>
              <td>${alarm.faultCode}</td>
              <td>
                <a href="/c2mon-web-ui/alarmviewer/id/${alarm.id}" class="view-tag btn btn-default btn-sm">
                  <i class="fa fa-external-link"></i>
                  View Alarm
                </a>
              </td>
            </tr>
          </c:forEach>
        </tbody>
      </table>
    </div>
</c2mon:template>