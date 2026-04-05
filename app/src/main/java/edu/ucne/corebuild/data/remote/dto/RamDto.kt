package edu.ucne.corebuild.data.remote.dto

import edu.ucne.corebuild.domain.model.Component

data class RamDto(
    val id: Int,
    val nombre: String? = null,
    val marca: String? = null,
    val tipo: String? = null,
    val capacidadTotal: String? = null,
    val configuracion: String? = null,
    val velocidad: String? = null,
    val latencia: String? = null,
    val voltaje: String? = null,
    val precioUsd: Double? = null,
    val descripcion: String? = null,
    val imageUrl: String? = null
)

fun RamDto.toDomain(idOverride: Int? = null): Component.RAM {
    return Component.RAM(
        id = idOverride ?: id,
        name = nombre ?: "Memoria RAM Desconocida",
        brand = marca ?: "",
        type = tipo ?: "",
        capacity = capacidadTotal ?: "",
        configuration = configuracion ?: "",
        speed = velocidad ?: "",
        latency = latencia ?: "",
        voltage = voltaje,
        hasRGB = nombre?.contains("RGB", ignoreCase = true) == true,
        price = precioUsd ?: 0.0,
        description = descripcion ?: "",
        imageUrl = imageUrl
    )
}
