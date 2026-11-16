import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Minimal PGN reader: splits a file into games and reads the header tags. Moves are kept as raw text for now. */
public final class PgnParser {

    private static final Pattern TAG = Pattern.compile("^\\[(\\w+)\\s+\"(.*)\"\\]\\s*$");

    private PgnParser() {
    }

    public static List<GameSummary> parse(String text) {
        List<GameSummary> games = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean sawMoves = false;

        for (String line : text.split("\\R")) {
            boolean isTag = line.startsWith("[");
            // A tag line after movetext means a new game has started.
            if (isTag && sawMoves) {
                addGame(games, current.toString());
                current.setLength(0);
                sawMoves = false;
            }
            if (!isTag && !line.isBlank()) {
                sawMoves = true;
            }
            current.append(line).append('\n');
        }
        addGame(games, current.toString());
        return games;
    }

    private static void addGame(List<GameSummary> games, String pgn) {
        if (pgn.isBlank()) {
            return;
        }
        Map<String, String> tags = new HashMap<>();
        for (String line : pgn.split("\\R")) {
            Matcher m = TAG.matcher(line.strip());
            if (m.matches()) {
                tags.put(m.group(1), m.group(2));
            }
        }
        games.add(new GameSummary(
                tags.getOrDefault("White", "?"),
                tags.getOrDefault("Black", "?"),
                tags.getOrDefault("Event", "?"),
                tags.getOrDefault("Date", "?"),
                tags.getOrDefault("Result", "*"),
                pgn.strip()));
    }
}
