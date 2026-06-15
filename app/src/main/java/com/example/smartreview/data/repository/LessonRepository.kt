package com.example.smartreview.data.repository

import com.example.smartreview.data.model.LessonContent

/**
 * Local lesson content access (mock now; Firestore/Room later).
 */
interface LessonRepository {

    suspend fun getLesson(lessonId: String): LessonContent?  // ✅ Thêm suspend

    suspend fun getLessonsForCourse(courseId: String): List<LessonContent>  // ✅ Thêm suspend
}