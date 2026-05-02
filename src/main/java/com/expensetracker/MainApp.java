package com.expensetracker;

import com.expensetracker.controller.MainController;
import com.expensetracker.model.TransactionList;
import com.expensetracker.storage.DataStorageManager;
import com.expensetracker.view.MainView;
import javafx.application.Application;
import javafx.stage.Stage;

/**
 * Entry point for the Personal Expense Tracker application.
 *
 * <p>Bootstraps the MVC architecture:
 * <ol>
 *   <li>Load saved data via DataStorageManager (Singleton)</li>
 *   <li>Create the Model (TransactionList)</li>
 *   <li>Create the Controller (MainController)</li>
 *   <li>Create the View (MainView) and register it as an Observer</li>
 *   <li>Show the primary window</li>
 * </ol>
 */
public class MainApp extends Application {

    @Override
    public void start(Stage primaryStage) {
        // 1. Load persisted data (Singleton DataStorageManager)
        TransactionList model = DataStorageManager.getInstance().load();

        // 2. Create Controller with the loaded Model
        MainController controller = new MainController(model);

        // 3. Create View and wire it up (View registers itself as Observer inside start())
        MainView view = new MainView();
        view.start(controller, primaryStage);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
