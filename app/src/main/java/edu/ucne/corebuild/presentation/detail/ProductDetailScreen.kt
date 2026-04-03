package edu.ucne.corebuild.presentation.detail

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import edu.ucne.corebuild.domain.model.Component
import edu.ucne.corebuild.ui.theme.CoreBuildTheme
import kotlinx.coroutines.launch

@Composable
fun ProductDetailScreen(
    id: Int,
    viewModel: ProductDetailViewModel = hiltViewModel(),
    onBackClick: () -> Unit,
    onCartClick: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(id) {
        viewModel.onEvent(ProductDetailEvent.LoadComponent(id))
    }

    ProductDetailContent(
        state = state,
        onBackClick = onBackClick,
        onCartClick = onCartClick,
        onAddToCart = { component, quantity ->
            viewModel.onEvent(ProductDetailEvent.AddToCart(component, quantity))
        },
        onBuyNow = { component, quantity ->
            viewModel.onEvent(ProductDetailEvent.AddToCart(component, quantity))
            onCartClick()
        },
        onToggleFavorite = {
            viewModel.onEvent(ProductDetailEvent.OnToggleFavorite)
        },
        onDismissSnackbar = {
            viewModel.onEvent(ProductDetailEvent.DismissSnackbar)
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductDetailContent(
    state: ProductDetailUiState,
    onBackClick: () -> Unit,
    onCartClick: () -> Unit,
    onAddToCart: (Component, Int) -> Unit,
    onBuyNow: (Component, Int) -> Unit,
    onToggleFavorite: () -> Unit,
    onDismissSnackbar: () -> Unit
) {
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    
    val availableToSelect = (state.quantityLimit - state.currentInCart).coerceAtLeast(0)
    var quantity by remember { mutableIntStateOf(1) }

    LaunchedEffect(availableToSelect) {
        if (availableToSelect > 0 && quantity > availableToSelect) {
            quantity = availableToSelect
        } else if (availableToSelect == 0) {
            quantity = 1
        }
    }

    LaunchedEffect(state.snackbarMessage) {
        state.snackbarMessage?.let {
            scope.launch {
                snackbarHostState.showSnackbar(it)
                onDismissSnackbar()
            }
        }
    }

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Detalles del Producto") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Atrás")
                    }
                },
                actions = {
                    IconButton(onClick = onToggleFavorite) {
                        Icon(
                            imageVector = if (state.isFavorite) Icons.Default.Favorite else Icons.Outlined.FavoriteBorder,
                            contentDescription = "Favorito",
                            tint = if (state.isFavorite) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                        )
                    }
                    IconButton(onClick = onCartClick) {
                        Icon(Icons.Default.ShoppingCart, contentDescription = "Carrito")
                    }
                },
                scrollBehavior = scrollBehavior
            )
        },
        bottomBar = {
            state.component?.let { component ->
                Surface(
                    tonalElevation = 8.dp,
                    shadowElevation = 8.dp
                ) {
                    Column(
                        modifier = Modifier
                            .navigationBarsPadding()
                            .padding(16.dp)
                            .fillMaxWidth()
                    ) {
                        if (availableToSelect == 0) {
                            Surface(
                                color = MaterialTheme.colorScheme.errorContainer,
                                shape = MaterialTheme.shapes.medium,
                                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Default.Info, 
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onErrorContainer,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        "Has alcanzado el límite máximo (${state.quantityLimit}).",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.onErrorContainer
                                    )
                                }
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    "Precio Total",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    "$${String.format("%.2f", component.price * quantity)}",
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = if (availableToSelect > 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            
                            QuantitySelector(
                                quantity = quantity,
                                limit = availableToSelect,
                                enabled = availableToSelect > 0,
                                onQuantityChange = { quantity = it }
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            OutlinedButton(
                                onClick = { onAddToCart(component, quantity) },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(56.dp),
                                shape = MaterialTheme.shapes.extraLarge,
                                enabled = availableToSelect > 0
                            ) {
                                Icon(Icons.Default.AddShoppingCart, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Agregar")
                            }
                            
                            Button(
                                onClick = { onBuyNow(component, quantity) },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(56.dp),
                                shape = MaterialTheme.shapes.extraLarge,
                                elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp),
                                enabled = availableToSelect > 0
                            ) {
                                Text("Comprar Ahora")
                            }
                        }
                    }
                }
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (state.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else {
                state.component?.let { component ->
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                    ) {
                        Box(
                            modifier = Modifier
                                    .fillMaxWidth()
                                    .height(300.dp)
                                    .padding(16.dp)
                        ) {
                            AsyncImage(
                                model = ImageRequest.Builder(LocalContext.current)
                                    .data(component.imageUrl ?: "https://via.placeholder.com/500")
                                    .crossfade(true)
                                    .build(),
                                contentDescription = component.name,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(MaterialTheme.shapes.extraLarge),
                                contentScale = ContentScale.Fit
                            )
                        }

                        Column(modifier = Modifier.padding(16.dp)) {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(2.dp)
                                ),
                                shape = MaterialTheme.shapes.extraLarge
                            ) {
                                Column(modifier = Modifier.padding(24.dp)) {
                                    Surface(
                                        color = MaterialTheme.colorScheme.secondaryContainer,
                                        shape = MaterialTheme.shapes.small
                                    ) {
                                        Text(
                                            text = component.category.uppercase(),
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Text(
                                        text = component.name,
                                        style = MaterialTheme.typography.headlineMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Text(
                                        text = component.description,
                                        style = MaterialTheme.typography.bodyLarge,
                                        lineHeight = MaterialTheme.typography.bodyLarge.lineHeight * 1.2
                                    )
                                }
                            }
                            
                            if (state.currentInCart > 0) {
                                Spacer(modifier = Modifier.height(16.dp))
                                Surface(
                                    color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f),
                                    shape = MaterialTheme.shapes.medium,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = "Ya tienes ${state.currentInCart} unidades en el carrito.",
                                        modifier = Modifier.padding(12.dp),
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(24.dp))
                            
                            Text(
                                "Especificaciones Técnicas",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 4.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            
                            SpecificComponentDetails(component)
                            
                            Spacer(modifier = Modifier.height(32.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun QuantitySelector(
    quantity: Int,
    limit: Int,
    enabled: Boolean,
    onQuantityChange: (Int) -> Unit
) {
    Surface(
        color = if (enabled) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f),
        shape = MaterialTheme.shapes.medium
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(4.dp)
        ) {
            IconButton(
                onClick = { if (quantity > 1) onQuantityChange(quantity - 1) },
                modifier = Modifier.size(32.dp),
                enabled = enabled && quantity > 1
            ) {
                Icon(Icons.Default.Remove, contentDescription = "Disminuir", modifier = Modifier.size(18.dp))
            }
            
            Text(
                text = quantity.toString(),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(horizontal = 12.dp),
                fontWeight = FontWeight.Bold,
                color = if (enabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
            )
            
            IconButton(
                onClick = { if (quantity < limit) onQuantityChange(quantity + 1) },
                modifier = Modifier.size(32.dp),
                enabled = enabled && quantity < limit
            ) {
                Icon(Icons.Default.Add, contentDescription = "Aumentar", modifier = Modifier.size(18.dp))
            }
        }
    }
}

@Composable
fun SpecificComponentDetails(component: Component) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.extraLarge,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = CardDefaults.outlinedCardBorder()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            when (component) {
                is Component.CPU -> {
                    DetailRow("Marca", component.brand)
                    DetailRow("Socket", component.socket)
                    DetailRow("Generación", component.generation)
                    DetailRow("Núcleos", component.cores.toString())
                    DetailRow("Hilos", component.threads.toString())
                    DetailRow("Reloj Base", component.baseClock)
                    DetailRow("Reloj Boost", component.boostClock)
                    DetailRow("TDP", component.tdp)
                }
                is Component.GPU -> {
                    DetailRow("Marca", component.brand)
                    DetailRow("Chipset", component.chipset)
                    DetailRow("VRAM", "${component.vram} ${component.vramType}")
                    DetailRow("Consumo", "${component.consumptionWatts}W")
                    component.recommendedPSU?.let {
                        DetailRow("Fuente Recomendada", it)
                    }
                }
                is Component.Motherboard -> {
                    DetailRow("Marca", component.brand)
                    DetailRow("Socket", component.socket)
                    DetailRow("Chipset", component.chipset)
                    DetailRow("Formato", component.format)
                    DetailRow("Tipo de RAM", component.ramType)
                }
                is Component.PSU -> {
                    DetailRow("Marca", component.brand)
                    DetailRow("Potencia", "${component.wattage}W")
                    DetailRow("Certificación", component.certification)
                    DetailRow("Modularidad", component.modularity)
                }
                is Component.RAM -> {
                    DetailRow("Marca", component.brand)
                    DetailRow("Tipo", component.type)
                    DetailRow("Capacidad", component.capacity)
                    DetailRow("Velocidad", component.speed)
                    DetailRow("Latencia", component.latency)
                }
            }
        }
    }
}

@Composable
fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(text = value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
    }
    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
}

@Preview(showBackground = true)
@Composable
fun ProductDetailScreenPreview() {
    CoreBuildTheme {
        ProductDetailContent(
            state = ProductDetailUiState(),
            onBackClick = {},
            onCartClick = {},
            onAddToCart = { _, _ -> },
            onBuyNow = { _, _ -> },
            onToggleFavorite = {},
            onDismissSnackbar = {}
        )
    }
}
