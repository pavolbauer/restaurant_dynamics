
public class Restaurant {
	
	private Position position;
	private int runningCost;
	private int dinnerCost;
	private int revenue = 0;
	private boolean isNew = true;
	
	
	public Restaurant(Position position, int runningCost, int dinnerCost) {
		this.position = position;
		this.runningCost = runningCost;
		this.dinnerCost = dinnerCost;
	}

	
	public void visit() {
		revenue = revenue + dinnerCost;
	}
	
	
	public double finishWeek(double tax) {
		double profit = revenue - runningCost;
		revenue = 0;
		if (profit > 0) {
			profit = profit * (1 - tax);
		}
		return profit;
	}
	


	public int getRunningCost() {
		return runningCost;
	}



	public void setRunningCost(int runningCost) {
		this.runningCost = runningCost;
	}



	public int getDinnerCost() {
		return dinnerCost;
	}



	public void setDinnerCost(int dinnerCost) {
		this.dinnerCost = dinnerCost;
	}


	public Position getPosition() {
		return position;
	}
	
	
	public boolean isNew() {
		boolean temp = isNew;
		isNew = false;
		return temp;
	}
	
	public int getRevenue() {
		return revenue;
	}

}
