package com.example.smartreview.data.mock

import com.example.smartreview.data.content.ContentIds
import com.example.smartreview.data.model.LessonBlock
import com.example.smartreview.data.model.LessonBlockType
import com.example.smartreview.data.model.LessonContent

/**
 * Canonical lesson bodies (local-first). Course shells live in [MockCourseData].
 */
object MockLessonData {

    private const val VIDEO_REACT_INTRO = "https://www.youtube.com/watch?v=LXb3EKWsInQ"
    private const val VIDEO_REACT_COMPONENTS = "https://www.youtube.com/watch?v=9xwazD5SyVg"
    private const val VIDEO_REACT_CONTEXT = "https://youtu.be/aqz-KE-bpKQ"
    private const val VIDEO_REACT_ERRORS = "https://www.youtube.com/watch?v=ScMzIvxBSi4"

    private val lessonsById = listOf(
        // ── React ───────────────────────────────────────────────────────────
        buildLesson(
            id = ContentIds.Lesson.REACT_INTRO,
            courseId = ContentIds.Course.REACT,
            moduleId = ContentIds.Module.REACT_FOUNDATIONS,
            orderInModule = 0,
            title = "Introduction to Modern React",
            subtitle = "Module 1 · Foundations",
            minutes = 12,
            videoUrl = VIDEO_REACT_INTRO,
            blocks = listOf(
                blockHeading("b1", "Giới thiệu bài học"),
                blockText(
                    "b2",
                    "React là thư viện JavaScript để xây dựng giao diện theo component. " +
                        "Bài học này giúp bạn nắm mô hình component-first.",
                ),
                blockImage("b3", "https://picsum.photos/seed/lesson_react_intro/600/320"),
                blockTip("b4", "Mẹo học", "Xem video trước, sau đó đọc từng block và làm quiz cuối bài."),
                blockQuizStub("b5", ContentIds.Quiz.REACT_INTRO, "Kiểm tra nhanh: React cơ bản"),
            ),
        ),
        buildLesson(
            id = ContentIds.Lesson.REACT_COMPONENTS,
            courseId = ContentIds.Course.REACT,
            moduleId = ContentIds.Module.REACT_FOUNDATIONS,
            orderInModule = 1,
            title = "Component Architecture",
            subtitle = "Module 1 · Structure",
            minutes = 18,
            videoUrl = VIDEO_REACT_COMPONENTS,
            blocks = listOf(
                blockHeading("b1", "Kiến trúc component"),
                blockText(
                    "b2",
                    "Tách UI thành component nhỏ, có trách nhiệm rõ ràng. Tránh component \"God\".",
                ),
                blockText("b3", "Props xuống, events lên — giữ luồng dữ liệu dễ đoán."),
                blockQuizStub("b4", ContentIds.Quiz.REACT_COMPONENTS, "Quiz: Component boundaries"),
            ),
        ),
        buildLesson(
            id = ContentIds.Lesson.REACT_ERRORS,
            courseId = ContentIds.Course.REACT,
            moduleId = ContentIds.Module.REACT_FOUNDATIONS,
            orderInModule = 2,
            title = "Error Handling Strategies",
            subtitle = "Module 1 · Reliability",
            minutes = 10,
            videoUrl = VIDEO_REACT_ERRORS,
            blocks = listOf(
                blockHeading("b1", "Xử lý lỗi"),
                blockText("b2", "Error boundary, fallback UI và logging giúp UX ổn định."),
            ),
        ),
        buildLesson(
            id = ContentIds.Lesson.REACT_CONTEXT,
            courseId = ContentIds.Course.REACT,
            moduleId = ContentIds.Module.REACT_STATE,
            orderInModule = 0,
            title = "Context API Deep Dive",
            subtitle = "Module 2 · State",
            minutes = 15,
            videoUrl = VIDEO_REACT_CONTEXT,
            blocks = listOf(
                blockHeading("b1", "Context API"),
                blockText("b2", "Context truyền dữ liệu qua cây component mà không prop drilling."),
                blockTip("b3", "Lưu ý", "Không lạm dụng Context — cân nhắc state holder/ViewModel."),
            ),
        ),
        // ── Android Compose ─────────────────────────────────────────────────
        buildLesson(
            id = ContentIds.Lesson.ANDROID_COMPOSE_BASICS,
            courseId = ContentIds.Course.ANDROID,
            moduleId = ContentIds.Module.ANDROID_COMPOSE,
            orderInModule = 0,
            title = "Jetpack Compose cơ bản",
            subtitle = "Module 1 · UI",
            minutes = 14,
            blocks = listOf(
                blockHeading("b1", "Composable functions"),
                blockText("b2", "Compose mô tả UI bằng hàm @Composable, recompose khi state đổi."),
                blockTip("b3", "Thực hành", "Mở Android Studio và tạo một Text + Button đơn giản."),
            ),
        ),
        buildLesson(
            id = ContentIds.Lesson.ANDROID_NAVIGATION,
            courseId = ContentIds.Course.ANDROID,
            moduleId = ContentIds.Module.ANDROID_COMPOSE,
            orderInModule = 1,
            title = "Navigation Compose",
            subtitle = "Module 1 · Điều hướng",
            minutes = 12,
            blocks = listOf(
                blockHeading("b1", "NavHost"),
                blockText("b2", "Navigation Compose giữ back stack và route string ổn định."),
            ),
        ),
        buildLesson(
            id = ContentIds.Lesson.ANDROID_VIEWMODEL,
            courseId = ContentIds.Course.ANDROID,
            moduleId = ContentIds.Module.ANDROID_ARCH,
            orderInModule = 0,
            title = "ViewModel & StateFlow",
            subtitle = "Module 2 · Kiến trúc",
            minutes = 16,
            blocks = listOf(
                blockHeading("b1", "MVVM trên Android"),
                blockText("b2", "ViewModel sống sót rotation; StateFlow cung cấp UI state cho Compose."),
            ),
        ),
        // ── Product thinking ──────────────────────────────────────────────────
        buildLesson(
            id = ContentIds.Lesson.PRODUCT_DISCOVERY,
            courseId = ContentIds.Course.PRODUCT,
            moduleId = ContentIds.Module.PRODUCT_CORE,
            orderInModule = 0,
            title = "Problem Discovery",
            subtitle = "Module 1 · Discovery",
            minutes = 11,
            blocks = listOf(
                blockHeading("b1", "Vấn đề đúng"),
                blockText("b2", "Xác định pain point người dùng trước khi thiết kế giải pháp."),
            ),
        ),
        buildLesson(
            id = ContentIds.Lesson.PRODUCT_ROADMAP,
            courseId = ContentIds.Course.PRODUCT,
            moduleId = ContentIds.Module.PRODUCT_CORE,
            orderInModule = 1,
            title = "Roadmap ưu tiên",
            subtitle = "Module 1 · Delivery",
            minutes = 9,
            blocks = listOf(
                blockHeading("b1", "Impact vs effort"),
                blockText("b2", "Sắp xếp backlog theo giá trị người dùng và chi phí kỹ thuật."),
            ),
        ),
    ).associateBy { it.id }

    fun getLesson(lessonId: String): LessonContent? = lessonsById[lessonId]

    fun getLessonsForCourse(courseId: String): List<LessonContent> =
        lessonsById.values
            .filter { it.courseId == courseId }
            .sortedWith(compareBy({ it.moduleId }, { it.orderInModule }))

    private fun buildLesson(
        id: String,
        courseId: String,
        moduleId: String,
        orderInModule: Int,
        title: String,
        subtitle: String,
        minutes: Int,
        blocks: List<LessonBlock>,
        videoUrl: String = "",
    ) = LessonContent(
        id = id,
        courseId = courseId,
        moduleId = moduleId,
        orderInModule = orderInModule,
        title = title,
        subtitle = subtitle,
        estimatedMinutes = minutes,
        blocks = blocks,
        videoUrl = videoUrl,
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
