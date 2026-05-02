package com.expensetracker.util;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.regex.*;

/**
 * Utility class that uses Regular Expressions to parse a quick-add transaction string.
 *
 * <p>The user can type a single string like:
 * <pre>
 *   Lunch 25 #food 19/10/2025
 *   Salary 5000 #salary 01/11/2025
 *   Coffee 15.50 #food
 * </pre>
 *
 * <p>The regex extracts:
 * <ul>
 *   <li>description — one or more words before the amount</li>
 *   <li>amount      — a positive decimal number</li>
 *   <li>category    — a word preceded by '#'</li>
 *   <li>date        — optional dd/MM/yyyy; defaults to today if absent</li>
 * </ul>
 */
public class TransactionParser {

    /**
     * Pattern: description  amount  #category  [optional date dd/MM/yyyy]
     * Groups:
     *   1 = description (words before amount)
     *   2 = amount (integer or decimal)
     *   3 = category (after #)
     *   4 = date string (optional, dd/MM/yyyy)
     */
    private static final Pattern QUICK_ADD_PATTERN = Pattern.compile(
            "^([a-zA-Z\\s]+?)\\s+(\\d+(?:\\.\\d+)?)\\s+#(\\w+)(?:\\s+(\\d{2}/\\d{2}/\\d{4}))?\\s*$",
            Pattern.CASE_INSENSITIVE
    );

    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern("dd/MM/yyyy");

    /** Result holder returned by {@link #parse(String)} */
    public static class ParseResult {
        public final String description;
        public final double amount;
        public final String category;
        public final LocalDate date;

        public ParseResult(String description, double amount,
                           String category, LocalDate date) {
            this.description = description.trim();
            this.amount      = amount;
            this.category    = capitalize(category);
            this.date        = date;
        }

        private static String capitalize(String s) {
            if (s == null || s.isEmpty()) return s;
            return s.substring(0, 1).toUpperCase() + s.substring(1).toLowerCase();
        }
    }

    /**
     * Attempts to parse a quick-add string using the regex pattern.
     *
     * @param input the user-typed string; e.g. "Lunch 25 #food 19/10/2025"
     * @return a {@link ParseResult} if the string matches the pattern, or null otherwise
     */
    public static ParseResult parse(String input) {
        if (input == null || input.isBlank()) return null;
        Matcher m = QUICK_ADD_PATTERN.matcher(input.trim());
        if (!m.matches()) return null;

        String description = m.group(1);
        double amount      = Double.parseDouble(m.group(2));
        String category    = m.group(3);
        LocalDate date     = m.group(4) != null
                ? LocalDate.parse(m.group(4), DATE_FORMAT)
                : LocalDate.now();

        return new ParseResult(description, amount, category, date);
    }

    /**
     * Returns true if the given string matches the quick-add format.
     *
     * @param input the string to test
     * @return true if parseable
     */
    public static boolean isValid(String input) {
        return parse(input) != null;
    }
}
