package edu.ucne.corebuild

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
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
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import dagger.hilt.android.AndroidEntryPoint
import edu.ucne.corebuild.presentation.bottleneck.BottleneckScreen
import edu.ucne.corebuild.presentation.cart.CartScreen
import edu.ucne.corebuild.presentation.comparator.ComparatorScreen
import edu.ucne.corebuild.presentation.detail.ProductDetailScreen
import edu.ucne.corebuild.presentation.home.HomeScreen
import edu.ucne.corebuild.presentation.navigation.Screen
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
                        "Build your dream PC",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                HorizontalDivider(modifier = Modifier.padding(horizontal = 28.dp))
                Spacer(modifier = Modifier.height(12.dp))
                
                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.Home, contentDescription = null) },
                    label = { Text("Inicio") },
                    selected = false,
                    onClick = {
                        scope.launch { drawerState.close() }
                        navController.navigate(Screen.Home) {
                            popUpTo(Screen.Home) { inclusive = true }
                        }
                    },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )
                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.Compare, contentDescription = null) },
                    label = { Text("Comparador") },
                    selected = false,
                    onClick = {
                        scope.launch { drawerState.close() }
                        navController.navigate(Screen.Comparator)
                    },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )
                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.Speed, contentDescription = null) },
                    label = { Text("Calculadora Cuello Botella") },
                    selected = false,
                    onClick = {
                        scope.launch { drawerState.close() }
                        navController.navigate(Screen.Bottleneck)
                    },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )
                
                Spacer(modifier = Modifier.weight(1f))
                
                HorizontalDivider(modifier = Modifier.padding(horizontal = 28.dp))
                
                Text(
                    "Apariencia",
                    modifier = Modifier.padding(start = 28.dp, top = 16.dp, bottom = 12.dp),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary
                )
                
                SingleChoiceSegmentedButtonRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 8.dp)
                ) {
                    val modes = listOf(
                        Triple(ThemeMode.LIGHT, Icons.Outlined.LightMode, "Claro"),
                        Triple(ThemeMode.SYSTEM, Icons.Outlined.SettingsSuggest, "Auto"),
                        Triple(ThemeMode.DARK, Icons.Outlined.DarkMode, "Oscuro")
                    )
                    
                    modes.forEachIndexed { index, (mode, icon, label) ->
                        SegmentedButton(
                            selected = ThemeSettings.themeState == mode,
                            onClick = { ThemeSettings.themeState = mode },
                            shape = SegmentedButtonDefaults.itemShape(index = index, count = modes.size),
                            icon = {
                                SegmentedButtonDefaults.Icon(active = ThemeSettings.themeState == mode) {
                                    Icon(icon, contentDescription = label)
                                }
                            },
                            label = { Text(label, style = MaterialTheme.typography.labelSmall) }
                        )
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
                    onBackClick = { navController.popBackStack() }
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
        }
    }
}
