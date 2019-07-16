package client;

public class Message {
	private String message;
	private int statusMessage;
	private int lineNumberTextArea;
	
	/*
	 * status message:
	 * 0 - mensagem sem status
	 * 1 - mensagem recebida no servidor
	 * 2 - mensagem entregue ao destinatário
	 * 3 - mensagem lida pelo destinatário
	 *  
	 */
	
	
	public Message(String message, int status, int lineNumber) {
		this.message = message;
		this.statusMessage = status;
		this.lineNumberTextArea = lineNumber;
	}
	
	public int getStatus() {
		return statusMessage;
	}
	
	public String getMessage() {
		return message;
	}
	
	public int getLineNumber() {
		return lineNumberTextArea;
	}
	
}
