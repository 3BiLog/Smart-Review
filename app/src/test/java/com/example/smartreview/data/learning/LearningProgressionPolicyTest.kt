package com.example.smartreview.data.learning

import com.example.smartreview.data.content.ContentIds
import com.example.smartreview.data.content.CourseCatalogAssembly
import com.example.smartreview.data.mock.MockCourseData
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class LearningProgressionPolicyTest {

    private val policy = LearningProgressionPolicy()

    @Test
    fun firstLessonUnlocked_emptyProgress() {
        val course = MockCourseData.courses.first { it.id == ContentIds.Course.REACT }
        val result = policy.applyToCourse(course, LearningProgressionPolicy.ProgressSnapshot())
        val module1 = result.course.modules.first()
        assertFalse(module1.isLocked)
        assertFalse(module1.lessons[0].isLocked)
        assertTrue(module1.lessons[1].isLocked)
        assertTrue(result.course.modules[1].isLocked)
    }

    @Test
    fun secondLessonUnlocks_afterFirstLessonAndQuizComplete() {
        val course = MockCourseData.courses.first { it.id == ContentIds.Course.REACT }
        val snapshot = LearningProgressionPolicy.ProgressSnapshot(
            completedLessonIds = setOf(ContentIds.Lesson.REACT_INTRO),
            completedQuizIds = setOf(ContentIds.Quiz.REACT_INTRO),
        )
        val result = policy.applyToCourse(course, snapshot)
        val module1 = result.course.modules.first()
        assertFalse(module1.lessons[1].isLocked)
    }

    @Test
    fun moduleTwoUnlocks_whenModuleOneFullyComplete() {
        val course = MockCourseData.courses.first { it.id == ContentIds.Course.REACT }
        val snapshot = LearningProgressionPolicy.ProgressSnapshot(
            completedLessonIds = setOf(
                ContentIds.Lesson.REACT_INTRO,
                ContentIds.Lesson.REACT_COMPONENTS,
                ContentIds.Lesson.REACT_ERRORS,
            ),
            completedQuizIds = setOf(
                ContentIds.Quiz.REACT_INTRO,
                ContentIds.Quiz.REACT_COMPONENTS,
            ),
        )
        val result = policy.applyToCourse(course, snapshot)
        assertFalse(result.course.modules[1].isLocked)
    }
}
