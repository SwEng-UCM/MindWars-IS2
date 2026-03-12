package game;

import ui.ConsoleIO;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class MapGrid {
    private final char[][] grid;
    private final boolean[][] bonus_cells; // matrix for power-up
    private final int size;
    private static final char EMPTY = '.';

    private final Map<Character, boolean[][]> visibility;

    public MapGrid(int size) {
        this.size = size;
        this.grid = new char[size][size];
        this.bonus_cells = new boolean[size][size]; // false
        this.visibility = new HashMap<>(); // Fog of war

        initializeGrid();
        generateBonus();
    }

    private void initializeGrid() {
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                grid[i][j] = EMPTY;
            }
        }
    }

    private void generateBonus() {
        int total_cells = size * size;
        int nr_bonuses = total_cells / 3;
        Random rand = new Random();
        int placed = 0;

        while (placed < nr_bonuses) {
            int r = rand.nextInt(size);
            int c = rand.nextInt(size);
            if (!bonus_cells[r][c]) {
                bonus_cells[r][c] = true;
                placed++;
            }
        }
    }

    public boolean hasBonus(int row, int col) {
        return isInside(row, col) && bonus_cells[row][col];
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

    public void initVisibilityForPlayer(char symbol) {
        visibility.put(symbol, new boolean[size][size]);
    }

    public void revealCellForPlayer(char symbol, int row, int col) {
        if (!isInside(row, col)) {
            return;
        }
        boolean[][] playerView = visibility.get(symbol);
        if (playerView != null) {
            playerView[row][col] = true;
        }
    }

    public void revealNeighbourForPlayer(char symbol, int row, int col) {
        for (int dr = -1; dr <= 1; dr++) {
            for (int dc = -1; dc <= 1; dc++) {
                revealCellForPlayer(symbol, row + dr, col + dc);
            }
        }
    }

    public void displayForPlayer(ui.ConsoleIO io, char symbol) {
        io.println("\n  CURRENT MAP:");
        boolean[][] playerView = visibility.get(symbol);

        io.println("");
        io.println("    0 1 2");
        for (int i = 0; i < size; i++) {
            StringBuilder line = new StringBuilder();
            line.append(" ").append(i).append("  ");

            for (int j = 0; j < size; j++) {
                if (playerView != null && playerView[i][j]) {
                    line.append(grid[i][j]).append(" ");
                } else {
                    line.append("? ");
                }
            }

            io.println(line.toString());
        }
        io.println("");
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