package com.expensetracker.controller;

import com.expensetracker.model.*;
import com.expensetracker.storage.DataStorageManager;
import com.expensetracker.util.CsvExporter;
import com.expensetracker.util.TransactionParser;

import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;

/**
 * Controller in the MVC pattern.
 *
 * <p>Listens for user actions (from the View) and calls the appropriate
 * methods on the Model (TransactionList). The View never modifies the
 * Model directly — all mutations go through this Controller.
 *
 * <p>The Controller also coordinates persistence by calling
 * {@link DataStorageManager} after every state change.
 */
public class MainController {

    private final TransactionList model;
    private final DataStorageManager storage;

    /**
     * Creates a new MainController.
     *
     * @param model   the TransactionList (Model); must not be null
     */
    public MainController(TransactionList model) {
        this.model   = model;
        this.storage = DataStorageManager.getInstance();
    }

    // ── Transaction Operations ────────────────────────────────────────────────

    /**
     * Adds a new transaction and persists the change.
     *
     * @param amount      must be > 0
     * @param category    must not be null or empty
     * @param date        must not be null
     * @param description may be empty
     * @param type        "INCOME" or "EXPENSE"
     */
    public void addTransaction(double amount, String category, LocalDate date,
                                String description, String type) {
        Transaction t = new Transaction(amount, category, date, description, type);
        model.addTransaction(t);
        storage.save(model);
    }

    /**
     * Adds a transaction using the quick-add regex format.
     * Example: "Lunch 25 #food 19/10/2025"
     *
     * @param quickInput the quick-add string
     * @param type       "INCOME" or "EXPENSE"
     * @return true if parsed and added successfully, false if format is invalid
     */
    public boolean addTransactionQuick(String quickInput, String type) {
        TransactionParser.ParseResult result = TransactionParser.parse(quickInput);
        if (result == null) return false;
        addTransaction(result.amount, result.category, result.date,
                result.description, type);
        return true;
    }

    /**
     * Deletes a transaction by its ID and persists the change.
     *
     * @param id the ID of the transaction to delete
     * @return true if deleted, false if not found
     */
    public boolean deleteTransaction(TransactionID id) {
        boolean removed = model.removeTransaction(id);
        if (removed) storage.save(model);
        return removed;
    }

    /**
     * Updates an existing transaction and persists the change.
     *
     * @param id          the ID of the transaction to update
     * @param amount      new amount
     * @param category    new category
     * @param date        new date
     * @param description new description
     * @param type        new type
     */
    public void updateTransaction(TransactionID id, double amount, String category,
                                   LocalDate date, String description, String type) {
        model.updateTransaction(id, amount, category, date, description, type);
        storage.save(model);
    }

    // ── Search & Filter ───────────────────────────────────────────────────────

    /**
     * Searches transactions by keyword.
     *
     * @param keyword the search term
     * @return matching transactions
     */
    public List<Transaction> search(String keyword) {
        return model.search(keyword);
    }

    /**
     * Filters transactions by date range.
     *
     * @param from start date (inclusive)
     * @param to   end date (inclusive)
     * @return matching transactions
     */
    public List<Transaction> filterByDateRange(LocalDate from, LocalDate to) {
        return model.getByDateRange(from, to);
    }

    /**
     * Filters transactions by category.
     *
     * @param category the category to filter
     * @return matching transactions
     */
    public List<Transaction> filterByCategory(String category) {
        return model.getByCategory(category);
    }

    // ── Budget ────────────────────────────────────────────────────────────────

    /**
     * Sets the monthly budget and persists the change.
     *
     * @param budget the budget amount; must be >= 0
     */
    public void setMonthlyBudget(double budget) {
        model.setMonthlyBudget(budget);
        storage.save(model);
    }

    // ── Export ────────────────────────────────────────────────────────────────

    /**
     * Exports all transactions to a CSV file.
     *
     * @param filePath destination path for the CSV file
     * @throws IOException if writing fails
     */
    public void exportToCsv(Path filePath) throws IOException {
        CsvExporter.export(model, filePath);
    }

    // ── Model Access (read-only for View) ─────────────────────────────────────

    /** @return the current TransactionList model (read-only access) */
    public TransactionList getModel() { return model; }
}
