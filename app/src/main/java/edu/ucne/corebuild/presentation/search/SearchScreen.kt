package edu.ucne.corebuild.presentation.search

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import edu.ucne.corebuild.domain.model.Component
import edu.ucne.corebuild.presentation.components.toPrice
import edu.ucne.corebuild.ui.theme.CoreBuildTheme

@Composable
fun SearchScreen(
    onBackClick: () -> Unit,
    onComponentClick: (Int) -> Unit,
    viewModel: SearchViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    SearchBody(
        uiState = uiState,
        onBackClick = onBackClick,
        onComponentClick = onComponentClick,
        onEvent = viewModel::onEvent
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchBody(
    uiState: SearchUiState,
    onBackClick: () -> Unit,
    onComponentClick: (Int) -> Unit,
    onEvent: (SearchEvent) -> Unit
) {
    var showFilters by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    title = {
                        TextField(
                            value = uiState.query,
                            onValueChange = { onEvent(SearchEvent.OnQueryChange(it)) },
                            placeholder = { Text("Buscar componentes...") },
                            modifier = Modifier.fillMaxWidth(),
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = androidx.compose.ui.graphics.Color.Transparent,
                                unfocusedContainerColor = androidx.compose.ui.graphics.Color.Transparent,
                                disabledContainerColor = androidx.compose.ui.graphics.Color.Transparent,
                                focusedIndicatorColor = androidx.compose.ui.graphics.Color.Transparent,
                                unfocusedIndicatorColor = androidx.compose.ui.graphics.Color.Transparent,
                            ),
                            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                            trailingIcon = {
                                if (uiState.query.isNotEmpty()) {
                                    IconButton(onClick = { onEvent(SearchEvent.OnQueryChange("")) }) {
                                        Icon(Icons.Default.Clear, contentDescription = "Limpiar")
                                    }
                                }
                            },
                            singleLine = true
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onBackClick) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Atrás")
                        }
                    },
                    actions = {
                        IconButton(onClick = { showFilters = !showFilters }) {
                            Icon(
                                Icons.Default.FilterList, 
                                contentDescription = "Filtros",
                                tint = if (showFilters) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                )
                
                if (showFilters) {
                    FilterSection(uiState, onEvent)
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (uiState.filteredComponents.isEmpty()) {
                Text(
                    text = "No se encontraron resultados",
                    modifier = Modifier.align(Alignment.Center)
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(uiState.filteredComponents) { component ->
                        ComponentSearchItem(
                            component = component,
                            onClick = { onComponentClick(component.id) }
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterSection(uiState: SearchUiState, onEvent: (SearchEvent) -> Unit) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Categorías", style = MaterialTheme.typography.labelLarge)
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(uiState.availableCategories) { category ->
                    FilterChip(
                        selected = uiState.selectedCategories.contains(category),
                        onClick = { onEvent(SearchEvent.OnCategorySelect(category)) },
                        label = { Text(category) }
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text("Marcas", style = MaterialTheme.typography.labelLarge)
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(uiState.availableBrands) { brand ->
                    FilterChip(
                        selected = uiState.selectedBrands.contains(brand),
                        onClick = { onEvent(SearchEvent.OnBrandSelect(brand)) },
                        label = { Text(brand) }
                    )
                }
            }
            
            TextButton(
                onClick = { onEvent(SearchEvent.OnClearFilters) },
                modifier = Modifier.align(Alignment.End)
            ) {
                Text("Limpiar filtros")
            }
        }
    }
}

@Composable
fun ComponentSearchItem(component: Component, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = component.category,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = component.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = component.description,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 1
                )
            }
            Text(
                text = component.price.toPrice(),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SearchScreenPreview() {
    CoreBuildTheme {
        SearchBody(
            uiState = SearchUiState(),
            onBackClick = {},
            onComponentClick = {},
            onEvent = {}
        )
    }
}
