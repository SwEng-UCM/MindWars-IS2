package game;

import ui.ConsoleIO;

public class MapGrid {
    public enum CellStatus {
        UNCLAIMED, PLAYER_1, PLAYER_2
    }

    private final CellStatus[][] grid;
    private final int size;

    public MapGrid(int size) {
        this.size = size;
        this.grid = new CellStatus[size][size];
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                grid[i][j] = CellStatus.UNCLAIMED;
            }
        }
    }

    public boolean isInside(int row, int col) {
        return row >= 0 && row < size && col >= 0 && col < size;
    }

    public boolean isFree(int row, int col) {
        return isInside(row, col) && grid[row][col] == CellStatus.UNCLAIMED;
    }

    public boolean claimCell(int playerNum, int row, int col) {
        if (isFree(row, col)) {
            grid[row][col] = (playerNum == 1) ? CellStatus.PLAYER_1 : CellStatus.PLAYER_2;
            return true;
        }
        return false;
    }

    public int countTerritory(int playerNum) {
        int count = 0;
        CellStatus target = (playerNum == 1) ? CellStatus.PLAYER_1 : CellStatus.PLAYER_2;
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                if (grid[i][j] == target)
                    count++;
            }
        }
        return count;
    }

    public void display(ConsoleIO io) {
        io.println("  CURRENT MAP : ");

        StringBuilder header = new StringBuilder("    ");
        for (int i = 0; i < size; i++)
            header.append(i).append(" ");
        io.println(header.toString());

        for (int i = 0; i < size; i++) {
            StringBuilder line = new StringBuilder("  " + i + " ");
            for (int j = 0; j < size; j++) {
                switch (grid[i][j]) {
                    case UNCLAIMED -> line.append(". ");
                    case PLAYER_1 -> line.append("1 ");
                    case PLAYER_2 -> line.append("2 ");
                }
            }
            io.println(line.toString());
        }
    }
}