import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Search rule for the game library.
 */
public final class GameSearch {

    private GameSearch() {
    }

    /**
     * Returns games matching the query. A blank query returns all games.
     * Search is case-insensitive across White, Black, Event, Date, and Result.
     */
    public static List<GameSummary> filter(List<GameSummary> games, String query) {
        if (query == null || query.isBlank()) {
            return List.copyOf(games);
        }

        String needle = query.strip().toLowerCase(Locale.ROOT);
        List<GameSummary> matches = new ArrayList<>();
        for (GameSummary game : games) {
            if (matches(game, needle)) {
                matches.add(game);
            }
        }
        return matches;
    }

    private static boolean matches(GameSummary game, String needle) {
        return contains(game.white(), needle)
                || contains(game.black(), needle)
                || contains(game.event(), needle)
                || contains(game.date(), needle)
                || contains(game.result(), needle);
    }

    private static boolean contains(String value, String needle) {
        return value != null && value.toLowerCase(Locale.ROOT).contains(needle);
    }
}
