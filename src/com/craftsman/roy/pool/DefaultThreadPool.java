package com.craftsman.roy.pool;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import com.craftsman.roy.test.Logger;

public class DefaultThreadPool<Job extends Runnable> implements ThreadPool<Job> {
	
	private static final int MAX_NUM = 10;
	private static final int MIN_NUM = 1;
	private static final int DEFAULT_NUM = 5;
	
	private final LinkedList<Job> jobs = new LinkedList<>();
	private final List<Worker> workers = new ArrayList<>();
	private int workerNum = DEFAULT_NUM;
	private AtomicLong threadNum = new AtomicLong();
	
	public DefaultThreadPool() {
		this(DEFAULT_NUM);
	}
	
	public DefaultThreadPool(int num) {
		initializeWorkers(num < MAX_NUM ? num < MIN_NUM ? MIN_NUM : num : MAX_NUM);
	}
	
	class Worker implements Runnable {
		
		private volatile boolean running = true;

		@Override
		public void run() {
			while (running) {
				Job job = null;
				synchronized (jobs) {
					while (jobs.isEmpty()) {
						Logger.info("jobs is empty.");
						try { // empty wait a job notify.
							jobs.wait();
						} catch (InterruptedException e) {
							e.printStackTrace();
							Thread.currentThread().interrupt();
							return;
						}
					}
					Logger.info("have job. run!");
					job = jobs.removeFirst();
				}
				if (null != job) {
					try {
						job.run();
					} catch (Exception ex) {
						ex.printStackTrace();
					}
				}
			}
		}
		
		public void shutdown() {
			running = false;
		}
		
	}

	@Override
	public void execute(Job job) {
		if (null != job) {
			synchronized (jobs) {
				Logger.info("addition a job, notify!");
				jobs.addLast(job);
				jobs.notify();
			}
		}
	}

	@Override
	public void shutdown() {
		for (Worker worker : workers) {
			worker.shutdown();
		}
	}

	@Override
	public void addWrokers(int num) {
		synchronized (jobs) {
			if (num + workerNum < MAX_NUM) {
				num = MAX_NUM - workerNum;
			}
			initializeWorkers(num);
			workerNum += num;
		}
	}

	@Override
	public void removeWorker(int num) {
		synchronized (jobs) {
			if (num >= workerNum) {
				throw new IllegalArgumentException("beyound workNum");
			}
			int count = 0;
			while (count < workerNum) {
				Worker worker = workers.get(count);
				if (workers.remove(worker)) {
					worker.shutdown();
					count ++;
				}
			}
			workerNum -= count;
		}
	}

	@Override
	public int getJobSize() {
		return jobs.size();
	}
	
	private void initializeWorkers(int num) {
		for (int i = 0; i < num; i ++) {
			Worker worker = new Worker();
			workers.add(worker);
			Thread thread = new Thread(worker, "thread-worker-" + threadNum.incrementAndGet());
			thread.start();
		}
	}

}
