# JETPACK COMPOSE RULES

## COMPOSABLE STRUCTURE

* Keep composables small and reusable
* Separate screens from reusable UI components
* Prefer feature-based organization
* Avoid oversized composable files

Files larger than 300 lines should be reviewed.

---

# REUSABLE COMPONENTS

Reusable UI belongs in:
presentation/components/

Examples:

* CourseCard
* SearchBar
* RatingBar
* CategoryChip

---

# SCREEN STRUCTURE

Screens belong in:
presentation/screens/

Examples:

* HomeScreen
* SearchScreen
* CourseDetailScreen

---

# STATE MANAGEMENT

Preferred:

* immutable UiState
* StateFlow
* collectAsStateWithLifecycle()

Avoid:

* mutable shared state
* business logic in composables

---

# UI EVENTS

Separate:

* UiState
* UiEvents
* UiActions

Prefer sealed classes for events.

---

# RECOMPOSITION RULES

* Avoid unnecessary remember
* Avoid creating objects repeatedly
* Avoid expensive work inside composables
* Preserve Compose performance

---

# PREVIEW RULES

* Keep previews isolated
* Avoid ViewModel usage in previews
* Avoid business logic in previews
* Use lightweight sample data only

---

# STYLING RULES

Preferred:

* Material 3
* consistent spacing
* reusable modifiers
* theme-based colors

Avoid:

* hardcoded dimensions everywhere
* duplicated styling

---

# NAMING RULES

Composable screens:

* HomeScreen
* SearchScreen

Reusable components:

* CourseCard
* SearchTopBar
* RatingBar

State:

* HomeUiState
* SearchUiState

ViewModels:

* HomeViewModel
* SearchViewModel
