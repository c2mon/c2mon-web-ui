<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>
<%@ taglib prefix="c2mon" tagdir="/WEB-INF/tags"%>

<!-- JSP variables -->
<c:set var="command" value="${history[0]}" />
<c:set var="commandId" value="${commandId}" />
<c:url var="home" value="../" />
<c:url var="historyviewer" value="../commandhistory/form" />
<c:url var="commandviewer" value="../commandviewer/${commandId}" />
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
          <a href="<c:url value="${historyviewer}"/>">Command Tag History</a>
          <span class="divider"></span>
        </li>
        <li>${command.id}</li>
      </ul>
    </div>
  </div>

  <div class="row">
    <div class="col-lg-12">
      <div class="page-header">
        <h2>
          CommandTag: ${command.name} ( ${commandId} ) <small>${description}</small>
        </h2>
      </div>
    </div>
  </div>

  <div class="row">
    <div class="col-lg-12">
      <p class="pull-left btn-toolbar">
        <a href="${commandviewer}" class="btn btn-default btn-large">
          <span class="glyphicon glyphicon-tags"></span>
          &nbsp;View Command Tag
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
              <th class="col-md-1">Value</th>
              <th class="col-md-1">User</th>
              <th class="col-md-1">Host</th>
              <th class="col-md-2">Report Time</th>
              <th class="col-md-1">Report Status</th>
              <th class="col-md-5">Report Description</th>
            </tr>
            </thead>

            <tbody>
            <c:forEach var="item" items="${history}">
              <tr>
               <td>
                  <script type="text/javascript">
                    document.write('${item.time}'.replace("T", " "));
                  </script>
                </td>
                <td>${item.value}</td>
                <td>${item.user}</td>
                <td>${item.host}</td>
                <td>
                  <script type="text/javascript">
                    document.write('${item.reportTime}'.replace("T", " "));
                  </script>
                 </td>
                <td>${item.reportStatus}</td>
                <td>${item.reportDesc}</td>
              </tr>
            </c:forEach>
            </tbody>
          </table>
        </div>
      </div>
  </div>

  <script type="text/javascript">

    function viewTagHistory() {
      var url = '${taghistory}' + '?' + window.location.href.split('?')[1];
      window.location.href = url;
    }

  </script>

</c2mon:template>
