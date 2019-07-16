package client;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Font;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Scanner;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.text.BadLocationException;

@SuppressWarnings("serial")
public class Client extends JFrame {

	private JTextField text_tf;
	private Socket socket;
	private PrintWriter writer;
	private String name;
	private JTextArea received_ta;
	private Scanner reader;

	private ArrayList<String> myMessages;
	private int indexMessages;
	private ArrayList<String> userMessages;
	
	private ArrayList<String> messages;
	
	private ArrayList<Message> messagesFromAnotherUser;

	private int indexReceivedMessages = 0;
	private int lastIndexViewed = 0;
	
	private ArrayList<Boolean> linesUsedByUser;
	
	private int lines = 0;
	
	private int vieweds = 0;
	
	public Client(String name) {
		super("WhatsMyApp : " + name);
		
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException
				| UnsupportedLookAndFeelException e2) {
			e2.printStackTrace();
		}

		messagesFromAnotherUser = new ArrayList<Message>();
		
		linesUsedByUser = new ArrayList<Boolean>();
		
		myMessages = new ArrayList<String>();
		indexMessages = 0;
		
		userMessages = new ArrayList<String>();
		messages = new ArrayList<String>();

		setResizable(false);
		this.name = name;

		Font fontText = new Font("Trebuchet MS", Font.BOLD, 23);
		text_tf = new JTextField();
		text_tf.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_ENTER && !text_tf.getText().equalsIgnoreCase("")) {
					send();
				}
			}
		});
		text_tf.setFont(fontText);
		text_tf.addFocusListener(new FocusListener() {
			
			@Override
			public void focusLost(FocusEvent e) {
				
			}
			
			@Override
			public void focusGained(FocusEvent e) {
				System.out.println("Text Field Ganhou foco.");
			}
		});

		JButton btn = new JButton("Enviar");
		btn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (!text_tf.getText().equalsIgnoreCase(""))
					send();
			}
		});

		btn.setFont(fontText);

		Container sendContainer = new JPanel();
		sendContainer.setLayout(new BorderLayout());
		sendContainer.add(BorderLayout.CENTER, text_tf);
		sendContainer.add(BorderLayout.EAST, btn);

		received_ta = new JTextArea();
		received_ta.setFont(fontText);
		received_ta.setEditable(false);
		JScrollPane scroll = new JScrollPane(received_ta);
		
		received_ta.addFocusListener(new FocusListener() {
			
			@Override
			public void focusLost(FocusEvent e) {
				
			}
			
			@Override
			public void focusGained(FocusEvent e) {
				// se o text area ganha foco e a mensagem não foi lida ainda, visualiza mensagem
				
				for(int i = lastIndexViewed; i < indexReceivedMessages; i++) {
					writer.println("ACK_viewed: - Index: "+i);
					writer.flush();
				}
				
				lastIndexViewed = indexReceivedMessages;
			}
		});
		
		received_ta.addMouseListener(new MouseListener() {
			
			@Override
			public void mouseReleased(MouseEvent e) {
				
			}
			
			@Override
			public void mousePressed(MouseEvent e) {
				
			}
			
			@Override
			public void mouseExited(MouseEvent e) {
				
			}
			
			@Override
			public void mouseEntered(MouseEvent e) {
				
			}
			
			@Override
			public void mouseClicked(MouseEvent e) {
				
				if(e.getButton() == MouseEvent.BUTTON3) {
					Point p = received_ta.getMousePosition();
					JMenuItem deleted = new JMenuItem("Deletar Mensagem");
					deleted.addActionListener(new ActionListener() {
						
						@Override
						public void actionPerformed(ActionEvent e) {
							int linha = 0;
							//pega todo o conteudo do TextArea
							String str[] = received_ta.getText().split("\n");
							
							//Enquanto a frase selecionada não for encontrada,vai continuar contando o numero de linhas
							//Quando encontrar, será a linha que a mensagem vai ta.
							while(!str[linha].equals(received_ta.getSelectedText())) {
								linha++;
							}
							
							System.out.println("Deleta mensagem "+linha);
							
							
							System.out.println(linha);
							
							if(linesUsedByUser.get(linha)) {
								System.out.println("Linha pode ser deletada.");
							} else {
								System.out.println("Linha não pode ser deletada.");
								return;
							}
							
							int selectionEnd = 0;
							int selectionStart = 0;
							
							try {
								selectionStart = received_ta.getLineStartOffset(linha);
								selectionEnd = received_ta.getLineEndOffset(linha);
							} catch (BadLocationException e1) {
								e1.printStackTrace();
							}
							
							selectionEnd += -1;
							
							System.out.println("selectionStart: "+selectionStart);
							System.out.println("selectionEnd: "+selectionEnd);
							
							received_ta.select(selectionStart, selectionEnd);
							received_ta.replaceSelection("Você apagou está mensagem.");
							
							writer.println("#DELETE:"+linha);
							writer.flush();
						}
					});
					JPopupMenu popup = new JPopupMenu();
					popup.add(deleted);
					popup.show(received_ta, p.x, p.y);
				}
			}
		});
		
		getContentPane().add(BorderLayout.CENTER, scroll);
		getContentPane().add(BorderLayout.SOUTH, sendContainer);
		
		setNetwork();
		
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setSize(320, 480);
		setLocationRelativeTo(null);
		setVisible(true);
	}

	private void send() {

		String message = text_tf.getText();
		
		myMessages.add(message);
		messages.add(message);
		
		linesUsedByUser.add(true);
		
		received_ta.append(message + " - *\n");
		lines++;
		
		writer.println(name + " : " + message);
		writer.flush();
		text_tf.setText("");
		text_tf.requestFocus();
	}

	private void setNetwork() {
		try {
			socket = new Socket("127.0.0.1", 5000);
			writer = new PrintWriter(socket.getOutputStream());
			reader = new Scanner(socket.getInputStream());
			new Thread(new ListenToServer()).start();
		} catch (Exception e) {
			
		}
	}

	private class ListenToServer extends Thread {
		@Override
		public void run() {
			try {
				String text;
				while (reader.hasNext()) {
					text = reader.nextLine();

					if (text.startsWith("ACK_server")) {
						// recebe confirmação do servidor e processa mensagem
						System.out.println("Mensagem recebida no servidor.");
						String message = text.substring("ACK_for_message".length());
						int index = message.indexOf(':');
						message = message.substring(index + 2);
						System.out.println(message);
						
						System.out.println(myMessages.get(indexMessages));
						
						System.out.println(message.equalsIgnoreCase(myMessages.get(indexMessages)));
						
						// se as mensagens forem confirmadas, troca o caractere de status(de * para #)
						if(message.equalsIgnoreCase(myMessages.get(indexMessages))) {
							
							int linha = lines;
							
							System.out.println(linha);
							
							int selectionEnd = 0;
							int selectionStart = 0;
							
							try {
								selectionStart = received_ta.getLineStartOffset(linha - 1);
								selectionEnd = received_ta.getLineEndOffset(linha - 1);
							} catch (BadLocationException e) {
								e.printStackTrace();
							}
							
							System.out.println(messages.get(linha - 1));
							
							selectionEnd += -1;
							
							System.out.println("selectionStart: "+selectionStart);
							System.out.println("selectionEnd: "+selectionEnd);
							
							received_ta.select(selectionStart, selectionEnd);
							
							received_ta.replaceSelection(myMessages.get(indexMessages) +" - #");
						}
						
						indexMessages++;
						
					} else if(text.startsWith("ACK_client")) {
						// recebe confimação do cliente e processa mensagem
						System.out.println(text);
						
						int linha = lines;
						
						System.out.println(linha);
						
						int selectionEnd = 0;
						int selectionStart = 0;
						
						try {
							selectionStart = received_ta.getLineStartOffset(linha - 1);
							selectionEnd = received_ta.getLineEndOffset(linha - 1);
						} catch (BadLocationException e) {
							e.printStackTrace();
						}
						
						System.out.println(messages.get(linha - 1));
						
						selectionEnd += -1;
						
						System.out.println("selectionStart: "+selectionStart);
						System.out.println("selectionEnd: "+selectionEnd);
						
						received_ta.select(selectionStart, selectionEnd);
						
						System.out.println(received_ta.getSelectedText().substring(0, received_ta.getSelectedText().length() - 1) + "$");
						
						received_ta.replaceSelection(received_ta.getSelectedText().substring(0, received_ta.getSelectedText().length() - 1) + "$");
						
					} else if (text.startsWith("ACK_viewed")){
						// recebe confirmação de que cliente visualizou a mensagem
						
						System.out.println(Integer.parseInt(text.substring(21)) + userMessages.size());
						
						int linha = Integer.parseInt(text.substring(21)) + userMessages.size();
						
						int selectionEnd = 0;
						int selectionStart = 0;
						
						try {
							selectionStart = received_ta.getLineStartOffset(linha);
							selectionEnd = received_ta.getLineEndOffset(linha);
						} catch (BadLocationException e) {
							e.printStackTrace();
						}
						
						System.out.println(messages.get(linha));
						
						selectionEnd += -1;
						
						System.out.println("selectionStart: "+selectionStart);
						System.out.println("selectionEnd: "+selectionEnd);
						
						received_ta.select(selectionStart, selectionEnd);
						
						System.out.println(received_ta.getSelectedText().substring(0, received_ta.getSelectedText().length() - 1) + "Lida");
						
						received_ta.replaceSelection(received_ta.getSelectedText().substring(0, received_ta.getSelectedText().length() - 1) + "Lida");
						
						vieweds++;
						
					} else if(text.startsWith("#DELETE")) {
						int linha = Integer.parseInt(text.substring(8));
						
						int selectionEnd = 0;
						int selectionStart = 0;
						
						try {
							selectionStart = received_ta.getLineStartOffset(linha);
							selectionEnd = received_ta.getLineEndOffset(linha);
						} catch (BadLocationException e) {
							e.printStackTrace();
						}
						
						System.out.println(messages.get(linha));
						
						selectionEnd += -1;
						
						System.out.println("selectionStart: "+selectionStart);
						System.out.println("selectionEnd: "+selectionEnd);
						
						received_ta.select(selectionStart, selectionEnd);
						
						received_ta.replaceSelection("Esta mensagem foi apagada.");
						
					} else {
						// chega mensagem de outra pessoa
						int index = text.indexOf(':');
						
						String nameSender = text.substring(0, index - 1);
						
						if (!nameSender.equalsIgnoreCase(name)) {
							received_ta.append(text + "\n");
							indexReceivedMessages++;
							lines++;
							userMessages.add(text);
							messages.add(text);
							messagesFromAnotherUser.add(new Message(text, 0, lines));
							linesUsedByUser.add(false);
							// envia confirmação de recebimento de mensagem
							
							writer.println("ACK_client: "+text);
							writer.flush();
							
						}
						
					}
				}
			} catch (Exception e) {

			}
		}
	}

	public static void main(String[] args) {
		new Client("Cliente 2");
	}
}
