public class Ladder {
    public static void main(String[] args) {
        System.out.println(createLadder(6));
    }

    public static String createLadder(int n) {
        if (n <= 0) return "";
        if (n == 1) return "|-|" + "\n";
        String ladder = createLadder(n-1);
        if (n % 2 == 0) return ladder + "| |" + "\n";
        else return ladder + "|=|" + "\n";
    }
}