package edu.ucne.corebuild.presentation.favorites

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import edu.ucne.corebuild.presentation.components.AnimatedListItem
import edu.ucne.corebuild.presentation.components.bounceClick
import edu.ucne.corebuild.presentation.home.ComponentItem
import edu.ucne.corebuild.ui.theme.CoreBuildTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoritesScreen(
    viewModel: FavoritesViewModel = hiltViewModel(),
    onComponentClick: (Int) -> Unit,
    onMenuClick: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()

    FavoritesScreenContent(
        state = state,
        onComponentClick = onComponentClick,
        onMenuClick = onMenuClick
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoritesScreenContent(
    state: FavoritesUiState,
    onComponentClick: (Int) -> Unit,
    onMenuClick: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mis Favoritos") },
                navigationIcon = {
                    IconButton(
                        onClick = onMenuClick,
                        modifier = Modifier.bounceClick()
                    ) {
                        Icon(Icons.Default.Menu, contentDescription = "Menú")
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (state.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (state.favorites.isEmpty()) {
                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        Icons.Default.Favorite,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.outline
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "No tienes favoritos aún",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 16.dp)
                ) {
                    items(
                        items = state.favorites,
                        key = { it.id }
                    ) { component ->
                        AnimatedListItem {
                            ComponentItem(
                                component = component,
                                onClick = { onComponentClick(component.id) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun FavoritesScreenPreview() {
    CoreBuildTheme {
        FavoritesScreenContent(
            state = FavoritesUiState(),
            onComponentClick = {},
            onMenuClick = {}
        )
    }
}
