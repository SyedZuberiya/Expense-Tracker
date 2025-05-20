import java.io.*;
import java.time.LocalDate;
import java.time.Month;
import java.time.format.DateTimeParseException;
import java.util.*;

enum TranType {
    INCOME,
    EXPENSE
}

class FinancialTransaction_Record {
    private TranType tranType;
    private String category;
    private double amount;
    private LocalDate tranDate;

    public FinancialTransaction_Record(TranType type, String category, double amount, LocalDate date) {
        this.tranType = type;
        this.category = category;
        this.amount = amount;
        this.tranDate = date;
    }

    public TranType getType() {
        return tranType;
    }

    public String getCategory() {
        return category;
    }

    public double getAmount() {
        return amount;
    }

    public LocalDate getDate() {
        return tranDate;
    }

    @Override
    public String toString() {
        return tranType + "," + category + "," + amount + "," + tranDate;
    }

    public static FinancialTransaction_Record fromString(String line) {
        String[] parts = line.split(",");
        if (parts.length != 4)
            throw new IllegalArgumentException("Invalid transaction entry: " + line);
        return new FinancialTransaction_Record(
                TranType.valueOf(parts[0]),
                parts[1],
                Double.parseDouble(parts[2]),
                LocalDate.parse(parts[3])
        );
    }
}

public class FinanceTracker {
    private List<FinancialTransaction_Record> records = new ArrayList<>();
    private static final Scanner scanner = new Scanner(System.in);
    private static final List<String> INCOME_CATEGORIES = Arrays.asList("Salary", "Business", "Investment", "Other");
    private static final List<String> EXPENSE_CATEGORIES = Arrays.asList("Food", "Rent", "Travel", "Utilities", "Entertainment", "Other");
    public String lastFilePath = null;

    public void addTransaction() {
        System.out.println("Enter type (1 for INCOME, 2 for EXPENSE):");
        int typeChoice;
        try {
            typeChoice = Integer.parseInt(scanner.nextLine());
        } catch (NumberFormatException e) {
            System.out.println("Invalid input. Please enter 1 or 2.");
            return;
        }

        if (typeChoice != 1 && typeChoice != 2) {
            System.out.println("Invalid type. Please enter 1 or 2.");
            return;
        }

        TranType type = (typeChoice == 1) ? TranType.INCOME : TranType.EXPENSE;
        List<String> validCategories = (type == TranType.INCOME) ? INCOME_CATEGORIES : EXPENSE_CATEGORIES;

        System.out.println("Choose category:");
        for (int i = 0; i < validCategories.size(); i++) {
            System.out.printf("%d. %s\n", i + 1, validCategories.get(i));
        }

        int categoryChoice;
        try {
            categoryChoice = Integer.parseInt(scanner.nextLine());
        } catch (NumberFormatException e) {
            System.out.println("Invalid category choice.");
            return;
        }

        if (categoryChoice < 1 || categoryChoice > validCategories.size()) {
            System.out.println("Invalid category choice.");
            return;
        }

        String category = validCategories.get(categoryChoice - 1);

        System.out.println("Enter amount:");
        double amount;
        try {
            amount = Double.parseDouble(scanner.nextLine());
            if (amount <= 0) {
                System.out.println("Amount must be positive.");
                return;
            }
        } catch (NumberFormatException e) {
            System.out.println("Invalid amount.");
            return;
        }

        LocalDate date = LocalDate.now();
        System.out.println("Enter date (YYYY-MM-DD) or press Enter for today:");
        String dateInput = scanner.nextLine().trim();
        if (!dateInput.isEmpty()) {
            try {
                date = LocalDate.parse(dateInput);
            } catch (DateTimeParseException e) {
                System.out.println("Invalid date format. Using today.");
            }
        }

        records.add(new FinancialTransaction_Record(type, category, amount, date));
        System.out.println("Transaction added.");
    }

    public void viewMonthlySummary(int month) {
        double totalIncome = 0, totalExpense = 0;
        System.out.println("\nSummary for " + Month.of(month));

        for (FinancialTransaction_Record record : records) {
            if (record.getDate().getMonthValue() == month) {
                if (record.getType() == TranType.INCOME) {
                    totalIncome += record.getAmount();
                } else {
                    totalExpense += record.getAmount();
                }
            }
        }

        System.out.printf("Total Income : $%.2f\n", totalIncome);
        System.out.printf("Total Expense: $%.2f\n", totalExpense);
        System.out.printf("Balance      : $%.2f\n", (totalIncome - totalExpense));
    }

    public void viewSummaryByCategory(String category) {
        double totalIncome = 0, totalExpense = 0;
        System.out.println("\nSummary for category: " + category);

        for (FinancialTransaction_Record record : records) {
            if (record.getCategory().equalsIgnoreCase(category)) {
                if (record.getType() == TranType.INCOME) {
                    totalIncome += record.getAmount();
                } else {
                    totalExpense += record.getAmount();
                }
            }
        }

        System.out.printf("Total Income : $%.2f\n", totalIncome);
        System.out.printf("Total Expense: $%.2f\n", totalExpense);
        System.out.printf("Balance      : $%.2f\n", (totalIncome - totalExpense));
    }

    public void viewSummaryByDateRange(LocalDate start, LocalDate end) {
        double totalIncome = 0, totalExpense = 0;
        System.out.println("\nSummary from " + start + " to " + end);

        for (FinancialTransaction_Record record : records) {
            if (!record.getDate().isBefore(start) && !record.getDate().isAfter(end)) {
                if (record.getType() == TranType.INCOME) {
                    totalIncome += record.getAmount();
                } else {
                    totalExpense += record.getAmount();
                }
            }
        }

        System.out.printf("Total Income : $%.2f\n", totalIncome);
        System.out.printf("Total Expense: $%.2f\n", totalExpense);
        System.out.printf("Balance      : $%.2f\n", (totalIncome - totalExpense));
    }

    public void saveToFile(String filename) {
        try (PrintWriter pw = new PrintWriter(new FileWriter(filename))) {
            for (FinancialTransaction_Record record : records) {
                pw.println(record);
            }
            lastFilePath = filename;
            System.out.println("Data saved to " + filename);
        } catch (IOException e) {
            System.out.println("Error saving to file: " + e.getMessage());
        }
    }

    public void loadFromFile(String filename) {
        File file = new File(filename);
        if (!file.exists()) {
            System.out.println("File not found: " + filename);
            return;
        }

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            records.clear();
            String line;
            while ((line = br.readLine()) != null) {
                try {
                    FinancialTransaction_Record record = FinancialTransaction_Record.fromString(line);
                    records.add(record);
                } catch (Exception e) {
                    System.out.println("Skipping invalid entry: " + line);
                }
            }
            lastFilePath = filename;
            System.out.println("Data loaded from " + filename);
        } catch (IOException e) {
            System.out.println("Error loading file: " + e.getMessage());
        }
    }

    public void generateSampleFileIfNotExists(String filename) {
        File file = new File(filename);
        if (!file.exists()) {
            try (PrintWriter pw = new PrintWriter(new FileWriter(file))) {
                pw.println("INCOME,Salary,3000.0," + LocalDate.now());
                pw.println("EXPENSE,Food,200.0," + LocalDate.now());
                System.out.println("Sample file created: " + filename);
            } catch (IOException e) {
                System.out.println("Failed to create sample file.");
            }
        }
    }

    public void handleViewSummary() {
        System.out.println("1. By Month\n2. By Category\n3. By Date Range");
        int option;
        try {
            option = Integer.parseInt(scanner.nextLine());
        } catch (NumberFormatException e) {
            System.out.println("Invalid input.");
            return;
        }

        switch (option) {
            case 1:
                System.out.println("Enter month (1-12):");
                int month = Integer.parseInt(scanner.nextLine());
                if (month < 1 || month > 12) {
                    System.out.println("Invalid month.");
                    return;
                }
                viewMonthlySummary(month);
                break;
            case 2:
                System.out.println("Enter category:");
                String category = scanner.nextLine();
                viewSummaryByCategory(category);
                break;
            case 3:
                try {
                    System.out.println("Enter start date (YYYY-MM-DD):");
                    LocalDate start = LocalDate.parse(scanner.nextLine());
                    System.out.println("Enter end date (YYYY-MM-DD):");
                    LocalDate end = LocalDate.parse(scanner.nextLine());
                    if (end.isBefore(start)) {
                        System.out.println("End date cannot be before start date.");
                        return;
                    }
                    viewSummaryByDateRange(start, end);
                } catch (DateTimeParseException e) {
                    System.out.println("Invalid date format.");
                }
                break;
            default:
                System.out.println("Invalid option.");
        }
    }

    private void openFile(String filepath) {
        try {
            Runtime.getRuntime().exec(new String[]{"cmd", "/c", "start", "\"\"", filepath});
        } catch (IOException e) {
            System.out.println("Could not open file.");
        }
    }

    public static void main(String[] args) {
        FinanceTracker tracker = new FinanceTracker();
        String userHome = System.getProperty("user.home");
        String downloadsPath = userHome + File.separator + "Downloads";
        String sampleFilePath = downloadsPath + File.separator + "transactions.txt";

        tracker.generateSampleFileIfNotExists(sampleFilePath);

        while (true) {
            System.out.println("\n--- Finance Tracker ---");
            System.out.println("1. Add Transaction");
            System.out.println("2. View Summary");
            System.out.println("3. Save to File");
            System.out.println("4. Load from File");
            System.out.println("5. Exit");

            int choice;
            try {
                choice = Integer.parseInt(scanner.nextLine());
            } catch (NumberFormatException e) {
                System.out.println("Invalid choice.");
                continue;
            }

            switch (choice) {
                case 1:
                    tracker.addTransaction();
                    break;
                case 2:
                    tracker.handleViewSummary();
                    break;
                case 3:
                    System.out.println("Enter filename to save:");
                    String saveFile = scanner.nextLine().trim();
                    if (!saveFile.endsWith(".txt")) saveFile += ".txt";
                    tracker.saveToFile(downloadsPath + File.separator + saveFile);
                    break;
                case 4:
                    System.out.println("Enter filename to load:");
                    String loadFile = scanner.nextLine().trim();
                    if (!loadFile.endsWith(".txt")) loadFile += ".txt";
                    tracker.loadFromFile(downloadsPath + File.separator + loadFile);
                    break;
                case 5:
                    System.out.println("Exiting...");
                    if (tracker.lastFilePath != null) {
                        tracker.openFile(tracker.lastFilePath);
                    }
                    scanner.close();
                    return;
                default:
                    System.out.println("Invalid option.");
            }
        }
    }
}
