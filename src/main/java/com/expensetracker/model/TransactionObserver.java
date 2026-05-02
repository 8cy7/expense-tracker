package com.expensetracker.model;

/**
 * Observer interface for the Observer design pattern.
 *
 * <p>Any component that needs to react to changes in the TransactionList
 * (the "Subject") must implement this interface.
 *
 * <p>In this application, the MainView implements this interface so it
 * automatically refreshes itself whenever the model changes.
 */
public interface TransactionObserver {

    /**
     * Called by the Subject (TransactionList) whenever its state changes
     * (transaction added, removed, or updated).
     *
     * @param list the updated TransactionList — never null
     */
    void onTransactionListChanged(TransactionList list);
}
