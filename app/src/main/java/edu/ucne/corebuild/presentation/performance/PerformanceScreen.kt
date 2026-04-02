package edu.ucne.corebuild.presentation.performance

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import edu.ucne.corebuild.domain.model.Component
import edu.ucne.corebuild.domain.performance.GamePreset

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PerformanceScreen(
    onMenuClick: () -> Unit,
    viewModel: PerformanceViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Simulador de Rendimiento") },
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
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // SECCIÓN 1 — Selectores de componentes
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ComponentSelector(
                    label = "CPU",
                    selected = uiState.selectedCpu,
                    options = uiState.cpus,
                    onSelect = { viewModel.onEvent(PerformanceEvent.SelectCpu(it as Component.CPU)) },
                    modifier = Modifier.weight(1f)
                )
                ComponentSelector(
                    label = "GPU",
                    selected = uiState.selectedGpu,
                    options = uiState.gpus,
                    onSelect = { viewModel.onEvent(PerformanceEvent.SelectGpu(it as Component.GPU)) },
                    modifier = Modifier.weight(1f)
                )
            }

            // SECCIÓN 2 — Selector de juego
            GameSelector(
                selectedGame = uiState.selectedGame,
                onGameSelected = { viewModel.onEvent(PerformanceEvent.SelectGame(it)) }
            )

            // SECCIÓN 3 — Selector de resolución
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    "Resolución",
                    style = MaterialTheme.typography.labelMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                    RESOLUTIONS.forEachIndexed { index, res ->
                        SegmentedButton(
                            selected = uiState.selectedResolution == res,
                            onClick = { viewModel.onEvent(PerformanceEvent.SelectResolution(res)) },
                            shape = SegmentedButtonDefaults.itemShape(index = index, count = RESOLUTIONS.size)
                        ) {
                            Text(res)
                        }
                    }
                }
            }

            // SECCIÓN 4 — Resultado
            if (uiState.fpsResult != null) {
                ResultCard(uiState.fpsResult!!)
            } else {
                // SECCIÓN 5 — Estado vacío
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 48.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.Speed,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.outlineVariant
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            "Selecciona una CPU y una GPU para ver el rendimiento estimado",
                            color = MaterialTheme.colorScheme.outline,
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
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

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.fillMaxWidth(0.45f)
        ) {
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

@Composable
fun GameSelector(
    selectedGame: GamePreset,
    onGameSelected: (GamePreset) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            "Juego para simular",
            style = MaterialTheme.typography.labelMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Box {
            OutlinedButton(
                onClick = { expanded = true },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(selectedGame.displayName)
                    Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                }
            }

            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier.fillMaxWidth(0.9f)
            ) {
                GamePreset.values().forEach { game ->
                    DropdownMenuItem(
                        text = { Text(game.displayName) },
                        onClick = {
                            onGameSelected(game)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun ResultCard(result: edu.ucne.corebuild.domain.performance.FpsResult) {
    val resultColor = when (result.label) {
        "Muy fluido"     -> Color(0xFF4CAF50)
        "Fluido"         -> Color(0xFF8BC34A)
        "Jugable"        -> Color(0xFFFFEB3B)
        "Limitado"       -> Color(0xFFFF9800)
        "No recomendado" -> Color(0xFFF44336)
        else             -> MaterialTheme.colorScheme.primary
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // a) Número grande de FPS
            Text(
                text = "${result.fps}",
                fontSize = 64.sp,
                fontWeight = FontWeight.Bold,
                color = resultColor
            )

            // b) Label
            Text(
                text = result.label,
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
                color = resultColor
            )

            // c) Barra de progreso
            LinearProgressIndicator(
                progress = { result.fraction },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(12.dp)
                    .clip(RoundedCornerShape(6.dp)),
                color = resultColor,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )

            // d) Chips
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally)
            ) {
                AssistChip(
                    onClick = {},
                    label = { Text("Limitado por: ${result.limitedBy}") },
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = if (result.limitedBy == "Equilibrado") 
                            Color(0xFF4CAF50).copy(alpha = 0.2f) 
                            else Color(0xFFF44336).copy(alpha = 0.2f)
                    )
                )
                AssistChip(
                    onClick = {},
                    label = { Text("${result.cpuContribution}% CPU / ${result.gpuContribution}% GPU") },
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    )
                )
            }
        }
    }
}
