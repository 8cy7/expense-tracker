# Personal Expense Tracker
### SE-4111 Course Project | Prince Sattam bin Abdulaziz University

---

## Prerequisites

| Tool | Version |
|------|---------|
| Java JDK | 17 or higher |
| Maven | 3.8+ |
| VS Code | Latest |

### VS Code Extensions needed:
- **Extension Pack for Java** (by Microsoft)
- **Maven for Java**

---

## Setup & Run

### 1. Clone / open the project folder in VS Code

```bash
cd expense-tracker
```

### 2. Install dependencies & run

```bash
mvn javafx:run
```

### 3. Build a runnable JAR

```bash
mvn package
java -jar target/expense-tracker-1.0-SNAPSHOT.jar
```

---

## Project Structure (MVC)

```
src/main/java/com/expensetracker/
│
├── MainApp.java                  ← Entry point
│
├── model/
│   ├── TransactionID.java        ← Immutable ADT (equals/hashCode)
│   ├── Transaction.java          ← Mutable ADT (RI + AF documented)
│   ├── TransactionList.java      ← Subject (Observer pattern) + Model
│   └── TransactionObserver.java  ← Observer interface
│
├── view/
│   ├── MainView.java             ← JavaFX View (implements Observer)
│   └── TransactionRow.java       ← TableView row wrapper
│
├── controller/
│   └── MainController.java       ← Controller (handles user events)
│
├── storage/
│   └── DataStorageManager.java   ← Singleton (JSON persistence)
│
└── util/
    ├── TransactionParser.java    ← Regex quick-add parser
    └── CsvExporter.java          ← CSV export utility
```

---

## Features

| # | Feature |
|---|---------|
| 1 | Add / Edit / Delete transactions (income or expense) |
| 2 | Categorize transactions (Food, Transport, Bills, etc.) |
| 3 | Pie chart visualization by spending category |
| 4 | Search & filter by keyword |
| 5 | Monthly budget with overspending alert |
| 6 | Export to CSV |
| 7 | Auto-calculate balance (Income − Expenses) |
| 8 | Quick-add via regex: `Lunch 25 #food 19/10/2025` |
| 9 | Data persistence (JSON, saved locally) |

---

## Design Patterns Used

| Pattern | Where |
|---------|-------|
| **MVC** | Full separation: Model / View / Controller packages |
| **Observer** | TransactionList (Subject) → MainView (Observer) |
| **Singleton** | DataStorageManager |

---

## Data Storage

Transactions are saved automatically to:
```
~/.expense-tracker/data.json
```
