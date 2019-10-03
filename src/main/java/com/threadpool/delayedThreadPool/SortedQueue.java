package com.threadpool.delayedThreadPool;

import java.util.PriorityQueue;
import java.util.Queue;

/**
 * Thread-safe Sorted Queue
 *
 * @param <T>
 */
public class SortedQueue<T extends Comparable<T>> {

  private Queue<T> queue = new PriorityQueue<T>();

  public SortedQueue() {
    this.queue = new PriorityQueue<T>();
  }

  /**
   * Put the item to the array
   * 
   * @param task
   * @throws InterruptedException
   */
  public synchronized void offer(T task) throws InterruptedException {
    this.queue.offer(task);
  }

  /**
   * Get the item from top of the sorted array
   * 
   * @return item
   * @throws InterruptedException
   */
  public synchronized T poll() throws InterruptedException {
    return this.queue.poll();
  }
  
  /**
   * 
   * @return array size
   */
  public synchronized int size() {
    return this.queue.size();
  }

  /**
   * If there is a new task that should be started earlier then get it
   * 
   * @param oldTask current old task
   * @return new task
   * @throws InterruptedException
   */
  public synchronized T changeTaskIfNeeds(T oldTask) throws InterruptedException {
    T newTask = this.queue.poll();
    if (newTask == null) {
      return oldTask;
    }
    if (newTask.compareTo(oldTask) < 0) {
      this.queue.offer(oldTask);
      return newTask;
    }
    this.queue.offer(newTask);
    return oldTask;
  }

}