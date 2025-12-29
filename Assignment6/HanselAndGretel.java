public class HanselAndGretel {

    /**
     * Reads the maze file and returns a 2D boolean array.
     *
     * The file is expected to have an integer representing the dimension (dim)
     * of the maze, followed by dim*dim booleans which represent maze cells.
     * A true value indicates an open cell, while false indicates a wall.
     *
     * @param file the file path of the maze input file
     * @return a 2D boolean array representing the maze layout
     */
    public static boolean[][] readMaze(String file) {
        // DO NOT MODIFY THIS METHOD
        StdIn.setFile(file);
        int dim = StdIn.readInt();
        boolean[][] maze = new boolean[dim][dim];
        for (int row = 0; row < dim; row++) {
            for (int col = 0; col < dim; col++) {
                maze[row][col] = StdIn.readBoolean();
            }
        }
        return maze;
    }

    /**
     * Recursively traverses the maze from the current position (row, col)
     * attempting to find a path to the exit (bottom-right corner).
     *
     * If the exit is reached, the current cell is marked as visited and
     * the method returns the exit coordinates {row, col}.
     * Otherwise, the method attempts to move in the following order: right, left, down, up.
     * For each valid move, it marks the cell as visited in the breadcrumbs.
     * If the recursive call does not find an exit, it backtracks by unmarking the cell.
     *
     * @param maze        a 2D boolean maze where true indicates an open cell and false indicates a wall
     * @param breadcrumbs a 2D boolean array tracking visited positions
     * @param row         the current row in the maze
     * @param col         the current column in the maze
     * @return an integer array containing the coordinates {row, col} of the exit if found,
     *         or {0, 0} if no exit exists
     */
    public static int[] takeStep(boolean[][] maze, boolean[][] breadcrumbs, int row, int col) {
        // Base case: if the finish cell is reached, mark it and return its coordinates.
        if (row == maze.length - 1 && col == maze[0].length - 1) {
            breadcrumbs[row][col] = true;
            return new int[]{row, col};
        }
        
        // Attempt to move right (col + 1).
        if (isValidCell(maze, breadcrumbs, row, col + 1)) {
            breadcrumbs[row][col + 1] = true;
            int[] result = takeStep(maze, breadcrumbs, row, col + 1);
            if (result[0] == maze.length - 1 && result[1] == maze[0].length - 1) {
                return result;
            }
            // Backtrack: unmark this cell.
            breadcrumbs[row][col + 1] = false;
            maze[row][col + 1] = false;
        }
        // Attempt to move left (col - 1).
        if (isValidCell(maze, breadcrumbs, row, col - 1)) {
            breadcrumbs[row][col - 1] = true;
            int[] result = takeStep(maze, breadcrumbs, row, col - 1);
            if (result[0] == maze.length - 1 && result[1] == maze[0].length - 1) {
                return result;
            }
            // Backtrack.
            breadcrumbs[row][col - 1] = false;
            maze[row][col - 1] = false;
        }
        // Attempt to move down (row + 1).
        if (isValidCell(maze, breadcrumbs, row + 1, col)) {
            breadcrumbs[row + 1][col] = true;
            int[] result = takeStep(maze, breadcrumbs, row + 1, col);
            if (result[0] == maze.length - 1 && result[1] == maze[0].length - 1) {
                return result;
            }
            // Backtrack.
            breadcrumbs[row + 1][col] = false;
            maze[row + 1][col] = false;
        }
        // Attempt to move up (row - 1).
        if (isValidCell(maze, breadcrumbs, row - 1, col)) {
            breadcrumbs[row - 1][col] = true;
            int[] result = takeStep(maze, breadcrumbs, row - 1, col);
            if (result[0] == maze.length - 1 && result[1] == maze[0].length - 1) {
                return result;
            }
            // Backtrack.
            breadcrumbs[row - 1][col] = false;
            maze[row - 1][col] = false;
        }
        
        // If no valid move is possible, backtrack from the current cell.
        return backtrack(maze, breadcrumbs, row, col);
    }

    /**
     * Backtracks from a dead-end to a previously visited cell that might have unexplored neighbors.
     * In the backtracking process, the current cell is marked as unvisited and blocked.
     * The method checks adjacent cells (right, left, down, up) for visited cells,
     * and attempts to resume the search from the first valid backtracking move.
     * If backtracking reaches the start and no moves remain, it returns {0, 0}.
     *
     * @param maze        a 2D boolean maze where true indicates an open cell and false indicates a wall
     * @param breadcrumbs a 2D boolean array tracking visited positions
     * @param row         the current row in the maze
     * @param col         the current column in the maze
     * @return an integer array containing the current position {row, col} after backtracking,
     *         or {0, 0} if no exit can be found
     */
    public static int[] backtrack(boolean[][] maze, boolean[][] breadcrumbs, int row, int col) {
        // If at the start, no further backtracking is possible.
        if (row == 0 && col == 0) {
            return new int[]{0, 0};
        }
        // Unmark the current cell to block this dead-end.
        breadcrumbs[row][col] = false;
        maze[row][col] = false;
        
        // Try backtracking in the order: right, left, down, up.
        if (isValidForBacktracking(breadcrumbs, row, col + 1)) {
            return takeStep(maze, breadcrumbs, row, col + 1);
        }
        if (isValidForBacktracking(breadcrumbs, row, col - 1)) {
            return takeStep(maze, breadcrumbs, row, col - 1);
        }
        if (isValidForBacktracking(breadcrumbs, row + 1, col)) {
            return takeStep(maze, breadcrumbs, row + 1, col);
        }
        if (isValidForBacktracking(breadcrumbs, row - 1, col)) {
            return takeStep(maze, breadcrumbs, row - 1, col);
        }
        // If no backtracking move is possible, return {0, 0}.
        return new int[]{0, 0};
    }

    /**
     * Checks if moving to cell (row, col) is valid.
     * A cell is valid if it is within bounds, open in the maze,
     * and has not already been visited.
     *
     * @param maze        the maze array
     * @param breadcrumbs the visited cells array
     * @param row         target row
     * @param col         target column
     * @return true if the cell is valid for moving into, false otherwise
     */
    public static boolean isValidCell(boolean[][] maze, boolean[][] breadcrumbs, int row, int col) {
        if (row >= 0 && row < maze.length && col >= 0 && col < maze[0].length) {
            return maze[row][col] && !breadcrumbs[row][col];
        }
        return false;
    }
//extra function 

    public static boolean isValidForBacktracking(boolean[][] breadcrumbs, int row, int col) {
        if (row >= 0 && row < breadcrumbs.length && col >= 0 && col < breadcrumbs[0].length) {
            return breadcrumbs[row][col];
        }
        return false;
    }

    /**
     * Writes the visited maze to the specified output file.
     *
     * The output file will begin with the maze's dimension (the number of rows)
     * followed by the 2D representation of the visited cells. Each cell is separated by
     * a space, and each row is written on a new line.
     *
     * @param visited a 2D boolean array where each element indicates whether
     *                the corresponding cell was visited (true) or not (false)
     * @param file    the file path where the visited maze should be written
     */
    public static void writeVisitedMaze(boolean[][] visited, String file) {
        StdOut.setFile(file);

        for (int i = 0; i < visited.length; i++) {
            for (int j = 0; j < visited[i].length; j++) {
                StdOut.print(visited[i][j] + " ");
            }
            StdOut.println();
        }
        StdOut.reset();
    }

    /**
     * The main entry point for the Hansel and Gretel maze traversal program.
     * 
     * This method:
     * 1. Reads a maze from the specified input file.
     * 2. Attempts to find a path from the top-left cell (0,0) to the bottom-right cell.
     * 3. Tracks which cells were visited during the traversal.
     * 4. Writes the visited cells to the specified output file.
     *
     * The program expects exactly two command-line arguments:
     * - The path to the input file containing the maze definition.
     * - The path to the output file where the visited cells will be written.
     *
     * @param args command line arguments containing input and output file paths
     */
    public static void main(String[] args) {
        // DO NOT MODIFY THIS CONDITIONAL STATEMENT
        if (args.length != 2) {
            System.out.println("Usage: java HanselAndGretelReference <inputfile> [outputfile]");
            return;
        }
        String inputFile = args[0];
        String outputFile = args[1];
    
        boolean[][] maze = readMaze(inputFile);
    
        if (maze.length == 1 && maze[0].length == 1) {
            boolean[][] breadcrumbs = new boolean[1][1];

            breadcrumbs[0][0] = maze[0][0];
            writeVisitedMaze(breadcrumbs, outputFile);
            return;
        }
    
        // For mazes larger than 1x1:
        boolean[][] breadcrumbs = new boolean[maze.length][maze[0].length];
        if (maze[0][0]) {
            breadcrumbs[0][0] = true;
            takeStep(maze, breadcrumbs, 0, 0);
        }
        writeVisitedMaze(breadcrumbs, outputFile);
    }
    
}