package com.example.sellerappdemo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.example.sellerappdemo.ViewModels.AuthViewModel
import com.example.sellerappdemo.models.ProductModel
import com.example.sellerappdemo.screens.AddProductScreen
import com.example.sellerappdemo.screens.EditProductScreen
import com.example.sellerappdemo.screens.LoginScreen
import com.example.sellerappdemo.screens.ShopFeedScreen
import com.example.sellerappdemo.ui.theme.ADAtSecondary
import com.example.sellerappdemo.ui.theme.SellerAppDemoTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 1. Initialize Supabase with Context
        com.example.sellerappdemo.supabase.Supabase.init(applicationContext)

        enableEdgeToEdge()
        setContent {
            SellerAppDemoTheme {
                AppScreen(Modifier.fillMaxSize())
            }
        }
    }
}

@Composable
fun AppScreen(modifier: Modifier, viewModel: AuthViewModel = viewModel()) {
    val state by viewModel.uiState.collectAsState()
    val navController = rememberNavController()

    if (state.isAuthenticated == null) {
        // Show a simple loading screen so Login doesn't "flash"
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = ADAtSecondary)
        }
    } else {
        NavHost(
            navController = navController,
            // 4. Set startDestination based on login status
            startDestination = if (state.isAuthenticated == true) "feed" else "login",
            enterTransition = { fadeIn(animationSpec = tween(700)) },
            exitTransition = { fadeOut(animationSpec = tween(700)) }
        ) {
            composable(route = "login") { LoginScreen(navController = navController) }
            composable(route = "feed") { ShopFeedScreen(navController = navController) }
            composable(route = "add_product") { AddProductScreen(navController = navController) }
            composable<ProductModel> { backStackEntry ->
                val product: ProductModel = backStackEntry.toRoute<ProductModel>()
                EditProductScreen(navController = navController, productModel = product)
            }
        }
    }
}

@Preview
@Composable
fun GreetingPreview() {
    SellerAppDemoTheme {
        AppScreen(Modifier.fillMaxSize())
    }
}