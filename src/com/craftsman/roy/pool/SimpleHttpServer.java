package com.craftsman.roy.pool;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class SimpleHttpServer {
	// 
	static ThreadPool<HttpRequestHandler> threadPool = new DefaultThreadPool<>(10);
	
	static final String BASE_PATH = "D:/local/workspace/samples/src";
	

	public static void main(String[] args) throws Exception {
		ServerSocket serverSocket = new ServerSocket(8080);
		Socket socket = null;
		while ((socket = serverSocket.accept()) != null) {
			threadPool.execute(new HttpRequestHandler(socket));
		}
		serverSocket.close();
	}

	static class HttpRequestHandler implements Runnable {
		private Socket socket;
		
		public HttpRequestHandler(Socket socket) {
			this.socket = socket;
		}

		@Override
		public void run() {
			String line = null;
			BufferedReader br = null;
			BufferedReader reader = null;
			PrintWriter out = null;
			InputStream in = null;
			try {
				reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
				String header = reader.readLine();
				System.out.println("request : " + header);
				String filePath = BASE_PATH + header.split(" ")[1];
				File file = new File(filePath);
				out = new PrintWriter(socket.getOutputStream());
				if (!file.exists()) {
					out.println("HTTP/1.1 404 Not Found");
					out.println("");
					out.flush();
					return;
				}
				if (filePath.endsWith(".jpg")) {
					in = new FileInputStream(file);
					ByteArrayOutputStream baos = new ByteArrayOutputStream();
					int i = 0;
					while ((i = in.read()) != -1) {
						baos.write(i);
					}
					byte[] array = baos.toByteArray();
					out.println("HTTP/1.1 200 OK");
					out.println("Server: nginx");
					out.println("Content-Type: image/jpeg");
					out.println("Content-Length: " + array.length);
					out.println("");
					socket.getOutputStream().write(array, 0, array.length);
				} else {
					br = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
					out.println("HTTP/1.1 200 OK");
					out.println("Server: nginx");
					out.println("Content-Type: text/html; charset=UTF-8");
					out.println("");
					while (null != (line = br.readLine())) {
						out.println(line);
					}
				}
				out.flush();
			} catch (Exception ex) {
				ex.printStackTrace();
				if (null != out) {
					out.println("HTTP/1.1 500");
					out.println("");
					out.flush();
				}
			} finally {
				close(br, in, reader, out, socket);
			}
		} // end run
		
		private static void  close(Closeable ... closeables) {
			if (null != closeables) {
				for (Closeable closeable : closeables) {
					if (null != closeable) {
						try {
							closeable.close();
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				}
			}
		}
		
	}
}
