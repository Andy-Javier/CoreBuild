package edu.ucne.corebuild.presentation.detail

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import edu.ucne.corebuild.domain.model.Component

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
        onAddToCart = { component ->
            viewModel.onEvent(ProductDetailEvent.AddToCart(component))
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductDetailContent(
    state: ProductDetailUiState,
    onBackClick: () -> Unit,
    onCartClick: () -> Unit,
    onAddToCart: (Component) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Detalles") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Atrás")
                    }
                },
                actions = {
                    IconButton(onClick = onCartClick) {
                        Icon(Icons.Default.ShoppingCart, contentDescription = "Carrito")
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
            } else {
                state.component?.let { component ->
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = component.name, style = MaterialTheme.typography.headlineLarge)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(text = "Categoría: ${component.category}", style = MaterialTheme.typography.titleMedium)
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(text = component.description, style = MaterialTheme.typography.bodyLarge)
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            SpecificComponentDetails(component)
                            
                            Spacer(modifier = Modifier.height(24.dp))
                            Text(
                                text = "Precio: $${component.price}",
                                style = MaterialTheme.typography.headlineSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        Button(
                            onClick = { onAddToCart(component) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = MaterialTheme.shapes.medium
                        ) {
                            Icon(Icons.Default.ShoppingCart, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Agregar al Carrito")
                        }
                    }
                } ?: run {
                    if (!state.isLoading) {
                        Text(
                            text = "Componente no encontrado",
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SpecificComponentDetails(component: Component) {
    when (component) {
        is Component.CPU -> {
            DetailText("Marca: ${component.brand}")
            DetailText("Socket: ${component.socket}")
            DetailText("Generación: ${component.generation}")
            DetailText("Núcleos: ${component.cores}")
            DetailText("Hilos: ${component.threads}")
            DetailText("Reloj Base: ${component.baseClock}")
            DetailText("Reloj Boost: ${component.boostClock}")
            DetailText("TDP: ${component.tdp}")
        }
        is Component.GPU -> {
            DetailText("Marca: ${component.brand}")
            DetailText("Chipset: ${component.chipset}")
            DetailText("VRAM: ${component.vram} ${component.vramType}")
            DetailText("Potencia Recomendada: ${component.recommendedWattage}")
        }
        is Component.Motherboard -> {
            DetailText("Marca: ${component.brand}")
            DetailText("Socket: ${component.socket}")
            DetailText("Chipset: ${component.chipset}")
            DetailText("Formato: ${component.format}")
            DetailText("Tipo de RAM: ${component.ramType}")
        }
        is Component.PSU -> {
            DetailText("Marca: ${component.brand}")
            DetailText("Potencia: ${component.wattage}W")
            DetailText("Certificación: ${component.certification}")
            DetailText("Modularidad: ${component.modularity}")
        }
        is Component.RAM -> {
            DetailText("Marca: ${component.brand}")
            DetailText("Tipo: ${component.type}")
            DetailText("Capacidad: ${component.capacity}")
            DetailText("Velocidad: ${component.speed}")
            DetailText("Latencia: ${component.latency}")
        }
    }
}

@Composable
fun DetailText(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodyMedium,
        modifier = Modifier.padding(vertical = 2.dp)
    )
}
