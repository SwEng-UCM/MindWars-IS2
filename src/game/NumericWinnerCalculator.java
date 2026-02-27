package game;

import player.Player;
import java.util.List;

/**
 * Utility to find the winner of an estimation round.
 * Priority: 1. Smallest difference | 2. Fastest time.
 */
public final class NumericWinnerCalculator {

    private NumericWinnerCalculator() {
    }

    public static Player calculateWinner(double target, List<EstimationResponse> responses) {
        if (responses == null || responses.isEmpty())
            return null;

        EstimationResponse best = responses.get(0);

        for (int i = 1; i < responses.size(); i++) {
            EstimationResponse current = responses.get(i);
            double currentDiff = Math.abs(current.value - target);
            double bestDiff = Math.abs(best.value - target);

            // Update winner if current is closer OR same distance but faster
            if (currentDiff < bestDiff || (currentDiff == bestDiff && current.timeTaken < best.timeTaken)) {
                best = current;
            }
        }
        return best.player;
    }

    // Helper class to group player guess and time
    public static class EstimationResponse {
        public final Player player;
        public final double value;
        public final long timeTaken;

        public EstimationResponse(Player player, double value, long timeTaken) {
            this.player = player;
            this.value = value;
            this.timeTaken = timeTaken;
        }
    }
}