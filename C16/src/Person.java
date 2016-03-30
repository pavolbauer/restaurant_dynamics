import java.util.Random;


public class Person {

	private Position position;
	private int maximumDiningInterval;
	private int diningCountdown;
	
	
	
	public Person(Position position, int maximumDiningInterval) {
		this.position = position;
		this.maximumDiningInterval = maximumDiningInterval;
		this.diningCountdown = calculateNextVisit();
	}
	
	
	public boolean visitRestaurant() {
		if (diningCountdown > 0) {
			diningCountdown--;
			return false;
		} else { 
			diningCountdown = calculateNextVisit();
			return true;
		}
	}

	
	private int calculateNextVisit() {
		Random r = new Random();
		return r.nextInt(maximumDiningInterval+1);
	}


	public int getMaximumDiningInterval() {
		return maximumDiningInterval;
	}



	public void setMaximumDiningInterval(int maximumDiningInterval) {
		this.maximumDiningInterval = maximumDiningInterval;
	}



	public Position getPosition() {
		return position;
	}
	
}
