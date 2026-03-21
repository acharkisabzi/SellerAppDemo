package com.example.sellerappdemo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.sellerappdemo.screens.AddProductScreen
import com.example.sellerappdemo.screens.LoginScreen
import com.example.sellerappdemo.screens.ShopFeedScreen
import com.example.sellerappdemo.ui.theme.SellerAppDemoTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SellerAppDemoTheme {
                AppScreen(Modifier.fillMaxSize())
            }
        }
    }
}

@Composable
fun AppScreen(modifier: Modifier) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "login"
    ) {
        composable(route = "login") {
            LoginScreen(navController = navController)
        }
        composable(route = "feed") {
            ShopFeedScreen(navController = navController)
        }
        composable(route = "add_product") {
            AddProductScreen(navController = navController)
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