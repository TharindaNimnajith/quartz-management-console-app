package com.example.quartz_scheduler.controller;

import com.example.quartz_scheduler.entity.Message;
import com.example.quartz_scheduler.entity.SchedulerJobInfo;
import com.example.quartz_scheduler.service.SchedulerJobService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/api")
public class JobController {

  private final SchedulerJobService schedulerJobService;

  @RequestMapping(value = "/saveOrUpdate", method = {RequestMethod.GET, RequestMethod.POST})
  public Message saveOrUpdate(SchedulerJobInfo job) {
    try {
      schedulerJobService.saveOrUpdate(job);
      return Message.success();
    } catch (Exception e) {
      return handleException("saveOrUpdate", e);
    }
  }

  @RequestMapping("/getAllJobs")
  public List<SchedulerJobInfo> getAllJobs() {
    return schedulerJobService.getAllJobList();
  }

  @RequestMapping(value = "/runJob", method = {RequestMethod.GET, RequestMethod.POST})
  public Message runJob(SchedulerJobInfo job) {
    try {
      schedulerJobService.startJobNow(job);
      return Message.success();
    } catch (Exception e) {
      return handleException("runJob", e);
    }
  }

  @RequestMapping(value = "/pauseJob", method = {RequestMethod.GET, RequestMethod.POST})
  public Message pauseJob(SchedulerJobInfo job) {
    try {
      schedulerJobService.pauseJob(job);
      return Message.success();
    } catch (Exception e) {
      return handleException("pauseJob", e);
    }
  }

  @RequestMapping(value = "/resumeJob", method = {RequestMethod.GET, RequestMethod.POST})
  public Message resumeJob(SchedulerJobInfo job) {
    try {
      schedulerJobService.resumeJob(job);
      return Message.success();
    } catch (Exception e) {
      return handleException("resumeJob", e);
    }
  }

  @RequestMapping(value = "/deleteJob", method = {RequestMethod.GET, RequestMethod.POST})
  public Message deleteJob(SchedulerJobInfo job) {
    try {
      schedulerJobService.deleteJob(job);
      return Message.success();
    } catch (Exception e) {
      return handleException("deleteJob", e);
    }
  }

  private Message handleException(String name, Exception e) {
    log.error("ERROR: {}\n", name, e);
    Message message = Message.failure();
    message.setMsg(e.getMessage());
    return message;
  }
}
