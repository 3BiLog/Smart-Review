# CLEAN ARCHITECTURE MIGRATION PLAN

## CURRENT STATUS

Current architecture:
Partial MVVM

Missing:

* domain layer
* repository abstraction
* dependency injection
* use cases

---

# IMPORTANT RULE

Migration must happen gradually.

Never perform full architecture migration at once.

---

# TARGET ARCHITECTURE

UI
↓
ViewModel
↓
UseCase
↓
Repository
↓
Remote / Local Data Source

---

# TARGET STRUCTURE

app/
├── core/
├── data/
├── domain/
├── presentation/
└── di/

---

# PHASE 1 — UI STABILIZATION

Current phase.

Goals:

* stabilize Compose
* improve maintainability
* split large composables
* improve package structure

Do NOT:

* introduce Hilt
* introduce Room
* introduce Retrofit
* rewrite architecture

---

# PHASE 2 — REPOSITORY LAYER

Goals:

* create repository interfaces
* move mock access away from ViewModels

Example:

ViewModel
↓
CourseRepository
↓
MockCourseData

---

# PHASE 3 — REMOTE API

Goals:

* Retrofit
* DTOs
* mappers
* API layer

---

# PHASE 4 — LOCAL DATABASE

Goals:

* Room
* caching
* offline support

---

# PHASE 5 — DEPENDENCY INJECTION

Goals:

* Hilt
* module separation
* scalable ViewModel injection

---

# MIGRATION RULES

* Keep app working after every step
* Avoid massive refactors
* Migrate feature-by-feature
* Verify compile stability continuously

---

# PRIORITY ORDER

1. Stability
2. Maintainability
3. Scalability
4. Architecture cleanliness

---

# FINAL RULE

Safe gradual improvement is better than aggressive refactoring.
