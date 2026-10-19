
package com.expense;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

public class Main {
    public static void main(String[] args) {
        Database.initialize();

        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(
                        UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {
                // Use Swing's default appearance if unavailable.
            }

            new ExpenseTrackerUI().setVisible(true);
        });
    }
}
