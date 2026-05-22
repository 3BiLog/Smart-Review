package com.example.smartreview.data.learning

import com.example.smartreview.data.content.ContentIds
import com.example.smartreview.data.mock.MockLessonData
import com.example.smartreview.data.model.LessonBlockType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class LessonContentBlocksTest {

    @Test
    fun contentBlocks_excludesQuizStub() {
        val lesson = MockLessonData.getLesson(ContentIds.Lesson.REACT_INTRO)!!
        val blocks = LessonContentBlocks.contentBlocks(lesson)
        assert(blocks.none { it.type == LessonBlockType.QUIZ_STUB })
        assertEquals(4, blocks.size)
    }

    @Test
    fun linkedQuizId_readsFromStub() {
        val lesson = MockLessonData.getLesson(ContentIds.Lesson.REACT_INTRO)!!
        assertEquals(ContentIds.Quiz.REACT_INTRO, LessonContentBlocks.linkedQuizId(lesson))
    }

    @Test
    fun linkedQuizId_nullWhenNoStub() {
        val lesson = MockLessonData.getLesson(ContentIds.Lesson.REACT_ERRORS)!!
        assertNull(LessonContentBlocks.linkedQuizId(lesson))
    }
}
