package com.example.sellerappdemo.ViewModels.data

import android.net.Uri
import com.example.sellerappdemo.models.ProductModel

data class ProductActionState(
    val product: ProductModel = ProductModel(),
    val imageUri: Uri? = null,
    val productName: String = "",
    val productPrice: Double? = 0.0,
    val isLoading: Boolean = false,
    val errorMessage: String = "",
    val userId: String = "",
    val isSuccess: Boolean = false,
    val imageChanged: Boolean = false
)
