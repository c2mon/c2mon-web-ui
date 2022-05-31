<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib prefix="c2mon" tagdir="/WEB-INF/tags"%>
<%
    response.setHeader("Cache-Control", "max-age=604800");
%>

<c2mon:template title="${title}">

<link rel="stylesheet" type="text/css" href="../css/datepicker.css">

  <div class="row">
    <div class="col-lg-12">
      <ul class="breadcrumb">
        <li>
          <a href="../">Home</a>
          <span class="divider"></span>
        </li>
        <li>Alarm State Viewer</li>
      </ul>

      <div class="page-header">
        <h1>Alarm State Viewer</h1>
      </div>

      <c:if test="${error != null}">
        <div class="alert alert-danger">
          User configuration <strong>${error}</strong> could not be found.
        </div>
      </c:if>

      <c:url var="submitUrl" value="${formSubmitUrl}" />

        <div>
          <form class="well form-inline" action="" method="post" >
             <div class="input-group col-xs-2">
                <div class="input-group-addon">User Configurations</div>
                <select id="id" name="id" class="form-control">
                  <c:forEach items="${laseruserconfigs}" var="userConfig">
                    <option>${userConfig.configName}</option>
                  </c:forEach>
                </select>
             </div>

            <div class="input-group">
              <span class="input-group-addon">At</span>
              <span class="input-daterange" id="datepickerFrom" style="display: inline;">
                <input type="text" class="form-control" name="date" value="${defaultAtDate}" />
              </span>
            </div>
            <div class="input-group col-lg-1">
              <input type="text" class="form-control" name="time" value="${defaultAtTime}" />
            </div>

             </br>

           <div class="input-group mb-3" style="padding: 6px">
            <div class="input-group-addon">Priority</div>
            <label id="toggle-invalid" class="btn btn-default navbar-btn" style="margin-bottom: 0px; margin-top: 0px; padding: 6px">
                  <input type="checkbox" name="priority" value="0" checked> 0
             </label>
             <label id="toggle-invalid" class="btn btn-default navbar-btn" style="margin-bottom: 0px; margin-top: 0px; margin-left: 5px; padding: 6px">
                  <input type="checkbox" name="priority" value="1" checked> 1
              </label>
             <label id="toggle-invalid" class="btn btn-default navbar-btn" style="margin-bottom: 0px; margin-top: 0px; margin-left: 5px; padding: 6px">
                  <input type="checkbox" name="priority" value="2" checked> 2
             </label>
             <label id="toggle-invalid" class="btn btn-default navbar-btn" style="margin-bottom: 0px; margin-top: 0px; margin-left: 5px; padding: 6px">
                  <input type="checkbox" name="priority" value="3" checked> 3
             </label>
           </div>
            </br>
            <div class="input-group"  style="padding: 6px">
                <div class="input-group-addon">Search Text</div>
                <input class="form-control" style="display: inline" type="text" name="textSearch" />
            </div>

            <input class="btn btn-large btn-primary" type="submit" value="Submit" name="submit">
          </form>
        </div>
    </div>
  </div>
  <!--/row-->
</c2mon:template>
<script type="text/javascript">
var content = localStorage.getItem("user_config")

var optionToSelect = null

if(${configName != null}){
     optionToSelect = '${configName}'
}else
    if(content != null){
        optionToSelect = content
    }

if(optionToSelect != null){
    var mySelect = document.getElementById('id');

    for(var i, j = 0; i = mySelect.options[j]; j++) {
        if(i.text == optionToSelect) {
            mySelect.selectedIndex = j;
            break;
        }
    }
}
</script>
