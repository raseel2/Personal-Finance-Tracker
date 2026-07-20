
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Personal Finance Tracker (Colorful UI version) Java Swing desktop application
 * to track income and expenses, with a vibrant, modern-styled interface and
 * local CSV persistence.
 *
 * Author: Raseel Al-Shahrani
 */
public class FinanceTracker extends JFrame {

    // ---- Vibrant color palette ----
    private static final Color BG_COLOR = new Color(247, 245, 255);      // soft lavender bg
    private static final Color CARD_COLOR = Color.WHITE;
    private static final Color PURPLE = new Color(124, 58, 237);         // primary
    private static final Color PURPLE_DARK = new Color(91, 33, 182);
    private static final Color TEAL = new Color(13, 148, 136);           // income accent
    private static final Color PINK = new Color(219, 39, 119);           // delete/danger
    private static final Color AMBER = new Color(245, 158, 11);          // header accent
    private static final Color TEXT_DARK = new Color(31, 27, 46);
    private static final Color TEXT_MUTED = new Color(120, 113, 140);
    private static final Color GREEN = new Color(5, 150, 105);
    private static final Color RED = new Color(225, 29, 72);
    private static final Font FONT_REGULAR = new Font("Segoe UI", Font.PLAIN, 14);
    private static final Font FONT_BOLD = new Font("Segoe UI", Font.BOLD, 14);
    private static final Font FONT_TITLE = new Font("Segoe UI", Font.BOLD, 22);
    private static final Font FONT_BALANCE = new Font("Segoe UI", Font.BOLD, 24);

    // Category color coding for the table
    private static final Color[] CATEGORY_COLORS = {
        new Color(255, 237, 213), // Food - peach
        new Color(219, 234, 254), // Transport - blue
        new Color(220, 252, 231), // Education - green
        new Color(253, 230, 138), // Shopping - amber
        new Color(233, 213, 255), // Salary - purple
        new Color(254, 226, 226), // Allowance - pink
        new Color(229, 231, 235) // Other - gray
    };
    private static final String[] CATEGORY_NAMES = {
        "Food", "Transport", "Education", "Shopping", "Salary", "Allowance", "Other"
    };

    private JTextField descriptionField;
    private JTextField amountField;
    private JComboBox<String> categoryBox;
    private JComboBox<String> typeBox;
    private JTable table;
    private DefaultTableModel tableModel;
    private JLabel balanceLabel;

    private final List<Transaction> transactions = new ArrayList<>();
    private static final String FILE_NAME = "transactions.csv";

    public FinanceTracker() {
        setTitle("Personal Finance Tracker");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(760, 580);
        setLocationRelativeTo(null);
        getContentPane().setBackground(BG_COLOR);
        setLayout(new BorderLayout());

        JPanel root = new JPanel(new BorderLayout(0, 15));
        root.setBorder(new EmptyBorder(20, 20, 20, 20));
        root.setBackground(BG_COLOR);

        root.add(buildHeader(), BorderLayout.NORTH);

        JPanel center = new JPanel(new BorderLayout(0, 15));
        center.setBackground(BG_COLOR);
        center.add(buildInputCard(), BorderLayout.NORTH);
        center.add(buildTableCard(), BorderLayout.CENTER);
        root.add(center, BorderLayout.CENTER);

        root.add(buildBalanceCard(), BorderLayout.SOUTH);

        add(root, BorderLayout.CENTER);

        loadFromFile();
        refreshTable();
    }

    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(PURPLE);
        header.setBorder(new EmptyBorder(18, 20, 18, 20));

        JLabel title = new JLabel("💰 Personal Finance Tracker");
        title.setFont(FONT_TITLE);
        title.setForeground(Color.WHITE);

        JLabel subtitle = new JLabel("Track your income and expenses");
        subtitle.setFont(FONT_REGULAR);
        subtitle.setForeground(new Color(237, 233, 254));

        JPanel textPanel = new JPanel();
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
        textPanel.setOpaque(false);
        textPanel.add(title);
        textPanel.add(subtitle);

        header.add(textPanel, BorderLayout.WEST);

        // Rounded corners feel via a wrapper
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBackground(BG_COLOR);
        header.setBorder(new EmptyBorder(18, 20, 18, 20));
        roundify(header, PURPLE, 16);
        wrapper.add(header, BorderLayout.CENTER);
        return wrapper;
    }

    private JPanel buildInputCard() {
        JPanel card = card(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 8, 6, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1;

        gbc.gridx = 0;
        gbc.gridy = 0;
        card.add(styledLabel("Description"), gbc);
        gbc.gridx = 1;
        card.add(styledLabel("Amount (SAR)"), gbc);
        gbc.gridx = 2;
        card.add(styledLabel("Category"), gbc);
        gbc.gridx = 3;
        card.add(styledLabel("Type"), gbc);

        descriptionField = styledTextField();
        amountField = styledTextField();
        categoryBox = styledComboBox(CATEGORY_NAMES);
        typeBox = styledComboBox(new String[]{"Expense", "Income"});

        gbc.gridy = 1;
        gbc.gridx = 0;
        card.add(descriptionField, gbc);
        gbc.gridx = 1;
        card.add(amountField, gbc);
        gbc.gridx = 2;
        card.add(categoryBox, gbc);
        gbc.gridx = 3;
        card.add(typeBox, gbc);

        JButton addButton = coloredButton("Add Transaction", TEAL, new Color(15, 118, 110));
        addButton.addActionListener(this::onAdd);
        JButton deleteButton = coloredButton("Delete Selected", PINK, new Color(190, 24, 93));
        deleteButton.addActionListener(this::onDelete);

        gbc.gridwidth = 1;
        gbc.gridy = 2;
        gbc.gridx = 0;
        card.add(addButton, gbc);
        gbc.gridx = 1;
        card.add(deleteButton, gbc);

        return wrapCard(card, "➕ Add Transaction", PURPLE);
    }

    private JPanel buildTableCard() {
        String[] columns = {"Description", "Amount", "Category", "Type"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        table = new JTable(tableModel);
        table.setFont(FONT_REGULAR);
        table.setRowHeight(32);
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setSelectionBackground(new Color(233, 213, 255));
        table.setSelectionForeground(TEXT_DARK);
        table.setFillsViewportHeight(true);

        // Color-code the Category column
        table.getColumnModel().getColumn(2).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object value, boolean isSelected,
                    boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(t, value, isSelected, hasFocus, row, column);
                setHorizontalAlignment(CENTER);
                setOpaque(true);
                if (!isSelected) {
                    int idx = indexOfCategory(String.valueOf(value));
                    setBackground(idx >= 0 ? CATEGORY_COLORS[idx] : Color.WHITE);
                } else {
                    setBackground(new Color(233, 213, 255));
                }
                setForeground(TEXT_DARK);
                setFont(FONT_BOLD.deriveFont(12f));
                return c;
            }
        });

        // Color-code the Type column (Income green, Expense red)
        table.getColumnModel().getColumn(3).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object value, boolean isSelected,
                    boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(t, value, isSelected, hasFocus, row, column);
                setHorizontalAlignment(CENTER);
                setForeground("Income".equals(value) ? GREEN : RED);
                setFont(FONT_BOLD);
                return c;
            }
        });

        // Color-code the Amount column to match type
        table.getColumnModel().getColumn(1).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object value, boolean isSelected,
                    boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(t, value, isSelected, hasFocus, row, column);
                setHorizontalAlignment(RIGHT);
                String type = String.valueOf(t.getValueAt(row, 3));
                setForeground("Income".equals(type) ? GREEN : RED);
                setFont(FONT_BOLD);
                return c;
            }
        });

        JTableHeader header = table.getTableHeader();
        header.setFont(FONT_BOLD);
        header.setBackground(PURPLE);
        header.setForeground(Color.WHITE);
        header.setPreferredSize(new Dimension(0, 38));

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(CARD_COLOR);

        JPanel card = card(new BorderLayout());
        card.add(scrollPane, BorderLayout.CENTER);

        return wrapCard(card, "📋 Transactions", AMBER);
    }

    private JPanel buildBalanceCard() {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(TEAL);
        card.setBorder(new EmptyBorder(16, 22, 16, 22));
        roundify(card, TEAL, 16);

        JLabel label = new JLabel("Current Balance");
        label.setFont(FONT_BOLD);
        label.setForeground(new Color(204, 251, 241));

        balanceLabel = new JLabel("0.00 SAR");
        balanceLabel.setFont(FONT_BALANCE);
        balanceLabel.setForeground(Color.WHITE);
        balanceLabel.setHorizontalAlignment(SwingConstants.RIGHT);

        card.add(label, BorderLayout.WEST);
        card.add(balanceLabel, BorderLayout.EAST);

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBackground(BG_COLOR);
        wrapper.add(card, BorderLayout.CENTER);
        return wrapper;
    }

    // ---------- Styled component helpers ----------
    private JPanel card(LayoutManager layout) {
        JPanel panel = new JPanel(layout);
        panel.setBackground(CARD_COLOR);
        panel.setBorder(new EmptyBorder(15, 15, 15, 15));
        return panel;
    }

    private JPanel wrapCard(JPanel inner, String titleText, Color accent) {
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBackground(BG_COLOR);

        JLabel title = new JLabel(titleText);
        title.setFont(FONT_BOLD.deriveFont(15f));
        title.setForeground(accent.darker());
        title.setBorder(new EmptyBorder(0, 4, 8, 0));

        JPanel shadowWrap = new JPanel(new BorderLayout());
        shadowWrap.setBackground(CARD_COLOR);
        shadowWrap.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(accent, 2, true),
                BorderFactory.createEmptyBorder()
        ));
        shadowWrap.add(inner, BorderLayout.CENTER);

        wrapper.add(title, BorderLayout.NORTH);
        wrapper.add(shadowWrap, BorderLayout.CENTER);
        return wrapper;
    }

    private JLabel styledLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(FONT_REGULAR);
        label.setForeground(TEXT_MUTED);
        return label;
    }

    private JTextField styledTextField() {
        JTextField field = new JTextField();
        field.setFont(FONT_REGULAR);
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(196, 181, 253), 2, true),
                new EmptyBorder(6, 8, 6, 8)
        ));
        return field;
    }

    private JComboBox<String> styledComboBox(String[] items) {
        JComboBox<String> box = new JComboBox<>(items);
        box.setFont(FONT_REGULAR);
        box.setBackground(Color.WHITE);
        return box;
    }

    private JButton coloredButton(String text, Color base, Color hover) {
        JButton button = new JButton(text);
        button.setFont(FONT_BOLD);
        button.setForeground(Color.WHITE);
        button.setBackground(base);
        button.setFocusPainted(false);
        button.setBorder(new EmptyBorder(10, 20, 10, 20));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setOpaque(true);
        button.addChangeListener(e -> {
            button.setBackground(button.getModel().isRollover() ? hover : base);
        });
        return button;
    }

    // Adds a subtle rounded-rectangle background painter to a panel
    private void roundify(JPanel panel, Color bg, int arc) {
        panel.setOpaque(false);
        panel.putClientProperty("bg", bg);
        panel.setUI(new javax.swing.plaf.basic.BasicPanelUI() {
            @Override
            public void paint(Graphics g, JComponent c) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(bg);
                g2.fillRoundRect(0, 0, c.getWidth(), c.getHeight(), arc, arc);
                g2.dispose();
                super.paint(g, c);
            }
        });
    }

    private int indexOfCategory(String name) {
        for (int i = 0; i < CATEGORY_NAMES.length; i++) {
            if (CATEGORY_NAMES[i].equals(name)) {
                return i;
            }
        }
        return -1;
    }

    // ---------- Logic ----------
    private void onAdd(ActionEvent e) {
        String description = descriptionField.getText().trim();
        String amountText = amountField.getText().trim();
        String category = (String) categoryBox.getSelectedItem();
        String type = (String) typeBox.getSelectedItem();

        if (description.isEmpty() || amountText.isEmpty()) {
            showMessage("Please fill in both description and amount.", "Missing Information");
            return;
        }

        double amount;
        try {
            amount = Double.parseDouble(amountText);
            if (amount <= 0) {
                throw new NumberFormatException();
            }
        } catch (NumberFormatException ex) {
            showMessage("Amount must be a positive number.", "Invalid Amount");
            return;
        }

        transactions.add(new Transaction(description, amount, category, type));
        saveToFile();
        refreshTable();

        descriptionField.setText("");
        amountField.setText("");
    }

    private void onDelete(ActionEvent e) {
        int row = table.getSelectedRow();
        if (row == -1) {
            showMessage("Please select a row to delete.", "No Selection");
            return;
        }
        transactions.remove(row);
        saveToFile();
        refreshTable();
    }

    private void showMessage(String message, String title) {
        JOptionPane.showMessageDialog(this, message, title, JOptionPane.WARNING_MESSAGE);
    }

    private void refreshTable() {
        tableModel.setRowCount(0);
        double balance = 0;
        for (Transaction t : transactions) {
            tableModel.addRow(new Object[]{
                t.description,
                String.format("%.2f", t.amount),
                t.category,
                t.type
            });
            balance += t.type.equals("Income") ? t.amount : -t.amount;
        }
        balanceLabel.setText(String.format("%.2f SAR", balance));
    }

    private void saveToFile() {
        try (PrintWriter writer = new PrintWriter(new FileWriter(FILE_NAME))) {
            for (Transaction t : transactions) {
                writer.println(t.description + "," + t.amount + "," + t.category + "," + t.type);
            }
        } catch (IOException ex) {
            showMessage("Could not save data: " + ex.getMessage(), "Save Error");
        }
    }

    private void loadFromFile() {
        File file = new File(FILE_NAME);
        if (!file.exists()) {
            return;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",", 4);
                if (parts.length == 4) {
                    transactions.add(new Transaction(
                            parts[0],
                            Double.parseDouble(parts[1]),
                            parts[2],
                            parts[3]
                    ));
                }
            }
        } catch (IOException ex) {
            System.err.println("Could not load saved data: " + ex.getMessage());
        }
    }

    private static class Transaction {

        String description;
        double amount;
        String category;
        String type;

        Transaction(String description, double amount, String category, String type) {
            this.description = description;
            this.amount = amount;
            this.category = category;
            this.type = type;
        }
    }

    public static void main(String[] args) {
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception ignored) {
            // fall back to default look and feel
        }
        SwingUtilities.invokeLater(() -> new FinanceTracker().setVisible(true));
    }
}
