package com.example.smartreview.data.learning

import com.example.smartreview.data.model.LessonBlock
import com.example.smartreview.data.model.LessonBlockType
import com.example.smartreview.data.model.LessonContent

object LessonContentBlocks {

    fun contentBlocks(lesson: LessonContent): List<LessonBlock> =
        lesson.blocks.filter { it.type != LessonBlockType.QUIZ_STUB }

    fun linkedQuizId(lesson: LessonContent): String? =
        lesson.blocks
            .firstOrNull { it.type == LessonBlockType.QUIZ_STUB }
            ?.quizStubId
            ?.takeIf { it.isNotBlank() }
}
