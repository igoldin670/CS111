public class Test {
    public static void main(String[] args) {
        String test = properFactors(21);
        System.out.println(test);
    }
    public static int pow(int x, int n) {
        int product = 1;
        for (int i = 1; i <= n; i++) {
            product *= x;
        }
        return product;
    }
    public static int numDigs(int x) {
        int count = 0;
        while (x > 0) {
            x /= 10;
            count++;
        }
        return count;
    }
    public static int numproperFactors(int x) {
        int count = 0;
        for (int i = 2; i < x; i++) {
            if (x % i == 0) {
                count++;
            }
        }
        return count;
    }
    public static String properFactors(int x) {
        String factors = "";
        for (int i = 2; i < x; i++) {
            if (x % i == 0) {
                factors += i + " ";
            }
        }
        return factors;
    }
    public static boolean isPrime(int x) {
        if (x==1) return false;
        if (x==2) return true;
        for (int i = 3; i < x; i++) {
            if (x % i == 0) {
                return false;
            }
        }
        return true;
    }
}
