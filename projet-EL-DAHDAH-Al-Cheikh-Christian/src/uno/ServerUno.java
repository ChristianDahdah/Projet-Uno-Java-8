package uno;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Scanner;

public class ServerUno {

	private ServerSocket serverSocket;
	private Socket socket;
	private ServerService service;
	private int numberOfPlayers;
	private ArrayList<ServerService> connectedPlayers;

	public ServerUno(int port) throws IOException {

		serverSocket = new ServerSocket(port);
		System.out.println("Server is running on port " + 9000);

		// ask from user how many players -- must be between 2 and 9
		@SuppressWarnings("resource")
		Scanner inputScanner = new Scanner(System.in);
		
		do {
			System.out.println("Enter how many players. Must be between 2 and 9");
			numberOfPlayers = inputScanner.nextInt();
		} while (numberOfPlayers > 9 || numberOfPlayers < 2);
		
		
		connectedPlayers = new ArrayList<ServerService>();
	
	}

	public void acceptPlayers() throws IOException {

		int connectedSockets = 0;
		
		System.out.println("Accepting players");
		
		while (connectedSockets < numberOfPlayers) {
			// wait for new client connection

			socket = serverSocket.accept();
			System.out.printf("New client connexion, client port %d\n", socket.getPort());
			
			service = new ServerService(socket);

			connectedPlayers.add(service);

			connectedSockets++;
		}
		System.out.println("All players are connected");
	}

	public ArrayList<ServerService> getConnectedPlayers()
	{
		return connectedPlayers;
	}
	
	public int getNumberOfPlayers() {
		return numberOfPlayers;
	}
	
	
	/*
	 * Main
	 */
	
	public static void main(String[] args) throws IOException, InterruptedException {
		
	ServerUno server=new ServerUno(9000);	
	server.acceptPlayers();
	
	Round round;
	
	int highestScore=0;
	String sendToClients;
	
	int playerPoints;
	
	do
	{
	round=new Round (server.getConnectedPlayers(),server.getNumberOfPlayers());
	round.startRound();	
	sendToClients="";
	
	for(ServerService player:server.getConnectedPlayers())
	{
		playerPoints=player.getPoints();
		if(highestScore<playerPoints)
			highestScore=playerPoints;
		sendToClients=sendToClients+" "+player.getPlayerName()+" "+playerPoints;
	}
	
	if(highestScore<500)
		sendToClients="fin-de-manche"+sendToClients;
	else
		sendToClients="fin-de-partie"+sendToClients;
	
	for(ServerService player:server.getConnectedPlayers())
	{
		player.sendToClient("\n"+sendToClients+"\n");
	}
	
	
	}
	while(highestScore<500);
	
	System.out.println("Game ended, server exiting");

	}

}
