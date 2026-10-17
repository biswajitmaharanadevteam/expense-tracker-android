# Google Play Console Publishing Guide for ExpenseMate

This guide provides a checklist and step-by-step procedure to prepare, sign, test, and publish **ExpenseMate** to the Google Play Store.

---

## 📋 Pre-Launch Readiness Checklist

### 1. Developer Account & Identity Verification
- [ ] Active Google Play Developer Account ($25 one-time registration fee).
- [ ] Complete identity verification (D-U-N-S number for organizations or government ID for individuals).
- [ ] Complete merchant account setup if adding premium features later.

### 2. App Content & Policy Compliance
- [ ] **Privacy Policy URL:** Host a clear privacy policy stating that ExpenseMate processes all financial records locally on the user's device without remote data collection.
- [ ] **Data Safety Form in Play Console:**
  - *Does your app collect or share any user data?* Select **No**.
  - *Are all data transmissions encrypted in transit?* N/A (no data leaves the device).
  - *Can users request data deletion?* Yes (via the in-app "Reset All Application Data" feature).
- [ ] **Target Audience & Content:**
  - Target age: 18 and older.
  - Category: **Finance**.
  - Content rating questionnaire (IARC): Complete rating (typically rated Everyone / PEGI 3).
- [ ] **Financial Features Policy:**
  - Declare that ExpenseMate is a manual tracking tool and does not act as a licensed bank or money transmitter.
  - Confirm no actual banking credentials or passwords are collected.

### 3. Store Listing Assets
- [ ] **App Name:** ExpenseMate: Expense Tracker (up to 30 characters).
- [ ] **Short Description:** Simple, secure personal expense & budget tracker with offline privacy (up to 80 characters).
- [ ] **Full Description:** Detailed feature breakdown including categories, budgets, interactive charts, PDF reports, and offline security (up to 4000 characters).
- [ ] **App Icon:** High-resolution 512 x 512 px PNG (32-bit color with alpha).
- [ ] **Feature Graphic:** 1024 x 500 px JPG or 24-bit PNG (no alpha).
- [ ] **Screenshots:**
  - Phone: Minimum 4 screenshots (1080 x 2400 px or 16:9 ratio).
  - 7-inch Tablet: Minimum 1 screenshot showing expanded layout.
  - 10-inch Tablet: Minimum 1 screenshot showing NavigationRail layout.

---

## 🔑 Secure Keystore Setup & App Signing

Google Play requires all new apps to be published as **Android App Bundles (.aab)** signed with **Play App Signing**.

### Step 1: Generate Release Keystore
Generate a dedicated release key using `keytool`:

```bash
keytool -genkeypair -v -keystore expensemate-release.keystore \
  -alias expensemate-release-key \
  -keyalg RSA \
  -keysize 2048 \
  -validity 10000 \
  -storetype JKS
```

> ⚠️ **CRITICAL SECURITY RULE:** Never commit `expensemate-release.keystore` or passwords into GitHub or public source control. Keep offline backups in an encrypted password manager or secure vault.

### Step 2: Configure Environment Variables or `gradle.properties`

To avoid hardcoding passwords, set environment variables on your build machine:

```powershell
# Windows PowerShell
$env:KEYSTORE_FILE="C:\Users\DELL\secure-keys\expensemate-release.keystore"
$env:KEYSTORE_PASSWORD="YourStrongKeystorePassword"
$env:KEY_ALIAS="expensemate-release-key"
$env:KEY_PASSWORD="YourStrongKeyPassword"
```

In `app/build.gradle.kts`:
```kotlin
android {
    signingConfigs {
        create("release") {
            storeFile = file(System.getenv("KEYSTORE_FILE") ?: "expensemate-release.keystore")
            storePassword = System.getenv("KEYSTORE_PASSWORD")
            keyAlias = System.getenv("KEY_ALIAS")
            keyPassword = System.getenv("KEY_PASSWORD")
        }
    }
    buildTypes {
        release {
            signingConfig = signingConfigs.getByName("release")
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
}
```

---

## 🛠 Generating the Production Android App Bundle (.aab)

Run the Gradle release bundle task:

```powershell
.\gradlew.bat bundleRelease
```

The resulting signed bundle will be generated at:
```
app/build/outputs/bundle/release/app-release.aab
```

Verify the bundle with Google's `bundletool` if desired:
```bash
java -jar bundletool.jar build-apks --bundle=app/build/outputs/bundle/release/app-release.aab --output=expensemate.apks
```

---

## 🚀 Release Track Strategy on Google Play Console

### Track 1: Internal Testing (1–2 days)
1. In Play Console, navigate to **Testing > Internal testing**.
2. Create a new release and upload `app-release.aab`.
3. Add your personal email to the internal tester list.
4. Download the app directly from the generated Google Play link to test real-world Play Store delivery, app update flow, and notification permissions.

### Track 2: Closed Testing (Required for personal accounts)
- Google requires new personal developer accounts to run a closed test with at least **20 testers opted-in for 14 continuous days**.
- Invite colleagues or beta testers.
- Collect feedback on layout scaling, dark mode contrast, and budget alerts.

### Track 3: Production Rollout
1. Navigate to **Release > Production**.
2. Create a release from your validated Closed Testing build.
3. Write localized release notes:
   ```
   Welcome to ExpenseMate 1.0.0!
   - Comprehensive income and expense tracking
   - Custom category and monthly budgets with alerts
   - Visual financial charts and trend analysis
   - PDF & CSV report export
   - 100% offline and privacy-focused
   ```
4. Start with a **Staged Rollout (20%)** to monitor crash-free sessions via Android Vitals.
5. If crash-free sessions exceed 99.8% over 48 hours, advance rollout to 50% and then 100%.
