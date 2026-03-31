package com.example.sellerappdemo.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import com.example.sellerappdemo.R
import com.example.sellerappdemo.ViewModels.ProductActionViewModel
import com.example.sellerappdemo.ui.theme.*
import com.example.sellerappdemo.ui.theme.widgets.AtelierFormField


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddProductScreen(
    navController: NavController,
    viewModel: ProductActionViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(state.isSuccess) {
        if (state.isSuccess) {
            navController.popBackStack()
        }
    }
    val context = LocalContext.current


    // Image picker launcher — preserved from original
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri -> viewModel.updateImageUri(uri) }

    Scaffold(
        containerColor = ADAtSurface,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "SellerAppDemo",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 21.sp,
                        letterSpacing = (-0.5).sp,
                        color = ADAtOnSurface
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.desc_back),
                            tint = ADAtOnSurface
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = ADAtSurfaceLowest.copy(alpha = 0.92f)
                )
            )
        },
        bottomBar = {
            // Reuse shared nav component from ShopFeedScreen.kt (same package)
            AtelierBottomNav(
                currentRoute = "add_product",
                onFeedClick = {
                    navController.navigate("feed") {
                        popUpTo("feed") { inclusive = true }
                    }
                },
                onAddClick = { /* already on this screen */ }
            )
        }
    ) { paddingValues ->
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
                    text = "NEW LISTING",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = ADAtSecondary,
                    letterSpacing = 2.sp
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = stringResource(R.string.title_new_product),
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
                if (state.imageUri != null) {
                    AsyncImage(
                        model = state.imageUri,
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
                    value = state.product.productName,
                    onValueChange = { viewModel.updateProductName(it) },
                    label = stringResource(R.string.label_product_name),
                    keyboardType = KeyboardType.Text
                )

                // Price
                AtelierFormField(
                    value = if (state.product.price == 0.0) "" else state.product.price.toString(),
                    onValueChange = { viewModel.updateProductPrice(it.toDoubleOrNull() ?: 0.0) },
                    label = stringResource(R.string.label_price),
                    keyboardType = KeyboardType.Number
                )

                // Error message with styled container
                if (state.errorMessage.isNotEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(ADAtErrContainer)
                            .padding(horizontal = 16.dp, vertical = 12.dp)
                    ) {
                        Text(
                            text = state.errorMessage,
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
                        viewModel.postProduct(context = context, update = false)
                    },
                    enabled = !state.isLoading,
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
                        defaultElevation = 0.dp,
                        pressedElevation = 0.dp
                    )
                ) {
                    if (state.isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(22.dp),
                            color = ADAtOnSecondary,
                            strokeWidth = 2.5.dp
                        )
                    } else {
                        Text(
                            text = stringResource(R.string.btn_post),
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
}
