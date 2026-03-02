package com.studentgui.ui;

import com.studentgui.db.DatabaseHelper;
import com.studentgui.util.InputValidator;
import com.studentgui.util.InputValidator.ValidationResult;
import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatLightLaf;

// JFreeChart
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.labels.StandardCategoryItemLabelGenerator;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.data.category.DefaultCategoryDataset;

// NOTE: Apache POI imports are intentionally omitted here to avoid clashing with
// java.awt.Font and java.awt.Color. POI types are used fully-qualified in exportExcel().

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;
import java.util.regex.Pattern;

public class StudentAppV2 extends JFrame {

    private static final long serialVersionUID = 2L;
    private static final String[] SUBJECTS = DatabaseHelper.getSubjects();

    private final DatabaseHelper db = new DatabaseHelper();

    private final CardLayout cardLayout = new CardLayout();
    private final JPanel mainContent = new JPanel(cardLayout);

    private JButton btnAdd, btnView, btnStats;
    private JTextField txtName, txtRollNo;
    private final Map<String, JTextField> markFields = new LinkedHashMap<>();

    private JTable table;
    private DefaultTableModel tableModel;
    private JTextField txtSearch;

    private JLabel lblTotal, lblAvg, lblTopper;
    private JPanel chartContainer;

    public StudentAppV2() {
        applyTheme("Dark");
        setupFrame();
        buildSidebar();
        buildMainContent();
        showCard("ADD", btnAdd);
    }

    private void setupFrame() {
        setTitle("🎓 Pro Student Management System v2 (Java)");
        setSize(1150, 780);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
    }

    private void applyTheme(String mode) {
        try {
            if ("Light".equals(mode))
                UIManager.setLookAndFeel(new FlatLightLaf());
            else
                UIManager.setLookAndFeel(new FlatDarkLaf());
            if (isDisplayable())
                SwingUtilities.updateComponentTreeUI(this);
        } catch (Exception ignored) {
        }
    }

    // ── Sidebar ──────────────────────────────────────────────────────────────────
    private void buildSidebar() {
        JPanel sidebar = new JPanel(new GridBagLayout());
        sidebar.setPreferredSize(new Dimension(220, 0));
        sidebar.setBackground(new Color(33, 37, 41));

        GridBagConstraints g = new GridBagConstraints();
        g.fill = GridBagConstraints.HORIZONTAL;
        g.insets = new Insets(5, 15, 5, 15);
        g.gridx = 0;

        JLabel logo = new JLabel("SMS PRO v2", SwingConstants.CENTER);
        logo.setFont(new Font("Segoe UI", Font.BOLD, 22));
        logo.setForeground(Color.WHITE);
        g.gridy = 0;
        g.insets = new Insets(20, 15, 25, 15);
        sidebar.add(logo, g);
        g.insets = new Insets(5, 15, 5, 15);

        btnAdd = makeSidebarBtn("✚  Add Student");
        btnView = makeSidebarBtn("📋  View Records");
        btnStats = makeSidebarBtn("📊  Performance");

        btnAdd.addActionListener(e -> showCard("ADD", btnAdd));
        btnView.addActionListener(e -> {
            showCard("VIEW", btnView);
            refreshTable();
        });
        btnStats.addActionListener(e -> {
            showCard("STATS", btnStats);
            updateStats();
        });

        g.gridy = 1;
        sidebar.add(btnAdd, g);
        g.gridy = 2;
        sidebar.add(btnView, g);
        g.gridy = 3;
        sidebar.add(btnStats, g);

        // Spacer
        JPanel spacer = new JPanel();
        spacer.setOpaque(false);
        g.gridy = 4;
        g.weighty = 1.0;
        sidebar.add(spacer, g);
        g.weighty = 0;

        // Appearance toggle
        JLabel themeLabel = new JLabel("Appearance:", SwingConstants.LEFT);
        themeLabel.setForeground(Color.LIGHT_GRAY);
        themeLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        g.gridy = 5;
        g.insets = new Insets(0, 15, 2, 15);
        sidebar.add(themeLabel, g);

        JComboBox<String> themeBox = new JComboBox<>(new String[] { "Dark", "Light" });
        themeBox.addActionListener(e -> applyTheme((String) themeBox.getSelectedItem()));
        g.gridy = 6;
        g.insets = new Insets(0, 15, 20, 15);
        sidebar.add(themeBox, g);

        add(sidebar, BorderLayout.WEST);
    }

    private JButton makeSidebarBtn(String text) {
        JButton b = new JButton(text);
        b.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        b.setHorizontalAlignment(SwingConstants.LEFT);
        b.setFocusPainted(false);
        return b;
    }

    private void showCard(String name, JButton active) {
        cardLayout.show(mainContent, name);
        for (JButton b : new JButton[] { btnAdd, btnView, btnStats })
            b.setBackground(b == active ? new Color(60, 100, 160) : null);
    }

    // ── Main content
    // ──────────────────────────────────────────────────────────────
    private void buildMainContent() {
        mainContent.add(buildAddPanel(), "ADD");
        mainContent.add(buildViewPanel(), "VIEW");
        mainContent.add(buildStatsPanel(), "STATS");
        add(mainContent, BorderLayout.CENTER);
    }

    // ── ADD panel
    // ─────────────────────────────────────────────────────────────────
    private JPanel buildAddPanel() {
        JPanel outer = new JPanel(new GridBagLayout());
        outer.setBorder(new EmptyBorder(30, 40, 30, 40));

        JPanel form = new JPanel(new GridBagLayout());

        // Title
        JLabel title = new JLabel("Add Student Marks");
        title.setFont(new Font("Segoe UI", Font.BOLD, 24));
        GridBagConstraints tg = new GridBagConstraints();
        tg.gridx = 0;
        tg.gridy = 0;
        tg.gridwidth = 4;
        tg.anchor = GridBagConstraints.NORTHWEST;
        tg.insets = new Insets(0, 0, 20, 0);
        outer.add(title, tg);

        // Name row
        addFormRow(form, 0, "Student Name:", txtName = field("Enter full name"), "Roll Number:",
                txtRollNo = field("Numeric only"));

        // Subjects header
        JLabel subHead = new JLabel("Subject Marks");
        subHead.setFont(new Font("Segoe UI", Font.BOLD, 15));
        GridBagConstraints sh = new GridBagConstraints();
        sh.gridx = 0;
        sh.gridy = 1;
        sh.gridwidth = 4;
        sh.insets = new Insets(16, 8, 6, 8);
        sh.anchor = GridBagConstraints.WEST;
        form.add(subHead, sh);

        // 2-column subject grid (3 rows × 2 pairs)
        for (int i = 0; i < SUBJECTS.length; i++) {
            String sub = SUBJECTS[i];
            int row = 2 + (i / 2);
            int colBase = (i % 2) * 2;

            GridBagConstraints lg = new GridBagConstraints();
            lg.gridx = colBase;
            lg.gridy = row;
            lg.insets = new Insets(5, 10, 5, 4);
            lg.anchor = GridBagConstraints.EAST;
            form.add(label(sub + ":"), lg);

            JTextField mf = field("0–100");
            mf.setPreferredSize(new Dimension(100, 30));
            markFields.put(sub, mf);
            GridBagConstraints fg = new GridBagConstraints();
            fg.gridx = colBase + 1;
            fg.gridy = row;
            fg.insets = new Insets(5, 0, 5, 20);
            fg.fill = GridBagConstraints.HORIZONTAL;
            form.add(mf, fg);
        }

        // Save button
        JButton saveBtn = new JButton("💾  Save Record");
        saveBtn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        saveBtn.setPreferredSize(new Dimension(0, 44));
        saveBtn.addActionListener(e -> saveData());
        GridBagConstraints bg = new GridBagConstraints();
        bg.gridx = 0;
        bg.gridy = 5;
        bg.gridwidth = 4;
        bg.fill = GridBagConstraints.HORIZONTAL;
        bg.insets = new Insets(24, 10, 5, 10);
        form.add(saveBtn, bg);

        GridBagConstraints fc = new GridBagConstraints();
        fc.gridx = 0;
        fc.gridy = 1;
        outer.add(form, fc);
        return outer;
    }

    private void addFormRow(JPanel form, int row, String l1, JTextField f1, String l2, JTextField f2) {
        GridBagConstraints g = new GridBagConstraints();
        g.gridy = row;
        g.insets = new Insets(8, 10, 8, 4);
        g.anchor = GridBagConstraints.EAST;
        g.gridx = 0;
        form.add(label(l1), g);
        g.gridx = 1;
        g.fill = GridBagConstraints.HORIZONTAL;
        g.insets = new Insets(8, 0, 8, 20);
        form.add(f1, g);
        g.gridx = 2;
        g.fill = GridBagConstraints.NONE;
        g.insets = new Insets(8, 10, 8, 4);
        form.add(label(l2), g);
        g.gridx = 3;
        g.fill = GridBagConstraints.HORIZONTAL;
        g.insets = new Insets(8, 0, 8, 10);
        form.add(f2, g);
    }

    private JLabel label(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        return l;
    }

    private JTextField field(String placeholder) {
        JTextField f = new JTextField(16);
        f.putClientProperty("JTextField.placeholderText", placeholder);
        return f;
    }

    private void saveData() {
        ValidationResult nr = InputValidator.validateName(txtName.getText());
        if (!nr.isValid()) {
            showErr(nr.errorMessage());
            return;
        }

        ValidationResult rr = InputValidator.validateRollNumber(txtRollNo.getText());
        if (!rr.isValid()) {
            showErr(rr.errorMessage());
            return;
        }

        Map<String, Integer> marks = new LinkedHashMap<>();
        for (String sub : SUBJECTS) {
            ValidationResult mr = InputValidator.validateMarks(markFields.get(sub).getText(), sub);
            if (!mr.isValid()) {
                showErr(mr.errorMessage());
                return;
            }
            if (mr.parsedValue() != null)
                marks.put(sub, (Integer) mr.parsedValue());
        }
        try {
            db.saveStudentMarks((String) nr.parsedValue(), (Integer) rr.parsedValue(), marks);
            JOptionPane.showMessageDialog(this, "Student record saved successfully!");
            clearForm();
        } catch (SQLException e) {
            showErr("Database Error: " + e.getMessage());
        }
    }

    private void clearForm() {
        txtName.setText("");
        txtRollNo.setText("");
        markFields.values().forEach(f -> f.setText(""));
    }

    // ── VIEW panel
    // ────────────────────────────────────────────────────────────────
    private JPanel buildViewPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));

        JPanel top = new JPanel(new BorderLayout());
        JLabel title = new JLabel("Student Records");
        title.setFont(new Font("Segoe UI", Font.BOLD, 24));
        top.add(title, BorderLayout.WEST);

        txtSearch = new JTextField(22);
        txtSearch.putClientProperty("JTextField.placeholderText", "Search by Name / Roll...");
        txtSearch.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                filterTable();
            }
        });
        top.add(txtSearch, BorderLayout.EAST);
        panel.add(top, BorderLayout.NORTH);

        String[] cols = { "Roll No", "Name", "Science", "Social", "Maths", "English", "Hindi", "Kannada", "Total",
                "Average" };
        tableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };
        table = new JTable(tableModel);
        table.setRowHeight(30);
        table.setAutoCreateRowSorter(true);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel btns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5));
        JButton del = new JButton("🗑  Delete Selected");
        del.setBackground(new Color(211, 47, 47));
        del.setForeground(Color.WHITE);
        del.addActionListener(e -> deleteRecord());
        JButton exp = new JButton("📥  Export to Excel");
        exp.addActionListener(e -> exportExcel());
        JButton ref = new JButton("🔄  Refresh");
        ref.addActionListener(e -> refreshTable());
        btns.add(del);
        btns.add(exp);
        btns.add(ref);
        panel.add(btns, BorderLayout.SOUTH);
        return panel;
    }

    private void refreshTable() {
        tableModel.setRowCount(0);
        try {
            List<Map<String, Object>> records = db.getAllRecords();
            for (Map<String, Object> r : records) {
                long total = 0;
                int count = 0;
                Object[] row = new Object[10];
                row[0] = r.get("ROLL_NO");
                row[1] = r.get("NAME");
                for (int i = 0; i < SUBJECTS.length; i++) {
                    Object val = r.get(SUBJECTS[i]);
                    if (val instanceof Number n) {
                        row[i + 2] = n.intValue();
                        total += n.intValue();
                        count++;
                    } else
                        row[i + 2] = "–";
                }
                row[8] = total;
                row[9] = count > 0 ? String.format("%.2f", (double) total / count) : "0.0";
                tableModel.addRow(row);
            }
        } catch (SQLException e) {
            showErr("Failed to load records: " + e.getMessage());
        }
    }

    private void filterTable() {
        String term = txtSearch.getText();
        ValidationResult vr = InputValidator.validateSearchTerm(term);
        if (!vr.isValid()) {
            showErr(vr.errorMessage());
            return;
        }
        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(tableModel);
        table.setRowSorter(sorter);
        if (term.isBlank()) {
            sorter.setRowFilter(null);
            return;
        }
        try {
            sorter.setRowFilter(RowFilter.regexFilter("(?i)" + Pattern.quote(term)));
        } catch (Exception ignored) {
        }
    }

    private void deleteRecord() {
        int row = table.getSelectedRow();
        if (row == -1) {
            showErr("Please select a record to delete");
            return;
        }
        int modelRow = table.convertRowIndexToModel(row);
        int roll = Integer.parseInt(tableModel.getValueAt(modelRow, 0).toString());
        if (JOptionPane.showConfirmDialog(this, "Delete roll no " + roll + "?", "Confirm",
                JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            try {
                db.deleteStudent(roll);
                refreshTable();
            } catch (SQLException e) {
                showErr("Delete failed: " + e.getMessage());
            }
        }
    }

    /**
     * Export to Excel using Apache POI.
     * All POI types are fully-qualified here to avoid clashing with java.awt.Font /
     * java.awt.Color.
     */
    private void exportExcel() {
        if (tableModel.getRowCount() == 0) {
            showErr("No data to export");
            return;
        }
        String filename = "student_report_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"))
                + ".xlsx";
        try (org.apache.poi.xssf.usermodel.XSSFWorkbook wb = new org.apache.poi.xssf.usermodel.XSSFWorkbook()) {
            org.apache.poi.ss.usermodel.Sheet sheet = wb.createSheet("Students");

            // Bold style for header
            org.apache.poi.ss.usermodel.CellStyle headerStyle = wb.createCellStyle();
            org.apache.poi.ss.usermodel.Font poiFont = wb.createFont();
            poiFont.setBold(true);
            headerStyle.setFont(poiFont);

            // Header row
            org.apache.poi.ss.usermodel.Row header = sheet.createRow(0);
            for (int c = 0; c < tableModel.getColumnCount(); c++) {
                org.apache.poi.ss.usermodel.Cell cell = header.createCell(c);
                cell.setCellValue(tableModel.getColumnName(c));
                cell.setCellStyle(headerStyle);
            }

            // Data rows
            for (int r = 0; r < tableModel.getRowCount(); r++) {
                org.apache.poi.ss.usermodel.Row dataRow = sheet.createRow(r + 1);
                for (int c = 0; c < tableModel.getColumnCount(); c++) {
                    Object val = tableModel.getValueAt(r, c);
                    org.apache.poi.ss.usermodel.Cell cell = dataRow.createCell(c);
                    if (val instanceof Number n)
                        cell.setCellValue(n.doubleValue());
                    else
                        cell.setCellValue(val == null ? "" : val.toString());
                }
            }

            // Auto-size all columns
            for (int c = 0; c < tableModel.getColumnCount(); c++)
                sheet.autoSizeColumn(c);

            try (FileOutputStream fos = new FileOutputStream(filename)) {
                wb.write(fos);
            }
            JOptionPane.showMessageDialog(this, "Exported to " + filename);
        } catch (IOException e) {
            showErr("Export failed: " + e.getMessage());
        }
    }

    // ── STATS panel
    // ───────────────────────────────────────────────────────────────
    private JPanel buildStatsPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));

        JLabel title = new JLabel("Performance Analytics");
        title.setFont(new Font("Segoe UI", Font.BOLD, 24));
        panel.add(title, BorderLayout.NORTH);

        JPanel cards = new JPanel(new GridLayout(1, 3, 20, 0));
        lblTotal = statCard(cards, "Total Students", "0");
        lblAvg = statCard(cards, "Class Average", "0.0");
        lblTopper = statCard(cards, "Top Performer", "–");
        panel.add(cards, BorderLayout.CENTER);

        chartContainer = new JPanel(new BorderLayout());
        chartContainer.setPreferredSize(new Dimension(800, 320));
        panel.add(chartContainer, BorderLayout.SOUTH);
        return panel;
    }

    private JLabel statCard(JPanel parent, String title, String value) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(new Color(50, 54, 57));
        card.setBorder(BorderFactory.createLineBorder(new Color(80, 80, 80)));
        JLabel lTitle = new JLabel(title, SwingConstants.CENTER);
        lTitle.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lTitle.setForeground(Color.LIGHT_GRAY);
        lTitle.setBorder(new EmptyBorder(12, 0, 0, 0));
        card.add(lTitle, BorderLayout.NORTH);
        JLabel lVal = new JLabel(value, SwingConstants.CENTER);
        lVal.setFont(new Font("Segoe UI", Font.BOLD, 26));
        lVal.setForeground(new Color(100, 180, 255));
        card.add(lVal, BorderLayout.CENTER);
        parent.add(card);
        return lVal;
    }

    private void updateStats() {
        try {
            List<Map<String, Object>> records = db.getAllRecords();
            if (records == null || records.isEmpty()) {
                lblTotal.setText("0");
                lblAvg.setText("0.0");
                lblTopper.setText("–");
                return;
            }
            int count = records.size();
            double grandAvgSum = 0;
            double maxAvg = -1;
            String topperName = "–";
            Map<String, Double> subjSums = new LinkedHashMap<>();
            Map<String, Integer> subjCnts = new LinkedHashMap<>();
            for (String s : SUBJECTS) {
                subjSums.put(s, 0.0);
                subjCnts.put(s, 0);
            }

            for (Map<String, Object> r : records) {
                double sum = 0;
                int n = 0;
                for (String sub : SUBJECTS) {
                    Object v = r.get(sub);
                    if (v instanceof Number num) {
                        double d = num.doubleValue();
                        sum += d;
                        n++;
                        subjSums.merge(sub, d, Double::sum);
                        subjCnts.merge(sub, 1, Integer::sum);
                    }
                }
                if (n > 0) {
                    double avg = sum / n;
                    grandAvgSum += avg;
                    if (avg > maxAvg) {
                        maxAvg = avg;
                        topperName = (String) r.get("NAME");
                    }
                }
            }

            lblTotal.setText(String.valueOf(count));
            lblAvg.setText(String.format("%.2f", grandAvgSum / count));
            lblTopper.setText(topperName);

            DefaultCategoryDataset dataset = new DefaultCategoryDataset();
            for (String sub : SUBJECTS) {
                int c = subjCnts.get(sub);
                dataset.addValue(c > 0 ? subjSums.get(sub) / c : 0, "Average", sub);
            }

            JFreeChart chart = ChartFactory.createBarChart(
                    "Average Score per Subject", "Subject", "Score",
                    dataset, PlotOrientation.VERTICAL, false, true, false);

            chart.setBackgroundPaint(new Color(33, 37, 41));
            CategoryPlot plot = chart.getCategoryPlot();
            plot.setBackgroundPaint(new Color(43, 43, 43));
            plot.setDomainGridlinePaint(new Color(80, 80, 80));
            plot.setRangeGridlinePaint(new Color(80, 80, 80));
            plot.getRangeAxis().setRange(0, 100);
            plot.getRangeAxis().setTickLabelPaint(Color.WHITE);
            plot.getRangeAxis().setLabelPaint(Color.WHITE);
            plot.getDomainAxis().setTickLabelPaint(Color.WHITE);
            plot.getDomainAxis().setLabelPaint(Color.WHITE);
            chart.getTitle().setPaint(Color.WHITE);

            BarRenderer renderer = (BarRenderer) plot.getRenderer();
            renderer.setSeriesPaint(0, new Color(31, 83, 200));
            renderer.setDefaultItemLabelGenerator(new StandardCategoryItemLabelGenerator());
            renderer.setDefaultItemLabelsVisible(true);
            renderer.setDefaultItemLabelPaint(Color.WHITE);
            renderer.setDefaultItemLabelFont(new Font("Segoe UI", Font.BOLD, 11));

            chartContainer.removeAll();
            ChartPanel cp = new ChartPanel(chart);
            cp.setBackground(new Color(43, 43, 43));
            chartContainer.add(cp, BorderLayout.CENTER);
            chartContainer.revalidate();
            chartContainer.repaint();

        } catch (SQLException e) {
            showErr("Stats error: " + e.getMessage());
        }
    }

    private void showErr(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Error", JOptionPane.ERROR_MESSAGE);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new StudentAppV2().setVisible(true));
    }
}
