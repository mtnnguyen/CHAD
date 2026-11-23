import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

public class HomePage extends JFrame {

    private static final String EMPTY_CARD = "empty";
    private static final String LIST_CARD = "list";

    private final List<GameSummary> games = new ArrayList<>();
    private final LibraryStore library = new LibraryStore();
    private final GameTableModel tableModel = new GameTableModel();
    private final CardLayout cards = new CardLayout();
    private final JPanel content = new JPanel(cards);
    private final JLabel emptyLabel = new JLabel("Import a PGN file to see your games here.", SwingConstants.CENTER);
    private final JLabel status = new JLabel(" No games imported");
    private final JTextField searchField = new JTextField(22);
    private final JCheckBox bookmarkedOnly = new JCheckBox("Bookmarked only");
    private final String loadWarning = library.loadWarning();
    private JTable table;
    private String importMessage = "";

    public HomePage() {
        super("Chess Game Analyzer");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1050, 650);
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
        wrapper.add(buildToolbar(), BorderLayout.NORTH);

        emptyLabel.setForeground(Color.GRAY);
        content.add(emptyLabel, EMPTY_CARD);

        table = new JTable(tableModel);
        table.setRowHeight(26);
        table.setAutoCreateRowSorter(true);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setFillsViewportHeight(true);
        table.getColumnModel().getColumn(0).setMaxWidth(50);
        table.getColumnModel().getColumn(GameTableModel.BOOKMARK_COLUMN).setMaxWidth(90);
        table.getColumnModel().getColumn(GameTableModel.NOTE_COLUMN).setPreferredWidth(240);
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int viewColumn = table.columnAtPoint(e.getPoint());
                int viewRow = table.rowAtPoint(e.getPoint());
                if (viewRow < 0 || viewColumn < 0) {
                    return;
                }

                table.setRowSelectionInterval(viewRow, viewRow);
                if (viewColumn == GameTableModel.BOOKMARK_COLUMN) {
                    if (e.getClickCount() == 1) {
                        toggleBookmark();
                    }
                }
                else if (e.getClickCount() == 2) {
                    editNote();
                }
            }
        });

        content.add(new JScrollPane(table), LIST_CARD);
        cards.show(content, EMPTY_CARD);
        wrapper.add(content, BorderLayout.CENTER);
        return wrapper;
    }

    private JComponent buildToolbar() {
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        toolbar.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));

        toolbar.add(new JLabel("Search:"));
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
        toolbar.add(searchField);

        JButton clearButton = new JButton("Clear");
        clearButton.addActionListener(e -> searchField.setText(""));
        toolbar.add(clearButton);

        bookmarkedOnly.addActionListener(e -> refreshTable());
        toolbar.add(bookmarkedOnly);

        JButton bookmarkButton = new JButton("Toggle Bookmark");
        bookmarkButton.addActionListener(e -> toggleBookmark());
        toolbar.add(bookmarkButton);

        JButton noteButton = new JButton("Edit Note");
        noteButton.addActionListener(e -> editNote());
        toolbar.add(noteButton);

        return toolbar;
    }

    /** Builds and shows the rows that match the search and bookmark filter. */
    private void refreshTable() {
        List<GameSummary> matchingGames = GameSearch.filter(games, searchField.getText());
        List<GameRow> rows = new ArrayList<>();

        for (GameSummary game : matchingGames) {
            boolean bookmarked = library.isBookmarked(game);
            if (bookmarkedOnly.isSelected() && !bookmarked) {
                continue;
            }
            rows.add(new GameRow(game, bookmarked, library.noteFor(game)));
        }

        tableModel.setRows(rows);

        if (rows.isEmpty()) {
            emptyLabel.setText(games.isEmpty()
                    ? "Import a PGN file to see your games here."
                    : "No games match the current search or bookmark filter.");
            cards.show(content, EMPTY_CARD);
        }
        else {
            cards.show(content, LIST_CARD);
        }

        int bookmarkCount = 0;
        for (GameSummary game : games) {
            if (library.isBookmarked(game)) {
                bookmarkCount++;
            }
        }

        String warning = loadWarning.isEmpty() ? "" : " | " + loadWarning;
        status.setText(games.isEmpty()
                ? " No games imported" + warning
                : " " + rows.size() + " of " + games.size() + " game(s) shown | "
                        + bookmarkCount + " bookmarked" + importMessage + warning);
    }

    private GameSummary selectedGame() {
        int viewRow = table.getSelectedRow();
        if (viewRow < 0) {
            return null;
        }
        return tableModel.rowAt(table.convertRowIndexToModel(viewRow)).game();
    }

    private void toggleBookmark() {
        GameSummary game = selectedGame();
        if (game == null) {
            JOptionPane.showMessageDialog(this, "Select a game first.",
                    "Bookmark", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        try {
            library.setBookmarked(game, !library.isBookmarked(game));
            refreshTable();
        }
        catch (IOException ex) {
            showSaveError(ex);
        }
    }

    private void editNote() {
        GameSummary game = selectedGame();
        if (game == null) {
            JOptionPane.showMessageDialog(this, "Select a game first.",
                    "Note", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        JTextArea area = new JTextArea(library.noteFor(game), 10, 48);
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        JScrollPane scroll = new JScrollPane(area);

        int choice = JOptionPane.showConfirmDialog(
                this,
                scroll,
                "Note - " + game.white() + " vs " + game.black(),
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE);

        if (choice == JOptionPane.OK_OPTION) {
            try {
                library.setNote(game, area.getText());
                refreshTable();
            }
            catch (IOException ex) {
                showSaveError(ex);
            }
        }
    }

    private void showSaveError(IOException ex) {
        JOptionPane.showMessageDialog(this,
                "Could not save your library:\n" + ex.getMessage(),
                "Save failed", JOptionPane.ERROR_MESSAGE);
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
