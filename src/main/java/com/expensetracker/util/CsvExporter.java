package com.expensetracker.util;

import com.expensetracker.model.Transaction;
import com.expensetracker.model.TransactionList;
import org.apache.commons.csv.*;

import java.io.*;
import java.nio.file.*;
import java.util.List;

/**
 * Utility class for exporting transaction data to CSV format.
 */
public class CsvExporter {

    /**
     * Exports all transactions from the given list to a CSV file.
     *
     * @param list     the TransactionList to export
     * @param filePath the destination file path
     * @throws IOException if writing fails
     */
    public static void export(TransactionList list, Path filePath) throws IOException {
        List<Transaction> transactions = list.getTransactions();

        try (BufferedWriter writer = Files.newBufferedWriter(filePath);
             CSVPrinter printer = new CSVPrinter(writer, CSVFormat.DEFAULT
                     .withHeader("ID", "Type", "Amount (SAR)", "Category",
                                 "Date", "Description"))) {

            for (Transaction t : transactions) {
                printer.printRecord(
                        t.getId().getValue(),
                        t.getType(),
                        String.format("%.2f", t.getAmount()),
                        t.getCategory(),
                        t.getDate().toString(),
                        t.getDescription()
                );
            }

            // Summary rows
            printer.println();
            printer.printRecord("", "TOTAL INCOME",  String.format("%.2f", list.getTotalIncome()),  "", "", "");
            printer.printRecord("", "TOTAL EXPENSES", String.format("%.2f", list.getTotalExpenses()), "", "", "");
            printer.printRecord("", "BALANCE",        String.format("%.2f", list.getBalance()),       "", "", "");
        }
    }
}
