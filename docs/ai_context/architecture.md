# Project Architecture & Context

## Overview
**Project Name:** Combo Generator (Breaking Companion App)
**Goal:** A companion app for Breakdancers (B-Boys/B-Girls) to manage moves, generate combos, and track battle readiness.

## Architectural Pattern
- **MVVM (Model-View-ViewModel):**
  - **Model:** Room Database Entities & DAOs (`com.princelumpy.breakvault.data`).
  - **ViewModel:** `AndroidViewModel` subclasses holding LiveData/State (`com.princelumpy.breakvault.viewmodel`).
  - **View:** Jetpack Compose Screens (`com.princelumpy.breakvault.ui.screens`).

## Key Technologies
- **Language:** Kotlin.
- **UI:** Jetpack Compose (Material3).
- **Database:** Room Database (SQLite wrapper).
- **Concurrency:** Kotlin Coroutines & Flow/LiveData.
- **Serialization:** `kotlinx.serialization` (JSON Import/Export).
- **Navigation:** Jetpack Compose Navigation.

## Core Features
1.  **Practice Mode:**
    - Manage Moves and Tags (Many-to-Many).
    - Generate random or structured combos based on moveListTags.
    - "Flashcard" mode.
2.  **Battle Mode:**
    - Manage Battle Combos with properties: Description, Energy Level, Readiness Status.
    - Tags for Battle Combos (Many-to-Many).
3.  **Data Management:**
    - JSON Export/Import of all app data (Moves, Tags, History, Battle data).
    - Database Reset.

## File Structure
- `data/`: Entities, DAOs, AppDB, TypeConverters, Transfer Objects (`AppDataExport`).
- `viewmodel/`: Business logic (`MoveViewModel`, `BattleViewModel`).
- `ui/screens/`: Composable screens.
- `ui/theme/`: Theme definitions.
