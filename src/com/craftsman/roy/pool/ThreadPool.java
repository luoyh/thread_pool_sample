package com.craftsman.roy.pool;

public interface ThreadPool<Job extends Runnable> {
	
	void execute(Job job);
	
	void shutdown();
	
	void addWrokers(int num);
	
	void removeWorker(int num);
	
	int getJobSize();

}
