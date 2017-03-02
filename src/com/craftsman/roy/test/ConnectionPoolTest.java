package com.craftsman.roy.test;

import java.sql.Connection;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

public class ConnectionPoolTest {
	static ConnectionPool pool = new ConnectionPool(10);
	static CountDownLatch start = new CountDownLatch(1);
	static long mills = 1000L;
	static CountDownLatch end;
	
	public static void main(String[] args) throws InterruptedException {
		Profiler.start();
		int threadCount = 50, count = 20;
		end = new CountDownLatch(threadCount);
		AtomicInteger got = new AtomicInteger();
		AtomicInteger nogot = new AtomicInteger();
		for (int i = 0; i < threadCount; i ++) {
			Thread thread = new Thread(new ConnectionRunner(count, got, nogot), "connection-runner-" + i);
			thread.start();
		}
		System.out.println("start");
		start.countDown();
		end.await();
		System.out.println("total invoke: " + (threadCount * count));
		System.out.println("got connection: " + got);
		System.out.println("no got connection: " + nogot);
		Profiler.end();
		System.exit(0);
	}

	
	static class ConnectionRunner implements Runnable {
		int count;
		AtomicInteger got;
		AtomicInteger nogot;
		
		public ConnectionRunner(int count, AtomicInteger got, AtomicInteger nogot) {
			this.count = count;
			this.got = got;
			this.nogot = nogot;
		}
		
		@Override
		public void run() {
			try {
				start.await();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			
			while (count > 0) {
				try {
					Connection connection = pool.fetchConnection(mills);
					if (null != connection) {
						try {
							connection.createStatement();
							connection.commit();
						} finally {
							pool.releaseConnection(connection);
							got.incrementAndGet();
						}
					} else {
						nogot.incrementAndGet();
					}
				} catch(Exception ex) {
					ex.printStackTrace();
				} finally {
					count --;
				}
			}
			end.countDown();
		}
	}
	
}
