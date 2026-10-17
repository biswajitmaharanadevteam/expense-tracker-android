# Compatibility and Quality Assurance Report: ExpenseMate

**App Version:** 1.0.0 (Release Candidate)  
**Package:** `com.expensemate.app`  
**Date:** October 17, 2026  
**Audience:** QA Engineers, Android Architects, Publishing Reviewers

---

## 1. Android Platform Compatibility Specification

| Dimension | Specification | Notes & Rationale |
|---|---|---|
| **Minimum SDK** | Android 7.0 (API 24 - Nougat) | Covers >96% of active Android devices globally; full support for Java 8 Time APIs, SQLite 3.12+, Room, and Compose BOM. |
| **Compile / Target SDK** | Android 15 (API 35 - Vanilla Ice Cream) | Conforms to Google Play's 2025/2026 target API requirements; supports edge-to-edge window insets and predictive back gestures. |
| **Java Runtimes** | JDK 17 (LTS), JDK 21 (LTS) | Fully compatible with Gradle 8.10.2 and Kotlin 2.0.21. |
| **Architectures** | `arm64-v8a`, `armeabi-v7a`, `x86_64` | Covers modern 64-bit phones as well as emulator environments. |

---

## 2. Form Factor & Adaptive Layout Matrix

ExpenseMate incorporates responsive design using `WindowType` (`Compact`, `Medium`, `Expanded`) and dynamic Compose layout containers.

| Device Category | Display Resolution | Window Class | Layout Adaptation & Behavior | Verification Status |
|---|---|---|---|---|
| **Compact Phone** (e.g. Pixel 4a, Galaxy S21) | 1080 x 2340 (411 x 892 dp) | Width: `Compact`<br>Height: `Medium` | Bottom `NavigationBar`, single-column vertical feed, stacked metric cards. Full IME keyboard insets handling. | **Passed** |
| **Large Smartphone** (e.g. Pixel 8 Pro, Galaxy S24 Ultra) | 1440 x 3120 (412 x 892 dp) | Width: `Compact`<br>Height: `Expanded` | Bottom `NavigationBar`, elevated card padding, full chart legends rendered without truncation. | **Passed** |
| **Landscape Orientation** | 2340 x 1080 (892 x 411 dp) | Width: `Expanded`<br>Height: `Compact` | Automatically shifts navigation from Bottom Bar to Left `NavigationRail`. Content reflows horizontally. | **Passed** |
| **Foldable Devices** (e.g. Galaxy Z Fold inner) | 1812 x 2176 (673 x 808 dp) | Width: `Medium`<br>Height: `Medium` | Dual-pane or expanded row metrics; multi-column category selection grid. | **Passed** |
| **Android Tablet** (e.g. Pixel Tablet, Galaxy Tab S9) | 2560 x 1600 (1280 x 800 dp) | Width: `Expanded`<br>Height: `Expanded` | Permanent left `NavigationRail`, 2-column or 4-column dashboard metric reflow, side-by-side transaction list and filters. | **Passed** |

---

## 3. Automated Test Suite Execution & Coverage

### Unit Test Verification (`app/src/test/`)

| Test Suite | File | Tests Executed | Assertion Objectives | Result |
|---|---|---|---|---|
| **Financial Calculations** | `CalculationsTest.kt` | 2 | - Total balance summation across multi-currency accounts<br>- Period income vs expense subtraction<br>- Net Cash Flow correctness<br>- Category spending percentage allocation | **PASS** |
| **Budget Enforcement** | `BudgetUseCaseTest.kt` | 1 | - Spending isolation by category and month<br>- Exclusion of income transactions from spending<br>- 80% threshold warning trigger<br>- 100% budget overrun flag | **PASS** |
| **Currency Formatter** | `CurrencyFormatterTest.kt` | 3 | - INR standard (`₹1,250.50`) and USD (`$500.00`) formatting<br>- Compact Indian numbering system (`₹15.0k`, `₹2.5L`, `₹1.2Cr`)<br>- Comma and dot decimal parsing; rejection of negatives | **PASS** |
| **CSV Export & Import** | `CsvExportImportTest.kt` | 2 | - Round-trip serialization/deserialization<br>- CSV quotation and comma escaping<br>- Row validation: skips negative/zero amounts and malformed dates | **PASS** |
| **Recurring Rules** | `RecurringTransactionTest.kt` | 2 | - Daily/weekly/monthly recurrence calculation<br>- Deduplication: guarantees no duplicate entry on same day<br>- Automatic deactivation upon reaching expiration date | **PASS** |
| **Filter & Search** | `FilterTransactionsTest.kt` | 3 | - Full-text search across titles, payees, notes<br>- Multi-attribute combination (Category + Type)<br>- Sorting by Amount and Date descending/ascending | **PASS** |

### UI & Instrumentation Test Suite (`app/src/androidTest/`)

| UI Test | Target Component | Verifications |
|---|---|---|
| `testTotalBalanceCardDisplaysCorrectValues` | `TotalBalanceCard` | Verifies balance text, currency symbol, income and expense sub-badges display correctly in Compose tree. |
| `testTransactionRowDisplaysDetails` | `TransactionRow` | Verifies category badge, merchant label, relative date, and negative expense sign formatting (`-₹1,250.00`). |
| `testEmptyStateDisplaysPrompt` | `EmptyState` | Verifies empty illustration, title, subtext, and action button exist when dataset is empty. |

---

## 4. Edge Cases, Security & Robustness Verification

1. **Zero / Empty Data State:**
   - Fresh install displays clean onboarding empty states with guidance on both Dashboard, Transactions, and Budgets.
   - Charts render informative empty placeholders rather than crashing on divide-by-zero angles.
2. **Safe Entity Deletion:**
   - Attempting to delete a category or account that has active transactions triggers an explanatory blocker dialog rather than a database foreign key constraint crash (`SQLiteConstraintException`).
3. **Configuration Change & Keyboard Handling:**
   - Forms use Jetpack Compose state hoisting with `rememberSaveable` and ViewModel state persistence across device rotations.
   - Soft keyboard avoids obscuring action buttons via `.imePadding()`.
4. **Android 13+ Runtime Permissions:**
   - Notification permissions are requested on first launch with graceful degradation if denied. Core financial tracking functions without requiring push access.
5. **Background Limits & WorkManager:**
   - Workers use `PeriodicWorkRequest` with `ExistingPeriodicWorkPolicy.KEEP` to survive device reboots without duplicating scheduled tasks.
