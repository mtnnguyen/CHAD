public record PerformanceStats(
        int totalGames,
        int wins,
        int draws,
        int losses
) {
    public double winRate() {
        if (totalGames == 0) {
            return 0.0;
        }

        return (double) wins / totalGames * 100;
    }
}
