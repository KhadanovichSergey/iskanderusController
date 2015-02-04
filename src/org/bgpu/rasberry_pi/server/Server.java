package org.bgpu.rasberry_pi.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {

	/**
	 * контроллер всех менеджеров ардуин
	 */
	private static IskanderusController ic = new IskanderusController();
	
	public static void main(String... args) {
		try {
			ServerSocket ss = new ServerSocket(1010);
			while (true) {
				Socket socket = ss.accept();
				new Thread(() -> {
					synchronized (ic) {
						ic.addSocket(socket);
					}
				}).start();
			}
		} catch (IOException e) {e.printStackTrace();}
	}
}
