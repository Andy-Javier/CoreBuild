package edu.ucne.corebuild.presentation.comparator

import androidx.compose.animation.*
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CompareArrows
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import edu.ucne.corebuild.domain.model.Component
import edu.ucne.corebuild.presentation.components.PerformanceBar
import edu.ucne.corebuild.ui.theme.CoreBuildTheme

@Composable
fun ComparatorScreen(
    onMenuClick: () -> Unit,
    viewModel: ComparatorViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    ComparatorBody(
        uiState = uiState,
        onMenuClick = onMenuClick,
        onEvent = viewModel::onEvent
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ComparatorBody(
    uiState: ComparatorUiState,
    onMenuClick: () -> Unit,
    onEvent: (ComparatorEvent) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Comparador PRO") },
                navigationIcon = {
                    IconButton(onClick = onMenuClick) {
                        Icon(Icons.Default.Menu, contentDescription = "Menú")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                SegmentedButton(
                    selected = uiState.selectedType == "CPU",
                    onClick = { onEvent(ComparatorEvent.SelectType("CPU")) },
                    shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2)
                ) { Text("Procesadores") }
                SegmentedButton(
                    selected = uiState.selectedType == "GPU",
                    onClick = { onEvent(ComparatorEvent.SelectType("GPU")) },
                    shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2)
                ) { Text("Gráficas") }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Column(modifier = Modifier.weight(1f)) {
                    if (uiState.selectedComponent1 != null) {
                        AsyncImage(
                            model = uiState.selectedComponent1.imageUrl ?: "https://via.placeholder.com/100",
                            contentDescription = null,
                            modifier = Modifier
                                .size(80.dp)
                                .align(Alignment.CenterHorizontally)
                                .clip(MaterialTheme.shapes.medium),
                            contentScale = ContentScale.Fit
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                    ComponentSelector(
                        label = "Componente 1",
                        selected = uiState.selectedComponent1,
                        options = uiState.filteredComponents,
                        onSelect = { onEvent(ComparatorEvent.SelectComponent1(it)) }
                    )
                }
                
                Column(modifier = Modifier.weight(1f)) {
                    if (uiState.selectedComponent2 != null) {
                        AsyncImage(
                            model = uiState.selectedComponent2.imageUrl ?: "https://via.placeholder.com/100",
                            contentDescription = null,
                            modifier = Modifier
                                .size(80.dp)
                                .align(Alignment.CenterHorizontally)
                                .clip(MaterialTheme.shapes.medium),
                            contentScale = ContentScale.Fit
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                    ComponentSelector(
                        label = "Componente 2",
                        selected = uiState.selectedComponent2,
                        options = uiState.filteredComponents,
                        onSelect = { onEvent(ComparatorEvent.SelectComponent2(it)) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            if (uiState.selectedComponent1 != null && uiState.selectedComponent2 != null) {
                ComparisonMatrix(uiState.selectedComponent1!!, uiState.selectedComponent2!!)
            } else {
                Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.CompareArrows, contentDescription = null, modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.outlineVariant)
                        Text("Selecciona dos componentes para comparar", color = MaterialTheme.colorScheme.outline, textAlign = TextAlign.Center)
                    }
                }
            }
        }
    }
}

@Composable
fun ComparisonMatrix(c1: Component, c2: Component) {
    LazyColumn(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        item {
            WinnerHeader(c1, c2)
        }

        val metrics = when {
            c1 is Component.CPU && c2 is Component.CPU -> listOf(
                Metric("Núcleos", c1.cores.toDouble(), c2.cores.toDouble(), "${c1.cores}", "${c2.cores}"),
                Metric("Hilos", c1.threads.toDouble(), c2.threads.toDouble(), "${c1.threads}", "${c2.threads}"),
                Metric("Boost Clock", parseGhz(c1.boostClock), parseGhz(c2.boostClock), c1.boostClock, c2.boostClock),
                Metric("Base Clock", parseGhz(c1.baseClock), parseGhz(c2.baseClock), c1.baseClock, c2.baseClock),
                Metric("Caché L3", parseMb(c1.cache), parseMb(c2.cache), c1.cache ?: "N/A", c2.cache ?: "N/A"),
                Metric("TDP", parseWatts(c1.tdp), parseWatts(c2.tdp), c1.tdp ?: "N/A", c2.tdp ?: "N/A", inverse = true),
                Metric("Precio", c1.price, c2.price, "$${c1.price.toInt()}", "$${c2.price.toInt()}", inverse = true)
            )
            c1 is Component.GPU && c2 is Component.GPU -> listOf(
                Metric("VRAM", extractValue(c1.vram), extractValue(c2.vram), c1.vram, c2.vram),
                Metric("Consumo", parseWatts(c1.consumptionWatts), parseWatts(c2.consumptionWatts), c1.consumptionWatts, c2.consumptionWatts, inverse = true),
                Metric("Precio", c1.price, c2.price, "$${c1.price.toInt()}", "$${c2.price.toInt()}", inverse = true)
            )
            else -> emptyList()
        }

        items(metrics) { metric ->
            ComparisonBar(metric)
        }
    }
}

@Composable
fun WinnerHeader(c1: Component, c2: Component) {
    val score1 = when (c1) {
        is Component.CPU -> cpuPerformanceScore(c1)
        is Component.GPU -> gpuPerformanceScore(c1)
        else -> 0.0
    }
    val score2 = when (c2) {
        is Component.CPU -> cpuPerformanceScore(c2)
        is Component.GPU -> gpuPerformanceScore(c2)
        else -> 0.0
    }

    val winner = when {
        score1 > score2 -> c1
        score2 > score1 -> c2
        else -> null
    }

    val vfm1 = if (c1.price > 0) score1 / c1.price else 0.0
    val vfm2 = if (c2.price > 0) score2 / c2.price else 0.0
    val bestVfm = when {
        vfm1 > vfm2 * 1.05 -> c1.name
        vfm2 > vfm1 * 1.05 -> c2.name
        else -> null
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.EmojiEvents, contentDescription = null, tint = Color(0xFFFFD700))
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = if (winner != null) "Mejor rendimiento estimado: ${winner.name}" else "Rendimiento equiparable",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
            }
            if (bestVfm != null) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "Mejor valor/precio: $bestVfm",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun ComparisonBar(metric: Metric) {
    val maxVal = maxOf(metric.v1, metric.v2).coerceAtLeast(1.0)
    val norm1 = (metric.v1 / maxVal).toFloat()
    val norm2 = (metric.v2 / maxVal).toFloat()

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = metric.label,
            style = MaterialTheme.typography.labelLarge,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                PerformanceBar(value = norm1)
                Text(
                    text = metric.t1,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Start
                )
            }
            
            Text(
                " VS ",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.outline,
                modifier = Modifier.padding(horizontal = 8.dp)
            )
            
            Column(modifier = Modifier.weight(1f)) {
                PerformanceBar(value = norm2)
                Text(
                    text = metric.t2,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.End
                )
            }
        }
    }
}

@Composable
fun ComponentSelector(
    label: String,
    selected: Component?,
    options: List<Component>,
    onSelect: (Component) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    Box(modifier = modifier) {
        OutlinedCard(
            onClick = { expanded = true },
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                Text(
                    text = selected?.name ?: "Seleccionar",
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = if (selected != null) FontWeight.Bold else FontWeight.Normal
                )
            }
        }

        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }, modifier = Modifier.fillMaxWidth(0.45f)) {
            options.forEach { component ->
                DropdownMenuItem(
                    text = { Text(component.name, fontSize = 12.sp) },
                    onClick = {
                        onSelect(component)
                        expanded = false
                    }
                )
            }
        }
    }
}

private fun extractValue(text: String): Double =
    Regex("""[\d.]+""").find(text)?.value?.toDoubleOrNull() ?: 0.0

private fun parseGhz(text: String): Double =
    Regex("""[\d.]+""").find(text)?.value?.toDoubleOrNull() ?: 0.0

private fun parseMb(text: String?): Double =
    Regex("""\d+""").find(text ?: "")?.value?.toDoubleOrNull() ?: 0.0

private fun parseWatts(text: String?): Double =
    Regex("""\d+""").find(text ?: "")?.value?.toDoubleOrNull() ?: 0.0

private fun cpuPerformanceScore(cpu: Component.CPU): Double {
    val boostGhz = parseGhz(cpu.boostClock)
    return cpu.cores * boostGhz
}

private fun gpuPerformanceScore(gpu: Component.GPU): Double {
    return parseWatts(gpu.consumptionWatts)
}

data class Metric(val label: String, val v1: Double, val v2: Double, val t1: String, val t2: String, val inverse: Boolean = false)

@Preview(showBackground = true)
@Composable
fun ComparatorScreenPreview() {
    CoreBuildTheme {
        ComparatorBody(
            uiState = ComparatorUiState(),
            onMenuClick = {},
            onEvent = {}
        )
    }
}
