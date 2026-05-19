package com.example.smartreview.data.repository.firebase

import com.example.smartreview.data.model.AuthResult
import com.example.smartreview.data.model.AuthUser
import com.example.smartreview.data.repository.AuthRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import kotlinx.coroutines.tasks.await

/**
 * Firebase Authentication implementation of [AuthRepository].
 */
class FirebaseAuthRepository(
    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance(),
) : AuthRepository {

    override suspend fun signInWithEmail(email: String, password: String): AuthResult {
        return try {
            val result = firebaseAuth.signInWithEmailAndPassword(email.trim(), password).await()
            val user = result.user
                ?: return AuthResult.Error("Đăng nhập thất bại. Vui lòng thử lại.")
            AuthResult.Success(toAuthUser(user.uid, user.email, email))
        } catch (e: Exception) {
            AuthResult.Error(mapFirebaseError(e))
        }
    }

    override suspend fun registerWithEmail(email: String, password: String): AuthResult {
        return try {
            val result = firebaseAuth.createUserWithEmailAndPassword(email.trim(), password).await()
            val user = result.user
                ?: return AuthResult.Error("Đăng ký thất bại. Vui lòng thử lại.")
            AuthResult.Success(toAuthUser(user.uid, user.email, email))
        } catch (e: Exception) {
            AuthResult.Error(mapFirebaseError(e))
        }
    }

    override fun signOut() {
        firebaseAuth.signOut()
    }

    override fun getCurrentUser(): AuthUser? {
        val user = firebaseAuth.currentUser ?: return null
        return toAuthUser(user.uid, user.email, "")
    }

    override fun isAuthenticated(): Boolean = firebaseAuth.currentUser != null

    private fun toAuthUser(uid: String, email: String?, fallbackEmail: String): AuthUser =
        AuthUser(uid = uid, email = email?.takeIf { it.isNotBlank() } ?: fallbackEmail.trim())

    private fun mapFirebaseError(e: Exception): String = when (e) {
        is FirebaseAuthInvalidCredentialsException ->
            "Email hoặc mật khẩu không đúng."
        is FirebaseAuthInvalidUserException ->
            "Tài khoản không tồn tại."
        is FirebaseAuthUserCollisionException ->
            "Email này đã được đăng ký."
        is FirebaseAuthWeakPasswordException ->
            "Mật khẩu quá yếu (tối thiểu 6 ký tự)."
        else -> e.localizedMessage?.takeIf { it.isNotBlank() }
            ?: "Có lỗi xảy ra. Vui lòng thử lại."
    }
}
