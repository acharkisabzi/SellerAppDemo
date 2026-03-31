package com.example.sellerappdemo.ViewModels

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.core.net.toUri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sellerappdemo.ViewModels.data.ProductActionState
import com.example.sellerappdemo.models.ProductModel
import com.example.sellerappdemo.models.UserModel
import com.example.sellerappdemo.supabase.supabase
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.storage.storage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.InputStream

class ProductActionViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(ProductActionState())
    val uiState = _uiState.asStateFlow()
    companion object {
        private val httpClient = OkHttpClient()
    }

    init {
        // Fetch the logged-in user ID immediately
        val currentUserId = supabase.auth.currentUserOrNull()?.id ?: ""
        updateUserId(currentUserId)
    }

    fun updateProduct(product: ProductModel) {
        _uiState.update { currentState ->
            currentState.copy(
                product = product
            )
        }
    }

    fun updateImageUrl(imageUrl: String) {
        _uiState.update { it.copy(product = it.product.copy(imageUrl = imageUrl)) }
    }

    // sets URI on screen load without marking image as changed
    fun initImageUri(imageUri: Uri?) {
        _uiState.update { it.copy(imageUri = imageUri) }
    }

    // user picked a new image — marks as changed so it gets re-uploaded
    fun updateImageUri(imageUri: Uri?) {
        _uiState.update { it.copy(imageChanged = true, imageUri = imageUri) }
    }

    fun updateProductName(productName: String) {
        _uiState.update{it.copy(product = it.product.copy(productName = productName))}
    }

    fun updateProductPrice(productPrice: Double?) {
        _uiState.update{it.copy(product = it.product.copy(price = productPrice ?: 0.0))}
    }

    fun updateLoading(isLoading: Boolean) {
        _uiState.update { currentState ->
            currentState.copy(
                isLoading = isLoading
            )
        }
    }

    fun updateErrorMessage(errorMessage: String) {
        _uiState.update { currentState ->
            currentState.copy(
                errorMessage = errorMessage
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

    private suspend fun downloadImageBytes(url: String, context: Context): ByteArray? =
        withContext(Dispatchers.IO) {
            if (url.contains("http")) {
                val request = Request.Builder().url(url).build()
                httpClient.newCall(request).execute().use { response ->
                    if (!response.isSuccessful) return@withContext null
                    return@withContext response.body?.bytes()
                }
            } else {
                val inputStream: InputStream =
                    context.contentResolver.openInputStream(url.toUri())!!
                val imageBytes = inputStream.readBytes()
                inputStream.close()
                return@withContext imageBytes
            }
        }


    fun postProduct(context: Context, update: Boolean) {


        // ── Validation (preserved from original) ──────────────
        if (_uiState.value.imageUri == null) {
            updateErrorMessage("Pick a photo first")
            return
        }
        if (_uiState.value.product.productName.isEmpty() || _uiState.value.product.price <= 0.0) {
            updateErrorMessage("Fill in all fields")
            return
        }

        viewModelScope.launch {
            updateLoading(true)
            updateErrorMessage("")

            try {
                // 1. Read image bytes
                val imageBytes: ByteArray? =
                    downloadImageBytes(url = _uiState.value.imageUri.toString(), context = context)
                if (imageBytes == null) {
                    updateErrorMessage("Failed to process image")
                    return@launch
                }

                // 2. Upload to Supabase Storage
                val publicUrl = if (_uiState.value.imageChanged) {
                    // delete old image first
                    val oldUrl = _uiState.value.product.imageUrl
                    if (oldUrl.isNotEmpty()) {
                        val oldPath = oldUrl.substringAfter("/products/")
                        Log.d("StorageDelete", "Attempting to delete: '$oldPath'")
                        try {
                            supabase.storage["products"].delete(listOf(oldPath))
                            Log.d("StorageDelete", "Delete success")
                        } catch (e: Exception) {
                            Log.e("StorageDelete", "Delete failed: ${e.message}")
                        }
                    }

                    // upload new image
                    val fileName = "${_uiState.value.userId}/${System.currentTimeMillis()}.jpg"
                    supabase.storage["products"].upload(path = fileName, data = imageBytes) { upsert = false }
                    supabase.storage["products"].publicUrl(fileName)
                } else {
                    _uiState.value.product.imageUrl
                }
                // 4. Get shop info
                val shop = supabase.postgrest["users"].select {
                    filter {
                        eq(
                            "id",
                            _uiState.value.userId
                        )
                    }
                }.decodeSingle<UserModel>()

                // 5. Save product to database
                if (update) {
                    val productId = _uiState.value.product.id
                    if (productId.isNullOrEmpty()) {
                        updateErrorMessage("Product ID missing")
                        return@launch
                    }
                    Log.d("ProductUpdate", "Updating product id: $productId with imageUrl: $publicUrl")
                    supabase.postgrest["products"].update(
                        ProductModel(
                            shopId      = _uiState.value.userId,
                            shopName    = shop.name,
                            area        = shop.area,
                            productName = _uiState.value.product.productName,
                            price       = _uiState.value.product.price,
                            imageUrl    = publicUrl,
                            inStock     = true
                        )
                    ) {
                        filter { eq("id", productId) }
                    }
                } else {
                    supabase.postgrest["products"].insert(
                        ProductModel(
                            shopId = _uiState.value.userId,
                            shopName = shop.name,
                            area = shop.area,
                            productName = _uiState.value.product.productName,
                            price = _uiState.value.product.price,
                            imageUrl = publicUrl,
                            inStock = true
                        )
                    )
                }
            } catch (e: Exception) {
                updateErrorMessage(e.message ?: "Something went wrong")
                return@launch
            } finally {
                updateLoading(false)
            }
            _uiState.update { it.copy(isSuccess = true) }
        }
    }
}