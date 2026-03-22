package com.example.sellerappdemo.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddProductScreen(navController: NavController) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var productName by remember { mutableStateOf("") }
    var productPrice by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    val userId = supabase.auth.currentUserOrNull()?.id ?: ""

    // Image picker launcher
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri -> imageUri = uri }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.title_new_product)) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.desc_back))
                    }
                },
                actions = {
                    val pickPhotoMsg = stringResource(R.string.msg_pick_photo)
                    val fillFieldsMsg = stringResource(R.string.msg_fill_fields)
                    val genericErrorMsg = stringResource(R.string.error_generic)
                    
                    TextButton(
                        onClick = {
                            if (imageUri == null) {
                                errorMessage = pickPhotoMsg
                                return@TextButton
                            }
                            if (productName.isEmpty() || productPrice.isEmpty()) {
                                errorMessage = fillFieldsMsg
                                return@TextButton
                            }
                            scope.launch {
                                isLoading = true
                                errorMessage = ""
                                try {
                                    // 1. Read image bytes
                                    val inputStream: InputStream =
                                        context.contentResolver.openInputStream(imageUri!!)!!
                                    val imageBytes = inputStream.readBytes()
                                    inputStream.close()

                                    // 2. Upload to Supabase Storage
                                    val fileName = "$userId/${System.currentTimeMillis()}.jpg"
                                    supabase.storage["products"].upload(
                                        path = fileName,
                                        data = imageBytes,

                                    ){upsert = false}

                                    // 3. Get public URL
                                    val imageUrl = supabase.storage["products"]
                                        .publicUrl(fileName)

                                    // 4. Get shop info
                                    val userDoc = supabase.postgrest["users"]
                                        .select { filter { eq("id", userId) } }
                                        .decodeSingle<Map<String, String>>()

                                    // 5. Save product to database
                                    supabase.postgrest["products"].insert(
                                        ProductModel(
                                            id = "",
                                            shopId = userId,
                                            shopName = userDoc["shop_name"] ?: "",
                                            area = userDoc["area"] ?: "",
                                            name = productName,
                                            price = productPrice.toDouble(),
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
                        enabled = !isLoading
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(modifier = Modifier.size(18.dp))
                        } else {
                            Text(stringResource(R.string.btn_post), style = MaterialTheme.typography.titleMedium)
                        }
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Image picker area — square like Instagram
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
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
                } else {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(stringResource(R.string.msg_tap_pick_photo))
                        Text(
                            stringResource(R.string.msg_square_best),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }

            // Product details
            Column(modifier = Modifier.padding(16.dp)) {
                OutlinedTextField(
                    value = productName,
                    onValueChange = { productName = it },
                    label = { Text(stringResource(R.string.label_product_name)) },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = productPrice,
                    onValueChange = { productPrice = it },
                    label = { Text(stringResource(R.string.label_price)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )

                if (errorMessage.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = errorMessage,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}