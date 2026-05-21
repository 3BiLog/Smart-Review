package com.example.smartreview.data.mock

import com.example.smartreview.data.model.LessonBlock
import com.example.smartreview.data.model.LessonBlockType
import com.example.smartreview.data.model.LessonContent

object MockLessonData {

    private val lessonsById = listOf(
        buildLesson(
            id = "l1",
            courseId = "c5",
            title = "Introduction to Modern React",
            subtitle = "Module 1 · Foundations",
            minutes = 12,
            blocks = listOf(
                blockHeading("b1", "Giới thiệu bài học"),
                blockText(
                    "b2",
                    "React là thư viện JavaScript để xây dựng giao diện người dùng theo component. " +
                        "Bài học này giúp bạn nắm mô hình tư duy component-first.",
                ),
                blockImage("b3", "https://picsum.photos/seed/lesson_l1/600/320"),
                blockTip("b4", "Mẹo học", "Đọc từng block và ghi chú ý chính trước khi chuyển block tiếp theo."),
                blockQuizStub("b5", "q_l1_check", "Kiểm tra nhanh (sắp ra mắt)"),
            ),
        ),
        buildLesson(
            id = "l2",
            courseId = "c5",
            title = "Component Architecture",
            subtitle = "Module 1 · Structure",
            minutes = 18,
            blocks = listOf(
                blockHeading("b1", "Kiến trúc component"),
                blockText(
                    "b2",
                    "Tách UI thành component nhỏ, có trách nhiệm rõ ràng. Tránh component \"God\" chứa quá nhiều logic.",
                ),
                blockText(
                    "b3",
                    "Props xuống, events lên — giữ luồng dữ liệu dễ đoán và dễ test.",
                ),
                blockQuizStub("b4", "q_l2_check", "Quiz: Component boundaries"),
            ),
        ),
        buildLesson(
            id = "l3",
            courseId = "c5",
            title = "Context API Deep Dive",
            subtitle = "Module 2 · State",
            minutes = 15,
            blocks = listOf(
                blockHeading("b1", "Context API"),
                blockText("b2", "Context truyền dữ liệu qua cây component mà không cần prop drilling từng cấp."),
                blockTip("b3", "Lưu ý", "Không lạm dụng Context cho mọi state — cân nhắc ViewModel hoặc state holder."),
            ),
        ),
        buildLesson(
            id = "l5",
            courseId = "c5",
            title = "Error Handling Strategies",
            subtitle = "Module 2 · Reliability",
            minutes = 10,
            blocks = listOf(
                blockHeading("b1", "Xử lý lỗi"),
                blockText("b2", "Boundary errors, fallback UI và logging giúp trải nghiệm ổn định khi có sự cố."),
            ),
        ),
    ).associateBy { it.id }

    fun getLesson(lessonId: String): LessonContent? = lessonsById[lessonId]

    fun getLessonsForCourse(courseId: String): List<LessonContent> =
        lessonsById.values.filter { it.courseId == courseId }

    private fun buildLesson(
        id: String,
        courseId: String,
        title: String,
        subtitle: String,
        minutes: Int,
        blocks: List<LessonBlock>,
    ) = LessonContent(
        id = id,
        courseId = courseId,
        title = title,
        subtitle = subtitle,
        estimatedMinutes = minutes,
        blocks = blocks,
    )

    private fun blockHeading(id: String, title: String) =
        LessonBlock(id = id, type = LessonBlockType.HEADING, title = title)

    private fun blockText(id: String, body: String) =
        LessonBlock(id = id, type = LessonBlockType.TEXT, body = body)

    private fun blockImage(id: String, url: String) =
        LessonBlock(id = id, type = LessonBlockType.IMAGE, imageUrl = url)

    private fun blockTip(id: String, title: String, body: String) =
        LessonBlock(id = id, type = LessonBlockType.TIP, title = title, body = body)

    private fun blockQuizStub(id: String, quizId: String, label: String) =
        LessonBlock(
            id = id,
            type = LessonBlockType.QUIZ_STUB,
            body = label,
            quizStubId = quizId,
        )
}
