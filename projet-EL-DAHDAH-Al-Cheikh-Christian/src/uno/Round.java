package uno;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

public class Round implements Runnable {

	private ArrayList<ServerService> connectedPlayers;
	private Deck pile;
	private ArrayList<Card> discardPile;
	private ArrayList<String> discardPileString;
	private int playerTurn;
	private int turnDirection; // can take value of +1 or -1
	private int numberOfPlayers;
	private int start;
	private boolean roundStatus;
	private String caseName;
	private String caseAttribute;

	// Constructor
	public Round(ArrayList<ServerService> connectedPlayers, int numberOfPlayers) {
		this.connectedPlayers = connectedPlayers;
		this.numberOfPlayers = numberOfPlayers;
		turnDirection = +1;
	}

	
	//Start of round
	public void startRound() throws InterruptedException {
		// Generating a new shuffled deck such that it has no +4 of Joker card at the
		// top
		System.out.println("Starting new round");

		pile = new Deck();
		pile.deckGenerator();

		// Initializing discardPile
		discardPile = new ArrayList<Card>();
		discardPile.add(pile.takeCard());
		discardPileString = new ArrayList<String>();
		discardPileString.add(0, discardPile.get(0).getName());
		discardPileString.add(1, discardPile.get(0).getColor());

		// Giving 7 cards for each player and giving him the pointer of the discard Pile
		for (ServerService player : connectedPlayers) {

			// Clear hand of player to avoid having cards from last round
			player.clearHand();
			player.sendToClient("debut-de-manche");

			for (int i = 0; i < 7; i++)
				player.addCard(pile.takeCard());
			player.setDiscardPile(discardPile, discardPileString);
			player.sendToClient("nouveau-talon " + discardPile.get(0).getName() + "-" + discardPile.get(0).getColor());
		}

		// Determining which player starts
		playerTurn = new Random().nextInt(numberOfPlayers);

		// Telling players that game started
		start = 0;

		roundStatus = false;

		if (discardPile.get(0).getName().equals("Inversion")) // If first turned card is Inversion we apply it directly
			turnDirection *= -1;
		else if (discardPile.get(0).getName().equals("+2")) { // If first turned card is +2 we apply it directly
			giveCard(2, connectedPlayers.get(playerTurn));
			nextTurn(1);
		} else if (discardPile.get(0).getName().equals("Passer"))
			nextTurn(1);

		for (ServerService player : connectedPlayers) {
			Thread notifyingPlayer = new Thread(this);
			player.setStatus(false);// Notifying players that a new round is starting
			notifyingPlayer.start();

			while (!player.getStatus())
				; // waiting for the player to take the value of start before changing it
			start++;
		}

		roundStatus = true;

		while (roundStatus) {
			for (ServerService player : connectedPlayers)
				checkEndOfRound(player);
		} // wait till round ends
	}

	public synchronized void applyCase(String cases) {
		// SWITCH CASES

		caseName = cases.split(" ")[0];
		if (cases.split(" ").length > 1)
			// case attribute can be a card, a card's color or a player's name, depending on
			// the case's name
			caseAttribute = cases.split(" ")[1];

		switch (caseName) {

		case "Invalid-Play": // card cannot be played, +2 cards penalty
			connectedPlayers.get(playerTurn).sendToClient("Jeu-Invalide");

			// in case of +4 or Joker card, the only time they are invalid if the requested
			// color was not true

			if (caseAttribute.split("-")[0].equals("Joker")) {// sending back card to client
				connectedPlayers.get(playerTurn).sendToClient("prends " + "Joker-noir");
			} else if (caseAttribute.split("-")[0].equals("+4")) {
				connectedPlayers.get(playerTurn).sendToClient("prends " + "+4-noir");
			} else {
				connectedPlayers.get(playerTurn).sendToClient("prends " + caseAttribute);
			}

			giveCard(2, connectedPlayers.get(playerTurn));
			nextTurn(1);
			break;

		case "Normal-Play": // for Numbered cards and Joker
			System.out.println("Case attribute is " + caseAttribute);
			connectedPlayers.get(playerTurn).removeCard(caseAttribute);
			notifyNewDiscardPile(caseAttribute);
			checkEndOfRound(connectedPlayers.get(playerTurn));

			nextTurn(1);
			break;

		case "+2":
			connectedPlayers.get(playerTurn).removeCard(caseAttribute);
			notifyNewDiscardPile(caseAttribute);

			if (playerTurn + turnDirection < 0)
				giveCard(2, connectedPlayers.get(numberOfPlayers - 1));
			else if (playerTurn + turnDirection >= numberOfPlayers)
				giveCard(2, connectedPlayers.get(0));
			else
				giveCard(2, connectedPlayers.get(playerTurn + turnDirection));

			checkEndOfRound(connectedPlayers.get(playerTurn));

			nextTurn(2);
			break;

		case "Inversion":
			connectedPlayers.get(playerTurn).removeCard(caseAttribute);
			notifyNewDiscardPile(caseAttribute);
			checkEndOfRound(connectedPlayers.get(playerTurn));

			turnDirection = turnDirection * (-1);

			checkEndOfRound(connectedPlayers.get(playerTurn));

			if (numberOfPlayers == 2)
				nextTurn(0);
			else
				nextTurn(1);

			break;

		case "Passer":
			connectedPlayers.get(playerTurn).removeCard(caseAttribute);
			notifyNewDiscardPile(caseAttribute);

			checkEndOfRound(connectedPlayers.get(playerTurn));

			nextTurn(2);
			break;

		case "+4":
			connectedPlayers.get(playerTurn).removeCard(caseAttribute);
			notifyNewDiscardPile(caseAttribute);

			if (playerTurn + turnDirection < 0)
				giveCard(4, connectedPlayers.get(numberOfPlayers - 1));
			else if (playerTurn + turnDirection >= numberOfPlayers)
				giveCard(4, connectedPlayers.get(0));
			else
				giveCard(4, connectedPlayers.get(playerTurn + turnDirection));

			checkEndOfRound(connectedPlayers.get(playerTurn));
			nextTurn(2);
			break;

		case "Contre-Uno":
			for (ServerService player : connectedPlayers) {
				/**
				 * If a player did not say Uno and has only 1 card he will take 2 cards. Last
				 * condition !caseAttribute.equals(player.getPlayerName() is added to prevent
				 * someone saying Contre-Uno to himself
				 */
				if (!caseAttribute.equals(player.getPlayerName())) {
					player.sendToClient(caseAttribute + " a dit Contre-Uno");
					if (!player.getSaidUno() && player.getNumberOfCards() == 1) {
						giveCard(2, player);
					}
				}
			}
			break;

		case "Uno":

			for (ServerService player : connectedPlayers) {
				// Case attribute is the name of the person who said Uno.
				if (player.getPlayerName().equals(caseAttribute)) {
					player.setSaidUno(true);
				} else {
					player.sendToClient(caseAttribute + " a dit uno.");
				}

			}
			break;

		case "je-passe":
			if (!connectedPlayers.get(playerTurn).getTookCard())
				giveCard(1, connectedPlayers.get(playerTurn));
			for (ServerService player : connectedPlayers) {
				if (player.getPlayerName().equals(caseAttribute))
					player.sendToClient("OK");
				else
					player.sendToClient("joueur " + caseAttribute + " passe");
			}
			nextTurn(1);
			break;

		case "je-pioche":
			giveCard(1, connectedPlayers.get(playerTurn));
			break;
		}

	}

	public void notifyNewDiscardPile(String card) {

		for (int i = 0; i < numberOfPlayers; i++)
			if (i != playerTurn)
				connectedPlayers.get(i)
						.sendToClient("joueur " + connectedPlayers.get(playerTurn).getPlayerName() + " pose " + card);
	}

	public void nextTurn(int step) {
		connectedPlayers.get(playerTurn).endTurn();

		// To avoid setting turn to true while round has ended

		for (int i = 0; i < step; i++) {
			playerTurn = playerTurn + turnDirection;

			if (playerTurn < 0)
				playerTurn = numberOfPlayers - 1;

			else if (playerTurn >= numberOfPlayers)
				playerTurn = 0;
		}

		if (roundStatus) {
			connectedPlayers.get(playerTurn).notifyTurn();
		}
	}

	public void checkEndOfRound(ServerService player) {
		if (player.getNumberOfCards() == 0) {
			for (ServerService players : connectedPlayers)
				players.addPoints();
			roundStatus = false;
			System.out.println("Hatayta false ntaket");
		}
	}

	@Override
	public void run() {

		int playerNumber = start;

		ServerService player = connectedPlayers.get(playerNumber);
		player.setStatus(true);

		System.out.println("Player " + player.getPlayerName() + " is set");

		if (playerNumber == playerTurn)
			player.notifyTurn();

		String playCase = "Disconnection"; // if player disconnected

		try {
			playCase = player.getRequest();
		} catch (IOException e) {
			System.out.println("Client disconnected");
		}

		while (roundStatus) {
			this.applyCase(playCase);
			try {
				playCase = player.getRequest();
			} catch (IOException e) {
				System.out.println("Client disconnected");
				break;
			}
		}

	}

	// Giving card for a player
	public void giveCard(int numberOfCards, ServerService player) {

		player.setSaidUno(false);

		for (int i = 0; i < numberOfCards; i++) {
			if (pile.getSize() == 0)
				pile.addToDeck(discardPile);
			player.addCard(pile.takeCard());
		}

		// Notifying other players
		for (ServerService notifyPlayers : connectedPlayers) {
			if (notifyPlayers != player)
				notifyPlayers.sendToClient("joueur " + player.getPlayerName() + " pioche " + numberOfCards);
		}

	}

	public boolean getRoundStatus() {
		return roundStatus;
	}
}
