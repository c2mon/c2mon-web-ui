<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib prefix="c2mon" tagdir="/WEB-INF/tags"%>

<c2mon:template title="${title}">

<link rel="stylesheet" type="text/css" href="../css/datepicker.css">

  <div class="row">
    <div class="col-lg-12">
      <ul class="breadcrumb">
        <li>
          <a href="../">Home</a>
          <span class="divider"></span>
        </li>
        <li>Alarm Events Viewer</li>
      </ul>

      <div class="page-header">
        <h1>Alarm Events Viewer</h1>
      </div>

      <c:url var="submitUrl" value="${formSubmitUrl}" />

        <div>
          <form class="well form-inline" action="" method="post" onsubmit="this.submit.disabled = true;">
             <div class="input-group" style="padding: 6px">
                <div class="input-group-addon">User Configurations</div>
                <select name="id" class="form-control">
                  <c:forEach items="${laseruserconfigs}" var="userConfig">
                    <option>${userConfig.configName}</option>
                  </c:forEach>
                </select>
             </div>

            </br>

             <div class="input-group" style="padding: 6px">
               <span class="input-group-addon">From</span>
               <span class="input-daterange" id="datepickerFrom" style="display: inline;">
                 <input type="date" class="form-control" name="start" value="${defaultFromDate}" />
               </span>
             </div>
             <div class="input-group col-lg-1">
               <input type="time" class="form-control" name="startTime" value="${defaultFromTime}" min="00:00" max="23:59" />
             </div>

             <div class="input-group " style="padding: 6px">
               <span class="input-group-addon">To</span>
               <span class="input-daterange" id="datepickerTo" style="display: inline;">
                 <input type="date" class="form-control" name="end" value="${defaultToDate}" />
               </span>
             </div>
             <div class="input-group col-lg-1">
               <input type="time" class="form-control" name="endTime" value="${defaultToTime}" min="00:00" max="23:59" />
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

