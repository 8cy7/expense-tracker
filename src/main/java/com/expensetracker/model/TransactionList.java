package com.expensetracker.model;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Mutable ADT representing the collection of all Transaction objects.
 * Acts as the "Subject" in the Observer design pattern.
 *
 * <p>Representation Invariant (RI):
 * - transactions != null
 * - No null elements in transactions
 * - All TransactionIDs in the map are unique
 * - All Transaction values are valid (their own RIs hold)
 * - monthlyBudget >= 0
 *
 * <p>Abstraction Function (AF):
 * - AF(this) = a user's complete financial record, where
 *   transactions maps unique TransactionIDs to their corresponding
 *   Transaction objects, and monthlyBudget is the user-defined
 *   spending limit for the current month.
 */
public class TransactionList {

    /** The core data structure: maps ID → Transaction */
    private final Map<TransactionID, Transaction> transactions;

    /** List of observers to notify on state change (Observer pattern) */
    private final List<TransactionObserver> observers;

    /** User-defined monthly budget (0 means no budget set) */
    private double monthlyBudget;

    // ── Constructor ───────────────────────────────────────────────────────────

    /** Creates an empty TransactionList with no budget set. */
    public TransactionList() {
        this.transactions = new LinkedHashMap<>();
        this.observers = new ArrayList<>();
        this.monthlyBudget = 0;
        checkRep();
    }

    // ── Observer Pattern ──────────────────────────────────────────────────────

    /**
     * Registers an observer to be notified when the list changes.
     *
     * @param observer the observer to add; must not be null
     */
    public void addObserver(TransactionObserver observer) {
        if (observer != null) observers.add(observer);
    }

    /** Notifies all registered observers that the model has changed. */
    private void notifyObservers() {
        for (TransactionObserver o : observers) {
            o.onTransactionListChanged(this);
        }
    }

    // ── CRUD Operations ───────────────────────────────────────────────────────

    /**
     * Adds a new transaction to the list.
     *
     * @param t the transaction to add; must not be null
     * @throws IllegalArgumentException if t is null or its ID already exists
     */
    public void addTransaction(Transaction t) {
        if (t == null) throw new IllegalArgumentException("Transaction must not be null");
        if (transactions.containsKey(t.getId()))
            throw new IllegalArgumentException("Duplicate TransactionID: " + t.getId());
        transactions.put(t.getId(), t);
        checkRep();
        notifyObservers();
    }

    /**
     * Removes a transaction by its ID.
     *
     * @param id the ID of the transaction to remove
     * @return true if removed, false if not found
     */
    public boolean removeTransaction(TransactionID id) {
        boolean removed = transactions.remove(id) != null;
        if (removed) {
            checkRep();
            notifyObservers();
        }
        return removed;
    }

    /**
     * Updates an existing transaction's fields.
     *
     * @param id          the ID of the transaction to update
     * @param amount      new amount
     * @param category    new category
     * @param date        new date
     * @param description new description
     * @param type        new type ("INCOME" or "EXPENSE")
     * @throws NoSuchElementException if no transaction with the given ID exists
     */
    public void updateTransaction(TransactionID id, double amount, String category,
                                   LocalDate date, String description, String type) {
        Transaction t = transactions.get(id);
        if (t == null) throw new NoSuchElementException("No transaction with ID: " + id);
        t.setAmount(amount);
        t.setCategory(category);
        t.setDate(date);
        t.setDescription(description);
        t.setType(type);
        checkRep();
        notifyObservers();
    }

    // ── Query Operations ──────────────────────────────────────────────────────

    /**
     * Returns all transactions as an unmodifiable list.
     *
     * @return list of all transactions (insertion order)
     */
    public List<Transaction> getTransactions() {
        return Collections.unmodifiableList(new ArrayList<>(transactions.values()));
    }

    /**
     * Returns a transaction by ID.
     *
     * @param id the TransactionID to look up
     * @return the Transaction, or null if not found
     */
    public Transaction getById(TransactionID id) {
        return transactions.get(id);
    }

    /**
     * Filters transactions by category.
     *
     * @param category the category to filter by
     * @return list of matching transactions
     */
    public List<Transaction> getByCategory(String category) {
        return transactions.values().stream()
                .filter(t -> t.getCategory().equalsIgnoreCase(category))
                .collect(Collectors.toList());
    }

    /**
     * Filters transactions by date range.
     *
     * @param from start date (inclusive)
     * @param to   end date (inclusive)
     * @return list of matching transactions
     */
    public List<Transaction> getByDateRange(LocalDate from, LocalDate to) {
        return transactions.values().stream()
                .filter(t -> !t.getDate().isBefore(from) && !t.getDate().isAfter(to))
                .collect(Collectors.toList());
    }

    /**
     * Searches transactions by keyword in description or category.
     *
     * @param keyword the search term
     * @return list of matching transactions
     */
    public List<Transaction> search(String keyword) {
        String lower = keyword.toLowerCase();
        return transactions.values().stream()
                .filter(t -> t.getDescription().toLowerCase().contains(lower)
                          || t.getCategory().toLowerCase().contains(lower))
                .collect(Collectors.toList());
    }

    // ── Financial Calculations ────────────────────────────────────────────────

    /**
     * Calculates total income across all transactions.
     *
     * @return sum of all income amounts
     */
    public double getTotalIncome() {
        return transactions.values().stream()
                .filter(Transaction::isIncome)
                .mapToDouble(Transaction::getAmount)
                .sum();
    }

    /**
     * Calculates total expenses across all transactions.
     *
     * @return sum of all expense amounts
     */
    public double getTotalExpenses() {
        return transactions.values().stream()
                .filter(Transaction::isExpense)
                .mapToDouble(Transaction::getAmount)
                .sum();
    }

    /**
     * Calculates the current balance (income - expenses).
     *
     * @return net balance
     */
    public double getBalance() {
        return getTotalIncome() - getTotalExpenses();
    }

    /**
     * Returns total expenses grouped by category.
     *
     * @return map of category → total amount (expenses only)
     */
    public Map<String, Double> getExpensesByCategory() {
        return transactions.values().stream()
                .filter(Transaction::isExpense)
                .collect(Collectors.groupingBy(
                        Transaction::getCategory,
                        Collectors.summingDouble(Transaction::getAmount)
                ));
    }

    /**
     * Returns expenses for the current month only.
     *
     * @return total expenses this month
     */
    public double getCurrentMonthExpenses() {
        LocalDate now = LocalDate.now();
        return transactions.values().stream()
                .filter(Transaction::isExpense)
                .filter(t -> t.getDate().getMonth() == now.getMonth()
                          && t.getDate().getYear() == now.getYear())
                .mapToDouble(Transaction::getAmount)
                .sum();
    }

    // ── Budget ────────────────────────────────────────────────────────────────

    /**
     * Sets the monthly budget.
     *
     * @param budget must be >= 0
     */
    public void setMonthlyBudget(double budget) {
        if (budget < 0) throw new IllegalArgumentException("Budget must be >= 0");
        this.monthlyBudget = budget;
        notifyObservers();
    }

    /** @return the current monthly budget (0 = not set) */
    public double getMonthlyBudget() { return monthlyBudget; }

    /**
     * Checks if current month's expenses exceed the budget.
     *
     * @return true if budget is set and exceeded
     */
    public boolean isBudgetExceeded() {
        return monthlyBudget > 0 && getCurrentMonthExpenses() > monthlyBudget;
    }

    /** @return number of transactions in the list */
    public int size() { return transactions.size(); }

    // ── Rep Invariant ─────────────────────────────────────────────────────────

    /** Verifies the Representation Invariant holds. */
    private void checkRep() {
        assert transactions != null : "RI violated: transactions map is null";
        assert monthlyBudget >= 0 : "RI violated: monthlyBudget < 0";
        for (Map.Entry<TransactionID, Transaction> entry : transactions.entrySet()) {
            assert entry.getKey() != null : "RI violated: null key in map";
            assert entry.getValue() != null : "RI violated: null value in map";
        }
    }

    @Override
    public String toString() {
        return String.format("TransactionList[size=%d, balance=%.2f SAR]",
                transactions.size(), getBalance());
    }
}
