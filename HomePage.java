import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
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
    private final GameTableModel tableModel = new GameTableModel();
    private final CardLayout cards = new CardLayout();
    private final JPanel content = new JPanel(cards);
    private final JLabel emptyLabel = new JLabel("Import a PGN file to see your games here.", SwingConstants.CENTER);
    private final JLabel status = new JLabel(" No games imported");
    private final JTextField searchField = new JTextField(24);
    private String importMessage = "";

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

        refreshTable();
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
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBorder(BorderFactory.createEmptyBorder(0, 16, 0, 16));
        wrapper.add(buildSearchBar(), BorderLayout.NORTH);

        emptyLabel.setForeground(Color.GRAY);
        content.add(emptyLabel, EMPTY_CARD);

        JTable table = new JTable(tableModel);
        table.setRowHeight(26);
        table.setAutoCreateRowSorter(true);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getColumnModel().getColumn(0).setMaxWidth(50);

        content.add(new JScrollPane(table), LIST_CARD);
        cards.show(content, EMPTY_CARD);
        wrapper.add(content, BorderLayout.CENTER);
        return wrapper;
    }

    private JComponent buildSearchBar() {
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        bar.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));

        bar.add(new JLabel("Search:"));
        searchField.setToolTipText("Search by player, event, date or result");
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                refreshTable();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                refreshTable();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                refreshTable();
            }
        });
        bar.add(searchField);

        JButton clearButton = new JButton("Clear");
        clearButton.addActionListener(e -> searchField.setText(""));
        bar.add(clearButton);

        return bar;
    }

    /** Rebuilds the table from the games that match the current search text. */
    private void refreshTable() {
        List<GameSummary> visibleGames = GameSearch.filter(games, searchField.getText());
        tableModel.setGames(visibleGames);

        if (visibleGames.isEmpty()) {
            emptyLabel.setText(games.isEmpty()
                    ? "Import a PGN file to see your games here."
                    : "No games match your search.");
            cards.show(content, EMPTY_CARD);
        }
        else {
            cards.show(content, LIST_CARD);
        }

        status.setText(games.isEmpty()
                ? " No games imported"
                : " " + visibleGames.size() + " of " + games.size() + " game(s) shown" + importMessage);
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
            importGames();
        }
        else if (choice == 1) {
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

    private void addImportedGames(List<GameSummary> imported, String source) {
        if (imported.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No games found in " + source,
                    "Import", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        games.addAll(imported);
        importMessage = " (+" + imported.size() + " from " + source + ")";
        refreshTable();
    }

    private void importFromLichess(String username) {
        try {
            List<GameSummary> imported = ImportLiChess.importGames(username);
            addImportedGames(imported, "Lichess");
        }
        catch (IOException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage());
        }
        catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
    }
}
