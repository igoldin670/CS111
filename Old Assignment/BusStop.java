public class BusStop {
    public static void main(String[] args) {
        char[] busList = new char[args.length];

        for (int i = 0; i < args.length; i++) 
        {
            busList[i] = args[i].charAt(0);
        }

        char finalBus = busList[busList.length - 1];
        for (int i = 0; i < args.length - 1; i++)
        {
            if (finalBus == busList[i])
            {
                System.out.println(i + 1);
                return;
            }
        }
        System.out.println(-1);
    }    
}
