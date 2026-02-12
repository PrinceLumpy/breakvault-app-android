# BreakVault

BreakVault is an Android application designed for breakdancers to manage their moves, create combos,
and track their training sessions.

## Features

* **Move Management:** Keep a list of all your breaking moves.
* **Combo Creation:** Create and save your own breaking combos.
* **Combo Generator:** Generate new combos for inspiration.
* **Battle Combos:** Manage combos specifically for battles.
* **Goal Setting:** Set and track your breakdancing goals.
* **Training Timer:** Time your practice sessions.

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
* **Jetpack Navigation:** For in-app navigation.
* **Coroutines and Flow:** For asynchronous programming.
