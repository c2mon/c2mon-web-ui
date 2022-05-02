<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>
<%@ taglib prefix="c2mon" tagdir="/WEB-INF/tags"%>

<!-- JSP variables -->
<c:url var="home" value="../" />
<c:set var="entity" value="${history[0]}" />
<c:url var="historyviewer" value="../supervisionhistoryviewer/form" />
<c:url var="xmlviewer" value="./xml/${entity.id}" />
<c:url var="csvviewer" value="./csv/${entity.id}" />
<c:url var="tagviewer" value="../tagviewer/id/${entity.id}" />
<c:url var="trend" value="/trendviewer/${entity.id}" />

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
          <a href="<c:url value="${historyviewer}"/>">Supervision Log History</a>
          <span class="divider"></span>
        </li>
        <li>${entity.id}</li>
      </ul>
    </div>
  </div>

  <div class="row">
    <div class="col-lg-12">
      <div class="page-header">
        <h2>
          Supervision History: ${entity.sul_entity} ${entity.id} <small>${description}</small>
        </h2>
      </div>
    </div>
  </div>

  <div class="row">
    <div class="col-lg-12">
      <table class="table table-striped table-bordered">
        <thead>
        <tr>
          <th>Timestamp</th>
          <th>Status</th>
          <th>Message</th>
        </tr>
        </thead>

        <tbody>
        <c:forEach var="item" items="${history}">

          <tr>
           <td>
             <script type="text/javascript">
               document.write('${item.eventTime}'.replace("T", " "));
             </script>
           </td>
            <td>
                <c:choose>
                  <c:when test="${item.status == 'DOWN'}">
                    <span class="label label-danger">
                    <i class="fa fa-bell"></i> ${item.status}
                    </span>
                  </c:when>
                  <c:when test="${item.status == 'STARTUP'}">
                    <span class="label label-warning">
                    <i class="fa fa-bell"></i> ${item.status}
                    </span>
                  </c:when>
                  <c:when test="${item.status == 'RUNNING'}">
                    <span class="label label-success">
                    <i class="fa fa-bell"></i> ${item.status}
                    </span>
                  </c:when>
                  <c:otherwise>
                    ${item.status}
                  </c:otherwise>
                </c:choose>
            </td>
            <td>${item.sul_message}</td>
          </tr>
        </c:forEach>

        </tbody>
      </table>
    </div>
  </div>
</c2mon:template>
