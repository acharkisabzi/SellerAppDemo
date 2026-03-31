package com.example.sellerappdemo.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ProductModel(
    val id: String? = "",
    @SerialName("shop_id") val shopId: String = "",
    @SerialName("shop_name") val shopName: String = "",
    val area: String = "",
    @SerialName("product_name")val productName: String = "",
    val price: Double = 0.0,
    @SerialName("image_url") val imageUrl: String = "",
    @SerialName("in_stock") val inStock: Boolean = true
)