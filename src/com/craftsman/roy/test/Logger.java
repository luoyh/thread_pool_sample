package com.craftsman.roy.test;

public abstract class Logger {
	
	public static final void info(String msg) {
		System.err.println(Thread.currentThread().getName() + "[" + msg + "]");
	}

}
