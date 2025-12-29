public class Elevator {
    public static void main(String[] args) {
        int numFloors = Integer.parseInt(args[0]);
        int floorRequest = Integer.parseInt(args[1]);
        int numRestricted = Integer.parseInt(args[2]);
        int optPass = (args.length > 3) ? Integer.parseInt(args[3]) : -1; 

        int e1 = 1;
        int e2 = 1;

        int[] requestFloor = new int[String.valueOf(Math.abs(floorRequest)).length()];
        int[] restrictedFloors = new int[numRestricted];

        for (int i = 0; i < requestFloor.length; i++) 
        {
            requestFloor[i] = floorRequest % 10;
            floorRequest /= 10;
        }

        int index = 0;
        for (int i = numFloors; i >= numFloors - numRestricted + 1; i--)
        {
            restrictedFloors[index++] = i;
        }

        for (int i = 0; i < requestFloor.length; i++) 
        {
            Boolean access = null;
            for (int j = 0; j < restrictedFloors.length; j++)
            {
                if (requestFloor[i] == restrictedFloors[j])
                {
                    access = (optPass % numFloors == requestFloor[i] || (optPass % numFloors == 0 && requestFloor[i] == numFloors));
                    break;
                }
            }
            
            if (Math.abs(requestFloor[i] - e1) == Math.abs(requestFloor[i] - e2)) {
                e1 = requestFloor[i];
                System.out.println("1 " + e1);
            } else if (Math.abs(requestFloor[i] - e1) < Math.abs(requestFloor[i] - e2)) {
                e1 = requestFloor[i];
                System.out.println("1 " + e1);
            } else {
                e2 = requestFloor[i];
                System.out.println("2 " + e2);
            }

            if (optPass != -1) 
            {
                if (access == null) {
                    continue;
                } else if (!access) {
                    System.out.println("Denied");
                } else if (access) {
                    System.out.println("Granted");
                }
            }
            
        }
    }
}