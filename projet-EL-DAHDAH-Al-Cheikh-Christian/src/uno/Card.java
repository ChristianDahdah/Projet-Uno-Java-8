package uno;

public abstract class Card {

	protected String name;
	protected String color;
	private int value;

	public Card(String name, String color, int value) {
		super();
		this.name = name;
		this.color = color;
		this.value = value;
	}

	public int getValue() {
		return value;
	}

	public String getName() {
		return name;
	}

	public String getColor() {
		return color;
	}

	public abstract String checkPlay(String name,String color);

	
	public String getFullName()
	{
		return name+"-"+color;
	}
	
}
