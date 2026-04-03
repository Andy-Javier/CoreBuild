package edu.ucne.corebuild.data.remote.dto

import edu.ucne.corebuild.domain.model.Component

data class CpuDto(
    val id: Int,
    val nombre: String? = null,
    val marca: String? = null,
    val socket: String? = null,
    val generacion: String? = null,
    val nucleos: Int? = null,
    val hilos: Int? = null,
    val frecuenciaBase: String? = null,
    val frecuenciaTurbo: String? = null,
    val cacheL3: String? = null,
    val tdpWatts: Int? = null,
    val graficosIntegrados: String? = null,
    val soporteRam: String? = null,
    val precioUsd: Double? = null,
    val descripcion: String? = null,
    val imageUrl: String? = null
)

fun CpuDto.toDomain(idOverride: Int? = null): Component.CPU {
    return Component.CPU(
        id = idOverride ?: id,
        name = nombre ?: "CPU Desconocido",
        brand = marca ?: "",
        socket = socket ?: "",
        generation = generacion ?: "",
        cores = nucleos ?: 0,
        threads = hilos ?: 0,
        baseClock = frecuenciaBase ?: "",
        boostClock = frecuenciaTurbo ?: "",
        cache = cacheL3,
        tdp = "${tdpWatts ?: 0}W",
        integratedGraphics = graficosIntegrados,
        ramSupport = soporteRam,
        price = precioUsd ?: 0.0,
        description = descripcion ?: "",
        imageUrl = imageUrl
    )
}
