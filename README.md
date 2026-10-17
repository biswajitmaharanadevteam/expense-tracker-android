# ExpenseMate 🪙 — Production-Ready Android Expense Tracker

**ExpenseMate** is a native, modern, offline-first personal finance management application built with **Jetpack Compose**, **Material 3**, **Room SQLite**, and **Clean MVVM Architecture**.

It enables users to track daily expenses, log income streams, configure category and monthly budgets, visualize spending patterns via custom canvas charts, manage accounts/wallets, schedule recurring transactions, and export audited financial reports to PDF and CSV — all while keeping 100% of user data strictly on the local device.

---

## 📱 Features & Highlights

- **Interactive Financial Dashboard:**
  - Real-time Total Balance aggregated across all active wallets and accounts.
  - Period Income, Expense, and Net Cash Flow (Surplus/Deficit).
  - Quick action shortcuts for instant logging (+ Income, - Expense, Manage Budgets).
  - Interactive Donut Chart for category breakdown and Bar Chart for 4-month comparisons.
  - Recent transactions list with direct edit/delete controls.
- **Transaction Management:**
  - Multi-attribute transactions: Title, Amount (decimal), Expense/Income type, Category, Account, Payment Method (Cash, Bank Account, UPI, Credit Card, Savings Account), Date/Time, Payee/Merchant, Notes, Tags.
  - Search across title, notes, payee, tags, and category name.
  - Multi-filter sheet: Type, Category, Payment Method, Date Range, and Sorting (Newest, Oldest, Highest, Lowest).
  - Grouped by day with daily totals.
  - Duplicate transaction shortcut and safe deletion with confirmation.
- **Budgeting & Spending Control:**
  - Overall monthly limits and category-specific spending limits.
  - Spending calculations automatically computed from actual expense records.
  - Visual progress indicators with color-coded alerts (<80% Green, 80-100% Amber warning, >100% Red overrun).
  - Month/Year switcher.
- **Reports & Analytics:**
  - Period filters: This Week, This Month, This Year, Custom Date Range.
  - Period-over-period comparison (percentage change vs. preceding equal period).
  - Spending trend line chart with daily points.
  - Largest transactions and average daily spending calculation.
  - Native export & share of financial reports to **PDF** and **CSV**.
- **Accounts & Wallets:**
  - Independent balances for Cash, Bank Account, UPI, Credit Card, and Savings Account.
  - Opening balance configuration.
  - Safe deletion: prevents deleting accounts with associated transaction records.
- **Categories:**
  - 15 default expense categories + 7 default income categories.
  - Create custom categories with custom icons and color palette tints.
  - Deletion safety protection for categories with active transactions.
- **Automated Background Work:**
  - WorkManager recurring transaction worker for daily, weekly, monthly, and yearly recurrences.
  - Idempotent deduplication logic to prevent double billing.
  - Daily entry reminder notifications at 8:00 PM.
- **Security & Privacy:**
  - 100% Offline operation.
  - Zero analytics, zero ad trackers, zero third-party cloud synchronization.
  - Full JSON backup and restore capabilities.
  - Clean data reset with double verification.

---

## 🛠 Technology Stack & Requirements

| Component | Technology | Version |
|---|---|---|
| **Language** | Kotlin | 2.0.21 |
| **Android Gradle Plugin** | AGP | 8.7.2 |
| **Gradle** | Gradle Wrapper | 8.10.2 |
| **Minimum SDK** | Android 7.0 (API 24) | Compatible with >96% of global active Android devices |
| **Target / Compile SDK** | Android 15 (API 35) | Latest stable Android standard |
| **JDK Required** | OpenJDK / Java SE | 17+ (JDK 17 or JDK 21 recommended) |
| **UI Framework** | Jetpack Compose | BOM 2024.11.00 (Material 3 v1.3.1) |
| **Database** | AndroidX Room | 2.6.1 with KSP code generator |
| **Preferences** | AndroidX DataStore | 1.1.1 Preferences |
| **Background Work** | AndroidX WorkManager | 2.10.0 |
| **Architecture** | MVVM + Clean Architecture | Repository Pattern + Kotlin StateFlows |

---

## 🚀 Getting Started & Android Studio Setup

### 1. Prerequisites
- Download and install [Android Studio](https://developer.android.com/studio) (Ladybug, Koala, or Iguana).
- Install **JDK 17** or ensure Android Studio's bundled `jbr` (JetBrains Runtime 17+) is selected.
- In Android Studio SDK Manager, install:
  - **Android SDK Platform 35**
  - **Android SDK Build-Tools 35.0.0**
  - **Android SDK Command-line Tools**

### 2. Opening the Project
1. Launch Android Studio.
2. Select **File > Open...** and navigate to:
   ```
   c:\Users\DELL\Documents\expence-tracker-android-app
   ```
3. Allow Gradle to synchronize dependencies. Android Studio will resolve all version catalog artifacts automatically.

---

## 📲 Running on Emulators and Physical Devices

### Running on Android Emulator
1. In Android Studio, open **Device Manager** (**Tools > Device Manager**).
2. Click **Create Device**:
   - Phone: Pixel 8 / Pixel 7 (API 34 or 35).
   - Tablet: Pixel Tablet (API 34 or 35) to test adaptive layouts and NavigationRail.
3. Start the virtual device.
4. Select the device in the target device dropdown at the top toolbar.
5. Click **Run 'app'** (`Shift + F10`).

### Running on Physical Android Device
1. On your phone, enable Developer Options:
   - Go to **Settings > About phone**.
   - Tap **Build Number** 7 times until Developer Mode is unlocked.
2. In **Settings > System > Developer options**:
   - Turn on **USB debugging**.
   - Turn on **Install via USB** (if prompted on Xiaomi / Oppo / Vivo devices).
3. Connect your phone via USB cable and select **Transfer files / MTP**.
4. When prompted on your phone, check **Always allow from this computer** and tap **Allow**.
5. Select your phone in Android Studio's device selector and click **Run**.

---

## 📦 Building APK and AAB Packages

### 1. Generating a Debug APK (Instant Testing)
Run the following in the project root directory:

**Windows (PowerShell):**
```powershell
.\gradlew.bat assembleDebug
```

**macOS / Linux:**
```bash
./gradlew assembleDebug
```

Output APK location:
```
app/build/outputs/apk/debug/app-debug.apk
```
You can transfer this `.apk` directly to your phone via USB or WhatsApp/Drive and install it.

---

### 2. Generating a Signed Release APK / AAB

#### Step A: Create a Secure Keystore (One-Time Setup)
Run the `keytool` command (available in any JDK `bin/` directory):
```bash
keytool -genkey -v -keystore expensemate-release-key.jks -alias expensemate-key -keyalg RSA -keysize 2048 -validity 10000
```
Enter your password, organization name, and country code. Store `expensemate-release-key.jks` in a secure location outside public version control.

#### Step B: Securely Configure Credentials in `gradle.properties`
Add the following to your **user-level** `~/.gradle/gradle.properties` (or project root for CI/CD):
```properties
EXPENSEMATE_KEYSTORE_PATH=c:/path/to/expensemate-release-key.jks
EXPENSEMATE_KEYSTORE_PASSWORD=your_keystore_password
EXPENSEMATE_KEY_ALIAS=expensemate-key
EXPENSEMATE_KEY_PASSWORD=your_key_password
```

#### Step C: Build the Signed Release Android App Bundle (AAB) for Play Store
```powershell
.\gradlew.bat bundleRelease
```
Output AAB location:
```
app/build/outputs/bundle/release/app-release.aab
```

#### Step D: Build the Signed Standalone Release APK
```powershell
.\gradlew.bat assembleRelease
```
Output APK location:
```
app/build/outputs/apk/release/app-release.apk
```

---

## 🧪 Automated Testing

Run all unit tests:
```powershell
.\gradlew.bat test
```

Run connected UI instrumentation tests on an active device or emulator:
```powershell
.\gradlew.bat connectedAndroidTest
```

### Unit Tests Included:
- `CalculationsTest`: Validates financial summary calculations, balance formulas, surplus/deficit cash flow, and category percentage distribution.
- `BudgetUseCaseTest`: Validates spending calculations, remaining balances, 80% warning triggers, and 100% budget breach thresholds.
- `CurrencyFormatterTest`: Validates standard and compact INR/USD formatting, negative numbers, and comma/dot decimal parsing.
- `CsvExportImportTest`: Validates round-trip CSV serialization, escaping of commas/quotes, and rejection of negative or zero values during import.
- `RecurringTransactionTest`: Validates daily, weekly, and monthly recurrence execution, rule deactivation on expiration, and duplicate prevention.
- `FilterTransactionsTest`: Validates full-text searching, multi-filter criteria, and sort order permutations.
- `ExpenseMateUiTest`: Verifies Compose UI elements, cards, transaction rows, and empty states.

---

## 📁 Project Architecture & Directory Structure

```
ExpenseMate/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── AndroidManifest.xml
│   │   │   ├── java/com/expensemate/app/
│   │   │   │   ├── ExpenseMateApp.kt             # Application class & WorkManager scheduler
│   │   │   │   ├── MainActivity.kt               # Edge-to-edge adaptive root Activity
│   │   │   │   ├── data/
│   │   │   │   │   ├── local/                    # Room DB, DAOs, Entities, Migrations
│   │   │   │   │   ├── preferences/              # DataStore Preferences Manager
│   │   │   │   │   └── repository/               # Repository interfaces & Room implementation
│   │   │   │   ├── domain/
│   │   │   │   │   ├── model/                    # Immutable business domain models
│   │   │   │   │   └── usecase/                  # Clean architecture Use Cases
│   │   │   │   ├── ui/
│   │   │   │   │   ├── theme/                    # Material 3 color palette, typography, shape
│   │   │   │   │   ├── components/               # Canvas Charts, Cards, Rows, Empty States
│   │   │   │   │   ├── dashboard/                # Home dashboard Screen & ViewModel
│   │   │   │   │   ├── transactions/             # Transactions History & Add/Edit screens
│   │   │   │   │   ├── budgets/                  # Monthly & Category Budgets screen
│   │   │   │   │   ├── reports/                  # Analytics & Report Sharing screen
│   │   │   │   │   ├── accounts/                 # Accounts & Wallets management
│   │   │   │   │   ├── categories/               # Category management
│   │   │   │   │   ├── settings/                 # App Settings & Backup screen
│   │   │   │   │   └── navigation/               # NavGraph & Route definitions
│   │   │   │   ├── worker/                       # WorkManager background workers
│   │   │   │   ├── export/                       # PDF, CSV, and JSON managers
│   │   │   │   └── notifications/                # Android Notification Channels & alerts
│   │   │   └── res/                              # Adaptive Icons, Strings, Color tokens, XML rules
│   │   ├── test/                                 # Complete Unit Test Suite
│   │   └── androidTest/                          # Compose UI Instrumentation Tests
├── gradle/libs.versions.toml                     # Centralized Gradle Version Catalog
├── build.gradle.kts                              # Root build script
├── settings.gradle.kts                           # Module and repo definitions
└── README.md                                     # Project Guide & Documentation
```

---

## 🔒 Privacy Guarantee

ExpenseMate is committed to user financial sovereignty. No transaction, account name, balance, or note is ever uploaded to remote servers or shared with third-party advertising SDKs.
