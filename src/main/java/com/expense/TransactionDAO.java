
package com.expense;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TransactionDAO {

    // Add a new transaction
    public void addTransaction(Transaction t) throws SQLException {
        String sql = """
            INSERT INTO transactions
            (type, category, amount, transaction_date, note)
            VALUES (?, ?, ?, ?, ?)
            """;

        try (Connection conn = Database.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, t.getType());
            ps.setString(2, t.getCategory());
            ps.setDouble(3, t.getAmount());
            ps.setString(4, t.getDate());
            ps.setString(5, t.getNote());

            ps.executeUpdate();
        }
    }

    // Retrieve all transactions
    public List<Transaction> getAllTransactions() throws SQLException {
        return searchTransactions("", "ALL", "ALL", "", "");
    }

    // Search and filter transactions
    public List<Transaction> searchTransactions(
            String search,
            String type,
            String category,
            String fromDate,
            String toDate) throws SQLException {

        List<Transaction> transactions = new ArrayList<>();

        StringBuilder sql = new StringBuilder("""
            SELECT * FROM transactions
            WHERE 1 = 1
            """);

        List<Object> parameters = new ArrayList<>();

        if (search != null && !search.isBlank()) {
            sql.append("""
                AND (
                    LOWER(category) LIKE LOWER(?)
                    OR LOWER(COALESCE(note, '')) LIKE LOWER(?)
                    OR LOWER(type) LIKE LOWER(?)
                )
                """);

            String keyword = "%" + search.trim() + "%";
            parameters.add(keyword);
            parameters.add(keyword);
            parameters.add(keyword);
        }

        if (type != null && !type.equals("ALL")) {
            sql.append(" AND type = ?");
            parameters.add(type);
        }

        if (category != null && !category.equals("ALL")) {
            sql.append(" AND category = ?");
            parameters.add(category);
        }

        if (fromDate != null && !fromDate.isBlank()) {
            sql.append(" AND transaction_date >= ?");
            parameters.add(fromDate);
        }

        if (toDate != null && !toDate.isBlank()) {
            sql.append(" AND transaction_date <= ?");
            parameters.add(toDate);
        }

        sql.append(" ORDER BY transaction_date DESC, id DESC");

        try (Connection conn = Database.connect();
             PreparedStatement ps =
                     conn.prepareStatement(sql.toString())) {

            for (int i = 0; i < parameters.size(); i++) {
                ps.setObject(i + 1, parameters.get(i));
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    transactions.add(new Transaction(
                        rs.getInt("id"),
                        rs.getString("type"),
                        rs.getString("category"),
                        rs.getDouble("amount"),
                        rs.getString("transaction_date"),
                        rs.getString("note")
                    ));
                }
            }
        }

        return transactions;
    }

    // Update an existing transaction
    public void updateTransaction(int id, Transaction t)
            throws SQLException {

        String sql = """
            UPDATE transactions
            SET type = ?,
                category = ?,
                amount = ?,
                transaction_date = ?,
                note = ?
            WHERE id = ?
            """;

        try (Connection conn = Database.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, t.getType());
            ps.setString(2, t.getCategory());
            ps.setDouble(3, t.getAmount());
            ps.setString(4, t.getDate());
            ps.setString(5, t.getNote());
            ps.setInt(6, id);

            ps.executeUpdate();
        }
    }

    // Delete a transaction by its database ID
    public void deleteTransaction(int id) throws SQLException {
        String sql = "DELETE FROM transactions WHERE id = ?";

        try (Connection conn = Database.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    // Return {total income, total expenses}
    public double[] getTotals() throws SQLException {
        String sql = """
            SELECT
                COALESCE(SUM(
                    CASE WHEN type = 'INCOME' THEN amount ELSE 0 END
                ), 0) AS total_income,
                COALESCE(SUM(
                    CASE WHEN type = 'EXPENSE' THEN amount ELSE 0 END
                ), 0) AS total_expenses
            FROM transactions
            """;

        try (Connection conn = Database.connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            if (rs.next()) {
                return new double[] {
                    rs.getDouble("total_income"),
                    rs.getDouble("total_expenses")
                };
            }
        }

        return new double[] {0, 0};
    }

    // Return {monthly income, monthly expenses}
    // month must be in YYYY-MM format
    public double[] getMonthlyTotals(String month)
            throws SQLException {

        String sql = """
            SELECT
                COALESCE(SUM(
                    CASE WHEN type = 'INCOME' THEN amount ELSE 0 END
                ), 0) AS total_income,
                COALESCE(SUM(
                    CASE WHEN type = 'EXPENSE' THEN amount ELSE 0 END
                ), 0) AS total_expenses
            FROM transactions
            WHERE transaction_date >= ?
              AND transaction_date < ?
            """;

        String nextMonth = java.time.YearMonth.parse(month)
                .plusMonths(1).toString();

        try (Connection conn = Database.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, month + "-01");
            ps.setString(2, nextMonth + "-01");

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new double[] {
                        rs.getDouble("total_income"),
                        rs.getDouble("total_expenses")
                    };
                }
            }
        }

        return new double[] {0, 0};
    }
}
