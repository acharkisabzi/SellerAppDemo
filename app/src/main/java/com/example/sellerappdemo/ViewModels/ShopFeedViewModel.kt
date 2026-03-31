package com.example.sellerappdemo.ViewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sellerappdemo.ViewModels.data.ShopFeedState
import com.example.sellerappdemo.models.ProductModel
import com.example.sellerappdemo.models.UserModel
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
                val shop = supabase.postgrest["users"]
                    .select { filter { eq("id", _uiState.value.shop.id.toString()) } }
                    .decodeSingle<UserModel>()
                updateShop(shop)
            } catch (_: Exception) {
                updateShopName("My Shop")
            }

            try {
                val result = supabase.postgrest["products"]
                    .select {
                        filter { eq("shop_id", _uiState.value.shop.id.toString()) }
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
        _uiState.update { it.copy(shop = it.shop.copy(name = shopName)) }
    }

    fun updateShopArea(area: String) {
        _uiState.update { it.copy(shop = it.shop.copy(area = area)) }
    }

    fun updateLoading(isLoading: Boolean) {
        _uiState.update { currentState ->
            currentState.copy(
                isLoading = isLoading
            )
        }
    }

    fun updateUserId(id: String) {
        _uiState.update { it.copy(shop = it.shop.copy(id = id)) }
    }

    fun updateShop(shop: UserModel) {
        _uiState.update { it.copy(shop = shop) }
    }
}