# NAVIGATION RULES

## PRIMARY RULE

Navigation stability is critical.

Do NOT modify navigation behavior without approval.

---

# CURRENT FLOW

Current partial flow:

* Auth
* Onboarding
* Home
* Payment

Some graphs are still disconnected.

---

# PREFERRED FUTURE FLOW

Auth
↓
Onboarding
↓
Home

But DO NOT implement automatically.

---

# SAFETY RULES

* Never rename routes silently
* Never change startDestination automatically
* Never remove destinations automatically
* Never duplicate destinations
* Preserve back stack behavior

---

# ORGANIZATION RULES

Preferred structure:

presentation/navigation/
├── NavGraph.kt
├── AuthNavGraph.kt
├── OnboardingNavGraph.kt
├── PaymentNavGraph.kt

---

# ROUTE RULES

Routes should:

* be centralized
* use constants
* avoid hardcoded strings

Example:
const val HOME_ROUTE = "home"

---

# NAVIGATION BEST PRACTICES

* Keep navigation logic outside composables when possible
* Use nested graphs carefully
* Avoid navigation side effects during recomposition
* Avoid duplicate navigation calls

---

# BEFORE NAVIGATION REFACTOR

You MUST:

1. Explain current flow
2. Explain proposed flow
3. Identify risks
4. Ask for approval
