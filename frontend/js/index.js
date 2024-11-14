$(function () {
  let calendarIndex = 0;
  let baseUrl = 'http://localhost:8080/api/'

  function loadJobs() {
    $.ajax({
      url: baseUrl + 'getAllJobs',
      method: 'GET',
      success: function (jobs) {
        let jobTableBody = $('#jobTableBody');
        jobTableBody.empty();

        jobs.forEach(function (job) {
          let row = `
                    <tr data-id="${job.jobId}">
                        <td>${job.jobId}</td>
                        <td id="name_${job.jobId}">${job.jobName}</td>
                        <td id="group_${job.jobId}">${job.jobGroup}</td>
                        <td id="cron_${job.jobId}">${job.cronExpression}</td>
                        <td id="status_${job.jobId}">${job.jobStatus}</td>
                        <td id="desc_${job.jobId}">${job.jobDescription}</td>
                        <td style="text-align: center;">
                            <div class="btn-group text-center" role="group" data-id="${job.jobId}">
                                <button type="button" class="btn btn-default btnRun" data-id="${job.jobId}">Run Once</button>
                                <button type="button" class="btn btn-default btnPause" data-id="${job.jobId}">Pause</button>
                                <button type="button" class="btn btn-default btnResume" data-id="${job.jobId}">Resume</button>
                                <button type="button" class="btn btn-default btnView" onclick='viewJob(${JSON.stringify(job)})'>View</button>
                                <button type="button" class="btn btn-default btnEdit" onclick='editJob(${JSON.stringify(job)})'>Edit</button>
                                <button type="button" class="btn btn-warning btnDelete" data-id="${job.jobId}">Delete</button>
                            </div>
                        </td>
                    </tr>
                    `;

          jobTableBody.append(row);
        });

        attachEventHandlers();
      },
      error: function (error) {
        console.log('Error fetching jobs:', error);
      }
    });
  }

  function attachEventHandlers() {
    $(".btnRun").click(function () {
      const jobId = $(this).parent().data("id");
      $.ajax({
        url: baseUrl + 'runJob?t=' + new Date().getTime(),
        type: "POST",
        data: {
          "jobName": $("#name_" + jobId).text(),
          "jobGroup": $("#group_" + jobId).text()
        },
        success: function (res) {
          if (res.valid) {
            alert("Run success!");
          } else {
            alert(res.msg);
          }
        }
      });
    });

    $(".btnPause").click(function () {
      const jobId = $(this).parent().data("id");
      $.ajax({
        url: baseUrl + 'pauseJob?t=' + new Date().getTime(),
        type: "POST",
        data: {
          "jobName": $("#name_" + jobId).text(),
          "jobGroup": $("#group_" + jobId).text()
        },
        success: function (res) {
          if (res.valid) {
            alert("Pause success!");
            location.reload();
          } else {
            alert(res.msg);
          }
        }
      });
    });

    $(".btnResume").click(function () {
      const jobId = $(this).parent().data("id");
      $.ajax({
        url: baseUrl + 'resumeJob?t=' + new Date().getTime(),
        type: "POST",
        data: {
          "jobName": $("#name_" + jobId).text(),
          "jobGroup": $("#group_" + jobId).text()
        },
        success: function (res) {
          if (res.valid) {
            alert("Resume success!");
            location.reload();
          } else {
            alert(res.msg);
          }
        }
      });
    });

    $(".btnDelete").click(function () {
      const jobId = $(this).parent().data("id");
      $.ajax({
        url: baseUrl + 'deleteJob?t=' + new Date().getTime(),
        type: "POST",
        data: {
          "jobName": $("#name_" + jobId).text(),
          "jobGroup": $("#group_" + jobId).text()
        },
        success: function (res) {
          if (res.valid) {
            alert("Delete success!");
            location.reload();
          } else {
            alert(res.msg);
          }
        }
      });
    });
  }

  window.viewJob = function (job) {
    $("#myModalLabel").html("View Job");

    $("#jobId").val(job.jobId);
    $("#edit_name").val(job.jobName).prop("disabled", true);
    $("#edit_group").val(job.jobGroup).prop("disabled", true);
    $("#edit_cron").val(job.cronExpression).prop("disabled", true);
    $("#edit_desc").val(job.jobDescription).prop("disabled", true);

    $("#jobCalendarTable tbody").empty();
    calendarIndex = 0;

    job.jobCalendars.forEach(function (calendar) {
      const newRow = document.createElement('tr');
      newRow.innerHTML = `
                <td><input type="text" class="form-control" name="calendarName[]" value="${calendar.calendarName}" disabled></td>
                <td>
                    <select class="form-control" name="calendarType[]" disabled>
                        <option value="ANNUAL_CALENDAR" ${calendar.calendarType === 'ANNUAL_CALENDAR' ? 'selected' : ''}>Annual Calendar</option>
                        <option value="CRON_CALENDAR" ${calendar.calendarType === 'CRON_CALENDAR' ? 'selected' : ''}>Cron Calendar</option>
                        <option value="DAILY_CALENDAR" ${calendar.calendarType === 'DAILY_CALENDAR' ? 'selected' : ''}>Daily Calendar</option>
                        <option value="HOLIDAY_CALENDAR" ${calendar.calendarType === 'HOLIDAY_CALENDAR' ? 'selected' : ''}>Holiday Calendar</option>
                        <option value="WEEKLY_CALENDAR" ${calendar.calendarType === 'WEEKLY_CALENDAR' ? 'selected' : ''}>Weekly Calendar</option>
                    </select>
                </td>
                <td><input type="text" class="form-control valueField" name="dateAndTime[]" value="${calendar.dateAndTime}" disabled></td>
            `;
      $("#jobCalendarTable tbody").append(newRow);
      calendarIndex++;
    });

    $("#jobCalendarTable thead tr th:last-child").hide();
    $("#addRow").hide();
    $("#save").hide();

    $("#myModal").modal("show");
  }

  window.editJob = function (job) {
    $("#myModalLabel").html("Edit Job");

    $("#jobId").val(job.jobId);
    $("#edit_name").val(job.jobName).prop("disabled", false);
    $("#edit_group").val(job.jobGroup).prop("disabled", false);
    $("#edit_cron").val(job.cronExpression).prop("disabled", false);
    $("#edit_desc").val(job.jobDescription).prop("disabled", false);

    $("#jobCalendarTable tbody").empty();
    calendarIndex = 0;

    job.jobCalendars.forEach(function (calendar) {
      const newRow = document.createElement('tr');
      newRow.innerHTML = `
                <td><input type="text" class="form-control" name="jobCalendars[${calendarIndex}].calendarName" value="${calendar.calendarName}" placeholder="Enter Calendar Name"></td>
                <td>
                    <select class="form-control calendarType" name="jobCalendars[${calendarIndex}].calendarType" data-index="${calendarIndex}">
                        <option value="ANNUAL_CALENDAR" ${calendar.calendarType === 'ANNUAL_CALENDAR' ? 'selected' : ''}>Annual Calendar</option>
                        <option value="CRON_CALENDAR" ${calendar.calendarType === 'CRON_CALENDAR' ? 'selected' : ''}>Cron Calendar</option>
                        <option value="DAILY_CALENDAR" ${calendar.calendarType === 'DAILY_CALENDAR' ? 'selected' : ''}>Daily Calendar</option>
                        <option value="HOLIDAY_CALENDAR" ${calendar.calendarType === 'HOLIDAY_CALENDAR' ? 'selected' : ''}>Holiday Calendar</option>
                        <option value="WEEKLY_CALENDAR" ${calendar.calendarType === 'WEEKLY_CALENDAR' ? 'selected' : ''}>Weekly Calendar</option>
                    </select>
                </td>
                <td><input type="text" class="form-control valueField" name="jobCalendars[${calendarIndex}].dateAndTime" value="${calendar.dateAndTime}" data-index="${calendarIndex}"></td>
                <td><button type="button" class="btn btn-danger btn-sm" onclick="removeRow(this)">Remove</button></td>
            `;
      $("#jobCalendarTable tbody").append(newRow);

      const selectElement = newRow.querySelector('select');
      const valueField = newRow.querySelector('input.valueField');

      selectElement.addEventListener('change', function () {
        const selectedType = this.value;
        updateValueField(selectedType, valueField);
      });

      updateValueField(calendar.calendarType, valueField);

      calendarIndex++;
    });

    $("#jobCalendarTable thead tr th:last-child").show();
    $("#addRow").show();
    $("#save").show();

    $("#myModal").modal("show");
  }

  $("#save").click(function () {
    $.ajax({
      url: baseUrl + 'saveOrUpdate?t=' + new Date().getTime(),
      type: "POST",
      data: $('#mainForm').serialize(),
      success: function (res) {
        if (res.valid) {
          alert("Success!");
          location.reload();
        } else {
          alert(res.msg);
        }
      }
    });
  });

  $("#createBtn").click(function () {
    $("#myModalLabel").html("Create Job");

    $("#jobId").val("").prop("disabled", false);
    $("#edit_name").val("").prop("disabled", false);
    $("#edit_group").val("").prop("disabled", false);
    $("#edit_cron").val("").prop("disabled", false);
    $("#edit_desc").val("").prop("disabled", false);

    $("#jobCalendarTable tbody").empty();
    calendarIndex = 0;

    $("#jobCalendarTable thead tr th:last-child").show();
    $("#addRow").show();
    $("#save").show();

    $("#myModal").modal("show");
  });

  function updateValueField(selectedType, valueField) {
    switch (selectedType) {
      case 'ANNUAL_CALENDAR':
      case 'HOLIDAY_CALENDAR':
        valueField.placeholder = 'YYYY.MM.DD (e.g: 2024.01.01)';
        break;
      case 'CRON_CALENDAR':
        valueField.placeholder = 'Cron Expression (e.g: 0 0/5 * * * ?)';
        break;
      case 'DAILY_CALENDAR':
        valueField.placeholder = 'HH:mm-HH:mm (e.g: 14:00-16:00)';
        break;
      case 'WEEKLY_CALENDAR':
        valueField.placeholder = 'Day of Week (e.g: MONDAY)';
        break;
      default:
        valueField.placeholder = 'Enter Value';
    }
    valueField.value = '';
  }

  $('#addRow').click(function () {
    const newRow = `
            <tr>
                <td><input type="text" class="form-control" name="jobCalendars[${calendarIndex}].calendarName" placeholder="Enter Calendar Name"></td>
                <td>
                    <select class="form-control calendarType" name="jobCalendars[${calendarIndex}].calendarType" data-index="${calendarIndex}">
                        <option value="ANNUAL_CALENDAR">Annual Calendar</option>
                        <option value="CRON_CALENDAR">Cron Calendar</option>
                        <option value="DAILY_CALENDAR">Daily Calendar</option>
                        <option value="HOLIDAY_CALENDAR">Holiday Calendar</option>
                        <option value="WEEKLY_CALENDAR">Weekly Calendar</option>
                    </select>
                </td>
                <td><input type="text" class="form-control valueField" name="jobCalendars[${calendarIndex}].dateAndTime" placeholder="Enter Value" data-index="${calendarIndex}"></td>
                <td><button type="button" class="btn btn-danger btn-sm" onclick="removeRow(this)">Delete</button></td>
            </tr>
        `;
    $("#jobCalendarTable tbody").append(newRow);

    const selectElement = $(`select[name='jobCalendars[${calendarIndex}].calendarType']`);
    const valueField = $(`input[name='jobCalendars[${calendarIndex}].dateAndTime']`);

    selectElement.change(function () {
      const selectedType = $(this).val();
      updateValueField(selectedType, valueField[0]);
    });

    updateValueField(selectElement.val(), valueField[0]);

    calendarIndex++;
  });

  window.removeRow = function (button) {
    $(button).closest('tr').remove();
  };

  loadJobs();
});
