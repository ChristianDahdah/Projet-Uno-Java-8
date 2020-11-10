package uno;

public class SpecialBlackCard extends Card {

	private final static String[] AVAILABLE_COLORS = { "rouge", "vert", "jaune", "bleu" };

	public SpecialBlackCard(String name, String color, int value) {
		super(name, color, value);
		// TODO Auto-generated constructor stub
	}

	@Override
	public String checkPlay(String cardNameRequest, String colorRequest) {
		// TODO Auto-generated method stub

		for (String color : AVAILABLE_COLORS) {
			if (color.equals(colorRequest)) // Checking if color is valid
			{
				if ("Joker".equals(cardNameRequest))
					return "Normal-Play";
				else if ("+4".equals(cardNameRequest))
					return "+4";
				break;
			}
		}

		return "Invalid-Play";
	}

}
