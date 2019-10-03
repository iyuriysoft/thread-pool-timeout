package com.threadpool.delayedThreadPool;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Custom thread pool that executes the tasks with delay
 *
 * The pool has certain count of the work threads (TaskWorker) that handles the
 * tasks (WrapRunnable)
 */
public class ThreadPoolTimeout {

  private static final SimpleDateFormat df = new SimpleDateFormat("HH:mm:ss.SSS");
  private SortedQueue<WrapRunnable> queue;
  private List<Thread> workers = new ArrayList<Thread>();
  private volatile boolean isRunning = true;
  private volatile WrapRunnable taskMaxDelayed = null; // task with max timeout

  public ThreadPoolTimeout(int nThread) {
    queue = new SortedQueue<>();
    for (int count = 0; count < nThread; count++) {
      Thread t = new Thread(new TaskWorker(), "Thread-" + count);
      t.start();
      workers.add(t);
    }
  }

  public static void out(String s) {
    System.out.println(String.format("%s %s", ThreadPoolTimeout.df.format(new Date()), s));
  }

  private String buildName(long timeout) {
    long startTime = new Date().getTime() + timeout;
    return String.format("(%s or %dms)", ThreadPoolTimeout.df.format(new Date(startTime)), timeout);
  }

  /**
   * Submit the new task with delay
   * 
   * @param task
   *          task that should be handled
   * @param timeout
   *          delay for the task
   * @throws InterruptedException
   */
  public void submitTask(Runnable task, long timeout) throws InterruptedException {
    if (isRunning) {
      queue.offer(new WrapRunnable(task, timeout, buildName(timeout)));
    }
  }

  /**
   * Shutdown all the tasks
   * 
   * @param waitMillis
   *          pause before shutdown
   * @param maxWaitMillis
   *          max waiting before terminate work thread
   * @throws InterruptedException
   */
  public void shutdown(int waitMillis, int maxWaitMillis) throws InterruptedException {
    Thread.sleep(waitMillis);
    isRunning = false;
    for (Thread t : workers) {
      t.join(maxWaitMillis);
    }
  }

  /**
   * The thread that handles the tasks
   *
   */
  private class TaskWorker implements Runnable {

    @Override
    public void run() {
      String name = Thread.currentThread().getName();
      out(String.format("%s BEGIN", name));
      try {
        Thread.sleep(1);
        WrapRunnable nextTask = queue.poll();
        while (isRunning || nextTask != null) {
          if (nextTask == null) {
            Thread.sleep(1);
            nextTask = queue.poll();
            continue;
          }
          out(String.format("%s %s Task is pending..", name, nextTask.getName()));
          while (nextTask.getStartDate() > new Date().getTime()) {
            Thread.sleep(1);
            synchronized (workers) {
              taskMaxDelayed = (taskMaxDelayed == null || taskMaxDelayed.compareTo(nextTask) < 0)
                  ? nextTask
                  : taskMaxDelayed;
            }
            if (queue.size() == 0)
              continue;
            if (taskMaxDelayed == nextTask) {
              // check stealing of the task
              WrapRunnable nextTask2 = queue.changeTaskIfNeeds(nextTask);
              if (nextTask2 != nextTask) {
                out(String.format("%s change Tasks  new:%s  old:%s", name, nextTask2.getName(),
                    nextTask.getName()));
                nextTask = nextTask2;
                taskMaxDelayed = null;
              }
            }
          }
          out(String.format("%s %s Task Started by Thread", name, nextTask.getName()));
          nextTask.getTask().run();
          out(String.format("%s %s Task Finished by Thread", name, nextTask.getName()));
          nextTask = queue.poll();
        }
      } catch (InterruptedException e) {
        out(String.format("EXCEPTION:: Interrupt:: %s\n%s", name, e.toString()));
      } catch (Exception e) {
        out(String.format("EXCEPTION:: %s\n%s", name, e.toString()));
      } finally {
        out(String.format("%s DONE", name));
      }
    }
  }

}
