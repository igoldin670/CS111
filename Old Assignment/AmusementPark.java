public class AmusementPark {
    public static void main(String[] args) {   
        int familySize = Integer.parseInt(args[0]);
        int numUnderFive = Integer.parseInt(args[1]);
        int numSeniors = Integer.parseInt(args[2]);
        boolean isWeekend = Boolean.parseBoolean(args[3]);
        
        if (numUnderFive + numSeniors > familySize) {
            System.out.println("error");
            return;
        }
        
        int numRegular = familySize - numUnderFive - numSeniors;
        double admissionFee = (numRegular * 35) + (numSeniors * 20);
        
        if (familySize >= 5) {
            admissionFee *= 0.90;
        }
        
        if (isWeekend) {
            admissionFee += 25;
        }
        
        System.out.println(admissionFee);
    }
}
