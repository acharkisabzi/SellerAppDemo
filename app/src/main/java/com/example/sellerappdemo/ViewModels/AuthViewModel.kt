package com.example.sellerappdemo.ViewModels


import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sellerappdemo.ViewModels.data.AuthState
import com.example.sellerappdemo.models.UserModel
import com.example.sellerappdemo.supabase.supabase
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.auth.user.UserSession
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.rpc
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AuthViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(AuthState())
    val uiState = _uiState.asStateFlow()

    init {
        val currentSession = supabase.auth.currentSessionOrNull()
        updateSession(currentSession)
        updateAuthenticated(currentSession != null)
    }

    fun updateName(name: String) {
        _uiState.update { it.copy(nameInput = name) }
    }

    fun updateEmail(email: String) {
        _uiState.update { it.copy(emailInput = email) }
    }

    fun updatePassword(password: String) {
        _uiState.update { it.copy(passwordInput = password) }
    }

    fun updateArea(area: String) {
        _uiState.update { it.copy(areaInput = area) }
    }

    fun updateUserName(userName: String) {
        _uiState.update { it.copy(usernameInput = userName) }
    }

    fun updateLoading(isLoading: Boolean) {
        _uiState.update { it.copy(isLoading = isLoading) }
    }

    fun updateSignUp(isSignUp: Boolean) {
        _uiState.update { it.copy(isSignUp = isSignUp) }
    }

    fun updateAuthenticated(isAuthenticated: Boolean) {
        _uiState.update { it.copy(isAuthenticated = isAuthenticated) }
    }

    fun updateSession(session: UserSession?) {
        _uiState.update { it.copy(session = session) }
    }

    fun updateError(error: String) {
        _uiState.update { it.copy(errorMessage = error) }
    }

    fun signOut() {
        viewModelScope.launch {
            try {
                updateLoading(true)
                supabase.auth.signOut()
                updateAuthenticated(false)
                _uiState.update { currentState ->
                    currentState.copy(
                        session = null,
                        isAuthenticated = false,
                        emailInput = "",
                        passwordInput = "",
                        errorMessage = ""
                    )
                }
            } catch (e: Exception) {
                updateAuthenticated(false)
                updateSession(null)
            } finally {
                updateLoading(false)
            }
        }
    }

    fun authorize() {
        viewModelScope.launch {
            updateLoading(true)
            updateError("")
            try {
                val exists = checkUserExists(_uiState.value.emailInput)
                if (_uiState.value.isSignUp) {
                    // Sign up
                    if (!exists) {
                        signUp(_uiState.value.emailInput, _uiState.value.passwordInput)
                    } else {
                        updateError("User already exists")
                        return@launch
                    }
                } else {
                    // Sign in
                    signIn(_uiState.value.emailInput, _uiState.value.passwordInput)
                }
                val currentSession = supabase.auth.currentSessionOrNull()
                updateSession(currentSession)
                updateAuthenticated(currentSession != null)
            } catch (e: Exception) {
                Log.d("AuthViewModel", e.toString())
                updateError("Error signing in")
            } finally {
                updateLoading(false)
            }
        }
    }

    private suspend fun checkUserExists(email: String): Boolean {
        return supabase.postgrest.rpc(
            function = "check_user_exists",
            parameters = mapOf("email_input" to email)
        ).decodeAs<Boolean>()
    }

    private suspend fun signIn(emailIn: String, passwordIn: String) {
        supabase.auth.signInWith(Email) {
            email = emailIn
            password = passwordIn
        }
    }

    private suspend fun signUp(emailIn: String, passwordIn: String) {
        val result = supabase.auth.signUpWith(Email) {
            email = emailIn
            password = passwordIn
        }
        supabase.postgrest["users"].insert(
            UserModel(
                id = result?.id,
                name = _uiState.value.nameInput,
                username = _uiState.value.usernameInput,
                area = _uiState.value.areaInput,
                email = _uiState.value.emailInput,
                phone = _uiState.value.phoneInput,
                isShop = true
            )
        )
    }
}