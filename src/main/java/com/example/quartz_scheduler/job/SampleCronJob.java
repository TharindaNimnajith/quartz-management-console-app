package com.example.quartz_scheduler.job;

import lombok.extern.slf4j.Slf4j;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;
import org.springframework.lang.NonNull;
import org.springframework.scheduling.quartz.QuartzJobBean;

@Slf4j
@DisallowConcurrentExecution
public class SampleCronJob extends QuartzJobBean {

  @Override
  protected void executeInternal(@NonNull JobExecutionContext context) {
    log.info("SampleCronJob Start................");
  }
}
