package edu.ucne.corebuild.presentation.smartbuild

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import androidx.compose.runtime.LaunchedEffect
import coil.compose.SubcomposeAsyncImage
import kotlinx.coroutines.flow.collectLatest
import edu.ucne.corebuild.domain.model.Component
import edu.ucne.corebuild.domain.smartbuilder.SmartBuild
import edu.ucne.corebuild.presentation.components.AnimatedFilterChip
import edu.ucne.corebuild.presentation.components.AnimatedListItem
import edu.ucne.corebuild.presentation.components.bounceClick
import edu.ucne.corebuild.ui.theme.CoreBuildTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SmartBuildScreen(
    onBackClick: () -> Unit,
    onComponentClick: (Int) -> Unit,
    onCartClick: () -> Unit,
    viewModel: SmartBuildViewModel = hiltViewModel()
) {
    val formState by viewModel.formState.collectAsState()
    val uiState by viewModel.uiState.collectAsState()
    val showSaveDialog by viewModel.showSaveDialog.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.navigationEvent.collectLatest { event ->
            when (event) {
                is SmartBuildNavigationEvent.NavigateToCart -> onCartClick()
            }
        }
    }

    if (showSaveDialog) {
        val build = (uiState as? SmartBuildUiState.Success)?.build
        if (build != null) {
            SaveBuildDialog(
                build = build,
                onConfirm = viewModel::confirmSaveBuild,
                onDismiss = viewModel::dismissSaveDialog
            )
        }
    }

    SmartBuildScreenContent(
        formState = formState,
        uiState = uiState,
        onBackClick = onBackClick,
        onComponentClick = onComponentClick,
        viewModel = viewModel
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SmartBuildScreenContent(
    formState: SmartBuildFormState,
    uiState: SmartBuildUiState,
    onBackClick: () -> Unit,
    onComponentClick: (Int) -> Unit,
    viewModel: SmartBuildViewModel? = null
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Armador Inteligente") },
                navigationIcon = {
                    IconButton(
                        onClick = onBackClick,
                        modifier = Modifier.bounceClick()
                    ) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Atrás")
                    }
                }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            AnimatedContent(
                targetState = uiState,
                transitionSpec = {
                    fadeIn(tween(300)) togetherWith fadeOut(tween(200))
                },
                contentAlignment = Alignment.Center,
                label = "SmartBuildState"
            ) { state ->
                when (state) {
                    is SmartBuildUiState.Idle -> {
                        SmartBuildForm(
                            formState = formState,
                            viewModel = viewModel,
                            onToggleCpu = { viewModel?.toggleCpuMode() },
                            onToggleGpu = { viewModel?.toggleGpuMode() },
                            onSelectCpu = { viewModel?.selectCpu(it) },
                            onSelectGpu = { viewModel?.selectGpu(it) },
                            onBuildClick = { viewModel?.buildNow() }
                        )
                    }
                    is SmartBuildUiState.Loading -> {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator()
                        }
                    }
                    is SmartBuildUiState.Success -> {
                        SmartBuildResult(
                            build = state.build,
                            onReset = { viewModel?.resetToForm() },
                            onSaveBuild = { viewModel?.requestSaveBuild() },
                            onComponentClick = onComponentClick
                        )
                    }
                    is SmartBuildUiState.Error -> {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text(
                                state.message,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun SmartBuildForm(
    formState: SmartBuildFormState,
    viewModel: SmartBuildViewModel?,
    onToggleCpu: () -> Unit,
    onToggleGpu: () -> Unit,
    onSelectCpu: (Component.CPU) -> Unit,
    onSelectGpu: (Component.GPU) -> Unit,
    onBuildClick: () -> Unit
) {
    val cpuSearchQuery = viewModel?.cpuSearchQuery?.collectAsState()?.value ?: ""
    val gpuSearchQuery = viewModel?.gpuSearchQuery?.collectAsState()?.value ?: ""
    val cpuGroups = viewModel?.filteredCpuGroups?.collectAsState()?.value ?: emptyList()
    val gpuGroups = viewModel?.filteredGpuGroups?.collectAsState()?.value ?: emptyList()

    Column(modifier = Modifier.fillMaxSize()) {

        Text(
            "Selecciona tus componentes base",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(16.dp),
            fontWeight = FontWeight.Bold
        )

        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            AnimatedFilterChip(
                selected = formState.cpuModeEnabled,
                onClick = onToggleCpu,
                label = { Text("Procesador (CPU)") },
                leadingIcon = { if (formState.cpuModeEnabled) Icon(Icons.Default.Check, null) },
                modifier = Modifier.bounceClick()
            )
            AnimatedFilterChip(
                selected = formState.gpuModeEnabled,
                onClick = onToggleGpu,
                label = { Text("Tarjeta Gráfica (GPU)") },
                leadingIcon = { if (formState.gpuModeEnabled) Icon(Icons.Default.Check, null) },
                modifier = Modifier.bounceClick()
            )
        }

        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {

            // ── Sección CPU ──────────────────────────────────────────
            if (formState.cpuModeEnabled) {
                stickyHeader {
                    SectionSearchHeader(
                        title = "Selecciona un Procesador",
                        searchQuery = cpuSearchQuery,
                        onSearchChange = { viewModel?.onCpuSearch(it) },
                        placeholder = "Buscar CPU..."
                    )
                }
                if (cpuGroups.isEmpty()) {
                    item {
                        EmptySearchResult(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp))
                    }
                } else {
                    cpuGroups.forEach { group ->
                        stickyHeader(key = "cpu-header-${group.brand}") {
                            BrandHeader(brand = group.brand)
                        }
                        item(key = "cpu-row-${group.brand}") {
                            LazyRow(
                                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                items(group.items, key = { "cpu-${it.id}" }) { cpu ->
                                    AnimatedListItem {
                                        SmartBuildComponentCard(
                                            component = cpu,
                                            isSelected = formState.selectedCpu == cpu,
                                            onClick = { onSelectCpu(cpu) }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // ── Sección GPU ──────────────────────────────────────────
            if (formState.gpuModeEnabled) {
                stickyHeader {
                    SectionSearchHeader(
                        title = "Selecciona una Tarjeta Gráfica",
                        searchQuery = gpuSearchQuery,
                        onSearchChange = { viewModel?.onGpuSearch(it) },
                        placeholder = "Buscar GPU...",
                        topPadding = if (formState.cpuModeEnabled) 8.dp else 0.dp
                    )
                }
                if (gpuGroups.isEmpty()) {
                    item {
                        EmptySearchResult(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp))
                    }
                } else {
                    gpuGroups.forEach { group ->
                        stickyHeader(key = "gpu-header-${group.brand}") {
                            BrandHeader(brand = group.brand)
                        }
                        item(key = "gpu-row-${group.brand}") {
                            LazyRow(
                                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                items(group.items, key = { "gpu-${it.id}" }) { gpu ->
                                    AnimatedListItem {
                                        SmartBuildComponentCard(
                                            component = gpu,
                                            isSelected = formState.selectedGpu == gpu,
                                            onClick = { onSelectGpu(gpu) }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        Button(
            onClick = onBuildClick,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .height(56.dp)
                .bounceClick(),
            enabled = formState.isReadyToBuild,
            shape = MaterialTheme.shapes.large
        ) {
            Icon(Icons.Default.Build, null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Armar build")
        }
    }
}

@Composable
fun SmartBuildComponentCard(
    component: Component,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(130.dp)
            .bounceClick()
            .clickable { onClick() }
            .then(
                if (isSelected) Modifier.border(
                    width = 2.dp,
                    color = MaterialTheme.colorScheme.primary,
                    shape = MaterialTheme.shapes.medium
                ) else Modifier
            ),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected)
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
            else
                MaterialTheme.colorScheme.surface
        ),
        border = if (!isSelected) androidx.compose.foundation.BorderStroke(
            1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
        ) else null
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
                    .background(Color.White),
                contentAlignment = Alignment.Center
            ) {
                SubcomposeAsyncImage(
                    model = component.imageUrl ?: "https://via.placeholder.com/150",
                    contentDescription = component.name,
                    modifier = Modifier.fillMaxSize().padding(6.dp),
                    contentScale = ContentScale.Fit,
                    loading = { CircularProgressIndicator(modifier = Modifier.size(16.dp)) }
                )
                if (isSelected) {
                    Box(modifier = Modifier.align(Alignment.TopEnd).padding(4.dp)) {
                        Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
            Column(modifier = Modifier.padding(8.dp)) {
                Text(
                    text = component.name,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    fontWeight = FontWeight.Medium,
                    minLines = 2
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = "$",
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(bottom = 2.dp),
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = String.format("%.2f", component.price),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SectionSearchHeader(
    title: String,
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    placeholder: String,
    topPadding: androidx.compose.ui.unit.Dp = 0.dp
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background)
            .padding(start = 16.dp, end = 16.dp, top = topPadding + 12.dp, bottom = 4.dp)
    ) {
        Text(
            title,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text(placeholder, style = MaterialTheme.typography.bodySmall) },
            leadingIcon = {
                Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(18.dp))
            },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(
                        onClick = { onSearchChange("") },
                        modifier = Modifier.bounceClick()
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Limpiar", modifier = Modifier.size(16.dp))
                    }
                }
            },
            singleLine = true,
            shape = MaterialTheme.shapes.large,
            textStyle = MaterialTheme.typography.bodySmall
        )
    }
}

@Composable
private fun BrandHeader(brand: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
            .padding(horizontal = 16.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        HorizontalDivider(
            modifier = Modifier.weight(1f),
            thickness = 0.5.dp,
            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
        )
        Text(
            text = brand,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 10.dp)
        )
        HorizontalDivider(
            modifier = Modifier.weight(1f),
            thickness = 0.5.dp,
            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
        )
    }
}

@Composable
private fun EmptySearchResult(modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
        Text(
            "Sin resultados",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun SmartBuildResult(
    build: SmartBuild,
    onReset: () -> Unit,
    onSaveBuild: () -> Unit,
    onComponentClick: (Int) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text(
                    "Tu PC personalizada",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                if (build.warnings.isNotEmpty()) {
                    build.warnings.forEach { warning ->
                        AnimatedListItem {
                            Card(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.errorContainer
                                )
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.Warning, null, tint = MaterialTheme.colorScheme.error)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        warning,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onErrorContainer
                                    )
                                }
                            }
                        }
                    }
                }
            }

            item {
                Text(
                    "Componentes fijos",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            build.anchorCpu?.let { cpu -> 
                item { 
                    AnimatedListItem {
                        ResultItem(cpu, true, onComponentClick) 
                    }
                } 
            }
            build.anchorGpu?.let { gpu -> 
                item { 
                    AnimatedListItem {
                        ResultItem(gpu, true, onComponentClick) 
                    }
                } 
            }

            item {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "Componentes sugeridos",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
            items(build.suggested) { component ->
                AnimatedListItem {
                    ResultItem(component, false, onComponentClick)
                }
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
                val total = (build.anchorCpu?.price ?: 0.0) +
                        (build.anchorGpu?.price ?: 0.0) +
                        build.suggested.sumOf { it.price }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Total Estimado", style = MaterialTheme.typography.titleMedium)
                    Text(
                        "$${String.format("%.2f", total)}",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = onReset,
                modifier = Modifier.weight(1f).height(56.dp).bounceClick(),
                shape = MaterialTheme.shapes.large
            ) {
                Text("Cambiar anclas")
            }
            Button(
                onClick = onSaveBuild,
                modifier = Modifier.weight(1f).height(56.dp).bounceClick(),
                shape = MaterialTheme.shapes.large
            ) {
                Text("Guardar build")
            }
        }
    }
}

@Composable
fun ResultItem(
    component: Component,
    isAnchor: Boolean,
    onClick: (Int) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().bounceClick().clickable { onClick(component.id) },
        colors = CardDefaults.cardColors(
            containerColor = if (isAnchor)
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.1f)
            else
                MaterialTheme.colorScheme.surface
        ),
        border = if (isAnchor) androidx.compose.foundation.BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
        ) else null
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = component.imageUrl ?: "https://via.placeholder.com/150",
                contentDescription = null,
                modifier = Modifier.size(40.dp).clip(CircleShape),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    component.category,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.secondary
                )
                Text(
                    component.name,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Text(
                "$${component.price}",
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun SaveBuildDialog(
    build: SmartBuild,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    val allComponents = buildList {
        build.anchorCpu?.let { add(it as edu.ucne.corebuild.domain.model.Component) }
        build.anchorGpu?.let { add(it as edu.ucne.corebuild.domain.model.Component) }
        addAll(build.suggested)
    }
    val total = allComponents.sumOf { it.price }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("¿Añadir al carrito?", fontWeight = FontWeight.Bold)
        },
        text = {
            Column {
                Text(
                    "Se añadirán los siguientes componentes al carrito:",
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(12.dp))
                allComponents.forEach { component ->
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp),
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
                            text = "$${String.format("%.0f", component.price)}",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Total:", fontWeight = FontWeight.Bold)
                    Text(
                        "$${String.format("%.2f", total)}",
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                modifier = Modifier.bounceClick()
            ) {
                Icon(Icons.Default.ShoppingCart, null)
                Spacer(modifier = Modifier.width(6.dp))
                Text("Añadir al carrito")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.bounceClick()
            ) {
                Text("Cancelar")
            }
        }
    )
}

@Preview(showBackground = true)
@Composable
fun SmartBuildScreenPreview() {
    CoreBuildTheme {
        SmartBuildScreenContent(
            formState = SmartBuildFormState(),
            uiState = SmartBuildUiState.Idle,
            onBackClick = {},
            onComponentClick = {}
        )
    }
}
