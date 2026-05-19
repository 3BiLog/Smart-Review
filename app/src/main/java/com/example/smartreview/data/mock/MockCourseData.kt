package com.example.smartreview.data.mock

import com.example.smartreview.data.model.Course
import com.example.smartreview.data.model.CourseModule
import com.example.smartreview.data.model.LessonItem

object MockCourseData {

    // ─── Sample lessons ──────────────────────────────────────────────────────
    private val l1 = LessonItem("l1", "Introduction to Modern React",  750, "https://picsum.photos/seed/l1/320/180", false, true)
    private val l2 = LessonItem("l2", "Component Architecture",       1125, "https://picsum.photos/seed/l2/320/180", false)
    private val l3 = LessonItem("l3", "Context API Deep Dive",         900, "https://picsum.photos/seed/l3/320/180", true)
    private val l4 = LessonItem("l4", "Zustand vs Redux Toolkit",     1335, "https://picsum.photos/seed/l4/320/180", true)
    private val l5 = LessonItem("l5", "Error Handling Strategies",     495, "https://picsum.photos/seed/l5/320/180", false)
    private val l6 = LessonItem("l6", "Optimizing Data Fetching",      750, "https://picsum.photos/seed/l6/320/180", false)
    private val l7 = LessonItem("l7", "Module Assessment",               0, "",                                      true)

    val courses = listOf(
        Course(
            id = "c1",
            title         = "Nhập môn Lập trình Python",
            imageUrl      = "https://picsum.photos/seed/python/400/225",
            difficulty    = "Beginner",
            lessonCount   = 12,
            progress      = 0.45f,
            xpReward      = 150,
            price         = 0L,
            rating        = 4.8f,
            reviewCount   = 980,
            durationHours = 8.5f,
            instructorName  = "Nguyễn Văn An",
            instructorTitle = "Senior Python Developer",
            instructorAvatar= "https://picsum.photos/seed/inst1/100/100",
            description   = "Khóa học lập trình Python từ cơ bản đến nâng cao, dành cho người mới bắt đầu. Nắm vững cú pháp, cấu trúc dữ liệu và lập trình hướng đối tượng.",
            category      = "Lập trình",
            isBestseller  = true,
            modules = listOf(
                CourseModule("m1", "Module 1: The Foundations",           3, "3 Lessons • 45m", listOf(l1, l2),     false),
                CourseModule("m2", "Module 2: Advanced State Management", 5, "5 Lessons • 1h 20m", listOf(l3, l4), true),
            )
        ),
        Course(
            id = "c2",
            title         = "Toán Cao Cấp 1: Đại số tuyến tính",
            imageUrl      = "https://picsum.photos/seed/math/400/225",
            difficulty    = "Intermediate",
            lessonCount   = 24,
            progress      = 0.10f,
            xpReward      = 300,
            price         = 299_000L,
            rating        = 4.6f,
            reviewCount   = 1_245,
            durationHours = 12.5f,
            instructorName  = "Trần Thị Bình",
            instructorTitle = "PhD Mathematics",
            instructorAvatar= "https://picsum.photos/seed/inst2/100/100",
            description   = "Nắm vững các khái niệm cơ bản của đại số tuyến tính: ma trận, vector, không gian vectơ và ánh xạ tuyến tính.",
            category      = "Toán",
            modules = listOf(
                CourseModule("m3", "Module 1: Vectors & Spaces", 4, "4 Lessons • 1h", listOf(l1, l2),     false),
                CourseModule("m4", "Module 2: Matrix Operations", 6, "6 Lessons • 1h 30m", listOf(l3, l4), true),
            )
        ),
        Course(
            id = "c3",
            title         = "Giao tiếp Tiếng Anh Chuyên nghiệp",
            imageUrl      = "https://picsum.photos/seed/english/400/225",
            difficulty    = "Advanced",
            lessonCount   = 40,
            progress      = 0.85f,
            xpReward      = 500,
            price         = 499_000L,
            rating        = 4.9f,
            reviewCount   = 2_100,
            durationHours = 20f,
            instructorName  = "Sarah Johnson",
            instructorTitle = "IELTS 9.0 Expert",
            instructorAvatar= "https://picsum.photos/seed/inst3/100/100",
            description   = "Nâng cao kỹ năng tiếng Anh chuyên nghiệp trong môi trường công sở quốc tế với các tình huống thực tế.",
            category      = "Ngoại ngữ",
            isBestseller  = true,
            modules = listOf(
                CourseModule("m5", "Module 1: Business Vocabulary", 5, "5 Lessons • 1h 15m", listOf(l1, l2), false),
                CourseModule("m6", "Module 2: Presentation Skills", 7, "7 Lessons • 1h 40m", listOf(l3, l4), true),
            )
        ),
        Course(
            id = "c4",
            title         = "Tư duy Phản biện & Giải quyết vấn đề",
            imageUrl      = "https://picsum.photos/seed/critical/400/225",
            difficulty    = "Beginner",
            lessonCount   = 8,
            progress      = 0f,
            xpReward      = 100,
            price         = 0L,
            rating        = 4.5f,
            reviewCount   = 560,
            durationHours = 4f,
            instructorName  = "Lê Hoàng Nam",
            instructorTitle = "Business Coach",
            instructorAvatar= "https://picsum.photos/seed/inst4/100/100",
            description   = "Phát triển tư duy logic và kỹ năng giải quyết vấn đề hiệu quả trong công việc và cuộc sống hàng ngày.",
            category      = "Kỹ năng mềm",
            modules = listOf(
                CourseModule("m7", "Module 1: Critical Thinking Basics", 3, "3 Lessons • 40m", listOf(l1, l2), false),
                CourseModule("m8", "Module 2: Problem Solving",          5, "5 Lessons • 1h",  listOf(l3, l4), true),
            )
        ),
        Course(
            id = "c5",
            title         = "Advanced React Patterns & Performance",
            imageUrl      = "https://picsum.photos/seed/react/400/225",
            difficulty    = "Advanced",
            lessonCount   = 48,
            progress      = 0f,
            xpReward      = 800,
            price         = 899_000L,
            rating        = 4.9f,
            reviewCount   = 1_245,
            durationHours = 12.5f,
            instructorName  = "Sarah Drasner",
            instructorTitle = "Senior Frontend Architect",
            instructorAvatar= "https://picsum.photos/seed/inst5/100/100",
            description   = "Master the most advanced concepts in React development. Concurrent rendering, custom hooks architecture, performance optimization.",
            category      = "Lập trình",
            isBestseller  = true,
            modules = listOf(
                CourseModule("m9",  "Module 1: The Foundations",           3, "3 Lessons • 45m",    listOf(l1, l2, l5),     false),
                CourseModule("m10", "Module 2: Advanced State Management", 5, "5 Lessons • 1h 20m", listOf(l3, l4),         true),
                CourseModule("m11", "Module 3: Async Programming",         4, "4 Lessons • 1h",     listOf(l5, l6, l7),     false),
            )
        ),
    )

    val upNextLessons = listOf(l1, l2, l5, l6, l7)
}