package org.bgpu.rasberry_pi.core;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Server {

	/**
	 * контроллер всех менеджеров задач
	 */
	private static IskanderusController ic = new IskanderusController();
	
	/**
	 * логгер
	 */
	private static Logger LOGGER = Logger.getLogger(Server.class.getName());
	
	public static void main(String... args) {	
		try {
			Server.LOGGER.log(Level.INFO, "starting server...");
			@SuppressWarnings("resource")
			ServerSocket ss = new ServerSocket(1010);
			Server.LOGGER.log(Level.INFO, "started server");
			while (true) {
				Socket socket = ss.accept();
				new Thread(() -> {
					ic.addSocket(socket);
				}).start();
			}
		} catch (IOException e) {e.printStackTrace();}
	}
}
