package com.craftsman.roy.test;

import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.util.concurrent.TimeUnit;

/**
 * Create a connection with JDK dynamic proxy.
 * @author luoyh
 *
 */
public class ConnectionDriver {
	
//	static class ConnectionHandler implements InvocationHandler {
//
//		@Override
//		public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
//			if (method.getName().equals("commit")) { // just deal commit method.
//				TimeUnit.MILLISECONDS.sleep(100);
//			}
//			return null;
//		}
//		
//	}
	
	public static Connection obtainConnection() {
		return (Connection) Proxy.newProxyInstance(ConnectionDriver.class.getClassLoader(), 
				new Class[]{ Connection.class }, (proxy, method, args) -> {
			if (method.getName().equals("commit")) {
				TimeUnit.MILLISECONDS.sleep(60);
			}
			return null;
		});
	}

}
