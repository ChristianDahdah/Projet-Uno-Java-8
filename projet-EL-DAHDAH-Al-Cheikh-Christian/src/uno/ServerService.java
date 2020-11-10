package uno;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;

public class ServerService {

	@SuppressWarnings("unused")
	private Socket socket;
	private InputStream in;
	private OutputStream out;
	private BufferedReader txtIn;
	private PrintWriter txtOut;
	private ArrayList<Card> hand;
	private boolean saidUno = false;
	private boolean turn;
	private String msg;
	private ArrayList<Card> discardPile;
	private ArrayList<String> discardPileString;
	private int points;
	private String playerName;
	private boolean status;
	private boolean tookCard = false;

	public ServerService(Socket socket) throws IOException {

		// Setting in/out streams between server and clients
		this.socket = socket;
		in = socket.getInputStream();
		out = socket.getOutputStream();
		txtIn = new BufferedReader(new InputStreamReader(in));
		txtOut = new PrintWriter(out, true);

		this.playerName = txtIn.readLine().split(" ")[1];
		sendToClient("bienvenue");
		hand = new ArrayList<Card>();
	}

	public String getRequest() throws IOException {
		msg = txtIn.readLine();

		System.out.println(playerName + ": " + msg);

		switch (msg) {
		case "Uno":
			return "Uno " + playerName;
		case "Contre-Uno":
			return "Contre-Uno " + playerName;
		case "je-pioche":
			if(tookCard)
				return "je-passe";
			tookCard = true;
			return "je-pioche";
		}

		if (turn) {
			if (msg.equals("je-passe"))
				return "je-passe " + playerName;

			String[] splittedMsg = msg.split(" ");

			if (splittedMsg[0].equals("je-pose"))

			{

				String requestedName = splittedMsg[1].split("-")[0];
				String requestedColor = splittedMsg[1].split("-")[1];

				// Condition for special black cards
				if (requestedName.equals("Joker") || requestedName.equals("+4")) {
					for (Card card : hand) {
						// Checking if player has the card
						if (card.getName().equals(requestedName)) {
							return card.checkPlay(requestedName, requestedColor) + " " + splittedMsg[1];
						}
					}
					return "Invalid-Play"; // Player does not have this card -- Giving penalty
				}

				// Condition for numbered cards, "+2","Inversion" and "Passer"
				for (Card card : hand) {
					// Checking if player has the card
					if (card.getFullName().equals(splittedMsg[1]))
						return card.checkPlay(discardPileString.get(0), discardPileString.get(1)) + " "
								+ splittedMsg[1];
				}
				return "Invalid-Play"; // Player does not have this card -- Giving penalty

			}
		}

		txtOut.println("Erreur : message invalide");
		return this.getRequest(); // If invalid command, re-listen to client
	}
	
	/**
	 * This function puts the played Card on the discardPile and removes it from the
	 * players hand
	 */
	public void removeCard(String cardToRemove) {

		String cardToRemoveName = cardToRemove.split("-")[0];
		String cardToRemoveColor = cardToRemove.split("-")[1];

		if (cardToRemoveName.equals("Joker") || cardToRemoveName.equals("+4"))
			for (int i = 0; i < hand.size(); i++) {
				if (cardToRemoveName.equals(hand.get(i).getName())) {
					discardPile.add(0, hand.get(i));
					hand.remove(i);
					discardPileString.clear();
					discardPileString.add(0, cardToRemove.split("-")[0]);
					discardPileString.add(1, cardToRemove.split("-")[1]);
					txtOut.println("OK");
					break;
				}
			}
		else
			for (int i = 0; i < hand.size(); i++) {
				if (cardToRemove.equals(hand.get(i).getName() + "-" + hand.get(i).getColor())) {
					discardPile.add(0, hand.get(i));
					hand.remove(i);
					discardPileString.clear();
					discardPileString.add(0, cardToRemoveName);
					discardPileString.add(1, cardToRemoveColor);
					txtOut.println("OK");
					break;
				}
			}

	}
	
	
	public void sendToClient(String msg) {
		txtOut.println(msg);
	}
	
	public void addCard(Card card) {
		hand.add(card);
		saidUno = false;
		txtOut.println("prends " + card.getName() + "-" + card.getColor());
	}

	public void addPoints() {
		for (Card card : hand)
			this.points += card.getValue();
	}

	public void notifyTurn() {
		turn = true;
		txtOut.println("nouveau-talon " + discardPileString.get(0) + "-" + discardPileString.get(1));
		txtOut.println("joue");
	}

	public void endTurn() {
		turn = false;
		tookCard = false;
	}

	public void clearHand() {
		hand.clear();
	}

	/*
	 * Getters
	 * 
	 * 
	 */
	
	public boolean getStatus() {
		return status;
	}

	public String getPlayerName() {
		return playerName;
	}

	public boolean getSaidUno() {

		return saidUno;
	}

	public boolean getTookCard() {
		return tookCard;
	}

	public int getPoints() {
		return this.points;
	}

	public int getNumberOfCards() {
		return hand.size();
	}

	
	/*
	 * Setters
	 * 
	 */
	
	public void setSaidUno(boolean status) {

		this.saidUno = status;
	}

	public void setStatus(boolean status) {
		this.status = status;
	}
	
	public void setDiscardPile(ArrayList<Card> discardPile, ArrayList<String> discardPileString) {
		this.discardPile = discardPile;
		this.discardPileString = discardPileString;
	}

}
