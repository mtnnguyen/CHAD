/**
 * A game shown in the library table, plus its bookmark and note.
 */
public record GameRow(GameSummary game, boolean bookmarked, String note) {
}
