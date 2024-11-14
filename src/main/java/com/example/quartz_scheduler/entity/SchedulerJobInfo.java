package com.example.quartz_scheduler.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@ToString(exclude = "jobCalendars")
@Getter
@Setter
@Entity
@Table(name = "scheduler_job_info")
public class SchedulerJobInfo {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long jobId;

  private String jobName;
  private String jobGroup;
  private String cronExpression;
  private String jobDescription;

  private String jobStatus;
  private String jobClass;

  @OneToMany(mappedBy = "schedulerJobInfo", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<JobCalendar> jobCalendars;
}
