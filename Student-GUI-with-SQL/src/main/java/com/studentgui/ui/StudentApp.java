package com.studentgui.ui;

import com.studentgui.db.DatabaseHelper;
import com.studentgui.util.InputValidator;
import com.studentgui.util.InputValidator.ValidationResult;
import com.formdev.flatlaf.FlatDarkLaf;
import com.opencsv.CSVWriter;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.regex.Pattern;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StudentApp extends JFrame {

    private DatabaseHelper db;
    private JPanel mainContentPanel;
    private CardLayout cardLayout;

    // Sidebar Buttons
    private JButton btnAdd;
    private JButton btnView;
    private JButton btnStats;

    // Panels
    private JPanel addPanel;
    private JPanel viewPanel;
    private JPanel statsPanel;

    // Add Form Components
    private JTextField txtName;
    private JTextField txtRollNo;
    private Map<String, JTextField> markFields = new HashMap<>();
    private static final String[] SUBJECTS = { "Science", "Social", "Maths", "English", "Hindi", "Kannada" };

    // View Components
    private JTable table;
    private DefaultTableModel tableModel;
    private JTextField txtSearch;

    // Stats Components
    private JLabel lblTotalStudents;
    private JLabel lblClassAvg;
    private JLabel lblTopper;
    private JPanel chartContainer;

    public StudentApp() {
        try {
            db = new DatabaseHelper();
            // Test connection
            db.connect().close();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Database Connection Failed: " + e.getMessage(), "Error",
                    JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Failed to load database config: " + e.getMessage(), "Error",
                    JOptionPane.ERROR_MESSAGE);
            // Continue anyway, maybe config will be fixed later or env is missing
        }

        setTitle("🎓 Pro Student Management System (Java)");
        setSize(1100, 750);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // Sidebar
        JPanel sidebar = createSidebar();
        add(sidebar, BorderLayout.WEST);

        // Main Content
        cardLayout = new CardLayout();
        mainContentPanel = new JPanel(cardLayout);

        addPanel = createAddPanel();
        viewPanel = createViewPanel();
        statsPanel = createStatsPanel();

        mainContentPanel.add(addPanel, "ADD");
        mainContentPanel.add(viewPanel, "VIEW");
        mainContentPanel.add(statsPanel, "STATS");

        add(mainContentPanel, BorderLayout.CENTER);

        // Styling
        try {
            UIManager.setLookAndFeel(new FlatDarkLaf());
            SwingUtilities.updateComponentTreeUI(this);
        } catch (Exception ex) {
            System.err.println("Failed to initialize FlatLaf");
        }
    }

    private JPanel createSidebar() {
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new GridBagLayout());
        sidebar.setPreferredSize(new Dimension(220, 0));
        sidebar.setBackground(new Color(33, 37, 41)); // Dark Sidebar

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(10, 20, 10, 20);
        gbc.gridx = 0;

        // Logo
        JLabel lblLogo = new JLabel("SMS PRO", SwingConstants.CENTER);
        lblLogo.setFont(new Font("Segoe UI", Font.BOLD, 24));
        lblLogo.setForeground(Color.WHITE);
        gbc.gridy = 0;
        gbc.insets = new Insets(10, 20, 30, 20); // Add bottom margin for logo
        sidebar.add(lblLogo, gbc);

        // Buttons
        gbc.insets = new Insets(5, 20, 5, 20); // Reset insets for buttons

        btnAdd = createSidebarButton("✚ Add Student");
        btnAdd.addActionListener(e -> showCard("ADD"));
        gbc.gridy = 1;
        sidebar.add(btnAdd, gbc);

        btnView = createSidebarButton("📋 View Records");
        btnView.addActionListener(e -> {
            showCard("VIEW");
            refreshTable();
        });
        gbc.gridy = 2;
        sidebar.add(btnView, gbc);

        btnStats = createSidebarButton("📊 Performance");
        btnStats.addActionListener(e -> {
            showCard("STATS");
            updateStats();
        });
        gbc.gridy = 3;
        sidebar.add(btnStats, gbc);

        // Filler
        gbc.gridy = 4;
        gbc.weighty = 1.0;
        sidebar.add(new JPanel() {
            {
                setOpaque(false);
            }
        }, gbc);

        return sidebar;
    }

    private JButton createSidebarButton(String text) {
        JButton btn = new JButton(text);
        btn.setFocusPainted(false);
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        // Style handled by FlatLaf usually, but we can customize slightly
        return btn;
    }

    private void showCard(String cardName) {
        cardLayout.show(mainContentPanel, cardName);
    }

    private JPanel createAddPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(new EmptyBorder(40, 40, 40, 40));

        JPanel form = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Title
        JLabel title = new JLabel("Add Student Marks");
        title.setFont(new Font("Segoe UI", Font.BOLD, 24));

        GridBagConstraints titleGbc = new GridBagConstraints();
        titleGbc.gridx = 0;
        titleGbc.gridy = 0;
        titleGbc.gridwidth = 2;
        titleGbc.anchor = GridBagConstraints.NORTHWEST;
        titleGbc.insets = new Insets(0, 0, 30, 0);
        panel.add(title, titleGbc);

        // Form Fields
        int row = 0;
        form.add(new JLabel("Student Name:"), gbc(0, row));
        txtName = new JTextField(20);
        form.add(txtName, gbc(1, row++));

        form.add(new JLabel("Roll Number:"), gbc(0, row));
        txtRollNo = new JTextField(20);
        form.add(txtRollNo, gbc(1, row++));

        // Marks
        JLabel lblMarks = new JLabel("Subject Marks");
        lblMarks.setFont(new Font("Segoe UI", Font.BOLD, 16));
        GridBagConstraints marksGbc = gbc(0, row++);
        marksGbc.gridwidth = 2;
        marksGbc.insets = new Insets(15, 5, 5, 5); // Add top margin
        form.add(lblMarks, marksGbc);

        for (int i = 0; i < SUBJECTS.length; i++) {
            String sub = SUBJECTS[i];
            form.add(new JLabel(sub + ":"), gbc(0, row));
            JTextField field = new JTextField(10);
            markFields.put(sub, field);
            form.add(field, gbc(1, row++));
        }

        JButton btnSave = new JButton("💾 Save Record");
        btnSave.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnSave.addActionListener(e -> saveData());

        GridBagConstraints btnGbc = gbc(0, row++);
        btnGbc.gridwidth = 2;
        btnGbc.fill = GridBagConstraints.HORIZONTAL;
        btnGbc.insets = new Insets(25, 5, 5, 5); // Add top margin
        form.add(btnSave, btnGbc);

        // Center form in panel
        GridBagConstraints centerGbc = new GridBagConstraints();
        centerGbc.gridx = 0;
        centerGbc.gridy = 1;
        panel.add(form, centerGbc);

        return panel;
    }

    private GridBagConstraints gbc(int x, int y) {
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = x;
        c.gridy = y;
        c.insets = new Insets(5, 5, 5, 5);
        c.anchor = GridBagConstraints.EAST;
        if (x == 1) {
            c.anchor = GridBagConstraints.WEST;
            c.fill = GridBagConstraints.HORIZONTAL;
        }
        return c;
    }

    private void saveData() {
        String name = txtName.getText();
        String roll = txtRollNo.getText();

        // Validate Name
        ValidationResult nameRes = InputValidator.validateName(name);
        if (!nameRes.isValid) {
            showError(nameRes.errorMessage);
            return;
        }

        // Validate Roll
        ValidationResult rollRes = InputValidator.validateRollNumber(roll);
        if (!rollRes.isValid) {
            showError(rollRes.errorMessage);
            return;
        }

        Map<String, Integer> marksMap = new HashMap<>();
        for (String sub : SUBJECTS) {
            String m = markFields.get(sub).getText();
            ValidationResult mRes = InputValidator.validateMarks(m, sub);
            if (!mRes.isValid) {
                showError(mRes.errorMessage);
                return;
            }
            if (mRes.parsedValue != null) {
                marksMap.put(sub, (Integer) mRes.parsedValue);
            }
        }

        try {
            db.saveStudentMarks((String) nameRes.parsedValue, (Integer) rollRes.parsedValue, marksMap);
            JOptionPane.showMessageDialog(this, "Student record saved successfully!");
            clearForm();
        } catch (SQLException e) {
            showError("Database Error: " + e.getMessage());
        }
    }

    private void clearForm() {
        txtName.setText("");
        txtRollNo.setText("");
        markFields.values().forEach(f -> f.setText(""));
    }

    private JPanel createViewPanel() {
        JPanel panel = new JPanel(new BorderLayout(20, 20));
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));

        // Top Bar
        JPanel topBar = new JPanel(new BorderLayout());
        JLabel title = new JLabel("Student Records");
        title.setFont(new Font("Segoe UI", Font.BOLD, 24));
        topBar.add(title, BorderLayout.WEST);

        txtSearch = new JTextField(20);
        txtSearch.putClientProperty("JTextField.placeholderText", "Search by Name/Roll..."); // FlatLaf feature
        txtSearch.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                filterTable();
            }
        });
        topBar.add(txtSearch, BorderLayout.EAST);
        panel.add(topBar, BorderLayout.NORTH);

        // Table
        String[] cols = { "Roll No", "Name", "Science", "Social", "Maths", "English", "Hindi", "Kannada", "Total",
                "Average" };
        tableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }

            @Override
            public Class<?> getColumnClass(int columnIndex) {
                if (columnIndex == 0 || columnIndex >= 2)
                    return Integer.class; // Rough fix for sorting types
                return String.class;
            }
        };
        table = new JTable(tableModel);
        table.setRowHeight(30);
        table.setAutoCreateRowSorter(true);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);

        // Buttons
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnDelete = new JButton("🗑 Delete Selected");
        btnDelete.setBackground(new Color(211, 47, 47));
        btnDelete.setForeground(Color.WHITE);
        btnDelete.addActionListener(e -> deleteRecord());

        JButton btnExport = new JButton("📥 Export to CSV");
        btnExport.addActionListener(e -> exportCsv());

        JButton btnRefresh = new JButton("🔄 Refresh");
        btnRefresh.addActionListener(e -> refreshTable());

        btnPanel.add(btnDelete);
        btnPanel.add(btnExport);
        btnPanel.add(btnRefresh);
        panel.add(btnPanel, BorderLayout.SOUTH);

        return panel;
    }

    private void refreshTable() {
        tableModel.setRowCount(0);
        try {
            java.util.List<Map<String, Object>> records = db.getAllRecords();
            for (Map<String, Object> r : records) {
                Long total = 0L;
                int count = 0;
                Object[] row = new Object[10];
                row[0] = r.get("ROLL_NO");
                row[1] = r.get("NAME");

                for (int i = 0; i < SUBJECTS.length; i++) {
                    Object val = r.get(SUBJECTS[i]);
                    if (val != null) {
                        try {
                            int v = ((Number) val).intValue();
                            row[i + 2] = v;
                            total += v;
                            count++;
                        } catch (Exception e) {
                            row[i + 2] = "-";
                        }
                    } else {
                        row[i + 2] = "-";
                    }
                }

                row[8] = total;
                row[9] = count > 0 ? String.format("%.2f", (double) total / count) : "0.0";

                tableModel.addRow(row);
            }
        } catch (SQLException e) {
            showError("Failed to fetch records: " + e.getMessage());
        }
    }

    private void filterTable() {
        String term = txtSearch.getText();
        ValidationResult val = InputValidator.validateSearchTerm(term);
        if (!val.isValid) {
            return;
        }

        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(tableModel);
        table.setRowSorter(sorter);
        if (term.trim().length() == 0) {
            sorter.setRowFilter(null);
        } else {
            // Use Pattern.quote to treat the search term as a literal string, safe from
            // regex errors
            try {
                sorter.setRowFilter(RowFilter.regexFilter("(?i)" + Pattern.quote(term)));
            } catch (Exception e) {
                // Should not happen with quoted pattern, but safety first
            }
        }
    }

    private void deleteRecord() {
        int row = table.getSelectedRow();
        if (row == -1) {
            showError("Please select a record to delete");
            return;
        }

        int modelRow = table.convertRowIndexToModel(row);
        Object rollObj = tableModel.getValueAt(modelRow, 0);
        int rollNo = Integer.parseInt(rollObj.toString()); // Safer parsing

        int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to delete roll no " + rollNo + "?",
                "Confirm", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                db.deleteStudent(rollNo);
                refreshTable();
            } catch (SQLException e) {
                showError("Error deleting: " + e.getMessage());
            }
        }
    }

    private void exportCsv() {
        try {
            String filename = "student_report_"
                    + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".csv";
            try (CSVWriter writer = new CSVWriter(new FileWriter(filename))) {
                // Headers
                String[] headers = new String[tableModel.getColumnCount()];
                for (int i = 0; i < tableModel.getColumnCount(); i++)
                    headers[i] = tableModel.getColumnName(i);
                writer.writeNext(headers);

                // Rows
                for (int i = 0; i < tableModel.getRowCount(); i++) {
                    String[] row = new String[tableModel.getColumnCount()];
                    for (int j = 0; j < tableModel.getColumnCount(); j++) {
                        Object val = tableModel.getValueAt(i, j);
                        row[j] = val == null ? "" : val.toString();
                    }
                    writer.writeNext(row);
                }
            }
            JOptionPane.showMessageDialog(this, "Data exported to " + filename);
        } catch (IOException e) {
            showError("Export failed: " + e.getMessage());
        }
    }

    private JPanel createStatsPanel() {
        JPanel panel = new JPanel(new BorderLayout(20, 20));
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));

        JLabel title = new JLabel("Performance Analytics");
        title.setFont(new Font("Segoe UI", Font.BOLD, 24));
        panel.add(title, BorderLayout.NORTH);

        // Cards
        JPanel cards = new JPanel(new GridLayout(1, 3, 20, 0));
        lblTotalStudents = createStatCard(cards, "Total Students", "0");
        lblClassAvg = createStatCard(cards, "Class Average", "0.0");
        lblTopper = createStatCard(cards, "Top Performer", "-");
        panel.add(cards, BorderLayout.CENTER);

        // Chart container - we'll add JFreeChart here
        chartContainer = new JPanel(new BorderLayout());
        chartContainer.setPreferredSize(new Dimension(800, 300));
        panel.add(chartContainer, BorderLayout.SOUTH);

        return panel;
    }

    private JLabel createStatCard(JPanel parent, String title, String initialVal) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(new Color(60, 63, 65)); // Slightly lighter card bg
        card.setBorder(BorderFactory.createLineBorder(new Color(80, 80, 80)));

        JLabel lblTitle = new JLabel(title, SwingConstants.CENTER);
        lblTitle.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lblTitle.setForeground(Color.LIGHT_GRAY);
        card.add(lblTitle, BorderLayout.NORTH);

        JLabel lblVal = new JLabel(initialVal, SwingConstants.CENTER);
        lblVal.setFont(new Font("Segoe UI", Font.BOLD, 24));
        lblVal.setForeground(new Color(100, 180, 255));
        card.add(lblVal, BorderLayout.CENTER);

        parent.add(card);
        return lblVal;
    }

    private void updateStats() {
        try {
            List<Map<String, Object>> records = db.getAllRecords();
            if (records == null || records.isEmpty()) {
                lblTotalStudents.setText("0");
                lblClassAvg.setText("0.0");
                lblTopper.setText("-");
                return;
            }

            int total = records.size();
            double grandTotalSum = 0;

            double maxAvg = -1;
            String topperName = "-";

            Map<String, Double> subjSums = new HashMap<>();

            // Re-initialize map
            for (String s : SUBJECTS) {
                subjSums.put(s, 0.0);
            }
            Map<String, Integer> subjCounts = new HashMap<>();
            for (String s : SUBJECTS) {
                subjCounts.put(s, 0);
            }

            for (Map<String, Object> r : records) {
                double studentSum = 0;
                int studentCount = 0;

                for (String sub : SUBJECTS) {
                    Object val = r.get(sub);
                    if (val != null && val instanceof Number) {
                        int v = ((Number) val).intValue();
                        studentSum += v;
                        studentCount++;

                        subjSums.put(sub, subjSums.get(sub) + v);
                        subjCounts.put(sub, subjCounts.get(sub) + 1);
                    }
                }

                double avg = 0;
                if (studentCount > 0) {
                    avg = studentSum / studentCount;
                    if (avg > maxAvg) {
                        maxAvg = avg;
                        topperName = (String) r.get("NAME");
                    }
                }
                grandTotalSum += avg;
            }

            lblTotalStudents.setText(String.valueOf(total));
            lblClassAvg.setText(String.format("%.2f", grandTotalSum / total));
            lblTopper.setText(topperName);

            // Update Chart
            DefaultCategoryDataset dataset = new DefaultCategoryDataset();
            for (String sub : SUBJECTS) {
                int count = subjCounts.get(sub);
                double avg = count > 0 ? subjSums.get(sub) / count : 0;
                dataset.addValue(avg, "Subjects", sub);
            }

            JFreeChart barChart = ChartFactory.createBarChart(
                    "Average Score per Subject",
                    "Subject",
                    "Score",
                    dataset,
                    PlotOrientation.VERTICAL,
                    false, true, false);

            barChart.setBackgroundPaint(new Color(33, 37, 41));
            CategoryPlot plot = barChart.getCategoryPlot();
            plot.setBackgroundPaint(new Color(43, 43, 43));
            plot.setDomainGridlinePaint(Color.white);
            plot.setRangeGridlinePaint(Color.white);
            plot.getRenderer().setSeriesPaint(0, new Color(31, 83, 141)); // Blue bars

            // Labels color
            barChart.getTitle().setPaint(Color.white);
            plot.getDomainAxis().setTickLabelPaint(Color.white);
            plot.getDomainAxis().setLabelPaint(Color.white);
            plot.getRangeAxis().setTickLabelPaint(Color.white);
            plot.getRangeAxis().setLabelPaint(Color.white);

            chartContainer.removeAll();
            ChartPanel chartPanel = new ChartPanel(barChart);
            chartPanel.setBackground(new Color(43, 43, 43));
            chartContainer.add(chartPanel, BorderLayout.CENTER);
            chartContainer.revalidate();
            chartContainer.repaint();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void showError(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Error", JOptionPane.ERROR_MESSAGE);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new StudentApp().setVisible(true);
        });
    }
}
