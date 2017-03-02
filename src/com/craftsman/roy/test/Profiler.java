package com.craftsman.roy.test;

public class Profiler {
	
	static ThreadLocal<Long> set = new ThreadLocal<Long>() {
		@Override
		protected Long initialValue() {
			return System.currentTimeMillis();
		}
	};

	
	public static void start() {
		set.set(System.currentTimeMillis());
	}
	
	public static long end() {
		long r = System.currentTimeMillis() - set.get();
		Logger.info("use time: " + r);
		return r;
	}
}
