package com.example.sellerappdemo.ViewModels

import android.content.Context
import android.net.Uri
import androidx.core.net.toUri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sellerappdemo.ViewModels.data.ProductActionState
import com.example.sellerappdemo.models.ProductModel
import com.example.sellerappdemo.supabase.supabase
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.postgrest
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
    private val httpClient = OkHttpClient()

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
        _uiState.update { currentState ->
            currentState.copy(
                imageUrl = imageUrl
            )
        }
    }

    fun updateImageUri(imageUri: Uri?) {
        _uiState.update { currentState ->
            currentState.copy(
                imageUri = imageUri
            )
        }
    }

    fun updateProductName(productName: String) {
        _uiState.update { currentState ->
            currentState.copy(
                productName = productName
            )
        }
    }

    fun updateProductPrice(productPrice: Double?) {
        _uiState.update { currentState ->
            currentState.copy(
                productPrice = productPrice
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
        if (_uiState.value.productName.isEmpty() || _uiState.value.productPrice!! <= 0.0) {
            updateErrorMessage("Fill in all fields")
            return
        }
        // ── Upload & Save (fully preserved from original) ──────
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
                val fileName = "${_uiState.value.userId}/${System.currentTimeMillis()}.jpg"
                supabase.storage["products"].upload(
                    path = fileName,
                    data = imageBytes,
                ) { upsert = update }


                // 3. Get public URL
                val publicUrl = supabase.storage["products"].publicUrl(fileName)
                updateImageUrl(publicUrl)

                // 4. Get shop info
                val userDoc = supabase.postgrest["users"].select {
                    filter {
                        eq(
                            "id",
                            _uiState.value.userId
                        )
                    }
                }.decodeSingle<Map<String, String>>()

                // 5. Save product to database
                if (update) {
                    supabase.postgrest["products"].upsert(
                        ProductModel(
                            id = _uiState.value.product.id,
                            shopId = _uiState.value.userId,
                            shopName = userDoc["shop_name"] ?: "",
                            area = userDoc["area"] ?: "",
                            name = _uiState.value.productName,
                            price = _uiState.value.productPrice!!,
                            imageUrl = publicUrl,
                            inStock = true
                        )
                    )
                } else {
                    supabase.postgrest["products"].insert(
                        ProductModel(
                            id = "",
                            shopId = _uiState.value.userId,
                            shopName = userDoc["shop_name"] ?: "",
                            area = userDoc["area"] ?: "",
                            name = _uiState.value.productName,
                            price = _uiState.value.productPrice!!,
                            imageUrl = publicUrl,
                            inStock = true
                        )
                    )
                }
            } catch (e: Exception) {
                updateErrorMessage(e.message ?: "Something went wrong")
            } finally {
                updateLoading(false)
                _uiState.update { it.copy(isSuccess = true) }
            }
        }
    }
}