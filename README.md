# restaurant_dynamics
Restaurant Business Dynamics Simulation (ArgeSim comparison C16)

Welcome to the ARGESIM Comparison C16 - 'Restaurant Business Dynamics' -  Applet! 
We simulate a spatial environment with randomly distributed population, concentrated around 5 clusters or "cities".
In this surrounding area, restaurants try to run their business, which is dependent on the number of customer attendance, as well as
the given tax rate, which in general reduces the restaurant revenue. Restaurants that are not capable to survive the market competition have to shut their business,
new restaurants are likely to show up when there is demand in a high populated area.

The exact definition of the simulation assignment can be found at http://www.argesim.org/uploads/tx_compdb/sne4142p52.pdf

You are able to run 4 simulation types:
* 1) Spatial Analysis:
Spatial plot of the essential simulation behaviour. You can observe the spatial distribution of restaurants on a randomly calculated population in a given simulation period.
Result: Number of restaurants
* 2) Restaurants over time:
This type provides a time domain analysis of the number of open restaurants in every week. 
Additionally, you have the possibility to average the result over more simulations runs by setting the 'Number of Runs' field.
Result: Average number of restaurants
* 3) Tax income over tax rate:
This method tries to maximise the total tax income of all restaurant revenues. It varies the tax rate, as well as the number of restaurants in the simulation environment, 
to achieve the highest possible income for the government. Again, you can average the result with more runs, but be aware that it increases the simulation runtime.
Result: Tax rate for maximum tax income
* 4) Restaurant's revenue over k:
Parameter k is a weighting coefficient, influencing the likelihood for an agent in a population to visit a near restaurant instead of a remotely situated one.
This method finds the best k to achieve the highest global restaurant revenue. 
Result: K for maximum restaurants revenue

Parameters:
* Simulation speed (Spatial analysis only): Time step of the simulation in weeks/s
* Duration: Simulated time span in years
* Initial Number of restaurants: Number of restaurants at the beginning of the simulation
* Number of runs (Types 2-4 only): Runs the simulation more often to average the resulting value
* Tax rate: the percentage of revenue which has to be payed back by the restaurants
* Profit threshold: Revenue threshold which needs to be hold weekly by every restaurant

Controls:
* (PLAY) - Starts the selected simulation type
* (STOP) - Stops the simulation
* (RESET)- Clears the plot window

Zoom:
Hold the mouse and pull downwards in the plot area to zoom in.
Hold the mouse and push upwards in the plot area to zoom out.
