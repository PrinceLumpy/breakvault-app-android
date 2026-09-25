# BreakVault

BreakVault is an Android app for breakdancers to keep track of their moves, experiment with combo
ideas, and prepare combos for battles.

## Features

The app has three tabs:

* **Moves:** Your full list of breaking moves.
  * Organize moves with tags. Each tag can have a color, and a move card shows a strip with its
    tags' colors.
  * Filter the list by tag, and search by name from the top bar. The search only looks within the
    moves the tag filter leaves.
* **Lab:** A place to work out combo ideas. Build combos from your moves or add custom moves.
* **Battle:** Combos you're ready to take into a battle.
  * Tag and color-code combos, and filter them by tag.
  * Sort by newest, name, or color group.
  * Mark combos as used during a session, then reset them all with "Clean Slate".
  * Import a combo from the Lab.

Also available:

* **Combo Generator:** Generates a random combo from your moves. It has two modes:
  * **Random:** picks moves from the tags you select.
  * **Structured:** follows a sequence of tags you define.

  Saving a generated combo puts it in the Lab as "Generated Combo #1", "#2", and so on.
* **Backup:** Export all your data to a JSON file, import it again later, or wipe the database.
  All three are in Settings.

## Architecture

This app is built with a modern Android architecture, using:

* **Single-Activity Architecture:** A single `Activity` hosts all of the app's screens.
* **Jetpack Navigation:** For navigating between different screens.
* **MVVM (Model-View-ViewModel):** For separating the UI from the business logic.
* **Repository Pattern:** For abstracting the data sources.
* **Dependency Injection:** Using Hilt for managing dependencies.
* **Room:** For local data storage.

## Technologies Used

* **Kotlin:** The primary programming language.
* **Jetpack Compose:** For building the UI.
* **Hilt:** For dependency injection.
* **Room:** For local database storage.
* **DataStore:** For saved UI preferences, such as the battle list sort.
* **kotlinx.serialization:** For data export and import.
* **Jetpack Navigation:** For in-app navigation.
* **Coroutines and Flow:** For asynchronous programming.
