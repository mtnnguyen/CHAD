import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

public class HomePage extends JFrame {

    private static final String EMPTY_CARD = "empty";
    private static final String LIST_CARD = "list";

    private final List<GameSummary> games = new ArrayList<>();
    private final DefaultTableModel tableModel = new DefaultTableModel(
            new String[] {"#", "White", "Black", "Result", "Date", "Event"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final CardLayout cards = new CardLayout();
    private final JPanel content = new JPanel(cards);
    private final JLabel status = new JLabel(" No games imported");

    public HomePage() {
        super("Chess Game Analyzer");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(900, 600);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        add(buildHeader(), BorderLayout.NORTH);
        add(buildContent(), BorderLayout.CENTER);
        status.setBorder(BorderFactory.createEmptyBorder(6, 12, 6, 12));
        add(status, BorderLayout.SOUTH);
    }

    private JComponent buildHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBorder(BorderFactory.createEmptyBorder(12, 16, 12, 16));

        JLabel title = new JLabel("Chess Game Analyzer");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 22f));

        JButton importButton = new JButton("Import Games");
        importButton.addActionListener(e -> showImportMenu());

        header.add(title, BorderLayout.WEST);
        header.add(importButton, BorderLayout.EAST);
        return header;
    }

    private JComponent buildContent() {
        JLabel empty = new JLabel("Import a PGN file to see your games here.", SwingConstants.CENTER);
        empty.setForeground(Color.GRAY);

        JTable table = new JTable(tableModel);
        table.setRowHeight(26);
        table.setAutoCreateRowSorter(true);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getColumnModel().getColumn(0).setMaxWidth(50);

        content.add(empty, EMPTY_CARD);
        content.add(new JScrollPane(table), LIST_CARD);
        content.setBorder(BorderFactory.createEmptyBorder(0, 16, 0, 16));
        cards.show(content, EMPTY_CARD);
        return content;
    }

    private void showImportMenu() {
        String[] options = {"Local PGN File", "Lichess"};

        int choice = JOptionPane.showOptionDialog(
                this,
                "Where do you want to import games from?",
                "Import Games",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                options,
                options[0]
        );

        if (choice == 0) {
            importGames();       // Your existing local file picker
        } else if (choice == 1) {
            // Ask for username, then fetch the games
            String username = JOptionPane.showInputDialog(this, "Enter your LiChess username");
            importFromLichess(username);
        }
    }

    private void importGames() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Import games");
        chooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("PGN files (*.pgn)", "pgn"));
        if (chooser.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }

        File file = chooser.getSelectedFile();
        try {
            List<GameSummary> imported = PgnParser.parse(Files.readString(file.toPath()));
            addImportedGames(imported, file.getName());
        }
        catch (IOException ex) {
                JOptionPane.showMessageDialog(this, "Could not read file:\n" + ex.getMessage(),
                        "Import failed", JOptionPane.ERROR_MESSAGE);
        }
    }
    private void addImportedGames(List<GameSummary> imported, String source){
        if (imported.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No games found in " + source,
                    "Import", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        for (GameSummary g : imported) {
            games.add(g);
            tableModel.addRow(new Object[] {
                    games.size(), g.white(), g.black(), g.result(), g.date(), g.event()});
        }
        cards.show(content, LIST_CARD);
        status.setText(" " + games.size() + " game(s) loaded (+" + imported.size() + " from " + source + ")");
    }


    private void importFromLichess(String username){

        try{
            List<GameSummary> imported = ImportLiChess.importGames(username);
            addImportedGames(imported, "Lichess");
        }
        catch (IOException ex){
            JOptionPane.showMessageDialog(this, ex.getMessage());
        }
        catch(InterruptedException ex){
            Thread.currentThread().interrupt();
        }
    }
}
