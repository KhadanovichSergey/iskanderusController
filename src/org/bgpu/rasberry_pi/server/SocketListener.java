package org.bgpu.rasberry_pi.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class SocketListener {
	
	public static void main(String... args) throws IOException, InterruptedException {
		ServerSocket serverSocket = new ServerSocket(1010);
		
		while (true) {
			Socket socket = serverSocket.accept();
			
			Thread.sleep(5);
		}
	}
}
