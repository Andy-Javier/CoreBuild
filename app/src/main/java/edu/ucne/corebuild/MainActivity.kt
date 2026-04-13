package edu.ucne.corebuild

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import dagger.hilt.android.AndroidEntryPoint
import edu.ucne.corebuild.presentation.auth.AuthViewModel
import edu.ucne.corebuild.presentation.navigation.CoreBuildDrawerContent
import edu.ucne.corebuild.presentation.navigation.CoreBuildNavGraph
import edu.ucne.corebuild.presentation.navigation.Screen
import edu.ucne.corebuild.ui.theme.CoreBuildTheme
import edu.ucne.corebuild.ui.theme.ThemeSettings
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            RequestNotificationPermission()
            CoreBuildTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    CoreBuildAppContent()
                }
            }
        }
    }
}

@Composable
fun RequestNotificationPermission() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        val launcher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestPermission(),
            onResult = { }
        )
        LaunchedEffect(Unit) {
            launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }
}

@Composable
fun CoreBuildAppContent(
    authViewModel: AuthViewModel = hiltViewModel()
) {
    val navController = rememberNavController()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val authUiState by authViewModel.uiState.collectAsState()

    val isAuthScreen = currentRoute?.contains("Login") == true || currentRoute?.contains("Register") == true

    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = !isAuthScreen,
        drawerContent = {
            CoreBuildDrawerContent(
                isLogged = authUiState.isLogged,
                isAdmin = authUiState.isAdmin,
                user = authUiState.user,
                currentRoute = currentRoute,
                onProfileClick = {
                    scope.launch { drawerState.close() }
                    navController.navigate(if (authUiState.isLogged) Screen.Profile else Screen.Login)
                },
                onNavigate = { screen ->
                    scope.launch { drawerState.close() }
                    navController.navigate(screen) {
                        if (screen == Screen.Home) popUpTo(Screen.Home) { inclusive = true }
                    }
                },
                onThemeChange = { ThemeSettings.themeState = it }
            )
        }
    ) {
        CoreBuildNavGraph(
            navController = navController,
            startDestination = Screen.Home,
            authViewModel = authViewModel,
            onMenuClick = { scope.launch { drawerState.open() } }
        )
    }
}
