package com.example.sellerappdemo.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UserModel(
    val id: String? = "",
    val username: String = "",
    val name: String = "",
    val email: String = "",
    val phone: String = "",
    val area: String = "",
    @SerialName("is_shop") val isShop: Boolean = false
)