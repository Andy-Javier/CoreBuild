package edu.ucne.corebuild.presentation.admin

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import edu.ucne.corebuild.core.network.NetworkManager
import edu.ucne.corebuild.data.remote.image.ImageUploader
import edu.ucne.corebuild.domain.auth.AuthManager
import edu.ucne.corebuild.domain.logs.AdminLog
import edu.ucne.corebuild.domain.logs.AdminLogRepository
import edu.ucne.corebuild.domain.model.Component
import edu.ucne.corebuild.domain.repository.ComponentRepository
import edu.ucne.corebuild.domain.repository.UserRepository
import edu.ucne.corebuild.util.Resource
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AdminViewModel @Inject constructor(
    private val componentRepository: ComponentRepository,
    private val networkManager: NetworkManager,
    private val authManager: AuthManager,
    private val userRepository: UserRepository,
    private val adminLogRepository: AdminLogRepository,
    private val imageUploader: ImageUploader,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(AdminUiState())
    val uiState: StateFlow<AdminUiState> = _uiState.asStateFlow()

    private var currentUserEmail: String = ""

    init {
        loadComponents()
        checkConnectivity()
        observeCurrentUser()
    }

    private fun observeCurrentUser() {
        viewModelScope.launch {
            userRepository.getLoggedUser().collect { user ->
                currentUserEmail = user?.email ?: "admin@corebuild.com"
            }
        }
    }

    private fun loadComponents() {
        viewModelScope.launch {
            componentRepository.getComponents().collect { resource ->
                when (resource) {
                    is Resource.Success -> {
                        _uiState.update { it.copy(components = resource.data ?: emptyList(), isLoading = false) }
                    }
                    is Resource.Error -> {
                        _uiState.update { it.copy(isLoading = false, errorMessage = resource.message) }
                    }
                    is Resource.Loading -> {
                        _uiState.update { it.copy(isLoading = true) }
                    }
                }
            }
        }
    }

    private fun checkConnectivity() {
        _uiState.update {
            it.copy(isOnline = networkManager.isOnline())
        }
    }

    private fun String.withSuffix(suffix: String): String =
        if (this.isBlank() || this.contains(suffix)) this else "$this$suffix"

    private fun String.withSuffixSpaced(suffix: String): String =
        if (this.isBlank() || this.contains(suffix)) this else "$this $suffix"

    fun onEvent(event: AdminEvent) {
        when (event) {
            AdminEvent.OnLoadComponents -> loadComponents()
            is AdminEvent.OnSelectType ->
                _uiState.update { it.copy(selectedType = event.type) }
            AdminEvent.OnCreateComponent -> {
                val component = buildComponentFromForm()
                if (component == null) {
                    _uiState.update { it.copy(errorMessage = "Completa los campos requeridos") }
                } else {
                    createComponent(component)
                }
            }
            is AdminEvent.OnUpdateComponent -> {
                updateComponent(event.component)
            }
            is AdminEvent.OnDeleteComponent ->
                deleteComponent(event.id, event.type)
            is AdminEvent.OnSelectComponent -> {
                val c = event.component
                val form = when (c) {
                    is Component.CPU -> ComponentFormState(
                        tipo = "CPU",
                        nombre = c.name,
                        marca = c.brand,
                        precioUsd = c.price.toString(),
                        descripcion = c.description,
                        imageUrl = c.imageUrl ?: "",
                        socket = c.socket,
                        generacion = c.generation,
                        nucleos = c.cores.toString(),
                        hilos = c.threads.toString(),
                        frecuenciaBase = c.baseClock.replace(" GHz", ""),
                        frecuenciaTurbo = c.boostClock.replace(" GHz", ""),
                        cacheL3 = (c.cache ?: "").replace("MB", ""),
                        tdpWatts = c.tdp.replace("W", ""),
                        graficosIntegrados = c.integratedGraphics ?: "",
                        soporteRam = c.ramSupport ?: ""
                    )
                    is Component.GPU -> ComponentFormState(
                        tipo = "GPU",
                        nombre = c.name,
                        marca = c.brand,
                        precioUsd = c.price.toString(),
                        descripcion = c.description,
                        imageUrl = c.imageUrl ?: "",
                        chipset = c.chipset,
                        vram = c.vram.replace("GB", ""),
                        tipoVram = c.vramType,
                        busMemoria = (c.memoryBus ?: "").replace("-bit", ""),
                        frecuenciaBase = (c.baseClock ?: "").replace(" GHz", ""),
                        frecuenciaBoost = (c.boostClock ?: "").replace(" GHz", ""),
                        consumoWatts = c.consumptionWatts.replace("W", ""),
                        fuenteRecomendada = c.recommendedPSU ?: "",
                        versionPcie = c.pcieInterface ?: ""
                    )
                    is Component.Motherboard -> ComponentFormState(
                        tipo = "Motherboard",
                        nombre = c.name,
                        marca = c.brand,
                        precioUsd = c.price.toString(),
                        descripcion = c.description,
                        imageUrl = c.imageUrl ?: "",
                        socket = c.socket,
                        chipsetMobo = c.chipset,
                        formato = c.format,
                        tipoRam = c.ramType,
                        velocidadRamMax = (c.maxRamSpeed ?: "").replace(" MHz", ""),
                        slotsRam = c.ramSlots?.toString() ?: ""
                    )
                    is Component.RAM -> ComponentFormState(
                        tipo = "RAM",
                        nombre = c.name,
                        marca = c.brand,
                        precioUsd = c.price.toString(),
                        descripcion = c.description,
                        imageUrl = c.imageUrl ?: "",
                        tipoRam2 = c.type,
                        capacidadTotal = c.capacity,
                        velocidad = c.speed.replace(" MHz", ""),
                        latencia = c.latency,
                        voltaje = (c.voltage ?: "").replace("V", "")
                    )
                    is Component.PSU -> ComponentFormState(
                        tipo = "PSU",
                        nombre = c.name,
                        marca = c.brand,
                        precioUsd = c.price.toString(),
                        descripcion = c.description,
                        imageUrl = c.imageUrl ?: "",
                        potenciaWatts = c.wattage.toString(),
                        certificacion = c.certification,
                        tipoModular = c.modularity,
                        ventilador = c.fanSize ?: "",
                        protecciones = c.protection ?: ""
                    )
                }
                _uiState.update {
                    it.copy(
                        selectedComponent = c,
                        showEditDialog = true,
                        selectedImageUri = c.imageUrl,
                        formState = form,
                        navigateBack = false
                    )
                }
            }
            AdminEvent.OnShowCreateDialog -> 
                _uiState.update { it.copy(showCreateDialog = true, selectedImageUri = null) }
            AdminEvent.OnDismissDialog ->
                _uiState.update {
                    it.copy(
                        showCreateDialog = false,
                        showEditDialog = false,
                        selectedComponent = null,
                        selectedImageUri = null,
                        navigateBack = false
                    )
                }
            AdminEvent.DismissMessage ->
                _uiState.update {
                    it.copy(
                        successMessage = null,
                        errorMessage = null
                    )
                }
            is AdminEvent.OnImageSelected -> {
                _uiState.update { 
                    it.copy(
                        selectedImageUri = event.uri,
                        formState = it.formState.copy(imageUrl = event.uri)
                    ) 
                }
            }
            AdminEvent.OnUploadImage -> uploadImage()
            is AdminEvent.OnFormFieldChange -> {
                val current = _uiState.value.formState
                val updated = when (event.field) {
                    "tipo" -> current.copy(tipo = event.value)
                    "nombre" -> current.copy(nombre = event.value)
                    "marca" -> current.copy(marca = event.value)
                    "precioUsd" -> current.copy(precioUsd = event.value)
                    "descripcion" -> current.copy(descripcion = event.value)
                    "imageUrl" -> current.copy(imageUrl = event.value)
                    "socket" -> current.copy(socket = event.value)
                    "generacion" -> current.copy(generacion = event.value)
                    "nucleos" -> current.copy(nucleos = event.value)
                    "hilos" -> current.copy(hilos = event.value)
                    "frecuenciaBase" -> current.copy(frecuenciaBase = event.value)
                    "frecuenciaTurbo" -> current.copy(frecuenciaTurbo = event.value)
                    "cacheL3" -> current.copy(cacheL3 = event.value)
                    "tdpWatts" -> current.copy(tdpWatts = event.value)
                    "graficosIntegrados" -> current.copy(graficosIntegrados = event.value)
                    "soporteRam" -> current.copy(soporteRam = event.value)
                    "chipset" -> current.copy(chipset = event.value)
                    "vram" -> current.copy(vram = event.value)
                    "tipoVram" -> current.copy(tipoVram = event.value)
                    "busMemoria" -> current.copy(busMemoria = event.value)
                    "frecuenciaBoost" -> current.copy(frecuenciaBoost = event.value)
                    "consumoWatts" -> current.copy(consumoWatts = event.value)
                    "fuenteRecomendada" -> current.copy(fuenteRecomendada = event.value)
                    "conectoresEnergia" -> current.copy(conectoresEnergia = event.value)
                    "versionPcie" -> current.copy(versionPcie = event.value)
                    "chipsetMobo" -> current.copy(chipsetMobo = event.value)
                    "formato" -> current.copy(formato = event.value)
                    "compatibilidadCpu" -> current.copy(compatibilidadCpu = event.value)
                    "tipoRam" -> current.copy(tipoRam = event.value)
                    "velocidadRamMax" -> current.copy(velocidadRamMax = event.value)
                    "slotsRam" -> current.copy(slotsRam = event.value)
                    "almacenamiento" -> current.copy(almacenamiento = event.value)
                    "puertos" -> current.copy(puertos = event.value)
                    "conectividad" -> current.copy(conectividad = event.value)
                    "tipoRam2" -> current.copy(tipoRam2 = event.value)
                    "capacidadTotal" -> current.copy(capacidadTotal = event.value)
                    "configuracion" -> current.copy(configuracion = event.value)
                    "velocidad" -> current.copy(velocidad = event.value)
                    "latencia" -> current.copy(latencia = event.value)
                    "voltaje" -> current.copy(voltaje = event.value)
                    "perfil" -> current.copy(perfil = event.value)
                    "potenciaWatts" -> current.copy(potenciaWatts = event.value)
                    "certificacion" -> current.copy(certificacion = event.value)
                    "tipoModular" -> current.copy(tipoModular = event.value)
                    "eficiencia" -> current.copy(eficiencia = event.value)
                    "ventilador" -> current.copy(ventilador = event.value)
                    "protecciones" -> current.copy(protecciones = event.value)
                    "conectores" -> current.copy(conectores = event.value)
                    else -> current
                }
                _uiState.update { it.copy(formState = updated) }
            }
            is AdminEvent.OnFieldChange -> {
                onEvent(AdminEvent.OnFormFieldChange(event.field, event.value))
            }
            AdminEvent.OnResetForm ->
                _uiState.update {
                    it.copy(formState = ComponentFormState(), navigateBack = false, selectedComponent = null, selectedImageUri = null)
                }
        }
    }

    internal fun buildComponentFromForm(): Component? {
        val f = _uiState.value.formState
        val precio = f.precioUsd.toDoubleOrNull()
            ?: return null
        if (f.nombre.isBlank() || f.marca.isBlank())
            return null
        
        val originalId = _uiState.value.selectedComponent?.id ?: 0
        
        return when (f.tipo) {
            "CPU" -> Component.CPU(
                id = originalId, name = f.nombre, brand = f.marca,
                socket = f.socket, generation = f.generacion,
                cores = f.nucleos.toIntOrNull() ?: 0,
                threads = f.hilos.toIntOrNull() ?: 0,
                baseClock = f.frecuenciaBase.withSuffixSpaced("GHz"),
                boostClock = f.frecuenciaTurbo.withSuffixSpaced("GHz"),
                cache = f.cacheL3.withSuffix("MB").ifBlank { null },
                tdp = "${f.tdpWatts}W",
                integratedGraphics = f.graficosIntegrados.ifBlank { null },
                ramSupport = f.soporteRam.ifBlank { null },
                price = precio, description = f.descripcion,
                imageUrl = f.imageUrl.ifBlank { null },
                category = "Procesador"
            )
            "GPU" -> Component.GPU(
                id = originalId, name = f.nombre, brand = f.marca,
                chipset = f.chipset, vram = f.vram.withSuffix("GB"),
                vramType = f.tipoVram,
                memoryBus = f.busMemoria.withSuffix("-bit").ifBlank { null },
                baseClock = f.frecuenciaBase.withSuffixSpaced("GHz").ifBlank { null },
                boostClock = f.frecuenciaBoost.withSuffixSpaced("GHz").ifBlank { null },
                consumptionWatts = "${f.consumoWatts}W",
                recommendedPSU = f.fuenteRecomendada.withSuffix("W").ifBlank { null },
                pcieInterface = f.versionPcie.ifBlank { null },
                price = precio, description = f.descripcion,
                imageUrl = f.imageUrl.ifBlank { null },
                category = "Tarjeta Gráfica"
            )
            "Motherboard" -> Component.Motherboard(
                id = originalId, name = f.nombre, brand = f.marca,
                socket = f.socket, chipset = f.chipsetMobo,
                format = f.formato, ramType = f.tipoRam,
                maxRamSpeed = f.velocidadRamMax.withSuffixSpaced("MHz").ifBlank { null },
                ramSlots = f.slotsRam.toIntOrNull(),
                maxRamCapacity = null, slotsM2 = 0,
                price = precio, description = f.descripcion,
                imageUrl = f.imageUrl.ifBlank { null },
                category = "Placa Base"
            )
            "RAM" -> Component.RAM(
                id = originalId, name = f.nombre, brand = f.marca,
                type = f.tipoRam2, capacity = f.capacidadTotal,
                speed = f.velocidad.withSuffixSpaced("MHz"), 
                latency = f.latencia,
                voltage = f.voltaje.withSuffix("V").ifBlank { null },
                hasRGB = f.nombre.contains("RGB", ignoreCase = true),
                price = precio, description = f.descripcion,
                imageUrl = f.imageUrl.ifBlank { null },
                category = "Memoria RAM",
                configuration = f.configuracion
            )
            "PSU" -> Component.PSU(
                id = originalId, name = f.nombre, brand = f.marca,
                wattage = f.potenciaWatts.toIntOrNull() ?: 0,
                certification = f.certificacion,
                modularity = f.tipoModular,
                fanSize = f.ventilador.ifBlank { null },
                protection = f.protecciones.ifBlank { null },
                price = precio, description = f.descripcion,
                imageUrl = f.imageUrl.ifBlank { null },
                category = "Fuente de Poder"
            )
            else -> null
        }
    }

    private fun uploadImage() {
        val uriStr = _uiState.value.selectedImageUri ?: return
        if (uriStr.startsWith("http")) return

        viewModelScope.launch {
            _uiState.update { it.copy(isUploadingImage = true) }
            imageUploader.uploadImage(context, Uri.parse(uriStr))
                .fold(
                    onSuccess = { url ->
                        _uiState.update {
                            it.copy(
                                isUploadingImage = false,
                                selectedImageUri = url,
                                formState = it.formState.copy(imageUrl = url),
                                successMessage = "Imagen lista para guardar"
                            )
                        }
                    },
                    onFailure = { e ->
                        _uiState.update {
                            it.copy(
                                isUploadingImage = false,
                                errorMessage = "Error subiendo imagen: ${e.message}"
                            )
                        }
                    }
                )
        }
    }

    private fun createComponent(component: Component) {
        if (!networkManager.isOnline()) {
            _uiState.update { it.copy(errorMessage = "Sin conexión") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            componentRepository.addComponent(component)
                .fold(
                    onSuccess = {
                        adminLogRepository.addLog(
                            AdminLog(
                                userEmail = currentUserEmail,
                                action = "CREATE",
                                componentName = component.name,
                                componentType = component.category
                            )
                        )
                        _uiState.update {
                            it.copy(
                                isSaving = false,
                                successMessage = "Componente guardado",
                                showCreateDialog = false,
                                selectedImageUri = null,
                                navigateBack = true,
                                formState = ComponentFormState()
                            )
                        }
                        loadComponents()
                    },
                    onFailure = { e ->
                        _uiState.update { it.copy(isSaving = false, errorMessage = e.message) }
                    }
                )
        }
    }

    private fun updateComponent(component: Component) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            val componentWithImage = component.withImageUrl(_uiState.value.selectedImageUri)
            componentRepository.updateComponent(componentWithImage)
                .fold(
                    onSuccess = {
                        adminLogRepository.addLog(
                            AdminLog(userEmail = currentUserEmail, action = "UPDATE", componentName = component.name, componentType = component.category)
                        )
                        _uiState.update {
                            it.copy(
                                isSaving = false, 
                                successMessage = "Actualizado", 
                                showEditDialog = false,
                                navigateBack = true,
                                formState = ComponentFormState(),
                                selectedComponent = null
                            )
                        }
                        loadComponents()
                    },
                    onFailure = { e ->
                        _uiState.update { it.copy(isSaving = false, errorMessage = e.message) }
                    }
                )
        }
    }

    private fun deleteComponent(id: Int, type: String) {
        viewModelScope.launch {
            componentRepository.deleteComponent(id, type)
                .fold(
                    onSuccess = {
                        _uiState.update { it.copy(successMessage = "Eliminado") }
                        loadComponents()
                    },
                    onFailure = { e ->
                        _uiState.update { it.copy(errorMessage = e.message) }
                    }
                )
        }
    }
}
