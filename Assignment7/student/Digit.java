package student;

import java.awt.image.BufferedImage;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;

/*
 * A Digit object will represent a single handwritten Digit from either the training or testing data sets.
 * 
 * Each digit will have two stored instance fields: label and pixels. 
 * The label will be a number between 0 and 9 and signifies what number the digit represents.
 * 
 * label is known in the training dataset.
 * label is also known in the testing dataset so that you can access if DigitMatcher is working correctly.
 * label is unknown (to be determined) in the unknown dataset.
 * 
 * pixels is a 28*28 array of 0's and 1's. Each element represents a single pixel from the digit's image.
 * 
 * 
 * @author Ana Paula Centeno
 * @author Tom Swope
 */
public class Digit {

	private int label;         // stores the number that the Digit represents
    private int[][] pixels;    // each integer represents one pixel from the digit’s picture
    private double similarity; // similarity to another digit

    /*
     * Constructor initializes Digit from inputs
     */
    public Digit(int label, int[] pixels) {
        if (pixels.length != 784) {
            throw new IllegalArgumentException("Pixel array is expected to contain exactly 784 elements.");
        }
        this.label = label;
        this.pixels = new int[28][28];
        constructPixels(pixels);
    }

    public Digit(int label, int[][] pixels) {
        this.label = label;
        this.pixels = pixels;
        this.similarity = 0.0;
    }
    

    /*
     * Initializes the pixels array with input
     */
    private void constructPixels(int[] pixels) {
        for (int i = 0; i < 28; i++) {
            for (int j = 0; j < 28; j++) {
                this.pixels[i][j] = pixels[i * 28 + j];
            }
        }
    }

    /*
     * Returns the digit's pixels array
     * 
     * @return the pixels 2D integer array
     */
    public int[][] getPixels() {
        return pixels;
    }

    /*
     * Returns the digit's label
     * 
     * @return the Digit's label
     */
    public int getLabel() {
    	return label;
    }
    
    /*
     * Returns the digits similarity compared with the unknown/unlabeld digit.
     * @return the Digit's similarity
     */
    public double getSimilarity() {
        return similarity;
    }

    /*
     * Calculates how similar this Digit is to parameter Digit.
     * 
     * The similarity score for two digits is computed by calculating the 
     * ratio of the number of matching pixels to the total number of pixels compared.
     * 
     * The higher the value the greater the similarity.
     * 
     * @param other the digit this object is compared against.
     */
    public void computeSimilarity (Digit other) {
        
        int similarityCount = 0;

        for ( int r = 0; r < pixels.length; r++ ) {
            for ( int c = 0; c < pixels[r].length; c++ ) {

                if (pixels[r][c] == other.getPixels()[r][c]) {
                	similarityCount += 1;
                }
            }
        }
        similarity = (double)(similarityCount) / (pixels.length * pixels[0].length);
    }

    /*
     * Prints all the Digit's pixels 
     */
    public void printPixels() {
        for (int i = 0; i < 28; i++) {
            for (int j = 0; j < 28; j++) {
                System.out.printf("%3d ", pixels[i][j]);
            }
            System.out.println();
        }
    }

    /*
     * Displays the digit
     */
    public void display () {
        BufferedImage image = new BufferedImage(28, 28, BufferedImage.TYPE_INT_RGB);
        int colorCode = 255*65536+255*256+255; // #FFFFFF = (255,255,255).
        for ( int i = 0; i < 28; i++ ) {
            for ( int j = 0; j < 28; j++ ) {
                int pixelValue = pixels[i][j];                
                image.setRGB(j, i, pixelValue == 0 ? 0 : colorCode);
            }
        }
        Picture p = new Picture(image);
		p.show();
    }

    /*
     * @return digit's String representation
     */
    public String toString () {
        String display = "label = " + label + " similarity score = " + getSimilarity() + "\n|";;
        for(int i = 0; i< 28; i++)
        	display += '-';
        display += "|\n|";
        for(int[] row: pixels) {
        	for(int p: row) 
        		if(p == 0)
        			display += ' ';
        		else
        			display += '*';
        	display += "|\n|";
        }
        for(int i = 0; i< 28; i++)
        	display += '_';
        display += "|\n";
        return display;
    }
}
