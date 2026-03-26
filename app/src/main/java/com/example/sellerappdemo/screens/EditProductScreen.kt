package com.example.sellerappdemo.screens

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import com.example.sellerappdemo.R
import com.example.sellerappdemo.models.ProductModel
import com.example.sellerappdemo.supabase.supabase
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.storage.storage
import kotlinx.coroutines.launch
import java.io.InputStream
import com.example.sellerappdemo.ui.theme.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProductScreen(
    navController: NavController,
    productModel: ProductModel,
    viewModel: ProductActionViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val productId = productModel.id
    var imageUri by remember { mutableStateOf<Uri?>(productModel.imageUrl.toUri()) }
    var productName by remember { mutableStateOf(productModel.name) }
    var productPrice by remember { mutableStateOf(productModel.price) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    val userId = supabase.auth.currentUserOrNull()?.id ?: ""

    // Image picker launcher — preserved from original
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri -> imageUri = uri }

    val pickPhotoMsg = stringResource(R.string.msg_pick_photo)
    val fillFieldsMsg = stringResource(R.string.msg_fill_fields)
    val genericErrorMsg = stringResource(R.string.error_generic)

    Scaffold(containerColor = ADAtSurface, topBar = {
        TopAppBar(
            title = {
                Text(
                    text = "SellerAppDemo",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 21.sp,
                    letterSpacing = (-0.5).sp,
                    color = ADAtOnSurface
                )
            }, navigationIcon = {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.desc_back),
                        tint = ADAtOnSurface
                    )
                }
            }, colors = TopAppBarDefaults.topAppBarColors(
                containerColor = ADAtSurfaceLowest.copy(alpha = 0.92f)
            )
        )
    }, bottomBar = {
        // Reuse shared nav component from ShopFeedScreen.kt (same package)
        AtelierBottomNav(currentRoute = "add_product", onFeedClick = {
            navController.navigate("feed") {
                popUpTo("feed") { inclusive = true }
            }
        }, onAddClick = { /* already on this screen */ })
    }) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
        ) {
            // ── Editorial Header ──────────────────────────────────────────────
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 24.dp, end = 24.dp, top = 16.dp, bottom = 4.dp)
            ) {
                Text(
                    text = "EDIT LISTING",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = ADAtSecondary,
                    letterSpacing = 2.sp
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = stringResource(R.string.title_edit_product),
                    fontSize = 32.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = (-0.5).sp,
                    color = ADAtOnSurface
                )
            }

                Spacer(Modifier.height(20.dp))

                // ── Image Picker (Instagram-style square) ─────────────────────────
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                        .aspectRatio(1f)
                        .clip(RoundedCornerShape(20.dp))
                        .background(ADAtSurfaceLow)
                        .clickable { imagePickerLauncher.launch("image/*") },
                    contentAlignment = Alignment.Center
                ) {
                    if (imageUri != null) {
                        AsyncImage(
                            model = imageUri,
                            contentDescription = stringResource(R.string.desc_product_photo),
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                        // "Change Photo" pill overlay
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(14.dp)
                                .clip(RoundedCornerShape(50.dp))
                                .background(ADAtSecondary.copy(alpha = 0.88f))
                                .padding(horizontal = 14.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = "Change Photo",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = ADAtOnSecondary
                            )
                        }
                    } else {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(60.dp)
                                    .clip(RoundedCornerShape(50.dp))
                                    .background(ADAtSecContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = null,
                                    tint = ADAtSecondary,
                                    modifier = Modifier.size(30.dp)
                                )
                            }
                            Text(
                                text = stringResource(R.string.msg_tap_pick_photo),
                                fontSize = 15.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = ADAtOnSurface
                            )
                            Text(
                                text = stringResource(R.string.msg_square_best),
                                fontSize = 13.sp,
                                color = ADAtOnSurfaceVar
                            )
                        }
                    }
                }

                Spacer(Modifier.height(28.dp))

                // ── Form Fields ───────────────────────────────────────────────────
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    // Product Name
                    AtelierFormField(
                        value = productName,
                        onValueChange = { productName = it },
                        label = stringResource(R.string.label_product_name),
                        keyboardType = KeyboardType.Text
                    )

                    // Price
                    AtelierFormField(
                        value = productPrice.toString(),
                        onValueChange = { productPrice = it.toDouble() },
                        label = stringResource(R.string.label_price),
                        keyboardType = KeyboardType.Number
                    )

                    // Error message with styled container
                    if (errorMessage.isNotEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(ADAtErrContainer)
                                .padding(horizontal = 16.dp, vertical = 12.dp)
                        ) {
                            Text(
                                text = errorMessage,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                color = ADAtError
                            )
                        }
                    }

                    Spacer(Modifier.height(4.dp))

                    // Post / Save Button — full-width, pill-shaped, violet
                    Button(
                        onClick = {
                            // ── Validation (preserved from original) ──────────────
                            if (imageUri == null) {
                                errorMessage = pickPhotoMsg
                                return@Button
                            }
                            if (productName.isEmpty() || productPrice.toString().isEmpty()) {
                                errorMessage = fillFieldsMsg
                                return@Button
                            }
                            // ── Upload & Save (fully preserved from original) ──────
                            scope.launch {
                                isLoading = true
                                errorMessage = ""
                                try {
                                    // 1. Read image bytes
                                    val imageBytes: ByteArray? = downloadImageBytes(url = imageUri.toString(), context = context)

                                    // 2. Upload to Supabase Storage
                                    val fileName = "$userId/${System.currentTimeMillis()}.jpg"
                                    if(imageBytes!=null){
                                        supabase.storage["products"].upload(
                                            path = fileName,
                                            data = imageBytes,
                                        ) { upsert = true }
                                    } else {
                                        errorMessage = "image bytes empty"
                                    }

                                    // 3. Get public URL
                                    val imageUrl = supabase.storage["products"].publicUrl(fileName)

                                    // 4. Get shop info
                                    val userDoc = supabase.postgrest["users"].select {
                                        filter {
                                            eq(
                                                "id",
                                                userId
                                            )
                                        }
                                    }.decodeSingle<Map<String, String>>()

                                    // 5. Save product to database
                                    supabase.postgrest["products"].upsert(
                                        ProductModel(
                                            id = productId,
                                            shopId = userId,
                                            shopName = userDoc["shop_name"] ?: "",
                                            area = userDoc["area"] ?: "",
                                            name = productName,
                                            price = productPrice,
                                            imageUrl = imageUrl,
                                            inStock = true
                                        )
                                    )
                                    navController.popBackStack()
                                } catch (e: Exception) {
                                    errorMessage = e.message ?: genericErrorMsg
                                }
                                isLoading = false
                            }
                        },
                        enabled = !isLoading,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(50.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ADAtSecondary,
                            contentColor = ADAtOnSecondary,
                            disabledContainerColor = ADAtSecondary.copy(alpha = 0.5f),
                            disabledContentColor = ADAtOnSecondary.copy(alpha = 0.6f)
                        ),
                        elevation = ButtonDefaults.buttonElevation(
                            defaultElevation = 0.dp, pressedElevation = 0.dp
                        )
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(22.dp),
                                color = ADAtOnSecondary,
                                strokeWidth = 2.5.dp
                            )
                        } else {
                            Text(
                                text = stringResource(R.string.btn_post_edited),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.3.sp
                            )
                        }
                    }

                    Spacer(Modifier.height(8.dp))
                }
            }
        }
        Text(
            text = "ProductModel is null",
            style = TextStyle(color = DError),
            fontWeight = FontWeight.ExtraBold
        )

}

// ─── Atelier Form Field ───────────────────────────────────────────────────────
// Borderless text field following the "Ghost Border" / surface-layering rule
@Composable
private fun AtelierFormField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    keyboardType: KeyboardType = KeyboardType.Text
) {
    Column {
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = ADAtOnSurfaceVar,
            letterSpacing = 0.4.sp,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        TextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            textStyle = TextStyle(
                fontWeight = FontWeight.Medium, fontSize = 16.sp, color = ADAtOnSurface
            ),
            singleLine = true,
            colors = TextFieldDefaults.colors(
                focusedContainerColor = ADAtSurfaceLowest,
                unfocusedContainerColor = ADAtSurfaceLowest,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                focusedTextColor = ADAtOnSurface,
                unfocusedTextColor = ADAtOnSurface,
                cursorColor = ADAtSecondary,
                focusedLabelColor = ADAtSecondary,
                unfocusedLabelColor = ADAtOnSurfaceVar
            )
        )
    }
}

private suspend fun downloadImageBytes(url: String, context: Context): ByteArray? = withContext(Dispatchers.IO) {
    if(url.contains("http")) {
        val client = OkHttpClient()
        val request = Request.Builder().url(url).build()

        client.newCall(request).execute().use { response ->
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