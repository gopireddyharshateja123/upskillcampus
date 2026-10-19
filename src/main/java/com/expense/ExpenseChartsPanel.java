
package com.expense;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PiePlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DefaultPieDataset;

import javax.swing.*;
import java.awt.*;
import java.sql.SQLException;
import java.time.YearMonth;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.List;

public class ExpenseChartsPanel extends JPanel {

    private final TransactionDAO dao = new TransactionDAO();

    private final DefaultPieDataset<String> pieDataset =
            new DefaultPieDataset<>();

    private final DefaultCategoryDataset barDataset =
            new DefaultCategoryDataset();

    private final JFreeChart pieChart;
    private final JFreeChart barChart;

    public ExpenseChartsPanel() {
        setLayout(new GridLayout(1, 2, 12, 0));
        setBackground(new Color(5, 16, 35));
        setPreferredSize(new Dimension(800, 285));

        pieChart = ChartFactory.createRingChart(
                "Expenses by Category",
                pieDataset,
                true,
                true,
                false
        );

        barChart = ChartFactory.createBarChart(
                "Monthly Income vs Expenses",
                "Month",
                "Amount",
                barDataset,
                PlotOrientation.VERTICAL,
                true,
                true,
                false
        );

        stylePieChart();
        styleBarChart();

        ChartPanel piePanel = new ChartPanel(pieChart);
        ChartPanel barPanel = new ChartPanel(barChart);

        styleChartPanel(piePanel);
        styleChartPanel(barPanel);

        add(piePanel);
        add(barPanel);

        refreshCharts();
    }

    private void styleChartPanel(ChartPanel panel) {
        panel.setBackground(new Color(8, 22, 45));
        panel.setBorder(BorderFactory.createLineBorder(
                new Color(28, 68, 115)));
        panel.setMouseWheelEnabled(true);
    }

    private void stylePieChart() {
        pieChart.setBackgroundPaint(new Color(8, 22, 45));
        pieChart.getTitle().setPaint(new Color(235, 242, 255));
        pieChart.getTitle().setFont(
                new Font("SansSerif", Font.BOLD, 15));

        PiePlot<?> plot = (PiePlot<?>) pieChart.getPlot();
        plot.setBackgroundPaint(new Color(8, 22, 45));
        plot.setOutlineVisible(false);
        plot.setLabelPaint(new Color(225, 235, 250));
        plot.setLabelBackgroundPaint(new Color(8, 22, 45));
        plot.setSectionPaint("Food", new Color(30, 144, 255));
        plot.setSectionPaint("Transport", new Color(255, 65, 125));
        plot.setSectionPaint("Bills", new Color(180, 90, 245));
        plot.setSectionPaint("Shopping", new Color(255, 190, 55));
        plot.setSectionPaint("Education", new Color(255, 130, 65));
        plot.setSectionPaint("Other", new Color(20, 200, 170));
    }

    private void styleBarChart() {
        barChart.setBackgroundPaint(new Color(8, 22, 45));
        barChart.getTitle().setPaint(new Color(235, 242, 255));
        barChart.getTitle().setFont(
                new Font("SansSerif", Font.BOLD, 15));

        CategoryPlot plot = barChart.getCategoryPlot();
        plot.setBackgroundPaint(new Color(8, 22, 45));
        plot.setOutlineVisible(false);
        plot.setRangeGridlinePaint(new Color(35, 55, 85));
        plot.getDomainAxis().setTickLabelPaint(
                new Color(215, 228, 248));
        plot.getDomainAxis().setLabelPaint(
                new Color(215, 228, 248));
        plot.getRangeAxis().setTickLabelPaint(
                new Color(215, 228, 248));
        plot.getRangeAxis().setLabelPaint(
                new Color(215, 228, 248));

        BarRenderer renderer = (BarRenderer) plot.getRenderer();
        renderer.setSeriesPaint(0, new Color(20, 205, 165));
        renderer.setSeriesPaint(1, new Color(255, 65, 125));
        renderer.setDrawBarOutline(false);
    }

    public void refreshCharts() {
        try {
            List<Transaction> transactions = dao.getAllTransactions();

            pieDataset.clear();
            barDataset.clear();

            Map<String, Double> expensesByCategory =
                    new LinkedHashMap<>();

            Map<YearMonth, double[]> monthlyTotals =
                    new LinkedHashMap<>();

            for (Transaction t : transactions) {
                if ("EXPENSE".equals(t.getType())) {
                    expensesByCategory.merge(
                            t.getCategory(),
                            t.getAmount(),
                            Double::sum
                    );
                }

                YearMonth month = YearMonth.parse(
                        t.getDate().substring(0, 7));

                double[] totals = monthlyTotals.computeIfAbsent(
                        month, key -> new double[2]);

                if ("INCOME".equals(t.getType())) {
                    totals[0] += t.getAmount();
                } else if ("EXPENSE".equals(t.getType())) {
                    totals[1] += t.getAmount();
                }
            }

            for (Map.Entry<String, Double> entry
                    : expensesByCategory.entrySet()) {
                pieDataset.setValue(
                        entry.getKey(), entry.getValue());
            }

            // Show the latest 12 months, including months with no activity.
            YearMonth currentMonth = YearMonth.now();

            for (int i = 11; i >= 0; i--) {
                YearMonth month = currentMonth.minusMonths(i);
                double[] totals = monthlyTotals.getOrDefault(
                        month, new double[2]);

                String label = month.toString();

                barDataset.addValue(
                        totals[0], "Income", label);
                barDataset.addValue(
                        totals[1], "Expenses", label);
            }

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(
                    this,
                    "Could not load chart data: " + ex.getMessage(),
                    "Chart Error",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }
}
