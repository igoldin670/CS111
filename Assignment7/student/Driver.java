package student;
 /*
 * The Driver class is used to test the DigitMatcher methods.
 * 
 * Update the main method to use the pre-written methods or
 * write your own.
 * 
 * @author Ana Paula Centeno
 * @author Tom Swope
 */

 public class Driver {

	/*
	 * Populates a Digit array from file.
	 * The test digit files contain a label so that we can check if
	 * DigitMatcher is working correctly.
	 * 
	 * @param filename is the input file containing test digits.
	 * 
	 * @return a Digit array containing the digits from @filename.
	 */
	public static Digit[] populateArrayOfTestDigits(String filename) {

		StdIn.setFile(filename);
		int numberOfDigits = StdIn.readInt();

		Digit[] testDigits = new Digit[numberOfDigits];

		for (int d = 0; d < numberOfDigits; d++) {
			String[] values = StdIn.readString().split(",");
			int label = Integer.parseInt(values[0]);
			int[] pixels = new int[784];

			for (int i = 0; i < 784; i++) {
				pixels[i] = Integer.parseInt(values[i + 1]) == 0 ? 0 : 1;
			}
			testDigits[d] = new Digit(label, pixels);
		}
		return testDigits;
	}

	/*
	 * Populates a Digit array from file.
	 * The unlabeled digit files DO NOT contain a label.
	 * 
	 * @param filename is the input file containing unlabeled digits.
	 * 
	 * @return a Digit array containing the digits from @filename.
	 */
	public static Digit[] populateArrayOfUnlabeledDigits(String filename) {

		StdIn.setFile(filename);
		int numberOfDigits = StdIn.readInt();

		Digit[] unlabeledDigits = new Digit[numberOfDigits];

		for (int d = 0; d < numberOfDigits; d++) {
			String[] values = StdIn.readString().split(",");
			int[] pixels = new int[784];

			for (int i = 0; i < 784; i++) {
				pixels[i] = Integer.parseInt(values[i]) == 0 ? 0 : 1;
			}
			unlabeledDigits[d] = new Digit(-9, pixels);
		}
		return unlabeledDigits;
	}

	/*
	 * Tests mostSimilar() by computing the accuracy of the prediction.
	 * 
	 * @param trainingFile contains the training dataset
	 * 
	 * @param testingFile contains the testing dataset
	 */
	public static void testMostSimilar(String trainingFile, String testingFile) {

		// initializes DigitMatcher with training data
		DigitMatcher trainingDataset = new DigitMatcher(trainingFile);

		// populates array with test digits
		// test digits contain the label
		Digit[] digits = populateArrayOfTestDigits(testingFile);
		int matchCount = 0;

		for (int i = 0; i < digits.length; i++) {

			Digit d = digits[i]; // retrieves the digit at index i

			// compute d's similarity to every digit in the trainning set
			trainingDataset.computeSimilarity(d);

			// retrieves the label that is the closest match
			int matchingLabel = trainingDataset.mostSimilar().getLabel();

			// increment count if matched correctly
			if (d.getLabel() == matchingLabel) {
				matchCount++;
			}

			// to show it is computing print a . every 100
			if (i % 100 == 0) {
				StdOut.print(".");
			}
		}
		StdOut.println();
		StdOut.println("Most similar accuracy = " + (double) matchCount / digits.length);
	}

	/*
	 * Tests kNearestNeighbors() by computing the accuracy of the prediction.
	 * 
	 * @param trainingFile contains the training dataset
	 * 
	 * @param testingFile contains the testing dataset
	 */
	public static void testKNearestNeighbors(int k, String trainingFile, String testingFile) {

		// initializes DigitMatcher with training data
		DigitMatcher digitMatcher = new DigitMatcher(trainingFile);

		// populates array with test digits
		// test digits contain the label
		Digit[] digits = populateArrayOfTestDigits(testingFile);
		int matchCount = 0;

		for (int i = 0; i < digits.length; i++) {

			Digit d = digits[i]; // retrieves the digit at index i

			// compute d's similarity to every digit in the trainning set
			digitMatcher.computeSimilarity(d);
			digitMatcher.rankBySimilarity(); // ranks the training digits by similarity

			// retrieves the label that is the closest match using the K nearest neighbhors
			// algorithm
			int matchingLabel = digitMatcher.kNearestNeighbors(k);

			// increment count if matched correctly
			if (d.getLabel() == matchingLabel) {
				matchCount++;
			}

			// to show it is computing print a . every 100
			if (i % 100 == 0) {
				StdOut.print(".");
			}
		}
		StdOut.println();
		StdOut.println("k-nearest neighbor's accuracy with k=" + k + " is " + (double) matchCount / digits.length);
	}

	/*
	 * Tests weightedKNearestNeighbors() by computing the accuracy of the
	 * prediction.
	 * 
	 * @param trainingFile contains the training dataset
	 * 
	 * @param testingFile contains the testing dataset
	 */
	public static void testWeightedKNearestNeighbors(int k, String trainingFile, String testingFile) {

		// initializes DigitMatcher with training data
		DigitMatcher digitMatcher = new DigitMatcher(trainingFile);

		// populates array with test digits
		// test digits contain the label
		Digit[] digits = populateArrayOfTestDigits(testingFile);
		int matchCount = 0;

		for (int i = 0; i < digits.length; i++) {

			Digit d = digits[i]; // retrieves the digit at index i

			// compute d's similarity to every digit in the trainning set
			digitMatcher.computeSimilarity(d);
			digitMatcher.rankBySimilarity(); // ranks the training digits by similarity

			// retrieves the label that is the closest match using the weighted K nearest
			// neighbhors algorithm
			int matchingLabel = digitMatcher.weightedKNearestNeighbors(k);

			// increment count if matched correctly
			if (d.getLabel() == matchingLabel) {
				matchCount++;
			}

			// to show it is computing print a . every 100
			if (i % 100 == 0) {
				StdOut.print(".");
			}
		}
		StdOut.println();
		StdOut.println(
				"weighted k-nearest neighbor's accuracy with k=" + k + " is " + (double) matchCount / digits.length);
	}

     
	public static void main(String[] args) {
		StdOut.println("DigitMatcher uses mini_smallerTrainingData_700.csv as training data.");
		DigitMatcher dm = new DigitMatcher("mini_smallerTrainingData_700.csv");
		Digit[] trainingData = dm.getDigits();
		StdOut.println("Number of Digits: " + trainingData.length);
		StdOut.println("\n--- First Training Digit ---");
		StdOut.println(trainingData[0]);
	
		// Load testing data
		Digit[] testData = populateArrayOfTestDigits("mini_testingData_126.csv");
	
		// Compute similarity between test digit and all training digits
		dm.computeSimilarity(testData[0]);
		StdOut.println("\n--- Test Digit at Index 0 ---");
		StdOut.println(testData[0]);
	
		// Print two example training digits
		StdOut.println("\n--- Training Digit at Index 0 ---");
		StdOut.print(trainingData[0]);
		StdOut.println("\n--- Training Digit at Index 4 ---");
		StdOut.print(trainingData[4]);
	
		// Most similar digit
		Digit mostSimilar = dm.mostSimilar();
		StdOut.println("\n--- Most Similar ---");
		StdOut.println("The most similar digit has " + mostSimilar.getLabel() + " label.");
		StdOut.print(mostSimilar);
	
		// k-Nearest Neighbors (k=5)
		dm.rankBySimilarity();
		int predictedLabelK5 = dm.kNearestNeighbors(5);
		StdOut.println("\n--- k-Nearest Neighbors (k=5) ---");
		StdOut.println("The label that occurs the most amongst the 5 highest similarity digits is " + predictedLabelK5);
	
		// Weighted k-Nearest Neighbors (k=5)
		int predictedWeightedLabelK5 = dm.weightedKNearestNeighbors(5);
		StdOut.println("\n--- Weighted k-Nearest Neighbors (k=5) ---");
		StdOut.println("The label that occurs the most amongst the 5 highest similarity digits is " + predictedWeightedLabelK5);
	
		// Loop: test accuracy using weighted kNN for k = 1 to 9
		StdOut.println("\n--- Weighted kNN Accuracy from k=1 to 9 ---");
		for (int k = 1; k < 10; k++) {
			testWeightedKNearestNeighbors(k, "mini_smallerTrainingData_700.csv", "mini_testingData_126.csv");
		}	

		for (int k = 1; k <= 7; k++) {
			testKNearestNeighbors(k, "mini_smallerTrainingData_700.csv", "mini_testingData_126.csv");
			testWeightedKNearestNeighbors(k, "mini_smallerTrainingData_700.csv", "mini_testingData_126.csv");
		}
		
	}
 }	