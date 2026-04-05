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
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.LightMode
import androidx.compose.material.icons.outlined.SettingsSuggest
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import coil.compose.AsyncImage
import coil.request.ImageRequest
import dagger.hilt.android.AndroidEntryPoint
import edu.ucne.corebuild.presentation.auth.AuthViewModel
import edu.ucne.corebuild.presentation.auth.LoginScreen
import edu.ucne.corebuild.presentation.auth.ProfileScreen
import edu.ucne.corebuild.presentation.auth.RegisterScreen
import edu.ucne.corebuild.presentation.bottleneck.BottleneckScreen
import edu.ucne.corebuild.presentation.cart.CartScreen
import edu.ucne.corebuild.presentation.comparator.ComparatorScreen
import edu.ucne.corebuild.presentation.detail.ProductDetailScreen
import edu.ucne.corebuild.presentation.favorites.FavoritesScreen
import edu.ucne.corebuild.presentation.home.HomeScreen
import edu.ucne.corebuild.presentation.navigation.Screen
import edu.ucne.corebuild.presentation.orders.OrderDetailScreen
import edu.ucne.corebuild.presentation.orders.OrdersScreen
import edu.ucne.corebuild.presentation.performance.PerformanceScreen
import edu.ucne.corebuild.presentation.recommendation.RecommendationScreen
import edu.ucne.corebuild.presentation.smartbuild.BuildSelectorScreen
import edu.ucne.corebuild.presentation.smartbuild.SmartBuildScreen
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
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                val launcher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.RequestPermission(),
                    onResult = { isGranted -> }
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
fun CoreBuildAppContent(
    authViewModel: AuthViewModel = hiltViewModel()
) {
    val navController = rememberNavController()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val authUiState by authViewModel.uiState.collectAsState()
    val context = LocalContext.current
    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                drawerContainerColor = MaterialTheme.colorScheme.surface,
                drawerTonalElevation = 4.dp,
                modifier = Modifier.width(280.dp)
            ) {
                Spacer(modifier = Modifier.height(24.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .border(2.dp, MaterialTheme.colorScheme.primary, CircleShape)
                            .clickable {
                                scope.launch { drawerState.close() }
                                if (authUiState.isLogged) {
                                    navController.navigate(Screen.Profile)
                                } else {
                                    navController.navigate(Screen.Login)
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        if (authUiState.user?.profilePicture != null) {
                            AsyncImage(
                                model = ImageRequest.Builder(context)
                                    .data(authUiState.user?.profilePicture)
                                    .crossfade(true)
                                    .build(),
                                contentDescription = "Perfil",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Icon(
                                Icons.Default.Person,
                                contentDescription = "Perfil",
                                modifier = Modifier.size(28.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = if (authUiState.isLogged) authUiState.user?.name ?: "Usuario" else "Invitado",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (authUiState.isLogged) "Ver perfil" else "Toca para iniciar sesión",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.clickable {
                                scope.launch { drawerState.close() }
                                if (authUiState.isLogged) navController.navigate(Screen.Profile)
                                else navController.navigate(Screen.Login)
                            }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(20.dp))
                HorizontalDivider(modifier = Modifier.padding(horizontal = 24.dp))
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
                    icon = Icons.Default.VideogameAsset,
                    label = "Simulador FPS",
                    selected = currentRoute?.contains("Performance") == true,
                    onClick = {
                        scope.launch { drawerState.close() }
                        navController.navigate(Screen.Performance)
                    }
                )
                DrawerItem(
                    icon = Icons.Default.AutoAwesome,
                    label = "Recomendador IA",
                    selected = currentRoute?.contains("BuildSelector") == true || currentRoute?.contains("Recommendation") == true || currentRoute?.contains("SmartBuild") == true,
                    onClick = {
                        scope.launch { drawerState.close() }
                        navController.navigate(Screen.BuildSelector)
                    }
                )
                Spacer(modifier = Modifier.weight(1f))
                HorizontalDivider(modifier = Modifier.padding(horizontal = 24.dp))
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
            exitTransition = { fadeOut(animationSpec = tween(300)) + slideOutHorizontally(animationSpec = tween(300)) { -it } }
        ) {
            composable<Screen.Home> {
                HomeScreen(
                    onComponentClick = { id -> navController.navigate(Screen.Detail(id)) },
                    onCartClick = { navController.navigate(Screen.Cart) },
                    onMenuClick = { scope.launch { drawerState.open() } }
                )
            }
            composable<Screen.Login> {
                LoginScreen(
                    onLoginSuccess = { navController.popBackStack() },
                    onRegisterClick = { navController.navigate(Screen.Register) },
                    onBackClick = { navController.popBackStack() }
                )
            }
            composable<Screen.Register> {
                RegisterScreen(
                    onRegisterSuccess = { navController.popBackStack() },
                    onLoginClick = { navController.navigate(Screen.Login) },
                    onBackClick = { navController.popBackStack() }
                )
            }
            composable<Screen.Profile> {
                ProfileScreen(
                    onBackClick = { navController.popBackStack() },
                    onLogoutSuccess = { navController.navigate(Screen.Home) { popUpTo(Screen.Home) { inclusive = true } } }
                )
            }
            composable<Screen.Cart> {
                CartScreen(
                    onBackClick = { navController.popBackStack() },
                    onMenuClick = { scope.launch { drawerState.open() } },
                    onNavigateToLogin = { navController.navigate(Screen.Login) },
                    onNavigateToThanks = { navController.navigate(Screen.Thanks) }
                )
            }
            composable<Screen.Thanks> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        Icons.Default.CheckCircle,
                        contentDescription = null,
                        modifier = Modifier.size(100.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Text(
                        text = "¡Gracias por tu compra!",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Estamos preparando tu hardware. Recibirás una notificación cuando sea entregado.",
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(48.dp))
                    Button(
                        onClick = { 
                            navController.navigate(Screen.Home) { 
                                popUpTo(Screen.Home) { inclusive = true } 
                            } 
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.large
                    ) {
                        Text("Ir al inicio")
                    }
                }
            }
            composable<Screen.Favorites> {
                FavoritesScreen(
                    onComponentClick = { id -> navController.navigate(Screen.Detail(id)) },
                    onMenuClick = { scope.launch { drawerState.open() } }
                )
            }
            composable<Screen.Orders> {
                OrdersScreen(
                    onOrderClick = { id -> navController.navigate(Screen.OrderDetail(id)) },
                    onMenuClick = { scope.launch { drawerState.open() } }
                )
            }
            composable<Screen.OrderDetail> { backStackEntry ->
                val detail: Screen.OrderDetail = backStackEntry.toRoute()
                OrderDetailScreen(orderId = detail.orderId, onBackClick = { navController.popBackStack() })
            }
            composable<Screen.Detail> { backStackEntry ->
                val detail: Screen.Detail = backStackEntry.toRoute()
                ProductDetailScreen(id = detail.id, onBackClick = { navController.popBackStack() }, onCartClick = { navController.navigate(Screen.Cart) })
            }
            composable<Screen.Comparator> {
                ComparatorScreen(onMenuClick = { scope.launch { drawerState.open() } })
            }
            composable<Screen.Bottleneck> {
                BottleneckScreen(onMenuClick = { scope.launch { drawerState.open() } })
            }
            composable<Screen.Performance> {
                PerformanceScreen(onMenuClick = { scope.launch { drawerState.open() } })
            }
            composable<Screen.BuildSelector> {
                BuildSelectorScreen(
                    onRecommendationClick = { navController.navigate(Screen.Recommendation) },
                    onSmartBuildClick = { navController.navigate(Screen.SmartBuild) }
                )
            }
            composable<Screen.Recommendation> {
                RecommendationScreen(
                    onMenuClick = { scope.launch { drawerState.open() } },
                    onComponentClick = { id -> navController.navigate(Screen.Detail(id)) }
                )
            }
            composable<Screen.SmartBuild> {
                SmartBuildScreen(
                    onBackClick = { navController.popBackStack() },
                    onComponentClick = { id -> navController.navigate(Screen.Detail(id)) },
                    onCartClick = { navController.navigate(Screen.Cart) }
                )
            }
        }
    }
}

@Composable
fun DrawerItem(icon: ImageVector, label: String, selected: Boolean, onClick: () -> Unit) {
    NavigationDrawerItem(
        icon = { Icon(icon, contentDescription = null) },
        label = { Text(label) },
        selected = selected,
        onClick = onClick,
        modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
    )
}

@Composable
fun ThemeOption(icon: ImageVector, selected: Boolean, onClick: () -> Unit) {
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
