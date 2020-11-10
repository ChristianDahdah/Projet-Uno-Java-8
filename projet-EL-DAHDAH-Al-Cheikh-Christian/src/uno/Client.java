package uno;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Scanner;

public class Client implements Runnable {

	private Socket socket;
	private InputStream in;
	private OutputStream out;
	private BufferedReader txtIn;
	private PrintWriter txtOut;
	private boolean gameStatus;
	private boolean roundStatus;
	private ArrayList<String> hand;
	private String receivedMsg;
	private String[] splitMsg;
	private boolean cardFound;

	public Client(String host, int port, String playerName) throws IOException {

		socket = new Socket(host, port);
		out = socket.getOutputStream();
		in = socket.getInputStream();
		txtIn = new BufferedReader(new InputStreamReader(in));
		txtOut = new PrintWriter(out, true);

		txtOut.println("je-suis " + playerName);
		hand = new ArrayList<String>();

	}

	public String printHand() {
		String printCards = "{";
		boolean firstCard = true;
		for (String card : hand) {
			if (firstCard) {
				printCards = printCards + card;
				firstCard = false;
			} else
				printCards = printCards + " | " + card;
		}
		return printCards + "}";
	}

	public void read() throws IOException {
		receivedMsg = txtIn.readLine();
		System.out.println(receivedMsg);

		splitMsg = receivedMsg.split(" ");

		switch (splitMsg[0]) {
		
		case "joue":
			System.out.println("Mes cartes sont: " + printHand());
			break;
		case "debut-de-manche":
			gameStatus = true;
			roundStatus = true;
			break;
		case "fin-de-manche":
			roundStatus = false;
			hand.clear();
			break;
		case "fin-de-partie":
			roundStatus = false;
			gameStatus = false;
			hand.clear();
			break;
		case "prends":
			hand.add(splitMsg[1]);
		}

	}

	@Override
	public void run() {

		while (!gameStatus)
			try {
				read();
			} catch (IOException e1) {
				e1.printStackTrace();
			}

		while (gameStatus) {
			try {
				read();
			} catch (IOException e) {
				System.out.println("Game ended");
				gameStatus = false;
			}
		}

	}

	private void send(String userMsg) {

		String[] splitMsg = userMsg.split(" ");

		// Condition to avoid sending an empty card name
		if (userMsg.equals("Uno")) {
			if (hand.size() == 1)
				txtOut.println(userMsg);
			else
				System.out.println("Invalide. Tu as plus qu'une carte");
		} else if (splitMsg[0].equals("je-pose")) {
			if (splitMsg.length == 2) {

				cardFound = false;

				// Case of special black cards
				if (splitMsg[1].split("-")[0].equals("Joker") || splitMsg[1].split("-")[0].equals("+4")) {
					for (int i = 0; i < hand.size(); i++) {
						if (hand.get(i).split("-")[0].equals("Joker") || hand.get(i).split("-")[0].equals("+4")) {
							hand.remove(i);
							txtOut.println(userMsg);
							cardFound = true;
							break; // break is necessary in order not to delete or send two cards having the same
							// name
						}
					}
				}

				else { // Case of non-black cards
					for (int i = 0; i < hand.size(); i++) {
						if (splitMsg[1].equals(hand.get(i))) {
							hand.remove(i);
							txtOut.println(userMsg);
							cardFound = true;
							break; // break is necessary in order not to delete or send two cards having the same
							// name
						}
					}
				}

				if (!cardFound)
					System.out.println("Tu n'as pas cette carte, re-pose une autre");

			} else
				System.out.println("Tu ne peux pas envoyer une carte vide");
		} else
			txtOut.println(userMsg);

	}

	// Getters
	private boolean getRoundStatus() {
		return roundStatus;
	}

	private boolean getGameStatus() {
		return gameStatus;
	}

	// Client Main
	public static void main(String[] args) throws IOException {
		@SuppressWarnings("resource")
		Scanner inputScanner = new Scanner(System.in);
		System.out.println("Insert player name");
		String userMsg = inputScanner.nextLine();

		Client player = new Client("127.0.0.1", 9000, userMsg);

		Thread playerListeningThread = new Thread(player);
		playerListeningThread.start();

		do {
			// try/catch to avoid sending a message when game ends or server disconnected
			try {
				userMsg = inputScanner.nextLine();
				if (player.getRoundStatus()) // to avoid sending message while round is not running
					player.send(userMsg);
			} catch (Exception e) {
				System.out.println("Game ended");
				player.setGameStatus(false);
			}

		} while (player.getGameStatus());
	}

	private void setGameStatus(boolean b) {
		gameStatus = false;
	}

}
