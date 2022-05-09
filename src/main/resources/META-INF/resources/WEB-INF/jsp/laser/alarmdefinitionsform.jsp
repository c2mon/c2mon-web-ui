<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib prefix="c2mon" tagdir="/WEB-INF/tags"%>

<c2mon:template title="${title}">
  <div class="row">
    <div class="col-lg-12">
      <ul class="breadcrumb">
        <li>
          <a href="../">Home</a>
          <span class="divider"></span>
        </li>
        <li>Alarm Definitions Viewer</li>
      </ul>

      <div class="page-header">
        <h1>Alarm Definitions Viewer</h1>
      </div>

      <c:if test="${error != null}">
        <div class="alert alert-danger">
          User configuration with name <strong>${error}</strong> could not be found.
        </div>
      </c:if>

      <c:url var="submitUrl" value="${formSubmitUrl}" />

      <form class="well form-inline" action="${submitUrl}" method="post" onsubmit="this.submit.disabled = true;">
        <div class="input-group">
            <div class="input-group-addon">User Configurations</div>
            <select name="id" class="form-control">
              <c:forEach items="${laseruserconfigs}" var="userConfig">
                <option >${userConfig.configName}</option>
              </c:forEach>
            </select>
       </div>
       <input class="btn btn-large btn-primary" type="submit" value="Submit" name="submit">
      </form>
    </div>
  </div>
  <!--/row-->
</c2mon:template>

