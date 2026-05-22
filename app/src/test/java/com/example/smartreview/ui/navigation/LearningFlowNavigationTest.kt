package com.example.smartreview.ui.navigation

import com.example.smartreview.data.content.ContentIds
import com.example.smartreview.ui.screens.lesson.lessonContentRoute
import com.example.smartreview.ui.screens.lessonplayer.lessonPlayerRoute
import org.junit.Assert.assertEquals
import org.junit.Test

class LearningFlowNavigationTest {

    @Test
    fun lessonVideoRoute_alwaysUsesPlayer() {
        assertEquals(
            lessonPlayerRoute(ContentIds.Lesson.REACT_INTRO),
            LearningFlowNavigation.lessonVideoRoute(ContentIds.Lesson.REACT_INTRO),
        )
        assertEquals(
            lessonPlayerRoute(ContentIds.Lesson.ANDROID_COMPOSE_BASICS),
            LearningFlowNavigation.lessonVideoRoute(ContentIds.Lesson.ANDROID_COMPOSE_BASICS),
        )
    }

    @Test
    fun lessonEntryRoute_aliasesVideoRoute() {
        assertEquals(
            LearningFlowNavigation.lessonVideoRoute(ContentIds.Lesson.REACT_INTRO),
            LearningFlowNavigation.lessonEntryRoute(ContentIds.Lesson.REACT_INTRO),
        )
    }

    @Test
    fun lessonContentRoute_usesTheoryDestination() {
        assertEquals(
            lessonContentRoute(ContentIds.Lesson.REACT_INTRO),
            "lesson/${ContentIds.Lesson.REACT_INTRO}",
        )
    }
}
