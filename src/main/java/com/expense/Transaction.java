
package com.expense;

public class Transaction {
    private final int id;
    private final String type;
    private final String category;
    private final double amount;
    private final String date;
    private final String note;

    public Transaction(int id, String type, String category,
                       double amount, String date, String note) {
        this.id = id;
        this.type = type;
        this.category = category;
        this.amount = amount;
        this.date = date;
        this.note = note;
    }

    public int getId() { return id; }
    public String getType() { return type; }
    public String getCategory() { return category; }
    public double getAmount() { return amount; }
    public String getDate() { return date; }
    public String getNote() { return note; }
}
