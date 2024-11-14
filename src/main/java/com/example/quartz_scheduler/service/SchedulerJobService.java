package com.example.quartz_scheduler.service;

import com.example.quartz_scheduler.component.JobScheduleCreator;
import com.example.quartz_scheduler.entity.JobCalendar;
import com.example.quartz_scheduler.entity.SchedulerJobInfo;
import com.example.quartz_scheduler.job.SampleCronJob;
import com.example.quartz_scheduler.repository.SchedulerRepository;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.quartz.Calendar;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SimpleTrigger;
import org.quartz.Trigger;
import org.quartz.TriggerKey;
import org.quartz.impl.calendar.AnnualCalendar;
import org.quartz.impl.calendar.CronCalendar;
import org.quartz.impl.calendar.DailyCalendar;
import org.quartz.impl.calendar.HolidayCalendar;
import org.quartz.impl.calendar.WeeklyCalendar;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Transactional
@Service
@RequiredArgsConstructor
public class SchedulerJobService {

  private final SchedulerFactoryBean schedulerFactoryBean;
  private final SchedulerRepository schedulerRepository;
  private final ApplicationContext context;
  private final JobScheduleCreator scheduleCreator;

  public List<SchedulerJobInfo> getAllJobList() {
    return schedulerRepository.findAll();
  }

  public void deleteJob(SchedulerJobInfo jobInfo) throws SchedulerException {
    SchedulerJobInfo getJobInfo = schedulerRepository.findByJobName(jobInfo.getJobName());
    schedulerRepository.delete(getJobInfo);
    schedulerFactoryBean.getScheduler().deleteJob(new JobKey(jobInfo.getJobName(), jobInfo.getJobGroup()));
    log.info(">>>>> jobName = [{}] deleted.", jobInfo.getJobName());
  }

  public void pauseJob(SchedulerJobInfo jobInfo) throws SchedulerException {
    SchedulerJobInfo getJobInfo = schedulerRepository.findByJobName(jobInfo.getJobName());
    getJobInfo.setJobStatus("PAUSED");
    schedulerRepository.save(getJobInfo);
    schedulerFactoryBean.getScheduler().pauseJob(new JobKey(jobInfo.getJobName(), jobInfo.getJobGroup()));
    log.info(">>>>> jobName = [{}] paused.", jobInfo.getJobName());
  }

  public void resumeJob(SchedulerJobInfo jobInfo) throws SchedulerException {
    SchedulerJobInfo getJobInfo = schedulerRepository.findByJobName(jobInfo.getJobName());
    getJobInfo.setJobStatus("RESUMED");
    schedulerRepository.save(getJobInfo);
    schedulerFactoryBean.getScheduler().resumeJob(new JobKey(jobInfo.getJobName(), jobInfo.getJobGroup()));
    log.info(">>>>> jobName = [{}] resumed.", jobInfo.getJobName());
  }

  public void startJobNow(SchedulerJobInfo jobInfo) throws SchedulerException {
    SchedulerJobInfo getJobInfo = schedulerRepository.findByJobName(jobInfo.getJobName());
    getJobInfo.setJobStatus("SCHEDULED & STARTED");
    schedulerRepository.save(getJobInfo);
    schedulerFactoryBean.getScheduler().triggerJob(new JobKey(jobInfo.getJobName(), jobInfo.getJobGroup()));
    log.info(">>>>> jobName = [{}] scheduled and started.", jobInfo.getJobName());
  }

  public void saveOrUpdate(SchedulerJobInfo scheduleJob) throws Exception {
    if (scheduleJob.getJobCalendars() != null) {
      for (JobCalendar calendar : scheduleJob.getJobCalendars()) {
        calendar.setSchedulerJobInfo(scheduleJob);
      }
    }

    scheduleJob.setJobClass(SampleCronJob.class.getName());

    if (scheduleJob.getJobId() == null) {
      scheduleNewJob(scheduleJob);
    } else {
      updateScheduleJob(scheduleJob);
    }
  }

  private void scheduleNewJob(SchedulerJobInfo jobInfo) throws Exception {
    Scheduler scheduler = schedulerFactoryBean.getScheduler();

    // Create JobDetail
    JobDetail jobDetail = JobBuilder.newJob((Class<? extends QuartzJobBean>) Class.forName(jobInfo.getJobClass()))
        .withIdentity(jobInfo.getJobName(), jobInfo.getJobGroup())
        .build();

    if (scheduler.checkExists(jobDetail.getKey())) {
      log.error("scheduleNewJob.jobAlreadyExists - jobName = [{}], jobGroup = [{}]", jobInfo.getJobName(), jobInfo.getJobGroup());
      return;
    }

    jobDetail = scheduleCreator.createJob(
        (Class<? extends QuartzJobBean>) Class.forName(jobInfo.getJobClass()),
        false,
        context,
        jobInfo.getJobName(),
        jobInfo.getJobGroup()
    );

    // Handle multiple calendars
    Calendar combinedCalendar = null;
    String calendarName = null;

    for (JobCalendar jobCalendar : jobInfo.getJobCalendars()) {
      Calendar calendar = createCalendarFromJobCalendar(jobCalendar);

      if (combinedCalendar != null) {
        calendar.setBaseCalendar(combinedCalendar);
      }

      combinedCalendar = calendar;
      calendarName = jobCalendar.getCalendarName();
      scheduler.addCalendar(calendarName, combinedCalendar, true, true);
    }

    // Create Trigger with the last calendar added
    Trigger trigger = scheduleCreator.createCronTrigger(
        jobInfo.getJobName(),
        new Date(),
        jobInfo.getCronExpression(),
        SimpleTrigger.MISFIRE_INSTRUCTION_FIRE_NOW,
        calendarName
    );

    scheduler.scheduleJob(jobDetail, trigger);
    jobInfo.setJobStatus("SCHEDULED");
    schedulerRepository.save(jobInfo);
    log.info(">>>>> jobName = [{}] scheduled.", jobInfo.getJobName());
  }

  private void updateScheduleJob(SchedulerJobInfo jobInfo) throws Exception {
    Scheduler scheduler = schedulerFactoryBean.getScheduler();

    // Remove existing calendars associated with the job
    List<JobCalendar> existingCalendars = schedulerRepository.findByJobName(jobInfo.getJobName()).getJobCalendars();
    if (existingCalendars != null) {
      for (JobCalendar existingCalendar : existingCalendars) {
        scheduler.deleteCalendar(existingCalendar.getCalendarName());
      }
    }

    // Handle multiple calendars
    Calendar combinedCalendar = null;
    String calendarName = null;

    for (JobCalendar jobCalendar : jobInfo.getJobCalendars()) {
      Calendar calendar = createCalendarFromJobCalendar(jobCalendar);

      if (combinedCalendar != null) {
        calendar.setBaseCalendar(combinedCalendar);
      }

      combinedCalendar = calendar;
      calendarName = jobCalendar.getCalendarName();
      scheduler.addCalendar(calendarName, combinedCalendar, true, true);
    }

    // Reschedule job with the new combined calendar
    Trigger newTrigger = scheduleCreator.createCronTrigger(
        jobInfo.getJobName(),
        new Date(),
        jobInfo.getCronExpression(),
        SimpleTrigger.MISFIRE_INSTRUCTION_FIRE_NOW,
        calendarName
    );

    scheduler.rescheduleJob(TriggerKey.triggerKey(jobInfo.getJobName(), jobInfo.getJobGroup()), newTrigger);
    jobInfo.setJobStatus("UPDATED & SCHEDULED");
    schedulerRepository.save(jobInfo);
    log.info(">>>>> jobName = [{}] updated and scheduled.", jobInfo.getJobName());
  }

  private Calendar createCalendarFromJobCalendar(JobCalendar jobCalendar) throws ParseException {
    return switch (jobCalendar.getCalendarType()) {
      case ANNUAL_CALENDAR -> getAnnualCalendar(jobCalendar.getDateAndTime());
      case HOLIDAY_CALENDAR -> getHolidayCalendar(jobCalendar.getDateAndTime());
      case DAILY_CALENDAR -> getDailyCalendar(jobCalendar.getDateAndTime());
      case WEEKLY_CALENDAR -> getWeeklyCalendar(jobCalendar.getDateAndTime());
      case CRON_CALENDAR -> new CronCalendar(jobCalendar.getDateAndTime());
    };
  }

  private WeeklyCalendar getWeeklyCalendar(String value) {
    WeeklyCalendar weeklyCalendar = new WeeklyCalendar();

    // Exclude the specified day
    int dayOfWeek = parseDayOfWeek(value);

    for (int i = java.util.Calendar.SUNDAY; i <= java.util.Calendar.SATURDAY; i++) {
      weeklyCalendar.setDayExcluded(i, i == dayOfWeek);
    }

    return weeklyCalendar;
  }

  private int parseDayOfWeek(String day) {
    return switch (StringUtils.toRootUpperCase(day)) {
      case "SUNDAY" -> java.util.Calendar.SUNDAY;
      case "MONDAY" -> java.util.Calendar.MONDAY;
      case "TUESDAY" -> java.util.Calendar.TUESDAY;
      case "WEDNESDAY" -> java.util.Calendar.WEDNESDAY;
      case "THURSDAY" -> java.util.Calendar.THURSDAY;
      case "FRIDAY" -> java.util.Calendar.FRIDAY;
      case "SATURDAY" -> java.util.Calendar.SATURDAY;
      default -> throw new IllegalArgumentException("Invalid day of the week: " + day);
    };
  }

  private DailyCalendar getDailyCalendar(String value) {
    String[] parts = StringUtils.split(value, "-");

    if (parts == null || parts.length != 2) {
      throw new IllegalArgumentException("Invalid time duration. Format should be HH:mm-HH:mm");
    }

    return new DailyCalendar(parts[0], parts[1]);
  }

  private AnnualCalendar getAnnualCalendar(String value) throws ParseException {
    java.util.Calendar calendar = java.util.Calendar.getInstance();
    calendar.setTime(getDate(value));

    AnnualCalendar annualCalendar = new AnnualCalendar();
    annualCalendar.setDayExcluded(calendar, true);
    return annualCalendar;
  }

  private HolidayCalendar getHolidayCalendar(String value) throws ParseException {
    HolidayCalendar holidayCalendar = new HolidayCalendar();
    holidayCalendar.addExcludedDate(getDate(value));
    return holidayCalendar;
  }

  private Date getDate(String value) throws ParseException {
    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy.MM.dd");
    return simpleDateFormat.parse(value);
  }
}
