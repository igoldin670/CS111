import java.awt.Point;

import java.util.ArrayList;
public class Track {
    private ArrayList<Point> track;
    private int length;
    private int maxX;
    private int maxY;
    private Point startFinish;

    public Track(String filename) {
        StdIn.setFile(filename);
        String[] lines = StdIn.readAllLines();
        if (lines.length == 0) {
            throw new IllegalArgumentException("Track file is empty.");
        }

        for (String line : lines) {
            maxX = Math.max(maxX, line.length() - 1);
        }
        maxY = lines.length - 1;

        Point firstPoint = null;
        char[][] track2D = new char[maxY + 1][maxX + 1];
        for (int y = 0; y <= maxY; y++) {
            String line = lines[y];
            for (int x = 0; x <= maxX; x++) {
                track2D[y][x] = x < line.length() ? line.charAt(x) : ' ';
                if (track2D[y][x] == 'S' && startFinish == null) {
                    startFinish = new Point(x, y);
                }
                if (firstPoint == null) {
                    firstPoint = new Point(x, y);
                }
            }
        }
        if (startFinish == null) {
            throw new IllegalArgumentException("Start/Finish point not found in track.");
        }
        if (firstPoint == null) {
            throw new IllegalArgumentException("No points found in track.");
        }

        track = new ArrayList<>();
        Point currentPoint = firstPoint;
        int[][] directions = {
                { 0, -1 }, { 1, -1 }, { 1, 0 }, { 1, 1 },
                { 0, 1 }, { -1, 1 }, { -1, 0 }, { -1, -1 }
        };

        while (!track.contains(currentPoint)) {
            track.add(currentPoint);
            boolean moved = false;

            for (int[] dir : directions) {
                int newX = currentPoint.x + dir[0];
                int newY = currentPoint.y + dir[1];

                if (newX >= 0 && newX <= maxX && newY >= 0 && newY <= maxY) {
                    char c = track2D[newY][newX];
                    Point nextPoint = new Point(newX, newY);
                    if ((c == '#' || c == 'S') && !track.contains(nextPoint)) {
                        currentPoint = nextPoint;
                        moved = true;
                        break;
                    }
                }
            }

            if (!moved) {
                break; // No more moves found — maybe open shape or endpoint
            }
        }

        this.length = track.size();
        System.out.println("Track length: " + length);
    }

    public Track(ArrayList<Point> track, Point startFinish) {
        if (track == null || track.isEmpty()) {
            throw new IllegalArgumentException("Track cannot be null or empty.");
        }
        if (startFinish == null) {
            throw new IllegalArgumentException("Start/Finish point cannot be null.");
        }
        if (!track.contains(startFinish)) {
            throw new IllegalArgumentException("Start/Finish point must be part of the track.");
        }
        this.track = new ArrayList<Point>(track.size());
        for ( Point p : track) {
            this.track.add(p);
        }

        this.startFinish = startFinish;
        this.length = track.size();
    }

    public ArrayList<Point> getTrack() {
        return track;
    }

    public int getLength() {
        return length;
    }

    public Point getPoint(int index) {
        if (index < 0 || index >= length) {
            throw new IndexOutOfBoundsException("Index out of bounds for track length.");
        }
        return track.get(index);
    }

    public int getIndex(Point point) {
        int index = track.indexOf(point);
        if (index == -1) {
            throw new IllegalArgumentException("Point not found in track.");
        }
        return index;
    }

    public Point getStartFinish() {
        return startFinish;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (int y = 0; y <= maxY; y++) {
            for (int x = 0; x <= maxX; x++) {
                Point p = new Point(x, y);
                if (track.contains(p)) {
                    if (p.equals(startFinish)) {
                        sb.append("🏁"); // Start/Finish point
                    } else {
                        sb.append('#'); // Track point
                    }
                } else {
                    sb.append(' '); // Empty space
                }
            }
            sb.append('\n');
        }

        return sb.toString();
    }
}
