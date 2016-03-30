import java.util.ArrayList;
import java.util.Random;

public class World {

	private ArrayList<Person> persons = new ArrayList<Person>();
	private ArrayList<Restaurant> restaurants = new ArrayList<Restaurant>();
	private ArrayList<Integer> revenueList = new ArrayList<Integer>();
	private int[][] peopleDensity;
	private int areaWidth;
	private int areaHeight;
	private int range;
	private int profitThreshold;
	private double openingProb;
	private double closingProb;
	private Double taxincome = 0.0;

	// Konstante
	private int[] maxR = { 100, 250, 250, 100, 300 };
	private double[] cityPopulation = { 0.1, 0.25, 0.25, 0.1, 0.3 };
	private int[] cityX = { 100, 360, 180, 510, 480 };
	private int[] cityY = { 70, 90, 250, 130, 300 };
	private int runningCost = 150;
	private int dinnerCost = 1;

	public World(int nrOfPersons, int nrOfRestaurants, int areaWidth, int areaHeight, int range, int profitThreshold, double openingProb, double closingProb) {
		this.areaHeight = areaHeight;
		this.areaWidth = areaWidth;
		this.range = range;
		this.profitThreshold = profitThreshold;
		this.openingProb = openingProb;
		this.closingProb = closingProb;

		Random r = new Random();
		peopleDensity = new int[areaWidth / 20][areaHeight / 20];

		// Positionen der Personen zufällig generieren, Häufung bei den Städten
		for (int i = 0; i < maxR.length; i++) {
			int maxR_ = maxR[i];

			for (int j = 0; j < cityPopulation[i] * nrOfPersons; j++) {
				Double x = 0.0;
				Double y = 0.0;

				do {
					double radius = maxR_ - maxR_ * Math.sqrt(1 - r.nextDouble());
					double angle = r.nextDouble() * 2 * Math.PI; // Winkel in
					// rad

					x = radius * Math.cos(angle) + cityX[i];
					y = radius * Math.sin(angle) + cityY[i];
				} while (x < 0 || x > areaWidth || y < 0 || y > areaHeight);

				Position pos = new Position(x.intValue(), y.intValue());
				persons.add(new Person(pos, 8));

				int cellx = x.intValue() / 20;
				int celly = y.intValue() / 20;
				peopleDensity[cellx][celly]++;
			}

		}

		// Restaurants gleichmäßig verteilen
		Double cols = Math.ceil(Math.sqrt(6 * nrOfRestaurants / 5));
		Double rows = Math.ceil(nrOfRestaurants / cols);
		Double hSpace = areaWidth / cols;
		Double vSpace = areaHeight / rows;
		Double x = hSpace / 2;
		Double y = vSpace / 2;

		for (int i = 0; i < nrOfRestaurants; i++) {
			Position pos = new Position(x.intValue(), y.intValue());
			restaurants.add(new Restaurant(pos, runningCost, dinnerCost));
			x = x + hSpace;
			if (x > areaWidth) {
				x = hSpace / 2;
				y = y + vSpace;
			}
		}
	}
	
	

	public ArrayList<Restaurant> nextStep(double taxrate, double k) {
		ArrayList<Restaurant> open = new ArrayList<Restaurant>();
		ArrayList<Restaurant> close = new ArrayList<Restaurant>();
				
		Random r = new Random();

		// Personen besuchen Restaurants an 7 Tagen pro Woche
		for (int i = 0; i < 7; i++) {
			for (Person pers : persons) {
				if (pers.visitRestaurant()) {
					ArrayList<Restaurant> candidates = withinRange(pers);
					if (candidates.size() > 0) {
						int choose = r.nextInt(candidates.size());
						candidates.get(choose).visit();
					}
				}
			}
		}

		// Wochenabrechnung
		for (Restaurant rest : restaurants) {
			
			if (rest.isNew()) {
				revenueList.add(rest.getRevenue());
			}
			
			double profit = rest.finishWeek(taxrate);
			if (profit > 0) {
				taxincome = taxincome + taxrate * profit / (1 - taxrate);
			}
			if (profit > profitThreshold) {
				if (r.nextDouble() < openingProb) {
					Position pos = positionForNewRestaurant(k);
					open.add(new Restaurant(pos, runningCost, dinnerCost));
				}
			} else {
				if (r.nextDouble() < closingProb) {
					close.add(rest);
				}
			}
		}

		restaurants.removeAll(close);
		restaurants.addAll(open);

		return restaurants;
	}

	private Position positionForNewRestaurant(double k) {
		Random r = new Random();
		double maxRatio = 0;
		int maxX = 0;
		int maxY = 0;

		for (int x = 0; x < areaWidth / 20; x++) {
			for (int y = 0; y < areaHeight / 20; y++) {

				double restaurantDensity = 0;
				for (Restaurant restaurant : restaurants) {
					Position cellcenter = new Position(x * 20 + 10, y * 20 + 10);
					double distance = dist(cellcenter, restaurant.getPosition());
					restaurantDensity = restaurantDensity + 1 / Math.pow(distance, k);
				}

				if ((peopleDensity[x][y] / restaurantDensity) > maxRatio) {
					maxRatio = peopleDensity[x][y] / restaurantDensity;
					maxX = x;
					maxY = y;
				}
			}
		}

		int x = maxX * 20 + r.nextInt(20);
		int y = maxY * 20 + r.nextInt(20);
		return new Position(x, y);
	}

	private ArrayList<Restaurant> withinRange(Person pers) {
		ArrayList<Restaurant> result = new ArrayList<Restaurant>();

		for (Restaurant rest : restaurants) {
			double distance = dist(pers.getPosition(), rest.getPosition());
			if (distance < range) {
				result.add(rest);
			}
		}
		return result;
	}

	private double dist(Position a, Position b) {
		double hdiff = a.getX() - b.getX();
		double vdiff = a.getY() - b.getY();
		return Math.sqrt(Math.pow(hdiff, 2) + Math.pow(vdiff, 2));
	}

	public ArrayList<Person> getPersons() {
		return persons;
	}

	public ArrayList<Restaurant> getRestaurants() {
		return restaurants;
	}

	public Double getTaxincome() {
		return taxincome;
	}
	
	public double getMeanRevenue() {
		double meanRevenue = 0;
		for (Integer rev : revenueList) {
			meanRevenue = meanRevenue + rev;
		}
		return meanRevenue / revenueList.size();
	}

}