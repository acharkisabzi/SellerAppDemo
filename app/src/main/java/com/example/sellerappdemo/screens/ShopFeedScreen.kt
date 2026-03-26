package com.example.sellerappdemo.screens

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import com.example.sellerappdemo.R
import com.example.sellerappdemo.models.ProductModel
import com.example.sellerappdemo.supabase.supabase
import com.example.sellerappdemo.ui.theme.*
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.coroutines.launch


@Composable
fun ShopFeedScreen(navController: NavController) {
    val scope = rememberCoroutineScope()
    var products by remember { mutableStateOf<List<ProductModel>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var shopName by remember { mutableStateOf("") }
    var shopArea by remember { mutableStateOf("") }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                productViewModel.loadShopAndProducts()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }

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
        containerColor = DSurface,
        // ── Glassmorphism Bottom Nav ──────────────────────────────────────────
        bottomBar = {
            AtelierBottomNav(
                currentRoute = "feed",
                onFeedClick = { /* already here */ },
                onAddClick  = { navController.navigate("add_product") }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(DSurface)
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
        ) {

            // ── Top App Bar ───────────────────────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "SellerAppDemo",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = DOnSurface,
                    letterSpacing = (-0.5).sp
                )
                // Subtle sign-out link
                TextButton(
                    onClick = {
                        scope.launch {
                            supabase.auth.signOut()
                            navController.navigate("login") {
                                popUpTo("feed") { inclusive = true }
                            }
                        }
                    },
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = stringResource(R.string.btn_sign_out),
                        color = DOutline,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // ── Social Profile Header ─────────────────────────────────────────
            ProfileHeader(
                shopName = shopName,
                shopArea = shopArea,
                productCount = products.size
            )

            Spacer(modifier = Modifier.height(20.dp))

            // ── Collection Grid ───────────────────────────────────────────────
            CollectionSection(
                products = products,
                isLoading = isLoading,
                navController = navController
            )

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

// ─── Profile Header ───────────────────────────────────────────────────────────
@Composable
private fun ProfileHeader(
    shopName: String,
    shopArea: String,
    productCount: Int
) {
    Box(modifier = Modifier.fillMaxWidth()) {

        // Banner gradient (acts as surface-container-high area)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp)
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            Color(0xFFE2DFFF), // secondary-container
                            Color(0xFFDEE3E6)  // surface-container-highest
                        )
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 104.dp) // avatar overlaps banner
        ) {
            // Avatar + action row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Circular avatar with border
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape)
                        .background(DSurfaceContainerLowest)
                        .shadow(elevation = 2.dp, shape = CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    // Initials fallback avatar
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(DSecondaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = shopName.take(1).uppercase().ifEmpty { "S" },
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold,
                            color = DSecondary
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Stats row — right-aligned
                Row(
                    horizontalArrangement = Arrangement.spacedBy(24.dp),
                    modifier = Modifier.padding(bottom = 4.dp)
                ) {
                    StatItem(value = productCount.toString(), label = "PRODUCTS")
                    StatItem(value = "4.9", label = "RATING")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Shop name + location
            Column(modifier = Modifier.padding(horizontal = 24.dp)) {
                Text(
                    text = shopName.ifEmpty { "My Shop" },
                    fontSize = 26.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = DOnSurface,
                    letterSpacing = (-0.5).sp
                )
                if (shopArea.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "@${shopArea.lowercase().replace(" ", "")}",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = DSecondary
                    )
                }
            }
        }
    }
}

// ─── Stat Item ────────────────────────────────────────────────────────────────
@SuppressLint("RememberInComposition")
@Composable
private fun StatItem(value: String, label: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = value,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = DOnSurface
        )
        Text(
            text = label,
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            color = DOutline,
            letterSpacing = 1.2.sp
        )
    }
}

// ─── Collection Section ───────────────────────────────────────────────────────
@Composable
private fun CollectionSection(
    products: List<ProductModel>,
    isLoading: Boolean,
    navController: NavController
) {
    // Section header
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Collection",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = DOnSurface,
            letterSpacing = (-0.3).sp
        )
        Text(
            text = "${products.size} items",
            fontSize = 13.sp,
            color = DOutline,
            fontWeight = FontWeight.Medium
        )
    }

    Spacer(modifier = Modifier.height(16.dp))

    when {
        isLoading -> {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    color = DSecondary,
                    strokeWidth = 2.dp,
                    modifier = Modifier.size(32.dp)
                )
            }
        }

        products.isEmpty() -> {
            // Empty state — editorial style
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(DSurfaceContainerLow)
                    .padding(vertical = 56.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = stringResource(R.string.msg_no_products),
                        fontSize = 17.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = DOnSurface
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = stringResource(R.string.msg_tap_to_add),
                        fontSize = 14.sp,
                        color = DOnSurfaceVariant
                    )
                }
            }
        }

        else -> {
            // 2-column grid — rendered in a fixed-height column since we're inside verticalScroll
            // We use a chunked list to avoid nested scroll conflicts
            val rows = products.chunked(2)
            Column(
                modifier = Modifier.padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                rows.forEach { rowProducts ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        rowProducts.forEach { product ->
                            AtelierProductCard(
                                product = product,
                                modifier = Modifier.weight(1f),
                                navController = navController
                            )
                        }
                        // Fill remaining space if odd number in last row
                        if (rowProducts.size == 1) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }
    }
}

// ─── Product Card ─────────────────────────────────────────────────────────────
@SuppressLint("RememberInComposition")
@Composable
private fun AtelierProductCard(
    product: ProductModel,
    modifier: Modifier = Modifier,
    navController: NavController
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(DSurfaceContainerLowest)
            // Ambient shadow — feels architectural, not stamped
            .shadow(
                elevation = 2.dp,
                shape = RoundedCornerShape(12.dp),
                ambientColor = DOnSurface.copy(alpha = 0.04f),
                spotColor = DOnSurface.copy(alpha = 0.04f)
            )
           .clickable(
                interactionSource = MutableInteractionSource(),
        indication = null,
        enabled = true,
        onClickLabel = "Edit ${product.name}",
        onClick = { navController.navigate(product) }
    ),
    ) {
        // Product image — 1:1 aspect ratio
        AsyncImage(
            model = product.imageUrl,
            contentDescription = product.name,
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp)),
            contentScale = ContentScale.Crop
        )

        // Product info
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = product.name,
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp,
                color = DOnSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(2.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.price_format, product.price.toInt()),
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = DSecondary
                )
                // Stock badge — tonal, no hard border
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(999.dp))
                        .background(
                            if (product.inStock)
                                DSecondaryContainer
                            else
                                Color(0xFFFFE0E0)
                        )
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = if (product.inStock)
                            stringResource(R.string.status_in_stock)
                        else
                            stringResource(R.string.status_out_stock),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (product.inStock) DSecondary else DError,
                        letterSpacing = 0.3.sp
                    )
                }
            }
        }
    }
}

// ─── Bottom Navigation ────────────────────────────────────────────────────────
@Composable
fun AtelierBottomNav(
    currentRoute: String,
    onFeedClick: () -> Unit,
    onAddClick: () -> Unit
) {
    // Glassmorphism surface — surface-container-lowest @ 70% opacity
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = DSurfaceContainerLowest.copy(alpha = 0.92f),
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
            )
            // Ambient top shadow — cloud, not stamp
            .shadow(
                elevation = 0.dp,
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
            )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 48.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Feed tab
            NavTabItem(
                icon = {
                    Icon(
                        imageVector = Icons.Outlined.Home,
                        contentDescription = "Feed",
                        modifier = Modifier.size(26.dp),
                        tint = if (currentRoute == "feed") DSecondary else DOutline
                    )
                },
                label = "Shop",
                isSelected = currentRoute == "feed",
                onClick = onFeedClick
            )

            // Add Product tab — highlighted center pill
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(
                            colors = listOf(DSecondary, DSecondaryDim)
                        )
                    )
                    .clickable { onAddClick() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = stringResource(R.string.desc_add_product),
                    tint = DOnSecondary,
                    modifier = Modifier.size(26.dp)
                )
            }
        }
    }
}

@Composable
private fun NavTabItem(
    icon: @Composable () -> Unit,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        icon()
        Spacer(modifier = Modifier.height(3.dp))
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color = if (isSelected) DSecondary else DOutline,
            letterSpacing = 0.2.sp
        )
    }
}