package com.example.sampleapp;

// source code for TicTacToe logic taken from 
// http://maxood-android-corner.blogspot.com/2011/01/tutorial-simple-tictactoe-app-on.html

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.SocketException;
import java.util.Scanner;
import com.example.tictactoe.R;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

//import java.util.Random; - for AI

public class TicTacToe extends Activity {

	int c[][];
	int i, j, k = 0;
	Button b[][];
	TextView textView;
	// AI ai;
	
	int clientId;
	boolean xVal = false;
	
	public static Handler handler;
	public static DatagramSocket socket;
	public static String serverIP = "54.82.110.122";
	public static SocketAddress serverSocketAddress = 
			new InetSocketAddress(serverIP, 20000);

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		handler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				String receievedText = (String) msg.obj;
				System.out.println("Handler receieved text: " + receievedText); // DEBUGGING
				Scanner textScan = new Scanner(receievedText);
				String startWord = textScan.next();
				
				if (startWord.equals("SETID")) {
					clientId = textScan.nextInt(); // set the assigned clientID
					send("MATCH " + clientId);
					System.out.println("MATCH was sent to the server"); // DEBUGGING
					return;
				}
				else if (startWord.equals("PLAY")) {
					xVal = true;
					setBoard();
					System.out.println("The board is set."); // DEBUGGING
					return;
				}
				else if (startWord.equals("MOVE")) {
					handleMove(textScan);
					return;
				}
			}
			
			public void handleMove(Scanner textScan) {
				int x = textScan.nextInt(); // store x-axis value
				int y = textScan.nextInt(); // store y-axis value
				boolean xPlayer = textScan.nextBoolean(); // store the boolean value
				if (b[x][y].isEnabled()) {
					b[x][y].setEnabled(false); // make that button disabled
					b[x][y].setText(xPlayer ? "X" : "O");
					c[x][y] = xPlayer ? 0 : 1;
					if (!checkBoard()) {
						enableButtons(); // the opponent moved, so allow the buttons to be pressed
						System.out.println("The player can press the buttons now!"); // DEBUGGING
					} else {
						send("QUIT " + clientId);
						System.out.println("QUIT was sent to the server"); // DEBUGGING
					}
				}
			}
			
			// Enable not-yet-X-or-O buttons!
			public void enableButtons() {	
				for (int x=1; x<4; x++) {
					for (int y=1; y<4; y++) {
						if (b[x][y].getText() == " ") {
							b[x][y].setEnabled(true);
						}
					}
				}
			}
			
			// end of handler
		};

		new Thread() {
			@Override
			public void run() {
				try {
					socket = new DatagramSocket();
					System.out.println("Datagram socket created");

					System.out.println("Reading from socket...");
					while (true) {
						// receive the server's response
						// create an empty UDP packet
						byte[] buf = new byte[500];
						DatagramPacket rxPacket = new DatagramPacket(buf, buf.length);
						// call receive (this will populate the packet with the 
						// received data and the other endpoint's info)
						socket.receive(rxPacket);
						// receive the payload
						String payload = new String(rxPacket.getData(), 0,
								rxPacket.getLength());
						System.out.println("Thread received text: " + payload); // DEBUGGING
						// get an empty message, fill it with the payload, and
						// send it to the handler
						Message m = handler.obtainMessage();
						m.obj = payload;
						m.sendToTarget();
					}
				} catch (java.io.IOException e) {
					e.printStackTrace();
				}
			}
			
			// end of thread
		}.start();
		
		System.out.println("REGISTER is about to be sent..."); // DEBUGGING	
		send("REGISTER");
		System.out.println("REGISTER was sent!"); // DEBUGGING	
		
		// end of onCreate
	}
	
	// Send a message to the server
	public void send(String text) {
		new AsyncTask<String, Void, Void>() {
			protected Void doInBackground(String... params) {
				String text = params[0];
				DatagramPacket txPacket;
				try {
					txPacket = new DatagramPacket(text.getBytes(),
							text.length(), TicTacToe.serverSocketAddress);
					TicTacToe.socket.send(txPacket); // send the message
					System.out.println("Sending text: " + text); // DEBUGGING
				} catch (SocketException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}	
				return null;
			}
		}.execute(text);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		menu.add("New Game");
		return true;
	}

	// Set up the game board
	private void setBoard() {
		// ai = new AI();
		b = new Button[4][4];
		c = new int[4][4];

		textView = (TextView) findViewById(R.id.dialogue);

		b[1][3] = (Button) findViewById(R.id.one);
		b[1][2] = (Button) findViewById(R.id.two);
		b[1][1] = (Button) findViewById(R.id.three);

		b[2][3] = (Button) findViewById(R.id.four);
		b[2][2] = (Button) findViewById(R.id.five);
		b[2][1] = (Button) findViewById(R.id.six);

		b[3][3] = (Button) findViewById(R.id.seven);
		b[3][2] = (Button) findViewById(R.id.eight);
		b[3][1] = (Button) findViewById(R.id.nine);

		for (i = 1; i <= 3; i++) {
			for (j = 1; j <= 3; j++)
				c[i][j] = 2;
		}

		textView.setText("Click a button to start.");

		// add click listeners for each button
		for (i = 1; i <= 3; i++) {
			for (j = 1; j <= 3; j++) {
				b[i][j].setOnClickListener(new MyClickListener(i, j));
				b[i][j].setText(" ");
				b[i][j].setEnabled(true);
			}
		}
	}

	class MyClickListener implements View.OnClickListener {
		int x;
		int y;

		// Constructor
		public MyClickListener(int x, int y) {
			this.x = x;
			this.y = y;
		}
		
		// Handle the button click
		public void onClick(View view) {
			System.out.println("Button at position " + x + " " + y + "was clicked."); // DEBUGGING
			if (b[x][y].isEnabled()) {
				b[x][y].setEnabled(false);
				b[x][y].setText(xVal ? "X" : "O");
				c[x][y] = xVal ? 0 : 1;
				if (!checkBoard()) {
					disableButtons();
					send("MOVE " + clientId + " " + x + " " + y + " " + xVal);
				} else {
					send("QUIT " + clientId);
				}	
			}
		}
		
		// Disable ALL the buttons
		public void disableButtons() {
			for (int x=1; x<4; x++) {
				for (int y=1; y<4; y++) {
					b[x][y].setEnabled(false);
				}
			}
		}
	}
	
	// Check the board to see if someone has won or if it's a draw
	private boolean checkBoard() {
		boolean gameOver = false;
		if ((c[1][1] == 0 && c[2][2] == 0 && c[3][3] == 0)
				|| (c[1][3] == 0 && c[2][2] == 0 && c[3][1] == 0)
				|| (c[1][2] == 0 && c[2][2] == 0 && c[3][2] == 0)
				|| (c[1][3] == 0 && c[2][3] == 0 && c[3][3] == 0)
				|| (c[1][1] == 0 && c[1][2] == 0 && c[1][3] == 0)
				|| (c[2][1] == 0 && c[2][2] == 0 && c[2][3] == 0)
				|| (c[3][1] == 0 && c[3][2] == 0 && c[3][3] == 0)
				|| (c[1][1] == 0 && c[2][1] == 0 && c[3][1] == 0)) {
			textView.setText("Game over. You win!");
			gameOver = true;
		} else if ((c[1][1] == 1 && c[2][2] == 1 && c[3][3] == 1)
				|| (c[1][3] == 1 && c[2][2] == 1 && c[3][1] == 1)
				|| (c[1][2] == 1 && c[2][2] == 1 && c[3][2] == 1)
				|| (c[1][3] == 1 && c[2][3] == 1 && c[3][3] == 1)
				|| (c[1][1] == 1 && c[1][2] == 1 && c[1][3] == 1)
				|| (c[2][1] == 1 && c[2][2] == 1 && c[2][3] == 1)
				|| (c[3][1] == 1 && c[3][2] == 1 && c[3][3] == 1)
				|| (c[1][1] == 1 && c[2][1] == 1 && c[3][1] == 1)) {
			textView.setText("Game over. You lost!");
			gameOver = true;
		} else {
			boolean empty = false;
			for (i = 1; i <= 3; i++) {
				for (j = 1; j <= 3; j++) {
					if (c[i][j] == 2) {
						empty = true;
						break;
					}
				}
			}
			if (!empty) {
				gameOver = true;
				textView.setText("Game over. It's a draw!");
			}
		}
		return gameOver;
	}
}
	
	/*
	 * private class AI { 
	 * 		public void takeTurn() { 
	 * 			if(c[1][1]==2 && 
	 * 					((c[1][2]==0 && c[1][3]==0) 
	 * 				 || (c[2][2]==0 && c[3][3]==0) 
	 * 				 || (c[2][1]==0 &&  c[3][1]==0))) { 
	 * 				markSquare(1,1); 
	 * 			} else if (c[1][2]==2 && 
	 * 				((c[2][2]==0 && c[3][2]==0)
	 * 			  || (c[1][1]==0 && c[1][3]==0))) { 
	 * 				markSquare(1,2); 
	 * 			} else if(c[1][3]==2 && 
	 * 				((c[1][1]==0 && c[1][2]==0) 
	 * 			  || (c[3][1]==0 && c[2][2]==0) 
	 * 			  || (c[2][3]==0 && c[3][3]==0))) { 
	 * 				markSquare(1,3); 
	 * 			} else if (c[2][1]==2 && 
	 * 				((c[2][2]==0 && c[2][3]==0) 
	 * 			  || (c[1][1]==0 && c[3][1]==0))){ 
	 * 				markSquare(2,1); 
	 * 			} else if(c[2][2]==2 && 
	 * 				((c[1][1]==0 && c[3][3]==0) 
	 * 			  || (c[1][2]==0 && c[3][2]==0) 
	 * 			  || (c[3][1]==0 && c[1][3]==0)
	 * 			  || (c[2][1]==0 && c[2][3]==0))) { 
	 * 				markSquare(2,2); 
	 * 			} else if (c[2][3]==2 && 
	 * 				((c[2][1]==0 && c[2][2]==0) 
	 * 			  || (c[1][3]==0 && c[3][3]==0))) {
	 * 				markSquare(2,3); 
	 * 			} else if(c[3][1]==2 && 
	 * 				((c[1][1]==0 && c[2][1]==0) 
	 * 			  || (c[3][2]==0 && c[3][3]==0) 
	 * 			  || (c[2][2]==0 && c[1][3]==0))) {
	 * 				markSquare(3,1); 
	 * 			} else if(c[3][2]==2 && 
	 * 				((c[1][2]==0 && c[2][2]==0) 
	 * 			  || (c[3][1]==0 && c[3][3]==0))) { 
	 * 				markSquare(3,2); 
	 * 			} else if( c[3][3]==2 &&
	 * 				((c[1][1]==0 && c[2][2]==0) 
	 * 			  || (c[1][3]==0 && c[2][3]==0) 
	 * 			  || (c[3][1]==0 && c[3][2]==0))) { 
	 * 				markSquare(3,3); 
	 * 			} else { 
	 * 				Random rand = new Random();
	 * 				int a = rand.nextInt(4); 
	 * 				int b = rand.nextInt(4); 
	 * 				while(a==0 || b==0 || c[a][b]!=2) { 
	 * 					a = rand.nextInt(4); 
	 * 					b = rand.nextInt(4); 
	 * 				}
	 *				markSquare(a,b); } 
	 *		}
	 * 
	 * 		private void markSquare(int x, int y) {
	 * 			 b[x][y].setEnabled(false);
	 * 			 b[x][y].setText("X"); 
	 * 			 c[x][y] = 1; 
	 * 			 checkBoard(); 
	 * 		} 
	 * 	}
	 */