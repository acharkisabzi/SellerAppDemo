package com.example.sellerappdemo.ViewModels


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sellerappdemo.ViewModels.data.AuthState
import com.example.sellerappdemo.models.UserModel
import com.example.sellerappdemo.supabase.supabase
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.auth.user.UserSession
import io.github.jan.supabase.postgrest.postgrest
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


    fun updateEmail(email: String) {
        _uiState.update { currentState ->
            currentState.copy(
                emailInput = email
            )
        }
    }

    fun updatePassword(password: String) {
        _uiState.update { currentState ->
            currentState.copy(
                passwordInput = password
            )
        }
    }

    fun updateArea(area: String) {
        _uiState.update { currentState ->
            currentState.copy(
                areaInput = area
            )
        }
    }

    fun updateShopName(name: String) {
        _uiState.update { currentState ->
            currentState.copy(
                shopNameInput = name
            )
        }
    }

    fun updateLoading(isLoading: Boolean) {
        _uiState.update { currentState ->
            currentState.copy(
                isLoading = isLoading
            )
        }
    }

    fun updateSignUp(isSignUp: Boolean) {
        _uiState.update { currentState ->
            currentState.copy(
                isSignUp = isSignUp
            )
        }
    }

    fun updateAuthenticated(isAuthenticated: Boolean) {
        _uiState.update { currentState ->
            currentState.copy(
                isAuthenticated = isAuthenticated
            )
        }
    }

    fun updateSession(session: UserSession?) {
        _uiState.update { currentState ->
            currentState.copy(session = session)
        }
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
            _uiState.update { currentState ->
                currentState.copy(
                    isLoading = true,
                    errorMessage = ""
                )
            }
            try {
                if (_uiState.value.isSignUp) {
                    // Sign up
                    val result = supabase.auth.signUpWith(Email) {
                        email = _uiState.value.emailInput
                        password = _uiState.value.passwordInput
                    }
                    // Save shop profile
                    supabase.postgrest["users"].insert(
                        UserModel(
                            id = result?.id,
                            shopName = _uiState.value.shopNameInput,
                            area = _uiState.value.areaInput,
                            role = "shop"
                        )
                    )
                } else {
                    // Sign in
                    supabase.auth.signInWith(Email) {
                        email = _uiState.value.emailInput
                        password = _uiState.value.passwordInput
                    }
                }
                val currentSession = supabase.auth.currentSessionOrNull()
                updateSession(currentSession)
                updateAuthenticated(currentSession != null)
            } catch (e: Exception) {
                _uiState.update { currentState ->
                    currentState.copy(
                        errorMessage = e.message ?: _uiState.value.genericError
                    )
                }

            } finally {
                updateLoading(false)
            }
        }
    }
}