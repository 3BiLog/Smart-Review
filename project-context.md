# SMART REVIEW — PROJECT CONTEXT

## PROJECT OVERVIEW

Project Name:
SmartReview

Type:
Online course review / learning platform

Current Stack:

* Kotlin
* Jetpack Compose
* Material 3
* Navigation Compose
* StateFlow
* ViewModel
* Coil

Architecture Status:

* Partial MVVM
* Compose-first structure
* Not fully Clean Architecture yet

Current Namespace:
com.example.smartreview

---

# CURRENT PACKAGE STRUCTURE

Current structure:

app/src/main/java/com/example/smartreview/
├── MainActivity.kt
├── data/
│   ├── model/
│   └── mock/
└── ui/
├── auth/
├── onboarding/
├── navigation/
├── theme/
├── components/
└── screens/
├── home/
├── courses/
├── search/
├── community/
└── payment/

---

# CURRENT ARCHITECTURE STATE

Implemented:

* Compose UI
* Navigation Compose
* StateFlow
* ViewModel
* UI State pattern
* Reusable composables

Missing:

* Repository layer
* Domain layer
* Dependency Injection
* Remote API layer
* Local database layer
* UseCases

---

# CURRENT KNOWN ISSUES

## Navigation

* Auth flow not fully connected
* Onboarding flow not fully connected
* Payment graph partially disconnected
* Some bottom nav routes still stubbed

## Compose

* Oversized composable files
* SearchScreen.kt very large
* Some reusable UI not extracted

## MVVM

* ViewModels still depend on mock data directly
* Repository abstraction missing

## Dependencies

* Compose BOM duplication risk
* No DI framework yet

---

# CURRENT SAFE PRIORITIES

Priority:

1. Compile stability
2. Navigation stability
3. UI maintainability
4. Composable decomposition
5. Package organization

NOT current priority:

* Full Clean Architecture migration
* Multi-module
* Hilt migration
* Room integration
* Retrofit integration

---

# DEVELOPMENT PHASE

Current Phase:
UI stabilization and structure improvement

Goal:
Prepare project for future scalable architecture safely.

---

# IMPORTANT RULES

* Preserve all existing behavior
* Avoid aggressive refactors
* Avoid architecture rewrites
* Focus on safe improvements only
* Ask before risky changes

---

# CURRENT TARGET STRUCTURE (FUTURE)

Preferred future structure:

app/
├── core/
├── data/
├── domain/
├── presentation/
├── di/
└── MainActivity.kt

Migration must happen gradually.
