# Transaction Class Documentation

## Overview
The `Transaction` class represents a financial transaction with various properties such as user, source, date, amount, category, and description. It uses JavaFX properties to enable observable features for UI binding.

## Package
`org.example.list`

## Dependencies
- `javafx.beans.property.*`
- `java.time.LocalDate`

## Properties

| Property | Type | Description |
|----------|------|-------------|
| user | `StringProperty` | The user associated with the transaction |
| source | `StringProperty` | The source of the transaction |
| date | `ObjectProperty<LocalDate>` | The date of the transaction |
| amount | `DoubleProperty` | The monetary amount of the transaction |
| category | `StringProperty` | The category of the transaction |
| description | `StringProperty` | Additional description of the transaction |

## Constructors

### `Transaction(String user, String source, LocalDate date, double amount, String category, String description)`
Creates a new Transaction with all properties initialized.

**Parameters:**
- `user`: The user associated with the transaction
- `source`: The source of the transaction
- `date`: The date when the transaction occurred
- `amount`: The monetary amount of the transaction
- `category`: The category of the transaction
- `description`: Additional description of the transaction

## Property Access Methods

| Method | Return Type | Description |
|--------|-------------|-------------|
| `userProperty()` | `StringProperty` | Returns the user property |
| `sourceProperty()` | `StringProperty` | Returns the source property |
| `dateProperty()` | `ObjectProperty<LocalDate>` | Returns the date property |
| `amountProperty()` | `DoubleProperty` | Returns the amount property |
| `categoryProperty()` | `StringProperty` | Returns the category property |
| `descriptionProperty()` | `StringProperty` | Returns the description property |

## Getter Methods

| Method | Return Type | Description |
|--------|-------------|-------------|
| `getUser()` | `String` | Returns the user value |
| `getSource()` | `String` | Returns the source value |
| `getDate()` | `LocalDate` | Returns the date value |
| `getAmount()` | `double` | Returns the amount value |
| `getCategory()` | `String` | Returns the category value |
| `getDescription()` | `String` | Returns the description value |

## Usage Example
```java
// Creating a new transaction
Transaction transaction = new Transaction(
    "JohnDoe", 
    "Bank Account", 
    LocalDate.now(), 
    125.50, 
    "Groceries", 
    "Weekly grocery shopping"
);

// Accessing properties
System.out.println("User: " + transaction.getUser());
System.out.println("Amount: " + transaction.getAmount());

// JavaFX property binding example
label.textProperty().bind(transaction.userProperty());# TransactionController Class Documentation

## Overview
The `TransactionController` class acts as the controller in a MVC (Model-View-Controller) pattern for managing financial transactions. It handles data loading, searching, and interaction between the view and data model.

## Package
`org.example.list`

## Dependencies
- `javafx.collections.*`
- `javafx.scene.control.Alert`
- `javafx.scene.input.KeyCode`
- `java.time.LocalDate`
- `java.time.format.DateTimeFormatter`
- `java.io.IOException`

## Fields

| Field | Type | Description |
|-------|------|-------------|
| `view` | `TransactionView` | Reference to the view component |
| `loader` | `TransactionLoader` | Handles loading of transaction data |
| `data` | `ObservableList<Transaction>` | Main data store for transactions |
| `filteredData` | `FilteredList<Transaction>` | Filtered view of the transaction data |

## Constructors

### `TransactionController(TransactionView view, TransactionLoader loader)`
Initializes the controller with view and loader components.

**Parameters:**
- `view`: The associated view component
- `loader`: The data loader component

**Initialization Behavior:**
1. Binds UI actions
2. Creates filtered list from main data
3. Updates view with initial data
4. Loads initial data

## Methods

### `bindActions()`
Binds UI controls to their respective handlers:
- Load button: Triggers data reload
- Search button: Triggers search
- Search field: Also triggers search on Enter key press

### `loadData()`
Loads transaction data from "transactions.csv" file through the loader component.

**Error Handling:**
- Shows error alert for IO failures
- Shows error alert for data format issues

### Search Functionality

#### `updateSearchFilter(String filter)`
Applies search filter to transaction data by setting a predicate on the filtered list.

**Search Fields:**
- User
- Source 
- Category
- Description
- Amount (numeric match)
- Date (format: yyyy-MM-dd)

#### `triggerSearch()`
Triggers search operation by getting text from search field and updating filter.

#### `matchesField(String value, String filter)`
Checks if string field contains the filter text (case-insensitive).

#### `matchesNumber(double amount, String filter)`
Attempts to parse filter as number and match against transaction amount.

#### `matchesDate(LocalDate date, String filter)`
Checks if date formatted as "yyyy-MM-dd" contains the filter text.

### `showError(String title, String message)`
Displays an error alert dialog with given title and message.

## Usage Example
```java
// Initialize components
TransactionView view = new TransactionView();
TransactionLoader loader = new CsvTransactionLoader();

// Create controller
TransactionController controller = new TransactionController(view, loader);

// The controller now handles all interactions between view and data# TransactionLoader Class Documentation

## Overview
The `TransactionLoader` class is responsible for loading transaction data from a CSV file and converting it into a list of `Transaction` objects.

## Package
`org.example.list`

## Dependencies
- `java.io.*`
- `java.time.LocalDate`
- `java.util.ArrayList`
- `java.util.List`

## Methods

### `loadTransactions(String filePath)`
Loads transactions from a CSV file and returns them as a list of `Transaction` objects.

**Parameters:**
- `filePath`: String - Path to the CSV file containing transaction data

**Returns:**
- `List<Transaction>` - A list of loaded transaction objects

**Throws:**
- `IOException` - If there's an error reading the file

**File Format Expectations:**
The CSV file should have the following format (with a header row that will be skipped):# TransactionView Class Documentation

## Overview
The `TransactionView` class represents the view component in the MVC pattern for displaying financial transactions. It provides a JavaFX-based UI with a table for transaction data, search functionality, and navigation controls.

## Package
`org.example.list`

## Dependencies
- `javafx.scene.control.*`
- `javafx.scene.layout.*`
- `javafx.stage.Stage`
- `java.time.LocalDate`
- `org.example.DashboardView`

## UI Components

### Main Components
| Component | Type | Description |
|-----------|------|-------------|
| `root` | `BorderPane` | Main container for all UI elements |
| `table` | `TableView<Transaction>` | Displays transaction data in tabular format |
| `searchField` | `TextField` | Input field for search queries |
| `searchButton` | `Button` | Triggers search operation |
| `loadButton` | `Button` | Button to reload/update data |
| `btnBack` | `Button` | Navigation button to return to dashboard |

### Table Columns
| Column | Type | Binds To |
|--------|------|----------|
| User | `String` | `userProperty()` |
| Source | `String` | `sourceProperty()` |
| Date | `LocalDate` | `dateProperty()` |
| Amount | `Double` | `amountProperty()` |
| Category | `String` | `categoryProperty()` |
| Description | `String` | `descriptionProperty()` |

## Methods

### Constructors
#### `TransactionView()`
Initializes the view by:
1. Configuring the transaction table
2. Setting up the UI layout

### Private Methods
#### `configureTable()`
- Creates and configures all table columns
- Sets up cell value factories for data binding
- Applies constrained resize policy for columns

#### `layoutUI()`
Organizes the UI components in a hierarchical structure:
1. Top: Back button (styled green)
2. Middle: Search components (field + button)
3. Center: Transaction table
4. Bottom: Load button

#### `returnToDashboard()`
- Closes current window
- Preserves user state
- Opens new DashboardView window

### Public Methods
#### `getView()`
Returns the root `BorderPane` container

#### `getLoadButton()`
Returns the load/update button

#### `getSearchField()`
Returns the search text field

#### `getSearchButton()`
Returns the search button

#### `updateTable(ObservableList<Transaction> data)`
Updates the table with new transaction data

## UI Structure
```plaintext
BorderPane (root)
└── VBox (mainContainer)
    ├── Button (btnBack) - "Dashboard"
    ├── HBox (searchBox)
    │   ├── TextField (searchField)
    │   └── Button (searchButton) - "Search"
    ├── TableView (table)
    └── Button (loadButton) - "Update Data"# TransactionViewer Class Documentation

## Overview
The `TransactionViewer` class serves as the main entry point for the transaction viewing application. It extends JavaFX's `Application` class to create and launch the transaction management interface.

## Package
`org.example.list`

## Dependencies
- `javafx.application.Application`
- `javafx.scene.Scene`
- `javafx.stage.Stage`

## Constants
| Constant | Type | Value | Description |
|----------|------|-------|-------------|
| `WIDTH` | `int` | 800 | Default window width |
| `HEIGHT` | `int` | 600 | Default window height |

## Methods

### `showTransactionView()`
Public static method to launch the application.

**Usage:**
```java
TransactionViewer.showTransactionView();# ConsumerTrendChart Class Documentation

## Overview
The `ConsumerTrendChart` class generates a bar chart visualization of a user's daily spending trends over the past 7 days (including today). It filters and processes transaction data specific to the current user.

## Package
`org.example`

## Dependencies
- `javafx.scene.chart.*`
- `java.io.*`
- `java.time.*`
- `java.util.*`

## Fields

| Field | Type | Description |
|-------|------|-------------|
| `TRANSACTION_FILE` | `static final String` | Path to transaction data file ("transactions.csv") |
| `currentUser` | `User` | Reference to the currently logged-in user |

## Constructor

### `ConsumerTrendChart(User currentUser)`
Initializes the chart generator with the current user context.

**Parameters:**
- `currentUser`: The user whose spending data will be visualized

## Public Methods

### `createChart()`
Creates and configures a JavaFX BarChart showing daily spending.

**Returns:**
- `BarChart<String, Number>` - Configured bar chart visualization

**Chart Configuration:**
- X-axis: Dates (CategoryAxis)
- Y-axis: Amount spent in dollars (NumberAxis)
- Series name: "Total Daily Spending"
- Data: Sorted daily totals for the past 7 days

## Private Methods

### `calculateDailySpending()`
Calculates daily spending totals from transaction data.

**Returns:**
- `Map<LocalDate, Double>` - Sorted map of dates to spending amounts

**Processing Logic:**
1. Determines date range (today and previous 6 days)
2. Reads transaction file line by line
3. Filters transactions:
   - Only current user's transactions
   - Within the 7-day window
4. Aggregates amounts by date
5. Ensures all 7 days are represented (even with $0)
6. Sorts results chronologically

**Data File Format:**
Expected CSV format (with header):# DashboardView Class Documentation

## Overview
The `DashboardView` class serves as the main user interface for the personal finance management application. It provides a comprehensive dashboard displaying user information, budget tracking, savings goals, and spending trends. The class implements JavaFX's `Application` interface to create a graphical user interface.

## Package
`org.example`

## Dependencies
- `javafx.application.*`
- `javafx.geometry.*`
- `javafx.scene.*`
- `javafx.scene.chart.*`
- `javafx.scene.control.*`
- `javafx.scene.layout.*`
- `javafx.stage.Stage`
- `org.example.list.TransactionViewer`
- `java.io.IOException`
- `java.util.concurrent.*`

## Class Fields

| Field | Type | Description |
|-------|------|-------------|
| `currentUser` | `static User` | Currently logged-in user |
| `userManager` | `UserManager` | Handles user-related operations |
| `passwordLabel` | `Label` | Displays user's password (masked) |
| `budgetLabel` | `Label` | Shows monthly budget information |
| `goalLabel` | `Label` | Displays savings goal information |
| `progressBar` | `ProgressBar` | Visualizes savings progress |
| `progressLabel` | `Label` | Shows savings progress percentage |
| `pageSelector` | `ComboBox<String>` | Navigation dropdown for different views |
| `formattedInput` | `FormattedInput` | Handles formatted input operations |
| `scheduler` | `ScheduledExecutorService` | Timer for periodic updates |

## Key Methods

### User Management
| Method | Description |
|--------|-------------|
| `setCurrentUser(User)` | Static method to set current user |
| `getCurrentUser()` | Static method to get current user |
| `showChangePasswordDialog()` | Displays password change dialog |
| `logout()` | Handles user logout process |

### Budget and Goals
| Method | Description |
|--------|-------------|
| `showGoalDialog()` | Shows dialog for setting savings goals |
| `showBudgetDialog()` | Displays budget configuration dialog |
| `updateSavedAmounts()` | Updates savings progress displays |

### UI Components
| Method | Description |
|--------|-------------|
| `start()` | Main JavaFX application method |
| `showAlert()` | Displays alert dialogs |
| `stop()` | Handles application shutdown |

## Application Features

### 1. User Information Section
- Displays username and password (masked)
- Change password functionality
- Logout button

### 2. Budget Tracking
- Monthly budget display
- Budget breakdown by category:
  - Shopping
  - Transport
  - Diet
  - Amusement
- Real-time budget updates

### 3. Savings Goals
- Monthly and annual savings targets
- Visual progress bar
- Percentage completion display
- Goal setting interface

### 4. Navigation
- Dropdown selector for:
  - Formatted Input view
  - Transaction Viewer
- Independent window management

### 5. Consumer Trends
- Visual spending chart (last 7 days)
- Automatic daily updates

## Implementation Details

### Initialization Flow
1. Checks for logged-in user
2. Verifies and resets monthly settings
3. Checks monthly expenses
4. Sets up main UI components
5. Starts periodic update scheduler

### Data Management
- Uses `UserManager` for persistence
- Automatically saves settings changes
- Periodic data refresh (every 10 seconds)

### UI Layout Hierarchy# TransactionViewer Class Documentation

## Overview
The `TransactionViewer` class serves as the main entry point for the transaction viewing application. It extends JavaFX's `Application` class to create and launch the transaction management interface.

## Package
`org.example.list`

## Dependencies
```java
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
// Additional dependencies...# RegisterFrame Class Documentation

## Overview
The `RegisterFrame` class provides a user registration interface built with JavaFX, implementing user account creation with validation logic.

## Package
`org.example`

## Dependencies
```java
import javafx.application.Application;
import javafx.geometry.*;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.stage.Stage;classDiagram
    class User {
        -String username
        -String password
        -double annualTarget
        -double monthlyTarget
        -double monthlyBudget
        -double shoppingBudget
        -double transportBudget
        -double dietBudget
        -double amusementBudget
        -double savedAmount
        -double annualSavedAmount
        -int currentYear
        -int currentMonth
        +User(String, String)
        +resetMonthlySettings()
        +resetAnnualSettings()
        // getters/setters...
    }# UserManager Class Documentation

## Overview
The `UserManager` class provides centralized user management functionality including authentication, registration, and financial tracking. It implements persistent storage using CSV files and includes automated transaction monitoring.

## Package
`org.example`

## Dependencies
```java
import javafx.application.Platform;
import javafx.scene.control.Alert;
import java.io.*;
import java.nio.file.Files;
import java.time.*;
import java.util.*;
import java.util.concurrent.*;
