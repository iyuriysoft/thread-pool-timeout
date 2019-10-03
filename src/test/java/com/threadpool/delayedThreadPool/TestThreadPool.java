package com.threadpool.delayedThreadPool;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.concurrent.Callable;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class TestThreadPool {

  // @Rule
  // public Timeout globalTimeout = new Timeout(5, TimeUnit.SECONDS);

  @Before
  public void setUp() throws Exception {
  }

  @Test(timeout = 1000)
  public void test4_CheckSortedQueue() throws InterruptedException, ExecutionException {
    System.out.println("\nCheckSortedQueue\n");
    String[] givenAr = { "zxcv", "hhhh", "sss", "dddd", "vvv" };
    String[] expectedAr = { "dddd", "hhhh", "sss", "vvv", "zxcv" };
    SortedQueue<String> queue = new SortedQueue<String>();
    CopyOnWriteArrayList<String> arr = new CopyOnWriteArrayList<>();
    Callable<String> t1 = () -> {
      queue.offer(givenAr[0]);
      return null;
    };
    Callable<String> t2 = () -> {
      queue.offer(givenAr[1]);
      return null;
    };
    Callable<String> t3 = () -> {
      queue.offer(givenAr[2]);
      return null;
    };
    Callable<String> t4 = () -> {
      queue.offer(givenAr[3]);
      return null;
    };
    Callable<String> t5 = () -> {
      queue.offer(givenAr[4]);
      return null;
    };

    Callable<String> p1 = () -> {
      arr.add(queue.poll());
      return null;
    };

    // multi put
    Executors.newFixedThreadPool(8).invokeAll(Arrays.asList(t1, t2, t3, t4, t5));

    // multi get
    Executors.newFixedThreadPool(8).invokeAll(Arrays.asList(p1, p1, p1, p1, p1));
    String[] result = arr.toArray(new String[5]);
    assertArrayEquals(expectedAr, result);
  }

  @Test(timeout = 1000)
  public void test3_CheckOrderFor5Tasks3Threads() throws InterruptedException {
    System.out.println("\nCheckOrderFor5Task3Threads\n");
    final AtomicInteger executedCount = new AtomicInteger(0);
    Integer[] arDelay = { 500, 350, 300, 200, 100 };
    ThreadPoolTimeout pool = new ThreadPoolTimeout(3);
    Runnable[] arRun = new Runnable[arDelay.length];
    arRun[0] = () -> {
      executedCount.updateAndGet(value -> value * 2 + 1);
    }; // 129
    arRun[1] = () -> {
      executedCount.updateAndGet(value -> value * 2 + 2);
    }; // 64
    arRun[2] = () -> {
      executedCount.updateAndGet(value -> value * 2 + 3);
    }; // 31
    arRun[3] = () -> {
      executedCount.updateAndGet(value -> value * 2 + 4);
    }; // 14
    arRun[4] = () -> {
      executedCount.updateAndGet(value -> value * 2 + 5);
    }; // 5
    for (int i = 0; i < arDelay.length; i++) {
      pool.submitTask(arRun[i], arDelay[i]);
    }
    pool.shutdown(600, 900);

    assertEquals(129, executedCount.get());
  }

  @Test(timeout = 1500)
  public void test3_CheckOrderForWorkStealing() throws InterruptedException {
    System.out.println("\nCheckOrderForWorkStealing\n");
    final AtomicInteger executedCount = new AtomicInteger(0);
    Integer[] arDelay = { 1000, 950, 900, 850, 200 };
    ThreadPoolTimeout pool = new ThreadPoolTimeout(3);
    Runnable[] arRun = new Runnable[arDelay.length];
    arRun[0] = () -> {
      executedCount.updateAndGet(value -> value * 2 + 1);
    }; // 129
    arRun[1] = () -> {
      executedCount.updateAndGet(value -> value * 2 + 2);
    }; // 64
    arRun[2] = () -> {
      executedCount.updateAndGet(value -> value * 2 + 3);
    }; // 31
    arRun[3] = () -> {
      executedCount.updateAndGet(value -> value * 2 + 4);
    }; // 14
    arRun[4] = () -> {
      executedCount.updateAndGet(value -> value * 2 + 5);
    }; // 5
    for (int i = 0; i < arDelay.length - 1; i++) {
      pool.submitTask(arRun[i], arDelay[i]);
    }
    Thread.sleep(150);
    pool.submitTask(arRun[4], arDelay[4]);
    pool.shutdown(1200, 500);
    assertEquals(129, executedCount.get());
  }

  @Test(timeout = 2000)
  public void test5_CheckInfiniteThread() throws InterruptedException {
    System.out.println("\nCheckInfiniteThread\n");
    ThreadPoolTimeout pool = new ThreadPoolTimeout(3);
    Runnable run = () -> {
      while (true)
        ;
    };
    pool.submitTask(run, 100);
    pool.shutdown(0, 1000);
  }

  @Test(timeout = 1000)
  public void test5_CheckSuppressException() throws InterruptedException {
    System.out.println("\nCheckSuppressException\n");
    ThreadPoolTimeout executor = new ThreadPoolTimeout(4);
    Runnable run = () -> {
      throw new RuntimeException("my");
    };
    executor.submitTask(run, 100);
    executor.shutdown(0, 1000);
  }

  @Test(timeout = 5000)
  public void test1_Check2000TasksTogether() throws InterruptedException {
    System.out.println("\nCheck2000TasksTogether\n");
    final AtomicInteger executedCount = new AtomicInteger(0);
    ThreadPoolTimeout executor = new ThreadPoolTimeout(4);
    for (int i = 0; i < 2000; i++) {
      executor.submitTask(new Runnable() {
        @Override
        public void run() {
          executedCount.incrementAndGet();
        }
      }, 0);
      executor.submitTask(new Runnable() {
        @Override
        public void run() {
          executedCount.decrementAndGet();
        }
      }, 0);
    }
    executor.shutdown(100, 4000);
    assertEquals(0, executedCount.get());
  }

  // @Test(timeout = 1000)
  // public void test2_Check2TasksOrder() throws InterruptedException {
  // System.out.println("\nCheck2TasksOrder\n");
  // final CountDownLatch first = new CountDownLatch(1);
  // final CountDownLatch second = new CountDownLatch(1);
  // final AtomicInteger firstExecuted = new AtomicInteger(0);
  // ThreadPoolTimeout executor = new ThreadPoolTimeout(4);
  // executor.submitTask(new Runnable() {
  // @Override
  // public void run() {
  // first.countDown();
  // try {
  // second.await();
  // firstExecuted.set(10);
  // } catch (InterruptedException e) {
  // Thread.currentThread().interrupt();
  // }
  // }
  // }, 200);
  // executor.submitTask(new Runnable() {
  // @Override
  // public void run() {
  // second.countDown();
  // try {
  // first.await();
  // Thread.sleep(1);
  // firstExecuted.set(20);
  // } catch (InterruptedException e) {
  // Thread.currentThread().interrupt();
  // }
  // }
  // }, 100);
  // executor.shutdown(10, 900);
  // assertEquals(firstExecuted.get(), 20);
  // }

}