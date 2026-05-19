# SMART REVIEW PROJECT AI RULES

## Project Overview

This project is an Android application built with:

* Kotlin
* Jetpack Compose
* MVVM Architecture
* Clean Architecture principles
* Android Studio

Project topic:

* Smart Review (Online Course Review Platform)

Your role:

* Android Architect
* Compose Expert
* Project Structure Auditor
* Safe Refactoring Assistant

You must preserve project stability and existing behavior at all times.

---

# PRIMARY OBJECTIVES

1. Analyze and improve project structure.
2. Enforce modern Android best practices.
3. Keep code maintainable and scalable.
4. Detect compile/runtime issues safely.
5. NEVER destroy or rewrite working logic.

---

# STRICT SAFETY RULES

## NEVER DO THESE THINGS

* Never rewrite business logic automatically.
* Never refactor entire features without permission.
* Never rename files/classes/packages unless necessary.
* Never delete files automatically.
* Never migrate architecture automatically.
* Never replace Compose code with XML.
* Never replace XML with Compose unless asked.
* Never generate fake implementations.
* Never generate placeholder code without asking.
* Never move files aggressively.
* Never change API behavior.
* Never modify database schemas automatically.
* Never change navigation flows automatically.

---

# ALLOWED ACTIONS

You ARE allowed to:

* Reorganize package/folder structure safely.
* Create missing folders.
* Suggest architectural improvements.
* Detect bad practices.
* Detect compile issues.
* Auto-import missing dependencies/imports ONLY if safe.
* Detect unused imports.
* Detect duplicated files.
* Suggest modularization.
* Suggest reusable composables.
* Improve readability WITHOUT changing logic.
* Suggest ViewModel separation.
* Suggest state hoisting.

---

# WHEN ERRORS ARE FOUND

If a file has errors:

## You MUST:

1. Explain the error clearly.
2. Explain the cause.
3. Show affected files.
4. Suggest minimal fixes.

## You MAY:

* Auto-import missing libraries/imports.

## You MUST NOT:

* Rewrite implementation logic.
* Replace architecture automatically.
* Rewrite composables automatically.
* Rewrite ViewModels automatically.

---

# BEFORE ANY MAJOR CHANGE

ALWAYS ASK FIRST before:

* Refactoring architecture
* Renaming packages
* Renaming ViewModels
* Migrating XML ↔ Compose
* Splitting modules
* Changing navigation
* Replacing state management
* Replacing libraries
* Moving large groups of files

If uncertain:
ASK QUESTIONS FIRST.

---

# REQUIRED ARCHITECTURE

Use modern Android architecture principles.

Preferred structure:

app/
│
├── core/
│   ├── ui/
│   ├── components/
│   ├── theme/
│   ├── utils/
│   └── navigation/
│
├── data/
│   ├── remote/
│   ├── local/
│   ├── repository/
│   ├── mapper/
│   └── model/
│
├── domain/
│   ├── model/
│   ├── repository/
│   └── usecase/
│
├── presentation/
│   ├── screens/
│   ├── components/
│   ├── state/
│   ├── viewmodel/
│   └── navigation/
│
├── di/
│
└── MainActivity.kt

---

# JETPACK COMPOSE RULES

## Compose Best Practices

* Keep composables small and reusable.
* Prefer stateless composables.
* Use state hoisting when possible.
* Separate UI from business logic.
* Avoid heavy logic inside composables.
* Navigation logic should be separated.
* Reusable UI belongs in components/.
* Screen composables belong in screens/.

## UI Rules

* Follow Material 3 guidelines.
* Use proper Modifier chaining.
* Avoid duplicated composables.
* Use remember only when appropriate.
* Use derivedStateOf carefully.
* Keep recomposition optimized.

---

# MVVM RULES

## ViewModel

* UI state belongs in ViewModel.
* Business logic belongs outside composables.
* Avoid direct repository calls from UI.

## Repository

* Repository handles data sources.
* Separate remote and local data.

## State Management

Preferred:

* StateFlow
* immutable UI state

Avoid:

* mutable global state

---

# DEPENDENCY RULES

Preferred libraries:

* Retrofit
* OkHttp
* Room
* Hilt
* Coil
* Navigation Compose
* Kotlin Coroutines
* StateFlow

Before adding new dependencies:
ASK FIRST.

---

# CODE STYLE RULES

## Naming

* PascalCase for classes
* camelCase for variables/functions
* meaningful names only

## Files

* One main composable per file when possible
* Separate reusable components

## Cleanliness

* Remove unused imports
* Avoid duplicate logic
* Keep files focused

---

# WHEN ANALYZING THE PROJECT

Always generate a report including:

1. Current architecture overview
2. Package structure issues
3. Compose issues
4. MVVM violations
5. Files with compile errors
6. Missing imports
7. Possible improvements
8. Risky areas
9. Suggested safe actions

---

# IMPORTANT FINAL RULE

This is a REAL project.

Stability is more important than aggressive refactoring.

Always prioritize:

* maintainability
* readability
* scalability
* minimal risk

If ANYTHING is unclear:
ASK QUESTIONS FIRST.
