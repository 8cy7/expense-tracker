package com.expensetracker.view;

import com.expensetracker.model.Transaction;
import com.expensetracker.model.TransactionID;
import javafx.beans.property.SimpleStringProperty;

/**
 * A simple data class wrapping a Transaction for display in JavaFX TableView.
 * Uses JavaFX StringProperty so the table can bind to values.
 */
public class TransactionRow {

    private final TransactionID id;
    private final SimpleStringProperty type;
    private final SimpleStringProperty amount;
    private final SimpleStringProperty category;
    private final SimpleStringProperty date;
    private final SimpleStringProperty description;

    public TransactionRow(Transaction t) {
        this.id          = t.getId();
        this.type        = new SimpleStringProperty(t.getType());
        this.amount      = new SimpleStringProperty(String.format("%.2f", t.getAmount()));
        this.category    = new SimpleStringProperty(t.getCategory());
        this.date        = new SimpleStringProperty(t.getDate().toString());
        this.description = new SimpleStringProperty(t.getDescription());
    }

    public TransactionID getId()          { return id; }
    public String getType()               { return type.get(); }
    public String getAmount()             { return amount.get(); }
    public String getCategory()           { return category.get(); }
    public String getDate()               { return date.get(); }
    public String getDescription()        { return description.get(); }

    public SimpleStringProperty typeProperty()        { return type; }
    public SimpleStringProperty amountProperty()      { return amount; }
    public SimpleStringProperty categoryProperty()    { return category; }
    public SimpleStringProperty dateProperty()        { return date; }
    public SimpleStringProperty descriptionProperty() { return description; }
}
