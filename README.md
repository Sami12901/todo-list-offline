# TODO LIST - 100% Offline Android Application

<p align="center">
  <img src="logo.svg" width="112" height="112" alt="TODO LIST Logo" style="border-radius: 24px;" />
</p>

<p align="center">
  <b>Modern • 100% Offline • Zero Telemetry • Clean Room Architecture</b>
</p>

<p align="center">
  <img src="https://img.shields.io/badge/Platform-Android-3DDC84.svg?logo=android" alt="Platform Android" />
  <img src="https://img.shields.io/badge/Language-Kotlin%201.9-7F52FF.svg?logo=kotlin" alt="Kotlin" />
  <img src="https://img.shields.io/badge/UI-Jetpack%20Compose-4285F4.svg?logo=jetpackcompose" alt="Jetpack Compose" />
  <img src="https://img.shields.io/badge/Design-Material%203-6750A4.svg" alt="Material Design 3" />
  <img src="https://img.shields.io/badge/Database-Room%20(SQLite)-00599C.svg?logo=sqlite" alt="Room SQLite" />
  <img src="https://img.shields.io/badge/APK%20Size-1.17%20MB-success.svg" alt="APK Size" />
  <img src="https://img.shields.io/badge/License-MIT-green.svg" alt="License: MIT" />
</p>

---

## Overview

**TODO LIST** is a clean-room, open-source Android To-Do and Schedule Planning application developed by **Sami**. It provides a complete, modern alternative to cloud-dependent task apps by operating **100% offline**:
- **Zero Internet Permissions**: The application intentionally does not declare `android.permission.INTERNET`, providing guaranteed hardware-level privacy.
- **No Cloud Dependencies**: Replaces Google Drive sync with local JSON backup and restore via Android's Storage Access Framework (SAF).
- **No Paywalls or In-App Purchases**: All features (unlimited tasks, subtasks, checklists, reminders, and multi-themes) are permanently free and unlocked for everyone.
- **Ultra Lightweight**: Highly optimized release build at only **1.17 MB**.

---

## Key Features

1. **Task Management & Checklists**
   - Unlimited tasks with rich notes, priority tiers (High, Medium, Low, None), and categories.
   - Interactive subtasks and checklists with dynamic completion counters (`2/3 done`).
   - Filter by All, Today, Upcoming, and Completed tasks.

2. **Color-Coded Categories**
   - Pre-populated categories: Work, Personal, Study, Shopping, Health.
   - Add, edit, or delete custom categories with customizable color badges.

3. **Calendar View**
   - Monthly calendar grid with visual indicators for scheduled deadlines.
   - Tap any day to inspect and manage its tasks.

4. **100% Offline Alarm Reminders**
   - Hardware-level `AlarmManager` reminders with custom snooze (10 minutes) and quick "Mark as Done" actions directly from notifications.
   - Boot receiver automatically restores pending alarms when the device restarts.

5. **Multi-Theme Engine**
   - **Classic Blue**: Clean original aesthetic.
   - **Dark Obsidian**: Modern dark mode.
   - **AMOLED Pure Black**: Battery-optimized pure black for OLED displays.
   - **Forest Emerald**, **Sunset Coral**, and **Royal Purple** accent themes.

6. **Local Backup & Restore (Storage Access Framework)**
   - Export all categories, tasks, and checklists into a human-readable JSON backup file (`todo_backup_YYYYMMDD_HHmmss.json`).
   - Restore or migrate your data to any device completely offline.

---

## Architecture & Tech Stack

```
com.sami.todolist
├── data/
│   ├── dao/                 # Room DAO with reactive Kotlin Flows
│   ├── database/            # Room SQLite database with initial seeding
│   ├── entity/              # TaskEntity, SubTaskEntity, TaskCategoryEntity
│   ├── model/               # Priority and RepeatType enums
│   └── repository/          # Clean repository pattern
├── backup/                  # Local JSON backup & restore engine (SAF)
├── reminder/                # AlarmManager scheduler & Boot receiver
├── ui/
│   ├── screens/             # MainTaskList, TaskEditor, Calendar, Categories, Settings, About
│   ├── theme/               # Material 3 colors, typography, dynamic themes
│   ├── viewmodel/           # TodoViewModel & TodoUiState
│   └── MainActivity.kt      # Main entry point with bottom navigation
└── TodoApplication.kt       # Application class & notification channels
```

---

## Downloads

The pre-built release APK is available directly in the repository:
- **Direct Download**: [`TODO_LIST_v1.0.0_Offline.apk`](TODO_LIST_v1.0.0_Offline.apk) (1.17 MB)
- **Official Release**: [GitHub Releases v1.0.0](https://github.com/Sami12901/todo-list-offline/releases/tag/v1.0.0)

---

## Building from Source

```bash
# Debug build
./gradlew assembleDebug

# Release build
./gradlew assembleRelease
```
Output APK: `app/build/outputs/apk/release/app-release.apk`

---

## Author & Maintainer

- **Developer**: **MD. SAMI ISLAM** ([@Sami12901](https://github.com/Sami12901))
- **Email**: `mdsamiislam2006@gmail.com`
- **License**: [MIT License](LICENSE)
