package com.example.quartz_scheduler.config;

import org.quartz.spi.TriggerFiredBundle;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;
import org.springframework.scheduling.quartz.SpringBeanJobFactory;

@Configuration
public class SchedulerJobFactory extends SpringBeanJobFactory implements ApplicationContextAware {

  private AutowireCapableBeanFactory beanFactory;

  @Override
  public void setApplicationContext(ApplicationContext context) {
    beanFactory = context.getAutowireCapableBeanFactory();
  }

  @Override
  protected @NonNull Object createJobInstance(@NonNull TriggerFiredBundle bundle) throws Exception {
    Object job = super.createJobInstance(bundle);
    beanFactory.autowireBean(job);
    return job;
  }
}
