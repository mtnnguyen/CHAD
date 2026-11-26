public class OpeningStatistics {

    private final String openingName;
    private final int wins;
    private final int draws;
    private final int losses;

    public OpeningStatistics(String openingName, int wins, int draws, int losses) {
        this.openingName = openingName;
        this.wins = wins;
        this.draws = draws;
        this.losses = losses;
    }

    public String getOpeningName() {
        return openingName;
    }

    public int getWins() {
        return wins;
    }

    public int getDraws() {
        return draws;
    }

    public int getLosses() {
        return losses;
    }

    public int getTotalGames() {
        return wins + draws + losses;
    }

    public double getWinRate() {
        if (getTotalGames() == 0) {
            return 0.0;
        }

        return (double) wins / getTotalGames() * 100;
    }
}
