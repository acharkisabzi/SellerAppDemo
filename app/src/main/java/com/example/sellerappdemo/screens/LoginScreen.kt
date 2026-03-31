package com.example.sellerappdemo.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.sellerappdemo.R
import com.example.sellerappdemo.ViewModels.AuthViewModel



@OptIn(ExperimentalLayoutApi::class)
@Composable
fun LoginScreen(navController: NavController) {
    val authViewModel: AuthViewModel = viewModel()
    val authUiState by authViewModel.uiState.collectAsState()

    LaunchedEffect(authUiState.isAuthenticated) {
        if (authUiState.isAuthenticated == true) {
            navController.navigate("feed") { popUpTo("login") { inclusive = true } }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .imeNestedScroll(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(R.string.login_title),
            style = MaterialTheme.typography.displayLarge
        )
        Text(
            text = stringResource(R.string.login_subtitle),
            style = MaterialTheme.typography.headlineSmall

        )

        Spacer(modifier = Modifier.height(40.dp))

        // Extra fields for sign up
        if (authUiState.isSignUp) {
            OutlinedTextField(
                value = authUiState.nameInput,
                onValueChange = { authViewModel.updateName(it) },
                label = { Text(stringResource(R.string.label_shop_name)) },
                modifier = Modifier.fillMaxWidth(),
                enabled = !authUiState.isLoading
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
                value = authUiState.areaInput,
                onValueChange = { authViewModel.updateArea(it)},
                label = { Text(stringResource(R.string.label_area)) },
                modifier = Modifier.fillMaxWidth(),
                enabled = !authUiState.isLoading
            )
            Spacer(modifier = Modifier.height(12.dp))
        }

        OutlinedTextField(
            value = authUiState.emailInput,
            onValueChange = {authViewModel.updateEmail(it)},
            label = { Text(stringResource(R.string.label_email)) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            modifier = Modifier.fillMaxWidth(),
            enabled = !authUiState.isLoading
        )


        OutlinedTextField(
            value = authUiState.passwordInput,
            onValueChange = { authViewModel.updatePassword(it)},
            label = { Text(stringResource(R.string.label_password)) },
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            modifier = Modifier.fillMaxWidth(),
            enabled = !authUiState.isLoading
        )
        Spacer(modifier = Modifier.height(8.dp))

        // Error message
        if (authUiState.errorMessage.isNotEmpty()) {
            Text(
                text = authUiState.errorMessage,
                color = MaterialTheme.colorScheme.error,
                fontSize = 13.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        Button(
            onClick = {
                authViewModel.authorize()
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !authUiState.isLoading
        ) {
            if (authUiState.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = MaterialTheme.colorScheme.onPrimary
                )
            } else {
                Text(if (authUiState.isSignUp) stringResource(R.string.btn_create_account) else stringResource(R.string.btn_sign_in),
                    style = MaterialTheme.typography.labelLarge)
            }
        }
        Spacer(modifier = Modifier.height(12.dp))

        TextButton(
            onClick = {authViewModel.updateSignUp(!authUiState.isSignUp)},
            enabled = !authUiState.isLoading
        ) {
            Text(if (authUiState.isSignUp) stringResource(R.string.msg_already_have_account) else stringResource(R.string.msg_first_time))
        }
    }
}