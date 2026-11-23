import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Properties;

/**
 * Saves and loads bookmarks and notes. This is the only class that knows
 * about the library file.
 *
 * Games are matched by a hash of their PGN, so saved data reattaches when
 * the same game is imported again.
 */
public final class LibraryStore {

    private static final String BOOKMARK_SUFFIX = ".bookmarked";
    private static final String NOTE_SUFFIX = ".note";

    private final Path file;
    private final Properties properties = new Properties();
    private String loadWarning = "";

    public LibraryStore() {
        this(defaultFile());
    }

    public LibraryStore(Path file) {
        this.file = file;
        load();
    }

    /** The default library file, or the file set with -Dchad.library.file=... */
    public static Path defaultFile() {
        String override = System.getProperty("chad.library.file");
        if (override != null && !override.isBlank()) {
            return Path.of(override);
        }
        return Path.of(System.getProperty("user.home"), ".chad", "library.properties");
    }

    public boolean isBookmarked(GameSummary game) {
        return Boolean.parseBoolean(properties.getProperty(keyFor(game) + BOOKMARK_SUFFIX, "false"));
    }

    public String noteFor(GameSummary game) {
        return properties.getProperty(keyFor(game) + NOTE_SUFFIX, "");
    }

    public void setBookmarked(GameSummary game, boolean bookmarked) throws IOException {
        String key = keyFor(game) + BOOKMARK_SUFFIX;
        if (bookmarked) {
            properties.setProperty(key, "true");
        }
        else {
            properties.remove(key);
        }
        save();
    }

    public void setNote(GameSummary game, String note) throws IOException {
        String key = keyFor(game) + NOTE_SUFFIX;
        String cleaned = note == null ? "" : note.strip();
        if (cleaned.isEmpty()) {
            properties.remove(key);
        }
        else {
            properties.setProperty(key, cleaned);
        }
        save();
    }

    /** A message to show if an existing library file could not be read. */
    public String loadWarning() {
        return loadWarning;
    }

    private void save() throws IOException {
        Path parent = file.toAbsolutePath().getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }
        try (Writer writer = Files.newBufferedWriter(file, StandardCharsets.UTF_8)) {
            properties.store(writer, "CHAD bookmarks and notes");
        }
    }

    private void load() {
        if (!Files.exists(file)) {
            return;
        }
        try (Reader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            properties.load(reader);
        }
        catch (IOException | IllegalArgumentException ex) {
            loadWarning = "Could not read saved notes (" + ex.getMessage() + ").";
        }
    }

    /**
     * Creates a stable ID for a game from its PGN.
     * Whitespace is normalised first so line-ending changes do not break the ID.
     */
    private static String keyFor(GameSummary game) {
        String normalisedPgn = game.pgn().replaceAll("\\s+", " ").strip();
        try {
            byte[] hash = MessageDigest.getInstance("SHA-256")
                    .digest(normalisedPgn.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder(hash.length * 2);
            for (byte b : hash) {
                hex.append(String.format("%02x", b));
            }
            return hex.toString();
        }
        catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 is not available", ex);
        }
    }
}
