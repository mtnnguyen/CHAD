import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.List;

/**
 * Displays the rows given to it. The model does not search or save anything.
 */
public final class GameTableModel extends AbstractTableModel {

    public static final int BOOKMARK_COLUMN = 1;
    public static final int NOTE_COLUMN = 7;

    private static final String[] COLUMNS =
            {"#", "Bookmark", "White", "Black", "Result", "Date", "Event", "Note"};

    private List<GameRow> rows = new ArrayList<>();

    /** Replaces the visible rows with a new list of games. */
    public void setRows(List<GameRow> rows) {
        this.rows = new ArrayList<>(rows);
        fireTableDataChanged();
    }

    /** Returns the row at a model index, ignoring any table sorting. */
    public GameRow rowAt(int modelRow) {
        return rows.get(modelRow);
    }

    @Override
    public int getRowCount() {
        return rows.size();
    }

    @Override
    public int getColumnCount() {
        return COLUMNS.length;
    }

    @Override
    public String getColumnName(int column) {
        return COLUMNS[column];
    }

    @Override
    public Class<?> getColumnClass(int column) {
        if (column == 0) {
            return Integer.class;
        }
        if (column == BOOKMARK_COLUMN) {
            return Boolean.class;
        }
        return String.class;
    }

    @Override
    public boolean isCellEditable(int row, int column) {
        return false;
    }

    @Override
    public Object getValueAt(int row, int column) {
        GameRow gameRow = rows.get(row);
        GameSummary game = gameRow.game();
        return switch (column) {
            case 0 -> row + 1;
            case BOOKMARK_COLUMN -> gameRow.bookmarked();
            case 2 -> game.white();
            case 3 -> game.black();
            case 4 -> game.result();
            case 5 -> game.date();
            case 6 -> game.event();
            case NOTE_COLUMN -> preview(gameRow.note());
            default -> "";
        };
    }

    private String preview(String note) {
        if (note == null || note.isBlank()) {
            return "";
        }
        String singleLine = note.replaceAll("\\s+", " ").strip();
        return singleLine.length() <= 70 ? singleLine : singleLine.substring(0, 67) + "...";
    }
}
