package com.example.quartz_scheduler.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@ToString(exclude = "schedulerJobInfo")
@Getter
@Setter
@Entity
@Table(name = "job_calendar")
public class JobCalendar {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private String calendarName;

  @Enumerated(EnumType.STRING)
  private CalendarType calendarType;

  private String dateAndTime;

  @ManyToOne
  @JoinColumn(name = "scheduler_job_info_id", nullable = false)
  @JsonIgnore
  private SchedulerJobInfo schedulerJobInfo;
}
