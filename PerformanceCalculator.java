import java.util.List;

public class PerformanceCalculator {

    public PerformanceStats calculate(
            List<GameSummary> games,
            String username
    ) {
        int wins = 0;
        int draws = 0;
        int losses = 0;

        for (GameSummary game : games) {
            boolean isWhite =
                    game.white().equalsIgnoreCase(username);
            boolean isBlack =
                    game.black().equalsIgnoreCase(username);

            if (!isWhite && !isBlack) {
                continue;
            }

            String result = game.result();

            if ("*".equals(result)) {
                continue;
            }

            if (!"1-0".equals(result)
                    && !"0-1".equals(result)
                    && !"1/2-1/2".equals(result)) {
                throw new IllegalArgumentException(
                        "Invalid game result: " + result
                );
            }

            if ("1/2-1/2".equals(result)) {
                draws++;
            } else if ("1-0".equals(result)) {
                if (isWhite) {
                    wins++;
                } else {
                    losses++;
                }
            } else if ("0-1".equals(result)) {
                if (isBlack) {
                    wins++;
                } else {
                    losses++;
                }
            }
        }

        return new PerformanceStats(
                wins + draws + losses,
                wins,
                draws,
                losses
        );
    }
}
