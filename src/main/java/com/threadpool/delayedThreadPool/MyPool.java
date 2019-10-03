package com.threadpool.delayedThreadPool;

/**
 * The thread to simulate the difficult task
 *
 */
class TestBigTask implements Runnable {
  private long cnt;

  public TestBigTask(long cnt) {
    this.cnt = cnt;
  }

  @Override
  public void run() {
    long startTime = System.nanoTime();
    System.out.println("  start :" + Thread.currentThread().getName());
    double b = 0;
    for (long i = 0; i < cnt; i++) {
      for (long j = 0; j < cnt; j++) {
        for (long k = 0; k < cnt; k++) {
          b = Math.pow(cnt, 2);
        }
      }
    }
    System.out.println(String.format("  end :%s  time :%.3fs  res :%.3f", Thread.currentThread().getName(),
        (System.nanoTime() - startTime) / 1e9, b));
  }
}

public class MyPool {

  @SuppressWarnings("unused")
  public static void main(String[] args) throws InterruptedException {
    long startTime = System.nanoTime();
    ThreadPoolTimeout pool = new ThreadPoolTimeout(3);
    if (false) {
      for (int i=0; i < 30; i++)
        pool.submitTask(new TestBigTask(2000), 0);
    } else {
      pool.submitTask(new TestBigTask(200), 4000);
      Thread.sleep(500);
      pool.submitTask(new TestBigTask(200), 3000);
      pool.submitTask(new TestBigTask(200), 2000);
      Thread.sleep(450);
      pool.submitTask(new TestBigTask(200), 1000);
      pool.submitTask(new TestBigTask(200), 100);
      pool.submitTask(new TestBigTask(200), 4500);
      Thread.sleep(450);
      pool.submitTask(new TestBigTask(200), 3500);
      Thread.sleep(450);
      pool.submitTask(new TestBigTask(200), 2500);
      Thread.sleep(450);
      pool.submitTask(new TestBigTask(200), 1500);
    }
    pool.shutdown(6000, 10000);
    System.out.println((System.nanoTime() - startTime) / 1e9);

  }
}
