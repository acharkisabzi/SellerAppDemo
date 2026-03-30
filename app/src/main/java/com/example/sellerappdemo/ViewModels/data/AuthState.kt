package com.example.sellerappdemo.ViewModels.data

import io.github.jan.supabase.auth.user.UserSession

data class AuthState(
    val nameInput: String = "",
    val emailInput: String = "",
    val phoneInput: String = "",
    val passwordInput: String = "",
    val usernameInput: String = "",
    val areaInput: String = "",
    val isSignUp: Boolean = false,
    val isLoading: Boolean = false,
    val errorMessage: String = "",
    val isAuthenticated: Boolean? = null,
    val genericError: String = "Something went wrong",
    val session: UserSession? = null
)
