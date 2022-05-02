<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<%@ taglib prefix="c2mon" tagdir="/WEB-INF/tags"%>

<c2mon:template title="${title}">
  <div class="row">
    <div class="col-lg-12">
      <ul class="breadcrumb">
        <li>
          <a href="../">Home</a>
          <span class="divider"></span>
        </li>
        <li>${title}</li>
      </ul>

      <div class="page-header">
        <h1>${title}</h1>
      </div>

      <div class="alert alert-danger">
         <strong> ${err} </strong>
      </div>

      <ul id="tabs" class="nav nav-tabs" role="tablist">
        <li role="presentation" class="active">
          <a href="#id" role="tab" data-toggle="tab" aria-controls="id">Search by ID</a>
        </li>
        <li role="presentation">
          <a href="#name" role="tab" data-toggle="tab" aria-controls="name">Search by Name</a>
        </li>
      </ul>
      <div id="tab-content" class="tab-content">
          <div class="tab-pane fade in active" id="id">
            <form class="well form-inline" action="" method="post" onsubmit="this.submit.disabled = true;">
                <div class="alert alert-info">
                   <strong>Enter a Alarm ID to view the Alarm's configuration</strong>
                </div>
              <input class="form-control" style="display: inline" type="text" name="id" id="id" value="${formTagValue}" oninput="numOnly(this.id);" placeholder="${formTagPlaceHolder} ID" maxlength="9" pattern="[0-9]{1,9}"/>
            <input class="btn btn-large btn-primary" type="submit" value="Submit" name="submit">
            </form>
          </div>

          <div class="tab-pane fade" id="name">
            <form class="well form-inline" action="" method="post" onsubmit="this.submit.disabled = true;">
               <div class="alert alert-info">
                   <strong>Enter the Fault Family, Fault Member, Fault Code to view the Alarm's configuration</strong>
               </div>
               <div class="input-group">
                 <div class="input-group-addon">Fault Family</div>
                 <input class="form-control" style="display: inline" type="text" name="faultFamily" value="${formTagValue}"/>
               </div>
               <div class="input-group">
                 <div class="input-group-addon">Fault Member</div>
                 <input class="form-control" style="display: inline" type="text" name="faultMember" value="${formTagValue}"/>
               </div>
               <div class="input-group">
                 <div class="input-group-addon">Fault Code</div>
                 <input class="form-control" style="display: inline" type="text" name="faultCode" id="faultCode" oninput="numOnly(this.id);" maxlength="9" pattern="[0-9]{1,9}"/>
               </div>
               <input class="btn btn-large btn-primary" type="submit" value="Submit" name="submit">
            </form>
          </div>
        </div>
      </div>
  </div>
  <!--/row-->
</c2mon:template>
<script type="text/javascript">
function numOnly(id) {
    // Get the element by id
    var element = document.getElementById(id);
    // Use numbers only pattern, from 0 to 9 with \-
    var regex = /[^0-9\-]/gi;
    // Replace other characters that are not in regex pattern
    element.value = element.value.replace(regex, "");
}

 // Tab click handler
  $('#tabs a').click(function(e) {
    e.preventDefault();
    $(this).tab('show');
  });

  // Store the currently selected tab in the hash value
  $("ul.nav-tabs > li > a").on("shown.bs.tab", function(e) {
    var id = $(e.target).attr("href").substr(1);
    window.location.hash = id;
  });

  // on load of the page: switch to the currently selected tab
  var hash = window.location.hash;
  $('#tabs a[href="' + hash + '"]').tab('show');
</script>

