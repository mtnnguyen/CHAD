import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.List;

//Table model for the games currently shown on the home page.
public final class GameTableModel extends AbstractTableModel {

    private static final String[] COLUMNS = {"#", "White", "Black", "Result", "Date", "Event"};

    private List<GameSummary> games = new ArrayList<>();

    /** Replaces the visible rows with a new list of games. */
    public void setGames(List<GameSummary> games) {
        this.games = new ArrayList<>(games);
        fireTableDataChanged();
    }

    @Override
    public int getRowCount() {
        return games.size();
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
        return column == 0 ? Integer.class : String.class;
    }

    @Override
    public boolean isCellEditable(int row, int column) {
        return false;
    }

    @Override
    public Object getValueAt(int row, int column) {
        GameSummary game = games.get(row);
        return switch (column) {
            case 0 -> row + 1;
            case 1 -> game.white();
            case 2 -> game.black();
            case 3 -> game.result();
            case 4 -> game.date();
            case 5 -> game.event();
            default -> "";
        };
    }
}
