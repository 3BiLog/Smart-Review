//package com.example.smartreview.data.mock
//
//import com.example.smartreview.data.content.ContentIds
//import com.example.smartreview.data.content.CourseCatalogAssembly
//import com.example.smartreview.data.model.Course
//
///**
// * Three curated courses (local-first). Lesson bodies: [MockLessonData].
// */
//object MockCourseData {
//
//    private val reactModules = listOf(
//        CourseCatalogAssembly.module(
//            id = ContentIds.Module.REACT_FOUNDATIONS,
//            title = "Module 1: Foundations",
//            lessonIds = listOf(
//                ContentIds.Lesson.REACT_INTRO,
//                ContentIds.Lesson.REACT_COMPONENTS,
//                ContentIds.Lesson.REACT_ERRORS,
//            ),
//        ),
//        CourseCatalogAssembly.module(
//            id = ContentIds.Module.REACT_STATE,
//            title = "Module 2: State Management",
//            lessonIds = listOf(ContentIds.Lesson.REACT_CONTEXT),
//        ),
//    )
//
//    private val androidModules = listOf(
//        CourseCatalogAssembly.module(
//            id = ContentIds.Module.ANDROID_COMPOSE,
//            title = "Module 1: Compose UI",
//            lessonIds = listOf(
//                ContentIds.Lesson.ANDROID_COMPOSE_BASICS,
//                ContentIds.Lesson.ANDROID_NAVIGATION,
//            ),
//        ),
//        CourseCatalogAssembly.module(
//            id = ContentIds.Module.ANDROID_ARCH,
//            title = "Module 2: Architecture",
//            lessonIds = listOf(ContentIds.Lesson.ANDROID_VIEWMODEL),
//        ),
//    )
//
//    private val productModules = listOf(
//        CourseCatalogAssembly.module(
//            id = ContentIds.Module.PRODUCT_CORE,
//            title = "Module 1: Product Core",
//            lessonIds = listOf(
//                ContentIds.Lesson.PRODUCT_DISCOVERY,
//                ContentIds.Lesson.PRODUCT_ROADMAP,
//            ),
//        ),
//    )
//
//    val courses = listOf(
//        Course(
//            id = ContentIds.Course.REACT,
//            title = "Advanced React Patterns & Performance",
//            imageUrl = "https://picsum.photos/seed/course_react/400/225",
//            difficulty = "Advanced",
//            lessonCount = CourseCatalogAssembly.totalLessonCount(reactModules),
//            progress = 0f,
//            xpReward = 800,
//            price = 899_000L,
//            rating = 4.9f,
//            reviewCount = 1_245,
//            durationHours = 2.5f,
//            instructorName = "Sarah Drasner",
//            instructorTitle = "Senior Frontend Architect",
//            instructorAvatar = "https://picsum.photos/seed/inst_react/100/100",
//            description = "Concurrent rendering, component architecture, Context API và xử lý lỗi — có video, bài đọc và quiz.",
//            category = "Lập trình",
//            isBestseller = true,
//            modules = reactModules,
//        ),
//        Course(
//            id = ContentIds.Course.ANDROID,
//            title = "Android với Jetpack Compose",
//            imageUrl = "https://picsum.photos/seed/course_android/400/225",
//            difficulty = "Intermediate",
//            lessonCount = CourseCatalogAssembly.totalLessonCount(androidModules),
//            progress = 0f,
//            xpReward = 600,
//            price = 0L,
//            rating = 4.7f,
//            reviewCount = 820,
//            durationHours = 1.5f,
//            instructorName = "Minh Android",
//            instructorTitle = "Android Engineer",
//            instructorAvatar = "https://picsum.photos/seed/inst_android/100/100",
//            description = "Compose UI, Navigation Compose và ViewModel/StateFlow — nội dung text-first, sẵn sàng bổ sung video.",
//            category = "Lập trình",
//            isBestseller = true,
//            modules = androidModules,
//        ),
//        Course(
//            id = ContentIds.Course.PRODUCT,
//            title = "Tư duy Sản phẩm cho Developer",
//            imageUrl = "https://picsum.photos/seed/course_product/400/225",
//            difficulty = "Beginner",
//            lessonCount = CourseCatalogAssembly.totalLessonCount(productModules),
//            progress = 0f,
//            xpReward = 200,
//            price = 0L,
//            rating = 4.6f,
//            reviewCount = 410,
//            durationHours = 0.5f,
//            instructorName = "Lê Hoàng Nam",
//            instructorTitle = "Product Coach",
//            instructorAvatar = "https://picsum.photos/seed/inst_product/100/100",
//            description = "Discovery và roadmap — bài học ngắn, phù hợp học xen kẽ giữa các khóa kỹ thuật.",
//            category = "Sản phẩm",
//            modules = productModules,
//        ),
//    )
//
//    /** Playlist for video player: lessons that have a YouTube URL. */
//    val upNextLessons = courses
//        .flatMap { course -> course.modules.flatMap { it.lessons } }
//        .distinctBy { it.id }
//        .filter { lesson ->
//            MockLessonData.getLesson(lesson.id)?.videoUrl?.isNotBlank() == true
//        }
//}
