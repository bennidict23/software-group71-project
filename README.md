# Finance Management System

## Overview

This is a personal finance management system implemented in Java using JavaFX for the user interface. It allows users to:

* Register, login, and manage accounts
* Set and view saving goals and budgets
* Record, import(from csv files downloaded from Wechat, Alipay or template csv we provided), and analyze transactions
* View spending statistics in charts (pie and line chart)
* Reset and change passwords

All user data is stored in local CSV files for simplicity.

---

## Requirements

* **Java 17** or above
* **JavaFX 17** (Make sure you have the JavaFX SDK installed)
* **Maven** (recommended for managing dependencies)
* Runs on **Windows**, **Mac**, or **Linux**

---

## Project Structure

```
software-group71-project/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── org/
│   │   │       └── example/
│   │   │           ├── DashboardView.java
│   │   │           ├── LoginFrame.java
│   │   │           ├── RegisterFrame.java
│   │   │           ├── User.java
│   │   │           ├── UserManager.java
│   │   │           ├── GoalSettingsView.java
│   │   │           ├── BudgetSettingsView.java
│   │   │           ├── ChangePasswordView.java
│   │   │           ├── TransactionViewer.java
│   │   │           ├── analysis/
│   │   │           │   ├── AIModelService.java
│   │   │           │   ├── AnalysisView.java
│   │   │           │   ├── BudgetRecommendationView.java
│   │   │           │   ├── SpendingForecastView.java
│   │   │           │   └── SpendingStructureChart.java
│   │   │           ├── dataimport/
│   │   │           │   ├── DataImportController.java
│   │   │           │   ├── DataImportModel.java
│   │   │           │   └── DataImportView.java
│   │   │           ├── list/
│   │   │           │   ├── Transaction.java
│   │   │           │   ├── TransactionController.java
│   │   │           │   ├── TransactionLoader.java
│   │   │           │   └── TransactionView.java
│   │   │           └── utils/
│   │   │               ├── CategoryRulesManager.java
│   │   │               ├── DeepSeekCategoryService.java
│   │   │               ├── LoadingUtils.java
│   │   │               ├── UIUtils.java
│   │   │               └── ConsumerTrendChart.java
│   │   └── resources/
│   │       └── img/
│   │            └── user.img
│   │       
│   └── test/
│       └── java/
│           └── org/
│               └── example/
│                   ├── DashboardViewTest.java
│                   ├── LoginFrameTest.java
│                   ├── RegisterFrameTest.java
│                   ├── UserTest.java
│                   ├── UserManagerTest.java
│                   ├── GoalSettingsViewTest.java
│                   └── BudgetSettingsViewTest.java
├── users.csv
├── user_settings.csv
├── pom.xml
├── <username>_transactions.csv
└── README.md

```

---

## How to Run

### 1. **Clone or unzip the project**

Unzip `software-group71-project.zip` or clone your repository.

### 2. **Install JavaFX**

Download JavaFX SDK 17 from [https://gluonhq.com/products/javafx/](https://gluonhq.com/products/javafx/)
Extract it, e.g. to `C:\javafx-sdk-17.0.1` (Windows) or `/usr/local/javafx-sdk-17.0.1` (Linux/Mac).

### 3. **Open the project in IntelliJ IDEA**

* Open IntelliJ IDEA
* Click **Open** and select the project root folder

### 4. **Configure JavaFX in IntelliJ IDEA**

#### (a) **Add JavaFX library**

* Go to `File > Project Structure > Libraries`
* Click `+` to add new library, select Java, and choose the JavaFX SDK `/lib` directory

#### (b) **Set VM options**

To run JavaFX applications, you **must** specify the VM options with JavaFX modules.
Go to `Run > Edit Configurations...`
Add the following to **VM options** (replace with your actual JavaFX path):

**Windows:**

```
--module-path "C:\javafx-sdk-17.0.1\lib" --add-modules javafx.controls,javafx.fxml
```

**Mac/Linux:**

```
--module-path /usr/local/javafx-sdk-17.0.1/lib --add-modules javafx.controls,javafx.fxml
```

#### (c) **Set Main Class**

Set the main class to `org.example.LoginFrame` (or whichever is your entry point).

---

## Usage

1. **Run the application**
   The login screen will appear.

2. **Register a new user**
   Click "Register" and fill in your credentials.

3. **Login**
   Enter your username and password.

4. **Dashboard**

   * View current savings and remaining budget
   * See spending charts and recent transaction trends

5. **Set goals/budget**
   Use the navigation bar to set annual/monthly saving goals and budgets.

6. **Import/record transactions**
   Use "Data Import" or "Transaction Viewer" to add, import, or review spending records.

7. **Analysis**
   Use the "Analysis" page for further statistics.

8. **Password management**
   Use "Change Password" or "Forgot Password" for credential management.

---

## Notes

* All user, setting, and transaction data are saved as CSV files in the project directory.
* If you see errors about JavaFX not found, **double check your VM options**.
* For UI icons or backgrounds, place your images under the `resources/` directory and reference them in code.

---

## Troubleshooting

* **"Missing JavaFX runtime components"**: Check that your VM options include the correct `--module-path` and `--add-modules` entries.
* **File not found**: The program auto-creates missing files on first launch, but make sure your working directory is correct.
* **Login/register errors**: Usernames must be unique; passwords are stored in plain text in CSV for demo purposes only.

---

## How to Run Tests

If your project includes unit tests (e.g., with JUnit):

* Right-click on the test class or directory and choose `Run`
* Or, run `mvn test` in the terminal if using Maven

---

## Author

*Group 71*
*Beijing University of Posts and Telecommunications*
*2024-2025*

---

**Good luck using your Finance Management System!**

---


