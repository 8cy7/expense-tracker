package com.expensetracker.model;

import java.time.LocalDate;

/**
 * Mutable ADT representing a single financial transaction (income or expense).
 *
 * <p>Representation Invariant (RI):
 * - id != null
 * - amount > 0
 * - category != null && !category.isEmpty()
 * - date != null
 * - description != null (can be empty string)
 * - type is either "INCOME" or "EXPENSE"
 *
 * <p>Abstraction Function (AF):
 * - AF(this) = a single financial record uniquely identified by 'id',
 *   with a monetary 'amount' (always positive), belonging to a 'category',
 *   occurring on 'date', described by 'description', and classified
 *   as either income or expense based on 'type'.
 */
public class Transaction {

    /** Unique identifier — immutable once set */
    private final TransactionID id;

    /** Monetary amount — always positive */
    private double amount;

    /** Category label (e.g., Food, Transport, Salary) */
    private String category;

    /** Date of the transaction */
    private LocalDate date;

    /** Optional description / note */
    private String description;

    /** Transaction type: "INCOME" or "EXPENSE" */
    private String type;

    /**
     * Constructs a new Transaction.
     *
     * @param amount      the monetary amount; must be > 0
     * @param category    the category; must not be null or empty
     * @param date        the date; must not be null
     * @param description a textual note; may be empty but not null
     * @param type        "INCOME" or "EXPENSE"
     * @throws IllegalArgumentException if any argument is invalid
     */
    public Transaction(double amount, String category, LocalDate date,
                       String description, String type) {
        this.id = new TransactionID();
        setAmount(amount);
        setCategory(category);
        setDate(date);
        setDescription(description);
        setType(type);
        checkRep();
    }

    /**
     * Constructs a Transaction with a specific ID (used when loading from storage).
     *
     * @param id          the existing TransactionID
     * @param amount      the monetary amount; must be > 0
     * @param category    the category; must not be null or empty
     * @param date        the date; must not be null
     * @param description a textual note; may be empty but not null
     * @param type        "INCOME" or "EXPENSE"
     */
    public Transaction(TransactionID id, double amount, String category,
                       LocalDate date, String description, String type) {
        this.id = id;
        setAmount(amount);
        setCategory(category);
        setDate(date);
        setDescription(description);
        setType(type);
        checkRep();
    }

    // ── Getters ──────────────────────────────────────────────────────────────

    /** @return the unique identifier of this transaction */
    public TransactionID getId() { return id; }

    /** @return the monetary amount (always positive) */
    public double getAmount() { return amount; }

    /** @return the category label */
    public String getCategory() { return category; }

    /** @return the date this transaction occurred */
    public LocalDate getDate() { return date; }

    /** @return the descriptive note */
    public String getDescription() { return description; }

    /** @return "INCOME" or "EXPENSE" */
    public String getType() { return type; }

    /** @return true if this is an income transaction */
    public boolean isIncome() { return "INCOME".equals(type); }

    /** @return true if this is an expense transaction */
    public boolean isExpense() { return "EXPENSE".equals(type); }

    // ── Setters (mutation) ───────────────────────────────────────────────────

    /**
     * Updates the monetary amount.
     *
     * @param amount must be > 0
     * @throws IllegalArgumentException if amount <= 0
     */
    public void setAmount(double amount) {
        if (amount <= 0) throw new IllegalArgumentException("Amount must be > 0");
        this.amount = amount;
        checkRep();
    }

    /**
     * Updates the category.
     *
     * @param category must not be null or empty
     */
    public void setCategory(String category) {
        if (category == null || category.isEmpty())
            throw new IllegalArgumentException("Category must not be null or empty");
        this.category = category;
        checkRep();
    }

    /**
     * Updates the date.
     *
     * @param date must not be null
     */
    public void setDate(LocalDate date) {
        if (date == null) throw new IllegalArgumentException("Date must not be null");
        this.date = date;
        checkRep();
    }

    /**
     * Updates the description.
     *
     * @param description must not be null (can be empty)
     */
    public void setDescription(String description) {
        if (description == null) throw new IllegalArgumentException("Description must not be null");
        this.description = description;
        checkRep();
    }

    /**
     * Updates the transaction type.
     *
     * @param type must be "INCOME" or "EXPENSE"
     */
    public void setType(String type) {
        if (!"INCOME".equals(type) && !"EXPENSE".equals(type))
            throw new IllegalArgumentException("Type must be INCOME or EXPENSE");
        this.type = type;
        checkRep();
    }

    // ── Rep check ────────────────────────────────────────────────────────────

    /** Verifies the Representation Invariant holds. */
    private void checkRep() {
        assert id != null : "RI violated: id is null";
        assert amount > 0 : "RI violated: amount must be > 0";
        assert category != null && !category.isEmpty() : "RI violated: category is null/empty";
        assert date != null : "RI violated: date is null";
        assert description != null : "RI violated: description is null";
        assert "INCOME".equals(type) || "EXPENSE".equals(type) : "RI violated: invalid type";
    }

    @Override
    public String toString() {
        return String.format("[%s] %s | %.2f SAR | %s | %s | %s",
                id, type, amount, category, date, description);
    }
}
