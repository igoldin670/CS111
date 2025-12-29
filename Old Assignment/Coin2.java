public class Coin2
{
    public static void main(String[] args)
    {
        int flip, count = 0;
        int num = Integer.parseInt(args[0]);
        for(int i = 0; i < num; i++)
        {
            flip = (int)(Math.random() * 2 + 1);
            if(flip == 1)
            {
                System.out.println("Heads");
                count++;
            }
            else
            {
                System.out.println("Tails");
            }
        }
        System.out.println("Heads: " + count);
    }    
}
