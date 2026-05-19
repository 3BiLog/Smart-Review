# SMART REVIEW — AI CONSTITUTION

## PRIMARY GOAL

Protect:

* stability
* compile safety
* maintainability
* scalability
* readability

Never sacrifice app stability for architectural perfection.

---

# ABSOLUTE SAFETY RULES

## NEVER

* Never rewrite business logic automatically
* Never silently refactor APIs
* Never modify navigation flow automatically
* Never change startDestination automatically
* Never add dependencies automatically
* Never overwrite files without approval
* Never remove files automatically
* Never rewrite ViewModel logic automatically
* Never migrate architecture aggressively
* Never rename routes silently
* Never introduce placeholder implementations
* Never introduce unnecessary recompositions

---

# BEFORE MAKING CHANGES

You MUST:

1. Analyze affected files
2. Explain planned changes
3. Explain risk level
4. Ask before medium/high-risk modifications

---

# ALLOWED SAFE ACTIONS

You MAY:

* Auto-import missing imports
* Remove unused imports
* Split oversized composables
* Improve package organization
* Create reusable composables
* Improve readability
* Suggest architecture improvements
* Detect compile issues
* Detect MVVM violations

---

# CODE QUALITY RULES

* Prefer readability over clever code
* Keep composables focused
* Avoid duplicated UI
* Avoid duplicated business logic
* Keep files maintainable
* Prefer immutable state

---

# PERFORMANCE RULES

* Avoid unnecessary recompositions
* Prefer LazyColumn/LazyRow for large lists
* Avoid expensive calculations in composables
* Avoid deeply passing mutable state

---

# FINAL RULE

If anything is unclear:
ASK QUESTIONS FIRST.

---

## Language Rule

* Always communicate with the user in Vietnamese.
* Technical code elements remain in English.
* All summaries, explanations, architecture analysis, and migration reports must be written in Vietnamese.
* Only use English if the user explicitly requests it.
