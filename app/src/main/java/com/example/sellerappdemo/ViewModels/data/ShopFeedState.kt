package com.example.sellerappdemo.ViewModels.data



import com.example.sellerappdemo.models.ProductModel
import com.example.sellerappdemo.models.UserModel


data class ShopFeedState(
    val products: List<ProductModel> = emptyList(),
    val shop: UserModel = UserModel(),
    val isLoading: Boolean = true,
)
