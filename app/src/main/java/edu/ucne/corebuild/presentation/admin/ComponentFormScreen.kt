package edu.ucne.corebuild.presentation.admin

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import edu.ucne.corebuild.domain.model.Component

@Composable
fun ComponentFormScreen(
    viewModel: AdminViewModel = hiltViewModel(),
    onBack: () -> Unit
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(state.navigateBack) {
        if (state.navigateBack) {
            onBack()
            viewModel.onEvent(AdminEvent.OnResetForm)
        }
    }

    ComponentFormBody(
        formState = state.formState,
        isSaving = state.isSaving,
        isUploadingImage = state.isUploadingImage,
        errorMessage = state.errorMessage,
        successMessage = state.successMessage,
        onEvent = viewModel::onEvent,
        onBack = onBack
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ComponentFormBody(
    formState: ComponentFormState,
    isSaving: Boolean,
    isUploadingImage: Boolean,
    errorMessage: String?,
    successMessage: String?,
    onEvent: (AdminEvent) -> Unit,
    onBack: () -> Unit
) {
    val imageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let { onEvent(AdminEvent.OnImageSelected(it.toString())) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Nuevo Componente") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Atrás")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {
            // ITEM 1 — Selector de tipo
            item {
                var expanded by remember { mutableStateOf(false) }
                val types = listOf("CPU", "GPU", "Motherboard", "RAM", "PSU")
                
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded }
                ) {
                    OutlinedTextField(
                        value = formState.tipo,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Tipo de componente") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable).fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        types.forEach { type ->
                            DropdownMenuItem(
                                text = { Text(type) },
                                onClick = {
                                    onEvent(AdminEvent.OnFormFieldChange("tipo", type))
                                    expanded = false
                                }
                            )
                        }
                    }
                }
            }

            // ITEM 2 — Selector de imagen
            item {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(150.dp)
                            .clip(MaterialTheme.shapes.medium)
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .clickable { imageLauncher.launch("image/*") },
                        contentAlignment = Alignment.Center
                    ) {
                        if (formState.imageUrl.isNotEmpty()) {
                            AsyncImage(
                                model = formState.imageUrl,
                                contentDescription = "Imagen seleccionada",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Icon(Icons.Default.AddAPhoto, contentDescription = "Seleccionar imagen", modifier = Modifier.size(48.dp))
                        }
                    }

                    if (formState.imageUrl.isNotEmpty() && !formState.imageUrl.startsWith("http")) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "⚠️ Imagen local. Súbela para que todos puedan verla.",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.error,
                            textAlign = TextAlign.Center
                        )
                        Button(
                            onClick = { onEvent(AdminEvent.OnUploadImage) },
                            enabled = !isUploadingImage,
                            modifier = Modifier.padding(top = 8.dp)
                        ) {
                            if (isUploadingImage) {
                                CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                            } else {
                                Text("Subir a Cloudinary")
                            }
                        }
                    }
                    
                    if (successMessage?.contains("Imagen") == true) {
                        Text(
                            "✅ Imagen subida y lista",
                            color = Color(0xFF4CAF50),
                            style = MaterialTheme.typography.labelMedium,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }

            // ITEM 3 — Campos comunes
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    FormField(formState.nombre, "nombre", "Nombre del producto", onEvent)
                    FormField(formState.marca, "marca", "Marca", onEvent, supportingText = "Obligatorio para aparecer en el inicio")
                    FormField(formState.precioUsd, "precioUsd", "Precio (USD)", onEvent, keyboardType = KeyboardType.Decimal)
                    FormField(formState.descripcion, "descripcion", "Descripción", onEvent)
                }
            }

            // ITEM 4 — Campos dinámicos
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    when (formState.tipo) {
                        "CPU" -> {
                            FormField(formState.socket, "socket", "Socket ej: AM5, LGA1700", onEvent)
                            FormField(formState.generacion, "generacion", "Generación ej: Zen 4", onEvent)
                            FormField(formState.nucleos, "nucleos", "Núcleos", onEvent, KeyboardType.Number)
                            FormField(formState.hilos, "hilos", "Hilos", onEvent, KeyboardType.Number)
                            FormField(formState.frecuenciaBase, "frecuenciaBase", "Frecuencia base ej: 3.8 GHz", onEvent)
                            FormField(formState.frecuenciaTurbo, "frecuenciaTurbo", "Frecuencia turbo ej: 5.1 GHz", onEvent)
                            FormField(formState.cacheL3, "cacheL3", "Caché L3 ej: 32MB", onEvent, isOptional = true)
                            FormField(formState.tdpWatts, "tdpWatts", "TDP (Watts)", onEvent, KeyboardType.Number)
                            FormField(formState.graficosIntegrados, "graficosIntegrados", "Gráficos integrados", onEvent, isOptional = true)
                            FormField(formState.soporteRam, "soporteRam", "Soporte RAM", onEvent, isOptional = true)
                        }
                        "GPU" -> {
                            FormField(formState.chipset, "chipset", "Chipset ej: AD102", onEvent)
                            FormField(formState.vram, "vram", "VRAM ej: 24GB", onEvent)
                            FormField(formState.tipoVram, "tipoVram", "Tipo VRAM ej: GDDR6X", onEvent)
                            FormField(formState.busMemoria, "busMemoria", "Bus de memoria", onEvent, isOptional = true)
                            FormField(formState.frecuenciaBase, "frecuenciaBase", "Frecuencia base", onEvent, isOptional = true)
                            FormField(formState.frecuenciaBoost, "frecuenciaBoost", "Frecuencia boost ej: 2.5 GHz", onEvent)
                            FormField(formState.consumoWatts, "consumoWatts", "Consumo (Watts)", onEvent, KeyboardType.Number)
                            FormField(formState.fuenteRecomendada, "fuenteRecomendada", "Fuente recomendada ej: 850W", onEvent)
                            FormField(formState.conectoresEnergia, "conectoresEnergia", "Conectores de energía", onEvent, isOptional = true)
                            FormField(formState.versionPcie, "versionPcie", "Versión PCIe", onEvent, isOptional = true)
                        }
                        "Motherboard" -> {
                            FormField(formState.socket, "socket", "Socket ej: AM5, LGA1700", onEvent)
                            FormField(formState.chipsetMobo, "chipsetMobo", "Chipset ej: B650, Z790", onEvent)
                            FormField(formState.formato, "formato", "Formato ej: ATX, Micro-ATX", onEvent)
                            FormField(formState.compatibilidadCpu, "compatibilidadCpu", "Compatibilidad CPU", onEvent, isOptional = true)
                            FormField(formState.tipoRam, "tipoRam", "Tipo de RAM ej: DDR5", onEvent)
                            FormField(formState.velocidadRamMax, "velocidadRamMax", "Velocidad RAM máx", onEvent, isOptional = true)
                            FormField(formState.slotsRam, "slotsRam", "Slots de RAM ej: 4", onEvent, KeyboardType.Number)
                            FormField(formState.almacenamiento, "almacenamiento", "Almacenamiento M.2/SATA", onEvent, isOptional = true)
                            FormField(formState.puertos, "puertos", "Puertos", onEvent, isOptional = true)
                            FormField(formState.conectividad, "conectividad", "Conectividad WiFi/BT", onEvent, isOptional = true)
                        }
                        "RAM" -> {
                            FormField(formState.tipoRam2, "tipoRam2", "Tipo ej: DDR4, DDR5", onEvent)
                            FormField(formState.capacidadTotal, "capacidadTotal", "Capacidad ej: 32GB (2x16GB)", onEvent)
                            FormField(formState.configuracion, "configuracion", "Configuración ej: Dual Channel", onEvent, isOptional = true)
                            FormField(formState.velocidad, "velocidad", "Velocidad ej: 3200 MHz", onEvent)
                            FormField(formState.latencia, "latencia", "Latencia ej: CL16", onEvent)
                            FormField(formState.voltaje, "voltaje", "Voltaje", onEvent, isOptional = true)
                            FormField(formState.perfil, "perfil", "Perfil ej: XMP, EXPO", onEvent, isOptional = true)
                        }
                        "PSU" -> {
                            FormField(formState.potenciaWatts, "potenciaWatts", "Potencia (Watts)", onEvent, KeyboardType.Number)
                            FormField(formState.certificacion, "certificacion", "Certificación ej: 80 Plus Gold", onEvent)
                            FormField(formState.tipoModular, "tipoModular", "Modularidad ej: Totalmente modular", onEvent)
                            FormField(formState.eficiencia, "eficiencia", "Eficiencia", onEvent, isOptional = true)
                            FormField(formState.ventilador, "ventilador", "Ventilador ej: 135mm", onEvent, isOptional = true)
                            FormField(formState.protecciones, "protecciones", "Protecciones", onEvent, isOptional = true)
                            FormField(formState.conectores, "conectores", "Conectores", onEvent, isOptional = true)
                        }
                    }
                }
            }

            // ITEM 5 — Mensaje de error
            if (errorMessage != null) {
                item {
                    Text(
                        text = errorMessage,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                }
            }

            // ITEM 6 — Botones
            item {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = { 
                            onBack()
                            onEvent(AdminEvent.OnResetForm)
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Cancelar")
                    }
                    Button(
                        onClick = { 
                            // Using a real CPU instance as dummy to satisfy the event parameter
                            // The ViewModel will use buildComponentFromForm() instead.
                            val dummy = Component.CPU(0, "", "", 0.0, "", "", "", 0, 0, "", "", "")
                            onEvent(AdminEvent.OnCreateComponent(dummy)) 
                        },
                        modifier = Modifier.weight(1f),
                        enabled = !isSaving && !isUploadingImage && formState.nombre.isNotBlank() && formState.marca.isNotBlank() && formState.precioUsd.isNotBlank()
                    ) {
                        if (isSaving) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp, color = Color.White)
                        } else {
                            Text("Guardar")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FormField(
    value: String,
    field: String,
    label: String,
    onEvent: (AdminEvent) -> Unit,
    keyboardType: KeyboardType = KeyboardType.Text,
    isOptional: Boolean = false,
    supportingText: String? = null
) {
    OutlinedTextField(
        value = value,
        onValueChange = {
            onEvent(AdminEvent.OnFormFieldChange(field, it))
        },
        label = { Text(if (isOptional) "$label (opcional)" else label) },
        modifier = Modifier.fillMaxWidth(),
        keyboardOptions = KeyboardOptions(
            keyboardType = keyboardType
        ),
        supportingText = supportingText?.let { { Text(it) } },
        singleLine = true
    )
}
