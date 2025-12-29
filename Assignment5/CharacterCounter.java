public class CharacterCounter {
    public static void main(String[] args) {
        StdIn.setFile(args[0]);
        StdOut.setFile(args[1]);
        
        int[] characterCounts = new int[95]; // 126-32+1 = 95 characters
        int totalChars = 0;
        
        while (StdIn.hasNextChar()) {
            char c = StdIn.readChar();
            totalChars++;
            if (c >= 32 && c <= 126) {
                characterCounts[c - 32]++;
            } 
        }
        
        for (int i = 0; i < 95; i++) {
            double frequency = (characterCounts[i] / (double) totalChars) * 100.0;
            char actualChar = (char)(i + 32);
            StdOut.println(actualChar + "," + (i + 32) + "," + characterCounts[i] + "," + frequency);
            System.out.println(actualChar + "," + (i + 32) + "," + characterCounts[i] + "," + frequency);
        }
    }
}