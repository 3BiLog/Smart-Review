package com.example.smartreview.data.repository

import android.content.Context
import com.example.smartreview.data.repository.prefs.SharedPreferencesLearningProgressRepository

object LearningProgressRepositoryProvider {

    private lateinit var appContext: Context

    fun init(context: Context) {
        appContext = context.applicationContext
    }

    val default: LearningProgressRepository by lazy {
        check(::appContext.isInitialized) {
            "LearningProgressRepositoryProvider.init(context) must be called from MainActivity."
        }
        SharedPreferencesLearningProgressRepository(appContext)
    }
}
