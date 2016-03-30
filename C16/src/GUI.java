/*import gov.noaa.pmel.sgt.JPane;
import gov.noaa.pmel.sgt.dm.Collection;
import gov.noaa.pmel.sgt.swing.JPlotLayout;*/

import ptolemy.plot.Plot;

import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Collections;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JApplet;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import javax.swing.border.MatteBorder;


public class GUI extends JApplet {
	
	private static final long serialVersionUID = -3733044381701753021L;
	
	private static Color customgray = new Color(235, 235, 235);
	private static Color customblue = new Color(207, 225, 235);
	
	private final JApplet selbstreferenz = this;
	
	// Simulationsobjekt
	private Simulation sim = new Simulation();
	
	// Thread für alle Simulationen
	private Thread workerThread;
	
	// Läuft der Thread oder ist er angehalten?
	private boolean spatialSimRunning = false;
	
	// Soll eine neue Sim gestartet werden oder die existierende weiterlaufen?
	private boolean spatialSimNew = true;
	
	
	private JTextField headline = new JTextField("Restaurant Business Dynamics");
	private JButton hide = new JButton("Hide Description");
	private JPanel balkenDescription = new JPanel();
//	private JPanel darstellung = new JPanel();
	//private JPlotLayout darstellungsLayout = new JPlotLayout(JPlotLayout.LINE, false, false, "Darstellung", null, false);
	private JPanel balkenEingabe = new JPanel();
	private JPanel lblParameter = new JPanel();
	private JPanel eingabefelder = new JPanel();
	private JButton play = new JButton("Play");
	private JButton stop = new JButton("Stop");
	private JButton reset = new JButton("Reset");
	private JButton defaults = new JButton("Load defaults");
	private JPanel balkenPlay = new JPanel();
	private JPanel balkenErgebnis = new JPanel();
	private JPanel lblErgebnis = new JPanel();
	private JPanel ergebnisfelder = new JPanel();
	private JLabel lblType = new JLabel("Type of simulation");
	private JLabel lblSpeed = new JLabel("Simulation speed [Weeks/s]");
	private JLabel lblDuration = new JLabel("Duration [years]");
	private JLabel lblRestaurants = new JLabel("Initial number of restaurants");
	private JLabel lblRuns = new JLabel("Number of runs");
	private JLabel lblTax = new JLabel("Tax rate [%]");
	private JLabel lblProfit = new JLabel("Profit threshold");
	private JComboBox type = new JComboBox();
	private JSpinner speed = new JSpinner(new SpinnerNumberModel(1, 1, 100, 1));
	private JSpinner duration = new JSpinner(new SpinnerNumberModel(5, 1, 20, 1));
	private JSpinner restaurants = new JSpinner(new SpinnerNumberModel(30, 1, 80, 1));
	private JSpinner runs = new JSpinner(new SpinnerNumberModel(50, 1, 100, 1));
	private JSpinner tax = new JSpinner(new SpinnerNumberModel(20, 0, 100, 1));
	private JSpinner profit = new JSpinner(new SpinnerNumberModel(350, 1, 1000, 1));
	private JLabel lblResult = new JLabel("Number of restaurants");
	private JTextField result = new JTextField();
	private JTextArea fliesstext = new JTextArea("Welcome to the ARGESIM Comparison C16 - 'Restaurant Business Dynamics' -  Applet!\n"+
"We simulate a spatial environment with randomly distributed population, concentrated around 5 clusters or 'cities'. "+
"In the given area, restaurants try to run their business, which is dependent on the number of customer attendance, as well as "+
"the tax rate, which in general reduces the restaurant revenue. Restaurants that are not capable to survive the market competition have to shut their business, "+
"new restaurants are likely to show up when there is demand in a high populated area.\n" +
"\n"+
"The exact definition of the simulation assignment can be found at http://www.argesim.org/uploads/tx_compdb/sne4142p52.pdf\n"+
"\n"+
"Simulation types:\n"+
"1) Spatial Analysis:\n"+
"Spatial plot of the essential simulation behaviour. You can observe the spatial distribution of restaurants on a randomly calculated population in a given simulation period.\n"+
"2) Restaurants over time:\n"+
"This type provides a time domain analysis of the number of open restaurants in every week.\n"+ 
"Additionally, you have the possibility to average the result over more simulations runs by setting the 'Number of Runs' field.\n"+
"3) Tax income over tax rate:\n"+
"This method tries to maximise the total tax income of all restaurant revenues. It varies the tax rate, as well as the number of restaurants in the simulation environment,\n"+ 
"to achieve the highest possible income for the government. Again, you can average the result with more runs, but be aware that it increases the simulation runtime.\n"+
"4) Restaurant's revenue over k:\n"+
"Parameter k is a weighting coefficient, influencing the likelihood for an agent in a population to visit a near restaurant instead of a remotely situated one.\n"+
"This method finds the best k to achieve the highest global restaurant revenue.\n"+
"\n"+
"Parameters:\n"+
"Simulation speed (Spatial analysis only): Time step of the simulation in weeks/s\n"+
"Duration: Simulated time span in years\n"+
"Initial Number of restaurants: Number of restaurants at the beginning of the simulation\n"+
"Number of runs (Types 2-4 only): Runs the simulation more often to average the resulting value\n"+
"Tax rate: the percentage of revenue which has to be payed back by the restaurants\n"+
"Profit threshold: Revenue threshold which needs to be hold weekly by every restaurant\n"+
"\n"+
"Controls:\n"+
"(PLAY) - Starts the selected simulation type\n"+
"(STOP) - Stops the simulation\n"+
"(RESET)- Clears the plot window\n"+
"(LOAD DEFAULTS) - Sets all parameters back to defaults\n"+
"\n"+
"Zoom:\n"+
"Hold the mouse and pull downwards in the plot area to zoom in.\n"+
"Hold the mouse and push upwards in the plot area to zoom out.\n");
	
	// Plot
	private Plot plot;
	
	
	public void init() {
		super.init();
		
		fliesstext.setMaximumSize(new Dimension(5000, 60));
		createGUI();
		initValues();
		
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				typeActionPerformed();
			}
		});
	}
	
	
	public void initValues() {
		stop.setEnabled(false);
		reset.setEnabled(false);
		runs.setEnabled(false);
		type.setSelectedIndex(0);
		speed.setValue(1);
		duration.setValue(5);
		restaurants.setValue(30);
		runs.setValue(50);
		tax.setValue(20);
		profit.setValue(350);
	}
	
	
	public void createGUI() {
		
		headline.setMaximumSize(new Dimension(5000, 50));
		headline.setEditable(false);
		headline.setBackground(Color.white);
		headline.setBorder(new MatteBorder(10, 10, 10, 10, Color.white));
		headline.setFont(new Font("Arial", Font.BOLD, 18));
		
		
		fliesstext.setMaximumSize(new Dimension(5000, 70));
		fliesstext.setEditable(false);
		fliesstext.setBackground(Color.white);
		fliesstext.setBorder(new MatteBorder(0, 10, 10, 10, Color.white));
		fliesstext.setFont(new Font("Arial", Font.PLAIN, 12));
		fliesstext.setLineWrap(true);
		fliesstext.setWrapStyleWord(true);
		
		
		hide.setPreferredSize(new Dimension(100, 22));
		hide.setVerticalAlignment(JButton.CENTER);
		hide.addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent e) {
				hideButtonAction();
				
			}
		});
		
		
		balkenDescription.setBackground(customblue);
		balkenDescription.setMaximumSize(new Dimension(5000, 30));
		balkenDescription.setBorder(new MatteBorder(0, 5, 0, 5, Color.white));
		balkenDescription.setLayout(new FlowLayout(FlowLayout.RIGHT, 5, 4));
		balkenDescription.add(hide);
		
		/*
		darstellungsLayout.setTitles("", "", "");
		darstellung = darstellungsLayout;
		darstellung.setBackground(Color.gray);
		darstellung.setMaximumSize(new Dimension(5000, 340));
		darstellung.setBorder(new MatteBorder(0, 5, 0, 5, Color.white));
		*/
		//TODO:
		plot = new Plot();
		plot.setBackground(customgray);
		plot.setMaximumSize(new Dimension(5000, 340));
		plot.setBorder(new MatteBorder(0, 5, 0, 5, Color.white));
        plot.setSize(600,400);
        
		
		lblParameter.setBackground(customgray);
		lblParameter.setPreferredSize(new Dimension(210, 300));
		lblParameter.setMaximumSize(new Dimension(210, 300));
		lblParameter.setBorder(new MatteBorder(10, 5, 10, 5, customgray));
		lblParameter.setLayout(new GridLayout(7, 1, 0, 10));
		lblParameter.add(lblType);
		lblParameter.add(lblSpeed);
		lblParameter.add(lblDuration);
		lblParameter.add(lblRestaurants);
		lblParameter.add(lblRuns);
		lblParameter.add(lblTax);
		lblParameter.add(lblProfit);

		
		type.addItem("Spatial analysis");
		type.addItem("Restaurants over time");
		type.addItem("Tax income over tax rate");
		type.addItem("Restaurants’ revenue over k");
		type.addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent e) {
				typeActionPerformed();
				
			}
		});
		
		
		eingabefelder.setBackground(customgray);
		eingabefelder.setMaximumSize(new Dimension(240, 300));
		eingabefelder.setBorder(new MatteBorder(10, 5, 10, 5, customgray));
		eingabefelder.setLayout(new GridLayout(7, 1, 0, 10));
		eingabefelder.add(type);
		eingabefelder.add(speed);
		eingabefelder.add(duration);
		eingabefelder.add(restaurants);
		eingabefelder.add(runs);
		eingabefelder.add(tax);
		eingabefelder.add(profit);
		

		balkenEingabe.setBackground(customgray);
		balkenEingabe.setMaximumSize(new Dimension(5000, 300));
		balkenEingabe.setBorder(new MatteBorder(0, 5, 0, 5, Color.white));
		balkenEingabe.setLayout(new BoxLayout(balkenEingabe, BoxLayout.X_AXIS));
		balkenEingabe.add(lblParameter);
		balkenEingabe.add(eingabefelder);
		
		
		play.setPreferredSize(new Dimension(90, 22));
		play.setVerticalAlignment(JButton.CENTER);
		play.addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent e) {
				playButtonAction();
				
			}
		});
		
		
		stop.setPreferredSize(new Dimension(90, 22));
		stop.setVerticalAlignment(JButton.CENTER);
		stop.addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent e) {
				stopButtonAction();
				
			}
		});
			
		
		reset.setPreferredSize(new Dimension(90, 22));
		reset.setVerticalAlignment(JButton.CENTER);
		reset.addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent e) {
				resetButtonAction();
				
			}
		});
		
		
		defaults.setPreferredSize(new Dimension(90, 22));
		defaults.setVerticalAlignment(JButton.CENTER);
		defaults.addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent e) {
				defaultsButtonAction();
				
			}
		});
		
		
		balkenPlay.setBackground(customblue);
		balkenPlay.setMaximumSize(new Dimension(5000, 35));
		balkenPlay.setBorder(new MatteBorder(0, 5, 5, 5, Color.white));
		balkenPlay.setLayout(new FlowLayout(FlowLayout.RIGHT, 5, 4));
		balkenPlay.add(play);
		balkenPlay.add(Box.createHorizontalStrut(0));
		balkenPlay.add(stop);
		balkenPlay.add(Box.createHorizontalStrut(0));
		balkenPlay.add(reset);
		balkenPlay.add(Box.createHorizontalStrut(0));
		balkenPlay.add(defaults);
		
		
		lblErgebnis.setBackground(customblue);
		lblErgebnis.setMaximumSize(new Dimension(210, 50));
		lblErgebnis.setPreferredSize(new Dimension(210, 120));
		lblErgebnis.setBorder(new MatteBorder(5, 5, 5, 5, customblue));
		lblErgebnis.setLayout(new GridLayout(1, 1, 0, 10));
		lblErgebnis.add(lblResult);
		
		result.setEditable(false);
		
		ergebnisfelder.setBackground(customblue);
		ergebnisfelder.setMaximumSize(new Dimension(240, 32));
		ergebnisfelder.setBorder(new MatteBorder(5, 5, 5, 5, customblue));
		ergebnisfelder.setLayout(new GridLayout(1, 1, 0, 10));
		ergebnisfelder.add(result);
		
		balkenErgebnis.setBackground(customblue);
		balkenErgebnis.setMaximumSize(new Dimension(5000, 50));
		balkenErgebnis.setBorder(new MatteBorder(0, 5, 5, 5, Color.white));
		balkenErgebnis.setLayout(new BoxLayout(balkenErgebnis, BoxLayout.X_AXIS));
		balkenErgebnis.add(lblErgebnis);
		balkenErgebnis.add(ergebnisfelder);
		
		
		
		Container content = getContentPane();
		
		content.setBackground(Color.white);
		
		BoxLayout layout = new BoxLayout(content, BoxLayout.Y_AXIS);	
		content.setLayout(layout);

		content.add(headline);
		content.add(fliesstext);
		content.add(balkenDescription);
		content.add(plot);
		content.add(balkenEingabe);
		content.add(balkenPlay);
		content.add(balkenErgebnis);
		
		content.setSize(620, 900);
		setSize(620, 900);
	}
	
	public void playButtonAction() {
		play.setEnabled(false);
		stop.setEnabled(true);
		reset.setEnabled(false);
		defaults.setEnabled(false);
		spatialSimRunning = true;
		type.setEnabled(false);
		
		// switch simulationstyp
		// typ spatial - neu:
		
		workerThread = new Thread(new Runnable() {
			public void run() {
					
				int nrOfPersons = 3000;
				int nrOfRestaurants = (Integer) restaurants.getValue();
				int areaWidth = 600; //plot.getWidth();
				int areaHeight = 400; //plot.getHeight();
				int range = 100;
				int durationInt = (Integer) duration.getValue(); 
				int runsInt = (Integer) runs.getValue();
				int profitThreshold = (Integer) profit.getValue();
				double openingProb = 0.1;
				double closingProb = 0.2;
				double taxrate = ((Integer) tax.getValue()) / 100;
				double k=4;
				
				switch (type.getSelectedIndex()) {
				
				case 0:
					if (spatialSimNew) {
						spatialSimNew = false;
						
						ArrayList<Position> personenList;
						//System.out.println("Spatial laeuft");
						
						//TODO:
											
						personenList = sim.init(nrOfPersons, nrOfRestaurants, areaWidth, areaHeight, range, profitThreshold, openingProb, closingProb, selbstreferenz);				
						// Grafik updaten (Personen einfügen)
						
						plot.setMarksStyle("points");
						plot.clear(0);
						plot.clear(1);
				        
						for (Position pos : personenList)
						{
							plot.addPoint(1, pos.getX(), pos.getY(), false);
						}
						plot.repaint();
						
						int simSchritte = (Integer) duration.getValue() * 52;
						ArrayList<Position> restaurantsList = null;
						plot.setMarksStyle("bigpoints");
						
						for (int i=0; i<simSchritte; i++) {
						// Pause zwischen den Schritten - Usereinstellung aus GUI (1000/usersetting)
							try {
								do {
									Thread.sleep(1000/((Integer) speed.getValue()));
								} while (!spatialSimRunning);
							} catch (InterruptedException e) {
								// Thread interrupted -> Schleife unterbrechen -> Thread läuft aus.
								break;
							}
							
							// Reset-Button -> spatialSimNew = true -> Schleife unterbrechen -> Thread läuft aus
							if (spatialSimNew) {
								break;
							}

							// simulation
							restaurantsList = sim.spatialNextStep(taxrate, k);
							plot.clear(0);
							for (Position pos : restaurantsList)
							{ 
								plot.addPoint(0, pos.getX(), pos.getY(), false);
							}
							plot.repaint();
						}
						
						Integer restNumber = restaurantsList.size(); 
						result.setText(restNumber.toString());
						spatialSimRunning=false;
						spatialSimNew = true;
						play.setEnabled(true);
						stop.setEnabled(false);
						reset.setEnabled(true);
						defaults.setEnabled(true);
						type.setEnabled(true);
					}
					break;
				
				// typ restaurants over time
				case 1:
					ArrayList<Double> restaurantList; // ein wert pro woche
					plot.clear(true);
					sim.init(nrOfPersons, nrOfRestaurants, areaWidth, areaHeight, range, profitThreshold, openingProb, closingProb, selbstreferenz);
					restaurantList = sim.timeRestaurants(durationInt, runsInt, taxrate, k);

					// Letzen wert im array als ergebnis
					int lastOne=restaurantList.size();
					plot.setMarksStyle("none");
					for (int i=0; i<lastOne; i++)
					{
						plot.addPoint(0, i, restaurantList.get(i), true);
					}
					plot.repaint();
					result.setText(restaurantList.get(lastOne-1).toString());
					play.setEnabled(true);
					stop.setEnabled(false);
					type.setEnabled(true);
					break;
					
				
				
				// typ tax income over time
				case 2:
					ArrayList<Position> income;
					plot.clear(true);
					sim.init(nrOfPersons, nrOfRestaurants, areaWidth, areaHeight, range, profitThreshold, openingProb, closingProb, selbstreferenz);
					income = sim.timeTaxIncome(durationInt, runsInt, k);
					// Zeitserie plotten			
					lastOne=income.size();

					for (int i=0; i<lastOne; i++)
					{
						plot.addPoint(0, income.get(i).getX()/1000., income.get(i).getY(), true);
					}
					plot.repaint();
					// Letzten Wert von x ins ergebnis schreiben
					Collections.sort(income);
					result.setText(""+income.get(lastOne-1).getX()/10.);
					play.setEnabled(true);
					stop.setEnabled(false);
					type.setEnabled(true);
					break;
				
				
				// typ revenue over k
				case 3:
					ArrayList<Position> revenue;
					plot.clear(true);
					sim.init(nrOfPersons, nrOfRestaurants, areaWidth, areaHeight, range, profitThreshold, openingProb, closingProb, selbstreferenz);
					revenue = sim.timeOverK(durationInt, runsInt, taxrate);
					
					// Zeitserie plotten
					lastOne=revenue.size();
					for (int i=0; i<lastOne; i++)
					{
						plot.addPoint(0, revenue.get(i).getX()/10., revenue.get(i).getY(), true);
					}
					plot.repaint();
					
					 Collections.sort(revenue);
					// Erster Wert von x ins ergebnis schreiben
					result.setText(""+revenue.get(lastOne-1).getX()/10.);
					play.setEnabled(true);
					stop.setEnabled(false);
					type.setEnabled(true);
					break;
				}
			}
		});
		workerThread.start();		
	}
	
	public void stopButtonAction() {
		play.setEnabled(true);
		stop.setEnabled(false);
		reset.setEnabled(true);
		
		spatialSimRunning = false;

		if (type.getSelectedIndex() != 0) {
			sim.interrupt();			
		}
	}
	
	public void resetButtonAction() {
		reset.setEnabled(false);
		defaults.setEnabled(true);
		type.setEnabled(true);
		
		spatialSimNew = true;
		workerThread.interrupt();
		sim.interrupt();
		
		plot.clear(0);
		plot.clear(1);
		result.setText("");
	}
	
	public void defaultsButtonAction() {
		initValues();
	}
	
	public void hideButtonAction() {
		if (fliesstext.isVisible()) {
			fliesstext.setVisible(false);
			fliesstext.setMaximumSize(new Dimension(5000, 0));
			hide.setText("Show Description");
		} else {
			fliesstext.setVisible(true);
			fliesstext.setMaximumSize(new Dimension(5000, 70));
			hide.setText("Hide Description");
		}
		((JPanel)getContentPane()).updateUI();
	}
	
	
    private void typeActionPerformed() {
        runs.setEnabled(true);
        tax.setEnabled(true);
        speed.setEnabled(false);

        switch(type.getSelectedIndex()) {
            case 0: runs.setEnabled(false);
                    speed.setEnabled(true);
                    lblResult.setText("Number of restaurants");
                    
		            plot.clearLegends();
		            plot.clear(true);
                    
			        plot.setTitle("Spatial Analysis of Restaurant Dynamics");
			        plot.setMarksStyle("bigdots", 0);
			        plot.setMarksStyle("points", 1);        
			        plot.addLegend(0, "Restaurants");
			        plot.addLegend(1, "Population");
			        plot.repaint();
			        result.setText("");
                    break;

            case 1: lblResult.setText("Average number of Restaurants");
            
	            plot.clearLegends();
	            plot.clear(true);
		            
			        plot.setTitle("Average number of Restaurants");
			        plot.repaint();
			        result.setText("");
                    break;

            case 2: tax.setEnabled(false);
                    lblResult.setText("Tax Rate for max. tax income");
                    
		            plot.clearLegends();
		            plot.clear(true);
		            
			        plot.setTitle("Tax Rate for max. tax income");
			        plot.repaint();
			        result.setText("");
                    break;

            case 3: lblResult.setText("k for max. restaurants revenue");
            
		            plot.clearLegends();
		            plot.clear(true);
		            
			        plot.setTitle("k for max. restaurants revenue");
			        plot.repaint();
			        result.setText("");
            		break;
        }
    }
    
	public static void readFile(StringBuffer datei) throws FileNotFoundException, IOException
	{
        String zeile = null;
        datei = new StringBuffer();
        BufferedReader in = new BufferedReader(new FileReader("description.txt"));
        while((zeile = in.readLine()) != null)
        {
           datei.append(in.readLine());
        }
        in.close();
	}
    
}