package com.example.sellerappdemo.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UserModel(
    val id: String? = "",
    @SerialName("shop_name") val shopName: String = "",
    val area: String = "",
    val role: String = "shop"
)