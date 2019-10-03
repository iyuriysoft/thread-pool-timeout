package com.threadpool.delayedThreadPool;

import java.util.Date;

/**
 * Wrapper for the thread with extra information like timeout to start and a
 * name
 *
 */
public class WrapRunnable implements Comparable<WrapRunnable> {

  private Runnable task;
  private long dtStart;
  private String name;

  public WrapRunnable(Runnable task, long millis, String name) {
    this.task = task;
    this.name = name;
    this.dtStart = new Date().getTime() + millis;
  }

  public String getName() {
    return this.name;
  }

  public Runnable getTask() {
    return this.task;
  }

  public long getStartDate() {
    return this.dtStart;
  }

  @Override
  public int compareTo(WrapRunnable o) {
    return Long.compare(this.dtStart, o.dtStart);
  }
}
