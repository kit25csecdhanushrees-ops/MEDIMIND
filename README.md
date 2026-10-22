# MediMind – Medicine Reminder & Drug Interaction Checker

MediMind is an intelligent medication adherence and clinical safety application built for Android with Kotlin and Jetpack Compose, featuring direct integration with openFDA and Google Gemini AI.

---

## 🚀 How to Post to GitHub & Deploy

The entire workspace is a self-contained, production-ready Android repository with pre-configured Gradle Wrapper and GitHub Actions CI.

### Option 1: Direct 1-Click Push from Google AI Studio (Easiest)
1. In the Google AI Studio builder interface, look at the **top right menu** (or Project Settings).
2. Click **Export** ➔ Select **Push to GitHub** or **Download ZIP**.
3. If connecting to GitHub, authorize your account and select/create your repository name (`medimind`).
4. AI Studio will automatically push all project files to your new GitHub repository!

### Option 2: Post via Git Command Line
Run the following standard commands in your local directory:

```bash
# 1. Initialize git
git init

# 2. Add all files
git add .

# 3. Commit
git commit -m "Initial commit: MediMind Medicine Reminder & Drug Interaction Checker"

# 4. Set main branch and remote repository URL
git branch -M main
git remote add origin https://github.com/YOUR_GITHUB_USERNAME/medimind.git

# 5. Push to GitHub
git push -u origin main
```

### Option 3: Automated GitHub Actions CI / APK Deployment
A pre-configured GitHub Actions workflow (`.github/workflows/android.yml`) is included:
- Every time you push to `main` on GitHub, GitHub Actions will automatically compile the code and build `app-debug.apk`.
- You can download the installable APK directly from the **Actions** tab on your GitHub repository!

---

## 🌟 Key Features

### 1. Today's Real-Time Schedule & Alarms
- **Live Timeline**: Morning, afternoon, evening, and bedtime dose grouping with instant status updates (`DUE`, `TAKEN`, `SNOOZED`, `SKIPPED`, `MISSED`).
- **Urgent Alarm Alert Banner**: Audible chime (`ToneGenerator`) and gentle haptic vibration feedback with instant action buttons:
  - **Take**: Logs actual ingestion timestamp and automatically decrements pill inventory.
  - **Snooze**: 5, 10, or 15-minute countdowns.
  - **Skip**: Patient-directed skip with status recording.
- **Folder Attribution**: Each dose card displays its source folder/cabinet (e.g., `📁 Cardiovascular`, `📁 Diabetes & Metabolic`).
- **Low Stock Warnings**: Proactive warning chips when prescriptions drop to 5 or fewer doses remaining.

### 2. Medicine Folders & Cabinets System
- **Cabinet View**: Medications are grouped into expandable, color-accented folder cabinets (e.g., 📁 **Cardiovascular**, 📁 **Diabetes & Metabolic**, 📁 **Lipids & Heart**, 📁 **Daily Essentials**, 📁 **Vitamins & Supplements**).
- **Cabinet Header**: Displays total medications and remaining pill inventory per folder (e.g., *Cardiovascular • 2 medications • 42 pills total*) with individual low-stock alerts.
- **View Toggle**: Switch between **Group as Folders** (cabinet shelves) and a flat list with one tap.
- **Folder Shelf Filter Bar**: Filter chips (`📁 All`, `📁 Cardiovascular (2)`, `📁 Diabetes & Metabolic (1)`, etc.) for rapid filtering.
- **+ New Folder**: Create custom folders (e.g., *"Morning Pack"*, *"Post-Surgery Recovery"*, *"Travel Essentials"*).
- **Move to Folder**: Tap the folder move icon on any medicine card to reassign it to a different folder.

### 3. Clinical Drug Interaction Checker
- **Automated Interaction Analysis**: Automatically recalculates interaction risks whenever medications are added or updated.
- **Pre-Seeded Demo Data**: Includes **Warfarin (Coumadin) 5mg** + **Aspirin (Bayer) 81mg** to demonstrate severe interaction detection immediately.
- **Severity Levels**:
  - 🔴 **Severe**: Critical clinical risk (e.g., Warfarin + Aspirin life-threatening bleeding hazard, Sildenafil + Nitroglycerin fatal hypotension).
  - 🟠 **Moderate**: Clinically significant (e.g., Levothyroxine + Calcium carbonate chelation).
  - 🟡 **Minor**: Mild interaction.
  - 🟢 **Safe**: No major contraindication.
- **Allergy & Chronic Condition Checks**: Flags cross-reactivities (e.g., Penicillin hypersensitivity, Aspirin / NSAID peptic ulcer exacerbation).
- **Manual Checker Tool**: Compare any two medications on demand.
- **Gemini AI Explanations**: Translates dense pharmacology into compassionate, patient-friendly guidance with questions to ask your doctor.

### 4. Add Medicine with openFDA Search & Voice Input
- **Search-as-you-Type**: Connects to the openFDA Drug Label API with autocomplete suggestions.
- **Voice Dictation**: Android Speech Recognizer integration for hands-free entry.
- **Real-Time Interaction Preview**: Alerts the patient if a new drug conflicts with their current regimen before saving.

### 5. Adherence Dashboard & Doctor Export
- **7-Day Compliance Score**: Interactive progress meter and day-by-day status breakdown.
- **Continuous Streak Counter**: Daily motivation with best streak records.
- **Export Doctor Report**: Generates a shareable, clinical-grade summary of medications grouped by folder, adherence metrics, documented allergies, and flagged interactions for doctor visits.

### 6. Caregiver Remote Monitoring Mode
- **Invite Code Pairing**: Unique code (`MIND-9842-CARE`) to link caregivers.
- **Missed Dose Alerts**: Real-time escalation alerts when scheduled doses are missed or delayed.

### 7. Accessibility & Multi-Language
- **Multi-Language Support**: English, हिंदी (Hindi), தமிழ் (Tamil).
- **Senior / Large Text Mode**: Dynamic typography scaling for readability.
- **Dark Mode**: High-contrast dark theme for night usage.

---

## 🔒 Safety Disclaimer
> *This app provides information only and is not a substitute for professional medical advice. Always consult your doctor or pharmacist before changing any medication. Never stop or adjust prescription dosages without medical supervision.*

---

## ⚙️ Environment Configuration

Set your Gemini API key in the AI Studio **Secrets** panel or configure in `.env`:

```bash
GEMINI_API_KEY=YOUR_GEMINI_API_KEY
```

---

## 🛠️ Local Development & Building

```bash
# Build with Gradle Wrapper
./gradlew assembleDebug

# Run unit and clinical validation tests
./gradlew testDebugUnitTest
```
