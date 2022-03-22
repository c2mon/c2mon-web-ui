<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>
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
        <li>${title}</li>
      </ul>

      <div class="page-header">
        <h1>${title}</h1>
      </div>

      <div class="alert alert-info">
        <strong>${instruction}</strong>
      </div>

      <c:if test="${error != null}">
        <div class="alert alert-danger">
          id: <strong>${error}</strong> could not be found.
        </div>
      </c:if>

      <ul id="tabs" class="nav nav-tabs">
        <li>
          <a href="#records" data-toggle="tab">Last Records</a>
        </li>
        <li>
          <a href="#days" data-toggle="tab">Last Days</a>
        </li>
        <li>
          <a href="#date" data-toggle="tab">From date to date</a>
        </li>
      </ul>
      <div id="tab-content" class="tab-content">

        <div class="tab-pane fade in active" id="records">
          <form class="well form-inline" action="" method="post">
            <div class="input-group">
              <div class="input-group-addon">Id</div>
              <input class="form-control" type="text" name="id" value="${formTagValue}" />
            </div>
            <div class="input-group">
              <div class="input-group-addon">Records</div>
              <input class="form-control" type="text" name="records" value="100" />
            </div>
            <input class="btn btn-large btn-primary" type="submit" value="Submit">
          </form>
        </div>

        <div class="tab-pane fade" id="days">
          <form class="well form-inline" action="" method="post">
            <div class="input-group">
              <div class="input-group-addon">Id</div>
              <input class="form-control" type="text" name="id" value="${formTagValue}" />
            </div>
            <div class="input-group">
              <div class="input-group-addon">Days</div>
              <select class="form-control" name="days">
                <option>1</option>
                <option>2</option>
                <option>3</option>
                <option>4</option>
                <option>5</option>
                <option>6</option>
                <option>7</option>
                <option>8</option>
                <option>9</option>
                <option>10</option>
                <option>11</option>
                <option>12</option>
                <option>13</option>
                <option>14</option>
                <option>15</option>
                <option>16</option>
                <option>17</option>
                <option>18</option>
                <option>19</option>
                <option>20</option>
                <option>21</option>
                <option>22</option>
                <option>23</option>
                <option>24</option>
                <option>25</option>
                <option>26</option>
                <option>27</option>
                <option>28</option>
                <option>29</option>
                <option>30</option>
              </select>
            </div>
            <input class="btn btn-large btn-primary" type="submit" value="Submit">
          </form>
        </div>

        <div class="tab-pane fade" id="date">
          <form class="well form-inline" action="" method="post">
            <div class="input-group col-xs-2">
              <span class="input-group-addon">Id</span>
              <input class="form-control" type="text" name="id" value="${formTagValue}" />
            </div>

            <div class="input-group">
              <span class="input-group-addon">From</span>
              <span class="input-daterange" id="datepickerFrom" style="display: inline;">
                <input type="text" class="form-control" name="start" value="${defaultFromDate}" />
              </span>
            </div>
            <div class="input-group col-lg-1">
              <input type="text" class="form-control" name="startTime" value="${defaultFromTime}" />
            </div>

            <div class="input-group ">
              <span class="input-group-addon">To</span>
              <span class="input-daterange" id="datepickerTo" style="display: inline;">
                <input type="text" class="form-control" name="end" value="${defaultToDate}" />
              </span>
            </div>
            <div class="input-group col-lg-1">
              <input type="text" class="form-control" name="endTime" value="${defaultToTime}" />
            </div>

            <input class="btn btn-large btn-primary" type="submit" value="Submit">
          </form>
        </div>
      </div>
    </div>
  </div>
  <!--/row-->

</c2mon:template>

  <script src="../js/bootstrap/bootstrap-datepicker.js"></script>

  <script type="text/javascript">
      $('#tabs a:first').tab('show'); // Marks the first tab as selected in the interface 

      // Activates the datepicker plugin
      // https://github.com/eternicode/bootstrap-datepicker
      $('.input-daterange').datepicker({
        todayBtn : true,
        forceParse : false,
        autoclose : true,
        format : "dd/mm/yyyy", // Format should match the format expected in the TrendViewController class
        todayHighlight : true,
        beforeShowDay : function(date) {
          if (date.getMonth() == (new Date()).getMonth())
            switch (date.getDate()) {
            case 8:
              return false;
            case 12:
              return "green";
            }
        }
      });
    </script>
