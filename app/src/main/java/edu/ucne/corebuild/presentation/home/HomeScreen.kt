package edu.ucne.corebuild.presentation.home

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import coil.compose.SubcomposeAsyncImage
import edu.ucne.corebuild.domain.model.Component
import edu.ucne.corebuild.presentation.components.AnimatedFilterChip
import edu.ucne.corebuild.presentation.components.AnimatedListItem
import edu.ucne.corebuild.presentation.components.bounceClick
import edu.ucne.corebuild.presentation.components.toPrice
import edu.ucne.corebuild.ui.theme.CoreBuildTheme
import kotlinx.coroutines.flow.collectLatest

@Composable
fun HomeScreen(
    viewModel: HomeViewModel = hiltViewModel(),
    onComponentClick: (Int) -> Unit,
    onCartClick: () -> Unit,
    onMenuClick: () -> Unit
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    DisposableEffect(Unit) {
        viewModel.onScreenEnter()
        onDispose { viewModel.onScreenExit() }
    }

    LaunchedEffect(Unit) {
        viewModel.navigationEvent.collectLatest { event ->
            when (event) {
                HomeNavigationEvent.NavigateToCart -> onCartClick()
            }
        }
    }

    HomeScreenContent(
        state = state,
        onEvent = viewModel::onEvent,
        onComponentClick = { id ->
            viewModel.onEvent(HomeEvent.OnComponentClick(id))
            onComponentClick(id)
        },
        onCartClick = onCartClick,
        onMenuClick = onMenuClick
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun HomeScreenContent(
    state: HomeUiState,
    onEvent: (HomeEvent) -> Unit,
    onComponentClick: (Int) -> Unit,
    onCartClick: () -> Unit,
    onMenuClick: () -> Unit
) {
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()

    if (state.showBuildDialog && state.featuredBuild != null) {
        FeaturedBuildDialog(
            build = state.featuredBuild,
            onDismiss = { onEvent(HomeEvent.OnToggleBuildDialog) },
            onAddToCart = { onEvent(HomeEvent.OnAddFeaturedToCart) }
        )
    }

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text("CoreBuild", fontWeight = FontWeight.Bold)
                },
                navigationIcon = {
                    IconButton(
                        onClick = onMenuClick,
                        modifier = Modifier.bounceClick()
                    ) {
                        Icon(Icons.Default.Menu, contentDescription = "Menú")
                    }
                },
                actions = {
                    IconButton(
                        onClick = onCartClick,
                        modifier = Modifier.bounceClick()
                    ) {
                        Icon(Icons.Default.ShoppingCart, contentDescription = "Carrito")
                    }
                },
                scrollBehavior = scrollBehavior
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            item {
                SearchBar(
                    query = state.searchQuery,
                    onQueryChange = { onEvent(HomeEvent.OnSearchQueryChange(it)) }
                )
            }

            stickyHeader {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.background)
                        .padding(bottom = 4.dp)
                ) {
                    CategoryFilter(
                        selectedCategory = state.selectedCategory,
                        onCategorySelected = { onEvent(HomeEvent.OnCategoryChange(it)) }
                    )
                }
            }

            if (state.selectedCategory == null && state.searchQuery.isBlank()) {
                state.featuredBuild?.let { build ->
                    item {
                        AnimatedListItem {
                            FeaturedBuildCard(
                                build = build,
                                onClick = { onEvent(HomeEvent.OnToggleBuildDialog) }
                            )
                        }
                    }
                }
            }

            if (state.isLoading) {
                item {
                    Box(
                        modifier = Modifier.fillMaxWidth().height(200.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
            } else {
                if (state.selectedCategory == null && state.smartRecommendations.isNotEmpty()) {
                    item {
                        AnimatedListItem {
                            SectionHeader(
                                title = "✨ Recomendado para ti",
                                subtitle = "Basado en tu actividad"
                            )
                        }
                    }
                    item {
                        ComponentHorizontalRow(
                            components = state.smartRecommendations,
                            onComponentClick = onComponentClick
                        )
                    }
                }

                if (state.selectedCategory == null && state.searchQuery.isBlank()) {
                    if (state.intelComponents.isNotEmpty()) {
                        item {
                            SectionHeader("Procesadores Intel", "Potencia para gaming")
                            ComponentHorizontalRow(state.intelComponents, onComponentClick)
                        }
                    }
                    if (state.amdCpuComponents.isNotEmpty()) {
                        item {
                            SectionHeader("Procesadores AMD Ryzen", "Eficiencia y núcleos")
                            ComponentHorizontalRow(state.amdCpuComponents, onComponentClick)
                        }
                    }
                    if (state.nvidiaComponents.isNotEmpty()) {
                        item {
                            SectionHeader("NVIDIA GeForce RTX", "Ray Tracing y DLSS")
                            ComponentHorizontalRow(state.nvidiaComponents, onComponentClick)
                        }
                    }
                    if (state.radeonComponents.isNotEmpty()) {
                        item {
                            SectionHeader("AMD Radeon RX", "Gráficos de alto nivel")
                            ComponentHorizontalRow(state.radeonComponents, onComponentClick)
                        }
                    }
                    if (state.recentlyViewed.isNotEmpty()) {
                        item {
                            SectionHeader("Visto recientemente", "Tu historial")
                            ComponentHorizontalRow(state.recentlyViewed, onComponentClick)
                        }
                    }
                }

                item {
                    SectionHeader(
                        title = if (state.selectedCategory != null) "Categoría: ${state.selectedCategory}" else "Más componentes",
                        subtitle = "${state.filteredComponents.size} disponibles"
                    )
                }

                if (state.filteredComponents.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier.fillMaxWidth().padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("No se encontraron productos", color = MaterialTheme.colorScheme.outline)
                        }
                    }
                } else {
                    val chunks = state.filteredComponents.chunked(2)
                    items(chunks) { rowItems ->
                        AnimatedListItem {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 6.dp),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                rowItems.forEach { component ->
                                    AmazonGridItem(
                                        component = component,
                                        onClick = onComponentClick,
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                                if (rowItems.size == 1) {
                                    Spacer(modifier = Modifier.weight(1f))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ComponentHorizontalRow(components: List<Component>, onComponentClick: (Int) -> Unit) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(components) { component ->
            AnimatedListItem {
                AmazonGridItem(component, onComponentClick)
            }
        }
    }
    Spacer(modifier = Modifier.height(24.dp))
}

@Composable
fun FeaturedBuildCard(
    build: PredefinedBuild,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(220.dp)
            .padding(16.dp)
            .bounceClick()
            .clickable { onClick() },
        shape = MaterialTheme.shapes.extraLarge,
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            AsyncImage(
                model = build.imageUrl,
                contentDescription = build.name,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = androidx.compose.ui.graphics.Brush.verticalGradient(
                            colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.8f))
                        )
                    )
            )
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(20.dp)
            ) {
                Surface(
                    color = MaterialTheme.colorScheme.primary,
                    shape = CircleShape
                ) {
                    Text(
                        text = "Build Destacada",
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = build.name,
                    style = MaterialTheme.typography.headlineSmall,
                    color = Color.White,
                    fontWeight = FontWeight.ExtraBold
                )
                Text(
                    text = "Desde ${build.totalPrice.toPrice()}",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White.copy(alpha = 0.9f),
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun FeaturedBuildDialog(
    build: PredefinedBuild,
    onDismiss: () -> Unit,
    onAddToCart: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(build.name, fontWeight = FontWeight.Bold) },
        text = {
            Column {
                Text(build.description, style = MaterialTheme.typography.bodyMedium)
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    "Componentes incluidos:",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleSmall
                )
                Spacer(modifier = Modifier.height(8.dp))
                build.components.forEach { component ->
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "• ${component.name}",
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.weight(1f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = component.price.toPrice(),
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Total Build:", fontWeight = FontWeight.Bold)
                    Text(
                        build.totalPrice.toPrice(),
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onAddToCart,
                modifier = Modifier.bounceClick()
            ) {
                Text("Añadir Todo al Carrito")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.bounceClick()
            ) {
                Text("Cerrar")
            }
        }
    )
}

@Composable
fun SectionHeader(title: String, subtitle: String) {
    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.secondary
        )
    }
}

@Composable
fun CategoryFilter(
    selectedCategory: String?,
    onCategorySelected: (String?) -> Unit
) {
    val categories = listOf("CPU", "GPU", "RAM", "Motherboard", "PSU")

    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            AnimatedFilterChip(
                selected = selectedCategory == null,
                onClick = { onCategorySelected(null) },
                label = { Text("Todos") },
                modifier = Modifier.bounceClick()
            )
        }
        items(categories) { category ->
            AnimatedFilterChip(
                selected = selectedCategory == category,
                onClick = { onCategorySelected(category) },
                label = { Text(category) },
                modifier = Modifier.bounceClick()
            )
        }
    }
}

@Composable
fun AmazonGridItem(
    component: Component,
    onClick: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .width(160.dp)
            .bounceClick()
            .clickable { onClick(component.id) },
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(
            1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
        )
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
                    .background(Color.White),
                contentAlignment = Alignment.Center
            ) {
                SubcomposeAsyncImage(
                    model = component.imageUrl ?: "https://via.placeholder.com/150",
                    contentDescription = component.name,
                    modifier = Modifier.fillMaxSize().padding(8.dp),
                    contentScale = ContentScale.Fit,
                    loading = { CircularProgressIndicator(modifier = Modifier.size(20.dp)) }
                )
            }
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = component.name,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    fontWeight = FontWeight.Medium,
                    minLines = 2
                )
                
                if (component is Component.RAM && component.configuration.isNotBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = MaterialTheme.shapes.small
                    ) {
                        Text(
                            text = component.configuration,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.Bottom) {
                    if (component is Component.RAM) {
                        Text(
                            text = "Desde ",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Text(
                        text = component.price.toPrice(),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Text(
                    text = "Envío GRATIS",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFF007185),
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        placeholder = { Text("¿Qué estás buscando?") },
        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
        singleLine = true,
        shape = MaterialTheme.shapes.medium,
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = MaterialTheme.colorScheme.surface,
            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
        )
    )
}

@Composable
fun ComponentItem(
    component: Component,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .bounceClick()
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp, pressedElevation = 4.dp),
        shape = MaterialTheme.shapes.extraLarge,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        )
    ) {
        Row(
            modifier = Modifier.padding(12.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            SubcomposeAsyncImage(
                model = component.imageUrl ?: "https://via.placeholder.com/150",
                contentDescription = component.name,
                modifier = Modifier.size(80.dp).clip(MaterialTheme.shapes.medium),
                contentScale = ContentScale.Crop,
                loading = {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                    }
                }
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = component.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2
                )
                Text(
                    text = component.category,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.secondary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = MaterialTheme.shapes.small
                ) {
                    Text(
                        text = component.price.toPrice(),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    CoreBuildTheme {
        HomeScreenContent(
            state = HomeUiState(),
            onEvent = {},
            onComponentClick = {},
            onCartClick = {},
            onMenuClick = {}
        )
    }
}
