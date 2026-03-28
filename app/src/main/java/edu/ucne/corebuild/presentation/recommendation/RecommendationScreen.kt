package edu.ucne.corebuild.presentation.recommendation

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import edu.ucne.corebuild.domain.model.Component

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecommendationScreen(
    onMenuClick: () -> Unit,
    onComponentClick: (Int) -> Unit,
    viewModel: RecommendationViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Recomendador IA") },
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
            // Inputs: Budget
            OutlinedTextField(
                value = uiState.budget,
                onValueChange = { viewModel.onEvent(RecommendationEvent.OnBudgetChange(it)) },
                label = { Text("Presupuesto Máximo (USD)") },
                placeholder = { Text("Ej: 1000") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                prefix = { Text("$ ") },
                shape = MaterialTheme.shapes.medium
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Prioritize Section
            Text(
                "Priorizar Rendimiento de:", 
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                PriorityChip(
                    label = "Procesador (CPU)",
                    selected = uiState.priority == "CPU",
                    onClick = { 
                        viewModel.onEvent(RecommendationEvent.OnPriorityChange(if (uiState.priority == "CPU") null else "CPU"))
                    },
                    modifier = Modifier.weight(1f)
                )
                PriorityChip(
                    label = "Gráfica (GPU)",
                    selected = uiState.priority == "GPU",
                    onClick = { 
                        viewModel.onEvent(RecommendationEvent.OnPriorityChange(if (uiState.priority == "GPU") null else "GPU"))
                    },
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = { viewModel.onEvent(RecommendationEvent.OnGenerateBuild) },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                enabled = uiState.budget.isNotEmpty() && !uiState.isLoading,
                shape = MaterialTheme.shapes.large
            ) {
                Icon(Icons.Default.AutoAwesome, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Generar Configuración")
            }

            Spacer(modifier = Modifier.height(24.dp))

            if (uiState.isLoading) {
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (uiState.error != null) {
                ErrorCard(uiState.error!!)
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 16.dp)
                ) {
                    if (uiState.recommendedComponents.isNotEmpty()) {
                        item {
                            Text(
                                "Build Sugerida",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        items(uiState.recommendedComponents) { component ->
                            RecommendedItem(component, onComponentClick)
                        }
                        item {
                            HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))
                            SummaryRow("Total Estimado:", "$${String.format("%.2f", uiState.totalPrice)}", true)
                            SummaryRow("Presupuesto Restante:", "$${String.format("%.2f", (uiState.budget.toDoubleOrNull() ?: 0.0) - uiState.totalPrice)}", false)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PriorityChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(label, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center) },
        modifier = modifier,
        shape = MaterialTheme.shapes.medium
    )
}

@Composable
fun ErrorCard(message: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.ErrorOutline, contentDescription = null, tint = MaterialTheme.colorScheme.error)
            Spacer(modifier = Modifier.width(12.dp))
            Text(text = message, color = MaterialTheme.colorScheme.onErrorContainer, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
fun SummaryRow(label: String, value: String, isPrimary: Boolean) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium)
        Text(
            value,
            style = if (isPrimary) MaterialTheme.typography.titleLarge else MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Bold,
            color = if (isPrimary) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun RecommendedItem(component: Component, onClick: (Int) -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick(component.id) },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(2.dp)),
        shape = MaterialTheme.shapes.medium
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val icon = when(component) {
                is Component.CPU -> Icons.Default.Memory
                is Component.GPU -> Icons.Default.DeveloperBoard
                is Component.Motherboard -> Icons.Default.SettingsInputComponent
                is Component.RAM -> Icons.Default.AlignVerticalBottom
                is Component.PSU -> Icons.Default.Power
            }
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(component.category, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.secondary)
                Text(component.name, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
            }
            Text("$${String.format("%.0f", component.price)}", fontWeight = FontWeight.Bold)
        }
    }
}
