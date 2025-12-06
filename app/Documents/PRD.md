# BreakPractice - Product Requirements Document

**Platform:** Android  
**Language:** Kotlin

---

## 1. Goal

Help dancers store, organize, and practice their moves by:

- Logging moves with custom moveListTags
- Randomly or structurally generating one combo at a time for practice
- Managing a personal moveListTag library

---

## 2. Core Features

### 2.1 Move Management

- Add a new move (fields: **name**, **moveListTags**)
- Assign one or more moveListTags to each move
- Edit or delete existing moves
- View all moves in a **scrollable list of cards**
    - Card shows name and moveListTags
    - Edit button on each card

### 2.2 Tag Management

- Default moveListTags on first launch: `"Toprock"`, `"Footwork"`, `"Freeze"`, `"Power"`
- Users can **add, edit, or delete moveListTags**
- Tags persist across sessions
- Separate **Tag List tab** displays all moveListTags
- Clicking a moveListTag shows all moves with that moveListTag
- Tag editing options:
    - Rename moveListTag
    - Delete moveListTag (removes it from all moves)
- Tags are selectable when adding or editing moves

### 2.3 Combo Generator

- Generates **one combo at a time**
- Two generation modes:
    - **Random:** pick a random length (2–5) and random moves from selected moveListTags
    - **Structured:** user specifies a moveListTag sequence (2–5 moveListTags, e.g. {tag1} → {tag2} → {tag3})
        - Picks one move from each specified moveListTag
- Ask user: **How many moves long? (2–5)**
    - Default: random length between 2 and 5
- After generating a combo:
    - Display clearly
    - Offer **"Save this combo"** button
    - Saved combos appear in a separate tab ("Saved Combos")
- Encourage exporting combos to an SRS flashcard app or daily practice

### 2.4 Flashcard Mode

- For moves with excluded moveListTags (like “get downs”)
- Prompt-style question: “Can you do this [move] in three different ways in and out of it?”
- Randomly select from moves tagged as excluded from combos

---

## 3. UI/UX Requirements

- **Menu navigation** to switch between:
    - Move List
    - Combo History
    - Saved Combos
    - Tag List

- **Landing screen (Move List):**
    - List of moves as cards
    - Button: “Add Move”
    - Button: “Generate Combo”

- **Add/Edit Move screen:**
    - Input: Move name
    - Multi-select dropdown of existing moveListTags
    - Field to create new moveListTag

- **Tag List screen:**
    - List of all user moveListTags
    - Click moveListTag to view associated moves
    - Buttons: Edit moveListTag name, Delete moveListTag

- **Combo Generator screen:**
    - Input: Combo length (2–5)
    - Mode toggle: Random or Structured
    - If Structured: fields to choose 2–5 moveListTags in order
    - Generate button
    - After generation: "Save this combo" button

- **Saved Combos screen:**
    - List of saved combos
    - Delete option on each combo

- **Flashcard screen:**
    - Shows random move with excluded moveListTag
    - “Next” button

**Design Goals:**

- Clean, minimal, modern
- Easy and fast data entry
- Visually satisfying cards (rounded corners, subtle shadows)

---

## 4. Data Model

**Move Object**

```kotlin
data class Move(
    val id: String, // UUID
    val name: String,
    val moveListTags: List<String>
)

Todo

Please read through and do all of this, read all files necessary consume as much data as you need: