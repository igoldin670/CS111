public class CharacterCounter {
    public static void main(String[] args) {
        char[] characterCounts = new char[128];
        StdIn.setFile(args[0]);
        // Inputs file to char list
        for (int i = 0; i < 128; i++) {
            if (StdIn.hasNextChar()) {
                characterCounts[i] = StdIn.readChar();
            }
        }

        // Count the number of characters in the ascii range 32 to 126
        int[] counts = new int[126-32];
        for (int i = 0; i < characterCounts.length; i++) {
            if (((int) characterCounts[i]) >= 32 && ((int) characterCounts[i] <= 126)) {
                counts[characterCounts[i] - 32]++;
            }
        }

        //Output the results
        for (int i = 0; i < counts.length; i++) {
            System.out.println((char)(i + 32) + "," + (i + 32) + "," + counts[i] + "," + (counts[i] / 128.0) * 100 );
        }
    }
}
