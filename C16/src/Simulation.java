import java.awt.Component;
import java.util.ArrayList;
import java.util.Collections;
import javax.swing.JProgressBar;
import javax.swing.JWindow;


public class Simulation {

	// NUR fuer Spatial analysis
	private World oneWorld;
	
	private int nrOfPersons;
	private int nrOfRestaurants;
	private int areaWidth;
	private int areaHeight;
	private int range;
	private int profitThreshold;
	private double openingProb;
	private double closingProb;
	private int threadcount;
	private JWindow d;
	private final JProgressBar progress = new JProgressBar();
	private Double taxincome;
	private Double meanRevenue;
	private boolean interrupted;
	
	
	public ArrayList<Position> init(int nrOfPersons, int nrOfRestaurants, int areaWidth, int areaHeight, int range, int profitThreshold, double openingProb, double closingProb, Component reference) {
		this.nrOfPersons = nrOfPersons;
		this.nrOfRestaurants = nrOfRestaurants;
		this.areaHeight = areaHeight;
		this.areaWidth = areaWidth;
		this.range = range;
		this.profitThreshold = profitThreshold;
		this.openingProb = openingProb;
		this.closingProb = closingProb;
		this.d = new JWindow();
		this.taxincome = 0.0;
		this.meanRevenue = 0.0;
		this.interrupted = false;
		
		//System.out.println("sim.init(" + nrOfPersons + ", " + nrOfRestaurants + ", " + areaWidth + ", " + areaHeight + ", " + range + ", " + profitThreshold + ", " + openingProb + ", " + closingProb + ")");
		
		// Progressdialog vorbereiten
		progress.setMinimum(0);
		progress.setValue(0);
		progress.setStringPainted(true);
		progress.setString("Calculating...");
		d.add(progress);
		d.setSize(300, 20);
		d.setLocationRelativeTo(reference);
		
		oneWorld = new World(nrOfPersons, nrOfRestaurants, areaWidth, areaHeight, range, profitThreshold, openingProb, closingProb);
		
		ArrayList<Person> personen = oneWorld.getPersons();		
		
		ArrayList<Position> positionList = new ArrayList<Position>();		
		
		for (Person person : personen) {
			positionList.add(person.getPosition());
		}
		
		return positionList;
	}

	
	
	public ArrayList<Position> spatialNextStep(double taxrate, double k) {
		ArrayList<Position> positionList = new ArrayList<Position>();
		ArrayList<Restaurant> restaurants = new ArrayList<Restaurant>();
		
		//System.out.println("sim.spatialNextStep("+taxrate+", "+k+")");
		
		restaurants = oneWorld.nextStep(taxrate, k);
		
		for (Restaurant restaurant : restaurants) {
			positionList.add(restaurant.getPosition());
		}
		
		return positionList;
	}
	
	
	public ArrayList<Double> timeRestaurants(int duration, final int runs, final double taxrate, final double k) {
		final int wochen = duration * 52;
		final int[][] count = new int[wochen][runs];
		final World[] worlds = new World[runs];
		boolean closeDialog = false;
		
		//System.out.println("sim.timeRestaurant("+duration+", "+runs+", "+taxrate+", "+k+")");
		
		if (!d.isVisible()) {
			progress.setMaximum(runs*wochen);
			d.setVisible(true);
			closeDialog = true;
		}
		
		threadcount = runs;
		
		for (int i = 0; i < runs; i++) {
			if (interrupted) {
				break;
			}
			final int run = i;
			new Thread(new Runnable() {
				
				public void run() {
					worlds[run] = new World(nrOfPersons, nrOfRestaurants, areaWidth, areaHeight, range, profitThreshold, openingProb, closingProb);
					for (int woche = 0; woche < wochen; woche++) {
						if (interrupted) {
							break;
						}
						progress.setValue(progress.getValue()+1);
						count[woche][run] = worlds[run].nextStep(taxrate, k).size();
					}
					taxincome = taxincome + worlds[run].getTaxincome() / runs;
					meanRevenue = meanRevenue + worlds[run].getMeanRevenue() / runs;
					threadcount--;
				}
			}).start();
		}
		
		while(threadcount > 0) {
			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {
			}
		}
		
		ArrayList<Double> restaurants = new ArrayList<Double>();
		
		for (int woche = 0; woche < wochen; woche++) {
			int sum = 0;
			for (int run = 0; run < runs; run++) {
				sum = sum + count[woche][run];
			}
			double mean = 1.0*sum/runs;
			restaurants.add(mean);
		}		
		
		if (closeDialog) {
			d.dispose();
		}
		
		return restaurants;
	}
	
	
	public ArrayList<Position> timeTaxIncome(int duration, int runs, double k) {
		ArrayList<Position> result = new ArrayList<Position>();

		//System.out.println("sim.timeTaxIncome("+duration+", "+runs+", "+k+")");
		
		progress.setMaximum(duration*52*runs*110);
		d.setVisible(true);
		
		
		double max = 0, taxatmax = 0;
		
		for (double tax = 0; tax <= 1; tax = tax + 0.01) {
			if (interrupted) {
				d.dispose();
				return result;
			}
			taxincome = 0.0;
			timeRestaurants(duration, runs, tax, k);
			Position p = new Position(new Double(1000*tax).intValue(), taxincome.intValue());
			if (taxincome > max) {
				max = taxincome;
				taxatmax = tax;
			}
			result.add(p);
		}
		
		
		// Größeren Nachbarn von max finden
		double anfang = taxatmax, ende = taxatmax;
		if (result.get(new Double(taxatmax*100-1).intValue()).getY() > result.get(new Double(taxatmax*100+1).intValue()).getY()) {
			anfang = taxatmax - 0.01;
		} else {
			ende = taxatmax + 0.01;
			
		}
		
		// Zweiter Durchgang
		for (double tax = anfang; tax < ende; tax = tax + 0.001) {
			if (interrupted) {
				break;
			}
			taxincome = 0.0;
			timeRestaurants(duration, runs, tax, k);
			Position p = new Position(new Double(1000*tax).intValue(), taxincome.intValue());
			result.add(p);
		}
		
		// Nach X sortieren
		Position.setSortbyX(true);
		Collections.sort(result);
		Position.setSortbyX(false);
		
		d.dispose();
		return result;
	}
	
	
	public ArrayList<Position> timeOverK(int duration, int runs, double taxrate) {
		ArrayList<Position> result = new ArrayList<Position>();
		
		//System.out.println("sim.timeOverK("+duration+", "+runs+", "+taxrate+")");		
		
		progress.setMaximum(duration*52*runs*60);
		d.setVisible(true);
		
		for (double k = 0; k <= 6; k = k + 0.1) {
			if (interrupted) {
				break;
			}
			
			meanRevenue = 0.0;
			
			timeRestaurants(duration, runs, taxrate, k);
			
			Position p = new Position(new Double(10*k).intValue(), meanRevenue.intValue());

			result.add(p);
		}
		
		d.dispose();
		return result;
	}
	
	public void interrupt() {
		this.interrupted = true;
	}
}
