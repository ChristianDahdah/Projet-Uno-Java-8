package uno;

public class NumberedCard extends Card {

	public NumberedCard(String name,String color, int value) {
		super(name, color, value);
		// TODO Auto-generated constructor stub
	}

	@Override
	public String checkPlay(String nameTalon, String colorTalon) {
		// TODO Auto-generated method stub
		
		if(colorTalon.equals(this.color))
			return "Normal-Play"; //denotes when card can be played and proceed with normal turn
		else if(nameTalon.equals(this.name))
			return "Normal-Play";
		
		return "Invalid-Play"; //denotes case where card cannot be played
	}
	
}
