package student;
/*
 * The DigitMatcher class is used to write the logic that will identify unlabeled Digits.
 * 
 * @author Ana Paula Centeno
 * @author Tom Swope
 */
	public class DigitMatcher {

		/* Array holding the dataset of training digits */
		private Digit[] digits;

		/*
		* Constructor 
		* Initializes the digits array with digits from a file
		* @param file containing the digits
		* 
		* File format:
		* Each line in the file represents one handwritten digit.
		* Each of these handwritten digits has a label, which notates the 
		* number that the digit represents, and the 784 pixels from a 28x28 image 
		* in a single row of comma separated values. 
		* 
		* The data for the digit is represented by the comma separated values.
		*    - The first value specifies that the digit's lable. 
		*    - The next 784 values represent pixels. A 0 means that the pixel is white; a 1 means that the pixel is black.
		* 
		*/
		public DigitMatcher(String trainingInputFile)  {
			StdIn.setFile(trainingInputFile);
			int numDigits = StdIn.readInt();
			digits = new Digit[numDigits];  

			for (int i = 0; i < numDigits; i++) {

				String[] values = StdIn.readString().split(",");

				int label = Integer.parseInt(values[0]);

				int[] pixels = new int[784];
				for (int j = 0; j < 784; j++) {
					int pixelValue = Integer.parseInt(values[j + 1]);
					pixels[j] = (pixelValue > 0) ? 1 : 0;
				}

				digits[i] = new Digit(label, pixels);
			}
		}
	/*
	 * Computes how similar the parameter digit is to every digit in the digits array.
	 * 
	 * The similarity is calculated by taking the difference (percentage of pixels) 
	 * between the two digits. USE the computeSimilarity method from the Digit class.
	 *
	 * @param digit is the test digit that needs to be identified. 
	 */
	public void computeSimilarity ( Digit digit ) {
		for (Digit trainingDigit : digits) {
			trainingDigit.computeSimilarity(digit);
		}
	}

	/*
	 * Assumes computeSimilarity() has been called to calculate the similaty between
	 * a test/unknown digit and every digit in the digits array.
	 * 
	 * Returns the Digit from the dataset that is most similar to the parameter 
	 * digit of computeSimilarity().
	 * 
	 * The most similar digit has the largest similarity value.
	 * 
	 */
	public Digit mostSimilar() {
		if (digits.length == 0) return null;

		Digit mostSimilarDigit = digits[0];
		double highestSimilarity = digits[0].getSimilarity();
	
		for (int i = 1; i < digits.length; i++) {
			if (digits[i].getSimilarity() > highestSimilarity) {
				highestSimilarity = digits[i].getSimilarity();
				mostSimilarDigit = digits[i];
			}
		}
	
		return mostSimilarDigit;
	}

	/*
	 * Find the Digit from the digits array with the greatest similarity
	 * between the indices [l, h].
	 * 
	 * @param low lowest array index to consider during the search
	 * @param high highest array index to consider during the search
	 * @return the index (from digits array) of the Digit with greatest similarity.
	 */
	public int findGreatestSimilarity (int low, int high) {
		if (low < 0 || high >= digits.length || low > high) {
			throw new IllegalArgumentException("Invalid range");
		}
	
		int bestIndex = low;
		double bestScore = digits[low].getSimilarity();
	
		for (int i = low + 1; i <= high; i++) {
			if (digits[i].getSimilarity() > bestScore) {
				bestScore = digits[i].getSimilarity();
				bestIndex = i;
			}
		}
	
		return bestIndex;
	}

	/*
	 * Assumes computeSimilarity() has been called to calculate the similarity between
	 * a digit and every digit in the dataset of digits.
	 * 
	 * Rank the Digits in the digits dataset by similarity.
	 * 
	 * Reorders the digits dataset to contain the most similar digit at index 0, 
	 * the next most similar at index 1 and so on.
	 * 
	 * for each index i in the digits dataset
	 * 	   find the most similar digit between index i and the end of the array
	 *     swap digit at index i with the digit at the index where the most similar digit lies
	 */
	public void rankBySimilarity() {
		for (int i = 0; i < digits.length - 1; i++) {
			int maxIndex = findGreatestSimilarity(i, digits.length - 1);

			Digit temp = digits[i];
			digits[i] = digits[maxIndex];
			digits[maxIndex] = temp;
		}
	}

	/*
	 * Depends on rankBySimilarity()
	 * 
	 * Steps: 
	 * 1. Find the k nearest neighbors based on similarity (k first items in digits array).
	 * 2. Of the k neighbors, finds the label that appears most often.
	 * 
	 * @param k 
	 * @return the label that occurs the most within the first k objects in digits.
	 */
	public int kNearestNeighbors (int k) {
		int[] labelCounts = new int[10];

		for (int i = 0; i < k; i++) {
			int label = digits[i].getLabel();
			labelCounts[label]++;
		}
	
		int mostFrequentLabel = 0;
		int maxCount = labelCounts[0];
	
		for (int i = 1; i < labelCounts.length; i++) {
			if (labelCounts[i] > maxCount) {
				maxCount = labelCounts[i];
				mostFrequentLabel = i;
			}
		}
	
		return mostFrequentLabel;
	}

	/*
	 * Depends on rankBySimilarity()
	 * 
	 * 1. Find the k nearest neighbors based on similarity.
	 * 2. For each of the k neighbors, compute the votes (based on similarity).
	 * 3. Find the label with the highest confidence level.
	 * 
	 * @param k 
	 * @return the label that occurs the most within the first k objects in digits.
	 */
	public int weightedKNearestNeighbors (int k) {
		double[] weightedVotes = new double[10];

		for (int i = 0; i < k; i++) {
			int label = digits[i].getLabel();
			double similarity = digits[i].getSimilarity();
			weightedVotes[label] += similarity;
		}

		int bestLabel = 0;
		double maxWeight = weightedVotes[0];

		for (int i = 1; i < weightedVotes.length; i++) {
			if (weightedVotes[i] > maxWeight) {
				maxWeight = weightedVotes[i];
				bestLabel = i;
			}
		}

		return bestLabel;
	}


	/*
	 * Returns the dataset of digits. (DO NOT REMOVE)
	 */
	public Digit[] getDigits() {
		return digits;
	}

	/*
	 * Prints the dataset of digits. (DO NOT REMOVE)
	 */
	public void printDigits() {
		for ( Digit digit : digits ) {
			System.out.println("Label: " + digit.getLabel());
			digit.printPixels();
			System.out.println();
		}
	}
}
