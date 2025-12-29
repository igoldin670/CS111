/**
 * Creates a ladder using recursion.
 * The ladder is built starting at the bottom and backs out
 * at the top.
 * 
 * ODD levels are denoted by |=|\n (rails/step followed by new line)
 * EVEN levels are denoted by | |\n (rails followed by new line)
 * The TOPMOST level (n = 1) is the end of the ladder: |-|\n
 * 
 * ASSUME n >= 1.
 */
public class Ladder {
    
    public static String createLadder(int n) {
        if (n <= 0) return "";
        if (n == 1) return "|-|" + "\n";
        String ladder = createLadder(n-1);
        if (n % 2 == 0) return ladder + "| |" + "\n";
        else return ladder + "|=|" + "\n";
    }
}
