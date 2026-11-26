import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OpeningAnalyzer {

    public static List<OpeningStatistics> analyze(List<OpeningResult> games) {
        Map<String, List<OpeningResult>> groupedGames = new HashMap<>();

        for (OpeningResult game : games) {
            String openingName = game.openingName();

            if (openingName == null || openingName.isBlank()) {
                openingName = "Unknown";
            } else {
                openingName = openingName.strip();
            }

            if (!groupedGames.containsKey(openingName)) {
                groupedGames.put(openingName, new ArrayList<>());
            }

            groupedGames.get(openingName).add(game);
        }

        List<OpeningStatistics> statistics = new ArrayList<>();

        for (String openingName : groupedGames.keySet()) {
            int wins = 0;
            int draws = 0;
            int losses = 0;

            for (OpeningResult game : groupedGames.get(openingName)) {
                String result = game.result();

                if ("Win".equals(result)) {
                    wins++;
                } else if ("Draw".equals(result)) {
                    draws++;
                } else if ("Loss".equals(result)) {
                    losses++;
                } else {
                    throw new IllegalArgumentException("Unknown game result: " + result);
                }
            }

            statistics.add(new OpeningStatistics(openingName, wins, draws, losses));
        }

        return statistics;
    }
}
