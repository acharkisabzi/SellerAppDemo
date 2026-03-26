package com.example.sellerappdemo.ViewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sellerappdemo.ViewModels.data.ShopFeedState
import com.example.sellerappdemo.models.ProductModel
import com.example.sellerappdemo.supabase.supabase
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ShopFeedViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(ShopFeedState())
    val uiState = _uiState.asStateFlow()

    init {
        updateUserId(supabase.auth.currentUserOrNull()?.id ?: "")
        loadShopAndProducts()
    }

    fun loadShopAndProducts() {
        viewModelScope.launch {
            try {
                // Fetch shop profile
                val userDoc = supabase.postgrest["users"]
                    .select { filter { eq("id", _uiState.value.userId) } }
                    .decodeSingle<Map<String, String>>()
                updateShopName(userDoc["shop_name"] ?: "My Shop")
                updateShopArea(userDoc["area"] ?: "")
            } catch (_: Exception) {
                updateShopName("My Shop")
            }

            try {
                val result = supabase.postgrest["products"]
                    .select {
                        filter { eq("shop_id", _uiState.value.userId) }
                        order("created_at", Order.DESCENDING)
                    }
                updateProducts(result.decodeList<ProductModel>())
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                updateLoading(false)
            }
        }
    }

    fun updateProducts(products: List<ProductModel>) {
        _uiState.update { currentState ->
            currentState.copy(
                products = products
            )
        }
    }

    fun updateShopName(shopName: String) {
        _uiState.update { currentState ->
            currentState.copy(
                shopName = shopName
            )
        }
    }

    fun updateShopArea(shopArea: String) {
        _uiState.update { currentState ->
            currentState.copy(
                shopArea = shopArea
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

    fun updateUserId(userId: String) {
        _uiState.update { currentState ->
            currentState.copy(
                userId = userId
            )
        }
    }
}