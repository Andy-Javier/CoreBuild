package edu.ucne.corebuild

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.LightMode
import androidx.compose.material.icons.outlined.SettingsSuggest
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import dagger.hilt.android.AndroidEntryPoint
import edu.ucne.corebuild.presentation.bottleneck.BottleneckScreen
import edu.ucne.corebuild.presentation.cart.CartScreen
import edu.ucne.corebuild.presentation.comparator.ComparatorScreen
import edu.ucne.corebuild.presentation.detail.ProductDetailScreen
import edu.ucne.corebuild.presentation.favorites.FavoritesScreen
import edu.ucne.corebuild.presentation.home.HomeScreen
import edu.ucne.corebuild.presentation.navigation.Screen
import edu.ucne.corebuild.presentation.orders.OrderDetailScreen
import edu.ucne.corebuild.presentation.orders.OrdersScreen
import edu.ucne.corebuild.presentation.recommendation.RecommendationScreen
import edu.ucne.corebuild.ui.theme.CoreBuildTheme
import edu.ucne.corebuild.ui.theme.ThemeMode
import edu.ucne.corebuild.ui.theme.ThemeSettings
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            // Solicitar permiso de notificaciones para Android 13+
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                val launcher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.RequestPermission(),
                    onResult = { isGranted ->
                        // Permiso gestionado
                    }
                )
                LaunchedEffect(Unit) {
                    launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }

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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CoreBuildAppContent() {
    val navController = rememberNavController()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                drawerContainerColor = MaterialTheme.colorScheme.surface,
                drawerTonalElevation = 4.dp
            ) {
                Spacer(modifier = Modifier.height(24.dp))
                Column(
                    modifier = Modifier.padding(horizontal = 28.dp)
                ) {
                    Icon(
                        Icons.Default.Build,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        "CoreBuild",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        "Crea tu PC ideal",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                HorizontalDivider(modifier = Modifier.padding(horizontal = 28.dp))
                Spacer(modifier = Modifier.height(12.dp))
                
                DrawerItem(
                    icon = Icons.Default.Home,
                    label = "Inicio",
                    selected = currentRoute?.contains("Home") == true,
                    onClick = {
                        scope.launch { drawerState.close() }
                        navController.navigate(Screen.Home) {
                            popUpTo(Screen.Home) { inclusive = true }
                        }
                    }
                )
                DrawerItem(
                    icon = Icons.Default.Favorite,
                    label = "Mis Favoritos",
                    selected = currentRoute?.contains("Favorites") == true,
                    onClick = {
                        scope.launch { drawerState.close() }
                        navController.navigate(Screen.Favorites)
                    }
                )
                DrawerItem(
                    icon = Icons.Default.ReceiptLong,
                    label = "Mis Pedidos",
                    selected = currentRoute?.contains("Orders") == true,
                    onClick = {
                        scope.launch { drawerState.close() }
                        navController.navigate(Screen.Orders)
                    }
                )
                DrawerItem(
                    icon = Icons.Default.ShoppingCart,
                    label = "Mi Carrito",
                    selected = currentRoute?.contains("Cart") == true,
                    onClick = {
                        scope.launch { drawerState.close() }
                        navController.navigate(Screen.Cart)
                    }
                )
                DrawerItem(
                    icon = Icons.Default.Compare,
                    label = "Comparador",
                    selected = currentRoute?.contains("Comparator") == true,
                    onClick = {
                        scope.launch { drawerState.close() }
                        navController.navigate(Screen.Comparator)
                    }
                )
                DrawerItem(
                    icon = Icons.Default.Speed,
                    label = "Cuello Botella",
                    selected = currentRoute?.contains("Bottleneck") == true,
                    onClick = {
                        scope.launch { drawerState.close() }
                        navController.navigate(Screen.Bottleneck)
                    }
                )
                DrawerItem(
                    icon = Icons.Default.AutoAwesome,
                    label = "Recomendador IA",
                    selected = currentRoute?.contains("Recommendation") == true,
                    onClick = {
                        scope.launch { drawerState.close() }
                        navController.navigate(Screen.Recommendation)
                    }
                )
                
                Spacer(modifier = Modifier.weight(1f))
                
                HorizontalDivider(modifier = Modifier.padding(horizontal = 28.dp))
                
                Text(
                    "Apariencia",
                    modifier = Modifier.padding(start = 28.dp, top = 16.dp, bottom = 12.dp),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary
                )
                
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    ThemeOption(Icons.Outlined.LightMode, ThemeSettings.themeState == ThemeMode.LIGHT) {
                        ThemeSettings.themeState = ThemeMode.LIGHT
                    }
                    ThemeOption(Icons.Outlined.SettingsSuggest, ThemeSettings.themeState == ThemeMode.SYSTEM) {
                        ThemeSettings.themeState = ThemeMode.SYSTEM
                    }
                    ThemeOption(Icons.Outlined.DarkMode, ThemeSettings.themeState == ThemeMode.DARK) {
                        ThemeSettings.themeState = ThemeMode.DARK
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    ) {
        NavHost(
            navController = navController, 
            startDestination = Screen.Home,
            enterTransition = { fadeIn(animationSpec = tween(300)) + slideInHorizontally(animationSpec = tween(300)) { it } },
            exitTransition = { fadeOut(animationSpec = tween(300)) + slideOutHorizontally(animationSpec = tween(300)) { -it } },
            popEnterTransition = { fadeIn(animationSpec = tween(300)) + slideInHorizontally(animationSpec = tween(300)) { -it } },
            popExitTransition = { fadeOut(animationSpec = tween(300)) + slideOutHorizontally(animationSpec = tween(300)) { it } }
        ) {
            composable<Screen.Home> {
                HomeScreen(
                    onComponentClick = { id ->
                        navController.navigate(Screen.Detail(id))
                    },
                    onCartClick = {
                        navController.navigate(Screen.Cart)
                    },
                    onMenuClick = {
                        scope.launch { drawerState.open() }
                    }
                )
            }
            composable<Screen.Favorites> {
                FavoritesScreen(
                    onComponentClick = { id ->
                        navController.navigate(Screen.Detail(id))
                    },
                    onMenuClick = {
                        scope.launch { drawerState.open() }
                    }
                )
            }
            composable<Screen.Orders> {
                OrdersScreen(
                    onOrderClick = { id ->
                        navController.navigate(Screen.OrderDetail(id))
                    },
                    onMenuClick = {
                        scope.launch { drawerState.open() }
                    }
                )
            }
            composable<Screen.OrderDetail> { backStackEntry ->
                val detail: Screen.OrderDetail = backStackEntry.toRoute()
                OrderDetailScreen(
                    orderId = detail.orderId,
                    onBackClick = { navController.popBackStack() }
                )
            }
            composable<Screen.Detail> { backStackEntry ->
                val detail: Screen.Detail = backStackEntry.toRoute()
                ProductDetailScreen(
                    id = detail.id,
                    onBackClick = { navController.popBackStack() },
                    onCartClick = { navController.navigate(Screen.Cart) }
                )
            }
            composable<Screen.Cart> {
                CartScreen(
                    onBackClick = { navController.popBackStack() },
                    onMenuClick = { scope.launch { drawerState.open() } }
                )
            }
            composable<Screen.Comparator> {
                ComparatorScreen(
                    onMenuClick = {
                        scope.launch { drawerState.open() }
                    }
                )
            }
            composable<Screen.Bottleneck> {
                BottleneckScreen(
                    onMenuClick = {
                        scope.launch { drawerState.open() }
                    }
                )
            }
            composable<Screen.Recommendation> {
                RecommendationScreen(
                 onMenuClick = {
                        scope.launch { drawerState.open() }
                    },
                    onComponentClick = { id ->
                        navController.navigate(Screen.Detail(id))
                    }
                )
            }
        }
    }
}

@Composable
fun DrawerItem(
    icon: ImageVector,
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    NavigationDrawerItem(
        icon = { Icon(icon, contentDescription = null) },
        label = { Text(label) },
        selected = selected,
        onClick = onClick,
        modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
    )
}

@Composable
fun ThemeOption(
    icon: ImageVector,
    selected: Boolean,
    onClick: () -> Unit
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Icon(icon, contentDescription = null) },
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
        )
    )
}
