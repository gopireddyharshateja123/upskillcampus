
package com.expense;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeParseException;
import java.util.List;

public class ExpenseTrackerUI extends JFrame {

    private final TransactionDAO dao = new TransactionDAO();
    private final ExpenseChartsPanel chartsPanel =
        new ExpenseChartsPanel();
    private Integer editingId = null;

    private final JComboBox<String> typeBox =
            new JComboBox<>(new String[]{"EXPENSE", "INCOME"});

    private final JComboBox<String> categoryBox =
            new JComboBox<>(new String[]{
                "Food", "Transport", "Education",
                "Shopping", "Bills", "Salary", "Other"
            });

    private final JTextField amountField = new JTextField(12);
    private final JTextField dateField =
            new JTextField(LocalDate.now().toString(), 12);
    private final JTextField noteField = new JTextField(12);

    // Search and filter controls
    private final JTextField searchField = new JTextField(12);

    private final JComboBox<String> typeFilter =
            new JComboBox<>(new String[]{
                "ALL", "EXPENSE", "INCOME"
            });

    private final JComboBox<String> categoryFilter =
            new JComboBox<>(new String[]{
                "ALL", "Food", "Transport", "Education",
                "Shopping", "Bills", "Salary", "Other"
            });

    private final JTextField fromDateField = new JTextField(10);
    private final JTextField toDateField = new JTextField(10);

    // Monthly report control
    private final JTextField monthField =
            new JTextField(YearMonth.now().toString(), 7);

    private final JLabel monthlyReportLabel =
            new JLabel("Select a month and click Monthly Report.");

    // Dashboard labels
    private final JLabel incomeLabel = new JLabel("Income: 0.00");
    private final JLabel expenseLabel = new JLabel("Expenses: 0.00");
    private final JLabel balanceLabel = new JLabel("Balance: 0.00");

    private final DefaultTableModel tableModel =
            new DefaultTableModel(
                new String[]{
                    "ID", "Type", "Category",
                    "Amount", "Date", "Note"
                }, 0) {

                @Override
                public boolean isCellEditable(int row, int column) {
                    return false;
                }
            };

    private final JTable table = new JTable(tableModel);

    private final JButton saveButton = new JButton("Add Transaction");

    public ExpenseTrackerUI() {
        setTitle("Personal Expense Tracker");
        setSize(1250, 720);
        setMinimumSize(new Dimension(950, 600));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        setLayout(new BorderLayout(10, 10));

        add(createHeaderPanel(), BorderLayout.NORTH);
        add(createFormPanel(), BorderLayout.WEST);
        add(createTablePanel(), BorderLayout.CENTER);

        saveButton.addActionListener(e -> saveTransaction());

        refreshAll();
    }

    // Heading, dashboard, search and filters
    private JPanel createHeaderPanel() {
        JPanel header = new JPanel();
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        header.setBorder(BorderFactory.createEmptyBorder(10, 10, 0, 10));

        JLabel heading = new JLabel("Personal Expense Tracker");
        heading.setFont(new Font("SansSerif", Font.BOLD, 25));
        heading.setAlignmentX(Component.CENTER_ALIGNMENT);

        JPanel dashboard = new JPanel(new GridLayout(1, 3, 12, 5));
        dashboard.setBorder(
                BorderFactory.createTitledBorder("Dashboard"));

        incomeLabel.setForeground(new Color(0, 130, 60));
        expenseLabel.setForeground(new Color(190, 50, 50));
        balanceLabel.setFont(new Font("SansSerif", Font.BOLD, 15));

        dashboard.add(incomeLabel);
        dashboard.add(expenseLabel);
        dashboard.add(balanceLabel);

        JPanel filters = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 5));
        filters.setBorder(
                BorderFactory.createTitledBorder("Search and Filters"));

        JButton applyButton = new JButton("Apply Filters");
        JButton resetButton = new JButton("Reset Filters");

        filters.add(new JLabel("Search:"));
        filters.add(searchField);
        filters.add(new JLabel("Type:"));
        filters.add(typeFilter);
        filters.add(new JLabel("Category:"));
        filters.add(categoryFilter);
        filters.add(new JLabel("From (YYYY-MM-DD):"));
        filters.add(fromDateField);
        filters.add(new JLabel("To:"));
        filters.add(toDateField);
        filters.add(applyButton);
        filters.add(resetButton);

        applyButton.addActionListener(e -> applyFilters());
        resetButton.addActionListener(e -> resetFilters());

        header.add(heading);
        header.add(Box.createVerticalStrut(8));
        header.add(dashboard);
        header.add(filters);

        return header;
    }

    // Transaction entry form
    private JPanel createFormPanel() {
        JPanel form = new JPanel(new GridLayout(0, 2, 8, 10));
        form.setBorder(
                BorderFactory.createTitledBorder("Transaction Details"));
        form.setPreferredSize(new Dimension(300, 0));

        form.add(new JLabel("Type:"));
        form.add(typeBox);

        form.add(new JLabel("Category:"));
        form.add(categoryBox);

        form.add(new JLabel("Amount:"));
        form.add(amountField);

        form.add(new JLabel("Date (YYYY-MM-DD):"));
        form.add(dateField);

        form.add(new JLabel("Note:"));
        form.add(noteField);

        JButton editButton = new JButton("Load Selected to Edit");
        JButton deleteButton = new JButton("Delete Selected");
        JButton clearButton = new JButton("Clear Form");
        JButton refreshButton = new JButton("Refresh");

        form.add(saveButton);
        form.add(editButton);
        form.add(deleteButton);
        form.add(clearButton);
        form.add(refreshButton);

        editButton.addActionListener(e -> loadSelectedForEdit());
        deleteButton.addActionListener(e -> deleteSelected());
        clearButton.addActionListener(e -> clearForm());
        refreshButton.addActionListener(e -> refreshAll());

        return form;
    }

    // Transaction table and monthly report
    private JPanel createTablePanel() {
        JPanel panel = new JPanel(new BorderLayout(5, 8));

        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setRowHeight(25);
        table.setAutoCreateRowSorter(true);

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(
                BorderFactory.createTitledBorder("Transaction History"));

        JPanel monthlyPanel =
                new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));

        monthlyPanel.setBorder(
                BorderFactory.createTitledBorder("Monthly Report"));

        JButton reportButton = new JButton("Monthly Report");

        monthlyPanel.add(new JLabel("Month (YYYY-MM):"));
        monthlyPanel.add(monthField);
        monthlyPanel.add(reportButton);
        monthlyPanel.add(monthlyReportLabel);

        reportButton.addActionListener(e -> showMonthlyReport());

        panel.add(scrollPane, BorderLayout.CENTER);
        panel.add(monthlyPanel, BorderLayout.SOUTH);

        return panel;
    }

    // Validate and add/update transaction
    private void saveTransaction() {
        try {
            String type = (String) typeBox.getSelectedItem();
            String category = (String) categoryBox.getSelectedItem();
            String amountText = amountField.getText().trim();
            String date = dateField.getText().trim();
            String note = noteField.getText().trim();

            if (type == null || category == null) {
                showError("Select a transaction type and category.");
                return;
            }

            if (amountText.isEmpty()) {
                showError("Amount is required.");
                return;
            }

            double amount = Double.parseDouble(amountText);

            if (!Double.isFinite(amount) || amount <= 0) {
                showError("Amount must be a positive number.");
                return;
            }

            if (date.isEmpty()) {
                showError("Date is required.");
                return;
            }

            LocalDate.parse(date);

            Transaction transaction = new Transaction(
                    editingId == null ? 0 : editingId,
                    type,
                    category,
                    amount,
                    date,
                    note
            );

            if (editingId == null) {
                dao.addTransaction(transaction);

                JOptionPane.showMessageDialog(
                        this, "Transaction added successfully.");
            } else {
                dao.updateTransaction(editingId, transaction);

                JOptionPane.showMessageDialog(
                        this, "Transaction updated successfully.");
            }

            clearForm();
            refreshAll();

        } catch (NumberFormatException ex) {
            showError("Enter a valid amount, such as 250 or 250.50.");
        } catch (DateTimeParseException ex) {
            showError("Enter a valid date in YYYY-MM-DD format.");
        } catch (SQLException ex) {
            showError("Database error: " + ex.getMessage());
        }
    }

    // Load the selected row into the form for editing
    private void loadSelectedForEdit() {
        int viewRow = table.getSelectedRow();

        if (viewRow == -1) {
            showError("Select a transaction from the table first.");
            return;
        }

        int row = table.convertRowIndexToModel(viewRow);

        editingId = Integer.parseInt(
                tableModel.getValueAt(row, 0).toString());

        typeBox.setSelectedItem(
                tableModel.getValueAt(row, 1).toString());

        categoryBox.setSelectedItem(
                tableModel.getValueAt(row, 2).toString());

        amountField.setText(
                tableModel.getValueAt(row, 3).toString());

        dateField.setText(
                tableModel.getValueAt(row, 4).toString());

        Object note = tableModel.getValueAt(row, 5);
        noteField.setText(note == null ? "" : note.toString());

        saveButton.setText("Update Transaction");
    }

    // Delete with confirmation
    private void deleteSelected() {
        int viewRow = table.getSelectedRow();

        if (viewRow == -1) {
            showError("Select a transaction from the table first.");
            return;
        }

        int row = table.convertRowIndexToModel(viewRow);

        int id = Integer.parseInt(
                tableModel.getValueAt(row, 0).toString());

        String category =
                tableModel.getValueAt(row, 2).toString();

        int choice = JOptionPane.showConfirmDialog(
                this,
                "Are you sure you want to delete the "
                        + category + " transaction (ID " + id + ")?",
                "Confirm Deletion",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);

        if (choice != JOptionPane.YES_OPTION) {
            return;
        }

        try {
            dao.deleteTransaction(id);

            if (editingId != null && editingId == id) {
                clearForm();
            }

            refreshAll();

            JOptionPane.showMessageDialog(
                    this, "Transaction deleted successfully.");

        } catch (SQLException ex) {
            showError("Could not delete transaction: "
                    + ex.getMessage());
        }
    }

    // Apply search and filters
    private void applyFilters() {
        try {
            String fromDate = fromDateField.getText().trim();
            String toDate = toDateField.getText().trim();

            if (!fromDate.isEmpty()) {
                LocalDate.parse(fromDate);
            }

            if (!toDate.isEmpty()) {
                LocalDate.parse(toDate);
            }

            if (!fromDate.isEmpty() && !toDate.isEmpty()
                    && LocalDate.parse(fromDate)
                    .isAfter(LocalDate.parse(toDate))) {
                showError("The From date must not be after the To date.");
                return;
            }

            List<Transaction> transactions = dao.searchTransactions(
                    searchField.getText().trim(),
                    (String) typeFilter.getSelectedItem(),
                    (String) categoryFilter.getSelectedItem(),
                    fromDate,
                    toDate
            );

            displayTransactions(transactions);

        } catch (DateTimeParseException ex) {
            showError("Filter dates must use YYYY-MM-DD format.");
        } catch (SQLException ex) {
            showError("Could not filter transactions: "
                    + ex.getMessage());
        }
    }

    // Reset all search controls
    private void resetFilters() {
        searchField.setText("");
        typeFilter.setSelectedItem("ALL");
        categoryFilter.setSelectedItem("ALL");
        fromDateField.setText("");
        toDateField.setText("");

        applyFilters();
    }

    // Refresh dashboard and table
    private void refreshAll() {
        loadDashboard();
        applyFilters();
    }

    private void loadDashboard() {
        try {
            double[] totals = dao.getTotals();

            double income = totals[0];
            double expenses = totals[1];
            double balance = income - expenses;

            incomeLabel.setText(String.format(
                    "Total Income: %.2f", income));

            expenseLabel.setText(String.format(
                    "Total Expenses: %.2f", expenses));

            balanceLabel.setText(String.format(
                    "Balance: %.2f", balance));

        } catch (SQLException ex) {
            showError("Could not load dashboard: " + ex.getMessage());
        }
    }

    private void displayTransactions(List<Transaction> transactions) {
        tableModel.setRowCount(0);

        for (Transaction t : transactions) {
            tableModel.addRow(new Object[]{
                t.getId(),
                t.getType(),
                t.getCategory(),
                String.format("%.2f", t.getAmount()),
                t.getDate(),
                t.getNote()
            });
        }
    }

    // Generate a report for a selected month
    private void showMonthlyReport() {
        try {
            String month = monthField.getText().trim();

            YearMonth.parse(month);

            double[] totals = dao.getMonthlyTotals(month);

            double income = totals[0];
            double expenses = totals[1];
            double balance = income - expenses;

            monthlyReportLabel.setText(String.format(
                    "Income: %.2f | Expenses: %.2f | Balance: %.2f",
                    income, expenses, balance));

        } catch (DateTimeParseException ex) {
            showError("Enter the month in YYYY-MM format, such as 2026-10.");
        } catch (SQLException ex) {
            showError("Could not generate monthly report: "
                    + ex.getMessage());
        }
    }

    // Reset the transaction form
    private void clearForm() {
        editingId = null;

        typeBox.setSelectedItem("EXPENSE");
        categoryBox.setSelectedItem("Food");
        amountField.setText("");
        dateField.setText(LocalDate.now().toString());
        noteField.setText("");

        saveButton.setText("Add Transaction");
        table.clearSelection();
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(
                this,
                message,
                "Expense Tracker",
                JOptionPane.ERROR_MESSAGE
        );
    }
}
