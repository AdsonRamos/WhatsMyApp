package server;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Server {

	List<PrintWriter> writers = new ArrayList<PrintWriter>();

	public Server() {

		int indexClient = 1;

		ServerSocket server;
		try {
			server = new ServerSocket(5000);
			while (true) {
				Socket s = server.accept();
				new Thread(new ListenToClient(s, indexClient++)).start();
				PrintWriter p = new PrintWriter(s.getOutputStream());
				writers.add(p);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void sendToAll(String text) {
		for (int i = 0; i < writers.size(); i++) {
			PrintWriter pw = writers.get(i);
			pw.println(text);
			pw.flush();
		}
	}

	// Mensagem chegou no servidor
	private void sendConfirmation(String message, int clientNumber) {

		PrintWriter pw = writers.get(clientNumber - 1);
		pw.println("ACK_server" + message);
		pw.flush();
	}

	// Mensagem chegou no outro cliente
	// Este método recebe a mensagem e o id do cliente que recebeu a mensagem
	private void receivedConfirmation(String message, int clientNumber) {
		System.out.println("ACK_received Message received in client " + clientNumber);
		
		if(clientNumber == 1) {
			clientNumber = 2;
		} else if(clientNumber == 2){
			clientNumber = 1;
		}
		
		PrintWriter pw = writers.get(clientNumber - 1);
		pw.println(message);
		pw.flush();
	}

	// Mensagem visualizada
	private void messageViewed(String text, int clientNumber) {
		
		if(clientNumber == 1) {
			clientNumber = 2;
		} else if(clientNumber == 2){
			clientNumber = 1;
		}
		
		PrintWriter pw = writers.get(clientNumber - 1);
		pw.println(text);
		pw.flush();

	}
	
	// deletar mensagem
	private void deleteMessage(String text, int clientNumber) {
		int linha = Integer.parseInt(text.substring(8));
		
		System.out.println(linha);
		
		if(clientNumber == 1) {
			clientNumber = 2;
		} else if(clientNumber == 2){
			clientNumber = 1;
		}
		
		PrintWriter pw = writers.get(clientNumber - 1);
		pw.println(text);
		pw.flush();
	}
	
	private class ListenToClient extends Thread {

		Scanner reader;
		int clientNumber;

		public ListenToClient(Socket socket, int clientNumber) {
			this.clientNumber = clientNumber;
			try {
				reader = new Scanner(socket.getInputStream());
			} catch (IOException e) {
				e.printStackTrace();
			}
			System.out.println("Número de clientes conectados: " + this.clientNumber);
		}

		@Override
		public void run() {
			String text;
			while (reader.hasNext()) {
				text = reader.nextLine();

				if (text.startsWith("ACK_client")) {
					receivedConfirmation(text, clientNumber);

				} else if(text.startsWith("ACK_viewed")) {
					messageViewed(text, clientNumber);
				} else if(text.startsWith("#DELETE")) { 
					deleteMessage(text, clientNumber);
				} else {
					sendConfirmation(text, clientNumber);
					sendToAll(text);
				}
			}
		}

	}

	public static void main(String[] args) {
		new Server();
	}
}
