package com.example.smartreview.data.content

/**
 * Stable local-first identifiers (Firestore/admin compatible).
 *
 * Pattern:
 * - course_{slug}
 * - module_{courseKey}_{index}
 * - lesson_{courseKey}_{slug}
 * - quiz_lesson_{courseKey}_{slug}  (1:1 with lesson when present)
 */
object ContentIds {

    object Course {
        const val REACT = "course_react"
        const val ANDROID = "course_android"
        const val PRODUCT = "course_product"
    }

    object Module {
        const val REACT_FOUNDATIONS = "module_react_foundations"
        const val REACT_STATE = "module_react_state"
        const val ANDROID_COMPOSE = "module_android_compose"
        const val ANDROID_ARCH = "module_android_arch"
        const val PRODUCT_CORE = "module_product_core"
    }

    object Lesson {
        const val REACT_INTRO = "lesson_react_intro"
        const val REACT_COMPONENTS = "lesson_react_components"
        const val REACT_ERRORS = "lesson_react_errors"
        const val REACT_CONTEXT = "lesson_react_context"
        const val ANDROID_COMPOSE_BASICS = "lesson_android_compose_basics"
        const val ANDROID_NAVIGATION = "lesson_android_navigation"
        const val ANDROID_VIEWMODEL = "lesson_android_viewmodel"
        const val PRODUCT_DISCOVERY = "lesson_product_discovery"
        const val PRODUCT_ROADMAP = "lesson_product_roadmap"
    }

    object Quiz {
        const val REACT_INTRO = "quiz_lesson_react_intro"
        const val REACT_COMPONENTS = "quiz_lesson_react_components"
    }
}
