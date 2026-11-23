/**
 * One game as it appears in the library table.
 *
 * <p>A row combines the game itself with the user's personal bookmark and note.
 * Keeping these together makes it easy for the table to display everything it
 * needs without knowing how bookmarks and notes are saved.</p>
 */
public record GameRow(GameSummary game, boolean bookmarked, String note) {
}
