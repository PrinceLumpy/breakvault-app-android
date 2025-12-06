# Practice Mode Expansion: Design Document

This document outlines the architectural and UI changes required to implement the **Goals**, **Timer**, and **Foundational Drills** features.

---

## 1. Goals & Progress Tracking

### Concept
Users can create high-level goals (e.g., "Master Power Moves") and define specific "Stages" or drills required to achieve them. Unlike a sequential video game level, these stages are open tasks. The user tracks reps/sets for each stage to reach a target.

### Database Schema Changes

#### New Entity: `Goal`
Represents the parent container for a set of tracking stages.
```kotlin
@Entity(tableName = "goals")
data class Goal(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val title: String,
    val description: String,
    val createdAt: Long = System.currentTimeMillis(),
    val lastUpdated: Long = System.currentTimeMillis(), // Added for sorting/sync
    val isArchived: Boolean = false
)
```

#### New Entity: `GoalStage`
Represents a single trackable item within a goal (e.g., "200 Windmills").
```kotlin
@Entity(
    tableName = "goal_stages",
    foreignKeys = [
        ForeignKey(entity = Goal::class, parentColumns = ["id"], childColumns = ["goalId"], onDelete = CASCADE)
    ]
)
data class GoalStage(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val goalId: String, // Foreign Key
    val name: String,   // e.g., "Flare Drills"
    val currentCount: Int = 0,
    val targetCount: Int,
    val unit: String = "reps", // e.g., "reps", "minutes", "sets"
    val createdAt: Long = System.currentTimeMillis(),
    val lastUpdated: Long = System.currentTimeMillis()
)
```

### UI/UX Design

#### A. Goals Dashboard (New Tab)
*   **List View:** Displays active goals.
*   **Card Content:** Goal Title, and an overall completion percentage.
*   **FAB:** "Create New Goal".

#### B. Goal Details Screen (The "Tracker")
*   **Header:** Goal Title & Optional Description.
*   **Visuals:** A master progress bar for the entire goal.
*   **Stage List:** A list of `GoalStage` items.

**Stage Item Layout (Row):**
1.  **Top:** Stage Name (e.g., "Halos").
2.  **Bottom:**
    *   **Text:** `45 / 100`.
    *   **Progress Bar:** Linear progress indicator showing `current / target`.
3. Tap on stage to navigate to the ViewGoalStage Page 
4. Hold to open an edit popup that can edit the Name, Description, and Target

#### C. View GoalStage Screen
This is where the user spends most of their practice time.
I still haven't finished acceptance criteria for this.
**"Add Reps" Dialog:**
*   Triggered by clicking the stage or the specific "Add" button.
*   **Input Field:** Numeric input (e.g., user did a set of 10, types "10").
*   **Action:** Adds input to `currentCount`.
*   **Reasoning:** Doing 50 reps and tapping `+` 50 times is bad UX.
---

## 2. In-App Timer

### Concept
A utility to help with footwork drills, freezes, or HIIT sets without leaving the app.

### Database Changes
*   None required initially. (Future: Save session logs).

### UI/UX Design

#### Timer Screen (New Tab or Utility Button)
*   **Two Modes:**
    1.  **Stopwatch:** Simple Start/Stop/Reset. Used for "How long can I hold this freeze?".
    2.  **Countdown/Interval:**
        *   Input: Duration (mm:ss).
        *   Controls: Start, Pause, Reset.
        *   Visuals: Large text, circular progress indicator.
        *   Audio: Beep on completion (requires `MediaPlayer` or `ToneGenerator`).

---

## 5. Navigation & Architecture Updates

### Navigation Structure
The "Practice" section is becoming feature-rich. To avoid a clutter of 6+ bottom tabs, we should reorganize:

**Current Bottom Bar:**
1.  Moves (Library)
2.  Saved Combos
3.  Tags
4.  Battle
5.  Settings

**Proposed Bottom Bar:**
1.  **Library** (Moves & Tags & Drills) -> Tab Layout at top?
2.  **Lab** (Combo Gen & Saved Combos)
3.  **Tracker** (Goals & Timer) -> **NEW**
4.  **Battle**
5.  **Settings**

*Alternatively, strictly for "Practice Mode" request:*
Keep existing tabs but add a **"Goals"** tab and put the Timer/Drills inside it or as sub-features.

### Recommended Implementation Order
1.  **Database Layer:** Create Entities (`Goal`, `GoalStage`, `Drill`) and DAOs.
2.  **Goals UI:** Build the Goal List and Detail screens. and Archived Goals in the side bar
3.  **Timer UI:** Build the utility screen.
