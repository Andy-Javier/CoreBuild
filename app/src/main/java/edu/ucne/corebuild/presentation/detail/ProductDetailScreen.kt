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
import edu.ucne.corebuild.presentation.components.toPrice
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
        onVariantClick = { variantId ->
            viewModel.onEvent(ProductDetailEvent.LoadComponent(variantId))
        },
        onDismissSnackbar = {
            viewModel.onEvent(ProductDetailEvent.DismissSnackbar)
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ProductDetailContent(
    state: ProductDetailUiState,
    onBackClick: () -> Unit,
    onCartClick: () -> Unit,
    onAddToCart: (Component, Int) -> Unit,
    onBuyNow: (Component, Int) -> Unit,
    onToggleFavorite: () -> Unit,
    onVariantClick: (Int) -> Unit,
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
                ProductPriceSection(
                    price = component.price,
                    quantity = quantity,
                    availableToSelect = availableToSelect,
                    quantityLimit = state.quantityLimit,
                    onQuantityChange = { quantity = it },
                    onAddToCart = { onAddToCart(component, quantity) },
                    onBuyNow = { onBuyNow(component, quantity) }
                )
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
                        ProductImageSection(
                            imageUrl = component.imageUrl,
                            name = component.name
                        )

                        Column(modifier = Modifier.padding(16.dp)) {
                            ProductHeaderCard(
                                component = component,
                                variantsSize = state.variants.size
                            )

                            if (state.variants.size > 1 && component is Component.RAM) {
                                RamVariantSection(
                                    variants = state.variants,
                                    currentComponentId = component.id,
                                    onVariantClick = onVariantClick
                                )
                            }
                            
                            if (state.currentInCart > 0) {
                                CartStatusInfo(currentInCart = state.currentInCart)
                            }
                            
                            Spacer(modifier = Modifier.height(24.dp))
                            
                            Text(
                                "Especificaciones Técnicas",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 4.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            
                            ProductSpecsTable(component)
                            
                            Spacer(modifier = Modifier.height(32.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ProductImageSection(imageUrl: String?, name: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(300.dp)
            .padding(16.dp)
    ) {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(imageUrl ?: "https://via.placeholder.com/500")
                .crossfade(true)
                .build(),
            contentDescription = name,
            modifier = Modifier
                .fillMaxSize()
                .clip(MaterialTheme.shapes.extraLarge),
            contentScale = ContentScale.Fit
        )
    }
}

@Composable
private fun ProductPriceSection(
    price: Double,
    quantity: Int,
    availableToSelect: Int,
    quantityLimit: Int,
    onQuantityChange: (Int) -> Unit,
    onAddToCart: () -> Unit,
    onBuyNow: () -> Unit
) {
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
                            "Has alcanzado el límite máximo ($quantityLimit).",
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
                        (price * quantity).toPrice(),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (availableToSelect > 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                QuantitySelector(
                    quantity = quantity,
                    limit = availableToSelect,
                    enabled = availableToSelect > 0,
                    onQuantityChange = onQuantityChange
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onAddToCart,
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
                    onClick = onBuyNow,
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

@Composable
private fun ProductHeaderCard(component: Component, variantsSize: Int) {
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
            
            val displayName = if (component is Component.RAM && variantsSize > 1) {
                val configPatterns = listOf(
                    Regex("""\s\d+x\d+GB.*""", RegexOption.IGNORE_CASE),
                    Regex("""\s\d+GB.*""", RegexOption.IGNORE_CASE)
                )
                var base = component.name
                for (pattern in configPatterns) {
                    val match = pattern.find(base)
                    if (match != null) {
                        base = base.substring(0, match.range.first).trim()
                        break
                    }
                }
                base
            } else {
                component.name
            }

            Text(
                text = displayName,
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
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun RamVariantSection(
    variants: List<Component>,
    currentComponentId: Int,
    onVariantClick: (Int) -> Unit
) {
    Spacer(modifier = Modifier.height(16.dp))
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.extraLarge,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(2.dp)
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "Selecciona tu configuración",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                "${variants.size} opciones disponibles",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(12.dp))
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                variants
                    .sortedBy { (it as? Component.RAM)?.price ?: 0.0 }
                    .forEach { variant ->
                        val isSelected = variant.id == currentComponentId
                        val ramVariant = variant as Component.RAM
                        val label = ramVariant.configuration.ifBlank { ramVariant.capacity }
                        FilterChip(
                            selected = isSelected,
                            onClick = { onVariantClick(variant.id) },
                            label = {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(label, fontWeight = FontWeight.Bold)
                                    Text(
                                        ramVariant.price.toPrice(),
                                        style = MaterialTheme.typography.labelSmall
                                    )
                                }
                            },
                            shape = MaterialTheme.shapes.medium
                        )
                    }
            }
        }
    }
}

@Composable
private fun CartStatusInfo(currentInCart: Int) {
    Spacer(modifier = Modifier.height(16.dp))
    Surface(
        color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f),
        shape = MaterialTheme.shapes.medium,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = "Ya tienes $currentInCart unidades en el carrito.",
            modifier = Modifier.padding(12.dp),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSecondaryContainer,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun ProductSpecsTable(component: Component) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.extraLarge,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = CardDefaults.outlinedCardBorder()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            when (component) {
                is Component.CPU -> CpuSpecs(component)
                is Component.GPU -> GpuSpecs(component)
                is Component.Motherboard -> MotherboardSpecs(component)
                is Component.PSU -> PsuSpecs(component)
                is Component.RAM -> RamSpecs(component)
            }
        }
    }
}

@Composable
private fun CpuSpecs(cpu: Component.CPU) {
    SpecRow("Marca", cpu.brand)
    SpecRow("Socket", cpu.socket)
    SpecRow("Generación", cpu.generation)
    SpecRow("Núcleos", cpu.cores.toString())
    SpecRow("Hilos", cpu.threads.toString())
    SpecRow("Reloj Base", cpu.baseClock)
    SpecRow("Reloj Boost", cpu.boostClock)
    SpecRow("TDP", cpu.tdp)
}

@Composable
private fun GpuSpecs(gpu: Component.GPU) {
    SpecRow("Marca", gpu.brand)
    SpecRow("Chipset", gpu.chipset)
    SpecRow("VRAM", "${gpu.vram} ${gpu.vramType}")
    val cleanWatts = gpu.consumptionWatts.replace("W", "", ignoreCase = true).trim()
    SpecRow("Consumo", "${cleanWatts}W")
    gpu.recommendedPSU?.let { SpecRow("Fuente Recomendada", it) }
}

@Composable
private fun MotherboardSpecs(mobo: Component.Motherboard) {
    SpecRow("Marca", mobo.brand)
    SpecRow("Socket", mobo.socket)
    SpecRow("Chipset", mobo.chipset)
    SpecRow("Formato", mobo.format)
    SpecRow("Tipo de RAM", mobo.ramType)
}

@Composable
private fun RamSpecs(ram: Component.RAM) {
    SpecRow("Marca", ram.brand)
    SpecRow("Tipo", ram.type)
    SpecRow("Capacidad", ram.capacity)
    SpecRow("Configuración", ram.configuration)
    SpecRow("Velocidad", ram.speed)
    SpecRow("Latencia", ram.latency)
}

@Composable
private fun PsuSpecs(psu: Component.PSU) {
    SpecRow("Marca", psu.brand)
    SpecRow("Potencia", "${psu.wattage}W")
    SpecRow("Certificación", psu.certification)
    SpecRow("Modularidad", psu.modularity)
}

@Composable
fun SpecRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(text = value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
    }
    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
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
            onVariantClick = {},
            onDismissSnackbar = {}
        )
    }
}
