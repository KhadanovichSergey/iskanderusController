package org.bgpu.rasberry_pi.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgpu.rasberry_pi.structs.init.ConfigLoader;
import org.bgpu.rasberry_pi.structs.init.Initializer;

/**
 * главный класс этого проекта
 * 
 * @author Khadanovich Sergey
 * @since 2015-03-10
 */
public class Server {
	
	private static final Logger LOGGER = LogManager.getLogger(Server.class);

	public static void main(String... args) throws Exception {
		
		Initializer.init(args);
		

		try {
			LOGGER.info("Starting server...");
			@SuppressWarnings("resource")
			ServerSocket ss = new ServerSocket(Integer.parseInt(ConfigLoader.instance().getValue("portNumber")));
			LOGGER.info("Started server...");
			while (true) {
				Socket socket = ss.accept();
				LOGGER.info("connected client");
				new Thread(new SocketListener(socket)).start();
			}
		} catch (IOException e) {LOGGER.catching(e);}
		
	}
}
