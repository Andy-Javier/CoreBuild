package edu.ucne.corebuild.presentation.overview

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import edu.ucne.corebuild.domain.model.CartItem
import edu.ucne.corebuild.presentation.components.PerformanceBar
import edu.ucne.corebuild.presentation.components.toPrice
import edu.ucne.corebuild.ui.theme.CoreBuildTheme

@Composable
fun BuildOverviewScreen(
    onBackClick: () -> Unit,
    onGoToHome: () -> Unit,
    viewModel: BuildOverviewViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    BuildOverviewBody(
        uiState = uiState,
        onBackClick = onBackClick,
        onGoToHome = onGoToHome
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BuildOverviewBody(
    uiState: BuildOverviewUiState,
    onBackClick: () -> Unit,
    onGoToHome: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Resumen del Build") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Atrás")
                    }
                }
            )
        }
    ) { padding ->
        if (uiState.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (uiState.components.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.large,
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Default.Build, contentDescription = null, modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.outline)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            "Agrega componentes al carrito para ver el resumen",
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        Button(onClick = onGoToHome) {
                            Text("Ir al catálogo")
                        }
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    OverviewCard(title = "Build Score") {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            val scoreColor = when {
                                uiState.scoreValue >= 0.75f -> Color(0xFF4CAF50)
                                uiState.scoreValue >= 0.45f -> Color(0xFFFFB300)
                                else -> Color(0xFFF44336)
                            }
                            
                            Box(contentAlignment = Alignment.Center) {
                                CircularProgressIndicator(
                                    progress = { uiState.scoreValue },
                                    modifier = Modifier.size(120.dp),
                                    strokeWidth = 10.dp,
                                    color = scoreColor,
                                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                                )
                                Text(
                                    text = "${(uiState.scoreValue * 100).toInt()}",
                                    style = MaterialTheme.typography.headlineLarge,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(uiState.scoreLabel, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = scoreColor)
                        }
                    }
                }

                item {
                    OverviewCard(title = "Rendimiento estimado (GTA V @ 1080p)") {
                        Column {
                            Text("~${uiState.estimatedFps} FPS", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(8.dp))
                            PerformanceBar(value = uiState.fpsBarValue)
                        }
                    }
                }

                item {
                    OverviewCard(title = "Balance del Sistema") {
                        Column {
                            PerformanceBar(value = 1f - uiState.bottleneckValue)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(uiState.bottleneckLabel, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }

                item {
                    OverviewCard(title = "Consumo Energético") {
                        Column {
                            val psuText = if (uiState.psuWatts > 0) "${uiState.psuWatts}W" else "Sin PSU detectada"
                            Text("${uiState.estimatedWatts}W / $psuText", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(8.dp))
                            PerformanceBar(value = 1f - uiState.powerBarValue)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                if (uiState.psuWatts > 0) "Margen disponible" else "Se recomienda añadir una PSU",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                item {
                    OverviewCard(title = "Compatibilidad") {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = if (uiState.compatibilityOk) Icons.Default.CheckCircle else Icons.Default.Warning,
                                    contentDescription = null,
                                    tint = if (uiState.compatibilityOk) Color(0xFF4CAF50) else Color(0xFFF44336)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    if (uiState.compatibilityOk) "Componentes compatibles" else "Se detectaron advertencias",
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            if (uiState.warnings.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(12.dp))
                                uiState.warnings.forEach { warning ->
                                    Row(modifier = Modifier.padding(vertical = 4.dp)) {
                                        Icon(Icons.Default.Info, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.error)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(warning, style = MaterialTheme.typography.bodySmall)
                                    }
                                }
                            }
                        }
                    }
                }

                item {
                    OverviewCard(title = "Componentes del build") {
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            contentPadding = PaddingValues(vertical = 8.dp)
                        ) {
                            items(uiState.components) { item ->
                                ComponentSummaryItem(item)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun OverviewCard(title: String, content: @Composable () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(title, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(16.dp))
            content()
        }
    }
}

@Composable
fun ComponentSummaryItem(item: CartItem) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.width(200.dp)
    ) {
        AsyncImage(
            model = item.component.imageUrl ?: "https://via.placeholder.com/40",
            contentDescription = null,
            modifier = Modifier.size(40.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Column {
            Text(item.component.name, maxLines = 1, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
            Text(item.component.price.toPrice(), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun BuildOverviewScreenPreview() {
    CoreBuildTheme {
        BuildOverviewBody(
            uiState = BuildOverviewUiState(),
            onBackClick = {},
            onGoToHome = {}
        )
    }
}
