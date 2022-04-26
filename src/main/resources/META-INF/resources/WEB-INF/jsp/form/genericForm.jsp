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

      <ul id="tabs" class="nav nav-tabs">
        <li>
          <a href="#id" data-toggle="tab">Search by ID</a>
        </li>
        <li>
          <a href="#name" data-toggle="tab">Search by Name</a>
        </li>
      </ul>
      <div id="tab-content" class="tab-content">
          <div class="tab-pane fade in active" id="id">
            <form class="well form-inline" action="" method="post" onsubmit="this.submit.disabled = true;">
                <div class="alert alert-info">
                   <strong>Enter a ${formTagPlaceHolder} ID to view the ${formTagPlaceHolder}'s configuration</strong>
                </div>
              <input class="form-control "style="display: inline" type="text" id="number" oninput="numOnly(this.id);" name="id" placeholder="${formTagPlaceHolder} ID" maxlength="9" pattern="[0-9]{1,9}"/>

            <input class="btn btn-large btn-primary" type="submit" value="Submit" name="submit">
            </form>
          </div>

          <div class="tab-pane fade" id="name">
            <form class="well form-inline" action="" method="post" onsubmit="this.submit.disabled = true;">
               <div class="alert alert-info">
                    <strong>Enter a ${formTagPlaceHolder} Name to view the ${formTagPlaceHolder}'s configuration</strong>
               </div>
              <input class="form-control" style="display: inline" type="text" name="name" value="${formTagValue}" placeholder="${formTagPlaceHolder} Name" />
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
</script>


