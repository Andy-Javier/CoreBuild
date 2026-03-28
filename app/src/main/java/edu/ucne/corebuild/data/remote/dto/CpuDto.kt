package edu.ucne.corebuild.data.remote.dto

import com.squareup.moshi.Json
import edu.ucne.corebuild.domain.model.Component

data class CpuDto(
    val id: Int,
    val nombre: String,
    val marca: String,
    val socket: String,
    val generacion: String,
    val nucleos: Int,
    val hilos: Int,
    val frecuenciaBase: String,
    val frecuenciaTurbo: String,
    val cacheL3: String,
    val tdpWatts: Int,
    val graficosIntegrados: String?,
    val soporteRam: String,
    val precioUsd: Double,
    val descripcion: String
)

fun CpuDto.toDomain(): Component.CPU {
    return Component.CPU(
        id = id,
        name = nombre,
        brand = marca,
        socket = socket,
        generation = generacion,
        cores = nucleos,
        threads = hilos,
        baseClock = frecuenciaBase,
        boostClock = frecuenciaTurbo,
        cache = cacheL3,
        tdp = tdpWatts.toString(),
        integratedGraphics = graficosIntegrados,
        price = precioUsd,
        description = descripcion
    )
}
