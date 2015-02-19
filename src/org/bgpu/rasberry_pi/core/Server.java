package org.bgpu.rasberry_pi.core;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Server {

	/**
	 * контроллер всех менеджеров задач
	 */
	private static IskanderusController ic = new IskanderusController();
	
	private static final Logger LOGGER = LogManager.getLogger(Server.class);

	public static void main(String... args) {	
		try {
			LOGGER.info("Starting server...");
			@SuppressWarnings("resource")
			ServerSocket ss = new ServerSocket(1010);
			LOGGER.info("Started server...");
			while (true) {
				Socket socket = ss.accept();
				LOGGER.info("connected client");
				new Thread(() -> {
					ic.addSocket(socket);
				}).start();
			}
		} catch (IOException e) {e.printStackTrace();}
	}
}
