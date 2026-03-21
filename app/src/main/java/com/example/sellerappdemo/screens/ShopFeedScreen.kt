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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import com.example.sellerappdemo.models.ProductModel
import com.example.sellerappdemo.supabase
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json

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
                title = { Text("My shop") },
                actions = {
                    TextButton(onClick = {
                        scope.launch {
                            supabase.auth.signOut()
                            navController.navigate("login") {
                                popUpTo("feed") { inclusive = true }
                            }
                        }
                    }) {
                        Text("Sign out")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navController.navigate("add_product") }
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add product")
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
                        Text("No products yet", fontSize = 18.sp, fontWeight = FontWeight.Medium)
                        Text(
                            "Tap + to add your first product",
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
                    text = "₹${product.price.toInt()}",
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = if (product.inStock) "In stock" else "Out of stock",
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