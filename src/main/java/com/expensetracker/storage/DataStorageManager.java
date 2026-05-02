package com.expensetracker.storage;

import com.expensetracker.model.Transaction;
import com.expensetracker.model.TransactionID;
import com.expensetracker.model.TransactionList;
import com.google.gson.*;
import com.google.gson.reflect.TypeToken;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.file.*;
import java.time.LocalDate;
import java.util.*;

/**
 * Singleton responsible for loading and saving all application data to disk.
 *
 * <p>Only one instance of this class ever exists. Any part of the program
 * that needs to persist or load data must go through this single instance,
 * ensuring data consistency and preventing file conflicts.
 *
 * <p>Data is stored as JSON in the user's home directory under
 * {@code .expense-tracker/data.json}.
 */
public class DataStorageManager {

    // ── Singleton ─────────────────────────────────────────────────────────────

    /** The single instance of DataStorageManager */
    private static DataStorageManager instance;

    /**
     * Returns the single instance of DataStorageManager.
     * Creates it on first call (lazy initialization).
     *
     * @return the singleton instance
     */
    public static DataStorageManager getInstance() {
        if (instance == null) {
            instance = new DataStorageManager();
        }
        return instance;
    }

    /** Private constructor — prevents external instantiation */
    private DataStorageManager() {
        gson = buildGson();
        dataFile = Paths.get(System.getProperty("user.home"),
                ".expense-tracker", "data.json");
        ensureDirectoryExists();
    }

    // ── Fields ────────────────────────────────────────────────────────────────

    private final Gson gson;
    private final Path dataFile;

    // ── Public API ────────────────────────────────────────────────────────────

    /**
     * Saves the entire TransactionList to disk as JSON.
     *
     * @param list the TransactionList to persist; must not be null
     */
    public void save(TransactionList list) {
        try {
            List<Map<String, Object>> records = new ArrayList<>();
            for (Transaction t : list.getTransactions()) {
                Map<String, Object> record = new LinkedHashMap<>();
                record.put("id",          t.getId().getValue());
                record.put("amount",      t.getAmount());
                record.put("category",    t.getCategory());
                record.put("date",        t.getDate().toString());
                record.put("description", t.getDescription());
                record.put("type",        t.getType());
                records.add(record);
            }
            Map<String, Object> root = new LinkedHashMap<>();
            root.put("monthlyBudget", list.getMonthlyBudget());
            root.put("transactions", records);

            String json = gson.toJson(root);
            Files.writeString(dataFile, json, StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING);
        } catch (IOException e) {
            System.err.println("Error saving data: " + e.getMessage());
        }
    }

    /**
     * Loads transaction data from disk into a TransactionList.
     *
     * @return a TransactionList populated with saved data,
     *         or an empty TransactionList if no data file exists
     */
    public TransactionList load() {
        TransactionList list = new TransactionList();
        if (!Files.exists(dataFile)) return list;

        try {
            String json = Files.readString(dataFile);
            JsonObject root = JsonParser.parseString(json).getAsJsonObject();

            double budget = root.has("monthlyBudget")
                    ? root.get("monthlyBudget").getAsDouble() : 0;
            list.setMonthlyBudget(budget);

            JsonArray arr = root.getAsJsonArray("transactions");
            for (JsonElement elem : arr) {
                JsonObject obj = elem.getAsJsonObject();
                TransactionID id  = new TransactionID(obj.get("id").getAsInt());
                double amount     = obj.get("amount").getAsDouble();
                String category   = obj.get("category").getAsString();
                LocalDate date    = LocalDate.parse(obj.get("date").getAsString());
                String desc       = obj.get("description").getAsString();
                String type       = obj.get("type").getAsString();
                list.addTransaction(new Transaction(id, amount, category, date, desc, type));
            }
        } catch (IOException | JsonParseException e) {
            System.err.println("Error loading data: " + e.getMessage());
        }
        return list;
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private Gson buildGson() {
        return new GsonBuilder().setPrettyPrinting().create();
    }

    private void ensureDirectoryExists() {
        try {
            Files.createDirectories(dataFile.getParent());
        } catch (IOException e) {
            System.err.println("Cannot create data directory: " + e.getMessage());
        }
    }
}
