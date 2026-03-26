package com.example.sellerappdemo.ViewModels.data



import com.example.sellerappdemo.models.ProductModel


data class ShopFeedState(
    var products: List<ProductModel> = emptyList(),
    var isLoading: Boolean = true,
    var shopName: String = "",
    var shopArea: String = "",
    val userId: String = "",
)
