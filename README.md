# PixieLib - Your Personal Smart Library

PixieLib is a feature-rich, native Android application designed for digital book cataloging, user reservations, and download logging. Featuring a modern emerald green interface, it supports both local SQLite storage and remote Firebase Firestore syncing, supporting dual reader and administrator roles.

---

## 📱 Key Features

### 1. Reader features
- **Custom Sign-Up & Sign-In**: Readers can register a private account and log in securely.
- **Dynamic Book Catalog**: Browse books with custom search and horizontal category scroll filters (*Development, AI & Networking, Software Architecture, Databases, Data Science, Security, Cloud, and Mobile*).
- **Reservation Workflow**: Reserve available books. View reservation status (*Pending* vs. *Confirmed*) along with the pickup date set by the administrator in your profile.
- **Online Reader**: Read downloads online directly within the app using the integrated Google Drive reader.
- **Native Download History**: A dedicated screen to view, search, and delete download logs, synced with Firebase Firestore in real-time.
- **Theme Support**: Seamless switching between premium Dark and Light mode.

### 2. Administrator Console
- **Predefined Credentials**: Secure access using preseeded administrator credentials (Username: `admin`, Password: `admin`).
- **Book Catalog CRUD**: Add, edit, or delete books with custom titles, authors, categories, covers, and PDF file attachments.
- **Availability Toggle**: Signal whether a book is available for reservation. When set to unavailable:
  - The book remains in the reader's catalog for visibility.
  - The card is dimmed, click actions are disabled, and the button displays a distinct **"Not Available"** badge.
- **Reservation Confirmations**: View all user reservations. Confirm a pending reservation by assigning a custom pickup date via a DatePickerDialog, or revoke/cancel existing reservations.

---

## 🛠️ Tech Stack

- **Platform**: Android (Native Java)
- **Local Storage**: SQLite (`SQLiteOpenHelper` schema migrations)
- **Cloud Sync**: Firebase Firestore & Firebase Authentication
- **Design System**: Material Design, Custom drawables, and brand green gradients
- **Build Tool**: Gradle (using Java 17 compiler)

---

## 🚀 Getting Started

### Prerequisites
1. **Android Studio** (Koala or newer recommended).
2. **Android SDK** configured for API Level 34/35.
3. **Java 17 (JDK 17)**: Ensure your environment uses Java 17. If you get `JAVA_HOME` errors, set your environment variable to point to Android Studio's bundled JDK:
   ```powershell
   # Windows (User Environment Variable)
   C:\Program Files\Android\Android Studio\jbr
   ```

### Setup Instructions
1. **Clone the Repository**:
   ```bash
   git clone https://github.com/yourusername/MyDashboard2.git
   cd MyDashboard2
   ```

2. **Add Firebase Config**:
   - Create a project on the [Firebase Console](https://console.firebase.google.com/).
   - Add an Android app with the package name `com.app.mydashboard`.
   - Download the `google-services.json` file and place it inside the `/app` directory of this project.
   - Enable **Cloud Firestore** and **Firebase Authentication** (Email/Password provider) in your Firebase Console.

3. **Open & Build**:
   - Open Android Studio and select **Open an Existing Project**.
   - Navigate to the cloned folder and let Gradle sync.
   - Click **Run** (`Shift + F10` / `Ctrl + R`) to launch the app on your emulator or physical device.

---

## 🔑 Test Credentials

| Role | Username | Password | Notes |
| :--- | :--- | :--- | :--- |
| **Administrator** | `admin` | `admin` | Pre-seeded in database. Cannot be registered via UI. |
| **Reader** | *Custom* | *Custom* | Create via the **Register** link on the login screen. |

---

## 📂 Project Structure

```
MyDashboard2/
├── app/
│   ├── src/main/
│   │   ├── java/com/app/mydashboard/
│   │   │   ├── AdminActivity.java            # Hub for admin CRUD & confirmations
│   │   │   ├── BookFormActivity.java         # Add/Edit book form
│   │   │   ├── HistoryActivity.java          # Native download history list
│   │   │   ├── LoginActivity.java            # Authentication and role routing
│   │   │   ├── MainActivity.java             # Main user catalog
│   │   │   ├── DatabaseHelper.java           # SQLite database schema operations
│   │   │   └── ...
│   │   └── res/
│   │       ├── layout/                       # UI layouts (XML)
│   │       ├── values/colors.xml             # Brand color system
│   │       └── ...
└── build.gradle.kts                          # App build configuration
```
