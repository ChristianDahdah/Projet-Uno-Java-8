package uno;

public class SpecialColoredCard extends Card{

	public SpecialColoredCard(String name, String color, int value) {
		super(name, color, value);
		
	}

	@Override
	public String checkPlay(String discardPileName, String discardPileColor) {
		
		
		if(discardPileColor.equals(this.color)||discardPileName.equals(this.name)) // Check if color is valid 
		{
			if("+2".equals(this.name))
				return "+2"; // Valid play
			else if ("Inversion".equals(this.name))
				return "Inversion"; // Valid play
			else if ("Passer".equals(this.name))
				return "Passer"; // Valid play
		}
		
		return "Invalid-Play"; // Invalid play. Colors do not match
	}

}
