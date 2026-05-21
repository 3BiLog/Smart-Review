package com.example.smartreview.data.mock

import com.example.smartreview.data.model.Quiz
import com.example.smartreview.data.model.QuizOption
import com.example.smartreview.data.model.QuizQuestion

object MockQuizData {

    private val quizzes = listOf(
        Quiz(
            id = "q_l1_check",
            title = "Kiểm tra: React cơ bản",
            subtitle = "Bài l1 · 3 câu hỏi",
            lessonId = "l1",
            questions = listOf(
                question(
                    id = "q1",
                    prompt = "React chủ yếu dùng để làm gì?",
                    options = listOf(
                        opt("a", "Xây dựng giao diện người dùng (UI)"),
                        opt("b", "Quản lý cơ sở dữ liệu server"),
                        opt("c", "Biên dịch hệ điều hành"),
                    ),
                    correct = "a",
                    explanation = "React là thư viện UI phía client.",
                ),
                question(
                    id = "q2",
                    prompt = "Đơn vị cấu thành UI trong React gọi là gì?",
                    options = listOf(
                        opt("a", "Module"),
                        opt("b", "Component"),
                        opt("c", "Packet"),
                    ),
                    correct = "b",
                    explanation = "UI được chia thành các component tái sử dụng.",
                ),
                question(
                    id = "q3",
                    prompt = "Props trong React dùng để?",
                    options = listOf(
                        opt("a", "Truyền dữ liệu từ component cha xuống con"),
                        opt("b", "Lưu trữ file ảnh"),
                        opt("c", "Cấu hình Firebase"),
                    ),
                    correct = "a",
                ),
            ),
        ),
        Quiz(
            id = "q_l2_check",
            title = "Kiểm tra: Component Architecture",
            subtitle = "Bài l2 · 2 câu hỏi",
            lessonId = "l2",
            questions = listOf(
                question(
                    id = "q1",
                    prompt = "Nguyên tắc tốt khi thiết kế component?",
                    options = listOf(
                        opt("a", "Một component làm mọi thứ"),
                        opt("b", "Trách nhiệm rõ ràng, kích thước vừa phải"),
                        opt("c", "Không dùng props"),
                    ),
                    correct = "b",
                ),
                question(
                    id = "q2",
                    prompt = "Luồng dữ liệu khuyến nghị trong React?",
                    options = listOf(
                        opt("a", "Props xuống, sự kiện lên"),
                        opt("b", "Chỉ dùng global mutable state"),
                        opt("c", "Không đồng bộ UI"),
                    ),
                    correct = "a",
                ),
            ),
        ),
    ).associateBy { it.id }

    fun getQuiz(quizId: String): Quiz? = quizzes[quizId]

    private fun opt(id: String, label: String) = QuizOption(id = id, label = label)

    private fun question(
        id: String,
        prompt: String,
        options: List<QuizOption>,
        correct: String,
        explanation: String = "",
    ) = QuizQuestion(
        id = id,
        prompt = prompt,
        options = options,
        correctOptionId = correct,
        explanation = explanation,
    )
}
