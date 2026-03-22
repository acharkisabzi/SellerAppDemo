package com.example.sellerappdemo.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import com.example.sellerappdemo.R
import com.example.sellerappdemo.models.ProductModel
import com.example.sellerappdemo.supabase.supabase
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShopFeedScreen(navController: NavController) {
    val scope = rememberCoroutineScope()
    var products by remember { mutableStateOf<List<ProductModel>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    val userId = supabase.auth.currentUserOrNull()?.id ?: ""

    // Load products when screen opens
    LaunchedEffect(Unit) {
        try {
            val result = supabase.postgrest["products"]
                .select {
                    filter { eq("shop_id", userId) }
                    order("created_at", Order.DESCENDING)
                }
            products = result.decodeList<ProductModel>()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        isLoading = false
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.title_my_shop)) },
                actions = {
                    val signoutDest = "login"
                    val currentDest = "feed"
                    TextButton(onClick = {
                        scope.launch {
                            supabase.auth.signOut()
                            navController.navigate("login") {
                                popUpTo(currentDest) { inclusive = true }
                            }
                        }
                    }) {
                        Text(stringResource(R.string.btn_sign_out))
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navController.navigate("add_product") }
            ) {
                Icon(Icons.Default.Add, contentDescription = stringResource(R.string.desc_add_product))
            }
        }
    ) { innerPadding ->
        when {
            isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            products.isEmpty() -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(stringResource(R.string.msg_no_products), fontSize = 18.sp, fontWeight = FontWeight.Medium)
                        Text(
                            stringResource(R.string.msg_tap_to_add),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            else -> {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier.padding(innerPadding),
                    contentPadding = PaddingValues(2.dp)
                ) {
                    items(products) { product ->
                        ProductCard(product = product)
                    }
                }
            }
        }
    }
}

@Composable
fun ProductCard(product: ProductModel) {
    Card(
        modifier = Modifier
            .padding(2.dp)
            .fillMaxWidth(),
    ) {
        Column {
            // Product image
            AsyncImage(
                model = product.imageUrl,
                contentDescription = product.name,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f),
                contentScale = ContentScale.Crop
            )
            // Product info
            Column(modifier = Modifier.padding(8.dp)) {
                Text(
                    text = product.name,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1
                )
                Text(
                    text = stringResource(R.string.price_format, product.price.toInt()),
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = if (product.inStock) stringResource(R.string.status_in_stock) else stringResource(R.string.status_out_stock),
                    fontSize = 11.sp,
                    color = if (product.inStock)
                        MaterialTheme.colorScheme.tertiary
                    else
                        MaterialTheme.colorScheme.error
                )
            }
        }
    }
}