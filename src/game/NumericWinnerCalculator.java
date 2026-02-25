package game;

import player.Player;
import java.util.List;

/**
 * Logic to determine the winner of a numeric estimation question.
 * If proximity is equal, the player with the faster response time wins.
 */
public final class NumericWinnerCalculator {

    private NumericWinnerCalculator() {
    }

    public static Player calculateWinner(double targetValue, List<EstimationResponse> responses) {
        if (responses == null || responses.isEmpty())
            return null;

        EstimationResponse winnerResponse = responses.get(0);

        for (int i = 1; i < responses.size(); i++) {
            EstimationResponse current = responses.get(i);

            double currentDiff = Math.abs(current.value - targetValue);
            double bestDiff = Math.abs(winnerResponse.value - targetValue);

            if (currentDiff < bestDiff) {
                // Current is closer
                winnerResponse = current;
            } else if (currentDiff == bestDiff) {
                // Tie in distance, compare time (lower is better)
                if (current.timeTaken < winnerResponse.timeTaken) {
                    winnerResponse = current;
                }
            }
        }

        return winnerResponse.player;
    }

    /**
     * DTO to hold round-specific data for comparison.
     */
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