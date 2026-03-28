package edu.ucne.corebuild.presentation.recommendation

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Computer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import edu.ucne.corebuild.domain.model.Component

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecommendationScreen(
    onBackClick: () -> Unit,
    viewModel: RecommendationViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Recomendador IA") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Atrás")
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
            // Inputs: Budget and Base Component
            OutlinedTextField(
                value = uiState.budget,
                onValueChange = { viewModel.onEvent(RecommendationEvent.OnBudgetChange(it)) },
                label = { Text("Presupuesto (USD)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                prefix = { Text("$ ") }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Selector simple de componente base (CPU o GPU)
            Text("Componente Base (Opcional)", style = MaterialTheme.typography.labelLarge)
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = uiState.baseComponent is Component.CPU,
                    onClick = { 
                        val cpu = uiState.allComponents.filterIsInstance<Component.CPU>().firstOrNull()
                        viewModel.onEvent(RecommendationEvent.OnBaseComponentSelect(if (uiState.baseComponent is Component.CPU) null else cpu))
                    },
                    label = { Text("CPU") }
                )
                FilterChip(
                    selected = uiState.baseComponent is Component.GPU,
                    onClick = { 
                        val gpu = uiState.allComponents.filterIsInstance<Component.GPU>().firstOrNull()
                        viewModel.onEvent(RecommendationEvent.OnBaseComponentSelect(if (uiState.baseComponent is Component.GPU) null else gpu))
                    },
                    label = { Text("GPU") }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = { viewModel.onEvent(RecommendationEvent.OnGenerateBuild) },
                modifier = Modifier.fillMaxWidth(),
                enabled = uiState.budget.isNotEmpty() && !uiState.isLoading
            ) {
                Icon(Icons.Default.AutoAwesome, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Generar Build Recomendada")
            }

            Spacer(modifier = Modifier.height(24.dp))

            if (uiState.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (uiState.recommendedComponents.isNotEmpty()) {
                        item {
                            Text(
                                "Configuración Sugerida",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        items(uiState.recommendedComponents) { component ->
                            RecommendedItem(component)
                        }
                        item {
                            Divider(modifier = Modifier.padding(vertical = 12.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Total Estimado:", fontWeight = FontWeight.Bold)
                                Text(
                                    "$${String.format("%.2f", uiState.totalPrice)}",
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    } else if (uiState.error != null) {
                        item {
                            Text(uiState.error!!, color = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun RecommendedItem(component: Component) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.Computer, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(component.category, style = MaterialTheme.typography.labelSmall)
                Text(component.name, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
            }
            Text("$${component.price}", style = MaterialTheme.typography.bodyMedium)
        }
    }
}
