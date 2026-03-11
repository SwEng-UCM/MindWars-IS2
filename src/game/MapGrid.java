package game;

import ui.ConsoleIO;

import java.util.HashMap;
import java.util.Map;

public class MapGrid {
    private final char[][] grid;
    private final int size;
    private static final char EMPTY = '.';

    private final Map<Character,boolean[][]> visibility;

    public MapGrid(int size) {
        this.size = size;
        this.grid = new char[size][size];
        this.visibility = new HashMap<>(); //Fog of war
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                grid[i][j] = EMPTY;
            }
        }
    }

    public boolean isInside(int row, int col) {
        return row >= 0 && row < size && col >= 0 && col < size;
    }

    public boolean isFree(int row, int col) {
        return isInside(row, col) && grid[row][col] == EMPTY;
    }

    public boolean claimCell(char symbol, int row, int col) {
        if (isFree(row, col)) {
            grid[row][col] = symbol;
            return true;
        }
        return false;
    }

    public int countTerritory(char playerSymbol) {
        int count = 0;
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                if (grid[i][j] == playerSymbol) {
                    count++;
                }
            }
        }
        return count;
    }

    public void initVisibilityForPlayer (char symbol) {
        visibility.put(symbol, new boolean[size][size]);
    }

    public void revealCellForPlayer (char symbol, int row, int col) {
        if (isInside(row, col)) {
            return;
        }
        boolean[][] playerView = visibility.get(symbol);
        if (playerView[row][col]) {
            playerView[row][col] = false;
        }
    }

    public void revealNeighbourForPlayer (char symbol, int row, int col) {
        for (int dr = -1; dr <= 1; dr++) {
            for (int dc = -1; dc <= 1; dc++) {
                revealCellForPlayer(symbol, row + dr, col + dc);
            }
        }
    }


    public void display(ConsoleIO io) {
        io.println("\n  CURRENT MAP:");

        StringBuilder header = new StringBuilder("    ");
        for (int i = 0; i < size; i++) {
            header.append(i).append(" ");
        }
        io.println(header.toString());

        for (int i = 0; i < size; i++) {
            StringBuilder line = new StringBuilder("  " + i + " ");
            for (int j = 0; j < size; j++) {
                line.append(grid[i][j]).append(" ");
            }
            io.println(line.toString());
        }
        io.println("");
    }

    public void clear() {
    for (int i = 0; i < size; i++) {
        for (int j = 0; j < size; j++) {
            grid[i][j] = EMPTY;
        }
    }
}
}