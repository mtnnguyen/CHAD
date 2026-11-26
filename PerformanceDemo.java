import java.util.List;

public class PerformanceDemo {

    public static void main(String[] args) {

        List<GameSummary> games = List.of(
                new GameSummary("Adam", "Bob", "Rapid",
                        "2026.11.01", "1-0", ""),

                new GameSummary("Alice", "Adam", "Blitz",
                        "2026.11.02", "1-0", ""),

                new GameSummary("Adam", "Charlie", "Rapid",
                        "2026.11.03", "1/2-1/2", "")
        );

        PerformanceCalculator calculator =
                new PerformanceCalculator();

        PerformanceStats stats =
                calculator.calculate(games, "Adam");

        System.out.println("Total games: " + stats.totalGames());
        System.out.println("Wins: " + stats.wins());
        System.out.println("Draws: " + stats.draws());
        System.out.println("Losses: " + stats.losses());
        System.out.println("Win rate: " + stats.winRate() + "%");

        // TEST 2: Empty dataset
        PerformanceStats emptyStats =
                calculator.calculate(List.of(), "Adam");

        System.out.println("Empty dataset: "
                + emptyStats.totalGames()
                + " games, "
                + emptyStats.winRate()
                + "% win rate");

        // TEST 3: Unfinished game
        List<GameSummary> unfinishedGames = List.of(
                new GameSummary(
                        "Adam", "Bob", "Casual",
                        "2026.11.16", "*", ""
                )
        );

        PerformanceStats unfinishedStats =
                calculator.calculate(unfinishedGames, "Adam");

        System.out.println("Unfinished games counted: "
                + unfinishedStats.totalGames());
    }
}



