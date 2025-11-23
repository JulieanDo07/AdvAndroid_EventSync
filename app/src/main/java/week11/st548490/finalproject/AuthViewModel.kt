package week11.st548490.finalproject.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.AuthResult
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class AuthViewModel : ViewModel() {
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    private val _uiState = MutableStateFlow<AuthUiState>(AuthUiState.Idle)
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    private val _email = MutableStateFlow("")
    val email: StateFlow<String> = _email.asStateFlow()

    private val _password = MutableStateFlow("")
    val password: StateFlow<String> = _password.asStateFlow()

    private val _validationError = MutableStateFlow("")
    val validationError: StateFlow<String> = _validationError.asStateFlow()

    fun updateEmail(email: String) {
        _email.value = email
        _validationError.value = ""
    }

    fun updatePassword(password: String) {
        _password.value = password
        _validationError.value = ""
    }

    private fun validateForm(email: String, password: String): Boolean {
        return when {
            email.isEmpty() -> {
                _validationError.value = "Email is required"
                false
            }
            !email.contains("@") || !email.contains(".") -> {
                _validationError.value = "Please enter a valid email"
                false
            }
            password.isEmpty() -> {
                _validationError.value = "Password is required"
                false
            }
            password.length < 6 -> {
                _validationError.value = "Password must be at least 6 characters"
                false
            }
            else -> {
                _validationError.value = ""
                true
            }
        }
    }

    private suspend fun createUserProfile(userId: String, email: String) {
        val userData = hashMapOf(
            "email" to email,
            "createdAt" to com.google.firebase.Timestamp.now(),
            "displayName" to email.substringBefore("@")
        )

        firestore.collection("users").document(userId)
            .set(userData)
            .await()
    }

    fun login(email: String, password: String) {
        if (!validateForm(email, password)) return

        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            try {
                val result = auth.signInWithEmailAndPassword(email, password).await()
                _uiState.value = AuthUiState.Success(result)
            } catch (e: Exception) {
                _uiState.value = AuthUiState.Error(e.message ?: "Login failed. Please check your credentials.")
            }
        }
    }

    fun signUp(email: String, password: String) {
        if (!validateForm(email, password)) return

        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            try {
                val result = auth.createUserWithEmailAndPassword(email, password).await()
                // Create user profile in Firestore
                createUserProfile(result.user?.uid ?: "", email)
                _uiState.value = AuthUiState.Success(result)
            } catch (e: Exception) {
                _uiState.value = AuthUiState.Error(e.message ?: "Sign up failed. Please try again.")
            }
        }
    }

    fun resetPassword(email: String) {
        if (email.isEmpty() || !email.contains("@") || !email.contains(".")) {
            _validationError.value = "Please enter a valid email address"
            return
        }

        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            try {
                auth.sendPasswordResetEmail(email).await()
                _uiState.value = AuthUiState.PasswordResetSent
            } catch (e: Exception) {
                _uiState.value = AuthUiState.Error(e.message ?: "Failed to send password reset email.")
            }
        }
    }

    fun resetState() {
        _uiState.value = AuthUiState.Idle
        _validationError.value = ""
    }
}

sealed class AuthUiState {
    object Idle : AuthUiState()
    object Loading : AuthUiState()
    object PasswordResetSent : AuthUiState()
    data class Success(val authResult: AuthResult) : AuthUiState()
    data class Error(val message: String) : AuthUiState()
}