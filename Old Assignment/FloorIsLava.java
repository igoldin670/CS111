public class FloorIsLava {
    public static void main(String[] args) {
        int num = Integer.parseInt(args[0]);
        for (int i = 1; i <= num; i++) 
        {
            if (i % 2 == 0)
                System.out.print(i + " ");
        }
        for (int i = num; i >= 1; i--) 
        {
            if (i % 2 != 0)
                System.out.print(i + " ");
        }
    }
}