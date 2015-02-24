package org.bgpu.rasberry_pi.server;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;

import org.bgpu.rasberry_pi.core.IskanderusController;
import org.bgpu.rasberry_pi.structs.AnswerSeterable;

public class SocketListener implements Runnable, AnswerSeterable {

	private Socket socket;
	
	private Object obj = new Object();
	
	private String answer;
	
	public SocketListener(Socket socket) {
		this.socket = socket;
	}
	
	@Override
	public void run() {
		read();
		try {
			socket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void setAnswer(String answer) {
		this.answer = answer;
		synchronized (obj) {
			obj.notify();
		}
	}
	
	private void read() {
		
		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			String text = reader.readLine();
			if (text.contains("run command")) {
				try {
					IskanderusController.getIskanderusController().switchCommand(text.replace("run command", "").trim(), this);
					synchronized (obj) {
						obj.wait();
					}
					sendAnswer(answer);
				} catch (NullPointerException npe) {
					sendAnswer("command not found");
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				
			}
		} catch (IOException ioe) {ioe.printStackTrace();}
	}
	
	private void sendAnswer(String answer) {
		try {
			BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
			writer.write(answer + "\n");
			writer.flush();
		} catch (IOException ioe) {ioe.printStackTrace();}
	}
}
