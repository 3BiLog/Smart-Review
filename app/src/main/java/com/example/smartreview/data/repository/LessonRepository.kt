package com.example.smartreview.data.repository

import com.example.smartreview.data.model.LessonContent

interface LessonRepository {

    suspend fun getLesson(lessonId: String): LessonContent?

    suspend fun getLessonsForCourse(courseId: String): List<LessonContent>
}