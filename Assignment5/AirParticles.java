/*
 * Fine air particles can come from power plants, motor vehicles, airplanes, 
 * residential wood burning, forest fires, agricultural burning, volcanic eruptions,
 * and dust storms.
 *
 * Particles smaller than 2.5 micrometers are able to bypass the nose and throat
 * and penetrate deep into the lungs and some may even enter the circulatory system.
 * 
 * Due to the many adverse effects fine particles can inflict on a large number of people, 
 * PM2.5 is one of the major pollutants closely monitored by health authorities around the world. 
 * You will most likely come across a dedicated column for PM2.5 alongside the Air Quality Index (AQI), 
 * Pollutants Standards Index (PSI) or the air quality standards adopted by your country.
 *
 * Refer to the assignment description on the website.
 */

 public class AirParticles {
    
    public static double findHighestLevel (double[] communities) {
        double max = Double.NEGATIVE_INFINITY;
        for (int i = 0; i < communities.length; i++) {
            if (communities[i] > max) { 
                max = communities[i];
            }
        }
        return max;
    }

    /*
     * Use the main function to test your function.
     * It populates the array with values from a file then calls findHighestLevel,
     * and displays the value returned by the function.
     */
    public static void main(String args[]) {
        int n = StdIn.readInt();
        double[] levels = new double[n];
        
        int i = 0;
        while (!StdIn.isEmpty()) {
            levels[i] = StdIn.readDouble();
            i++;
        }
        
        double highest = findHighestLevel(levels);
        System.out.println("Highest PM2.5 Level is " + highest);
    }
}
