package uno;

import java.util.ArrayList;
import java.util.Collections;

public class Deck {

	private ArrayList<Card> deck;

	Deck() {
		deck = new ArrayList<Card>();
	}

	public void deckGenerator() {
		// Generating non black cards
		String[] colors = { "vert", "bleu", "rouge", "jaune" };

		for (String color : colors) {
			deck.add(new NumberedCard("0", color, 0)); // Generating only 1 card of value '0'

			for (int i = 1; i <= 9; i++)// Generating 2 cards of each value '1' to '9'
			{
				deck.add(new NumberedCard(String.valueOf(i), color, i));
				deck.add(new NumberedCard(String.valueOf(i), color, i));
			}

			for (int i = 1; i <= 2; i++)// Generating 2 cards of "Reverse", "+2" and "Block"
			{
				deck.add(new SpecialColoredCard("Inversion", color, 20));
				deck.add(new SpecialColoredCard("+2", color, 20));
				deck.add(new SpecialColoredCard("Passer", color, 20));
			}

		}

		// Generating black cards
		for (int i = 1; i <= 4; i++) {
			deck.add(new SpecialBlackCard("Joker", "noir", 50));
			deck.add(new SpecialBlackCard("+4", "noir", 50));
		}

		Collections.shuffle(deck);
		
		//Condition to not get a +4 or a joker at the top of the deck
		while(deck.get(0).getName().equals("Joker")||deck.get(0).getName().equals("+4")) 
			Collections.shuffle(deck);
	}
	
	public Card takeCard()
	{
		Card card=deck.get(0);
		deck.remove(0);
		return card;
	}
	
	public int getSize()
	{
		return deck.size();
	}

	public void addToDeck(ArrayList<Card> discardPile) {
		for(int i=1;i<discardPile.size();i++)
		{
			deck.add(discardPile.get(i));
			discardPile.remove(i);
		}
		Collections.shuffle(deck);
	}
	
}
