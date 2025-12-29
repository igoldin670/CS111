public class EgyptianPyramid {
    public static void main(String[] args) {

        int gridSize = Integer.parseInt(args[0]);
        int numBricks = Integer.parseInt(args[1]);

        char[][] pyramid = new char[gridSize][gridSize];
        for (int i = 0; i < gridSize; i++) {
            for (int j = 0; j < gridSize; j++) {
                pyramid[i][j] = '=';
            }
        }

        int bricksRemaining = numBricks;
        int colStart = 0;
        for (int row = gridSize - 1; row >= 0; row--) {
            for (int col = colStart; col < gridSize - colStart; col++) {
                if (bricksRemaining > 0) {
                    pyramid[row][col] = 'X';
                    bricksRemaining--;
                } else {
                    break;
                }
            }
            colStart++;
        }

        for (int i = 0; i < gridSize; i++) {
            for (int j = 0; j < gridSize; j++) {
                System.out.print(pyramid[i][j]);
            }
            System.out.println();
        }

        System.out.println(bricksRemaining + " Bricks Remaining");
    }
}
