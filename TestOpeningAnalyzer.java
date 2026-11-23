
import java.util.List;

public class TestOpeningAnalyzer {

    public static void main(String[] args) {
        List<OpeningResult> games = List.of(
                new OpeningResult("Italian Game", "Win"),
                new OpeningResult("Italian Game", "Loss"),
                new OpeningResult("Sicilian Defence", "Loss"),
                new OpeningResult("Italian Game", "Win"),
                new OpeningResult("Sicilian Defence", "Draw")
        );

        List<OpeningStatistics> results = OpeningAnalyzer.analyze(games);

        if (results.size() != 2) {
            throw new AssertionError("Expected exactly two opening rows.");
        }

        checkOpening(results, "Italian Game", 2, 0, 1, 2.0 / 3 * 100);
        checkOpening(results, "Sicilian Defence", 0, 1, 1, 0.0);
        if (!OpeningAnalyzer.analyze(List.of()).isEmpty()) {
            throw new AssertionError("Expected no opening rows for an empty game list.");
        }
        System.out.println("PASS: empty game list");

        checkInvalidResult("Other");
        checkInvalidResult(null);
        List<OpeningStatistics> missingOpenings = OpeningAnalyzer.analyze(List.of(
                new OpeningResult(null, "Win"),
                new OpeningResult("   ", "Draw")
        ));

        if (missingOpenings.size() != 1) {
            throw new AssertionError("Missing opening names should share one row.");
        }

        checkOpening(missingOpenings, "Unknown", 1, 1, 0, 50.0);
        List<OpeningStatistics> spacedNames = OpeningAnalyzer.analyze(List.of(
                new OpeningResult("Italian Game", "Win"),
                new OpeningResult(" Italian Game ", "Loss"),
                new OpeningResult("Italian Game  ", "Draw")
        ));

        if (spacedNames.size() != 1) {
            throw new AssertionError(
                    "Opening names with extra spaces should be grouped together."
            );
        }

        checkOpening(spacedNames, "Italian Game", 1, 1, 1, 100.0 / 3);

        System.out.println("All opening analyzer tests passed!");
    }

    private static void checkInvalidResult(String result) {
        try {
            OpeningAnalyzer.analyze(List.of(
                    new OpeningResult("Italian Game", result)
            ));
        } catch (IllegalArgumentException exception) {
            System.out.println("PASS: rejected invalid result " + result);
            return;
        }

        throw new AssertionError(
                "Expected IllegalArgumentException for result: " + result
        );
    }

    private static void checkOpening(
            List<OpeningStatistics> results,
            String openingName,
            int expectedWins,
            int expectedDraws,
            int expectedLosses,
            double expectedWinRate) {

        for (OpeningStatistics opening : results) {
            if (!openingName.equals(opening.getOpeningName())) {
                continue;
            }

            int expectedTotal = expectedWins + expectedDraws + expectedLosses;

            if (opening.getTotalGames() != expectedTotal
                    || opening.getWins() != expectedWins
                    || opening.getDraws() != expectedDraws
                    || opening.getLosses() != expectedLosses) {
                throw new AssertionError(
                        "Incorrect game counts for " + openingName
                );
            }

            double actualWinRate = opening.getWinRate();

            if (!Double.isFinite(actualWinRate)
                    || Math.abs(actualWinRate - expectedWinRate) > 0.001) {
                throw new AssertionError(
                        "Incorrect win rate for " + openingName
                );
            }

            System.out.println("PASS: " + openingName + " statistics");
            return;
        }

        throw new AssertionError("Missing opening: " + openingName);
    }
}
