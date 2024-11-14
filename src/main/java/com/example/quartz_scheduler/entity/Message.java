package com.example.quartz_scheduler.entity;

import java.io.Serial;
import java.io.Serializable;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class Message implements Serializable {

  @Serial
  private static final long serialVersionUID = 4321761252954619538L;

  private boolean valid;
  private String msg;

  public Message(boolean valid) {
    super();
    this.valid = valid;
  }

  public static Message failure() {
    return new Message(false);
  }

  public static Message success() {
    return new Message(true);
  }
}
