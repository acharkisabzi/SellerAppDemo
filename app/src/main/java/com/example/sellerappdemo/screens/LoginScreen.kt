package com.example.sellerappdemo.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.sellerappdemo.R
import com.example.sellerappdemo.ViewModels.AuthViewModel


@Composable
fun LoginScreen(navController: NavController, viewModel: AuthViewModel = viewModel()) {

    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(state.isAuthenticated) {
        if (state.isAuthenticated == true) {
            navController.navigate("feed") {
                popUpTo("login") { inclusive = true }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(R.string.app_title),
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = stringResource(R.string.seller_app_subtitle),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(40.dp))

        // Extra fields for sign up
        if (state.isSignUp) {
            OutlinedTextField(
                value = state.shopNameInput,
                onValueChange = { viewModel.updateShopName(it) },
                label = { Text(stringResource(R.string.label_shop_name)) },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedTextField(
                value = authUiState.usernameInput,
                onValueChange = { authViewModel.updateUserName(it) },
                label = { Text(stringResource(R.string.create_username)) },
                modifier = Modifier.fillMaxWidth(),
                enabled = !authUiState.isLoading
            )
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedTextField(
                value = state.areaInput,
                onValueChange = { viewModel.updateArea(it) },
                label = { Text(stringResource(R.string.label_area)) },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(12.dp))
        }

        OutlinedTextField(
            value = state.emailInput,
            onValueChange = { viewModel.updateEmail(it) },
            label = { Text(stringResource(R.string.label_email)) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = state.passwordInput,
            onValueChange = { viewModel.updatePassword(it) },
            label = { Text(stringResource(R.string.label_password)) },
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))

        // Error message
        if (state.errorMessage.isNotEmpty()) {
            Text(
                text = state.errorMessage,
                color = MaterialTheme.colorScheme.error,
                fontSize = 13.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        Button(
            onClick = {
                viewModel.authorize()
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !state.isLoading
        ) {
            if (state.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = MaterialTheme.colorScheme.onPrimary
                )
            } else {
                Text(
                    if (state.isSignUp) stringResource(R.string.btn_create_account) else stringResource(
                        R.string.btn_sign_in
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        TextButton(onClick = { viewModel.updateSignUp(!state.isSignUp) }) {
            Text(
                if (state.isSignUp) stringResource(R.string.msg_already_have_account) else stringResource(
                    R.string.msg_new_shop
                )
            )
        }
    }
}