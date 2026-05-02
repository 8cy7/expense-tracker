package com.expensetracker.view;

import com.expensetracker.controller.MainController;
import com.expensetracker.model.*;
import javafx.application.Platform;
import com.expensetracker.util.TransactionParser;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.*;
import javafx.scene.Scene;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.*;
import javafx.stage.*;

import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.*;

/**
 * JavaFX View in the MVC pattern.
 *
 * <p>Implements {@link TransactionObserver} so it automatically refreshes
 * whenever the Model (TransactionList) changes. The View never modifies
 * the Model directly — all actions are delegated to the Controller.
 */
public class MainView implements TransactionObserver {

    private MainController controller;

    // ── Summary labels ────────────────────────────────────────────────────────
    private Label lblBalance, lblIncome, lblExpenses, lblBudgetStatus;

    // ── Table ─────────────────────────────────────────────────────────────────
    private TableView<TransactionRow> tableView;
    private ObservableList<TransactionRow> tableData;

    // ── Chart ─────────────────────────────────────────────────────────────────
    private PieChart pieChart;

    // ── Search ────────────────────────────────────────────────────────────────
    private TextField searchField;

    // ── Stage reference ───────────────────────────────────────────────────────
    private Stage primaryStage;

    /**
     * Builds and shows the main application window.
     *
     * @param controller the Controller to delegate actions to
     * @param stage      the primary JavaFX Stage
     */
    public void start(MainController controller, Stage stage) {
        this.controller   = controller;
        this.primaryStage = stage;

        // Register as observer so we receive model-change notifications
        controller.getModel().addObserver(this);

        stage.setTitle("Personal Expense Tracker");
        stage.setMinWidth(950);
        stage.setMinHeight(650);

        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: #f4f6f9;");

        root.setTop(buildHeader());
        root.setCenter(buildCenter());
        root.setBottom(buildStatusBar());

        Scene scene = new Scene(root, 1100, 700);
        stage.setScene(scene);
        stage.show();

        refresh(controller.getModel());
    }

    // ── Observer callback ─────────────────────────────────────────────────────

    /**
     * Called automatically by TransactionList (Subject) when data changes.
     * Schedules a UI refresh on the JavaFX Application Thread.
     *
     * @param list the updated TransactionList
     */
    @Override
    public void onTransactionListChanged(TransactionList list) {
        Platform.runLater(() -> refresh(list));
    }

    // ── UI Builders ───────────────────────────────────────────────────────────

    private HBox buildHeader() {
        HBox header = new HBox(20);
        header.setPadding(new Insets(16, 24, 16, 24));
        header.setAlignment(Pos.CENTER_LEFT);
        header.setStyle("-fx-background-color: #2c3e50;");

        Label title = new Label("💰 Personal Expense Tracker");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 22));
        title.setTextFill(Color.WHITE);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button btnAdd    = styledButton("＋ Add Transaction", "#27ae60");
        Button btnExport = styledButton("⬇ Export CSV", "#2980b9");
        Button btnBudget = styledButton("🎯 Set Budget", "#8e44ad");

        btnAdd.setOnAction(e -> showAddDialog());
        btnExport.setOnAction(e -> handleExport());
        btnBudget.setOnAction(e -> showBudgetDialog());

        header.getChildren().addAll(title, spacer, btnAdd, btnExport, btnBudget);
        return header;
    }

    private SplitPane buildCenter() {
        SplitPane split = new SplitPane();
        split.setDividerPositions(0.65);

        VBox left = new VBox(12);
        left.setPadding(new Insets(16));

        TableView<TransactionRow> table = buildTable();
        left.getChildren().addAll(buildSummaryCards(), buildSearchBar(), table);
        VBox.setVgrow(table, Priority.ALWAYS);

        VBox right = new VBox(12);
        right.setPadding(new Insets(16));
        right.getChildren().add(buildChart());

        split.getItems().addAll(left, right);
        return split;
    }

    private HBox buildSummaryCards() {
        lblBalance  = cardLabel("Balance",  "0.00 SAR", "#2c3e50");
        lblIncome   = cardLabel("Income",   "0.00 SAR", "#27ae60");
        lblExpenses = cardLabel("Expenses", "0.00 SAR", "#e74c3c");
        lblBudgetStatus = cardLabel("Budget", "Not Set", "#8e44ad");

        HBox cards = new HBox(12,
                buildCard("Balance",  lblBalance,      "#2c3e50"),
                buildCard("Income",   lblIncome,       "#27ae60"),
                buildCard("Expenses", lblExpenses,     "#e74c3c"),
                buildCard("Budget",   lblBudgetStatus, "#8e44ad")
        );
        cards.setAlignment(Pos.CENTER);
        return cards;
    }

    private VBox buildCard(String title, Label valueLabel, String color) {
        VBox card = new VBox(4);
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(14, 20, 14, 20));
        card.setStyle("-fx-background-color: white; -fx-background-radius: 10; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 8, 0, 0, 2);");
        HBox.setHgrow(card, Priority.ALWAYS);

        Label lbl = new Label(title);
        lbl.setFont(Font.font("Arial", 12));
        lbl.setTextFill(Color.web("#888"));

        valueLabel.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        valueLabel.setTextFill(Color.web(color));

        card.getChildren().addAll(lbl, valueLabel);
        return card;
    }

    private Label cardLabel(String id, String text, String color) {
        Label l = new Label(text);
        l.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        l.setTextFill(Color.web(color));
        return l;
    }

    private HBox buildSearchBar() {
        searchField = new TextField();
        searchField.setPromptText("Search by description or category...");
        searchField.setPrefWidth(300);
        HBox.setHgrow(searchField, Priority.ALWAYS);

        Button btnSearch = styledButton("🔍 Search", "#2980b9");
        Button btnClear  = styledButton("✕ Clear", "#7f8c8d");

        btnSearch.setOnAction(e -> handleSearch());
        btnClear.setOnAction(e -> {
            searchField.clear();
            refresh(controller.getModel());
        });

        HBox bar = new HBox(8, new Label("Search:"), searchField, btnSearch, btnClear);
        bar.setAlignment(Pos.CENTER_LEFT);
        return bar;
    }

    @SuppressWarnings("unchecked")
    private TableView<TransactionRow> buildTable() {
        tableView = new TableView<>();
        tableData = FXCollections.observableArrayList();
        tableView.setItems(tableData);
        tableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<TransactionRow, String> colType = new TableColumn<>("Type");
        colType.setCellValueFactory(new PropertyValueFactory<>("type"));
        colType.setPrefWidth(80);

        TableColumn<TransactionRow, String> colAmount = new TableColumn<>("Amount (SAR)");
        colAmount.setCellValueFactory(new PropertyValueFactory<>("amount"));

        TableColumn<TransactionRow, String> colCategory = new TableColumn<>("Category");
        colCategory.setCellValueFactory(new PropertyValueFactory<>("category"));

        TableColumn<TransactionRow, String> colDate = new TableColumn<>("Date");
        colDate.setCellValueFactory(new PropertyValueFactory<>("date"));

        TableColumn<TransactionRow, String> colDesc = new TableColumn<>("Description");
        colDesc.setCellValueFactory(new PropertyValueFactory<>("description"));
        colDesc.setPrefWidth(200);

        TableColumn<TransactionRow, Void> colActions = new TableColumn<>("Actions");
        colActions.setCellFactory(col -> new TableCell<>() {
            final Button btnEdit   = styledButton("Edit", "#f39c12");
            final Button btnDelete = styledButton("Delete", "#e74c3c");
            final HBox   box       = new HBox(6, btnEdit, btnDelete);

            {
                btnEdit.setOnAction(e -> {
                    TransactionRow row = getTableView().getItems().get(getIndex());
                    showEditDialog(row.getId());
                });
                btnDelete.setOnAction(e -> {
                    TransactionRow row = getTableView().getItems().get(getIndex());
                    controller.deleteTransaction(row.getId());
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : box);
            }
        });

        tableView.getColumns().addAll(colType, colAmount, colCategory, colDate, colDesc, colActions);
        VBox.setVgrow(tableView, Priority.ALWAYS);
        return tableView;
    }

    private PieChart buildChart() {
        pieChart = new PieChart();
        pieChart.setTitle("Expenses by Category");
        pieChart.setLegendVisible(true);
        pieChart.setPrefHeight(350);
        VBox.setVgrow(pieChart, Priority.ALWAYS);
        return pieChart;
    }

    private HBox buildStatusBar() {
        HBox bar = new HBox();
        bar.setPadding(new Insets(6, 16, 6, 16));
        bar.setStyle("-fx-background-color: #ecf0f1;");
        Label status = new Label("Ready");
        status.setFont(Font.font("Arial", 11));
        status.setTextFill(Color.web("#666"));
        bar.getChildren().add(status);
        return bar;
    }

    // ── Refresh (called by Observer) ──────────────────────────────────────────

    private void refresh(TransactionList list) {
        // Update summary cards
        lblBalance.setText(String.format("%.2f SAR", list.getBalance()));
        lblIncome.setText(String.format("%.2f SAR",  list.getTotalIncome()));
        lblExpenses.setText(String.format("%.2f SAR", list.getTotalExpenses()));

        if (list.getMonthlyBudget() > 0) {
            double spent   = list.getCurrentMonthExpenses();
            double budget  = list.getMonthlyBudget();
            String budgetText = String.format("%.0f / %.0f SAR", spent, budget);
            lblBudgetStatus.setText(budgetText);
            lblBudgetStatus.setTextFill(list.isBudgetExceeded()
                    ? Color.RED : Color.web("#27ae60"));
        } else {
            lblBudgetStatus.setText("Not Set");
        }

        // Refresh table
        tableData.clear();
        for (Transaction t : list.getTransactions()) {
            tableData.add(new TransactionRow(t));
        }

        // Refresh pie chart
        Map<String, Double> catMap = list.getExpensesByCategory();
        ObservableList<PieChart.Data> pieData = FXCollections.observableArrayList();
        catMap.forEach((cat, amt) ->
                pieData.add(new PieChart.Data(cat + String.format(" (%.0f)", amt), amt)));
        pieChart.setData(pieData);
    }

    // ── Dialogs ───────────────────────────────────────────────────────────────

    private void showAddDialog() {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Add Transaction");
        dialog.setHeaderText("Enter transaction details");

        ButtonType addBtn = new ButtonType("Add", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(addBtn, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));

        ComboBox<String> typeBox = new ComboBox<>(
                FXCollections.observableArrayList("EXPENSE", "INCOME"));
        typeBox.setValue("EXPENSE");

        TextField amountField  = new TextField();
        amountField.setPromptText("e.g. 50.00");

        ComboBox<String> categoryBox = new ComboBox<>(FXCollections.observableArrayList(
                "Food", "Transport", "Bills", "Salary", "Shopping",
                "Health", "Entertainment", "Education", "Other"));
        categoryBox.setEditable(true);
        categoryBox.setValue("Food");

        DatePicker datePicker = new DatePicker(LocalDate.now());
        TextField descField   = new TextField();
        descField.setPromptText("Optional description");

        // Quick-add row
        TextField quickField = new TextField();
        quickField.setPromptText("Quick: Lunch 25 #food 19/10/2025");
        Button parseBtn = styledButton("Parse", "#2980b9");
        parseBtn.setOnAction(e -> {
            TransactionParser.ParseResult r = TransactionParser.parse(quickField.getText());
            if (r != null) {
                amountField.setText(String.valueOf(r.amount));
                categoryBox.setValue(r.category);
                datePicker.setValue(r.date);
                descField.setText(r.description);
            } else {
                showAlert("Invalid Format",
                        "Use: Description Amount #category [dd/MM/yyyy]");
            }
        });

        grid.add(new Label("Quick Add:"), 0, 0);
        grid.add(quickField, 1, 0);
        grid.add(parseBtn, 2, 0);
        grid.add(new Label("Type:"),        0, 1); grid.add(typeBox,     1, 1);
        grid.add(new Label("Amount (SAR):"), 0, 2); grid.add(amountField, 1, 2);
        grid.add(new Label("Category:"),    0, 3); grid.add(categoryBox, 1, 3);
        grid.add(new Label("Date:"),        0, 4); grid.add(datePicker,  1, 4);
        grid.add(new Label("Description:"), 0, 5); grid.add(descField,   1, 5);

        dialog.getDialogPane().setContent(grid);

        dialog.showAndWait().ifPresent(result -> {
            if (result == addBtn) {
                try {
                    double amount = Double.parseDouble(amountField.getText().trim());
                    controller.addTransaction(
                            amount,
                            categoryBox.getValue(),
                            datePicker.getValue(),
                            descField.getText(),
                            typeBox.getValue()
                    );
                } catch (NumberFormatException ex) {
                    showAlert("Invalid Input", "Please enter a valid amount.");
                }
            }
        });
    }

    private void showEditDialog(TransactionID id) {
        Transaction t = controller.getModel().getById(id);
        if (t == null) return;

        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Edit Transaction");

        ButtonType saveBtn = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveBtn, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));

        ComboBox<String> typeBox = new ComboBox<>(
                FXCollections.observableArrayList("EXPENSE", "INCOME"));
        typeBox.setValue(t.getType());

        TextField amountField  = new TextField(String.valueOf(t.getAmount()));
        ComboBox<String> categoryBox = new ComboBox<>(FXCollections.observableArrayList(
                "Food", "Transport", "Bills", "Salary", "Shopping",
                "Health", "Entertainment", "Education", "Other"));
        categoryBox.setEditable(true);
        categoryBox.setValue(t.getCategory());
        DatePicker datePicker = new DatePicker(t.getDate());
        TextField descField   = new TextField(t.getDescription());

        grid.add(new Label("Type:"),        0, 0); grid.add(typeBox,     1, 0);
        grid.add(new Label("Amount (SAR):"), 0, 1); grid.add(amountField, 1, 1);
        grid.add(new Label("Category:"),    0, 2); grid.add(categoryBox, 1, 2);
        grid.add(new Label("Date:"),        0, 3); grid.add(datePicker,  1, 3);
        grid.add(new Label("Description:"), 0, 4); grid.add(descField,   1, 4);

        dialog.getDialogPane().setContent(grid);

        dialog.showAndWait().ifPresent(result -> {
            if (result == saveBtn) {
                try {
                    double amount = Double.parseDouble(amountField.getText().trim());
                    controller.updateTransaction(id, amount, categoryBox.getValue(),
                            datePicker.getValue(), descField.getText(), typeBox.getValue());
                } catch (NumberFormatException ex) {
                    showAlert("Invalid Input", "Please enter a valid amount.");
                }
            }
        });
    }

    private void showBudgetDialog() {
        TextInputDialog dialog = new TextInputDialog(
                String.valueOf(controller.getModel().getMonthlyBudget()));
        dialog.setTitle("Set Monthly Budget");
        dialog.setHeaderText("Enter your monthly spending limit (SAR):");
        dialog.setContentText("Budget:");
        dialog.showAndWait().ifPresent(value -> {
            try {
                double budget = Double.parseDouble(value.trim());
                controller.setMonthlyBudget(budget);
            } catch (NumberFormatException ex) {
                showAlert("Invalid Input", "Please enter a valid number.");
            }
        });
    }

    private void handleSearch() {
        String keyword = searchField.getText().trim();
        if (keyword.isEmpty()) {
            refresh(controller.getModel());
            return;
        }
        tableData.clear();
        for (Transaction t : controller.search(keyword)) {
            tableData.add(new TransactionRow(t));
        }
    }

    private void handleExport() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Export to CSV");
        chooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("CSV Files", "*.csv"));
        chooser.setInitialFileName("transactions.csv");
        java.io.File file = chooser.showSaveDialog(primaryStage);
        if (file != null) {
            try {
                controller.exportToCsv(file.toPath());
                showAlert("Export Successful", "File saved to: " + file.getAbsolutePath());
            } catch (IOException ex) {
                showAlert("Export Failed", ex.getMessage());
            }
        }
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private Button styledButton(String text, String color) {
        Button btn = new Button(text);
        btn.setStyle(String.format(
                "-fx-background-color: %s; -fx-text-fill: white; " +
                "-fx-font-weight: bold; -fx-background-radius: 6; " +
                "-fx-padding: 6 14 6 14;", color));
        btn.setOnMouseEntered(e -> btn.setOpacity(0.85));
        btn.setOnMouseExited(e -> btn.setOpacity(1.0));
        return btn;
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}

