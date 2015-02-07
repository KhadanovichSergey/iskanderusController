package org.bgpu.rasberry_pi.core;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {

	/**
	 * контроллер всех менеджеров задач
	 */
	private static IskanderusController ic = new IskanderusController();

	public static void main(String... args) {	
		try {
			System.out.println("starting server...");
			@SuppressWarnings("resource")
			ServerSocket ss = new ServerSocket(1010);
			System.out.println("started server");
			while (true) {
				Socket socket = ss.accept();
				new Thread(() -> {
					ic.addSocket(socket);
				}).start();
			}
		} catch (IOException e) {e.printStackTrace();}
	}
}
