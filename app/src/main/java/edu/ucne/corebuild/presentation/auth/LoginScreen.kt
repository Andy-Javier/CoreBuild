package edu.ucne.corebuild.presentation.auth

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import edu.ucne.corebuild.ui.theme.CoreBuildTheme

@Composable
fun LoginScreen(
    viewModel: AuthViewModel = hiltViewModel(),
    onLoginSuccess: () -> Unit,
    onRegisterClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    LaunchedEffect(uiState.isLogged) {
        if (uiState.isLogged) {
            onLoginSuccess()
        }
    }
    LoginBody(
        uiState = uiState,
        onEvent = viewModel::onEvent,
        onRegisterClick = onRegisterClick
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginBody(
    uiState: AuthUiState,
    onEvent: (AuthEvent) -> Unit,
    onRegisterClick: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Iniciar Sesión") }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                "Bienvenido de nuevo",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(32.dp))
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Correo electrónico") },
                leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Spacer(modifier = Modifier.height(16.dp))
            PasswordField(
                value = password,
                onValueChange = { password = it },
                showPassword = showPassword,
                onToggleVisibility = { showPassword = !showPassword }
            )
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = { onEvent(AuthEvent.OnLogin(email, password)) },
                modifier = Modifier.fillMaxWidth(),
                enabled = email.isNotBlank() && password.isNotBlank() && !uiState.isLoading
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("Entrar")
                }
            }
            TextButton(onClick = onRegisterClick) {
                Text("¿No tienes cuenta? Regístrate")
            }
            uiState.error?.let { error ->
                Text(
                    text = error,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(top = 16.dp)
                )
            }
        }
    }
}

@Composable
fun PasswordField(
    value: String,
    onValueChange: (String) -> Unit,
    showPassword: Boolean,
    onToggleVisibility: () -> Unit
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text("Contraseña") },
        leadingIcon = {
            Icon(Icons.Default.Lock, null)
        },
        trailingIcon = {
            IconButton(onClick = onToggleVisibility) {
                Icon(
                    if (showPassword)
                        Icons.Default.Visibility
                    else Icons.Default.VisibilityOff,
                    contentDescription = null
                )
            }
        },
        visualTransformation = if (showPassword)
            VisualTransformation.None
        else PasswordVisualTransformation(),
        modifier = Modifier.fillMaxWidth(),
        singleLine = true
    )
}

@Preview(showBackground = true)
@Composable
fun LoginScreenPreview() {
    CoreBuildTheme {
        LoginBody(
            uiState = AuthUiState(),
            onEvent = {},
            onRegisterClick = {}
        )
    }
}
